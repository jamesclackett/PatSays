package com.jimboidin.patsays.Utils;

import com.jimboidin.patsays.Game.Card;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TheBrain {
    private ArrayList<Card> mHandList;
    private ArrayList<Card> mPlayPile;
    private final ArrayList<String> mTurnList;
    private final List<String> orderedRegular =
            Arrays.asList("4", "5", "6", "7", "8", "9", "Jack", "Queen", "King", "Ace");
    private final List<String> special = Arrays.asList("2", "3", "10", "joker");


    public TheBrain(ArrayList<Card> handList, ArrayList<Card> playPile, ArrayList<String> turnList){
        this.mHandList = handList;
        this.mPlayPile = playPile;
        this.mTurnList = turnList;
    }


    public ArrayList<Card> getPlayable(){
        ArrayList<Card> playableList = new ArrayList<>();

        if (mPlayPile.size() == 0) { //all cards are playable if play pile is clear
            return mHandList;
        }

        Card frontCard = mPlayPile.get(0);
        if (frontCard.getValue().equals("3") && mPlayPile.size()>1)
            frontCard = mPlayPile.get(1);

        for (Card card : mHandList){
            //check if special card or if still 3 (3 was only card in pile)
            if (special.contains(card.getValue()) || frontCard.getValue().equals("3")){
                playableList.add(card);
                System.out.println("cond 1 triggered by " + card.getValue());
            }
            else if (frontCard.getValue().equals("7")){
                if (orderedRegular.indexOf(card.getValue()) <= orderedRegular.indexOf("7")){
                    playableList.add(card);
                    System.out.println("cond 2 triggered by " + card.getValue());
                }
            }
            else if (orderedRegular.indexOf(card.getValue()) >= orderedRegular.indexOf(frontCard.getValue())){
                    playableList.add(card);
                    System.out.println("cond 3 triggered by " + card.getValue());
            }
        }
        return playableList;
    }

    public String getNextTurn(String currentTurn){
        //returns uid key of next player
        int index = mTurnList.indexOf(currentTurn);
        if (index +1 == mTurnList.size())
            return mTurnList.get(0);
        else
            return mTurnList.get(index+1);
    }

    public boolean isActionable(String value){
        return value.equals("10") || value.equals("8") || value.equals("joker");
    }


}
