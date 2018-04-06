package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;


public class Task extends AppCompatActivity {

    private ListView taskComments;
    private TextView taskTitle;
    private TextView taskCompletionTime;
    private TextView taskDescription;
    private TextView taskPrice;
    private TextView taskTimestamp;
    private TextView taskSpecialInstructions;
    private Button dropOffLocation;
    private ImageView profileImage;
    private String id;
    private String creatorId;

    private FirebaseAuth mAuth;
    private FirebaseUser user;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_task);

            Intent intent = getIntent();
            id = intent.getStringExtra("taskId");

            //Show the back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            // ...
            mAuth = FirebaseAuth.getInstance();

            taskTitle = (TextView) findViewById(R.id.task_title);
            taskCompletionTime = (TextView) findViewById(R.id.task_completion_time);
            taskDescription = (TextView) findViewById(R.id.task_description);
            taskPrice = (TextView) findViewById(R.id.task_price);
            taskTimestamp = (TextView) findViewById(R.id.task_timestamp);
            taskSpecialInstructions = (TextView) findViewById(R.id.task_special_instructions);
            dropOffLocation = (Button) findViewById(R.id.task_drop_off_button);
            profileImage = (ImageView) findViewById(R.id.task_profile_image);

            //==============================================================
            //TODO: below is how I plan to implement requesting a task and some of the setup required in onCreate
            //use the "id" retrieved from intent to fill in the above fields

            //also use the id to retrieve the creator's userid from the entry in errands
                //this is (-> errands -> <"id"> -> user -> uid) in the firebase
                //this will be needed when we are trying to communicate with the creator of the task
                // to request approval when the request button is pressed.

            creatorId = ""; //store the creator's id in this variable for later use

            //set request button to disabled if the user status indicates they are already on a task
                //still need to add this attribute to User



            //==============================================================




            //Set the user profile picture
            //String imgurl = currentUser.getPhotoUrl().toString();
            //Glide.with(this).load(imgurl).apply(RequestOptions.circleCropTransform()).into(profileImage);

            profileImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showProfile();
                }
            });
        }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);

    }
    
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    public void showProfile(){
        final Dialog dialog = new Dialog(this);
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.summary);

        //Do processing here

        dialog.show();
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        // TODO Fix error where application closes after first login
        if (user == null) {
            Intent login = new Intent(this, Login.class);
            startActivityForResult(login, SIGN_IN);
        } else {
            this.user = user;
        }
    }

    private void onRequest(View view)
    {
        //TODO: implement requesting Task

        //check that the task is still available in the DB using status
            //must be robust to users taking the task while we are looking at the details
            //if taken, Toast the user that it is taken and return to Feed

        //If not taken:
            //notify the creator that they have a pending request
                //multiple options here:
                    //1. When users login and enter the app, request permission to use push notifications
                    //   This uses Google Cloud Messaging through firebase
                        //documentation: https://developers.google.com/cloud-messaging/android/client
                    //   In essence, you request permission to use push notifications through GMS and when they
                    //   you can get a registration token specific to their devices instance of the app and can
                    //   use that token to send them a notification directly. We could save this token on first
                    //   login in the user's User object that is loaded into the firebase users table

                    //2. Have a notification page that users can navigate to view a list of their notifications.
                        //This would require that we have a way of tagging relevant parties in each notification
                        // and only populate their listview with those notifications tagged for them
                        // Likely this would be easier in the context of what we already have done
                        // but requires a lot of work in the DB and querying to make it work properly.
                            //one approach in DB implementation would be having a notifications table parallel to the users
                            //   and errands tables that hold all notifications with tags as described above
                            //alternatively we could hold a notifications table as a child of each user in the firebase as well
                            //   however, this might cause issues when trying to pull a user from the firebase into a User object
                            //   because it wouldn't have a corresonding field in the User object
                            //          although this may be possible to include as another Notification opbject nested inside the User
                            //          object. But I'm not entirely sure that is possible.

    }
}
