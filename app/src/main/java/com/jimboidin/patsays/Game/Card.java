package com.jimboidin.patsays.Game;

import android.media.Image;

public class Card {
    private String suit;
    private int value;
    private boolean isFaceDown = false;
    private Image icon;

    public Card(String suit, int value, Image icon){
        this.suit = suit;
        this.value = value;
        this.icon = icon;
    }

    public String getSuit(){return suit; }
    public int getValue() { return value; }
    public boolean isFaceDown() {return isFaceDown; }
    public Image getIcon() {return icon; }

    public void setFaceDown(boolean faceDown) {isFaceDown = faceDown; }
}
