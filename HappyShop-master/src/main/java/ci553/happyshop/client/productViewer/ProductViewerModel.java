package ci553.happyshop.client.productViewer;

import ci553.happyshop.catalogue.Product;
import ci553.happyshop.storageAccess.DatabaseRW;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ProductViewerModel {

    private final DatabaseRW db;
    private ProductViewerView view;

    private List<Product> lastResults = new ArrayList<>();

    public ProductViewerModel(DatabaseRW db) {
        this.db = db;
    }

    public void setView(ProductViewerView view) {
        this.view = view;
    }

    public void loadAllProducts() {
        if (view == null) return;

        try {
            List<Product> products = db.getAllProducts();

            lastResults.clear();
            lastResults.addAll(products);

            // convert to ProductRow list
            List<ProductViewerView.ProductRow> rows = new ArrayList<>();
            for (Product p : products) {
                rows.add(toRow(p));
            }

            // ID sort (DB already orders, but keep this for safety)
            rows.sort((a, b) -> a.id.compareToIgnoreCase(b.id));

            view.setResults(rows);          // left list
            view.showGrid(rows);            // MAIN GRID (Option 2)
            view.showStatus("Showing all products (" + rows.size() + ")");

            if (!rows.isEmpty()) {
                view.showSelected(rows.get(0));
            }

        } catch (Exception e) {
            e.printStackTrace();
            view.showStatus("Failed to load products: " + e.getMessage());
            view.setResults(List.of());
            view.showGrid(List.of());
        }
    }

    public void search(String text) {
        if (view == null) return;

        if (text == null || text.isBlank()) {
            loadAllProducts();
            return;
        }

        try {
            //returns list
            ArrayList<Product> products = db.searchProduct(text.trim());

            lastResults.clear();
            lastResults.addAll(products);

            // convert to ProductRow list
            List<ProductViewerView.ProductRow> rows = new ArrayList<>();
            for (Product p : products) {
                rows.add(toRow(p));
            }

            rows.sort((a, b) -> a.id.compareToIgnoreCase(b.id));

            if (rows.isEmpty()) {
                view.showMessage("No products found for: " + text.trim());
                view.setResults(List.of());
                view.showGrid(List.of());
                view.showStatus("0 results");
                return;
            }

            view.setResults(rows);          // left list shows matches
            view.showGrid(rows);            // grid shows matches
            view.showStatus("Results: " + rows.size());

            view.showSelected(rows.get(0));

        } catch (SQLException e) {
            view.showMessage("Search failed: " + e.getMessage());
            view.setResults(List.of());
            view.showGrid(List.of());
        }
    }

    public void select(String productId) {
        if (view == null || productId == null) return;

        for (Product p : lastResults) {
            if (productId.equals(p.getProductId())) {
                view.showSelected(toRow(p));
                return;
            }
        }
    }


    private boolean looksLikeId(String text) {
        String t = text.trim();
        if (t.length()< 3) return false;
        for (int i = 0; i < t.length(); i++) {
            if (!Character.isDigit(t.charAt(i))) return false;
        }
        return true;
    }

    private ProductViewerView.ProductRow toRow(Product p) {
        String id = p.getProductId();
        String desc = p.getProductDescription();
        String priceText = String.format("%.2f", p.getUnitPrice());
        int stock = p.getStockQuantity();
        String image = p.getProductImageName();

        String name = desc == null ? "" : desc;

        return new ProductViewerView.ProductRow(
                id,
                name,
                desc == null ? "" : desc,
                image,
                stock,
                priceText
        );
    }
}

