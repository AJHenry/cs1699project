package com.errand.team5.errand;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

/**
 * Added by Austin M, 4/15/18
 *
 * Receives task fields from UpdateReceiver, finds matching task,
 * replaces existing fields with all non-null input fields
 */

public class UpdateTask extends AppCompatActivity {

    //Firebase refs
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseDatabase db;

    //Log tag
    private String TAG = "UpdateTask class";
    private String DEBUG = "DEBUG: UpdateTask: ";

    //Request code
    static final int CONFIRM_UPDATE = 1;

    //Save task info
    private TaskData updatedTaskInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        // get current Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if(requestCode == CONFIRM_UPDATE){
            if(resultCode == RESULT_OK){
                //overwrite DB with new task (only defined fields)
            }
            else{
                Toast.makeText(this, "Please make your changes here and submit", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        FirebaseUser fUser = mAuth.getCurrentUser();
        checkLogin(fUser);

        // unpack UpdateReceiver's data
        Intent intent = getIntent();
        final Bundle extras = intent.getExtras();
        final boolean delete = intent.getBooleanExtra("delete", false);

        // Get all fields from UpdateReceiver's data
        final TaskData taskData;
        try {
            if ((taskData = (TaskData) extras.getSerializable("taskData")) != null) {
                //Query DB for a matching task (just check title and user ID)
                final String title = taskData.getTitle();
                Log.wtf(DEBUG, "Task title from bundle: " + title);
                Log.wtf(DEBUG, "User ID from current instance: " + user.getUid());
                //Query database for all tasks with creator id of this user
                Query userTasks = db.getReference("errands").orderByChild("creatorId").equalTo(user.getUid());
                userTasks.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean matchFound = false;
                        for(DataSnapshot ds: dataSnapshot.getChildren()){
                            TaskModel errand = ds.getValue(TaskModel.class);
                            Log.wtf(DEBUG, "Looping - task title from DB: " + errand.getTitle());
                            if(errand.getTitle().equals(title)){
                                // we have a match - send bundle along to UpdateConfirm,
                                Intent confirmIntent = new Intent(getApplicationContext(), UpdateConfirm.class);
                                confirmIntent.putExtra("taskData", taskData);
                                confirmIntent.putExtra("delete", delete);
                                startActivityForResult(confirmIntent, CONFIRM_UPDATE);
                                matchFound = true;
                                break;
                            }
                        }
                        if(!matchFound){
                            //no matching task - send user to CreateTask and populate fields
                            Toast.makeText(getApplicationContext(), "Didn't find matching task", Toast.LENGTH_SHORT).show();
                            Intent createIntent = new Intent(getApplicationContext(), CreateTask.class);
                            createIntent.putExtra("taskData", taskData);
                            startActivity(createIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //we're screwed
                        Toast.makeText(getApplicationContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
                    }
                });

            }
        }catch (NullPointerException e) {
            Log.wtf(TAG, "ERROR GETTING taskData info, please contact help");
        }
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
            Log.wtf(DEBUG, "Current user: " + fUser.getUid());
            this.user = fUser;
        }
    }

}
