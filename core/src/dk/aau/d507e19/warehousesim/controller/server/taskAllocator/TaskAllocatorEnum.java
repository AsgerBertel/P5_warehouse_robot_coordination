package dk.aau.d507e19.warehousesim.controller.server.taskAllocator;

import dk.aau.d507e19.warehousesim.controller.server.Server;
import dk.aau.d507e19.warehousesim.storagegrid.StorageGrid;

public enum TaskAllocatorEnum {
    SEQUENTIAL_TASK_ALLOCATOR("SequentialTaskAllocator", true),
    DISTANCE_TASK_ALLOCATOR("DistanceTaskAllocator", true),
    LEAST_USED_ROBOT_TASK_ALLOCATOR("LeastUsedRobotTaskAllocator", true),
    WORKLOAD_TASK_ALLOCATOR("WorkloadTaskAllocator", true),
    FAIR_TASK_ALLOCATOR("FairTaskAllocator", true);

    private String name;
    private boolean works;
    TaskAllocatorEnum(String name, boolean works) {
        this.works = works;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean works() {
        return works;
    }

    public TaskAllocator getTaskAllocator(StorageGrid grid, Server server){
        switch (this) {
            case SEQUENTIAL_TASK_ALLOCATOR:
                return new DummyTaskAllocator(server);
            case DISTANCE_TASK_ALLOCATOR:
                return new NaiveShortestDistanceTaskAllocator(grid, server);
            case LEAST_USED_ROBOT_TASK_ALLOCATOR:
                return new LeastUsedRobotTaskAllocator(grid, server);
            case WORKLOAD_TASK_ALLOCATOR:
                return new SmartAllocator(server);
            case FAIR_TASK_ALLOCATOR:
                return new FairTaskAllocator(grid, server);
            default:
                throw new RuntimeException("Could not identify task allocator " + this.getName());
        }
    }
}
