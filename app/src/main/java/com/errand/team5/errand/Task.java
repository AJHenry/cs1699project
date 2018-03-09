package com.errand.team5.errand;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;



public class Task extends AppCompatActivity implements OnMapReadyCallback {

    private ListView taskComments;
    private TextView taskTitle;
    private TextView taskCompletionTime;
    private TextView taskDescription;
    private TextView taskPrice;
    private EditText taskComment;
    private Button taskCommentButton;
    private GoogleMap mMap;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_task);

            //Show the back button
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            Intent intent = getIntent();
            String taskID = intent.getStringExtra("taskID");

            taskComments = (ListView) findViewById(R.id.task_comments);
            taskTitle = (TextView) findViewById(R.id.task_title);
            taskCompletionTime = (TextView) findViewById(R.id.task_completion_time);
            taskDescription = (TextView) findViewById(R.id.task_description);
            taskPrice = (TextView) findViewById(R.id.task_price);
            taskComment = (EditText) findViewById(R.id.task_comment);
            taskCommentButton = (Button) findViewById(R.id.task_comment_button);

            taskTitle.setText(taskID);

            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney, Australia, and move the camera.
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }


}
