package com.jimboidin.patsays;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;
import com.jimboidin.patsays.Game.HandListAdapter;
import com.jimboidin.patsays.Game.TableCardsFragment;

import java.util.ArrayList;
import java.lang.*;
import java.util.Calendar;
import java.util.HashMap;

public class GameActivity extends AppCompatActivity implements HandListAdapter.Listener {
    private final String TAG = "GameActivity";
    private RecyclerView mHandRecyclerView;
    private RecyclerView.Adapter mHandListAdapter;
    private ArrayList<Card> mHandList, mSelectedList;
    private Boolean mIsHost;
    private ArrayList<String> mPlayerList;
    private String mHostName;
    private FirebaseAuth mAuth;
    private DatabaseReference mCurrentGameDB;
    private ValueEventListener mDealtListener;
    private ChildEventListener mPlayerLeftListener;


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

        setupMyFragment();
        setupOpponentFragments();
        //setupPlayerLeftListener();
        startTimeoutHandler();

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
            if (!mPlayerList.get(i).equals(mHostName)){
                mCurrentGameDB.child("Players").child(mPlayerList.get(i))
                        .child("Cards").child("In_Hand").setValue(dealtMap.get(mPlayerList.get(i)));
            }
        }
    }

    private void setupDealtListener(){
        mDealtListener = mCurrentGameDB.child("Players").child(mAuth.getUid())
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
        ArrayList<Card> finalList = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            finalList.add(mHandList.remove(i));

        mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Final").setValue(finalList);
        mCurrentGameDB.child("Players").child(mAuth.getUid())
                .child("Cards").child("In_Hand").removeValue();
        createInHandRecyclerView();
    }

    private void setupMyFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TableCardsFragment myTableCards = new TableCardsFragment();
        myTableCards.setDBRef(mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards"));
        ft.replace(R.id.my_table_fragment, myTableCards);
        ft.commit();
    }

    private void setupOpponentFragments(){
        int[] idPlaceholders = new int[]{R.id.placeholder_0, R.id.placeholder_1, R.id.placeholder_2};
        ArrayList<String> opponentList = new ArrayList<>(); //create this to avoid building opponent fragment for self

        for (String str : mPlayerList)
            if (!str.equals(mAuth.getUid()))
                opponentList.add(str);

        for (int i = 0; i < opponentList.size(); i++){
            DatabaseReference playerDbRef = mCurrentGameDB.child("Players").child(opponentList.get(i)).child("Cards");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            TableCardsFragment opponentFragment = new TableCardsFragment();
            opponentFragment.setDBRef(playerDbRef);
            ft.replace(idPlaceholders[i], opponentFragment);
            ft.commit();
        }
    }


    private void createInHandRecyclerView() {
        mHandRecyclerView = findViewById(R.id.hand_recycler_view);
        mHandRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager horizontalLayoutManager =
                new LinearLayoutManager(GameActivity.this, LinearLayoutManager.HORIZONTAL,
                        false);
        mHandRecyclerView.setLayoutManager(horizontalLayoutManager);
        mHandListAdapter = new HandListAdapter(this, mHandList);
        mHandRecyclerView.setAdapter(mHandListAdapter);
    }

    @Override
    public void itemSelected(ArrayList<Card> selectedList) { //gets selectedList sent from HandListAdapter interface
        mSelectedList = selectedList;
    }

    private void placeChosenCards() {
        if (mSelectedList.size() == 3){
            mHandList.removeAll(mSelectedList);
            mHandListAdapter.notifyDataSetChanged();
            Button chooseButton = findViewById(R.id.choose_cards_button);
            chooseButton.setVisibility(View.GONE);
            mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Chosen").setValue(mSelectedList);
        }
        else {
            Log.w(TAG, "incorrect number of cards chosen. Must choose 3");
            showToast("Choose 3 cards only");
        }
    }


    private void showToast(String message) {
        Toast toast = Toast.makeText(this, message, Toast.LENGTH_LONG);
        toast.show();
    }


    private void closeGameServer(){
        Log.i(TAG, "gameserver close called");
        if (mDealtListener != null){
            mCurrentGameDB.child("Players").child(mAuth.getUid())
                    .child("Cards").child("In_Hand").removeEventListener(mDealtListener);
        }
        if (mPlayerLeftListener != null)
            mCurrentGameDB.child("Players").removeEventListener(mPlayerLeftListener);

        mCurrentGameDB.removeValue(); //destroy gameServer
        endTimeoutHandler();
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeGameServer();
    }

    private void displayToast(String message) {
        Toast.makeText(GameActivity.this, message,
                Toast.LENGTH_SHORT).show();
    }


    private Handler handler;
    private Runnable runnable, monitor;
    private void startTimeoutHandler(){
        handler = new Handler();

        runnable = new Runnable() {
            @Override
            public void run() {
                long time = Calendar.getInstance().getTimeInMillis();
                mCurrentGameDB.child("Game_Info").child("Players_Active").child(mAuth.getUid()).setValue(time);
                handler.postDelayed(this, 5000);
            }
        };
        runnable.run();

        //all clients monitor others for potential disconnection. Cannot rely on single client to monitor
        //as they may disconnect themselves.
        monitor = new Runnable() {
            @Override
            public void run() {
                long time = Calendar.getInstance().getTimeInMillis();
                mCurrentGameDB.child("Game_Info").child("Players_Active").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists())
                            for (DataSnapshot child : snapshot.getChildren()){
                                long lastTime = child.getValue(Long.class);
                                if (lastTime != 1 && (time - lastTime > 8000 || snapshot.getChildrenCount() != mPlayerList.size())){
                                    displayToast("Player Left - Ending Game..");
                                    Log.i(TAG, "Player disconnected");
                                    closeGameServer();
                                }
                            }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) { }
                });

                handler.postDelayed(this, 5000);
            }
        };
        monitor.run();
    }

    private void endTimeoutHandler(){
        if (handler != null){
            if (runnable != null)
                handler.removeCallbacks(runnable);
            if (monitor != null)
                handler.removeCallbacks(monitor);
        }
    }



}