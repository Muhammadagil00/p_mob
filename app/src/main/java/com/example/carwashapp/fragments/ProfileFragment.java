package com.example.carwashapp.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.carwashapp.R;
import com.example.carwashapp.activities.BookingActivity;
import com.example.carwashapp.activities.LoginActivity;
import com.example.carwashapp.utils.SessionManager;

public class ProfileFragment extends Fragment {

    private SessionManager sessionManager;
    private TextView tvUserName, tvUserEmail, tvUserRole;
    private LinearLayout btnNewBooking, btnSettings;
    private Button btnLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        initViews(view);
        loadUserData();

        return view;
    }

    private void initViews(View view) {
        sessionManager = new SessionManager(requireContext());

        tvUserName = view.findViewById(R.id.tvUserName);
        tvUserEmail = view.findViewById(R.id.tvUserEmail);
        tvUserRole = view.findViewById(R.id.tvUserRole);
        btnNewBooking = view.findViewById(R.id.btnNewBooking);
        btnSettings = view.findViewById(R.id.btnSettings);
        btnLogout = view.findViewById(R.id.btnLogout);

        // Set click listeners
        if (btnNewBooking != null) {
            btnNewBooking.setOnClickListener(v -> {
                Intent intent = new Intent(requireContext(), BookingActivity.class);
                startActivity(intent);
            });
        }

        if (btnSettings != null) {
            btnSettings.setOnClickListener(v -> {
                // TODO: Implement settings
                Toast.makeText(requireContext(), "Settings akan segera tersedia", Toast.LENGTH_SHORT).show();
            });
        }

        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> logout());
        }
    }

    private void loadUserData() {
        if (tvUserName != null) {
            tvUserName.setText(sessionManager.getUserName() != null ? sessionManager.getUserName() : "User");
        }

        if (tvUserEmail != null) {
            tvUserEmail.setText(sessionManager.getUserEmail() != null ? sessionManager.getUserEmail() : "email@example.com");
        }

        if (tvUserRole != null) {
            String role = sessionManager.getUserRole();
            if ("admin".equals(role)) {
                tvUserRole.setText("Administrator");
                tvUserRole.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            } else {
                tvUserRole.setText("User");
                tvUserRole.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
            }
        }
    }

    private void logout() {
        sessionManager.logoutUser();
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
