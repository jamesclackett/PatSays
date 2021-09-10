package com.jimboidin.patsays.Utils;

import com.jimboidin.patsays.Game.Card;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/*
    The Brain separates some of the game logic from GameActivity.
    It can:
        > calculate who's turn is next
        > Which cards in a list are playable against a single card
        > return if a card is an actionable (ie 10, joker, 8)
        > Sort the cards in ascending order.

    Note: in TheBrain, 7 and 8 are not included in the 'special' list because they cannot
    be played at any time.
*/

public class TheBrain {
    private ArrayList<Card> mHandList;
    private ArrayList<Card> mPlayPile;
    private final ArrayList<String> mTurnList;
    private static final List<String> orderedRegular =
            Arrays.asList("4", "5", "6", "7", "8", "9", "Jack", "Queen", "King", "Ace");
    private static final List<String> special = Arrays.asList("2", "3", "10", "joker");
    public static List<String> order =
            Arrays.asList("4", "5", "6", "7", "8", "9", "Jack", "Queen", "King", "Ace", "2", "3", "10", "joker");


    public TheBrain(ArrayList<Card> handList, ArrayList<Card> playPile, ArrayList<String> turnList){
        this.mHandList = handList;
        this.mPlayPile = playPile;
        this.mTurnList = turnList;
    }


    // returns the list of cards that can be played on top of the most recent play pile card
    // special cards are always added
    // reverses the checking behaviour if a 7 has been played
    // otherwise, uses index number to know if a card is greater or equal (ie can be played)
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

    // As turn list is specified on creation of TheBrain, all that is needed is to calculate
    // the next position in the list to get the next turn
    public String getNextTurn(String currentTurn){
        //returns uid key of next player
        int index = mTurnList.indexOf(currentTurn);
        if (index +1 == mTurnList.size())
            return mTurnList.get(0);
        else
            return mTurnList.get(index+1);
    }

    // Actionable Cards are those which change the behavior of the game briefly and
    // require some extra code.
    // While 7 is technically an actionable card, no extra code is needed other than
    // an extra if condition in getPlayable(), so it is not included here.
    public boolean isActionable(String value){
        return value.equals("10") || value.equals("8") || value.equals("joker");
    }


    // uses collections.sort and sorts based on the cards position in the ordered list above
    // TODO- maybe change return value to void
    public static ArrayList<Card> sort(ArrayList<Card> cards){
        Collections.sort(cards, new SortByValue());
        return cards;
    }

    static class SortByValue implements Comparator<Card> {
        @Override
        public int compare(Card cardA, Card cardB) {
            return order.indexOf(cardA.getValue()) - order.indexOf(cardB.getValue());
        }
    }




}
