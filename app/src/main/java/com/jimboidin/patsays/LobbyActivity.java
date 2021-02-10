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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class LobbyActivity extends AppCompatActivity {
    private String hostName;
    private ArrayList<String> playerList;
    private DatabaseReference currentGameDB;
    private Button startButton, inviteButton;
    private TextView playersTextView;
    private boolean host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lobby);

        host = getIntent().getBooleanExtra("host", false);

        playersTextView = findViewById(R.id.player_list);
        startButton = findViewById(R.id.start_button);
        inviteButton = findViewById(R.id.invite_button);
        inviteButton.setOnClickListener(v -> openInviteDialog());


        if (host)
            createServerGame();
        else
            joinServerGame();

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
        usersDB.orderByChild("name").equalTo(name).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String inviteID = "";
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    inviteID = childDataSnapshot.getKey();
                }
                System.out.println("TEST "+ inviteID);
                usersDB.child(inviteID).child("Invitations").child("invitee")
                        .setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail());
                usersDB.child(inviteID).child("Invitations").child("host").setValue(hostName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }


    private void createServerGame(){
        hostName = FirebaseAuth.getInstance().getUid();
        currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(hostName);
        currentGameDB.child("Players").child(hostName).setValue(true);
        currentGameDB.child("Players").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //playerLeft & player joined
                System.out.println("listener Triggered!"+snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    private void joinServerGame() {
    }

    @Override
    public void onBackPressed() {
        if (host)
            currentGameDB.removeValue(); //destroy gameServer //TODO - add code to remove all other players from lobby
        finish();
        super.onBackPressed();
    }
}