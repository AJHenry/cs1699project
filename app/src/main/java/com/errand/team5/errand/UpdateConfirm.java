package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.places.Place;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;

/**
 * Added by Austin M, 4/15/18
 *
 * Displays updated Task information, asks user to confirm
 * If OK, write to DB and drop back to Main
 * If no, populate CreateTask fields and drop to CreateTask activity
 */

public class UpdateConfirm extends AppCompatActivity {

    //Log tag
    private final String TAG = "UpdateConfirm";

    //Firebase refs
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    //Save to the reference for all tasks
    FirebaseDatabase database;
    DatabaseReference ref;

    //Task data
    private Bundle extras;
    private String taskID;
    private double fees;
    private double subtotal;

    //Save original location?
    TaskModel origTask;
    TaskModel newErrand;

    final boolean toSend = false;


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        // get current Firebase instances
        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("errands");
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser fUser = mAuth.getCurrentUser();
        checkLogin(fUser);

        extras = getIntent().getExtras();
        boolean delete = extras.getBoolean("delete", false);
        TaskData taskData;
        try {
            if ((taskData = (TaskData) extras.getSerializable("taskData")) != null) {
                //toSend = true;
                taskID = extras.getString("taskID");
                showDialog(taskData, delete);
            }
        }catch (NullPointerException e){
            Log.wtf(TAG, "ERROR GETTING taskData info, please contact help");
        }
    }

    public void showDialog(TaskData taskData, boolean delete){
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.update_confirm);

        //merge updated info with original
        mergeInfo(taskData);

        TextView baseCost = (TextView) dialog.findViewById(R.id.payment_base_cost);
        TextView feeCost = (TextView) dialog.findViewById(R.id.payment_fees);

        TextView subtotalText = (TextView) dialog.findViewById(R.id.payment_subtotal);

        TextView specialInstructionsText = (TextView) dialog.findViewById(R.id.update_confirm_special_instructions);
        specialInstructionsText.setText(specialInstructions);

        NumberFormat format = NumberFormat.getCurrencyInstance();

        subtotalText.setText(format.format(subtotal));
        baseCost.setText(format.format(basePrice));
        feeCost.setText(format.format(fees));

        TextView update_confirmTitle = (TextView) dialog.findViewById(R.id.update_confirm_title);
        update_confirmTitle.setText(title);
        TextView update_confirmDescription = (TextView) dialog.findViewById(R.id.update_confirm_description);
        update_confirmDescription.setText(description);
        TextView update_confirmDropOffAddress = (TextView) dialog.findViewById(R.id.update_confirm_drop_off);
        update_confirmDropOffAddress.setText(addr);

        /*
        TextView update_confirmErrandAddress = (TextView) dialog.findViewById(R.id.update_confirm_errand);
        update_confirmErrandAddress.setText(msg);
        */

        //Confirm button
        Button confirmButton = (Button) dialog.findViewById(R.id.update_confirm_confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //dialog.dismiss();

                //mLocation dropOffMLocation = new mLocation(dropOffPlace.getLatLng().latitude, dropOffPlace.getLatLng().longitude);
                //Get payment first
                //Then add to database

                //Create a new Model
                TaskModel createdErrand = new TaskModel();
                //createdErrand.setDropOffDestination(dropOffMLocation);
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
                if(updateTaskEntry(createdErrand)){
                    //Display success to user
                    Toast.makeText(getApplicationContext(), "Successfully requested Errand", Toast.LENGTH_LONG).show();

                    //Need to set the result to ok
                    if (!toSend){
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
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
        Button cancelButton = (Button) dialog.findViewById(R.id.update_confirm_cancel);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        dialog.show();

    }

    /**
     * Method responsible for writing to firebase
     * @param newErrand The errand object to insert
     * @return True if successful, false otherwise
     */
    public boolean updateTaskEntry(TaskModel newErrand, boolean delete){
        Log.d(TAG, "Created entry");

        //Status of the operation
        final boolean result = true;

        if(delete){
            ref.child(taskID).removeValue();
            return result;
        }

        //Set the rest of the bookkeeping data
        newErrand.setTaskId(taskID);
        newErrand.setCreatorId(user.getUid());
        newErrand.setPublishTime(new mTimestamp());

        //re-use old addresses
        double lat = origTask.getDropOffDestination().getLatitude();
        double lng = origTask.getDropOffDestination().getLongitude();

        //Add to firebase
        ref.child(taskID).setValue(newErrand);

        //Now we need to save to Geofire so we are able to query based on location
        DatabaseReference geofireRef = FirebaseDatabase.getInstance().getReference("geofire");
        GeoFire geoFire = new GeoFire(geofireRef);

        geoFire.setLocation(taskID, new GeoLocation(lat, lng), new GeoFire.CompletionListener() {
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
     *
     * Check if current user profile is null, if so, redirect them to login
     * @param fUser Firebase Auth token from active instance
     */
    private void checkLogin(FirebaseUser fUser) {
        if (fUser == null) {
            Intent login = new Intent(this, Login.class);
            startActivity(login);
        } else {
            //Log.wtf(DEBUG, "Current user: " + fUser.getUid());
            this.user = fUser;
        }
    }

    private void mergeInfo(TaskData taskData){
        //get original task info
        Query q = ref.child(taskID);
        q.addListenerForSingleValueEvent(new ValueEventListener() {
                                             @Override
                                             public void onDataChange(DataSnapshot dataSnapshot) {
                                                 origTask = dataSnapshot.getValue(TaskModel.class);
                                             }

                                             @Override
                                             public void onCancelled(DatabaseError databaseError) {
                                                 //we're screwed
                                                 Toast.makeText(getApplicationContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
                                             }
                                         }
        );

        //get fields from taskData, replacing with original if null
        String si = setSpecialInstructions(taskData.getSpecialInstructions());
        if(si.length() == 0){
            si = origTask.getSpecialInstructions();
        }
        String title = taskData.getTitle();
        if(title.length() == 0){
            title = origTask.getTitle();
        }
        double basePrice = taskData.getPrice();
        if(basePrice < 0){
            basePrice = origTask.getBaseCost();
        }
        String description = taskData.getDescription();
        if(description.length() == 0){

        }
        final String address = taskData.getAddress();
        final String address2 = taskData.getAddress2();
        final String city = taskData.getCity();
        final String state = taskData.getState();
        final String zip = taskData.getZip();
        final String addr = address + ", " + address2 + ", " + city + ", " + state + " " + zip;
        final int timeToComplete = taskData.getTimeToComplete();

        final double fees = basePrice * 0.20;
        final double subtotal = fees + basePrice;
    }
}
