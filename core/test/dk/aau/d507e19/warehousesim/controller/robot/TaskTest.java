package dk.aau.d507e19.warehousesim.controller.robot;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.*;

public class TaskTest {

    @Test
    public void removeAllButCornersPathTest() {
        ArrayList<GridCoordinate> allCoordinates = new ArrayList<>();
        ArrayList<GridCoordinate> expectedStrippedCoordinates = new ArrayList<>();

        // Full path
        Collections.addAll(allCoordinates,
                new GridCoordinate(1, 1),
                new GridCoordinate(1, 2),
                new GridCoordinate(1, 3),
                new GridCoordinate(2, 3),
                new GridCoordinate(2, 4),
                new GridCoordinate(3, 4),
                new GridCoordinate(4, 4),
                new GridCoordinate(4, 3),
                new GridCoordinate(4, 2),
                new GridCoordinate(4, 1));

        // Only the corners
        Collections.addAll(expectedStrippedCoordinates,
                new GridCoordinate(1, 3),
                new GridCoordinate(2, 3),
                new GridCoordinate(2, 4),
                new GridCoordinate(4, 4),
                new GridCoordinate(4, 1));

        Task task = new Task(allCoordinates, Action.NONE);
        assertEquals(expectedStrippedCoordinates, task.getStrippedPath());
    }

    @Test (expected = IllegalArgumentException.class)
    public void removeAllButCornersZeroLenTest() {
        ArrayList<GridCoordinate> allCoordinates = new ArrayList<>();
        Task task = new Task(allCoordinates, Action.NONE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void noncontinuousStraightPathTest() {
        ArrayList<GridCoordinate> allCoordinates = new ArrayList<>();
        allCoordinates.add(new GridCoordinate(1, 1));
        allCoordinates.add(new GridCoordinate(2, 1));
        allCoordinates.add(new GridCoordinate(4, 1));
        Task task = new Task(allCoordinates, Action.NONE);
    }

    @Test (expected = IllegalArgumentException.class)
    public void noncontinuousCornerPathTest() {
        ArrayList<GridCoordinate> allCoordinates = new ArrayList<>();
        allCoordinates.add(new GridCoordinate(1, 1));
        allCoordinates.add(new GridCoordinate(2, 1));
        allCoordinates.add(new GridCoordinate(3, 1));
        allCoordinates.add(new GridCoordinate(3, 3));
        Task task = new Task(allCoordinates, Action.NONE);
    }


    public void removeAllButCornersOneLenTest() {
        ArrayList<GridCoordinate> allCoordinates = new ArrayList<>();
        allCoordinates.add(new GridCoordinate(0,0));
        Task task = new Task(allCoordinates, Action.NONE);
        assertTrue(task.getStrippedPath().isEmpty());
    }

    @Test
    public void getPath() {
    }

    @Test
    public void getAction() {
    }

    @Test
    public void getTarget() {
    }

    @Test
    public void isValidPath() {
    }
}