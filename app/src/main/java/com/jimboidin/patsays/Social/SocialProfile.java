package com.jimboidin.patsays.Social;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SocialProfile {
    private final String TAG = "SocialProfile.class";
    private String uId;
    private DatabaseReference profileDB;

    public SocialProfile(String uId){
        this.uId = uId;
        profileDB = FirebaseDatabase.getInstance().getReference().child("Users").child(uId);
    }

    public ArrayList<String> getRecentPlayers(){
        ArrayList<String> recentList = new ArrayList<>();
        profileDB.child("RecentPlayers").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        recentList.add(childData.getValue(String.class));
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return recentList;
    }

    public ArrayList<String> getFriendsRequests() {
        ArrayList<String> requestList = new ArrayList<>();
        profileDB.child("FriendRequests").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        requestList.add(childData.getValue(String.class));
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return requestList;
    }

    public ArrayList<String> getFriends() {
        ArrayList<String> friendsList = new ArrayList<>();
        profileDB.child("Friends").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        friendsList.add(childData.getValue(String.class));
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return friendsList;
    }

    public ArrayList<String> getInvites() {
        ArrayList<String> inviteList = new ArrayList<>();
        profileDB.child("Invitations").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    for (DataSnapshot childData : snapshot.getChildren()){
                        inviteList.add(childData.getValue(String.class));
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return inviteList;
    }
    public String getEmail() {
        final String[] email = {null};
        profileDB.child("email").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    email[0] = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return email[0];
    }

    public String getUsername() {
        final String[] username = {null};
        profileDB.child("username").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    username[0] = snapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        return username[0];
    }

    public void addFriend(String uId){
        profileDB.child("Friends").child(uId).setValue(true);
    }

    public void invite(String myName, String invitedName, String hostName){
        DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
        usersDB.orderByChild("username").equalTo(invitedName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String invitedUserID = null;
                for (DataSnapshot childDataSnapshot : dataSnapshot.getChildren()) {
                    invitedUserID = childDataSnapshot.getKey();
                    Log.d(TAG, "invite player: user found");
                }

                if (invitedUserID != null && myName != null){
                    usersDB.child(invitedUserID).child("Invitations")
                            .child(uId).child("host").setValue(hostName);
                    usersDB.child(invitedUserID).child("Invitations")
                            .child(uId).child("invitee").setValue(myName);
                    Log.d(TAG, "invite player: DB invite created");
                } else
                    Log.w(TAG, "invite player: invitation not created");
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void requestFriend(String uId){
        FirebaseDatabase.getInstance().getReference().child("Users").child(uId).child("FriendRequests")
                .child(this.uId).setValue(true);
    }

}
