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
    private final String TAG = "InvitationsFragment";
    private Context mContext;
    private String mHostName;
    private User myProfile;
    private FirebaseAuth mAuth;
    private DatabaseReference mInvitesDB;
    private ValueEventListener mInviteListener;
    private ArrayAdapter<Invite> mArrayAdapter;
    private LeaveSocialListener mLeaveListener;
    private LobbyListener mLobbyListener;

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
        if (getView() != null){
            mAuth = FirebaseAuth.getInstance();
            Boolean mInLobby = mLobbyListener.askIsLobby();
            FloatingActionButton fab = getView().findViewById(R.id.invite_by_name_button);

            if (mInLobby)
                mHostName = mLobbyListener.getHostName();
            else
                fab.setVisibility(View.GONE);

            fab.setOnClickListener(v -> openInviteDialog());
            getMyProfile();
            initializeListView();
            getInvitations();
        }

    }

    private void getMyProfile(){
        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(mAuth.getUid()).child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    myProfile = new User(snapshot.getValue(String.class), mAuth.getUid());
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void initializeListView() {
        ListView mInvitesListView = getView().findViewById(R.id.invites_listView);
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

    private void openInviteDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Invite");

        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> findAndInvite(input.getText().toString()));
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void findAndInvite(String inviteInput){
        if (!inviteInput.equals(myProfile.getUsername())){
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.orderByChild("username").equalTo(inviteInput).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        User user = new User(inviteInput, childDataSnapshot.getKey());
                        User.invite(myProfile, user, mHostName);
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
        else {
            displayToast("You cannot invite yourself to a game");
        }

    }

    private void getInvitations() {
        mInvitesDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Invitations");
        mInviteListener = mInvitesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mArrayAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        String hostNameStr = childData.getKey();
                        String inviteeStr = childData.child("invitee").getValue(String.class);
                        Invite invite = new Invite(inviteeStr, hostNameStr);
                        mArrayAdapter.add(invite);
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void checkLobbyExists(String hostName) {
        DatabaseReference currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games");
        currentGameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(hostName)) {
                    startLobbyActivity(hostName);
                }
                else{
                    displayToast("Lobby no longer exists");
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
        startActivity(intent);
        mLeaveListener.onLeave();
    }


    private void removeListeners() {
        if (mInviteListener != null)
            mInvitesDB.removeEventListener(mInviteListener);
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
}

class Invite{
    private final String inviteeName, hostName;

    public Invite(String inviteeName, String hostName){
        this.inviteeName = inviteeName;
        this.hostName = hostName;
    }

    public String getHostName() { return hostName; }

    @NonNull
    @Override
    public String toString() { return  inviteeName + " invited you to join: " + hostName; }
}