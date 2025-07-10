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
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for the Debug Screen.
 * Manages debug controls for each station, including Modbus TCP communication for Piston switches
 * and other configurable Modbus coils, allowing dynamic coil address changes via text fields.
 * Also includes controls for setting voltage and current via Modbus registers.
 */
public class DebugViewModel implements Initializable {

    // Station 1 Debug Controls
    @FXML private TitledPane station1TitledPane;
    @FXML private CheckBox station1PistonSwitch;
    @FXML private TextField station1PistonCoilAddressField; // Coil Address Field
    @FXML private TextField station1StateValueField;
    @FXML private TextField station1CompCyclesField;
    @FXML private Button station1StartPrepButton;
    @FXML private Button station1StopPrepButton;
    @FXML private Button station1ResetPrepButton;
    @FXML private Button station1UpdateTriggerButton;

    // Station 2 Debug Controls
    @FXML private TitledPane station2TitledPane;
    @FXML private CheckBox station2PistonSwitch;
    @FXML private TextField station2PistonCoilAddressField; // Coil Address Field
    @FXML private TextField station2StateValueField;
    @FXML private TextField station2CompCyclesField;
    @FXML private Button station2StartPrepButton;
    @FXML private Button station2StopPrepButton;
    @FXML private Button station2ResetPrepButton;
    @FXML private Button station2UpdateTriggerButton;

    // Station 3 Debug Controls
    @FXML private TitledPane station3TitledPane;
    @FXML private CheckBox station3PistonSwitch;
    @FXML private TextField station3PistonCoilAddressField; // Coil Address Field
    @FXML private TextField station3StateValueField;
    @FXML private TextField station3CompCyclesField;
    @FXML private Button station3StartPrepButton;
    @FXML private Button station3StopPrepButton;
    @FXML private Button station3ResetPrepButton;
    @FXML private Button station3UpdateTriggerButton;

    // Station Selection CheckBoxes
    @FXML private TitledPane stationSelectionTitledPane;
    @FXML private CheckBox stationSelectionSwitch1;
    @FXML private TextField stationSelectionSwitch1CoilAddressField; // Coil Address Field
    @FXML private CheckBox stationSelectionSwitch2;
    @FXML private TextField stationSelectionSwitch2CoilAddressField; // Coil Address Field
    @FXML private CheckBox stationSelectionSwitch3;
    @FXML private TextField stationSelectionSwitch3CoilAddressField; // Coil Address Field

    // New: SETTINGS Controls
    @FXML private TitledPane settingsTitledPane; // Renamed from stationSelectionTitledPane1 for clarity
    @FXML private TextField setVoltageCoilAddressField; // Address for voltage register
    @FXML private TextField setCurrentCoilAddressField; // Address for current register
    @FXML private Button setVoltageButton;
    @FXML private Button setCurrentButton;
    @FXML private TextField voltageTextBox; // Value for voltage
    @FXML private TextField currentTextBox; // Value for current


    // Test Selection CheckBoxes
    @FXML private TitledPane testSelectionTitledPane;
    @FXML private CheckBox testSelectionSwitchNrml_IL;
    @FXML private TextField testSelectionSwitchNrml_ILCoilAddressField; // Coil Address Field
    @FXML private CheckBox testSelectionSwitchOvr_IL;
    @FXML private TextField testSelectionSwitchOvr_ILCoilAddressField; // Coil Address Field
    @FXML private CheckBox testSelectionSwitchOvr_FL;
    @FXML private TextField testSelectionSwitchOvr_FLCoilAddressField; // Coil Address Field

    // Voltage Controls CheckBoxes
    @FXML private TitledPane voltageControlsTitledPane;
    @FXML private CheckBox voltageControlSwitch230;
    @FXML private TextField voltageControlSwitch230CoilAddressField; // Coil Address Field
    @FXML private CheckBox voltageControlSwitch240;
    @FXML private TextField voltageControlSwitch240CoilAddressField; // Coil Address Field
    @FXML private CheckBox voltageControlSwitch250;
    @FXML private TextField voltageControlSwitch250CoilAddressField; // Coil Address Field

