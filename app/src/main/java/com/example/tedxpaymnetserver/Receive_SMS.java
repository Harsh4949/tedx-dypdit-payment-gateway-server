package com.example.tedxpaymnetserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Receive_SMS extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isSetupDone = SendAndReceivePreferences.getboolean(context, "isServerSetupDone", false);
        boolean onStopBtnClicked = SendAndReceivePreferences.getboolean(context, "onStopBtnClicked", false);
        if (!(onStopBtnClicked && isSetupDone)) return; // Exit if not setup


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
            String msgReceivedSenderBank = smsMessage.getDisplayOriginatingAddress();
            String msg = smsMessage.getDisplayMessageBody();

            String refNo = extractRefNo(msg);
            String extractedAmount = extractValidAmount(msg);

            try {
                if ((!Objects.equals(refNo, ""))) { // && expectedSender.equals(msgReceivedSenderBank)

                    Toast.makeText(context, refNo + msgReceivedSenderBank, Toast.LENGTH_LONG).show();

                    LocalTransactionStorage.saveTransaction(context, new TransactionModel(refNo, extractedAmount, getCurrentDateTime()));


                }

            } catch (Exception e) {
                Log.e("Receive_SMS", "Error processing SMS: " + e.getMessage());
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

    private String extractValidAmount(String msg) {
        // Regex Explanation:
        // Optional INR/Rs followed by up to 4 digits (optionally with comma like 1,000) ending with .00
        Pattern pattern = Pattern.compile("(?:INR\\s*|Rs\\.\\s*)?(\\d{1,4}(?:,\\d{3})?)\\.00\\b");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            return matcher.group(1); // ✅ Captures the amount before `.00`
        }
        return null;
    }

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }


    private class UpiRefValidator {

        /**
         * Extracts the 12-digit UPI reference number securely for a known sender
         *
         * @param msg                the full SMS content
         * @param expectedSenderBank     the exact expected sender ID (e.g., "AX-IPBMSG-S")
         * @param expectedAmountList list of valid amounts like "1.00", "307.00"
         * @return 12-digit UPI reference number, or null if not valid
         */

        @Nullable
        public String extractUpiRefFromTrustedSender(String msg, String expectedSenderBank, String msgReceivedSenderBank, List<String> expectedAmountList) {

            String lowerMsg = msg.toLowerCase();

            // 1. Match only if sender is trusted
            if (!expectedSenderBank.equals(msgReceivedSenderBank)) {
                return null; // Block all unknown senders
            }

            // 2. Must contain a valid amount
            if (!containsValidAmount(lowerMsg, expectedAmountList)) {
                return null;
            }

            // 3. Extract 12 digit ref no only
            Pattern pattern = Pattern.compile("\\b([0-9]{12})\\b");
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
