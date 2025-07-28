package com.example.tedxpaymnetserver;

import android.Manifest;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresPermission;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NetworkBufferedSender {

    private static final String TAG = "BufferedSender";
    private static final List<TransactionData> bufferList = new ArrayList<>();
    private static final String PREF_KEY = "transaction_buffer_list";

    public static void trySend(Context context, TransactionData data) {
        bufferList.add(data);
        saveBufferToPreferences(context); // Always store it first

        if (NetworkUtils.isNetworkAvailable(context)) {
            resendBuffered(context); // Try to send the entire buffer (including this new one)
        } else {
            Log.d(TAG, "Offline: Saved to buffer");
        }
    }

    private static void send(Context context, TransactionData data, boolean fromBuffer) {
        ApiService apiService = RetrofitClient.getApiService(context);
        String requiredRoot = SendAndReceivePreferences.retriveData(context, "reqiredroot", "");

        apiService.sendTransaction(requiredRoot, data).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, (fromBuffer ? "Sent from buffer: " : "Sent live: ") + data.refNo);
                    bufferList.remove(data);
                    saveBufferToPreferences(context); // Update buffer
                } else {
                    Log.e(TAG, "Failed response code: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.e(TAG, "Failed to send data: " + t.getMessage());
                if (!fromBuffer) {
                    bufferList.add(data); // Save back if live failed
                    saveBufferToPreferences(context);
                }
            }
        });
    }

    public static void resendBufferedWithCallback(Context context, Runnable onComplete) {
        loadBufferFromPreferences(context);

        if (bufferList.isEmpty()) {
            Log.d(TAG, "No buffered data to resend.");
            if (onComplete != null) onComplete.run();
            return;
        }

        List<TransactionData> copyList = new ArrayList<>(bufferList);
        final int[] completedCount = {0};

        for (TransactionData data : copyList) {
            ApiService apiService = RetrofitClient.getApiService(context);
            String requiredRoot = SendAndReceivePreferences.retriveData(context, "reqiredroot", "");

            apiService.sendTransaction(requiredRoot, data).enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        bufferList.remove(data);
                        saveBufferToPreferences(context);
                        Log.d(TAG, "Sent from buffer: " + data.refNo);
                    } else {
                        Log.e(TAG, "Response failed: " + response.code());
                    }
                    checkComplete();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Log.e(TAG, "Send failed: " + t.getMessage());
                    checkComplete();
                }

                private void checkComplete() {
                    completedCount[0]++;
                    if (completedCount[0] == copyList.size()) {
                        if (onComplete != null) onComplete.run();
                    }
                }
            });
        }
    }

    public static void resendBuffered(Context context) {
        resendBufferedWithCallback(context, null);
    }

    public static boolean isNetworkAvailable(Context context) {
        return NetworkUtils.isNetworkAvailable(context);
    }

    static class NetworkUtils {

        @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
        @SuppressWarnings("deprecation") // Suppress warning for older versions
        public static boolean isNetworkAvailable(Context context) {
            if (context == null) {
                return false;
            }
            ConnectivityManager cm = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            if (cm == null) {
                return false;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // M is API 23
                Network activeNetwork = cm.getActiveNetwork();
                if (activeNetwork == null) {
                    return false;
                }
                NetworkCapabilities networkCapabilities = cm.getNetworkCapabilities(activeNetwork);
                if (networkCapabilities == null) {
                    return false;
                }
                // Check for internet capability and validated (actually connected to internet)
                return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                        && networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
            } else {
                // For older versions, use the deprecated method
                NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();
                return activeNetworkInfo != null && activeNetworkInfo.isConnected();
            }
        }
    }


    private static void saveBufferToPreferences(Context context) {
        String json = new Gson().toJson(bufferList);
        context.getSharedPreferences("buffer_store", Context.MODE_PRIVATE)
                .edit()
                .putString(PREF_KEY, json)
                .apply();
    }

    private static void loadBufferFromPreferences(Context context) {
        String json = context.getSharedPreferences("buffer_store", Context.MODE_PRIVATE)
                .getString(PREF_KEY, null);

        if (json != null) {
            Type type = new TypeToken<List<TransactionData>>() {
            }.getType();
            List<TransactionData> loaded = new Gson().fromJson(json, type);
            bufferList.clear();
            bufferList.addAll(loaded);
        }
    }

    public static int getBufferedCount(Context context) {
        loadBufferFromPreferences(context);
        return bufferList.size();
    }


}
