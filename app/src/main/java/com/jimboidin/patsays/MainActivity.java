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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.jimboidin.patsays.Auth.LoginActivity;
import com.jimboidin.patsays.Game.LobbyActivity;
import com.jimboidin.patsays.Social.SocialActivity;

/* Entry
    Entry point for application.
    Allows user to logout
    Allows navigation to:
        > LobbyActivity
        > SocialActivity
 */

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private FirebaseAuth mAuth;

    // Initialize the views and make call to updateToken()
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        updateNotificationToken();

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

    // Tokens may change, so this updates the users' notification token in the database.
    // Only called by onCreate of this activity
    private void updateNotificationToken() {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
            @Override
            public void onComplete(@NonNull Task<String> task) {
                FirebaseDatabase.getInstance().getReference().child("Users")
                        .child(mAuth.getUid()).child("notificationToken").setValue(task.getResult());
            }
        });
    }

    private void startSocialActivity(){
        Intent intent = new Intent(getApplicationContext(), SocialActivity.class);
        startActivity(intent);
    }

    // When clicking on 'create game' (as opposed to accepting invite), LoginActivity is started in 'Host-Mode'
    private void startLobbyActivity() {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host", true);
        intent.putExtra("host_name", mAuth.getUid());
        startActivity(intent);
    }

    // Logs user out with FirebaseAuth built-in method and takes user to Login page
    // lets backend know that the user is now logged out
    private void logOut() {
        FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).child("signedIn").setValue(false);
        FirebaseMessaging.getInstance().deleteToken(); // so users don't keep getting notifications
        mAuth.signOut();
        Log.d(TAG, "user signed out");
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}