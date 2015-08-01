package com.example.winnabuska.jpokeri.CardSelect;

import android.content.Context;
import android.os.Vibrator;

import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.Dealer;

import java.util.HashMap;

/**
 * Created by WinNabuska on 12.7.2015.
 */
public class CardSwitcher {
    private final String STILL = "still", LEFT = "left", RIGHT = "right", UP = "up", DOWN = "down";
    private CardSelectActivity cardSelectActivity;
    private Card[]cards;
    private Dealer dealer;
    private Card[]cardsUnderJoker;
    private HashMap<String, int[]> animationParams;

    public CardSwitcher(CardSelectActivity cardSelectActivity){
        this.cardSelectActivity = cardSelectActivity;
        dealer = new Dealer();
        cards = dealer.giveARandomHand();
        cardsUnderJoker = new Card[5];
        initializeAnimationParams();
        for(int i = 0; i<(cards).length; i++) {
            cardSelectActivity.setImageButtonImage(i, getImageRecourcesID(cards[i]), animationParams.get(STILL));
            cardsUnderJoker[i] = cards[i];
        }
    }

    public CardSwitcher(CardSelectActivity cardSelectActivity, Card[]cards, Card[] cardsUnderJoker){
        this.cardSelectActivity = cardSelectActivity;
        dealer = new Dealer();
        this.cards = cards;
        this.cardsUnderJoker = cardsUnderJoker;
        initializeAnimationParams();
        for(int i = 0; i<(cards).length; i++)
            cardSelectActivity.setImageButtonImage(i, getImageRecourcesID(cards[i]), animationParams.get(STILL));
    }

    protected void onLongClick(int index) {
        if (cards[index].getNumericalSuit() == Card.SUIT_JOKER)
            cards[index] = cardsUnderJoker[index];
        else{
            cardsUnderJoker[index] = cards[index];
            cards[index] = dealer.getJoker();
        }
        changeCardsImage(index, animationParams.get(STILL));
    }

    protected void onSwipe(int index, String swipeDirection){
        vibrate(new long[]{100, 20, 100});
        if(cards[index].getNumericalSuit() != Card.SUIT_JOKER) {
            switch (swipeDirection) {
                case (DOWN):  cards[index] = dealer.getNextValueCard(cards[index]);    break;
                case (UP):    cards[index] = dealer.getPrevValueCard(cards[index]);    break;
                case (RIGHT): cards[index] = dealer.getNextSuitCard(cards[index]);     break;
                case (LEFT):  cards[index] = dealer.getPrevSuitCard(cards[index]);     break;
            }
        }
        else{
            cards[index] = cardsUnderJoker[index];
        }
        changeCardsImage(index, animationParams.get(swipeDirection));
    }

    private void changeCardsImage(int index , int [] currentAnimationParams){
        cardSelectActivity.setImageButtonImage(index, getImageRecourcesID(cards[index]), currentAnimationParams);

        for(int i = 0; i<5 ; i++)
            cardSelectActivity.setCardButtonFogged(false, i);
        cardSelectActivity.setRecomendationBtnEnability(true);
        cardSelectActivity.setRecomendationBtnFogged(false);
        for(int i = 0; i<4;i++) {
            for (int j = 1 + i; j < 5; j++) {
                if (cards[i] == cards[j]) {
                    cardSelectActivity.setCardButtonFogged(true, i);
                    cardSelectActivity.setCardButtonFogged(true, j);
                    cardSelectActivity.setRecomendationBtnEnability(false);
                    cardSelectActivity.setRecomendationBtnFogged(true);
                    break;
                }
            }
        }
    }

    private int getImageRecourcesID(Card card){
        String imageName;
        if(card.getNumericalSuit()!=Card.SUIT_JOKER) {
            imageName = card.getSuit()+card.getValue();
        }else{
            imageName = card.getSuit();
        }
        Context context = cardSelectActivity.getApplicationContext();
        return context.getResources().getIdentifier(imageName, "drawable", context.getPackageName());
    }

    protected void vibrate(long[] pattern){
        Vibrator vibrator = (Vibrator) cardSelectActivity.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
    }

    protected Card[] getCards(){
        return cards;
    }
    protected Card[] getCardsUnderJoker(){ return cardsUnderJoker; }

    private void initializeAnimationParams(){
        animationParams = new HashMap<>();
        animationParams.put(DOWN, new int[]{0,0,0,100});
        animationParams.put(UP, new int[]{0,0,0,-100});
        animationParams.put(RIGHT, new int[]{0, 100, 0, 0});
        animationParams.put(LEFT, new int[]{0, -100, 0, 0});
        animationParams.put(STILL, null);
    }


    /*protected void onSwipeLeft(int i) {
        selectedAnimationParams = animationParams.get("left");
        if (cards[i].getNumericalSuit() == Card.SUIT_JOKER) {
            changeCard(i, cardsUnderJoker[i]);
        }else{
            changeCard(i, dealer.getPrevSuitCard(cards[i]));
        }
    }
    protected void onSwipeRight(int i) {
        selectedAnimationParams = animationParams.get("right");
        if (cards[i].getNumericalSuit() == Card.SUIT_JOKER) {
            changeCard(i, cardsUnderJoker[i]);
        }else{
            changeCard(i, dealer.getNextSuitCard(cards[i]));
        }
    }
    protected void onSwipeUp(int i) {
        selectedAnimationParams = animationParams.get("up");
        if (cards[i].getNumericalSuit() == Card.SUIT_JOKER) {
            changeCard(i, cardsUnderJoker[i]);
        }else{
            changeCard(i, dealer.getPrevValueCard(cards[i]));
        }
    }

    protected void onSwipeDown(int i) {
        selectedAnimationParams = animationParams.get("down");
        if (cards[i].getNumericalSuit() == Card.SUIT_JOKER) {
            changeCard(i, cardsUnderJoker[i]);
        }else{
            changeCard(i, dealer.getNextValueCard(cards[i]));
        }
    }
    private void changeCard(int index, Card newCard){

        cards[index] = newCard;
        cardSelectActivity.setImageButtonImage(index, correspondingImage(newCard), selectedAnimationParams);

        boolean hasDuplicates = false;

        for(int i = 0; i<5; i++) {
            cardSelectActivity.setCardButtonFogged(false, i);
        }
        for(int i = 0; i<4;i++) {
            for (int j = 1 + i; j < 5; j++) {
                if (cards[i] == cards[j]) {
                    cardSelectActivity.setCardButtonFogged(true, i);
                    cardSelectActivity.setCardButtonFogged(true, j);
                    hasDuplicates = true;
                }
            }
        }
        if(hasDuplicates) {
            cardSelectActivity.setRecomendationBtnEnability(false);
        }
        else {
            cardSelectActivity.setRecomendationBtnEnability(true);
        }
    }*/
}
