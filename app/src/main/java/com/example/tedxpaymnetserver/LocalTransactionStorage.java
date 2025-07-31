package com.example.tedxpaymnetserver;

import android.content.Context;
import android.content.SharedPreferences;

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

    public static void removeTransaction(Context context, String refNo) {
        List<TransactionData> list = getAllTransactions(context);
        if (list == null || list.isEmpty()) return;

        // Remove transaction by refNo
        List<TransactionData> updatedList = new ArrayList<>();
        for (TransactionData data : list) {
            if (!data.getRefNo().equals(refNo)) {
                updatedList.add(data);
            }
        }

        saveTransactionList(context, updatedList);
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
