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
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for the Debug Screen.
 * Manages debug controls for each station, including Modbus TCP communication for Piston switches.
 */
public class DebugViewModel implements Initializable {

    // Station 1 FXML elements
    @FXML private TitledPane station1TitledPane;
    @FXML private CheckBox station1PistonSwitch;
    @FXML private TextField station1StateValueField;
    @FXML private TextField station1CompCyclesField;
    @FXML private Button station1StartPrepButton;
    @FXML private Button station1StopPrepButton;
    @FXML private Button station1ResetPrepButton;
    @FXML private Button station1UpdateTriggerButton;

    // Station 2 FXML elements
    @FXML private TitledPane station2TitledPane;
    @FXML private CheckBox station2PistonSwitch;
    @FXML private TextField station2StateValueField;
    @FXML private TextField station2CompCyclesField;
    @FXML private Button station2StartPrepButton;
    @FXML private Button station2StopPrepButton;
    @FXML private Button station2ResetPrepButton;
    @FXML private Button station2UpdateTriggerButton;

    // Station 3 FXML elements
    @FXML private TitledPane station3TitledPane;
    @FXML private CheckBox station3PistonSwitch;
    @FXML private TextField station3StateValueField;
    @FXML private TextField station3CompCyclesField;
    @FXML private Button station3StartPrepButton;
    @FXML private Button station3StopPrepButton;
    @FXML private Button station3ResetPrepButton;
    @FXML private Button station3UpdateTriggerButton;

    // Manual Controls TitledPane (for future expansion)
    @FXML private TitledPane manualControlsTitledPane;

    private ArduinoOptaConfig currentConfig;
    private ScheduledExecutorService modbusReadScheduler; // For periodic reading (optional, but good for debug)

    // Hardcoded coil addresses for other stations (for now)
    // IMPORTANT: Replace these with actual addresses from your PLC setup.
    private static final int STATION1_PISTON_COIL_OFFSET = 0; // This will be read from config
    private static final int STATION2_PISTON_COIL_ADDRESS = 1; // Example hardcoded
    private static final int STATION3_PISTON_COIL_ADDRESS = 2; // Example hardcoded

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Debug View Initializing...");

