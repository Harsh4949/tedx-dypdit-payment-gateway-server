package com.example.tedxpaymnetserver;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

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

        if (isNetworkAvailable(context)) {
            resendBuffered(context); // Try to send the entire buffer (including this new one)
        } else {
            Log.d(TAG, "Offline: Saved to buffer");
        }
    }


    private static void send(Context context, TransactionData data, boolean fromBuffer) {
        ApiService apiService = RetrofitClient.getApiService();

        apiService.sendTransaction(data).enqueue(new Callback<Void>() {

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
            ApiService apiService = RetrofitClient.getApiService();

            apiService.sendTransaction(data).enqueue(new Callback<Void>() {
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
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo active = cm.getActiveNetworkInfo();
        return active != null && active.isConnected();
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
