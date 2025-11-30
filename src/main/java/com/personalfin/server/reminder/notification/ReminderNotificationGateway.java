package com.personalfin.server.reminder.notification;

import com.personalfin.server.reminder.model.Bill;
import com.personalfin.server.reminder.model.ReminderType;

public interface ReminderNotificationGateway {

    void notify(Bill bill, ReminderType type, String message);
}

