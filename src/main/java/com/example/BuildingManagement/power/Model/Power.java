package com.example.BuildingManagement.power.Model;

public class Power {

    private String nodeId;
    private double voltage;
    private double current;
    private double power;
    private double energy;

    public Power() {}

    public Power(String nodeId, double voltage, double current, double power, double energy) {
        this.nodeId = nodeId;
        this.voltage = voltage;
        this.current = current;
        this.power = power;
        this.energy = energy;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public double getVoltage() {
        return voltage;
    }

    public void setVoltage(double voltage) {
        this.voltage = voltage;
    }

    public double getCurrent() {
        return current;
    }

    public void setCurrent(double current) {
        this.current = current;
    }

    public double getPower() {
        return power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public double getEnergy() {
        return energy;
    }

    public void setEnergy(double energy) {
        this.energy = energy;
    }

    @Override
    public String toString() {
        return "Power{" +
                "nodeId='" + nodeId + '\'' +
                ", voltage=" + voltage +
                ", current=" + current +
                ", power=" + power +
                ", energy=" + energy +
                '}';
    }
}
