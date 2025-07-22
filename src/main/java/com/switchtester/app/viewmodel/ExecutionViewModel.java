package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.TestTypeConfig;
import com.switchtester.app.service.ModbusService;
import com.switchtester.app.service.NotificationManager;
import com.switchtester.app.service.TestTypeConfigManager;
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * ViewModel for the Execution Screen.
 * Manages the UI and logic for configuring and running tests on three independent stations.
 */
public class ExecutionViewModel implements Initializable {

    // region FXML UI Components - Station 1
    @FXML private ComboBox<String> station1TestTypeComboBox;
    @FXML private TextField station1TestTypeDescField;
    @FXML private ComboBox<String> station1VoltageComboBox;
    @FXML private ComboBox<String> station1CurrentComboBox;
    @FXML private TextField station1NumOperationsField;
    @FXML private TextField station1CycleTimeField;
    @FXML private TextField station1OnTimeField;
    @FXML private TextField station1OffTimeField;
    @FXML private Button station1StartButton;
    @FXML private Button station1StopButton;
    @FXML private Button station1ResetButton;
    @FXML private Button station1SaveButton;
    @FXML private Button station1LoadButton;
    @FXML private TextField station1CyclesField;
    @FXML private TextField station1TotalCyclesField;
    // endregion

    // region FXML UI Components - Station 2
    @FXML private ComboBox<String> station2TestTypeComboBox;
    @FXML private TextField station2TestTypeDescField;
    @FXML private ComboBox<String> station2VoltageComboBox;
    @FXML private ComboBox<String> station2CurrentComboBox;
    @FXML private TextField station2NumOperationsField;
    @FXML private TextField station2CycleTimeField;
    @FXML private TextField station2OnTimeField;
    @FXML private TextField station2OffTimeField;
    @FXML private Button station2StartButton;
    @FXML private Button station2StopButton;
    @FXML private Button station2ResetButton;
    @FXML private Button station2SaveButton;
    @FXML private Button station2LoadButton;
    @FXML private TextField station2CyclesField;
    @FXML private TextField station2TotalCyclesField;
    // endregion

    // region FXML UI Components - Station 3
    @FXML private ComboBox<String> station3TestTypeComboBox;
    @FXML private TextField station3TestTypeDescField;
    @FXML private ComboBox<String> station3VoltageComboBox;
    @FXML private ComboBox<String> station3CurrentComboBox;
    @FXML private TextField station3NumOperationsField;
    @FXML private TextField station3CycleTimeField;
    @FXML private TextField station3OnTimeField;
    @FXML private TextField station3OffTimeField;
    @FXML private Button station3StartButton;
    @FXML private Button station3StopButton;
    @FXML private Button station3ResetButton;
    @FXML private Button station3SaveButton;
    @FXML private Button station3LoadButton;
    @FXML private TextField station3CyclesField;
    @FXML private TextField station3TotalCyclesField;
    // endregion

    // region Modbus Addresses (Hardcoded)
    private static final int STATION1_TEST_TYPE_ADDR = 200;
    private static final int STATION1_VOLTAGE_ADDR = 201;
    private static final int STATION1_CURRENT_ADDR = 202;
    private static final int STATION1_NUM_CYCLES_ADDR = 203;
    private static final int STATION1_ON_TIME_ADDR = 204;
    private static final int STATION1_OFF_TIME_ADDR = 205;
    private static final int STATION1_CURRENT_CYCLE_COUNT_ADDR = 206;

    private static final int STATION2_CURRENT_CYCLE_COUNT_ADDR = 216;
    private static final int STATION3_CURRENT_CYCLE_COUNT_ADDR = 226;
    // endregion

    private List<TestTypeConfig> testConfigs;
    private ScheduledExecutorService cycleCountScheduler;
    private boolean isUpdatingComboBoxes = false;

