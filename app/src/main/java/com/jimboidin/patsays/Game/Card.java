package com.jimboidin.patsays.Game;

import android.media.Image;

//card values will correspond to face numbers - i.e. 2 will have a value of 2,
//ace will have a value of 11, jack will be 12 etc..

public class Card {
    private String suit;
    private String value;
    private boolean isFaceDown = false;
    private int iconID;

    public Card(String suit, String value, int iconID){
        this.suit = suit;
        this.value = value;
        this.iconID = iconID;
    }

    public String getSuit(){return suit; }
    public String getValue() { return value; }
    public boolean isFaceDown() {return isFaceDown; }
    public int getIconID() {return iconID; }

    public void setFaceDown(boolean faceDown) {isFaceDown = faceDown; }
}
