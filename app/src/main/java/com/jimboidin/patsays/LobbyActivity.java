package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.Social.SocialActivity;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {
    private final String TAG = "LobbyActivity";
    private String mHostName, mUsername;
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

        mIsHost = getIntent().getBooleanExtra("host", false);
        mHostName = getIntent().getStringExtra("host_name");
        mAuth = FirebaseAuth.getInstance();
        getMyUsername();
        mPlayerList = new ArrayList<>();
        mPlayersTextView = findViewById(R.id.player_list);

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> hostStartGame());
        Button inviteButton = findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(v -> startSocialActivity());

        if (mIsHost)
            createGameServer(); //host creates and sets up the game server
        else {
            startButton.setEnabled(false);
            joinGameServer(); //non-host players join the server setup by host's application
        }
    }


    private void getMyUsername(){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    mUsername = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void createGameServer(){
        System.out.println("hostname: " + mHostName);
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mCurrentGameDB.setValue(true); //overwrite any game servers host previously created
        mCurrentGameDB.child("Players").child(mHostName).setValue(true);
        Log.i(TAG, "Game Server Created");
        setupPlayerListener();
    }

    private void joinGameServer() {
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mCurrentGameDB.child("Players").child(mAuth.getUid()).setValue(true);
        Log.i(TAG, "Got reference to host server");
        setupPlayerListener();
        setupStartListener();
    }

    //starts game for host and triggers start listener for non-hosts
    private void hostStartGame(){
        if (mPlayerList.size() < 2 || mPlayerList.size() > 4)
            displayToast("Must be more than 2 and less than 5 players to start");
        else{
            mCurrentGameDB.child("Game_Info").child("start").setValue(true);
            Log.i(TAG, "Host started game");
            startGameActivity();
        }
    }

    //updated mPlayersTextView if a player joins/leaves game
    //Closes game if the server no longer exists (host left)
    private void setupPlayerListener(){
        DatabaseReference recentPlayersDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Recent_Players");
        mPlayerListener = mCurrentGameDB.child("Players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.i(TAG, "mPlayerListener triggered");
                mPlayerList.clear();
                mPlayersTextView.setText("");

                if (snapshot.exists())
                    for (DataSnapshot child : snapshot.getChildren()) { //get uIds/keys of all players
                        mPlayerList.add(child.getKey());
                        if (!child.getKey().equals(mAuth.getUid()))
                            recentPlayersDB.child(child.getKey()).setValue(true);
                        mPlayersTextView.append(child.getKey() + "\n"); //add to TextView
                    }
                else {
                    displayToast("Host has left game: exiting lobby");
                    Log.i(TAG, "mPlayerListener: Game/key/Players/ snapshot doesn't exist");
                    closeGame();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    //waits for Database-GameInfo/start to become true then starts GameActivity
    private void setupStartListener(){
        mStartListener = mCurrentGameDB.child("Game_Info").child("start").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.getValue().equals(true)){
                        startGameActivity();
                    }
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

    private void startSocialActivity() {
        Intent intent = new Intent(this, SocialActivity.class);
        intent.putExtra("in_lobby", true);
        intent.putExtra("host_name", mHostName);
        startActivity(intent);
    }

    private void removeListeners(){
        if (mPlayerListener != null)
            mCurrentGameDB.child("Players").removeEventListener(mPlayerListener);
        if (mStartListener != null)
            mCurrentGameDB.child("Game_Info").child("start").removeEventListener(mStartListener);
    }

    //closes GameServer if host and leaves server if non-host, finishes activity for all
    private void closeGame(){
        removeListeners();
        if (mIsHost){
            mCurrentGameDB.removeValue();
            Log.i(TAG, "closeGame: GameServer destroyed");
        }
        else{
            mCurrentGameDB.child("Players").child(mAuth.getUid()).removeValue();
            Log.i(TAG, "closeGame: left GameServer");
        }
        finish();
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