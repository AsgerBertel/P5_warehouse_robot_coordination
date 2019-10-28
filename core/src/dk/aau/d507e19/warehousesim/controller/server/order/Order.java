package dk.aau.d507e19.warehousesim.controller.server.order;

import dk.aau.d507e19.warehousesim.storagegrid.PickerTile;
import dk.aau.d507e19.warehousesim.storagegrid.product.Product;

import java.util.ArrayList;

public class Order {
    private ArrayList<OrderLine> linesInOrder;
    private PickerTile picker;

    public Order(ArrayList<OrderLine> linesInOrder) {
        this.linesInOrder = linesInOrder;
    }

    public ArrayList<OrderLine> getLinesInOrder() {
        return linesInOrder;
    }

    public PickerTile getPicker() {
        return picker;
    }

    public ArrayList<Product> getAllProductsInOrder(){
        ArrayList<Product> productsInOrder = new ArrayList<>();
        for(OrderLine line : getLinesInOrder()){
            for(int i = 0; i < line.getAmount(); i++){
                productsInOrder.add(line.getProduct());
            }
        }

        return productsInOrder;
    }

    public void setPicker(PickerTile picker) {
        this.picker = picker;
    }

    @Override
    public String toString() {
        return "Order{" +
                "linesInOrder=" + linesInOrder +
                ", picker=" + picker +
                '}';
    }
}