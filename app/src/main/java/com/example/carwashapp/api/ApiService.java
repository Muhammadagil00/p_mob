package com.example.carwashapp.api;


import com.example.carwashapp.models.LoginResponse;
import com.example.carwashapp.models.User;
import com.example.carwashapp.models.Booking;
import com.example.carwashapp.models.Service;
import com.example.carwashapp.models.BookingListResponse;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.GET;
import retrofit2.http.Path;
import java.util.List;

public interface ApiService {

    @POST("api/users/register")
    Call<User> registerUser(@Body User user);

    @POST("api/users/login")
    Call<LoginResponse> loginUser(@Body User user);

    @POST("api/bookings")
    Call<Booking> createBooking(@Body Booking booking);

    @GET("api/services")
    Call<List<Service>> getServices();

    @GET("api/bookings")
    Call<BookingListResponse> getBookings();

    @GET("api/users/{userId}/bookings")
    Call<BookingListResponse> getUserBookings(@Path("userId") String userId);

}