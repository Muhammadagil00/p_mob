package com.example.carwashapp.adapters;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.models.Booking;

import java.util.ArrayList;
import java.util.List;

public class RiwayatBookingAdapter extends RecyclerView.Adapter<RiwayatBookingAdapter.ViewHolder> {
    private static final String TAG = "RiwayatBookingAdapter";

    private List<Booking> bookingList;
    private OnBookingClickListener listener;
    private OnBookingActionListener actionListener;

    public interface OnBookingClickListener {
        void onBookingClick(Booking booking);
    }

    public interface OnBookingActionListener {
        void onCancelBooking(Booking booking);
        void onViewDetails(Booking booking);
    }

    public RiwayatBookingAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList != null ? bookingList : new ArrayList<>();
        Log.d(TAG, "Adapter created with " + this.bookingList.size() + " bookings");
    }

    public void setOnBookingClickListener(OnBookingClickListener listener) {
        this.listener = listener;
    }

    public void setOnBookingActionListener(OnBookingActionListener actionListener) {
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_booking_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            if (position < 0 || position >= bookingList.size()) {
                Log.e(TAG, "Invalid position: " + position + ", list size: " + bookingList.size());
                return;
            }

            Booking booking = bookingList.get(position);
            if (booking == null) {
                Log.e(TAG, "Booking is null at position: " + position);
                return;
            }

            // Set service name (from related service object or serviceId)
            String serviceName = "Layanan";
            try {
                if (booking.getService() != null && booking.getService().getName() != null) {
                    serviceName = booking.getService().getName();
                } else if (booking.getServiceId() != null) {
                    serviceName = "Service ID: " + booking.getServiceId();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting service name: " + e.getMessage());
            }

            if (holder.tvServiceType != null) {
                holder.tvServiceType.setText(serviceName);
            }

            // Format and set date with time slot
            String dateText = "Tanggal: ";
            try {
                dateText += (booking.getDate() != null ? booking.getDate() : "Tidak diketahui");
                if (booking.getTimeSlot() != null) {
                    dateText += " (" + booking.getTimeSlot() + ")";
                }
            } catch (Exception e) {
                Log.e(TAG, "Error formatting date: " + e.getMessage());
                dateText += "Tidak diketahui";
            }

            if (holder.tvDate != null) {
                holder.tvDate.setText(dateText);
            }

            // Set location
            String locationText = "Lokasi: ";
            try {
                locationText += (booking.getLocation() != null ? booking.getLocation() : "Tidak diketahui");
            } catch (Exception e) {
                Log.e(TAG, "Error getting location: " + e.getMessage());
                locationText += "Tidak diketahui";
            }

            if (holder.tvLocation != null) {
                holder.tvLocation.setText(locationText);
            }

            // Set status with color
            try {
                String status = booking.getStatus() != null ? booking.getStatus() : "pending";
                if (holder.tvStatus != null) {
                    holder.tvStatus.setText(getStatusText(status));
                    holder.tvStatus.setBackgroundColor(getStatusColor(status));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting status: " + e.getMessage());
                if (holder.tvStatus != null) {
                    holder.tvStatus.setText("Menunggu");
                    holder.tvStatus.setBackgroundColor(0xFF9E9E9E);
                }
            }

            // Set click listener for item
            holder.itemView.setOnClickListener(v -> {
                try {
                    if (actionListener != null) {
                        actionListener.onViewDetails(booking);
                    } else if (listener != null) {
                        listener.onBookingClick(booking);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in click listener: " + e.getMessage());
                }
            });

            // Set cancel button visibility and click listener
            try {
                String status = booking.getStatus() != null ? booking.getStatus().toLowerCase() : "pending";
                if (holder.btnCancel != null) {
                    if ("pending".equals(status)) {
                        holder.btnCancel.setVisibility(View.VISIBLE);
                        holder.btnCancel.setOnClickListener(v -> {
                            try {
                                if (actionListener != null) {
                                    actionListener.onCancelBooking(booking);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error in cancel button listener: " + e.getMessage());
                            }
                        });
                    } else {
                        holder.btnCancel.setVisibility(View.GONE);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting cancel button: " + e.getMessage());
                if (holder.btnCancel != null) {
                    holder.btnCancel.setVisibility(View.GONE);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error in onBindViewHolder at position " + position + ": " + e.getMessage(), e);
        }
    }

    @Override
    public int getItemCount() {
        return bookingList != null ? bookingList.size() : 0;
    }

    private String getStatusText(String status) {
        switch (status.toLowerCase()) {
            case "done":
                return "Selesai";
            case "processing":
                return "Sedang Dikerjakan";
            case "pending":
                return "Menunggu";
            case "cancelled":
                return "Dibatalkan";
            default:
                return "Menunggu";
        }
    }

    private int getStatusColor(String status) {
        switch (status.toLowerCase()) {
            case "done":
                return 0xFF4CAF50; // Green
            case "processing":
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
        try {
            this.bookingList = newBookings != null ? newBookings : new ArrayList<>();
            Log.d(TAG, "Updating adapter with " + this.bookingList.size() + " bookings");
            notifyDataSetChanged();
        } catch (Exception e) {
            Log.e(TAG, "Error updating bookings: " + e.getMessage(), e);
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvServiceType;
        TextView tvDate;
        TextView tvLocation;
        TextView tvStatus;
        Button btnCancel;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            try {
                tvServiceType = itemView.findViewById(R.id.tvServiceType);
                tvDate = itemView.findViewById(R.id.tvDate);
                tvLocation = itemView.findViewById(R.id.tvLocation);
                tvStatus = itemView.findViewById(R.id.tvStatus);
                btnCancel = itemView.findViewById(R.id.btnCancel);
            } catch (Exception e) {
                Log.e(TAG, "Error initializing ViewHolder: " + e.getMessage(), e);
            }
        }
    }
}
