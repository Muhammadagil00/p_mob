package com.example.carwashapp.activities;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.models.Booking;

import java.util.List;

public class RiwayatBookingAdapter extends RecyclerView.Adapter<RiwayatBookingAdapter.ViewHolder> {
    private List<Booking> bookingList;

    public RiwayatBookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
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
        holder.tvServiceType.setText(booking.getServiceType() != null ? booking.getServiceType() : "N/A");

        // Format and set date
        String date = booking.getDate() != null ? formatDate(booking.getDate()) : "N/A";
        holder.tvDate.setText(date);

        // Set location
        holder.tvLocation.setText(booking.getLocation() != null ? booking.getLocation() : "N/A");

        // Set status with appropriate background
        String status = booking.getStatus() != null ? booking.getStatus() : "Pending";
        holder.tvStatus.setText(status);
        setStatusBackground(holder.tvStatus, status);
    }

    private String formatDate(String isoDate) {
        try {
            // Assuming the date is in ISO format, extract just the date part
            if (isoDate.contains("T")) {
                return isoDate.split("T")[0];
            }
            return isoDate;
        } catch (Exception e) {
            return isoDate;
        }
    }

    private void setStatusBackground(TextView statusView, String status) {
        int backgroundRes;
        switch (status.toLowerCase()) {
            case "completed":
            case "selesai":
                backgroundRes = R.drawable.bg_status_completed;
                break;
            case "in progress":
            case "sedang dikerjakan":
            case "progress":
                backgroundRes = R.drawable.bg_status_in_progress;
                break;
            case "waiting":
            case "menunggu":
                backgroundRes = R.drawable.bg_status_waiting;
                break;
            default:
                backgroundRes = R.drawable.bg_status_default;
                break;
        }
        statusView.setBackgroundResource(backgroundRes);
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceType, tvDate, tvLocation, tvStatus;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvServiceType = itemView.findViewById(R.id.tvServiceType);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}