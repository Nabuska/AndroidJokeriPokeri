package com.example.winnabuska.jpokeri.CardSelect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageButton;

import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.CardLock.CardLockActivity;
import com.example.winnabuska.jpokeri.R;

import java.util.ArrayList;


public class CardSelectActivity extends Activity {
    private ArrayList<ImageButton> imageButtons;
    private ImageButton calculateProbabilitiesBtn;
    private CardSwitcher cardSwitcher;

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_select);
        imageButtons = new ArrayList<>();
        imageButtons.add((ImageButton) findViewById(R.id.card_image_0));
        imageButtons.add((ImageButton) findViewById(R.id.card_image_1));
        imageButtons.add((ImageButton) findViewById(R.id.card_image_2));
        imageButtons.add((ImageButton) findViewById(R.id.card_image_3));
        imageButtons.add((ImageButton) findViewById(R.id.card_image_4));

        if (savedInstanceState == null) {
            cardSwitcher = new CardSwitcher(this);
        }else {
            Object[] objCards = (Object[]) savedInstanceState.getSerializable("cards");
            Object[] objCardsUnderJoker = (Object[]) savedInstanceState.getSerializable("cardsUnderJoker");
            Card[] cards = new Card[objCards.length];
            Card[] cardsUnderJoker = new Card[objCardsUnderJoker.length];
            for(int i = 0; i<objCards.length; i++){
                cards[i] = (Card)objCards[i];
                cardsUnderJoker[i] = (Card)objCardsUnderJoker[i];
            }

            cardSwitcher = new CardSwitcher(this, cards, cardsUnderJoker);
        }

        for (ImageButton current : imageButtons) {
            current.setOnTouchListener(new View.OnTouchListener() {

                float xPress, yPress, xLift, yLift;

                @Override
                public boolean onTouch(View currentView, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        xPress = event.getX();
                        yPress = event.getY();
                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        xLift = event.getX();
                        yLift = event.getY();
                        float xSwipe = xPress - xLift;
                        float ySwipe = yPress - yLift;
                        if ((Math.sqrt(xSwipe * xSwipe + ySwipe * ySwipe)) > 60) {
                            //Select cards
                            if (Math.abs(xSwipe) > Math.abs(ySwipe)) {
                                if (xSwipe > 0)
                                    cardSwitcher.onSwipe(imageButtons.indexOf(currentView), "left");
                                else
                                    cardSwitcher.onSwipe(imageButtons.indexOf(currentView), "right");
                            } else {
                                if (ySwipe > 0)
                                    cardSwitcher.onSwipe(imageButtons.indexOf(currentView), "up");
                                else
                                    cardSwitcher.onSwipe(imageButtons.indexOf(currentView), "down");
                            }
                        } else Log.i("swipe", "Swipe not registered");
                    }
                    return false;
                }
            });
            current.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View currentView) {
                    Log.i("info", "long click");
                    cardSwitcher.onLongClick(imageButtons.indexOf(currentView));
                    return false;
                }
            });
        }
        calculateProbabilitiesBtn = (ImageButton) findViewById(R.id.calculate_probabilities_btn);
        calculateProbabilitiesBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CardLockActivity.class);
                intent.putExtra("cards", cardSwitcher.getCards());
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable("cards", cardSwitcher.getCards());
        outState.putSerializable("cardsUnderJoker", cardSwitcher.getCardsUnderJoker());
    }

    protected void setCardButtonFogged(boolean transparent, int index){
        if(transparent)
            imageButtons.get(index).setAlpha(25);
        else
            imageButtons.get(index).setAlpha(1000);
    }

    protected void setRecomendationBtnEnability(boolean enabled){
        calculateProbabilitiesBtn.setEnabled(enabled);
    }

    protected void setRecomendationBtnFogged(boolean fogged){
        if(fogged) calculateProbabilitiesBtn.getBackground().setAlpha(15);
        else calculateProbabilitiesBtn.getBackground().setAlpha(1000);
    }

    protected void setImageButtonImage(final int index, final int imageResources, int[] animationParams){


        if(animationParams == null) {
            AlphaAnimation alphaAnimation = new AlphaAnimation(0.0f, 1.0f);
            alphaAnimation.setDuration(800);
            alphaAnimation.setFillAfter(true);
            imageButtons.get(index).setAnimation(alphaAnimation);
            imageButtons.get(index).startAnimation(alphaAnimation);
            imageButtons.get(index).setImageResource(imageResources);
        }
        else {
            TranslateAnimation slideAniamtion = new TranslateAnimation(animationParams[0], animationParams[1], animationParams[2], animationParams[3]);
            slideAniamtion.setDuration(50);
            slideAniamtion.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {}
                @Override
                public void onAnimationEnd(Animation animation) {imageButtons.get(index).setImageResource(imageResources);}
                @Override
                public void onAnimationRepeat(Animation animation) {}
            });
            imageButtons.get(index).setAnimation(slideAniamtion);
            imageButtons.get(index).startAnimation(slideAniamtion);
        }
    }
}