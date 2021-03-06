package dk.aau.d507e19.warehousesim.storagegrid.product;

import dk.aau.d507e19.warehousesim.Simulation;
import dk.aau.d507e19.warehousesim.WarehouseSpecs;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Bin {
    ArrayList<Product> products = new ArrayList<>();

    public Bin() {
    }

    public Bin(ArrayList<Product> products) {
        addProducts(products);
    }

    public void addProducts(ArrayList<Product> newProducts){
        // Go through all products
        for(Product newProd : newProducts){
            addProduct(newProd);
        }
    }

    public void addProduct(Product newProd){
        // If the number of SKUs per bin is already full, and the new product has a new SKU
        if(getSKUs().size() == Simulation.getWarehouseSpecs().SKUsPerBin && !hasSKU(newProd.getSKU())){
            throw new IllegalArgumentException("Cannot add more SKUs to bin. Already have '" + getSKUs().size()
                    + "' out of '" + Simulation.getWarehouseSpecs().SKUsPerBin + "'");
        }
        // If the bin is already full of products
        else if(products.size() == Simulation.getWarehouseSpecs().productsPerBin){
            throw new IllegalArgumentException("Cannot add more products to bin. Already has '" + products.size()
                    + "' out of '" + Simulation.getWarehouseSpecs().productsPerBin + "'");
        } else {
            products.add(newProd);
        }
    }

    public boolean removeProduct(Product prod){
        if(!products.contains(prod)) return false;
        this.products.remove(prod);
        return true;
    }

    public void removeProducts(ArrayList<Product> products){
        for(Product prod : products){
            this.products.remove(prod);
        }
    }

    private ArrayList<SKU> getSKUs(){
        ArrayList<SKU> SKUs = new ArrayList<>();
        for (Product prod : products) {
            if(!SKUs.contains(prod.SKU)){
                SKUs.add(prod.SKU);
            }
        }
        return SKUs;
    }

    public boolean isFull(){
        return products.size() == Simulation.getWarehouseSpecs().productsPerBin;
    }

    public boolean hasSKU(SKU sku){
        for (Product prod : products) {
            if(prod.SKU.equals(sku)) return true;
        }
        return false;
    }

    public boolean hasRoomForMoreSKUs(){
        ArrayList<SKU> SKUs = new ArrayList<>();
        for (Product prod : products) {
            if(!SKUs.contains(prod.SKU)) SKUs.add(prod.SKU);
        }

        if(SKUs.size() < 9) return true;

        return false;
    }

    @Override
    public String toString() {
        if(products.isEmpty()){
            return "Bin{}";
        } else {
            String s;
            s = "Bin{\n";
            for (Product prod : products) {
                s = s.concat(prod + "\n");
            }
            s = s.concat("}");
            return s;
        }
    }

    public ArrayList<Product> getProducts() {
        return products;
    }

    public boolean hasProduct(Product product){
        for(Product prod : products){
            if(prod.equals(product)) return true;
        }
        return false;
    }

    public int productCount(Product product) {
        int count = 0;
        for(Product prod : products){
            if(prod.equals(product)) count++;
        }
        return count;
    }

    public boolean isEmpty(){
        return products.isEmpty();
    }
}
