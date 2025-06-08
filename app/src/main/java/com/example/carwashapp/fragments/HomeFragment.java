package com.example.carwashapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.activities.BookingActivity;
import com.example.carwashapp.activities.MainActivity;
import com.example.carwashapp.adapters.ServiceSliderAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";

    private SessionManager sessionManager;
    private ApiService apiService;
    private TextView tvWelcome;
    private LinearLayout btnGoBooking, btnGoHistory;
    private RecyclerView rvServices;
    private ServiceSliderAdapter serviceAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        initViews(view);
        loadServices();

        return view;
    }

    private void initViews(View view) {
        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getApiService();

        tvWelcome = view.findViewById(R.id.tv_welcome);
        btnGoBooking = view.findViewById(R.id.btnGoBooking);
        btnGoHistory = view.findViewById(R.id.btnGoHistory);
        rvServices = view.findViewById(R.id.rvServices);

        // Set welcome message
        String userName = sessionManager.getUserName();
        if (tvWelcome != null) {
            tvWelcome.setText("Selamat datang, " + userName + "!");
        }

        // Setup service slider
        if (rvServices != null) {
            LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
            rvServices.setLayoutManager(layoutManager);
        }

        // Set click listeners
        if (btnGoBooking != null) {
            btnGoBooking.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BookingActivity.class);
                startActivity(intent);
            });
        }

        if (btnGoHistory != null) {
            btnGoHistory.setOnClickListener(v -> {
                // Navigate to booking tab
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).navigateToBooking();
                }
            });
        }
    }

    private void loadServices() {
        apiService.getServices().enqueue(new Callback<ApiResponse<List<Service>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Service>>> call, Response<ApiResponse<List<Service>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Service>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Service> services = apiResponse.getData();
                        serviceAdapter = new ServiceSliderAdapter(services);
                        serviceAdapter.setOnServiceClickListener(service -> {
                            // Handle service click - go to booking with selected service
                            Intent intent = new Intent(requireContext(), BookingActivity.class);
                            intent.putExtra("selected_service_id", service.getId());
                            intent.putExtra("selected_service_name", service.getName());
                            startActivity(intent);
                        });
                        if (rvServices != null) {
                            rvServices.setAdapter(serviceAdapter);
                        }
                    } else {
                        Log.e(TAG, "API mengembalikan error: " + apiResponse.getError());
                        Toast.makeText(requireContext(), "Gagal memuat layanan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e(TAG, "Gagal memuat layanan: " + response.message());
                    Toast.makeText(requireContext(), "Gagal memuat layanan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Service>>> call, Throwable t) {
                Log.e(TAG, "Error jaringan: " + t.getMessage());
                Toast.makeText(requireContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
