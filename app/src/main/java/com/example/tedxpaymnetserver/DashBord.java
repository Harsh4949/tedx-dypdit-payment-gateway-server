package com.example.tedxpaymnetserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DashBord extends AppCompatActivity {

    BroadcastReceiver updateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dash_bord);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.dashBord_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Define the receiver safely
        updateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String refNo = intent.getStringExtra("ref_no");
                if (refNo != null) {
                    addTransactionCard(refNo, 599, "10:00 PM");
                }
            }
        };

    }

    private void addTransactionCard(String refNo, int amount, String time) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // ✅ Register the local receiver
        IntentFilter filter = new IntentFilter("com.example.UPDATE_UI");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(updateReceiver, filter, Context.RECEIVER_EXPORTED);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (updateReceiver != null) {
            unregisterReceiver(updateReceiver);
        }
    }
}