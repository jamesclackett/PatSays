
package com.jimboidin.patsays.Game;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.R;
import com.jimboidin.patsays.Social.User;
import com.jimboidin.patsays.Utils.TheBrain;

import java.util.ArrayList;
import java.lang.*;
import java.util.Calendar;
import java.util.HashMap;

/*
    NOTE: Please refer to game-rules.txt for an explanation of the Pat Says Game itself.

    GameActivity is where actual game-play between players happens
    UI:
        A horizontal RecyclerView centre screen containing the players in-hand cards (ordered)
        User can select cards from the recyclerview by clicking
        A fragment at bottom, containing the players on-table cards
        1-3 fragments at the top of screen (left | centre | right) for opponents on-table cards
        A button at top of screen that allows user to place the selected card(s)

    LOGIC:
        First, variables initialized and table-card fragments are created (own and opponents)
        Turn listener and Play Pile Listener are started
        Beginning of game separated into two paths (HOST or NON-HOST)
        HOST:
            > Sets up ReadyListener which waits for all players to choose their first cards.
            > Deals cards and sends them to the database. Also sends leftover cards to database

        NON-HOST:
            > Sets up dealt listener which pulls players hand from database when it appears.

        Afterwards, both host and non-host then setup their final cards (3 unknown face-down).
        After final cards are set, the In Hand recycler view is created with the remaining cards.
        At this point, the basic setup of game is complete. The next events must be triggered.

        Users select 3 cards from their hand and these are placed in their own fragment as chosen
        cards. When all players have chosen their cards, the host starts the first turn.

        From now onwards, the game works on a rotation of turns. On each turn, the players
        turnListener is triggered which prompts them to choose a card to play. If they are
        unable to play, handleNonPlay() is called which moved all play_pile cards into their hand
        if they are able to play a card the handlePlay() plays the card (ads it to top of play_pile
        and changes current game behaviour accordingly).
        handlePlay() also makes a call to pickupCard().
        Both handlePlay() and handleNonPlay() change the turn in the database. This triggers the next
        player's application and the cycle continues.

        NEEDED: RULES/EXPLANATION OF GAME FINISH/WIN

 */

public class GameActivity extends AppCompatActivity implements HandListAdapter.Listener {
    private final String TAG = "GameActivity";
    private RecyclerView mHandRecyclerView;
    private RecyclerView.Adapter mHandListAdapter;
    private ArrayList<Card> mHandList, mSelectedList,mPlayPile;
    private ImageView mPlayPileImage;
    private Boolean mIsHost;
    private ArrayList<String> mPlayerList;
    private HashMap<String, String> mUsernameMap;
    private String mHostName;
    private FirebaseAuth mAuth;
    private DatabaseReference mCurrentGameDB;
    private ValueEventListener mDealtListener, mTurnListener, mPlayPileListener, mLeftoverListener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Set up the custom toolbar
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        mAuth = FirebaseAuth.getInstance();

        // The host variables below allow the activity to act differently if the user is the host
        mIsHost = getIntent().getBooleanExtra("is_host", false);
        mHostName = getIntent().getStringExtra("host_name");

        mPlayerList = getIntent().getStringArrayListExtra("player_list");
        mCurrentGameDB = FirebaseDatabase.getInstance().getReference().child("Games").child(mHostName);
        mUsernameMap = new HashMap<String, String>(); // holds uid:username key-pairs
        mSelectedList = new ArrayList<>(); // cards that the user currently has selected
        mHandList = new ArrayList<>(); // cards in the users own hand
        mPlayPile = new ArrayList<>(); // cards that have been played
        mPlayPileImage = findViewById(R.id.playpile_image_view);

        createUsernameMap();
        setupMyFragment();
        setupOpponentFragments();
        initializeTurnListener();
        initializePlayPileListener();
        initializeLeftoverListener();
        //startTimeoutHandler();

        Button chooseButton = findViewById(R.id.choose_cards_button);
        chooseButton.setOnClickListener(v -> placeChosenCards());

