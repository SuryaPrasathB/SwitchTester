package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.User; // Import the User model
import com.switchtester.app.service.UserManager; // Import UserManager
import com.switchtester.app.util.PasswordHasher; // Import PasswordHasher
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set; // Import Set

/**
 * ViewModel for the Login Screen.
 * Handles user input, login logic, and interaction with the MainApp for navigation.
 */
public class LoginViewModel implements Initializable {

    // FXML injected UI elements
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private ComboBox<String> profileTypeComboBox;
    @FXML
    private Label messageLabel;

    private Stage loginStage;

    // ObservableList for the ComboBox items
    private ObservableList<String> profileTypes = FXCollections.observableArrayList(
            "Admin", "Production", "Quality", "Maintenance"
    );

    public void setLoginStage(Stage stage) {
        this.loginStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profileTypeComboBox.setItems(profileTypes);
        profileTypeComboBox.getSelectionModel().selectFirst();
        messageLabel.setText("");
    }

    @FXML
    private void handleEnterKey(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            handleLoginButton(new ActionEvent());
        }
    }

    @FXML
    private void handleLoginButton(ActionEvent event) {
        String username = usernameField.getText().trim();
        String plainTextPassword = passwordField.getText();
        String selectedProfileType = profileTypeComboBox.getSelectionModel().getSelectedItem();

        if (username.isEmpty() || plainTextPassword.isEmpty() || selectedProfileType == null) {
            messageLabel.setText("Please fill in all fields.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            return;
        }

        // Retrieve user from UserManager
        Optional<User> userOptional = UserManager.getUserByUsername(username);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            // Check password hash and profile type
            if (PasswordHasher.checkPassword(plainTextPassword, user.getPasswordHash()) &&
                selectedProfileType.equals(user.getProfileType())) {

                // Authentication successful
                messageLabel.setText("Login Successful!");
                messageLabel.setStyle("-fx-text-fill: #27ae60;");

                // Set the logged-in user profile and permissions in MainApp
                ApplicationLauncher.setLoggedInUserProfile(user.getProfileType());
                ApplicationLauncher.setLoggedInUserPermissions(user.getPermissions()); // Set granular permissions

                ApplicationLauncher.logger.info("Login successful for " + user.getUsername() + " (" + user.getProfileType() + ")!");

                navigateToDashboardWithDelay();

            } else {
                // Password or profile type mismatch
                messageLabel.setText("Invalid username, password, or profile type.");
                messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            }
        } else {
            // User not found
            messageLabel.setText("Invalid username, password, or profile type.");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        }
    }

    private void navigateToDashboardWithDelay() {
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            Platform.runLater(() -> {
                if (loginStage != null) {
                    loginStage.close();
                }
                ApplicationLauncher.showMainDashboardScreen();
            });
        }).start();
    }

    @FXML
    private void handleCancelButton(ActionEvent event) {
        ApplicationLauncher.logger.info("Login cancelled. Exiting application.");
        Platform.exit();
        System.exit(0);
    }
}
