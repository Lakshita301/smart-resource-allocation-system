package com.example.smartalloc.strategy;

import com.example.smartalloc.model.Task;
import java.util.List;

public class FCFSStrategy implements SchedulingStrategy {

    @Override
    public List<Task> sort(List<Task> tasks) {
        return tasks;
    }
}
