package com.example.winnabuska.jpokeri.CardLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.Parcelable;
import android.util.Log;

import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.CardLock.Evaluation.CardSwitchOption;
import com.example.winnabuska.jpokeri.CardLock.Evaluation.HandEvaluateService;
import com.example.winnabuska.jpokeri.CardLock.Evaluation.Scoring;
import com.example.winnabuska.jpokeri.R;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by WinNabuska on 18.7.2015.
 */
public class CardLockController {

    private CardLockActivity cardLockActivity;
    private int [] chartColors;
    private Card[] handCards;
    private ArrayList<CardSwitchOption> options;
    private CardSwitchOption selectedOption;
    private IntentFilter filter;
    private ArrayList<String> betAmounts;
    private double currentBet;

    public CardLockController(CardLockActivity cardLockActivity){
        this.cardLockActivity = cardLockActivity;
        Resources res = cardLockActivity.getResources();
        Scoring.setWinHandNames(res.getStringArray(R.array.win_hand_names_lang_finnish));
        chartColors = res.getIntArray(R.array.chart_colors);
        betAmounts = new ArrayList<>(Arrays.asList(res.getStringArray(R.array.bet_amounts)));
        currentBet = Double.valueOf(betAmounts.get(0));

        Object[] objCards = (Object[]) cardLockActivity.getIntent().getSerializableExtra("cards");
        handCards = new Card[objCards.length];
        for (int i = 0; i < handCards.length; i++)
            handCards[i] = (Card) objCards[i];

        startEvaluationService();
    }

