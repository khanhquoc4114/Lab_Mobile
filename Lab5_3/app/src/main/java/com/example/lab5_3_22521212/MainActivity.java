package com.example.lab5_3_22521212;

import static com.example.lab5_3_22521212.SmsReceiver.SMS_MESSAGE_ADDRESSES_KEY;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class MainActivity extends AppCompatActivity {
    private ReentrantLock reentrantLock;
    private SwitchMaterial swAutoResponse;
    private LinearLayout llButtons;
    private Button btnSafe, btnMayday;
    private ArrayList<String> requesters;
    private ArrayAdapter<String> adapter;
    private ListView lvMessages;
    public static boolean isRunning;
    private SharedPreferences.Editor editor;
    private final String AUTO_RESPONSE = "auto_response";
    BroadcastReceiver broadcastReceiver;
    private static final int SMS_PERMISSION_CODE = 100;

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

        // Kiểm tra quyền
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECEIVE_SMS}, SMS_PERMISSION_CODE);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        }

        findViewsByIds();
        initVariables();
        handleOnClickListener();

        Intent intent = getIntent();
        if (intent != null && intent.getStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY) != null) {
            ArrayList<String> addresses = intent.getStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY);
            processReceiveAddresses(addresses);
        }
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

    // Gán id cho controls
    private void findViewsByIds() {
        swAutoResponse = findViewById(R.id.sw_auto_response);
        llButtons = findViewById(R.id.ll_buttons);
        lvMessages = findViewById(R.id.lv_messages);
        btnSafe = findViewById(R.id.btn_safe);
        btnMayday = findViewById(R.id.btn_mayday);
    }

    private void respond(String to, String response) {
        reentrantLock.lock();
        try {
            requesters.remove(to);
            adapter.notifyDataSetChanged();
        } finally {
            reentrantLock.unlock();
        }
        SmsManager sms = SmsManager.getDefault();
        sms.sendTextMessage(to, null, response, null, null);
    }

    public void respond(boolean ok) {
        String okString = getString(R.string.i_am_safe_and_well_worry_not);
        String notOkString = getString(R.string.tell_my_mother_i_love_her);
        String outputString = ok ? okString : notOkString;
        ArrayList<String> requestersCopy = new ArrayList<>(requesters);
        for (String to : requestersCopy)
            respond(to, outputString);
    }

    @SuppressLint("InlinedApi")
    private void initBroadcastReceiver() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                ArrayList<String> addresses = intent.getStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY);
                assert addresses != null;
                processReceiveAddresses(addresses);
            }
        };
        IntentFilter intentFilter = new IntentFilter(SmsReceiver.SMS_FORWARD_BROADCAST_RECEIVER);
        registerReceiver(broadcastReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED);
    }

    public void processReceiveAddresses(ArrayList<String> addresses) {
        if (addresses == null || addresses.isEmpty()) return;

        for (String address : addresses) {
            reentrantLock.lock();
            try {
                if (!requesters.contains(address)) {
                    requesters.add(address);
                    adapter.notifyDataSetChanged();
                }
            } finally {
                reentrantLock.unlock();
            }
        }

        if (swAutoResponse.isChecked()) respond(true);
    }

    private void handleOnClickListener() {
        btnSafe.setOnClickListener(view -> respond(true));
        btnMayday.setOnClickListener(view -> respond(false));

        swAutoResponse.setOnCheckedChangeListener((buttonView, isChecked) -> {
            llButtons.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            editor.putBoolean(AUTO_RESPONSE, isChecked);
            editor.apply();
        });
    }

    private void initVariables() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        editor = sharedPreferences.edit();
        reentrantLock = new ReentrantLock();
        requesters = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, requesters);
        lvMessages.setAdapter(adapter);

        Intent intent = getIntent();
        if (intent != null && intent.getStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY) != null) {
            ArrayList<String> addresses = intent.getStringArrayListExtra(SMS_MESSAGE_ADDRESSES_KEY);
            processReceiveAddresses(addresses);
        }

        boolean autoResponse = sharedPreferences.getBoolean(AUTO_RESPONSE, false);
        swAutoResponse.setChecked(autoResponse);
        llButtons.setVisibility(autoResponse ? View.GONE : View.VISIBLE);
        initBroadcastReceiver();
        // send addresses to broadcastReceiver, add code below
    }

    @Override
    protected void onResume() {
        super.onResume();
        isRunning = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        isRunning = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }
}
