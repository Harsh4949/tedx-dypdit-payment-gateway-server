package com.example.tedxpaymnetserver;

import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.List;

public class DashBord extends AppCompatActivity {

    static List<TransactionData>  transactions;
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
            loadTransactions();
            return insets;
        });


    }

    private void addTransactionCard(String refNo, String amount, String time, String status) {
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

        // Status
        TextView textStatus = new TextView(this);
        textStatus.setText(status);
        textStatus.setTextSize(14f);
        if (status.equals("Sent"))
            textStatus.setTextColor(Color.GREEN);
        else
            textStatus.setTextColor(Color.RED);

        // Add views to layout
        innerLayout.addView(refText);
        innerLayout.addView(amountText);
        innerLayout.addView(timeText);
        innerLayout.addView(textStatus);
        cardView.addView(innerLayout);

        // Add card to container
        LinearLayout container = findViewById(R.id.transactionListContainer);
        container.addView(cardView);
    }

    private void loadTransactions() {
        // Reset totals before recounting
        totalAmount = 0.0;
        totalTransactions = 0;

        // Get transaction list
        transactions = LocalTransactionStorage.getAllTransactions(this);

        // ✅ Clear previous transaction views to prevent duplication
        LinearLayout container = findViewById(R.id.transactionListContainer);
        container.removeAllViews();

        // Loop through each transaction and populate UI
        for (TransactionData transaction : transactions) {
            addTransactionCard(transaction.getRefNo(), transaction.getAmount(), transaction.getTimestamp() ,transaction.getStatus());

            try {
                totalAmount += Double.parseDouble(transaction.getAmount());
                totalTransactions++;
            } catch (NumberFormatException e) {
                Log.e("DashBord", "Error parsing amount: " + transaction.getAmount(), e);
            }
        }

        // Set totals in summary TextViews
        totalTransactionsTextView.setText(String.valueOf(totalTransactions));
        totalAmountTextView.setText("₹ " + String.format("%.2f", totalAmount));
    }



    public void onclearDataBtnClicked(View view) {

        LocalTransactionStorage.clearAllTransactions(getApplicationContext());
        LinearLayout container = findViewById(R.id.transactionListContainer);
        container.removeAllViews();
        totalTransactions = 0;
        totalAmount = 0.0;
        totalTransactionsTextView.setText("0");
        totalAmountTextView.setText("₹ 0.00");

        Toast.makeText(DashBord.this, "All transaction data cleared ✅", Toast.LENGTH_SHORT).show();
    }
}