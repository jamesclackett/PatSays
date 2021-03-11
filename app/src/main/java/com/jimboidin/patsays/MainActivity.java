package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.jimboidin.patsays.Auth.LoginActivity;
import com.jimboidin.patsays.Social.SocialActivity;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        updateToken();

        TextView userTextView = findViewById(R.id.username_text_view);
        String info = "Signed in as \n " + mAuth.getCurrentUser().getEmail();
        userTextView.setText(info);

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logOut());

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> startLobbyActivity());

        Button mSocial = findViewById(R.id.social_button);
        mSocial.setOnClickListener(v -> startSocialActivity());
    }

    private void updateToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                sendTokenToDB(task.getResult());
            }
        });
    }

    private void sendTokenToDB(String result) {
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getUid()).child("notificationToken").setValue(result);
    }

    private void startSocialActivity(){
        Intent intent = new Intent(getApplicationContext(), SocialActivity.class);
        startActivity(intent);
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host", true);
        intent.putExtra("host_name", mAuth.getUid());
        startActivity(intent);
    }

    private void logOut() {
        mAuth.signOut();
        Log.d(TAG, "user signed out");
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}