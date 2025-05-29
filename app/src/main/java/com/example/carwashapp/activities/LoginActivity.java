package com.example.carwashapp.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.LoginResponse;
import com.example.carwashapp.models.User;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

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
        apiService = ApiClient.getApiService(getApplicationContext());

        if (sessionManager.isLoggedIn()) {
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        }

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
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

        apiService.loginUser(user).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();
                    User loggedInUser = loginResponse.getUser();
                    String token = loginResponse.getToken();

                    sessionManager.createLoginSession(
                            loggedInUser.getId(),
                            loggedInUser.getName(),
                            loggedInUser.getEmail(),
                            loggedInUser.getRole(),
                            token
                    );

                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Email atau password salah", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Login gagal: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
