package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.adapters.AdminServiceAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.ApiResponseWrapper;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import okhttp3.ResponseBody;

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
    private AdminServiceAdapter serviceAdapter;
    private FloatingActionButton fabAddService;

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
        apiService = ApiClient.getApiService();

        rvServices = findViewById(R.id.rvServices);
        fabAddService = findViewById(R.id.fabAddService);

        if (rvServices != null) {
            rvServices.setLayoutManager(new LinearLayoutManager(this));
        }

        // Set up FAB click listener
        if (fabAddService != null) {
            fabAddService.setOnClickListener(v -> {
                Intent intent = new Intent(this, AddServiceActivity.class);
                startActivity(intent);
            });
        }

        // Set title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Kelola Layanan");
        }
    }

    private void checkAdminAccess() {
        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            Toast.makeText(this, "Akses ditolak. Anda bukan admin.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadServices() {
        // Try alternative endpoint first
        loadServicesAlternative();
    }

    private void loadServicesAlternative() {
        apiService.getServicesRaw().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();

                        // Parse using flexible wrapper
                        List<Service> services = ApiResponseWrapper.parseListResponse(jsonResponse, Service.class);
                        boolean isSuccess = ApiResponseWrapper.isSuccessResponse(jsonResponse);

                        if (isSuccess && services != null && !services.isEmpty()) {
                            serviceAdapter = new AdminServiceAdapter(ManageServicesActivity.this, services);
                            serviceAdapter.setOnServiceActionListener(new AdminServiceAdapter.OnServiceActionListener() {
                                @Override
                                public void onEditService(Service service) {
                                    editService(service);
                                }

                                @Override
                                public void onDeleteService(Service service) {
                                    deleteService(service);
                                }
                            });
                            if (rvServices != null) {
                                rvServices.setAdapter(serviceAdapter);
                            }
                            Toast.makeText(ManageServicesActivity.this,
                                "Berhasil memuat " + services.size() + " layanan dari API", Toast.LENGTH_SHORT).show();
                        } else {
                            loadDemoServices();
                        }
                    } else {
                        // Fallback to original endpoint
                        loadServicesOriginal();
                    }
                } catch (Exception e) {
                    // Fallback to original endpoint
                    loadServicesOriginal();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Fallback to original endpoint
                loadServicesOriginal();
            }
        });
    }

    private void loadServicesOriginal() {
        apiService.getServices().enqueue(new Callback<ApiResponse<List<Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<Service>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Service>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Service> services = apiResponse.getData();
                        serviceAdapter = new AdminServiceAdapter(ManageServicesActivity.this, services);
                        serviceAdapter.setOnServiceActionListener(new AdminServiceAdapter.OnServiceActionListener() {
                            @Override
                            public void onEditService(Service service) {
                                editService(service);
                            }

                            @Override
                            public void onDeleteService(Service service) {
                                deleteService(service);
                            }
                        });
                        if (rvServices != null) {
                            rvServices.setAdapter(serviceAdapter);
                        }
                        Toast.makeText(ManageServicesActivity.this,
                            "Berhasil memuat " + services.size() + " layanan dari API", Toast.LENGTH_SHORT).show();
                    } else {
                        Log.e(TAG, "API mengembalikan error: " + apiResponse.getError());
                        loadDemoServices();
                    }
                } else {
                    Log.e(TAG, "Gagal memuat layanan: " + response.message());
                    loadDemoServices();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Service>>> call, Throwable t) {
                Log.e(TAG, "Error jaringan: " + t.getMessage());
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
        service1.setDuration(60);
        service1.setActive(true);
        demoServices.add(service1);

        Service service2 = new Service();
        service2.setId("admin_demo_2");
        service2.setName("Cuci Mobil Premium");
        service2.setDescription("Cuci mobil lengkap dengan wax dan poles");
        service2.setPrice(50000);
        service2.setDuration(90);
        service2.setActive(true);
        demoServices.add(service2);

        Service service3 = new Service();
        service3.setId("admin_demo_3");
        service3.setName("Cuci Motor");
        service3.setDescription("Cuci motor bersih dan mengkilap");
        service3.setPrice(15000);
        service3.setDuration(30);
        service3.setActive(true);
        demoServices.add(service3);

        Service service4 = new Service();
        service4.setId("admin_demo_4");
        service4.setName("Detailing Mobil");
        service4.setDescription("Perawatan detail interior dan eksterior");
        service4.setPrice(100000);
        service4.setDuration(120);
        service4.setActive(false);
        demoServices.add(service4);

        serviceAdapter = new AdminServiceAdapter(ManageServicesActivity.this, demoServices);
        serviceAdapter.setOnServiceActionListener(new AdminServiceAdapter.OnServiceActionListener() {
            @Override
            public void onEditService(Service service) {
                editService(service);
            }

            @Override
            public void onDeleteService(Service service) {
                deleteService(service);
            }
        });

        if (rvServices != null) {
            rvServices.setAdapter(serviceAdapter);
        }

        Toast.makeText(this, "Menampilkan " + demoServices.size() + " layanan demo", Toast.LENGTH_SHORT).show();
    }

    private void editService(Service service) {
        Log.d(TAG, "Editing service: " + service.getName());
        Intent intent = new Intent(this, AddServiceActivity.class);
        intent.putExtra("edit_mode", true);
        intent.putExtra("service_id", service.getId());
        intent.putExtra("service_name", service.getName());
        intent.putExtra("service_description", service.getDescription());
        intent.putExtra("service_price", service.getPrice());
        intent.putExtra("service_duration", service.getDuration());
        intent.putExtra("service_active", service.isActive());
        startActivity(intent);
    }

    private void deleteService(Service service) {
        Log.d(TAG, "=== DELETE SERVICE START ===");
        Log.d(TAG, "Service ID: " + (service.getId() != null ? service.getId() : "NULL"));
        Log.d(TAG, "Service Name: " + (service.getName() != null ? service.getName() : "NULL"));
        Log.d(TAG, "Current adapter item count: " + (serviceAdapter != null ? serviceAdapter.getItemCount() : "NULL ADAPTER"));

        // Check if this is a demo service
        if (service.getId() != null && service.getId().startsWith("admin_demo_")) {
            Log.d(TAG, "Deleting demo service - removing from adapter only");
            handleDemoServiceDelete(service);
            return;
        }

        String token = sessionManager.getToken();
        if (token == null) {
            Log.e(TAG, "Token is null, cannot delete service");
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading
        Toast.makeText(this, "Menghapus layanan...", Toast.LENGTH_SHORT).show();

        apiService.deleteService(ApiClient.createAuthHeader(token), service.getId())
            .enqueue(new Callback<ApiResponse<Void>>() {
                @Override
                public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                    Log.d(TAG, "Delete response code: " + response.code());

                    if (response.isSuccessful()) {
                        Log.d(TAG, "Service deleted successfully from server");

                        // Remove from adapter immediately
                        if (serviceAdapter != null) {
                            serviceAdapter.removeServiceById(service.getId());
                        }

                        Toast.makeText(ManageServicesActivity.this,
                            "Layanan \"" + service.getName() + "\" berhasil dihapus", Toast.LENGTH_SHORT).show();

                        // Optional: Refresh data after a delay to ensure consistency
                        // Don't refresh immediately to avoid race condition

                    } else {
                        Log.e(TAG, "Failed to delete service: " + response.code() + " - " + response.message());

                        // Try to get error message from response body
                        String errorMessage = "Gagal menghapus layanan";
                        try {
                            if (response.errorBody() != null) {
                                String errorBody = response.errorBody().string();
                                Log.e(TAG, "Error body: " + errorBody);
                                errorMessage = "Gagal menghapus layanan: " + response.message();
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "Error reading error body: " + e.getMessage());
                        }

                        Toast.makeText(ManageServicesActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                    Log.e(TAG, "Network error deleting service: " + t.getMessage());
                    Toast.makeText(ManageServicesActivity.this,
                        "Error jaringan: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
    }

    private void refreshDataWithDelay() {
        // Refresh data after a short delay to ensure server consistency
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            Log.d(TAG, "Refreshing data after delete operation");
            loadServices();
        }, 1000); // 1 second delay
    }

    private void handleDemoServiceDelete(Service service) {
        Log.d(TAG, "Handling demo service delete");

        if (serviceAdapter != null) {
            serviceAdapter.removeServiceById(service.getId());
            Toast.makeText(this,
                "Layanan demo \"" + service.getName() + "\" berhasil dihapus", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Demo service removed from adapter");
        } else {
            Log.e(TAG, "Service adapter is null");
            Toast.makeText(this, "Error: Adapter tidak tersedia", Toast.LENGTH_SHORT).show();
        }
    }

    private void forceRefreshAdapter() {
        if (serviceAdapter != null) {
            serviceAdapter.notifyDataSetChanged();
            Log.d(TAG, "Forced adapter refresh");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity
        loadServices();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
