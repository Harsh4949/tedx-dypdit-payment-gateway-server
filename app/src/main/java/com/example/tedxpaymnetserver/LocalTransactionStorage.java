package com.example.tedxpaymnetserver;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;


public class LocalTransactionStorage {

    public static List<TransactionModel> transactions = new ArrayList<>();

    public static void saveTransaction(Context context, TransactionModel transaction) {
        transactions.add(transaction);
    }

    public static List<TransactionModel> getAllTransactions(Context context) {

        // Dummy Data (Replace with actual SharedPreferences or DB logic)
        transactions.add(new TransactionModel("123456789012", "300.00", "10:00 AM"));

        return transactions;
    }
}
