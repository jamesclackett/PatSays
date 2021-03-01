package com.jimboidin.patsays.Game;

import android.media.Image;

import androidx.annotation.NonNull;

//card values will correspond to face numbers - i.e. 2 will have a value of 2,
//ace will have a value of 11, jack will be 12 etc..

public class Card {
    private String suit;
    private String value;
    private int iconID;
    private boolean isSelected;

    public Card(){ }

    public Card(String suit, String value, int iconID){
        this.suit = suit;
        this.value = value;
        this.iconID = iconID;
        this.isSelected = false;
    }

    public String getSuit(){return suit; }
    public String getValue() { return value; }
    public int getIconID() {return iconID; }
    public boolean isSelected() {return  isSelected; }
    public void setSelected(boolean selected){ isSelected = selected; }
}
