package com.example.smartalloc.strategy;

import com.example.smartalloc.model.Task;
import java.util.Comparator;
import java.util.List;

public class SJFStrategy implements SchedulingStrategy {

    @Override
    public List<Task> sort(List<Task> tasks) {
        tasks.sort(Comparator.comparingInt(Task::getExecutionTime));
        return tasks;
    }
}
