package com.personalfin.server.expense.service;

import com.personalfin.server.auth.util.SecurityUtils;
import com.personalfin.server.expense.model.Expense;
import com.personalfin.server.expense.repository.ExpenseRepository;
import com.personalfin.server.user.service.UserService;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ExpenseCsvExportService {

    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;

    public ExpenseCsvExportService(ExpenseRepository expenseRepository, UserService userService) {
        this.expenseRepository = expenseRepository;
        this.userService = userService;
    }

    /**
     * Generate a CSV of the current user's expenses between the given dates (inclusive).
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
                .sorted(Comparator.comparing(Expense::getTransactionDate)
                        .thenComparing(Expense::getCreatedAt))
                .toList();

        return generateCsv(expenses);
    }

    private byte[] generateCsv(List<Expense> expenses) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             PrintWriter printWriter = new PrintWriter(writer)) {

            // Write UTF-8 BOM for Excel compatibility
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            // Write CSV header
            printWriter.println("Date,Description,Merchant,Category,Amount,Payment Method");

            // Write expense rows
            BigDecimal total = BigDecimal.ZERO;
            for (Expense expense : expenses) {
                String date = expense.getTransactionDate() != null
                        ? DATE_FORMATTER.format(expense.getTransactionDate())
                        : "";
                String description = escapeCsvField(expense.getDescription());
                String merchant = escapeCsvField(expense.getMerchant());
                String category = escapeCsvField(expense.getCategory());
                String amount = expense.getAmount() != null
                        ? expense.getAmount().toPlainString()
                        : "0.00";
                String paymentMethod = escapeCsvField(expense.getPaymentMethod());

                printWriter.printf("%s,%s,%s,%s,%s,%s%n",
                        date, description, merchant, category, amount, paymentMethod);

                if (expense.getAmount() != null) {
                    total = total.add(expense.getAmount());
                }
            }

            // Write total row
            printWriter.printf("%s,%s,%s,%s,%s,%s%n",
                    "", "", "", "Total", total.toPlainString(), "");

            printWriter.flush();
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate expenses CSV", e);
        }
    }

    /**
     * Escape CSV field values that contain commas, quotes, or newlines.
     * Wraps the field in double quotes and escapes any existing quotes.
     */
    private String escapeCsvField(String value) {
        if (value == null) {
            return "";
        }

        // If the value contains comma, quote, or newline, wrap it in quotes
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            // Escape existing quotes by doubling them
            String escaped = value.replace("\"", "\"\"");
            return "\"" + escaped + "\"";
        }

        return value;
    }
}