    private void startEvaluationService(){
        filter = new IntentFilter();
        filter.addAction(HandEvaluateService.EVALUATION_STARTS);
        filter.addAction(HandEvaluateService.NEW_PATTERN_EVALUATION);
        filter.addAction(HandEvaluateService.PATTERN_PROGRESS);
        filter.addAction(HandEvaluateService.ALL_PATTERNS_EVALUATED);
        filter.addAction(HandEvaluateService.EXTRA_INFO);
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction().toString();

                if (action.equals(HandEvaluateService.EVALUATION_STARTS)) {
                    int numberOfSwitchOptions = intent.getIntExtra(HandEvaluateService.NUMBER_OF_PATTERS, 0);
                    if(numberOfSwitchOptions == 1)
                        cardLockActivity.setCancelLoadingButtonVisibility(false);
                    cardLockActivity.initializePrimaryProgressBar(numberOfSwitchOptions);
                }
                else if(action.equals(HandEvaluateService.EXTRA_INFO)){
                    String extraLoadInfo = intent.getStringExtra(HandEvaluateService.EXTRA_INFO);
                    cardLockActivity.putExtraLoadInfo(extraLoadInfo);
                }
                else if (action.equals(HandEvaluateService.NEW_PATTERN_EVALUATION)) {
                    cardLockActivity.incrementPrimaryProgressBar();
                    int casesInPattern = intent.getIntExtra(HandEvaluateService.CASES_IN_PATTERN, 0);
                    cardLockActivity.initializeSecondaryProgressBar(casesInPattern);
                    boolean[] pattern = intent.getBooleanArrayExtra(HandEvaluateService.PATTERN_BOOLEAN_ARRAY);
                    cardLockActivity.changeLoadCardImages(pattern, handCards);
                }
                else if (action.equals(HandEvaluateService.PATTERN_PROGRESS)) {
                    int secondaryProgress = intent.getIntExtra(HandEvaluateService.SINGLE_PATTERN_PROGRESS, 0);
                    cardLockActivity.setSecondaryProgressBarProgress(secondaryProgress);
                }
                else if (action.equals(HandEvaluateService.ALL_PATTERNS_EVALUATED)) {
                    Parcelable[] parcelableOptions = intent.getParcelableArrayExtra(HandEvaluateService.SWITCHOPTIONS);
                    if(parcelableOptions !=null) {
                        options = new ArrayList<>();
                        for (int i = 0; i < parcelableOptions.length; i++)
                            options.add((CardSwitchOption) parcelableOptions[i]);
                        selectedOption = findBestOption();
                    }
                    else{
                        CardSwitchOption changeAllCards = intent.getParcelableExtra(HandEvaluateService.SINGLESWITCHOPTION);
                        selectedOption = changeAllCards;
                        options.add(changeAllCards);
                    }
                    refreshAllInfo();
                    cardLockActivity.dismissDialog();
                }
            }
        };
        Intent evaluateService = new Intent(cardLockActivity.getApplicationContext(), HandEvaluateService.class);
        evaluateService.putExtra("cards", handCards);
        evaluateService.putExtra("command", "evaluate");
        cardLockActivity.registerReceiver(broadcastReceiver, filter);
        cardLockActivity.startService(evaluateService);
    }

    //This method is called when the user turns all the cards over to see the expected values and probabilities of the this so called 'extra switch option'.
    //Extra switch option is the option where all cards are discarded. This option is usually not automatically evaluated.
    //Only in cases where it is the most profitable to discard all the cards this option will be calculated automatically inside HandEvaluationService,
    //this info is retrieved using CardDataBaseAdapter that has all the 1914 cases listed where discard all is most profitable thing to do.
    //If so this method is not called, but HandEvaluationService evaluates extra switch option by it self.
    private void evaluateExtraSwitchOption(){
        Intent evaluateLastService = new Intent(cardLockActivity.getApplicationContext(), HandEvaluateService.class);
        evaluateLastService.putExtra("cards", handCards);
        evaluateLastService.putExtra("command", "evaluate_last");
        cardLockActivity.startService(evaluateLastService);
    }

    protected void refreshAllInfo(){
        refreshValueChartData();
        refreshProbabilityCharts();
        refreshTextInfo();
        refreshExpectedValueInfo();
        refreshCardImages();
        cardLockActivity.setChangeBetButtonText("Panos\n" + currentBet);
    }

    protected void betButtonClick(){
        //Change bet amount. First moves to tail, second first index 1 becomes index 0 (first) and all the values behind move one index forward.
        String oldBet = betAmounts.remove(0);
        betAmounts.add(oldBet);
        currentBet = Double.valueOf(betAmounts.get(0));
        refreshValueChartData();
        refreshExpectedValueInfo();
        refreshCardImages();
        cardLockActivity.setChangeBetButtonText("Panos\n" + currentBet);
    }

    private CardSwitchOption findBestOption(){
        double bestValue = 0;
        CardSwitchOption bestOption = null;
        for(CardSwitchOption option: options){
            if(option.getExpectedValue()>bestValue){
                bestValue = option.getExpectedValue();
                bestOption = option;
            }
        }
        return bestOption;
    }

    protected void cardImageClicked(int clickedIndex){
        //Find matching option
        boolean [] oldOptionPattern = selectedOption.getSwitchPattern();
        boolean selectedOptionFound = false;
        for(CardSwitchOption option:options){
            boolean[]optionPattern = option.getSwitchPattern();
            for(int i = 0; i<optionPattern.length; i++){
                if(i == clickedIndex && optionPattern[i] == oldOptionPattern[i])
                    break;
                else if(i != clickedIndex && optionPattern[i] != oldOptionPattern[i])
                    break;
                else if(i == optionPattern.length-1){
                    selectedOptionFound = true;
                    selectedOption = option;
                }
                else//so far so good
                    continue;
            }
            if(selectedOptionFound)
                break;
        }
        if(!selectedOptionFound){
            Log.i("info", "not found");
            evaluateExtraSwitchOption();
            cardLockActivity.showDialog();
        }
        refreshAllInfo();
    }

    private void refreshProbabilityCharts(){
        ArrayList<BarEntry>probabilities = new ArrayList<>();
        ArrayList<String> xAxisProbabilities = new ArrayList<>();
        ArrayList<Integer>includedIndexes = new ArrayList<>();
        int index = 0;

        for(int i = 0; i<9; i++) {
            if(selectedOption.getProbabilityOf(i)>0){
                includedIndexes.add(i);
                probabilities.add(new BarEntry((float)(selectedOption.getProbabilityOf(i)*100), index++));
                xAxisProbabilities.add(Scoring.winHandNames[i]);
            }
        }

        BarDataSet probability_BD_Set = new BarDataSet(probabilities, "%");
        probability_BD_Set.setColors(getProbabilityChartColors(includedIndexes));
        ArrayList<BarDataSet> yAxisProbabilities = new ArrayList<>();
        yAxisProbabilities.add(probability_BD_Set);

        BarData probabilityData = new BarData(xAxisProbabilities, yAxisProbabilities);

        cardLockActivity.setProbabilityBarChartData(probabilityData);
    }

    private void refreshValueChartData(){
        ArrayList<BarEntry>addedValues = new ArrayList<>();
        ArrayList<String> xAxisValues = new ArrayList<>();
        ArrayList<Integer>includedIndexes = new ArrayList<>();
        int index = 0;

        if(selectedOption.getExpectedValue()>0) {
            cardLockActivity.hideValueChart(false);

            for (int i = 1; i < 9; i++) {
                if (selectedOption.getProbabilityOf(i) > 0) {
                    includedIndexes.add(i);
                    double addedValue = selectedOption.getProbabilityOf(i) * Scoring.multipliers[i] * currentBet;
                    addedValues.add(new BarEntry((float) addedValue, index++));
                    xAxisValues.add(Scoring.winHandNames[i]);
                }
            }

            BarDataSet values_BD_Set = new BarDataSet(addedValues, "odotusarvo eriteltynä");

            values_BD_Set.setColors(getValueChartColors(includedIndexes));
            ArrayList<BarDataSet> yAxisValues = new ArrayList<>();
            yAxisValues.add(values_BD_Set);

            BarData valueData = new BarData(xAxisValues, yAxisValues);

            cardLockActivity.setValueBarChartData(valueData);
        }
        else
            cardLockActivity.hideValueChart(true);

    }

    private int[] getProbabilityChartColors(ArrayList<Integer> includedIndexes){
        int [] includedColors = new int[includedIndexes.size()];
        for(int i = 0; i<includedIndexes.size(); i++){
            includedColors[i] = chartColors[includedIndexes.get(i)];
        }
        return includedColors;
    }

    private int[] getValueChartColors(ArrayList<Integer> includedIndexes){
        int [] includedColors;
        if(includedIndexes.get(0) == Scoring.NOTHING) {
            includedIndexes.remove(0);
            includedIndexes.trimToSize();
        }
        includedColors = new int[includedIndexes.size()];
        for(int i = 0; i<includedIndexes.size(); i++) {
            includedColors[i] = chartColors[includedIndexes.get(i)];
        }
        return includedColors;
    }

    private void refreshTextInfo(){
        int[]outcomes = new int[10];

        outcomes[0] = selectedOption.getNumberOfDifferentOutcomes();
        for(int i = 0; i<9; i++){
            outcomes[i+1] = selectedOption.getRankCount(i);
        }
        cardLockActivity.setTextInfo(outcomes);
    }

    private void refreshExpectedValueInfo(){
        double expectedValue = 1000 * selectedOption.getExpectedValue()*currentBet;
        expectedValue = Math.round(expectedValue);
        expectedValue/=1000;
        cardLockActivity.setExpectedValueText("Odotusarvo:\n" + expectedValue + "e");
    }

    private void refreshCardImages(){
        for(int i = 0; i<handCards.length; i++){
            if((selectedOption.getSwitchPattern())[i])
                cardLockActivity.setLockCardImage(i, "card_backside");
            else
                cardLockActivity.setLockCardImage(i, handCards[i].getParameters());
        }
    }
}
