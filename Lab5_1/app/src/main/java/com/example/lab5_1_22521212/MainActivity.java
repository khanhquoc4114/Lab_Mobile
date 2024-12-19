package com.example.lab5_1_22521212;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.Manifest;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private IntentFilter filter;
    private static final int SMS_PERMISSION_CODE = 100;
    PowerStateChangeReceiver powerStateChangeReceiver;
    IntentFilter powerFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }

        initBroadcastReceiver();
        initPowerStateReceiver();
    }

    // Hỏi quyền của điện thoại
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS permission is required to receive messages", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void processReceive(Context context, Intent intent) {
        Toast.makeText(context, getString(R.string.you_have_a_new_message), Toast.LENGTH_LONG).show();
        TextView tvContent = findViewById(R.id.tv_content);

        final String SMS_EXTRA = "pdus";
        Bundle bundle = intent.getExtras();
        SmsMessage smsMsg;

        if (bundle != null) {
            Object[] messages = (Object[]) bundle.get(SMS_EXTRA);
            if (messages != null) {
                StringBuilder smsBuilder = new StringBuilder();
                for (Object message : messages) {
                    if (message instanceof byte[]) {
                        smsMsg = SmsMessage.createFromPdu((byte[]) message);
                        String msgBody = smsMsg.getMessageBody();
                        String address = smsMsg.getDisplayOriginatingAddress();
                        smsBuilder.append(address).append(":\n").append(msgBody).append("\n");
                    }
                }
                tvContent.setText(smsBuilder.toString());
            } else {
                Toast.makeText(context, "No messages found", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(context, "No extras found in the intent", Toast.LENGTH_SHORT).show();
        }
    }

    private void initBroadcastReceiver() {
        filter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        broadcastReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                processReceive(context, intent);
            }
        };
    }

    private void initPowerStateReceiver() {
        powerFilter = new IntentFilter();
        powerFilter.addAction(Intent.ACTION_POWER_CONNECTED);
        powerFilter.addAction(Intent.ACTION_POWER_DISCONNECTED);
        powerStateChangeReceiver = new PowerStateChangeReceiver();
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, filter);
        registerReceiver(powerStateChangeReceiver, powerFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        unregisterReceiver(powerStateChangeReceiver);
    }
}