package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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

public class LocationFeed extends AppCompatActivity {


    //Components
    private ListView feed;
    private ProgressBar spinner;

    //Global Variables
    private android.location.Location lastKnownLocation;
    private ArrayList<TaskModel> taskList;
    private double lat;
    private double lng;

    //Firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference myRef;



    //Debug
    private final String TAG = "Search";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);


        lat = getIntent().getDoubleExtra("lat", 0.0);
        lng = getIntent().getDoubleExtra("lng", 0.0);
        //Toast.makeText(getContext(), "No tasks in your area", Toast.LENGTH_LONG).show();
        Snackbar snackbar = Snackbar
                .make(findViewById(R.id.search_layout), "Received location from intent lat: "+lat + " long: "+lng, Snackbar.LENGTH_INDEFINITE);
        snackbar.show();

        //Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Components
        //TODO THIS WILL CAUSE AN ERROR, sometimes if the navigates away from the view when firebase tries to fill it
        //Maybe fixed, someone else confirm
        feed = (ListView) findViewById(R.id.search_feed);

        mLocation location = new mLocation(lat, lng);

        updateUI(location);
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
    public void onStart(){
        super.onStart();

        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
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
                    .make(findViewById(R.id.search_layout), "No tasks found in a 20 mi radius for lat: "+lat +" long: " + lng, Snackbar.LENGTH_INDEFINITE);

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
    private void updateUI(final mLocation userLocation) {
        //TODO decide when to update

        //Global arraylist of errands
        final ArrayList<TaskModel> errands = new ArrayList<>();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Query database for all tasks with creator id of this user
        final DatabaseReference myTasksRef = database.getReference("errands");

        //GeoFire instance
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geofire");
        GeoFire geoFire = new GeoFire(ref);

        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        //First we need to establish where the user is with lat and lng
        double lat = userLocation.getLatitude();
        double lng = userLocation.getLongitude();

        //Now we need to set a radius of how far we need to look, in km
        double radius = 50;

        //Now we build a query with that
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);

        //TODO Come up with a better plan than this
        //This flag will be set with onGeoQueryReady
        //But getting the last key may not be done since firebase is async
        //So instead we have to set the flag in onGeoQueryReady, and then call it from the last onKeyEntered
        final boolean[] isReady = {false};

        //Now we need listeners for each type of event
        //We're really only concerned with
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d(TAG, "Found one " + key);
                //Query the firebase based on the key
                Query queryRef = myTasksRef.orderByChild("taskId").equalTo(key);

                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Log.d(TAG, "Got it from the database");
                        if (dataSnapshot.exists()) {
                            Log.d(TAG, "Even added it!");
                            // dataSnapshot is the "issue" node with all children with id 0
                            for (DataSnapshot errandMap : dataSnapshot.getChildren()) {
                                //Add the errand to the list
                                TaskModel errand = errandMap.getValue(TaskModel.class);
                                if (errand.status == 0) {
                                    errands.add(errand);
                                }
                            }

                            if (isReady[0]) {
                                //Generate the keys when everything is done
                                generateFeed(errands, userLocation);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Should not happen
                        Toast.makeText(getApplicationContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
                    }
                });
            }

            @Override
            public void onKeyExited(String key) {
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            //Gets called when all the keys are found
            @Override
            public void onGeoQueryReady() {
                isReady[0] = true;
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "Error a=with Geofire ref error: " + error);
            }
        });
    }
}
