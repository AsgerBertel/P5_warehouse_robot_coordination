package dk.aau.d507e19.warehousesim.controller.robot;

import dk.aau.d507e19.warehousesim.storagegrid.product.Product;

public class Order {
    private Product product;
    private int amount;

    public Order(Product product, int amount) {
        this.product = product;
        this.amount = amount;
    }

}