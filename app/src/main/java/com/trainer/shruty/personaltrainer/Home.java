package com.trainer.shruty.personaltrainer;

import static android.widget.Toast.makeText;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import lecho.lib.hellocharts.gesture.ContainerScrollType;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class Home extends AppCompatActivity {

    DBHandlerExercise db = new DBHandlerExercise(this);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

       /* List<DBExercise> ls2 = db.getAllExercises();
        for (DBExercise exe : ls2
             ) {
            db.deleteExercise(exe);
        }*/

        /**
         * CRUD Operations
         * */
        // Inserting Contacts
       /* Log.d("Insert: ", "Inserting ..");
        if(db.getAllExercises().size() == 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date date = sdf.parse("11/11/2015");
                db.addExercise(new DBExercise(1, 3.53, 10.0, 5, date));
                date = sdf.parse("23/11/2015");
                db.addExercise(new DBExercise(1, 4.53, 10.0, 8, date));
                date = sdf.parse("22/11/2015");
                db.addExercise(new DBExercise(1, 5.53, 10.0, 4, date));
                date = sdf.parse("21/11/2015");
                db.addExercise(new DBExercise(1, 4.53, 10.0, 6, date));
                date = sdf.parse("20/11/2015");
                db.addExercise(new DBExercise(1, 6.53, 10.0, 8, date));
                date = sdf.parse("19/11/2015");
                db.addExercise(new DBExercise(1, 4.53, 12.0, 4, date));
                date = sdf.parse("18/11/2015");
                db.addExercise(new DBExercise(1, 5.53, 10.0, 7, date));
                date = sdf.parse("17/11/2015");
                db.addExercise(new DBExercise(1, 4.53, 10.0, 6, date));
                date = sdf.parse("16/11/2015");
                db.addExercise(new DBExercise(1, 5.53, 10.0, 5, date));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }*/
        //Plot Graph
        plotGraph();

        //update Cards
        updateCards();
        /*List<DBExercise> ls = db.getLastExercises();
        List<DBExercise> ls2 = db.getAllExercises();*/
        /*for (DBExercise exe : ls2
             ) {
            db.deleteExercise(exe);
        }*/



    }

    private void plotGraph(){

        //FETCh plot data
        HashMap<Date,Integer> plotData = getPlotData();
        Map<Date,Integer> plotSort = new TreeMap<Date, Integer>(plotData);
        LineChartView chart = (LineChartView)findViewById(R.id.chart);
        chart.setPadding(2,400,2,2);
        chart.setInteractive(true);
        chart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
        boolean isEnabled = true;
        chart.setContainerScrollEnabled(isEnabled, ContainerScrollType.HORIZONTAL);

        List<PointValue> values = new ArrayList<PointValue>();
        Date currentDate = new Date();
        for (Date date:plotSort.keySet()
             ) {
            values.add(new PointValue( (6 - ((int) ((currentDate.getTime() - date.getTime()) / (1000 * 60 * 60 * 24)))), plotData.get(date)));
        }

        //In most cased you can call data model methods in builder-pattern-like manner.
        Line line = new Line(values).setColor(Color.parseColor("#f5a623")).setCubic(true);
        line.setHasPoints(true);
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);
        //values of the axeY
        List<AxisValue> axisValuesForX = new ArrayList<AxisValue>();
        List<AxisValue> axisValuesForY = new ArrayList<AxisValue>();
        for (int i = 0; i < 7; i ++){
            axisValuesForX.add(new AxisValue(i));
        }
        /*for (int i = 10; i < 100; i += 10){
            axisValuesForY.add(new AxisValue(i));
        }*/

        Axis axisX = new Axis(axisValuesForX);
        Axis axisY = new Axis().setHasLines(true).setTextColor(Color.WHITE).setTextSize(14);

        axisX.setName("Repetition in Last 7 Days").setTextColor(Color.WHITE).setTextSize(14);
        //axisY.setName("Repetition");
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);

        Viewport v = new Viewport(chart.getMaximumViewport());
        v.bottom = 0;
        v.top = 100;
        v.left = 0;
        v.right = 7;
        chart.setMaximumViewport(v);
        chart.setCurrentViewport(v);
        chart.setLineChartData(data);
    }

    private HashMap<Date,Integer> getPlotData(){
        //Fetch last 7 day exercise
        List<DBExercise> ls = db.getLastExercises();

        //Create a group of exercise on same date
        HashMap<Date,List<DBExercise>> exeMap = new HashMap<>();
        for (DBExercise exe: ls
             ) {
            if(exeMap.containsKey(exe.getDate())) {
                List<DBExercise> temp = exeMap.get(exe.getDate());
                temp.add(exe);
                exeMap.put(exe.getDate(), temp);
            }
            else{
                List<DBExercise> temp = new ArrayList<>();
                temp.add(exe);
                exeMap.put(exe.getDate(), temp);
            }
        }

        //Create Plot Data based on the Exercise type for each date
        HashMap<Date,Integer> plotData = new HashMap<>();
        for (Date date: exeMap.keySet()
             ) {
            int avgRepetition = 0;
            int totRepetition = 0;
            for (DBExercise exe: exeMap.get(date)
                 ) {
                totRepetition += exe.getRepetition();
            }
            avgRepetition = totRepetition / exeMap.get(date).size();
            plotData.put(date,avgRepetition);
        }
        return plotData;
        //return sortHashMap(plotData);
    }

    private HashMap<Date,Integer> sortHashMap(HashMap<Date,Integer> hmap){
        HashMap<Date,Integer> sorted = new HashMap<>();
        Map<Date,Integer> map = new TreeMap<Date,Integer>(hmap);
        System.out.println("After Sorting:");
        Set set2 = map.entrySet();
        Iterator iterator2 = set2.iterator();
        while(iterator2.hasNext()) {
            Map.Entry me2 = (Map.Entry)iterator2.next();
            sorted.put((Date) me2.getKey(), (Integer) me2.getValue());
        }
        return  sorted;
    }

    private void updateCards(){
        DBExercise lastExes = db.getLastExercise();
        if(lastExes != null)
        {
            //Update Reps
            TextView txt = (TextView)findViewById(R.id.push_last_reps);
            txt.setText(String.valueOf(lastExes.getRepetition()));
            //Update TUT
            txt = (TextView)findViewById(R.id.push_last_tut);
            txt.setText(String.valueOf(lastExes.getAvg_tut()));
            //Update Duration
            txt = (TextView)findViewById(R.id.push_last_dur);
            txt.setText(String.valueOf(lastExes.getDuration()));
        }
        //Now Best Performance
        //Update Reps
        TextView txtBst = (TextView)findViewById(R.id.push_best_reps);
        txtBst.setText(String.valueOf(db.getBestReps()));

        //Update Tut
        txtBst = (TextView)findViewById(R.id.push_best_tut);
        txtBst.setText(String.valueOf(db.getBestTUT()));

        //Update Duration
        txtBst = (TextView)findViewById(R.id.push_best_duration);
        txtBst.setText(String.valueOf(db.getBestDuration()));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
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

    public void shareLastPushUp(View view){
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        StringBuilder sb = new StringBuilder();
        DBExercise lastExes = db.getLastExercise();
        if(lastExes != null)
        {
            sb.append("Last Workout Statistics : \n");
            sb.append("Repetitions: " + String.valueOf(lastExes.getRepetition()) + "\n");
            sb.append("Average TUT: " + String.valueOf(lastExes.getAvg_tut())+ "\n");
            sb.append("Duration: " + String.valueOf(lastExes.getDuration()));
        }
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            this.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText("Whatsapp have not been installed.");
        }
    }

    public void shareBestPushUp(View view) {
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setType("text/plain");
        whatsappIntent.setPackage("com.whatsapp");
        StringBuilder sb = new StringBuilder();
        sb.append("Best Workout Statistics :) "+ "\n");
        sb.append("Repetitions: " + String.valueOf(db.getBestReps())+ "\n");
        sb.append("Average TUT: " + String.valueOf(db.getBestTUT())+ "\n");
        sb.append("Duration: " + String.valueOf(db.getBestDuration()));
        whatsappIntent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        try {
            this.startActivity(whatsappIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            ex.printStackTrace();
            makeText(getApplicationContext(), ex.toString(), Toast.LENGTH_SHORT).show();
            //Toast.makeText("Whatsapp have not been installed.");
        }
    }

    public void sharePullUp(View view){
        makeText(getApplicationContext(), "Sorry No Records for Pull Up to share", Toast.LENGTH_SHORT).show();
    }

    public void nextPage(View view) {
            //Start the next activity
            Intent intent = new Intent(this, WorkOutDashboard.class);
            startActivity(intent);
    }
}
