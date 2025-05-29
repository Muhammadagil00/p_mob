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
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.DateTimePickerHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingActivity extends AppCompatActivity {
    private Spinner spinnerService;
    private EditText etDate, etTime, etLocation;
    private Button btnBook;
    private TextView tvPrice;
    private ApiService apiService;
    private List<Service> serviceList = new ArrayList<>();
    private Service selectedService;
    private Calendar selectedDateTime = Calendar.getInstance();
    private DateTimePickerHelper dateTimePickerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking);

        spinnerService = findViewById(R.id.spinnerService);
        etDate = findViewById(R.id.etDate);
        etTime = findViewById(R.id.etTime);
        etLocation = findViewById(R.id.etLocation);
        btnBook = findViewById(R.id.btnBook);
        tvPrice = findViewById(R.id.tvPrice);

        apiService = ApiClient.getApiService(getApplicationContext());
        dateTimePickerHelper = new DateTimePickerHelper(this);

        loadServices();

        etDate.setOnClickListener(v -> dateTimePickerHelper.showDatePicker(etDate));
        etTime.setOnClickListener(v -> dateTimePickerHelper.showTimePicker(etTime));
        btnBook.setOnClickListener(v -> bookService());
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    serviceList = response.body();
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
            }
            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void bookService() {
        if (selectedService == null) {
            Toast.makeText(this, "Pilih layanan terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }
        String location = etLocation.getText().toString().trim();
        if (etDate.getText().toString().isEmpty() || etTime.getText().toString().isEmpty() || location.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }
        // Format ISO 8601
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
        String isoDate = sdf.format(selectedDateTime.getTime());
        Booking booking = new Booking(selectedService.getName(), isoDate, location);
        apiService.createBooking(booking).enqueue(new Callback<Booking>() {
            @Override
            public void onResponse(Call<Booking> call, Response<Booking> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(BookingActivity.this, "Booking berhasil!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(BookingActivity.this, "Gagal booking: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Booking> call, Throwable t) {
                Toast.makeText(BookingActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}