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

import java.text.NumberFormat;
import java.time.DayOfWeek;
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
        //==============================================================
        //TODO: below is how I plan to implement requesting a task and some of the setup required in onCreate
        //use the "id" retrieved from intent to fill in the above fields
        thisErrand = errandsTable.child(id);
        //TODO this is mohit's task
        //also use the id to retrieve the creator's userid from the entry in errands
        //this is (-> errands -> <"id"> -> user -> uid) in the firebase
        //this will be needed when we are trying to communicate with the creator of the task
        // to request approval when the request button is pressed.
        thisErrand.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String creatorId = dataSnapshot.child("creatorId").getValue(String.class);
                Log.d("debug", "creatorId = " + creatorId + "; uId = " + mAuth.getUid());
                if(creatorId.equals(mAuth.getUid()))
                {
                    requestButton.setEnabled(false);
                    lock = true;
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        thisErrand.child("status").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                status = dataSnapshot.getValue(Integer.class);
                if(lock)
                {
                    requestButton.setEnabled(false);
                }
                else if(status != 0 && requestButton.isEnabled())
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
            updateUI();
        }
    }
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
                final DatabaseReference userEntry = testUserTable.child(userId);
                DatabaseReference creatorNotificationsTable = creatorEntry.child("notifications");
                DatabaseReference userNotificationTable = userEntry.child("notifications");
                final DatabaseReference creatorNewNotificationRef = creatorNotificationsTable.push();
                final DatabaseReference userNewNotificationRef = userNotificationTable.child(creatorNewNotificationRef.getKey());
                creatorEntry.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String cId = dataSnapshot.child("uid").getValue(String.class);
                        String cPhotoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
                        String cDisplayName = dataSnapshot.child("displayName").getValue(String.class);
                        String cEmail = dataSnapshot.child("email").getValue(String.class);
                        final User creator = new User(cId, cPhotoUrl, cDisplayName, cEmail);
                        userEntry.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                String rId = dataSnapshot.child("uid").getValue(String.class);
                                String rPhotoUrl = dataSnapshot.child("photoUrl").getValue(String.class);
                                String rDisplayName = dataSnapshot.child("displayName").getValue(String.class);
                                String rEmail = dataSnapshot.child("email").getValue(String.class);
                                final User requester = new User(rId, rPhotoUrl, rDisplayName, rEmail);
                                Notification newNotficationCreator = new Notification(creatorNewNotificationRef.getKey(), "This needs approval.", id, requester, creator, Notification.NEEDS_APPROVAL, Notification.OPEN);
                                Notification newNotificationUser = new Notification(userNewNotificationRef.getKey(), "Pending approval from creator.", id, requester, creator, Notification.PENDING_APPROVAL, Notification.OPEN);
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
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void updateUI(){
                            // get the firebase reference
        FirebaseDatabase fb = FirebaseDatabase.getInstance();
                    DatabaseReference table = fb.getReference("errands").child(id); // reference to specific taskID
                    // Now we want to query table multiple times

                    table.addListenerForSingleValueEvent(new ValueEventListener() {

                        public void onDataChange(DataSnapshot data) {
                            TaskModel model = data.getValue(TaskModel.class);
                            taskTitle.setText(model.getTitle());
                            taskCompletionTime.setText(model.getTimeToCompleteMins()+"");
                            taskDescription.setText(model.getDescription());

                            NumberFormat format = NumberFormat.getCurrencyInstance();
                            taskPrice.setText(format.format(model.getBaseCost()));
                            taskTimestamp.setText(getTimeAgo(model.getPublishTime().getTime()));
                            taskSpecialInstructions.setText(model.getSpecialInstructions());
                            dropOffLocation.setText(getCompleteAddressString(model.getDropOffDestination()));

                            ImageView img = (ImageView) findViewById(R.id.task_profile_image);
                            String imgurl = model.getUser().getPhotoUrl();
                            Glide.with(getApplicationContext())
                                    .load(imgurl)
                                    .apply(RequestOptions.circleCropTransform())
                                    .into(img);
                                }

                        @Override
                public void onCancelled(DatabaseError databaseError) {
                                }

                    });

    }

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;



    private String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        //TODO Fix issue with people submitting wrong timestamp
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return "just now";
            //return null;
        }

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

    private String getCompleteAddressString(mLocation location) {
        double LATITUDE = location.getLatitude();
        double LONGITUDE = location.getLongitude();
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
                Log.w("M", strReturnedAddress.toString());
            } else {
                Log.w("M", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("M", "Canont get Address!");
        }
        return strAdd;
    }
}

