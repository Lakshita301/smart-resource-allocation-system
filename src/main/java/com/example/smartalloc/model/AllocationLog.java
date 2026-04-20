package com.example.smartalloc.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AllocationLog {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String timestamp;
    private final String message;

    public AllocationLog(String message) {
        this.timestamp = LocalDateTime.now().format(FORMATTER);
        this.message = message;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }
}
