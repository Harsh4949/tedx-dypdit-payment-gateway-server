package com.example.tedxpaymnetserver;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Setup extends AppCompatActivity {

    public static final int ADMIN_INTENT = 15, REquestlocation = 1;
    static Switch adminPermission, smsPermission;
    Button confirmSetupBtn;
    EditText ticketAmount, serverHolder, adminPassword, BankNameMsg;
    DevicePolicyManager mDevicePolicyManager;
    ComponentName mComponentName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_setup);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.setup_Server), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            if (SendAndReceivePreferences.getboolean(getApplicationContext(), "isServerSetupDone", false)) {
                serverHolder.setText(SendAndReceivePreferences.retriveData(getApplicationContext(), "serverHolder", ""));
                serverHolder.setEnabled(false);
                ticketAmount.setText(SendAndReceivePreferences.retriveData(getApplicationContext(), "ticketAmounts", ""));
                ticketAmount.setEnabled(false);
                BankNameMsg.setText(SendAndReceivePreferences.retriveData(getApplicationContext(), "bankSenderId", ""));
                BankNameMsg.setEnabled(false);
                confirmSetupBtn.setVisibility(View.GONE);
                findViewById(R.id.AdminpasswordLayout).setVisibility(View.GONE);
            }

            return insets;

        });


        mComponentName = new ComponentName(this, myDeviceAdminReceiver.class);
        mDevicePolicyManager = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);

        adminPermission = findViewById(R.id.swich_admin_permission);
        smsPermission = findViewById(R.id.swich_sms_permission);
        confirmSetupBtn = findViewById(R.id.confirmSetupBtn);
        ticketAmount = findViewById(R.id.ticketAmount);
        serverHolder = findViewById(R.id.serverHolder);
        adminPassword = findViewById(R.id.adminPassword);
        BankNameMsg = findViewById(R.id.BankNameMsg);

        smsPermission.setChecked(SendAndReceivePreferences.getboolean(getApplicationContext(), "smsPermission", false));
        adminPermission.setChecked(SendAndReceivePreferences.getboolean(getApplicationContext(), "adminPermission", false));

        confirmSetupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (smsPermission.isChecked() && adminPermission.isChecked() && !ticketAmount.getText().toString().isEmpty() && !serverHolder.getText().toString().isEmpty() && (adminPassword.getText().toString()).equals("Harsh@4949")) {


                    SendAndReceivePreferences.storeData(getApplicationContext(), "bankSenderId", BankNameMsg.getText().toString());
                    SendAndReceivePreferences.storeData(getApplicationContext(), "ticketAmounts", ticketAmount.getText().toString());
                    SendAndReceivePreferences.storeData(getApplicationContext(), "serverHolder", serverHolder.getText().toString());
                    SendAndReceivePreferences.setboolean(getApplicationContext(), "isServerSetupDone", true);

                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    Toast.makeText(getApplicationContext(), "Please give all permissions !! And Fill Filds", Toast.LENGTH_SHORT).show();
                }
            }
        });

        smsPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (smsPermission.isChecked()) {
                    SendAndReceivePreferences.setboolean(getApplicationContext(), "smsPermission", true);

                    if (ContextCompat.checkSelfPermission(Setup.this, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(Setup.this,
                                new String[]{Manifest.permission.SEND_SMS}, 1);
                    } else {
                        Toast.makeText(Setup.this, "SMS Permission already granted", Toast.LENGTH_SHORT).show();
                    }

                } else {
                    SendAndReceivePreferences.setboolean(getApplicationContext(), "smsPermission", false);
                    Toast.makeText(Setup.this, "We need SMS permission !!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        adminPermission.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                try {
                    if (adminPermission.isChecked()) {
                        SendAndReceivePreferences.setboolean(getApplicationContext(), "adminPermission", true);

                        Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mComponentName);
                        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Some description abt admin");

                        startActivityForResult(intent, ADMIN_INTENT);
                    } else {
                        SendAndReceivePreferences.setboolean(getApplicationContext(), "adminPermission", false);

                        try {
                            mDevicePolicyManager.removeActiveAdmin(mComponentName);
                        } catch (Exception e) {
                            if (e.getMessage() != null && e.getMessage().contains("Admin ComponentInfo")) {
                                Toast.makeText(Setup.this, "YOU DIDN'T GRANT ADMIN PERMISSION BEFORE!!", Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                } catch (Exception e) {
                    if (adminPermission.isChecked() && mDevicePolicyManager.getActiveAdmins() == null) {
                        Toast.makeText(Setup.this, "You have not given admin permission", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Toast.makeText(Setup.this, e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        });
    }


}