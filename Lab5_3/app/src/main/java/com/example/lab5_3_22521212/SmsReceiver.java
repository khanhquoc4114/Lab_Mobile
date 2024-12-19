package com.example.lab5_3_22521212;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;
import java.util.ArrayList;


public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_FORWARD_BROADCAST_RECEIVER = "sms_forward_broadcast_receiver";
    public static final String SMS_MESSAGE_ADDRESSES_KEY = "sms_messages_key";

    @Override
    public void onReceive(Context context, Intent intent) {
        String queryString = "are you ok?".toLowerCase();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            Object[] pdus = (Object[]) bundle.get("pdus");
            if (pdus != null && pdus.length > 0) {
                SmsMessage[] messages = new SmsMessage[pdus.length];
                ArrayList<String> addresses = new ArrayList<>();

                for (int i = 0; i < pdus.length; i++) {
                    if (pdus[i] instanceof byte[]) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                        String msgBody = messages[i].getMessageBody();
                        String address = messages[i].getDisplayOriginatingAddress();

                        if (msgBody.toLowerCase().contains(queryString)) {
                            addresses.add(address);
                        }
                    }
                }

                if (!addresses.isEmpty()) {
                    if (!MainActivity.isRunning) {
                        // Start MainActivity if it stopped
                        Intent iMain = new Intent(context, MainActivity.class);
                        iMain.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        iMain.putStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY, addresses);
                        context.startActivity(iMain);
                    } else {
                        Intent iForwardBroadcastReceiver = new Intent(SMS_FORWARD_BROADCAST_RECEIVER);
                        iForwardBroadcastReceiver.putStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY, addresses);
                        context.sendBroadcast(iForwardBroadcastReceiver);
                    }
                }
            }
        }
    }
}
