package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


// A class to take care of the user functions and data that are needed in all three social fragments
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

    // Creates an invitation entry in the database 'Users/{invitee}/Invitations/{my_profile}/{my_username}
    public static void invite(User myProfile, User toInvite){
        if (myProfile != null && toInvite != null ){
            DatabaseReference invitesDB = FirebaseDatabase.getInstance().getReference()
                    .child("Users").child(toInvite.getId()).child("Invitations");
            invitesDB.child(myProfile.getId()).child("invited_by").setValue(myProfile.getUsername());
        }
    }

    // Creates friend request entry in the database 'Users/{invitee}/Friend_Requests/{my_id}
    public static void requestFriend(User myProfile, User toAdd){
        if (myProfile != null && toAdd != null){
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users");
            usersDB.child(toAdd.getId()).child("Friend_Requests").child(myProfile.getId()).setValue(true);
        }
    }

    // This removes the friend from the database for both the user and the friend
    public static void removeFriend(User myProfile, User user) {
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(myProfile.getId()).child("Friends").child(user.getId()).removeValue();
        FirebaseDatabase.getInstance().getReference()
                .child("Users").child(user.getId()).child("Friends").child(myProfile.getId()).removeValue();
    }

    // If the user accepts a friend request, add to database for user & friend.
    // Removes the entry from /FriendRequests
    // TODO make a firebase function that takes care of this
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