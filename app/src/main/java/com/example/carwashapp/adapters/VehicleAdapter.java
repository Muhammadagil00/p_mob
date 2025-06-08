package com.example.carwashapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.models.Vehicle;

import java.util.List;

public class VehicleAdapter extends RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder> {
    private List<Vehicle> vehicleList;
    private OnVehicleClickListener onVehicleClickListener;

    public interface OnVehicleClickListener {
        void onVehicleClick(Vehicle vehicle);
    }

    public VehicleAdapter(List<Vehicle> vehicleList) {
        this.vehicleList = vehicleList;
    }

    public void setOnVehicleClickListener(OnVehicleClickListener listener) {
        this.onVehicleClickListener = listener;
    }

    @NonNull
    @Override
    public VehicleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_vehicle, parent, false);
        return new VehicleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VehicleViewHolder holder, int position) {
        Vehicle vehicle = vehicleList.get(position);
        holder.bind(vehicle);
    }

    @Override
    public int getItemCount() {
        return vehicleList.size();
    }

    class VehicleViewHolder extends RecyclerView.ViewHolder {
        private TextView tvVehicleName;
        private TextView tvPlateNumber;
        private TextView tvVehicleType;
        private TextView tvYear;

        public VehicleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvVehicleName = itemView.findViewById(R.id.tvVehicleName);
            tvPlateNumber = itemView.findViewById(R.id.tvPlateNumber);
            tvVehicleType = itemView.findViewById(R.id.tvVehicleType);
            tvYear = itemView.findViewById(R.id.tvYear);

            itemView.setOnClickListener(v -> {
                if (onVehicleClickListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        onVehicleClickListener.onVehicleClick(vehicleList.get(position));
                    }
                }
            });
        }

        public void bind(Vehicle vehicle) {
            tvVehicleName.setText(vehicle.getBrand() + " " + vehicle.getModel());
            tvPlateNumber.setText(vehicle.getPlateNumber());
            tvVehicleType.setText(vehicle.getVehicleType().equals("car") ? "Mobil" : "Motor");
            tvYear.setText(String.valueOf(vehicle.getYear()));
        }
    }
}
