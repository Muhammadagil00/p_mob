package com.example.carwashapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.adapters.ServiceSliderAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ManageServicesActivity extends AppCompatActivity {
    private static final String TAG = "ManageServices";

    private SessionManager sessionManager;
    private ApiService apiService;
    private RecyclerView rvServices;
    private ServiceSliderAdapter serviceAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_services);

        initViews();
        checkAdminAccess();
        loadServices();
    }

    private void initViews() {
        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService(this);

        rvServices = findViewById(R.id.rvServices);
        if (rvServices != null) {
            rvServices.setLayoutManager(new LinearLayoutManager(this));
        }
    }

    private void checkAdminAccess() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Akses ditolak. Anda bukan admin.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<List<Service>>() {
            @Override
            public void onResponse(Call<List<Service>> call, Response<List<Service>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Service> services = response.body();
                    serviceAdapter = new ServiceSliderAdapter(services);
                    serviceAdapter.setOnServiceClickListener(service -> {
                        // Handle service click for editing
                        Toast.makeText(ManageServicesActivity.this,
                            "Edit service: " + service.getName(), Toast.LENGTH_SHORT).show();
                    });
                    if (rvServices != null) {
                        rvServices.setAdapter(serviceAdapter);
                    }
                    Toast.makeText(ManageServicesActivity.this,
                        "Loaded " + services.size() + " services from API", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Failed to load services: " + response.message());
                    loadDemoServices();
                }
            }

            @Override
            public void onFailure(Call<List<Service>> call, Throwable t) {
                Log.e(TAG, "Network error: " + t.getMessage());
                loadDemoServices();
            }
        });
    }

    private void loadDemoServices() {
        // Create demo services for admin management
        List<Service> demoServices = new ArrayList<>();

        Service service1 = new Service();
        service1.setId("admin_demo_1");
        service1.setName("Cuci Mobil Basic");
        service1.setDescription("Cuci mobil standar dengan sabun dan air bersih");
        service1.setPrice(25000);
        demoServices.add(service1);

        Service service2 = new Service();
        service2.setId("admin_demo_2");
        service2.setName("Cuci Mobil Premium");
        service2.setDescription("Cuci mobil lengkap dengan wax dan poles");
        service2.setPrice(50000);
        demoServices.add(service2);

        Service service3 = new Service();
        service3.setId("admin_demo_3");
        service3.setName("Cuci Motor");
        service3.setDescription("Cuci motor bersih dan mengkilap");
        service3.setPrice(15000);
        demoServices.add(service3);

        Service service4 = new Service();
        service4.setId("admin_demo_4");
        service4.setName("Detailing Mobil");
        service4.setDescription("Perawatan detail interior dan eksterior");
        service4.setPrice(100000);
        demoServices.add(service4);

        serviceAdapter = new ServiceSliderAdapter(demoServices);
        serviceAdapter.setOnServiceClickListener(service -> {
            // Handle service click for editing
            Toast.makeText(ManageServicesActivity.this,
                "Edit service: " + service.getName() + " (Demo)", Toast.LENGTH_SHORT).show();
        });

        if (rvServices != null) {
            rvServices.setAdapter(serviceAdapter);
        }

        Toast.makeText(this, "Menampilkan " + demoServices.size() + " layanan demo", Toast.LENGTH_SHORT).show();
    }
}
