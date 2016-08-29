package com.trainer.shruty.personaltrainer;

import java.util.Date;

/**
 * Created by Shruty on 17-Jun-16.
 */
public class DBExercise {

    //private variables
    int _id;
    int _type;
    Double _durtaion;
    Double _avg_tut;
    int _repetition;
    Date _date;
    // Empty constructor
    public DBExercise(){

    }
    // constructor
    public DBExercise(int id, int type, Double duration, Double avg_tut, int repetition, Date date){
        this._id = id;
        this._type = type;
        this._durtaion = duration;
        this._avg_tut = avg_tut;
        this._repetition = repetition;
        this._date =date;
    }

    // constructor
    public DBExercise(int type, Double duration, Double avg_tut, int repetition, Date date){
        this._type = type;
        this._durtaion = duration;
        this._avg_tut = avg_tut;
        this._repetition = repetition;
        this._date =date;
    }
    // getting ID
    public int getID(){
        return this._id;
    }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting type
    public int getType(){
        return this._type;
    }

    // setting type
    public void setType(int type){
        this._type = type;
    }

    // getting duration
    public Double getDuration(){
        return this._durtaion;
    }

    // setting duration
    public void setDuration(Double duration){
        this._durtaion = duration;
    }


    // getting setAvg_tut
    public Double getAvg_tut(){
        return this._avg_tut;
    }

    // setting setAvg_tut
    public void setAvg_tut(Double avg_tut){
        this._avg_tut = avg_tut;
    }

    // getting repetition
    public int getRepetition(){
        return this._repetition;
    }

    // setting repetition
    public void setRepetition(int repetition){
        this._repetition = repetition;
    }

    // getting Date
    public Date getDate(){
        return this._date;
    }

    // setting Date
    public void setDate(Date date){
        this._date = date;
    }
}