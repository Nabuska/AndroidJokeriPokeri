package com.example.winnabuska.jpokeri.CardLock.Evaluation;

import android.app.IntentService;
import android.content.Intent;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.Dealer;
import com.example.winnabuska.jpokeri.R;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * helper methods.
 */
public class HandEvaluateService extends IntentService {

    public static final String EVALUATION_STARTS = "com.JPokeri.EVALUATION_STARTS";
    public static final String NEW_PATTERN_EVALUATION = "com.JPokeri.NEW_PATTERN_EVALUATION";
    public static final String PATTERN_PROGRESS = "com.JPokeri.PATTERN_PROGRESS";
    public static final String ALL_PATTERNS_EVALUATED = "com.JPokeri.PATTERNS_EVALUATED";
    public static final String EXTRA_INFO = "com.JPokeri.MY_EXTRA_INFO";

    public static final String NUMBER_OF_PATTERS = "com.JPokeri.NEW_PATTERN_EVALUATION";
    public static final String PATTERN_BOOLEAN_ARRAY = "com.JPokeri.PATTERN_BOOLEAN_ARRAY";
    public static final String CASES_IN_PATTERN = "com.JPokeri.CASES_IN_PATTERN";
    public static final String SINGLE_PATTERN_PROGRESS = "com.JPokeri.SINGLE_PATTERN_PROGRESS";
    public static final String SWITCHOPTIONS = "com.JPokeri.GET_SWITCHOPTIONS";
    public static final String SINGLESWITCHOPTION = "com.JPokeri.GET_SWITCHOPTIONS";

    private Card[] handCards;
    private Card[] deckCards;
    private CardSwitchOption [] cardSwitchOptions;

    public HandEvaluateService(){
        super(HandEvaluateService.class.getName());
    }
    public HandEvaluateService(String name){
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Object[] objCards = (Object[]) intent.getSerializableExtra("cards");
        handCards = new Card[5];
        for(int i = 0; i<5; i++)
            handCards[i] = (Card) objCards[i];

        deckCards = Stream.of(Dealer.cards)
                        .filter(dc -> Stream.of(handCards).noneMatch(c -> c==dc))
                        .collect(Collectors.toList())
                        .toArray(new Card[Dealer.cards.size()-5]);

        String command = intent.getStringExtra("command");
        Intent startIntent = new Intent(EVALUATION_STARTS);

        if(command.equals("evaluate")) {

            defineEveryCardSwitch();

            startIntent.putExtra(NUMBER_OF_PATTERS, cardSwitchOptions.length);
            HandEvaluateService.this.sendBroadcast(startIntent);

            long startTime =  System.currentTimeMillis();
            for (int i = cardSwitchOptions.length - 1; i >= 0; i--) {
                Intent middleAnnouncement = new Intent(NEW_PATTERN_EVALUATION);
                middleAnnouncement.putExtra(PATTERN_BOOLEAN_ARRAY, cardSwitchOptions[i].getSwitchPattern());
                middleAnnouncement.putExtra(CASES_IN_PATTERN, cardSwitchOptions[i].getNumberOfDifferentOutcomes());
                HandEvaluateService.this.sendBroadcast(middleAnnouncement);
                calculateExpectedValue(cardSwitchOptions[i]);
            }

            Intent endAnnoucement = new Intent(ALL_PATTERNS_EVALUATED);
            endAnnoucement.putExtra(SWITCHOPTIONS, cardSwitchOptions);
            HandEvaluateService.this.sendBroadcast(endAnnoucement);
        }
        else if(command.equals("evaluate_last")){
            startIntent.putExtra(NUMBER_OF_PATTERS, 1);
            HandEvaluateService.this.sendBroadcast(startIntent);
            CardSwitchOption changeAllCards = evaluateLastOption();
            Intent endAnnoucement = new Intent(ALL_PATTERNS_EVALUATED);
            endAnnoucement.putExtra(SINGLESWITCHOPTION, changeAllCards);
            HandEvaluateService.this.sendBroadcast(endAnnoucement);
        }
    }

    private CardSwitchOption evaluateLastOption(){
        CardSwitchOption changeAllCards = new CardSwitchOption();
        for(int i = 0; i<5; i++)
            changeAllCards.addSwitchIndex(i);

        Intent middleAnnouncement = new Intent(NEW_PATTERN_EVALUATION);
        middleAnnouncement.putExtra(PATTERN_BOOLEAN_ARRAY, changeAllCards.getSwitchPattern());
        middleAnnouncement.putExtra(CASES_IN_PATTERN, changeAllCards.getNumberOfDifferentOutcomes());
        HandEvaluateService.this.sendBroadcast(middleAnnouncement);

        calculateExpectedValue(changeAllCards);
        return changeAllCards;
    }

