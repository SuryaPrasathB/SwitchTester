package com.switchtester.app.model.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties; // For JSON serialization
import com.fasterxml.jackson.annotation.JsonProperty;

// Using Jackson annotations for robust JSON serialization/deserialization
@JsonIgnoreProperties(ignoreUnknown = true) // Ignore any unknown fields in JSON
public class TestTypeConfig {
    @JsonProperty("name")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("numOperations")
    private String numOperations;
    @JsonProperty("cycleTime")
    private String cycleTime;
    @JsonProperty("onTime")
    private String onTime;
    @JsonProperty("offTime")
    private String offTime;
    @JsonProperty("pf")
    private String pf;
    @JsonProperty("outputVoltage")
    private String outputVoltage;
    @JsonProperty("outputCurrent")
    private String outputCurrent;
    @JsonProperty("watts")
    private String watts;

    // Default constructor for Jackson
    public TestTypeConfig() {
    }

    public TestTypeConfig(String name, String description, String numOperations, String cycleTime, String onTime,
                          String offTime, String pf, String outputVoltage, String outputCurrent, String watts) {
        this.name = name;
        this.description = description;
        this.numOperations = numOperations;
        this.cycleTime = cycleTime;
        this.onTime = onTime;
        this.offTime = offTime;
        this.pf = pf;
        this.outputVoltage = outputVoltage;
        this.outputCurrent = outputCurrent;
        this.watts = watts;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getNumOperations() {
        return numOperations;
    }

    public String getCycleTime() {
        return cycleTime;
    }

    public String getOnTime() {
        return onTime;
    }

    public String getOffTime() {
        return offTime;
    }

    public String getPf() {
        return pf;
    }

    public String getOutputVoltage() {
        return outputVoltage;
    }

    public String getOutputCurrent() {
        return outputCurrent;
    }

    public String getWatts() {
        return watts;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setNumOperations(String numOperations) {
        this.numOperations = numOperations;
    }

    public void setCycleTime(String cycleTime) {
        this.cycleTime = cycleTime;
    }

    public void setOnTime(String onTime) {
        this.onTime = onTime;
    }

    public void setOffTime(String offTime) {
        this.offTime = offTime;
    }

    public void setPf(String pf) {
        this.pf = pf;
    }

    public void setOutputVoltage(String outputVoltage) {
        this.outputVoltage = outputVoltage;
    }

    public void setOutputCurrent(String outputCurrent) {
        this.outputCurrent = outputCurrent;
    }

    public void setWatts(String watts) {
        this.watts = watts;
    }

    @Override
    public String toString() {
        return name; // This is important for ComboBox to display the name
    }
}
