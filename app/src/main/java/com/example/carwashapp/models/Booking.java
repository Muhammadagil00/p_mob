package com.example.carwashapp.models;

public class Booking {
    private String id;
    private String userId;
    private String serviceId;
    private String vehicleId;
    private String date;
    private String timeSlot;
    private String location;
    private String status; // "pending", "processing", "done", "cancelled"
    private String notes;
    private String createdAt;
    private String updatedAt;

    // Related objects (populated by API)
    private Service service;
    private Vehicle vehicle;
    private User user;

    public Booking() {}

    public Booking(String serviceId, String vehicleId, String date, String timeSlot, String location, String notes) {
        this.serviceId = serviceId;
        this.vehicleId = vehicleId;
        this.date = date;
        this.timeSlot = timeSlot;
        this.location = location;
        this.notes = notes;
    }

    // Getters & Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getServiceId() { return serviceId; }
    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getVehicleId() { return vehicleId; }
    public void setVehicleId(String vehicleId) { this.vehicleId = vehicleId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getTimeSlot() { return timeSlot; }
    public void setTimeSlot(String timeSlot) { this.timeSlot = timeSlot; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public Service getService() { return service; }
    public void setService(Service service) { this.service = service; }

    public Vehicle getVehicle() { return vehicle; }
    public void setVehicle(Vehicle vehicle) { this.vehicle = vehicle; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}