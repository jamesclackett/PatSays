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
    private LinkedList<Card> cardLinkedList;

    public Deck() {
        cardLinkedList = populateDeck();
    }


    public LinkedList<Card> getCardLinkedList() { return cardLinkedList; }
    public int getSize() { return cardLinkedList.size(); }


    private LinkedList<Card> populateDeck() {
        DeckBuilder deckBuilder = new DeckBuilder();
        return deckBuilder.createCardList();
    }

    public void removeJokers(){
        Iterator<Card> itr = cardLinkedList.iterator();
        while (itr.hasNext()){
            Card card = itr.next();
            if (card.getSuit().equals("joker"))
                itr.remove();
        }
    }


    public Card drawCard(){
        Random random = new Random();
        int randomIndex = random.nextInt(getSize());
        cardLinkedList.get(randomIndex).setFaceDown(true);

        return cardLinkedList.remove(randomIndex);
    }

    public ArrayList<LinkedList<Card>> dealHand(int players){
        int handSize = getSize() / players;
        ArrayList<LinkedList<Card>> handList = new ArrayList<>();

        for (int i = 0; i < players; i++){
            LinkedList<Card> hand = new LinkedList<>();
            for (int j = 0; j < handSize; j++)
                hand.add(drawCard());

            handList.add(hand);
        }

        return handList;
    }
}
