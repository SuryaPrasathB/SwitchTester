package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.User;
import com.switchtester.app.service.UserManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle; // Import StageStyle
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * ViewModel for the Users Screen.
 * Responsible for loading and displaying user profiles as cards,
 * and handling actions like adding new users.
 */
public class UsersViewModel implements Initializable {

    @FXML
    private FlowPane userCardsContainer;

    private Stage ownerStage; // To pass to sub-views for alert ownership

    /**
     * Sets the owner stage for this UsersViewModel.
     * This is typically called by the DashboardViewModel when loading the UsersView.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Users View Initialized.");
        loadUserCards(); // Load and display user cards on initialization
    }

    /**
     * Loads all users from the UserManager and displays them as individual cards
     * in the userCardsContainer FlowPane.
     */
    public void loadUserCards() {
        userCardsContainer.getChildren().clear(); // Clear existing cards
        List<User> users = UserManager.loadUsers(); // Load all users

        for (User user : users) {
            // Only show users that are not hidden, or show all if specifically desired (e.g., for Admin)
            // For now, let's display all users in the user management view,
            // but you could add a filter here based on user.isHidden() if needed.
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/UserCardView.fxml"));
                Parent userCardNode = loader.load();

                UserCardViewModel userCardController = loader.getController();
                userCardController.setUser(user); // Pass the User object to the card controller
                userCardController.setUsersViewModel(this); // Pass a reference to this ViewModel
                userCardController.setOwnerStage(this.ownerStage); // Pass the owner stage for alerts

                userCardsContainer.getChildren().add(userCardNode);
            } catch (IOException e) {
                ApplicationLauncher.logger.error("Error loading UserCardView.fxml for user " + user.getUsername() + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Handles the action for adding a new user.
     * This will typically open a dialog for user creation.
     */
    @FXML
    private void handleAddUser() {
        ApplicationLauncher.logger.info("Add New User button clicked!");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/UserEditDialog.fxml"));
            Parent userEditDialog = loader.load();

            UserEditDialogViewModel controller = loader.getController();

            // Create a new stage for the dialog
            Stage dialogStage = new Stage();
            dialogStage.setTitle("Add New User");
            // Use UNDECORATED style to remove native window borders/buttons
            dialogStage.initStyle(StageStyle.UNDECORATED);
            dialogStage.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialogStage.initOwner(ownerStage); // Set owner for proper modality and alert display
            dialogStage.setScene(new javafx.scene.Scene(userEditDialog));
            dialogStage.setResizable(false);
            dialogStage.setAlwaysOnTop(true); // Attempt to keep dialog always on top

            // Set the controller's references
            controller.setUsersViewModel(this);
            controller.setOwnerStage(this.ownerStage);

            // --- Manual Modality Control ---
            // Disable the owner stage's root node to prevent interaction with the main app
            // while the dialog is open. This simulates modality without showAndWait().
            if (ownerStage != null && ownerStage.getScene() != null && ownerStage.getScene().getRoot() != null) {
                ownerStage.getScene().getRoot().setDisable(true);
            }

            // Add a listener to re-enable the owner stage's root when the dialog closes
            dialogStage.showingProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    if (oldValue == true && newValue == false) { // Dialog is closing
                        if (ownerStage != null && ownerStage.getScene() != null && ownerStage.getScene().getRoot() != null) {
                            ownerStage.getScene().getRoot().setDisable(false);
                        }
                    }
                }
            });

            // Position the dialog manually in the center of the owner stage
            if (ownerStage != null) {
                dialogStage.setX(ownerStage.getX() + (ownerStage.getWidth() / 2) - (dialogStage.getWidth() / 2));
                dialogStage.setY(ownerStage.getY() + (ownerStage.getHeight() / 2) - (dialogStage.getHeight() / 2));
            }

            dialogStage.show(); // Show the dialog non-modally, allowing the main thread to continue

        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading UserEditDialog.fxml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Methods to be called by UserCardViewModel after edit/remove/hide actions
    public void onUserEdited(User user) {
        ApplicationLauncher.logger.info("User edited/added: " + user.getUsername());
        loadUserCards(); // Refresh the list to reflect changes
    }

    public void onUserRemoved(User user) {
        ApplicationLauncher.logger.info("User removed: " + user.getUsername());
        loadUserCards(); // Refresh the list
    }

    public void onUserHidden(User user) {
        ApplicationLauncher.logger.info("User hidden/shown: " + user.getUsername());
        loadUserCards(); // Refresh the list (hidden users might still be loaded but filtered visually later)
    }
}
