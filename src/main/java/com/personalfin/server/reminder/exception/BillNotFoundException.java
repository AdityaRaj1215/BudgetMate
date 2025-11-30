package com.personalfin.server.reminder.exception;

import java.util.UUID;

public class BillNotFoundException extends RuntimeException {
    public BillNotFoundException(UUID id) {
        super("Bill not found: " + id);
    }
}

