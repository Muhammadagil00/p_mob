package com.example.carwashapp.models;

import java.util.List;

public class BookingListResponse {
    private List<Booking> data;
    public List<Booking> getData() { return data; }
    public void setData(List<Booking> data) { this.data = data; }
} 