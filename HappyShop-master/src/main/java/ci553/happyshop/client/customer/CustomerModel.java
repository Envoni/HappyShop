package ci553.happyshop.client.customer;

import ci553.happyshop.authentication.AuthSession;
import ci553.happyshop.authentication.UserAccount;
import ci553.happyshop.catalogue.Order;
import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.ProductListFormatter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * You can either directly modify the CustomerModel class to implement the required tasks,
 * or create a subclass of CustomerModel and override specific methods where appropriate.
 */
public class CustomerModel {
    public CustomerView cusView;
    public DatabaseRW databaseRW; //Interface type, not specific implementation
                                  //Benefits: Flexibility: Easily change the database implementation.

    private Product theProduct =null; // product found from search
    private ArrayList<Product> trolley =  new ArrayList<>(); // a list of products in trolley

    // Four UI elements to be passed to CustomerView for display updates.
    private String imageName = "imageHolder.jpg";                // Image to show in product preview (Search Page)
    private String displayLaSearchResult = "No Product was searched yet"; // Label showing search result message (Search Page)
    private String displayTaTrolley = "";                                // Text area content showing current trolley items (Trolley Page)
    private String displayTaReceipt = "";                                // Text area content showing receipt after checkout (Receipt Page)

    private String customerUsername;

    public void setCustomerUsername(String customerUsername) {
        this.customerUsername = customerUsername;
    }


    //SELECT productID, description, image, unitPrice,inStock quantity
    void search() throws SQLException {
        String productId = cusView.tfId.getText().trim();
        if (productId.isEmpty()) {
            theProduct = null;
            displayLaSearchResult = "No Product was searched yet";
            updateView();
            return;
        }
        theProduct = databaseRW.searchByProductId(productId);
        if (theProduct == null) {
            displayLaSearchResult = "No Product was found with ID " + productId;
            updateView();
            return;
        }

        double unitPrice = theProduct.getUnitPrice();
        String description = theProduct.getProductDescription();
        int stock = theProduct.getStockQuantity();
        String baseInfo = String.format("Product_Id: %s\n%s,\nPrice: £%.2f", productId, description, unitPrice);

        if (stock <= 0) {
            displayLaSearchResult = baseInfo + "\nOut of stock";
        }
        else if (stock < 100) {
            displayLaSearchResult = baseInfo + String.format("\n%d units left.", stock);
        }
        else {
            displayLaSearchResult = baseInfo;
        }
        updateView();

    }

    void addToTrolley(int qty){
        if(theProduct!= null){
            if (qty < 1) qty = 1;

            int stock = theProduct.getStockQuantity();
            if (qty < 1) qty = 1;
            if (qty > stock) {
                qty = stock;
                displayLaSearchResult = "Quantity reduced to available stock: " + stock;
            }

            Product toAdd = copyForTrolley(theProduct, qty);
            mergeIntoTrolley(toAdd);
            sortTrolleyById();

            displayTaTrolley = ProductListFormatter.buildString(trolley); //build a String for trolley so that we can show it
        }
        else{
            displayLaSearchResult = "Please search for an available product before adding it to the trolley";
            System.out.println("must search and get an available product before add to trolley");
        }
        displayTaReceipt=""; // Clear receipt to switch back to trolleyPage (receipt shows only when not empty)
        updateView();

        if (theProduct == null) {
            displayLaSearchResult = "Please search for an available product";
            updateView();
            return;
        }
        if (theProduct.getStockQuantity() <= 0) {
            displayLaSearchResult = "This product is out of stock";
            updateView();
            return;
        }
    }

    private Product copyForTrolley(Product p, int qty) {
        Product copy = new Product(
                p.getProductId(),
                p.getProductDescription(),
                p.getProductImageName(),
                p.getUnitPrice(),
                p.getStockQuantity()
        );
        copy.setOrderedQuantity(qty);
        return copy;
    }

    private void mergeIntoTrolley(Product toAdd) {
        String id = toAdd.getProductId();

        for (Product existing : trolley) {
            if (id.equals(existing.getProductId())) {
                int newQty = existing.getOrderedQuantity() + toAdd.getOrderedQuantity();
                existing.setOrderedQuantity(newQty);
                return; // merged -> done
            }
        }
        trolley.add(toAdd); // not found -> append
    }

    private void sortTrolleyById() {
        trolley.sort((a, b) -> a.getProductId().compareToIgnoreCase(b.getProductId()));
    }

