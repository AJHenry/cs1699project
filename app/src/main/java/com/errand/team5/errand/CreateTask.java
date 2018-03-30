package com.errand.team5.errand;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

public class CreateTask extends AppCompatActivity implements View.OnClickListener {

    private Button[] amount = new Button[3];
    private Button amount_unfocus;
    private int[] amount_id = {R.id.c1, R.id.c2, R.id.c3};

    private Button[] type = new Button[2];
    private Button type_unfocus;
    private int[] type_id = {R.id.hours, R.id.mins};

    //Places from PlacePicker
    private Place dropOffPlace;
    private Place errandPlace;

    //Used for Log
    private final String TAG = "CreateTask Class";

    private Location loc;

    //Activity results from place picker
    private final int DROP_OFF_PLACE_PICKER = 1;
    private final int ERRAND_PLACE_PICKER = 2;

    //Components
    private Button dropOffLocation;
    private Button errandLocation;
    private CurrencyEditText costInput;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText specialInstructionsInput;

    //TODO Check for user login


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        getSupportActionBar().setTitle("Create Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get location passed from MainActivity
        Bundle extras = getIntent().getExtras();

        //Get the data if it is not nll
        if (extras != null) {
            Log.d(TAG, "Data sent from intent");
            double lat = extras.getDouble("LAT");
            double lng = extras.getDouble("LONG");
            loc = new Location("");
            loc.setLatitude(lat);
            loc.setLongitude(lng);
            Log.d(TAG, "Long: " + loc.getLongitude());
            Log.d(TAG, "Lat: " + loc.getLatitude());

            //Call the picker for the current location
            dropOffPicker();
        }

        //Components
        dropOffLocation = (Button) findViewById(R.id.drop_off_button);
        errandLocation = (Button) findViewById(R.id.errand_location_button);
        costInput = (CurrencyEditText) findViewById(R.id.cost);
        titleInput = (EditText) findViewById(R.id.title);
        descriptionInput = (EditText) findViewById(R.id.description);
        specialInstructionsInput = (EditText) findViewById(R.id.special_instructions);

        errandLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                errandPicker();
            }
        });

        dropOffLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dropOffPicker();
            }
        });

        for (int i = 0; i < amount.length; i++) {
            amount[i] = (Button) findViewById(amount_id[i]);
            amount[i].setBackgroundColor(Color.rgb(207, 207, 207));
            amount[i].setOnClickListener(this);
        }

        for (int i = 0; i < type.length; i++) {
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
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

    private void setTypeFocus(Button btn_unfocus, Button btn_focus) {
        btn_unfocus.setTextColor(Color.rgb(49, 50, 51));
        btn_unfocus.setBackgroundColor(Color.rgb(207, 207, 207));
        btn_focus.setTextColor(Color.rgb(255, 255, 255));
        btn_focus.setBackgroundColor(Color.rgb(3, 106, 150));
        this.type_unfocus = btn_focus;
    }

    private void setAmountFocus(Button btn_unfocus, Button btn_focus) {
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
                //TODO Check to make sure they filled out the required fields
                createTask();
                //setRequest();
                return true;
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setRequest() {
        Intent returnIntent = new Intent();
        setResult(Activity.RESULT_OK, returnIntent);
        finish();
    }

    private void dropOffPicker() {

        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            //If location is not null, put a pin near there
            //WARNING, Google Place picker has a bug where it does not use setLatLngBounds correctly
            if (loc != null) {
                Log.d(TAG, "Location available");
                //Create new latlon
                LatLng mLatLng = new LatLng(loc.getLongitude(), loc.getLatitude());
                //Radius in meters
                double radius = 50;
                //Create a new bound and pass it
                LatLngBounds mLatLngBounds = toBounds(mLatLng, radius);
                //builder.setLatLngBounds(mLatLngBounds);
            }
            //Start the activity
            startActivityForResult(builder.build(this), DROP_OFF_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
        } catch (GooglePlayServicesRepairableException e) {
        }
    }


    private void errandPicker() {

        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            //If location is not null, put a pin near there
            if (loc != null) {
                //Create new latlon
                LatLng mLatLng = new LatLng(loc.getLongitude(), loc.getLatitude());
                //Radius in meters
                double radius = 50;
                //Create a new bound and pass it
                LatLngBounds mLatLngBounds = toBounds(mLatLng, radius);
                builder.setLatLngBounds(mLatLngBounds);
            }
            //Start the activity
            startActivityForResult(builder.build(this), ERRAND_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
        } catch (GooglePlayServicesRepairableException e) {
        }
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DROP_OFF_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                dropOffPlace = PlacePicker.getPlace(data, this);
                //String toastMsg = String.format("Place: %s", dropOffPlace.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                //Set the text of the drop off location
                dropOffLocation.setText(dropOffPlace.getAddress());
            }
        }

        if (requestCode == ERRAND_PLACE_PICKER) {
            if (resultCode == RESULT_OK) {
                errandPlace = PlacePicker.getPlace(data, this);
                //String toastMsg = String.format("Place: %s", dropOffPlace.getName());
                //Toast.makeText(this, toastMsg, Toast.LENGTH_LONG).show();
                //Set the text of the drop off location
                errandLocation.setText(errandPlace.getAddress());
            }
        }
    }

    public LatLngBounds toBounds(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }


    public void createTask(){
        //TODO Verify fields aren't blank
        boolean dataSatisfied = true;

        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        double baseprice = costInput.getRawValue();
        String dropOffAddress = dropOffPlace.getAddress().toString();
        String errandAddress = errandPlace.getAddress().toString();
        String specialInstructions = specialInstructionsInput.getText().toString();

        if(title == null || title.isEmpty()){
            //Display error underneath
            dataSatisfied = false;
        }
        if(description == null || description.isEmpty()){
            //Display error underneath

            dataSatisfied = false;
        }
        if(baseprice <= 0.0){
            //Display error underneath

            dataSatisfied = false;
        }
        if(dropOffAddress == null || dropOffAddress.isEmpty()){
            //Display error underneath

            dataSatisfied = false;
        }
        if(errandAddress == null || errandAddress.isEmpty()){
            //Display error underneath

            dataSatisfied = false;
        }

        //Data is valid
        if(dataSatisfied) {
            showSummary(title, description, baseprice, dropOffAddress, specialInstructions);
        }
    }


    public void showSummary(String title, String description, double basePrice, String dropOffAddress, String specialInstructions){
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.summary);

        TextView summaryTitle = (TextView) dialog.findViewById(R.id.summary_title);
        summaryTitle.setText(title);
        TextView summaryDescription = (TextView) dialog.findViewById(R.id.summary_description);
        summaryDescription.setText(description);
        //TextView summaryBasePrice = (TextView) dialog.findViewById(R.id.);
        //TODO fix hacky concatenate
        //summaryBasePrice.setText(""+basePrice);
        TextView summaryDropOffAddress = (TextView) dialog.findViewById(R.id.summary_drop_off);
        summaryDropOffAddress.setText(description);
        //TextView summaryErrandAddress = (TextView) dialog.findViewById(R.id.summary_errand);
        //summaryErrandAddress.setText(msg);

        Button dialogButton = (Button) dialog.findViewById(R.id.summary_confirm);
        dialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }
}
