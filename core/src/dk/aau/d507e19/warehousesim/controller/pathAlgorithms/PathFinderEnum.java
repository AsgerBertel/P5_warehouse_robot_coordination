package dk.aau.d507e19.warehousesim.controller.pathAlgorithms;

import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.aStar.Astar;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.aStarExtended.AstarExtended;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.chp.CHPathfinder;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.RRTPlanner;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.RRTType;
import dk.aau.d507e19.warehousesim.controller.robot.RobotController;
import dk.aau.d507e19.warehousesim.controller.server.Server;

public enum PathFinderEnum {
    DUMMYPATHFINDER("DummyPathFinder", true), ASTAR("A*", true), ASTARCORNERS("A* Corners", true),
    CHPATHFINDER("CustomH - Turns", true), RRT("RRT", true), RRTSTAR("RRT*", true), RRTSTAREXTENDED("RRT*EXTENDED",true);

    private String name;
    private boolean works;
    PathFinderEnum(String name, boolean works) {
        this.name = name;
        this.works = works;
    }

    public boolean works() {
        return works;
    }

    public String getName() {
        return name;
    }

    public PathFinder getPathFinder(Server server, RobotController robotController){
        switch (this) {
            case ASTAR:
                return new Astar(server, robotController.getRobot());
            case CHPATHFINDER:
                return CHPathfinder.defaultCHPathfinder(server.getGridBounds(), robotController);
            case RRT:
                return new RRTPlanner(RRTType.RRT, robotController);
            case RRTSTAR:
                return new RRTPlanner(RRTType.RRT_STAR, robotController);
            case DUMMYPATHFINDER:
                return new DummyPathFinder(robotController);
            case ASTARCORNERS:
                return new AstarExtended(server, robotController.getRobot());
            case RRTSTAREXTENDED:
                return new RRTPlanner(RRTType.RRT_STAR_EXTENDED,robotController);
            default:
                throw new RuntimeException("Could not identify pathfinder " + this.getName());
        }
    }
}
