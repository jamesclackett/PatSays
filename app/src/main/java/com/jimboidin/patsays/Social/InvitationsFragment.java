package com.jimboidin.patsays.Social;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.text.InputType;
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
import com.jimboidin.patsays.Game.LobbyActivity;
import com.jimboidin.patsays.R;

/*
    A fragment that shows the user a list of game invites they have received.
    The user can click on an invite and accept it to be taken to that game. In this case,
    the fragment will interface with SocialActivity, telling it to finish() and an intent will
    be created for LobbyActivity.
    The logic of this fragment is similar to FriendsFragment in that it uses ListViews & Adapters,
    however there are no context menus. A user simply clicks on an invite to select.
*/
//TODO - Firebase Functions for invites etc, so can restrict access to DB later
public class InvitationsFragment extends Fragment {
    private final String TAG = "InvitationsFragment";
    private Context mContext;
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

    // See FriendsFragment.onAttach() for explanation.
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        mLeaveListener = (LeaveSocialListener) context; // tells social activity to finish
        mLobbyListener = (LobbyListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null){
            mAuth = FirebaseAuth.getInstance();
            Boolean mInLobby = mLobbyListener.askIsLobby();
            FloatingActionButton fab = getView().findViewById(R.id.invite_by_name_button);

            if (!mInLobby) fab.setVisibility(View.GONE); // only allow invites from inside lobby

            fab.setOnClickListener(v -> openInviteDialog());
            getMyProfile();
            initializeListView();
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

    // Standard initiation of ListView and Adapter.
    // onClick of item makes a call to checkLobbyExists() with the Invite item's data
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
        getInvitations();
    }

    // Gets (and listens for) any invitations added to the database.
    // Pulls the data and creates an Invite Object out of it. This Invite obj is then given
    // to the array adapter.
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
                        String invitedByStr = childData.child("invited_by").getValue(String.class);
                        Invite invite = new Invite(invitedByStr, hostNameStr);
                        mArrayAdapter.add(invite);
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Checks to make sure the game exists in the database. If it does, then call startLobbyActivity(name)
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

    // Starts the LobbyActivity, passing in the hostname.
    // Tells SocialActivity to finish using mLeaveListener.onLeave()
    private void startLobbyActivity(String hostName) {
        removeListeners();
        Intent intent = new Intent(mContext, LobbyActivity.class);
        intent.putExtra("host_name", hostName);
        startActivity(intent);
        mLeaveListener.onLeave();
    }


    // This allows the user to create an invite of their own after they click on the invite FAB
    // Makes a call to findAndInvite() with the details entered
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

    // If the user exists, invite them to the game using User.invite()
    private void findAndInvite(String inviteInput){
        if (!inviteInput.equals(myProfile.getUsername())){
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.orderByChild("username").equalTo(inviteInput).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                        User user = new User(inviteInput, childDataSnapshot.getKey());
                        User.invite(myProfile, user);
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

    // terminates the invite listener so it does not continue to run
    private void removeListeners() {
        if (mInviteListener != null)
            mInvitesDB.removeEventListener(mInviteListener);
    }

    private void displayToast(String message) {
        Toast.makeText(mContext, message,
                Toast.LENGTH_SHORT).show();
    }

    // standard aside from call to removeListeners()
    @Override
    public void onDestroy() {
        super.onDestroy();
        removeListeners();
    }
}


// A simple class to bundle invitation details together and print out if required
class Invite{
    private final String invitedByName, hostName;

    public Invite(String invitedByName, String hostName){
        this.invitedByName = invitedByName;
        this.hostName = hostName;
    }

    public String getHostName() { return hostName; }

    @NonNull
    @Override
    public String toString() { return  invitedByName + " invited you to join: " + hostName; }
}