package com.jimboidin.patsays.Game;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class Deck {
    private HashMap<String, ArrayList<Card>> cardMap;

    public Deck() {
        populateDeck();
    }


    public HashMap<String, ArrayList<Card>> getCardMap() { return cardMap; }
    public int getSize() { return calculateSize(); }

    private void populateDeck() {
        //add all cards
    }

    public void removeJokers(){
        cardMap.remove("joker");
    }

    private int calculateSize() {
        int counter = 0;
        for (String key : cardMap.keySet()){
            counter += cardMap.get(key).size();
        }
        return counter;
    }

    public Card drawCard(){
        //get random suit
        Random random = new Random();
        List<String> keys = new ArrayList<>(cardMap.keySet());
        String randomKey = keys.get(random.nextInt(keys.size()));
        ArrayList<Card> randomSuit = cardMap.get(randomKey);

        //get random card
        random = new Random();
        int randomInt = random.nextInt(randomSuit.size());
        randomSuit.remove(randomInt);

        return randomSuit.remove(randomInt);

    }

    public ArrayList<Card> dealCards(int players){
        ArrayList<Card> dealtCardList = new ArrayList<>();
        int dealSize = calculateSize() / players;
        for (int i = 0; i < dealSize; i++){
            dealtCardList.add(drawCard());
        }
        return dealtCardList;
    }
}
