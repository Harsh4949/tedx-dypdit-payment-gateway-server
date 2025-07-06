package com.example.tedxpaymnetserver;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    TextView displayMsg;
    Boolean isSetupDone, onStopBtnClicked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Button resendBufferBtn = findViewById(R.id.btn_resend_buffer);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            onStopBtnClicked = SendAndReceivePreferences.getboolean(getApplicationContext(), "onStopBtnClicked", false);
            isSetupDone = SendAndReceivePreferences.getboolean(getApplicationContext(), "isServerSetupDone", false);

            String setServerStatus = ((!(onStopBtnClicked) && isSetupDone) ? "\uD83D\uDFE2 Server Started..." : "\uD83D\uDD34 Server Stopped...");
            ((TextView) findViewById(R.id.displayMsg)).setText(setServerStatus);

            //initialize layout before it render

            return insets;
        });

        displayMsg = findViewById(R.id.displayMsg);

        resendBufferBtn.setOnClickListener(v -> {
            NetworkBufferedSender.resendBuffered(getApplicationContext());
            Toast.makeText(this, "Trying to resend buffered data...", Toast.LENGTH_SHORT).show();
        });


        //Start Logic Form Here

    }

    public void onStartServer(View view) {

        if (!isSetupDone) {
            Toast.makeText(this, "🚫 Complete setup first...", Toast.LENGTH_SHORT).show();

        } else if (onStopBtnClicked) {
            SendAndReceivePreferences.setboolean(getApplicationContext(), "onStopBtnClicked", false);
            displayMsg.setText("\uD83D\uDFE2 Server Started...");

        } else if (isSetupDone && onStopBtnClicked) {
            Toast.makeText(this, "⚙️ Server is already running.", Toast.LENGTH_SHORT).show();
        }

    }

    public void onStopServer(View view) {

        if (!isSetupDone) {
            Toast.makeText(this, "🚫 Complete setup first...", Toast.LENGTH_SHORT).show();
        }

        SendAndReceivePreferences.setboolean(getApplicationContext(), "onStopBtnClicked", true);
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