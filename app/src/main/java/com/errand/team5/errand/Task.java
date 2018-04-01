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

    private FirebaseAuth mAuth;
    private FirebaseUser user;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_task);

            Intent intent = getIntent();
            String id = intent.getStringExtra("taskId");

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
}
