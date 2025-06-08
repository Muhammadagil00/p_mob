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
import com.example.carwashapp.adapters.RiwayatBookingAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Booking;
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
        apiService = ApiClient.getApiService();
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
        Log.d(TAG, "Memuat booking...");
        showLoading(true);
        hideEmptyState();

        String token = sessionManager.getToken();
        if (token == null || token.isEmpty()) {
            Log.w(TAG, "Token tidak tersedia");
            showError("Token tidak valid, silakan login ulang");
            return;
        }

        Log.d(TAG, "Menggunakan endpoint user bookings dengan token");
        apiService.getUserBookings(ApiClient.createAuthHeader(token)).enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                showLoading(false);
                Log.d(TAG, "Response diterima. Code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> bookings = apiResponse.getData();
                        if (!bookings.isEmpty()) {
                            Log.d(TAG, "Berhasil memuat " + bookings.size() + " booking");
                            adapter = new RiwayatBookingAdapter(bookings);
                            recyclerView.setAdapter(adapter);
                            hideEmptyState();
                        } else {
                            Log.d(TAG, "Tidak ada booking ditemukan");
                            showEmptyState("Belum ada riwayat booking.\nSilakan buat booking terlebih dahulu.");
                        }
                    } else {
                        Log.e(TAG, "API mengembalikan error: " + apiResponse.getError());
                        showError("Gagal memuat data: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "API call gagal dengan code: " + response.code() + ", message: " + response.message());
                    if (response.code() == 401) {
                        handleUnauthorized();
                    } else {
                        showError("Gagal memuat data: " + response.message() + " (Code: " + response.code() + ")");
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error jaringan: " + t.getMessage(), t);
                showError("Koneksi bermasalah: " + t.getMessage());
            }
        });
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
        Log.w(TAG, "Akses tidak diizinkan, membersihkan sesi");
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
        Log.d(TAG, "onResume - memuat ulang data booking");
        loadBookings();
    }
}