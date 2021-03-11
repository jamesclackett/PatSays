package com.jimboidin.patsays.Utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.jimboidin.patsays.Auth.LoginActivity;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FBMessagingService";
    private FirebaseAuth mAuth;



    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "NEW TOKEN: " + s);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String message = remoteMessage.getNotification().getBody();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
