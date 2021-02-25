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
    private LeaveSocialListener listener;

    public interface LeaveSocialListener{
        void onLeave();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_invitations, container, false);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
        listener = (LeaveSocialListener) context;
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        mInvitesListView = getView().findViewById(R.id.invites_listView);
        mArrayAdapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_1);
        mInvitesListView.setAdapter(mArrayAdapter);

        getInvitations();
    }

    private void getInvitations() {
        mInvitesDB = FirebaseDatabase.getInstance().getReference()
                .child("Users").child(mAuth.getUid()).child("Invitations");
        mInviteListener = mInvitesDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mArrayAdapter.clear();
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren())
                        if (childData.child("host").exists() && childData.child("invitee").exists()){

                            String hostNameStr = childData.child("host").getValue(String.class);
                            String inviteeStr = childData.child("invitee").getValue(String.class);

                            addInvitation(hostNameStr, inviteeStr);
                        }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    private void addInvitation(String hostName, String inviteeName) {
        String message = inviteeName + " invited you to join: " + hostName;
        mArrayAdapter.add(message);
        mInvitesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                checkLobbyExists(hostName);
                mInvitesDB.child(hostName).removeValue();
                mArrayAdapter.remove(message);
            }
        });
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
        listener.onLeave();
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
}