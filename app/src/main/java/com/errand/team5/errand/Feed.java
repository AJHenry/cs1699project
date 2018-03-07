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


/**
 * A simple {@link Fragment} subclass.
 */
public class Feed extends Fragment {

    private ListView feed;

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

        ArrayList<TaskModel> dataModels= new ArrayList<>();

        dataModels.add(new TaskModel("A", "Coffee Run", "15 mins", 10));
        dataModels.add(new TaskModel("B", "Fold Laundry", "30 mins", 20));

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
