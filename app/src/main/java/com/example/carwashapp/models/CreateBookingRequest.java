package com.example.carwashapp.models;

public class CreateBookingRequest {
    private String serviceId;
    private String vehicleId;
    private String date; // YYYY-MM-DD format
    private String timeSlot; // e.g., "09:00-10:00"
    private String location;
    private String notes;

    public CreateBookingRequest() {}

    public CreateBookingRequest(String serviceId, String vehicleId, String date, String timeSlot, String location, String notes) {
        this.serviceId = serviceId;
        this.vehicleId = vehicleId;
        this.date = date;
        this.timeSlot = timeSlot;
        this.location = location;
        this.notes = notes;
    }

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

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
