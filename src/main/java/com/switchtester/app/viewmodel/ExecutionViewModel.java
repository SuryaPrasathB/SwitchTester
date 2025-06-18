package com.switchtester.app.viewmodel;

import java.net.URL;
import java.util.ResourceBundle;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.TestTypeConfig;
import com.switchtester.app.service.TestTypeConfigManager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;

/**
 * ViewModel for the Execution Screen.
 * Handles the logic for managing test execution for each station,
 * including populating configurable options and handling actions.
 */
public class ExecutionViewModel implements Initializable {

    // FXML elements for Station 1
    @FXML private ComboBox<TestTypeConfig> station1TestTypeComboBox; // Changed to TestTypeConfig
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
    @FXML private TextField station1VoltageField; // Output Voltage
    @FXML private TextField station1CurrentField; // Output Current
    @FXML private TextField station1WattsField;
    @FXML private TextField station1PfField;
    @FXML private TextField station1CyclesField;

    // FXML elements for Station 2
    @FXML private ComboBox<TestTypeConfig> station2TestTypeComboBox; // Changed to TestTypeConfig
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
    @FXML private TextField station2VoltageField; // Output Voltage
    @FXML private TextField station2CurrentField; // Output Current
    @FXML private TextField station2WattsField;
    @FXML private TextField station2PfField;
    @FXML private TextField station2CyclesField;

    // FXML elements for Station 3
    @FXML private ComboBox<TestTypeConfig> station3TestTypeComboBox; // Changed to TestTypeConfig
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
    @FXML private TextField station3VoltageField; // Output Voltage
    @FXML private TextField station3CurrentField; // Output Current
    @FXML private TextField station3WattsField;
    @FXML private TextField station3PfField;
    @FXML private TextField station3CyclesField;

    // ObservableList for Test Types (now loaded from config)
    private ObservableList<TestTypeConfig> availableTestTypes;

    // Dummy data for Voltage and Current (remain hardcoded as per request)
    private ObservableList<String> voltages = FXCollections.observableArrayList(
            "12 V", "24 V", "48 V", "120 V", "230 V", "240 V", "400 V"
    );

    private ObservableList<String> currents = FXCollections.observableArrayList(
            "1 A", "2 A", "5 A", "6 A", "10 A", "15 A", "20 A"
    );

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Execution View Initialized.");

        // Load test types from configuration
        availableTestTypes = FXCollections.observableArrayList(TestTypeConfigManager.loadTestTypeConfigs());

        // Populate ComboBoxes
        station1TestTypeComboBox.setItems(availableTestTypes);
        station1VoltageComboBox.setItems(voltages);
        station1CurrentComboBox.setItems(currents);

        station2TestTypeComboBox.setItems(availableTestTypes);
        station2VoltageComboBox.setItems(voltages);
        station2CurrentComboBox.setItems(currents);

        station3TestTypeComboBox.setItems(availableTestTypes);
        station3VoltageComboBox.setItems(voltages);
        station3CurrentComboBox.setItems(currents);

        // Set initial dummy values for parameters and descriptions
        setInitialStationData(1);
        setInitialStationData(2);
        setInitialStationData(3);

