package com.example.lab5_1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        initBroadcastReceiver();
    }

    public void processReceive(Context context, Intent intent)
    {
        Toast.makeText(context,getString(R.string.you_have_a_new_message),
                Toast.LENGTH_LONG).show();
        TextView tvContent = (TextView) findViewById(R.id.tv_content);

        // pdus -> key to get messages
        final String SMS_EXTRA = "pdus";
        Bundle bundle = intent.getExtras();

        Object[] messages = (Object[]) bundle.get(SMS_EXTRA);
        StringBuilder sms = new StringBuilder();
        String format = bundle.getString("format"); // Retrieve SMS format

        SmsMessage smsMsg;
        for (int i = 0; i < messages.length; i++)
        {
            if (Build.VERSION.SDK_INT >= 23)
                smsMsg = SmsMessage.createFromPdu((byte[]) messages[i], format);
            else
                smsMsg = SmsMessage.createFromPdu((byte[]) messages[i]);

            // message body
            String msbBody = smsMsg.getMessageBody();

            // source add
            String address = smsMsg.getDisplayOriginatingAddress();
            sms.append(address).append(":\n").append(msbBody).append("\n");
        }

        tvContent.setText(tvContent.getText() + sms.toString());

    }

    private void initBroadcastReceiver()
    {
        // filter incoming messages
        filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        // create broadcast receiver
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                processReceive(context,intent);
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        // make sure broadcastReceiver was created
        if(broadcastReceiver == null)
            initBroadcastReceiver();

        // register Receiver
        registerReceiver(broadcastReceiver,filter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        //unregister receiver
        unregisterReceiver(broadcastReceiver);
    }

}