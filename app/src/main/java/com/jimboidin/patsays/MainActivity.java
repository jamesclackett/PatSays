package com.jimboidin.patsays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;

public class MainActivity extends AppCompatActivity {
    private Button mLogout;
    private Button mCreateGame;
    private TextView mUserName;
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();

        mUserName = findViewById(R.id.username_text_view);
        mUserName.setText("Signed in as \n" + mAuth.getCurrentUser().getEmail());

        mLogout = findViewById(R.id.logout_button);
        mLogout.setOnClickListener(v -> logOut());

        mCreateGame = findViewById(R.id.create_button);
        mCreateGame.setOnClickListener(v -> startLobbyActivity());
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host", true);
        startActivity(intent);
    }

    private void logOut() {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}