package com.jimboidin.patsays.Game;

import com.jimboidin.patsays.Utils.DeckBuilder;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Deck {
    private LinkedList<Card> cardLinkedList;

    public Deck() {
        populateDeck();
    }


    public LinkedList<Card> getCardLinkedList() { return cardLinkedList; }
    public int getSize() { return cardLinkedList.size(); }


    private void populateDeck() {
        DeckBuilder deckBuilder = new DeckBuilder();
        cardLinkedList = deckBuilder.createCardList();
    }

    public void removeJokers(){
        for (Card card : cardLinkedList){
            if (card.getSuit().equals("joker"))
                cardLinkedList.remove(card);
        }
    }


    public Card drawCard(){
        Random random = new Random();
        int randomIndex = random.nextInt(getSize());

        return cardLinkedList.remove(randomIndex);
    }

    public LinkedList<Card> dealHand(int players){
        int handSize = getSize() / players;
        LinkedList<Card> hand = new LinkedList<>();

        for (int i = 0; i < handSize; i++)
            hand.add(drawCard());

        return hand;
    }
}