    void checkOut() throws IOException, SQLException {
        if(!trolley.isEmpty()){
            UserAccount account = AuthSession.get();
            if (account == null) {
                displayLaSearchResult = "You must be logged in to checkout";
                updateView();
                return;
            }
            String username = account.getUsername();
            // Group the products in the trolley by productId to optimize stock checking
            // Check the database for sufficient stock for all products in the trolley.
            // If any products are insufficient, the update will be rolled back.
            // If all products are sufficient, the database will be updated, and insufficientProducts will be empty.
            // Note: If the trolley is already organized (merged and sorted), grouping is unnecessary.
            ArrayList<Product> insufficientProducts = databaseRW.purchaseStocks(new ArrayList<>(trolley));

            if(insufficientProducts.isEmpty()){ // If stock is sufficient for all products
                //get OrderHub and tell it to make a new Order
                OrderHub orderHub = OrderHub.getOrderHub();
                Order theOrder = orderHub.newOrder(username, new ArrayList<>(trolley));

                trolley.clear();
                displayTaTrolley ="";
                displayTaReceipt = String.format(
                        "Order_ID: %s\nOrdered_Date_Time: %s\n%s",
                        theOrder.getOrderId(),
                        theOrder.getOrderedDateTime(),
                        ProductListFormatter.buildString(theOrder.getProductList())
                );
                System.out.println(displayTaReceipt);
            }
            else{ // Some products have insufficient stock — build an error message to inform the customer
                StringBuilder errorMsg = new StringBuilder();
                for(Product p : insufficientProducts){
                    errorMsg.append("\u2022 "+ p.getProductId()).append(", ")
                            .append(p.getProductDescription()).append(" (Only ")
                            .append(p.getStockQuantity()).append(" available, ")
                            .append(p.getOrderedQuantity()).append(" requested)\n");
                }
                theProduct=null;

                StringBuilder msg = new StringBuilder();
                msg.append("Items adjusted due to insufficient stock:");

                for (Product shortage : insufficientProducts) {
                    String id = shortage.getProductId();
                    String desc = shortage.getProductDescription();

                    int available = shortage.getStockQuantity();       // available now
                    int requested = shortage.getOrderedQuantity();     // requested by customer

                    Product inTrolley = findInTrolleyById(id);
                    if (inTrolley == null) continue;

                    if (available <= 0) {
                        trolley.remove(inTrolley);
                        msg.append("• ").append(id).append(" ").append(desc)
                                .append(" removed (0 available, ").append(requested).append(" requested)\n");
                    } else {
                        inTrolley.setOrderedQuantity(available);
                        msg.append("• ").append(id).append(" ").append(desc)
                                .append(" reduced to ").append(available)
                                .append(" (requested ").append(requested).append(")\n");
                    }
                }
            }
        }
        else{
            displayTaTrolley = "Your trolley is empty";
            System.out.println("Your trolley is empty");
        }
        updateView();
    }

    /**
     * Groups products by their productId to optimize database queries and updates.
     * By grouping products, we can check the stock for a given `productId` once, rather than repeatedly
     */
    private ArrayList<Product> groupProductsById(ArrayList<Product> proList) {
        Map<String, Product> grouped = new HashMap<>();
        for (Product p : proList) {
            String id = p.getProductId();
            if (grouped.containsKey(id)) {
                Product existing = grouped.get(id);
                existing.setOrderedQuantity(existing.getOrderedQuantity() + p.getOrderedQuantity());
            } else {
                // Make a shallow copy to avoid modifying the original
                grouped.put(id,new Product(p.getProductId(),p.getProductDescription(),
                        p.getProductImageName(),p.getUnitPrice(),p.getStockQuantity()));
            }
        }
        return new ArrayList<>(grouped.values());
    }

    void cancel(){
        trolley.clear();
        displayTaTrolley="";
        updateView();
    }
    void closeReceipt(){
        displayTaReceipt="";
    }

    void updateView() {
        if(theProduct != null){
            imageName = theProduct.getProductImageName();
            String relativeImageUrl = StorageLocation.imageFolder +imageName; //relative file path, eg images/0001.jpg
            // Get the full absolute path to the image
            Path imageFullPath = Paths.get(relativeImageUrl).toAbsolutePath();
            imageName = imageFullPath.toUri().toString(); //get the image full Uri then convert to String
            System.out.println("Image absolute path: " + imageFullPath); // Debugging to ensure path is correct
        }
        else{
            imageName = "imageHolder.jpg";
        }

        if (theProduct != null) {
            cusView.setQtyMax(theProduct.getStockQuantity());
        } else {
            cusView.setQtyMax(99); // or 1 if you prefer
        }

        cusView.update(imageName, displayLaSearchResult, displayTaTrolley,displayTaReceipt);
    }

    void removeFromTrolley() {
        String productId = cusView.tfId.getText().trim();
        int qty = 1;
        try {
            // uses your spinner value if available
            qty = cusView.getSelectedQty();   // see small helper below
        } catch (Exception ignored) {}

        if (productId.isEmpty()) {
            displayLaSearchResult = "Type a Product ID to remove from trolley";
            updateView();
            return;
        }
        if (qty < 1) qty = 1;
        Product found = null;
        for (Product p : trolley) {
            if (productId.equals(p.getProductId())) {
                found = p;
                break;
            }
        }
        if (found == null) {
            displayLaSearchResult = "The product is not in your trolley: " + productId;
            updateView();
            return;
        }

        int newQty = found.getOrderedQuantity() - qty;
        if (newQty <= 0) {
            trolley.remove(found);
        } else {
            found.setOrderedQuantity(newQty);
        }
        sortTrolleyById();
        displayTaTrolley = ProductListFormatter.buildString(trolley);
        displayTaReceipt = "";
        updateView();
    }

    private Product findInTrolleyById(String id) {
        for (Product p : trolley) {
            if (p.getProductId().equals(id)) return p;
        }
        return null;
    }

    // extra notes:
     //Path.toUri(): Converts a Path object (a file or a directory path) to a URI object.
     //File.toURI(): Converts a File object (a file on the filesystem) to a URI object

    //for test only
    public ArrayList<Product> getTrolley() {
        return trolley;
    }
}
