package com.example.smartalloc.model;

public class ResourcePool {

    private static final ResourcePool INSTANCE = new ResourcePool();

    private int totalCPU = 100;
    private int totalMemory = 200;

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

    public synchronized boolean configure(int cpu, int memory) {
        if (cpu < 1 || memory < 1) {
            return false;
        }

        int usedCPU = totalCPU - availableCPU;
        int usedMemory = totalMemory - availableMemory;

        if (cpu < usedCPU || memory < usedMemory) {
            return false;
        }

        totalCPU = cpu;
        totalMemory = memory;
        availableCPU = cpu - usedCPU;
        availableMemory = memory - usedMemory;
        return true;
    }

    public synchronized boolean canFit(int cpu, int memory) {
        return cpu > 0 && memory > 0 && cpu <= availableCPU && memory <= availableMemory;
    }

    public synchronized int getTotalCPU() {
        return totalCPU;
    }

    public synchronized int getTotalMemory() {
        return totalMemory;
    }

    public synchronized int getAvailableCPU() {
        return availableCPU;
    }

    public synchronized int getAvailableMemory() {
        return availableMemory;
    }
}
