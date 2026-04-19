package com.example.smartalloc.controller;

import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import com.example.smartalloc.repository.TaskRepository;
import com.example.smartalloc.service.SchedulerService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskController {

    private final TaskRepository repo;
    private final SchedulerService scheduler;
    private final ResourcePool pool;

    public TaskController(TaskRepository repo, SchedulerService scheduler, ResourcePool pool) {
        this.repo = repo;
        this.scheduler = scheduler;
        this.pool = pool;
    }

    public void registerRoutes(HttpServer server) {
        server.createContext("/", this::home);
        server.createContext("/add", this::addTask);
        server.createContext("/run", this::runScheduler);
        server.createContext("/strategy", this::setStrategy);
    }

    private void home(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            redirect(exchange, "/");
            return;
        }

        sendHtml(exchange, renderPage(repo.findAll()));
    }

    private void addTask(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            Task task = new Task();
            task.setCpu(parseInt(form.get("cpu")));
            task.setMemory(parseInt(form.get("memory")));
            task.setExecutionTime(parseInt(form.get("executionTime")));
            task.setPriority(parseInt(form.get("priority")));
            task.setStatus(TaskStatus.PENDING);
            repo.save(task);
        }

        redirect(exchange, "/");
    }

    private void runScheduler(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            scheduler.runScheduler();
        }

        redirect(exchange, "/");
    }

    private void setStrategy(HttpExchange exchange) throws IOException {
        if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            Map<String, String> form = readForm(exchange);
            scheduler.setStrategy(form.getOrDefault("type", "FCFS"));
        }

        redirect(exchange, "/");
    }

    private Map<String, String> readForm(HttpExchange exchange) throws IOException {
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        Map<String, String> values = new HashMap<>();

        if (body.isBlank()) {
            return values;
        }

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            values.put(key, value);
        }

        return values;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private int parseInt(String value) {
        try {
            return Math.max(1, Integer.parseInt(value));
        } catch (Exception e) {
            return 1;
        }
    }

    private void redirect(HttpExchange exchange, String location) throws IOException {
        exchange.getResponseHeaders().add("Location", location);
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

    private String renderPage(List<Task> tasks) {
        StringBuilder rows = new StringBuilder();
        long pendingCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.PENDING).count();
        long runningCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count();
        long completedCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();

        if (tasks.isEmpty()) {
            rows.append("<tr><td class=\"empty\" colspan=\"6\">No tasks added yet. Add one above to begin.</td></tr>");
        } else {
            for (Task task : tasks) {
                rows.append("<tr>")
                        .append("<td><strong>#").append(task.getId()).append("</strong></td>")
                        .append("<td>").append(task.getCpu()).append("</td>")
                        .append("<td>").append(task.getMemory()).append("</td>")
                        .append("<td>").append(task.getExecutionTime()).append(" sec</td>")
                        .append("<td>").append(task.getPriority()).append("</td>")
                        .append("<td><span class=\"badge ")
                        .append(statusClass(task.getStatus()))
                        .append("\">")
                        .append(task.getStatus())
                        .append("</span></td>")
                        .append("</tr>");
            }
        }

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Smart Resource Allocation</title>
                    <style>
                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            color: #1d2433;
                            background: #eef2f6;
                            font-family: Arial, Helvetica, sans-serif;
                        }

                        .page {
                            width: min(1120px, calc(100%% - 32px));
                            margin: 0 auto;
                            padding: 28px 0 44px;
                        }

                        .topbar {
                            display: flex;
                            justify-content: space-between;
                            gap: 18px;
                            align-items: flex-end;
                            padding: 28px;
                            color: white;
                            background: linear-gradient(135deg, #13505b, #287271);
                            border-radius: 8px;
                            box-shadow: 0 18px 40px rgba(27, 44, 63, 0.16);
                        }

                        .eyebrow {
                            margin: 0 0 8px;
                            color: #c8f3e7;
                            font-size: 13px;
                            font-weight: 700;
                            text-transform: uppercase;
                        }

                        h1 {
                            margin: 0;
                            font-size: 34px;
                        }

                        h2 {
                            margin: 0 0 16px;
                            color: #243447;
                            font-size: 20px;
                        }

                        .strategy-pill {
                            min-width: 180px;
                            padding: 14px 16px;
                            background: rgba(255, 255, 255, 0.14);
                            border: 1px solid rgba(255, 255, 255, 0.28);
                            border-radius: 8px;
                            text-align: center;
                        }

                        .strategy-pill span {
                            display: block;
                            margin-bottom: 4px;
                            color: #c8f3e7;
                            font-size: 12px;
                            font-weight: 700;
                            text-transform: uppercase;
                        }

                        .strategy-pill strong {
                            font-size: 24px;
                        }

                        .metrics {
                            display: grid;
                            grid-template-columns: repeat(5, minmax(0, 1fr));
                            gap: 14px;
                            margin: 18px 0;
                        }

                        .metric, .panel {
                            background: white;
                            border: 1px solid #dce4ec;
                            border-radius: 8px;
                            box-shadow: 0 10px 24px rgba(27, 44, 63, 0.08);
                        }

                        .metric {
                            padding: 18px;
                        }

                        .metric span {
                            display: block;
                            margin-bottom: 8px;
                            color: #697789;
                            font-size: 13px;
                            font-weight: 700;
                            text-transform: uppercase;
                        }

                        .metric strong {
                            color: #1f2d3d;
                            font-size: 28px;
                        }

                        .workbench {
                            display: grid;
                            grid-template-columns: 1.2fr 0.8fr;
                            gap: 18px;
                            align-items: start;
                            margin-bottom: 18px;
                        }

                        .panel {
                            padding: 22px;
                        }

                        .form-grid {
                            display: grid;
                            grid-template-columns: repeat(2, minmax(0, 1fr));
                            gap: 14px;
                        }

                        label {
                            display: block;
                            margin-bottom: 6px;
                            color: #4d5d70;
                            font-size: 13px;
                            font-weight: 700;
                        }

                        input, select, button {
                            width: 100%%;
                            padding: 11px 12px;
                            border: 1px solid #cbd6e2;
                            border-radius: 6px;
                            font: inherit;
                        }

                        input:focus, select:focus {
                            outline: 3px solid rgba(40, 114, 113, 0.18);
                            border-color: #287271;
                        }

                        button {
                            cursor: pointer;
                            color: white;
                            font-weight: 700;
                            background: #287271;
                            border-color: #287271;
                            transition: transform 0.15s ease, background 0.15s ease;
                        }

                        button:hover {
                            background: #1f5c5b;
                            transform: translateY(-1px);
                        }

                        .add-button {
                            margin-top: 16px;
                        }

                        .control-stack {
                            display: grid;
                            gap: 14px;
                        }

                        .run-button {
                            background: #d94f30;
                            border-color: #d94f30;
                        }

                        .run-button:hover {
                            background: #b94228;
                        }

                        .table-panel {
                            overflow: hidden;
                            padding: 0;
                        }

                        .table-header {
                            display: flex;
                            justify-content: space-between;
                            gap: 12px;
                            align-items: center;
                            padding: 20px 22px;
                            border-bottom: 1px solid #dce4ec;
                        }

                        .table-header p {
                            margin: 0;
                            color: #697789;
                            font-size: 14px;
                        }

                        table {
                            width: 100%%;
                            border-collapse: collapse;
                        }

                        th, td {
                            padding: 14px 18px;
                            border-bottom: 1px solid #e5ebf0;
                            text-align: left;
                        }

                        th {
                            color: #5a6878;
                            background: #f5f7fa;
                            font-size: 12px;
                            text-transform: uppercase;
                        }

                        tr:last-child td {
                            border-bottom: 0;
                        }

                        .badge {
                            display: inline-block;
                            min-width: 92px;
                            padding: 6px 9px;
                            border-radius: 999px;
                            font-size: 12px;
                            font-weight: 700;
                            text-align: center;
                        }

                        .badge-pending {
                            color: #755700;
                            background: #fff0bc;
                        }

                        .badge-running {
                            color: #075c63;
                            background: #c8f3e7;
                        }

                        .badge-completed {
                            color: #1d6b3d;
                            background: #d8f3dc;
                        }

                        .badge-cancelled {
                            color: #8a1f16;
                            background: #ffd9d2;
                        }

                        .empty {
                            padding: 28px;
                            color: #697789;
                            text-align: center;
                        }

                        @media (max-width: 860px) {
                            .topbar, .workbench {
                                grid-template-columns: 1fr;
                                display: grid;
                            }

                            .metrics {
                                grid-template-columns: repeat(2, minmax(0, 1fr));
                            }
                        }

                        @media (max-width: 560px) {
                            .page {
                                width: min(100%% - 20px, 1120px);
                                padding-top: 10px;
                            }

                            .topbar, .panel {
                                padding: 18px;
                            }

                            h1 {
                                font-size: 27px;
                            }

                            .metrics, .form-grid {
                                grid-template-columns: 1fr;
                            }

                            th, td {
                                padding: 12px 10px;
                            }
                        }
                    </style>
                </head>
                <body>
                    <main class="page">
                        <section class="topbar">
                            <div>
                                <p class="eyebrow">Resource Scheduler</p>
                                <h1>Smart Resource Allocation System</h1>
                            </div>
                            <div class="strategy-pill">
                                <span>Strategy</span>
                                <strong>%s</strong>
                            </div>
                        </section>

                        <section class="metrics">
                            <div class="metric">
                                <span>CPU Free</span>
                                <strong>%d/%d</strong>
                            </div>
                            <div class="metric">
                                <span>Memory Free</span>
                                <strong>%d/%d</strong>
                            </div>
                            <div class="metric">
                                <span>Pending</span>
                                <strong>%d</strong>
                            </div>
                            <div class="metric">
                                <span>Running</span>
                                <strong>%d</strong>
                            </div>
                            <div class="metric">
                                <span>Completed</span>
                                <strong>%d</strong>
                            </div>
                        </section>

                        <section class="workbench">
                            <div class="panel">
                                <h2>Add Task</h2>
                                <form action="/add" method="post">
                                    <div class="form-grid">
                                        <div>
                                            <label for="cpu">CPU Units</label>
                                            <input id="cpu" type="number" name="cpu" min="1" placeholder="Example: 20" required>
                                        </div>
                                        <div>
                                            <label for="memory">Memory Units</label>
                                            <input id="memory" type="number" name="memory" min="1" placeholder="Example: 50" required>
                                        </div>
                                        <div>
                                            <label for="executionTime">Execution Time</label>
                                            <input id="executionTime" type="number" name="executionTime" min="1" placeholder="Seconds" required>
                                        </div>
                                        <div>
                                            <label for="priority">Priority</label>
                                            <input id="priority" type="number" name="priority" min="1" placeholder="Higher runs first" required>
                                        </div>
                                    </div>
                                    <button class="add-button" type="submit">Add Task</button>
                                </form>
                            </div>

                            <div class="panel">
                                <h2>Controls</h2>
                                <div class="control-stack">
                                    <form action="/run" method="post">
                                        <button class="run-button" type="submit">Run Scheduler</button>
                                    </form>

                                    <form action="/strategy" method="post">
                                        <label for="strategy">Scheduling Strategy</label>
                                        <select id="strategy" name="type">
                                            <option value="FCFS">FCFS</option>
                                            <option value="PRIORITY">Priority</option>
                                            <option value="SJF">SJF</option>
                                        </select>
                                        <button class="add-button" type="submit">Set Strategy</button>
                                    </form>
                                </div>
                            </div>
                        </section>

                        <section class="panel table-panel">
                            <div class="table-header">
                                <h2>Tasks</h2>
                                <p>Refresh the page when you want the latest task status.</p>
                            </div>
                            <table>
                                <thead>
                                    <tr>
                                        <th>ID</th>
                                        <th>CPU</th>
                                        <th>Memory</th>
                                        <th>Time</th>
                                        <th>Priority</th>
                                        <th>Status</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    %s
                                </tbody>
                            </table>
                        </section>
                    </main>
                </body>
                </html>
                """.formatted(
                scheduler.getCurrentStrategy(),
                pool.getAvailableCPU(),
                pool.getTotalCPU(),
                pool.getAvailableMemory(),
                pool.getTotalMemory(),
                pendingCount,
                runningCount,
                completedCount,
                rows
        );
    }

    private String statusClass(TaskStatus status) {
        return switch (status) {
            case PENDING -> "badge-pending";
            case RUNNING -> "badge-running";
            case COMPLETED -> "badge-completed";
            case CANCELLED -> "badge-cancelled";
        };
    }
}
