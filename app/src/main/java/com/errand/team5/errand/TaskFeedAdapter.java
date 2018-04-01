package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
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
            TextView price;
            TextView time;
            TextView description;
            TextView timestamp;
            ImageView profileImage;
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
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);
                viewHolder.description = (TextView) convertView.findViewById(R.id.special_instructions);
                viewHolder.profileImage = (ImageView) convertView.findViewById(R.id.profile);
                viewHolder.timestamp = (TextView) convertView.findViewById(R.id.listview_timestamp);

                viewHolder.profileImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Show the profile fragment
                        showProfile();
                    }
                });

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //Display the data


            viewHolder.title.setText(task.getTitle());
            viewHolder.description.setText(task.getDescription());
            //viewHolder.creatorRating.setRating(dataModel.getCreatorRating());

            //Change later
            viewHolder.timestamp.setText(getTimeAgo(task.getPublishTime().getTime()));

            //mLocation
            //Needs more accurate text descriptions, like feet
            if(location != null) {
                //Calculate the distance
                float distance = location.distanceTo(task.getDropOffDestination());
                //Meters to miles
                int miles = (int) (distance * 0.000621371192);
                viewHolder.distance.setText(miles + " miles away");
            }


            viewHolder.price.setText(Double.toString(task.getBaseCost()));
            //viewHolder.time.setText(dataModel.getTimeToComplete());

            // Return the completed view to render on screen
            return convertView;
        }


    public void showProfile(){
        final Dialog dialog = new Dialog(getContext());
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.summary);

        //Do processing here

        dialog.show();

    }


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public static String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        // TODO: localize
        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return "just now";
        } else if (diff < 2 * MINUTE_MILLIS) {
            return "a minute ago";
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " minutes ago";
        } else if (diff < 90 * MINUTE_MILLIS) {
            return "an hour ago";
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " hours ago";
        } else if (diff < 48 * HOUR_MILLIS) {
            return "yesterday";
        } else {
            return diff / DAY_MILLIS + " days ago";
        }
    }
}
