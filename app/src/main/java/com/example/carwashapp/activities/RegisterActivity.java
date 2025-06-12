package com.example.carwashapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.RegisterRequest;
import com.example.carwashapp.models.User;
import com.example.carwashapp.utils.ApiClient;
import com.google.android.material.textfield.TextInputEditText;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {
    private TextInputEditText etName, etEmail, etPassword;
    private Button btnRegister;
    private TextView tvLogin;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.et_name);
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnRegister = findViewById(R.id.btn_register);
        tvLogin = findViewById(R.id.tv_login);

        apiService = ApiClient.getApiService();

        btnRegister.setOnClickListener(v -> registerUser());

        tvLogin.setOnClickListener(v -> finish());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (name.isEmpty()){
            etName.setError("Nama tidak boleh kosong");
            etName.requestFocus();
            return;
        }
        if (email.isEmpty()){
            etEmail.setError("Email tidak boleh kosong");
            etEmail.requestFocus();
            return;
        }
        if (password.isEmpty()){
            etPassword.setError("Password tidak boleh kosong");
            etPassword.requestFocus();
            return;
        }

        RegisterRequest registerRequest = new RegisterRequest(name, email, password);

        Log.d("RegisterActivity", "Attempting registration with email: " + email);
        Log.d("RegisterActivity", "API Base URL: " + ApiClient.getBaseUrl());

        apiService.registerUser(registerRequest).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                Log.d("RegisterActivity", "Register response received. Code: " + response.code());

                if (response.isSuccessful()) {
                    // Check for successful HTTP status codes (200, 201, etc.)
                    if (response.code() == 201 || response.code() == 200) {
                        Log.d("RegisterActivity", "✅ Registration successful with HTTP " + response.code());
                        Toast.makeText(RegisterActivity.this, "✅ Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }

                    // Try to parse standard ApiResponse format
                    if (response.body() != null) {
                        ApiResponse<User> apiResponse = response.body();
                        Log.d("RegisterActivity", "ApiResponse isSuccess: " + apiResponse.isSuccess());
                        if (apiResponse.isSuccess()) {
                            Log.d("RegisterActivity", "✅ Registration successful via ApiResponse");
                            Toast.makeText(RegisterActivity.this, "✅ Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                            finish();
                        } else {
                            String errorMessage = apiResponse.getError() != null ? apiResponse.getError() : "Registrasi gagal";
                            Log.d("RegisterActivity", "❌ Registration failed via ApiResponse: " + errorMessage);
                            Toast.makeText(RegisterActivity.this, "❌ " + errorMessage, Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d("RegisterActivity", "✅ Registration successful (empty response body)");
                        Toast.makeText(RegisterActivity.this, "✅ Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                } else {
                    // Handle HTTP error codes
                    Log.e("RegisterActivity", "❌ Registration failed with HTTP " + response.code());
                    String errorMessage = getRegisterErrorMessage(response.code());
                    Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                Log.e("RegisterActivity", "❌ Registration network error: " + t.getMessage(), t);
                Toast.makeText(RegisterActivity.this, "🌐 Gagal mendaftar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getRegisterErrorMessage(int httpCode) {
        switch (httpCode) {
            case 400:
                return "❌ Data registrasi tidak valid. Periksa kembali form Anda.";
            case 409:
                return "❌ Email sudah terdaftar. Gunakan email lain atau login.";
            case 422:
                return "❌ Format data tidak sesuai. Periksa email dan password.";
            case 429:
                return "⚠️ Terlalu banyak percobaan. Coba lagi dalam beberapa menit.";
            case 500:
                return "🔧 Server bermasalah. Silakan coba lagi nanti.";
            default:
                return "❌ Registrasi gagal (Kode: " + httpCode + "). Silakan coba lagi.";
        }
    }
}