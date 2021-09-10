package com.jimboidin.patsays.Game;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.jimboidin.patsays.R;

import java.util.ArrayList;
import java.util.List;

/*
    Fragment to handle the logic and UI of players' on-table cards.
    As Pat Says allows multi-player, this avoids duplicating the same code for each player.
    Displays 3 face-down (hidden) cards, and three face-up.
    Card icons are represented as image views, and the actual card info (suit etc) is stored in
    an ArrayList<Card>


 */

public class TableCardsFragment extends Fragment {
    private ImageView mFinalCard0, mFinalCard1, mFinalCard2; // the face-down card icons
    private ImageView mChosenCard0, mChosenCard1, mChosenCard2; // the face-up cards icons.
    private ArrayList<Card> mChosenList, mFinalList;
    private DatabaseReference mDBRef;
    private Context mContext;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table_cards, container, false);
    }

    // Give the fragments ImageViews a reference to their layout.
    @Override
    public void onStart() {
        super.onStart();
        View view = getView();
        if (view != null){
            mFinalCard0 = view.findViewById(R.id.final_card_0);
            mFinalCard1 = view.findViewById(R.id.final_card_1);
            mFinalCard2 = view.findViewById(R.id.final_card_2);
            mChosenCard0 = view.findViewById(R.id.chosen_card_0);
            mChosenCard1 = view.findViewById(R.id.chosen_card_1);
            mChosenCard2 = view.findViewById(R.id.chosen_card_2);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        setDBListeners();
        mContext = context;
        super.onAttach(context);
    }

    // Sets the image icons of the chosen cards
    // Method is called when a change in the database (in chosen cards section) occurs
    public void setChosenCards(ArrayList<Card> chosenList){
        mChosenList = chosenList;
        mChosenCard0.setImageDrawable(mContext.getResources().getDrawable(mChosenList.get(0).getIconID()));
        mChosenCard1.setImageDrawable(mContext.getResources().getDrawable(mChosenList.get(1).getIconID()));
        mChosenCard2.setImageDrawable(mContext.getResources().getDrawable(mChosenList.get(2).getIconID()));
    }
    public void setFinalCards(ArrayList<Card> finalList){
        mFinalList = finalList;
    }

    private void setDBListeners(){
        //setChosenListener:
        // Waits for a change in the database and updates the fragments UI accordingly
        mDBRef.child("Chosen").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Card> tempList = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()){
                        Card card = child.getValue(Card.class);
                        tempList.add(card);
                    }
                    setChosenCards(tempList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
        //set Final Listener
        // Waits for a change in the database and updates the fragments final cards list
        mDBRef.child("Final").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()){
                    ArrayList<Card> tempList = new ArrayList<>();
                    for (DataSnapshot child : snapshot.getChildren()){
                        Card card = child.getValue(Card.class);
                        tempList.add(card);
                    }
                    setFinalCards(tempList);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // used by parent activity after creating the fragment.
    // Tells the fragment the location to set up its dataChange listeners
    public void setDBRef(DatabaseReference ref){
        mDBRef = ref;
    }

}