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
    private static final int CONFIRM_UPDATE = 1;
    private static final int CREATE_NEW = 2;
    private static final int EDIT = 3;


    //Save task info
    private TaskData updatedTaskInfo;
    private String taskID;
    private boolean match;
    private boolean alreadyRead = false;

    //delete or no
    private boolean delete = false;


    @Override
    protected void onCreate(Bundle savedInstanceState){

        super.onCreate(savedInstanceState);

        // get current Firebase instances
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        match = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CONFIRM_UPDATE){
            if(resultCode == RESULT_OK){
                //confirmed updates - done
                if(delete){
                    Toast.makeText(this, "entry deleted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "changes saved", Toast.LENGTH_SHORT).show();
                }
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);

            }
            else if(resultCode == RESULT_CANCELED){
                //did not confirm updates - drop to CreateTask to edit
                Toast.makeText(this, "Please make your desired changes", Toast.LENGTH_LONG).show();
                Intent createIntent = new Intent(this, CreateTask.class);
                createIntent.putExtra("taskData", updatedTaskInfo);
                createIntent.putExtra("editOnly", true);
                startActivityForResult(createIntent, EDIT);
            }
        }
        else if(requestCode == CREATE_NEW){
            if(resultCode == RESULT_OK){
                Toast.makeText(this, "new task request saved", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
            else{
                Toast.makeText(this, "request canceled", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        else if(requestCode == EDIT){
            if(resultCode == RESULT_OK){
                //send back to confirmation screen with new data
                Bundle extras = data.getExtras();
                try{
                    Toast.makeText(this, "request canceled", Toast.LENGTH_SHORT).show();
                    TaskData taskData = (TaskData) extras.getSerializable("passBack");
                    Intent confirmIntent = new Intent(this, UpdateConfirm.class);
                    confirmIntent.putExtra("taskData", taskData);
                    confirmIntent.putExtra("delete", delete);
                    confirmIntent.putExtra("taskID", taskID);
                    startActivityForResult(confirmIntent, CONFIRM_UPDATE);
                }
                catch(NullPointerException e){
                    Log.wtf(TAG, "ERROR GETTING taskData info, please contact help");
                }
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
        delete = intent.getBooleanExtra("delete", false);

        // Get all fields from UpdateReceiver's data
        final TaskData taskData;
        try {
            if ((taskData = (TaskData) extras.getSerializable("taskData")) != null) {
                //save task data
                updatedTaskInfo = taskData;
                //Query DB for a matching task (just check title and user ID)
                final String title = taskData.getTitle();
                Log.wtf(DEBUG, "Task title from bundle: " + title);
                Log.wtf(DEBUG, "User ID from current instance: " + user.getUid());
                //Query database for all tasks with creator id of this user
                Query userTasks = db.getReference("errands").orderByChild("creatorId").equalTo(user.getUid());
                userTasks.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(!alreadyRead) {
                            for (DataSnapshot ds : dataSnapshot.getChildren()) {
                                TaskModel errand = ds.getValue(TaskModel.class);
                                String taskKey = ds.getKey();
                                Log.wtf(DEBUG, "Looping - task title from DB: " + errand.getTitle());
                                if (errand.getTitle().equals(title)) {
                                    // we have a match - send bundle along to UpdateConfirm
                                    Log.wtf(DEBUG, "Found a match: " + errand.getTitle());
                                    taskID = taskKey;
                                    match = true;
                                    alreadyRead = true;
                                    break;
                                }
                            }
                            if (match) {
                                Intent confirmIntent = new Intent(getApplicationContext(), UpdateConfirm.class);
                                confirmIntent.putExtra("taskData", taskData);
                                confirmIntent.putExtra("delete", delete);
                                confirmIntent.putExtra("taskID", taskID);
                                startActivityForResult(confirmIntent, CONFIRM_UPDATE);
                            } else {
                                //no matching task - send user to CreateTask and populate fields
                                Toast.makeText(getApplicationContext(), "Didn't find matching task", Toast.LENGTH_SHORT).show();
                                Intent createIntent = new Intent(getApplicationContext(), CreateTask.class);
                                createIntent.putExtra("taskData", taskData);
                                startActivityForResult(createIntent, CREATE_NEW);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        //we're screwed
                        Toast.makeText(getApplicationContext(), "Error reading Errands from Firebase", Toast.LENGTH_LONG).show();
                    }
                });

                //either have user confirm changes, or send them to CreateTask
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
