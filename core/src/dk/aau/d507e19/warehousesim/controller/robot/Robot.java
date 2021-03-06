package dk.aau.d507e19.warehousesim.controller.robot;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import dk.aau.d507e19.warehousesim.*;
import dk.aau.d507e19.warehousesim.storagegrid.BinTile;
import dk.aau.d507e19.warehousesim.storagegrid.PickerTile;
import dk.aau.d507e19.warehousesim.storagegrid.Tile;
import dk.aau.d507e19.warehousesim.storagegrid.product.Bin;
import dk.aau.d507e19.warehousesim.storagegrid.product.Product;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Robot {
    private Simulation simulation;
    private Position currentPosition;
    private Status currentStatus;
    private float currentSpeed;
    private Bin bin = null;
    private int robotID;
    private RobotController robotController;
    private long binDeliveriesCompleted = 0;
    private int distanceTraveled = 0;

    /**
     * Robot STATS
     */
    // Speed
    private final float maxSpeedBinsPerSecond = Simulation.getWarehouseSpecs().robotTopSpeed / Simulation.getWarehouseSpecs().binSizeInMeters;
    private final float accelerationBinSecond = Simulation.getWarehouseSpecs().robotAcceleration / Simulation.getWarehouseSpecs().binSizeInMeters;
    private final float decelerationBinSecond = Simulation.getWarehouseSpecs().robotDeceleration / Simulation.getWarehouseSpecs().binSizeInMeters;

    private final float breakingDistanceMaxSpeedBins = decelerationBinSecond / maxSpeedBinsPerSecond;

    private final float ROBOT_SIZE = Tile.TILE_SIZE;

    public Robot(Position startingPosition, int robotID, Simulation simulation) {
        this.currentPosition = startingPosition;
        this.simulation = simulation;
        this.robotID = robotID;
        currentStatus = Status.AVAILABLE;

        // Initialize controller for this robot
        this.robotController = new RobotController(simulation.getServer(), this, Simulation.getPathFinder());
    }

    public void update() {
        robotController.update();
    }

    public void deliverBinToPicker(GridCoordinate pickerCoords, ArrayList<Product> productsToPick) {
        for(Product product : productsToPick){
            bin.getProducts().remove(product);
        }
        PickerTile picker = (PickerTile) simulation.getStorageGrid().getTile(pickerCoords.getX(), pickerCoords.getY());
        picker.acceptProducts(productsToPick);
        simulation.incrementOrderProcessedCount();
    }

    public void putDownBin(){
        GridCoordinate coordinate = getApproximateGridCoordinate();
        Tile tile = simulation.getStorageGrid().getTile(coordinate.getX(), coordinate.getY());
        if (tile instanceof BinTile && !((BinTile) tile).hasBin()) {
            ((BinTile) tile).addBin(bin);
            bin = null;
        } else throw new RuntimeException("Robot could not put back bin at ("
                + coordinate.getX() + "," + coordinate.getY() + ")");
    }

    public void pickUpBin() {
        GridCoordinate coordinate = getApproximateGridCoordinate();
        Tile tile = simulation.getStorageGrid().getTile(coordinate.getX(), coordinate.getY());
        if (tile instanceof BinTile && ((BinTile) tile).hasBin()) {
            bin = ((BinTile) tile).releaseBin();
        } else throw new RuntimeException("Robot could not pick up bin at ("
                + coordinate.getX() + "," + coordinate.getY() + ")");
    }

    public void render(SpriteBatch batch) {
        switch (currentStatus) {
                case AVAILABLE:
                    batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotAvailable.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    break;
                case BUSY:
                    if(isCarrying())batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotTaskAssignedCarrying.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    else batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotTaskAssigned.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    break;
                case RELOCATING:
                    if(isCarrying())batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotMovingOutOfWayCarrying.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    else batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotMovingOutOfWay.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    break;
                case RELOCATING_BUSY:
                    if(isCarrying())batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotMovingOutOfWayCarrying.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    else batch.draw(GraphicsManager.getTexture("Simulation/Robots/robotMovingOutOfWayBusy.png"), currentPosition.getX(), currentPosition.getY(), Tile.TILE_SIZE, Tile.TILE_SIZE);
                    break;
                default:
                    throw new RuntimeException("Robot status unavailable");
        }
    }

    public Position getCurrentPosition() {
        return currentPosition;
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

    public Status getCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(Status currentStatus) {
        this.currentStatus = currentStatus;
    }

    public void setBin(Bin bin) {
        this.bin = bin;
    }

    public boolean collidesWith(Position collider) {
        // We have to subtract -0.01 to avoid collisions due to floating point rounding.
        boolean withInXBounds = collider.getX() >= currentPosition.getX()
                && collider.getX() <= currentPosition.getX() + ROBOT_SIZE - 0.01;
        boolean withInYBounds = collider.getY() >= currentPosition.getY()
                && collider.getY() <= currentPosition.getY() + ROBOT_SIZE - 0.01;
        return withInXBounds && withInYBounds;
    }

    public int getRobotID() {
        return robotID;
    }

    public GridCoordinate getGridCoordinate() {
        GridCoordinate gridCoordinate =
                new GridCoordinate(Math.round(currentPosition.getX()), Math.round(currentPosition.getY()));

        if (!currentPosition.isSameAs(gridCoordinate))
            throw new IllegalStateException("Robot is not at the center of a tile. Current position : "
                    + currentPosition
                    + "\n If you want an approximate grid position use getApproximateGridCoordinate()");

        return gridCoordinate;
    }

    public GridCoordinate getApproximateGridCoordinate() {
        return new GridCoordinate(Math.round(currentPosition.getX()), Math.round(currentPosition.getY()));
    }

    public boolean isCarrying(){
        return bin != null;
    }

    public void updatePosition(Position newPosition, float newSpeed){
        currentPosition = newPosition;
        currentSpeed = newSpeed;
    }

    public int getSize() {
        return 1;
    }

    public Bin getBin() {
        return bin;
    }

    public RobotController getRobotController() {
        return robotController;
    }

    public String getStatsAsCSV(){
        StringBuilder builder = new StringBuilder();
        // Robot ID
        builder.append(robotID).append(',');

        // Deliveries completed
        builder.append(binDeliveriesCompleted).append(',');

        DecimalFormat df = new DecimalFormat("#,000");
        df.setRoundingMode(RoundingMode.HALF_UP);
        df.setGroupingUsed(false);
        // Distance traveled in meters
        builder.append(df.format(getDistanceTraveledInMeters())).append(',');

        // Idle time
        DecimalFormat df2 = (DecimalFormat) NumberFormat.getNumberInstance(Locale.US);
        df2.setRoundingMode(RoundingMode.HALF_UP);
        df2.setGroupingUsed(false);
        df2.applyPattern("###.00");
        builder.append(df2.format(getIdleTimeInSeconds()));

        return builder.toString();
    }

    public void incrementDeliveriesCompleted(){
        binDeliveriesCompleted++;
    }

    public void addToDistanceTraveled(int extraDistance){
        distanceTraveled += extraDistance;
    }

    public double getDistanceTraveledInMeters(){
        return distanceTraveled * Simulation.getWarehouseSpecs().binSizeInMeters;
    }

    public double getIdleTimeInSeconds(){
        return (double)robotController.getIdleTimeTicks() / SimulationApp.TICKS_PER_SECOND;
    }

    public long getBinDeliveriesCompleted() {
        return binDeliveriesCompleted;
    }

    public void renderPriority(SpriteBatch batch, OrthographicCamera worldCamera, OrthographicCamera fontCamera) {
        float offset = (robotController.getRobot().getSize()) / 2f;
        Vector3 screenPosition = worldCamera.project(new Vector3(currentPosition.getX() + offset, currentPosition.getY() + offset, 0));
        screenPosition = fontCamera.unproject(new Vector3(screenPosition.x, Gdx.graphics.getHeight() - screenPosition.y, screenPosition.z));
        GraphicsManager.getFont().setColor(Color.WHITE);
        GraphicsManager.getFont().draw(batch, robotController.getTicksSinceTaskAssigned() + "",
                screenPosition.x, screenPosition.y);
    }

    public void setRobotID(int robotID) {
        this.robotID = robotID;
    }
}
