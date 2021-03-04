package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class User {
    private final String username, id;

        public User(String username, String id){
        this.username = username;
        this.id = id;
    }

    public String getUsername() { return username; }
    public String getId() { return id; }

    @NonNull
    @Override
    public String toString() { return getUsername(); }


    public static void invite(User myProfile, User toInvite, String hostname){
        if (myProfile != null && toInvite != null && hostname != null){
            DatabaseReference invitesDB = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(toInvite.getId()).child("Invitations");
            invitesDB.child(myProfile.getId()).child("invitee").setValue(myProfile.getUsername());
        }
    }

    public static void requestFriend(User myProfile, User toAdd){
        if (myProfile != null && toAdd != null){
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.child(toAdd.getId()).child("Friend_Requests").child(myProfile.getId()).setValue(true);
        }
    }

    public static void removeFriend(User myProfile, User user) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(myProfile.getId()).child("Friends").child(user.getId()).removeValue();
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getId()).child("Friends").child(myProfile.getId()).removeValue();
    }

    public static void handleRequest(User myProfile, User user, boolean isAccept) {
        if (isAccept){
            FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(myProfile.getId()).child("Friends").child(user.getId()).setValue(true);
            FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(user.getId()).child("Friends")
                    .child(myProfile.getId()).setValue(true);
        }
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(myProfile.getId()).child("Friend_Requests").child(user.getId()).removeValue();
    }
}