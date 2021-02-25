package com.jimboidin.patsays.Social;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.LobbyActivity;
import com.jimboidin.patsays.R;

import java.util.ArrayList;


public class InvitationsFragment extends Fragment {
    private Context mContext;
    private final String TAG = "InvitationsFragment";
    private FirebaseAuth mAuth;
    private DatabaseReference mInvitesDB;
    private ValueEventListener mInviteListener;
    private ListView mInvitesListView;
    private ArrayAdapter<String> mArrayAdapter;
    private ArrayList<String> mInvitesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_invitations, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mInvitesListView = getView().findViewById(R.id.invites_listView);
        mArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
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
                                Log.i(TAG, "invite received");
                                String hostNameStr = childData.child("host").getValue(String.class);
                                String inviteeStr = childData.child("invitee").getValue(String.class);
                                if (!mInvitesList.contains(hostNameStr)){ //TODO - look into this
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
        String message = inviteeName + " invited you to join: " + hostName;
        mArrayAdapter.add(message);
        mInvitesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkLobbyExists(hostName, message);
            }
        });
    }

    private void checkLobbyExists(String hostName, String message) {
        DatabaseReference currentGameDB = FirebaseDatabase.getInstance().getReference().child("Games");
        currentGameDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.hasChild(hostName)) {
                    Log.i(TAG, "Lobby exists: true");
                    startLobbyActivity(hostName);
                }
                else{
                    displayToast("Lobby no longer exists");
                    Log.i(TAG, "Lobby exists: false");
                }
                mInvitesDB.child(hostName).removeValue();
                mArrayAdapter.remove(message);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void startLobbyActivity(String hostName) {
        mInvitesDB.removeEventListener(mInviteListener);
        Intent intent = new Intent(mContext, LobbyActivity.class);
        intent.putExtra("host_name", hostName);
        //TODO - add flags to close this fragment & Social Activity
        Log.i(TAG, "startLobby called");
        startActivity(intent);
    }

    private void displayToast(String message) {
        Toast.makeText(mContext, message,
                Toast.LENGTH_SHORT).show();
    }


}