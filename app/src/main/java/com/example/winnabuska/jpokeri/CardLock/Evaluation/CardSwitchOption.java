package com.example.winnabuska.jpokeri.CardLock.Evaluation;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by WinNabuska on 6.7.2015.
 */
public class CardSwitchOption implements Parcelable {

    private static final String KEY_OUTCOMES = "outcomes";
    private static final String KEY_INDEX_OF_EVERY_SWITCH = "outcomes";

    private int[] outcomes;
    private int[] indexOfEverySwitch;
    private int numberOfSwitchCards = 0;

    public CardSwitchOption() {
        indexOfEverySwitch = new int[0];
        outcomes = new int[9];
    }

    public boolean[] getSwitchPattern() {
        boolean[] switchComb = new boolean[5];
        for (int current : indexOfEverySwitch) {
            switchComb[current] = true;
        }
        return switchComb;
    }

    public int getNumberOfDifferentOutcomes() {
        int numberOfOutcomes = 0;
        for (int i = 0; i < outcomes.length; i++)
            numberOfOutcomes += outcomes[i];
        return numberOfOutcomes;
    }

    public int getNumberOfSwitchCards(){
        return numberOfSwitchCards;
    }

    public double getExpectedValue() {
        double total = 0;
        for (int i = 1; i < 9; i++) {
            total += outcomes[i] * Scoring.multipliers[i];
        }
        double expectedValue = total / getNumberOfDifferentOutcomes();
        return expectedValue;
    }

    public int[] getindexOfEverySwitch() {
        return indexOfEverySwitch;
    }

    protected void addScoreRanking(int ranking) {
        outcomes[ranking]++;
    }

    public double getProbabilityOf(int rank) {
        double probability = (1.0 * outcomes[rank]) / getNumberOfDifferentOutcomes();
        return probability;
    }

    public int[] getOutcomes() {
        return outcomes;
    }

    public int getRankCount(int rank){
        return outcomes[rank];
    }


    public void addSwitchIndex(int index) {
        int[] newIndexHolder = new int[++numberOfSwitchCards];
        System.arraycopy(indexOfEverySwitch, 0, newIndexHolder, 0, indexOfEverySwitch.length);
        newIndexHolder[newIndexHolder.length - 1] = index;
        indexOfEverySwitch = newIndexHolder;
    }

    @Override
    public String toString() {
        return "toString no created";
        /*String strValue = "\nKeep: ";
        for(int i = 0; i<5; i++)
            strValue += (getSwitchPattern())[i] +"|";
        strValue +="\n";
        for(Card current:keepCards)
            if(current!=null)
                strValue += current.toString() + ", ";
        strValue = strValue.substring(0, strValue.length()-2);
        strValue += "\nNumber of Combinations: " + getNumberOfDifferentOutcomes() + "\nExpected value = " + getExpectedValue();*/
//        for(int i = 0; i<outcomes.length; i++){
//            strValue += "\n" + Scoring.winHandNames[i] + ": " + 100.0*outcomes[i]/getNumberOfDifferentOutcomes();
//            strValue += "\n\t\tScenarios: " + outcomes[i];
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.numberOfSwitchCards);
        dest.writeIntArray(this.indexOfEverySwitch);
        dest.writeIntArray(this.outcomes);
    }

    public CardSwitchOption(Parcel in) {
        this.numberOfSwitchCards = in.readInt();
        this.indexOfEverySwitch = new int[numberOfSwitchCards];
        this.outcomes = new int[9];
        in.readIntArray(this.indexOfEverySwitch);
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