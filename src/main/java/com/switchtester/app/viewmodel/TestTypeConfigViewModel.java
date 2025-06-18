package com.switchtester.app.viewmodel;

import com.switchtester.app.model.TestTypeConfig;
import com.switchtester.app.service.TestTypeConfigManager;
import com.switchtester.app.service.NotificationManager; // Import NotificationManager
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType; // Import NotificationType enum

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert; // Keep Alert import if still used elsewhere, but will remove showAlert method
import javafx.scene.control.ButtonType; // Keep ButtonType if still used elsewhere
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.stage.Stage; // Keep Stage import if setOwnerStage is still used

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class TestTypeConfigViewModel implements Initializable {

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
    @FXML private TextField outputVoltageField;
    @FXML private TextField outputCurrentField;
    @FXML private TextField wattsField;

    private ObservableList<TestTypeConfig> testTypeConfigs;
    private TestTypeConfig draggedItem; // To store the item being dragged

    // Removed ownerStage as showAlert is no longer used.
    // If other parts of SettingsViewModel need ownerStage, it should be kept there.
    // private Stage ownerStage;

    /**
     * Sets the owner stage for alert dialogs.
     * This method is called by the SettingsViewModel when loading this view.
     * This method is no longer strictly needed for alerts, but might be for other dialogs.
     * Keeping it for now for compatibility.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        // this.ownerStage = stage; // No longer directly used for alerts
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize TableView columns
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

        // Load configurations
        loadTestTypeConfigs();

        // Listen for selection changes and show the Test Type details.
        testTypeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> showTestTypeDetails(newValue));

        // --- Drag and Drop Implementation ---
        testTypeTable.setRowFactory(tv -> {
            TableRow<TestTypeConfig> row = new TableRow<>();

            row.setOnDragDetected(event -> {
                if (!row.isEmpty() && row.getItem() != null) {
                    Dragboard db = row.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    // Store the index of the dragged row, not the item itself, for reordering
                    content.putString(String.valueOf(row.getIndex()));
                    db.setContent(content);
                    draggedItem = row.getItem(); // Store the actual item
                    event.consume();
                }
            });

            row.setOnDragOver(event -> {
                Dragboard db = event.getDragboard();
                if (db.hasString() && draggedItem != null && !row.isEmpty() && row.getItem() != draggedItem) {
                    event.acceptTransferModes(TransferMode.MOVE);
                    // Optional: Add visual feedback for drag over (e.g., highlight the row)
                    row.setStyle("-fx-background-color: #e0e0e0;"); // Light grey background
                }
                event.consume();
            });

            row.setOnDragExited(event -> {
                // Remove visual feedback when drag leaves the row
                row.setStyle(""); // Reset style
                event.consume();
            });

            row.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString() && draggedItem != null) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    int droppedIdx = row.isEmpty() ? testTypeConfigs.size() : row.getIndex();

                    // Perform the reorder
                    TestTypeConfig temp = testTypeConfigs.remove(draggedIdx);
                    testTypeConfigs.add(droppedIdx, temp);

                    // Select the newly moved item
                    testTypeTable.getSelectionModel().select(temp);

                    // Save the new order
                    TestTypeConfigManager.saveTestTypeConfigs(testTypeConfigs);
                    success = true;
                }
                event.setDropCompleted(success);
                draggedItem = null; // Clear dragged item
                event.consume();
            });

            row.setOnDragDone(event -> {
                // Clean up after drag operation
                draggedItem = null;
                // Ensure all rows reset their style
                testTypeTable.refresh(); // Forces redraw of all rows to clear any lingering drag-over styles
                event.consume();
            });

            return row;
        });
    }

    /**
     * Loads test type configurations from the manager and populates the TableView.
     */
    private void loadTestTypeConfigs() {
        testTypeConfigs = FXCollections.observableArrayList(TestTypeConfigManager.loadTestTypeConfigs());
        testTypeTable.setItems(testTypeConfigs);
    }

    /**
     * Fills all text fields with details of the given TestTypeConfig.
     * If the TestTypeConfig is null, clears all text fields.
     * @param config The TestTypeConfig to display, or null to clear fields.
     */
    private void showTestTypeDetails(TestTypeConfig config) {
        if (config != null) {
            nameField.setText(config.getName());
            descriptionField.setText(config.getDescription());
            numOperationsField.setText(config.getNumOperations());
            cycleTimeField.setText(config.getCycleTime());
            onTimeField.setText(config.getOnTime());
            offTimeField.setText(config.getOffTime());
            pfField.setText(config.getPf());
            outputVoltageField.setText(config.getOutputVoltage());
            outputCurrentField.setText(config.getOutputCurrent());
            wattsField.setText(config.getWatts());
        } else {
            clearFields();
        }
    }

    /**
     * Clears all input fields.
     */
    @FXML
    private void handleClearFields() {
        clearFields();
        testTypeTable.getSelectionModel().clearSelection(); // Deselect item in table
    }

    private void clearFields() {
        nameField.setText("");
        descriptionField.setText("");
        numOperationsField.setText("");
        cycleTimeField.setText("");
        onTimeField.setText("");
        offTimeField.setText("");
        pfField.setText("");
        outputVoltageField.setText("");
        outputCurrentField.setText("");
        wattsField.setText("");
    }

    /**
     * Handles the "New" button click. Clears fields for a new entry.
     */
    @FXML
    private void handleNewTestType() {
        clearFields();
        testTypeTable.getSelectionModel().clearSelection();
    }

    /**
     * Handles the "Edit" button click. Populates fields with selected item's data.
     */
    @FXML
    private void handleEditTestType() {
        TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();
        if (selectedConfig != null) {
            showTestTypeDetails(selectedConfig);
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                                               "No Selection",
                                                               "Please select a test type from the table to edit.");
        }
    }

    /**
     * Handles the "Delete" button click. Removes the selected test type.
     */
    @FXML
    private void handleDeleteTestType() {
        TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();
        if (selectedConfig != null) {
            // For confirmation, we still need a modal dialog.
            // Notification is for transient messages, not user input.
            // Reverting to Alert for confirmation as per previous discussion.
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Delete");
            confirmationAlert.setHeaderText("Delete Test Type?");
            confirmationAlert.setContentText("Are you sure you want to delete '" + selectedConfig.getName() + "'?");
            // If ownerStage was passed, set it here:
            // if (ownerStage != null) { confirmationAlert.initOwner(ownerStage); }
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                testTypeConfigs.remove(selectedConfig);
                TestTypeConfigManager.saveTestTypeConfigs(testTypeConfigs);
                clearFields();
                NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                                                   "Deleted",
                                                                   "'" + selectedConfig.getName() + "' has been deleted successfully.");
            }
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                                               "No Selection",
                                                               "Please select a test type from the table to delete.");
        }
    }

    /**
     * Handles the "Save" button click. Adds a new test type or updates an existing one.
     */
    @FXML
    private void handleSaveTestType() {
        if (isInputValid()) {
            String name = nameField.getText();
            String description = descriptionField.getText();
            String numOperations = numOperationsField.getText();
            String cycleTime = cycleTimeField.getText();
            String onTime = onTimeField.getText();
            String offTime = offTimeField.getText();
            String pf = pfField.getText();
            String outputVoltage = outputVoltageField.getText();
            String outputCurrent = outputCurrentField.getText();
            String watts = wattsField.getText();

            TestTypeConfig selectedConfig = testTypeTable.getSelectionModel().getSelectedItem();

            if (selectedConfig == null) { // New Test Type
                // Check for duplicate name
                if (testTypeConfigs.stream().anyMatch(config -> config.getName().equalsIgnoreCase(name))) {
                    NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                                       "Duplicate Name",
                                                                       "A test type with this name already exists. Please choose a different name.");
                    return;
                }
                TestTypeConfig newConfig = new TestTypeConfig(name, description, numOperations, cycleTime, onTime, offTime, pf, outputVoltage, outputCurrent, watts);
                testTypeConfigs.add(newConfig);
                NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                                                   "Saved",
                                                                   "'" + name + "' has been added successfully.");
            } else { // Update existing Test Type
                // Check for duplicate name if name was changed
                if (!selectedConfig.getName().equalsIgnoreCase(name) &&
                    testTypeConfigs.stream().anyMatch(config -> config.getName().equalsIgnoreCase(name))) {
                    NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                                       "Duplicate Name",
                                                                       "A test type with this name already exists. Please choose a different name.");
                    return;
                }
                selectedConfig.setName(name);
                selectedConfig.setDescription(description);
                selectedConfig.setNumOperations(numOperations);
                selectedConfig.setCycleTime(cycleTime);
                selectedConfig.setOnTime(onTime);
                selectedConfig.setOffTime(offTime);
                selectedConfig.setPf(pf);
                selectedConfig.setOutputVoltage(outputVoltage);
                selectedConfig.setOutputCurrent(outputCurrent);
                selectedConfig.setWatts(watts);
                testTypeTable.refresh(); // Refresh the table to show updated values
                NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                                                   "Saved",
                                                                   "'" + name + "' has been updated successfully.");
            }
            TestTypeConfigManager.saveTestTypeConfigs(testTypeConfigs);
            clearFields();
            testTypeTable.getSelectionModel().clearSelection();
        }
    }

    /**
     * Validates the input fields.
     * @return true if input is valid, false otherwise.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            errorMessage += "No valid test type name!";
        }
        // Add more validation rules as needed for other fields (e.g., numeric checks)

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                               "Invalid Fields",
                                                               errorMessage);
            return false;
        }
    }

    // Removed the old showAlert helper method
}
