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
import com.braintreepayments.api.dropin.DropInRequest;
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

    //Activity results from Braintree drop-in UI
    private final int BRAINTREE_DROPIN = 3;

    //Components
    private Button dropOffLocation;
    private Button errandLocation;
    private CurrencyEditText costInput;
    private EditText titleInput;
    private EditText descriptionInput;
    private EditText specialInstructionsInput;
    private NumberPicker timeTypeInput;
    private NumberPicker timeAmountInput;

    //Edit existing task or not
    private boolean editExisting;

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

        //Editing existing task or not
        editExisting = getIntent().getBooleanExtra("editOnly", false);

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

        if (requestCode == BRAINTREE_DROPIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Payment held until task completion!", Toast.LENGTH_LONG).show();
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
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
        double dBasePrice = costInput.getRawValue()/100.0;
        long basePrice = costInput.getRawValue()/100;
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
        double fees = dBasePrice * 0.20;
        double subtotal = fees + dBasePrice;

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


        if(dBasePrice <= 0.0){
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
            if(!editExisting) {
                showSummary(title, description, basePrice, fees, subtotal, dropOffPlace, specialInstructions, timeToComplete);
            }
            else{
                Log.e(TAG, "Returning edited task data to UpdateTask");
                TaskData passBack = new TaskData();
                passBack.setTitle(title);
                passBack.setDescription(description);
                passBack.setPrice(basePrice);
                passBack.setDropOffLocation(new mLocation(dropOffPlace.getLatLng().latitude, dropOffPlace.getLatLng().longitude));
                passBack.setSpecialInstructions(specialInstructions);
                passBack.setTimeToComplete(timeToComplete);
                Intent returnIntent = new Intent();
                returnIntent.putExtra("passBack", passBack);
                setResult(RESULT_OK, returnIntent);
                finish();
            }
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
                if(createTaskEntry(createdErrand)){
                    //Display success to user
                    Toast.makeText(getApplicationContext(), "Successfully requested Errand", Toast.LENGTH_LONG).show();

                    //Need to set the result to ok
                    toSend = false; //TEMPORARY - FOR TESTING
                    if (!toSend){
                        //Paypal drop-in payment UI (testing)
                        //braintreeDropIn();

                        Intent returnIntent = new Intent();
                        setResult(Activity.RESULT_OK, returnIntent);
                        dialog.dismiss();
                        finish();

                    }
                    else {
                        //Used for passing data along to the next app
                        // Only called when we receive data from another app
                        Intent launchIntent = getPackageManager().getLaunchIntentForPackage("com.example.kmt71.couponapp");
                        Bundle extras = new Bundle();
                        extras.putInt("whichTrig",4);
                        extras.putString("store", title);
                        dialog.dismiss();

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

    /**
     * Method responsible for opening Braintree drop-in UI for payment
     * @return none
     */
    public void braintreeDropIn(){
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken("eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiIyMTdhODgwMTcyOGE0ZTgyN2VmNDM0ZGZhYzExNmQ4MzI2ZjU2MjQwOGJkYTc0MTllMTNjYmJkYjkwNzgxMWVkfGNyZWF0ZWRfYXQ9MjAxOC0wNC0wNVQxODoxODowNi41MDc3NDYwMTUrMDAwMFx1MDAyNm1lcmNoYW50X2lkPTM0OHBrOWNnZjNiZ3l3MmJcdTAwMjZwdWJsaWNfa2V5PTJuMjQ3ZHY4OWJxOXZtcHIiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvMzQ4cGs5Y2dmM2JneXcyYi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzLzM0OHBrOWNnZjNiZ3l3MmIvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vY2xpZW50LWFuYWx5dGljcy5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tLzM0OHBrOWNnZjNiZ3l3MmIifSwidGhyZWVEU2VjdXJlRW5hYmxlZCI6dHJ1ZSwicGF5cGFsRW5hYmxlZCI6dHJ1ZSwicGF5cGFsIjp7ImRpc3BsYXlOYW1lIjoiQWNtZSBXaWRnZXRzLCBMdGQuIChTYW5kYm94KSIsImNsaWVudElkIjpudWxsLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjp0cnVlLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJ1bnZldHRlZE1lcmNoYW50IjpmYWxzZSwiYnJhaW50cmVlQ2xpZW50SWQiOiJtYXN0ZXJjbGllbnQzIiwiYmlsbGluZ0FncmVlbWVudHNFbmFibGVkIjp0cnVlLCJtZXJjaGFudEFjY291bnRJZCI6ImFjbWV3aWRnZXRzbHRkc2FuZGJveCIsImN1cnJlbmN5SXNvQ29kZSI6IlVTRCJ9LCJtZXJjaGFudElkIjoiMzQ4cGs5Y2dmM2JneXcyYiIsInZlbm1vIjoib2ZmIn0=");
        startActivityForResult(dropInRequest.getIntent(this), BRAINTREE_DROPIN);
    }
}
