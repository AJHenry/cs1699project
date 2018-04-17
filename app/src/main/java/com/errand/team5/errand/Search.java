package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

public class Search extends AppCompatActivity {


    //Components
    private ListView feed;
    private ProgressBar spinner;

    //Global Variables
    private android.location.Location lastKnownLocation;
    private ArrayList<TaskModel> taskList;
    private String term;

    //Firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference myRef;

    //Debug
    private final String TAG = "Search";

    private int passOnFlag = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e(TAG,"In Search.java");
        setContentView(R.layout.activity_search);

        String apiKey =  getIntent().getStringExtra("ApiKey");
        Log.e(TAG, "Key: " + apiKey);
        Uri.Builder uri = new Uri.Builder();
        uri.authority("keys");

        ContentProviderClient myCP = getContentResolver().acquireContentProviderClient("keys");
        try {
            Cursor myQuery = myCP.query(uri.build(), new String[]{"Test"} , "selection", new String[]{apiKey}, "sort");
            if (myQuery != null){
                myQuery.moveToFirst();

                if (myQuery.getString(1).equals(apiKey)){
                    Toast.makeText(getApplicationContext(), "API KEY FOUND",Toast.LENGTH_LONG).show();
                }
                else{
                    Toast.makeText(getApplicationContext(), "API KEY NOT FOUND 1",Toast.LENGTH_LONG).show();
                }
            }
            else {
                Toast.makeText(getApplicationContext(), "API KEY NOT FOUND 2",Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(),Toast.LENGTH_LONG).show();
        }

        term = getIntent().getStringExtra("SearchTerm");
        if(!(term.isEmpty())){
            //Toast.makeText(getContext(), "No tasks in your area", Toast.LENGTH_LONG).show();
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.search_layout), "Received term from Service, term: "+term, Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }else{
            Toast.makeText(getApplicationContext(), "Search term provided was empty", Toast.LENGTH_LONG).show();
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.search_layout), "Search term provided was empty", Snackbar.LENGTH_INDEFINITE);
            snackbar.show();
        }

        //Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Components
        //TODO THIS WILL CAUSE AN ERROR, sometimes if the navigates away from the view when firebase tries to fill it
        //Maybe fixed, someone else confirm
        feed = (ListView) findViewById(R.id.search_feed);
    }

    /**
     * Check if their profile is null, if so, redirect them to login
     * @param user Firebase User currently logged in
     */
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(this, Login.class);
            startActivity(login);
        } else {
            this.user = user;
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        //spinner.setVisibility(View.VISIBLE);
        startLocationService();
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        stopLocation();
        super.onPause();
    }

    @Override
    public void onStart(){
        super.onStart();

        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    @Override
    public void onStop(){
        super.onStop();
    }

    /**
     * mLocation Service
     * Calls the updateUI method
     */
    private void startLocationService() {
        Log.d(TAG, "Started location service");

        long mLocTrackingInterval = 1000 * 5; // 5 sec
        float trackingDistance = 0;
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(trackingDistance)
                .setInterval(mLocTrackingInterval);

        SmartLocation.with(this)
                .location()
                //.continuous()
                .oneFix()
                .config(builder.build())
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(android.location.Location location) {
                        Log.d(TAG, "Updated location");
                        lastKnownLocation = location;
                        Log.d(TAG, "Lon: " + lastKnownLocation.getLongitude() + " Lat: " + lastKnownLocation.getLatitude());
                        updateUI(location);
                    }
                });
    }

    /**
     * Stops location service from running in background
     */
    private void stopLocation() {
        SmartLocation.with(this)
                .location()
                .stop();
    }

    /**
     * This generates the feed for the home screen
     * Gets called from updateUI()
     * @param errandList The list of errands to display
     * @param location location of the current user, not sure why this is needed however
     */
    private void generateFeed(final ArrayList<TaskModel> errandList, android.location.Location location) {
        Log.d(TAG, "Generated feed for home screen");

        //TODO display a text with no tasks available in your area
        if (errandList.isEmpty()) {
            Toast.makeText(this, "No tasks in your area", Toast.LENGTH_LONG).show();
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.search_layout), "No tasks found for term: "+term, Snackbar.LENGTH_INDEFINITE);

            snackbar.show();
            return;
        }

        TaskFeedAdapter adapter = new TaskFeedAdapter(errandList, this, location);

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent task = new Intent(getApplicationContext(), Task.class);
                task.putExtra("taskId", errandList.get(position).getTaskId());
                task.putExtra("passOnFlag", passOnFlag);
                startActivity(task);
            }
        });
    }

    //Gets called from startLocation()

    /**
     * Takes the user's location and uses Geofire to query the database and then passes the TaskModel Objects to
     * generateFeed in order to display them to the user
     * TODO Add a content provider to store the data in
     *
     * @param userLocation Search of the user to query errands on
     */
    private void updateUI(final Location userLocation) {
        //TODO decide when to update

        //Global arraylist of errands
        final ArrayList<TaskModel> errands = new ArrayList<>();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Query database for all tasks with creator id of this user
        final DatabaseReference myTasksRef = database.getReference("errands");

        Query query = myTasksRef.orderByChild("title").equalTo(term);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot errandValue : dataSnapshot.getChildren()) {
                        TaskModel errand = errandValue.getValue(TaskModel.class);
                        if(errand.getStatus() == 0) {
                            errands.add(errand);
                        }
                    }
                }
                generateFeed(errands, userLocation);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Error with firebase, contact help", Toast.LENGTH_LONG).show();
            }
        });
    }
}
