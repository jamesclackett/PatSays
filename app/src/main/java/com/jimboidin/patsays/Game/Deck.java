package com.jimboidin.patsays.Game;

import com.jimboidin.patsays.Utils.DeckBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Deck {
    private ArrayList<Card> cardArrayList;

    public Deck() {
        cardArrayList = populateDeck();
    }


    public ArrayList<Card> getCardArrayList() { return cardArrayList; }
    public int getSize() { return cardArrayList.size(); }


    private ArrayList<Card> populateDeck() {
        DeckBuilder deckBuilder = new DeckBuilder();
        return deckBuilder.createCardList();
    }

    public void removeJokers(){
        Iterator<Card> itr = cardArrayList.iterator();
        while (itr.hasNext()){
            Card card = itr.next();
            if (card.getSuit().equals("joker"))
                itr.remove();
        }
    }


    public Card drawCard(){
        Random random = new Random();
        int randomIndex = random.nextInt(getSize());

        return cardArrayList.remove(randomIndex);
    }

    public ArrayList<ArrayList<Card>> dealHand(int players){
        int handSize = getSize() / players;
        ArrayList<ArrayList<Card>> handList = new ArrayList<>();

        for (int i = 0; i < players; i++){
            ArrayList<Card> hand = new ArrayList<>();
            for (int j = 0; j < handSize; j++)
                hand.add(drawCard());

            handList.add(hand);
        }

        return handList;
    }
}
