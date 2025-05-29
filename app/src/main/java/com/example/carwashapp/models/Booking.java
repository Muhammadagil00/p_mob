package com.example.carwashapp.models;

public class Booking {
    private String id;
    private String serviceType;
    private String date; // ISO 8601 String
    private String location;
    private String status;

    public Booking() {}

    public Booking(String serviceType, String date, String location) {
        this.serviceType = serviceType;
        this.date = date;
        this.location = location;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}