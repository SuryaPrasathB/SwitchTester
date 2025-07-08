package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.ArduinoOptaConfig;
import com.switchtester.app.service.ArduinoOptaConfigManager;
import com.switchtester.app.service.NotificationManager;
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ViewModel for the Arduino Opta Configuration screen.
 * Manages displaying and saving Modbus TCP connection settings.
 */
public class ArduinoOptaConfigViewModel implements Initializable {

    @FXML private TextField ipAddressField;
    @FXML private TextField portField;
    @FXML private TextField pistonCoilAddressField;
    @FXML private Label messageLabel;

    private Stage ownerStage; // To set the owner of alerts

    /**
     * Sets the owner stage for this ViewModel.
     * @param ownerStage The Stage that owns this view.
     */
    public void setOwnerStage(Stage ownerStage) {
        this.ownerStage = ownerStage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("ArduinoOptaConfigViewModel initialized.");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);
        loadConfigToUI(); // Load existing config on startup
    }

    /**
     * Loads the current Arduino Opta configuration and displays it in the UI fields.
     */
    @FXML
    private void handleLoadConfig() {
        loadConfigToUI();
        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                           "Config Loaded",
                                                           "Arduino Opta configuration loaded successfully.");
        ApplicationLauncher.logger.info("Arduino Opta configuration manually loaded to UI.");
    }

    private void loadConfigToUI() {
        ArduinoOptaConfig config = ArduinoOptaConfigManager.getCurrentConfig();
        ipAddressField.setText(config.getIpAddress());
        portField.setText(String.valueOf(config.getPort()));
        pistonCoilAddressField.setText(String.valueOf(config.getPistonCoilAddress()));
        ApplicationLauncher.logger.debug("Loaded config to UI: {}", config);
    }

    /**
     * Saves the configuration entered in the UI fields.
     */
    @FXML
    private void handleSaveConfig() {
        if (isInputValid()) {
            try {
                String ipAddress = ipAddressField.getText().trim();
                int port = Integer.parseInt(portField.getText().trim());
                int pistonCoilAddress = Integer.parseInt(pistonCoilAddressField.getText().trim());

                ArduinoOptaConfig newConfig = new ArduinoOptaConfig(ipAddress, port, pistonCoilAddress);
                ArduinoOptaConfigManager.saveConfig(newConfig);

                NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                                                   "Config Saved",
                                                                   "Arduino Opta configuration saved successfully.");
                ApplicationLauncher.logger.info("Arduino Opta configuration saved: {}", newConfig);
            } catch (NumberFormatException e) {
                showError("Port and Coil Address must be valid numbers.");
                ApplicationLauncher.logger.warn("Failed to save config: Invalid number format for port/coil address. {}", e.getMessage());
            } catch (Exception e) {
                showError("Error saving configuration: " + e.getMessage());
                ApplicationLauncher.logger.error("Error saving Arduino Opta configuration: {}", e.getMessage(), e);
            }
        }
    }

    /**
     * Validates the input fields for IP address, port, and coil address.
     * @return true if input is valid, false otherwise.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (ipAddressField.getText() == null || ipAddressField.getText().trim().isEmpty()) {
            errorMessage += "IP Address cannot be empty.\n";
        }
        // Basic IP address format validation (can be more robust)
        else if (!ipAddressField.getText().trim().matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            errorMessage += "Invalid IP Address format.\n";
        }

        try {
            int port = Integer.parseInt(portField.getText().trim());
            if (port < 1 || port > 65535) {
                errorMessage += "Port must be between 1 and 65535.\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Port must be a valid number.\n";
        }

        try {
            int pistonCoilAddress = Integer.parseInt(pistonCoilAddressField.getText().trim());
            if (pistonCoilAddress < 0) { // Coil addresses are typically non-negative
                errorMessage += "Piston Coil Address cannot be negative.\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Piston Coil Address must be a valid number.\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showError(errorMessage);
            return false;
        }
    }

    /**
     * Displays an error message within the dialog.
     * @param message The error message to display.
     */
    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setOpacity(1.0);
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);

        // Fade out the error message after 5 seconds
        FadeTransition fadeOut = new FadeTransition(Duration.seconds(5), messageLabel);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        });
        fadeOut.play();
    }
}
