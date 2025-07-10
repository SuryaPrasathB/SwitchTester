package com.switchtester.app.service;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.ArduinoOptaConfig;
import com.switchtester.app.viewmodel.NotificationViewModel;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.ReadMultipleRegistersRequest; // Corrected import for reading holding registers
import net.wimpi.modbus.msg.ReadMultipleRegistersResponse; // Corrected import for reading holding registers
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.msg.WriteSingleRegisterRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import net.wimpi.modbus.procimg.SimpleRegister;
import java.net.InetAddress;

/**
 * Service class for handling Modbus TCP communication with the Arduino Opta.
 * This class is responsible for connecting, disconnecting, reading, and writing
 * Modbus coils and registers using the net.wimpi.modbus library.
 */
public class ModbusService {

    private static TCPMasterConnection connection;
    private static ModbusTCPTransaction transaction;
    private static ArduinoOptaConfig currentConfig; // Cache the current configuration
    private static boolean isConnected = false;
    private static final int MODBUS_SLAVE_ID = 1; // Default slave ID for Arduino Opta (often 1)

    // Static initializer block to load config
    static {
        try {
            currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Failed to load Arduino Opta config: {}", e.getMessage(), e);
            // This error will be caught by the calling ViewModel (e.g., DashboardViewModel)
            // and a notification will be shown there.
        }
    }

    /**
     * Establishes a connection to the Modbus TCP server.
     * If already connected, it logs a message and does nothing.
     * This method is synchronized to prevent multiple connection attempts simultaneously.
     * @return true if connection is successful or already established, false otherwise.
     */
    public static synchronized boolean connect() {
        if (isConnected && connection != null && connection.isConnected()) {
            ApplicationLauncher.logger.info("Modbus connection already active.");
            return true;
        }

        // Ensure currentConfig is loaded before attempting connection
        if (currentConfig == null) {
            currentConfig = ArduinoOptaConfigManager.getCurrentConfig();
            if (currentConfig == null) {
                ApplicationLauncher.logger.error("Arduino Opta configuration is null. Cannot connect to Modbus master.");
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Connection Failed", "Arduino Opta configuration missing.");
                return false;
            }
        }

        ApplicationLauncher.logger.info("Attempting to connect to Modbus TCP master at {}:{}", currentConfig.getIpAddress(), currentConfig.getPort());
        try {
            InetAddress addr = InetAddress.getByName(currentConfig.getIpAddress());
            connection = new TCPMasterConnection(addr);
            connection.setPort(currentConfig.getPort());
            connection.connect(); // Establish the connection

            transaction = new ModbusTCPTransaction(connection);
            transaction.setRetries(3); // Set retry attempts for transactions within a single transaction execution

            isConnected = connection.isConnected();
            if (isConnected) {
                ApplicationLauncher.logger.info("Successfully connected to Modbus TCP server at {}:{}",
                        currentConfig.getIpAddress(), currentConfig.getPort());
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.SUCCESS,
                        "Modbus Connected", "Successfully connected to Arduino Opta.");
            } else {
                ApplicationLauncher.logger.error("Failed to establish Modbus TCP connection to {}:{}",
                        currentConfig.getIpAddress(), currentConfig.getPort());
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Connection Failed", "Could not connect to Arduino Opta.");
            }
        } catch (Exception e) {
            isConnected = false;
            ApplicationLauncher.logger.error("Error connecting to Modbus TCP server at {}:{}: {}",
                    currentConfig.getIpAddress(), currentConfig.getPort(), e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Connection Error", "Error connecting to Arduino Opta: " + e.getMessage());
        }
        return isConnected;
    }

