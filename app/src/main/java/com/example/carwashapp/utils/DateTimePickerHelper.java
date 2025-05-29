package com.example.carwashapp.utils;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class DateTimePickerHelper {
    
    private Context context;
    private Calendar calendar;
    private SimpleDateFormat dateFormat;
    private SimpleDateFormat timeFormat;
    
    public DateTimePickerHelper(Context context) {
        this.context = context;
        this.calendar = Calendar.getInstance();
        this.dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }
    
    public void showDatePicker(EditText editText) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                editText.setText(dateFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        
        // Customize dialog appearance
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }
    
    public void showTimePicker(EditText editText) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                editText.setText(timeFormat.format(calendar.getTime()));
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // 24 hour format
        );
        
        // Customize dialog appearance
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        timePickerDialog.show();
    }
    
    public void showDatePickerWithCallback(DatePickerCallback callback) {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            (view, year, month, dayOfMonth) -> {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                String formattedDate = dateFormat.format(calendar.getTime());
                callback.onDateSelected(formattedDate, calendar.getTime());
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        );
        
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        datePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        datePickerDialog.show();
    }
    
    public void showTimePickerWithCallback(TimePickerCallback callback) {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
            context,
            android.R.style.Theme_Holo_Light_Dialog_MinWidth,
            (view, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String formattedTime = timeFormat.format(calendar.getTime());
                callback.onTimeSelected(formattedTime, hourOfDay, minute);
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        );
        
        timePickerDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        timePickerDialog.show();
    }
    
    public interface DatePickerCallback {
        void onDateSelected(String formattedDate, java.util.Date date);
    }
    
    public interface TimePickerCallback {
        void onTimeSelected(String formattedTime, int hour, int minute);
    }
}
