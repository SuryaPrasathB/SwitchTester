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
 * Also includes controls for setting voltage, current, and duty cycle via Modbus registers.
 */
public class DebugViewModel implements Initializable {

    // --- FXML UI Components ---

    // region Station 1 Components
    @FXML private TitledPane station1TitledPane;
    @FXML private CheckBox station1PistonSwitch;
    @FXML private TextField station1PistonCoilAddressField;
    @FXML private TextField station1StateValueField;
    @FXML private TextField station1CompCyclesField;
    @FXML private Button station1StartPrepButton;
    @FXML private Button station1StopPrepButton;
    @FXML private Button station1ResetPrepButton;
    @FXML private Button station1UpdateTriggerButton;
    // endregion

    // region Station 2 Components
    @FXML private TitledPane station2TitledPane;
    @FXML private CheckBox station2PistonSwitch;
    @FXML private TextField station2PistonCoilAddressField;
    @FXML private TextField station2StateValueField;
    @FXML private TextField station2CompCyclesField;
    @FXML private Button station2StartPrepButton;
    @FXML private Button station2StopPrepButton;
    @FXML private Button station2ResetPrepButton;
    @FXML private Button station2UpdateTriggerButton;
    // endregion

    // region Station 3 Components
    @FXML private TitledPane station3TitledPane;
    @FXML private CheckBox station3PistonSwitch;
    @FXML private TextField station3PistonCoilAddressField;
    @FXML private TextField station3StateValueField;
    @FXML private TextField station3CompCyclesField;
    @FXML private Button station3StartPrepButton;
    @FXML private Button station3StopPrepButton;
    @FXML private Button station3ResetPrepButton;
    @FXML private Button station3UpdateTriggerButton;
    // endregion

    // region Station Selection Components
    @FXML private TitledPane stationSelectionTitledPane;
    @FXML private CheckBox stationSelectionSwitch1;
    @FXML private TextField stationSelectionSwitch1CoilAddressField;
    @FXML private CheckBox stationSelectionSwitch2;
    @FXML private TextField stationSelectionSwitch2CoilAddressField;
    @FXML private CheckBox stationSelectionSwitch3;
    @FXML private TextField stationSelectionSwitch3CoilAddressField;
    // endregion

    // region Settings Components
    @FXML private TitledPane settingsTitledPane;
    @FXML private TextField setVoltageCoilAddressField;
    @FXML private TextField setCurrentCoilAddressField;
    @FXML private Button setVoltageButton;
    @FXML private Button setCurrentButton;
    @FXML private TextField voltageTextBox;
    @FXML private TextField currentTextBox;
    // endregion
    
    // region Duty Cycle Components
    @FXML private TitledPane dutyCycleTitledPane;
    @FXML private TitledPane dutyCycleTitledPane1;
    @FXML private TextField onTimeCoilAddressField1;
    @FXML private TextField offTimeCoilAddressField1;
    @FXML private TextField onTimeField1;
    @FXML private TextField offTimeField1;
    @FXML private Button updateDutyCycle1;
    @FXML private Button decDutyCycle1;
    @FXML private Button incDutyCycle1;

    @FXML private TitledPane dutyCycleTitledPane2;
    @FXML private TextField onTimeCoilAddressField2;
    @FXML private TextField offTimeCoilAddressField2;
    @FXML private TextField onTimeField2;
    @FXML private TextField offTimeField2;
    @FXML private Button updateDutyCycle2;
    @FXML private Button decDutyCycle2;
    @FXML private Button incDutyCycle2;

    @FXML private TitledPane dutyCycleTitledPane3;
    @FXML private TextField onTimeCoilAddressField3;
    @FXML private TextField offTimeCoilAddressField3;
    @FXML private TextField onTimeField3;
    @FXML private TextField offTimeField3;
    @FXML private Button updateDutyCycle3;
    @FXML private Button decDutyCycle3;
    @FXML private Button incDutyCycle3;
    // endregion

