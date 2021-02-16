package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;
import com.jimboidin.patsays.Game.HandListAdapter;

import java.util.ArrayList;
import java.lang.*;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private final String TAG = "GameActivity";
    private RecyclerView mHandRecyclerView;
    private RecyclerView.Adapter mHandListAdapter;
    private ArrayList<Card> mHandList, mSelectedList, mFinalList, mChosenList;
    private Boolean mIsHost;
    private ArrayList<String> mPlayerList;
    private String mHostName;
    private FirebaseAuth mAuth;
    private DatabaseReference mCurrentGameDB;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        mAuth = FirebaseAuth.getInstance();
        mIsHost = getIntent().getBooleanExtra("is_host", false);
        mHostName = getIntent().getStringExtra("host_name");
        mPlayerList = getIntent().getStringArrayListExtra("player_list");
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mSelectedList = new ArrayList<>();
        mHandList = new ArrayList<>();
        mChosenList = new ArrayList<>();

        Button chooseButton = findViewById(R.id.choose_cards_button);
        chooseButton.setOnClickListener(v -> placeChosenCards());

        if (mIsHost) {
            try {
                dealCards(mPlayerList.size());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else{
            setupDealtListener();
        }

    }


    private void dealCards(int players) throws Exception {
        System.out.println("dealCards() called!");
        if (players > 4 || players < 2)
            throw new Exception("Incorrect number of players");

        Deck deck = new Deck();
        HashMap<String, ArrayList<Card>> dealtMap = new HashMap<>();
        ArrayList<ArrayList<Card>> dealtDeck = deck.dealHand(players);

        for (int i = 0; i < players; i++)
            dealtMap.put(mPlayerList.get(i), dealtDeck.get(i));

        mHandList = dealtMap.get(mAuth.getUid());
        sendDealtCards(dealtMap);
        setupFinalCards();
    }

    private void sendDealtCards(HashMap<String, ArrayList<Card>> dealtMap) {
        for (int i = 0; i < mPlayerList.size(); i++){
            mCurrentGameDB.child("Players").child(mPlayerList.get(i))
                    .child("Cards").child("In_Hand").setValue(dealtMap.get(mPlayerList.get(i)));
        }
    }

    private void setupDealtListener(){
        mCurrentGameDB.child("Players").child(mAuth.getUid())
                .child("Cards").child("In_Hand").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                    getDealtCards();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void getDealtCards(){
        System.out.println("here 1");
        DatabaseReference myHandDb = mCurrentGameDB.child("Players").child(mAuth.getUid())
                .child("Cards").child("In_Hand");
        myHandDb.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    for (DataSnapshot child : snapshot.getChildren()){
                        Card card = child.getValue(Card.class);
                        mHandList.add(card);
                    }
                    setupFinalCards();
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    private void setupFinalCards() {
        mFinalList = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            mFinalList.add(mHandList.remove(i));

        mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Final").setValue(mFinalList);
        createInHandRecyclerView();
    }



    private void createInHandRecyclerView() {
        mHandRecyclerView = findViewById(R.id.hand_recycler_view);
        mHandRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(GameActivity.this, LinearLayoutManager.HORIZONTAL,
                        false);
        mHandRecyclerView.setLayoutManager(horizontalLayoutManager);
        System.out.println("handlist type is : " + mHandList.getClass());
        mHandListAdapter = new HandListAdapter(this, mHandList, mSelectedList);
        mHandRecyclerView.setAdapter(mHandListAdapter);
    }

    private void placeChosenCards() {
        if (mSelectedList.size() == 3){
            mChosenList.addAll(mSelectedList);
            ImageView chosenCard0 = findViewById(R.id.chosen_card_0);
            ImageView chosenCard1 = findViewById(R.id.chosen_card_1);
            ImageView chosenCard2 = findViewById(R.id.chosen_card_2);
            chosenCard0.setImageDrawable(getResources().getDrawable(mChosenList.get(0).getIconID()));
            chosenCard1.setImageDrawable(getResources().getDrawable(mChosenList.get(1).getIconID()));
            chosenCard2.setImageDrawable(getResources().getDrawable(mChosenList.get(2).getIconID()));

            mHandList.removeAll(mChosenList);
            mHandListAdapter.notifyDataSetChanged();
            Button chooseButton = findViewById(R.id.choose_cards_button);
            chooseButton.setVisibility(View.GONE);
            mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Chosen").setValue(mChosenList);
        }
        else {
            Log.w(TAG, "incorrect number of cards chosen. Must choose 3");
            showToast("Choose 3 cards only");
        }
    }

    private void setOpponentRecyclerViews() {

    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }


    private void closeGameServer(){
        if (mIsHost)
            mCurrentGameDB.removeValue(); //destroy gameServer
        else
            mCurrentGameDB.child("Players").child(mAuth.getUid()).removeValue();
        finish();
    }

    @Override
    public void onBackPressed() {
        closeGameServer();
        super.onBackPressed();
    }
}