package dk.aau.d507e19.warehousesim.controller.server;

import dk.aau.d507e19.warehousesim.controller.robot.Robot;
import dk.aau.d507e19.warehousesim.controller.robot.plan.task.BinDelivery;
import dk.aau.d507e19.warehousesim.controller.robot.plan.task.Task;
import dk.aau.d507e19.warehousesim.controller.server.order.Order;
import dk.aau.d507e19.warehousesim.controller.server.order.OrderLine;
import dk.aau.d507e19.warehousesim.controller.server.taskAllocator.DummyTaskAllocator;
import dk.aau.d507e19.warehousesim.controller.server.taskAllocator.NaiveShortestDistanceTaskAllocator;
import dk.aau.d507e19.warehousesim.controller.server.taskAllocator.ShortestDistanceTaskAllocator;
import dk.aau.d507e19.warehousesim.controller.server.taskAllocator.TaskAllocator;
import dk.aau.d507e19.warehousesim.storagegrid.BinTile;
import dk.aau.d507e19.warehousesim.storagegrid.PickerTile;
import dk.aau.d507e19.warehousesim.storagegrid.StorageGrid;
import dk.aau.d507e19.warehousesim.storagegrid.product.Product;

import java.util.*;

public class OrderManager {
    private ArrayList<Order> orderQueue = new ArrayList<>();
    private ArrayList<Order> ordersFinished = new ArrayList<>();
    private Server server;
    private TaskAllocator taskAllocator;
    private ArrayList<Task> tasksQueue = new ArrayList<>();
    private HashMap<Order, ArrayList<Task>> processingOrdersToTaskMap = new HashMap<>();

    OrderManager(Server server) {
        this.server = server;
        this.taskAllocator = server.getSimulation().getSimulationApp().getTaskAllocatorSelected().getTaskAllocator(server.getSimulation().getStorageGrid());
    }

    public void takeOrder(Order order){
        this.orderQueue.add(order);
    }

    private void checkIfOrdersAreCompleted() {
        boolean isCompleted;
        ArrayList<Order> ordersToRemove = new ArrayList<>();
        for(Order order : processingOrdersToTaskMap.keySet()){
            isCompleted = true;
            if(!processingOrdersToTaskMap.get(order).isEmpty()){
                for(Task task : processingOrdersToTaskMap.get(order)){
                    if(!task.isCompleted()) isCompleted = false;
                }

                if(isCompleted){
                    order.getPicker().setAvailable();
                    ordersFinished.add(order);
                    ordersToRemove.add(order);
                }
            }
        }

        for(Order order : ordersToRemove){
            processingOrdersToTaskMap.remove(order);
        }
    }


    public void update(){
        checkIfOrdersAreCompleted();

        ArrayList<PickerTile> availablePickers = server.getAvailablePickers();

        int maxOrderAssignPerTick = 50;
        Iterator<Order> orderIterator = orderQueue.iterator();

        while(orderIterator.hasNext() && !availablePickers.isEmpty() && maxOrderAssignPerTick != 0){
            Order order = orderIterator.next();
            // Assign order to picker
            availablePickers.get(0).assignOrder(order);
            // Give order a reference to picker
            order.setPicker(availablePickers.get(0));
            // Divide order into tasks to robots and add to list of available tasks
            ArrayList<Task> tasksFromOrder = createTasksFromOrder(order);
            if(tasksFromOrder != null){
                processingOrdersToTaskMap.put(order, tasksFromOrder);
                tasksQueue.addAll(tasksFromOrder);
                System.out.println("Commenced order: " + orderQueue.get(0));
                // Remove order from queue
                orderIterator.remove();
                // Picker is no longer available
                availablePickers.remove(0);
            }
            // If order could not be divided into tasks, it is not assigned to a picker
            // And not put into the processing ArrayList
            else {
                availablePickers.get(0).setAvailable();
                order.removePicker();
            }
            maxOrderAssignPerTick--;
        }

        if(server.hasAvailableRobot()) {
            Iterator<Task> taskIterator = tasksQueue.iterator();
            while(taskIterator.hasNext()){
                Task task = taskIterator.next();
                Optional<Robot> optimalRobot = taskAllocator.findOptimalRobot(server.getAllRobots(), task);
                if(optimalRobot.isPresent()){
                    if(optimalRobot.get().getRobotController().assignTask(task)){
                        task.setRobot(optimalRobot.get());
                        taskIterator.remove();
                    }
                }
            }
        }
    }

    private ArrayList<Task> createTasksFromOrder(Order order) {
        ArrayList<Task> orderTasks = new ArrayList<>();

        // Go through all lines in order
        for(OrderLine line : order.getLinesInOrder()){
            // Split each line into tasks
            ArrayList<Task> orderLineTasks = splitIntoTasks(line, order);
            if(orderLineTasks == null) return null;
            orderTasks.addAll(orderLineTasks);
        }

        // Reserve the bin for all tasks
        for(Task task : orderTasks)
            server.getReservationManager().reserveBinTile(((BinDelivery)task).getBinCoords());

        return orderTasks;
    }

    private ArrayList<Task> splitIntoTasks(OrderLine line, Order order) {
        Product product = line.getProduct();

        StorageGrid storageGrid = server.getSimulation().getStorageGrid();
        ArrayList<BinTile> binTiles = storageGrid.tilesWithProduct(line.getProduct());
        binTiles.sort(Comparator.comparingInt(tile -> tile.getBin().productCount(product)));

        ArrayList<Task> productTasks = new ArrayList<>();

        int remainingProducts = line.getAmount();
        for(BinTile tile : binTiles){
            if(server.getReservationManager().isBinReserved(tile.getGridCoordinate())) continue;
            remainingProducts -= tile.getBin().productCount(product);
            productTasks.add(new BinDelivery(order, tile.getGridCoordinate()));
            if(remainingProducts <= 0) break;
        }

        if(remainingProducts > 0) return null;

        return productTasks;
    }

    public int ordersInQueue(){
        return orderQueue.size();
    }

    public int ordersFinished(){
        return ordersFinished.size();
    }

    public int tasksInQueue(){
        int tasksNotComplete = 0;
        for(ArrayList<Task> taskArray : processingOrdersToTaskMap.values()){
            for(Task task : taskArray){
                if(!task.isCompleted()) tasksNotComplete++;
            }
        }
        return tasksNotComplete;
    }
}
