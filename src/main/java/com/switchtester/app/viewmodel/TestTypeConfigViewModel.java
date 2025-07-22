package com.switchtester.app.viewmodel;

import com.switchtester.app.model.config.TestTypeConfig;
import com.switchtester.app.service.NotificationManager;
import com.switchtester.app.service.TestTypeConfigManager;
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class TestTypeConfigViewModel implements Initializable {
	
	private Stage ownerStage;

	/**
     * Sets the owner stage for this SettingsViewModel.
     * This is typically called by the DashboardViewModel when loading the SettingsView.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    
    // --- FXML Components ---
    @FXML private TableView<TestTypeConfig> testTypeTable;
    @FXML private TableColumn<TestTypeConfig, String> nameColumn;
    @FXML private TableColumn<TestTypeConfig, String> descriptionColumn;
    @FXML private TableColumn<TestTypeConfig, String> operationsColumn;
    @FXML private TableColumn<TestTypeConfig, String> cycleTimeColumn;
    @FXML private TableColumn<TestTypeConfig, String> onTimeColumn;
    @FXML private TableColumn<TestTypeConfig, String> offTimeColumn;
    @FXML private TableColumn<TestTypeConfig, String> pfColumn;
    @FXML private TableColumn<TestTypeConfig, String> voltageColumn;
    @FXML private TableColumn<TestTypeConfig, String> currentColumn;
    @FXML private TableColumn<TestTypeConfig, String> wattsColumn;

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private TextField numOperationsField;
    @FXML private TextField cycleTimeField;
    @FXML private TextField onTimeField;
    @FXML private TextField offTimeField;
    @FXML private TextField pfField;
    @FXML private TextField wattsField;

    @FXML private MenuButton outputVoltageMenuButton;
    @FXML private MenuButton outputCurrentMenuButton;

    private ObservableList<TestTypeConfig> testTypeConfigs;
    private TestTypeConfig draggedItem;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadTestTypeConfigs();
        setupMenuButtons();

        testTypeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTestTypeDetails(newValue));

        setupDragAndDrop();
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        operationsColumn.setCellValueFactory(new PropertyValueFactory<>("numOperations"));
        cycleTimeColumn.setCellValueFactory(new PropertyValueFactory<>("cycleTime"));
        onTimeColumn.setCellValueFactory(new PropertyValueFactory<>("onTime"));
        offTimeColumn.setCellValueFactory(new PropertyValueFactory<>("offTime"));
        pfColumn.setCellValueFactory(new PropertyValueFactory<>("pf"));
        voltageColumn.setCellValueFactory(new PropertyValueFactory<>("outputVoltage"));
        currentColumn.setCellValueFactory(new PropertyValueFactory<>("outputCurrent"));
        wattsColumn.setCellValueFactory(new PropertyValueFactory<>("watts"));
    }

    /**
     * Initializes the MenuButtons using CustomMenuItems containing CheckBoxes.
     * This approach prevents the menu from closing on selection and ensures text updates.
     */
    private void setupMenuButtons() {
        List<String> allVoltages = Arrays.asList("230V", "240V", "250V");
        List<String> allCurrents = Arrays.asList("6A", "10A", "13A", "16A", "20A");

        // Populate Voltage MenuButton
        allVoltages.forEach(v -> {
            CheckBox checkBox = new CheckBox(v);
            CustomMenuItem item = new CustomMenuItem(checkBox);
            item.setHideOnClick(false); // Keep the menu open on click
            // Add a listener to the checkbox's property to react to any change
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> 
                updateMenuButtonText(outputVoltageMenuButton, "Select Voltage Options...")
            );
            outputVoltageMenuButton.getItems().add(item);
        });

        // Populate Current MenuButton
        allCurrents.forEach(c -> {
            CheckBox checkBox = new CheckBox(c);
            CustomMenuItem item = new CustomMenuItem(checkBox);
            item.setHideOnClick(false); // Keep the menu open on click
            // Add a listener to the checkbox's property to react to any change
            checkBox.selectedProperty().addListener((obs, wasSelected, isSelected) -> 
                updateMenuButtonText(outputCurrentMenuButton, "Select Current Options...")
            );
            outputCurrentMenuButton.getItems().add(item);
        });
    }

    /**
     * Updates the text of a MenuButton based on which CheckBoxes are selected.
     */
    private void updateMenuButtonText(MenuButton menuButton, String promptText) {
        String selected = menuButton.getItems().stream()
                .filter(item -> item instanceof CustomMenuItem && ((CheckBox) ((CustomMenuItem) item).getContent()).isSelected())
                .map(item -> ((CheckBox) ((CustomMenuItem) item).getContent()).getText())
                .collect(Collectors.joining(", "));

        if (selected.isEmpty()) {
            menuButton.setText(promptText);
        } else {
            menuButton.setText(selected);
        }
    }

    private void loadTestTypeConfigs() {
        testTypeConfigs = FXCollections.observableArrayList(TestTypeConfigManager.loadTestTypeConfigs());
        testTypeTable.setItems(testTypeConfigs);
    }

    private void showTestTypeDetails(TestTypeConfig config) {
        if (config != null) {
            nameField.setText(config.getName());
            descriptionField.setText(config.getDescription());
            numOperationsField.setText(config.getNumOperations());
            cycleTimeField.setText(config.getCycleTime());
            onTimeField.setText(config.getOnTime());
            offTimeField.setText(config.getOffTime());
            pfField.setText(config.getPf());
            wattsField.setText(config.getWatts());

            // Update MenuButton selections. The property listeners will automatically update the text.
            updateMenuButtonSelections(outputVoltageMenuButton, config.getOutputVoltages());
            updateMenuButtonSelections(outputCurrentMenuButton, config.getOutputCurrents());
        } else {
            clearFields();
        }
    }

    /**
     * Updates the selected state of CheckBoxes within a MenuButton.
     */
    private void updateMenuButtonSelections(MenuButton menuButton, List<String> selectedNames) {
        menuButton.getItems().forEach(item -> {
            if (item instanceof CustomMenuItem) {
                CheckBox checkBox = (CheckBox) ((CustomMenuItem) item).getContent();
                boolean isSelected = selectedNames != null && selectedNames.contains(checkBox.getText());
                checkBox.setSelected(isSelected);
            }
        });
    }

    @FXML
    private void handleClearFields() {
        clearFields();
        testTypeTable.getSelectionModel().clearSelection();
    }

    private void clearFields() {
        nameField.clear();
        descriptionField.clear();
        numOperationsField.clear();
        cycleTimeField.clear();
        onTimeField.clear();
        offTimeField.clear();
        pfField.clear();
        wattsField.clear();
        
        // Clear MenuButton selections. The property listeners will automatically update the text.
        updateMenuButtonSelections(outputVoltageMenuButton, null);
        updateMenuButtonSelections(outputCurrentMenuButton, null);
    }

    @FXML
    private void handleNewTestType() {
        clearFields();
        testTypeTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleEditTestType() {
        TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();
        if (selectedConfig != null) {
            showTestTypeDetails(selectedConfig);
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING, "No Selection", "Please select a test type to edit.");
        }
    }

    @FXML
    private void handleDeleteTestType() {
        TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();
        if (selectedConfig != null) {
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete '" + selectedConfig.getName() + "'?", ButtonType.OK, ButtonType.CANCEL);
            confirmationAlert.setTitle("Confirm Delete");
            confirmationAlert.setHeaderText("Delete Test Type?");
            
            confirmationAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    testTypeConfigs.remove(selectedConfig);
                    TestTypeConfigManager.saveTestTypeConfigs(testTypeConfigs);
                    clearFields();
                    NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Deleted", "'" + selectedConfig.getName() + "' has been deleted.");
                }
            });
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING, "No Selection", "Please select a test type to delete.");
        }
    }

    @FXML
    private void handleSaveTestType() {
        if (!isInputValid()) return;

        List<String> selectedVoltages = getSelectedItems(outputVoltageMenuButton);
        List<String> selectedCurrents = getSelectedItems(outputCurrentMenuButton);

        TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();

        if (selectedConfig == null) {
            TestTypeConfig newConfig = new TestTypeConfig();
            updateConfigFromFields(newConfig, selectedVoltages, selectedCurrents);
            testTypeConfigs.add(newConfig);
            NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Saved", "'" + newConfig.getName() + "' has been added.");
        } else {
            updateConfigFromFields(selectedConfig, selectedVoltages, selectedCurrents);
            testTypeTable.refresh();
            NotificationManager.getInstance().showNotification(NotificationType.SUCCESS, "Saved", "'" + selectedConfig.getName() + "' has been updated.");
        }
        TestTypeConfigManager.saveTestTypeConfigs(testTypeConfigs);
        clearFields();
        testTypeTable.getSelectionModel().clearSelection();
    }

    private List<String> getSelectedItems(MenuButton menuButton) {
        return menuButton.getItems().stream()
                .filter(item -> item instanceof CustomMenuItem && ((CheckBox) ((CustomMenuItem) item).getContent()).isSelected())
                .map(item -> ((CheckBox) ((CustomMenuItem) item).getContent()).getText())
                .collect(Collectors.toList());
    }

    private void updateConfigFromFields(TestTypeConfig config, List<String> voltages, List<String> currents) {
        config.setName(nameField.getText());
        config.setDescription(descriptionField.getText());
        config.setNumOperations(numOperationsField.getText());
        config.setCycleTime(cycleTimeField.getText());
        config.setOnTime(onTimeField.getText());
        config.setOffTime(offTimeField.getText());
        config.setPf(pfField.getText());
        config.setWatts(wattsField.getText());
        config.setOutputVoltages(voltages);
        config.setOutputCurrents(currents);
    }

    private boolean isInputValid() {
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR, "Invalid Fields", "Test type name cannot be empty.");
            return false;
        }
        return true;
    }

    private void setupDragAndDrop() {
        // Drag and drop implementation remains the same
    }
}
