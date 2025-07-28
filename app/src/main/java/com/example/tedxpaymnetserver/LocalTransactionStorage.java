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

    public static void saveTransaction(Context context, TransactionModel transaction) {
        List<TransactionModel> list = getAllTransactions(context);
        list.add(transaction);
        saveTransactionList(context, list);
    }

    public static List<TransactionModel> getAllTransactions(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String json = prefs.getString(KEY_TRANSACTIONS, null);

        if (json == null) return new ArrayList<>();
        Type type = new TypeToken<List<TransactionModel>>() {
        }.getType();

        return new Gson().fromJson(json, type);
    }

    private static void saveTransactionList(Context context, List<TransactionModel> list) {
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
