package ci553.happyshop.client.productViewer;
import ci553.happyshop.ui.AppTheme;
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

    private ProductViewerModel model;

    private final javafx.scene.layout.TilePane grid = new javafx.scene.layout.TilePane();
    private final javafx.scene.control.ScrollPane gridScroll = new javafx.scene.control.ScrollPane(grid);

    private final Label status = new Label();

    public void setController(ProductViewerController controller) {
        this.controller = controller;
    }

    public void setModel(ProductViewerModel model) {
        this.model = model;
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

        lvResults.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row == null) return;
            if (controller != null) controller.select(row.id);
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

        taDesc.setPrefRowCount(6);
        taDesc.setPrefHeight(150);

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPrefColumns(4);                // grid columns (adjust)
        grid.getStyleClass().add("pv-grid");

        gridScroll.setFitToWidth(true);
        gridScroll.setFitToHeight(true);
        gridScroll.getStyleClass().add("pv-grid-scroll");

        BorderPane root = new BorderPane();
        root.setTop(top);
        root.setLeft(left);
        root.setPadding(new Insets(12));
        root.getStyleClass().add("product-viewer-root");
        status.getStyleClass().add("pv-status");
        root.setBottom(status);
        BorderPane.setMargin(status, new Insets(8, 0, 0, 0));

        root.setCenter(gridScroll);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        AppTheme.register(scene);
        window.setTitle("HappyShop - Product Viewer");
        window.setScene(scene);
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        if (model != null) model.loadAllProducts();
        window.show();
    }

    public void setResults(java.util.List<ProductRow> rows) {
        lvResults.getItems().setAll(rows);
        showGrid(rows); // IMPORTANT: populate the grid from the same data
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
        taDesc.setText(row.fullDescription == null ? "" : row.fullDescription);

        String uri = toImageUri(row.imageName, row.id);
        ivProduct.setImage(uri == null? null : new Image(uri, true));

    }

    public void showMessage(String msg) {
        lbTitle.setText(msg);
    }

    private String toImageUri(String imageName, String productId) {

        //DB image name
        String[] candidates = new String[] {
                clean(imageName),
                clean(productId) + ".jpg",
                clean(productId) + ".png"
        };

        for (String file : candidates) {
            if (file == null || file.isBlank()) continue;

            //classpath: src/main/resources/<file>
            var r0 = getClass().getResource("/" + file);
            if (r0 != null) return r0.toExternalForm();

            //classpath: src/main/resources/images/<file>
            var r1 = getClass().getResource("/images/" + file);
            if (r1 != null) return r1.toExternalForm();

            //disk: <project>/images/<file>
            try {
                Path wd = Paths.get(System.getProperty("user.dir"));

                Path p1 = wd.resolve("images").resolve(file).toAbsolutePath();
                if (java.nio.file.Files.exists(p1)) return p1.toUri().toString();

                //disk: <project>/src/main/resources/<file>
                Path p2 = wd.resolve("src/main/resources").resolve(file).toAbsolutePath();
                if (java.nio.file.Files.exists(p2)) return p2.toUri().toString();

                //disk: <project>/src/main/resources/images/<file>
                Path p3 = wd.resolve("src/main/resources/images").resolve(file).toAbsolutePath();
                if (java.nio.file.Files.exists(p3)) return p3.toUri().toString();

            } catch (Exception ignored) { }
        }

        return null;
    }

    private String clean(String s) {
        if (s == null) return null;
        String t = s.trim().replace("\\", "/");
        while (t.startsWith("/")) t = t.substring(1);
        return t;
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

    public void showGrid(java.util.List<ProductRow> rows) {
        grid.getChildren().clear();
        if (rows == null || rows.isEmpty()) return;

        for (ProductRow r : rows) {
            VBox card = new VBox(6);
            card.getStyleClass().add("pv-card");
            ImageView cardImg = new ImageView();
            cardImg.setFitWidth(110);
            cardImg.setFitHeight(110);
            cardImg.setPreserveRatio(true);
            cardImg.setSmooth(true);
            cardImg.getStyleClass().add("pv-card-image");

            try {
                String uri = toImageUri(r.imageName, r.id);
                ivProduct.setImage(uri == null? null : new Image(uri, true));
            } catch (Exception ignored) {}

            Label id = new Label(r.id);
            id.getStyleClass().add("pv-card-id");
            Label name = new Label(r.name);
            name.setWrapText(true);
            name.getStyleClass().add("pv-card-name");
            Label price = new Label("£" + r.priceText);
            price.getStyleClass().add("pv-card-meta");
            Label stock = new Label("Stock: " + r.stock);
            stock.getStyleClass().add("pv-card-meta");
            card.getChildren().addAll(cardImg, id, name, price, stock);

            card.setOnMouseClicked(e -> {
                if (controller != null) controller.select(r.id);
            });

        grid.getChildren().add(card);
        }
    }

    public void showStatus(String msg) {
        status.setText(msg == null ? "" : msg);
    }
}
