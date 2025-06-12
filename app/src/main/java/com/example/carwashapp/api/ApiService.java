package com.example.carwashapp.api;

import com.example.carwashapp.models.*;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;
import java.util.List;

public interface ApiService {

    // Authentication Endpoints
    @POST("api/users/register")
    Call<ApiResponse<User>> registerUser(@Body RegisterRequest request);

    @POST("api/users/login")
    Call<LoginResponse> loginUser(@Body LoginRequest request);

    @GET("api/users/profile")
    Call<ApiResponse<User>> getUserProfile(@Header("Authorization") String token);

    @PUT("api/users/profile")
    Call<ApiResponse<User>> updateProfile(@Header("Authorization") String token, @Body User user);

    // Services Endpoints
    @GET("api/services")
    Call<ApiResponse<List<Service>>> getServices();

    @GET("api/services/{id}")
    Call<ApiResponse<Service>> getServiceDetails(@Path("id") String serviceId);

    // Admin Services Endpoints
    @POST("api/services")
    Call<ApiResponse<Service>> createService(@Header("Authorization") String token, @Body Service service);

    @PUT("api/services/{id}")
    Call<ApiResponse<Service>> updateService(@Header("Authorization") String token, @Path("id") String serviceId, @Body Service service);

    @DELETE("api/services/{id}")
    Call<ApiResponse<Void>> deleteService(@Header("Authorization") String token, @Path("id") String serviceId);

    // Admin Bookings Endpoints
    @GET("api/admin/bookings")
    Call<ApiResponse<List<Booking>>> getAllBookings(@Header("Authorization") String token);

    @PATCH("api/bookings/{id}/status")
    Call<ApiResponse<Booking>> updateBookingStatus(@Header("Authorization") String token, @Path("id") String bookingId, @Body BookingStatusRequest request);

    // Vehicles Endpoints
    @GET("api/vehicles")
    Call<ApiResponse<List<Vehicle>>> getVehicles(@Header("Authorization") String token);

    @POST("api/vehicles")
    Call<ApiResponse<Vehicle>> addVehicle(@Header("Authorization") String token, @Body VehicleRequest vehicle);

    @GET("api/vehicles/{id}")
    Call<ApiResponse<Vehicle>> getVehicleDetails(@Header("Authorization") String token, @Path("id") String vehicleId);

    @PUT("api/vehicles/{id}")
    Call<ApiResponse<Vehicle>> updateVehicle(@Header("Authorization") String token, @Path("id") String vehicleId, @Body VehicleRequest vehicle);

    @DELETE("api/vehicles/{id}")
    Call<ApiResponse<Void>> deleteVehicle(@Header("Authorization") String token, @Path("id") String vehicleId);

    // Bookings Endpoints
    @GET("api/bookings/available-slots")
    Call<ApiResponse<List<String>>> getAvailableSlots(@Query("date") String date);

    @POST("api/bookings")
    Call<ApiResponse<Booking>> createBooking(@Header("Authorization") String token, @Body CreateBookingRequest request);

    @GET("api/bookings")
    Call<ApiResponse<List<Booking>>> getUserBookings(@Header("Authorization") String token);

    @GET("api/admin/bookings")
    Call<ApiResponse<List<Booking>>> getAllBookingsForAdmin(@Header("Authorization") String token);

    @GET("api/bookings/{id}")
    Call<ApiResponse<Booking>> getBookingDetails(@Header("Authorization") String token, @Path("id") String bookingId);

    @PATCH("api/bookings/{id}/cancel")
    Call<ApiResponse<Booking>> cancelBooking(@Header("Authorization") String token, @Path("id") String bookingId);

    @PATCH("api/admin/bookings/{id}/status")
    Call<ApiResponse<Booking>> updateBookingStatus(@Header("Authorization") String token, @Path("id") String bookingId, @Body UpdateBookingStatusRequest request);

    // Transactions Endpoints
    @POST("api/transactions")
    Call<ApiResponse<Transaction>> createTransaction(@Header("Authorization") String token, @Body Transaction transaction);

    @GET("api/transactions")
    Call<ApiResponse<List<Transaction>>> getUserTransactions(@Header("Authorization") String token);

    @GET("api/transactions/{id}")
    Call<ApiResponse<Transaction>> getTransactionDetails(@Header("Authorization") String token, @Path("id") String transactionId);

    @PATCH("api/transactions/{id}/confirm")
    Call<ApiResponse<Transaction>> confirmPayment(@Header("Authorization") String token, @Path("id") String transactionId);

    // Reviews Endpoints
    @POST("api/reviews")
    Call<ApiResponse<Review>> createReview(@Header("Authorization") String token, @Body Review review);

    @GET("api/reviews")
    Call<ApiResponse<List<Review>>> getUserReviews(@Header("Authorization") String token);

    @GET("api/reviews/all")
    Call<ApiResponse<List<Review>>> getAllReviews();

    @PUT("api/reviews/{id}")
    Call<ApiResponse<Review>> updateReview(@Header("Authorization") String token, @Path("id") String reviewId, @Body Review review);

    @DELETE("api/reviews/{id}")
    Call<ApiResponse<Void>> deleteReview(@Header("Authorization") String token, @Path("id") String reviewId);

    @GET("api/reviews/booking/{bookingId}")
    Call<ApiResponse<Review>> getReviewByBooking(@Header("Authorization") String token, @Path("bookingId") String bookingId);

    // Alternative endpoints for flexible response handling
    @GET("api/services")
    Call<ResponseBody> getServicesRaw();

    @GET("api/vehicles")
    Call<ResponseBody> getVehiclesRaw(@Header("Authorization") String token);

    @GET("api/bookings")
    Call<ResponseBody> getUserBookingsRaw(@Header("Authorization") String token);

    @GET("api/transactions")
    Call<ResponseBody> getUserTransactionsRaw(@Header("Authorization") String token);

    @GET("api/reviews")
    Call<ResponseBody> getUserReviewsRaw(@Header("Authorization") String token);

    // Raw vehicle endpoints for flexible request handling
    @POST("api/vehicles")
    Call<ResponseBody> addVehicleRaw(@Header("Authorization") String token, @Body Object vehicle);

    @PUT("api/vehicles/{id}")
    Call<ResponseBody> updateVehicleRaw(@Header("Authorization") String token, @Path("id") String vehicleId, @Body Object vehicle);

    // Booking endpoint with nested response structure
    @GET("api/bookings")
    Call<BookingResponse> getUserBookingsNested(@Header("Authorization") String token);

    // Admin booking endpoint with nested response structure
    @GET("api/admin/bookings")
    Call<BookingResponse> getAdminBookingsNested(@Header("Authorization") String token);

}