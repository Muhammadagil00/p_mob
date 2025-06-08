package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.adapters.VehicleAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Vehicle;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VehicleActivity extends AppCompatActivity {
    private RecyclerView rvVehicles;
    private Button btnAddVehicle;
    private ImageView btnBack;
    private VehicleAdapter vehicleAdapter;
    private ApiService apiService;
    private SessionManager sessionManager;
    private List<Vehicle> vehicleList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);

        initViews();
        loadVehicles();
    }

    private void initViews() {
        rvVehicles = findViewById(R.id.rvVehicles);
        btnAddVehicle = findViewById(R.id.btnAddVehicle);
        btnBack = findViewById(R.id.btnBack);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        rvVehicles.setLayoutManager(new LinearLayoutManager(this));

        btnAddVehicle.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVehicleActivity.class);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
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
                        vehicleAdapter = new VehicleAdapter(vehicleList);
                        vehicleAdapter.setOnVehicleClickListener(vehicle -> {
                            // Handle vehicle click for editing
                            Intent intent = new Intent(VehicleActivity.this, EditVehicleActivity.class);
                            intent.putExtra("vehicle_id", vehicle.getId());
                            startActivity(intent);
                        });
                        rvVehicles.setAdapter(vehicleAdapter);
                    } else {
                        Toast.makeText(VehicleActivity.this, "Gagal memuat kendaraan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(VehicleActivity.this, "Gagal memuat kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Vehicle>>> call, Throwable t) {
                Toast.makeText(VehicleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVehicles(); // Refresh data when returning to this activity
    }
}
