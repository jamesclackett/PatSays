package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;


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
}