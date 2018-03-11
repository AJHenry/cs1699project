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

public class MyTasks extends Fragment {

    private ListView feed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_my_tasks, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        generateFeed();
    }

    private void generateFeed(){
        Log.d("F", "FFFFFFFFFFFFF");
        feed=(ListView) getView().findViewById(R.id.my_task_feed);

        ArrayList<TaskModel> dataModels= new ArrayList<>();

        dataModels.add(new TaskModel("C", "Grocery Shopping", "1 hr est.", 80, "I need someone to get me these ingredients, it does not matter from where. I need 2 onions and a Dr. Pepper 20oz"));

        TaskFeedAdapter adapter= new TaskFeedAdapter(dataModels,getView().getContext());

        feed.setAdapter(adapter);
        feed.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Toast.makeText(getView().getContext(), "Clicked on "+position, Toast.LENGTH_LONG).show();
            }
        });
    }
}