package com.jimboidin.patsays;

import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void deckPopulatedCorrectly(){
        assertEquals(54, new Deck().getSize());
    }

    @Test
    public void deckContainsTwoJokers(){
        Deck deck = new Deck();
        assertEquals(2, getNumJokers(deck));
    }


    @Test
    public void jokersRemovedCorrectly(){
        Deck deck = new Deck();
        deck.removeJokers(); // issue here
        assertEquals(0, getNumJokers(deck));
    }

    @Test
    public void drawCardTest(){
        Deck deck = new Deck();
        Card card = deck.drawCard();
        System.out.print(card.getSuit() + ", " + card.getValue() + ", " + card.getIconID());
        assertEquals(53, deck.getSize());
    }

    @Test
    public void dealHandTest(){
        Deck deck = new Deck();
        int players = 4;

        ArrayList<LinkedList<Card>> cardsList = deck.dealHand(players);
        assertEquals(players, cardsList.size());

        System.out.print("players = " + cardsList.size()
                + " | handSize = " + cardsList.get(0).size()
                + " | cards left in deck = " + deck.getSize());

        for (int i = 0; i < cardsList.size(); i++){
            assertEquals(54 / players, cardsList.get(i).size()); // cards per player
        }
        assertEquals(54 % players, deck.getSize()); // cards leftover
    }

    private int getNumJokers(Deck deck) {
        int counter = 0;
        LinkedList<Card> testCardList = deck.getCardLinkedList();
        for (Card card : testCardList){
            if (card.getSuit().equals("joker")) counter++;
        }
        return counter;
    }
}