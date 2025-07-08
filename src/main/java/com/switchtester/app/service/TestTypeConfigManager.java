package com.switchtester.app.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.switchtester.app.ApplicationLauncher;
import com.switchtester.app.model.config.TestTypeConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestTypeConfigManager {

    private static final String CONFIG_FILE_PATH = "test_type_configs.json"; // Stored in app's root directory
    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT); // Pretty print JSON
    }

    public static List<TestTypeConfig> loadTestTypeConfigs() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (!configFile.exists() || configFile.length() == 0) {
            // If file doesn't exist or is empty, create default configurations
            List<TestTypeConfig> defaultConfigs = createDefaultTestTypeConfigs();
            saveTestTypeConfigs(defaultConfigs); // Save defaults for the first time
            return defaultConfigs;
        }
        try {
            return Arrays.asList(objectMapper.readValue(configFile, TestTypeConfig[].class));
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error loading test type configurations: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>(); // Return empty list on error
        }
    }

    public static void saveTestTypeConfigs(List<TestTypeConfig> configs) {
        File configFile = new File(CONFIG_FILE_PATH);
        try {
            objectMapper.writeValue(configFile, configs);
        } catch (IOException e) {
            ApplicationLauncher.logger.error("Error saving test type configurations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static Optional<TestTypeConfig> getTestTypeConfigByName(String name) {
        return loadTestTypeConfigs().stream()
                .filter(config -> config.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    // Creates and returns the hardcoded test types as a list of objects
    private static List<TestTypeConfig> createDefaultTestTypeConfigs() {
        List<TestTypeConfig> defaults = new ArrayList<>();

        defaults.add(new TestTypeConfig(
                "Normal Operation Test with Inductive Loads",
                "as per 19.1 of IS3854:2023",
                "40000",
                "2000",
                "500",
                "1500",
                "0.8",
                "N/A", "N/A", "N/A"
        ));

        defaults.add(new TestTypeConfig(
                "Endurance Test",
                "Standard endurance cycle",
                "100000",
                "1000",
                "300",
                "700",
                "1.0",
                "N/A", "N/A", "N/A"
        ));

        defaults.add(new TestTypeConfig(
                "Dielectric Strength Test",
                "High voltage insulation test",
                "1",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "1500 V AC",
                "0.001 A",
                "N/A"
        ));

        defaults.add(new TestTypeConfig(
                "Insulation Resistance Test",
                "Measures insulation resistance",
                "1",
                "N/A",
                "N/A",
                "N/A",
                "N/A",
                "500 V DC",
                "N/A",
                "N/A"
        ));

        return defaults;
    }
}
