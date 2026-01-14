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

    public void search(String text) {
        if (view == null) return;
        if (text == null || text.isEmpty()) {
            view.showMessage("Please enter a product name or ID");
            view.setResults(List.of());
            return;
        }
        try {
            Product p;
            if (looksLikeId(text)) {
                p = db.searchByProductId(text.trim());
            }
            else {
                p = db.searchByProductName(text.trim());
            }
            lastResults.clear();

            if (p == null) {
                view.showMessage("Product not found");
                view.setResults(List.of());
                return;
            }
            lastResults.add(p);
            view.setResults(List.of(toRow(p)));

        }
        catch (SQLException e) {
            view.showMessage("Search failed: " + e.getMessage());
            view.setResults(List.of());
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