    // Normal Operation - IL CheckBoxes (Currents)
    @FXML private TitledPane normalOperationIL_TitledPane;
    @FXML private CheckBox normalOpIL_1A;
    @FXML private TextField normalOpIL_1ACoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_2A_1;
    @FXML private TextField normalOpIL_2A_1CoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_2A_2;
    @FXML private TextField normalOpIL_2A_2CoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_5A;
    @FXML private TextField normalOpIL_5ACoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_10A_1;
    @FXML private TextField normalOpIL_10A_1CoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_10A_2;
    @FXML private TextField normalOpIL_10A_2CoilAddressField; // Coil Address Field
    @FXML private CheckBox normalOpIL_10A_3;
    @FXML private TextField normalOpIL_10A_3CoilAddressField; // Coil Address Field

    // Overload Test - IL CheckBoxes (Currents)
    @FXML private TitledPane overloadTestIL_TitledPane;
    @FXML private CheckBox overloadIL_0_1A;
    @FXML private TextField overloadIL_0_1ACoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_0_2A_1;
    @FXML private TextField overloadIL_0_2A_1CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_0_2A_2;
    @FXML private TextField overloadIL_0_2A_2CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_0_5A;
    @FXML private TextField overloadIL_0_5ACoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_1A;
    @FXML private TextField overloadIL_1ACoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_2A_1;
    @FXML private TextField overloadIL_2A_1CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_2A_2;
    @FXML private TextField overloadIL_2A_2CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_5A;
    @FXML private TextField overloadIL_5ACoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_10A_1;
    @FXML private TextField overloadIL_10A_1CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_10A_2;
    @FXML private TextField overloadIL_10A_2CoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadIL_10A_3;
    @FXML private TextField overloadIL_10A_3CoilAddressField; // Coil Address Field

    // Overload Test - FL CheckBoxes (Bulbs)
    @FXML private TitledPane overloadTestFL_TitledPane;
    @FXML private CheckBox overloadFl_6A_Bulbs;
    @FXML private TextField overloadFl_6A_BulbsCoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadFl_10A_Bulbs;
    @FXML private TextField overloadFl_10A_BulbsCoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadFl_13A_Bulbs;
    @FXML private TextField overloadFl_13A_BulbsCoilAddressField; // Coil Address Field
    @FXML private CheckBox overloadFl_16A_Bulbs;
    @FXML private TextField overloadFl_16A_BulbsCoilAddressField; // Coil Address Field


    private ArduinoOptaConfig currentConfig;
    private ScheduledExecutorService modbusReadScheduler; // For periodic reading

    // Map to store all Modbus-controlled switches and their associated address fields
    private Map<CheckBox, TextField> modbusControlledSwitches = new HashMap<>();

    // Default coil addresses for switches (can be adjusted by user in UI)
    // These are initial placeholders. Real values should come from a persistent config.
    private static final int DEFAULT_STATION1_PISTON_COIL = 0; // Will be overridden by config
    private static final int DEFAULT_STATION2_PISTON_COIL = 1;
    private static final int DEFAULT_STATION3_PISTON_COIL = 2;

    private static final int DEFAULT_STATION_SELECT_1_COIL = 10;
    private static final int DEFAULT_STATION_SELECT_2_COIL = 11;
    private static final int DEFAULT_STATION_SELECT_3_COIL = 12;

    private static final int DEFAULT_TEST_NRML_IL_COIL = 20;
    private static final int DEFAULT_TEST_OVR_IL_COIL = 21;
    private static final int DEFAULT_TEST_OVR_FL_COIL = 22;

    private static final int DEFAULT_VOLTAGE_230_COIL = 30;
    private static final int DEFAULT_VOLTAGE_240_COIL = 31;
    private static final int DEFAULT_VOLTAGE_250_COIL = 32;

    private static final int DEFAULT_NORMAL_OP_IL_1A_COIL = 40;
    private static final int DEFAULT_NORMAL_OP_IL_2A_1_COIL = 41;
    private static final int DEFAULT_NORMAL_OP_IL_2A_2_COIL = 42;
    private static final int DEFAULT_NORMAL_OP_IL_5A_COIL = 43;
    private static final int DEFAULT_NORMAL_OP_IL_10A_1_COIL = 44;
    private static final int DEFAULT_NORMAL_OP_IL_10A_2_COIL = 45;
    private static final int DEFAULT_NORMAL_OP_IL_10A_3_COIL = 46;

