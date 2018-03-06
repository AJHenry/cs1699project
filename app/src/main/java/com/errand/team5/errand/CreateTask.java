package com.errand.team5.errand;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class CreateTask extends AppCompatActivity {

    Button b;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        b = (Button) findViewById(R.id.create);

        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setRequest();
            }
        });
    }


    private void setRequest(){
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }
}
