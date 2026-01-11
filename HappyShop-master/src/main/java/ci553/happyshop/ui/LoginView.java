package ci553.happyshop.ui;

import ci553.happyshop.authentication.AuthService;
import ci553.happyshop.authentication.UserAccount;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.util.function.Consumer;
import ci553.happyshop.authentication.AuthSession;


public class LoginView {
    private TextField userField;
    private PasswordField passField;
    private Label text;
    public void start(Stage window, Consumer<UserAccount> onSuccess) {

        Label title = new Label("HappyShop Login");
        userField = new TextField();
        userField.setPromptText("Username");
        passField = new PasswordField();
        passField.setPromptText("Password");
        text = new Label("");
        Button loginButton = new Button("Login");
        loginButton.setOnAction(e-> doLogin(onSuccess));

        userField.setOnAction(e-> doLogin(onSuccess));
        passField.setOnAction(e-> doLogin(onSuccess));


        Button createButton = new Button("Create Customer Account");
        createButton.setOnAction(e -> showCreateAccountWindow(window, onSuccess));
        VBox root = new VBox(10, title, userField, passField, loginButton, createButton, text);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);
        window.setTitle("HappyShop Login");
        window.setScene(scene);
        window.setResizable(false);
        window.show();
    }
    private void showCreateAccountWindow(Stage owner, Consumer<UserAccount> onSuccess) {
        Label title = new Label("Create Customer Account");
        TextField userField = new TextField();
        userField.setPromptText("Username");
        PasswordField passField = new PasswordField();
        passField.setPromptText("Password");
        PasswordField confirmPField = new PasswordField();
        confirmPField.setPromptText("Confirm password");
        Label text = new Label("");
        Button createButton = new Button("Create");
        createButton.setOnAction(e -> {
            //writes log in to csv
            String error = AuthService.registerCustomer(userField.getText().trim(), passField.getText().trim(), confirmPField.getText().trim());
            if (error != null) {
                text.setText(error);
                return;
            }
            //logs user straight in after account creation
            UserAccount account = AuthService.authenticate(userField.getText().trim(), passField.getText().trim());
            if (account == null) {
                text.setText("Account created, please login...");
                return;
            }
            Stage stage = (Stage) createButton.getScene().getWindow();
            AuthSession.set(account);
            stage.close();
            onSuccess.accept(account);
        });
        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        });
        VBox root = new VBox(10, title, userField, passField, confirmPField, createButton, cancelButton, text);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        Scene scene = new Scene(root, 400, 300);
        Stage dialog = new Stage();
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setTitle("Create Account");
        dialog.setScene(scene);
        dialog.setResizable(false);
        dialog.show();
    }
    private void doLogin(Consumer<UserAccount> onSuccess) {
        String u = userField.getText() == null ? "" : userField.getText().trim();
        String p = passField.getText() == null ? "" : passField.getText().trim();

        if (u.isEmpty() || p.isEmpty()) {
            text.setText("Enter username and password");
            return;
        }

        UserAccount account = AuthService.authenticate(u, p);

        if (account == null) {
            text.setText("Invalid username and or password");
            return;
        }
        text.setText("");
        passField.clear();
        AuthSession.set(account);
        onSuccess.accept(account);
    }
}