    private static final int DEFAULT_OVERLOAD_IL_0_1A_COIL = 50;
    private static final int DEFAULT_OVERLOAD_IL_0_2A_1_COIL = 51;
    private static final int DEFAULT_OVERLOAD_IL_0_2A_2_COIL = 52;
    private static final int DEFAULT_OVERLOAD_IL_0_5A_COIL = 53;
    private static final int DEFAULT_OVERLOAD_IL_1A_COIL = 54;
    private static final int DEFAULT_OVERLOAD_IL_2A_1_COIL = 55;
    private static final int DEFAULT_OVERLOAD_IL_2A_2_COIL = 56;
    private static final int DEFAULT_OVERLOAD_IL_5A_COIL = 57;
    private static final int DEFAULT_OVERLOAD_IL_10A_1_COIL = 58;
    private static final int DEFAULT_OVERLOAD_IL_10A_2_COIL = 59;
    private static final int DEFAULT_OVERLOAD_IL_10A_3_COIL = 60;

    private static final int DEFAULT_OVERLOAD_FL_6A_COIL = 70;
    private static final int DEFAULT_OVERLOAD_FL_10A_COIL = 71;
    private static final int DEFAULT_OVERLOAD_FL_13A_COIL = 72;
    private static final int DEFAULT_OVERLOAD_FL_16A_COIL = 73;

    // Default register addresses for the new SETTINGS controls
    private static final int DEFAULT_SET_VOLTAGE_REGISTER = 100;
    private static final int DEFAULT_SET_CURRENT_REGISTER = 101;


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

            // Setup button handlers (these don't depend on Modbus connection)
            setupButtonHandlers();

            // Link and set up all Modbus-controlled switches with their address fields
            setupAllModbusControlledSwitches();

            // Setup the new SETTINGS controls for Modbus register writes
            setupSettingsControls();

