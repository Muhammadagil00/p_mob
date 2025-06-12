package com.example.carwashapp.models;

public class Vehicle {
    private String id;
    private String userId;
    private String brand;
    private String model;
    private int year;
    private String color;
    private String plateNumber;
    private String vehicleType; // "car" or "motorcycle"
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    public Vehicle() {}

    public Vehicle(String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        this.brand = brand;
        this.model = model;
        this.year = year;
        this.color = color;
        this.plateNumber = plateNumber;
        this.vehicleType = vehicleType;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public String getPlateNumber() { return plateNumber; }
    public void setPlateNumber(String plateNumber) { this.plateNumber = plateNumber; }

    public String getVehicleType() { return vehicleType; }
    public void setVehicleType(String vehicleType) { this.vehicleType = vehicleType; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return brand + " " + model + " (" + plateNumber + ")";
    }

    public void setLicensePlate(String s) {

    }
}
