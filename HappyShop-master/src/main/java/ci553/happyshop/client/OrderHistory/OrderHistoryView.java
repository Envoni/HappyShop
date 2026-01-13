package ci553.happyshop.client.OrderHistory;

import ci553.happyshop.authentication.AuthSession;
import ci553.happyshop.authentication.UserRole;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.orderManagement.OrderState;
import ci553.happyshop.ui.AppTheme;
import ci553.happyshop.utility.UIStyle;
import ci553.happyshop.utility.WinPosManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.Map;
import java.util.TreeMap;

import ci553.happyshop.storageAccess.OrderFileManager;
import ci553.happyshop.utility.StorageLocation;


public class OrderHistoryView {

    private final int WIDTH = 400;
    private final int HEIGHT = 400;

    private final TextArea taHistory = new TextArea();
    private TextField tfUserSearch; // only used for staff/admin
    private String defaultUsername;

    private final ListView<OrderRow> lvOrders = new ListView<>();
    private final TextArea taReceipt = new TextArea();
    private final javafx.collections.ObservableList<OrderRow> orderRows = javafx.collections.FXCollections.observableArrayList();

    private String activeUsername;


    public OrderHistoryView() {}

    public void start(Stage window) {
        UserRole role = (AuthSession.get() == null) ? null : AuthSession.get().getRole();
        defaultUsername = (AuthSession.get() == null) ? null : AuthSession.get().getUsername();

        Label title = new Label("Order History");
        title.getStyleClass().add("tracker-title");
        VBox root = new VBox(10);
        root.setPadding(new Insets(12));
        root.setAlignment(Pos.TOP_CENTER);
        root.getStyleClass().add("tracker-root");

        root.getChildren().add(title);
        lvOrders.setPrefHeight(160);
        lvOrders.setPlaceholder(new Label("No orders"));

        taReceipt.setEditable(false);
        taReceipt.getStyleClass().add("tracker-area");
        taReceipt.setPromptText("Select an order to view receipt");

        lvOrders.setPlaceholder(new Label("No orders"));
        lvOrders.setPrefHeight(150);
        taReceipt.setPrefHeight(200);
        lvOrders.setCellFactory(list -> new ListCell<>(){
            @Override
            protected void updateItem(OrderRow item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText("Order " + item.orderId+ " " + item.orderState + " " + item.totalText + "\n" + "" + item.summaryText);
            }
        });

        lvOrders.getSelectionModel().selectedItemProperty().addListener((obs, oldV, row) -> {
            if (row == null) return;
            taReceipt.setText(loadReceipt(row.orderId, row.orderState));
        });

        if (role != null && role != UserRole.CUSTOMER) {
            tfUserSearch = new TextField();
            tfUserSearch.setPromptText("Search username...");

            Button btnSearch = new Button("Search");
            btnSearch.setOnAction(e -> refresh(tfUserSearch.getText().trim()));

            HBox searchRow = new HBox(10, tfUserSearch, btnSearch);
            searchRow.setAlignment(Pos.CENTER);

            root.getChildren().add(searchRow);
        } else {
            refresh(defaultUsername);
        }

        root.getChildren().addAll(lvOrders, taReceipt);
        lvOrders.setItems(orderRows);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        AppTheme.register(scene);
        window.setTitle("HappyShop - Order History");
        window.setScene(scene);
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
    }

    private void refresh(String username) {
        taReceipt.clear();
        orderRows.clear();
        taHistory.clear();

        if (username == null || username.isBlank()) {
            taHistory.setText("No username provided");
            return;
        }

        activeUsername = username;
        OrderHub hub = OrderHub.getOrderHub();
        TreeMap<Integer, OrderState> map = hub.getOrdersForCustomer(username);

        if (map.isEmpty()) {
            taHistory.setText("No orders found for: " + username);
            return;
        }
        for (Map.Entry<Integer, OrderState> e : map.entrySet()) {
            int orderId = e.getKey();
            OrderState state = e.getValue();
            String totalText = "";
            String summaryText = "";

            orderRows.add(new OrderRow(orderId, state, totalText, summaryText));
        }
        lvOrders.getSelectionModel().selectLast();
        taReceipt.setText("Select an order to view receipt");
    }

    private static final class OrderRow {
        final int orderId;
        final OrderState orderState;
        final String totalText;
        final String summaryText;
        OrderRow(int orderId, OrderState orderState, String totalText, String summaryText) {
            this.orderId = orderId;
            this.orderState = orderState;
            this.totalText = totalText;
            this.summaryText = summaryText;
        }
    }

    private String loadReceipt(int orderId, OrderState state) {
        try {
            return switch (state) {
                case Ordered -> OrderFileManager.readOrderFile(StorageLocation.orderedPath, orderId);
                case Progressing -> OrderFileManager.readOrderFile(StorageLocation.progressingPath, orderId);
                case Collected -> OrderFileManager.readOrderFile(StorageLocation.collectedPath, orderId);
            };
        } catch (Exception ex) {
            return "Could not load receipt for order " + orderId + " (" + state + ")\n" + ex.getMessage();
        }
    }


}

