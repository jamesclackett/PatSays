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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GameActivity extends AppCompatActivity {
    private String TAG = ".GameActivity";
    private LinkedList<Card> myCards, handList, selectedList, tableChosenList, tableFinalList, playPile;
    private ArrayList<ArrayList<Card>> opponentTableCards;
    private ArrayList<String> playerIds;
    private RecyclerView handRecyclerView;
    private RecyclerView.Adapter handListAdapter;
    private DatabaseReference currentPlayersDB;

    private ImageView finalCard0, finalCard1, finalCard2, chosenCard0, chosenCard1, chosenCard2;
    private Button chooseButton;
    private Button endButton;
    private Boolean host;
    private int players = 4;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);
        host = true;

        myCards = new LinkedList<>();
        handList = new LinkedList<>();
        selectedList = new LinkedList<>();
        tableChosenList = new LinkedList<>();
        tableFinalList = new LinkedList<>();
        playPile = new LinkedList<>();
        opponentTableCards = new ArrayList<>();
        playerIds = new ArrayList<>(); //will be given the uIds of each player (passed from lobby) using fake data for now.
        playerIds.add(FirebaseAuth.getInstance().getUid());
        playerIds.add(FirebaseAuth.getInstance().getUid()+"player2");
        playerIds.add(FirebaseAuth.getInstance().getUid()+"player3");
        playerIds.add(FirebaseAuth.getInstance().getUid()+"player4");


        //this reference is how other players will be involved in the same game
        currentPlayersDB = FirebaseDatabase.getInstance().getReference().child("Games")
                .child("game_"+FirebaseAuth.getInstance().getUid()).child("Players");

        currentPlayersDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                getCards(snapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        }); // calls getCards();

        chooseButton = findViewById(R.id.choose_cards_button);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeChosenCards();
            }
        });

        endButton = findViewById(R.id.end_game_button);
        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeGameServer();
            }
        });



        if (host) {
            try {
                dealCards(players);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        setupCards();
        createInHandRecyclerView();
    }



    private void dealCards(int players) throws Exception {
        if (players > 4 || players < 2)
            throw new Exception("Incorrect number of players");

        Deck deck = new Deck();
        ArrayList<LinkedList<Card>> dealtCards = new ArrayList<>();
        dealtCards = deck.dealHand(players);

        myCards = dealtCards.get(0);
        sendOpponentCards(dealtCards); //send cards to server
    }

    private void getCards(DataSnapshot snapshot){
        System.out.println("snapshot received");
        for (int i = 1; i < players; i++){ // start from 1 prevents own cards received (i think..) will use Ids later.
            ArrayList<Card> playerCards = (ArrayList<Card>) snapshot.child("player_"+i).child("Cards").child("In_hand").getValue();
            System.out.println(i + " " + playerCards);
            opponentTableCards.add(playerCards);
        }
        setOpponentRecyclerViews();
    }


    private void sendOpponentCards(ArrayList<LinkedList<Card>> dealtCards) {
        for (int i = 0; i < playerIds.size(); i++){
            currentPlayersDB.child(playerIds.get(i)).child("Cards").child("In_hand").setValue(dealtCards.get(i));
        }
        System.out.println("finished sending cards");
    }

    private void closeGameServer(){
        DatabaseReference currentGameServer = FirebaseDatabase.getInstance().getReference()
                .child("Games").child("game_"+FirebaseAuth.getInstance().getUid());
        currentGameServer.removeValue();
    }

    private void setupCards() {
        //Initialise card lists for game.
        for (int i = 0; i < 3; i++)
            tableFinalList.add(myCards.remove(i));

        handList.addAll(myCards);
    }

    private void createInHandRecyclerView() {
        handRecyclerView = findViewById(R.id.hand_recycler_view);
        handRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(GameActivity.this, LinearLayoutManager.HORIZONTAL,
                        false);
        handRecyclerView.setLayoutManager(horizontalLayoutManager);
        handListAdapter = new HandListAdapter(this, handList, selectedList);
        handRecyclerView.setAdapter(handListAdapter);
    }

    private void placeChosenCards() {
        if (selectedList.size() == 3){
            tableChosenList.addAll(selectedList);
            chosenCard0 = findViewById(R.id.chosen_card_0);
            chosenCard1 = findViewById(R.id.chosen_card_1);
            chosenCard2 = findViewById(R.id.chosen_card_2);
            chosenCard0.setImageDrawable(getResources().getDrawable(tableChosenList.get(0).getIconID()));
            chosenCard1.setImageDrawable(getResources().getDrawable(tableChosenList.get(1).getIconID()));
            chosenCard2.setImageDrawable(getResources().getDrawable(tableChosenList.get(2).getIconID()));

            handList.removeAll(tableChosenList);
            handListAdapter.notifyDataSetChanged();
            chooseButton.setVisibility(View.GONE);
        }
        else {
            Log.w(TAG, "incorrect number of cards chosen. Must choose 3");
            showToast("Choose 3 cards only");
        }
    }

    private void setOpponentRecyclerViews() {
            //System.out.println(opponentTableCards);
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }
    

}