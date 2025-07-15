package com.switchtester.app.viewmodel;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.service.ModbusService;
import com.switchtester.app.service.NotificationManager;
import com.switchtester.app.viewmodel.NotificationViewModel.NotificationType;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for the main Dashboard screen.
 * Manages navigation between different application views and applies Role-Based Access Control (RBAC).
 * Also implements the sliding expandable sidebar menu and Modbus TCP connection status display.
 */
public class DashboardViewModel implements Initializable {

    @FXML private Label currentScreenTitle;
    @FXML private Label dateTimeLabel;
    @FXML private StackPane contentArea; // Area where different screens are loaded
    @FXML private VBox notificationStack; // fx:id for the floating notification container

    // Sidebar elements for animation control
    @FXML private VBox sidePanel; // The main VBox for the left sidebar
    @FXML private Label appTitleLabel; // "Switch Tester" title in the sidebar

    // Navigation HBoxes (side panel items)
    @FXML private HBox dashboardNav;
    @FXML private HBox projectsNav;
    @FXML private HBox executionNav;
    @FXML private HBox reportsNav;
    @FXML private HBox debugNav;
    @FXML private HBox settingsNav;
    @FXML private HBox usersNav;

    // Labels within navigation HBoxes for opacity animation
    @FXML private Label dashboardLabel;
    @FXML private Label projectsLabel;
    @FXML private Label executionLabel;
    @FXML private Label reportsLabel;
    @FXML private Label debugLabel;
    @FXML private Label settingsLabel;
    @FXML private Label usersLabel;
    @FXML private Label logoutLabel; // Label for the logout button

    // Modbus TCP Status UI elements
    @FXML private Circle tcpStatusCircle;
    @FXML private Label tcpStatusLabel;
    @FXML private Button retryModbusButton;

    private Stage dashboardStage;
    private ScheduledExecutorService statusUpdateScheduler; // For periodic status updates

    // Flag to prevent multiple concurrent Modbus connection attempts
    private static volatile boolean isConnectionAttemptInProgress = false;

    // Map to hold references to navigation HBoxes for easier RBAC application
    private Map<String, HBox> navItems = new HashMap<>();
    // Map to hold references to navigation Labels for opacity control
    private Map<String, Label> navLabels = new HashMap<>();

    /**
     * Sets the primary stage for the dashboard.
     * @param stage The Stage object for the dashboard.
     */
    public void setDashboardStage(Stage stage) {
        this.dashboardStage = stage;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ApplicationLauncher.logger.info("Dashboard View Initializing...");

        // Initialize the NotificationManager with the notificationStack
        NotificationManager.getInstance().setNotificationContainer(notificationStack);

        // --- TEMPORARY TEST NOTIFICATION ---
        NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                           "Welcome!",
                                                           "Dashboard loaded successfully. Testing notifications.");
        ApplicationLauncher.logger.info("Dashboard loaded successfully. Testing notifications.");
        // --- END TEMPORARY TEST NOTIFICATION ---

        // Populate the navItems map for RBAC
        navItems.put("dashboardNav", dashboardNav);
        navItems.put("projectsNav", projectsNav);
        navItems.put("executionNav", executionNav);
        navItems.put("reportsNav", reportsNav);
        navItems.put("debugNav", debugNav);
        navItems.put("settingsNav", settingsNav);
        navItems.put("usersNav", usersNav);

        // Populate the navLabels map for opacity control
        navLabels.put("appTitleLabel", appTitleLabel);
        navLabels.put("dashboardLabel", dashboardLabel);
        navLabels.put("projectsLabel", projectsLabel);
        navLabels.put("executionLabel", executionLabel);
        navLabels.put("reportsLabel", reportsLabel);
        navLabels.put("debugLabel", debugLabel);
        navLabels.put("settingsLabel", settingsLabel);
        navLabels.put("usersLabel", usersLabel);
        navLabels.put("logoutLabel", logoutLabel);

        // Apply RBAC first to ensure correct initial managed/visible state
        applyRBAC();

        // Set up sidebar hover animations
        setupSidebarHoverAnimation();

