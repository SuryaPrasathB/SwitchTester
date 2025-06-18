package com.switchtester.app.viewmodel;

import java.io.IOException;

import com.switchtester.app.ApplicationLauncher;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage; // Import Stage

public class SettingsViewModel {

    @FXML
    private StackPane mainContentPane;

    private Stage ownerStage; // New field to hold the owner stage for alerts in sub-views

    /**
     * Sets the owner stage for this SettingsViewModel.
     * This is typically called by the DashboardViewModel when loading the SettingsView.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    @FXML
    public void initialize() {
        // Optionally load default content when settings page opens
        // handleTestTypeConfig(); // Uncomment if you want Test Type Config to be the default view
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
        } catch (IOException e) {
            e.printStackTrace(); // Log the error
            ApplicationLauncher.logger.error("Error loading TestTypeConfigView.fxml: " + e.getMessage());
            // Optionally, show an alert to the user
        }
    }

    // Add handlers for other navigation links as they are added
}