            // Attempt to connect to Modbus and then setup switches/periodic read on a separate thread
            new Thread(() -> {
                // Ensure Modbus connection is attempted/established
                if (!ModbusService.isConnected()) {
                    ModbusService.connect();
                }

                // All UI updates MUST be on the JavaFX Application Thread
                Platform.runLater(() -> {
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
     * Links each CheckBox to its corresponding TextField for coil address and sets up listeners.
     * Also populates the `modbusControlledSwitches` map.
     */
    private void setupAllModbusControlledSwitches() {
        // Station 1 Piston
        setupModbusToggleSwitch(station1PistonSwitch, station1PistonCoilAddressField, DEFAULT_STATION1_PISTON_COIL);
        // Station 2 Piston
        setupModbusToggleSwitch(station2PistonSwitch, station2PistonCoilAddressField, DEFAULT_STATION2_PISTON_COIL);
        // Station 3 Piston
        setupModbusToggleSwitch(station3PistonSwitch, station3PistonCoilAddressField, DEFAULT_STATION3_PISTON_COIL);

        // Station Selection
        setupModbusToggleSwitch(stationSelectionSwitch1, stationSelectionSwitch1CoilAddressField, DEFAULT_STATION_SELECT_1_COIL);
        setupModbusToggleSwitch(stationSelectionSwitch2, stationSelectionSwitch2CoilAddressField, DEFAULT_STATION_SELECT_2_COIL);
        setupModbusToggleSwitch(stationSelectionSwitch3, stationSelectionSwitch3CoilAddressField, DEFAULT_STATION_SELECT_3_COIL);

        // Test Selection
        setupModbusToggleSwitch(testSelectionSwitchNrml_IL, testSelectionSwitchNrml_ILCoilAddressField, DEFAULT_TEST_NRML_IL_COIL);
        setupModbusToggleSwitch(testSelectionSwitchOvr_IL, testSelectionSwitchOvr_ILCoilAddressField, DEFAULT_TEST_OVR_IL_COIL);
        setupModbusToggleSwitch(testSelectionSwitchOvr_FL, testSelectionSwitchOvr_FLCoilAddressField, DEFAULT_TEST_OVR_FL_COIL);

        // Voltage Controls
        setupModbusToggleSwitch(voltageControlSwitch230, voltageControlSwitch230CoilAddressField, DEFAULT_VOLTAGE_230_COIL);
        setupModbusToggleSwitch(voltageControlSwitch240, voltageControlSwitch240CoilAddressField, DEFAULT_VOLTAGE_240_COIL);
        setupModbusToggleSwitch(voltageControlSwitch250, voltageControlSwitch250CoilAddressField, DEFAULT_VOLTAGE_250_COIL);

        // Normal Operation - IL (Currents)
        setupModbusToggleSwitch(normalOpIL_1A, normalOpIL_1ACoilAddressField, DEFAULT_NORMAL_OP_IL_1A_COIL);
        setupModbusToggleSwitch(normalOpIL_2A_1, normalOpIL_2A_1CoilAddressField, DEFAULT_NORMAL_OP_IL_2A_1_COIL);
        setupModbusToggleSwitch(normalOpIL_2A_2, normalOpIL_2A_2CoilAddressField, DEFAULT_NORMAL_OP_IL_2A_2_COIL);
        setupModbusToggleSwitch(normalOpIL_5A, normalOpIL_5ACoilAddressField, DEFAULT_NORMAL_OP_IL_5A_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_1, normalOpIL_10A_1CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_1_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_2, normalOpIL_10A_2CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_2_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_3, normalOpIL_10A_3CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_3_COIL);

        // Overload Test - IL (Currents)
        setupModbusToggleSwitch(overloadIL_0_1A, overloadIL_0_1ACoilAddressField, DEFAULT_OVERLOAD_IL_0_1A_COIL);
        setupModbusToggleSwitch(overloadIL_0_2A_1, overloadIL_0_2A_1CoilAddressField, DEFAULT_OVERLOAD_IL_0_2A_1_COIL);
        setupModbusToggleSwitch(overloadIL_0_2A_2, overloadIL_0_2A_2CoilAddressField, DEFAULT_OVERLOAD_IL_0_2A_2_COIL);
        setupModbusToggleSwitch(overloadIL_0_5A, overloadIL_0_5ACoilAddressField, DEFAULT_OVERLOAD_IL_0_5A_COIL);
        setupModbusToggleSwitch(overloadIL_1A, overloadIL_1ACoilAddressField, DEFAULT_OVERLOAD_IL_1A_COIL);
        setupModbusToggleSwitch(overloadIL_2A_1, overloadIL_2A_1CoilAddressField, DEFAULT_OVERLOAD_IL_2A_1_COIL);
        setupModbusToggleSwitch(overloadIL_2A_2, overloadIL_2A_2CoilAddressField, DEFAULT_OVERLOAD_IL_2A_2_COIL);
        setupModbusToggleSwitch(overloadIL_5A, overloadIL_5ACoilAddressField, DEFAULT_OVERLOAD_IL_5A_COIL);
        setupModbusToggleSwitch(overloadIL_10A_1, overloadIL_10A_1CoilAddressField, DEFAULT_OVERLOAD_IL_10A_1_COIL);
        setupModbusToggleSwitch(overloadIL_10A_2, overloadIL_10A_2CoilAddressField, DEFAULT_OVERLOAD_IL_10A_2_COIL);
        setupModbusToggleSwitch(overloadIL_10A_3, overloadIL_10A_3CoilAddressField, DEFAULT_OVERLOAD_IL_10A_3_COIL);

        // Overload Test - FL (Bulbs)
        setupModbusToggleSwitch(overloadFl_6A_Bulbs, overloadFl_6A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_6A_COIL);
        setupModbusToggleSwitch(overloadFl_10A_Bulbs, overloadFl_10A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_10A_COIL);
        setupModbusToggleSwitch(overloadFl_13A_Bulbs, overloadFl_13A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_13A_COIL);
        setupModbusToggleSwitch(overloadFl_16A_Bulbs, overloadFl_16A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_16A_COIL);
    }

    /**
     * Sets up a Modbus-controlled CheckBox and its associated TextField for coil address.
     * This method handles:
     * 1. Initializing the TextField with a default address.
     * 2. Adding a listener to the CheckBox to write to Modbus based on the TextField's address.
     * 3. Adding input validation to the TextField to ensure it's an integer.
     * 4. Adding the CheckBox and TextField to the `modbusControlledSwitches` map for periodic reads.
     *
     * @param toggleSwitch The CheckBox that controls a Modbus coil.
     * @param addressField The TextField where the user can input the coil address.
     * @param defaultAddress The default integer address to set in the TextField.
     */
    private void setupModbusToggleSwitch(CheckBox toggleSwitch, TextField addressField, int defaultAddress) {
        // Populate the map for periodic reads
        modbusControlledSwitches.put(toggleSwitch, addressField);

        // Set initial text for the address field
        addressField.setText(String.valueOf(defaultAddress));

        // Add listener for input validation on the address field
        addressField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) { // Allow only digits
                Platform.runLater(() -> {
                    addressField.setText(oldVal); // Revert to old value if invalid
                    NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                            "Invalid Input", "Coil address must be a number.");
                });
            }
        });

        // Add listener for when the switch is toggled by the user
        toggleSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int coilAddress = Integer.parseInt(addressField.getText());
                ApplicationLauncher.logger.debug("Switch for coil {} toggled to: {}", coilAddress, newVal);
                // Execute Modbus write on a separate thread to keep UI responsive
                new Thread(() -> {
                    boolean success = ModbusService.writeCoil(coilAddress, newVal);
                    if (!success) {
                        // If write failed, revert UI state as it doesn't reflect actual PLC state
                        Platform.runLater(() -> {
                            toggleSwitch.setSelected(oldVal); // Revert to previous state
                            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                    "Modbus Write Failed", "Failed to write to coil " + coilAddress + ". Check connection.");
                        });
                    }
                }, "ModbusWriteThread-" + coilAddress).start();
            } catch (NumberFormatException e) {
                NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                        "Invalid Address", "Please enter a valid number for the coil address.");
                Platform.runLater(() -> toggleSwitch.setSelected(oldVal)); // Revert switch if address is invalid
            }
        });

        // Initial read of coil state to set the switch's initial position
        // This is called after Modbus connection attempt in initialize()
        readCoilStateAndUpdateUI(toggleSwitch, addressField);
    }

    /**
     * Sets up the controls within the "SETTINGS" TitledPane for writing to Modbus registers.
     */
    private void setupSettingsControls() {
        // Initialize address fields with default values
        setVoltageCoilAddressField.setText(String.valueOf(DEFAULT_SET_VOLTAGE_REGISTER));
        setCurrentCoilAddressField.setText(String.valueOf(DEFAULT_SET_CURRENT_REGISTER));

        // Add validation for address fields (numeric only)
        addNumericValidation(setVoltageCoilAddressField);
        addNumericValidation(setCurrentCoilAddressField);

        // Add validation for value text boxes (numeric, allowing decimals)
        addDoubleValidation(voltageTextBox);
        addDoubleValidation(currentTextBox);

        // Set action handlers for buttons
        setVoltageButton.setOnAction(event -> handleSetVoltage());
        setCurrentButton.setOnAction(event -> handleSetCurrent());
    }

    /**
     * Adds a listener to a TextField to ensure only numeric input (integers) is allowed.
     * @param textField The TextField to validate.
     */
    private void addNumericValidation(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                Platform.runLater(() -> {
                    textField.setText(oldVal);
                    NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                            "Invalid Input", "Address must be a whole number.");
                });
            }
        });
    }

    /**
     * Adds a listener to a TextField to ensure only numeric input (integers or decimals) is allowed.
     * @param textField The TextField to validate.
     */
    private void addDoubleValidation(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            // Allow empty string, or numbers with optional decimal part
            if (!newVal.matches("-?\\d*(\\.\\d*)?")) {
                Platform.runLater(() -> {
                    textField.setText(oldVal);
                    NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                            "Invalid Input", "Value must be a number.");
                });
            }
        });
    }

    /**
     * Handles the action for the "SET VOLTAGE" button.
     * Reads the address and value, then attempts to write to the Modbus register.
     */
    @FXML
    private void handleSetVoltage() {
        try {
            int registerAddress = Integer.parseInt(setVoltageCoilAddressField.getText());
            double voltageValue = Double.parseDouble(voltageTextBox.getText());
            int intValue = (int) voltageValue; // Modbus registers typically hold integers

            ApplicationLauncher.logger.info("Attempting to set voltage: {} to address {}", voltageValue, registerAddress);

            new Thread(() -> {
                boolean success = ModbusService.writeRegister(registerAddress, intValue);
                Platform.runLater(() -> {
                    if (success) {
                        NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                "Voltage Set", "Voltage " + voltageValue + " written to register " + registerAddress);
                    } else {
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                "Modbus Write Failed", "Failed to write voltage to register " + registerAddress + ". Check connection.");
                    }
                });
            }, "SetVoltageThread").start();

        } catch (NumberFormatException e) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Invalid Input", "Please enter valid numbers for voltage and address.");
            ApplicationLauncher.logger.error("Invalid input for set voltage: {}", e.getMessage());
        }
    }

    /**
     * Handles the action for the "SET CURRENT" button.
     * Reads the address and value, then attempts to write to the Modbus register.
     */
    @FXML
    private void handleSetCurrent() {
        try {
            int registerAddress = Integer.parseInt(setCurrentCoilAddressField.getText());
            double currentValue = Double.parseDouble(currentTextBox.getText());
            int intValue = (int) currentValue; // Modbus registers typically hold integers

            ApplicationLauncher.logger.info("Attempting to set current: {} to address {}", currentValue, registerAddress);

            new Thread(() -> {
                boolean success = ModbusService.writeRegister(registerAddress, intValue);
                Platform.runLater(() -> {
                    if (success) {
                        NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                "Current Set", "Current " + currentValue + " written to register " + registerAddress);
                    } else {
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                "Modbus Write Failed", "Failed to write current to register " + registerAddress + ". Check connection.");
                    }
                });
            }, "SetCurrentThread").start();

        } catch (NumberFormatException e) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Invalid Input", "Please enter valid numbers for current and address.");
            ApplicationLauncher.logger.error("Invalid input for set current: {}", e.getMessage());
        }
    }


    /**
     * Reads the state of a specific Modbus coil (from its associated TextField)
     * and updates the corresponding UI CheckBox.
     * @param toggleSwitch The CheckBox to update.
     * @param addressField The TextField containing the Modbus coil address to read.
     */
    private void readCoilStateAndUpdateUI(CheckBox toggleSwitch, TextField addressField) {
        // Only attempt to read if Modbus is actually connected
        if (!ModbusService.isConnected()) {
            ApplicationLauncher.logger.debug("Skipping read for switch {} as Modbus is not connected.", toggleSwitch.getId());
            return;
        }

        try {
            int coilAddress = Integer.parseInt(addressField.getText());
            new Thread(() -> {
                Boolean coilState = ModbusService.readCoil(coilAddress);
                if (coilState != null) {
                    Platform.runLater(() -> toggleSwitch.setSelected(coilState));
                    ApplicationLauncher.logger.debug("Coil {} state read: {}", coilAddress, coilState);
                } else {
                    ApplicationLauncher.logger.warn("Could not read state of coil {}. Modbus connection might be down or address invalid.", coilAddress);
                    // Notification for read failure is handled by ModbusService itself
                }
            }, "ModbusReadThread-" + coilAddress).start();
        } catch (NumberFormatException e) {
            ApplicationLauncher.logger.error("Invalid coil address for switch {}: {}", toggleSwitch.getId(), addressField.getText());
            // No notification here, as periodic read shouldn't spam with "invalid address"
        }
    }

    /**
     * Starts a scheduled task to periodically read the state of all Modbus-controlled coils
     * and update their respective UI switches.
     */
    private void startPeriodicCoilRead() {
        if (modbusReadScheduler != null && !modbusReadScheduler.isShutdown()) {
            modbusReadScheduler.shutdownNow(); // Ensure any previous scheduler is stopped
        }
        modbusReadScheduler = Executors.newSingleThreadScheduledExecutor();
        modbusReadScheduler.scheduleAtFixedRate(() -> {
            // Iterate through all linked switches and update their UI
            Platform.runLater(() -> { // Ensure UI updates are on JavaFX Application Thread
                modbusControlledSwitches.forEach((toggleSwitch, addressField) ->
                    readCoilStateAndUpdateUI(toggleSwitch, addressField)
                );
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
