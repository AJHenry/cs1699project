package com.errand.team5.errand;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

/**
 * Added by Austin M, 4/15/18
 *
 * Displays updated Task information, asks user to confirm
 * If OK, write to DB and drop back to Main
 * If no, populate CreateTask fields and drop to CreateTask activity
 */

public class UpdateConfirm extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        Toast.makeText(this, "UPDATE CONFIRM SCREEN - NOT IMPLEMENTED YET", Toast.LENGTH_LONG).show();
        finish();
    }
}
