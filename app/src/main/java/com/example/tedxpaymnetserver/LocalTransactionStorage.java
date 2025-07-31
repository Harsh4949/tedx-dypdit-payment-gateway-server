package com.example.tedxpaymnetserver;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class LocalTransactionStorage {


    private static final String PREF_NAME = "TransactionPref";
    private static final String KEY_TRANSACTIONS = "transactions";

    public static void saveTransaction(Context context, TransactionData transaction) {
        List<TransactionData> list = getAllTransactions(context);
        list.add(transaction);
        saveTransactionList(context, list);
    }



    public static List<TransactionData> getAllTransactions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRANSACTIONS, null);

        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<TransactionData>>() {
        }.getType();

        return new Gson().fromJson(json, type);
    }

    public static void updateTransaction(Context context, TransactionData transaction) {
        List<TransactionData> list = getAllTransactions(context);

        boolean isUpdated = false;
        for (TransactionData data : list) {
            // Find the transaction by refNo and update it
            if (data.getRefNo().equals(transaction.getRefNo())) {
                data.setStatus("Sent");
                isUpdated = true;
                break;
            }
        }

        if (isUpdated) {
            // Save the updated list back to persistent storage (SharedPreferences, DB, etc.)
            saveTransactionList(context, list); // Assuming saveTransactions handles saving the list
        } else {
            Log.e("updateTransaction", "Transaction with refNo " + transaction.getRefNo() + " not found.");
        }
    }

    private static void saveTransactionList(Context context, List<TransactionData> list) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        String json = new Gson().toJson(list);
        editor.putString(KEY_TRANSACTIONS, json);
        editor.apply();
    }

    public static void clearAllTransactions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_TRANSACTIONS).apply();   //  Only removes transaction data
    }

}
