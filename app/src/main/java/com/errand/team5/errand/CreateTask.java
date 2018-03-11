package com.errand.team5.errand;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class CreateTask extends AppCompatActivity implements View.OnClickListener {

    private Button[] amount = new Button[3];
    private Button amount_unfocus;
    private int[] amount_id = {R.id.c1, R.id.c2, R.id.c3};

    private Button[] type = new Button[2];
    private Button type_unfocus;
    private int[] type_id = {R.id.hours, R.id.mins};



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        getSupportActionBar().setTitle("Create Task");

        for(int i = 0; i < amount.length; i++){
            amount[i] = (Button) findViewById(amount_id[i]);
            amount[i].setBackgroundColor(Color.rgb(207, 207, 207));
            amount[i].setOnClickListener(this);
        }

        for(int i = 0; i < type.length; i++){
            type[i] = (Button) findViewById(type_id[i]);
            type[i].setBackgroundColor(Color.rgb(207, 207, 207));
            type[i].setOnClickListener(this);
        }

        type_unfocus = type[0];
        amount_unfocus = amount[0];

        setTypeFocus(amount_unfocus, amount[0]);
        setTypeFocus(type_unfocus, type[0]);
    }

    @Override
    public void onClick(View v) {
        //setTypeForcus(type_unfocus, (Button) findViewById(v.getId()));
        //Or use switch
        switch (v.getId()) {
            case R.id.c1:
                setAmountFocus(amount_unfocus, amount[0]);
                break;

            case R.id.c2:
                setAmountFocus(amount_unfocus, amount[1]);
                break;

            case R.id.c3:
                setAmountFocus(amount_unfocus, amount[2]);
                break;

            case R.id.hours:
                setTypeFocus(type_unfocus, type[0]);
                break;

            case R.id.mins:
                setTypeFocus(type_unfocus, type[1]);
                break;
        }
    }

    private void setTypeFocus(Button btn_unfocus, Button btn_focus){
        btn_unfocus.setTextColor(Color.rgb(49, 50, 51));
        btn_unfocus.setBackgroundColor(Color.rgb(207, 207, 207));
        btn_focus.setTextColor(Color.rgb(255, 255, 255));
        btn_focus.setBackgroundColor(Color.rgb(3, 106, 150));
        this.type_unfocus = btn_focus;
    }

    private void setAmountFocus(Button btn_unfocus, Button btn_focus){
        btn_unfocus.setTextColor(Color.rgb(49, 50, 51));
        btn_unfocus.setBackgroundColor(Color.rgb(207, 207, 207));
        btn_focus.setTextColor(Color.rgb(255, 255, 255));
        btn_focus.setBackgroundColor(Color.rgb(3, 106, 150));
        this.amount_unfocus = btn_focus;
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
