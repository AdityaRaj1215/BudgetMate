package com.personalfin.server.reminder.web;

import com.personalfin.server.reminder.exception.BillNotFoundException;
import java.time.Instant;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ReminderExceptionHandler {

    @ExceptionHandler(BillNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleBillNotFound(BillNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of(
                        "timestamp", Instant.now(),
                        "error", "NOT_FOUND",
                        "message", ex.getMessage()
                ));
    }
}

