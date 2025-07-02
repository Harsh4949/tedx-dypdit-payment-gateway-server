package com.example.tedxpaymnetserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Receive_SMS extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isSetupDone = SendAndReceivePreferences.getboolean(context, "isServerSetupDone", false);
        if (!isSetupDone) return; // Exit if not setup


        //  Get the stored ticket prices string, sender ID, Server Holdername
        String ticketPrices = SendAndReceivePreferences.retriveData(context, "ticketAmounts", "");
        List<String> expectedAmountList = Arrays.asList(ticketPrices.split(" "));

        String expectedSender = SendAndReceivePreferences.retriveData(context, "bankSenderId", "");
        String serverHolder = SendAndReceivePreferences.retriveData(context, "serverHolder", "");

        Bundle bundle = intent.getExtras();
        String format = bundle.getString("format");
        Object[] smsObj = (Object[]) bundle.get("pdus");

        for (Object obj : smsObj) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj, format);
            String mobno = smsMessage.getDisplayOriginatingAddress();
            String msg = smsMessage.getDisplayMessageBody();

            if (msg != null) {
                // SmsManager smsManager = SmsManager.getDefault();

                Toast.makeText(context, extractRefNo(msg), Toast.LENGTH_LONG).show();

                // smsManager.sendTextMessage(mobno, null, "Hi", null, null);
            }

        }
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

    private class UpiRefValidator {

        /**
         * Extracts the 12-digit UPI reference number securely for a known sender
         *
         * @param msg                the full SMS content
         * @param expectedSender     the exact expected sender ID (e.g., "AX-IPBMSG-S")
         * @param expectedAmountList list of valid amounts like "1.00", "307.00"
         * @return 12-digit UPI reference number, or null if not valid
         */

        @Nullable
        public String extractUpiRefFromTrustedSender(String msg, String expectedSender, String msgSenderId, List<String> expectedAmountList) {

            String lowerMsg = msg.toLowerCase();

            // 1. Match only if sender is trusted
            if (!expectedSender.equals(msgSenderId)) {
                return null; // Block all unknown senders
            }

            // 2. Must contain "upi/credit/"
            if (!lowerMsg.contains("upi/credit/")) {
                return null;
            }

            // 3. Must contain a valid amount
            if (!containsValidAmount(lowerMsg, expectedAmountList)) {
                return null;
            }

            // 4. Extract 12 digit ref no only
            Pattern pattern = Pattern.compile("\\b\\d{12}\\b");
            Matcher matcher = pattern.matcher(lowerMsg);
            if (matcher.find()) {
                return matcher.group(1); // return only if all checks pass
            }

            return null;
        }

        private boolean containsValidAmount(String msg, List<String> amounts) {
            for (String amt : amounts) {
                if (msg.contains(amt.toLowerCase())) {
                    return true;
                }
            }
            return false;
        }
    }



}
