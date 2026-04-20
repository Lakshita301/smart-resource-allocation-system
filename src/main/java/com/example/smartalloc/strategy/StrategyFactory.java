package com.example.smartalloc.strategy;

import java.util.HashMap;
import java.util.Map;

public class StrategyFactory {

    private final Map<String, SchedulingStrategy> strategies = new HashMap<>();

    public StrategyFactory() {
        strategies.put("FCFS", new FCFSStrategy());
        strategies.put("PRIORITY", new PriorityStrategy());
        strategies.put("SJF", new SJFStrategy());
    }

    public SchedulingStrategy getStrategy(String type) {
        return strategies.getOrDefault(type, strategies.get("FCFS"));
    }
}
