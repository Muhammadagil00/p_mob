package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.BookingListResponse;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminDashboardActivity extends AppCompatActivity {
    private static final String TAG = "AdminDashboard";

    private SessionManager sessionManager;
    private ApiService apiService;
    private TextView tvWelcomeAdmin;
    private TextView tvTotalBookings;
    private Button btnManageServices;
    private Button btnViewAllBookings;
    private Button btnLogout;
    private RecyclerView rvRecentBookings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        initViews();
        checkAdminAccess();
        loadDashboardData();
    }

    private void initViews() {
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        btnManageServices = findViewById(R.id.btnManageServices);
        btnViewAllBookings = findViewById(R.id.btnViewAllBookings);
        btnLogout = findViewById(R.id.btnLogout);
        rvRecentBookings = findViewById(R.id.rvRecentBookings);

        // Set welcome message
        String adminName = sessionManager.getUserName();
        tvWelcomeAdmin.setText("Selamat datang, Admin " + adminName);

        // Setup RecyclerView
        if (rvRecentBookings != null) {
            rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));
        }

        // Set click listeners
        btnManageServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageServicesActivity.class);
            startActivity(intent);
        });

        btnViewAllBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, RiwayatBookingActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void checkAdminAccess() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Akses ditolak. Anda bukan admin.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void loadDashboardData() {
        loadRecentBookings();
    }

    private void loadRecentBookings() {
        apiService.getBookings().enqueue(new Callback<BookingListResponse>() {
            @Override
            public void onResponse(Call<BookingListResponse> call, Response<BookingListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Booking> bookings = response.body().getData();
                    if (bookings != null) {
                        tvTotalBookings.setText("Total Booking: " + bookings.size());

                        // Show only recent 5 bookings
                        List<Booking> recentBookings = bookings.size() > 5 ?
                            bookings.subList(0, 5) : bookings;

                        // For now, just show count - can add adapter later
                        Toast.makeText(AdminDashboardActivity.this,
                            "Loaded " + bookings.size() + " bookings from API", Toast.LENGTH_SHORT).show();
                    } else {
                        tvTotalBookings.setText("Total Booking: 0");
                    }
                } else {
                    Log.e(TAG, "Failed to load bookings: " + response.message());
                    loadDemoBookings();
                }
            }

            @Override
            public void onFailure(Call<BookingListResponse> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                loadDemoBookings();
            }
        });
    }

    private void loadDemoBookings() {
        // Create demo bookings for admin dashboard
        List<Booking> demoBookings = new ArrayList<>();

        Booking booking1 = new Booking();
        booking1.setId("demo_booking_1");
        booking1.setServiceType("Cuci Mobil Basic");
        booking1.setDate("2024-01-15");
        booking1.setLocation("Jakarta Selatan");
        booking1.setStatus("completed");
        demoBookings.add(booking1);

        Booking booking2 = new Booking();
        booking2.setId("demo_booking_2");
        booking2.setServiceType("Cuci Mobil Premium");
        booking2.setDate("2024-01-14");
        booking2.setLocation("Jakarta Pusat");
        booking2.setStatus("in_progress");
        demoBookings.add(booking2);

        Booking booking3 = new Booking();
        booking3.setId("demo_booking_3");
        booking3.setServiceType("Cuci Motor");
        booking3.setDate("2024-01-13");
        booking3.setLocation("Jakarta Utara");
        booking3.setStatus("pending");
        demoBookings.add(booking3);

        // Update total bookings
        tvTotalBookings.setText("Total Booking: " + demoBookings.size() + " (Demo)");

        // For now, just show text instead of RecyclerView
        Toast.makeText(this, "Menampilkan " + demoBookings.size() + " booking demo", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
    }
}
