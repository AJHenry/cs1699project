package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.auth.api.credentials.CredentialPickerConfig.Prompt.SIGN_IN;


public class editTask extends AppCompatActivity {

    private ListView taskComments;
    private TextView taskTitle;
    private TextView taskCompletionTime;
    private EditText taskDescription;
    private EditText taskPrice;
    private TextView taskTimestamp;
    private EditText taskSpecialInstructions;
    private Button dropOffLocation;
    private ImageView profileImage;

    private String title = "";
    private String time = "";
    private String descriptor = "";
    private double price = 0.0;
    private long timeStamp = 0;
    private String instructions = "";
    private String locat = ""; // location
    private String comments = "";
    private double lon = 0.0; // longitude
    private double lat = 0.0; // latitude
    private String id;

    private int status;

    private DatabaseReference db;
    private DatabaseReference errandsTable;
    private DatabaseReference testUserTable;
    private DatabaseReference thisErrand;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private boolean lock;

    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        id = intent.getStringExtra("idForTask");

        //Show the back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // ...
        context = this;
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance().getReference();
        errandsTable = db.child("errands");
        testUserTable = db.child("testUsers");
        lock = false;

        taskTitle = (TextView) findViewById(R.id.task_title);
        taskCompletionTime = (TextView) findViewById(R.id.task_completion_time);
        taskDescription = (EditText) findViewById(R.id.task_description);
        taskPrice = (EditText) findViewById(R.id.task_price);
        taskTimestamp = (TextView) findViewById(R.id.task_timestamp);
        taskSpecialInstructions = (EditText) findViewById(R.id.task_special_instructions);
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

        // get the firebase reference
        FirebaseDatabase fb = FirebaseDatabase.getInstance();
        DatabaseReference table = fb.getReference("errands").child(id); // reference to specific taskID
        // Now we want to query table multiple times

        // gets the task title
        DatabaseReference getTitle = table.child("title");
        getTitle.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                title = (String) data.getValue();
                taskTitle.setText(title);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the task completion time
        DatabaseReference getTime = table.child("timeToCompleteMins");
        getTime.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                long time1 = (long) data.getValue();
                time = time1+"";
                taskCompletionTime.setText(time);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the task description
        DatabaseReference getDescriptor = table.child("description");
        getDescriptor.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                descriptor = (String) data.getValue();
                taskDescription.setText(descriptor);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the task price
        DatabaseReference getPrice = table.child("baseCost");
        getPrice.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                price = Double.parseDouble(data.getValue().toString());
                String price1 = price + "";
                taskPrice.setText(price1);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the timeStamp "posted x minutes ago" (calculates the x)
        DatabaseReference getPublish = table.child("publishTime");
        DatabaseReference getTimestamp = getPublish.child("time");
        getTimestamp.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                timeStamp = (long) data.getValue();
                String timePost = getElapsedTime(timeStamp);
                taskTimestamp.setText(timePost);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the special instructions
        DatabaseReference getInstructions = table.child("specialInstructions");
        getInstructions.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                instructions = (String) data.getValue();
                taskSpecialInstructions.setText(instructions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // gets the location's longitude and latitude
        DatabaseReference getLocationAttrs = table.child("dropOffDestination");
        DatabaseReference getLongitude = getLocationAttrs.child("longitude");
        getLongitude.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                lon = Double.parseDouble(data.getValue().toString());
                Log.i("Longitude", "Here: " + lon);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });
        DatabaseReference getLat = getLocationAttrs.child("latitude");
        getLat.addValueEventListener(new ValueEventListener() {

            public void onDataChange(DataSnapshot data) {
                lat = Double.parseDouble(data.getValue().toString());
                Log.i("Latitude", "Here: " + lat);
                String addy = getCompleteAddressString(lat, lon);
                Log.i("getCompleteAddress", "address = " + addy);
                dropOffLocation.setText(addy);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }

        });

        // now that we have the longitude and the latitude
        // we need to retrieve the location address
//            Log.i("Latitude", "Here: " + lat);
//            Log.i("Longitude", "Here: " + lon);
//            String addy = getCompleteAddressString(lat, lon);
//            Log.i("getCompleteAddress", "address = " + addy);
//            dropOffLocation.setText(addy);

    }

    // Added Helper Methods Here

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

    public long[] printDifference(Date startDate, Date endDate){

        //milliseconds
        long different = endDate.getTime() - startDate.getTime();

        System.out.println("startDate : " + startDate);
        System.out.println("endDate : "+ endDate);
        System.out.println("different : " + different);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = different / daysInMilli;
        different = different % daysInMilli;

        long elapsedHours = different / hoursInMilli;
        different = different % hoursInMilli;

        long elapsedMinutes = different / minutesInMilli;
        different = different % minutesInMilli;

        long elapsedSeconds = different / secondsInMilli;

        long [] arr = {elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds};
        return arr;

//        System.out.printf(
//                "%d days, %d hours, %d minutes, %d seconds%n",
//                elapsedDays,
//                elapsedHours, elapsedMinutes, elapsedSeconds);

    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    private String getElapsedTime(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        //TODO Fix issue with people submitting wrong timestamp
        long now = System.currentTimeMillis();
//        if (time > now || time <= 0) {
//            Log.d(TAG, "Error in timestamp");
//            Log.d(TAG, "Given: "+time);
//            Log.d(TAG, "Actual: "+now);
//            return "just now";
//            //return null;
//        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }


    // taken from the link:
    // https://stackoverflow.com/questions/9409195/how-to-get-complete-address-from-latitude-and-longitude
    // Gets the complete address by using the latitude and the longitude
    // of a certain location.
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("My Current loction addy", strReturnedAddress.toString());
            } else {
                Log.w("My Current loction addy", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("My Current loction addy", "Canont get Address!");
        }
        return strAdd;
    }

    public void onUpdateTask(View view) {
        taskDescription = (EditText) findViewById(R.id.task_description);
        taskPrice = (EditText) findViewById(R.id.task_price);
        taskSpecialInstructions = (EditText) findViewById(R.id.task_special_instructions);

        // might have to change these
        String description = taskDescription.getText().toString();
        String price = taskPrice.getText().toString();
        String instructions = taskSpecialInstructions.getText().toString();

        int time1 = Integer.parseInt(time);
        double price1 = Double.parseDouble(price);
        // end of having to change


        // start updating the table
        FirebaseDatabase fb = FirebaseDatabase.getInstance();
        DatabaseReference table = fb.getReference("errands").child(id); // gets reference to current id
        table.child("baseCost").setValue(price1);
        table.child("specialInstructions").setValue(instructions);
        table.child("description").setValue(description);
        // End update for the table
        Toast.makeText(this, "Successfully Updated", Toast.LENGTH_LONG).show();
    }
}
