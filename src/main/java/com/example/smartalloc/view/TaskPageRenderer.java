package com.example.smartalloc.view;

import com.example.smartalloc.model.AllocationLog;
import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.model.Task;
import com.example.smartalloc.model.TaskStatus;
import java.util.List;

public class TaskPageRenderer {

    public String render(List<Task> tasks, List<AllocationLog> logs, ResourcePool pool, String currentStrategy, String message) {
        long pendingCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.PENDING).count();
        long runningCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.RUNNING).count();
        long completedCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.COMPLETED).count();
        long failedCount = tasks.stream().filter(task -> task.getStatus() == TaskStatus.FAILED).count();

        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <title>Smart Resource Allocation</title>
                    <style>
                        * { box-sizing: border-box; }
                        body {
                            margin: 0;
                            color: #1d2433;
                            background: #eef2f6;
                            font-family: Arial, Helvetica, sans-serif;
                        }
                        .page {
                            width: min(1180px, calc(100% - 32px));
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
                        h1 { margin: 0; font-size: 34px; }
                        h2 { margin: 0 0 16px; color: #243447; font-size: 20px; }
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
                        .strategy-pill strong { font-size: 24px; }
                        .message {
                            margin: 18px 0 0;
                            padding: 12px 14px;
                            color: #17424a;
                            background: #d9f2ec;
                            border: 1px solid #a9d8cf;
                            border-radius: 8px;
                            font-weight: 700;
                        }
                        .metrics {
                            display: grid;
                            grid-template-columns: repeat(6, minmax(0, 1fr));
                            gap: 14px;
                            margin: 18px 0;
                        }
                        .metric, .panel {
                            background: white;
                            border: 1px solid #dce4ec;
                            border-radius: 8px;
                            box-shadow: 0 10px 24px rgba(27, 44, 63, 0.08);
                        }
                        .metric { padding: 18px; }
                        .metric span {
                            display: block;
                            margin-bottom: 8px;
                            color: #697789;
                            font-size: 13px;
                            font-weight: 700;
                            text-transform: uppercase;
                        }
                        .metric strong { color: #1f2d3d; font-size: 28px; }
                        .workbench {
                            display: grid;
                            grid-template-columns: 1fr 1fr 1fr;
                            gap: 18px;
                            align-items: start;
                            margin-bottom: 18px;
                        }
                        .panel { padding: 22px; }
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
                            width: 100%;
                            padding: 10px 11px;
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
                        }
                        button:disabled {
                            cursor: not-allowed;
                            color: #6a7683;
                            background: #e5ebf0;
                            border-color: #cbd6e2;
                        }
                        .danger { background: #d94f30; border-color: #d94f30; }
                        .add-button { margin-top: 16px; }
                        .control-stack { display: grid; gap: 14px; }
                        .table-panel { overflow-x: auto; padding: 0; }
                        .table-header {
                            display: flex;
                            justify-content: space-between;
                            gap: 12px;
                            align-items: center;
                            padding: 20px 22px;
                            border-bottom: 1px solid #dce4ec;
                        }
                        .table-header p { margin: 0; color: #697789; font-size: 14px; }
                        table { width: 100%; border-collapse: collapse; }
                        th, td {
                            padding: 12px;
                            border-bottom: 1px solid #e5ebf0;
                            text-align: left;
                            vertical-align: top;
                        }
                        th {
                            color: #5a6878;
                            background: #f5f7fa;
                            font-size: 12px;
                            text-transform: uppercase;
                        }
                        tr:last-child td { border-bottom: 0; }
                        td input { min-width: 82px; }
                        .actions {
                            display: grid;
                            grid-template-columns: 1fr 1fr;
                            gap: 8px;
                            min-width: 180px;
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
                        .badge-pending { color: #755700; background: #fff0bc; }
                        .badge-running { color: #075c63; background: #c8f3e7; }
                        .badge-completed { color: #1d6b3d; background: #d8f3dc; }
                        .badge-cancelled { color: #8a1f16; background: #ffd9d2; }
                        .badge-failed { color: #66220f; background: #ffd8a8; }
                        .empty { padding: 28px; color: #697789; text-align: center; }
                        .logs { display: grid; gap: 10px; margin: 0; padding: 0; list-style: none; }
                        .logs li {
                            display: grid;
                            grid-template-columns: 170px 1fr;
                            gap: 10px;
                            padding: 10px 0;
                            border-bottom: 1px solid #e5ebf0;
                        }
                        .logs li:last-child { border-bottom: 0; }
                        .logs time { color: #697789; font-weight: 700; }
                        @media (max-width: 980px) {
                            .topbar, .workbench { display: grid; grid-template-columns: 1fr; }
                            .metrics { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                        }
                        @media (max-width: 560px) {
                            .page { width: min(100% - 20px, 1180px); padding-top: 10px; }
                            .topbar, .panel { padding: 18px; }
                            h1 { font-size: 27px; }
                            .metrics, .form-grid, .logs li { grid-template-columns: 1fr; }
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
                                <strong>{{strategy}}</strong>
                            </div>
                        </section>
                        {{message}}
                        <section class="metrics">
                            <div class="metric"><span>CPU Free</span><strong>{{cpuFree}}/{{cpuTotal}}</strong></div>
                            <div class="metric"><span>Memory Free</span><strong>{{memoryFree}}/{{memoryTotal}}</strong></div>
                            <div class="metric"><span>Pending</span><strong>{{pending}}</strong></div>
                            <div class="metric"><span>Running</span><strong>{{running}}</strong></div>
                            <div class="metric"><span>Completed</span><strong>{{completed}}</strong></div>
                            <div class="metric"><span>Failed</span><strong>{{failed}}</strong></div>
                        </section>

                        <section class="workbench">
                            <div class="panel">
                                <h2>Add Task</h2>
                                <form action="/add" method="post">
                                    <div class="form-grid">
                                        <div><label for="cpu">CPU Units</label><input id="cpu" type="number" name="cpu" min="1" max="{{cpuFree}}" required></div>
                                        <div><label for="memory">Memory Units</label><input id="memory" type="number" name="memory" min="1" max="{{memoryFree}}" required></div>
                                        <div><label for="executionTime">Execution Time</label><input id="executionTime" type="number" name="executionTime" min="1" required></div>
                                        <div><label for="priority">Priority</label><input id="priority" type="number" name="priority" min="1" required></div>
                                    </div>
                                    <button class="add-button" type="submit">Add Task</button>
                                </form>
                            </div>

                            <div class="panel">
                                <h2>Controls</h2>
                                <div class="control-stack">
                                    <form action="/run" method="post"><button class="danger" type="submit">Run Scheduler</button></form>
                                    <form action="/strategy" method="post">
                                        <label for="strategy">Scheduling Strategy</label>
                                        <select id="strategy" name="type">
                                            <option value="FCFS" {{fcfsSelected}}>FCFS</option>
                                            <option value="PRIORITY" {{prioritySelected}}>Priority</option>
                                            <option value="SJF" {{sjfSelected}}>SJF</option>
                                        </select>
                                        <button class="add-button" type="submit">Set Strategy</button>
                                    </form>
                                </div>
                            </div>

                            <div class="panel">
                                <h2>Resource Config</h2>
                                <form action="/config" method="post">
                                    <div class="form-grid">
                                        <div><label for="configCpu">Total CPU</label><input id="configCpu" type="number" name="cpu" min="1" value="{{cpuTotal}}" required></div>
                                        <div><label for="configMemory">Total Memory</label><input id="configMemory" type="number" name="memory" min="1" value="{{memoryTotal}}" required></div>
                                    </div>
                                    <button class="add-button" type="submit">Update Resources</button>
                                </form>
                            </div>
                        </section>

                        <section class="panel table-panel">
                            <div class="table-header">
                                <h2>Tasks</h2>
                                <p>Edit and delete are available only while a task is pending.</p>
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
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>{{rows}}</tbody>
                            </table>
                        </section>

                        <section class="panel">
                            <h2>Allocation Logs</h2>
                            {{logs}}
                        </section>
                    </main>
                </body>
                </html>
                """
                .replace("{{strategy}}", escape(currentStrategy))
                .replace("{{message}}", renderMessage(message))
                .replace("{{cpuFree}}", String.valueOf(pool.getAvailableCPU()))
                .replace("{{cpuTotal}}", String.valueOf(pool.getTotalCPU()))
                .replace("{{memoryFree}}", String.valueOf(pool.getAvailableMemory()))
                .replace("{{memoryTotal}}", String.valueOf(pool.getTotalMemory()))
                .replace("{{pending}}", String.valueOf(pendingCount))
                .replace("{{running}}", String.valueOf(runningCount))
                .replace("{{completed}}", String.valueOf(completedCount))
                .replace("{{failed}}", String.valueOf(failedCount))
                .replace("{{fcfsSelected}}", selected(currentStrategy, "FCFS"))
                .replace("{{prioritySelected}}", selected(currentStrategy, "PRIORITY"))
                .replace("{{sjfSelected}}", selected(currentStrategy, "SJF"))
                .replace("{{rows}}", renderRows(tasks, pool))
                .replace("{{logs}}", renderLogs(logs));
    }

    private String renderRows(List<Task> tasks, ResourcePool pool) {
        if (tasks.isEmpty()) {
            return "<tr><td class=\"empty\" colspan=\"7\">No tasks added yet. Add one above to begin.</td></tr>";
        }

        StringBuilder rows = new StringBuilder();
        for (Task task : tasks) {
            rows.append("<tr>")
                    .append("<td><strong>#").append(task.getId()).append("</strong></td>")
                    .append(renderEditableCell(task, "cpu", task.getCpu(), pool.getAvailableCPU()))
                    .append(renderEditableCell(task, "memory", task.getMemory(), pool.getAvailableMemory()))
                    .append(renderEditableCell(task, "executionTime", task.getExecutionTime(), 9999))
                    .append(renderEditableCell(task, "priority", task.getPriority(), 9999))
                    .append("<td><span class=\"badge ").append(statusClass(task.getStatus())).append("\">")
                    .append(task.getStatus()).append("</span></td>")
                    .append("<td>").append(renderActions(task)).append("</td>")
                    .append("</tr>");
        }

        return rows.toString();
    }

    private String renderEditableCell(Task task, String field, int value, int max) {
        if (task.getStatus() == TaskStatus.PENDING) {
            return "<td><input form=\"edit-" + task.getId() + "\" type=\"number\" name=\"" + field
                    + "\" min=\"1\" max=\"" + max + "\" value=\"" + value + "\" required></td>";
        }

        return "<td>" + value + "</td>";
    }

    private String renderActions(Task task) {
        if (task.getStatus() != TaskStatus.PENDING) {
            return """
                    <div class="actions">
                        <button type="button" disabled>Edit</button>
                        <button type="button" disabled>Delete</button>
                    </div>
                    """;
        }

        return """
                <div class="actions">
                    <form id="edit-%d" action="/edit" method="post">
                        <input type="hidden" name="id" value="%d">
                        <button type="submit">Edit</button>
                    </form>
                    <form action="/delete" method="post">
                        <input type="hidden" name="id" value="%d">
                        <button class="danger" type="submit">Delete</button>
                    </form>
                </div>
                """.formatted(task.getId(), task.getId(), task.getId());
    }

    private String renderLogs(List<AllocationLog> logs) {
        if (logs.isEmpty()) {
            return "<p class=\"empty\">No allocation events yet.</p>";
        }

        StringBuilder html = new StringBuilder("<ul class=\"logs\">");
        for (AllocationLog log : logs) {
            html.append("<li><time>")
                    .append(escape(log.getTimestamp()))
                    .append("</time><span>")
                    .append(escape(log.getMessage()))
                    .append("</span></li>");
        }
        html.append("</ul>");
        return html.toString();
    }

    private String renderMessage(String message) {
        if (message == null || message.isBlank()) {
            return "";
        }

        return "<p class=\"message\">" + escape(message) + "</p>";
    }

    private String selected(String currentStrategy, String value) {
        return value.equals(currentStrategy) ? "selected" : "";
    }

    private String statusClass(TaskStatus status) {
        return switch (status) {
            case PENDING -> "badge-pending";
            case RUNNING -> "badge-running";
            case COMPLETED -> "badge-completed";
            case CANCELLED -> "badge-cancelled";
            case FAILED -> "badge-failed";
        };
    }

    private String escape(String value) {
        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
