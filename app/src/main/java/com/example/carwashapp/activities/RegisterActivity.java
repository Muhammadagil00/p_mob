package com.example.carwashapp.activities;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;


import com.example.carwashapp.R;
import com.example.carwashapp.api.ApiService;
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

        apiService = ApiClient.getApiService(getApplicationContext());

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

        User user = new User(name, email, password);

        apiService.registerUser(user).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()){
                    Toast.makeText(RegisterActivity.this, "Registrasi berhasil! Silakan login.", Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, "Registrasi gagal: Email sudah terdaftar.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Gagal mendaftar: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}