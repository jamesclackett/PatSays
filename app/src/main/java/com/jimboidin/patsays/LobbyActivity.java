package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class LobbyActivity extends AppCompatActivity {
    private final String TAG = "LobbyActivity - debug";
    private String mHostName, myUsername;
    private FirebaseAuth mAuth;
    private ArrayList<String> mPlayerList;
    private DatabaseReference mCurrentGameDB;
    private TextView mPlayersTextView;
    private boolean mIsHost;
    private ValueEventListener mPlayerListener, mStartListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);
        Log.d(TAG, "onCreate called ");

        mIsHost = getIntent().getBooleanExtra("host", false);
        mAuth = FirebaseAuth.getInstance();
        mPlayerList = new ArrayList<>();
        mPlayersTextView = findViewById(R.id.player_list);

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> startReady());
        Button inviteButton = findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(v -> openInviteDialog());

        getMyUsername();


        if (mIsHost) {
            mHostName = mAuth.getUid();
            createServerGame();
        }
        else {
            mHostName = getIntent().getStringExtra("host_name");
            startButton.setEnabled(false);
            joinServerGame();
        }
    }

    private void getMyUsername(){
        DatabaseReference usernameDB = FirebaseDatabase.getInstance()
                .getReference().child("Users").child(mAuth.getUid()).child("username");
        usernameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    myUsername = snapshot.getValue(String.class);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void createServerGame(){
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mCurrentGameDB.child("Players").child(mHostName).setValue(true);
        setupPlayerListener();
    }

    private void joinServerGame() {
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mCurrentGameDB.child("Players").child(mAuth.getUid()).setValue(true);
        setupPlayerListener();
        setupStartListener();
    }

    private void startReady(){
        if (mPlayerList.size() < 2 || mPlayerList.size() > 4)
            displayToast("Must be more than 2 and less than 5 players to start");
        else{
            mCurrentGameDB.child("Game_Info").child("start").setValue(true);
            startGameActivity();
        }

    }

    private void setupStartListener(){
        Log.d(TAG, "setupStartListener called");
        mStartListener = mCurrentGameDB.child("Game_Info").child("start").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d(TAG, "startListener: onDataChange called");
                if (snapshot.exists()){
                    if (snapshot.getValue().equals(true)){
                        Log.d(TAG, "startListener: startGameActivity called");
                        startGameActivity();
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void openInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Invite");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> invitePlayer(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void invitePlayer(String name){
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDB.orderByChild("username").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String invitedUserID = null;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    invitedUserID = childDataSnapshot.getKey();
                    Log.d(TAG, "invite player: user found");
                }

                if (invitedUserID != null && myUsername != null){
                    usersDB.child(invitedUserID).child("Invitations")
                            .child(mAuth.getUid()).child("host").setValue(mHostName);
                    usersDB.child(invitedUserID).child("Invitations")
                            .child(mAuth.getUid()).child("invitee").setValue(myUsername);
                    Log.d(TAG, "invite player: DB invite created");
                } else
                    Log.w(TAG, "invite player: invitation not created");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setupPlayerListener(){
        mPlayerListener = mCurrentGameDB.child("Players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPlayerList.clear();
                if (snapshot.exists()){
                    HashMap test = (HashMap) snapshot.getValue();
                    for (Object str : test.keySet()){
                        mPlayerList.add((String) str);
                    }
                    StringBuilder sb = new StringBuilder();
                    for (String str : mPlayerList){
                        sb.append(str);
                        if (str.equals(mHostName))
                            sb.append(" == host");
                        sb.append("\n");
                    }
                    mPlayersTextView.setText(sb);
                }
                else {
                    displayToast("Host has left the game");
                    closeGame();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void startGameActivity() {
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("host_name", mHostName);
        intent.putExtra("is_host", mIsHost);
        intent.putStringArrayListExtra("player_list", mPlayerList);
        startActivity(intent);
        removeListeners();
        finish();
    }

    private void removeListeners(){
        if (mPlayerListener != null)
            mCurrentGameDB.child("Players").removeEventListener(mPlayerListener);
        if (mStartListener != null)
            mCurrentGameDB.child("Game_Info").child("start").removeEventListener(mStartListener);
    }


    private void closeGame(){
        removeListeners();
        if (mIsHost)
            mCurrentGameDB.removeValue(); //destroy gameServer
        else{
            mCurrentGameDB.child("Players").child(mAuth.getUid()).removeValue();
        }
        finish(); // to make sure when host leaves, players do not stay in lobby
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeGame();
    }

    private void displayToast(String message) {
        Toast.makeText(LobbyActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}