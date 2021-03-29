package com.jimboidin.patsays;

import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.Game.Deck;
import com.jimboidin.patsays.Utils.TheBrain;

import org.junit.Test;

import java.lang.reflect.Array;
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

        ArrayList<ArrayList<Card>> cardsList = deck.dealHand(players);
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
        ArrayList<Card> testCardList = deck.getCardArrayList();
        for (Card card : testCardList){
            if (card.getSuit().equals("joker")) counter++;
        }
        return counter;
    }

    @Test
    public void testBrain(){
        ArrayList<Card> handList = new ArrayList<>();
        handList.add(new Card("hearts", "2", R.drawable.hearts2));
        handList.add(new Card("hearts", "7", R.drawable.hearts7));
        handList.add(new Card("hearts", "4", R.drawable.hearts4));
        handList.add(new Card("hearts", "Ace", R.drawable.hearts11));
        handList.add(new Card("hearts", "3", R.drawable.hearts3));
        handList.add(new Card("hearts", "5", R.drawable.hearts5));

        ArrayList<Card> playPile = new ArrayList<>();
        playPile.add(new Card("diamonds", "3", R.drawable.diamonds3));
        playPile.add(new Card("diamonds", "7", R.drawable.diamonds9));
        playPile.add(new Card("diamonds", "9", R.drawable.diamonds7));
        playPile.add(new Card("diamonds", "4", R.drawable.diamonds4));

        ArrayList<String> turnList = new ArrayList<>();
        turnList.add("player 1");
        turnList.add("player 2");
        turnList.add("player 3");

        TheBrain brain = new TheBrain(handList, playPile, turnList);
        //System.out.println(brain.getNextTurn("player 1"));

        brain.getPlayable();
    }

}