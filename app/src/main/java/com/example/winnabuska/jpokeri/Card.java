package com.example.winnabuska.jpokeri;

import android.util.Log;

import java.io.Serializable;

/**
 * Created by WinNabuska on 12.7.2015.
 */
public class Card implements Serializable {

    public static String [] suits = new String [] {"joker" ,"hearts", "diamonds", "clubs", "spades"};

    public final static byte SUIT_HEARTS     = 1;
    public final static byte SUIT_DIAMONDS   = 2;
    public final static byte SUIT_CLUBS      = 3;
    public final static byte SUIT_SPADES     = 4;
    public final static byte SUIT_JOKER      = 0;
    public final static byte VALUE_JOKER      = 0;
    public final static byte VALUE_ACE_LOW    = 1;
    public final static byte VALUE_ACE_HIGH   = 14;

    public final byte suit;
    public final byte value;
    private static int numberOfCards = 0;
    private int id;

    public Card(int suit, int value) throws CardException{
        id = numberOfCards++;
        if(suit>=1 && suit<=4 && value>=1 && value<=13){
            this.suit = (byte) suit;
            this.value = (byte) value;
        }
        else if(suit == 0){
            this.suit = (byte) suit;
            this.value = 0;
        }
        else throw new CardException(suit, value);

    }

    public String getSuitName() {
        return suits[suit];
    }

    public int getNumericalSuit(){
        return suit;
    }

    public int getValue() {
        return value;
    }

    public int getID(){
        return id;
    }

    @Override
    public String toString(){
        if(value < 2 || value >10){
            switch(value){
                case(1):    return "Ace of " + suits[suit];
                case(11):   return "Jack of " + suits[suit];
                case(12):   return "Queen of " + suits[suit];
                case(13):   return "King of " + suits[suit];
                case(0):    return "Joker";
                default:    Log.e("error", "invalide card. suit = " + suit + " | value = " + value);
                            System.exit(0); break;
            }
        }
        return value + " of " + Card.suits[suit];
    }

    public String getParameters(){
        if(suit == Card.SUIT_JOKER)
            return suits[suit];
        else
            return suits[suit] + value;
    }
}