package com.trainer.shruty.personaltrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Shruty on 16-Jun-16.
 */
public class DBHandlerUser extends SQLiteOpenHelper {
    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;

    // Database Name
    private static final String DATABASE_NAME = "personaltrainer";

    // user table name
    private static final String TABLE_DBUSER = "user";

    // user Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_UNAME = "uname";
    private static final String KEY_FNAME = "fname";
    private static final String KEY_LNAME = "lname";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_AGE = "age";
    private static final String KEY_HEIGHT = "height";
    //private static final String KEY_Date = "date";//"YYYY-MM-DD HH:MM:SS.SSS"

    DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public DBHandlerUser(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_DBUSER_TABLE = "CREATE TABLE " + TABLE_DBUSER + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_UNAME + " TEXT,"
                + KEY_FNAME + " TEXT,"
                + KEY_LNAME + " TEXT,"
                + KEY_PASSWORD + " TEXT,"
                + KEY_AGE + " INTEGER,"
                + KEY_HEIGHT + " INTEGER)";//"YYYY-MM-DD HH:MM:SS.SSS"
        db.execSQL(CREATE_DBUSER_TABLE);
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DBUSER);

        // Create tables again
        onCreate(db);
    }

    /**
     * All CRUD(Create, Read, Update, Delete) Operations
     */

    // Adding new user
    void addUser(DBUser user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        if(user.getID() > 0)
            values.put(KEY_ID, user.getID()); // DBUser ID
        values.put(KEY_UNAME, user.getUserName()); // DBUser UserName
        values.put(KEY_FNAME, user.getFirstName()); // DBUser FirstName
        values.put(KEY_LNAME, user.getLastName()); // DBUser LastName
        values.put(KEY_PASSWORD, user.getPassword()); // DBUser Password
        values.put(KEY_AGE,user.getAge()); // DBUser Age
        values.put(KEY_HEIGHT, user.getAge()); // DBUser Height

        // Inserting Row
        db.insert(TABLE_DBUSER, null, values);
        db.close(); // Closing database connection
    }

    // Getting single user
    DBUser getDBUser(String uName, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_DBUSER,
                new String[]{KEY_ID, KEY_UNAME, KEY_FNAME, KEY_LNAME, KEY_PASSWORD, KEY_AGE, KEY_HEIGHT},
                KEY_UNAME + "=? and " + KEY_PASSWORD + "=?",
                new String[]{uName, password},
                null, null, null, null);
        if (cursor != null)
            cursor.moveToFirst();
        if(cursor.getCount() > 0){
            DBUser user = new DBUser(Integer.parseInt(cursor.getString(0)),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4),
                    Integer.parseInt(cursor.getString(5)),
                    Integer.parseInt(cursor.getString(6)));
            // return user
            return user;
        }
        else
            return null;
    }


    // Deleting single user
    public void deleteDBUser(DBUser user) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DBUSER, KEY_ID + " = ?",
                new String[]{String.valueOf(user.getID())});
        db.close();
    }


    // Getting users Count
    public int getDBUsersCount() {
        String countQuery = "SELECT  * FROM " + TABLE_DBUSER;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        cursor.close();

        // return count
        return cursor.getCount();
    }

/*
    // Getting All DBUsers
    public List<DBUser> getAllDBUsers() {
        List<DBUser> userList = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_DBUSER;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor.moveToFirst()) {
            do {
                Date date = new Date();
                try {
                    date= df.parse(cursor.getString(5));
                }
                catch (ParseException e) {
                    e.printStackTrace();
                }
                DBUser user = new DBUser();
                user.setID(Integer.parseInt(cursor.getString(0)));
                user.setType(Integer.parseInt(cursor.getString(1)));
                user.setDuration(Double.parseDouble(cursor.getString(2)));
                user.setAvg_tut(Double.parseDouble(cursor.getString(3)));
                user.setRepetition(Integer.parseInt(cursor.getString(4)));
                user.setDate(date);
                // Adding user to list
                userList.add(user);
            } while (cursor.moveToNext());
        }

        // return user list
        return userList;
    }

    // Updating single user
    public int updateDBUser(DBUser user) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_Type, user.getType()); // DBUser Type
        values.put(KEY_Duration, user.getDuration()); // DBUser Duration
        values.put(KEY_Avg_TUT, user.getAvg_tut()); // DBUser Avg TUT
        values.put(KEY_Repetition, user.getRepetition()); // DBUser Repetition
        values.put(KEY_Date, df.format(user.getDate())); // DBUser Date
        // updating row,
        return db.update(TABLE_DBUSER, values, KEY_ID + " = ?",
                new String[]{TABLE_DBUSER.valueOf(user.getID())});
    }*/
}
