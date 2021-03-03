package com.jimboidin.patsays.Social;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.LobbyActivity;
import com.jimboidin.patsays.R;


public class InvitationsFragment extends Fragment {
    private Context mContext;
    private final String TAG = "InvitationsFragment";
    private String mUsername, mHostName;
    private FirebaseAuth mAuth;
    private DatabaseReference mInvitesDB;
    private ValueEventListener mInviteListener;
    private ListView mInvitesListView;
    private ArrayAdapter<Invite> mArrayAdapter;
    private LeaveSocialListener mLeaveListener;
    private LobbyListener mLobbyListener;
    private Boolean mInLobby;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitations, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mLeaveListener = (LeaveSocialListener) context;
        mLobbyListener = (LobbyListener) context;

    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mInLobby = mLobbyListener.askIsLobby();
        if (mInLobby){
            Log.i(TAG, "interface worked!");
            mHostName = mLobbyListener.getHostName();
        }

        FloatingActionButton fab = getView().findViewById(R.id.invite_by_name_button);
        if (!mInLobby)
            fab.setVisibility(View.GONE);
        fab.setOnClickListener(v -> openInviteDialog());
        initializeInviteListView();

        getMyUsername();
        getInvitations();
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

    private void initializeInviteListView() {
        mInvitesListView = getView().findViewById(R.id.invites_listView);
        mArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mInvitesListView.setAdapter(mArrayAdapter);

        mInvitesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Invite invite = (Invite) parent.getItemAtPosition(position);
                String hostName = invite.getHostName();
                checkLobbyExists(hostName);
                mInvitesDB.child(hostName).removeValue();
            }
        });
    }

    private void getInvitations() {
        mInvitesDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Invitations");
        mInviteListener = mInvitesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("Invites: onDataChange invite triggered!!");
                mArrayAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren())
                        if (childData.child("host").exists() && childData.child("invitee").exists()){

                            String hostNameStr = childData.child("host").getValue(String.class);
                            String inviteeStr = childData.child("invitee").getValue(String.class);

                            addInvitation(inviteeStr, hostNameStr);
                        }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }


    private void addInvitation(String inviteeName, String hostName) {
        Invite invite = new Invite(inviteeName, hostName);
        mArrayAdapter.add(invite);
        Log.i(TAG, "Invite added to ListView");
    }

    private void checkLobbyExists(String hostName) {
        DatabaseReference currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games");
        currentGameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(hostName)) {
                    startLobbyActivity(hostName);
                    Log.i(TAG, "Lobby exists: true");
                }
                else{
                    displayToast("Lobby no longer exists");
                    Log.i(TAG, "Lobby exists: false");
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startLobbyActivity(String hostName) {
        removeListeners();
        Intent intent = new Intent(mContext, LobbyActivity.class);
        intent.putExtra("host_name", hostName);
        Log.i(TAG, "startLobby called");
        startActivity(intent);
        mLeaveListener.onLeave();
    }

    private void displayToast(String message) {
        Toast.makeText(mContext, message,
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }

    private void removeListeners() {
        if (mInviteListener != null)
            mInvitesDB.removeEventListener(mInviteListener);
    }

    private void openInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Invite");

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> invitePlayer(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void invitePlayer(String inviteInput){
        if (!inviteInput.equals(mUsername)){
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.orderByChild("username").equalTo(inviteInput).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    String invitedUserID = null;
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        invitedUserID = childDataSnapshot.getKey();
                        Log.d(TAG, "invite player: user found");
                    }

                    if (invitedUserID != null && mUsername != null){
                        usersDB.child(invitedUserID).child("Invitations")
                                .child(mAuth.getUid()).child("host").setValue(mHostName);
                        usersDB.child(invitedUserID).child("Invitations")
                                .child(mAuth.getUid()).child("invitee").setValue(mUsername);
                        Log.d(TAG, "invite player: DB invite created");
                    } else{
                        displayToast("Invitation not sent - try again");
                        Log.w(TAG, "invite player: unsuccessful. invited=" + invitedUserID
                                + ", username=" + mUsername);
                    }

                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
        else {
            Log.i(TAG, "Invited self to game - discard" );
            displayToast("You cannot invite yourself to a game");
        }

    }
}

class Invite{
    private final String inviteeName, hostName, message;

    public Invite(String inviteeName, String hostName){
        this.inviteeName = inviteeName;
        this.hostName = hostName;
        this.message = inviteeName + " invited you to join: " + hostName;
    }
    public String getInviteeName() { return inviteeName; }
    public String getHostName() { return hostName; }
    public String getMessage() { return message; }
    @NonNull
    @Override
    public String toString() { return message; }
}