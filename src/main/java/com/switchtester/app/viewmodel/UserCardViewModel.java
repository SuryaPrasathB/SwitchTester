package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.User;
import com.switchtester.app.service.UserManager;
import com.switchtester.app.service.NotificationManager; // Import NotificationManager
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType; // Import NotificationType enum
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert; // Keep Alert for confirmation dialog
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * ViewModel for a single User Card displayed in the User Management screen.
 * Handles displaying user details and actions like edit, hide/show, and remove.
 */
public class UserCardViewModel implements Initializable {

    @FXML private Label usernameLabel;
    @FXML private Label profileTypeLabel;
    @FXML private Label statusLabel; // To show if user is hidden
    @FXML private MenuButton optionsMenuButton;
    @FXML private MenuItem hideShowMenuItem;

    private User user; // The User object associated with this card
    private UsersViewModel usersViewModel; // Reference to the parent UsersViewModel
    private Stage ownerStage; // To set as owner for alert dialogs (for confirmation)

    /**
     * Sets the User object for this card and updates the UI.
     * @param user The User object to display.
     */
    public void setUser(User user) {
        this.user = user;
        updateCardUI();
    }

    /**
     * Sets the reference to the parent UsersViewModel.
     * This allows communication back to refresh the user list.
     * @param usersViewModel The UsersViewModel instance.
     */
    public void setUsersViewModel(UsersViewModel usersViewModel) {
        this.usersViewModel = usersViewModel;
    }

    /**
     * Sets the owner stage for alert dialogs launched from this card.
     * @param stage The Stage that owns this view.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialization logic if needed, but UI update is done via setUser()
    }

    /**
     * Updates the UI elements of the card based on the current User object.
     */
    private void updateCardUI() {
        if (user != null) {
            usernameLabel.setText(user.getUsername());
            profileTypeLabel.setText(user.getProfileType());
            if (user.isHidden()) {
                statusLabel.setText("Hidden");
                statusLabel.setStyle("-fx-text-fill: #e74c3c;"); // Red for hidden
                hideShowMenuItem.setText("Show");
            } else {
                statusLabel.setText(""); // No status if not hidden
                hideShowMenuItem.setText("Hide");
            }
        }
    }

    /**
     * Handles the "Edit" menu item action.
     * Opens a dialog to edit the current user's details.
     */
    @FXML
    private void handleEditUser() {
    	ApplicationLauncher.logger.info("Edit user: " + user.getUsername());
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/UserEditDialog.fxml"));
            Parent userEditDialog = loader.load();

            UserEditDialogViewModel controller = loader.getController();
            controller.setUser(this.user); // Pass the user to be edited
            controller.setUsersViewModel(this.usersViewModel); // Pass reference to refresh list after edit
            controller.setOwnerStage(this.ownerStage); // Pass owner stage for alerts

            Stage dialogStage = new Stage();
            dialogStage.setTitle("Edit User: " + user.getUsername());
            dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            dialogStage.initOwner(ApplicationLauncher.getPrimaryStage()); // Set owner for proper modality
            dialogStage.setScene(new javafx.scene.Scene(userEditDialog));
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setResizable(false);
            dialogStage.setAlwaysOnTop(true); // Attempt to keep dialog always on top

            dialogStage.showAndWait(); // Show dialog and wait for it to close

        } catch (IOException e) {
            ApplicationLauncher.logger.info("Error loading UserEditDialog.fxml for editing user: " + e.getMessage());
            e.printStackTrace();
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                               "Error",
                                                               "Could not open edit dialog. An error occurred while trying to open the user edit dialog.");
        }
    }

    /**
     * Handles the "Hide" or "Show" menu item action.
     * Toggles the hidden status of the user.
     */
    @FXML
    private void handleHideShowUser() {
        if (user != null) {
            user.setHidden(!user.isHidden()); // Toggle hidden status
            UserManager.updateUser(user); // Save the updated user
            updateCardUI(); // Update UI to reflect change
            if (usersViewModel != null) {
                usersViewModel.onUserHidden(user); // Notify parent to refresh/update
            }
            NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                               "User Status Updated",
                                                               "User '" + user.getUsername() + "' is now " + (user.isHidden() ? "hidden." : "visible."));
        }
    }

    /**
     * Handles the "Remove" menu item action.
     * Deletes the user after confirmation.
     */
    @FXML
    private void handleRemoveUser() {
        if (user != null) {
            // For confirmation, we still need a modal dialog (Alert).
            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Deletion");
            confirmationAlert.setHeaderText("Delete User: " + user.getUsername() + "?");
            confirmationAlert.setContentText("Are you sure you want to permanently remove user '" + user.getUsername() + "'?");
            if (ApplicationLauncher.getPrimaryStage() != null) {
                confirmationAlert.initOwner(ApplicationLauncher.getPrimaryStage()); // Set the owner stage for the alert
            }
            Optional<ButtonType> result = confirmationAlert.showAndWait();

            if (result.isPresent() && result.get() == ButtonType.OK) {
                UserManager.deleteUser(user.getUsername());
                if (usersViewModel != null) {
                    usersViewModel.onUserRemoved(user); // Notify parent to remove card
                }
                NotificationManager.getInstance().showNotification(NotificationType.SUCCESS,
                                                                   "User Removed",
                                                                   "User '" + user.getUsername() + "' has been successfully removed.");
            }
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                                               "No User Selected",
                                                               "Please select a user to remove.");
        }
    }

    // Removed the old showAlert helper method as it's replaced by NotificationManager
}
