package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.User;
import com.switchtester.app.service.UserManager;
import com.switchtester.app.util.PasswordHasher;
import javafx.application.Platform; // Import Platform
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.text.Font;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ViewModel for the User Edit/Add Dialog.
 * Handles user creation, editing, and permission management.
 */
public class UserEditDialogViewModel implements Initializable {

    @FXML private Label dialogTitleLabel;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private ComboBox<String> profileTypeComboBox;
    @FXML private CheckBox hiddenCheckBox;
    @FXML private VBox permissionsContainer; // Container for permission checkboxes
    @FXML private Label messageLabel;
    @FXML private Button saveButton; // fx:id added to the Save button in FXML

    private User userToEdit;
    private UsersViewModel usersViewModel;
    private Stage ownerStage; // Owner stage for alerts

    // List of all possible dashboard panel permissions (fx:id of HBoxes in DashboardView.fxml)
    private static final List<String> ALL_PERMISSIONS = Arrays.asList(
        "dashboardNav", "projectsNav", "executionNav", "reportsNav", "debugNav", "settingsNav", "usersNav"
    );

    // ObservableList for profile types
    private ObservableList<String> profileTypes = FXCollections.observableArrayList(
            "Admin", "Production", "Quality", "Maintenance"
    );

    /**
     * Sets the user to be edited. If null, it's an "Add New User" operation.
     * @param user The User object to display.
     */
    public void setUser(User user) {
        this.userToEdit = user;
        if (userToEdit != null) {
            dialogTitleLabel.setText("Edit User: " + userToEdit.getUsername());
            usernameField.setText(userToEdit.getUsername());
            usernameField.setEditable(false); // Username cannot be changed after creation
            profileTypeComboBox.getSelectionModel().select(userToEdit.getProfileType());
            hiddenCheckBox.setSelected(userToEdit.isHidden());
            populatePermissions(userToEdit.getPermissions());
            // Clear password fields when editing, user must re-enter if they want to change
            passwordField.setText("");
            confirmPasswordField.setText("");
        } else {
            dialogTitleLabel.setText("Add New User");
            populatePermissions(new HashSet<>()); // No permissions selected by default for new user
        }
    }

    /**
     * Sets the reference to the parent UsersViewModel for refreshing the list.
     * @param usersViewModel The UsersViewModel instance.
     */
    public void setUsersViewModel(UsersViewModel usersViewModel) {
        this.usersViewModel = usersViewModel;
    }

    /**
     * Sets the owner stage for alert dialogs launched from this dialog.
     * This method MUST be called by the parent ViewModel.
     * @param stage The Stage that owns this dialog.
     */
    public void setOwnerStage(Stage stage) {
        this.ownerStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        profileTypeComboBox.setItems(profileTypes);
        profileTypeComboBox.getSelectionModel().selectFirst();
        messageLabel.setText("");
        messageLabel.setVisible(false);
        messageLabel.setManaged(false);

        // Populate permissions checkboxes initially (for "Add New User" case)
        populatePermissions(new HashSet<>());

        // Handle Enter key press on password fields to trigger save
        passwordField.setOnAction(event -> handleSaveUser());
        confirmPasswordField.setOnAction(event -> handleSaveUser());

        // Defer getting the stage until the scene is fully set
        Platform.runLater(() -> {
            // This ensures saveButton.getScene() and .getWindow() are not null
            // when handleSaveUser or handleCancel are called via button clicks.
            // No direct field for dialogStage is needed anymore.
        });
    }

    /**
     * Dynamically creates and populates CheckBoxes for each permission.
     * @param currentPermissions The set of permissions the user currently has.
     */
    private void populatePermissions(Set<String> currentPermissions) {
        permissionsContainer.getChildren().clear();
        for (String permission : ALL_PERMISSIONS) {
            CheckBox checkBox = new CheckBox(formatPermissionName(permission));
            checkBox.setUserData(permission);
            checkBox.setSelected(currentPermissions.contains(permission));
            checkBox.setFont(Font.font("Inter Regular", 12));
            permissionsContainer.getChildren().add(checkBox);
        }
    }

    /**
     * Formats the permission string (e.g., "dashboardNav" to "Dashboard").
     */
    private String formatPermissionName(String permission) {
        String formatted = permission.replace("Nav", "");
        if (!formatted.isEmpty()) {
            return formatted.substring(0, 1).toUpperCase() + formatted.substring(1);
        }
        return permission;
    }

