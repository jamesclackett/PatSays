package com.jimboidin.patsays.Game;

import com.jimboidin.patsays.Utils.DeckBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/*
    The Deck class is used to allow GameActivity to interact with the deck of cards.
    The deck is build using Utils.DeckBuilder, but interacted with via the methods here.
*/

public class Deck {
    private ArrayList<Card> cardArrayList;

    // construct Deck
    public Deck() {
        cardArrayList = populateDeck();
    }


    public ArrayList<Card> getCardArrayList() { return cardArrayList; }

    public int getSize() { return cardArrayList.size(); }


    private ArrayList<Card> populateDeck() {
        DeckBuilder deckBuilder = new DeckBuilder();
        return deckBuilder.createCardList();
    }

    // May be used later if multiple game-modes are implemented in Pat Says
    public void removeJokers(){
        Iterator<Card> itr = cardArrayList.iterator();
        while (itr.hasNext()){
            Card card = itr.next();
            if (card.getSuit().equals("joker"))
                itr.remove();
        }
    }

    // draw and remove a random card from the deck.
    public Card drawCard(){
        Random random = new Random();
        int randomIndex = random.nextInt(getSize());

        return cardArrayList.remove(randomIndex);
    }

    // creates an ArrayList who's length is equal to the number of players.
    // Each index of the array list contains another array list of cards.
    // This method uses drawCard to get cards from the deck, and then inserts into the list
    public ArrayList<ArrayList<Card>> dealHand(int players){
        int handSize = 9;
        ArrayList<ArrayList<Card>> handList = new ArrayList<>();

        for (int i = 0; i < players; i++){
            ArrayList<Card> hand = new ArrayList<>();
            for (int j = 0; j < handSize; j++)
                hand.add(drawCard());

            handList.add(hand);
        }

        return handList;
    }

    // Similar in logic to dealHand.
    // uses drawCard to create a list of cards. This list is used for the
    // 'leftover' pile that players will draw from until empty.
    public ArrayList<Card> getLeftoverCards(){
        ArrayList<Card> leftoverList = new ArrayList<>();
        int size = getSize();
        for (int i = 0; i < size; i++){
            leftoverList.add(drawCard());
        }

        return leftoverList;
    }
}
