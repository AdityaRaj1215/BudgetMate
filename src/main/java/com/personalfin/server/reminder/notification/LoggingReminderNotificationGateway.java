package com.personalfin.server.reminder.notification;

import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingReminderNotificationGateway implements ReminderNotificationGateway {

    private static final Logger log = LoggerFactory.getLogger(LoggingReminderNotificationGateway.class);

    @Override
    public void notify(Bill bill, ReminderType type, String message) {
        log.info("Reminder [{}] for bill {} scheduled on {} :: {}",
                type, bill.getName(), bill.getNextDueDate(), message);
    }
}

