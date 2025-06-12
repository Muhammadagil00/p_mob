package com.example.carwashapp.fragments;

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
import com.example.carwashapp.adapters.RiwayatBookingAdapter;
import com.example.carwashapp.api.ApiService;
import com.example.carwashapp.models.ApiResponse;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.utils.ApiClient;
import com.example.carwashapp.utils.SessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingFragment extends Fragment {
    private static final String TAG = "BookingFragment";
    
    private SessionManager sessionManager;
    private ApiService apiService;
    private RecyclerView rvBookings;
    private LinearLayout tvNoBookings;
    private RiwayatBookingAdapter bookingAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_booking, container, false);
        
        initViews(view);
        loadBookings();
        
        return view;
    }

    private void initViews(View view) {
        sessionManager = new SessionManager(requireContext());
        apiService = ApiClient.getApiService();
        
        rvBookings = view.findViewById(R.id.rvBookings);
        tvNoBookings = view.findViewById(R.id.tvNoBookings);

        // Setup RecyclerView
        if (rvBookings != null) {
            rvBookings.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
    }

    private void loadBookings() {
        String token = sessionManager.getToken();
        if (token == null) {
            showNoBookings();
            return;
        }

        apiService.getUserBookings(ApiClient.createAuthHeader(token)).enqueue(new Callback<ApiResponse<List<Booking>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Booking>>> call, Response<ApiResponse<List<Booking>>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<List<Booking>> apiResponse = response.body();
                    if (apiResponse.isSuccess() && apiResponse.getData() != null) {
                        List<Booking> bookings = apiResponse.getData();
                        if (!bookings.isEmpty()) {
                            showBookings(bookings);
                        } else {
                            showNoBookings();
                        }
                    } else {
                        Log.e(TAG, "API mengembalikan error: " + apiResponse.getError());
                        showNoBookings();
                    }
                } else {
                    Log.e(TAG, "Gagal memuat booking: " + response.message());
                    showNoBookings();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Booking>>> call, Throwable t) {
                Log.e(TAG, "Error jaringan: " + t.getMessage());
                Toast.makeText(requireContext(), "Error koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showNoBookings();
            }
        });
    }

    private void showBookings(List<Booking> bookings) {
        if (rvBookings != null && tvNoBookings != null) {
            rvBookings.setVisibility(View.VISIBLE);
            tvNoBookings.setVisibility(View.GONE);
            
            bookingAdapter = new RiwayatBookingAdapter(bookings);
            rvBookings.setAdapter(bookingAdapter);
        }
    }

    private void showNoBookings() {
        if (rvBookings != null && tvNoBookings != null) {
            rvBookings.setVisibility(View.GONE);
            tvNoBookings.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadBookings(); // Refresh data when fragment becomes visible
    }
}
