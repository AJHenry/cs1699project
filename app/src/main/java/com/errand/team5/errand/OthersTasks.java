package com.errand.team5.errand;


import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class OthersTasks extends Fragment {

    private ListView feed;
    private final String TAG = "OtherTasksClass";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_others_tasks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateFeed();
    }

    private void generateFeed() {
        Log.d(TAG, "Generated Feed");
        feed = (ListView) getView().findViewById(R.id.others_task_feed);

        ArrayList<TaskModel> taskList = new ArrayList<>();

        //Create a new drop off location
        mLocation dropOff = new mLocation(0,0);

        TaskModel exampleTask = new TaskModel("A", "TEST", 0, 0, new mTimestamp(), 30, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null, null);

        taskList.add(exampleTask);
        TaskFeedAdapter adapter = new TaskFeedAdapter(taskList, getView().getContext(), null);

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getView().getContext(), "Clicked on " + position, Toast.LENGTH_LONG).show();
            }
        });
    }
}
