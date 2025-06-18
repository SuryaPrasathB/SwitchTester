package com.switchtester.app.viewmodel;

import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import com.switchtester.app.ApplicationLauncher;

/**
 * ViewModel for the Projects Screen.
 */
public class ProjectsViewModel implements Initializable {
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Projects View Initialized.");
    }
}
