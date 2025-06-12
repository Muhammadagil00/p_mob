package com.example.carwashapp.activities;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.CreateBookingRequest;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.models.Vehicle;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.DateTimePickerHelper;
import com.example.carwashapp.utils.SessionManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private Spinner spinnerService, spinnerVehicle;
    private EditText etDate, etTime, etLocation, etNotes;
    private Button btnBook;
    private TextView tvPrice;
    private ApiService apiService;
    private SessionManager sessionManager;
    private List<Service> serviceList = new ArrayList<>();
    private List<Vehicle> vehicleList = new ArrayList<>();
    private Service selectedService;
    private Vehicle selectedVehicle;
    private Calendar selectedDateTime = Calendar.getInstance();
    private DateTimePickerHelper dateTimePickerHelper;
    private boolean isBookingInProgress = false;
    private int retryCount = 0;
    private static final int MAX_RETRY = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        spinnerService = findViewById(R.id.spinnerService);
        spinnerVehicle = findViewById(R.id.spinnerVehicle);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etLocation = findViewById(R.id.etLocation);
        etNotes = findViewById(R.id.etNotes);
        btnBook = findViewById(R.id.btnBook);
        tvPrice = findViewById(R.id.tvPrice);

        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);
        dateTimePickerHelper = new DateTimePickerHelper(this);

        loadServices();
        loadVehicles();

        etDate.setOnClickListener(v -> dateTimePickerHelper.showDatePicker(etDate));
        etTime.setOnClickListener(v -> dateTimePickerHelper.showTimePicker(etTime));
        btnBook.setOnClickListener(v -> bookService());
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<ApiResponse<List<Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<Service>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Service>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        serviceList = apiResponse.getData();
                        List<String> serviceNames = new ArrayList<>();
                        for (Service s : serviceList) {
                            serviceNames.add(s.getName());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BookingActivity.this, android.R.layout.simple_spinner_item, serviceNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerService.setAdapter(adapter);
                        spinnerService.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedService = serviceList.get(position);
                                tvPrice.setText("Harga: Rp " + selectedService.getPrice());
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                        if (!serviceList.isEmpty()) {
                            selectedService = serviceList.get(0);
                            tvPrice.setText("Harga: Rp " + selectedService.getPrice());
                        }
                    } else {
                        Toast.makeText(BookingActivity.this, "Gagal memuat layanan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingActivity.this, "Gagal memuat layanan", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Service>>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadVehicles() {
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getVehicles(ApiClient.createAuthHeader(token)).enqueue(new Callback<ApiResponse<List<Vehicle>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Vehicle>>> call, Response<ApiResponse<List<Vehicle>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Vehicle>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        vehicleList = apiResponse.getData();
                        List<String> vehicleNames = new ArrayList<>();
                        for (Vehicle v : vehicleList) {
                            vehicleNames.add(v.toString());
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(BookingActivity.this, android.R.layout.simple_spinner_item, vehicleNames);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        spinnerVehicle.setAdapter(adapter);
                        spinnerVehicle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                selectedVehicle = vehicleList.get(position);
                            }
                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {}
                        });
                        if (!vehicleList.isEmpty()) {
                            selectedVehicle = vehicleList.get(0);
                        }
                    } else {
                        Toast.makeText(BookingActivity.this, "Gagal memuat kendaraan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingActivity.this, "Gagal memuat kendaraan", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<List<Vehicle>>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void bookService() {
        // Prevent multiple clicks
        if (isBookingInProgress) {
            Toast.makeText(this, "Booking sedang diproses, harap tunggu...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedService == null) {
            Toast.makeText(this, "Pilih layanan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedVehicle == null) {
            Toast.makeText(this, "Pilih kendaraan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate form inputs
        if (!validateForm()) {
            return;
        }
        String location = etLocation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();

        if (date.isEmpty() || time.isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Tanggal, waktu, dan lokasi harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        // Format date to YYYY-MM-DD
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate;
        try {
            formattedDate = outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            Toast.makeText(this, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create time slot from time input (improve this logic)
        String timeSlot = createTimeSlot(time);

        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateBookingRequest request = new CreateBookingRequest(
                selectedService.getId(),
                selectedVehicle.getId(),
                formattedDate,
                timeSlot,
                location,
                notes
        );

        // Set booking in progress
        isBookingInProgress = true;
        btnBook.setEnabled(false);
        btnBook.setText("Memproses...");
        retryCount = 0; // Reset retry count

        performBookingRequest();
    }

    private String handleBookingError(Response<?> response) {
        try {
            if (response.code() == 429) {
                // Rate limiting error
                if (response.errorBody() != null) {
                    String errorBody = response.errorBody().string();
                    if (errorBody.contains("Too many booking attempts")) {
                        return "⚠️ Terlalu banyak percobaan booking.\nSilakan tunggu beberapa menit sebelum mencoba lagi.";
                    }
                }
                return "⚠️ Server sedang sibuk. Silakan coba lagi dalam beberapa menit.";
            } else if (response.code() == 400) {
                // Parse validation errors from server
                if (response.errorBody() != null) {
                    try {
                        String errorBody = response.errorBody().string();
                        if (errorBody.contains("Location must be at least 5 characters")) {
                            return "📍 Lokasi harus minimal 5 karakter.\nContoh: 'Jl. Sudirman No. 123'";
                        } else if (errorBody.contains("Validation failed")) {
                            return "❌ Data tidak valid:\n" + parseValidationErrors(errorBody);
                        }
                    } catch (Exception e) {
                        // Fallback to generic message
                    }
                }
                return "❌ Data booking tidak valid. Periksa kembali form Anda.";
            } else if (response.code() == 401) {
                return "🔐 Sesi telah berakhir. Silakan login ulang.";
            } else if (response.code() == 403) {
                return "🚫 Akses ditolak. Anda tidak memiliki izin untuk booking.";
            } else if (response.code() == 404) {
                return "❓ Layanan booking tidak ditemukan.";
            } else if (response.code() >= 500) {
                return "🔧 Server sedang bermasalah. Silakan coba lagi nanti.";
            } else {
                return "❌ Gagal booking: " + response.message() + " (Kode: " + response.code() + ")";
            }
        } catch (Exception e) {
            return "❌ Terjadi kesalahan saat memproses response.";
        }
    }

    private String parseValidationErrors(String errorBody) {
        try {
            // Simple parsing for validation details
            if (errorBody.contains("details")) {
                String[] parts = errorBody.split("details\":\\[\"");
                if (parts.length > 1) {
                    String details = parts[1].split("\"]")[0];
                    return details.replace("\",\"", "\n• ");
                }
            }
            return "Periksa kembali data yang Anda masukkan";
        } catch (Exception e) {
            return "Periksa kembali data yang Anda masukkan";
        }
    }

    private String createTimeSlot(String startTime) {
        try {
            // Parse start time
            String[] timeParts = startTime.split(":");
            int hour = Integer.parseInt(timeParts[0]);
            int minute = Integer.parseInt(timeParts[1]);

            // Add service duration to get end time
            int durationMinutes = selectedService != null ? selectedService.getDuration() : 60;

            // Calculate end time
            int totalMinutes = hour * 60 + minute + durationMinutes;
            int endHour = (totalMinutes / 60) % 24;
            int endMinute = totalMinutes % 60;

            // Format as HH:MM-HH:MM
            return String.format("%02d:%02d-%02d:%02d", hour, minute, endHour, endMinute);
        } catch (Exception e) {
            // Fallback to simple format
            return startTime + "-" + startTime;
        }
    }

    private void retryBookingWithDelay() {
        retryCount++;
        int delaySeconds = (int) Math.pow(2, retryCount); // Exponential backoff: 2, 4, 8 seconds

        Toast.makeText(this, "⏳ Server sibuk, mencoba lagi dalam " + delaySeconds + " detik... (Percobaan " + retryCount + "/" + MAX_RETRY + ")", Toast.LENGTH_LONG).show();

        btnBook.setText("Mencoba lagi dalam " + delaySeconds + "s...");

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                btnBook.setText("Memproses...");
                // Retry the booking with same parameters
                performBookingRequest();
            }
        }, delaySeconds * 1000);
    }

    private void performBookingRequest() {
        // Extract booking logic to separate method for retry
        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            resetBookingState();
            return;
        }

        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Format date
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String formattedDate;
        try {
            formattedDate = outputFormat.format(inputFormat.parse(date));
        } catch (Exception e) {
            Toast.makeText(this, "Format tanggal tidak valid", Toast.LENGTH_SHORT).show();
            resetBookingState();
            return;
        }

        String timeSlot = createTimeSlot(time);

        CreateBookingRequest request = new CreateBookingRequest(
                selectedService.getId(),
                selectedVehicle.getId(),
                formattedDate,
                timeSlot,
                location,
                notes
        );

        // Make the API call (same as before)
        apiService.createBooking(ApiClient.createAuthHeader(token), request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(BookingActivity.this, "✅ Booking berhasil!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gagal booking";
                        Toast.makeText(BookingActivity.this, "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                        resetBookingState();
                    }
                } else {
                    if (response.code() == 429 && retryCount < MAX_RETRY) {
                        retryBookingWithDelay();
                    } else {
                        String errorMessage = handleBookingError(response);
                        Toast.makeText(BookingActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                        resetBookingState();
                    }
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "🌐 Error jaringan: " + t.getMessage(), Toast.LENGTH_LONG).show();
                resetBookingState();
            }
        });
    }

    private void resetBookingState() {
        isBookingInProgress = false;
        btnBook.setEnabled(true);
        btnBook.setText("Book Sekarang");
        retryCount = 0;
    }

    private boolean validateForm() {
        String date = etDate.getText().toString().trim();
        String time = etTime.getText().toString().trim();
        String location = etLocation.getText().toString().trim();

        // Validate date
        if (date.isEmpty()) {
            etDate.setError("Tanggal harus diisi");
            etDate.requestFocus();
            Toast.makeText(this, "📅 Pilih tanggal booking", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate time
        if (time.isEmpty()) {
            etTime.setError("Waktu harus diisi");
            etTime.requestFocus();
            Toast.makeText(this, "⏰ Pilih waktu booking", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate location (minimum 5 characters as per server requirement)
        if (location.isEmpty()) {
            etLocation.setError("Lokasi harus diisi");
            etLocation.requestFocus();
            Toast.makeText(this, "📍 Masukkan lokasi layanan", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (location.length() < 5) {
            etLocation.setError("Lokasi minimal 5 karakter");
            etLocation.requestFocus();
            Toast.makeText(this, "📍 Lokasi harus minimal 5 karakter\nContoh: 'Jl. Sudirman No. 123'", Toast.LENGTH_LONG).show();
            return false;
        }

        // Validate date format and not in the past
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            Date selectedDate = inputFormat.parse(date);
            Date today = new Date();

            // Reset time to compare only dates
            Calendar todayCal = Calendar.getInstance();
            todayCal.setTime(today);
            todayCal.set(Calendar.HOUR_OF_DAY, 0);
            todayCal.set(Calendar.MINUTE, 0);
            todayCal.set(Calendar.SECOND, 0);
            todayCal.set(Calendar.MILLISECOND, 0);

            Calendar selectedCal = Calendar.getInstance();
            selectedCal.setTime(selectedDate);
            selectedCal.set(Calendar.HOUR_OF_DAY, 0);
            selectedCal.set(Calendar.MINUTE, 0);
            selectedCal.set(Calendar.SECOND, 0);
            selectedCal.set(Calendar.MILLISECOND, 0);

            if (selectedCal.before(todayCal)) {
                etDate.setError("Tanggal tidak boleh di masa lalu");
                etDate.requestFocus();
                Toast.makeText(this, "📅 Pilih tanggal hari ini atau yang akan datang", Toast.LENGTH_SHORT).show();
                return false;
            }
        } catch (Exception e) {
            etDate.setError("Format tanggal tidak valid");
            etDate.requestFocus();
            Toast.makeText(this, "📅 Format tanggal harus DD/MM/YYYY", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate time format
        if (!isValidTimeFormat(time)) {
            etTime.setError("Format waktu tidak valid");
            etTime.requestFocus();
            Toast.makeText(this, "⏰ Format waktu harus HH:MM (24 jam)", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Clear any previous errors
        etDate.setError(null);
        etTime.setError(null);
        etLocation.setError(null);

        return true;
    }

    private boolean isValidTimeFormat(String time) {
        try {
            String[] parts = time.split(":");
            if (parts.length != 2) return false;

            int hour = Integer.parseInt(parts[0]);
            int minute = Integer.parseInt(parts[1]);

            return hour >= 0 && hour <= 23 && minute >= 0 && minute <= 59;
        } catch (Exception e) {
            return false;
        }
    }
}