package dk.aau.d507e19.warehousesim.controller.pathAlgorithms;

import java.util.ArrayList;

public class AStarTile {
    private int currentXPosition;
    private int currentYPosition;
    private int previousXposition;
    private int getPreviousYposition;
    private int H;
    private int G = 0;
    private int F;
    private boolean isBlocked = false;
    private ArrayList<Reservation> listOfResevations = new ArrayList<>();

    public AStarTile(int currentXPosition, int currentYPosition) {
        this.currentXPosition = currentXPosition;
        this.currentYPosition = currentYPosition;
    }

    //Setters

    public void setPreviousXposition(int previousXposition) {
        this.previousXposition = previousXposition;
    }

    public void setPreviousYposition(int getPreviousYposition) {
        this.getPreviousYposition = getPreviousYposition;
    }

    public void setBlocked(boolean blocked) {
        isBlocked = blocked;
    }

    //Getters

    public int getCurrentXPosition() {
        return currentXPosition;
    }

    public int getCurrentYPosition() {
        return currentYPosition;
    }

    public int getPreviousXposition() {
        return previousXposition;
    }

    public int getGetPreviousYposition() {
        return getPreviousYposition;
    }

    public int getG() {
        return G;
    }

    public int getH() {
        return H;
    }

    public int getF() {
        return F;
    }

    //Methods

    public boolean isBlocked() {
        return isBlocked;
    }

    public void calculateH(int xFinalPosition, int yFinalPosition) {

        H = Math.abs((xFinalPosition - currentXPosition)) + Math.abs((yFinalPosition - currentYPosition));

    }

    public int calculateG(int previousG) {
        G = previousG + 1;
        return G;
    }

    public int calculateF() {

        F = G + H;
        return F;
    }

    public void addReservationoList(Reservation reservation) {

        listOfResevations.add(reservation);

    }

    public void removeReservation(Reservation reservation) {
        for (Reservation reservationInList : listOfResevations) {
            if(reservationInList.equals(reservation))
                listOfResevations.remove(reservationInList);

        }
    }

    @Override
    public String toString() {
        return "Tile{" +
                "currentXPosition=" + currentXPosition +
                ", currentYPosition=" + currentYPosition +
                ", previousXposition=" + previousXposition +
                ", getPreviousYposition=" + getPreviousYposition +
                ", H=" + H +
                ", G=" + G +
                ", F=" + F +
                ", isBlocked=" + isBlocked +
                '}';
    }
}