package com.example.carwashapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.ApiResponseWrapper;
import com.example.carwashapp.models.Vehicle;
import com.example.carwashapp.models.VehicleRequest;
import com.example.carwashapp.models.VehicleCreateRequest;
import com.example.carwashapp.models.VehicleRequestV2;
import com.example.carwashapp.models.VehicleRequestV3;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import okhttp3.ResponseBody;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVehicleActivity extends AppCompatActivity {
    private static final String TAG = "AddVehicleActivity";

    private TextInputEditText etBrand, etModel, etYear, etColor, etPlateNumber;
    private Spinner spinnerVehicleType;
    private Button btnSave;
    private ImageView btnBack;
    private ApiService apiService;
    private SessionManager sessionManager;
    private boolean isEditMode = false;
    private String vehicleId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vehicle);

        // Check if this is edit mode
        isEditMode = getIntent().getBooleanExtra("edit_mode", false);
        vehicleId = getIntent().getStringExtra("vehicle_id");

        initViews();
        setupSpinner();

        if (isEditMode && vehicleId != null) {
            loadVehicleForEdit();
        }
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

        // Set title based on mode
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(isEditMode ? "Edit Kendaraan" : "Tambah Kendaraan");
        }
    }

    private void setupSpinner() {
        String[] vehicleTypes = {"Mobil", "Motor"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, vehicleTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerVehicleType.setAdapter(adapter);
    }

    private void loadVehicleForEdit() {
        String token = sessionManager.getToken();
        if (token == null || vehicleId == null) {
            return;
        }

        apiService.getVehicleDetails(ApiClient.createAuthHeader(token), vehicleId).enqueue(new Callback<ApiResponse<Vehicle>>() {
            @Override
            public void onResponse(Call<ApiResponse<Vehicle>> call, Response<ApiResponse<Vehicle>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<Vehicle> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        populateVehicleData(apiResponse.getData());
                    } else {
                        Toast.makeText(AddVehicleActivity.this, "Gagal memuat data kendaraan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AddVehicleActivity.this, "Gagal memuat data kendaraan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Vehicle>> call, Throwable t) {
                Toast.makeText(AddVehicleActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateVehicleData(Vehicle vehicle) {
        etBrand.setText(vehicle.getBrand());
        etModel.setText(vehicle.getModel());
        etYear.setText(String.valueOf(vehicle.getYear()));
        etColor.setText(vehicle.getColor());
        etPlateNumber.setText(vehicle.getPlateNumber());

        // Set spinner selection
        String vehicleTypeDisplay = vehicle.getVehicleType().equals("car") ? "Mobil" : "Motor";
        ArrayAdapter<String> adapter = (ArrayAdapter<String>) spinnerVehicleType.getAdapter();
        int position = adapter.getPosition(vehicleTypeDisplay);
        spinnerVehicleType.setSelection(position);
    }

    private void saveVehicle() {
        String brand = etBrand.getText().toString().trim();
        String model = etModel.getText().toString().trim();
        String yearStr = etYear.getText().toString().trim();
        String color = etColor.getText().toString().trim();
        String plateNumber = etPlateNumber.getText().toString().trim();
        String vehicleTypeDisplay = spinnerVehicleType.getSelectedItem().toString();

        // Validasi input
        if (brand.isEmpty()) {
            etBrand.setError("Merek kendaraan harus diisi");
            etBrand.requestFocus();
            return;
        }

        if (model.isEmpty()) {
            etModel.setError("Model kendaraan harus diisi");
            etModel.requestFocus();
            return;
        }

        if (yearStr.isEmpty()) {
            etYear.setError("Tahun harus diisi");
            etYear.requestFocus();
            return;
        }

        if (color.isEmpty()) {
            etColor.setError("Warna harus diisi");
            etColor.requestFocus();
            return;
        }

        if (plateNumber.isEmpty()) {
            etPlateNumber.setError("Nomor plat harus diisi");
            etPlateNumber.requestFocus();
            return;
        }

        // Validasi tahun
        int year;
        try {
            year = Integer.parseInt(yearStr);
            if (year < 1900 || year > 2030) {
                etYear.setError("Tahun harus antara 1900-2030");
                etYear.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etYear.setError("Tahun harus berupa angka");
            etYear.requestFocus();
            return;
        }

        // Validasi nomor plat (basic validation)
        if (plateNumber.length() < 3) {
            etPlateNumber.setError("Nomor plat terlalu pendek");
            etPlateNumber.requestFocus();
            return;
        }

        String vehicleType = vehicleTypeDisplay.equals("Mobil") ? "car" : "motorcycle";

        String token = sessionManager.getToken();
        if (token == null) {
            Toast.makeText(this, "Token tidak valid", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "=== SAVING VEHICLE ===");
        Log.d(TAG, "Brand: " + brand);
        Log.d(TAG, "Model: " + model);
        Log.d(TAG, "Year: " + year);
        Log.d(TAG, "Color: " + color);
        Log.d(TAG, "Plate: " + plateNumber);
        Log.d(TAG, "Type: " + vehicleType);
        Log.d(TAG, "Edit mode: " + isEditMode);
        Log.d(TAG, "Vehicle ID: " + vehicleId);
        Log.d(TAG, "======================");

        // Show progress to user
        Toast.makeText(this, "Mencoba menyimpan dengan berbagai format...", Toast.LENGTH_SHORT).show();

        // Disable button to prevent double submission
        btnSave.setEnabled(false);
        btnSave.setText(isEditMode ? "Mengupdate..." : "Menyimpan...");

        // Try with original VehicleRequest first
        tryVehicleRequest(token, brand, model, year, color, plateNumber, vehicleType);
    }

    private void tryVehicleRequest(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        VehicleRequest vehicleRequest = new VehicleRequest(brand, model, year, color, plateNumber, vehicleType);
        Log.d(TAG, "Trying VehicleRequest: " + vehicleRequest.toString());

        Call<ApiResponse<Vehicle>> call;
        if (isEditMode && vehicleId != null) {
            call = apiService.updateVehicle(ApiClient.createAuthHeader(token), vehicleId, vehicleRequest);
        } else {
            call = apiService.addVehicle(ApiClient.createAuthHeader(token), vehicleRequest);
        }

        call.enqueue(new Callback<ApiResponse<Vehicle>>() {
            @Override
            public void onResponse(Call<ApiResponse<Vehicle>> call, Response<ApiResponse<Vehicle>> response) {
                Log.d(TAG, "VehicleRequest response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessResponse(response.body());
                } else if (response.code() == 400) {
                    Log.w(TAG, "VehicleRequest failed with 400, trying alternative format");
                    // Try alternative format
                    tryVehicleCreateRequest(token, brand, model, year, color, plateNumber, vehicleType);
                } else {
                    handleErrorResponse(response);
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Vehicle>> call, Throwable t) {
                Log.e(TAG, "VehicleRequest network error: " + t.getMessage());
                handleNetworkError(t);
            }
        });
    }

    private void tryVehicleCreateRequest(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        Log.w(TAG, "Trying VehicleRequestV2 format (snake_case fields)");

        VehicleRequestV2 requestV2 = new VehicleRequestV2(brand, model, String.valueOf(year), color, plateNumber, vehicleType);
        Log.d(TAG, "Trying VehicleRequestV2: " + requestV2.toString());

        Call<ResponseBody> call;
        if (isEditMode && vehicleId != null) {
            call = apiService.updateVehicleRaw(ApiClient.createAuthHeader(token), vehicleId, requestV2);
        } else {
            call = apiService.addVehicleRaw(ApiClient.createAuthHeader(token), requestV2);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "VehicleRequestV2 response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "VehicleRequestV2 success response: " + jsonResponse);

                        // Parse response to check if it's successful
                        boolean isSuccess = ApiResponseWrapper.isSuccessResponse(jsonResponse);
                        if (isSuccess) {
                            handleRawSuccessResponse();
                        } else {
                            String errorMsg = ApiResponseWrapper.getErrorMessage(jsonResponse);
                            Log.e(TAG, "VehicleRequestV2 API error: " + errorMsg);
                            tryVehicleRequestV3(token, brand, model, year, color, plateNumber, vehicleType);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing VehicleRequestV2 response: " + e.getMessage());
                        tryVehicleRequestV3(token, brand, model, year, color, plateNumber, vehicleType);
                    }
                } else {
                    Log.w(TAG, "VehicleRequestV2 failed with " + response.code() + ", trying V3 format");
                    tryVehicleRequestV3(token, brand, model, year, color, plateNumber, vehicleType);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "VehicleRequestV2 network error: " + t.getMessage());
                tryVehicleRequestV3(token, brand, model, year, color, plateNumber, vehicleType);
            }
        });
    }

    private void tryVehicleRequestV3(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        Log.w(TAG, "Trying VehicleRequestV3 format (minimal fields)");

        VehicleRequestV3 requestV3 = new VehicleRequestV3(brand, model, year, color, plateNumber);
        Log.d(TAG, "Trying VehicleRequestV3: " + requestV3.toString());

        Call<ResponseBody> call;
        if (isEditMode && vehicleId != null) {
            call = apiService.updateVehicleRaw(ApiClient.createAuthHeader(token), vehicleId, requestV3);
        } else {
            call = apiService.addVehicleRaw(ApiClient.createAuthHeader(token), requestV3);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "VehicleRequestV3 response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "VehicleRequestV3 success response: " + jsonResponse);

                        // Parse response to check if it's successful
                        boolean isSuccess = ApiResponseWrapper.isSuccessResponse(jsonResponse);
                        if (isSuccess) {
                            handleRawSuccessResponse();
                        } else {
                            String errorMsg = ApiResponseWrapper.getErrorMessage(jsonResponse);
                            Log.e(TAG, "VehicleRequestV3 API error: " + errorMsg);
                            tryMapRequest(token, brand, model, year, color, plateNumber, vehicleType);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing VehicleRequestV3 response: " + e.getMessage());
                        tryMapRequest(token, brand, model, year, color, plateNumber, vehicleType);
                    }
                } else {
                    Log.w(TAG, "VehicleRequestV3 failed with " + response.code() + ", trying Map format");
                    tryMapRequest(token, brand, model, year, color, plateNumber, vehicleType);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "VehicleRequestV3 network error: " + t.getMessage());
                tryMapRequest(token, brand, model, year, color, plateNumber, vehicleType);
            }
        });
    }

    private void tryMapRequest(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        Log.w(TAG, "Trying Map format (last resort)");

        // Try multiple map formats
        tryMapFormat1(token, brand, model, year, color, plateNumber, vehicleType);
    }

    private void tryMapFormat1(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        // Format 1: Standard camelCase
        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        requestMap.put("brand", brand);
        requestMap.put("model", model);
        requestMap.put("year", year);
        requestMap.put("color", color);
        requestMap.put("plateNumber", plateNumber);
        requestMap.put("vehicleType", vehicleType);

        Log.d(TAG, "Trying Map Format 1: " + requestMap.toString());

        Call<ResponseBody> call;
        if (isEditMode && vehicleId != null) {
            call = apiService.updateVehicleRaw(ApiClient.createAuthHeader(token), vehicleId, requestMap);
        } else {
            call = apiService.addVehicleRaw(ApiClient.createAuthHeader(token), requestMap);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Map Format 1 response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Map Format 1 success response: " + jsonResponse);

                        boolean isSuccess = ApiResponseWrapper.isSuccessResponse(jsonResponse);
                        if (isSuccess) {
                            handleRawSuccessResponse();
                        } else {
                            String errorMsg = ApiResponseWrapper.getErrorMessage(jsonResponse);
                            Log.e(TAG, "Map Format 1 API error: " + errorMsg);
                            tryMapFormat2(token, brand, model, year, color, plateNumber, vehicleType);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Map Format 1 response: " + e.getMessage());
                        tryMapFormat2(token, brand, model, year, color, plateNumber, vehicleType);
                    }
                } else {
                    Log.w(TAG, "Map Format 1 failed with " + response.code() + ", trying Format 2");
                    tryMapFormat2(token, brand, model, year, color, plateNumber, vehicleType);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Map Format 1 network error: " + t.getMessage());
                tryMapFormat2(token, brand, model, year, color, plateNumber, vehicleType);
            }
        });
    }

    private void tryMapFormat2(String token, String brand, String model, int year, String color, String plateNumber, String vehicleType) {
        // Format 2: snake_case
        java.util.Map<String, Object> requestMap = new java.util.HashMap<>();
        requestMap.put("brand", brand);
        requestMap.put("model", model);
        requestMap.put("year", String.valueOf(year)); // String year
        requestMap.put("color", color);
        requestMap.put("plate_number", plateNumber); // snake_case
        requestMap.put("type", vehicleType); // "type" instead of "vehicleType"

        Log.d(TAG, "Trying Map Format 2: " + requestMap.toString());

        Call<ResponseBody> call;
        if (isEditMode && vehicleId != null) {
            call = apiService.updateVehicleRaw(ApiClient.createAuthHeader(token), vehicleId, requestMap);
        } else {
            call = apiService.addVehicleRaw(ApiClient.createAuthHeader(token), requestMap);
        }

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(TAG, "Map Format 2 response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d(TAG, "Map Format 2 success response: " + jsonResponse);

                        boolean isSuccess = ApiResponseWrapper.isSuccessResponse(jsonResponse);
                        if (isSuccess) {
                            handleRawSuccessResponse();
                        } else {
                            String errorMsg = ApiResponseWrapper.getErrorMessage(jsonResponse);
                            Log.e(TAG, "Map Format 2 API error: " + errorMsg);
                            handleAllFormatsFailed(response);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing Map Format 2 response: " + e.getMessage());
                        handleAllFormatsFailed(response);
                    }
                } else {
                    Log.e(TAG, "All formats failed. Last response code: " + response.code());
                    handleAllFormatsFailed(response);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Map Format 2 network error: " + t.getMessage());
                handleNetworkError(t);
            }
        });
    }

    private void handleRawSuccessResponse() {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        String message = isEditMode ? "Kendaraan berhasil diupdate" : "Kendaraan berhasil ditambahkan";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Vehicle saved successfully with raw response");
        finish();
    }

    private void handleSuccessResponse(ApiResponse<Vehicle> apiResponse) {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        if (apiResponse.isSuccess()) {
            String message = isEditMode ? "Kendaraan berhasil diupdate" : "Kendaraan berhasil ditambahkan";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Vehicle saved successfully");
            finish();
        } else {
            String errorMessage = apiResponse.getError() != null ? apiResponse.getError() :
                (isEditMode ? "Gagal mengupdate kendaraan" : "Gagal menambahkan kendaraan");
            Log.e(TAG, "API Error: " + errorMessage);
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAllFormatsFailed(Response<ResponseBody> lastResponse) {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        try {
            String errorBody = lastResponse.errorBody() != null ? lastResponse.errorBody().string() : "No error body";
            Log.e(TAG, "All formats failed. Last HTTP Error " + lastResponse.code() + ": " + lastResponse.message() + ", Body: " + errorBody);

            String message = "Gagal menyimpan kendaraan. ";
            if (lastResponse.code() == 400) {
                message += "Data tidak valid atau format tidak sesuai dengan server.";
            } else if (lastResponse.code() == 401) {
                message += "Sesi telah berakhir. Silakan login ulang.";
            } else if (lastResponse.code() == 422) {
                message += "Data tidak sesuai dengan validasi server.";
            } else if (lastResponse.code() == 500) {
                message += "Terjadi kesalahan pada server.";
            } else {
                message += "Error " + lastResponse.code() + ": " + lastResponse.message();
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error reading final error body: " + e.getMessage());
            handleFinalError("Tidak dapat menyimpan kendaraan. Semua format request gagal.");
        }
    }

    private void handleErrorResponse(Response<ApiResponse<Vehicle>> response) {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
            Log.e(TAG, "HTTP Error " + response.code() + ": " + response.message() + ", Body: " + errorBody);

            String message = "Error " + response.code() + ": ";
            if (response.code() == 400) {
                message += "Data tidak valid. Periksa kembali input Anda.";
            } else if (response.code() == 401) {
                message += "Sesi telah berakhir. Silakan login ulang.";
            } else if (response.code() == 422) {
                message += "Data tidak sesuai format yang diharapkan.";
            } else {
                message += response.message();
            }

            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error reading error body: " + e.getMessage());
            handleFinalError(isEditMode ? "Gagal mengupdate kendaraan" : "Gagal menambahkan kendaraan");
        }
    }

    private void handleNetworkError(Throwable t) {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        Log.e(TAG, "Network error: " + t.getMessage(), t);
        Toast.makeText(this, "Error jaringan: " + t.getMessage(), Toast.LENGTH_LONG).show();
    }

    private void handleFinalError(String message) {
        btnSave.setEnabled(true);
        btnSave.setText("Simpan");

        Log.e(TAG, "Final error: " + message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
