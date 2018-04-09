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
import com.braintreepayments.api.BraintreeFragment;
import com.braintreepayments.api.exceptions.InvalidArgumentException;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.SphericalUtil;

import org.w3c.dom.Text;

import java.text.NumberFormat;


public class CreateTask extends AppCompatActivity {

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

    //Braintree/Paypal
    private BraintreeFragment braintreeInstance;
    // this is an unsecure temp tokenization key for Braintree, will replace later
    private String btAuth = "sandbox_64h4y8bv_nyqs77zshf4f6wsc";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_task);

        //Needs to be wrapped in a try-catch for a nullpointer
        try {
            getSupportActionBar().setTitle("Create Task");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }catch(NullPointerException e){
            Log.e(TAG, "Error setting title and back button for actionBar");
        }

        //Get the references to firebase
        mAuth = FirebaseAuth.getInstance();

        //Get references to Braintree
        try {
            braintreeInstance = BraintreeFragment.newInstance(this, btAuth);
        }
        catch(InvalidArgumentException e){
            Log.wtf(TAG, "ERROR connecting to Braintree, please contact help");
        }

        //Get location passed from MainActivity
        Bundle extras = getIntent().getExtras();

        //Components
        dropOffLocation = (Button) findViewById(R.id.task_drop_off_button);
        errandLocation = (Button) findViewById(R.id.errand_location_button);
        costInput = (CurrencyEditText) findViewById(R.id.cost);
        titleInput = (EditText) findViewById(R.id.title);
        descriptionInput = (EditText) findViewById(R.id.description);
        specialInstructionsInput = (EditText) findViewById(R.id.special_instructions);
        timeTypeInput = (NumberPicker) findViewById(R.id.time_type);
        timeAmountInput = (NumberPicker) findViewById(R.id.time_amount);

        //Used for getting errand info from Team 4
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

        //Set on click listeners for the pickers
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
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(this, Login.class);
            startActivity(login);
        } else {
            this.user = user;
        }
    }

    //TODO Anyone know what this is for?
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }


    /**
     * Used for the checkmark in the top right
     * @param menu
     * @return
     */
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
                createTask();
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

    /**
     * Used for accessing the Google Place Picker API for the drop off location
     */
    private void dropOffPicker() {
        try {
            //Create a new Place Picker intent
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            //Start the activity
            startActivityForResult(builder.build(this), DROP_OFF_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
        } catch (GooglePlayServicesRepairableException e) {
        }
    }


    /**
     * Used to pick the errand location
     * CURRENTLY UNUSED
     */
    private void errandPicker() {
        try {
            //Create a new place picker intent
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

            //Start the activity for place picker
            startActivityForResult(builder.build(this), ERRAND_PLACE_PICKER);
        } catch (GooglePlayServicesNotAvailableException e) {
        } catch (GooglePlayServicesRepairableException e) {
        }
    }


    /**
     * Method callback for Place Picker
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //Drop off location place picker
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


    /**
     * Used for getting the values from the input fields and verifying they are valid
     *
     * Called when a person hits the checkmark in the toolbar
     */
    public void createTask(){
        boolean dataSatisfied = true;

        //Get all the info from the inputs
        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        double basePrice = costInput.getRawValue()/100.0;
        String dropOffAddress = "";
        if(dropOffPlace != null) {
            dropOffAddress = dropOffPlace.getAddress().toString();
        }
        String specialInstructions = specialInstructionsInput.getText().toString();

        //Spinner values
        int timeToComplete = 0;
        if (timeTypeInput.getValue() == 1) {
            // HOUR
            timeToComplete = (timeAmountInput.getValue() + 1) * 60;
        } else {
            //MIN
            timeToComplete = (timeAmountInput.getValue() + 1) * 15;
        }

        //TextInputLayout fields, used for displaying error messages
        TextInputLayout titleInputLayout = (TextInputLayout) findViewById(R.id.text_input_title);
        TextInputLayout descriptionInputLayout = (TextInputLayout) findViewById(R.id.text_input_description);
        TextInputLayout basePriceInputLayout = (TextInputLayout) findViewById(R.id.text_input_cost);
        TextView addressErrorLayout = (TextView) findViewById(R.id.location_error_text);
        titleInputLayout.setErrorEnabled(false);
        descriptionInputLayout.setErrorEnabled(false);
        basePriceInputLayout.setErrorEnabled(false);
        addressErrorLayout.setText(null);


        //TODO Fee Calculation
        double fees = basePrice * 0.20;
        double subtotal = fees + basePrice;

        //Error checking
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


        if(basePrice <= 0.0){
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

        //Data is valid
        if(dataSatisfied) {
            showSummary(title, description, basePrice, fees, subtotal, dropOffPlace, specialInstructions, timeToComplete);
        }
    }


    /**
     * Used for showing a summary of the person's errand
     * TODO display user errand time
     * @param title
     * @param description
     * @param basePrice
     * @param fees
     * @param subtotal
     * @param dropOffPlace
     * @param specialInstructions
     * @param timeToComplete
     */
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
                //TODO PayPal login
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
                        //Used for passing data along to the next app
                        // Only called when we receive data from another app
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

    /**
     * Method responsible for writing to firebase
     * @param newErrand The errand object to insert
     * @return True if successful, false otherwise
     */
    public boolean createTaskEntry(TaskModel newErrand){
        Log.d(TAG, "Created entry");

        //Status of the operation
        final boolean result = true;

        //Save to the reference for all tasks
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ref = database.getReference("errands");

        //Create a new task ID for reference
        String taskId = ref.push().getKey();

        //Set the rest of the bookkeeping data
        newErrand.setTaskId(taskId);
        newErrand.setCreatorId(user.getUid());
        newErrand.setPublishTime(new mTimestamp());

        //Add to firebase
        ref.child(taskId).setValue(newErrand);

        //Now we need to save to Geofire so we are able to query based on location
        DatabaseReference geofireRef = FirebaseDatabase.getInstance().getReference("geofire");
        GeoFire geoFire = new GeoFire(geofireRef);

        //Set the key as the taskId, and the location as the drop off address
        double lat = newErrand.getDropOffDestination().getLatitude();
        double lng = newErrand.getDropOffDestination().getLongitude();

        geoFire.setLocation(taskId, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                //See if there's an error
                if (error != null) {
                    //System.err.println("There was an error saving the location to GeoFire: " + error);
                    Log.e(TAG, "Error writing to geofire database: "+error);
                }
            }
        });

        //Tell them it was successful
        return result;
    }
}
