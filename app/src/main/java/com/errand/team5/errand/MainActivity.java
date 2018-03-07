package com.errand.team5.errand;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private ViewFlipper vf;
    //The request code for creating a task
    static final int CREATE_TASK_REQUEST = 1;

    private GoogleSignInClient mGoogleSignInClient;

    //Account for google sign in
    private GoogleSignInAccount account;

    private TextView name;
    private TextView email;

    private ListView feed;


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
                Toast.makeText(this, "Result turned ok", Toast.LENGTH_LONG).show();
            }else{
                //Failure
                Toast.makeText(this, "Result failed", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //We don't want the toolbar showing
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Task Feed");


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_task);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createTask();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //Get the name and text view
        View headerView = navigationView.getHeaderView(0);
        name = (TextView) headerView.findViewById(R.id.name);
        email = (TextView) headerView.findViewById(R.id.email);

        //View Flipper for nav drawer
        vf = (ViewFlipper)findViewById(R.id.main_flipper);

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);




        generateFeed();

    }


    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        GoogleSignInAccount acc = GoogleSignIn.getLastSignedInAccount(this);
        checkLogin(acc);

        if(account != null){
            email.setText(account.getEmail());
            name.setText(account.getDisplayName());
        }
    }

    //Check if their profile is null, if so, redirect them to login
    private void checkLogin(GoogleSignInAccount gsia){
        if(gsia == null){
            Intent login = new Intent(this, Login.class);
            startActivity(login);
        }else{
            account = gsia;
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

        if (id == R.id.nav_open) {
            vf.setDisplayedChild(0);
        } else if (id == R.id.nav_ongoing) {
            vf.setDisplayedChild(1);
        } else if (id == R.id.nav_history) {
            vf.setDisplayedChild(2);
        } else if (id == R.id.nav_settings) {
            vf.setDisplayedChild(3);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void generateFeed(){
        feed=(ListView)findViewById(R.id.task_feed);

        ArrayList<TaskModel> dataModels= new ArrayList<>();

        dataModels.add(new TaskModel("A", "Coffee Run", "15 mins", 10));
        dataModels.add(new TaskModel("B", "Fold Laundry", "30 mins", 20));

        TaskFeedAdapter adapter= new TaskFeedAdapter(dataModels,getApplicationContext());

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getApplicationContext(), "Clicked on"+position, Toast.LENGTH_LONG).show();
            }
        });
    }

}
