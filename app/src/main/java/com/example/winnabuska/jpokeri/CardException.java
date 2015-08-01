package com.example.winnabuska.jpokeri;

import android.util.Log;

/**
 * Created by WinNabuska on 5.7.2015.
 */
public class CardException extends Exception {
    public CardException(int suit, int value){
        super("Card suit " + suit + " value " + value + " not defined");
        Log.i("exception", "Card Exception");
    }
}