    // region Test Selection Components
    @FXML private TitledPane testSelectionTitledPane;
    @FXML private CheckBox testSelectionSwitchNrml_IL;
    @FXML private TextField testSelectionSwitchNrml_ILCoilAddressField;
    @FXML private CheckBox testSelectionSwitchOvr_IL;
    @FXML private TextField testSelectionSwitchOvr_ILCoilAddressField;
    @FXML private CheckBox testSelectionSwitchOvr_FL;
    @FXML private TextField testSelectionSwitchOvr_FLCoilAddressField;
    // endregion

    // region Voltage Controls Components
    @FXML private TitledPane voltageControlsTitledPane;
    @FXML private CheckBox voltageControlSwitch230;
    @FXML private TextField voltageControlSwitch230CoilAddressField;
    @FXML private CheckBox voltageControlSwitch240;
    @FXML private TextField voltageControlSwitch240CoilAddressField;
    @FXML private CheckBox voltageControlSwitch250;
    @FXML private TextField voltageControlSwitch250CoilAddressField;
    // endregion

    // region Normal Operation - IL Components
    @FXML private TitledPane normalOperationIL_TitledPane;
    @FXML private CheckBox normalOpIL_1A;
    @FXML private TextField normalOpIL_1ACoilAddressField;
    @FXML private CheckBox normalOpIL_2A_1;
    @FXML private TextField normalOpIL_2A_1CoilAddressField;
    @FXML private CheckBox normalOpIL_2A_2;
    @FXML private TextField normalOpIL_2A_2CoilAddressField;
    @FXML private CheckBox normalOpIL_5A;
    @FXML private TextField normalOpIL_5ACoilAddressField;
    @FXML private CheckBox normalOpIL_10A_1;
    @FXML private TextField normalOpIL_10A_1CoilAddressField;
    @FXML private CheckBox normalOpIL_10A_2;
    @FXML private TextField normalOpIL_10A_2CoilAddressField;
    @FXML private CheckBox normalOpIL_10A_3;
    @FXML private TextField normalOpIL_10A_3CoilAddressField;
    // endregion

    // region Overload Test - IL Components
    @FXML private TitledPane overloadTestIL_TitledPane;
    @FXML private CheckBox overloadIL_0_1A;
    @FXML private TextField overloadIL_0_1ACoilAddressField;
    @FXML private CheckBox overloadIL_0_2A_1;
    @FXML private TextField overloadIL_0_2A_1CoilAddressField;
    @FXML private CheckBox overloadIL_0_2A_2;
    @FXML private TextField overloadIL_0_2A_2CoilAddressField;
    @FXML private CheckBox overloadIL_0_5A;
    @FXML private TextField overloadIL_0_5ACoilAddressField;
    @FXML private CheckBox overloadIL_1A;
    @FXML private TextField overloadIL_1ACoilAddressField;
    @FXML private CheckBox overloadIL_2A_1;
    @FXML private TextField overloadIL_2A_1CoilAddressField;
    @FXML private CheckBox overloadIL_2A_2;
    @FXML private TextField overloadIL_2A_2CoilAddressField;
    @FXML private CheckBox overloadIL_5A;
    @FXML private TextField overloadIL_5ACoilAddressField;
    @FXML private CheckBox overloadIL_10A_1;
    @FXML private TextField overloadIL_10A_1CoilAddressField;
    @FXML private CheckBox overloadIL_10A_2;
    @FXML private TextField overloadIL_10A_2CoilAddressField;
    @FXML private CheckBox overloadIL_10A_3;
    @FXML private TextField overloadIL_10A_3CoilAddressField;
    // endregion

    // region Overload Test - FL Components
    @FXML private TitledPane overloadTestFL_TitledPane;
    @FXML private CheckBox overloadFl_6A_Bulbs;
    @FXML private TextField overloadFl_6A_BulbsCoilAddressField;
    @FXML private CheckBox overloadFl_10A_Bulbs;
    @FXML private TextField overloadFl_10A_BulbsCoilAddressField;
    @FXML private CheckBox overloadFl_13A_Bulbs;
    @FXML private TextField overloadFl_13A_BulbsCoilAddressField;
    @FXML private CheckBox overloadFl_16A_Bulbs;
    @FXML private TextField overloadFl_16A_BulbsCoilAddressField;
    // endregion

