package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * ViewModel for the Splash Screen with animated progress.
 */
public class SplashViewModel implements Initializable {

    private Stage splashStage;

    @FXML
    private ProgressBar loadingProgressBar;

    public void setSplashStage(Stage stage) {
        this.splashStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Task<Void> loadingTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                // Stage 1: Slow start to 25%
                for (int i = 1; i <= 25; i++) {
                    updateProgress(i / 100.0, 1.0);
                    Thread.sleep(30); // slow
                }

                Thread.sleep(400); // pause/anticipation

                // Stage 2: Fast jump to 80%
                for (int i = 26; i <= 80; i++) {
                    updateProgress(i / 100.0, 1.0);
                    Thread.sleep(10); // fast
                }

                Thread.sleep(600); // pause again

                // Stage 3: Sudden finish
                for (int i = 81; i <= 100; i++) {
                    updateProgress(i / 100.0, 1.0);
                    Thread.sleep(5); // almost instant
                }

                return null;
            }

            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (splashStage != null) splashStage.close();
                    ApplicationLauncher.showLoginScreen();
                });
            }

            @Override
            protected void failed() {
                ApplicationLauncher.logger.error("Splash screen loading failed: " + getException().getMessage());
                Platform.runLater(() -> {
                    if (splashStage != null) splashStage.close();
                    ApplicationLauncher.showLoginScreen();
                });
            }
        };

        loadingProgressBar.progressProperty().bind(loadingTask.progressProperty());
        new Thread(loadingTask).start();
    }
}
