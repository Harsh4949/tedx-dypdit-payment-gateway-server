package com.example.tedxpaymnetserver;

import android.content.Context;
import android.content.SharedPreferences;

public class SendAndReceivePreferences {
    public static void storeData(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String retriveData(Context context, String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, value);
    }

    public static void setboolean(Context context, String key, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, b);
        editor.apply();
    }

    public static boolean getboolean(Context context, String key, boolean b) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("MyPreferences", Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(key, b);
    }

}
