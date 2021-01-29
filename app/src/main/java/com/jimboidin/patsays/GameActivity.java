package com.jimboidin.patsays;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;
import com.jimboidin.patsays.Game.HandListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

public class GameActivity extends AppCompatActivity {
    private String TAG = ".GameActivity";
    private LinkedList<Card> myCards, handList, selectedList, tableChosenList, tableFinalList, playPile;
    private RecyclerView handRecyclerView;
    private RecyclerView.Adapter handListAdapter;
    private ImageView finalCard0, finalCard1, finalCard2, chosenCard0, chosenCard1, chosenCard2;
    private Button chooseButton;
    private Boolean host = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        myCards = new LinkedList<>();
        handList = new LinkedList<>();
        selectedList = new LinkedList<>();
        tableChosenList = new LinkedList<>();
        tableFinalList = new LinkedList<>();
        playPile = new LinkedList<>();

        chooseButton = findViewById(R.id.choose_cards_button);
        chooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                placeChosenCards();
            }
        });

        if (host)
            dealCards(1);
        else
            getDealtCards(); //TODO - Add some kind of wait before downloading

        setupGame(); //TODO - Add some kind of wait before setting up
        createInHandRecyclerView();
        createOnTableRecyclerViews();
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

    private void createOnTableRecyclerViews() {
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


    private void dealCards(int players){
        //TODO - Create exception for more than 4 players or less than 2.
        // over 4 players will be impossible to display clearly on screen.

        Deck deck = new Deck();
        ArrayList<LinkedList<Card>> dealtCards = new ArrayList<>();
        dealtCards = deck.dealHand(players);

        myCards = dealtCards.remove(0);
        sendOpponentCards(dealtCards); //send cards to server
    }

    private void getDealtCards(){
        //get cards from server.
        myCards = null; //will be set to received list
    }

    private void setupGame() {
        //Initialise card lists for game.
        for (int i = 0; i < 3; i++)
            tableFinalList.add(myCards.remove(i));

        handList.addAll(myCards);
    }

    private void sendOpponentCards(ArrayList<LinkedList<Card>> dealtCards) {
        //send cards off to server
        //opponents will DL their own set and have a method similar to setup
    }

    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }

}