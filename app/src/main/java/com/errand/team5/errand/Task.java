package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import com.google.firebase.database.ValueEventListener;

import java.time.DayOfWeek;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private Button requestButton;
    private ImageView profileImage;
    private Button editTaskButton;
    private String id;
    private boolean lock;

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

    private int status;

    private DatabaseReference db;
    private DatabaseReference errandsTable;
    private DatabaseReference testUserTable;
    private DatabaseReference thisErrand;
    private FirebaseAuth mAuth;
    private FirebaseUser user;

    private Context context;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_task);


            Intent intent = getIntent();
            id = intent.getStringExtra("taskId");

            context = this;

            //Show the back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            // Configure sign-in to request the user's ID, email address, and basic
            // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
            // ...
            mAuth = FirebaseAuth.getInstance();
            db = FirebaseDatabase.getInstance().getReference();
            errandsTable = db.child("errands");
            testUserTable = db.child("testUsers");
            lock = false;

            taskTitle = (TextView) findViewById(R.id.task_title);
            taskCompletionTime = (TextView) findViewById(R.id.task_completion_time);
            taskDescription = (TextView) findViewById(R.id.task_description);
            taskPrice = (TextView) findViewById(R.id.task_price);
            taskTimestamp = (TextView) findViewById(R.id.task_timestamp);
            taskSpecialInstructions = (TextView) findViewById(R.id.task_special_instructions);
            dropOffLocation = (Button) findViewById(R.id.task_drop_off_button);
            profileImage = (ImageView) findViewById(R.id.task_profile_image);
            requestButton = (Button) findViewById(R.id.button_request);
            editTaskButton = (Button) findViewById(R.id.editTaskButton);
            editTaskButton.setEnabled(false);

            //==============================================================
            //TODO: below is how I plan to implement requesting a task and some of the setup required in onCreate
            //use the "id" retrieved from intent to fill in the above fields
            thisErrand = errandsTable.child(id);

            //also use the id to retrieve the creator's userid from the entry in errands
                //this is (-> errands -> <"id"> -> user -> uid) in the firebase
                //this will be needed when we are trying to communicate with the creator of the task
                // to request approval when the request button is pressed.

            thisErrand.child("status").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    status = dataSnapshot.getValue(Integer.class);
                    if(status != 0 && requestButton.isEnabled())
                    {
                        Toast.makeText(context, "Sorry, this task is no longer available :(", Toast.LENGTH_LONG).show();
                        requestButton.setEnabled(false);
                    }
                    else if(status == 0 && requestButton.isEnabled() == false)
                    {
                        requestButton.setEnabled(true);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            }); //this disables the request button if the errand is set into a non-requestable state while the user is viewing the page. also does the opposite.

            // edit task does not work at all, probably because I did not put in my credit card
            // or paypal information. It was working perfectly fine before the paypal integration. -- Mohit
            /*
            thisErrand.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String creatorId = dataSnapshot.child("creatorId").getValue(String.class);
                    Log.d("debug", "creatorId = " + creatorId + "; uId = " + mAuth.getUid());
                    if(creatorId.equals(mAuth.getUid()))
                    {
                        requestButton.setEnabled(false);
                        editTaskButton.setEnabled(true);
                        editTaskButton.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // starts the edit activity task
                                Intent startEditing = new Intent(Task.this, editTask.class);
                                startEditing.putExtra("idForTask", id);
                                startActivity(startEditing);
                            }
                        });
                        lock = true;
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            }); */



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

            requestButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    onRequest();
                }
            });

            // Added a way to query from the database (Mohit)
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
            // Finished Querying from the database (Mohit)


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

    // Added Helper Functions (Mohit)

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

    // Ended Adding Helper Function(Mohit)

    private void onRequest()
    {
        //TODO: implement requesting Task

        //check that the task is still available in the DB using status
            //must be robust to users taking the task while we are looking at the details
            //if taken, Toast the user that it is taken and return to Feed


        //TODO Require paypal login here (then on success do the following)
        thisErrand.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //get creatorId and current userId then for each of them, in their user table, add the notification to their notification table
                String creatorId = dataSnapshot.child("creatorId").getValue(String.class);
                String userId = user.getUid();

                //load the notfications into the users notifications table
                DatabaseReference creatorEntry = testUserTable.child(creatorId);
                DatabaseReference userEntry = testUserTable.child(userId);

                DatabaseReference creatorNotificationsTable = creatorEntry.child("notifications");
                DatabaseReference userNotificationTable = userEntry.child("notifications");

                DatabaseReference creatorNewNotificationRef = creatorNotificationsTable.push();
                DatabaseReference userNewNotificationRef = userNotificationTable.push();

                Notification newNotficationCreator = new Notification(creatorNewNotificationRef.getKey(), "This needs approval.", id, Notification.NEEDS_APPROVAL, Notification.OPEN);
                Notification newNotificationUser = new Notification(userNewNotificationRef.getKey(), "Pending approval from creator.", id, Notification.PENDING_APPROVAL, Notification.OPEN);

                creatorNewNotificationRef.setValue(newNotficationCreator);
                userNewNotificationRef.setValue(newNotificationUser);

                thisErrand.child("status").setValue(1);
                requestButton.setEnabled(false);
                Toast.makeText(context, "Your request is pending approval. See notifications page to know when you have been accepted or declined.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(context, MainActivity.class);
                //put any extras if needed
                startActivity(intent);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

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