    private final StationConfig savedStation1Config = new StationConfig();
    private final StationConfig savedStation2Config = new StationConfig();
    private final StationConfig savedStation3Config = new StationConfig();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("ExecutionViewModel initializing...");
        loadTestConfigs();
        populateTestTypeComboBoxes();
        populateStaticComboBoxes(); // Populate Voltage and Current
        setupStationListeners();
        setupInterlocks();
        // setupButtonActions() is no longer needed as we use @FXML annotations directly
        startPeriodicCycleCountRead();
    }

    private void loadTestConfigs() {
        try {
            testConfigs = TestTypeConfigManager.loadTestTypeConfigs();
            if (testConfigs == null || testConfigs.isEmpty()) {
                NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Config Error", "Could not load test configurations.");
            }
        } catch (Exception e) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Config Error", "Exception while loading test configs.");
            ApplicationLauncher.logger.error("Exception loading test configs", e);
        }
    }

    private void populateTestTypeComboBoxes() {
        if (testConfigs != null) {
            List<String> testNames = testConfigs.stream().map(TestTypeConfig::getName).collect(Collectors.toList());
            station1TestTypeComboBox.getItems().addAll(testNames);
            station2TestTypeComboBox.getItems().addAll(testNames);
            station3TestTypeComboBox.getItems().addAll(testNames);
        }
    }

    private void populateStaticComboBoxes() {
        List<String> voltages = Arrays.asList("230V", "240V", "250V");
        List<String> currents = Arrays.asList("6A", "10A", "13A", "16A");

        station1VoltageComboBox.getItems().setAll(voltages);
        station2VoltageComboBox.getItems().setAll(voltages);
        station3VoltageComboBox.getItems().setAll(voltages);

        station1CurrentComboBox.getItems().setAll(currents);
        station2CurrentComboBox.getItems().setAll(currents);
        station3CurrentComboBox.getItems().setAll(currents);
    }

    private void setupStationListeners() {
        station1TestTypeComboBox.valueProperty().addListener(createTestTypeListener(
                station1TestTypeDescField, station1NumOperationsField,
                station1CycleTimeField, station1OnTimeField, station1OffTimeField
        ));
        station2TestTypeComboBox.valueProperty().addListener(createTestTypeListener(
                station2TestTypeDescField, station2NumOperationsField,
                station2CycleTimeField, station2OnTimeField, station2OffTimeField
        ));
        station3TestTypeComboBox.valueProperty().addListener(createTestTypeListener(
                station3TestTypeDescField, station3NumOperationsField,
                station3CycleTimeField, station3OnTimeField, station3OffTimeField
        ));
    }

    private void setupInterlocks() {
        station1TestTypeComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station2TestTypeComboBox, station3TestTypeComboBox));
        station2TestTypeComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1TestTypeComboBox, station3TestTypeComboBox));
        station3TestTypeComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1TestTypeComboBox, station2TestTypeComboBox));

        station1VoltageComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station2VoltageComboBox, station3VoltageComboBox));
        station2VoltageComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1VoltageComboBox, station3VoltageComboBox));
        station3VoltageComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1VoltageComboBox, station2VoltageComboBox));

        station1CurrentComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station2CurrentComboBox, station3CurrentComboBox));
        station2CurrentComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1CurrentComboBox, station3CurrentComboBox));
        station3CurrentComboBox.valueProperty().addListener((obs, ov, nv) -> updateOthers(nv, station1CurrentComboBox, station2CurrentComboBox));
    }

    @SafeVarargs
    private final <T> void updateOthers(T newValue, ComboBox<T>... others) {
        if (isUpdatingComboBoxes) return;

        isUpdatingComboBoxes = true;
        for (ComboBox<T> other : others) {
            other.setValue(newValue);
        }
        isUpdatingComboBoxes = false;
    }

    // --- FXML Button Handlers ---

    @FXML private void handleStation1Start() { handleGenericButton("Start", 1); }
    @FXML private void handleStation1Stop() { handleGenericButton("Stop", 1); }
    @FXML private void handleStation1Reset() { handleGenericButton("Reset", 1); }
    @FXML private void handleStation1Save() { handleSave(1); }
    @FXML private void handleStation1Load() { handleLoad(1); }

    @FXML private void handleStation2Start() { handleGenericButton("Start", 2); }
    @FXML private void handleStation2Stop() { handleGenericButton("Stop", 2); }
    @FXML private void handleStation2Reset() { handleGenericButton("Reset", 2); }
    @FXML private void handleStation2Save() { handleSave(2); }
    @FXML private void handleStation2Load() { handleLoad(2); }

    @FXML private void handleStation3Start() { handleGenericButton("Start", 3); }
    @FXML private void handleStation3Stop() { handleGenericButton("Stop", 3); }
    @FXML private void handleStation3Reset() { handleGenericButton("Reset", 3); }
    @FXML private void handleStation3Save() { handleSave(3); }
    @FXML private void handleStation3Load() { handleLoad(3); }

    // --- Button Logic Implementation ---

    private void handleSave(int stationNum) {
        ApplicationLauncher.logger.info("Saving configuration for Station {}", stationNum);
        
        StationConfig config;
        ComboBox<String> testTypeCombo, voltageCombo, currentCombo;
        TextField numOpsField, onTimeField, offTimeField;

        switch(stationNum) {
            case 1:
                config = savedStation1Config; testTypeCombo = station1TestTypeComboBox; voltageCombo = station1VoltageComboBox; currentCombo = station1CurrentComboBox;
                numOpsField = station1NumOperationsField; onTimeField = station1OnTimeField; offTimeField = station1OffTimeField;
                break;
            case 2:
                config = savedStation2Config; testTypeCombo = station2TestTypeComboBox; voltageCombo = station2VoltageComboBox; currentCombo = station2CurrentComboBox;
                numOpsField = station2NumOperationsField; onTimeField = station2OnTimeField; offTimeField = station2OffTimeField;
                break;
            case 3:
                config = savedStation3Config; testTypeCombo = station3TestTypeComboBox; voltageCombo = station3VoltageComboBox; currentCombo = station3CurrentComboBox;
                numOpsField = station3NumOperationsField; onTimeField = station3OnTimeField; offTimeField = station3OffTimeField;
                break;
            default: return;
        }

        config.setTestType(testTypeCombo.getValue());
        config.setVoltage(voltageCombo.getValue());
        config.setCurrent(currentCombo.getValue());
        config.setNumOperations(numOpsField.getText());
        config.setOnTime(onTimeField.getText());
        config.setOffTime(offTimeField.getText());

        if (config.isValid()) {
            NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Config Saved", "Configuration for Station " + stationNum + " has been saved.");
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING, "Save Failed", "Please select all parameters before saving.");
        }
    }

    private void handleLoad(int stationNum) {
        ApplicationLauncher.logger.info("Loading configuration to PLC for Station {}", stationNum);
        
        StationConfig config;
        int[] addresses;

        switch(stationNum) {
            case 1:
                config = savedStation1Config;
                addresses = new int[]{STATION1_TEST_TYPE_ADDR, STATION1_VOLTAGE_ADDR, STATION1_CURRENT_ADDR, STATION1_NUM_CYCLES_ADDR, STATION1_ON_TIME_ADDR, STATION1_OFF_TIME_ADDR};
                break;
            case 2:
                config = savedStation2Config;
                addresses = new int[]{210, 211, 212, 213, 214, 215}; // Placeholder addresses
                break;
            case 3:
                config = savedStation3Config;
                addresses = new int[]{220, 221, 222, 223, 224, 225}; // Placeholder addresses
                break;
            default: return;
        }

        if (!config.isValid()) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Load Failed", "No valid configuration saved for Station " + stationNum + ".");
            return;
        }
        if (!ModbusService.isConnected()) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Modbus Error", "Modbus not connected.");
            return;
        }

        station1TotalCyclesField.setText(config.getNumOperations());
        station2TotalCyclesField.setText(config.getNumOperations());
        station3TotalCyclesField.setText(config.getNumOperations());

        new Thread(() -> {
            try {
                boolean t_success = ModbusService.writeRegister(addresses[0], 0); // Placeholder for test type
                boolean v_success = ModbusService.writeRegister(addresses[1], Integer.parseInt(config.getVoltage().replace("V", "").trim()));
                boolean c_success = ModbusService.writeRegister(addresses[2], Integer.parseInt(config.getCurrent().replace("A", "").trim()));
                boolean n_success = ModbusService.writeRegister(addresses[3], Integer.parseInt(config.getNumOperations()));
                boolean on_success = ModbusService.writeRegister(addresses[4], Integer.parseInt(config.getOnTime()));
                boolean off_success = ModbusService.writeRegister(addresses[5], Integer.parseInt(config.getOffTime()));

                Platform.runLater(() -> {
                    if (t_success && v_success && c_success && n_success && on_success && off_success) {
                        NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Load Complete", "Configuration for Station " + stationNum + " sent to PLC.");
                    } else {
                        NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Load Failed", "Failed to write one or more parameters to PLC for Station " + stationNum + ".");
                    }
                });
            } catch (NumberFormatException e) {
                Platform.runLater(() -> NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Data Error", "Invalid numeric data in saved configuration."));
            }
        }, "LoadConfigThread-Station" + stationNum).start();
    }
    
    private void handleGenericButton(String action, int stationNum) {
        ApplicationLauncher.logger.info("{} button clicked for Station {}", action, stationNum);
        NotificationManager.getInstance().showNotification(NotificationType.INFO, action + " Clicked", "Station " + stationNum + " " + action + " command issued.");
    }

    private void startPeriodicCycleCountRead() {
        if (cycleCountScheduler != null && !cycleCountScheduler.isShutdown()) {
            cycleCountScheduler.shutdownNow();
        }
        cycleCountScheduler = Executors.newSingleThreadScheduledExecutor();
        cycleCountScheduler.scheduleAtFixedRate(() -> {
            if (ModbusService.isConnected()) {
                Platform.runLater(this::updateAllCycleCounters);
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    private void updateAllCycleCounters() {
        new Thread(() -> {
            Integer value1 = ModbusService.readRegister(STATION1_CURRENT_CYCLE_COUNT_ADDR);
            if (value1 != null) Platform.runLater(() -> station1CyclesField.setText(String.valueOf(value1)));
        }).start();
        new Thread(() -> {
            Integer value2 = ModbusService.readRegister(STATION2_CURRENT_CYCLE_COUNT_ADDR);
            if (value2 != null) Platform.runLater(() -> station2CyclesField.setText(String.valueOf(value2)));
        }).start();
        new Thread(() -> {
            Integer value3 = ModbusService.readRegister(STATION3_CURRENT_CYCLE_COUNT_ADDR);
            if (value3 != null) Platform.runLater(() -> station3CyclesField.setText(String.valueOf(value3)));
        }).start();
    }

    public void cleanup() {
        if (cycleCountScheduler != null && !cycleCountScheduler.isShutdown()) {
            cycleCountScheduler.shutdownNow();
            ApplicationLauncher.logger.info("ExecutionViewModel scheduler shut down.");
        }
    }

    private ChangeListener<String> createTestTypeListener(TextField descField, TextField... fields) {
        return (obs, oldVal, newVal) -> {
            if (newVal == null) return;
            testConfigs.stream()
                    .filter(c -> c.getName().equals(newVal))
                    .findFirst()
                    .ifPresent(config -> {
                        descField.setText(config.getDescription());
                        fields[0].setText(config.getNumOperations());
                        fields[1].setText(config.getCycleTime());
                        fields[2].setText(config.getOnTime());
                        fields[3].setText(config.getOffTime());
                    });
        };
    }

    private static class StationConfig {
        private String testType, voltage, current, numOperations, onTime, offTime;
        public boolean isValid() { return testType != null && !testType.isEmpty() && voltage != null && !voltage.isEmpty() && current != null && !current.isEmpty(); }
        public void setTestType(String testType) { this.testType = testType; }
        public String getVoltage() { return voltage; }
        public void setVoltage(String voltage) { this.voltage = voltage; }
        public String getCurrent() { return current; }
        public void setCurrent(String current) { this.current = current; }
        public String getNumOperations() { return numOperations; }
        public void setNumOperations(String numOperations) { this.numOperations = numOperations; }
        public String getOnTime() { return onTime; }
        public void setOnTime(String onTime) { this.onTime = onTime; }
        public String getOffTime() { return offTime; }
        public void setOffTime(String offTime) { this.offTime = offTime; }
    }
}
