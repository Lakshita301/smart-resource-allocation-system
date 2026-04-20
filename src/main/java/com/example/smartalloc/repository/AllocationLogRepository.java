package com.example.smartalloc.repository;

import com.example.smartalloc.model.AllocationLog;
import java.util.ArrayList;
import java.util.List;

public class AllocationLogRepository {

    private final List<AllocationLog> logs = new ArrayList<>();

    public synchronized void add(String message) {
        logs.add(0, new AllocationLog(message));
    }

    public synchronized List<AllocationLog> findAll() {
        return new ArrayList<>(logs);
    }
}
