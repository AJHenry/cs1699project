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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class MyTasks extends Fragment {

    private ListView feed;
    private final String TAG = "MyTasks Class";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_tasks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateFeed();
    }

    private void generateFeed() {
        Log.d(TAG, "Generated Feed");
        feed = (ListView) getView().findViewById(R.id.my_task_feed);

        ArrayList<TaskModel> taskList = new ArrayList<>();

        //Create a new drop off location
        Location dropOff = new Location("");
        dropOff.setLongitude(0);
        dropOff.setLatitude(0);

        TaskModel exampleTask = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 30, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask1 = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 45, 10.0f, 1.0f, "Test Task", "Test Description", null, dropOff, null);
        TaskModel exampleTask2 = new TaskModel("A", "TEST", 0, 0, new Timestamp(Calendar.getInstance().get(Calendar.MILLISECOND)), 60, 10.0f, 1.0f, "Burger King Delivery", "I would like someone to pick me up a medium Whopper meal with cheese. Onion rings as the side and Diet Coke as the drink", null, dropOff, null);

        taskList.add(exampleTask);
        taskList.add(exampleTask1);
        taskList.add(exampleTask2);
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