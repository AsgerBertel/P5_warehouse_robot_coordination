package dk.aau.d507e19.warehousesim.controller.pathAlgorithms;

import dk.aau.d507e19.warehousesim.controller.path.Path;
import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;
import dk.aau.d507e19.warehousesim.exception.NextStepBlockedException;

public interface PartialPathFinder extends PathFinder {

    @Override
    Path calculatePath(GridCoordinate start, GridCoordinate destination);

}
