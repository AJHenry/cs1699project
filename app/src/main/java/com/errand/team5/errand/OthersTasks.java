package com.errand.team5.errand;


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

public class OthersTasks extends Fragment {

    private ListView feed;

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

    private void generateFeed(){
        Log.d("F", "FFFFFFFFFFFFF");
        feed=(ListView) getView().findViewById(R.id.others_task_feed);

        ArrayList<TaskModel> taskList= new ArrayList<>();

        taskList.add(new TaskModel("A", "Coffee Run", "15 mins est.", 10, "I would like a Venti Coffee with 3 cream and 3 sugar"));
        taskList.add(new TaskModel("B", "Fold Laundry", "2 hrs estimated", 20, "I will provide the deterget and dryer sheets, I need someone to load and fold my laundry"));

        TaskFeedAdapter adapter= new TaskFeedAdapter(taskList,getView().getContext());

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getView().getContext(), "Clicked on "+position, Toast.LENGTH_LONG).show();
            }
        });
    }
}
