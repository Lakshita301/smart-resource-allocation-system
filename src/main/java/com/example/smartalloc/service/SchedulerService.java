package com.example.smartalloc.service;

import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import com.example.smartalloc.repository.TaskRepository;
import com.example.smartalloc.strategy.SchedulingStrategy;
import com.example.smartalloc.strategy.StrategyFactory;
import java.util.List;

public class SchedulerService {

    private final TaskRepository repo;
    private final ResourcePool pool;
    private final StrategyFactory factory;

    private String currentStrategy = "FCFS";

    public SchedulerService(TaskRepository repo, ResourcePool pool, StrategyFactory factory) {
        this.repo = repo;
        this.pool = pool;
        this.factory = factory;
    }

    public void setStrategy(String strategy) {
        this.currentStrategy = strategy;
    }

    public String getCurrentStrategy() {
        return currentStrategy;
    }

    public void runScheduler() {
        List<Task> tasks = repo.findByStatus(TaskStatus.PENDING);
        SchedulingStrategy strategy = factory.getStrategy(currentStrategy);

        for (Task task : strategy.sort(tasks)) {
            if (pool.allocate(task.getCpu(), task.getMemory())) {
                task.setStatus(TaskStatus.RUNNING);
                repo.save(task);
                new Thread(() -> execute(task)).start();
            }
        }
    }

    private void execute(Task task) {
        try {
            Thread.sleep(task.getExecutionTime() * 1000L);
            task.setStatus(TaskStatus.COMPLETED);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(TaskStatus.CANCELLED);
        } finally {
            pool.release(task.getCpu(), task.getMemory());
            repo.save(task);
        }
    }
}
