package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.BookingListResponse;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RiwayatBookingActivity extends AppCompatActivity {
    private static final String TAG = "RiwayatBookingActivity";

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private Button btnRetry;
    private Button btnGoToBooking;
    private ApiService apiService;
    private SessionManager sessionManager;
    private RiwayatBookingAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_riwayat_booking);

        initViews();
        checkAuthentication();
        loadBookings();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        btnRetry = findViewById(R.id.btnRetry);
        btnGoToBooking = findViewById(R.id.btnGoToBooking);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        apiService = ApiClient.getApiService(getApplicationContext());
        sessionManager = new SessionManager(this);

        btnRetry.setOnClickListener(v -> loadBookings());
        btnGoToBooking.setOnClickListener(v -> {
            Intent intent = new Intent(this, BookingActivity.class);
            startActivity(intent);
        });
    }

    private void checkAuthentication() {
        if (!sessionManager.isLoggedIn()) {
            Log.w(TAG, "User not logged in, redirecting to login");
            Toast.makeText(this, "Silakan login terlebih dahulu", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        String token = sessionManager.getToken();
        String userId = sessionManager.getUserId();
        String userName = sessionManager.getUserName();

        Log.d(TAG, "Authentication check - User: " + userName + ", ID: " + userId + ", Token available: " + (token != null && !token.isEmpty()));

        if (token == null || token.isEmpty()) {
            Log.w(TAG, "No authentication token found");
            Toast.makeText(this, "Token tidak valid, silakan login ulang", Toast.LENGTH_SHORT).show();
            sessionManager.logoutUser();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadBookings() {
        Log.d(TAG, "Loading bookings...");
        showLoading(true);
        hideEmptyState();

        // Try user-specific endpoint first
        String userId = sessionManager.getUserId();
        if (userId != null && !userId.isEmpty()) {
            Log.d(TAG, "Trying user-specific endpoint for user: " + userId);
            apiService.getUserBookings(userId).enqueue(new Callback<BookingListResponse>() {
                @Override
                public void onResponse(Call<BookingListResponse> call, Response<BookingListResponse> response) {
                    handleBookingResponse(response, true);
                }

                @Override
                public void onFailure(Call<BookingListResponse> call, Throwable t) {
                    Log.w(TAG, "User-specific endpoint failed, trying general endpoint: " + t.getMessage());
                    tryGeneralBookingsEndpoint();
                }
            });
        } else {
            Log.d(TAG, "No user ID available, trying general endpoint");
            tryGeneralBookingsEndpoint();
        }
    }

    private void tryGeneralBookingsEndpoint() {
        apiService.getBookings().enqueue(new Callback<BookingListResponse>() {
            @Override
            public void onResponse(Call<BookingListResponse> call, Response<BookingListResponse> response) {
                handleBookingResponse(response, false);
            }

            @Override
            public void onFailure(Call<BookingListResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Network error: " + t.getMessage(), t);
                showError("Koneksi bermasalah: " + t.getMessage());
            }
        });
    }

    private void handleBookingResponse(Response<BookingListResponse> response, boolean isUserSpecific) {
        showLoading(false);
        String endpointType = isUserSpecific ? "user-specific" : "general";
        Log.d(TAG, "API Response received from " + endpointType + " endpoint. Code: " + response.code());

        if (response.isSuccessful()) {
            BookingListResponse bookingResponse = response.body();
            if (bookingResponse != null) {
                List<Booking> bookings = bookingResponse.getData();
                if (bookings != null && !bookings.isEmpty()) {
                    Log.d(TAG, "Successfully loaded " + bookings.size() + " bookings from " + endpointType + " endpoint");
                    adapter = new RiwayatBookingAdapter(bookings);
                    recyclerView.setAdapter(adapter);
                    hideEmptyState();
                } else {
                    Log.d(TAG, "No bookings found from " + endpointType + " endpoint");
                    if (isUserSpecific) {
                        // If user-specific endpoint returns empty, try general endpoint
                        Log.d(TAG, "User-specific endpoint returned empty, trying general endpoint");
                        tryGeneralBookingsEndpoint();
                    } else {
                        showEmptyState("Belum ada riwayat booking.\nSilakan buat booking terlebih dahulu.");
                    }
                }
            } else {
                Log.e(TAG, "Response body is null from " + endpointType + " endpoint");
                if (isUserSpecific) {
                    Log.d(TAG, "User-specific endpoint returned null body, trying general endpoint");
                    tryGeneralBookingsEndpoint();
                } else {
                    showError("Data tidak valid dari server");
                }
            }
        } else {
            Log.e(TAG, "API call failed from " + endpointType + " endpoint with code: " + response.code() + ", message: " + response.message());
            if (response.code() == 401) {
                handleUnauthorized();
            } else if (response.code() == 404) {
                if (isUserSpecific) {
                    Log.d(TAG, "User-specific endpoint not found, trying general endpoint");
                    tryGeneralBookingsEndpoint();
                } else {
                    showEmptyState("Endpoint tidak ditemukan");
                }
            } else {
                if (isUserSpecific) {
                    Log.d(TAG, "User-specific endpoint failed, trying general endpoint");
                    tryGeneralBookingsEndpoint();
                } else {
                    showError("Gagal memuat data: " + response.message() + " (Code: " + response.code() + ")");
                }
            }
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void showEmptyState(String message) {
        tvEmptyState.setText(message);
        tvEmptyState.setVisibility(View.VISIBLE);
        btnRetry.setVisibility(View.VISIBLE);
        btnGoToBooking.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyState() {
        tvEmptyState.setVisibility(View.GONE);
        btnRetry.setVisibility(View.GONE);
        btnGoToBooking.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        showEmptyState("Terjadi kesalahan");
    }

    private void handleUnauthorized() {
        Log.w(TAG, "Unauthorized access, clearing session");
        Toast.makeText(this, "Sesi telah berakhir, silakan login ulang", Toast.LENGTH_LONG).show();
        sessionManager.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data ketika user kembali ke halaman ini
        Log.d(TAG, "onResume - refreshing booking data");
        loadBookings();
    }
}