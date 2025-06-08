package com.example.carwashapp.activities;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Vehicle;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVehicleActivity extends AppCompatActivity {
    private TextInputEditText etBrand, etModel, etYear, etColor, etPlateNumber;
    private Spinner spinnerVehicleType;
    private Button btnSave;
    private ImageView btnBack;
    private ApiService apiService;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        initViews();
        setupSpinner();
    }

    private void initViews() {
        etBrand = findViewById(R.id.etBrand);
        etModel = findViewById(R.id.etModel);
        etYear = findViewById(R.id.etYear);
        etColor = findViewById(R.id.etColor);
        etPlateNumber = findViewById(R.id.etPlateNumber);
        spinnerVehicleType = findViewById(R.id.spinnerVehicleType);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        apiService = ApiClient.getApiService();
        sessionManager = new SessionManager(this);

        btnSave.setOnClickListener(v -> saveVehicle());
        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSpinner() {
        String[] vehicleTypes = {"Mobil", "Motor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
    }

    private void saveVehicle() {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String plateNumber = etPlateNumber.getText().toString().trim();
        String vehicleTypeDisplay = spinnerVehicleType.getSelectedItem().toString();

        if (brand.isEmpty() || model.isEmpty() || yearStr.isEmpty() || color.isEmpty() || plateNumber.isEmpty()) {
            Toast.makeText(this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
            return;
        }

        int year;
        try {
            year = Integer.parseInt(yearStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Tahun harus berupa angka", Toast.LENGTH_SHORT).show();
            return;
        }

        String vehicleType = vehicleTypeDisplay.equals("Mobil") ? "car" : "motorcycle";

        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Vehicle vehicle = new Vehicle(brand, model, year, color, plateNumber, vehicleType);

        apiService.addVehicle(ApiClient.createAuthHeader(token), vehicle).enqueue(new Callback<ApiResponse<Vehicle>>() {
            @Override
            public void onResponse(Call<ApiResponse<Vehicle>> call, Response<ApiResponse<Vehicle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Vehicle> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Toast.makeText(AddVehicleActivity.this, "Kendaraan berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Gagal menambahkan kendaraan";
                        Toast.makeText(AddVehicleActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddVehicleActivity.this, "Gagal menambahkan kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Vehicle>> call, Throwable t) {
                Toast.makeText(AddVehicleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
