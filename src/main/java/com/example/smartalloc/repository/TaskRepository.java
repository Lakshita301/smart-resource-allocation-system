package com.example.smartalloc.repository;

import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TaskRepository {

    private final Map<Long, Task> tasks = new LinkedHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    public synchronized Task save(Task task) {
        if (task.getId() == null) {
            task.setId(sequence.getAndIncrement());
        }

        tasks.put(task.getId(), task);
        return task;
    }

    public synchronized List<Task> findAll() {
        return new ArrayList<>(tasks.values());
    }

    public synchronized List<Task> findByStatus(TaskStatus status) {
        return tasks.values()
                .stream()
                .filter(task -> task.getStatus() == status)
                .collect(Collectors.toList());
    }
}