        // Add listeners to ComboBoxes to update parameters
        station1TestTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(1, newVal, station1VoltageComboBox.getValue(), station1CurrentComboBox.getValue()));
        station1VoltageComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(1, station1TestTypeComboBox.getValue(), newVal, station1CurrentComboBox.getValue()));
        station1CurrentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(1, station1TestTypeComboBox.getValue(), station1VoltageComboBox.getValue(), newVal));

        station2TestTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(2, newVal, station2VoltageComboBox.getValue(), station2CurrentComboBox.getValue()));
        station2VoltageComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(2, station2TestTypeComboBox.getValue(), newVal, station2CurrentComboBox.getValue()));
        station2CurrentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(2, station2TestTypeComboBox.getValue(), station2VoltageComboBox.getValue(), newVal));

        station3TestTypeComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(3, newVal, station3VoltageComboBox.getValue(), station3CurrentComboBox.getValue()));
        station3VoltageComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(3, station3TestTypeComboBox.getValue(), newVal, station3CurrentComboBox.getValue()));
        station3CurrentComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                updateStationParameters(3, station3TestTypeComboBox.getValue(), station3VoltageComboBox.getValue(), newVal));
    }

    /**
     * Sets initial data for a specific station's parameters and descriptions.
     * Selects the first available test type and default voltage/current.
     * @param stationNumber The station number (1, 2, or 3).
     */
    private void setInitialStationData(int stationNumber) {
        TestTypeConfig defaultTestType = availableTestTypes.isEmpty() ? null : availableTestTypes.get(0);
        String defaultVoltage = voltages.isEmpty() ? null : voltages.get(0);
        String defaultCurrent = currents.isEmpty() ? null : currents.get(0);

        switch (stationNumber) {
            case 1:
                station1TestTypeComboBox.getSelectionModel().select(defaultTestType);
                station1VoltageComboBox.getSelectionModel().select(defaultVoltage);
                station1CurrentComboBox.getSelectionModel().select(defaultCurrent);
                updateStationParameters(1, defaultTestType, defaultVoltage, defaultCurrent);
                break;
            case 2:
                station2TestTypeComboBox.getSelectionModel().select(defaultTestType);
                station2VoltageComboBox.getSelectionModel().select(defaultVoltage);
                station2CurrentComboBox.getSelectionModel().select(defaultCurrent);
                updateStationParameters(2, defaultTestType, defaultVoltage, defaultCurrent);
                break;
            case 3:
                station3TestTypeComboBox.getSelectionModel().select(defaultTestType);
                station3VoltageComboBox.getSelectionModel().select(defaultVoltage);
                station3CurrentComboBox.getSelectionModel().select(defaultCurrent);
                updateStationParameters(3, defaultTestType, defaultVoltage, defaultCurrent);
                break;
        }
    }

    /**
     * Updates the parameter fields based on the selected test type, voltage, and current.
     * This method now fetches data from the TestTypeConfig object.
     * @param stationNumber The station number (1, 2, or 3).
     * @param selectedTestTypeConfig The currently selected TestTypeConfig object.
     * @param selectedVoltage The currently selected switch voltage.
     * @param selectedCurrent The currently selected switch current.
     */
    private void updateStationParameters(int stationNumber, TestTypeConfig selectedTestTypeConfig, String selectedVoltage, String selectedCurrent) {
        String testTypeDesc = "";
        String numOperations = "N/A";
        String cycleTime = "N/A";
        String onTime = "N/A";
        String offTime = "N/A";
        String pf = "N/A";
        String outputVoltage = selectedVoltage != null ? selectedVoltage : "N/A";
        String outputCurrent = selectedCurrent != null ? selectedCurrent : "N/A";
        String watts = "N/A";

        if (selectedTestTypeConfig != null) {
            testTypeDesc = selectedTestTypeConfig.getDescription();
            numOperations = selectedTestTypeConfig.getNumOperations();
            cycleTime = selectedTestTypeConfig.getCycleTime();
            onTime = selectedTestTypeConfig.getOnTime();
            offTime = selectedTestTypeConfig.getOffTime();
            pf = selectedTestTypeConfig.getPf();

            // Override output voltage/current if specified in TestTypeConfig
            if (selectedTestTypeConfig.getOutputVoltage() != null && !selectedTestTypeConfig.getOutputVoltage().isEmpty() && !"N/A".equalsIgnoreCase(selectedTestTypeConfig.getOutputVoltage())) {
                outputVoltage = selectedTestTypeConfig.getOutputVoltage();
            }
            if (selectedTestTypeConfig.getOutputCurrent() != null && !selectedTestTypeConfig.getOutputCurrent().isEmpty() && !"N/A".equalsIgnoreCase(selectedTestTypeConfig.getOutputCurrent())) {
                outputCurrent = selectedTestTypeConfig.getOutputCurrent();
            }
        }

        // Calculate Watts if voltage and current are numbers and PF is available
        try {
            double voltageVal = Double.parseDouble(outputVoltage.replaceAll("[^\\d.]", ""));
            double currentVal = Double.parseDouble(outputCurrent.replaceAll("[^\\d.]", ""));
            double pfVal = Double.parseDouble(pf);
            if (!Double.isNaN(voltageVal) && !Double.isNaN(currentVal) && !Double.isNaN(pfVal)) {
                watts = String.format("%.2f W", voltageVal * currentVal * pfVal);
            }
        } catch (NumberFormatException e) {
            watts = "N/A"; // If parsing fails, keep as N/A
        }


        // Update the fields for the specific station
        switch (stationNumber) {
            case 1:
                station1TestTypeDescField.setText(testTypeDesc);
                station1NumOperationsField.setText(numOperations);
                station1CycleTimeField.setText(cycleTime);
                station1OnTimeField.setText(onTime);
                station1OffTimeField.setText(offTime);
                station1VoltageField.setText(outputVoltage);
                station1CurrentField.setText(outputCurrent);
                station1WattsField.setText(watts);
                station1PfField.setText(pf);
                break;
            case 2:
                station2TestTypeDescField.setText(testTypeDesc);
                station2NumOperationsField.setText(numOperations);
                station2CycleTimeField.setText(cycleTime);
                station2OnTimeField.setText(onTime);
                station2OffTimeField.setText(offTime);
                station2VoltageField.setText(outputVoltage);
                station2CurrentField.setText(outputCurrent);
                station2WattsField.setText(watts);
                station2PfField.setText(pf);
                break;
            case 3:
                station3TestTypeDescField.setText(testTypeDesc);
                station3NumOperationsField.setText(numOperations);
                station3CycleTimeField.setText(cycleTime);
                station3OnTimeField.setText(onTime);
                station3OffTimeField.setText(offTime);
                station3VoltageField.setText(outputVoltage);
                station3CurrentField.setText(outputCurrent);
                station3WattsField.setText(watts);
                station3PfField.setText(pf);
                break;
        }
    }


    // --- Action Handlers for Station 1 ---
    @FXML
    private void handleStation1Start(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 1 START clicked!");
        // Logic to start test on Station 1
        station1CyclesField.setText("Running..."); // Update status
    }

    @FXML
    private void handleStation1Stop(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 1 STOP clicked!");
        // Logic to stop test on Station 1
        station1CyclesField.setText("Stopped"); // Update status
    }

    @FXML
    private void handleStation1Reset(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 1 RESET clicked!");
        // Logic to reset Station 1
        station1CyclesField.setText("0"); // Reset cycles
    }

    @FXML
    private void handleStation1Save(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 1 SAVE clicked!");
        // Logic to save current configuration for Station 1
    }

    @FXML
    private void handleStation1Load(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 1 LOAD clicked!");
        // Logic to load a saved configuration for Station 1
    }

    // --- Action Handlers for Station 2 ---
    @FXML
    private void handleStation2Start(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 2 START clicked!");
        // Logic to start test on Station 2
        station2CyclesField.setText("Running..."); // Update status
    }

    @FXML
    private void handleStation2Stop(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 2 STOP clicked!");
        // Logic to stop test on Station 2
        station2CyclesField.setText("Stopped"); // Update status
    }

    @FXML
    private void handleStation2Reset(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 2 RESET clicked!");
        // Logic to reset Station 2
        station2CyclesField.setText("0"); // Reset cycles
    }

    @FXML
    private void handleStation2Save(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 2 SAVE clicked!");
        // Logic to save current configuration for Station 2
    }

    @FXML
    private void handleStation2Load(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 2 LOAD clicked!");
        // Logic to load a saved configuration for Station 2
    }

    // --- Action Handlers for Station 3 ---
    @FXML
    private void handleStation3Start(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 3 START clicked!");
        // Logic to start test on Station 3
        station3CyclesField.setText("Running..."); // Update status
    }

    @FXML
    private void handleStation3Stop(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 3 STOP clicked!");
        // Logic to stop test on Station 3
        station3CyclesField.setText("Stopped"); // Update status
    }

    @FXML
    private void handleStation3Reset(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 3 RESET clicked!");
        // Logic to reset Station 3
        station3CyclesField.setText("0"); // Reset cycles
    }

    @FXML
    private void handleStation3Save(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 3 SAVE clicked!");
        // Logic to save current configuration for Station 3
    }

    @FXML
    private void handleStation3Load(ActionEvent event) {
        ApplicationLauncher.logger.info("Station 3 LOAD clicked!");
        // Logic to load a saved configuration for Station 3
    }
}