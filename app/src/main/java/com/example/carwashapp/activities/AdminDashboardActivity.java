package com.example.carwashapp.activities;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.adapters.AdminBookingAdapter;
import com.example.carwashapp.adapters.DashboardBookingAdapter;
import com.example.carwashapp.adapters.RiwayatBookingAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.BookingResponse;
import com.example.carwashapp.models.UpdateBookingStatusRequest;
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
    private TextView tvBookingStatus;
    private Button btnManageServices;
    private Button btnViewAllBookings;
    private Button btnLogout;
    private Button btnRefreshDashboard;
    // RecyclerView removed - booking data moved to AdminAllBookingsActivity

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
        apiService = ApiClient.getApiService();

        tvWelcomeAdmin = findViewById(R.id.tvWelcomeAdmin);
        tvTotalBookings = findViewById(R.id.tvTotalBookings);
        tvBookingStatus = findViewById(R.id.tvBookingStatus);
        btnManageServices = findViewById(R.id.btnManageServices);
        btnViewAllBookings = findViewById(R.id.btnViewAllBookings);
        btnLogout = findViewById(R.id.btnLogout);
        btnRefreshDashboard = findViewById(R.id.btnRefreshDashboard);
        // rvRecentBookings removed from dashboard - data moved to AdminAllBookingsActivity

        // Set welcome message
        String adminName = sessionManager.getUserName();
        tvWelcomeAdmin.setText("Selamat datang, Admin " + adminName);

        // RecyclerView removed - booking data moved to AdminAllBookingsActivity

        // Set click listeners
        btnManageServices.setOnClickListener(v -> {
            Intent intent = new Intent(this, ManageServicesActivity.class);
            startActivity(intent);
        });

        btnViewAllBookings.setOnClickListener(v -> {
            Intent intent = new Intent(this, AdminAllBookingsActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> logout());

        btnRefreshDashboard.setOnClickListener(v -> {
            Log.d(TAG, "🔄 Manual refresh triggered by admin");
            tvBookingStatus.setText("Memuat ulang data...");
            refreshDashboardData();
        });
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
        Log.d(TAG, "=== LOADING ADMIN DASHBOARD BOOKINGS ===");

        try {
            // Debug session info
            debugSessionInfo();

            String token = sessionManager.getToken();
            if (token == null) {
                Log.w(TAG, "❌ Token tidak tersedia untuk admin");
                runOnUiThread(() -> {
                    tvTotalBookings.setText("Total Booking: 0 (No Token)");
                    Toast.makeText(AdminDashboardActivity.this,
                        "❌ Token tidak valid. Silakan login ulang.", Toast.LENGTH_LONG).show();
                });
                return;
            }

            // Debug API call info
            Log.d(TAG, "=== ADMIN DASHBOARD API DEBUG ===");
            Log.d(TAG, "🌐 Base URL: " + ApiClient.getBaseUrl());
            Log.d(TAG, "🎯 Full URL: " + ApiClient.getBaseUrl() + "api/admin/bookings");
            Log.d(TAG, "🔑 Token length: " + token.length());
            Log.d(TAG, "🔑 Token preview: " + (token.length() > 20 ? token.substring(0, 20) + "..." : token));
            Log.d(TAG, "👤 User Role: " + sessionManager.getUserRole());
            Log.d(TAG, "🔐 Is Admin: " + sessionManager.isAdmin());

            // Validate admin status
            if (!sessionManager.isAdmin()) {
                Log.w(TAG, "⚠️ User is not admin but accessing admin dashboard");
                runOnUiThread(() -> {
                    Toast.makeText(AdminDashboardActivity.this,
                        "⚠️ Akses admin diperlukan", Toast.LENGTH_LONG).show();
                });
            }

            // Use admin nested endpoint to get all bookings for dashboard
            Log.d(TAG, "🚀 Calling admin nested bookings API for dashboard");
            apiService.getAdminBookingsNested(ApiClient.createAuthHeader(token)).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                Log.d(TAG, "📥 Admin Nested Dashboard API Response Code: " + response.code());

                // Ensure UI updates happen on main thread
                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            BookingResponse bookingResponse = response.body();
                            if (bookingResponse.isSuccess() && bookingResponse.getData() != null &&
                                bookingResponse.getData().getBookings() != null) {

                                List<Booking> bookings = bookingResponse.getData().getBookings();
                                Log.d(TAG, "✅ Admin Dashboard: Berhasil memuat " + bookings.size() + " booking dari admin nested API");

                                // Update total bookings with real data (no demo text)
                                tvTotalBookings.setText("Total Booking: " + bookings.size());

                                // Update status info only (no booking list on dashboard)
                                updateBookingStatusInfo(bookings);

                                // Show success message
                                Toast.makeText(AdminDashboardActivity.this,
                                    "✅ Dashboard memuat " + bookings.size() + " booking. Klik 'Lihat Semua' untuk detail.", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "⚠️ Admin nested API response not successful: " + bookingResponse.getMessage());
                                tvTotalBookings.setText("Total Booking: 0");
                                updateBookingStatusInfo(new ArrayList<>());
                                Toast.makeText(AdminDashboardActivity.this,
                                    "⚠️ Tidak ada booking ditemukan", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.e(TAG, "❌ Admin Nested HTTP Error " + response.code() + ": " + response.message());

                            // Try to read error body for more details
                            String errorDetails = "";
                            try {
                                if (response.errorBody() != null) {
                                    errorDetails = response.errorBody().string();
                                    Log.e(TAG, "🚨 Admin Nested Error Body: " + errorDetails);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading admin nested error body: " + e.getMessage());
                            }

                            // Try fallback endpoint with user nested structure
                            Log.w(TAG, "🔄 Admin nested endpoint failed, trying user nested fallback...");
                            tryUserBookingsForDashboard();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception in admin nested onResponse UI update: " + e.getMessage(), e);
                        showDashboardError("Error memproses admin response");
                    }
                });
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e(TAG, "=== ❌ ADMIN NESTED API NETWORK ERROR ===");
                Log.e(TAG, "Error Type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error Message: " + t.getMessage());
                Log.e(TAG, "Cause: " + (t.getCause() != null ? t.getCause().getMessage() : "null"));

                // Check specific error types
                String errorType = "Network Error";
                if (t instanceof java.net.UnknownHostException) {
                    errorType = "DNS Error";
                    Log.e(TAG, "🌐 DNS Resolution failed - Check internet connection");
                } else if (t instanceof java.net.ConnectException) {
                    errorType = "Connection Error";
                    Log.e(TAG, "🔌 Connection failed - Server might be down");
                } else if (t instanceof java.net.SocketTimeoutException) {
                    errorType = "Timeout Error";
                    Log.e(TAG, "⏰ Request timeout - Server too slow");
                } else if (t instanceof javax.net.ssl.SSLException) {
                    errorType = "SSL Error";
                    Log.e(TAG, "🔒 SSL/TLS error - Certificate issue");
                } else if (t instanceof IllegalStateException) {
                    errorType = "State Error";
                    Log.e(TAG, "🚨 IllegalStateException - UI thread issue");
                } else if (t instanceof com.google.gson.JsonSyntaxException) {
                    errorType = "JSON Parse Error";
                    Log.e(TAG, "📝 JSON parsing failed - Response structure mismatch");
                }

                // Try fallback to user nested bookings endpoint
                Log.w(TAG, "🔄 Admin nested endpoint failed, trying user nested endpoint as fallback...");
                tryUserBookingsForDashboard();
            }
        });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception in loadRecentBookings: " + e.getMessage(), e);
            runOnUiThread(() -> {
                showDashboardError("Error loading bookings: " + e.getMessage());
            });
        }
    }

    private void debugSessionInfo() {
        try {
            Log.d(TAG, "=== SESSION DEBUG INFO ===");
            Log.d(TAG, "Is Logged In: " + sessionManager.isLoggedIn());
            Log.d(TAG, "Is Admin: " + sessionManager.isAdmin());
            Log.d(TAG, "User ID: " + sessionManager.getUserId());
            Log.d(TAG, "User Name: " + sessionManager.getUserName());
            Log.d(TAG, "User Role: " + sessionManager.getUserRole());
            Log.d(TAG, "Token Available: " + (sessionManager.getToken() != null));

            // Additional debugging
            String token = sessionManager.getToken();
            if (token != null) {
                Log.d(TAG, "Token starts with: " + (token.length() > 10 ? token.substring(0, 10) + "..." : token));
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in debugSessionInfo: " + e.getMessage(), e);
        }
    }

    private void tryUserBookingsForDashboard() {
        Log.d(TAG, "=== 🔄 FALLBACK: Trying Nested User Bookings for Dashboard ===");
        String token = sessionManager.getToken();
        if (token == null) {
            Log.e(TAG, "❌ Token is null in fallback");
            runOnUiThread(() -> {
                tvTotalBookings.setText("Total Booking: 0 (No Token)");
                Toast.makeText(AdminDashboardActivity.this,
                    "❌ Token tidak valid. Silakan login ulang.", Toast.LENGTH_LONG).show();
            });
            return;
        }

        // Try nested user bookings endpoint (matches server response structure)
        Log.d(TAG, "🎯 Using nested endpoint: getUserBookingsNested");
        apiService.getUserBookingsNested(ApiClient.createAuthHeader(token)).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                Log.d(TAG, "📥 Nested Bookings Fallback Response Code: " + response.code());

                runOnUiThread(() -> {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            BookingResponse bookingResponse = response.body();
                            if (bookingResponse.isSuccess() && bookingResponse.getData() != null &&
                                bookingResponse.getData().getBookings() != null) {

                                List<Booking> bookings = bookingResponse.getData().getBookings();
                                Log.d(TAG, "✅ Nested Fallback: Berhasil memuat " + bookings.size() + " booking");

                                tvTotalBookings.setText("Total Booking: " + bookings.size());
                                updateBookingStatusInfo(bookings);

                                Toast.makeText(AdminDashboardActivity.this,
                                    "✅ Dashboard memuat " + bookings.size() + " booking. Klik 'Lihat Semua' untuk detail.", Toast.LENGTH_SHORT).show();
                            } else {
                                Log.w(TAG, "⚠️ Nested fallback API response not successful");
                                showDashboardError("Tidak ada booking ditemukan");
                            }
                        } else {
                            Log.e(TAG, "❌ Nested fallback HTTP Error " + response.code());

                            // Try to read error body
                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "🚨 Nested Error Body: " + errorBody);
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error reading nested error body: " + e.getMessage());
                            }

                            showDashboardError("Error server: " + response.code());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception in nested fallback UI update: " + e.getMessage(), e);
                        showDashboardError("Error memproses data nested");
                    }
                });
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e(TAG, "❌ Nested fallback network error: " + t.getMessage());
                runOnUiThread(() -> {
                    try {
                        showDashboardError("Network Error (Nested): " + t.getMessage());
                    } catch (Exception e) {
                        Log.e(TAG, "❌ Exception in nested fallback error handling: " + e.getMessage(), e);
                    }
                });
            }
        });
    }

    private void showDashboardError(String message) {
        try {
            tvTotalBookings.setText("Total Booking: 0 (Error)");
            if (tvBookingStatus != null) {
                tvBookingStatus.setText("❌ Error: " + message);
            }
            Toast.makeText(AdminDashboardActivity.this,
                "❌ " + message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "❌ Exception in showDashboardError: " + e.getMessage(), e);
        }
    }

    // setupRecentBookingsWithActions method removed - booking data moved to AdminAllBookingsActivity

    private void updateBookingStatusInfo(List<Booking> bookings) {
        if (tvBookingStatus == null) return;

        if (bookings.isEmpty()) {
            tvBookingStatus.setText("📋 Belum ada booking dari users");
            return;
        }

        // Count bookings by status
        int pending = 0, processing = 0, done = 0, cancelled = 0;

        for (Booking booking : bookings) {
            String status = booking.getStatus() != null ? booking.getStatus().toLowerCase() : "pending";
            switch (status) {
                case "pending": pending++; break;
                case "processing": processing++; break;
                case "done": done++; break;
                case "cancelled": cancelled++; break;
            }
        }

        // Create status summary
        StringBuilder statusText = new StringBuilder();
        statusText.append("📊 Total: ").append(bookings.size()).append(" booking");

        if (pending > 0) statusText.append(" | ⏳ Pending: ").append(pending);
        if (processing > 0) statusText.append(" | 🔄 Processing: ").append(processing);
        if (done > 0) statusText.append(" | ✅ Done: ").append(done);
        if (cancelled > 0) statusText.append(" | ❌ Cancelled: ").append(cancelled);

        tvBookingStatus.setText(statusText.toString());
    }

    private void refreshDashboardData() {
        Log.d(TAG, "🔄 Refreshing admin dashboard data...");
        if (tvBookingStatus != null) {
            tvBookingStatus.setText("🔄 Memuat ulang data...");
        }
        loadDashboardData();
    }

    // Admin action methods moved to AdminAllBookingsActivity

    private void updateBookingStatusFromDashboard(Booking booking, String newStatus) {
        Log.d(TAG, "=== UPDATING BOOKING STATUS FROM DASHBOARD ===");
        Log.d(TAG, "Booking ID: " + booking.getId());
        Log.d(TAG, "Current Status: " + booking.getStatus());
        Log.d(TAG, "New Status: " + newStatus);

        String token = sessionManager.getToken();
        if (token == null) {
            Log.e(TAG, "❌ Token is null, cannot update status");
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Mengupdate status booking...", Toast.LENGTH_SHORT).show();

        // Create request body
        UpdateBookingStatusRequest request = new UpdateBookingStatusRequest(newStatus);

        // Call admin endpoint for status update
        apiService.updateBookingStatus(ApiClient.createAuthHeader(token), booking.getId(), request)
            .enqueue(new Callback<ApiResponse<Booking>>() {
                @Override
                public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                    Log.d(TAG, "📥 Update Status Response Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Booking> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            // Update local data and refresh dashboard
                            booking.setStatus(newStatus);

                            String statusText = getStatusDisplayText(newStatus);
                            Toast.makeText(AdminDashboardActivity.this,
                                "✅ Status booking berhasil diubah ke " + statusText, Toast.LENGTH_SHORT).show();

                            // Refresh dashboard data
                            loadDashboardData();
                        } else {
                            Log.e(TAG, "❌ API Error: " + apiResponse.getMessage());
                            Toast.makeText(AdminDashboardActivity.this,
                                "❌ Gagal mengupdate status: " + apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.e(TAG, "❌ HTTP Error " + response.code() + ": " + response.message());
                        Toast.makeText(AdminDashboardActivity.this,
                            "❌ Error server: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                    Log.e(TAG, "❌ Network error: " + t.getMessage());
                    Toast.makeText(AdminDashboardActivity.this,
                        "❌ Error jaringan: " + t.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
    }

    private String getStatusDisplayText(String status) {
        switch (status.toLowerCase()) {
            case "pending": return "Menunggu Konfirmasi";
            case "processing": return "Sedang Dikerjakan";
            case "done": return "Cucian Selesai";
            case "cancelled": return "Dibatalkan";
            default: return "Status Tidak Diketahui";
        }
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
