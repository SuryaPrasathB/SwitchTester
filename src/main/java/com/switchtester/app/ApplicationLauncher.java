package com.switchtester.app;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.util.Set; // Import Set for permissions

// Import SLF4J Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.switchtester.app.service.ModbusService; // Import ModbusService
import com.switchtester.app.viewmodel.DashboardViewModel;
import com.switchtester.app.viewmodel.LoginViewModel;
import com.switchtester.app.viewmodel.SplashViewModel;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Main application class for the Switch Tester project.
 * This class extends Application and serves as the entry point for the JavaFX application.
 * It is responsible for initializing the primary stage and loading the initial view (Splash Screen).
 */
public class ApplicationLauncher extends Application { // Renamed from MainApp

    // Initialize a public static final logger for the entire application
    // Other classes can now access this logger directly via ApplicationLauncher.logger
    public static final Logger logger = LoggerFactory.getLogger(ApplicationLauncher.class);

    // The primary stage will be used for the main application windows (Dashboard, etc.)
    // It will always be DECORATED to allow for a title bar when not in fullscreen.
    private static Stage primaryStage;

    // Path to the application icon
    private static final String APP_ICON_PATH = "/com/switchtester/app/images/app_icon.png";

    // --- RBAC: Store Logged-in User Profile and Permissions ---
    private static String loggedInUserProfile; // e.g., "Admin", "Production", "Quality", "Maintenance"
    private static Set<String> loggedInUserPermissions; // Granular permissions for dashboard panels

    public static String getLoggedInUserProfile() {
        return loggedInUserProfile;
    }

    public static void setLoggedInUserProfile(String profile) {
        ApplicationLauncher.loggedInUserProfile = profile;
        logger.debug("Logged in user profile set to: {}", profile);
    }

    public static Set<String> getLoggedInUserPermissions() {
        return loggedInUserPermissions;
    }

    public static void setLoggedInUserPermissions(Set<String> permissions) {
        ApplicationLauncher.loggedInUserPermissions = permissions;
        logger.debug("Logged in user permissions set: {}", permissions);
    }
    // --- End RBAC ---

    // --- Single Instance Control ---
    private static File lockFile;
    private static FileChannel fileChannel;
    private static FileLock fileLock;
    private static final String LOCK_FILE_NAME = "SwitchTesterApp.lock";

