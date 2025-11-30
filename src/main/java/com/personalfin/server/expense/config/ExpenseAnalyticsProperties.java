package com.personalfin.server.expense.config;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "expense.analytics")
public class ExpenseAnalyticsProperties {

    private final Heatmap heatmap = new Heatmap();

    public Heatmap getHeatmap() {
        return heatmap;
    }

    public static class Heatmap {
        private List<BigDecimal> thresholds = new ArrayList<>(List.of(
                new BigDecimal("500"),
                new BigDecimal("1500"),
                new BigDecimal("3000"),
                new BigDecimal("6000")
        ));

        public List<BigDecimal> getThresholds() {
            return thresholds;
        }

        public void setThresholds(List<BigDecimal> thresholds) {
            this.thresholds = thresholds;
        }
    }
}

