package com.jimboidin.patsays.Social;

import androidx.annotation.NonNull;

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