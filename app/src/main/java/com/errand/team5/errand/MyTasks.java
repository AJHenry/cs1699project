package com.errand.team5.errand;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Queue;

public class MyTasks extends Fragment {

    private ListView feed;
    private final String TAG = "MyTasks Class";

    //Firebase auth
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_tasks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // ...
        mAuth = FirebaseAuth.getInstance();
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
        if (user == null) {
            Intent login = new Intent(getContext(), Login.class);
            startActivity(login);
            getActivity().finish();
        } else {
            this.user = user;
            updateUI();
        }
    }

    private void updateUI(){
        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        //Query database for all tasks with creator id of this user
        Query myTasksRef = database.getReference("errands").orderByChild("creatorId").equalTo(user.getUid());

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
                    generateFeed(errands);
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

    private void generateFeed(ArrayList<TaskModel> errandList) {
        Log.d(TAG, "Generated Feed");
        feed = (ListView) getView().findViewById(R.id.my_task_feed);

        /*
        ArrayList<TaskModel> taskList = new ArrayList<>();

        //Create a new drop off location
        mLocation dropOff = new mLocation("");
        dropOff.setLongitude(0);
        dropOff.setLatitude(0);

        TaskModel exampleTask = new TaskModel("A", "TEST", 0, 0, new mTimestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 30, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask1 = new TaskModel("A", "TEST", 0, 0, new mTimestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 45, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask2 = new TaskModel("A", "TEST", 0, 0, new mTimestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 60, 10.0f, 1.0f, "Burger King Delivery", "I would like someone to pick me up a medium Whopper meal with cheese. Onion rings as the side and Diet Coke as the drink", null, dropOff, null);

        taskList.add(exampleTask);
        taskList.add(exampleTask1);
        taskList.add(exampleTask2);
        */
        TaskFeedAdapter adapter = new TaskFeedAdapter(errandList, getView().getContext(), null);

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getView().getContext(), "Clicked on " + position, Toast.LENGTH_LONG).show();
            }
        });
    }
}