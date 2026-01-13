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

public class OrderHistoryView {

    private final int WIDTH = 400;
    private final int HEIGHT = 400;

    private final TextArea taHistory = new TextArea();
    private TextField tfUserSearch; // only used for staff/admin
    private String defaultUsername;

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
        taHistory.setEditable(false);
        taHistory.getStyleClass().add("tracker-area");

        if (role != null && role != UserRole.CUSTOMER) {
            tfUserSearch = new TextField();
            tfUserSearch.setPromptText("Search username...");
            Button btnSearch = new Button("Search");
            btnSearch.setOnAction(e -> refresh(tfUserSearch.getText().trim()));
            HBox row = new HBox(10, tfUserSearch, btnSearch);
            row.setAlignment(Pos.CENTER);
            root.getChildren().addAll(title, row, taHistory);
            taHistory.setText("Enter a username and click Search");
        } else {
            // just shows customer account history
            root.getChildren().addAll(title, taHistory);
            refresh(defaultUsername);
        }

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        AppTheme.register(scene);
        window.setTitle("HappyShop - Order History");
        window.setScene(scene);
        WinPosManager.registerWindow(window, WIDTH, HEIGHT);
        window.show();
    }

    private void refresh(String username) {
        OrderHub hub = OrderHub.getOrderHub();
        TreeMap<Integer, OrderState> map = hub.getOrdersForCustomer(username);
        if (username == null || username.isBlank()) {
            taHistory.setText("No username provided");
            return;
        }
        if (map.isEmpty()) {
            taHistory.setText("No orders found for: " + username);
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(username).append("\n\n");
        for (Map.Entry<Integer, OrderState> e : map.entrySet()) {
            sb.append("Order ").append(e.getKey())
                    .append("    ").append(e.getValue())
                    .append("\n");
        }
        taHistory.setText(sb.toString());
    }
}

