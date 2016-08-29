package com.trainer.shruty.personaltrainer;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

/**
 * Created by Shruty on 17-Jun-16.
 */

public class Login extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
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

    public void login(View view) {
        EditText username = (EditText)findViewById(R.id.txt_uname);
        EditText password = (EditText)findViewById(R.id.txt_pswrd);
        if(username.getText().toString().isEmpty() || password.getText().toString().isEmpty())
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Please Enter Credentials");
            return;
        }
        DBHandlerUser db = new DBHandlerUser(this);
        if(db.getDBUser(username.getText().toString(), password.getText().toString()) != null) {
            //Start the next activity
            Intent intent = new Intent(this, Home.class);
            startActivity(intent);
        }
        else
        {
            TextView error = (TextView)findViewById(R.id.txt_error);
            error.setText("Invalid Credentials");
        }
    }
}