    /**
     * Attempts to acquire a file lock to ensure only one instance of the application runs.
     * @return true if the lock is successfully acquired, false otherwise.
     */
    private static boolean acquireLock() {
        try {
            File tempDir = new File(System.getProperty("java.io.tmpdir"));
            lockFile = new File(tempDir, LOCK_FILE_NAME);

            // Ensure the lock file is deleted on exit, regardless of how the app closes
            lockFile.deleteOnExit();

            fileChannel = new RandomAccessFile(lockFile, "rw").getChannel();
            fileLock = fileChannel.tryLock(); // Try to acquire the lock

            if (fileLock == null) {
                // Lock could not be acquired, another instance is running
                logger.warn("Another instance of Switch Tester App is already running. Exiting.");
                return false;
            }

            // Add a shutdown hook to release the lock and close Modbus connection when the application exits
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                releaseLock();
                ModbusService.shutdown(); // Call ModbusService shutdown
            }));

            logger.info("Application lock acquired successfully.");
            return true;
        } catch (IOException | OverlappingFileLockException e) {
            logger.error("Error acquiring application lock: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Releases the acquired file lock and closes the file channel.
     */
    private static void releaseLock() {
        try {
            if (fileLock != null) {
                fileLock.release();
                fileLock = null;
            }
            if (fileChannel != null) {
                fileChannel.close();
                fileChannel = null;
            }
            if (lockFile != null && lockFile.exists()) {
                if (lockFile.delete()) { // Explicitly delete the lock file
                    logger.debug("Application lock file deleted.");
                } else {
                    logger.warn("Failed to delete application lock file: {}", lockFile.getAbsolutePath());
                }
            }
            logger.info("Application lock released.");
        } catch (IOException e) {
            logger.error("Error releasing application lock: {}", e.getMessage(), e);
        }
    }
    // --- End Single Instance Control ---


    /**
     * The start method is the main entry point for all JavaFX applications.
     * It is called after the init() method returns, and after the system is ready for the application to begin running.
     *
     * @param stage The primary stage for this application, onto which the application scene can be set.
     * This stage will be used for the dashboard and subsequent main application screens.
     * @throws Exception if something goes wrong during application startup.
     */
    @Override
    public void start(Stage stage) throws Exception {
        // Add a very early log message to check if logging is working
        logger.debug("Log4j2 initialized successfully (early check in start method)."); 
        logger.info("Starting Switch Tester Application...");
        // Attempt to acquire the application lock
        if (!acquireLock()) {
            Platform.exit(); // Exit if another instance is already running
            return;
        }

        primaryStage = stage;
        primaryStage.setTitle("Switch Tester Application");
        primaryStage.initStyle(StageStyle.DECORATED); // Set style ONCE here for the primary stage

        // Set the application icon for the primary stage
        try {
            URL iconUrl = getClass().getResource(APP_ICON_PATH);
            if (iconUrl != null) {
                primaryStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                logger.debug("Application icon loaded from: {}", APP_ICON_PATH);
            } else {
                logger.warn("Application icon not found at: {}", APP_ICON_PATH);
            }
        } catch (Exception e) {
            logger.error("Error loading application icon: {}", e.getMessage(), e);
        }

        showSplashScreen();
    }

    /**
     * Returns the primary stage of the application.
     * This is useful for setting owners of dialogs or accessing the main window.
     * @return The primary Stage.
     */
    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    /**
     * Loads and displays the splash screen on a *new*, temporary stage.
     * The splash screen will be undecorated (no title bar, close buttons) and centered.
     */
    public static void showSplashScreen() {
        try {
            Stage splashStage = new Stage();
            splashStage.initStyle(StageStyle.UNDECORATED);
            splashStage.setTitle("Loading Switch Tester...");

            try {
                URL iconUrl = ApplicationLauncher.class.getResource(APP_ICON_PATH);
                if (iconUrl != null) {
                    splashStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                }
            } catch (Exception e) {
                logger.error("Error loading splash screen icon: {}", e.getMessage(), e);
            }

            FXMLLoader loader = new FXMLLoader(ApplicationLauncher.class.getResource("view/SplashView.fxml"));
            Parent splashRoot = loader.load();

            SplashViewModel splashViewModel = loader.getController();
            splashViewModel.setSplashStage(splashStage);

            Scene splashScene = new Scene(splashRoot);

            splashStage.setScene(splashScene);
            splashStage.sizeToScene();
            splashStage.centerOnScreen();
            splashStage.show();
            logger.info("Splash screen displayed.");
        } catch (IOException e) {
            logger.error("Error loading splash screen: {}", e.getMessage(), e);
            showLoginScreen(); // Fallback to login if splash fails
        }
    }

    /**
     * Loads and displays the login screen on a *new*, temporary stage.
     * This method is called from the SplashViewModel after the splash screen's loading is complete
     * and the splash stage has been closed.
     */
    public static void showLoginScreen() {
        try {
            Stage loginStage = new Stage();
            loginStage.initStyle(StageStyle.UNDECORATED);
            loginStage.setTitle("Login");

            try {
                URL iconUrl = ApplicationLauncher.class.getResource(APP_ICON_PATH);
                if (iconUrl != null) {
                    loginStage.getIcons().add(new Image(iconUrl.toExternalForm()));
                }
            } catch (Exception e) {
                logger.error("Error loading login screen icon: {}", e.getMessage(), e);
            }

            FXMLLoader loader = new FXMLLoader(ApplicationLauncher.class.getResource("view/LoginView.fxml"));
            Parent loginRoot = loader.load();

            LoginViewModel loginViewModel = loader.getController();
            loginViewModel.setLoginStage(loginStage);

            Scene loginScene = new Scene(loginRoot);

            loginStage.setScene(loginScene);
            loginStage.sizeToScene();
            loginStage.centerOnScreen();
            loginStage.show();
            logger.info("Login screen displayed.");
        } catch (IOException e) {
            logger.error("Error loading login screen: {}", e.getMessage(), e);
        }
    }

    /**
     * Loads and displays the main dashboard screen on the primary stage in fullscreen mode.
     * This method is called after successful login.
     */
    public static void showMainDashboardScreen() {
        try {
            primaryStage.initStyle(StageStyle.DECORATED);

            FXMLLoader loader = new FXMLLoader(ApplicationLauncher.class.getResource("view/DashboardView.fxml"));
            Parent dashboardRoot = loader.load();

            DashboardViewModel dashboardViewModel = loader.getController();
            dashboardViewModel.setDashboardStage(primaryStage);

            Scene dashboardScene = new Scene(dashboardRoot);

            primaryStage.setScene(dashboardScene);
            primaryStage.setFullScreen(true);
            primaryStage.setFullScreenExitHint("");
            primaryStage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.valueOf("Esc"));

            primaryStage.setTitle("Switch Tester Dashboard");
            primaryStage.show();
            logger.info("Main Dashboard screen displayed in fullscreen.");
        } catch (IOException e) {
            logger.error("Error loading dashboard screen: {}", e.getMessage(), e);
        }
    }

    /**
     * The main method is ignored in JavaFX applications.
     * The `launch()` method is called to start the JavaFX application.
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
