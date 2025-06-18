package com.switchtester.app.service;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.viewmodel.NotificationViewModel;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox; // We'll use a VBox to stack notifications

import java.io.IOException;
import java.net.URL;

/**
 * Manages the display of custom notifications within the application.
 * This is a singleton class that allows any part of the application to show a notification.
 * Notifications are displayed in a designated VBox on the main dashboard.
 */
public class NotificationManager {

    private static NotificationManager instance;
    private VBox notificationStack; // The container on the DashboardView.fxml

    // Private constructor to enforce singleton pattern
    private NotificationManager() {
    }

    /**
     * Returns the singleton instance of the NotificationManager.
     * @return The NotificationManager instance.
     */
    public static synchronized NotificationManager getInstance() {
        if (instance == null) {
            instance = new NotificationManager();
        }
        return instance;
    }

    /**
     * Sets the VBox container where notifications will be displayed.
     * This method MUST be called once during the initialization of the DashboardViewModel.
     * @param container The VBox from DashboardView.fxml that will hold notifications.
     */
    public void setNotificationContainer(VBox container) {
        this.notificationStack = container;
        // Ensure the container is visible and managed
        if (this.notificationStack != null) {
            this.notificationStack.setVisible(true);
            this.notificationStack.setManaged(true);
            ApplicationLauncher.logger.info("NotificationManager: Container set. Visible: " + container.isVisible() + ", Managed: " + container.isManaged());
        } else {
        	ApplicationLauncher.logger.error("NotificationManager: Attempted to set null container.");
        }
    }

    /**
     * Shows a notification message.
     * This method can be called from any thread, as it uses Platform.runLater().
     * @param type The type of notification (SUCCESS, WARNING, ERROR, INFO).
     * @param title The title of the notification.
     * @param message The main message content of the notification.
     */
    public void showNotification(NotificationViewModel.NotificationType type, String title, String message) {
        if (notificationStack == null) {
            ApplicationLauncher.logger.error("NotificationManager: Notification container not set. Cannot display notification: " + title);
            return;
        }

        Platform.runLater(() -> {
            try {
                ApplicationLauncher.logger.info("NotificationManager: Attempting to load NotificationView.fxml for: " + title);
                URL fxmlUrl = getClass().getResource("/com/switchtester/app/view/NotificationView.fxml");
                if (fxmlUrl == null) {
                	ApplicationLauncher.logger.error("NotificationManager: ERROR: NotificationView.fxml not found at path: /com/switchtester/app/view/NotificationView.fxml");
                    return;
                }
                FXMLLoader loader = new FXMLLoader(fxmlUrl);
                Parent notificationNode = loader.load();
                NotificationViewModel controller = loader.getController();

                controller.setNotification(type, title, message);
                controller.setOnCloseCallback(() -> {
                    // Callback when a notification closes (e.g., auto-hide or 'X' button)
                    ApplicationLauncher.logger.info("NotificationManager: Notification closed: " + title);
                });

                // Add to the top of the VBox so newer notifications appear above older ones
                notificationStack.getChildren().add(0, notificationNode);
                ApplicationLauncher.logger.info("NotificationManager: Added notification '" + title + "'. Current children count: " + notificationStack.getChildren().size());
                ApplicationLauncher.logger.info("NotificationManager: Notification container dimensions - Width: " + notificationStack.getWidth() + ", Height: " + notificationStack.getHeight());

            } catch (IOException e) {
                ApplicationLauncher.logger.error("NotificationManager: Error loading NotificationView.fxml: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) { // Catch any other unexpected exceptions during notification creation
                ApplicationLauncher.logger.error("NotificationManager: An unexpected error occurred while showing notification: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
