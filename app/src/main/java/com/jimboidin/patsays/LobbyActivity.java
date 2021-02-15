package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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

public class LobbyActivity extends AppCompatActivity {
    private final String TAG = "LobbyActivity";
    private String mHostName;
    private ArrayList<String> playerList;
    private DatabaseReference mCurrentGameDB;
    private TextView mPlayersTextView;
    private boolean mIsHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        mIsHost = getIntent().getBooleanExtra("host", false);

        mPlayersTextView = findViewById(R.id.player_list);
        Button startButton = findViewById(R.id.start_button);
        Button inviteButton = findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(v -> openInviteDialog());


        if (mIsHost) {
            mHostName = FirebaseAuth.getInstance().getUid();
            createServerGame();
        }
        else {
            mHostName = getIntent().getStringExtra("host_name");
            startButton.setEnabled(false);
            joinServerGame();
        }


    }

    private void createServerGame(){
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mCurrentGameDB.child("Players").child(mHostName).setValue(true);
        mCurrentGameDB.child("Players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //playerLeft & player joined
                System.out.println("listener Triggered!"+snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void joinServerGame() { }


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
        String myUserId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDB.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String invitedUserID = null;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    invitedUserID = childDataSnapshot.getKey();
                    Log.d(TAG, "invite player: user found");
                }

                if (invitedUserID != null && myUserId != null){
                    usersDB.child(invitedUserID).child("Invitations").child("invitee")
                            .setValue(myUserId);
                    usersDB.child(invitedUserID).child("Invitations").child("host").setValue(mHostName);
                    Log.d(TAG, "invite player: DB invite created");
                } else
                    Log.w(TAG, "invite player: invitation not created");

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }



    @Override
    public void onBackPressed() {
        if (mIsHost)
            mCurrentGameDB.removeValue(); //destroy gameServer //TODO - add code to remove all other players from lobby
        finish();
        super.onBackPressed();
    }

    private void displayToast(String message) {
        Toast.makeText(LobbyActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}