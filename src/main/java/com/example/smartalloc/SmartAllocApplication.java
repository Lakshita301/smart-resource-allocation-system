package com.example.smartalloc;

import com.example.smartalloc.controller.TaskController;
import com.example.smartalloc.model.ResourcePool;
import com.example.smartalloc.repository.TaskRepository;
import com.example.smartalloc.service.SchedulerService;
import com.example.smartalloc.strategy.StrategyFactory;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;

public class SmartAllocApplication {

    public static void main(String[] args) {
        try {
            TaskRepository repository = new TaskRepository();
            ResourcePool pool = ResourcePool.getInstance();
            StrategyFactory factory = new StrategyFactory();
            SchedulerService scheduler = new SchedulerService(repository, pool, factory);
            TaskController controller = new TaskController(repository, scheduler, pool);

            HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
            controller.registerRoutes(server);
            server.setExecutor(null);
            server.start();

            System.out.println("Smart Resource Allocation System is running.");
            System.out.println("Open http://localhost:8080 in your browser.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
