package com.trainer.shruty.personaltrainer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class Register extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register, menu);
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

    public void register(View view) {
        String username = ((EditText)findViewById(R.id.txt_uname)).getText().toString();
        String firstName = ((EditText)findViewById(R.id.txt_fname)).getText().toString();
        String lastName = ((EditText)findViewById(R.id.txt_lname)).getText().toString();
        String password = ((EditText)findViewById(R.id.txt_pswrd)).getText().toString();
        String cnfPassword = ((EditText)findViewById(R.id.txt_cnf_pswrd)).getText().toString();
        String ageGrp = ((Spinner)findViewById(R.id.sp_age)).getSelectedItem().toString();
        String height = ((EditText)findViewById(R.id.txt_height)).getText().toString();
        if(username.isEmpty() || firstName.isEmpty() || lastName.isEmpty() || password.isEmpty() || cnfPassword.isEmpty() || ageGrp.isEmpty() || height.isEmpty())
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Please enter all fields");
            return;
        }
        if(password.length() < 4)
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Password should be of length 4");
            return;
        }
        if(!password.equals(cnfPassword))
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Password and Confirm Password does not match");
            return;
        }
        if(ageGrp.equalsIgnoreCase("Age Group"))
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Please select an age group");
            return;
        }
        int age = 0;
        switch (ageGrp)
        {
            case "10-15":
                age = 12;
                break;
            case "16-24":
                age = 18;
                break;
            case "25-40":
                age = 27;
                break;
            case "40+":
                age = 42;
                break;
            default:
                TextView error = (TextView)findViewById(R.id.txt_error);
                error.setText("Please select an age group");
                return;
        }
        if(!TextUtils.isDigitsOnly(height))
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Height must be a number");
            return;
        }
        //Create a user and add in database
        DBHandlerUser db = new DBHandlerUser(this);
        DBUser user = new DBUser(username,firstName,lastName,password,age, Integer.parseInt(height));
        //add into DB
        try{
            db.addUser(user);
            //Start the next activity
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Unable to create User");
        }
    }
}
