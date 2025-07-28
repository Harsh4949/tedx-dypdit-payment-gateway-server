package com.example.tedxpaymnetserver;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.annotation.Nullable;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Receive_SMS extends BroadcastReceiver {

    private static final ExecutorService executor = Executors.newFixedThreadPool(6);
    private static final long DEBOUNCE_INTERVAL_MS = 2000; // 2 seconds to prevent double-processing
    private static final Set<String> recentRefs = new HashSet<>();
    private static final long lastProcessedTime = 0;

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isSetupDone = SendAndReceivePreferences.getboolean(context, "isServerSetupDone", false);
        boolean onStopBtnClicked = SendAndReceivePreferences.getboolean(context, "onStopBtnClicked", false);
        if (!(!(onStopBtnClicked) && isSetupDone)) return; // Exit if not setup

        // 👇 Hold the broadcast to do async work safely
        final PendingResult pendingResult = goAsync();


        executor.execute(() -> {
            try {
                handleIncomingSMS(context, intent);
            } catch (Exception e) {
                Log.e("Receive_SMS", "Executor Error: " + e.getMessage());
            } finally {
                pendingResult.finish();  // ✅ Must call finish to release the broadcast
            }
        });

    }

    private void handleIncomingSMS(Context context, Intent intent) {
        String ticketPrices = SendAndReceivePreferences.retriveData(context, "ticketAmounts", "");
        List<String> expectedAmountList = Arrays.asList(ticketPrices.split(" "));

        String MsgContext = SendAndReceivePreferences.retriveData(context, "MsgContext", "");
        List<String> contextKeywords = Arrays.asList(MsgContext.split(" "));

        String expectedSender = SendAndReceivePreferences.retriveData(context, "bankSenderId", "");
        String serverHolder = SendAndReceivePreferences.retriveData(context, "serverHolder", "");

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        String format = bundle.getString("format");
        Object[] smsObj = (Object[]) bundle.get("pdus");

        if (smsObj == null) return;

        for (Object obj : smsObj) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) obj, format);
            String msgReceivedSenderBank = smsMessage.getDisplayOriginatingAddress();
            String msg = smsMessage.getDisplayMessageBody();

            String refNo = extractRefNo(msg);
            String extractedAmount = UpiRefValidator.extractValidAmount(msg);

            if (refNo != null && extractedAmount != null) {

//                // Duplicate prevention with debounce + recent ref tracking
//                synchronized (recentRefs) {
//                    long now = System.currentTimeMillis();
//                    if ((now - lastProcessedTime) < DEBOUNCE_INTERVAL_MS || recentRefs.contains(refNo)) {
//                        Log.d("Receive_SMS", "Duplicate or debounce triggered for: " + refNo);
//                        return;
//                    }
//                    lastProcessedTime = now;
//                    recentRefs.add(refNo);
//
//                    // Remove refNo from set after 5 minutes
//                    new Handler(Looper.getMainLooper()).postDelayed(() -> recentRefs.remove(refNo), 5 * 60 * 1000);
//                }

                boolean validAmount = UpiRefValidator.containsValidAmount(msg, expectedAmountList);
                boolean hasContext = UpiRefValidator.containsValidContext(msg, contextKeywords);
                boolean hasValidSender = UpiRefValidator.hasValidSeder(msg, expectedSender, msgReceivedSenderBank);

                if (validAmount && hasContext) {  // && hasValidSender Add,-> removed for testing..

                    LocalTransactionStorage.saveTransaction(context, new TransactionModel(refNo, extractedAmount, getCurrentDateTime()));

                    //send Data to server using BUffer
                    NetworkBufferedSender.trySend(context,
                            new TransactionData(refNo, extractedAmount, getCurrentDateTime(), serverHolder, expectedSender));

                    //sendDataToServer(refNo, extractedAmount, getCurrentDateTime(), serverHolder, expectedSender);

                    // UI feedback for debugging (optional, remove in production)
                    Log.d("Receive_SMS", "Received: " + refNo + " " + extractedAmount);
                }
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

    private String getCurrentDateTime() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a");
        return now.format(formatter);
    }


    private static class UpiRefValidator {


        public static Boolean hasValidSeder(String msg, String expectedSenderBank, String msgReceivedSenderBank) {

            return expectedSenderBank.equals(msgReceivedSenderBank); // Block all unknown senders
        }

        private static boolean containsValidAmount(String msg, List<String> amounts) {
            for (String amt : amounts) {
                if (msg.contains(amt)) {
                    return true;
                }
            }
            return false;
        }

        public static Boolean containsValidContext(String msg, List<String> contextKeywords) {
            // Convert message to lowercase for case-insensitive matching
            String lowerMsg = msg.toLowerCase();

            // 1. Check if message contains any of the keywords like "credited", "received", "upi/credit"
            boolean isCredit = false;

            for (String keyword : contextKeywords) {
                if (lowerMsg.contains(keyword.toLowerCase())) {
                    isCredit = true;
                    break;
                }
            }

            return isCredit;
        }

        private static String extractValidAmount(String msg) {
            // Regex Explanation:
            // Optional INR/Rs followed by up to 4 digits (optionally with comma like 1,000) ending with .00
            Pattern pattern = Pattern.compile("(?:INR\\s*|Rs\\.\\s*)?(\\d{1,4}(?:,\\d{3})?)\\.00\\b");
            Matcher matcher = pattern.matcher(msg);
            if (matcher.find()) {
                return matcher.group(1); // ✅ Captures the amount before `.00`
            }
            return null;
        }

    }



}
