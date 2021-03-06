package dk.aau.d507e19.warehousesim.controller.server.order;

import dk.aau.d507e19.warehousesim.storagegrid.product.Product;

public class OrderLine {
    private Product product;
    private int amount;

    public OrderLine(Product product, int amount) {
        this.product = product;
        this.amount = amount;
    }

    public int getAmount() {
        return amount;
    }

    public Product getProduct() {
        return product;
    }

    @Override
    public String toString() {
        return "OrderLine{" +
                "product=" + product +
                ", amount=" + amount +
                '}';
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
