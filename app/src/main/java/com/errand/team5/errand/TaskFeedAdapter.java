package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Andrew on 3/7/2018.
 * <p>
 * Template for displaying tasks in a list view
 */

public class TaskFeedAdapter extends ArrayAdapter<TaskModel> {

    private Context mContext;
    private ArrayList<TaskModel> dataSet;
    private Location location;
    private User creator;

    private final String TAG = "TaskFeedAdapter";

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
        this.mContext = context;
        this.location = currentLocation;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        TaskModel task = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final ViewHolder viewHolder; // view lookup cache stored in tag

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

        creator = task.getUser();
        //Make sure the user isn't null
        if(creator != null) {
            String imgurl = creator.getPhotoUrl();
            Glide.with(getContext()).load(imgurl).apply(RequestOptions.circleCropTransform()).into(viewHolder.profileImage);
        }

        viewHolder.title.setText(task.getTitle());
        viewHolder.description.setText(task.getDescription());
        //viewHolder.creatorRating.setRating(dataModel.getCreatorRating());

        //Change later
        viewHolder.timestamp.setText(getTimeAgo(task.getPublishTime().getTime()));
        Log.d(TAG, task.getPublishTime().getTime()+"");

        //mLocation
        //Needs more accurate text descriptions, like feet
        if (location != null) {
            //Calculate the distance
            float distance = location.distanceTo(task.getDropOffDestination());

            String distanceText = DistanceFormatter.format((int)distance);
            //Meters to miles
            int miles = (int) (distance * 0.000621371192);

            viewHolder.distance.setText(distanceText+" away");
        }

        //Set the price tag
        Log.d(TAG, task.getBaseCost()+"");
        DecimalFormat df = new DecimalFormat("0.##");
        viewHolder.price.setText(df.format(task.getBaseCost()));
        //viewHolder.time.setText(dataModel.getTimeToComplete());

        // Return the completed view to render on screen
        return convertView;
    }


    public void showProfile() {
        final Dialog dialog = new Dialog(getContext());
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.fragment_profile);

        ImageView img = (ImageView) dialog.findViewById(R.id.profile_picture);
        String imgurl = creator.getPhotoUrl();
        Glide.with(getContext())
                .load(imgurl)
                .apply(RequestOptions.circleCropTransform())
                .into(img);

        //Do processing here

        dialog.show();

    }


    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;


    public String getTimeAgo(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        //TODO Fix issue with people submitting wrong timestamp
        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            Log.d(TAG, "Error in timestamp");
            Log.d(TAG, "Given: "+time);
            Log.d(TAG, "Actual: "+now);
            return "just now";
            //return null;
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
