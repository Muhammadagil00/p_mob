package com.example.carwashapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.models.Booking;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class RiwayatBookingAdapter extends RecyclerView.Adapter<RiwayatBookingAdapter.ViewHolder> {

    private List<Booking> bookingList;
    private OnBookingClickListener listener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public RiwayatBookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Booking booking = bookingList.get(position);
        
        // Set service type
        holder.tvServiceType.setText(booking.getServiceType() != null ? booking.getServiceType() : "Service");
        
        // Format and set date
        String dateText = "Tanggal: " + (booking.getDate() != null ? booking.getDate() : "Tidak diketahui");
        holder.tvDate.setText(dateText);
        
        // Set location
        String locationText = "Lokasi: " + (booking.getLocation() != null ? booking.getLocation() : "Tidak diketahui");
        holder.tvLocation.setText(locationText);
        
        // Set status with color
        String status = booking.getStatus() != null ? booking.getStatus() : "pending";
        holder.tvStatus.setText(getStatusText(status));
        holder.tvStatus.setBackgroundColor(getStatusColor(status));
        
        // Set click listener
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBookingClick(booking);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return "Selesai";
            case "in_progress":
                return "Sedang Dikerjakan";
            case "pending":
                return "Menunggu";
            case "cancelled":
                return "Dibatalkan";
            default:
                return "Pending";
        }
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "completed":
                return 0xFF4CAF50; // Green
            case "in_progress":
                return 0xFF2196F3; // Blue
            case "pending":
                return 0xFFFF9800; // Orange
            case "cancelled":
                return 0xFFF44336; // Red
            default:
                return 0xFF9E9E9E; // Gray
        }
    }

    public void updateBookings(List<Booking> newBookings) {
        this.bookingList = newBookings;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceType;
        TextView tvDate;
        TextView tvLocation;
        TextView tvStatus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
