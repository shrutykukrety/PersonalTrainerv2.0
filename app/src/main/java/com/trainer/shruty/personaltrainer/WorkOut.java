package com.trainer.shruty.personaltrainer;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;

import static edu.cmu.pocketsphinx.SpeechRecognizerSetup.defaultSetup;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class WorkOut extends AppCompatActivity  implements RecognitionListener, SensorEventListener, TextToSpeech.OnInitListener {

    /*Comments:
    * Step 1. Call your method to start measuring count
    * Step 2. Update elements exe_tut_up,  exe_tut_down & exe_reps
    * Step 3. Once Done Add the exercise into DB by db.addExercise(new DBExercise(1, 35.3, 10.0, 60, date))
    * where params are DBExercise(int type, Double duration, Double avg_tut, int repetition, Date date);
    * Step 4. Call method "stopTimer()"*/

    private TextView textTimer;
    private long startTime = 0L;
    private Handler myHandler = new Handler();
    long timeInMillies = 0L;
    long timeSwap = 0L;
    long finalTime = 0L;

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

    private static Boolean timerUp = false;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    private boolean goalAchieved = false;
    private int repGoal = 0;
    private static final int threshold = 5;

    //Text to Speech
    private TextToSpeech myTTS;
    private int MY_DATA_CHECK_CODE = 0;

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private float newXMax = 0;
    private float newYMax = 0;
    private float newZMax = 0;
    private float X = 0;
    private float Y = 0;
    private float Z = 0;
    private TextView currentX, currentY, currentZ, maxX, maxY, maxZ,variableValue,current_tut_up,current_tut_down;
    final float alpha = (float) 0.8;
    float[] linear_acceleration = new float[3];
    float[] gravity = new float[3];

    // time variable
    private ArrayList<Double> up_list   = new ArrayList<Double>();
    private ArrayList<Double> down_list = new ArrayList<Double>();
    private double average_up= 0.;
    private double sum_total_up = 0.;
    private double average_down= 0.;
    private double sum_total_down = 0.;
    private double total_average= 0.;
    private int counter = 0;  // Counting the Push ups or Pull Ups
    private double time_up =0 ; // Up Time counter
    private double time_down =0 ; // Down Time counter
    private boolean flagset=false;
    private double total_milliseconds;
    private double total_milliseconds_1;
    private long l1;
    private long l2;
    private long calculateDifference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_out);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        String exe = intent.getStringExtra("exercise");
        if(exe.equalsIgnoreCase("push")) {
            ((LinearLayout) findViewById(R.id.layout_exe)).setBackground(getResources().getDrawable(R.drawable.screen_shot_2015_10_26_at_145251_2));
            ((TextView) findViewById(R.id.exe_name)).setText("Push Ups");
        }
        else if(exe.equalsIgnoreCase("pull")) {
            ((LinearLayout) findViewById(R.id.layout_exe)).setBackground(getResources().getDrawable(R.drawable.pull_up));
            ((TextView) findViewById(R.id.exe_name)).setText("Pull Ups");
        }

        //update global timer variable
        textTimer = (TextView) findViewById(R.id.timer);

        //Update Cards
        updateCards();

        //Set Voice Recognizer
        captions = new HashMap<String, Integer>();
        captions.put(KWS_SEARCH, R.string.kws_caption);
        captions.put(LEE_SEARCH, R.string.lee_caption_2);

        ((TextView) findViewById(R.id.voice_recog_text))
                .setText("Preparing the recognizer");

        // so we execute it in async task

        new AsyncTask<Void, Void, Exception>() {
            @Override
            protected Exception doInBackground(Void... params) {
                try {
                    Assets assets = new Assets(WorkOut.this);
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

        //Check TTS event listner
        Intent checkTTSIntent = new Intent();
        checkTTSIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkTTSIntent, MY_DATA_CHECK_CODE);
    }

    private void updateCards(){
        // Best Performance
        repGoal = db.getBestReps();//update global variable
        //Update Reps
        TextView txtBst = (TextView)findViewById(R.id.reps_goal);
        txtBst.setText(String.valueOf(repGoal));

        txtBst = (TextView)findViewById(R.id.exe_best_reps);
        txtBst.setText(String.valueOf(repGoal));

        //Update Tut
        txtBst = (TextView)findViewById(R.id.tut_goal);
        txtBst.setText(String.valueOf(db.getBestTUT()));

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
            Log.d("shr: ", text);
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
        else if(text.toLowerCase().contains(STARTPHRASE)) {
            /*((TextView) findViewById(R.id.result_text)).setText("Start: " + text + " ups");
            switchSearch(KWS_SEARCH);*/
            recognizer.stop();
            Log.d("Start/Stop: ", text);
            MediaPlayer mediaPlayer = MediaPlayer.create(this, R.raw.europa);
            mediaPlayer.start();

            if (STARTPHRASE.contains(text)) {
                startTimer();
            }
            /*else {
                stopTimer();
            }*/

            //Set KWS_Search
            //switchSearch(KWS_SEARCH);
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
        getMenuInflater().inflate(R.menu.menu_work_out, menu);
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

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        recognizer.stop();
        recognizer.shutdown();
    }

    public void startTimer(View v) {
        if (!timerUp) {
            recognizer.stop();
            startTimer();
        }
        else
            stopTimer();
    }

    public void Initialize()
    {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    public void initializeViews() {
        variableValue = (TextView) findViewById(R.id.exe_reps);
        current_tut_up = (TextView) findViewById(R.id.exe_tut_up);
        current_tut_down = (TextView) findViewById(R.id.exe_tut_down);
    }
    private void startTimer(){
        startTime = SystemClock.uptimeMillis();
        myHandler.postDelayed(updateTimerMethod, 0);
        timerUp = true;
        ((ImageView) findViewById(R.id.btn_handle_timer)).setBackgroundResource(R.drawable.ic_pause_circle_outline_black_48dp);
        ((TextView) findViewById(R.id.voice_recog_text)).setText("Yes you can break your own record.");

        //Initializing sensors
        initializeViews();
         Initialize();

    }

    protected void onResume() {   // Listening events
        super.onResume();
        if(sensorManager != null)
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() { //Unregister the accelerometer
        super.onPause();
        if (sensorManager != null)
            sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    // PUSH UP EXERCISE

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        //displayCleanValues(); //Clean x,y,z accelerometer values
        //displayCurrentValues();// // Current  x,y,z accelerometer values
        //displayMaxValues(); //Max values


        // LOW PASS FILTER
        gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
        gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
        gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

        linear_acceleration[0] = event.values[0] - gravity[0];
        linear_acceleration[1] = event.values[1] - gravity[1];
        linear_acceleration[2] = event.values[2] - gravity[2];

        X = Math.abs(linear_acceleration[0]);
        Y = Math.abs(linear_acceleration[1]);
        Z = Math.abs(linear_acceleration[2]);

        if(Z>-1 && Z<1)
        {
            if(!flagset) {
                variableValue.setText(String.valueOf((int) counter / 2));
                if(counter != 0 && counter %2 == 0) {
                    if (!goalAchieved && counter / 2 > repGoal) {
                        speakWords("You Did It");
                        goalAchieved = true;
                    }
                    else if(!goalAchieved && counter / 2 > repGoal - threshold)
                        speakWords(String.valueOf( "Only "+ String.valueOf(repGoal - ((int) counter / 2) + 1) + " more to record"));
                    else
                        speakWords(String.valueOf((int) counter / 2));
                }
                flagset = true;

                // total_milliseconds = milliseconds + seconds * 1000 + minutes * 60 * 1000 + hour * 3600 * 1000;
                Date d1 = new Date();
                l1 = d1.getTime();
                counter++;
            }
        }
        else if(Z > 2 || Z < -2)
        {
            if(flagset)
            {
                Calendar c = Calendar.getInstance();
                double seconds;
                /*float rem_millisecs;
                float extra_seconds;
                float total_secs;*/

                Date d2 = new Date();
                l2 = d2.getTime();

                DecimalFormat decf = new DecimalFormat("#.00");

                calculateDifference = l2-l1;
                seconds = Double.valueOf(calculateDifference)/1000.0;

                if(counter%2 == 0) //Down Counter
                {
                    /*if(calculateDifference>1000)
                    {
                        seconds = calculateDifference/1000;
                        rem_millisecs = calculateDifference%1000;
                        extra_seconds = rem_millisecs/1000;
                        total_secs = seconds+extra_seconds;
                        if(total_secs > 0.0)
                        current_tut_down.setText(Float.toString(total_secs));
                    }
                    else {
                        extra_seconds = calculateDifference / 1000;
                        if (extra_seconds > 0.0)
                            current_tut_down.setText(Float.toString(extra_seconds));
                    }*/
                    current_tut_down.setText(decf.format(seconds));
                    //Convert to a double value sec.millisecs
                    down_list.add(seconds);
                }
                else  // Up counter
                {
                    /*if (calculateDifference > 1000) {
                        seconds = calculateDifference / 1000;
                        rem_millisecs = calculateDifference % 1000;
                        extra_seconds = rem_millisecs / 1000;
                        total_secs = seconds + extra_seconds;
                        if (total_secs > 0.0)
                            current_tut_up.setText(Float.toString(total_secs));
                    }
                    else {
                        extra_seconds = calculateDifference / 1000;
                        if (extra_seconds > 0.0)
                            current_tut_up.setText(Float.toString(extra_seconds));
                    }*/
                    current_tut_up.setText(decf.format(seconds));
                    up_list.add(seconds);
                }

                // db.addExercise(new DBExercise(1,'TOTAL DURATION', total_average, counter, d2));
                flagset = false;
            }
        }

        if(Y>2)
        {
            Date d3 = new Date();

            //Calculate Sum total UP
            for (int i=0;i<up_list.size()-1;i++)
            {
                sum_total_up=sum_total_up + up_list.get(i);
            }

            //Calculate Sum total DOWN
            for (int i=0;i<down_list.size()-1;i++)
            {
                sum_total_down=sum_total_down+ down_list.get(i);
            }

            //Calculate Average Up
            average_up = sum_total_up / 1000 * up_list.size();//Convert milli-seconds to seconds

            //Calculate Average Down
            average_down = sum_total_down / 1000 * down_list.size();//Convert milli-seconds to seconds

            total_average = average_up +  average_down;

            // db.addExercise(new DBExercise(1,'TOTAL DURATION', total_average, counter, d3));
            stopTimer();

            // maxX.setText(Double.toString(average_up));
            // maxY.setText(Double.toString(sum_total_up));
            // maxZ.setText(Long.toString(up_list.size()));
        }
    }

    @Override
    public void onInit(int initStatus) {
        if (initStatus == TextToSpeech.SUCCESS) {
            myTTS.setLanguage(Locale.US);
        }
        else if (initStatus == TextToSpeech.ERROR) {
            Toast.makeText(this, "Sorry! Text To Speech failed...", Toast.LENGTH_LONG).show();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MY_DATA_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                myTTS = new TextToSpeech(this, this);
            }
            else {
                Intent installTTSIntent = new Intent();
                installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installTTSIntent);
            }
        }
    }

    public void stopTimer(View v){
        stopTimer();
    }

    private void stopTimer(){
        timeSwap += timeInMillies;
        myHandler.removeCallbacks(updateTimerMethod);
        timerUp = false;

        ((ImageView) findViewById(R.id.btn_handle_timer)).setBackgroundResource(R.drawable.ic_play_circle_outline_black_48dp);
        ((TextView) findViewById(R.id.voice_recog_text)).setText("You did well....!!!");

        //Update the exercise in DB
        double totalDuration = Double.valueOf(textTimer.getText().toString().replace(":","."));//(timeSwap / 1000) / 60;
        db.addExercise(new DBExercise(1, totalDuration, total_average/2. ,((int)counter/2 - 1), new Date()));
        /*try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        speakWords("Exercise Finished");
        Toast.makeText(this, "Your Exercise has been updated.", Toast.LENGTH_LONG).show();
        Intent intent = new Intent(this, Home.class);
        startActivity(intent);
    }

    private void speakWords(String speech) {
        //implement TTS here
        myTTS.speak(speech, TextToSpeech.QUEUE_FLUSH, null);
    }



    private Runnable updateTimerMethod = new Runnable() {

        public void run() {
            timeInMillies = SystemClock.uptimeMillis() - startTime;
            finalTime = timeSwap + timeInMillies;

            int seconds = (int) (finalTime / 1000);
            int minutes = seconds / 60;
            seconds = seconds % 60;
            int milliseconds = (int) (finalTime % 1000);
            textTimer.setText(String.format("%02d", minutes) + ":"
                    + String.format("%02d", seconds));
            myHandler.postDelayed(this, 0);
        }

    };
}
