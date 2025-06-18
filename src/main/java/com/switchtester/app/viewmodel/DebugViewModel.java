package com.switchtester.app.viewmodel;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import com.switchtester.app.ApplicationLauncher;

/**
 * ViewModel for the Debug Screen.
 * This is a placeholder for future debugging tools and logs.
 */
public class DebugViewModel implements Initializable {

    /**
     * Initializes the controller class. This method is automatically called
     * after the FXML file has been loaded.
     *
     * @param location The location used to resolve relative paths for the root object, or null if the location is not known.
     * @param resources The resources used to localize the root object, or null if the root object was not localized.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Debug View Initialized.");
        // Future: Initialize logging output, connect to debug services, etc.
    }
}
