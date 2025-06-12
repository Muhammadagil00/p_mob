package com.example.carwashapp.activities;

import android.app.AlertDialog;
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
import com.example.carwashapp.models.ApiResponseWrapper;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.BookingResponse;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import okhttp3.ResponseBody;

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

        try {
            showLoading(true);
            hideEmptyState();

            String token = sessionManager.getToken();
            if (token == null || token.isEmpty()) {
                Log.w(TAG, "Token tidak tersedia");
                showError("Token tidak valid, silakan login ulang");
                return;
            }

            Log.d(TAG, "Menggunakan endpoint user bookings dengan token");

            // Try nested response endpoint first (matches your backend structure)
            loadBookingsNested(token);
        } catch (Exception e) {
            Log.e(TAG, "Error saat memuat booking: " + e.getMessage(), e);
            showLoading(false);
            showError("Terjadi kesalahan saat memuat data");
        }
    }

    private void loadBookingsNested(String token) {
        Log.d(TAG, "=== LOADING BOOKINGS WITH NESTED RESPONSE ===");

        apiService.getUserBookingsNested(ApiClient.createAuthHeader(token)).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(Call<BookingResponse> call, Response<BookingResponse> response) {
                try {
                    showLoading(false);
                    Log.d(TAG, "Nested response received. Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        BookingResponse bookingResponse = response.body();
                        Log.d(TAG, "BookingResponse: " + bookingResponse.toString());

                        if (bookingResponse.isSuccess() && bookingResponse.getData() != null) {
                            BookingResponse.BookingData data = bookingResponse.getData();
                            List<Booking> bookings = data.getBookings();

                            if (bookings != null && !bookings.isEmpty()) {
                                Log.d(TAG, "Successfully loaded " + bookings.size() + " bookings from nested response");

                                // Log pagination info
                                if (data.getPagination() != null) {
                                    Log.d(TAG, "Pagination: " + data.getPagination().toString());
                                }

                                displayBookings(bookings);
                            } else {
                                Log.d(TAG, "No bookings found in nested response");
                                showEmptyState("Belum ada riwayat booking.\nSilakan buat booking terlebih dahulu.");
                            }
                        } else {
                            Log.e(TAG, "Nested response not successful: " + bookingResponse.getMessage());
                            // Fallback to flexible parsing
                            loadBookingsWithFlexibleParsing(token);
                        }
                    } else {
                        Log.e(TAG, "Nested API call failed with code: " + response.code());
                        handleErrorResponse(response, token);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in nested response parsing: " + e.getMessage(), e);
                    // Fallback to flexible parsing
                    loadBookingsWithFlexibleParsing(token);
                }
            }

            @Override
            public void onFailure(Call<BookingResponse> call, Throwable t) {
                Log.e(TAG, "Nested endpoint failed: " + t.getMessage(), t);
                // Fallback to flexible parsing
                loadBookingsWithFlexibleParsing(token);
            }
        });
    }

    private void loadBookingsWithFlexibleParsing(String token) {
        Log.d(TAG, "=== LOADING BOOKINGS WITH FLEXIBLE PARSING ===");

        apiService.getUserBookingsRaw(ApiClient.createAuthHeader(token)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    showLoading(false);
                    Log.d(TAG, "Raw response received. Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Raw JSON response: " + jsonResponse);

                        // Try multiple parsing strategies
                        List<Booking> bookings = parseBookingsFlexibly(jsonResponse);

                        if (bookings != null && !bookings.isEmpty()) {
                            Log.d(TAG, "Successfully parsed " + bookings.size() + " bookings");
                            displayBookings(bookings);
                        } else {
                            Log.d(TAG, "No bookings found after flexible parsing");
                            showEmptyState("Belum ada riwayat booking.\nSilakan buat booking terlebih dahulu.");
                        }
                    } else {
                        Log.e(TAG, "Raw API call failed with code: " + response.code());
                        handleErrorResponse(response, token);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error in flexible parsing: " + e.getMessage(), e);
                    // Fallback to original endpoint
                    loadBookingsOriginal(token);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Raw endpoint failed: " + t.getMessage(), t);
                // Fallback to original endpoint
                loadBookingsOriginal(token);
            }
        });
    }

    private List<Booking> parseBookingsFlexibly(String jsonResponse) {
        Log.d(TAG, "Attempting flexible parsing of bookings");

        try {
            // Strategy 1: Use ApiResponseWrapper
            List<Booking> bookings = ApiResponseWrapper.parseListResponse(jsonResponse, Booking.class);
            if (bookings != null && !bookings.isEmpty()) {
                Log.d(TAG, "Strategy 1 (ApiResponseWrapper) successful: " + bookings.size() + " bookings");
                return bookings;
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 1 failed: " + e.getMessage());
        }

        try {
            // Strategy 2: Direct array parsing
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.reflect.TypeToken<List<Booking>> typeToken = new com.google.gson.reflect.TypeToken<List<Booking>>() {};
            List<Booking> bookings = gson.fromJson(jsonResponse, typeToken.getType());
            if (bookings != null && !bookings.isEmpty()) {
                Log.d(TAG, "Strategy 2 (Direct array) successful: " + bookings.size() + " bookings");
                return bookings;
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 2 failed: " + e.getMessage());
        }

        try {
            // Strategy 3: Parse as single object and convert to list
            com.google.gson.Gson gson = new com.google.gson.Gson();
            Booking singleBooking = gson.fromJson(jsonResponse, Booking.class);
            if (singleBooking != null && singleBooking.getId() != null) {
                Log.d(TAG, "Strategy 3 (Single object) successful");
                List<Booking> bookings = new java.util.ArrayList<>();
                bookings.add(singleBooking);
                return bookings;
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 3 failed: " + e.getMessage());
        }

        try {
            // Strategy 4: Parse nested data.bookings field (your backend structure)
            com.google.gson.Gson gson = new com.google.gson.Gson();
            com.google.gson.JsonObject jsonObject = gson.fromJson(jsonResponse, com.google.gson.JsonObject.class);

            if (jsonObject.has("data")) {
                com.google.gson.JsonElement dataElement = jsonObject.get("data");
                if (dataElement.isJsonObject()) {
                    com.google.gson.JsonObject dataObject = dataElement.getAsJsonObject();
                    if (dataObject.has("bookings")) {
                        com.google.gson.JsonElement bookingsElement = dataObject.get("bookings");
                        if (bookingsElement.isJsonArray()) {
                            com.google.gson.reflect.TypeToken<List<Booking>> typeToken = new com.google.gson.reflect.TypeToken<List<Booking>>() {};
                            List<Booking> bookings = gson.fromJson(bookingsElement, typeToken.getType());
                            if (bookings != null) {
                                Log.d(TAG, "Strategy 4 (Nested data.bookings) successful: " + bookings.size() + " bookings");
                                return bookings;
                            }
                        }
                    }
                } else if (dataElement.isJsonArray()) {
                    // Fallback: data is direct array
                    com.google.gson.reflect.TypeToken<List<Booking>> typeToken = new com.google.gson.reflect.TypeToken<List<Booking>>() {};
                    List<Booking> bookings = gson.fromJson(dataElement, typeToken.getType());
                    if (bookings != null) {
                        Log.d(TAG, "Strategy 4 (Nested data array) successful: " + bookings.size() + " bookings");
                        return bookings;
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 4 failed: " + e.getMessage());
        }

        try {
            // Strategy 5: Parse as BookingResponse (complete structure)
            com.google.gson.Gson gson = new com.google.gson.Gson();
            BookingResponse bookingResponse = gson.fromJson(jsonResponse, BookingResponse.class);
            if (bookingResponse != null && bookingResponse.isSuccess() &&
                bookingResponse.getData() != null && bookingResponse.getData().getBookings() != null) {
                List<Booking> bookings = bookingResponse.getData().getBookings();
                Log.d(TAG, "Strategy 5 (BookingResponse) successful: " + bookings.size() + " bookings");
                return bookings;
            }
        } catch (Exception e) {
            Log.w(TAG, "Strategy 5 failed: " + e.getMessage());
        }

        Log.e(TAG, "All parsing strategies failed");
        return new java.util.ArrayList<>();
    }

    private void displayBookings(List<Booking> bookings) {
        try {
            if (recyclerView != null) {
                adapter = new RiwayatBookingAdapter(bookings);

                // Set action listener for cancel and view details
                adapter.setOnBookingActionListener(new RiwayatBookingAdapter.OnBookingActionListener() {
                    @Override
                    public void onCancelBooking(Booking booking) {
                        showCancelConfirmation(booking);
                    }

                    @Override
                    public void onViewDetails(Booking booking) {
                        showBookingDetails(booking);
                    }
                });

                recyclerView.setAdapter(adapter);
                hideEmptyState();
                Log.d(TAG, "Bookings displayed successfully");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error displaying bookings: " + e.getMessage(), e);
            showError("Terjadi kesalahan saat menampilkan data");
        }
    }

    private void handleErrorResponse(Response<?> response, String token) {
        try {
            if (response.code() == 401) {
                handleUnauthorized();
            } else {
                String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                Log.e(TAG, "Error response body: " + errorBody);

                // Try flexible parsing as fallback
                loadBookingsWithFlexibleParsing(token);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling error response: " + e.getMessage(), e);
            loadBookingsOriginal(token);
        }
    }

    private void loadBookingsOriginal(String token) {
        Log.d(TAG, "Using original endpoint as fallback");

        apiService.getUserBookings(ApiClient.createAuthHeader(token)).enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                try {
                    showLoading(false);
                    Log.d(TAG, "Original response received. Code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<List<Booking>> apiResponse = response.body();
                        if (apiResponse != null && apiResponse.isSuccess() && apiResponse.getData() != null) {
                            List<Booking> bookings = apiResponse.getData();
                            if (bookings != null && !bookings.isEmpty()) {
                                Log.d(TAG, "Successfully loaded " + bookings.size() + " bookings");

                                if (recyclerView != null) {
                                    adapter = new RiwayatBookingAdapter(bookings);
                                    recyclerView.setAdapter(adapter);
                                    hideEmptyState();
                                }
                            } else {
                                Log.d(TAG, "No bookings found");
                                showEmptyState("Belum ada riwayat booking.\nSilakan buat booking terlebih dahulu.");
                            }
                        } else {
                            String errorMsg = apiResponse != null ? apiResponse.getError() : "Response tidak valid";
                            Log.e(TAG, "API returned error: " + errorMsg);
                            showError("Gagal memuat data: " + errorMsg);
                        }
                    } else {
                        Log.e(TAG, "Original API call failed with code: " + response.code() + ", message: " + response.message());
                        if (response.code() == 401) {
                            handleUnauthorized();
                        } else {
                            showError("Gagal memuat data: " + response.message() + " (Code: " + response.code() + ")");
                        }
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing original response: " + e.getMessage(), e);
                    showError("Terjadi kesalahan saat memproses data");
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                try {
                    showLoading(false);
                    Log.e(TAG, "Original endpoint network error: " + t.getMessage(), t);
                    showError("Koneksi bermasalah: " + (t.getMessage() != null ? t.getMessage() : "Tidak diketahui"));
                } catch (Exception e) {
                    Log.e(TAG, "Error handling original failure: " + e.getMessage(), e);
                }
            }
        });
    }

    private void showCancelConfirmation(Booking booking) {
        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Batalkan Booking");
            builder.setMessage("Apakah Anda yakin ingin membatalkan booking ini?\n\n" +
                    "Service: " + (booking.getService() != null ? booking.getService().getName() : "Unknown") + "\n" +
                    "Tanggal: " + booking.getDate() + "\n" +
                    "Lokasi: " + booking.getLocation());

            builder.setPositiveButton("Ya, Batalkan", (dialog, which) -> {
                cancelBooking(booking);
            });

            builder.setNegativeButton("Tidak", (dialog, which) -> {
                dialog.dismiss();
            });

            builder.show();
        } catch (Exception e) {
            Log.e(TAG, "Error showing cancel confirmation: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
        }
    }

    private void cancelBooking(Booking booking) {
        try {
            Log.d(TAG, "Cancelling booking: " + booking.getId());

            String token = sessionManager.getToken();
            if (token == null) {
                Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
                return;
            }

            // Show loading
            showLoading(true);

            apiService.cancelBooking(ApiClient.createAuthHeader(token), booking.getId()).enqueue(new Callback<ApiResponse<Booking>>() {
                @Override
                public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                    showLoading(false);

                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse<Booking> apiResponse = response.body();
                        if (apiResponse.isSuccess()) {
                            Toast.makeText(RiwayatBookingActivity.this, "Booking berhasil dibatalkan", Toast.LENGTH_SHORT).show();
                            Log.d(TAG, "Booking cancelled successfully");

                            // Reload bookings
                            loadBookings();
                        } else {
                            String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gagal membatalkan booking";
                            Toast.makeText(RiwayatBookingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Cancel booking API error: " + errorMessage);
                        }
                    } else {
                        String message = "Gagal membatalkan booking";
                        if (response.code() == 400) {
                            message = "Booking tidak dapat dibatalkan";
                        } else if (response.code() == 404) {
                            message = "Booking tidak ditemukan";
                        }
                        Toast.makeText(RiwayatBookingActivity.this, message, Toast.LENGTH_SHORT).show();
                        Log.e(TAG, "Cancel booking failed with code: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                    showLoading(false);
                    Toast.makeText(RiwayatBookingActivity.this, "Error jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Cancel booking network error: " + t.getMessage(), t);
                }
            });

        } catch (Exception e) {
            showLoading(false);
            Log.e(TAG, "Error cancelling booking: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan saat membatalkan booking", Toast.LENGTH_SHORT).show();
        }
    }

    private void showBookingDetails(Booking booking) {
        try {
            StringBuilder details = new StringBuilder();
            details.append("Detail Booking\n\n");
            details.append("ID: ").append(booking.getId()).append("\n");

            if (booking.getService() != null) {
                details.append("Service: ").append(booking.getService().getName()).append("\n");
                details.append("Harga: Rp ").append(String.format("%,d", booking.getService().getPrice())).append("\n");
                details.append("Durasi: ").append(booking.getService().getDuration()).append(" menit\n");
            }

            if (booking.getVehicle() != null) {
                details.append("Kendaraan: ").append(booking.getVehicle().getBrand())
                       .append(" ").append(booking.getVehicle().getModel())
                       .append(" (").append(booking.getVehicle().getPlateNumber()).append(")\n");
            }

            details.append("Tanggal: ").append(booking.getDate()).append("\n");
            details.append("Waktu: ").append(booking.getTimeSlot()).append("\n");
            details.append("Lokasi: ").append(booking.getLocation()).append("\n");
            details.append("Status: ").append(booking.getStatus()).append("\n");

            if (booking.getNotes() != null && !booking.getNotes().isEmpty()) {
                details.append("Catatan: ").append(booking.getNotes()).append("\n");
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Detail Booking");
            builder.setMessage(details.toString());
            builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
            builder.show();

        } catch (Exception e) {
            Log.e(TAG, "Error showing booking details: " + e.getMessage(), e);
            Toast.makeText(this, "Terjadi kesalahan saat menampilkan detail", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLoading(boolean show) {
        try {
            if (progressBar != null) {
                progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat mengubah loading state: " + e.getMessage(), e);
        }
    }

    private void showEmptyState(String message) {
        try {
            if (tvEmptyState != null) {
                tvEmptyState.setText(message != null ? message : "Tidak ada data");
                tvEmptyState.setVisibility(View.VISIBLE);
            }
            if (btnRetry != null) {
                btnRetry.setVisibility(View.VISIBLE);
            }
            if (btnGoToBooking != null) {
                btnGoToBooking.setVisibility(View.VISIBLE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat menampilkan empty state: " + e.getMessage(), e);
        }
    }

    private void hideEmptyState() {
        try {
            if (tvEmptyState != null) {
                tvEmptyState.setVisibility(View.GONE);
            }
            if (btnRetry != null) {
                btnRetry.setVisibility(View.GONE);
            }
            if (btnGoToBooking != null) {
                btnGoToBooking.setVisibility(View.GONE);
            }
            if (recyclerView != null) {
                recyclerView.setVisibility(View.VISIBLE);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saat menyembunyikan empty state: " + e.getMessage(), e);
        }
    }

    private void showError(String message) {
        try {
            String errorMessage = message != null ? message : "Terjadi kesalahan";
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
            showEmptyState("Terjadi kesalahan");
        } catch (Exception e) {
            Log.e(TAG, "Error saat menampilkan error: " + e.getMessage(), e);
        }
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