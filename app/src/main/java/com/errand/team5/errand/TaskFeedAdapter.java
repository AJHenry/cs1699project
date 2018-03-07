package com.errand.team5.errand;

import android.content.Context;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Andrew on 3/7/2018.
 */

public class TaskFeedAdapter extends ArrayAdapter<TaskModel> {

        Context mContext;
    private ArrayList<TaskModel> dataSet;

        // View lookup cache
        private static class ViewHolder {
            TextView title;
            TextView distance;
            TextView duration;
            TextView price;
            TextView time;
        }

        public TaskFeedAdapter(ArrayList<TaskModel> data, Context context) {
            super(context, R.layout.row_item, data);
            this.dataSet = data;
            this.mContext=context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            TaskModel dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {

                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.row_item, parent, false);
                viewHolder.title = (TextView) convertView.findViewById(R.id.title);
                viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
                viewHolder.distance = (TextView) convertView.findViewById(R.id.distance);
                viewHolder.price = (TextView) convertView.findViewById(R.id.price);
                viewHolder.time = (TextView) convertView.findViewById(R.id.time);


                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }


            viewHolder.title.setText(dataModel.getTitle());


            //Change later
            viewHolder.duration.setText("20 mins ago");
            viewHolder.distance.setText("1 mile away");

            viewHolder.price.setText("$"+Integer.toString(dataModel.getPrice()));
            viewHolder.time.setText("/"+dataModel.getTimeToComplete());

            // Return the completed view to render on screen
            return convertView;
        }

}
