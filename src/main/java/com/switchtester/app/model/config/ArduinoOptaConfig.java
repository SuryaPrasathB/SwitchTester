package com.switchtester.app.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Configuration model for Arduino Opta PLC.
 * This class holds network parameters and Modbus-specific addresses.
 */
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unknown fields in JSON
public class ArduinoOptaConfig {

    @JsonProperty("ipAddress")
    private String ipAddress;
    @JsonProperty("port")
    private int port;
    @JsonProperty("timeoutSeconds")
    private int timeoutSeconds; // Added timeout for Modbus operations
    // Removed @JsonProperty("pistonCoilAddress") private int pistonCoilAddress; // This field is no longer part of the config

    // New fields for settings section (Modbus registers)
    @JsonProperty("setVoltageRegisterAddress")
    private int setVoltageRegisterAddress;
    @JsonProperty("setCurrentRegisterAddress")
    private int setCurrentRegisterAddress;


    @JsonCreator
    public ArduinoOptaConfig(
            @JsonProperty("ipAddress") String ipAddress,
            @JsonProperty("port") int port,
            @JsonProperty("timeoutSeconds") Integer timeoutSeconds, // Use Integer for nullable
            // Removed @JsonProperty("pistonCoilAddress") Integer pistonCoilAddress, // Removed from constructor
            @JsonProperty("setVoltageRegisterAddress") Integer setVoltageRegisterAddress,
            @JsonProperty("setCurrentRegisterAddress") Integer setCurrentRegisterAddress) {
        this.ipAddress = ipAddress;
        this.port = port;
        // Provide default values if not present in JSON
        this.timeoutSeconds = (timeoutSeconds != null) ? timeoutSeconds : 5; // Default to 5 seconds
        // Removed this.pistonCoilAddress = (pistonCoilAddress != null) ? pistonCoilAddress : 0; // No longer needed
        this.setVoltageRegisterAddress = (setVoltageRegisterAddress != null) ? setVoltageRegisterAddress : 100; // Default
        this.setCurrentRegisterAddress = (setCurrentRegisterAddress != null) ? setCurrentRegisterAddress : 101; // Default
    }

    // Getters
    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    // Removed getPistonCoilAddress() // No longer needed
    // public int getPistonCoilAddress() {
    //     return pistonCoilAddress;
    // }

    public int getSetVoltageRegisterAddress() {
        return setVoltageRegisterAddress;
    }

    public int getSetCurrentRegisterAddress() {
        return setCurrentRegisterAddress;
    }

    // Setters (if configuration can be changed at runtime and saved)
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    // Removed setPistonCoilAddress() // No longer needed
    // public void setPistonCoilAddress(int pistonCoilAddress) {
    //     this.pistonCoilAddress = pistonCoilAddress;
    // }

    public void setSetVoltageRegisterAddress(int setVoltageRegisterAddress) {
        this.setVoltageRegisterAddress = setVoltageRegisterAddress;
    }

    public void setSetCurrentRegisterAddress(int setCurrentRegisterAddress) {
        this.setCurrentRegisterAddress = setCurrentRegisterAddress;
    }
}
