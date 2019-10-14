package dk.aau.d507e19.warehousesim.storagegrid;

import dk.aau.d507e19.warehousesim.WarehouseSpecs;
import dk.aau.d507e19.warehousesim.storagegrid.product.Bin;
import dk.aau.d507e19.warehousesim.storagegrid.product.Product;
import dk.aau.d507e19.warehousesim.storagegrid.product.SKU;

import java.util.ArrayList;

public class ProductDistributor {
    private static final int totalSKUsInWarehouse = WarehouseSpecs.SKUs;
    private static final int productsInWarehouse = WarehouseSpecs.productsInStock;
    private static final int numberOfBins = WarehouseSpecs.wareHouseWidth * WarehouseSpecs.wareHouseHeight;
    private static final int productsPerBin = WarehouseSpecs.productsPerBin;
    private static final int SKUsPerBin = WarehouseSpecs.SKUsPerBin;
    private static float[][] SKUDistribution = WarehouseSpecs.skuDistribution;

    public static void distributeProducts(StorageGrid grid){
        int[][] SKUs =  calculateProductsPerSKU();

        ArrayList<Product> allProducts = generateProducts(SKUs);

        distributeToGrid(allProducts, grid);

        for (Product prod : allProducts){
            System.out.println(prod.toString());
        }
    }

    private static void distributeToGrid(ArrayList<Product> allProducts, StorageGrid grid) {
        Tile tile;
        Bin bin;

        // Run though all tiles in the warehouse
        for(int x = 0; x < WarehouseSpecs.wareHouseWidth; ++x){
            for(int y = 0; y < WarehouseSpecs.wareHouseHeight; ++y){
                tile = grid.getTile(x,y);
                if(tile instanceof BinTile){
                    // If it does not have a bin, add one
                    if(!((BinTile) tile).hasBin()) ((BinTile) tile).addBin(new Bin());

                    // Start adding products to bin
                    bin = ((BinTile) tile).getBin();
                    // Keep filling the bin
                    while(!bin.isFull() && !allProducts.isEmpty()){
                        // If the bin has the SKU and is not full, add the product.
                        if(bin.hasSKU(allProducts.get(0).getSKU()) && !bin.isFull()) {
                            bin.addProduct(allProducts.get(0));
                            allProducts.remove(0);
                        }
                        // If the bin is not full and has room for more SKUs
                        else if(!bin.isFull() && bin.hasRoomForMoreSKUs()){
                            bin.addProduct(allProducts.get(0));
                            allProducts.remove(0);
                        } else break;
                    }
                }
            }
        }

        if(!allProducts.isEmpty()) throw new RuntimeException("Not enough room for all products." +
                "Still need room for '" + allProducts.size() + "' products.");
    }

    /**
     * Get an ArrayList of all products
     * @param SKUs Array of int arrays. int[i][0] = SKU name of index i. int[i][1] = number of products for index i.
     * @return
     */
    private static ArrayList<Product> generateProducts(int[][] SKUs) {
        ArrayList<Product> allProducts = new ArrayList<>();

        for(int i = 0; i < SKUs.length; ++i){
            for(int j = 0; j < SKUs[i][1]; ++j){
                allProducts.add(new Product( new SKU(SKUs[i][0] + ""), j));
            }
        }

        return allProducts;
    }

    /**
     * Calculate how many products each SKU should have according to the distribution given in WarehouseSpec
     * @return Array of int arrays. int[i][0] = SKU name of index i. int[i][1] = number of products for index i.
     */
    public static int[][] calculateProductsPerSKU() {
        if(!isValidDistribution()) throw new IllegalArgumentException("Distribution or turnover does not equal 100%");

        // A list of all SKUs. Index 0 is SKU name as int. Index 1 is number of products for the given SKU.
        int[][] products = new int[totalSKUsInWarehouse][2];

        int numberOfDistributions = SKUDistribution.length;

        int currentSKU = 0;
        int nextSKUName = 0;
        int shouldHaveExtraProduct = 0;
        int totalSKUsCounted = 0;
        int totalProductsCounted = 0;
        // Run through other SKU distributions
        for(int i = 0; i < numberOfDistributions; ++i){
            // How many SKUs in this category (Total SKUs / SKU turnover in percent)
            int SKUsInCategory;
            // If this is the last distribution take the rest of the SKUs.
            if(i == numberOfDistributions - 1) SKUsInCategory = totalSKUsInWarehouse - totalSKUsCounted;
            else SKUsInCategory = (int)(totalSKUsInWarehouse * (SKUDistribution[i][0] / 100));

            // Products in this SKU category (Total products / SKU category turnover)
            int productsInSKUCat;
            // If this is the last distribution, take the rest of the products
            if(i == numberOfDistributions - 1) productsInSKUCat = productsInWarehouse - totalProductsCounted;
            else productsInSKUCat = (int) (productsInWarehouse * (SKUDistribution[i][1] / 100));

            // How many products does each SKU in this category have? (products in this SKU / SKUs in category)
            int productPerSKU = productsInSKUCat / SKUsInCategory;

            // If the products did not divide equally between the SKUs
            if(productPerSKU * SKUsInCategory != productsInSKUCat){
                shouldHaveExtraProduct = Math.abs((productPerSKU * SKUsInCategory) - productsInSKUCat);
            }

            // Fill in SKU name and number of products for each SKU
            for(int j = nextSKUName; j < SKUsInCategory + currentSKU; j++){
                // Give SKU a name in int
                products[j][0] = nextSKUName;
                // Assign number of products for this SKU
                if(shouldHaveExtraProduct != 0) {
                    products[j][1] = productPerSKU + 1;
                    shouldHaveExtraProduct -= 1;
                }
                else products[j][1] = productPerSKU;

                nextSKUName++;
                totalSKUsCounted++;
                totalProductsCounted += products[j][1];
            }
            currentSKU = nextSKUName;
        }

        return products;
    }

    public static boolean isValidDistribution() {
        int SKUSum = 0;
        int turnoverSum = 0;

        for(int i = 0; i < SKUDistribution.length; ++i){
            SKUSum += SKUDistribution[i][0];
            turnoverSum += SKUDistribution[i][1];
        }

        return SKUSum == 100 && turnoverSum == 100;
    }

    public static void setSKUDistribution(float[][] SKUDistribution) {
        ProductDistributor.SKUDistribution = SKUDistribution;
    }
}
