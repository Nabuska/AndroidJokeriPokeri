package com.example.winnabuska.jpokeri.CardLock.Evaluation;

import com.example.winnabuska.jpokeri.Card;

/**
 * Created by WinNabuska on 6.7.2015.
 */
public class Scoring {
    public final static int MULTIPLIER_OF_NOTHING = 0, MULTIPLIER_OF_TWOPAIRS = 2, MULTIPLIER_OF_THREEOFAKIND = 2, MULTIPLIER_OF_STRAIGHT = 3, MULTIPLIER_OF_FLUSH = 4,
            MULTIPLIER_OF_FULLHOUSE = 8, MULTIPLIER_OF_FOUROFAKIND = 15, MULTIPLIER_OF_STRAIGHTFLUSH = 30, MULTIPLIER_OF_FIVEOFAKIND = 50,
            NOTHING = 0, TWOPAIRS = 1, THREEOFAKIND = 2, STRAIGHT = 3, FLUSH = 4, FULLHOUSE = 5, FOUROFAKIND = 6, STRAIGHTFLUSH = 7, FIVEOFAKIND = 8;

    public final static int[] multipliers = new int[]{MULTIPLIER_OF_NOTHING, MULTIPLIER_OF_TWOPAIRS, MULTIPLIER_OF_THREEOFAKIND, MULTIPLIER_OF_STRAIGHT,
            MULTIPLIER_OF_FLUSH, MULTIPLIER_OF_FULLHOUSE, MULTIPLIER_OF_FOUROFAKIND, MULTIPLIER_OF_STRAIGHTFLUSH, MULTIPLIER_OF_FIVEOFAKIND};
    public final static int[] values = new int[]
            {NOTHING, TWOPAIRS, THREEOFAKIND, STRAIGHT, FLUSH, FULLHOUSE, FOUROFAKIND, STRAIGHTFLUSH, FIVEOFAKIND};


    private static final byte SUIT_JOKER = Card.SUIT_JOKER, VALUE_JOKER = Card.VALUE_JOKER, VALUE_ACE_LOW = Card.VALUE_ACE_LOW, VALUE_ACE_HIGH = Card.VALUE_ACE_HIGH;

    public static String [] winHandNames;
    public static void setWinHandNames(String [] names){
        Scoring.winHandNames = names;
    }


    private Scoring() {
    }

    public static int getHandRanking(Card[] cards) {
        byte[] cardValues = new byte[15];
        boolean hasExtraPare = false;
        boolean couldBeStraight = true;
        boolean isFlush = (cards[0].suit == cards[1].suit &&
                cards[1].suit == cards[2].suit &&
                cards[2].suit == cards[3].suit &&
                cards[3].suit == cards[4].suit);
        byte mostSameValue = 1;
        for (byte i = 0; i < 5; i++) {
            cardValues[cards[i].value]++;
            if (cardValues[cards[i].value] > mostSameValue) {
                mostSameValue = cardValues[cards[i].value];
                couldBeStraight = false;
            } else if (cardValues[cards[i].value] > 1) {
                hasExtraPare = true;
            }
        }

        if (cardValues[VALUE_JOKER] > 0) {
            byte[] handSuits = new byte[5];
            byte i;
            byte high = 1;
            for (i = 0; i < 5; i++) {
                if (++handSuits[cards[i].suit] > high) {
                    high = handSuits[cards[i].suit];
                } else if (handSuits[cards[i].suit] == high || cards[i].suit == SUIT_JOKER) {
                    continue;
                } else {
                    break;
                }
            }
            if (high == 4) isFlush = true;
        }

        cardValues[VALUE_ACE_HIGH] = cardValues[VALUE_ACE_LOW];

        //using hansSuits[Card.SUITE_JOKER] is not a reliable way to get the number of jokers.
        //To get the number of jokers use handValues[VALUE_JOKER]
        if (couldBeStraight) {
            if (cardValues[VALUE_JOKER] == 0) {
                if (isStraightNoJoker(cardValues)) {
                    if (isFlush)
                        return Scoring.STRAIGHTFLUSH;
                    else
                        return Scoring.STRAIGHT;
                }
                //Has joker
                else {
                    if (isFlush)
                        return Scoring.FLUSH;
                    else
                        return Scoring.NOTHING;
                }
            } else {
                if (isFlush) {
                    if (isStraightHasJoker(cardValues)) {
                        return Scoring.STRAIGHTFLUSH;
                    } else
                        return Scoring.FLUSH;
                } else {
                    if (isStraightHasJoker(cardValues))
                        return Scoring.STRAIGHT;
                    else
                        return Scoring.NOTHING;
                }
            }
        } else {
            mostSameValue += cardValues[Card.VALUE_JOKER];//plus jokers
            if (mostSameValue == 2) {
                if (hasExtraPare)
                    return Scoring.TWOPAIRS;
                else
                    return Scoring.NOTHING;
            } else if (mostSameValue == 3) {
                if (hasExtraPare)
                    return Scoring.FULLHOUSE;
                else
                    return Scoring.THREEOFAKIND;
            } else if (mostSameValue == 4) {
                return Scoring.FOUROFAKIND;
            } else if (mostSameValue == 5) {
                return Scoring.FIVEOFAKIND;
            } else {
                return 8;
            }
        }
    }

    private static boolean isStraightNoJoker(byte[] cardValues) {
        byte j;
        byte limit;
        for (byte i = 1; i < 10; i++) {
            if (cardValues[i] == 1) {
                limit = (byte) (i + 5);
                for (j = i; j < limit && cardValues[j] == 1; j++) {
                }
                if (j == limit) {
                    return true;
                } else if (i == 1) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }

    //This method should only be run after it is for sure there are no same value cards in hand
    private static boolean isStraightHasJoker(byte[] cardValues) {
        byte singlesWithinFive;
        for (byte i = 1; i < 10; i++) {
            if (cardValues[i] > 0) {
                singlesWithinFive = 0;
                byte limit = (byte) (i + 5);
                for (byte j = i; j < limit; j++) {
                    singlesWithinFive += cardValues[j];
                }
                if (singlesWithinFive == 4) {
                    return true;
                } else if (i == 1) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
}
