package com.personalfin.server.reminder.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "reminder_notifications")
public class ReminderNotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "bill_id")
    private Bill bill;

    @Column(name = "notification_date", nullable = false)
    private LocalDate notificationDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 20)
    private ReminderType type;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false, length = 32)
    private String channel = "LOG";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now(ZoneOffset.UTC);

    public ReminderNotificationLog() {
    }

    public ReminderNotificationLog(Bill bill, LocalDate notificationDate, ReminderType type, String message, String channel) {
        this.bill = bill;
        this.notificationDate = notificationDate;
        this.type = type;
        this.message = message;
        this.channel = channel;
    }

    public UUID getId() {
        return id;
    }

    public Bill getBill() {
        return bill;
    }

    public LocalDate getNotificationDate() {
        return notificationDate;
    }

    public ReminderType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public String getChannel() {
        return channel;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}

