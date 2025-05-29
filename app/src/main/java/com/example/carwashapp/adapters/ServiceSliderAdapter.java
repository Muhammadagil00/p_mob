package com.example.carwashapp.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.carwashapp.R;
import com.example.carwashapp.models.Service;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class ServiceSliderAdapter extends RecyclerView.Adapter<ServiceSliderAdapter.ServiceViewHolder> {
    
    private List<Service> serviceList;
    private OnServiceClickListener listener;

    public interface OnServiceClickListener {
        void onServiceClick(Service service);
    }

    public ServiceSliderAdapter(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public void setOnServiceClickListener(OnServiceClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ServiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service_slider, parent, false);
        return new ServiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ServiceViewHolder holder, int position) {
        Service service = serviceList.get(position);
        
        holder.tvServiceName.setText(service.getName() != null ? service.getName() : "Service");
        holder.tvServiceDescription.setText(service.getDescription() != null ? service.getDescription() : "Deskripsi layanan");
        
        // Format price to Indonesian Rupiah
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formattedPrice = formatter.format(service.getPrice());
        holder.tvServicePrice.setText(formattedPrice);
        
        // Set service icon based on service name
        setServiceIcon(holder.ivServiceIcon, service.getName());
        
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onServiceClick(service);
            }
        });
    }

    private void setServiceIcon(ImageView imageView, String serviceName) {
        // Set different icons based on service name
        if (serviceName != null) {
            String name = serviceName.toLowerCase();
            if (name.contains("basic") || name.contains("cuci")) {
                imageView.setImageResource(R.drawable.ic_car_wash);
            } else if (name.contains("premium") || name.contains("lengkap")) {
                imageView.setImageResource(R.drawable.ic_car_wash);
            } else if (name.contains("wax") || name.contains("poles")) {
                imageView.setImageResource(R.drawable.ic_car_wash);
            } else {
                imageView.setImageResource(R.drawable.ic_car_wash);
            }
        } else {
            imageView.setImageResource(R.drawable.ic_car_wash);
        }
    }

    @Override
    public int getItemCount() {
        return serviceList != null ? serviceList.size() : 0;
    }

    public void updateServices(List<Service> newServices) {
        this.serviceList = newServices;
        notifyDataSetChanged();
    }

    static class ServiceViewHolder extends RecyclerView.ViewHolder {
        ImageView ivServiceIcon;
        TextView tvServiceName;
        TextView tvServiceDescription;
        TextView tvServicePrice;

        public ServiceViewHolder(@NonNull View itemView) {
            super(itemView);
            ivServiceIcon = itemView.findViewById(R.id.ivServiceIcon);
            tvServiceName = itemView.findViewById(R.id.tvServiceName);
            tvServiceDescription = itemView.findViewById(R.id.tvServiceDescription);
            tvServicePrice = itemView.findViewById(R.id.tvServicePrice);
        }
    }
}
