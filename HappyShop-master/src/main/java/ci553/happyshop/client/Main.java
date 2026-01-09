package ci553.happyshop.client;

import ci553.happyshop.authentication.UserAccount;
import ci553.happyshop.authentication.UserRole;
import ci553.happyshop.client.customer.CustomerController;
import ci553.happyshop.client.customer.CustomerModel;
import ci553.happyshop.client.customer.CustomerView;
import ci553.happyshop.client.emergency.EmergencyExit;
import ci553.happyshop.client.orderTracker.OrderTracker;
import ci553.happyshop.client.picker.PickerController;
import ci553.happyshop.client.picker.PickerModel;
import ci553.happyshop.client.picker.PickerView;
import ci553.happyshop.client.warehouse.AlertSimulator;
import ci553.happyshop.client.warehouse.HistoryWindow;
import ci553.happyshop.client.warehouse.WarehouseController;
import ci553.happyshop.client.warehouse.WarehouseModel;
import ci553.happyshop.client.warehouse.WarehouseView;
import ci553.happyshop.orderManagement.OrderHub;
import ci553.happyshop.storageAccess.DatabaseRW;
import ci553.happyshop.storageAccess.DatabaseRWFactory;
import ci553.happyshop.ui.LoginView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


/**
 * The Main JavaFX application class. The Main class is executable directly.
 * It serves as a foundation for UI logic and starts all the clients (UI) in one go.
 *
 * This class launches all standalone clients (Customer, Picker, OrderTracker, Warehouse, EmergencyExit)
 * and links them together into a fully working system.
 *
 * It performs essential setup tasks, such as initializing the order map in the OrderHub
 * and registering observers.
 *
 * Note: Each client type can be instantiated multiple times (e.g., calling startCustomerClient() as many times as needed)
 * to simulate a multi-user environment, where multiple clients of the same type interact with the system concurrently.
 *
 * @version 1.0
 * @author  Shine Shan University of Brighton
 */

public class Main extends Application {

    public static void main(String[] args) {
        launch(args); // Launches the JavaFX application and calls the @Override start()
    }

    //starts the system
    @Override
    public void start(Stage primaryStage) {
        initializeOrderMap();
        showLogin(primaryStage);
    }

    private void showLogin(Stage primaryStage) {
        LoginView loginView = new LoginView();
        loginView.start(primaryStage, user -> showLauncher(primaryStage, user));
    }

