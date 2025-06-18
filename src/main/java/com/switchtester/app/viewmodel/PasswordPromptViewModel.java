package com.switchtester.app.viewmodel;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.animation.FadeTransition;

import java.lang.Runnable;

public class PasswordPromptViewModel {

    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel; // Changed fx:id to messageLabel to match FXML

    private String expectedPassword;
    @SuppressWarnings("unused")
	private DashboardViewModel dashboardViewModel; // This reference might not be strictly needed anymore due to callbacks

    private Runnable onPasswordCorrectCallback;
    private Runnable onPasswordCancelledCallback;


    @FXML
    public void initialize() {
        messageLabel.setText(""); // Use messageLabel
        messageLabel.setVisible(false); // Use messageLabel
        messageLabel.setManaged(false); // Use messageLabel

        // This line handles the Enter key press on the password field
        // When Enter is pressed in the passwordField, it triggers the handleOk() method.
        passwordField.setOnAction(event -> handleOk());
    }

    public void setExpectedPassword(String password) {
        this.expectedPassword = password;
    }

    public void setOnPasswordCorrectCallback(Runnable callback) {
        this.onPasswordCorrectCallback = callback;
    }

    public void setOnPasswordCancelledCallback(Runnable callback) {
        this.onPasswordCancelledCallback = callback;
    }

    // This setter is less critical now as callbacks handle the primary communication.
    // Keep it if DashboardViewModel needs to set other properties on this ViewModel.
    public void setDashboardViewModel(DashboardViewModel dashboardViewModel) {
        this.dashboardViewModel = dashboardViewModel;
    }

    @FXML
    private void handleOk() {
        String enteredPassword = passwordField.getText();

        if (enteredPassword.equals(expectedPassword)) {
            if (onPasswordCorrectCallback != null) {
                onPasswordCorrectCallback.run();
            }
        } else {
            showError("Incorrect password. Please try again.");
            passwordField.clear();
        }
    }

    @FXML
    private void handleCancel() {
        if (onPasswordCancelledCallback != null) {
            onPasswordCancelledCallback.run();
        }
    }

    private void showError(String message) {
        messageLabel.setText(message); // Use messageLabel
        messageLabel.setOpacity(1.0); // Use messageLabel
        messageLabel.setVisible(true); // Use messageLabel
        messageLabel.setManaged(true); // Use messageLabel

        FadeTransition fadeOut = new FadeTransition(Duration.seconds(5), messageLabel); // Use messageLabel
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            messageLabel.setVisible(false); // Use messageLabel
            messageLabel.setManaged(false); // Use messageLabel
        });
        fadeOut.play();
    }
}
