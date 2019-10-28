package dk.aau.d507e19.warehousesim.controller.robot;

import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.DummyPathFinder;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.aStar.Astar;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.PathFinder;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.chp.CHPathfinder;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.RRTPlanner;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.RRTType;
import dk.aau.d507e19.warehousesim.controller.robot.plan.task.Task;
import dk.aau.d507e19.warehousesim.controller.server.Server;
import dk.aau.d507e19.warehousesim.controller.server.TimeFrame;

import java.util.LinkedList;
import java.util.Optional;

public class RobotController {
    private Server server;
    private PathFinder pathFinder;
    private Robot robot;

    private LinkedList<Task> tasks = new LinkedList<>();
    private LinkedList<Runnable> planningSteps = new LinkedList<>();

    public RobotController(Server server, Robot robot, String pathFinderString){
        this.server = server;
        this.robot = robot;
        this.pathFinder = generatePathFinder(pathFinderString);
        server.getReservationManager().reserve(robot, robot.getGridCoordinate(), TimeFrame.indefiniteTimeFrameFrom(server.getTimeInTicks()));
    }

    private PathFinder generatePathFinder(String pathFinderString) {
        switch (pathFinderString) {
            case "Astar":
                return new Astar(server, robot);
            case "RRT*":
                return new RRTPlanner(RRTType.RRT_STAR, this);
            case "RRT":
                return new RRTPlanner(RRTType.RRT, this);
            case "DummyPathFinder":
                return new DummyPathFinder();
            case "CustomH - Turns":
                return CHPathfinder.defaultCHPathfinder(server.getGridBounds(), this);
            default:
                throw new RuntimeException("Could not identify pathfinder " + pathFinderString);
        }
    }

    public boolean assignTask(Task task){
        tasks.add(task);
        // tasks.add(new TotalReset());  todo
        return true;
    }

    public void cancelTask(Task task) {
        // todo
    }

    public void update() {
        // If robot has nothing to do, set status available and return.
        if (tasks.isEmpty()) {
            robot.setCurrentStatus(Status.AVAILABLE);
            return;
        }else{
            robot.setCurrentStatus(Status.BUSY);
        }

        Task currentTask = tasks.peekFirst();
        if (!currentTask.isCompleted())
            currentTask.perform();

        if (currentTask.isCompleted())
            tasks.removeFirst();
    }

    public PathFinder getPathFinder() {
        return this.pathFinder;
    }

    public Server getServer() {
        return this.server;
    }

    public Robot getRobot() {
        return robot;
    }
}
