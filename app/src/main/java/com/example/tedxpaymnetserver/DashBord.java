package com.example.tedxpaymnetserver;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class DashBord extends AppCompatActivity {

    List<TransactionModel> transactions;
    int totalTransactions = 0;
    double totalAmount = 0.00;

    TextView totalTransactionsTextView, totalAmountTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_bord);

        totalTransactionsTextView = findViewById(R.id.totalTransactions);
        totalAmountTextView = findViewById(R.id.totalAmount);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashBord_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            transactions = LocalTransactionStorage.getAllTransactions(this);
            for (TransactionModel transaction : transactions) {
                addTransactionCard(transaction.getRefNo(), transaction.getAmount(), transaction.getTimestamp());
                try {
                    totalAmount += Double.parseDouble(transaction.getAmount());
                    totalTransactions++;
                } catch (NumberFormatException e) {
                    Log.e("DashBord", "Error parsing amount: " + transaction.getAmount(), e);
                }
            }

            totalTransactionsTextView.setText("" + totalTransactions);
            totalAmountTextView.setText("₹ " + String.format("%.2f", totalAmount));

            return insets;
        });

    }

    private void addTransactionCard(String refNo, String amount, String time) {
        // Create CardView
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 20, 0, 0);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(16f);
        cardView.setCardElevation(8f);
        cardView.setCardBackgroundColor(Color.parseColor("#F0FFFF"));

        // Inner Layout
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setPadding(32, 24, 32, 24);

        // Reference No.
        TextView refText = new TextView(this);
        refText.setText("#" + refNo);
        refText.setTextSize(18f);
        refText.setTypeface(null, Typeface.BOLD);
        refText.setTextColor(Color.BLACK);

        // Amount
        TextView amountText = new TextView(this);
        amountText.setText("₹" + amount);
        amountText.setTextSize(16f);
        amountText.setTextColor(Color.parseColor("#2ECC71"));

        // Time
        TextView timeText = new TextView(this);
        timeText.setText(time);
        timeText.setTextSize(14f);
        timeText.setTextColor(Color.GRAY);

        // Add views to layout
        innerLayout.addView(refText);
        innerLayout.addView(amountText);
        innerLayout.addView(timeText);
        cardView.addView(innerLayout);

        // Add card to container
        LinearLayout container = findViewById(R.id.transactionListContainer);
        container.addView(cardView);
    }

}