    /**
     * Disconnects from the Modbus TCP server.
     * This method is synchronized to prevent issues during concurrent access.
     */
    public static synchronized void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
            isConnected = false;
            ApplicationLauncher.logger.info("Disconnected from Modbus TCP server.");
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.INFO,
                    "Modbus Disconnected", "Disconnected from Arduino Opta.");
        } else {
            ApplicationLauncher.logger.info("Modbus TCP master not connected, no action needed for disconnect.");
        }
    }

    /**
     * Checks if the Modbus TCP master is currently connected.
     * @return true if connected, false otherwise.
     */
    public static synchronized boolean isConnected() {
        // Ensure that the internal 'isConnected' flag is synchronized with the actual connection state
        // This is important because the connection might drop externally.
        if (connection != null) {
            isConnected = connection.isConnected();
        } else {
            isConnected = false;
        }
        return isConnected;
    }

    /**
     * Writes a boolean value to a Modbus coil.
     * Automatically attempts to connect if not already connected.
     *
     * @param coilAddress The address of the coil to write to.
     * @param value       The boolean value to write (true for ON, false for OFF).
     * @return true if the write operation was successful, false otherwise.
     */
    public static boolean writeCoil(int coilAddress, boolean value) {
        // Attempt to connect if not connected before performing the write
        if (!isConnected() && !connect()) { // Use isConnected() to re-check actual state
            ApplicationLauncher.logger.warn("Cannot write coil {}: Modbus not connected.", coilAddress);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Not connected to Arduino Opta. Cannot write coil.");
            return false;
        }

        try {
            WriteCoilRequest request = new WriteCoilRequest(coilAddress, value);
            request.setUnitID(MODBUS_SLAVE_ID); // Set slave ID
            transaction.setRequest(request);
            transaction.execute(); // Execute the transaction

            ApplicationLauncher.logger.info("Successfully wrote coil {}: {}", coilAddress, value);
            return true;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error writing coil {}: {}. Marking as disconnected.", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Write Error", "Failed to write coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return false;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error writing coil {}: {}", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error writing coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return false;
        }
    }

    /**
     * Reads the boolean value of a Modbus coil.
     * Automatically attempts to connect if not already connected.
     *
     * @param coilAddress The address of the coil to read from.
     * @return The boolean value of the coil, or null if the read operation fails.
     */
    public static Boolean readCoil(int coilAddress) {
        // Attempt to connect if not connected before performing the read
        if (!isConnected() && !connect()) { // Use isConnected() to re-check actual state
            ApplicationLauncher.logger.warn("Cannot read coil {}: Modbus not connected.", coilAddress);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Not connected to Arduino Opta. Cannot read coil.");
            return null;
        }

        try {
            ReadCoilsRequest request = new ReadCoilsRequest(coilAddress, 1); // Read 1 coil
            request.setUnitID(MODBUS_SLAVE_ID); // Set slave ID
            transaction.setRequest(request);
            transaction.execute(); // Execute the transaction

            ReadCoilsResponse response = (ReadCoilsResponse) transaction.getResponse();
            boolean value = response.getCoilStatus(0); // Get status of the first (and only) coil read

            ApplicationLauncher.logger.info("Successfully read coil {}: {}", coilAddress, value);
            return value;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error reading coil {}: {}. Marking as disconnected.", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Read Error", "Failed to read coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return null;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error reading coil {}: {}", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error reading coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return null;
        }
    }

    /**
     * Reads the value of a single Modbus holding register.
     * Automatically attempts to connect if not already connected.
     *
     * @param registerAddress The address of the holding register to read.
     * @return The integer value of the register, or null if the read operation fails.
     */
    public static Integer readRegister(int registerAddress) {
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot read register {}: Modbus not connected.", registerAddress);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Not connected to Arduino Opta. Cannot read register.");
            return null;
        }

        try {
            // Read 1 holding register using ReadMultipleRegistersRequest (Function Code 0x03)
            ReadMultipleRegistersRequest request = new ReadMultipleRegistersRequest(registerAddress, 1);
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ReadMultipleRegistersResponse response = (ReadMultipleRegistersResponse) transaction.getResponse();
            // Get the value from the first (and only) register.
            // toUnsignedShort() is generally safer for Modbus registers.
            int value = response.getRegister(0).toUnsignedShort();

            ApplicationLauncher.logger.info("Successfully read register {}: {}", registerAddress, value);
            return value;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error reading register {}: {}. Marking as disconnected.", registerAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Read Error", "Failed to read register " + registerAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return null;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error reading register {}: {}", registerAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error reading register " + registerAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return null;
        }
    }

    /**
     * Writes an integer value to a single Modbus holding register.
     * Automatically attempts to connect if not already connected.
     *
     * @param registerAddress The address of the holding register to write.
     * @param value The integer value to write.
     * @return true if the write operation is successful, false otherwise.
     */
    public static boolean writeRegister(int registerAddress, int value) {
        if (!isConnected() && !connect()) {
            ApplicationLauncher.logger.warn("Cannot write register {}: Modbus not connected.", registerAddress);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Not connected to Arduino Opta. Cannot write register.");
            return false;
        }

        try {
            // Create a SimpleRegister from the integer value
            SimpleRegister register = new SimpleRegister(value);
            WriteSingleRegisterRequest request = new WriteSingleRegisterRequest(registerAddress, register);
            request.setUnitID(MODBUS_SLAVE_ID);
            transaction.setRequest(request);
            transaction.execute();

            ApplicationLauncher.logger.info("Successfully wrote {} to register {}", value, registerAddress);
            return true;
        } catch (ModbusException e) {
            ApplicationLauncher.logger.error("Modbus error writing {} to register {}: {}. Marking as disconnected.", value, registerAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Write Error", "Failed to write to register " + registerAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            return false;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error writing {} to register {}: {}", value, registerAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error writing register " + registerAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
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
