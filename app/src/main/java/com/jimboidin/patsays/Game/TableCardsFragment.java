package com.jimboidin.patsays.Game;

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


public class TableCardsFragment extends Fragment {
    private ImageView mFinalCard0, mFinalCard1, mFinalCard2;
    private ImageView mChosenCard0, mChosenCard1, mChosenCard2;
    private ArrayList<Card> mChosenList, mFinalList;
    private DatabaseReference mDBRef;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_table_cards, container, false);
    }

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

    public void setChosenCards(ArrayList<Card> chosenList){
        mChosenList = chosenList;
        mChosenCard0.setImageDrawable(getResources().getDrawable(mChosenList.get(0).getIconID()));
        mChosenCard1.setImageDrawable(getResources().getDrawable(mChosenList.get(1).getIconID()));
        mChosenCard2.setImageDrawable(getResources().getDrawable(mChosenList.get(2).getIconID()));
    }
    public void setFinalCards(ArrayList<Card> finalList){
        mFinalList = finalList;
    }
    public void setDBRef(DatabaseReference ref){

        mDBRef = ref;
        //setChosenListener:
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

}