    private ArduinoOptaConfig currentConfig;
    private ScheduledExecutorService modbusReadScheduler; // For periodic reading

    // Map to store all Modbus-controlled switches and their associated address fields
    private final Map<CheckBox, TextField> modbusControlledSwitches = new HashMap<>();

    // --- Constants ---
    private static final int TOTAL_CYCLE_TIME_MS = 2000;
    private static final double INITIAL_DUTY_CYCLE_PERCENTAGE = 0.25; // 25%

    // --- Default Coil and Register Addresses ---
    private static final int DEFAULT_STATION1_PISTON_COIL = 0;
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

    private static final int DEFAULT_SET_VOLTAGE_REGISTER = 100;
    private static final int DEFAULT_SET_CURRENT_REGISTER = 101;

    private static final int STATION1_PREP_REGISTER = 0;
    private static final int STATION1_PREP_COIL = 4;
    private static final int STATION2_PREP_REGISTER = 16;
    private static final int STATION2_PREP_COIL = 5;
    private static final int STATION3_PREP_REGISTER = 32;
    private static final int STATION3_PREP_COIL = 6;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Debug View Initializing...");

        try {
            currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
            setupButtonHandlers();
            setupAllModbusControlledSwitches();
            setupSettingsControls();
            setupAllDutyCycleControls(); // Re-integrated this call

            new Thread(() -> {
                if (!ModbusService.isConnected()) {
                    ModbusService.connect();
                }

                Platform.runLater(() -> {
                    if (ModbusService.isConnected()) {
                        // startPeriodicCoilRead(); // Uncomment to enable periodic reading
                    } else {
                        ApplicationLauncher.logger.warn("Modbus not connected, periodic coil read will not start.");
                        NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                "Modbus Not Connected", "Periodic coil read disabled. Check Modbus config.");
                    }
                    ApplicationLauncher.logger.info("Debug View UI setup complete.");
                });
            }, "ModbusInitThread").start();

        } catch (Exception e) {
            ApplicationLauncher.logger.error("Error during Debug View initialization: {}", e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Debug View Error", "Failed to initialize Debug View: " + e.getMessage());
        }
    }

    /**
     * Sets up all duty cycle controls for every station.
     */
    private void setupAllDutyCycleControls() {
        // Setup for Station 1
        setupStationDutyCycle(onTimeField1, offTimeField1, incDutyCycle1, decDutyCycle1);
        updateDutyCycle1.setOnAction(e -> handleUpdateDutyCycle(1, onTimeField1, offTimeField1, onTimeCoilAddressField1, offTimeCoilAddressField1));

        // Setup for Station 2
        setupStationDutyCycle(onTimeField2, offTimeField2, incDutyCycle2, decDutyCycle2);
        updateDutyCycle2.setOnAction(e -> handleUpdateDutyCycle(2, onTimeField2, offTimeField2, onTimeCoilAddressField2, offTimeCoilAddressField2));

        // Setup for Station 3
        setupStationDutyCycle(onTimeField3, offTimeField3, incDutyCycle3, decDutyCycle3);
        updateDutyCycle3.setOnAction(e -> handleUpdateDutyCycle(3, onTimeField3, offTimeField3, onTimeCoilAddressField3, offTimeCoilAddressField3));

        // Set initial values based on the default duty cycle
        int initialOnTime = (int) (TOTAL_CYCLE_TIME_MS * INITIAL_DUTY_CYCLE_PERCENTAGE);
        onTimeField1.setText(String.valueOf(initialOnTime));
        onTimeField2.setText(String.valueOf(initialOnTime));
        onTimeField3.setText(String.valueOf(initialOnTime));
    }

    /**
     * Sets up the listeners and handlers for a single station's duty cycle controls.
     *
     * @param onTimeField  The TextField for ON time.
     * @param offTimeField The TextField for OFF time.
     * @param incButton    The button to increment ON time.
     * @param decButton    The button to decrement ON time.
     */
    private void setupStationDutyCycle(TextField onTimeField, TextField offTimeField, Button incButton, Button decButton) {
        // Listener to automatically update OFF time when ON time changes.
        onTimeField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                int onTime = Integer.parseInt(newValue);
                if (onTime >= 0 && onTime <= TOTAL_CYCLE_TIME_MS) {
                    int offTime = TOTAL_CYCLE_TIME_MS - onTime;
                    offTimeField.setText(String.valueOf(offTime));
                } else {
                    offTimeField.setText(""); // Clear if out of range
                }
            } catch (NumberFormatException e) {
                offTimeField.setText(""); // Clear if not a number
            }
        });

        // Handler for the increment button
        incButton.setOnAction(e -> {
            try {
                int currentOnTime = Integer.parseInt(onTimeField.getText());
                if (currentOnTime < TOTAL_CYCLE_TIME_MS) {
                    onTimeField.setText(String.valueOf(currentOnTime + 1));
                }
            } catch (NumberFormatException ex) {
                onTimeField.setText("1"); // Start from 1 if invalid
            }
        });

        // Handler for the decrement button
        decButton.setOnAction(e -> {
            try {
                int currentOnTime = Integer.parseInt(onTimeField.getText());
                if (currentOnTime > 0) {
                    onTimeField.setText(String.valueOf(currentOnTime - 1));
                }
            } catch (NumberFormatException ex) {
                onTimeField.setText("0"); // Start from 0 if invalid
            }
        });
    }

    /**
     * Handles the "Update" button click for a station's duty cycle.
     * Writes the ON and OFF time values to their respective Modbus registers.
     *
     * @param stationNumber The station number (1, 2, or 3).
     * @param onTimeField The TextField for the ON time value.
     * @param offTimeField The TextField for the OFF time value.
     * @param onTimeAddressField The TextField for the ON time register address.
     * @param offTimeAddressField The TextField for the OFF time register address.
     */
    private void handleUpdateDutyCycle(int stationNumber, TextField onTimeField, TextField offTimeField, TextField onTimeAddressField, TextField offTimeAddressField) {
        try {
            int onTime = Integer.parseInt(onTimeField.getText());
            int offTime = Integer.parseInt(offTimeField.getText());
            int onTimeAddress = Integer.parseInt(onTimeAddressField.getText());
            int offTimeAddress = Integer.parseInt(offTimeAddressField.getText());
            
            ApplicationLauncher.logger.info("Updating Station {} Duty Cycle: ON={}ms (Addr:{}), OFF={}ms (Addr:{})", 
                                            stationNumber, onTime, onTimeAddress, offTime, offTimeAddress);

            new Thread(() -> {
                boolean onTimeSuccess = ModbusService.writeRegister(onTimeAddress, onTime);
                boolean offTimeSuccess = ModbusService.writeRegister(offTimeAddress, offTime);
                
                Platform.runLater(() -> {
                    if(onTimeSuccess && offTimeSuccess) {
                         NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Duty Cycle Updated", "Station " + stationNumber + " updated successfully.");
                    } else {
                        String errorMessage = "Could not update duty cycle for station " + stationNumber + ".";
                        if (!onTimeSuccess) errorMessage += " Failed to write ON time.";
                        if (!offTimeSuccess) errorMessage += " Failed to write OFF time.";
                         NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Update Failed", errorMessage);
                    }
                });
            }, "UpdateDutyCycleThread-Station" + stationNumber).start();

        } catch (NumberFormatException e) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Invalid Input", "Please enter valid numbers for duty cycle times and addresses.");
        }
    }


    /**
     * Links each CheckBox to its corresponding TextField for coil address and sets up listeners.
     */
    private void setupAllModbusControlledSwitches() {
        setupModbusToggleSwitch(station1PistonSwitch, station1PistonCoilAddressField, DEFAULT_STATION1_PISTON_COIL);
        setupModbusToggleSwitch(station2PistonSwitch, station2PistonCoilAddressField, DEFAULT_STATION2_PISTON_COIL);
        setupModbusToggleSwitch(station3PistonSwitch, station3PistonCoilAddressField, DEFAULT_STATION3_PISTON_COIL);

        setupModbusToggleSwitch(stationSelectionSwitch1, stationSelectionSwitch1CoilAddressField, DEFAULT_STATION_SELECT_1_COIL);
        setupModbusToggleSwitch(stationSelectionSwitch2, stationSelectionSwitch2CoilAddressField, DEFAULT_STATION_SELECT_2_COIL);
        setupModbusToggleSwitch(stationSelectionSwitch3, stationSelectionSwitch3CoilAddressField, DEFAULT_STATION_SELECT_3_COIL);

        setupModbusToggleSwitch(testSelectionSwitchNrml_IL, testSelectionSwitchNrml_ILCoilAddressField, DEFAULT_TEST_NRML_IL_COIL);
        setupModbusToggleSwitch(testSelectionSwitchOvr_IL, testSelectionSwitchOvr_ILCoilAddressField, DEFAULT_TEST_OVR_IL_COIL);
        setupModbusToggleSwitch(testSelectionSwitchOvr_FL, testSelectionSwitchOvr_FLCoilAddressField, DEFAULT_TEST_OVR_FL_COIL);

        setupModbusToggleSwitch(voltageControlSwitch230, voltageControlSwitch230CoilAddressField, DEFAULT_VOLTAGE_230_COIL);
        setupModbusToggleSwitch(voltageControlSwitch240, voltageControlSwitch240CoilAddressField, DEFAULT_VOLTAGE_240_COIL);
        setupModbusToggleSwitch(voltageControlSwitch250, voltageControlSwitch250CoilAddressField, DEFAULT_VOLTAGE_250_COIL);

        setupModbusToggleSwitch(normalOpIL_1A, normalOpIL_1ACoilAddressField, DEFAULT_NORMAL_OP_IL_1A_COIL);
        setupModbusToggleSwitch(normalOpIL_2A_1, normalOpIL_2A_1CoilAddressField, DEFAULT_NORMAL_OP_IL_2A_1_COIL);
        setupModbusToggleSwitch(normalOpIL_2A_2, normalOpIL_2A_2CoilAddressField, DEFAULT_NORMAL_OP_IL_2A_2_COIL);
        setupModbusToggleSwitch(normalOpIL_5A, normalOpIL_5ACoilAddressField, DEFAULT_NORMAL_OP_IL_5A_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_1, normalOpIL_10A_1CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_1_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_2, normalOpIL_10A_2CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_2_COIL);
        setupModbusToggleSwitch(normalOpIL_10A_3, normalOpIL_10A_3CoilAddressField, DEFAULT_NORMAL_OP_IL_10A_3_COIL);

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

        setupModbusToggleSwitch(overloadFl_6A_Bulbs, overloadFl_6A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_6A_COIL);
        setupModbusToggleSwitch(overloadFl_10A_Bulbs, overloadFl_10A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_10A_COIL);
        setupModbusToggleSwitch(overloadFl_13A_Bulbs, overloadFl_13A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_13A_COIL);
        setupModbusToggleSwitch(overloadFl_16A_Bulbs, overloadFl_16A_BulbsCoilAddressField, DEFAULT_OVERLOAD_FL_16A_COIL);
    }

    /**
     * Sets up a Modbus-controlled CheckBox and its associated TextField for coil address.
     *
     * @param toggleSwitch   The CheckBox that controls a Modbus coil.
     * @param addressField   The TextField where the user can input the coil address.
     * @param defaultAddress The default integer address to set in the TextField.
     */
    private void setupModbusToggleSwitch(CheckBox toggleSwitch, TextField addressField, int defaultAddress) {
        modbusControlledSwitches.put(toggleSwitch, addressField);
        addressField.setText(String.valueOf(defaultAddress));
        addNumericValidation(addressField);

        toggleSwitch.selectedProperty().addListener((obs, oldVal, newVal) -> {
            try {
                int coilAddress = Integer.parseInt(addressField.getText());
                ApplicationLauncher.logger.debug("Switch for coil {} toggled to: {}", coilAddress, newVal);
                new Thread(() -> {
                    boolean success = ModbusService.writeCoil(coilAddress, newVal);
                    if (!success) {
                        Platform.runLater(() -> {
                            toggleSwitch.setSelected(oldVal); // Revert UI on failure
                            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                    "Modbus Write Failed", "Failed to write to coil " + coilAddress + ". Check connection.");
                        });
                    }
                }, "ModbusWriteThread-" + coilAddress).start();
            } catch (NumberFormatException e) {
                NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                        "Invalid Address", "Please enter a valid number for the coil address.");
                Platform.runLater(() -> toggleSwitch.setSelected(oldVal));
            }
        });
    }

    /**
     * Sets up the controls within the "SETTINGS" TitledPane for writing to Modbus registers.
     */
    private void setupSettingsControls() {
        setVoltageCoilAddressField.setText(String.valueOf(DEFAULT_SET_VOLTAGE_REGISTER));
        setCurrentCoilAddressField.setText(String.valueOf(DEFAULT_SET_CURRENT_REGISTER));
        addNumericValidation(setVoltageCoilAddressField);
        addNumericValidation(setCurrentCoilAddressField);
        addDoubleValidation(voltageTextBox);
        addDoubleValidation(currentTextBox);
        setVoltageButton.setOnAction(event -> handleSetVoltage());
        setCurrentButton.setOnAction(event -> handleSetCurrent());
    }

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

    private void addDoubleValidation(TextField textField) {
        textField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("-?\\d*(\\.\\d*)?")) {
                Platform.runLater(() -> {
                    textField.setText(oldVal);
                    NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                            "Invalid Input", "Value must be a number.");
                });
            }
        });
    }

    @FXML
    private void handleSetVoltage() {
        handleRegisterWrite(setVoltageCoilAddressField, voltageTextBox, "Voltage");
    }

    @FXML
    private void handleSetCurrent() {
        handleRegisterWrite(setCurrentCoilAddressField, currentTextBox, "Current");
    }
    
    private void handleRegisterWrite(TextField addressField, TextField valueField, String valueName) {
        try {
            int registerAddress = Integer.parseInt(addressField.getText());
            double value = Double.parseDouble(valueField.getText());
            int intValue = (int) value;

            ApplicationLauncher.logger.info("Attempting to set {}: {} to address {}", valueName, value, registerAddress);

            new Thread(() -> {
                boolean success = ModbusService.writeRegister(registerAddress, intValue);
                Platform.runLater(() -> {
                    if (success) {
                        NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                valueName + " Set", valueName + " " + value + " written to register " + registerAddress);
                    } else {
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                "Modbus Write Failed", "Failed to write " + valueName.toLowerCase() + " to register " + registerAddress + ". Check connection.");
                    }
                });
            }, "Set" + valueName + "Thread").start();

        } catch (NumberFormatException e) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                    "Invalid Input", "Please enter valid numbers for " + valueName.toLowerCase() + " and address.");
            ApplicationLauncher.logger.error("Invalid input for set {}: {}", valueName, e.getMessage());
        }
    }


    private void readCoilStateAndUpdateUI(CheckBox toggleSwitch, TextField addressField) {
        if (!ModbusService.isConnected()) {
            return;
        }
        try {
            int coilAddress = Integer.parseInt(addressField.getText());
            new Thread(() -> {
                Boolean coilState = ModbusService.readCoil(coilAddress);
                if (coilState != null) {
                    Platform.runLater(() -> toggleSwitch.setSelected(coilState));
                }
            }, "ModbusReadThread-" + coilAddress).start();
        } catch (NumberFormatException e) {
            // Log silently, don't spam user with notifications for periodic task failures
            ApplicationLauncher.logger.error("Invalid coil address for switch {}: {}", toggleSwitch.getId(), addressField.getText());
        }
    }

    private void startPeriodicCoilRead() {
        if (modbusReadScheduler != null && !modbusReadScheduler.isShutdown()) {
            modbusReadScheduler.shutdownNow();
        }
        modbusReadScheduler = Executors.newSingleThreadScheduledExecutor();
        modbusReadScheduler.scheduleAtFixedRate(() -> {
            Platform.runLater(() -> modbusControlledSwitches.forEach(this::readCoilStateAndUpdateUI));
        }, 5, 5, TimeUnit.SECONDS);
        ApplicationLauncher.logger.info("Periodic Modbus coil read scheduled.");
    }

    private void setupButtonHandlers() {
        station1StartPrepButton.setOnAction(event -> handleStartPrep(1));
        station1StopPrepButton.setOnAction(event -> handleStopPrep(1));
        station1ResetPrepButton.setOnAction(event -> handleResetPrep(1));
        station1UpdateTriggerButton.setOnAction(event -> handleStationButton(1, "UPDATE TRIGGER"));

        station2StartPrepButton.setOnAction(event -> handleStartPrep(2));
        station2StopPrepButton.setOnAction(event -> handleStopPrep(2));
        station2ResetPrepButton.setOnAction(event -> handleResetPrep(2));
        station2UpdateTriggerButton.setOnAction(event -> handleStationButton(2, "UPDATE TRIGGER"));

        station3StartPrepButton.setOnAction(event -> handleStartPrep(3));
        station3StopPrepButton.setOnAction(event -> handleStopPrep(3));
        station3ResetPrepButton.setOnAction(event -> handleResetPrep(3));
        station3UpdateTriggerButton.setOnAction(event -> handleStationButton(3, "UPDATE TRIGGER"));
    }

    private void handleStationButton(int stationNum, String action) {
        ApplicationLauncher.logger.info("Station {} {} button clicked.", stationNum, action);
        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                "Button Clicked", "Station " + stationNum + " " + action + " button clicked. (Logic to be implemented)");
    }

    private void handleStartPrep(int stationNum) {
        handlePrepAction(stationNum, 123, "START PREP.");
    }

    private void handleStopPrep(int stationNum) {
        handlePrepAction(stationNum, 456, "STOP PREP.");
    }

    private void handleResetPrep(int stationNum) {
        handlePrepAction(stationNum, 789, "RESET PREP.");
    }
    
    private void handlePrepAction(int stationNum, int registerValue, String actionName) {
        int registerAddress;
        int coilAddress;

        switch (stationNum) {
            case 1:
                registerAddress = STATION1_PREP_REGISTER;
                coilAddress = STATION1_PREP_COIL;
                break;
            case 2:
                registerAddress = STATION2_PREP_REGISTER;
                coilAddress = STATION2_PREP_COIL;
                break;
            case 3:
                registerAddress = STATION3_PREP_REGISTER;
                coilAddress = STATION3_PREP_COIL;
                break;
            default:
                ApplicationLauncher.logger.error("Invalid station number for {}: {}", actionName, stationNum);
                return;
        }

        ApplicationLauncher.logger.info("Station {} {}: Writing {} to register {} and TRUE to coil {}",
                stationNum, actionName, registerValue, registerAddress, coilAddress);

        new Thread(() -> {
            boolean regSuccess = ModbusService.writeRegister(registerAddress, registerValue);
            boolean coilSuccess = ModbusService.writeCoil(coilAddress, true);

            Platform.runLater(() -> {
                if (regSuccess && coilSuccess) {
                    NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                            actionName + " Success", "Station " + stationNum + ": Prep values written.");
                } else {
                    NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                            actionName + " Failed", "Station " + stationNum + ": Failed to write all prep values.");
                }
            });
        }, "Station" + stationNum + actionName.replace(" ", "") + "Thread").start();
    }
    
    public void cleanup() {
        if (modbusReadScheduler != null && !modbusReadScheduler.isShutdown()) {
            modbusReadScheduler.shutdownNow();
            ApplicationLauncher.logger.info("DebugViewModel: Periodic Modbus read scheduler shut down.");
        }
    }
}
