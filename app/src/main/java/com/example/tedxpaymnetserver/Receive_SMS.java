package com.example.tedxpaymnetserver;

import android.Manifest;
import android.app.NotificationManager;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Receive_SMS extends BroadcastReceiver{

    SmsManager smsManager;
    public int level;
    String format, msg,feature[];
    private DevicePolicyManager mDevicePolicyManager;


    @Override
    public void onReceive(Context context, Intent intent) {

        mDevicePolicyManager = (DevicePolicyManager) context.getSystemService(
                Context.DEVICE_POLICY_SERVICE);

        Bundle bundle = intent.getExtras();
        format = bundle.getString("format");
        Object[] smsObj = (Object[]) bundle.get("pdus");

        for (Object obj : smsObj) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj, format);
            msg = smsMessage.getDisplayMessageBody();
            smsManager=SmsManager.getDefault();

            if(msg.contains("Hello"))
            {
                Toast.makeText(context,"hi", Toast.LENGTH_SHORT).show();
                return;
            }

            if(msg.contains("Hello Assistify") && msg.contains(fbuser)) {
                try {
                    SmsManager.getDefault().sendTextMessage(mobno, null, "Welcome to Assistify !!", null, null);
                    SmsManager.getDefault().sendTextMessage(mobno, null, "1)getbattery <password>\n2)lockphone <password>\n3)ringphone <password>\n4)offsilent <password>", null, null);
                    SmsManager.getDefault().sendTextMessage(mobno, null, "5)dosilent <password>\n6)getlocation <password>\n7)getcontact <password> <contactname>\n8)getcontactlist <password> <startingofname>", null, null);
                } catch (Exception e) {
                    Log.i("Sms69", "Sms Not send" + e.getMessage());
                }
                featureprocessed = true;
            }
            else if(!msg.contains(" ") && featureprocessed)
            {
                smsManager.sendTextMessage(mobno,null,"No password entered !!",null,null);
                smsManager.sendTextMessage(mobno,null,"Session Terminated...",null,null);
                featureprocessed=false;
            }
            else if (featureprocessed)
            {
                feature=msg.split(" ");
                if (feature[1].compareTo(check) == 0)
                {
                    String s=feature[0].toLowerCase();


                }
                else
                {
                    smsManager.sendTextMessage(mobno, null, "Wrong password !!", null, null);
                    smsManager.sendTextMessage(mobno, null, "Session Terminated...", null, null);
                    featureprocessed=false;

                }
            }
        }




            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
    }


    private void sendLocationViaSMS(double latitude, double longitude, Context context) {

    }
}
