package com.switchtester.app.service;

import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.ArduinoOptaConfig;
import com.switchtester.app.viewmodel.NotificationViewModel;
import net.wimpi.modbus.ModbusException;
import net.wimpi.modbus.io.ModbusTCPTransaction;
import net.wimpi.modbus.msg.ReadCoilsRequest;
import net.wimpi.modbus.msg.ReadCoilsResponse;
import net.wimpi.modbus.msg.WriteCoilRequest;
import net.wimpi.modbus.net.TCPMasterConnection;
import java.net.InetAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service for Modbus TCP communication with the Arduino Opta PLC.
 * Handles connection management, reading, and writing Modbus coils.
 */
public class ModbusService {

    private static TCPMasterConnection connection;
    private static ModbusTCPTransaction transaction;
    private static ArduinoOptaConfig currentConfig; // Cache the current configuration
    private static boolean isConnected = false;
    private static final int MODBUS_SLAVE_ID = 1; // Default slave ID for Arduino Opta (often 1)

    // ScheduledExecutorService for connection retry logic
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    /**
     * Attempts to establish a Modbus TCP connection to the Arduino Opta PLC.
     * Uses the configuration loaded from ArduinoOptaConfigManager.
     *
     * @return true if the connection is successful, false otherwise.
     */
    public static boolean connect() {
        if (isConnected && connection != null && connection.isConnected()) {
            ApplicationLauncher.logger.info("Modbus connection already active.");
            return true;
        }

        currentConfig = ArduinoOptaConfigManager.getCurrentConfig(); // Get the latest config

        try {
            InetAddress addr = InetAddress.getByName(currentConfig.getIpAddress());
            connection = new TCPMasterConnection(addr);
            connection.setPort(currentConfig.getPort());
            connection.connect(); // Establish the connection

            transaction = new ModbusTCPTransaction(connection);
            transaction.setRetries(3); // Set retry attempts for transactions

            isConnected = connection.isConnected();
            if (isConnected) {
                ApplicationLauncher.logger.info("Successfully connected to Modbus TCP server at {}:{}",
                        currentConfig.getIpAddress(), currentConfig.getPort());
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.SUCCESS,
                        "Modbus Connected", "Successfully connected to Arduino Opta.");
                // Stop any pending retry tasks if connection is successful
                scheduler.shutdownNow();
                scheduler = Executors.newSingleThreadScheduledExecutor(); // Re-initialize for future use
            } else {
                ApplicationLauncher.logger.error("Failed to establish Modbus TCP connection to {}:{}",
                        currentConfig.getIpAddress(), currentConfig.getPort());
                NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                        "Modbus Connection Failed", "Could not connect to Arduino Opta.");
                scheduleReconnect(); // Attempt to reconnect
            }
        } catch (Exception e) {
            isConnected = false;
            ApplicationLauncher.logger.error("Error connecting to Modbus TCP server at {}:{}: {}",
                    currentConfig.getIpAddress(), currentConfig.getPort(), e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Connection Error", "Error connecting to Arduino Opta: " + e.getMessage());
            scheduleReconnect(); // Attempt to reconnect
        }
        return isConnected;
    }

    /**
     * Schedules a reconnection attempt after a delay.
     */
    private static void scheduleReconnect() {
        if (scheduler.isShutdown()) {
            scheduler = Executors.newSingleThreadScheduledExecutor(); // Re-initialize if shutdown
        }
        ApplicationLauncher.logger.info("Scheduling Modbus reconnection attempt in 10 seconds...");
        NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.WARNING,
                "Modbus Reconnecting", "Attempting to reconnect to Arduino Opta...");
        scheduler.schedule(ModbusService::connect, 10, TimeUnit.SECONDS);
    }

    /**
     * Disconnects from the Modbus TCP server.
     */
    public static void disconnect() {
        if (connection != null && connection.isConnected()) {
            connection.close();
            isConnected = false;
            ApplicationLauncher.logger.info("Disconnected from Modbus TCP server.");
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.INFO,
                    "Modbus Disconnected", "Disconnected from Arduino Opta.");
        }
        // Also shut down any pending retry tasks
        scheduler.shutdownNow();
        scheduler = Executors.newSingleThreadScheduledExecutor(); // Re-initialize for future use
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
        if (!isConnected && !connect()) { // Attempt to connect if not connected
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
            ApplicationLauncher.logger.error("Modbus error writing coil {}: {}. Attempting reconnect.", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Write Error", "Failed to write coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            scheduleReconnect(); // Attempt to reconnect
            return false;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error writing coil {}: {}", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error writing coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            scheduleReconnect(); // Attempt to reconnect
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
        if (!isConnected && !connect()) { // Attempt to connect if not connected
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
            ApplicationLauncher.logger.error("Modbus error reading coil {}: {}. Attempting reconnect.", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Read Error", "Failed to read coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            scheduleReconnect(); // Attempt to reconnect
            return null;
        } catch (Exception e) {
            ApplicationLauncher.logger.error("Unexpected error reading coil {}: {}", coilAddress, e.getMessage(), e);
            NotificationManager.getInstance().showNotification(NotificationViewModel.NotificationType.ERROR,
                    "Modbus Error", "Unexpected error reading coil " + coilAddress + ": " + e.getMessage());
            isConnected = false; // Mark as disconnected
            scheduleReconnect(); // Attempt to reconnect
            return null;
        }
    }

    /**
     * Returns the current connection status.
     * @return true if connected, false otherwise.
     */
    public static boolean isConnected() {
        return isConnected;
    }

    /**
     * Called when the application is shutting down to ensure Modbus connection is closed.
     */
    public static void shutdown() {
        disconnect();
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdownNow();
            ApplicationLauncher.logger.info("Modbus reconnection scheduler shut down.");
        }
    }
}
