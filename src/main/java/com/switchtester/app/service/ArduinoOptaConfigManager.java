package com.switchtester.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.switchtester.app.model.config.ArduinoOptaConfig; // Import the new config model
import com.switchtester.app.ApplicationLauncher; // For logging

import java.io.File;
import java.io.IOException;

/**
 * Manages loading and saving of Arduino Opta PLC configuration.
 * Configuration is stored in a JSON file.
 */
public class ArduinoOptaConfigManager {

    private static final String CONFIG_FILE_PATH = "arduino_opta_config.json"; // Stored in app's root directory
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static ArduinoOptaConfig currentConfig; // Cache the loaded configuration

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
        // Load config on application startup
        currentConfig = loadConfig();
    }

    /**
     * Loads the Arduino Opta configuration from the JSON file.
     * If the file doesn't exist or is empty, it creates and saves a default configuration.
     * @return The loaded or default ArduinoOptaConfig object.
     */
    public static ArduinoOptaConfig loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() || configFile.length() == 0) {
            // If file doesn't exist or is empty, create default configuration
            ArduinoOptaConfig defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig); // Save default for the first time
            ApplicationLauncher.logger.info("Created and saved default Arduino Opta configuration.");
            return defaultConfig;
        }
        try {
            currentConfig = objectMapper.readValue(configFile, ArduinoOptaConfig.class);
            ApplicationLauncher.logger.info("Loaded Arduino Opta configuration from file.");
            return currentConfig;
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading Arduino Opta configuration: {}", e.getMessage(), e);
            // Fallback to default config on error
            ArduinoOptaConfig defaultConfig = createDefaultConfig();
            ApplicationLauncher.logger.warn("Falling back to default Arduino Opta configuration due to load error.");
            return defaultConfig;
        }
    }

    /**
     * Saves the given Arduino Opta configuration to the JSON file.
     * @param config The ArduinoOptaConfig object to save.
     */
    public static void saveConfig(ArduinoOptaConfig config) {
        File configFile = new File(CONFIG_FILE_PATH);
        try {
            objectMapper.writeValue(configFile, config);
            currentConfig = config; // Update cached config
            ApplicationLauncher.logger.info("Saved Arduino Opta configuration to file.");
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error saving Arduino Opta configuration: {}", e.getMessage(), e);
        }
    }

    /**
     * Returns the currently loaded Arduino Opta configuration.
     * Ensures a configuration is always available (either loaded or default).
     * @return The current ArduinoOptaConfig object.
     */
    public static ArduinoOptaConfig getCurrentConfig() {
        if (currentConfig == null) {
            currentConfig = loadConfig(); // Ensure it's loaded if not already
        }
        return currentConfig;
    }

    /**
     * Creates and returns a default ArduinoOptaConfig object.
     * @return A new ArduinoOptaConfig object with default values.
     */
    private static ArduinoOptaConfig createDefaultConfig() {
        // You can set sensible defaults here.
        // These are example values; replace with your actual Arduino Opta's default IP and Modbus coil address.
        return new ArduinoOptaConfig("192.168.1.10", 502); // Default IP, Port, Piston Coil 0
    }
}
