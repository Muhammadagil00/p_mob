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
        if (selectedService == null) {
            Toast.makeText(this, "Pilih layanan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        if (selectedVehicle == null) {
            Toast.makeText(this, "Pilih kendaraan terlebih dahulu", Toast.LENGTH_SHORT).show();
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

        // Create time slot from time input
        String timeSlot = time + "-" + time; // You might want to improve this logic

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

        apiService.createBooking(ApiClient.createAuthHeader(token), request).enqueue(new Callback<ApiResponse<Booking>>() {
            @Override
            public void onResponse(Call<ApiResponse<Booking>> call, Response<ApiResponse<Booking>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Booking> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(BookingActivity.this, "Booking berhasil!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gagal booking";
                        Toast.makeText(BookingActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(BookingActivity.this, "Gagal booking: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<ApiResponse<Booking>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}