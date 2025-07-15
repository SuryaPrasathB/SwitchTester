package com.switchtester.app.service;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.ArduinoOptaConfig;
import com.switchtester.app.viewmodel.NotificationViewModel;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest;
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import java.net.InetAddress;

/**
 * Service class for handling Modbus TCP communication with the Arduino Opta.
 * This class is responsible for connecting, disconnecting, reading, and writing
 * Modbus coils and registers using the net.wimpi.modbus library.
 *
 * IMPORTANT: This class is designed to be called from background threads for blocking operations
 * to prevent freezing the JavaFX Application Thread.
 */
public class ModbusService {

    private static TCPMasterConnection connection;
    private static ModbusTCPTransaction transaction;
    private static ArduinoOptaConfig currentConfig; // Cache the current configuration
    private static final int MODBUS_SLAVE_ID = 1; // Default slave ID for Arduino Opta (often 1)

    // A lock object to synchronize access to connection establishment/closure
    private static final Object connectionLock = new Object();

    // Static initializer block to load config
    static {
        try {
            currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Failed to load Arduino Opta config: {}", e.getMessage(), e);
            // Notification for config loading failure is handled by the calling ViewModel.
        }
    }

    /**
     * Establishes a connection to the Modbus TCP server.
     * This method is designed to be called from a background thread as it performs blocking I/O.
     * It uses an internal lock to prevent multiple concurrent connection attempts.
     *
     * @return true if connection is successful or already established, false otherwise.
     */
    public static boolean connect() {
        synchronized (connectionLock) { // Only one thread can attempt to connect at a time
            if (connection != null && connection.isConnected()) {
                ApplicationLauncher.logger.info("Modbus connection already active.");
                return true;
            }

            // Ensure currentConfig is loaded before attempting connection
            if (currentConfig == null) {
                currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
                if (currentConfig == null) {
                    ApplicationLauncher.logger.error("Arduino Opta configuration is null. Cannot connect to Modbus master.");
                    // Use Platform.runLater for UI notification as this might be called from a background thread
                    javafx.application.Platform.runLater(() ->
                        NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                                "Modbus Connection Failed", "Arduino Opta configuration missing.")
                    );
                    return false;
                }
            }

            ApplicationLauncher.logger.info("Attempting to connect to Modbus TCP master at {}:{}", currentConfig.getIpAddress(), currentConfig.getPort());
            try {
                InetAddress addr = InetAddress.getByName(currentConfig.getIpAddress());
                connection = new TCPMasterConnection(addr);
                connection.setPort(currentConfig.getPort());
                // Set a timeout for the socket read operations, not the connect itself directly in jamod
                // For connect timeout, you'd typically need to manage the socket creation manually or use a different library.
                // The connect() call below can still block for the OS-level TCP connection timeout (e.g., 20-30s if unreachable).
                connection.setTimeout(currentConfig.getTimeoutSeconds() * 1000); // Set read/write timeout

                connection.connect(); // Establish the connection (this is the blocking call)

                // Only create transaction if connection is successful
                if (connection.isConnected()) {
                    transaction = new ModbusTCPTransaction(connection);
                    transaction.setRetries(3); // Set retry attempts for transactions within a single transaction execution

                    ApplicationLauncher.logger.info("Successfully connected to Modbus TCP server at {}:{}",
                            currentConfig.getIpAddress(), currentConfig.getPort());
                    javafx.application.Platform.runLater(() ->
                        NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.SUCCESS,
                                "Modbus Connected", "Successfully connected to Arduino Opta.")
                    );
                    return true;
                } else {
                    ApplicationLauncher.logger.error("Failed to establish Modbus TCP connection to {}:{}: Connection not active after connect() call.",
                            currentConfig.getIpAddress(), currentConfig.getPort());
                    javafx.application.Platform.runLater(() ->
                        NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                                "Modbus Connection Failed", "Could not connect to Arduino Opta.")
                    );
                    return false;
                }
            } catch (Exception e) {
                ApplicationLauncher.logger.error("Error connecting to Modbus TCP server at {}:{}: {}",
                        currentConfig.getIpAddress(), currentConfig.getPort(), e.getMessage(), e);
                javafx.application.Platform.runLater(() ->
                    NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                            "Modbus Connection Error", "Error connecting to Arduino Opta: " + e.getMessage())
                );
                return false;
            } finally {
                // Ensure connection is closed if it failed to establish properly,
                // or if it was partially established and then an error occurred.
                // However, if connect() throws, 'connection' might not be fully initialized or connected.
                // The isConnected() check below is more robust.
            }
        }
    }

    /**
     * Disconnects from the Modbus TCP server.
     */
    public static void disconnect() {
        synchronized (connectionLock) { // Synchronize disconnection to avoid race conditions
            if (connection != null && connection.isConnected()) {
                connection.close();
                ApplicationLauncher.logger.info("Disconnected from Modbus TCP server.");
                javafx.application.Platform.runLater(() ->
                    NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.INFO,
                            "Modbus Disconnected", "Disconnected from Arduino Opta.")
                );
            } else {
                ApplicationLauncher.logger.info("Modbus TCP master not connected, no action needed for disconnect.");
            }
        }
    }

    /**
     * Checks if the Modbus TCP master is currently connected.
     * This method does not block for connection attempts.
     * @return true if connected, false otherwise.
     */
    public static boolean isConnected() {
        // Direct check without synchronization on connectionLock for quick status
        return connection != null && connection.isConnected();
    }

    /**
     * Writes a boolean value to a Modbus coil.
     * Automatically attempts to connect if not already connected.
     * This method should be called from a background thread if the connection attempt
     * or the write operation itself might be blocking.
     *
     * @param coilAddress The address of the coil to write to.
     * @param value       The boolean value to write (true for ON, false for OFF).
     * @return true if the write operation was successful, false otherwise.
     */
    public static boolean writeCoil(int coilAddress, boolean value) {
        // Attempt to connect if not connected before performing the write
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot write coil {}: Modbus not connected.", coilAddress);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Not connected to Arduino Opta. Cannot write coil.")
            );
            return false;
        }

        try {
            WriteCoilRequest request = new WriteCoilRequest(coilAddress, value);
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ApplicationLauncher.logger.info("Successfully wrote coil {}: {}", coilAddress, value);
            return true;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error writing coil {}: {}.", coilAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Write Error", "Failed to write coil " + coilAddress + ": " + e.getMessage())
            );
            // If a ModbusException occurs, the connection might be unstable.
            // Consider disconnecting or re-attempting connection.
            disconnect(); // Force disconnect on ModbusException to trigger re-connection logic
            return false;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error writing coil {}: {}", coilAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Unexpected error writing coil " + coilAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on unexpected error
            return false;
        }
    }

    /**
     * Reads the boolean value of a Modbus coil.
     * Automatically attempts to connect if not already connected.
     * This method should be called from a background thread if the connection attempt
     * or the read operation itself might be blocking.
     *
     * @param coilAddress The address of the coil to read from.
     * @return The boolean value of the coil, or null if the read operation fails.
     */
    public static Boolean readCoil(int coilAddress) {
        // Attempt to connect if not connected before performing the read
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot read coil {}: Modbus not connected.", coilAddress);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Not connected to Arduino Opta. Cannot read coil.")
            );
            return null;
        }

        try {
            ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1); // Read 1 coil
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ReadCoilsResponse response = (ReadCoilsResponse) transaction.getResponse();
            boolean value = response.getCoilStatus(0); // Get status of the first (and only) coil read

            ApplicationLauncher.logger.debug("Successfully read coil {}: {}", coilAddress, value);
            return value;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error reading coil {}: {}.", coilAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Read Error", "Failed to read coil " + coilAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on ModbusException
            return null;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error reading coil {}: {}", coilAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Unexpected error reading coil " + coilAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on unexpected error
            return null;
        }
    }

    /**
     * Reads the value of a single Modbus holding register.
     * Automatically attempts to connect if not already connected.
     * This method should be called from a background thread if the connection attempt
     * or the read operation itself might be blocking.
     *
     * @param registerAddress The address of the holding register to read.
     * @return The integer value of the register, or null if the read operation fails.
     */
    public static Integer readRegister(int registerAddress) {
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot read register {}: Modbus not connected.", registerAddress);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Not connected to Arduino Opta. Cannot read register.")
            );
            return null;
        }

        try {
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, 1);
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            int value = response.getRegister(0).toUnsignedShort(); // Get the value from the first (and only) register.

            ApplicationLauncher.logger.debug("Successfully read register {}: {}", registerAddress, value);
            return value;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error reading register {}: {}.", registerAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Read Error", "Failed to read register " + registerAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on ModbusException
            return null;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error reading register {}: {}", registerAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Unexpected error reading register " + registerAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on unexpected error
            return null;
        }
    }

    /**
     * Writes an integer value to a single Modbus holding register.
     * Automatically attempts to connect if not already connected.
     * This method should be called from a background thread if the connection attempt
     * or the write operation itself might be blocking.
     *
     * @param registerAddress The address of the holding register to write.
     * @param value The integer value to write.
     * @return true if the write operation is successful, false otherwise.
     */
    public static boolean writeRegister(int registerAddress, int value) {
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot write register {}: Modbus not connected.", registerAddress);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Not connected to Arduino Opta. Cannot write register.")
            );
            return false;
        }

        try {
            SimpleRegister register = new SimpleRegister(value);
            WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(registerAddress, register);
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ApplicationLauncher.logger.info("Successfully wrote {} to register {}", value, registerAddress);
            return true;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error writing {} to register {}: {}.", value, registerAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Write Error", "Failed to write to register " + registerAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on ModbusException
            return false;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error writing {} to register {}: {}", value, registerAddress, e.getMessage(), e);
            javafx.application.Platform.runLater(() ->
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Error", "Unexpected error writing register " + registerAddress + ": " + e.getMessage())
            );
            disconnect(); // Force disconnect on unexpected error
            return false;
        }
    }

    /**
     * Called when the application is shutting down to ensure Modbus connection is closed.
     */
    public static void shutdown() {
        disconnect();
    }
}
