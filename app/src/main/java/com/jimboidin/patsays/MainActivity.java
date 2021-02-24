package com.jimboidin.patsays;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.jimboidin.patsays.Auth.LoginActivity;
import com.jimboidin.patsays.Social.SocialActivity;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity - debug";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        TextView userTextView = findViewById(R.id.username_text_view);
        String info = "Signed in as \n " + mAuth.getCurrentUser().getEmail();
        userTextView.setText(info);

        Button logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> logOut());

        Button createButton = findViewById(R.id.create_button);
        createButton.setOnClickListener(v -> startLobbyActivity());

        Button mInvitations = findViewById(R.id.invitations_button);
        mInvitations.setOnClickListener(v -> startSocialActivity());
    }

    private void startSocialActivity(){
        Intent intent = new Intent(getApplicationContext(), SocialActivity.class);
        startActivity(intent);
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host", true);
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