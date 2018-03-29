package com.errand.team5.errand;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RatingBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Andrew on 3/7/2018.
 *
 * Template for displaying tasks in a list view
 */

public class TaskFeedAdapter extends ArrayAdapter<TaskModel> {

    private Context mContext;
    private ArrayList<TaskModel> dataSet;
    private Location location;

        // View lookup cache
        private static class ViewHolder {
            TextView title;
            TextView distance;
            TextView duration;
            TextView price;
            TextView time;
            TextView description;
            RatingBar creatorRating;
        }

        public TaskFeedAdapter(ArrayList<TaskModel> data, Context context, Location currentLocation) {
            super(context, R.layout.listview_feed, data);
            this.dataSet = data;
            this.mContext=context;
            this.location = currentLocation;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TaskModel task = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());

                //Get all the components we need to modify
                convertView = inflater.inflate(R.layout.listview_feed, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.description = (TextView) convertView.findViewById(R.id.instructions);
                viewHolder.creatorRating = (RatingBar) convertView.findViewById(R.id.creatorRating);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //Display the data


            viewHolder.title.setText(task.getTitle());
            viewHolder.description.setText(task.getDescription());
            //viewHolder.creatorRating.setRating(dataModel.getCreatorRating());

            //Change later
            viewHolder.duration.setText("20 mins ago");

            //Location
            //Needs more accurate text descriptions, like feet
            if(location != null) {
                //Calculate the distance
                float distance = location.distanceTo(task.getDropOffDestination());
                //Meters to miles
                int miles = (int) (distance * 0.000621371192);
                viewHolder.distance.setText(miles + " miles away");
            }


            viewHolder.price.setText(Float.toString(task.getBaseCost()));
            //viewHolder.time.setText(dataModel.getTimeToComplete());

            // Return the completed view to render on screen
            return convertView;
        }



}
