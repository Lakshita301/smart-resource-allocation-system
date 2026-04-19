package com.example.smartalloc.model;

public class ResourcePool {

    private static final ResourcePool INSTANCE = new ResourcePool();

    private final int totalCPU = 100;
    private final int totalMemory = 200;

    private int availableCPU = 100;
    private int availableMemory = 200;

    private ResourcePool() {
    }

    public static ResourcePool getInstance() {
        return INSTANCE;
    }

    public synchronized boolean allocate(int cpu, int memory) {
        if (cpu <= availableCPU && memory <= availableMemory) {
            availableCPU -= cpu;
            availableMemory -= memory;
            return true;
        }

        return false;
    }

    public synchronized void release(int cpu, int memory) {
        availableCPU = Math.min(totalCPU, availableCPU + cpu);
        availableMemory = Math.min(totalMemory, availableMemory + memory);
    }

    public int getTotalCPU() {
        return totalCPU;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public synchronized int getAvailableCPU() {
        return availableCPU;
    }

    public synchronized int getAvailableMemory() {
        return availableMemory;
    }
}