        try {
            // Load the current Arduino Opta configuration
            currentConfig = ArduinoOptaConfigManager.getCurrentConfig();

            // Setup other button handlers (placeholders for now) - these don't depend on Modbus connection
            setupButtonHandlers();

            // Attempt to connect to Modbus and then setup switches/periodic read on a separate thread
            new Thread(() -> {
                // Ensure Modbus connection is attempted/established
                if (!ModbusService.isConnected()) {
                    ModbusService.connect();
                }

                // All UI updates MUST be on the JavaFX Application Thread
                Platform.runLater(() -> {
                    // Setup Piston Switches ONLY after a connection attempt has been made
                    setupPistonSwitch(station1PistonSwitch, currentConfig.getPistonCoilAddress());
                    setupPistonSwitch(station2PistonSwitch, STATION2_PISTON_COIL_ADDRESS);
                    setupPistonSwitch(station3PistonSwitch, STATION3_PISTON_COIL_ADDRESS);

                    // Start periodic read only if Modbus is connected
                    if (ModbusService.isConnected()) {
                        startPeriodicCoilRead();
                    } else {
                        ApplicationLauncher.logger.warn("Modbus not connected, periodic coil read will not start.");
                        NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                "Modbus Not Connected", "Periodic coil read disabled. Check Modbus config.");
                    }
                    ApplicationLauncher.logger.info("Debug View UI setup complete.");
                });
            }, "ModbusInitThread").start(); // Give the thread a name for easier debugging

        } catch (Exception e) {
            ApplicationLauncher.logger.error("Error during Debug View initialization: {}", e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                               "Debug View Error",
                                                               "Failed to initialize Debug View: " + e.getMessage());
        }
    }

    /**
     * Sets up the Piston CheckBox to act as a Modbus coil toggle.
     * @param pistonSwitch The CheckBox control.
     * @param coilAddress The Modbus coil address associated with this switch.
     */
    private void setupPistonSwitch(CheckBox pistonSwitch, int coilAddress) {
        // Listener for when the switch is toggled by the user
        pistonSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            ApplicationLauncher.logger.debug("Piston switch for coil {} toggled to: {}", coilAddress, newVal);
            // Execute Modbus write on a separate thread to keep UI responsive
            new Thread(() -> {
                boolean success = ModbusService.writeCoil(coilAddress, newVal);
                if (!success) {
                    // If write failed, revert UI state as it doesn't reflect actual PLC state
                    Platform.runLater(() -> {
                        pistonSwitch.setSelected(oldVal); // Revert to previous state
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                "Modbus Write Failed", "Failed to write to coil " + coilAddress + ". Check connection.");
                    });
                }
            }, "ModbusWriteThread-" + coilAddress).start();
        });

        // Initial read of coil state to set the switch's initial position
        // This also needs to be on a separate thread and run after Modbus connection is confirmed
        // It's called from Platform.runLater after the connection attempt in initialize()
        readCoilStateAndUpdateUI(pistonSwitch, coilAddress);
    }

    /**
     * Reads the state of a specific Modbus coil and updates the corresponding UI CheckBox.
     * @param pistonSwitch The CheckBox to update.
     * @param coilAddress The Modbus coil address to read.
     */
    private void readCoilStateAndUpdateUI(CheckBox pistonSwitch, int coilAddress) {
        // Only attempt to read if Modbus is actually connected
        if (!ModbusService.isConnected()) {
            ApplicationLauncher.logger.debug("Skipping read for coil {} as Modbus is not connected.", coilAddress);
            return;
        }

        new Thread(() -> {
            Boolean coilState = ModbusService.readCoil(coilAddress);
            if (coilState != null) {
                Platform.runLater(() -> pistonSwitch.setSelected(coilState));
                ApplicationLauncher.logger.debug("Coil {} state read: {}", coilAddress, coilState);
            } else {
                ApplicationLauncher.logger.warn("Could not read state of coil {}. Modbus connection might be down.", coilAddress);
                // Notification for read failure is handled by ModbusService itself
            }
        }, "ModbusReadThread-" + coilAddress).start();
    }

    /**
     * Starts a scheduled task to periodically read the state of all piston coils
     * and update their respective UI switches.
     */
    private void startPeriodicCoilRead() {
        if (modbusReadScheduler != null && !modbusReadScheduler.isShutdown()) {
            modbusReadScheduler.shutdownNow(); // Ensure any previous scheduler is stopped
        }
        modbusReadScheduler = Executors.newSingleThreadScheduledExecutor();
        modbusReadScheduler.scheduleAtFixedRate(() -> {
            // Ensure this runs on the JavaFX Application Thread for UI updates
            Platform.runLater(() -> {
                // Read and update for Station 1
                readCoilStateAndUpdateUI(station1PistonSwitch, currentConfig.getPistonCoilAddress());
                // Read and update for Station 2
                readCoilStateAndUpdateUI(station2PistonSwitch, STATION2_PISTON_COIL_ADDRESS);
                // Read and update for Station 3
                readCoilStateAndUpdateUI(station3PistonSwitch, STATION3_PISTON_COIL_ADDRESS);
            });
        }, 5, 5, TimeUnit.SECONDS); // Read every 5 seconds
        ApplicationLauncher.logger.info("Periodic Modbus coil read scheduled.");
    }

    /**
     * Sets up action handlers for all buttons.
     */
    private void setupButtonHandlers() {
        // Station 1 Buttons
        station1StartPrepButton.setOnAction(event -> handleStationButton(1, "START PREP."));
        station1StopPrepButton.setOnAction(event -> handleStationButton(1, "STOP PREP."));
        station1ResetPrepButton.setOnAction(event -> handleStationButton(1, "RESET PREP."));
        station1UpdateTriggerButton.setOnAction(event -> handleStationButton(1, "UPDATE TRIGGER"));

        // Station 2 Buttons
        station2StartPrepButton.setOnAction(event -> handleStationButton(2, "START PREP."));
        station2StopPrepButton.setOnAction(event -> handleStationButton(2, "STOP PREP."));
        station2ResetPrepButton.setOnAction(event -> handleStationButton(2, "RESET PREP."));
        station2UpdateTriggerButton.setOnAction(event -> handleStationButton(2, "UPDATE TRIGGER"));

        // Station 3 Buttons
        station3StartPrepButton.setOnAction(event -> handleStationButton(3, "START PREP."));
        station3StopPrepButton.setOnAction(event -> handleStationButton(3, "STOP PREP."));
        station3ResetPrepButton.setOnAction(event -> handleStationButton(3, "RESET PREP."));
        station3UpdateTriggerButton.setOnAction(event -> handleStationButton(3, "UPDATE TRIGGER"));
    }

    /**
     * Generic handler for station buttons.
     * @param stationNum The station number.
     * @param action The action performed (e.g., "START PREP.").
     */
    private void handleStationButton(int stationNum, String action) {
        ApplicationLauncher.logger.info("Station {} {} button clicked.", stationNum, action);
        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                           "Button Clicked",
                                                           "Station " + stationNum + " " + action + " button clicked. (Logic to be implemented)");
        // Future: Implement specific logic for each button, potentially involving Modbus writes to other coils/registers.
    }

    /**
     * Called when the DebugView is no longer active (e.g., user navigates away).
     * Ensures Modbus periodic read scheduler is shut down.
     */
    public void cleanup() {
        if (modbusReadScheduler != null && !modbusReadScheduler.isShutdown()) {
            modbusReadScheduler.shutdownNow();
            ApplicationLauncher.logger.info("DebugViewModel: Periodic Modbus read scheduler shut down during cleanup.");
        }
    }
}