        // --- Set sidebar to closed by default AFTER initial layout pass ---
        Platform.runLater(() -> {
            sidePanel.setPrefWidth(sidePanel.getMinWidth());
            for (Label label : navLabels.values()) {
                label.setOpacity(0.0);
                label.setManaged(false); // Ensure labels don't take up space when hidden
            }
            ApplicationLauncher.logger.debug("Sidebar initialized to closed state after layout.");
        });
        // --- End sidebar default state setup ---


        // Update date and time
        Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateDateTime()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();

        // Load the default screen (DashboardContent)
        showScreen("/com/switchtester/app/view/DashboardContent.fxml", "Dashboard");

        // Initialize and start periodic Modbus connection status update
        // The initial update will also trigger a connection attempt if disconnected
        updateModbusConnectionStatusUI();
        startModbusStatusScheduler(); // Start periodic updates
        ApplicationLauncher.logger.info("Dashboard View Initialized successfully.");
    }

    /**
     * Sets up the hover animation for the sidebar.
     * When mouse enters, sidebar expands and labels fade in.
     * When mouse exits, sidebar collapses and labels fade out.
     */
    private void setupSidebarHoverAnimation() {
        sidePanel.setOnMouseEntered(event -> {
            ApplicationLauncher.logger.debug("Mouse Entered Sidebar - Expanding...");
            // Expand sidebar width
            Timeline expandTimeline = new Timeline();
            expandTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(0.2),
                            new KeyValue(sidePanel.prefWidthProperty(), sidePanel.getMaxWidth()))
            );

            // Fade in labels
            for (Label label : navLabels.values()) {
                //ApplicationLauncher.logger.debug("  Processing label: {} (Initial managed: {}, opacity: {})", label.getText(), label.isManaged(), label.getOpacity());

                // Check if the parent HBox (nav item) is visible. If not, this label should not animate.
                boolean parentHBoxVisible = true;
                if (label.getParent() instanceof HBox) {
                    HBox parentHBox = (HBox) label.getParent();
                    parentHBoxVisible = parentHBox.isVisible();
                    //ApplicationLauncher.logger.debug("    Parent HBox for {}: visible={}, managed={}", label.getText(), parentHBox.isVisible(), parentHBox.isManaged());
                } else {
                    ApplicationLauncher.logger.debug("    Label {} parent is not an HBox or is null.", label.getText());
                }


                if (parentHBoxVisible) { // Only animate if the parent navigation item is visible (not hidden by RBAC)
                    label.setManaged(true); // Make it managed so it takes up space during expansion
                    Timeline fadeInTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(0.2), new KeyValue(label.opacityProperty(), 1.0))
                    );
                    fadeInTimeline.play();
                    //ApplicationLauncher.logger.debug("    Fading IN label: {} (New managed: {}, opacity: {})", label.getText(), label.isManaged(), label.getOpacity());
                } else {
                    ApplicationLauncher.logger.debug("    Skipping label {} - Parent HBox is not visible (RBAC).", label.getText());
                }
            }
            expandTimeline.play();
        });

        sidePanel.setOnMouseExited(event -> {
            ApplicationLauncher.logger.debug("Mouse Exited Sidebar - Collapsing...");
            // Collapse sidebar width
            Timeline collapseTimeline = new Timeline();
            collapseTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.seconds(0.2),
                            new KeyValue(sidePanel.prefWidthProperty(), sidePanel.getMinWidth()))
            );

            // Fade out labels
            for (Label label : navLabels.values()) {
                //ApplicationLauncher.logger.debug("  Processing label for collapse: {} (Initial managed: {}, opacity: {})", label.getText(), label.isManaged(), label.getOpacity());
                // Only fade out labels that are currently managed and visible (i.e., not already hidden by RBAC)
                if (label.isManaged() && label.getOpacity() > 0) { // Only animate if it's currently visible
                    Timeline fadeOutTimeline = new Timeline(
                            new KeyFrame(Duration.seconds(0.2), new KeyValue(label.opacityProperty(), 0.0))
                    );
                    fadeOutTimeline.setOnFinished(e -> {
                        // After fading out, set managed to false to remove from layout calculation
                        label.setManaged(false);
                        //ApplicationLauncher.logger.debug("    Faded OUT label: {} (New managed: {}, opacity: {})", label.getText(), label.isManaged(), label.getOpacity());
                    });
                    fadeOutTimeline.play();
                } else {
                    ApplicationLauncher.logger.debug("    Skipping collapse animation for label {} - not managed or already transparent.", label.getText());
                }
            }
            collapseTimeline.play();
        });
    }


    /**
     * Updates the date and time label.
     */
    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        dateTimeLabel.setText(now.format(formatter));
    }

    /**
     * Applies Role-Based Access Control to the navigation sidebar.
     * Hides navigation items that the logged-in user does not have permission for.
     * Also sets initial managed/opacity state for labels.
     */
    private void applyRBAC() {
        Set<String> userPermissions = ApplicationLauncher.getLoggedInUserPermissions();

        if (userPermissions == null) {
            ApplicationLauncher.logger.error("User permissions not set. Hiding all navigation items.");
            navItems.values().forEach(item -> {
                item.setVisible(false);
                item.setManaged(false);
            });
            return;
        }

        // Apply RBAC to HBoxes and ensure their corresponding labels are set up for animation
        for (Map.Entry<String, HBox> entry : navItems.entrySet()) {
            String permissionKey = entry.getKey();
            HBox navItem = entry.getValue();
            Label navLabel = navLabels.get(permissionKey + "Label"); // Get corresponding label

            boolean hasPermission = userPermissions.contains(permissionKey);

            navItem.setVisible(hasPermission);
            navItem.setManaged(hasPermission); // Also manage visibility to save space

            // If the HBox itself is not visible (due to RBAC), ensure its label is also not managed/visible
            if (!hasPermission && navLabel != null) {
                navLabel.setManaged(false);
                navLabel.setOpacity(0.0);
                ApplicationLauncher.logger.debug("  RBAC: Hiding label {} because parent nav item {} is not visible.", navLabel.getText(), permissionKey);
            }
        }
        ApplicationLauncher.logger.info("RBAC applied for user permissions: {}", userPermissions);
    }


    /**
     * Resets the background style of all navigation HBoxes to transparent.
     */
    private void resetNavigationStyles() {
        navItems.values().forEach(navItem ->
            navItem.setStyle("-fx-background-color: transparent; -fx-background-radius: 5;")
        );
        ApplicationLauncher.logger.debug("Navigation styles reset.");
    }

    /**
     * Loads the specified FXML screen into the content area and updates the title.
     * @param fxmlPath The path to the FXML file.
     * @param title The title to set for the current screen.
     */
    private void showScreen(String fxmlPath, String title) {
        try {
            // Cleanup previous DebugViewModel if navigating away from it
            if (contentArea.getChildren().size() > 0) {
                Parent currentContent = (Parent) contentArea.getChildren().get(0);
                if (currentContent.getUserData() instanceof DebugViewModel) {
                     ((DebugViewModel) currentContent.getUserData()).cleanup();
                     ApplicationLauncher.logger.info("DashboardViewModel: Cleaned up previous DebugViewModel.");
                }
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent screen = loader.load();

            // If the screen has a ViewModel that needs the ownerStage, set it here
            Object controller = loader.getController();
            if (controller instanceof SettingsViewModel) {
                ((SettingsViewModel) controller).setOwnerStage(dashboardStage);
                // Pass a callback to SettingsViewModel to allow it to update the main screen title
                ((SettingsViewModel) controller).setMainScreenTitleUpdater(this::updateMainScreenTitle);
            } else if (controller instanceof UsersViewModel) {
                ((UsersViewModel) controller).setOwnerStage(dashboardStage);
            } else if (controller instanceof TestTypeConfigViewModel) {
                ((TestTypeConfigViewModel) controller).setOwnerStage(dashboardStage);
            } else if (controller instanceof DebugViewModel) {
                // Set the current DebugViewModel instance as user data for cleanup later
                screen.setUserData(controller);
            }


            contentArea.getChildren().setAll(screen); // Replace current content
            currentScreenTitle.setText(title); // Update title
            updateActiveNavItem(title); // Update active navigation item style
            ApplicationLauncher.logger.info("Screen loaded: {} (Title: {})", fxmlPath, title);
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading screen: {} - {}", fxmlPath, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                               "Screen Load Error",
                                                               "Failed to load " + title + " screen. Please check logs.");
        }
    }

    /**
     * Helper method to update the main screen title.
     * This method is passed as a callback to sub-view models.
     * @param newTitle The new title to set.
     */
    private void updateMainScreenTitle(String newTitle) {
        currentScreenTitle.setText(newTitle);
        ApplicationLauncher.logger.debug("Main screen title updated to: {}", newTitle);
    }


    /**
     * Updates the styling of the navigation items to highlight the active screen.
     * This method now uses explicit mapping for clarity and robustness.
     * @param activeTitle The title of the currently active screen.
     */
    private void updateActiveNavItem(String activeTitle) {
        resetNavigationStyles(); // First, reset all styles

        // Determine which navigation key corresponds to the active title
        String targetNavItemKey = null;

        if (activeTitle.equalsIgnoreCase("Dashboard")) {
            targetNavItemKey = "dashboardNav";
        } else if (activeTitle.equalsIgnoreCase("Projects")) {
            targetNavItemKey = "projectsNav";
        } else if (activeTitle.equalsIgnoreCase("Execution")) {
            targetNavItemKey = "executionNav";
        } else if (activeTitle.equalsIgnoreCase("Reports")) {
            targetNavItemKey = "reportsNav";
        } else if (activeTitle.equalsIgnoreCase("Debug Console")) {
            targetNavItemKey = "debugNav";
        } else if (activeTitle.equalsIgnoreCase("Users")) {
            targetNavItemKey = "usersNav";
        }
        // Special handling for Settings and its sub-views
        else if (activeTitle.equalsIgnoreCase("Application Settings") ||
                   activeTitle.equalsIgnoreCase("Admin Login for Settings") ||
                   activeTitle.equalsIgnoreCase("Test Type Configuration") ||
                   activeTitle.equalsIgnoreCase("Arduino Opta Configuration")) {
            targetNavItemKey = "settingsNav";
        }

        // Apply style if a target key was found
        if (targetNavItemKey != null) {
            HBox navItemToHighlight = navItems.get(targetNavItemKey);
            if (navItemToHighlight != null) {
                navItemToHighlight.setStyle("-fx-background-color: #3498db; -fx-background-radius: 5;");
                ApplicationLauncher.logger.debug("Active navigation item set to: {}", targetNavItemKey);
            }
        } else {
            ApplicationLauncher.logger.warn("No matching navigation item found for active title: {}", activeTitle);
        }
    }

    // --- Navigation Handlers ---
    @FXML
    private void showDashboardScreen(MouseEvent event) {
        showScreen("/com/switchtester/app/view/DashboardContent.fxml", "Dashboard");
    }

    @FXML
    private void showProjectsScreen(MouseEvent event) {
        showScreen("/com/switchtester/app/view/ProjectsView.fxml", "Projects");
    }

    @FXML
    private void showExecutionScreen(MouseEvent event) {
        showScreen("/com/switchtester/app/view/ExecutionView.fxml", "Execution");
    }

    @FXML
    private void showReportsScreen(MouseEvent event) {
        showScreen("/com/switchtester/app/view/ReportsView.fxml", "Reports");
    }

    @FXML
    private void showDebugScreen(MouseEvent event) {
        showScreen("/com/switchtester/app/view/DebugView.fxml", "Debug Console");
    }

    @FXML
    private void showUsersScreen(MouseEvent event) {
        Set<String> currentUserPermissions = ApplicationLauncher.getLoggedInUserPermissions();
        if (currentUserPermissions != null && currentUserPermissions.contains("usersNav")) {
            showScreen("/com/switchtester/app/view/UsersView.fxml", "Users");
            ApplicationLauncher.logger.info("User accessed Users screen.");
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.WARNING,
                                                               "Permission Denied",
                                                               "You do not have permission to access the Users screen.");
            ApplicationLauncher.logger.warn("Access denied to Users screen for current user.");
        }
    }

    @FXML
    private void showSettingsScreen(MouseEvent event) {
        String userProfile = ApplicationLauncher.getLoggedInUserProfile();
        boolean requirePassword = !("Admin".equals(userProfile)); // Admin does not require password

        if (requirePassword) {
            try {
                FXMLLoader passwordLoader = new FXMLLoader(getClass().getResource("/com/switchtester/app/view/PasswordPromptView.fxml"));
                Parent passwordPromptRoot = passwordLoader.load();

                PasswordPromptViewModel passwordController = passwordLoader.getController();
                passwordController.setExpectedPassword("admin123"); // Password for settings access
                passwordController.setOnPasswordCorrectCallback(() -> {
                    loadSettingsViewDirectly();
                    ApplicationLauncher.logger.info("Settings password correct. Loading settings view.");
                });
                passwordController.setOnPasswordCancelledCallback(() -> {
                    showDashboardScreen(null); // Revert to dashboard if cancelled
                    NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                                       "Access Cancelled",
                                                                       "Settings access cancelled.");
                    ApplicationLauncher.logger.info("Settings access cancelled by user.");
                });

                contentArea.getChildren().setAll(passwordPromptRoot);
                updateMainScreenTitle("Admin Login for Settings");
                updateActiveNavItem("Admin Login for Settings"); // Highlight settings nav
                ApplicationLauncher.logger.debug("Password prompt for settings displayed.");
            } catch (IOException e) {
                ApplicationLauncher.logger.error("Error loading password prompt for settings: {}", e.getMessage(), e);
                NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                                   "Error",
                                                                   "Could not open password prompt for settings.");
                showDashboardScreen(null); // Fallback
            }
        } else {
            ApplicationLauncher.logger.info("Admin user. Loading settings view directly.");
            loadSettingsViewDirectly();
        }
    }

    /**
     * Helper method to load the SettingsView directly into the content area.
     * This is called when the password is correct or for Admin users.
     */
    private void loadSettingsViewDirectly() {
        try {
            URL settingsViewUrl = getClass().getResource("/com/switchtester/app/view/SettingsView.fxml");
            if (settingsViewUrl == null) {
                ApplicationLauncher.logger.error("ERROR: SettingsView.fxml not found. Please check the file path.");
                NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                                   "File Not Found",
                                                                   "SettingsView.fxml could not be loaded.");
                showDashboardScreen(null); // Fallback
                return;
            }
            FXMLLoader settingsLoader = new FXMLLoader(settingsViewUrl);
            Parent settingsRoot = settingsLoader.load();

            contentArea.getChildren().setAll(settingsRoot);
            updateMainScreenTitle("Application Settings");
            updateActiveNavItem("Application Settings"); // Highlight settings nav

            SettingsViewModel settingsController = settingsLoader.getController();
            if (dashboardStage != null) {
                settingsController.setOwnerStage(dashboardStage);
            }
            settingsController.setMainScreenTitleUpdater(this::updateMainScreenTitle);
            settingsController.handleTestTypeConfig(); // Load the default Test Type Configuration within settings
            ApplicationLauncher.logger.info("Settings view loaded successfully.");
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading settings view: {}", e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationType.ERROR,
                                                               "Screen Load Error",
                                                               "Could not load settings view.");
            showDashboardScreen(null); // Fallback
        }
    }

    @FXML
    private void handleLogoutButton(MouseEvent event) {
        ApplicationLauncher.logger.info("Logout button clicked!");
        if (dashboardStage != null) {
            // Shut down Modbus status scheduler immediately
            if (statusUpdateScheduler != null && !statusUpdateScheduler.isShutdown()) {
                statusUpdateScheduler.shutdownNow();
                ApplicationLauncher.logger.info("DashboardViewModel: Modbus status scheduler shut down on logout.");
            }

            // Perform Modbus disconnection on a background thread
            new Thread(() -> {
                ModbusService.disconnect(); // This can block
                ApplicationLauncher.logger.info("Modbus service disconnected on logout (background thread).");

                // After disconnection, perform UI updates on the JavaFX Application Thread
                Platform.runLater(() -> {
                    dashboardStage.close();
                    ApplicationLauncher.logger.debug("Dashboard stage closed.");

                    ApplicationLauncher.showLoginScreen();
                    ApplicationLauncher.setLoggedInUserProfile(null); // Clear logged in user profile on logout
                    NotificationManager.getInstance().showNotification(NotificationType.INFO,
                                                                       "Logged Out",
                                                                       "You have been successfully logged out.");
                });
            }, "LogoutModbusDisconnectThread").start(); // Name the thread for debugging
        }
    }

    /**
     * Updates the UI elements for Modbus TCP connection status.
     * This method should be called periodically or after connection attempts.
     */
    private void updateModbusConnectionStatusUI() {
        Platform.runLater(() -> { // Ensure UI updates are on JavaFX Application Thread
            boolean isConnected = ModbusService.isConnected();
            if (isConnected) {
                tcpStatusCircle.setFill(Color.LIMEGREEN);
                tcpStatusLabel.setText("TCP Server Connected");
                retryModbusButton.setVisible(false);
                retryModbusButton.setManaged(false);
                isConnectionAttemptInProgress = false; // Reset flag if connected
                ApplicationLauncher.logger.debug("Modbus status UI updated: Connected.");
            } else {
                tcpStatusCircle.setFill(Color.RED);
                tcpStatusLabel.setText("TCP Server Disconnected");
                retryModbusButton.setVisible(true);
                retryModbusButton.setManaged(true);
                ApplicationLauncher.logger.debug("Modbus status UI updated: Disconnected.");

                // ONLY call connect if currently disconnected AND no attempt is already in progress
                if (!isConnectionAttemptInProgress) {
                    isConnectionAttemptInProgress = true; // Set flag before starting attempt
                    new Thread(() -> {
                        ModbusService.connect(); // This will block until connection or timeout
                        Platform.runLater(() -> {
                            // After connection attempt, update UI again
                            updateModbusConnectionStatusUI();
                            isConnectionAttemptInProgress = false; // Reset flag after attempt
                        });
                    }, "PeriodicModbusConnect").start();
                } else {
                    ApplicationLauncher.logger.debug("Modbus connection attempt already in progress. Skipping new attempt.");
                }
            }
        });
    }

    /**
     * Handles the action when the "Retry" button for Modbus connection is clicked.
     */
    @FXML
    private void handleRetryModbusConnection(MouseEvent event) {
        ApplicationLauncher.logger.info("Retry Modbus Connection button clicked.");
        if (!isConnectionAttemptInProgress) {
            isConnectionAttemptInProgress = true;
            // Attempt connection on a new thread to keep UI responsive
            new Thread(() -> {
                ModbusService.connect();
                // After connection attempt, update UI
                Platform.runLater(() -> {
                    updateModbusConnectionStatusUI();
                    isConnectionAttemptInProgress = false;
                });
            }, "ModbusRetryThread").start();
        } else {
            NotificationManager.getInstance().showNotification(NotificationType.INFO,
                    "Connection in Progress", "A Modbus connection attempt is already in progress.");
        }
    }

    /**
     * Starts a scheduled task to periodically update the Modbus connection status UI.
     */
    private void startModbusStatusScheduler() {
        if (statusUpdateScheduler != null && !statusUpdateScheduler.isShutdown()) {
            statusUpdateScheduler.shutdownNow();
        }
        statusUpdateScheduler = Executors.newSingleThreadScheduledExecutor();
        statusUpdateScheduler.scheduleAtFixedRate(() -> {
            // This scheduler only calls the UI update method.
            // The UI update method will then decide if a connection attempt is needed.
            updateModbusConnectionStatusUI();
        }, 0, 5, TimeUnit.SECONDS); // Check every 5 seconds, starting immediately
        ApplicationLauncher.logger.info("Modbus status update scheduler started.");

        // Add a shutdown hook for the scheduler when the application exits
        // This is important for clean application exit.
        // This is added to the stage's close request, ensuring it's tied to the UI lifecycle.
        Platform.runLater(() -> {
            if (dashboardStage != null) {
                dashboardStage.setOnCloseRequest(event -> {
                    if (statusUpdateScheduler != null && !statusUpdateScheduler.isShutdown()) {
                        statusUpdateScheduler.shutdownNow();
                        ApplicationLauncher.logger.info("DashboardViewModel: Modbus status scheduler shut down on window close.");
                    }
                    // Also ensure Modbus service is disconnected when the primary stage closes
                    ModbusService.disconnect(); // This will be handled by the logout button's new thread if logout is pressed
                    ApplicationLauncher.logger.info("Modbus service disconnected on primary stage close.");
                });
            }
        });
    }
}
