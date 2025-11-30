package com.personalfin.server.expense.service;

import com.personalfin.server.expense.config.ExpenseCategorizerProperties;
import com.personalfin.server.expense.dto.ExpenseCategorizationRequest;
import com.personalfin.server.expense.dto.ExpenseCategorizationResponse;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class RuleBasedExpenseCategorizer implements ExpenseCategorizer {

    private final ExpenseCategorizerProperties properties;
    private final Map<String, String> keywordCache = new ConcurrentHashMap<>();

    public RuleBasedExpenseCategorizer(ExpenseCategorizerProperties properties) {
        this.properties = properties;
        preloadKeywords();
    }

    private void preloadKeywords() {
        properties.getCategories().forEach((category, keywords) -> {
            if (keywords == null) {
                return;
            }
            keywords.stream()
                    .filter(StringUtils::hasText)
                    .forEach(keyword ->
                            keywordCache.put(keyword.trim().toLowerCase(Locale.ROOT), category));
        });
    }

    @Override
    public ExpenseCategorizationResponse categorize(ExpenseCategorizationRequest request) {
        String searchSpace = buildSearchSpace(request);

        // First try exact match
        Optional<Map.Entry<String, String>> exactMatch = keywordCache.entrySet()
                .stream()
                .filter(entry -> searchSpace.contains(entry.getKey()))
                .findFirst();

        if (exactMatch.isPresent()) {
            String category = exactMatch.get().getValue();
            return new ExpenseCategorizationResponse(
                    category,
                    properties.getMatchConfidence(),
                    exactMatch.get().getKey(),
                    buildSuggestions(category));
        }

        // Try fuzzy matching for typos
        Optional<Map.Entry<String, String>> fuzzyMatch = keywordCache.entrySet()
                .stream()
                .filter(entry -> fuzzyMatch(searchSpace, entry.getKey()))
                .findFirst();

        if (fuzzyMatch.isPresent()) {
            String category = fuzzyMatch.get().getValue();
            // Lower confidence for fuzzy matches
            double fuzzyConfidence = properties.getMatchConfidence() * 0.7;
            return new ExpenseCategorizationResponse(
                    category,
                    fuzzyConfidence,
                    fuzzyMatch.get().getKey(),
                    buildSuggestions(category));
        }

        return new ExpenseCategorizationResponse(
                properties.getDefaultCategory(),
                properties.getFallbackConfidence(),
                null,
                buildSuggestions(properties.getDefaultCategory()));
    }

    private boolean fuzzyMatch(String text, String keyword) {
        // Simple fuzzy matching: check if keyword is a substring with 1-2 character differences
        if (text.contains(keyword)) {
            return true;
        }

        // Check for common typos (single character difference)
        if (keyword.length() > 3) {
            for (int i = 0; i <= text.length() - keyword.length(); i++) {
                String substring = text.substring(i, Math.min(i + keyword.length(), text.length()));
                if (levenshteinDistance(substring, keyword) <= 1) {
                    return true;
                }
            }
        }

        return false;
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];

        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                            Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1),
                            dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1)
                    );
                }
            }
        }

        return dp[s1.length()][s2.length()];
    }

    private String buildSearchSpace(ExpenseCategorizationRequest request) {
        StringBuilder builder = new StringBuilder(request.description().toLowerCase(Locale.ROOT));
        if (StringUtils.hasText(request.normalizedMerchant())) {
            builder.append(" ").append(request.normalizedMerchant().toLowerCase(Locale.ROOT));
        }
        return builder.toString();
    }

    private Map<String, Double> buildSuggestions(String primaryCategory) {
        Map<String, Double> suggestions = new LinkedHashMap<>();
        suggestions.put(primaryCategory, properties.getMatchConfidence());
        properties.getCategories().keySet().stream()
                .filter(category -> !category.equals(primaryCategory))
                .forEach(category -> suggestions.put(category, properties.getFallbackConfidence()));
        return suggestions;
    }
}

