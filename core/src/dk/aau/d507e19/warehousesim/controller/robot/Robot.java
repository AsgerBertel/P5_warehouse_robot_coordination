package dk.aau.d507e19.warehousesim.controller.robot;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import dk.aau.d507e19.warehousesim.Position;
import dk.aau.d507e19.warehousesim.SimulationApp;
import dk.aau.d507e19.warehousesim.Tile;
import dk.aau.d507e19.warehousesim.WarehouseSpecs;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.PathFinder;

public class Robot {
    private Position currentPosition;
    private Task currentTask;
    private Status currentStatus;
    private float currentSpeed;
    private Path pathToTarget;

    /**
     * Robot STATS
     */
    // Pickup time
    private final static int pickUpTimeInTicks = SimulationApp.TICKS_PER_SECOND * WarehouseSpecs.robotPickUpSpeedInSeconds;
    private final static int deliverTimeInTicks = SimulationApp.TICKS_PER_SECOND * WarehouseSpecs.robotDeliverToPickerInSeconds;
    private int ticksLeftForCurrentTask = 0;
    // Speed
    private final float maxSpeedBinsPerSecond = WarehouseSpecs.robotTopSpeed / WarehouseSpecs.binSizeInMeters;
    private final float accelerationBinSecond = WarehouseSpecs.robotAcceleration / WarehouseSpecs.binSizeInMeters;
    private final float decelerationBinSecond = WarehouseSpecs.robotDeceleration / WarehouseSpecs.binSizeInMeters;
    private final float minSpeedBinsPerSecond = WarehouseSpecs.robotMinimumSpeed / WarehouseSpecs.binSizeInMeters;

    private LineTraverser currentTraverser;
    private PathFinder pathFinder;

    public Robot(Position currentPosition, PathFinder pathFinder) {
        this.currentPosition = currentPosition;
        this.pathFinder = pathFinder;
        currentStatus = Status.AVAILABLE;
    }

    public void update() {
        if (currentStatus == Status.PICK_UP_TASK_ASSIGNED) {
            // If destination is reached start pickup
            if (pathToTarget.getCornersPath().size() == 1) pickupProduct();
            // If movement still needed
            else moveWithLineTraverser();
        } else if (currentStatus == Status.TASK_ASSIGNED_CARRYING){
            // If delivery station already reached
            if(pathToTarget.getCornersPath().size() == 1) deliverProduct();
            // If movement still needed
            else moveWithLineTraverser();
        } else if (currentStatus == Status.MOVE_TASK_ASSIGNED){
            // If target reached, show as available
            if(pathToTarget.getCornersPath().size() == 1) currentStatus = Status.AVAILABLE;
            // If movement still needed
            else moveWithLineTraverser();
        }
    }

    private void deliverProduct(){
        if (ticksLeftForCurrentTask == 0) {
            currentStatus = Status.AVAILABLE;
        } else {
            // If still picking up the product
            ticksLeftForCurrentTask -= 1;
        }
    }

    private void pickupProduct(){
        if (ticksLeftForCurrentTask == 0) {
            currentStatus = Status.CARRYING;
        } else {
            // If still picking up the product
            ticksLeftForCurrentTask -= 1;
        }
    }

    private void moveWithLineTraverser(){
        currentTraverser.traverse();
        if (currentTraverser.destinationReached()){
            pathToTarget.getCornersPath().remove(0);

            // Create new traverser for next line in the path
            if(pathToTarget.getCornersPath().size() > 1)
                assignTraverser();
        }
    }


    public void render(SpriteBatch batch) {
        switch (currentStatus) {
            case AVAILABLE:
                batch.draw(new Texture("Simulation/Robots/robotAvailable.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                break;
            case PICK_UP_TASK_ASSIGNED:
            case MOVE_TASK_ASSIGNED:
                batch.draw(new Texture("Simulation/Robots/robotTaskAssigned.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                break;
            case TASK_ASSIGNED_CARRYING:
            case CARRYING:
                batch.draw(new Texture("Simulation/Robots/robotTaskAssignedCarrying.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                break;
            default:
                throw new RuntimeException("Robot status unavailable");
        }
    }

    public void assignTask(Task task) {
        currentTask = task;
        if(task.getAction() == Action.PICK_UP){
            currentStatus = Status.PICK_UP_TASK_ASSIGNED;
            ticksLeftForCurrentTask = pickUpTimeInTicks;
        } else if (task.getAction() == Action.DELIVER){
            if(currentStatus != Status.CARRYING) throw new IllegalArgumentException("Robot is not carrying anything");
            // If the target is not a deliveryTile
            // TODO: 08/10/2019 Throw exception, if the target is not a deliveryTile
            currentStatus = Status.TASK_ASSIGNED_CARRYING;
            ticksLeftForCurrentTask = deliverTimeInTicks;
        } else if (task.getAction() == Action.MOVE){
            currentStatus = Status.MOVE_TASK_ASSIGNED;
            ticksLeftForCurrentTask = 0;
        }

        pathToTarget = pathFinder.calculatePath(
                new GridCoordinate((int) currentPosition.getX(),(int) currentPosition.getY()), task.getDestination());

        // If the robot has to move
        if(pathToTarget.getCornersPath().size() > 1) assignTraverser();
    }

    private void assignTraverser(){
        currentTraverser = new LineTraverser(pathToTarget.getCornersPath().get(0),
                pathToTarget.getCornersPath().get(1), this);
    }

    public void cancelTask() {
        // TODO: 03/10/2019 Manage situations where the robot is in between tiles
    }

    public Position getCurrentPosition() {
        return currentPosition;
    }

    void decelerate() {
        if (currentSpeed > 0) {
            currentSpeed -= decelerationBinSecond / (float) SimulationApp.TICKS_PER_SECOND;
            if (currentSpeed < minSpeedBinsPerSecond)
                currentSpeed = minSpeedBinsPerSecond;
        }
    }

    void accelerate() {
        if (currentSpeed < maxSpeedBinsPerSecond) {
            currentSpeed += accelerationBinSecond / (float) SimulationApp.TICKS_PER_SECOND;
            if (currentSpeed > maxSpeedBinsPerSecond)
                currentSpeed = maxSpeedBinsPerSecond;
        }
    }

    public float getAccelerationBinSecond() {
        return accelerationBinSecond;
    }

    public float getDecelerationBinSecond() {
        return decelerationBinSecond;
    }

    public float getCurrentSpeed() {
        return currentSpeed;
    }

    public float getMaxSpeedBinsPerSecond() {
        return maxSpeedBinsPerSecond;
    }

    public void move(float deltaX, float deltaY) {
        currentPosition.setX(currentPosition.getX() + deltaX);
        currentPosition.setY(currentPosition.getY() + deltaY);
    }

    public float getMinimumSpeed(){
        return WarehouseSpecs.robotMinimumSpeed;
    }

    public Status getCurrentStatus() {
        return currentStatus;
    }
}
