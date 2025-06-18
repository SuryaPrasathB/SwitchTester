package com.switchtester.app.viewmodel;

import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox; // Import VBox
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

import com.switchtester.app.ApplicationLauncher;

/**
 * ViewModel for a single Notification message.
 * Manages the display, styling, and auto-hide behavior of a notification.
 */
public class NotificationViewModel implements Initializable {

    // Enum to define different types of notifications
    public enum NotificationType {
        SUCCESS("#d4edda", "#28a745", "/com/switchtester/app/images/success_icon.png"), // Light green, Green
        WARNING("#fff3cd", "#ffc107", "/com/switchtester/app/images/warning_icon.png"), // Light yellow, Yellow
        ERROR("#f8d7da", "#dc3545", "/com/switchtester/app/images/error_icon.png"),   // Light red, Red
        INFO("#d1ecf1", "#17a2b8", "/com/switchtester/app/images/info_icon.png");     // Light blue, Blue

        private final String backgroundColor;
        private final String borderColor;
        private final String iconPath;

        NotificationType(String backgroundColor, String borderColor, String iconPath) {
            this.backgroundColor = backgroundColor;
            this.borderColor = borderColor;
            this.iconPath = iconPath;
        }

        public String getBackgroundColor() {
            return backgroundColor;
        }

        public String getBorderColor() {
            return borderColor;
        }

        public String getIconPath() {
            return iconPath;
        }
    }

    @FXML private HBox notificationRoot;
    @FXML private ImageView iconImageView;
    @FXML private Label titleLabel;
    @FXML private Label messageLabel;
    @FXML private Button closeButton;

    private Runnable onCloseCallback; // Callback to notify parent when notification is closed

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initial setup, but actual content is set via setNotification method
    }

    /**
     * Sets the content and style of the notification.
     * @param type The type of notification (SUCCESS, WARNING, ERROR, INFO).
     * @param title The title of the notification.
     * @param message The main message content of the notification.
     */
    public void setNotification(NotificationType type, String title, String message) {
        titleLabel.setText(title);
        messageLabel.setText(message);

        // Apply styling based on notification type
        notificationRoot.setStyle(String.format("-fx-background-color: %s; -fx-background-radius: 8; -fx-border-color: %s; -fx-border-width: 1; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);",
                type.getBackgroundColor(), type.getBorderColor()));

        // Set icon based on notification type
        try {
            URL iconUrl = getClass().getResource(type.getIconPath());
            if (iconUrl != null) {
                iconImageView.setImage(new Image(iconUrl.toExternalForm()));
            } else {
                ApplicationLauncher.logger.error("Notification icon not found: " + type.getIconPath());
                // Fallback to a default or hide the icon if not found
            }
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Error loading notification icon: " + e.getMessage());
            // Fallback or hide
        }

        // Start auto-hide timer
        startAutoHideTimer();
    }

    /**
     * Sets a callback to be executed when the notification is closed.
     * @param callback The Runnable to execute.
     */
    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    /**
     * Handles the action when the close button is clicked.
     * Removes the notification.
     */
    @FXML
    private void handleCloseButton() {
        closeNotification();
    }

    /**
     * Starts a timer to automatically hide the notification after a delay.
     */
    private void startAutoHideTimer() {
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(5), event -> {
            // Start fade out animation after the delay
            FadeTransition fadeOut = new FadeTransition(Duration.seconds(0.5), notificationRoot);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> closeNotification()); // Close after fade out
            fadeOut.play();
        }));
        timeline.setCycleCount(1);
        timeline.play();
    }

    /**
     * Closes the notification and triggers the onCloseCallback.
     */
    private void closeNotification() {
        // Remove the notification from its parent
        if (notificationRoot.getParent() != null) {
            // Corrected cast: The parent of notificationRoot (an HBox) is notificationStack (a VBox)
            ((VBox) notificationRoot.getParent()).getChildren().remove(notificationRoot);
        }
        if (onCloseCallback != null) {
            onCloseCallback.run(); // Notify parent (NotificationManager)
        }
    }
}
