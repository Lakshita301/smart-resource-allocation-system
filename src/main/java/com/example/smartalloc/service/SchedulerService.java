package com.example.smartalloc.service;

import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import com.example.smartalloc.repository.AllocationLogRepository;
import com.example.smartalloc.repository.TaskRepository;
import com.example.smartalloc.strategy.SchedulingStrategy;
import com.example.smartalloc.strategy.StrategyFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class SchedulerService {

    private final TaskRepository repo;
    private final ResourcePool pool;
    private final StrategyFactory factory;
    private final AllocationLogRepository logs;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    private String currentStrategy = "FCFS";

    public SchedulerService(TaskRepository repo, ResourcePool pool, StrategyFactory factory, AllocationLogRepository logs) {
        this.repo = repo;
        this.pool = pool;
        this.factory = factory;
        this.logs = logs;
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
                logs.add("Task #" + task.getId() + " allocated CPU " + task.getCpu() + " and Memory " + task.getMemory() + ".");
                executor.submit(() -> execute(task));
            }
        }
    }

    private void execute(Task task) {
        try {
            Thread.sleep(task.getExecutionTime() * 1000L);
            if (shouldFail(task)) {
                task.setStatus(TaskStatus.FAILED);
                logs.add("Task #" + task.getId() + " failed during execution.");
            } else {
                task.setStatus(TaskStatus.COMPLETED);
                logs.add("Task #" + task.getId() + " completed successfully.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            task.setStatus(TaskStatus.CANCELLED);
            logs.add("Task #" + task.getId() + " was cancelled during execution.");
        } finally {
            pool.release(task.getCpu(), task.getMemory());
            repo.save(task);
        }
    }

    private boolean shouldFail(Task task) {
        return task.getExecutionTime() % 13 == 0;
    }
}
