package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class InvitationsActivity extends AppCompatActivity {
    private final String TAG = "InvitationsActivity";
    private String mInviteeStr, mHostNameStr;
    private FirebaseAuth mAuth;
    private DatabaseReference mInvitesDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        mAuth = FirebaseAuth.getInstance();
        getInvitations();
    }

    private void getInvitations() {
        if (mAuth.getUid() != null){
            mInvitesDB = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getUid()).child("Invitations");
            mInvitesDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        Log.d(TAG, "invite received");
                        mHostNameStr = snapshot.child("host").getValue(String.class);
                        mInviteeStr = snapshot.child("invitee").getValue(String.class);
                        displayInvitation();
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

    }

    private void displayInvitation() {
        TextView inviteeTextView = findViewById(R.id.invitee_text_view);
        TextView hostTextView = findViewById(R.id.host_text_view);

        String message = mInviteeStr + "invited you to join: ";
        inviteeTextView.setText(message);
        hostTextView.setText(mHostNameStr);
        hostTextView.setOnClickListener(v -> checkLobbyExists());
    }

    private void startLobbyActivity() {
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host_name", mHostNameStr);
        startActivity(intent);
        finish();
    }

    private void checkLobbyExists() {
        DatabaseReference currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games");
        currentGameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(mHostNameStr)) {
                    Log.d(TAG, "Lobby exists: true");
                    startLobbyActivity();
                }
                else{
                    displayToast("Lobby no longer exists");
                    Log.w(TAG, "Lobby exists: false");
                }
                mInvitesDB.removeValue();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayToast(String message) {
        Toast.makeText(InvitationsActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }
}