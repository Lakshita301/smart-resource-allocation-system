package com.example.smartalloc.strategy;

public class StrategyFactory {

    private final FCFSStrategy fcfs;
    private final PriorityStrategy priority;
    private final SJFStrategy sjf;

    public StrategyFactory() {
        this.fcfs = new FCFSStrategy();
        this.priority = new PriorityStrategy();
        this.sjf = new SJFStrategy();
    }

    public SchedulingStrategy getStrategy(String type) {
        return switch (type) {
            case "PRIORITY" -> priority;
            case "SJF" -> sjf;
            default -> fcfs;
        };
    }
}
