package dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt;

import dk.aau.d507e19.warehousesim.RunConfigurator;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.Node;
import dk.aau.d507e19.warehousesim.controller.pathAlgorithms.rrt.RRT;
import dk.aau.d507e19.warehousesim.controller.robot.GridCoordinate;
import dk.aau.d507e19.warehousesim.controller.robot.Robot;
import dk.aau.d507e19.warehousesim.controller.robot.RobotController;
import dk.aau.d507e19.warehousesim.controller.server.ReservationManager;
import dk.aau.d507e19.warehousesim.controller.server.Server;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.mockito.Mockito;


public class NodeTest {
    Node<Object> root, rootLeft, rootRight, rootLeftLeft, rootLeftLeftLeft, rootLeftLeftLeftLeft;
    int count =0;
    Server server = Mockito.mock(Server.class);
    private RobotController robotController = Mockito.mock(RobotController.class);
    private Robot robot = Mockito.mock(Robot.class);
    private ReservationManager reservationManager = Mockito.mock(ReservationManager.class);


    @Before
    public void makeTree() {
        RunConfigurator.setDefaultRunConfiguration();
        root = new Node<>(new Object(), null, false);
        rootLeft = new Node<>(new Object(), root, false);
        rootRight = new Node<>(new Object(), root,false );
        rootLeftLeft = new Node<>(new Object(), rootLeft,false);
        rootLeftLeftLeft = new Node<>(new Object(), rootLeftLeft, false);
        rootLeftLeftLeftLeft = new Node<>(new Object(), rootLeftLeftLeft, false);
    }

    @Test
    public void setParentTest() {
        //create node without a parent
        Node<Object> node1 = new Node<>(new Object(), null, false);
        assertNull(node1.getParent());
        //set root as node1's parent
        node1.setParent(root);
        assertTrue(root.getChildren().contains(node1));
        //set rootLeft to be parent instead
        node1.setParent(rootLeft);
        assertTrue(rootLeft.getChildren().contains(node1));
        assertFalse(root.getChildren().contains(node1));
    }

    @Test
    public void makeRootTest() {
        rootLeftLeftLeftLeft.makeRoot();
        assertNull(rootLeftLeftLeftLeft.getParent());
        //Assert that the old parent is now a child to the old child
        assertTrue(rootLeftLeftLeftLeft.getChildren().contains(rootLeftLeftLeft));
        assertEquals(rootLeftLeftLeftLeft, rootLeftLeftLeft.getParent());
        //Continue for the rest of the tree, until the old root is hit
        assertTrue(rootLeftLeftLeft.getChildren().contains(rootLeftLeft));
        assertEquals(rootLeftLeftLeft, rootLeftLeft.getParent());

        assertTrue(rootLeftLeft.getChildren().contains(rootLeft));
        assertEquals(rootLeftLeft, rootLeft.getParent());

        assertTrue(rootLeft.getChildren().contains(root));
        assertEquals(rootLeft, root.getParent());

        //Assert that parent and child is swapped
        assertFalse(rootLeftLeft.getChildren().contains(rootLeftLeftLeft));
        assertNotEquals(rootLeftLeft, rootLeftLeftLeft.getParent());

    }

    @Test
    public void removeChildTest() {
        assertTrue(root.getChildren().contains(rootRight));
        assertEquals(root, rootRight.getParent());
        root.removeChild(rootRight);
        assertFalse(root.getChildren().contains(rootRight));
        assertNotEquals(rootRight.getParent(), root);
    }

    @Test
    public void nodeTest() {
        Node<Object> node1 = new Node<>(new Object(), null, false);
        Node<Object> node2 = new Node<>(new Object(), node1, false);
        assertTrue(node1.getChildren().contains(node2));
        assertEquals(node1, node2.getParent());
    }

    @Test
    public void getRootTest(){
        assertEquals(root,root.getRoot());
        assertEquals(root,rootLeftLeftLeftLeft.getRoot());
        assertEquals(rootLeftLeft.getRoot(),rootRight.getRoot());
    }

    @Test
    public void findNodeTest(){
        //check if we can find every single node in tree.
        assertEquals(root,root.findNode(root.getData()));
        assertEquals(rootLeft,root.findNode(rootLeft.getData()));
        assertEquals(rootRight,root.findNode(rootRight.getData()));
        assertEquals(rootLeftLeft,root.findNode(rootLeftLeft.getData()));
        assertEquals(rootLeftLeftLeft,root.findNode(rootLeftLeftLeft.getData()));
        //make sure we cant find a node that is not in the tree
        //assertNull(root.findNode(new GridCoordinate(15,18)));
    }

    @Test
    public void testCopy1(){
        //create new Node
        Node<Object> n0 = root.copy();
        //check that they are equal(their data + children is same)
        assertEquals(n0,root);
        //check that they are not the same object!
        assertNotEquals(n0.hashCode(),root.hashCode());
        //check that both have the same amount of children
        assertEquals(n0.getChildren().size(),root.getChildren().size());
        //check that two first children are the same, but not the same object
        assertEquals(n0.getChildren().get(0),root.getChildren().get(0));
        assertNotEquals(n0.getChildren().get(0).hashCode(),root.getChildren().get(0).hashCode());
    }
    @Test
    public void testCopy2(){
        when(robotController.getServer()).thenReturn(server);
        when(robotController.getServer().getReservationManager()).thenReturn(reservationManager);
        when(robotController.getRobot()).thenReturn(robot);
        when(robotController.getRobot().getGridCoordinate()).thenReturn(new GridCoordinate(0,0));
        RRT rrt = new RRT(robotController);
        GridCoordinate gcStart = new GridCoordinate(0,0);
        GridCoordinate gcDest = new GridCoordinate(15,29);
        rrt.generateRRTPathFromEmpty(gcStart, gcDest);
        Node<GridCoordinate> root = rrt.allNodesMap.get(gcStart);
        Node<GridCoordinate> rootCopy = root.copy();
        //loop through all nodes in rootCopy and check that they exist
        copyLoop(rrt,rootCopy);
        assertEquals(count,rrt.allNodesMap.size());

    }
    private void copyLoop(RRT rrt, Node<GridCoordinate> node){
        count++;
        for(Node<GridCoordinate> n : node.getChildren()){
            copyLoop(rrt,n);
            assertTrue(rrt.allNodesMap.containsKey(n.getData()));
        }
    }


}
