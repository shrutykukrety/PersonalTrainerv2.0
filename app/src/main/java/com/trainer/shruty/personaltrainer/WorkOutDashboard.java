package com.trainer.shruty.personaltrainer;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;


import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class WorkOutDashboard extends AppCompatActivity  implements
        RecognitionListener {

    DBHandlerExercise db = new DBHandlerExercise(this);

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
    private static final String LEE_SEARCH = "lee";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "hey trainer";
    private static final String PUSHPHRASE = "push up";
    private static final String PULLPHRASE = "pull up";
    private static final String STARTPHRASE = "start";
    private static final String STOPPHRASE = "stop";

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_out_dashboard);

        //Update Cards
        updateCards();

        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(LEE_SEARCH, R.string.lee_caption);

        ((TextView) findViewById(R.id.voice_recog_text))
                .setText("Preparing the recognizer");
        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(WorkOutDashboard.this);
                    File assetDir = assets.syncAssets();
                    setupRecognizer(assetDir);
                } catch (IOException e) {
                    return e;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Exception result) {
                if (result != null) {
                    ((TextView) findViewById(R.id.voice_recog_text))
                            .setText("Failed to init recognizer ");// + result);
                } else {
                    switchSearch(KWS_SEARCH);
                }
            }
        }.execute();


    }

    private void updateCards() {
        DBExercise lastExes = db.getLastExercise();
        SimpleDateFormat sf = new SimpleDateFormat("MMM dd");
        if (lastExes != null) {
            //Update Last Date
            TextView txt = (TextView) findViewById(R.id.push_last_try);
            txt.setText("Last Try : " + String.valueOf(sf.format(lastExes.getDate())));
        }
    }

    public void callPushUp(View view) {
        recognizer.stop();
        //Start the next activity
        Intent intent = new Intent(this, WorkOut.class);
        intent.putExtra("exercise", "push");
        startActivity(intent);
    }
    //     * In partial result we get quick updates about current hypothesis. In
//     * keyword spotting mode we can react here, in other modes we need to wait
//     * for final result in onResult.
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        if (text.equals(KEYPHRASE)) {
            Log.d("shruty: ", text);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.kung_fu);
            mediaPlayer.start(); // no need to call prepare(); create() does that for you
            switchSearch(LEE_SEARCH);
        }
//        else if (text.equals(DIGITS_SEARCH))
//            switchSearch(DIGITS_SEARCH);
//        else if (text.equals(PHONE_SEARCH))
//            switchSearch(PHONE_SEARCH);
//        else if (text.equals(FORECAST_SEARCH))
//            switchSearch(FORECAST_SEARCH);
        else if(PUSHPHRASE.contains(text) || PULLPHRASE.contains(text)) {
            /*((TextView) findViewById(R.id.result_text)).setText("Start: " + text + " ups");
            switchSearch(KWS_SEARCH);*/
            recognizer.stop();
            Log.d("Start/Stop: ", text);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.europa);
            mediaPlayer.start();
            //Start the next activity
            Intent intent = new Intent(this, WorkOut.class);
            if (PUSHPHRASE.contains(text))
                intent.putExtra("exercise", "push");
            else
                intent.putExtra("exercise", "pull");
            startActivity(intent);
        }
    }


    //     * This callback is called when we stop the recognizer.
    @Override
    public void onResult(Hypothesis hypothesis) {
//        ((TextView) findViewById(R.id.result_text)).setText("");
//        if (hypothesis != null) {
//            String text = hypothesis.getHypstr();
//            text = "you chose : " + text;
//            makeText(getApplicationContext(), text, Toast.LENGTH_SHORT).show();
//            switchSearch(KWS_SEARCH);
//        }
       /* String text = hypothesis.getHypstr();
        if(PUSHPHRASE.contains(text) || PULLPHRASE.contains(text)) {
            *//*((TextView) findViewById(R.id.result_text)).setText("Start: " + text + " ups");
            switchSearch(KWS_SEARCH);*//*
            //Start the next activity
            Intent intent = new Intent(this, WorkOut.class);
            if (PUSHPHRASE.contains(text))
                intent.putExtra("exercise", "push");
            else
                intent.putExtra("exercise", "pull");
            startActivity(intent);
        }*/
    }

    @Override
    public void onBeginningOfSpeech() {
    }

    //    We stop recognizer here to get a final result
    @Override
    public void onEndOfSpeech() {
       /* if (!recognizer.getSearchName().equals(KWS_SEARCH))
            switchSearch(KWS_SEARCH);*/
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 10000);//Time till which it will listen

        String caption = getResources().getString(captions.get(searchName));
        ((TextView) findViewById(R.id.voice_recog_text)).setText(caption);
    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))

                        // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                        //.setRawLogDir(assetsDir)

                        // Threshold to tune for keyphrase to balance between false alarms and misses
                .setKeywordThreshold(1e-45f)

                .setFloat("-vad_threshold", 3.0)

                        // Use context-independent phonetic search, context-dependent is too slow for mobile
                .setBoolean("-allphone_ci", true)

                .getRecognizer();
        recognizer.addListener(this);

//        They are added here for demonstration. You can leave just one.

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

        // Create grammar-based search for selection between demos
        File leeGrammar = new File(assetsDir, "lee.gram");
        recognizer.addGrammarSearch(LEE_SEARCH, leeGrammar);

        // Create grammar-based search for selection between demos
//        File menuGrammar = new File(assetsDir, "menu.gram");
//        recognizer.addGrammarSearch(MENU_SEARCH, menuGrammar);
//
//        // Create grammar-based search for digit recognition
//        File digitsGrammar = new File(assetsDir, "digits.gram");
//        recognizer.addGrammarSearch(DIGITS_SEARCH, digitsGrammar);
//
//        // Create language model search
//        File languageModel = new File(assetsDir, "weather.dmp");
//        recognizer.addNgramSearch(FORECAST_SEARCH, languageModel);
//
//        // Phonetic search
//        File phoneticModel = new File(assetsDir, "en-phone.dmp");
//        recognizer.addAllphoneSearch(PHONE_SEARCH, phoneticModel);
    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.voice_recog_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        Log.d("Timeout: ", "");
        switchSearch(KWS_SEARCH);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_work_out_dashboard, menu);
        return true;
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        recognizer.stop();
        recognizer.shutdown();
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
