package com.errand.team5.errand;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.Toast;

import com.blackcat.currencyedittext.CurrencyEditText;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Text;

import java.text.NumberFormat;


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

    //Current mLocation
    private Location loc;
    private boolean toSend = false;

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
    private NumberPicker timeTypeInput;
    private NumberPicker timeAmountInput;

    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseUser user;



    //Global Values
    private int currentTimeSelected = 0;

    //TODO Check for user login


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);
        getSupportActionBar().setTitle("Create Task");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //Get the references to firebase
        mAuth = FirebaseAuth.getInstance();

        //Get location passed from MainActivity
        Bundle extras = getIntent().getExtras();

        //Get the data if it is not nll


        //Components
        dropOffLocation = (Button) findViewById(R.id.task_drop_off_button);
        errandLocation = (Button) findViewById(R.id.errand_location_button);
        costInput = (CurrencyEditText) findViewById(R.id.cost);
        titleInput = (EditText) findViewById(R.id.title);
        descriptionInput = (EditText) findViewById(R.id.description);
        specialInstructionsInput = (EditText) findViewById(R.id.special_instructions);
        timeTypeInput = (NumberPicker) findViewById(R.id.time_type);
        timeAmountInput = (NumberPicker) findViewById(R.id.time_amount);

        TaskData taskData;
        try {
            if ((taskData = (TaskData) extras.getSerializable("taskData")) != null) {

                titleInput.setText(taskData.getTitle());
                costInput.setValue(taskData.getPrice());
                descriptionInput.setText(taskData.getDescription());
                specialInstructionsInput.setText(taskData.getSpecialInstructions());
                toSend = true;
            }
        }catch (NullPointerException e){
            Log.wtf(TAG, "ERROR GETTING taskData info, please contact help");
        }

        //Populate the pickers
        timeTypeInput.setMinValue(0);
        timeTypeInput.setMaxValue(1);
        timeTypeInput.setDisplayedValues( new String[] { "Mins", "Hours" } );

        //Populate the amount picker
        timeAmountInput.setMinValue(0);
        timeAmountInput.setMaxValue(2);
        timeAmountInput.setDisplayedValues( new String[] { "15", "30", "45" } );
        

        timeTypeInput.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                Log.d(TAG, "Called");
                if(i == 1){
                    timeAmountInput.setMinValue(0);
                    timeAmountInput.setMaxValue(2);
                    timeAmountInput.setDisplayedValues( new String[] { "15", "30", "45" } );
                }else{
                    timeAmountInput.setMinValue(0);
                    timeAmountInput.setMaxValue(2);
                    timeAmountInput.setDisplayedValues( new String[] { "1", "2", "3" } );
                }
            }
        });


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
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        // TODO Fix error where application closes after first login
        if (user == null) {
            Intent login = new Intent(this, Login.class);
            startActivity(login);
            //finish();
        } else {
            this.user = user;
        }
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
                Log.d(TAG, "mLocation available");
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
                //builder.setLatLngBounds(mLatLngBounds);
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
        double baseprice = costInput.getRawValue()/100.0;
        String dropOffAddress = "";
        if(dropOffPlace != null) {
            dropOffAddress = dropOffPlace.getAddress().toString();
        }
        //String errandAddress = errandPlace.getAddress().toString();
        String specialInstructions = specialInstructionsInput.getText().toString();

        int timeToComplete = 0;
        if (timeTypeInput.getValue() == 1) {
            // HOUR
            timeToComplete = (timeAmountInput.getValue() + 1) * 60;
        } else {
            //MIN
            timeToComplete = (timeAmountInput.getValue() + 1) * 15;
        }

        //TextInputLayout
        TextInputLayout titleInputLayout = (TextInputLayout) findViewById(R.id.text_input_title);
        TextInputLayout descriptionInputLayout = (TextInputLayout) findViewById(R.id.text_input_description);
        TextInputLayout basePriceInputLayout = (TextInputLayout) findViewById(R.id.text_input_cost);
        TextView addressErrorLayout = (TextView) findViewById(R.id.location_error_text);
        titleInputLayout.setErrorEnabled(false);
        descriptionInputLayout.setErrorEnabled(false);
        basePriceInputLayout.setErrorEnabled(false);
        addressErrorLayout.setText(null);


        //Fee Calculation
        double fees = baseprice * 0.20;

        double subtotal = fees + baseprice;

        if(title.length() < 5){
            titleInputLayout.setErrorEnabled(true);
            titleInputLayout.setError("Enter a longer title");
        }

        if(title.length() > 25){
            titleInputLayout.setErrorEnabled(true);
            titleInputLayout.setError("Enter a shorter title");
        }

        if(title == null || title.isEmpty()){
            //Display error underneath
            titleInputLayout.setErrorEnabled(true);
            titleInputLayout.setError("Valid title required");
            dataSatisfied = false;
        }

        if(description.length() > 500){
            descriptionInputLayout.setErrorEnabled(true);
            descriptionInputLayout.setError("Enter a shorter description");
        }

        if(description.length() < 10){
            descriptionInputLayout.setErrorEnabled(true);
            descriptionInputLayout.setError("Enter a longer description");
        }

        if(description == null || description.isEmpty()){
            //Display error underneath
            descriptionInputLayout.setErrorEnabled(true);
            descriptionInputLayout.setError("Valid description required");
            dataSatisfied = false;
        }


        if(baseprice <= 0.0){
            //Display error underneath
            basePriceInputLayout.setErrorEnabled(true);
           basePriceInputLayout.setError("Cost*");
            dataSatisfied = false;
        }
        if(dropOffAddress == null || dropOffAddress.isEmpty()){
            //Display error underneath
            addressErrorLayout.setText("Please select a valid address");
            dataSatisfied = false;
        }
        /*
        if(errandAddress == null || errandAddress.isEmpty()){
            //Display error underneath

            dataSatisfied = false;
        }
        */

        //Data is valid
        if(dataSatisfied) {
            showSummary(title, description, baseprice, fees, subtotal, dropOffPlace, specialInstructions, timeToComplete);
        }
    }


    public void showSummary(final String title, final String description, final double basePrice, final double fees, double subtotal, final Place dropOffPlace, final String specialInstructions, final int timeToComplete){
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.summary);

        TextView baseCost = (TextView) dialog.findViewById(R.id.payment_base_cost);
        TextView feeCost = (TextView) dialog.findViewById(R.id.payment_fees);

        TextView subtotalText = (TextView) dialog.findViewById(R.id.payment_subtotal);

        TextView specialInstructionsText = (TextView) dialog.findViewById(R.id.summary_special_instructions);
        specialInstructionsText.setText(specialInstructions);

        NumberFormat format = NumberFormat.getCurrencyInstance();

        subtotalText.setText(format.format(subtotal));
        baseCost.setText(format.format(basePrice));
        feeCost.setText(format.format(fees));

        TextView summaryTitle = (TextView) dialog.findViewById(R.id.summary_title);
        summaryTitle.setText(title);
        TextView summaryDescription = (TextView) dialog.findViewById(R.id.summary_description);
        summaryDescription.setText(description);
        TextView summaryDropOffAddress = (TextView) dialog.findViewById(R.id.summary_drop_off);
        summaryDropOffAddress.setText(dropOffPlace.getAddress().toString());
        //TextView summaryErrandAddress = (TextView) dialog.findViewById(R.id.summary_errand);
        //summaryErrandAddress.setText(msg);

        //Confirm button
        Button confirmButton = (Button) dialog.findViewById(R.id.summary_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();

                mLocation dropOffMLocation = new mLocation(dropOffPlace.getLatLng().latitude, dropOffPlace.getLatLng().longitude);
                //Get payment first
                //Then add to database

                //Create a new Model
                TaskModel createdErrand = new TaskModel();
                createdErrand.setDropOffDestination(dropOffMLocation);
                createdErrand.setBaseCost(basePrice);
                createdErrand.setCategory(0);
                createdErrand.setDescription(description);
                createdErrand.setStatus(0);
                createdErrand.setSpecialInstructions(specialInstructions);
                createdErrand.setPaymentCost(fees);
                createdErrand.setTitle(title);
                createdErrand.setTimeToCompleteMins(timeToComplete);
                createdErrand.setUser(new User(user.getUid(), user.getPhotoUrl().toString(), user.getDisplayName(), user.getEmail()));


                //Pass it to database
                if(createTaskEntry(createdErrand)){
                    //Display success to user
                    Toast.makeText(getApplicationContext(), "Successfully requested Errand", Toast.LENGTH_LONG).show();

                    //Need to set the result to ok
                    if (!toSend){
                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();
                    }
                    else {
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.kmt71.couponapp");
                        Bundle extras = new Bundle();
                        extras.putInt("whichTrig",4);
                        extras.putString("store", title);

                        if (launchIntent != null) {
                            launchIntent.putExtras(extras);
                            startActivity(launchIntent);//null pointer check in case package name was not found
                        }
                    }
                }else{
                    //Shouldn't ever happen
                    Toast.makeText(getApplicationContext(), "Error requesting Errand, contact help", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Cancel button
        Button cancelButton = (Button) dialog.findViewById(R.id.summary_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    public boolean createTaskEntry(TaskModel newErrand){
        Log.d(TAG, "Created entry");

        //Save to the reference for all tasks
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("errands");
        String id = ref.push().getKey();



        //Set the rest of the bookkeeping data
        newErrand.setTaskId(id);
        newErrand.setCreatorId(user.getUid());
        newErrand.setPublishTime(new mTimestamp());

        ref.child(id).setValue(newErrand);

        //Save to the reference for users tasks
        //DatabaseReference createdTasks = database.getReference("created_tasks");
        //createdTasks.child(user.getUid()).push().setValue(newErrand.getTaskId());

        //Tell them it was successful
        return true;
    }
}
