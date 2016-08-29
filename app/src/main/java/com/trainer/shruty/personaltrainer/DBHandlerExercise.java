package com.trainer.shruty.personaltrainer;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class DBHandlerExercise extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "personaltrainerExe";

    // exercise table name
    private static final String TABLE_EXERCISE = "exercise";

    // exercise Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_Type = "type";
    private static final String KEY_Duration = "duration";
    private static final String KEY_Avg_TUT = "avg_tut";
    private static final String KEY_Repetition = "repetition";
    private static final String KEY_Date = "date";//"YYYY-MM-DD HH:MM:SS.SSS"

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DBHandlerExercise(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_EXERCISE_TABLE = "CREATE TABLE " + TABLE_EXERCISE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_Type + " INTEGER,"
                + KEY_Duration + " REAL,"
                + KEY_Avg_TUT + " REAL,"
                + KEY_Repetition + " INTEGER,"
                + KEY_Date + " TEXT" + ")";//"YYYY-MM-DD HH:MM:SS.SSS"
        db.execSQL(CREATE_EXERCISE_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXERCISE);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new exercise
    void addExercise(DBExercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if(exercise.getID() > 0)
            values.put(KEY_ID, exercise.getID()); // DBExercise ID
        values.put(KEY_Type, exercise.getType()); // DBExercise Type
        values.put(KEY_Duration, exercise.getDuration()); // DBExercise Duration
        values.put(KEY_Avg_TUT, exercise.getAvg_tut()); // DBExercise Avg TUT
        values.put(KEY_Repetition, exercise.getRepetition()); // DBExercise Repetition
        values.put(KEY_Date, df.format(exercise.getDate())); // DBExercise Date

        // Inserting Row
        db.insert(TABLE_EXERCISE, null, values);
        db.close(); // Closing database connection
    }

    // Getting single exercise
    DBExercise getExercise(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_EXERCISE,
                new String[] { KEY_ID,KEY_Type, KEY_Duration, KEY_Avg_TUT, KEY_Repetition, KEY_Date  },
                KEY_ID + "=?",
                new String[] { String.valueOf(id) },
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        Date date = new Date();
        try {
            date= df.parse(cursor.getString(5));
        }
        catch (ParseException e) {
            e.printStackTrace();
        }

        DBExercise exercise = new DBExercise(Integer.parseInt(cursor.getString(0)),
                Integer.parseInt(cursor.getString(1)),
                Double.parseDouble(cursor.getString(2)),
                Double.parseDouble(cursor.getString(3)),
                Integer.parseInt(cursor.getString(4)),
                date);
        // return exercise
        return exercise;
    }

    //Overload
    public List<DBExercise> getLastExercises(){
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -7);
        Date dateBefore7Days = cal.getTime();

        String selectQuery = "SELECT  * FROM " + TABLE_EXERCISE;

        List<DBExercise> exerciseList = new ArrayList<>();
        // Select All Query

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Date date = new Date();
                    try {
                        date= df.parse(cursor.getString(5));

                        if(date.after(dateBefore7Days) && date.before(new Date())) {
                            DBExercise exercise = new DBExercise();
                            exercise.setID(Integer.parseInt(cursor.getString(0)));
                            exercise.setType(Integer.parseInt(cursor.getString(1)));
                            exercise.setDuration(Double.parseDouble(cursor.getString(2)));
                            exercise.setAvg_tut(Double.parseDouble(cursor.getString(3)));
                            exercise.setRepetition(Integer.parseInt(cursor.getString(4)));
                            exercise.setDate(date);
                            // Adding exercise to list
                            exerciseList.add(exercise);
                        }
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // return exercise list
        return exerciseList;
    }

    //Overload
    public List<DBExercise> getAllExercises(){
        String selectQuery = "SELECT  * FROM " + TABLE_EXERCISE;
        return getAllExercises(selectQuery);
    }

    public int getBestReps() {
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISE + " order by " + KEY_Repetition + " desc  LIMIT 1";
        List<DBExercise> ls = getAllExercises(selectQuery);
        int reps = 0;
        if (ls.size() > 0)
            reps = ls.get(0).getRepetition();
        return reps;
    }
    public double getBestTUT() {
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISE + " order by " + KEY_Avg_TUT + " desc  LIMIT 1";
        List<DBExercise> ls = getAllExercises(selectQuery);
        double tut = 0;
        if (ls.size() > 0)
            tut = ls.get(0).getAvg_tut();
        return tut;
    }
    public double getBestDuration() {
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISE + " order by " + KEY_Duration + " desc  LIMIT 1";
        List<DBExercise> ls = getAllExercises(selectQuery);
        double tut = 0;
        if (ls.size() > 0)
            tut = ls.get(0).getDuration();
        return tut;
    }
    //Overload
    public DBExercise getLastExercise(){
        String selectQuery = "SELECT * FROM " + TABLE_EXERCISE + " order by "+ KEY_Date +  " desc  LIMIT 1";
        List<DBExercise> ls = getAllExercises(selectQuery);
        if (ls.size() > 0)
            return ls.get(0);
        else
            return null;
    }



    // Getting All Exercises
    public List<DBExercise> getAllExercises(String selectQuery) {
        List<DBExercise> exerciseList = new ArrayList<>();
        // Select All Query

        try {
            SQLiteDatabase db = this.getWritableDatabase();
            Cursor cursor = db.rawQuery(selectQuery, null);

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    Date date = new Date();
                    try {
                        date = df.parse(cursor.getString(5));

                        DBExercise exercise = new DBExercise();
                        exercise.setID(Integer.parseInt(cursor.getString(0)));
                        exercise.setType(Integer.parseInt(cursor.getString(1)));
                        exercise.setDuration(Double.parseDouble(cursor.getString(2)));
                        exercise.setAvg_tut(Double.parseDouble(cursor.getString(3)));
                        exercise.setRepetition(Integer.parseInt(cursor.getString(4)));
                        exercise.setDate(date);
                        // Adding exercise to list
                        exerciseList.add(exercise);
                    }
                    catch (ParseException e) {
                        e.printStackTrace();
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        // return exercise list
        return exerciseList;
    }

    // Updating single exercise
    public int updateExercise(DBExercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_Type, exercise.getType()); // DBExercise Type
        values.put(KEY_Duration, exercise.getDuration()); // DBExercise Duration
        values.put(KEY_Avg_TUT, exercise.getAvg_tut()); // DBExercise Avg TUT
        values.put(KEY_Repetition, exercise.getRepetition()); // DBExercise Repetition
        values.put(KEY_Date, df.format(exercise.getDate())); // DBExercise Date
        // updating row,
        return db.update(TABLE_EXERCISE, values, KEY_ID + " = ?",
                new String[] { TABLE_EXERCISE.valueOf(exercise.getID()) });
    }

    // Deleting single exercise
    public void deleteExercise(DBExercise exercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXERCISE, KEY_ID + " = ?",
                new String[] { String.valueOf(exercise.getID()) });
        db.close();
    }


    // Getting exercises Count
    public int getExercisesCount() {
        String countQuery = "SELECT  * FROM " + TABLE_EXERCISE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

}
