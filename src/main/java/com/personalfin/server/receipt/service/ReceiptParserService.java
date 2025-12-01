package com.personalfin.server.receipt.service;

import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.service.ExpenseCategorizer;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ReceiptParserService {

    private final ExpenseCategorizer expenseCategorizer;

    // Common patterns for amount extraction
    private static final Pattern AMOUNT_PATTERN = Pattern.compile(
            "(?:total|amount|amt|rs\\.?|inr|₹)\\s*:?\\s*([0-9,]+(?:\\.[0-9]{2})?)",
            Pattern.CASE_INSENSITIVE
    );

    // Patterns for date extraction
    private static final Pattern DATE_PATTERN = Pattern.compile(
            "\\b(\\d{1,2}[/-]\\d{1,2}[/-]\\d{2,4})\\b"
    );

    // Patterns for merchant/store name (usually at the top)
    private static final Pattern MERCHANT_PATTERN = Pattern.compile(
            "^([A-Z][A-Z\\s&]+(?:PVT|LTD|INC|LLC)?)",
            Pattern.MULTILINE
    );

    public ReceiptParserService(ExpenseCategorizer expenseCategorizer) {
        this.expenseCategorizer = expenseCategorizer;
    }

    public ReceiptParsedData parse(String ocrText) {
        BigDecimal amount = extractAmount(ocrText);
        String merchant = extractMerchant(ocrText);
        LocalDate date = extractDate(ocrText);
        String category = extractCategory(ocrText, merchant);

        return new ReceiptParsedData(amount, merchant, date, category, ocrText);
    }

    private BigDecimal extractAmount(String text) {
        // Try to find amount patterns
        Matcher matcher = AMOUNT_PATTERN.matcher(text);
        BigDecimal maxAmount = BigDecimal.ZERO;

        while (matcher.find()) {
            String amountStr = matcher.group(1).replace(",", "");
            try {
                BigDecimal amount = new BigDecimal(amountStr);
                if (amount.compareTo(maxAmount) > 0) {
                    maxAmount = amount;
                }
            } catch (NumberFormatException e) {
                // Continue searching
            }
        }

        // If no pattern found, try to find any large number that looks like an amount
        if (maxAmount.compareTo(BigDecimal.ZERO) == 0) {
            Pattern numberPattern = Pattern.compile("\\b([0-9]{3,}(?:\\.[0-9]{2})?)\\b");
            Matcher numberMatcher = numberPattern.matcher(text);
            while (numberMatcher.find()) {
                try {
                    BigDecimal amount = new BigDecimal(numberMatcher.group(1));
                    // Assume amounts are typically between 10 and 1,000,000
                    if (amount.compareTo(BigDecimal.valueOf(10)) >= 0
                            && amount.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                        if (amount.compareTo(maxAmount) > 0) {
                            maxAmount = amount;
                        }
                    }
                } catch (NumberFormatException e) {
                    // Continue
                }
            }
        }

        return maxAmount.setScale(2, RoundingMode.HALF_UP);
    }

    private String extractMerchant(String text) {
        // Try to find merchant name at the beginning
        Matcher matcher = MERCHANT_PATTERN.matcher(text);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }

        // Fallback: take first line that looks like a store name
        String[] lines = text.split("\n");
        for (String line : lines) {
            line = line.trim();
            if (line.length() > 3 && line.length() < 50 && !line.matches(".*\\d{4,}.*")) {
                return line;
            }
        }

        return null;
    }

    private LocalDate extractDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (matcher.find()) {
            String dateStr = matcher.group(1);
            // Try different date formats
            String[] formats = {
                    "dd/MM/yyyy", "dd-MM-yyyy", "dd/MM/yy", "dd-MM-yy",
                    "MM/dd/yyyy", "MM-dd-yyyy", "yyyy-MM-dd"
            };

            for (String format : formats) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                    LocalDate date = LocalDate.parse(dateStr, formatter);
                    // Sanity check: date should be within last 5 years and not in future
                    LocalDate fiveYearsAgo = LocalDate.now().minusYears(5);
                    LocalDate tomorrow = LocalDate.now().plusDays(1);
                    if (date.isAfter(fiveYearsAgo) && date.isBefore(tomorrow)) {
                        return date;
                    }
                } catch (DateTimeParseException e) {
                    // Try next format
                }
            }
        }

        // Default to today if no date found
        return LocalDate.now();
    }

    private String extractCategory(String text, String merchant) {
        ExpenseCategorizationRequest request = new ExpenseCategorizationRequest(
                text,
                merchant,
                BigDecimal.ZERO
        );
        return expenseCategorizer.categorize(request).category();
    }

    public static class ReceiptParsedData {
        private final BigDecimal amount;
        private final String merchant;
        private final LocalDate date;
        private final String category;
        private final String rawText;

        public ReceiptParsedData(BigDecimal amount, String merchant, LocalDate date,
                                String category, String rawText) {
            this.amount = amount;
            this.merchant = merchant;
            this.date = date;
            this.category = category;
            this.rawText = rawText;
        }

        public BigDecimal getAmount() {
            return amount;
        }

        public String getMerchant() {
            return merchant;
        }

        public LocalDate getDate() {
            return date;
        }

        public String getCategory() {
            return category;
        }

        public String getRawText() {
            return rawText;
        }
    }
}









