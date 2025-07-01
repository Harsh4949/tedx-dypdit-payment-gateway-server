package com.example.tedxpaymnetserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Receive_SMS extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String format = bundle.getString("format");
        Object[] smsObj = (Object[]) bundle.get("pdus");

        for (Object obj : smsObj) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj, format);
            String mobno = smsMessage.getDisplayOriginatingAddress();
            String msg = smsMessage.getDisplayMessageBody();

            if (msg != null) {
                SmsManager smsManager = SmsManager.getDefault();

                Toast.makeText(context, extractRefNo(msg), Toast.LENGTH_LONG).show();

                // smsManager.sendTextMessage(mobno, null, "Hi", null, null);
            }

        }
    }

    private boolean isBankMessage(String sender, String body) {
        return sender.matches(".*(AX-IPBMSG-G|BK|BANK|HDFC|SBI|AXIS).*") &&
                body.matches("(?i).*\\b(credited|debited|received|IMPS|NEFT|UPI)\\b.*");
    }

    @Nullable
    private String extractRefNo(String msg) {
        // This pattern will match any standalone 12-digit number
        Pattern pattern = Pattern.compile("\\b\\d{12}\\b");
        Matcher matcher = pattern.matcher(msg);
        while (matcher.find()) {
            return matcher.group();  // Return first matched 12-digit number
        }
        return null;
    }


}
