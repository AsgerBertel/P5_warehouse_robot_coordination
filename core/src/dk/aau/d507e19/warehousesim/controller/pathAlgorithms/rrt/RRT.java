package dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt;

import dk.aau.d507e19.warehousesim.WarehouseSpecs;
import dk.aau.d507e19.warehousesim.controller.path.Step;
import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;
import dk.aau.d507e19.warehousesim.controller.robot.Robot;
import dk.aau.d507e19.warehousesim.controller.robot.RobotController;
import dk.aau.d507e19.warehousesim.controller.server.Server;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RRT extends RRTBase {

    public RRT(RobotController robotController) {
        super(robotController);
    }

    public ArrayList<Step> generateRRTPathFromEmpty(GridCoordinate start, GridCoordinate destination) {
        return super.generatePathFromEmpty(start,destination);
    }
    public ArrayList<Step> generateRRTPath(GridCoordinate start, GridCoordinate destination){
        return super.generatePath(start,destination);
    }
}
