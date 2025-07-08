package com.switchtester.app.model.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Objects;

/**
 * Represents the configuration for connecting to the Arduino Opta PLC via Modbus TCP.
 */
public class ArduinoOptaConfig {

    @JsonProperty("ipAddress")
    private String ipAddress;
    @JsonProperty("port")
    private int port;
    @JsonProperty("pistonCoilAddress")
    private int pistonCoilAddress; // Coil address for the Piston control

    // Default constructor for Jackson deserialization
    public ArduinoOptaConfig() {
        // Default values for initial setup if not provided in JSON
        this.ipAddress = "192.168.1.10"; // Default IP address
        this.port = 502; // Default Modbus TCP port
        this.pistonCoilAddress = 0; // Default coil address for Piston (e.g., Coil 0)
    }

    /**
     * Constructor for creating an ArduinoOptaConfig object.
     * @param ipAddress The IP address of the Arduino Opta Modbus TCP server.
     * @param port The Modbus TCP port (default is 502).
     * @param pistonCoilAddress The Modbus coil address for the Piston control.
     */
    @JsonCreator
    public ArduinoOptaConfig(@JsonProperty("ipAddress") String ipAddress,
                             @JsonProperty("port") int port,
                             @JsonProperty("pistonCoilAddress") int pistonCoilAddress) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP Address cannot be null");
        this.port = port;
        this.pistonCoilAddress = pistonCoilAddress;
    }

    // Getters
    public String getIpAddress() {
        return ipAddress;
    }

    public int getPort() {
        return port;
    }

    public int getPistonCoilAddress() {
        return pistonCoilAddress;
    }

    // Setters
    public void setIpAddress(String ipAddress) {
        this.ipAddress = Objects.requireNonNull(ipAddress, "IP Address cannot be null");
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setPistonCoilAddress(int pistonCoilAddress) {
        this.pistonCoilAddress = pistonCoilAddress;
    }

    @Override
    public String toString() {
        return "ArduinoOptaConfig{" +
               "ipAddress='" + ipAddress + '\'' +
               ", port=" + port +
               ", pistonCoilAddress=" + pistonCoilAddress +
               '}';
    }
}
