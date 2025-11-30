package com.personalfin.server.expense.config;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "expense.categorizer")
public class ExpenseCategorizerProperties {

    private String defaultCategory = "Uncategorized";
    private double matchConfidence = 0.9;
    private double fallbackConfidence = 0.3;
    private Map<String, List<String>> categories = new LinkedHashMap<>();

    public String getDefaultCategory() {
        return defaultCategory;
    }

    public void setDefaultCategory(String defaultCategory) {
        this.defaultCategory = defaultCategory;
    }

    public double getMatchConfidence() {
        return matchConfidence;
    }

    public void setMatchConfidence(double matchConfidence) {
        this.matchConfidence = matchConfidence;
    }

    public double getFallbackConfidence() {
        return fallbackConfidence;
    }

    public void setFallbackConfidence(double fallbackConfidence) {
        this.fallbackConfidence = fallbackConfidence;
    }

    public Map<String, List<String>> getCategories() {
        return categories;
    }

    public void setCategories(Map<String, List<String>> categories) {
        this.categories = categories;
    }
}

