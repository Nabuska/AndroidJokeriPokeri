package com.example.winnabuska.jpokeri;

import java.util.ArrayList;

/**
 * Created by WinNabuska on 5.7.2015.
 */
public class Dealer {

    ArrayList<Card> cards;

    public Dealer(){
        try{
            cards = new ArrayList<>();
            for(int i = 1; i < 5; i++){
                for(int j = 1; j < 14; j++){
                    Card newCard = new Card(i, j);
                    cards.add(newCard);
                }
            }//Note: if joker is not the last card in the deck then method getJoker does not work right
            cards.add(new Card(0, 0));//Joker
        }catch(CardException e){e.printStackTrace();}
    }

    public Card[] giveARandomHand(){
        Card[] newHand = new Card[5];
        for(int i = -1; ++i<newHand.length;){
            newHand[i] = cards.get((int)(Math.random()*cards.size()));
            for(int j=0; j<i; j++){
                if(newHand[j] == newHand[i]){
                    i--;
                    break;
                }
            }
        }
        return newHand;
    }

    public Card[] getCards(){
        Card[] returnCards = new Card[cards.size()];
        return cards.toArray(returnCards);
    }

    public Card getJoker(){
        return cards.get(cards.size()-1);
    }

    public Card getPrevValueCard(Card card){
        if(card.getNumericalSuit() != Card.SUIT_JOKER) {
            int nextValue = card.getValue() - 1;
            if (nextValue < 1)
                nextValue = 13;
            return findCard(card.getSuit(), nextValue);
        }
        else
            return card;
    }

    public Card getNextValueCard(Card card){
        int nextValue = card.getValue() + 1;
        if(card.getNumericalSuit() != Card.SUIT_JOKER) {
            if (nextValue > 13)
                nextValue = 1;
            return findCard(card.getSuit(), nextValue);
        }
        else
            return card;
    }

    private Card findCard(String suit, int value){
        for(Card current: cards)
            if(current.getValue()==value && current.getSuit().equals(suit))
                return current;
        return null;
    }

    public Card getNextSuitCard(Card currentCard){

        if(currentCard.getNumericalSuit() != Card.SUIT_JOKER) {
            String nextSuite = null;
            switch (currentCard.getSuit()) {
                case ("hearts"):
                    nextSuite = "clubs";
                    break;
                case ("clubs"):
                    nextSuite = "diamonds";
                    break;
                case ("diamonds"):
                    nextSuite = "spades";
                    break;
                case ("spades"):
                    nextSuite = "hearts";
                    break;
                case ("joker"):
                    nextSuite = "hearts";
                    break;
            }
            return findCard(nextSuite, currentCard.getValue());
        }
        else
            return currentCard;
    }

    public Card getPrevSuitCard(Card currentCard){
        String nextSuite = null;
        if(currentCard.getNumericalSuit() != Card.SUIT_JOKER) {
            switch (currentCard.getSuit()) {
                case ("hearts"):
                    nextSuite = "spades";
                    break;
                case ("spades"):
                    nextSuite = "diamonds";
                    break;
                case ("diamonds"):
                    nextSuite = "clubs";
                    break;
                case ("clubs"):
                    nextSuite = "hearts";
                    break;
                case ("joker"):
                    nextSuite = "spades";
                    break;
            }
            return findCard(nextSuite, currentCard.getValue());
        }
        else
            return currentCard;
    }

    public String toString(){
        String returnStr = "";
        for(int i = 0; i<cards.size(); i++)
            returnStr+=cards.get(i).toString() + "\n";
        return returnStr;
    }
}

