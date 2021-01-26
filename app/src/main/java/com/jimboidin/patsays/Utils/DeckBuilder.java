package com.jimboidin.patsays.Utils;

import android.media.Image;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.jimboidin.patsays.Game.Card;
import com.jimboidin.patsays.R;

import java.util.LinkedList;

public class DeckBuilder {
    LinkedList<Card> cardLinkedList = new LinkedList<>();

    public LinkedList<Card> createCardList(){
        //create jokers
        cardLinkedList.add(new Card("joker", "joker", R.drawable.joker));
        cardLinkedList.add(new Card("joker", "joker", R.drawable.joker));

        //create hearts
        cardLinkedList.add(new Card("hearts", "2", R.drawable.hearts2));
        cardLinkedList.add(new Card("hearts", "3", R.drawable.hearts3));
        cardLinkedList.add(new Card("hearts", "4", R.drawable.hearts4));
        cardLinkedList.add(new Card("hearts", "5", R.drawable.hearts5));
        cardLinkedList.add(new Card("hearts", "6", R.drawable.hearts6));
        cardLinkedList.add(new Card("hearts", "7", R.drawable.hearts7));
        cardLinkedList.add(new Card("hearts", "8", R.drawable.hearts8));
        cardLinkedList.add(new Card("hearts", "9", R.drawable.hearts9));
        cardLinkedList.add(new Card("hearts", "10", R.drawable.hearts10));
        cardLinkedList.add(new Card("hearts", "Ace", R.drawable.hearts11));
        cardLinkedList.add(new Card("hearts", "Jack", R.drawable.hearts12));
        cardLinkedList.add(new Card("hearts", "Queen", R.drawable.hearts13));
        cardLinkedList.add(new Card("hearts", "King", R.drawable.hearts14));

        //create diamonds
        cardLinkedList.add(new Card("diamonds", "2", R.drawable.diamonds2));
        cardLinkedList.add(new Card("diamonds", "3", R.drawable.diamonds3));
        cardLinkedList.add(new Card("diamonds", "4", R.drawable.diamonds4));
        cardLinkedList.add(new Card("diamonds", "5", R.drawable.diamonds5));
        cardLinkedList.add(new Card("diamonds", "6", R.drawable.diamonds6));
        cardLinkedList.add(new Card("diamonds", "7", R.drawable.diamonds7));
        cardLinkedList.add(new Card("diamonds", "8", R.drawable.diamonds8));
        cardLinkedList.add(new Card("diamonds", "9", R.drawable.diamonds9));
        cardLinkedList.add(new Card("diamonds", "10", R.drawable.diamonds10));
        cardLinkedList.add(new Card("diamonds", "Ace", R.drawable.diamonds11));
        cardLinkedList.add(new Card("diamonds", "Jack", R.drawable.diamonds12));
        cardLinkedList.add(new Card("diamonds", "Queen", R.drawable.diamonds13));
        cardLinkedList.add(new Card("diamonds", "King", R.drawable.diamonds14));

        //create clubs
        cardLinkedList.add(new Card("clubs", "2", R.drawable.clubs2));
        cardLinkedList.add(new Card("clubs", "3", R.drawable.clubs3));
        cardLinkedList.add(new Card("clubs", "4", R.drawable.clubs4));
        cardLinkedList.add(new Card("clubs", "5", R.drawable.clubs5));
        cardLinkedList.add(new Card("clubs", "6", R.drawable.clubs6));
        cardLinkedList.add(new Card("clubs", "7", R.drawable.clubs7));
        cardLinkedList.add(new Card("clubs", "8", R.drawable.clubs8));
        cardLinkedList.add(new Card("clubs", "9", R.drawable.clubs9));
        cardLinkedList.add(new Card("clubs", "10", R.drawable.clubs10));
        cardLinkedList.add(new Card("clubs", "Ace", R.drawable.clubs11));
        cardLinkedList.add(new Card("clubs", "Jack", R.drawable.clubs12));
        cardLinkedList.add(new Card("clubs", "Queen", R.drawable.clubs13));
        cardLinkedList.add(new Card("clubs", "King", R.drawable.clubs14));

        //create spades
        cardLinkedList.add(new Card("spades", "2", R.drawable.spades2));
        cardLinkedList.add(new Card("spades", "3", R.drawable.spades3));
        cardLinkedList.add(new Card("spades", "4", R.drawable.spades4));
        cardLinkedList.add(new Card("spades", "5", R.drawable.spades5));
        cardLinkedList.add(new Card("spades", "6", R.drawable.spades6));
        cardLinkedList.add(new Card("spades", "7", R.drawable.spades7));
        cardLinkedList.add(new Card("spades", "8", R.drawable.spades8));
        cardLinkedList.add(new Card("spades", "9", R.drawable.spades9));
        cardLinkedList.add(new Card("spades", "10", R.drawable.spades10));
        cardLinkedList.add(new Card("spades", "Ace", R.drawable.spades11));
        cardLinkedList.add(new Card("spades", "Jack", R.drawable.spades12));
        cardLinkedList.add(new Card("spades", "Queen", R.drawable.spades13));
        cardLinkedList.add(new Card("spades", "King", R.drawable.spades14));


        return cardLinkedList;
    }
}
