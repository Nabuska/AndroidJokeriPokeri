package com.example.winnabuska.jpokeri.CardLock;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.example.winnabuska.jpokeri.Card;
import com.example.winnabuska.jpokeri.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;

import java.util.ArrayList;

public class CardLockActivity extends Activity {

    private ArrayList<ImageButton> cardImageButtons;
    private View progressDialogCustomView;
    private BarChart probabilityBarChart;
    private BarChart valueBarChart;
    private CardLockController controller;

    private Dialog dialog;
    private ImageView[] loadingCardImages;
    private ProgressBar primaryProgressBar;
    private ProgressBar secondaryProgressBar;
    private Button cancelLoadingButton;

    private ArrayList<TextView> descriptions;
    private ArrayList<TextView> numbers;

    private TextView expected_value_textview;
    private Button changeBetBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card_lock);

        initProgressDialog();
        initTextualDescriptions();

        expected_value_textview = ((TextView) findViewById(R.id.expected_value_textview));

        probabilityBarChart = (BarChart) findViewById(R.id.probability_barchart);
        probabilityBarChart.animateXY(200, 200);
        probabilityBarChart.setDescription("");
        probabilityBarChart.getAxisRight().setEnabled(false);
        probabilityBarChart.getXAxis().setTextSize(1);

        valueBarChart = (BarChart) findViewById(R.id.value_barchart);
        valueBarChart.animateXY(200, 200);
        valueBarChart.getAxisRight().setEnabled(false);
        valueBarChart.setDescription("");
        valueBarChart.getXAxis().setSpaceBetweenLabels(0);
        valueBarChart.getXAxis().setTextSize(1);

        cardImageButtons = new ArrayList<>();
        cardImageButtons.add((ImageButton) findViewById(R.id.lockactivity_card_image_0));
        cardImageButtons.add((ImageButton) findViewById(R.id.lockactivity_card_image_1));
        cardImageButtons.add((ImageButton) findViewById(R.id.lockactivity_card_image_2));
        cardImageButtons.add((ImageButton) findViewById(R.id.lockactivity_card_image_3));
        cardImageButtons.add((ImageButton) findViewById(R.id.lockactivity_card_image_4));
        controller = new CardLockController(this);
        for(ImageView current: cardImageButtons){
            current.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    controller.cardImageClicked(cardImageButtons.indexOf(v));
                }
            });
        }

        changeBetBtn = (Button) findViewById(R.id.change_bet_btn);
        changeBetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                controller.betButtonClick();
            }
        });
    }

    private void initProgressDialog(){
        dialog = new Dialog(CardLockActivity.this);
        final LayoutInflater inflater = LayoutInflater.from(getApplicationContext());
        progressDialogCustomView = inflater.inflate(R.layout.loading_dialog, null);

        loadingCardImages = new ImageView[5];
        loadingCardImages[0] = (ImageView) progressDialogCustomView.findViewById(R.id.card_loading_image1);
        loadingCardImages[1] = (ImageView) progressDialogCustomView.findViewById(R.id.card_loading_image2);
        loadingCardImages[2] = (ImageView) progressDialogCustomView.findViewById(R.id.card_loading_image3);
        loadingCardImages[3] = (ImageView) progressDialogCustomView.findViewById(R.id.card_loading_image4);
        loadingCardImages[4] = (ImageView) progressDialogCustomView.findViewById(R.id.card_loading_image5);

        primaryProgressBar = (ProgressBar) progressDialogCustomView.findViewById(R.id.primaryBar);
        secondaryProgressBar = (ProgressBar) progressDialogCustomView.findViewById(R.id.secondaryBar);

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(progressDialogCustomView);
        dialog.setCancelable(false);
        cancelLoadingButton = (Button) dialog.findViewById(R.id.cancel_button);
        cancelLoadingButton.
                setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                        finish();
                    }
                });
        dialog.show();
    }

    private void initTextualDescriptions(){
        descriptions = new ArrayList<>();
        descriptions.add((TextView) findViewById(R.id.text_info_0));
        descriptions.add((TextView) findViewById(R.id.text_info_1));
        descriptions.add((TextView) findViewById(R.id.text_info_2));
        descriptions.add((TextView) findViewById(R.id.text_info_3));
        descriptions.add((TextView) findViewById(R.id.text_info_4));
        descriptions.add((TextView) findViewById(R.id.text_info_5));
        descriptions.add((TextView) findViewById(R.id.text_info_6));
        descriptions.add((TextView) findViewById(R.id.text_info_7));
        descriptions.add((TextView) findViewById(R.id.text_info_8));
        descriptions.add((TextView) findViewById(R.id.text_info_9));

        numbers = new ArrayList<>();
        numbers.add((TextView) findViewById(R.id.info_numbers_0));
        numbers.add((TextView) findViewById(R.id.info_numbers_1));
        numbers.add((TextView) findViewById(R.id.info_numbers_2));
        numbers.add((TextView) findViewById(R.id.info_numbers_3));
        numbers.add((TextView) findViewById(R.id.info_numbers_4));
        numbers.add((TextView) findViewById(R.id.info_numbers_5));
        numbers.add((TextView) findViewById(R.id.info_numbers_6));
        numbers.add((TextView) findViewById(R.id.info_numbers_7));
        numbers.add((TextView) findViewById(R.id.info_numbers_8));
        numbers.add((TextView) findViewById(R.id.info_numbers_9));
    }

    protected void setChangeBetButtonText(String text){
        changeBetBtn.setText(text);
    }

    protected void changeLoadCardImages(boolean[]pattern, Card[]handCards) {
        for(int i = 0; i<pattern.length; i++){
            if(pattern[i]){
                loadingCardImages[i].setImageResource(CardLockActivity.this.getResources().getIdentifier("card_backside", "drawable",
                        CardLockActivity.this.getPackageName()));
            }else{
                loadingCardImages[i].setImageResource(CardLockActivity.this.getResources().getIdentifier(handCards[i].getParameters(), "drawable",
                        CardLockActivity.this.getPackageName()));
            }
        }
    }

    protected void setLockCardImage(int index, String card){
        cardImageButtons.get(index).setImageResource(CardLockActivity.this.getResources().getIdentifier(card, "drawable", CardLockActivity.this.getPackageName()));
    }

    protected void initializePrimaryProgressBar(int progressMax){
        primaryProgressBar.setMax(progressMax);
        primaryProgressBar.setProgress(0);
    }

    protected void incrementPrimaryProgressBar(){
        primaryProgressBar.incrementProgressBy(1);
        vibrate(new long[]{0, 40});
    }

    protected void hideValueChart(boolean hide){
        if(hide)
            valueBarChart.setVisibility(View.GONE);
        else
            valueBarChart.setVisibility(View.VISIBLE);
    }

    protected void initializeSecondaryProgressBar(int progressMax){
        secondaryProgressBar.setMax(progressMax);
        secondaryProgressBar.setProgress(0);
    }

    protected void setSecondaryProgressBarProgress(int progress){
        secondaryProgressBar.setProgress(progress);
    }


    protected void vibrate(long[] pattern) {
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(pattern, -1);
    }

    protected void setCancelLoadingButtonVisibility(boolean visible){
        if(visible)
            cancelLoadingButton.setVisibility(View.VISIBLE);
        else
            cancelLoadingButton.setVisibility(View.GONE);
    }
    protected void dismissDialog(){
        dialog.dismiss();
    }

    protected void showDialog(){
        dialog.show();
    }

    protected void  setProbabilityBarChartData(BarData data){
        probabilityBarChart.setData(data);
        probabilityBarChart.invalidate();
    }

    protected void setValueBarChartData(BarData data){
        valueBarChart.setData(data);
        valueBarChart.invalidate();
    }

    protected void putExtraLoadInfo(String extraLoadInfo){
        ((TextView)progressDialogCustomView.findViewById(R.id.load_info_tv)).setText(extraLoadInfo);
    }


    protected void setTextInfo(int caseNumber, int total){
        int textColor = total==0 ? getResources().getColor(R.color.LightGrey) : getResources().getColor(R.color.Black);
        numbers.get(caseNumber).setText(String.valueOf(total));
        numbers.get(caseNumber).setTextColor(textColor);
        descriptions.get(caseNumber).setTextColor(textColor);
    }

    protected void setExpectedValueText(String text){
        expected_value_textview.setText(text);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_card_lock, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

