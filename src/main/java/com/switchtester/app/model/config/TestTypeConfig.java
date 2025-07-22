package com.switchtester.app.model.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@JsonIgnoreProperties(ignoreUnknown = true)
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
    
    // Changed from String to List<String> to store multiple selections
    @JsonProperty("outputVoltages")
    private List<String> outputVoltages = new ArrayList<>();
    
    // Changed from String to List<String> to store multiple selections
    @JsonProperty("outputCurrents")
    private List<String> outputCurrents = new ArrayList<>();
    
    @JsonProperty("watts")
    private String watts;

    // Default constructor for Jackson
    public TestTypeConfig() {
    }

    // Constructor updated to accept lists
    public TestTypeConfig(String name, String description, String numOperations, String cycleTime, String onTime,
                          String offTime, String pf, List<String> outputVoltages, List<String> outputCurrents, String watts) {
        this.name = name;
        this.description = description;
        this.numOperations = numOperations;
        this.cycleTime = cycleTime;
        this.onTime = onTime;
        this.offTime = offTime;
        this.pf = pf;
        this.outputVoltages = outputVoltages;
        this.outputCurrents = outputCurrents;
        this.watts = watts;
    }

    // --- Getters and Setters ---

    // Getters and setters for the new lists
    public List<String> getOutputVoltages() {
        return outputVoltages;
    }

    public void setOutputVoltages(List<String> outputVoltages) {
        this.outputVoltages = outputVoltages;
    }

    public List<String> getOutputCurrents() {
        return outputCurrents;
    }

    public void setOutputCurrents(List<String> outputCurrents) {
        this.outputCurrents = outputCurrents;
    }

    // Helper methods for TableView display. @JsonIgnore prevents them from interfering with serialization.
    @JsonIgnore
    public String getOutputVoltage() {
        if (outputVoltages == null || outputVoltages.isEmpty()) return "";
        return String.join(", ", outputVoltages);
    }

    @JsonIgnore
    public String getOutputCurrent() {
        if (outputCurrents == null || outputCurrents.isEmpty()) return "";
        return String.join(", ", outputCurrents);
    }
    
    // Standard Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getNumOperations() { return numOperations; }
    public void setNumOperations(String numOperations) { this.numOperations = numOperations; }
    public String getCycleTime() { return cycleTime; }
    public void setCycleTime(String cycleTime) { this.cycleTime = cycleTime; }
    public String getOnTime() { return onTime; }
    public void setOnTime(String onTime) { this.onTime = onTime; }
    public String getOffTime() { return offTime; }
    public void setOffTime(String offTime) { this.offTime = offTime; }
    public String getPf() { return pf; }
    public void setPf(String pf) { this.pf = pf; }
    public String getWatts() { return watts; }
    public void setWatts(String watts) { this.watts = watts; }

    @Override
    public String toString() {
        return name;
    }
}
