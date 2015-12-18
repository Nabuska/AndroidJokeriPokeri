package com.example.winnabuska.jpokeri.CardSelect;

import android.content.Context;
import android.os.Vibrator;
import android.widget.ImageButton;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.Dealer;

import java.util.HashMap;
import java.util.Set;

/**
 * Created by WinNabuska on 12.7.2015.
 */
public class CardSwitcher {
    private final String STILL = "still", LEFT = "left", RIGHT = "right", UP = "up", DOWN = "down";
    private CardSelectActivity cardSelectActivity;
    private Card[]cards;
    private Card[]cardsUnderJoker;
    private HashMap<String, int[]> animationParams;

    public CardSwitcher(CardSelectActivity cardSelectActivity){
        this.cardSelectActivity = cardSelectActivity;
        cards = Dealer.giveARandomHand();
        cardsUnderJoker = new Card[5];
        initializeAnimationParams();
        for(int i = 0; i<(cards).length; i++) {
            cardSelectActivity.setImageButtonImage(i, getImageRecourcesID(cards[i]), animationParams.get(STILL));
            cardsUnderJoker[i] = cards[i];
        }
    }

    public CardSwitcher(CardSelectActivity cardSelectActivity, Card[]cards, Card[] cardsUnderJoker){
        this.cardSelectActivity = cardSelectActivity;
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
            cards[index] = Dealer.getJoker();
        }
        changeCardsImage(index, animationParams.get(STILL));
    }

    protected void onSwipe(int index, String swipeDirection){
        vibrate(new long[]{100, 20, 100});
        if(cards[index].getNumericalSuit() != Card.SUIT_JOKER) {
            switch (swipeDirection) {
                case (DOWN):  cards[index] = Dealer.getNextValueCard(cards[index]);    break;
                case (UP):    cards[index] = Dealer.getPrevValueCard(cards[index]);    break;
                case (RIGHT): cards[index] = Dealer.getNextSuitCard(cards[index]);     break;
                case (LEFT):  cards[index] = Dealer.getPrevSuitCard(cards[index]);     break;
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
        cardSelectActivity.setCalculateButtonEnability(true);

        for(int i = 0; i<4;i++) {
            for (int j = 1 + i; j < 5; j++) {
                if (cards[i] == cards[j]) {
                    cardSelectActivity.setCardButtonFogged(true, i);
                    cardSelectActivity.setCardButtonFogged(true, j);
                    cardSelectActivity.setCalculateButtonEnability(false);
                    break;
                }
            }
        }
    }

    private int getImageRecourcesID(Card card){
        String imageName;
        if(card.getNumericalSuit()!=Card.SUIT_JOKER) {
            imageName = card.getSuitName()+card.getValue();
        }else{
            imageName = card.getSuitName();
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
}
