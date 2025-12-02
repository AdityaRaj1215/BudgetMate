package com.personalfin.server.expense.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.user.service.UserService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

@Service
public class ExpensePdfExportService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;

    public ExpensePdfExportService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    /**
     * Generate a PDF of the current user's expenses between the given dates (inclusive).
     * If start or end is null, all expenses for the user are included.
     */
    public byte[] exportExpenses(LocalDate start, LocalDate end) {
        UUID userId = SecurityUtils.getCurrentUserId(userService);
        if (userId == null) {
            throw new IllegalStateException("User not authenticated");
        }

        List<Expense> expenses = expenseRepository.findByUserId(userId)
                .stream()
                .filter(e -> {
                    LocalDate d = e.getTransactionDate();
                    boolean afterStart = (start == null) || !d.isBefore(start);
                    boolean beforeEnd = (end == null) || !d.isAfter(end);
                    return afterStart && beforeEnd;
                })
                .sorted(Comparator.comparing(Expense::getTransactionDate).thenComparing(Expense::getCreatedAt))
                .toList();

        return generatePdf(expenses, start, end);
    }

    private byte[] generatePdf(List<Expense> expenses, LocalDate start, LocalDate end) {
        try (PDDocument document = new PDDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            PDPageContentStream contentStream = new PDPageContentStream(document, page);

            float margin = 50;
            float yStart = page.getMediaBox().getHeight() - margin;
            float yPosition = yStart;
            float leading = 16;

            // Header
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 18);
            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Transaction History");
            contentStream.endText();
            yPosition -= leading * 2;

            // Date range info
            contentStream.setFont(PDType1Font.HELVETICA, 11);
            DateTimeFormatter df = DateTimeFormatter.ISO_DATE;
            String rangeText;
            if (start != null || end != null) {
                String startStr = (start != null) ? df.format(start) : "...";
                String endStr = (end != null) ? df.format(end) : "...";
                rangeText = "Period: " + startStr + " to " + endStr;
            } else {
                rangeText = "Period: All transactions";
            }

            contentStream.beginText();
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText(rangeText);
            contentStream.endText();
            yPosition -= leading * 2;

            // Column headers
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            writeRow(contentStream, margin, yPosition,
                    "Date", "Description", "Category", "Amount", "Payment");
            yPosition -= leading;

            contentStream.setFont(PDType1Font.HELVETICA, 10);
            BigDecimal total = BigDecimal.ZERO;

            for (Expense e : expenses) {
                if (yPosition < margin + leading * 2) {
                    contentStream.close();
                    page = new PDPage(PDRectangle.A4);
                    document.addPage(page);
                    contentStream = new PDPageContentStream(document, page);
                    yPosition = yStart;

                    // Re-write column headers on new page
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
                    writeRow(contentStream, margin, yPosition,
                            "Date", "Description", "Category", "Amount", "Payment");
                    yPosition -= leading;
                    contentStream.setFont(PDType1Font.HELVETICA, 10);
                }

                String dateStr = e.getTransactionDate() != null ? df.format(e.getTransactionDate()) : "";
                String desc = safe(e.getDescription());
                String category = safe(e.getCategory());
                String amountStr = e.getAmount() != null ? e.getAmount().toPlainString() : "";
                String payment = safe(e.getPaymentMethod());

                writeRow(contentStream, margin, yPosition,
                        dateStr, desc, category, amountStr, payment);
                yPosition -= leading;

                if (e.getAmount() != null) {
                    total = total.add(e.getAmount());
                }
            }

            // Total row
            yPosition -= leading;
            if (yPosition < margin + leading) {
                contentStream.close();
                page = new PDPage(PDRectangle.A4);
                document.addPage(page);
                contentStream = new PDPageContentStream(document, page);
                yPosition = yStart;
            }

            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11);
            writeRow(contentStream, margin, yPosition,
                    "", "", "Total", total.toPlainString(), "");

            contentStream.close();

            document.save(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate expenses PDF", e);
        }
    }

    private void writeRow(PDPageContentStream contentStream,
                          float margin,
                          float y,
                          String date,
                          String description,
                          String category,
                          String amount,
                          String payment) throws IOException {

        float x = margin;
        float[] colWidths = new float[]{70, 210, 90, 70, 70};

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(truncate(date, 12));
        contentStream.endText();
        x += colWidths[0];

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(truncate(description, 40));
        contentStream.endText();
        x += colWidths[1];

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(truncate(category, 18));
        contentStream.endText();
        x += colWidths[2];

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(truncate(amount, 14));
        contentStream.endText();
        x += colWidths[3];

        contentStream.beginText();
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(truncate(payment, 14));
        contentStream.endText();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String truncate(String value, int maxLen) {
        if (value == null) {
            return "";
        }
        return value.length() <= maxLen ? value : value.substring(0, maxLen - 1) + "…";
    }
}



