package com.personalfin.server.reminder.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "reminder")
public class ReminderProperties {

    private final Scheduler scheduler = new Scheduler();
    private final Notification notification = new Notification();

    public Scheduler getScheduler() {
        return scheduler;
    }

    public Notification getNotification() {
        return notification;
    }

    public static class Scheduler {
        private String cron = "0 0 6 * * *";
        private String zoneId = "Asia/Kolkata";

        public String getCron() {
            return cron;
        }

        public void setCron(String cron) {
            this.cron = cron;
        }

        public String getZoneId() {
            return zoneId;
        }

        public void setZoneId(String zoneId) {
            this.zoneId = zoneId;
        }
    }

    public static class Notification {
        private int daysBefore = 3;

        public int getDaysBefore() {
            return daysBefore;
        }

        public void setDaysBefore(int daysBefore) {
            this.daysBefore = daysBefore;
        }
    }
}

