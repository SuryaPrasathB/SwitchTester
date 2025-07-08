package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher; // Import ApplicationLauncher for logger
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; // Import Stage
import java.util.function.Consumer; // Import Consumer

public class SettingsViewModel {

    @FXML
    private StackPane mainContentPane;

    private Stage ownerStage; // New field to hold the owner stage for alerts in sub-views
    private Consumer<String> mainScreenTitleUpdater; // Callback to update the main dashboard title

    /**
     * Sets the owner stage for this SettingsViewModel.
     * This is typically called by the DashboardViewModel when loading the SettingsView.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    /**
     * Sets the callback function to update the main screen's title.
     * @param mainScreenTitleUpdater A Consumer that accepts the new title string.
     */
    public void setMainScreenTitleUpdater(Consumer<String> mainScreenTitleUpdater) {
        this.mainScreenTitleUpdater = mainScreenTitleUpdater;
    }

    @FXML
    public void initialize() {
        ApplicationLauncher.logger.info("SettingsViewModel initialized.");
        // Optionally load default content when settings page opens
        handleTestTypeConfig(); // Load Test Type Config to be the default view
    }

    @FXML
    public void handleTestTypeConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/TestTypeConfigView.fxml"));
            Pane testTypeConfigPane = loader.load();

            // Get the controller for TestTypeConfigView
            TestTypeConfigViewModel testTypeConfigController = loader.getController();

            // Pass the ownerStage (which SettingsViewModel received from DashboardViewModel)
            // to the TestTypeConfigViewModel. This ensures alerts from TestTypeConfigViewModel
            // are owned by the main application stage.
            testTypeConfigController.setOwnerStage(this.ownerStage);

            mainContentPane.getChildren().setAll(testTypeConfigPane);

            // Update the main dashboard's screen title
            if (mainScreenTitleUpdater != null) {
                mainScreenTitleUpdater.accept("Test Type Configuration"); // Set the desired title
                ApplicationLauncher.logger.debug("Main screen title updated to 'Test Type Configuration' from Settings.");
            }
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading TestTypeConfigView.fxml: {}", e.getMessage(), e);
            // Optionally, show an alert to the user
        }
    }

    @FXML
    public void handleArduinoOptaConfig() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/ArduinoOptaConfigView.fxml"));
            Pane arduinoOptaConfigPane = loader.load();

            // Get the controller for ArduinoOptaConfigViewModel
            ArduinoOptaConfigViewModel arduinoOptaConfigController = loader.getController();

            // Pass the ownerStage to the ArduinoOptaConfigViewModel
            arduinoOptaConfigController.setOwnerStage(this.ownerStage);

            mainContentPane.getChildren().setAll(arduinoOptaConfigPane);

            // Update the main dashboard's screen title
            if (mainScreenTitleUpdater != null) {
                mainScreenTitleUpdater.accept("Arduino Opta Configuration"); // Set the desired title
                ApplicationLauncher.logger.debug("Main screen title updated to 'Arduino Opta Configuration' from Settings.");
            }
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading ArduinoOptaConfigView.fxml: {}", e.getMessage(), e);
            // Optionally, show an alert to the user
        }
    }
}
