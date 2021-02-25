package com.jimboidin.patsays.Social;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.jimboidin.patsays.R;

public class SocialActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_social);

        InvitationsFragment invitationsFragment = (InvitationsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.invitations_fragment);

    }

    /* TODO -- Create tabview with fragment pager that holds:

                   Friends fragment
                   Invitations Fragment
                   Recent Players


     */

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //TODO - close fragments and destroy event listeners
        //TODO - cloe activity
    }
}