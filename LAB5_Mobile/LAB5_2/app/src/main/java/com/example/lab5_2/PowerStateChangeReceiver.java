package com.example.lab5_2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class PowerStateChangeReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (context == null)
            return;
        if (intent.getAction().equals(Intent.ACTION_POWER_CONNECTED))
            Toast.makeText(context, R.string.power_conneted, Toast.LENGTH_LONG).show();
        if (intent.getAction().equals(Intent.ACTION_POWER_DISCONNECTED))
                Toast.makeText(context, R.string.power_disconnected, Toast.LENGTH_LONG).show();
    }
}
