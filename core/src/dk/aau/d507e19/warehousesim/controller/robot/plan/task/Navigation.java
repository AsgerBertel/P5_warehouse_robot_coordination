package dk.aau.d507e19.warehousesim.controller.robot.plan.task;

import dk.aau.d507e19.warehousesim.TickTimer;
import dk.aau.d507e19.warehousesim.controller.path.Line;
import dk.aau.d507e19.warehousesim.controller.path.Path;
import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;
import dk.aau.d507e19.warehousesim.controller.robot.MovementPredictor;
import dk.aau.d507e19.warehousesim.controller.robot.Robot;
import dk.aau.d507e19.warehousesim.controller.robot.RobotController;
import dk.aau.d507e19.warehousesim.controller.robot.plan.LineTraversal;
import dk.aau.d507e19.warehousesim.controller.server.Reservation;
import dk.aau.d507e19.warehousesim.controller.server.Server;
import dk.aau.d507e19.warehousesim.controller.server.TimeFrame;
import dk.aau.d507e19.warehousesim.exception.NoPathFoundException;

import java.util.ArrayList;

public class Navigation implements Task {

    private int maximumRetries = -1;
    private static final int TICKS_BETWEEN_RETRIES = 30;

    private GridCoordinate destination;
    private Path path;
    private ArrayList<LineTraversal> lineTraversals = new ArrayList<>();

    private RobotController robotController;
    private Robot robot;

    private TickTimer retryTimer = new TickTimer(TICKS_BETWEEN_RETRIES);
    private boolean isCompleted = false;


    public Navigation(RobotController robotController, GridCoordinate destination) {
        this.robotController = robotController;
        this.destination = destination;
        this.robot = robotController.getRobot();
        retryTimer.setRemainingTicks(0);
    }

    public void setMaximumRetries(int maxRetries){
        this.maximumRetries = maxRetries;
    }

    @Override
    public void perform() {
        if(isCompleted())
            throw new RuntimeException("Can't perform task that is already completed");

        if(path == null){
            if(!retryTimer.isDone()){
                retryTimer.decrement();
            }else {
                planPath();
                retryTimer.reset();
                traversePath();
            }
        }else{
            traversePath();
        }
    }

    private void traversePath() {
        if(lineTraversals.isEmpty())
            createLineTraversals();

        LineTraversal currentLineTraversal = lineTraversals.get(0);
        currentLineTraversal.perform();
        if(currentLineTraversal.isCompleted()){
            lineTraversals.remove(currentLineTraversal);

            // Path has finished traversing
            if(lineTraversals.isEmpty()){
                isCompleted = true;
            }

        }
    }

    private void createLineTraversals() {
        lineTraversals.clear();
        ArrayList<Line> lines = path.getLines();
        for(Line line : lines)
            lineTraversals.add(new LineTraversal(robot, line));
    }

    private void planPath() {
        Server server = robotController.getServer();
        server.getReservationManager().removeReservationsBy(robot);

        GridCoordinate start = robot.getGridCoordinate();
        try {
            path = robotController.getPathFinder().calculatePath(start, destination);
        } catch (NoPathFoundException e) {
            e.printStackTrace(); // todo
        }

        ArrayList<Reservation> reservations = MovementPredictor.calculateReservations(robot, path, server.getTimeInTicks(),0);
        reservations.add(createLastTileIndefiniteReservation(reservations));

        server.getReservationManager().reserve(reservations);
    }

    private Reservation createLastTileIndefiniteReservation(ArrayList<Reservation> reservations) {
        Reservation lastReservation = reservations.get(reservations.size() - 1);
        TimeFrame indefiniteTimeFrame = TimeFrame.indefiniteTimeFrameFrom(lastReservation.getTimeFrame().getStart());
        return new Reservation(lastReservation.getRobot(), lastReservation.getGridCoordinate(), indefiniteTimeFrame);
    }

    public void interrupt(){
        this.path = null;
        lineTraversals.clear();
        // todo clear reservations
    }

    @Override
    public boolean isCompleted() {
        return isCompleted;
    }

    @Override
    public boolean hasFailed() {
        return false;
    }

    @Override
    public void setRobot(Robot robot) {
        this.robot = robot;
    }
}