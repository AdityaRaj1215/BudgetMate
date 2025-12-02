package com.personalfin.server.budget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "budget")
public class BudgetProperties {

    private Coach coach = new Coach();

    public Coach getCoach() {
        return coach;
    }

    public void setCoach(Coach coach) {
        this.coach = coach;
    }

    public static class Coach {
        private boolean enabled = true;
        private double overspendThreshold = 1.0; // Warn when spending exceeds 100% of daily limit

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public double getOverspendThreshold() {
            return overspendThreshold;
        }

        public void setOverspendThreshold(double overspendThreshold) {
            this.overspendThreshold = overspendThreshold;
        }
    }
}










