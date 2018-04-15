package com.errand.team5.errand;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Notifications extends AppCompatActivity {

    private Context context;
    private ListView notis;
    private FirebaseAuth mAuth;
    private FirebaseUser user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Firebase instance
        context = this;
        mAuth = FirebaseAuth.getInstance();

        //Components
        //TODO THIS WILL CAUSE AN ERROR, sometimes if the navigates away from the view when firebase tries to fill it
        //Maybe fixed, someone else confirm
        notis = (ListView) findViewById(R.id.listview_noti);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);

        updateUI();

    }

    private void generateFeed(final ArrayList<com.errand.team5.errand.Notification> notiList)
    {

        NotificationAdapter adapter = new NotificationAdapter(notiList, context);

        notis.setAdapter(adapter);
    }

    private void updateUI(){
        //TODO decide when to update

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Query database for all tasks with creator id of this user
        DatabaseReference myNotiRef = database.getReference("testUsers").child(user.getUid()).child("notifications");

        myNotiRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    ArrayList<com.errand.team5.errand.Notification> notifications = new ArrayList<>();
                    // dataSnapshot is the "issue" node with all children with id 0
                    for (DataSnapshot notiMap : dataSnapshot.getChildren()) {
                        //Add the errand to the list
                        com.errand.team5.errand.Notification noti = notiMap.getValue(com.errand.team5.errand.Notification.class);
                        notifications.add(noti);
                    }
                    generateFeed(notifications);
                }else{
                    //TODO no data found for the user
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Should not happen
                Toast.makeText(context, "Error reading Notifications from Firebase", Toast.LENGTH_LONG).show();
            }
        });
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(context, Login.class);
            startActivity(login);
        } else {
            this.user = user;
        }
    }

    @Override
    public void onResume() {
        try {
            getSupportActionBar().setTitle("Notifications");
        }catch (NullPointerException e){

        }
        super.onResume();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
