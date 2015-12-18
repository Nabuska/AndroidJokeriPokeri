package com.example.winnabuska.jpokeri.CardLock.Evaluation;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.winnabuska.jpokeri.Dealer;

/**
 * Created by WinNabuska on 6.7.2015.
 */
public class CardSwitchOption implements Parcelable {

    public int[] outcomes;
    public int[] everySwitchIndex;
    public int numberOfSwitchCards = 0;

    public CardSwitchOption() {
        everySwitchIndex = new int[0];
        outcomes = new int[9];
    }

    public boolean[] getSwitchPattern() {
        boolean[] switchComb = new boolean[5];
        for (int current : everySwitchIndex) {
            switchComb[current] = true;
        }
        return switchComb;
    }

    //Calculates how many different results the cardswitchoption can have
    public int getNumberOfDifferentOutcomes() {
        int numberOfCardsInDeck = Dealer.cards.size()-5;

        int numberOfSwitchCards = everySwitchIndex.length;
        int numberOfCases = 1;

        for(int i = numberOfCardsInDeck; i>numberOfCardsInDeck-numberOfSwitchCards; i--)
            numberOfCases *= i;
        for(int i = numberOfSwitchCards; i > 1; i--)
            numberOfCases/=i;

        return numberOfCases;
    }

    public double getExpectedValue() {
        double total = 0;
        for (int i = 1; i < 9; i++) {
            total += outcomes[i] * Scoring.multipliers[i];
        }
        double expectedValue = total / getNumberOfDifferentOutcomes();
        return expectedValue;
    }

    public double getProbabilityOf(int rank) {
        double probability = (1.0 * outcomes[rank]) / getNumberOfDifferentOutcomes();
        return probability;
    }

    public void addSwitchIndex(int index) {
        int[] newIndexHolder = new int[++numberOfSwitchCards];
        System.arraycopy(everySwitchIndex, 0, newIndexHolder, 0, everySwitchIndex.length);
        newIndexHolder[newIndexHolder.length - 1] = index;
        everySwitchIndex = newIndexHolder;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.numberOfSwitchCards);
        dest.writeIntArray(this.everySwitchIndex);
        dest.writeIntArray(this.outcomes);
    }

    public CardSwitchOption(Parcel in) {
        this.numberOfSwitchCards = in.readInt();
        this.everySwitchIndex = new int[numberOfSwitchCards];
        this.outcomes = new int[9];
        in.readIntArray(this.everySwitchIndex);
        in.readIntArray(this.outcomes);
    }

    public static final Creator<CardSwitchOption> CREATOR = new Creator<CardSwitchOption>() {

        public CardSwitchOption createFromParcel(Parcel in) {
            return new CardSwitchOption(in);
        }

        public CardSwitchOption[] newArray(int size) {
            return new CardSwitchOption[size];
        }
    };
}