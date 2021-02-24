package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class InvitationsActivity extends AppCompatActivity {
    private final String TAG = "InvitationsActivity";
    private FirebaseAuth mAuth;
    private DatabaseReference mInvitesDB;
    private ValueEventListener mInviteListener;
    private ListView mInvitesListView;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mInvitesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitations);

        mAuth = FirebaseAuth.getInstance();
        mInvitesListView = findViewById(R.id.invites_listView);
        mArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mInvitesListView.setAdapter(mArrayAdapter);
        mInvitesList = new ArrayList<>();

        getInvitations();
    }

    private void getInvitations() {
        if (mAuth.getUid() != null){
            mInvitesDB = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(mAuth.getUid()).child("Invitations");
            mInviteListener = mInvitesDB.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        for (DataSnapshot childData : snapshot.getChildren()){
                            //below condition is because (host).setValue in Lobby triggers this listener
                            //before invitee is set.
                            if (childData.child("host").exists() && childData.child("invitee").exists()){
                                Log.d(TAG, "invite received");
                                System.out.println(childData);
                                String hostNameStr = childData.child("host").getValue(String.class);
                                String inviteeStr = childData.child("invitee").getValue(String.class);
                                if (!mInvitesList.contains(hostNameStr)){
                                    mInvitesList.add(hostNameStr);
                                    displayInvitation(hostNameStr, inviteeStr);
                                }
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {}
            });
        }

    }

    private void displayInvitation(String hostName, String inviteeName) {
        System.out.println("displayInvitations called");
        String message = inviteeName + " invited you to join: " + hostName;
        mArrayAdapter.add(message);
        mInvitesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkLobbyExists(hostName);
            }
        });
    }

    private void startLobbyActivity(String hostName) {
        mInvitesDB.removeEventListener(mInviteListener);
        Intent intent = new Intent(getApplicationContext(), LobbyActivity.class);
        intent.putExtra("host_name", hostName);
        Log.d(TAG, "startLobby called");
        startActivity(intent);
        finish();
    }

    private void checkLobbyExists(String hostName) {
        DatabaseReference currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games");
        currentGameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(hostName)) {
                    Log.d(TAG, "Lobby exists: true");
                    startLobbyActivity(hostName);
                }
                else{
                    displayToast("Lobby no longer exists");
                    Log.w(TAG, "Lobby exists: false");
                }
                mInvitesDB.child(hostName).removeValue();
                mArrayAdapter.notifyDataSetChanged();

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void displayToast(String message) {
        Toast.makeText(InvitationsActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mInvitesDB.removeEventListener(mInviteListener);
    }

}