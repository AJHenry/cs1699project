package com.errand.team5.errand;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.location.Location;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
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


public class Feed extends Fragment {

    //Components
    private ListView feed;
    private ProgressBar spinner;

    //Global Variables
    private Location lastKnownLocation;
    private ArrayList<TaskModel> taskList;

    //Firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private DatabaseReference myRef;

    //Used for the broadcast receiver
    BroadcastReceiver receiver = null;

    //Debug
    private final String TAG = "FeedClass";


    public Feed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);



        //Firebase instance
        mAuth = FirebaseAuth.getInstance();

        //Components
        //TODO THIS WILL CAUSE AN ERROR, sometimes if the navigates away from the view when firebase tries to fill it
        //Maybe fixed, someone else confirm
        feed = (ListView) getActivity().findViewById(R.id.task_feed);
        spinner = (ProgressBar) getActivity().findViewById(R.id.main_loading);

        // Check if user is signed in (non-null)
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    /**
     * Check if their profile is null, if so, redirect them to login
     * @param user Firebase User currently logged in
     */
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(getContext(), Login.class);
            startActivity(login);
        } else {
            this.user = user;
        }
    }

    @Override
    public void onResume() {
        try {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");
        }catch (NullPointerException e){

        }
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

        SmartLocation.with(getActivity())
                .location()
                //.continuous()
                .oneFix()
                .config(builder.build())
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
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
        SmartLocation.with(getActivity())
                .location()
                .stop();
    }

    /**
     * This generates the feed for the home screen
     * Gets called from updateUI()
     * @param errandList The list of errands to display
     * @param location location of the current user, not sure why this is needed however
     */
    private void generateFeed(final ArrayList<TaskModel> errandList, Location location) {
        Log.d(TAG, "Generated feed for home screen");

        Snackbar snackbar = null;
        try {
             snackbar = Snackbar
                    .make(getActivity().findViewById(R.id.main_layout), "No tasks available in your area", Snackbar.LENGTH_INDEFINITE);
        }catch (NullPointerException e){

        }

        //TODO display a text with no tasks available in your area


        //Get rid of the spinner
        spinner.setVisibility(View.GONE);

        try {
            TaskFeedAdapter adapter = new TaskFeedAdapter(errandList, getView().getContext(), location);

            feed.setAdapter(adapter);
            feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent task = new Intent(getContext(), Task.class);
                    task.putExtra("taskId", errandList.get(position).getTaskId());
                    startActivity(task);
                }
            });
        }catch(NullPointerException e){

        }

        try {
            if (errandList.isEmpty()) {
                snackbar.show();
            } else {
                snackbar.dismiss();
            }
        }catch (NullPointerException e){

        }
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

        //GeoFire instance
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("geofire");
        GeoFire geoFire = new GeoFire(ref);

        // creates a new query around [37.7832, -122.4056] with a radius of 0.6 kilometers
        //First we need to establish where the user is with lat and lng
        double lat = userLocation.getLatitude();
        double lng = userLocation.getLongitude();

        //Now we need to set a radius of how far we need to look, in km
        double radius = 20;

        //Now we build a query with that
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat, lng), radius);

        //TODO Come up with a better plan than this
        //This flag will be set with onGeoQueryReady
        //But getting the last key may not be done since firebase is async
        //So instead we have to set the flag in onGeoQueryReady, and then call it from the last onKeyEntered
        final boolean[] isReady = {false};
        final int[] keyCount = {0};
        final int[] refCount = {0};

        //Now we need listeners for each type of event
        //We're really only concerned with
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                keyCount[0]++;
                Log.d(TAG, "Found one " + key);
                //Query the firebase based on the key
                Query queryRef = myTasksRef.orderByChild("taskId").equalTo(key);

                queryRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        refCount[0]++;
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

                        }
                        if(refCount[0] == keyCount[0]){
                            if (isReady[0]) {
                                //Generate the keys when everything is done
                                generateFeed(errands, userLocation);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //Should not happen
                        Toast.makeText(getContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
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
                if(refCount[0] == keyCount[0]){
                    //Generate the keys when everything is done
                    generateFeed(errands, userLocation);
                }
            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.e(TAG, "Error a=with Geofire ref error: " + error);
            }
        });


    }
}