    private void showLauncher(Stage primaryStage, UserAccount user) {

        Label title = new Label("HappyShop Launcher");
        Label roleLabel = new Label("Role: " + user.getRole());

        Button customerButton = new Button("Open Customer Client");
        customerButton.setOnAction(e -> startCustomerClient());

        Button trackerButton = new Button("Open Order Tracker");
        trackerButton.setOnAction(e -> startOrderTracker());

        Button pickerButton = new Button("Open Picker Client");
        pickerButton.setOnAction(e -> startPickerClient());

        Button warehouseButton = new Button("Open Warehouse Client");
        warehouseButton.setOnAction(e -> startWarehouseClient());

        Button emergencyExitButton = new Button("Open Emergency Exit");
        emergencyExitButton.setOnAction(e -> startEmergencyExit());

        Button backButton = new Button("Back to Login");
        backButton.setOnAction(e -> showLogin(primaryStage));

        Button btnExit = new Button("Exit");
        btnExit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });

        VBox root = new VBox(10);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        root.getChildren().addAll(title, roleLabel);

        // CUSTOMER: Customer + Tracker
        if (user.getRole() == UserRole.CUSTOMER) {
            root.getChildren().addAll(customerButton, trackerButton);
        }

        // STAFF: Picker + Warehouse + Tracker
        if (user.getRole() == UserRole.STAFF) {
            root.getChildren().addAll(warehouseButton, pickerButton, trackerButton);
        }

        // ADMIN: everything
        if (user.getRole() == UserRole.ADMIN) {
            root.getChildren().addAll(customerButton, warehouseButton, pickerButton, trackerButton, emergencyExitButton);
        }

        root.getChildren().addAll(backButton, btnExit);

        Scene scene = new Scene(root, 360, 420);
        primaryStage.setTitle("HappyShop Launcher");
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    /** The customer GUI -search prodduct, add to trolley, cancel/submit trolley, view receipt
     *
     * Creates the Model, View, and Controller objects, links them together so they can communicate with each other.
     * Also creates the DatabaseRW instance via the DatabaseRWFactory and injects it into the CustomerModel.
     * Starts the customer interface.
     *
     * Also creates the RemoveProductNotifier, which tracks the position of the Customer View
     * and is triggered by the Customer Model when needed.
     */
    private void startCustomerClient(){
        CustomerView cusView = new CustomerView();
        CustomerController cusController = new CustomerController();
        CustomerModel cusModel = new CustomerModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        cusView.cusController = cusController;
        cusController.cusModel = cusModel;
        cusModel.cusView = cusView;
        cusModel.databaseRW = databaseRW;
        cusView.start(new Stage());

        //RemoveProductNotifier removeProductNotifier = new RemoveProductNotifier();
        //removeProductNotifier.cusView = cusView;
        //cusModel.removeProductNotifier = removeProductNotifier;
    }

    /** The picker GUI, - for staff to pack customer's order,
     *
     * Creates the Model, View, and Controller objects for the Picker client.
     * Links them together so they can communicate with each other.
     * Starts the Picker interface.
     *
     * Also registers the PickerModel with the OrderHub to receive order notifications.
     */
    //changed order
    private void startPickerClient(){
        PickerModel pickerModel = new PickerModel();
        PickerView pickerView = new PickerView();
        PickerController pickerController = new PickerController();
        pickerView.pickerController = pickerController;
        pickerController.pickerModel = pickerModel;
        pickerModel.pickerView = pickerView;
        pickerView.start(new Stage()); // UI is built first so labels can exist
        pickerModel.registerWithOrderHub();
    }

    //The OrderTracker GUI - for customer to track their order's state(Ordered, Progressing, Collected)
    //This client is simple and does not follow the MVC pattern, as it only registers with the OrderHub
    //to receive order status notifications. All logic is handled internally within the OrderTracker.
    private void startOrderTracker(){
        OrderTracker orderTracker = new OrderTracker();
        orderTracker.registerWithOrderHub();
    }

    //initialize the orderMap<orderId, orderState> for OrderHub during system startup
    private void initializeOrderMap(){
        OrderHub orderHub = OrderHub.getOrderHub();
        orderHub.initializeOrderMap();
    }

    /** The Warehouse GUI- for warehouse staff to manage stock
     * Initializes the Warehouse client's Model, View, and Controller,and links them together for communication.
     * It also creates the DatabaseRW instance via the DatabaseRWFactory and injects it into the Model.
     * Once the components are linked, the warehouse interface (view) is started.
     *
     * Also creates the dependent HistoryWindow and AlertSimulator,
     * which track the position of the Warehouse window and are triggered by the Model when needed.
     * These components are linked after launching the Warehouse interface.
     */
    private void startWarehouseClient(){
        WarehouseView view = new WarehouseView();
        WarehouseController controller = new WarehouseController();
        WarehouseModel model = new WarehouseModel();
        DatabaseRW databaseRW = DatabaseRWFactory.createDatabaseRW();

        // Link controller, model, and view and start view
        view.controller = controller;
        controller.model = model;
        model.view = view;
        model.databaseRW = databaseRW;
        view.start(new Stage());

        //create dependent views that need window info
        HistoryWindow historyWindow = new HistoryWindow();
        AlertSimulator alertSimulator = new AlertSimulator();

        // Link after start
        model.historyWindow = historyWindow;
        model.alertSimulator = alertSimulator;
        historyWindow.warehouseView = view;
        alertSimulator.warehouseView = view;
    }

    //starts the EmergencyExit GUI, - used to close the entire application immediatelly
    private void startEmergencyExit(){
        EmergencyExit.getEmergencyExit();
    }
}



