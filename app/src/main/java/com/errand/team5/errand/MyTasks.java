package com.errand.team5.errand;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Queue;

public class MyTasks extends Fragment {

    //Components
    private ListView feed;
    private ProgressBar spinner;

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

        //Components
        spinner = (ProgressBar) getActivity().findViewById(R.id.main_loading);
        feed = (ListView) getActivity().findViewById(R.id.my_task_feed);
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
            //getActivity().finish();
        } else {
            this.user = user;
            updateUI();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        spinner.setVisibility(View.VISIBLE);
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();
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

    /**
     * Shows the data when it is a available
     * @param errandList
     */
    private void generateFeed(final ArrayList<TaskModel> errandList) {
        Log.d(TAG, "Generated Feed");

        //Get rid of the spinner
        spinner.setVisibility(View.GONE);

        final TaskFeedAdapter adapter = new TaskFeedAdapter(errandList, getView().getContext(), null);

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                // display a dialog
                // delete a task, when we click on the task, give an option to delete
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Choose Option")
                        .setPositiveButton("Edit Task", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int otherID) {
                                // TODO show Task.java page on click, giving user option to edit the page
                                // Creates a new Task intent
                                Intent task = new Intent(getActivity(), Task.class);
                                // We can retrieve the task ID at any postion using this code
                                task.putExtra("taskId", errandList.get(position).getTaskId());
                                // starts the task activity
                                startActivity(task);

                                Toast.makeText(getView().getContext(), "It works at this position " + position, Toast.LENGTH_LONG).show();

                            }
                        })
                        .setNegativeButton("Delete Errand", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int ids) {
                                // TODO delete the errand from the firebase
                                String tID = errandList.get(position).getTaskId();
                                FirebaseDatabase fb = FirebaseDatabase.getInstance();
                                DatabaseReference table = fb.getReference("errands").child(tID); // reference to table is correct
                                //Intent intent = getIntent();
                                //String tID = intent.getStringExtra("taskId");
                                Log.i("ShowTaskID", "Task ID = " + tID); // task ID is displayed correctly
                                table.removeValue(); // and it still does not remove?!
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();
                Toast.makeText(getView().getContext(), "Clicked on " + position, Toast.LENGTH_LONG).show();
            }
        });
    }
}