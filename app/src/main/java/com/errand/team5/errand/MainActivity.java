package com.errand.team5.errand;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewFlipper vf;
    //The request code for creating a task
    static final int CREATE_TASK_REQUEST = 1;

    //The result code for loggin in
    private final int SIGN_IN = 10101;

    //The request code for location permissions
    private final int LOCATION_PERMISSION = 99;

    //Debugging
    private final String TAG = "MainActivity";


    private FirebaseAuth mAuth;

    private FirebaseUser user;


    private TextView name;
    private TextView email;
    private ImageView profileImage;
    private ProgressBar loadingBar;

    private ListView feed;

    //mLocation data variables
    //See here for more details
    //https://github.com/codepath/android_guides/wiki/Retrieving-Location-with-LocationServices-API
    private Location lastKnownLocation = null;

    private boolean locationReady = false;
    private boolean accountReady = false;


    /**
     * Used for issuing a startActivityResult to create a Task
     */
    private void createTask() {
        Intent intent = new Intent(this, CreateTask.class);
        startActivityForResult(intent, CREATE_TASK_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == CREATE_TASK_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                //Successfully create task
                Toast.makeText(this, "Result turned ok, update feed", Toast.LENGTH_LONG).show();
            } else {
                //Failure
                Toast.makeText(this, "Result failed", Toast.LENGTH_LONG).show();
            }
        } else if (requestCode == SIGN_IN) {
            fillUserUI(mAuth.getCurrentUser());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar options
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Task Feed");

        //Create Button
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_task);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkPermissions()) {
                    createTask();
                } else {
                    Toast.makeText(getApplicationContext(), "Permissions needed in order to create a task", Toast.LENGTH_LONG).show();
                }
            }
        });

        //Drawer
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Components
        View headerView = navigationView.getHeaderView(0);
        name = (TextView) headerView.findViewById(R.id.name);
        email = (TextView) headerView.findViewById(R.id.email);
        profileImage = (ImageView) headerView.findViewById(R.id.profile_image);


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        // ...
        mAuth = FirebaseAuth.getInstance();

        //Check for the internet
        if(!isConnectedToInternet()){
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.main_layout), "Please check your connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateUI();
                        }
                    });
            snackbar.show();
        }

        checkPermissions();
    }

    private void updateUI() {
        //TODO Show a loading spinner until this is all ready

        //Check for the internet
        if(!isConnectedToInternet()){
            Snackbar snackbar = Snackbar
                    .make(findViewById(R.id.main_layout), "Please check your connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            updateUI();
                        }
                    });
            snackbar.show();
            return;
        }

        //Check permissions, account, and location
        if (!accountReady || !checkPermissions() || !locationReady) {
            return;
        }

        //Lets get rid of the loading spinner

        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;

        fragmentClass = Feed.class;

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit();
    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        checkLogin(currentUser);
    }

    //Fills in the user name and email and picture
    private void fillUserUI(FirebaseUser currentUser) {
        //Set the email and name in the drawer
        email.setText(currentUser.getEmail());
        name.setText(currentUser.getDisplayName());

        String imgurl = currentUser.getPhotoUrl().toString();
        Glide.with(this).load(imgurl).apply(RequestOptions.circleCropTransform()).into(profileImage);
    }


    @Override
    public void onResume() {
        //Start the location service
        startLocationService();
        super.onResume();
    }

    @Override
    public void onPause() {
        //Stop location service
        stopLocation();
        super.onPause();
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(FirebaseUser user) {
        if (user == null) {
            Intent login = new Intent(this, Login.class);
            startActivityForResult(login, SIGN_IN);
        } else {
            this.user = user;
            accountReady = true;
            Log.d(TAG, "Acount ready = " +accountReady);
            fillUserUI(user);
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        // Create a new fragment and specify the fragment to show based on nav item clicked
        Fragment fragment = null;
        Class fragmentClass = null;

        if (id == R.id.nav_open) {
            fragmentClass = Feed.class;
        } else if (id == R.id.nav_ongoing) {
            fragmentClass = ActiveTasks.class;
        } else if (id == R.id.nav_history) {
            fragmentClass = History.class;
        } else if (id == R.id.nav_account) {
            fragmentClass = Account.class;
        } else if (id == R.id.nav_settings) {
            fragmentClass = Settings.class;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.main_layout, fragment).commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    /* PERMISSIONS CHECK */
    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //Start the location service
                    startLocationService();

                } else {

                    Snackbar snackbar = Snackbar
                            .make(findViewById(R.id.main_layout), "Location permissions needed to use this app", Snackbar.LENGTH_INDEFINITE)
                            .setAction("RETRY", new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    checkPermissions();
                                }
                            });
                    snackbar.show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request.
        }
    }

    /**
     * mLocation Service
     * Calls the updateUI method
     */
    private void startLocationService() {
        Log.d(TAG, "Started location service");

        long mLocTrackingInterval = 1000 * 5; // 5 sec
        float trackingDistance = 0;
        LocationAccuracy trackingAccuracy = LocationAccuracy.HIGH;

        LocationParams.Builder builder = new LocationParams.Builder()
                .setAccuracy(trackingAccuracy)
                .setDistance(trackingDistance)
                .setInterval(mLocTrackingInterval);

        SmartLocation.with(this)
                .location()
                //.continuous()
                .oneFix()
                .config(builder.build())
                .start(new OnLocationUpdatedListener() {
                    @Override
                    public void onLocationUpdated(Location location) {
                        Log.d(TAG, "Updated location");
                        lastKnownLocation = location;
                        Log.d(TAG, "Lon: " + lastKnownLocation.getLongitude() + " Lat: " + lastKnownLocation.getLatitude());
                        //mLocation is ready to be used
                        locationReady = true;
                        Log.d(TAG, "Location ready = " +locationReady);
                        updateUI();
                    }
                });
    }

    private void stopLocation() {
        Log.d(TAG, "Stopped location service");
        SmartLocation.with(this)
                .location()
                .stop();
    }

    private boolean isConnectedToInternet() {
        ConnectivityManager cm =
                (ConnectivityManager)getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

}
