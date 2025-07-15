package com.switchtester.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.switchtester.app.model.config.ArduinoOptaConfig;

import java.io.File;
import java.io.IOException;

/**
 * Manages loading and saving of Arduino Opta configuration.
 * This class ensures a singleton configuration is available throughout the application.
 */
public class ArduinoOptaConfigManager {

    private static final String CONFIG_FILE_PATH = "arduino_opta_config.json"; // Stored in app's root directory
    private static ArduinoOptaConfig currentConfig;
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
    }

    /**
     * Retrieves the current Arduino Opta configuration.
     * If the configuration file does not exist or cannot be loaded, a default configuration is created and saved.
     * @return The current ArduinoOptaConfig instance.
     */
    public static synchronized ArduinoOptaConfig getCurrentConfig() {
        if (currentConfig == null) {
            currentConfig = loadConfig();
        }
        return currentConfig;
    }

    /**
     * Loads the Arduino Opta configuration from the JSON file.
     * If the file doesn't exist or there's an error loading, it creates a default.
     * @return The loaded or default ArduinoOptaConfig.
     */
    private static ArduinoOptaConfig loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() || configFile.length() == 0) {
            System.out.println("Arduino Opta config file not found or is empty. Creating default configuration.");
            ArduinoOptaConfig defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig); // Save the newly created default config
            return defaultConfig;
        }
        try {
            return objectMapper.readValue(configFile, ArduinoOptaConfig.class);
        } catch (IOException e) {
            System.err.println("Error loading Arduino Opta configuration: " + e.getMessage());
            e.printStackTrace();
            // Fallback to default if loading fails
            System.err.println("Falling back to default Arduino Opta configuration due to load error.");
            ArduinoOptaConfig defaultConfig = createDefaultConfig();
            saveConfig(defaultConfig); // Attempt to save the new default config
            return defaultConfig;
        }
    }

    /**
     * Saves the current Arduino Opta configuration to the JSON file.
     * @param config The ArduinoOptaConfig instance to save.
     */
    public static synchronized void saveConfig(ArduinoOptaConfig config) {
        File configFile = new File(CONFIG_FILE_PATH);
        try {
            objectMapper.writeValue(configFile, config);
            currentConfig = config; // Update the cached config
            System.out.println("Arduino Opta configuration saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving Arduino Opta configuration: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Creates a default Arduino Opta configuration.
     * @return A new ArduinoOptaConfig instance with default values.
     */
    private static ArduinoOptaConfig createDefaultConfig() {
        // Use a common default IP for testing, and include defaults for new fields
        // Removed pistonCoilAddress as it's no longer managed here
        return new ArduinoOptaConfig("192.168.1.123", 502, 5, 100, 101);
    }
}
