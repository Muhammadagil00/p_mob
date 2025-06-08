package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.LoginRequest;
import com.example.carwashapp.models.LoginResponse;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.models.User;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private SessionManager sessionManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        sessionManager = new SessionManager(this);
        apiService = ApiClient.getApiService();

        // Test API connection
        testApiConnection();

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Tambahkan button untuk test dengan user default (untuk debugging)
        // createTestUser(); // Uncomment jika ingin membuat test user
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (email.isEmpty()) {
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);



        LoginRequest loginRequest = new LoginRequest(email, password);
        Log.d(TAG, "Attempting login with email: " + email);
        Log.d(TAG, "API Base URL: https://pb-carwash-backend-production.up.railway.app/");
        Log.d(TAG, "Login endpoint: api/users/login");

        apiService.loginUser(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Log.d(TAG, "Login response received. Code: " + response.code());

                if (response.isSuccessful()) {
                    if (response.body() != null) {
                        LoginResponse loginResponse = response.body();

                        if (loginResponse.getUser() != null && loginResponse.getToken() != null) {
                            handleSuccessfulLogin(loginResponse);
                        } else {
                            Log.e(TAG, "Login response incomplete - user or token is null");
                            Toast.makeText(LoginActivity.this, "Data login tidak lengkap", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Response body is null");
                        Toast.makeText(LoginActivity.this, "Response kosong dari server", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // HTTP error (4xx, 5xx)
                    Log.e(TAG, "HTTP Error - Code: " + response.code() + ", Message: " + response.message());

                    if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                    } else if (response.code() == 500) {
                        Toast.makeText(LoginActivity.this, "Server bermasalah, coba lagi nanti", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(LoginActivity.this, "Login gagal (Error " + response.code() + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e(TAG, "Login network error: " + t.getMessage(), t);
                Toast.makeText(LoginActivity.this, "Login gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private void handleSuccessfulLogin(LoginResponse loginResponse) {
        User loggedInUser = loginResponse.getUser();
        String token = loginResponse.getToken();
        Log.d(TAG, "Login successful for user: " + loggedInUser.getName());

        sessionManager.createLoginSession(
                loggedInUser.getId(),
                loggedInUser.getName(),
                loggedInUser.getEmail(),
                loggedInUser.getRole(),
                token
        );

        Toast.makeText(LoginActivity.this, "Login berhasil! Selamat datang " + loggedInUser.getName(), Toast.LENGTH_SHORT).show();
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
        finish();
    }

    private void testApiConnection() {
        Log.d(TAG, "Testing API connection...");
        // Test dengan endpoint services yang tidak memerlukan auth
        apiService.getServices().enqueue(new Callback<ApiResponse<List<com.example.carwashapp.models.Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<com.example.carwashapp.models.Service>>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "API connection test successful - Code: " + response.code());
                } else {
                    Log.e(TAG, "API connection test failed - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<com.example.carwashapp.models.Service>>> call, Throwable t) {
                Log.e(TAG, "API connection test network error: " + t.getMessage(), t);
            }
        });
    }

    // Method untuk membuat test user (untuk debugging)
    private void createTestUser() {
        Log.d(TAG, "Creating test user...");
        com.example.carwashapp.models.RegisterRequest testUser = new com.example.carwashapp.models.RegisterRequest(
                "Test User",
                "test@test.com",
                "test123"
        );

        apiService.registerUser(testUser).enqueue(new Callback<ApiResponse<com.example.carwashapp.models.User>>() {
            @Override
            public void onResponse(Call<ApiResponse<com.example.carwashapp.models.User>> call, Response<ApiResponse<com.example.carwashapp.models.User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<com.example.carwashapp.models.User> apiResponse = response.body();
                    if (apiResponse.isSuccess()) {
                        Log.d(TAG, "Test user created successfully");
                        Toast.makeText(LoginActivity.this, "Test user created: test@test.com / test123", Toast.LENGTH_LONG).show();
                    } else {
                        Log.d(TAG, "Test user already exists or creation failed: " + apiResponse.getError());
                    }
                } else {
                    Log.e(TAG, "Failed to create test user - Code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<com.example.carwashapp.models.User>> call, Throwable t) {
                Log.e(TAG, "Test user creation network error: " + t.getMessage());
            }
        });
    }
}
