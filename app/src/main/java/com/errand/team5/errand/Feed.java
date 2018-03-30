package com.errand.team5.errand;


import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

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

        mAuth = FirebaseAuth.getInstance();

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        myRef = database.getReference("users");

        //GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(getActivity());

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(getContext(), Login.class);
            startActivity(login);
            getActivity().finish();
        } else {
            this.user = user;
        }
    }

    private void updateUI(){
        //Example RULE SET
        myRef.child(user.getUid()).setValue("Hello, World!");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
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
     * Location Service
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
                        updateUI(lastKnownLocation);
                    }
                });
    }

    private void stopLocation(){
        SmartLocation.with(getActivity())
                .location()
                .stop();
    }

    private void generateFeed(Location location) {
        Log.d(TAG, "Generated feed");
        feed = (ListView) getView().findViewById(R.id.task_feed);

        taskList = new ArrayList<>();

        //Create a new drop off location
        Location dropOff = new Location("");
        dropOff.setLongitude(-125.076);
        dropOff.setLatitude(37.986);

        TaskModel exampleTask = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 30, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask1 = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 45, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask2 = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 60, 10.0f, 1.0f, "Burger King Delivery", "I would like someone to pick me up a medium Whopper meal with cheese. Onion rings as the side and Diet Coke as the drink", null, dropOff, null);

        taskList.add(exampleTask);
        taskList.add(exampleTask1);
        taskList.add(exampleTask2);

        //taskList.add(new TaskModel("A", "Coffee Run", "15 mins est.", 10, "I would like a Venti Coffee with 3 cream and 3 sugar", 4.2f));
        //taskList.add(new TaskModel("B", "Fold Laundry", "2 hrs est.", 20, "I will provide the detergent and dryer sheets, I need someone to load and fold my laundry", 2.8f));
        //taskList.add(new TaskModel("C", "Burger King Delivery", "30 mins est.", 12, "I would like someone to pick me up a medium Whopper meal with cheese. Onion rings as the side and Diet Coke as the drink", 4.6f));

        TaskFeedAdapter adapter = new TaskFeedAdapter(taskList, getView().getContext(), location);

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent task = new Intent(getContext(), Task.class);
                task.putExtra("taskID", taskList.get(position).getTaskId());
                startActivity(task);
            }
        });
    }

    private void updateUI(Location location){
        //TODO decide when to update
        generateFeed(location);
    }
}