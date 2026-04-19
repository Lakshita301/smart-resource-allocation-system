# Smart Resource Allocation System

A simple Java web demo that allocates CPU and memory resources to tasks using scheduling strategies.

This version does not need Maven, Gradle, Spring Boot, or any external dependency. It runs using only Java 17.

## Patterns Used

- MVC: `TaskController`, generated HTML view, model classes
- Singleton: `ResourcePool`
- Strategy: `FCFSStrategy`, `PriorityStrategy`, `SJFStrategy`
- Factory: `StrategyFactory`
- Repository: in-memory `TaskRepository`

## Run

From this project folder, run:

```powershell
.\run.ps1
```

Open:

```text
http://localhost:8080
```

## Demo Flow

1. Add tasks with CPU, memory, execution time, and priority.
2. Select a scheduling strategy.
3. Click **Run Scheduler**.
4. Tasks move from `PENDING` to `RUNNING` to `COMPLETED` as resources are allocated and released.
