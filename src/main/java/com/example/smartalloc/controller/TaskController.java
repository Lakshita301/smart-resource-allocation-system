package com.example.smartalloc.controller;

import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import com.example.smartalloc.repository.AllocationLogRepository;
import com.example.smartalloc.repository.TaskRepository;
import com.example.smartalloc.service.SchedulerService;
import com.example.smartalloc.view.TaskPageRenderer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class TaskController {

    private final TaskRepository repo;
    private final SchedulerService scheduler;
    private final ResourcePool pool;
    private final AllocationLogRepository logs;
    private final TaskPageRenderer renderer;

    public TaskController(TaskRepository repo, SchedulerService scheduler, ResourcePool pool, AllocationLogRepository logs) {
        this.repo = repo;
        this.scheduler = scheduler;
        this.pool = pool;
        this.logs = logs;
        this.renderer = new TaskPageRenderer();
    }

    public void registerRoutes(HttpServer server) {
        server.createContext("/", this::home);
        server.createContext("/add", this::addTask);
        server.createContext("/edit", this::editTask);
        server.createContext("/delete", this::deleteTask);
        server.createContext("/run", this::runScheduler);
        server.createContext("/strategy", this::setStrategy);
        server.createContext("/config", this::configureResources);
    }

    private void home(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            redirect(exchange, "/", "Only GET is allowed here.");
            return;
        }

        String message = FormParser.query(exchange).getOrDefault("message", "");
        sendHtml(exchange, renderer.render(repo.findAll(), logs.findAll(), pool, scheduler.getCurrentStrategy(), message));
    }

    private void addTask(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = FormParser.form(exchange);
            Task task = buildTask(form);
            String validation = validateTask(task);

            if (validation.isEmpty()) {
                repo.save(task);
                redirect(exchange, "/", "Task added.");
                return;
            }

            redirect(exchange, "/", validation);
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for adding tasks.");
    }

    private void editTask(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = FormParser.form(exchange);
            Task existing = repo.findById(FormParser.parseLong(form.get("id")));

            if (existing == null) {
                redirect(exchange, "/", "Task not found.");
                return;
            }

            if (existing.getStatus() != TaskStatus.PENDING) {
                redirect(exchange, "/", "Only pending tasks can be edited.");
                return;
            }

            Task updated = buildTask(form);
            String validation = validateTask(updated);
            if (!validation.isEmpty()) {
                redirect(exchange, "/", validation);
                return;
            }

            existing.setCpu(updated.getCpu());
            existing.setMemory(updated.getMemory());
            existing.setExecutionTime(updated.getExecutionTime());
            existing.setPriority(updated.getPriority());
            repo.save(existing);
            redirect(exchange, "/", "Task #" + existing.getId() + " updated.");
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for editing tasks.");
    }

    private void deleteTask(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = FormParser.form(exchange);
            Task task = repo.findById(FormParser.parseLong(form.get("id")));

            if (task == null) {
                redirect(exchange, "/", "Task not found.");
                return;
            }

            if (task.getStatus() != TaskStatus.PENDING) {
                redirect(exchange, "/", "Only pending tasks can be deleted.");
                return;
            }

            task.setStatus(TaskStatus.CANCELLED);
            repo.save(task);
            logs.add("Task #" + task.getId() + " was cancelled before allocation.");
            redirect(exchange, "/", "Task #" + task.getId() + " cancelled.");
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for deleting tasks.");
    }

    private void runScheduler(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            scheduler.runScheduler();
            redirect(exchange, "/", "Scheduler started.");
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for running the scheduler.");
    }

    private void setStrategy(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = FormParser.form(exchange);
            scheduler.setStrategy(form.getOrDefault("type", "FCFS"));
            redirect(exchange, "/", "Strategy updated.");
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for changing strategy.");
    }

    private void configureResources(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = FormParser.form(exchange);
            int cpu = FormParser.parsePositiveInt(form.get("cpu"));
            int memory = FormParser.parsePositiveInt(form.get("memory"));

            if (pool.configure(cpu, memory)) {
                redirect(exchange, "/", "Resource limits updated.");
            } else {
                redirect(exchange, "/", "Resource limits must be positive and cannot be below currently allocated resources.");
            }
            return;
        }

        redirect(exchange, "/", "Only POST is allowed for resource configuration.");
    }

    private Task buildTask(Map<String, String> form) {
        Task task = new Task();
        task.setCpu(FormParser.parsePositiveInt(form.get("cpu")));
        task.setMemory(FormParser.parsePositiveInt(form.get("memory")));
        task.setExecutionTime(FormParser.parsePositiveInt(form.get("executionTime")));
        task.setPriority(FormParser.parsePositiveInt(form.get("priority")));
        task.setStatus(TaskStatus.PENDING);
        return task;
    }

    private String validateTask(Task task) {
        if (task.getCpu() < 1 || task.getMemory() < 1 || task.getExecutionTime() < 1 || task.getPriority() < 1) {
            return "Task values must be positive.";
        }

        if (!pool.canFit(task.getCpu(), task.getMemory())) {
            return "Task CPU and memory cannot exceed currently available resources.";
        }

        return "";
    }

    private void redirect(HttpExchange exchange, String location, String message) throws IOException {
        String target = message.isBlank() ? location : location + "?message=" + FormParser.encode(message);
        exchange.getResponseHeaders().add("Location", target);
        exchange.sendResponseHeaders(303, -1);
        exchange.close();
    }

    private void sendHtml(HttpExchange exchange, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }
}