    /**
     * Helper method to get the current Stage of this dialog.
     * This method is now robust as it's called after UI components are initialized.
     * @return The Stage object for this dialog.
     */
    private Stage getDialogStage() {
        // Any node in the FXML can be used to get the scene, then the window (which is the Stage)
        return (Stage) saveButton.getScene().getWindow();
    }

    /**
     * Handles the Save button action.
     * Validates input, hashes password, and saves/updates the user.
     */
    @FXML
    private void handleSaveUser() {
        if (!isInputValid()) {
            return;
        }

        String username = usernameField.getText().trim();
        String plainTextPassword = passwordField.getText();
        String profileType = profileTypeComboBox.getSelectionModel().getSelectedItem();
        boolean hidden = hiddenCheckBox.isSelected();

        Set<String> selectedPermissions = permissionsContainer.getChildren().stream()
                .filter(node -> node instanceof CheckBox)
                .map(node -> (CheckBox) node)
                .filter(CheckBox::isSelected)
                .map(checkBox -> (String) checkBox.getUserData())
                .collect(Collectors.toSet());

        boolean success = false;
        User savedUser = null;

        if (userToEdit == null) { // Adding a new user
            if (UserManager.getUserByUsername(username).isPresent()) {
                showMessage("User with this username already exists.", true);
                return;
            }
            String hashedPassword = PasswordHasher.hashPassword(plainTextPassword);
            User newUser = new User(username, hashedPassword, profileType, selectedPermissions, hidden);
            success = UserManager.addUser(newUser);
            savedUser = newUser;
            if (success) {
                showMessage("User '" + username + "' added successfully!", false);
            } else {
                showMessage("Failed to add user '" + username + "'.", true);
            }
        } else { // Editing an existing user
            userToEdit.setProfileType(profileType);
            userToEdit.setHidden(hidden);
            userToEdit.setPermissions(selectedPermissions);

            if (!plainTextPassword.isEmpty()) {
                String hashedPassword = PasswordHasher.hashPassword(plainTextPassword);
                userToEdit.setPasswordHash(hashedPassword);
            }
            success = UserManager.updateUser(userToEdit);
            savedUser = userToEdit;
            if (success) {
                showMessage("User '" + username + "' updated successfully!", false);
            } else {
                showMessage("Failed to update user '" + username + "'.", true);
            }
        }

        if (success && usersViewModel != null) {
            usersViewModel.onUserEdited(savedUser);
            // Close the dialog after successful save/update and parent notification
            getDialogStage().close(); // This will now reliably get the stage
        }
    }

    /**
     * Handles the Cancel button action. Closes the dialog.
     */
    @FXML
    private void handleCancel() {
        getDialogStage().close(); // This will now reliably get the stage
    }

    /**
     * Validates the input fields.
     * @return true if input is valid, false otherwise.
     */
    private boolean isInputValid() {
        String errorMessage = "";

        if (usernameField.getText() == null || usernameField.getText().trim().isEmpty()) {
            errorMessage += "Username cannot be empty!\n";
        }

        if (userToEdit == null || !passwordField.getText().isEmpty() || !confirmPasswordField.getText().isEmpty()) {
            if (passwordField.getText() == null || passwordField.getText().isEmpty()) {
                errorMessage += "Password cannot be empty!\n";
            }
            if (!passwordField.getText().equals(confirmPasswordField.getText())) {
                errorMessage += "Password and Confirm Password do not match!\n";
            }
        }

        if (profileTypeComboBox.getSelectionModel().getSelectedItem() == null) {
            errorMessage += "Profile Type must be selected!\n";
        }

        if (errorMessage.isEmpty()) {
            return true;
        } else {
            showMessage(errorMessage, true);
            return false;
        }
    }

    /**
     * Displays a message to the user.
     * @param message The message to display.
     * @param isError True if the message is an error, false for success.
     */
    private void showMessage(String message, boolean isError) {
        messageLabel.setText(message);
        if (isError) {
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
        } else {
            messageLabel.setStyle("-fx-text-fill: #27ae60;");
        }
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
    }

    /**
     * Helper method to show an alert dialog.
     * Sets the owner stage for the alert to ensure it appears on top of the main application.
     */
    private Optional<ButtonType> showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        if (ownerStage != null) {
            alert.initOwner(ApplicationLauncher.getPrimaryStage());
        }
        return alert.showAndWait();
    }
}
