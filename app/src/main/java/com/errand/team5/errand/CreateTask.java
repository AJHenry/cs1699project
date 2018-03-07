package com.errand.team5.errand;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CreateTask extends AppCompatActivity {

    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        b = (Button) findViewById(R.id.create);
        getSupportActionBar().setTitle("Create Task");

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRequest();
            }
        });
    }

        @Override
        public boolean onCreateOptionsMenu(Menu menu) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.completed, menu);
            return true;
        }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.create_task:
                setRequest();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRequest(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