    private void defineEveryCardSwitch(){
        CardDataBaseAdapter dataBaseAdapter = new CardDataBaseAdapter(getApplicationContext());
        dataBaseAdapter.open();
        if(dataBaseAdapter.countRows() != CardDataBaseAdapter.EXPECTED_ROWS_SIZE){
            Intent extraAnnouncement = new Intent(EXTRA_INFO);
            extraAnnouncement.putExtra(EXTRA_INFO, getApplicationContext().getString(R.string.first_use_message));
            HandEvaluateService.this.sendBroadcast(extraAnnouncement);
            dataBaseAdapter.initializeDatabaseContent();
        }
        int optionsSize = 31;
        if(dataBaseAdapter.matchesExceptionCase(handCards)){
            Intent extraAnnouncement = new Intent(EXTRA_INFO);
            extraAnnouncement.putExtra(EXTRA_INFO, getApplicationContext().getString(R.string.exceptional_hand_pattern_message));
            HandEvaluateService.this.sendBroadcast(extraAnnouncement);
            optionsSize = 32;
        }
        dataBaseAdapter.close();
        cardSwitchOptions = new CardSwitchOption[optionsSize];
        int currentIndex = 0;
        for(int a = 0; a<2; a++)
            for(int b = 0; b<2; b++)
                for(int c = 0; c< 2; c++)
                    for(int d = 0; d<2; d++)
                        for(int e = 0; e<2; e++){
                            //patterns form in 5bit binary like manner 0 to 31
                            if(currentIndex < optionsSize) {
                                int [] indexes = {a, b ,c, d, e};
                                CardSwitchOption currentCardSwitch = new CardSwitchOption();
                                for(int n = 0; n<indexes.length; n++){
                                    if(indexes[n] == 1)
                                        currentCardSwitch.addSwitchIndex(n);
                                }
                                cardSwitchOptions[currentIndex] = (currentCardSwitch);
                            }
                            currentIndex++;
                        }
    }

    private void calculateExpectedValue(CardSwitchOption switchOption) {

        int[] everySwitchCardIndex = switchOption.everySwitchIndex;
        int numberOfSwitchCards = everySwitchCardIndex.length;
        int loops = 1;
        if (numberOfSwitchCards > 0) {
            int[] loopMax = getLoopMaxValues(switchOption, deckCards.length);
            boolean[] switchPattern = switchOption.getSwitchPattern();
            byte [] indexes = new byte[6];
            indexes[5] = -1;
            byte[] outerloopIndexes = getNextOuterloopIndexes(switchPattern);
            Card[] handCopy = handCards.clone();

            for (indexes[0] = (byte) (1 + indexes[outerloopIndexes[0]]); indexes[0] <= loopMax[0]; indexes[0]++) {
                if(switchPattern[0])
                    handCopy[0] = deckCards[indexes[0]];
                for (indexes[1] = (byte) (1 + indexes[outerloopIndexes[1]]); indexes[1] <= loopMax[1]; indexes[1]++) {
                    if(switchPattern[1])
                        handCopy[1] = deckCards[indexes[1]];
                    for (indexes[2] = (byte) (1 + indexes[outerloopIndexes[2]]); indexes[2] <= loopMax[2]; indexes[2]++) {
                        if(switchPattern[2])
                            handCopy[2] = deckCards[indexes[2]];
                        for (indexes[3] = (byte) (1 + indexes[outerloopIndexes[3]]); indexes[3] <= loopMax[3]; indexes[3]++) {
                            if(switchPattern[3])
                                handCopy[3] = deckCards[indexes[3]];
                            for (indexes[4] = (byte) (1 + indexes[outerloopIndexes[4]]); indexes[4] <= loopMax[4]; indexes[4]++) {
                                if(switchPattern[4])
                                    handCopy[4] = deckCards[indexes[4]];
                                switchOption.outcomes[Scoring.getHandRanking(handCopy)]++;
                                if(loops % 20000 == 0){
                                    Intent progressAnnouncement = new Intent(PATTERN_PROGRESS);
                                    progressAnnouncement.putExtra(SINGLE_PATTERN_PROGRESS, loops);
                                    HandEvaluateService.this.sendBroadcast(progressAnnouncement);
                                }
                                loops++;
                            }
                        }
                    }
                }
            }
        } else {
            switchOption.outcomes[Scoring.getHandRanking(handCards)]++;
        }
        Intent progressAnnouncement = new Intent(PATTERN_PROGRESS);
        progressAnnouncement.putExtra(SINGLE_PATTERN_PROGRESS, loops);
        HandEvaluateService.this.sendBroadcast(progressAnnouncement);
    }

    private byte [] getNextOuterloopIndexes(boolean[]switches){
        byte [] outerloopIndexes = new byte[switches.length+1];
        int lastBrotherIndex = 0;
        boolean hasBigBrothers =false;
        for(int i = 0; i<switches.length; i++){
            if(switches[i]) {
                if (!hasBigBrothers) {
                    hasBigBrothers = true;
                    outerloopIndexes[i] = 5;
                    lastBrotherIndex = i;
                }
                else{
                    outerloopIndexes[i] = (byte)lastBrotherIndex;
                    lastBrotherIndex = i;
                }
            }
            else {
                outerloopIndexes[i] = 5;
            }
        }
        return outerloopIndexes;
    }

    private int [] getLoopMaxValues(CardSwitchOption switchOption, int deckCardsLength){
        int [] max = new int[5];
        boolean [] switchPattern = switchOption.getSwitchPattern();
        int numberOfSwitchIndexes = 0;
        for(int i = max.length-1; i>=0; i--){
            if(switchPattern[i]) max[i] = deckCardsLength-(++numberOfSwitchIndexes);
            else max[i] = 0;
        }
        return max;
    }

    @Override
    public String toString(){
        String inHand="";
        for(Card current: handCards)
            inHand += current.toString() + ", ";
        return inHand.substring(0, inHand.lastIndexOf(","));
    }
}