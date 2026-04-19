package com.example.smartalloc.strategy;

import com.example.smartalloc.model.Task;
import java.util.List;

public interface SchedulingStrategy {

    List<Task> sort(List<Task> tasks);
}
