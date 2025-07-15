package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.ArduinoOptaConfig;
import com.switchtester.app.service.ArduinoOptaConfigManager;
import com.switchtester.app.service.ModbusService;
import com.switchtester.app.service.NotificationManager;
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Label; // Import Label for messageLabel
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.function.Consumer;

/**
 * ViewModel for the Arduino Opta Configuration screen.
 * Manages the display and modification of Modbus connection parameters
 * (IP address, port, timeout) and Modbus register addresses for voltage/current.
 * This version is adapted to the provided FXML, which only exposes IP and Port for direct editing.
 */
public class ArduinoOptaConfigViewModel implements Initializable {

    @FXML private TextField ipAddressField;
    @FXML private TextField portField;
    // Removed FXML fields for timeoutSecondsField, setVoltageRegisterAddressField, setCurrentRegisterAddressField
    // as they are not present in the provided FXML.
    @FXML private Label messageLabel; // Added to match the provided FXML
    @FXML private Button saveButton;
    // Removed cancelButton as it's not present in the provided FXML.

    private ArduinoOptaConfig currentConfig;
    private Consumer<String> mainScreenTitleUpdater;
    private Stage ownerStage;
    
    /**
     * Sets the owner stage for this SettingsViewModel.
     * This is typically called by the DashboardViewModel when loading the SettingsView.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    /**
     * Sets the callback for updating the main screen title.
     * @param mainScreenTitleUpdater A Consumer that accepts a String for the new title.
     */
    public void setMainScreenTitleUpdater(Consumer<String> mainScreenTitleUpdater) {
        this.mainScreenTitleUpdater = mainScreenTitleUpdater;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("ArduinoOptaConfigViewModel Initializing...");
        messageLabel.setText(""); // Clear message label on init
        messageLabel.setVisible(false); // Hide message label initially
        messageLabel.setManaged(false); // Don't take up space when hidden

        loadConfigAndPopulateFields();
        setupInputValidation();
    }

    /**
     * Loads the current Arduino Opta configuration and populates the UI fields.
     */
    private void loadConfigAndPopulateFields() {
        currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
        if (currentConfig != null) {
            ipAddressField.setText(currentConfig.getIpAddress());
            portField.setText(String.valueOf(currentConfig.getPort()));
            // No longer setting text for timeout, voltage, current register fields as they are not in FXML
            ApplicationLauncher.logger.info("Arduino Opta config loaded: IP={}, Port={}", currentConfig.getIpAddress(), currentConfig.getPort());
        } else {
            // This notification is still relevant if the config manager fails to provide a config
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Config Error", "Failed to load Arduino Opta configuration.");
            ApplicationLauncher.logger.error("Arduino Opta config is null during initialization.");
        }
    }

    /**
     * Sets up input validation for numeric fields.
     * Only applies to fields present in the FXML.
     */
    private void setupInputValidation() {
        addIntegerValidation(portField);
        // Removed validation for timeout, voltage, current register fields as they are not in FXML
    }

    /**
     * Adds a listener to a TextField to ensure only integer input is allowed.
     * @param textField The TextField to validate.
     */
    private void addIntegerValidation(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) { // Allow only digits
                Platform.runLater(() -> {
                    textField.setText(oldVal); // Revert to old value if invalid
                    showMessage("Field must contain only numbers.", true);
                });
            } else {
                // Clear message if input becomes valid
                showMessage("", false);
            }
        });
    }

    /**
     * Handles the save button click.
     * Validates input, saves the new configuration, and attempts to reconnect Modbus.
     */
    @FXML
    private void handleSaveConfig() { // Method name changed to match FXML
        if (isInputValid()) {
            String ipAddress = ipAddressField.getText();
            int port = Integer.parseInt(portField.getText());

            // Load the existing config to preserve values not exposed in the current UI
            ArduinoOptaConfig existingConfig = ArduinoOptaConfigManager.getCurrentConfig();

            // Create a new config object, updating only the fields from the UI
            // Other fields (timeout, voltage/current registers) will retain their values from existingConfig
            ArduinoOptaConfig newConfig = new ArduinoOptaConfig(
                    ipAddress,
                    port,
                    existingConfig.getTimeoutSeconds(), // Use existing value
                    existingConfig.getSetVoltageRegisterAddress(), // Use existing value
                    existingConfig.getSetCurrentRegisterAddress() // Use existing value
            );

            ArduinoOptaConfigManager.saveConfig(newConfig);
            NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                    "Config Saved", "Arduino Opta configuration saved successfully.");
            ApplicationLauncher.logger.info("Arduino Opta config saved: IP={}, Port={}",
                    ipAddress, port);

            // Attempt to reconnect Modbus with the new configuration on a background thread
            new Thread(() -> {
                ModbusService.disconnect(); // Disconnect existing connection first
                boolean connected = ModbusService.connect(); // Attempt new connection
                Platform.runLater(() -> {
                    if (connected) {
                        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                "Modbus Reconnected", "Modbus reconnected with new settings.");
                    } else {
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                "Modbus Reconnect Failed", "Failed to reconnect Modbus with new settings. Check logs.");
                    }
                });
            }, "ModbusReconnectThread").start();

            // Optionally, navigate back to settings main or close this sub-view
            // For now, it stays on this screen.
            showMessage("Configuration saved!", false); // Local message
        }
    }

    /**
     * Handles the "Load" button click.
     * Reloads the current configuration from the file and populates fields.
     */
    @FXML
    private void handleLoadConfig() { // New method for the Load button
        loadConfigAndPopulateFields();
        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                "Config Loaded", "Arduino Opta configuration reloaded.");
        ApplicationLauncher.logger.info("Arduino Opta config reloaded from file.");
        showMessage("Configuration reloaded.", false); // Local message
    }

    /**
     * Validates the input fields before saving.
     * Only validates fields present in the FXML.
     * @return true if all inputs are valid, false otherwise.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (ipAddressField.getText() == null || ipAddressField.getText().trim().isEmpty()) {
            errorMessage += "IP Address cannot be empty.\n";
        } else if (!ipAddressField.getText().matches("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")) {
            errorMessage += "Invalid IP Address format.\n";
        }

        try {
            int port = Integer.parseInt(portField.getText());
            if (port < 1 || port > 65535) {
                errorMessage += "Port must be between 1 and 65535.\n";
            }
        } catch (NumberFormatException e) {
            errorMessage += "Port must be a valid number.\n";
        }

        // Removed validation for timeout, voltage, current register fields as they are not in FXML

        if (errorMessage.isEmpty()) {
            showMessage("", false); // Clear any previous error messages
            return true;
        } else {
            showMessage(errorMessage.trim(), true);
            ApplicationLauncher.logger.warn("Arduino Opta config validation failed: {}", errorMessage.trim());
            return false;
        }
    }

    /**
     * Displays a message in the local messageLabel.
     * @param message The message to display.
     * @param isError True if the message indicates an error, false otherwise.
     */
    private void showMessage(String message, boolean isError) {
        if (message == null || message.isEmpty()) {
            messageLabel.setText("");
            messageLabel.setVisible(false);
            messageLabel.setManaged(false);
        } else {
            messageLabel.setText(message);
            messageLabel.setStyle("-fx-text-fill: " + (isError ? "#e74c3c;" : "#27ae60;"));
            messageLabel.setVisible(true);
            messageLabel.setManaged(true);
        }
    }
}
