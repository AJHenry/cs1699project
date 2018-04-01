package com.errand.team5.errand;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.location.Location;
import android.widget.ProgressBar;
import android.widget.Toast;

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

    private ListView feed;
    private ArrayList<TaskModel> taskList;
    private Location lastKnownLocation;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private ProgressBar spinner;
    private boolean userReady;
    private boolean locationReady;

    private final String TAG = "FeedClass";

    private DatabaseReference myRef;


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
        spinner = (ProgressBar) getView().findViewById(R.id.spinner);

        //TODO THIS WILL CAUSE AN ERROR, sometimes
        feed = (ListView) getView().findViewById(R.id.task_feed);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    //Check if their profile is null, if so, redirect them to login
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
        Log.d(TAG, "onResume");
        //start spinner
        spinner.setVisibility(View.VISIBLE);
        startLocationService();
        //end spinner
        spinner.setVisibility(View.GONE);
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
                        Log.d(TAG, "Lon: "+lastKnownLocation.getLongitude()+" Lat: "+lastKnownLocation.getLatitude());

                        updateUI(location);
                    }
                });
    }

    private void stopLocation(){
        SmartLocation.with(getActivity())
                .location()
                .stop();
    }

    private void generateFeed(final ArrayList<TaskModel> errandList, Location location) {
        Log.d(TAG, "Generated feed for home screen");


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
    }

    private void updateUI(final Location location){
        //TODO decide when to update

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Query database for all tasks with creator id of this user
        DatabaseReference myTasksRef = database.getReference("errands");

        myTasksRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    ArrayList<TaskModel> errands = new ArrayList<>();
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot errandMap : dataSnapshot.getChildren()) {
                        //Add the errand to the list
                        TaskModel errand = errandMap.getValue(TaskModel.class);
                        errands.add(errand);
                    }
                    generateFeed(errands, location);
                }else{
                    //TODO no data found for the user
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Should not happen
                Toast.makeText(getContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
            }
        });
    }
}