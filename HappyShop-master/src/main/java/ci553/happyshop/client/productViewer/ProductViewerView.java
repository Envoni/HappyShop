package ci553.happyshop.client.productViewer;
import ci553.happyshop.ui.AppTheme;
import ci553.happyshop.utility.StorageLocation;
import ci553.happyshop.utility.WinPosManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.nio.file.Path;
import java.nio.file.Paths;

public class ProductViewerView {
    private final int WIDTH = 800;
    private final int HEIGHT = 600;

    private final TextField tfSearch = new TextField();
    private final Button btnSearch = new Button("Search");
    private final ListView<ProductRow> lvResults = new ListView<>();

    private final ImageView ivProduct = new ImageView();
    private final Label lbTitle = new Label("Select a product");
    private final Label lbPrice = new Label("");
    private final Label lbStock = new Label("");
    private final TextArea taDesc = new TextArea();

    private ProductViewerController controller;

    public void setController(ProductViewerController controller) {
        this.controller = controller;
    }

    public void start(Stage window) {
        Label heading = new Label("Product Viewer");
        heading.getStyleClass().add("pv-title");
        tfSearch.setPromptText("Search");
        tfSearch.getStyleClass().add("pv-search");
        btnSearch.getStyleClass().add("pv-button");
        btnSearch.setOnAction(e -> {
            if (controller != null) controller.search(tfSearch.getText().trim());
        });

        HBox top = new HBox(10,heading, tfSearch, btnSearch);
        top.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(tfSearch, Priority.ALWAYS);
        top.getStyleClass().add("pv-top");

        lvResults.setPlaceholder(new Label("No products found"));
        lvResults.getStyleClass().add("pv-list");
        lvResults.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(ProductRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item.id + " " + item.name + " " + item.priceText + " " + item.stock);
            }
        });

        VBox left = new VBox(10, new Label("Results"), lvResults);
        left.getStyleClass().add("pv-left");
        VBox.setVgrow(lvResults, Priority.ALWAYS);

        ivProduct.setFitWidth(200);
        ivProduct.setFitHeight(200);
        ivProduct.setPreserveRatio(true);
        ivProduct.getStyleClass().add("pv-image");

        lbTitle.getStyleClass().add("pv-detail-title");
        lbPrice.getStyleClass().add("pv-detail-line");
        lbStock.getStyleClass().add("pv-detail-line");

        taDesc.setEditable(false);
        taDesc.setWrapText(true);
        taDesc.getStyleClass().add("pv-desc");
        taDesc.setPrefRowCount(6);
        VBox right = new VBox(10, ivProduct, lbTitle, lbPrice, lbStock, taDesc);
        right.getStyleClass().add("pv-right");
        VBox.setVgrow(taDesc, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(left);
        root.setCenter(right);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("product-viewer-root");
        Scene scene = new Scene(root, WIDTH, HEIGHT);
        AppTheme.register(scene);
        window.setTitle("HappyShop - Product Viewer");
        window.setScene(scene);
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
    }

    public void setResults(java.util.List<ProductRow> rows) {
        lvResults.getItems().setAll(rows);
        if (!rows.isEmpty()) lvResults.getSelectionModel().selectFirst();
    }

    public void showSelected(ProductRow row) {
        if (row == null) {
            lbTitle.setText("Select a product");
            lbPrice.setText("");
            lbStock.setText("");
            taDesc.setText("");
            ivProduct.setImage(null);
            return;
        }

        lbTitle.setText(row.id + " - " + row.name);
        lbPrice.setText("Price: £" + row.priceText);
        lbStock.setText(row.stock <= 0 ? "Stock: 0 " : "Stock: " + row.stock);

        //load image
        try {
            String img = row.imageName;
            if (img == null || img.isBlank()) {
                ivProduct.setImage(null);
                return;
            }
            String relative = StorageLocation.imageFolder + img;
            Path full = Paths.get(relative).toAbsolutePath();
            ivProduct.setImage(new Image(full.toUri().toString(), true));
        } catch (Exception ex) {
            ivProduct.setImage(null);
        }
    }

    public void showMessage(String msg) {
        lbTitle.setText(msg);
    }

    public static final class ProductRow {
        public final String id;
        public final String name;
        public final String fullDescription;
        public final String imageName;
        public final int stock;
        public final String priceText;

        public ProductRow(String id, String name, String fullDescription, String imageName, int stock, String priceText) {
            this.id = id;
            this.name = name;
            this.fullDescription = fullDescription;
            this.imageName = imageName;
            this.stock = stock;
            this.priceText = priceText;
        }
    }
}
