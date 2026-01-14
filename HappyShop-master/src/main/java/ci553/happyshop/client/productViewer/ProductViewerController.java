package ci553.happyshop.client.productViewer;

public class ProductViewerController {
    private final ProductViewerModel model;

    public ProductViewerController(ProductViewerModel model) {
        this.model = model;
    }

    public void search(String text) {
        model.search(text);
    }

    public void select(String productId) {
        model.select(productId);
    }
}
