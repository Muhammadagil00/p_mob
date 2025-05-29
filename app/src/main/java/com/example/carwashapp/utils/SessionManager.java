package com.example.carwashapp.utils;


import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "CarWashAppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_USER_ROLE = "userRole";
    private static final String KEY_TOKEN = "token";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SessionManager(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void createLoginSession(String userId, String userName, String userEmail, String userRole, String token){
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_NAME, userName);
        editor.putString(KEY_USER_EMAIL, userEmail);
        editor.putString(KEY_USER_ROLE, userRole);
        editor.putString(KEY_TOKEN, token);
        editor.apply();
    }

    public void logoutUser(){
        editor.clear();
        editor.apply();
    }

    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public String getUserId(){ return sharedPreferences.getString(KEY_USER_ID, null); }
    public String getUserName(){ return sharedPreferences.getString(KEY_USER_NAME, null); }
    public String getUserEmail(){ return sharedPreferences.getString(KEY_USER_EMAIL, null); }
    public String getUserRole(){ return sharedPreferences.getString(KEY_USER_ROLE, null); }
    public String getToken(){ return sharedPreferences.getString(KEY_TOKEN, null); }

    public boolean isAdmin(){ return "admin".equals(getUserRole()); }
}