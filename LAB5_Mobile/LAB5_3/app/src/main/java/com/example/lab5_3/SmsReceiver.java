package com.example.lab5_3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;

import java.util.ArrayList;

public class SmsReceiver extends BroadcastReceiver {
    public static final String SMS_FORWARD_BROADCAST_RECEIVER = "sms_forward_broadcast_receiver";
    public static final String SMS_MESSAGE_ADDRESS_KEY = "sms_messages_key";

    @Override
    public void onReceive(Context context, Intent intent) {

        String queryString = "Are you OK?".toLowerCase();
        Bundle bundle = intent.getExtras();
        String format = bundle.getString("format"); // Retrieve SMS format
        if (bundle != null)
        {
            Object[] pdus = (Object[]) bundle.get("pdus");
            SmsMessage[] messages = new SmsMessage[pdus.length];
            for (int i =0; i < pdus.length; i++)
            {
                if (Build.VERSION.SDK_INT >= 23)
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i], format);
                }
                else
                {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                }
            }

            ArrayList<String> addresses = new ArrayList<>();
            for (SmsMessage message : messages)
            {
                if (message.getMessageBody().toLowerCase().contains(queryString))
                    addresses.add(message.getOriginatingAddress());
            }

            if (addresses.size() > 0)
            {
                if (!MainActivity.isRunning)
                {
                    //Start MainActivity if it stopped
                    Intent intent1 = new Intent(context, MainActivity.class);
                    intent1.putStringArrayListExtra(SMS_MESSAGE_ADDRESS_KEY,addresses);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent1);

                }
                else
                {
                    Intent iForwardBroadcastReceiver = new Intent(SMS_FORWARD_BROADCAST_RECEIVER);
                    iForwardBroadcastReceiver.putStringArrayListExtra(SMS_MESSAGE_ADDRESS_KEY,addresses);
                    context.sendBroadcast(iForwardBroadcastReceiver);
                }
            }
        }
    }
}
