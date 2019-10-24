package dk.aau.d507e19.warehousesim.controller.server.taskAllocator;

import dk.aau.d507e19.warehousesim.controller.robot.Order;
import dk.aau.d507e19.warehousesim.controller.robot.Robot;
import dk.aau.d507e19.warehousesim.controller.robot.Status;

import java.util.ArrayList;
import java.util.Optional;

public class DummyTaskAllocator implements TaskAllocator {

    @Override
    public Optional<Robot> findOptimalRobot(ArrayList<Robot> robots, Order order) {
        for(Robot robot : robots){
            if(robot.getCurrentStatus() == Status.AVAILABLE) return Optional.of(robot);
        }
        return Optional.empty();
    }
}