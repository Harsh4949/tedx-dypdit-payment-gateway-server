package com.example.tedxpaymnetserver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView displayMsg;
    Boolean isSetupDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            isSetupDone = SendAndReceivePreferences.getboolean(getApplicationContext(), "isServerSetupDone", false);
            String setServerStatus = (isSetupDone ? "\uD83D\uDFE2 Server Started..." : "\uD83D\uDD34 Server Stopped...");
            ((TextView) findViewById(R.id.displayMsg)).setText(setServerStatus);

            //initialize layout before it render

            return insets;
        });

        displayMsg = findViewById(R.id.displayMsg);

        //Start Logic Form Here

    }

    public void onStartServer(View view) {

        if (!isSetupDone) {
            Toast.makeText(this, "🚫 Complete setup first...", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isSetupDone) {
            Toast.makeText(this, "⚙️ Server is already running.", Toast.LENGTH_SHORT).show();
            displayMsg.setText("\uD83D\uDFE2 Server Started");
        }


    }

    public void onStopServer(View view) {

        if (!isSetupDone) {
            Toast.makeText(this, "🚫 Complete setup first...", Toast.LENGTH_SHORT).show();
            return;
        }

        displayMsg.setText("\uD83D\uDD34 Server Stopped...");
    }

    public void onSetupServer(View view) {

        Intent intent = new Intent(this, Setup.class);
        startActivity(intent);

    }

    public void onViewDashboard(View view) {
        if (!isSetupDone) {
            Toast.makeText(this, "🚫 Complete setup first...", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, DashBord.class);
        startActivity(intent);
    }

}