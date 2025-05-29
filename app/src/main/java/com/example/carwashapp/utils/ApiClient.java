package com.example.carwashapp.utils;


import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import com.example.carwashapp.utils.SessionManager;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;
import java.io.IOException;


import com.example.carwashapp.api.ApiService;

public class ApiClient {
    private static final String BASE_URL = "https://pb-carwash-backend-production.up.railway.app/";
    private static Retrofit retrofit = null;

    public static ApiService getApiService(Context context) {
        // Always recreate the client to ensure fresh authentication headers
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();
                        SessionManager sessionManager = new SessionManager(context);
                        String token = sessionManager.getToken();

                        Request.Builder builder = original.newBuilder();

                        if (token != null && !token.isEmpty()) {
                            builder.header("Authorization", "Bearer " + token);
                            android.util.Log.d("ApiClient", "Adding auth header with token: " + token.substring(0, Math.min(10, token.length())) + "...");
                        } else {
                            android.util.Log.w("ApiClient", "No token available for request to: " + original.url());
                        }

                        // Add common headers
                        builder.header("Content-Type", "application/json");
                        builder.header("Accept", "application/json");

                        Request request = builder.build();
                        return chain.proceed(request);
                    }
                })
                .build();

        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }

        return retrofit.create(ApiService.class);
    }
}