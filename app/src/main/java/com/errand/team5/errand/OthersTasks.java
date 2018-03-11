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

        taskList.add(new TaskModel("A", "Math Tutor", "1 hr est.", 15, "I am in need of an Algebra tutor"));
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
