package dk.aau.d507e19.warehousesim.controller.path;

import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;

public class Step {

    private final GridCoordinate gridCoordinate;
    private StepType stepType;
    private long waitTimeInTicks;

    public enum StepType {
        WAITING_STEP, MOVEMENT_STEP;
    }

    public Step(GridCoordinate gridCoordinate, long waitTimeInTicks){
        this.stepType = StepType.WAITING_STEP;
        this.gridCoordinate = gridCoordinate;
        this.waitTimeInTicks = waitTimeInTicks;

        if(waitTimeInTicks == 0)
            throw new IllegalArgumentException("Wait time must not be 0; " +
                    "Omit the wait time entirely to create a movement step instead of a waiting step");
    }

    public Step(GridCoordinate gridCoordinate){
        this.stepType = StepType.MOVEMENT_STEP;
        this.gridCoordinate = gridCoordinate;

    }

    public Step(int x, int y){
        this(new GridCoordinate(x, y));
    }

    public Step(int x, int y, long waitTimeInTicks){
        this(new GridCoordinate(x, y), waitTimeInTicks);
    }

    public boolean isStepValidContinuationOf(Step previousStep){
        if(previousStep == null) return true;

        if(this.stepType == StepType.WAITING_STEP){
            boolean isPreviousStepMovementStep = previousStep.stepType == StepType.MOVEMENT_STEP;
            boolean hasSameCoords = this.gridCoordinate.equals(previousStep.gridCoordinate);
            return isPreviousStepMovementStep && hasSameCoords;
        }else{
            return this.gridCoordinate.isNeighbourOf(previousStep.gridCoordinate);
        }
    }

    public boolean isWaitingStep(){
        return stepType == StepType.WAITING_STEP;
    }

    public GridCoordinate getGridCoordinate(){
        return gridCoordinate;
    }

    public long getWaitTimeInTicks(){
        if(!isWaitingStep())
            throw new IllegalStateException("Cannot call getWaitTimeInTicks() on a non-wait step; " +
                    "This step is a " + stepType.name());

        return waitTimeInTicks;
    }

}
