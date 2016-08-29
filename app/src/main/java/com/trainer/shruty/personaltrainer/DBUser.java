package com.trainer.shruty.personaltrainer;

import java.util.Date;

/**
 * Created by Shruty on 16-Jun-16.
 */
public class DBUser {
    //private variables
    int _id;
    String _uName;
    String _fName;
    String _lName;
    String _password;
    int _age;
    int _height;

    public DBUser(String uName, String fName, String lName,String password, int age, int height)
    {
        this._uName = uName;
        this._fName = fName;
        this._lName = lName;
        this._password = password;
        this._age = age;
        this._height = height;
    }

    public DBUser(int id,String uName, String fName, String lName,String password, int age, int height)
    {
        this._id = id;
        this._uName = uName;
        this._fName = fName;
        this._lName = lName;
        this._password = password;
        this._age = age;
        this._height = height;
    }

    // getting ID
    public int getID(){ return this._id; }

    // setting id
    public void setID(int id){
        this._id = id;
    }

    // getting UserName
    public String getUserName(){ return this._uName; }

    // setting UserName
    public void setUserName(String uname){
        this._uName = uname;
    }

    // getting FirstName
    public String getFirstName(){ return this._fName; }

    // setting FirstName
    public void setFirstName(String fName){
        this._fName = fName;
    }

    // getting LastName
    public String getLastName(){ return this._lName; }

    // setting LastName
    public void setLastName(String lname){
        this._lName = lname;
    }

    // getting Password
    public String getPassword(){ return this._password; }

    // setting Password
    public void setPassword(String password){
        this._password= password;
    }

    // getting Age
    public int getAge(){ return this._age; }

    // setting Age
    public void setAge(int age){
        this._age = age;
    }

    // getting Height
    public int getHeight(){ return this._height; }

    // setting Height
    public void setHeight(int height){
        this._height = height;
    }
}
