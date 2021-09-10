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


/*
    FirebaseMessagingService is used to allow the device to listen for notifications
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FBMessagingService";

    // This method is called when the app is first installed (or if token changes)
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        Log.i(TAG, "NEW TOKEN: " + s);
    }

    // This method handles the notification if we are inside the app. Displays a toast to user.
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String message = remoteMessage.getNotification().getBody();
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show());
    }
}
