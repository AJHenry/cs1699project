package com.errand.team5.errand;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;


public class Feed extends Fragment {

    private ListView feed;
    private ArrayList<TaskModel> taskList;

    public Feed() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateFeed();
    }

    private void generateFeed(){
        Log.d("F", "FFFFFFFFFFFFF");
        feed=(ListView) getView().findViewById(R.id.task_feed);

        taskList = new ArrayList<>();

        taskList.add(new TaskModel("A", "Coffee Run", "15 mins est.", 10, "I would like a Venti Coffee with 3 cream and 3 sugar", 4.2f));
        taskList.add(new TaskModel("B", "Fold Laundry", "2 hrs est.", 20, "I will provide the detergent and dryer sheets, I need someone to load and fold my laundry", 2.8f));
        taskList.add(new TaskModel("C", "Burger King Delivery", "30 mins est.", 12, "I would like someone to pick me up a medium Whopper meal with cheese. Onion rings as the side and Diet Coke as the drink", 4.6f));

        TaskFeedAdapter adapter= new TaskFeedAdapter(taskList,getView().getContext());

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent task = new Intent(getContext(), Task.class);
                task.putExtra("taskID", taskList.get(position).getTaskID());
                startActivity(task);
            }
        });
    }
}