        // The host deals cards and starts game when all players are ready
        // non-hosts wait for dealt cards
        if (mIsHost) {
            initializeReadyListener();
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


    //TODO - Firebase function for this.
    private void createUsernameMap(){
        for (int i = 0; i < mPlayerList.size(); i++){
            String uId = mPlayerList.get(i);
            DatabaseReference usersDB = FirebaseDatabase.getInstance().getReference().child("Users")
                    .child(uId);
            usersDB.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot.exists()){
                        System.out.println("HERE " + snapshot.child("username").getValue(String.class));
                        mUsernameMap.put(uId, snapshot.child("username").getValue(String.class));
                        System.out.println("username map size: " + mUsernameMap.size());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }


    // called by host application at the beginning of game
    // Double checks player count still OK
    // Creates a nested array list containing the cards for each player
    // makes calls to (sendDealtCards, sendLeftoverCards, setupFinalCards)
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
        sendLeftoverCards(deck.getLeftoverCards());
        setupFinalCards();
    }

    // simply adds the cards leftover to the database. These cards are picked up each turn until
    // there are none left.
    private void sendLeftoverCards(ArrayList<Card> leftoverCards) {
        mCurrentGameDB.child("Game_Info").child("Leftover_Cards").setValue(leftoverCards);
    }

    // For host application. Sends the dealt cards to the database.
    // gets hand in the form of a HashMap (player_name : ArrayList<Card>)
    // and sends to DB 'Players/{player_name}/Cards/In_Hand/{ArrayList}'
    private void sendDealtCards(HashMap<String, ArrayList<Card>> dealtMap) {
        for (int i = 0; i < mPlayerList.size(); i++){
            if (!mPlayerList.get(i).equals(mHostName)){
                mCurrentGameDB.child("Players").child(mPlayerList.get(i))
                        .child("Cards").child("In_Hand").setValue(dealtMap.get(mPlayerList.get(i)));
            }
        }
    }

    // For non-host applications.
    // Waits for the host to add the dealt cards to this user's DB then calls getDealtCards()
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

    // Adds the dealt cards in DB to the users Hand List (nothing in adapter yet)
    // calls setupFinalCards()
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

    // called by getDealtCards if non-host, by dealCards() if host
    // This method places three cards from hand list into finalList and adds to DB
    // InHand is no longer necessary in the DB so it is removed and the local handlist is ordered.
    // createInHandRecyclerView() is called
    private void setupFinalCards() {
        ArrayList<Card> finalList = new ArrayList<>();
        for (int i = 0; i < 3; i++)
            finalList.add(mHandList.remove(i));
        TheBrain.sort(mHandList);

        mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Final").setValue(finalList);
        mCurrentGameDB.child("Players").child(mAuth.getUid())
                .child("Cards").child("In_Hand").removeValue();
        createInHandRecyclerView();
    }

    /*

    ==============================================================================================
    EXPLANATION OF FRAGMENT BEHAVIOUR:
    ----------------------------------

    The fragments in this activity hold the players table cards, and their opponents table cards
    Table cards are both the face-down(final) and face-up(chosen) cards.

    As players use up their face-up (chosen) cards, they are removed from their array list,
    and their icon is turned to null/transparent

    When players have no more chosen cards, a single (selected) face-down card is revealed (ie the
    icon is changed from a red back to the icon which corresponds to its suit & value.
    Similar to face-up cards, when a player uses a card, its icon is set to null and it is removed
    from the array list

     */

    // Creates the users fragment and loads it into activity.
    // Gives the fragment a database reference that it can set its listeners to.
    private void setupMyFragment(){
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        TableCardsFragment myTableCards = new TableCardsFragment();
        myTableCards.setDBRef(mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards"));
        // TODO - figure out if there is a race condition between onAttach and setDBRef
        ft.replace(R.id.my_table_fragment, myTableCards);
        ft.commit();
    }

    // Creates the opponent fragment(s) and loads them into activity
    // creates depending on the number of players in player_list (-1 so not to include self)
    // opponentList == playerList minus self
    private void setupOpponentFragments(){
        int[] idPlaceholders = new int[]{R.id.placeholder_0, R.id.placeholder_1, R.id.placeholder_2};

        ArrayList<String> opponentList = new ArrayList<>();
        for (String str : mPlayerList)
            if (!str.equals(mAuth.getUid()))
                opponentList.add(str);

        // for every opponent, create a fragment in a similar fashion to the way own fragment is made
        //  ie create, insert and give db reference
        for (int i = 0; i < opponentList.size(); i++){
            DatabaseReference playerDbRef = mCurrentGameDB.child("Players").child(opponentList.get(i)).child("Cards");
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            TableCardsFragment opponentFragment = new TableCardsFragment();
            opponentFragment.setDBRef(playerDbRef);
            ft.replace(idPlaceholders[i], opponentFragment);
            ft.commit();
        }
    }

    // Sets up the the recyclerview which contains the players hand of cards
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


    //interface with HandListAdapter to get the cards the user has clicked (selected)
    @Override
    public void itemSelected(ArrayList<Card> selectedList) { //gets selectedList sent from HandListAdapter interface
        mSelectedList = selectedList;
        for (Card card : mSelectedList) {
            System.out.println(card.getSuit() + " " + card.getValue());
        }
    }

    // called when the player presses Place Cards button at the beginning of game
    // Makes sure the user selected 3 cards
    // Updates the users cards in the database and notifies that they are now 'ready'
    private void placeChosenCards() {
        if (mSelectedList.size() == 3){
            mHandList.removeAll(mSelectedList);
            mHandListAdapter.notifyDataSetChanged();
            Button chooseButton = findViewById(R.id.choose_cards_button);
            chooseButton.setVisibility(View.GONE);
            mCurrentGameDB.child("Players").child(mAuth.getUid()).child("Cards").child("Chosen").setValue(mSelectedList);
            mCurrentGameDB.child("Game_Info").child("Ready").child(mAuth.getUid()).setValue(true);
        }
        else {
            Log.w(TAG, "incorrect number of cards chosen. Must choose 3");
            displayToast("Choose 3 cards only");
        }
    }


    /*
        The below code is BAD. I will change it the minute I figure out a better way..

        It is designed to detect whether a player has been disconnected from game for more
        than 5 seconds. If so, the game is closed.
        I built this as the game cannot end if a player gets disconnected due to some kind of
        failure.
        It works by updating the DB constantly with a timestamp. It also constantly checks if the
        timestamp is over 5 seconds old. This is overkill for the simple thing I want to achieve.
    */
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
                                if (lastTime != 1 && (time - lastTime > 20000 || snapshot.getChildrenCount() != mPlayerList.size())){
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

    // This method simply waits for all players to be ready (ie have selected their table cards at
    // the beginning of the game. When all are ready, the turn is changed for the first time which
    // gets the game moving by itself.
    private void initializeReadyListener(){
        mCurrentGameDB.child("Game_Info").child("Ready").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    if (snapshot.getChildrenCount() == mPlayerList.size()){
                        mCurrentGameDB.child("Game_Info").child("turn").setValue(mAuth.getUid());
                        mCurrentGameDB.child("Game_Info").child("Ready").removeEventListener(this); //TODO: may not work, test later
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

    // Every time a player finishes their turn, the 'turn' variable is updated in the database
    // This triggers the listeners of this method. If the turn matches users name, then startChooser()
    // is triggered
    private void initializeTurnListener(){
        mTurnListener = mCurrentGameDB.child("Game_Info").child("turn").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    changeTurnIcon(snapshot.getValue(String.class));
                    if (snapshot.getValue(String.class).equals(mAuth.getUid())) {
                        startChooser();
                        for (Card card : mPlayPile) {
                            System.out.println("playpile test: " + card.getSuit() + ", " + card.getValue());
                        }
                    }
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Updates the leftover text view every time a card is taken from the leftover pile
    private void initializeLeftoverListener(){
        mLeftoverListener = mCurrentGameDB.child("Game_Info").child("Leftover_Cards").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                TextView leftoverTV = findViewById(R.id.leftoverTextView);
                if (snapshot.exists()){
                    long leftover = snapshot.getChildrenCount();
                    String str = Long.toString(leftover);
                    leftoverTV.setText(str);
                }
                else
                    leftoverTV.setText("0");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void changeTurnIcon(String uId){
        String player = mUsernameMap.get(uId);
        System.out.println("player is: " + player);
        TextView turnTV = findViewById(R.id.turnTextView);
        turnTV.setText(player);
    }


    // The play pile is where played cards are put
    // if a card is put in the play pile by any player, this listener is triggered.
    // the play pile list is appended to, and the play pile image is changed to the most recent card
    private void initializePlayPileListener(){
        mPlayPileListener = mCurrentGameDB.child("Game_Info").child("Play_Pile").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mPlayPile.clear();
                if (snapshot.exists()) {
                    if (snapshot.getValue() != null) {
                        for (DataSnapshot child : snapshot.getChildren()){
                            Card card = child.getValue(Card.class);
                            mPlayPile.add(card);
                        }
                        mPlayPileImage.setImageDrawable(getResources().getDrawable(mPlayPile.get(0).getIconID()));
                    }
                }
                else {
                        mPlayPileImage.setImageDrawable(null);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // this method begins the process of choosing a card to play.
    // Firstly, if TheBrain returns no playable cards, then handleNonPlay() is triggered
    // otherwise, the place card button is revealed.
    private void startChooser(){
        TheBrain brain = new TheBrain(mHandList, mPlayPile, mPlayerList);
        ArrayList<Card> playable = brain.getPlayable();
        String nextTurn = brain.getNextTurn(mAuth.getUid());
        Button placeButton = findViewById(R.id.place_button);

        if (playable.size() == 0){
            handleNonPlay(nextTurn);
        }

        else {
            placeButton.setVisibility(View.VISIBLE);
            placeButton.setOnClickListener(v -> handlePlay());
        }
    }

    // When a player cannot make a play because of the cards they have
    // called by startChooser based on the result of TheBrain's getPlayable() method
    // All play pile cards are moved to the users hand (play pile removed locally, and from DB)
    private void handleNonPlay(String nextTurn){
        displayToast("no playable cards. picking up..");
        mHandList.addAll(mPlayPile);
        TheBrain.sort(mHandList);
        mPlayPile.clear(); // THIS might not be necessary...?
        mCurrentGameDB.child("Game_Info").child("Play_Pile").removeValue();
        mHandListAdapter.notifyDataSetChanged();
        mCurrentGameDB.child("Game_Info").child("turn").setValue(nextTurn);
    }

    // Triggered when the user clicks on Place Card Button
    // checks if the selected cards are playable.
    // then checks if they are special cards and if so, handles this outside of method
    // Otherwise, the selected cards are moved from hand into the playpile. pickupCard() is run
    // The database is updated and the turn is changed
    private void handlePlay(){
        TheBrain brain = new TheBrain(mHandList, mPlayPile, mPlayerList);
        ArrayList<Card> playable = brain.getPlayable();
        String nextTurn = brain.getNextTurn(mAuth.getUid());
        if (mSelectedList.size() == 0)
            return; // if no card(s) selected, nothing will happen and it is still user's turn

        String value = mSelectedList.get(0).getValue();
        // check if card is playable and make sure if multiple cards, they are of the same value.
        for (Card card : mSelectedList) {
            if (!playable.contains(card) || !card.getValue().equals(value)) {
                displayToast("Selected unplayable card(s)");
                return;
            }
        }

        if (brain.isActionable(value)){
            switch (value){
                case "10":
                    play10();
                    break;
                case "joker":
                    chooseJokerVictim();
                    break;
                case "8":
                    String turn = brain.getNextTurn(nextTurn); //twice because 8 equals skip go
                    play8(turn);
                    break;
            }
        }
        else {
            for (Card card : mSelectedList){
                mHandList.remove(card);
                mPlayPile.add(0, card);
            }
            mHandListAdapter.notifyDataSetChanged();
            mCurrentGameDB.child("Game_Info").child("Play_Pile").setValue(mPlayPile);
            pickupCard(); // check if there are cards still leftover and pickup if so
            Button placeButton = findViewById(R.id.place_button);
            placeButton.setVisibility(View.GONE);
            mCurrentGameDB.child("Game_Info").child("turn").setValue(nextTurn);
        }

    }

    // Handle play of specual card: 10
    private void play10() {
        for (Card card : mSelectedList)
            mHandList.remove(card);
        mHandListAdapter.notifyDataSetChanged();
        mCurrentGameDB.child("Game_Info").child("Play_Pile").removeValue();
        pickupCard();
        Button placeButton = findViewById(R.id.place_button);
        placeButton.setVisibility(View.GONE);
        mCurrentGameDB.child("Game_Info").child("turn").setValue("GO_AGAIN!");
        mCurrentGameDB.child("Game_Info").child("turn").setValue(mAuth.getUid());

    }

    // Allows the player to select the opponent they wish to play the Joker against
    private void chooseJokerVictim(){
        FrameLayout fl0 = findViewById(R.id.placeholder_0);
        FrameLayout fl1 = findViewById(R.id.placeholder_1);
        FrameLayout fl2 = findViewById(R.id.placeholder_2);

        ArrayList<FrameLayout> flList = new ArrayList<>();
        flList.add(fl0);
        flList.add(fl1);
        flList.add(fl2);

        for (FrameLayout fl : flList){
            fl.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    System.out.println("frame_layout id : "+ fl.getId());
                }
            });
        }


    }

    // Handle play of special card: Joker
    private void playJoker() {
        String victim = "hahah";
        for (Card card : mSelectedList)
            mHandList.remove(card);
        mHandListAdapter.notifyDataSetChanged();
        mCurrentGameDB.child("Players").child(victim).child("Joker").setValue(true);
        pickupCard();
        Button placeButton = findViewById(R.id.place_button);
        placeButton.setVisibility(View.GONE);
        mCurrentGameDB.child("Game_Info").child("turn").setValue(victim);


    }

    // Handle play of special card: 8
    private void play8(String nextTurn) {
        for (Card card : mSelectedList){
            mHandList.remove(card);
            mPlayPile.add(0, card);
        }
        mHandListAdapter.notifyDataSetChanged();
        mCurrentGameDB.child("Game_Info").child("Play_Pile").setValue(mPlayPile);
        pickupCard();
        Button placeButton = findViewById(R.id.place_button);
        placeButton.setVisibility(View.GONE);
        mCurrentGameDB.child("Game_Info").child("turn").setValue("SKIP!");
        mCurrentGameDB.child("Game_Info").child("turn").setValue(nextTurn);

    }

    // This method checks if there are cards remaining in the 'leftover' pile and if so adds one
    // to the user's hand.
    // pickupCard is called when the user makes a play
    private void pickupCard() {
        mCurrentGameDB.child("Game_Info").child("Leftover_Cards").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Card> temp = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()){
                        temp.add(child.getValue(Card.class));
                    }
                    Card pickupCard = temp.remove(0);
                    mHandList.add(pickupCard);
                    TheBrain.sort(mHandList);
                    mHandListAdapter.notifyItemInserted(mHandList.indexOf(pickupCard));
                    mCurrentGameDB.child("Game_Info").child("Leftover_Cards").setValue(temp);
                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    // Clears up the game and its associated listeners & data before exiting activity
    private void closeGameServer(){
        Log.i(TAG, "gameserver close called");
        removeListeners();
        mCurrentGameDB.removeValue(); //destroy gameServer
        endTimeoutHandler();
        finish();
    }

    // Terminates listeners so they do not continue to run after activity is destroyed
    private void removeListeners(){
        if (mDealtListener != null) {
            mCurrentGameDB.child("Players").child(mAuth.getUid())
                    .child("Cards").child("In_Hand").removeEventListener(mDealtListener);
        }
        if (mTurnListener != null)
            mCurrentGameDB.child("Game_Info").child("turn").removeEventListener(mTurnListener);
        if (mPlayPileListener != null)
            mCurrentGameDB.child("currentCard").removeEventListener(mPlayPileListener);
        if (mLeftoverListener != null)
            mCurrentGameDB.child("Game_Info").child("Leftover_Cards").removeEventListener(mLeftoverListener);
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

}