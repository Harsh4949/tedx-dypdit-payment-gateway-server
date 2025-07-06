package com.example.tedxpaymnetserver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NetworkChangeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (NetworkBufferedSender.isNetworkAvailable(context)) {
            Log.d("NetworkChangeReceiver", "Internet is back, resending...");
            NetworkBufferedSender.resendBuffered(context);
        }
    }

}
