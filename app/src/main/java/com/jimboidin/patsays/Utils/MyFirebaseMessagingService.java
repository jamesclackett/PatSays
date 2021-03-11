package com.jimboidin.patsays.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private final String TAG = "FBMessagingService";
    private FirebaseAuth mAuth;



    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        mAuth = FirebaseAuth.getInstance();
        Log.i(TAG, "NEW TOKEN: " + s);
    }
}
