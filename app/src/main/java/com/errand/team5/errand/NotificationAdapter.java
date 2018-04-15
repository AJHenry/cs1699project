package com.errand.team5.errand;

import android.app.Dialog;
import android.content.Context;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;

public class NotificationAdapter extends ArrayAdapter<Notification>{

    private Context mContext;
    private ArrayList<Notification> dataSet;
    private User otherUser;

    private final String TAG = "TaskFeedAdapter";
    // View lookup cache
    private static class ViewHolder {
        TextView message;
        ImageView profile;
        Button decline;
        Button accept;
        Button dismiss;
        Button confirm;

    }

    protected NotificationAdapter(ArrayList<Notification> data, Context context) {
        super(context, R.layout.fragment_notification, data);
        this.dataSet = data;
        this.mContext = context;
    }

    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // Get the data item for this position
        Notification noti = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        final com.errand.team5.errand.NotificationAdapter.ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null) {

            viewHolder = new com.errand.team5.errand.NotificationAdapter.ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());

            //Get all the components we need to modify
            convertView = inflater.inflate(R.layout.fragment_notification, parent, false);
            viewHolder.message = (TextView) convertView.findViewById(R.id.message);
            viewHolder.profile = (ImageView) convertView.findViewById(R.id.profile);
            viewHolder.accept = (Button) convertView.findViewById(R.id.button_accept);
            viewHolder.decline = (Button) convertView.findViewById(R.id.button_decline);
            viewHolder.dismiss = (Button) convertView.findViewById(R.id.button_dismiss);
            viewHolder.confirm = (Button) convertView.findViewById(R.id.button_confirm);

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (com.errand.team5.errand.NotificationAdapter.ViewHolder) convertView.getTag();
        }


        if(noti.getType() == Notification.NEEDS_APPROVAL || noti.getType() == Notification.NEEDS_CONFIRMATION)
        {
            //this notification exists in the creators table
            //therefore otherUser should be the requester
            otherUser = noti.getRequester();

            if(noti.getType() == Notification.NEEDS_APPROVAL && noti.getStatus() == Notification.OPEN)
            {
                //this is a pending request for the errand in the creators notis
                viewHolder.accept.setVisibility(View.VISIBLE);
                viewHolder.accept.setText("Accept");
                viewHolder.decline.setVisibility(View.VISIBLE);
                viewHolder.decline.setText("Decline");
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("" + otherUser.getDisplayName() + " has requested your errand! Do you accept?");
            }
            else if(noti.getType() == Notification.NEEDS_APPROVAL && noti.getStatus() == Notification.CLOSED)
            {
                //this is an approved/active errand
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("" + otherUser.getDisplayName() + " is currently funning your errand...");
            }
            else if(noti.getType() == Notification.NEEDS_CONFIRMATION && noti.getStatus() == Notification.OPEN)
            {
                //pending approval that it was completed
                viewHolder.accept.setVisibility(View.VISIBLE);
                viewHolder.accept.setText("Confirm");
                viewHolder.decline.setVisibility(View.VISIBLE);
                viewHolder.decline.setText("Deny");
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("" + otherUser.getDisplayName() + " has claimed to have completed your errand. Can you confirm this?");
            }
            else if(noti.getType() == Notification.NEEDS_CONFIRMATION && noti.getStatus() == Notification.CLOSED)
            {
                //confirmed it was completed
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.VISIBLE);
                viewHolder.dismiss.setText("Dismiss");
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("You've confirmed that " + otherUser.getDisplayName() + " has completed your errand! Thank you for using errand!");
            }
        }
        else
        {
            //this notification exists in the requester's table
            //there for otherUser should be the creator
            otherUser = noti.getCreator();

            if(noti.getType() == Notification.PENDING_APPROVAL && noti.getStatus() == Notification.OPEN)
            {
                //waiting for creator to approve request
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("Waiting for " + otherUser.getDisplayName() + " to approve you for his/her errand...");
            }
            else if(noti.getType() == Notification.PENDING_APPROVAL && noti.getStatus() == Notification.CLOSED)
            {
                //actively running the task
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.VISIBLE);
                viewHolder.message.setText("You're currently running an errand for " + otherUser.getDisplayName() + ". Hit the confirm button below once you have completed it to request his/her confirmation.");
            }
            else if(noti.getType() == Notification.PENDING_CONFIRMATION && noti.getStatus() == Notification.OPEN)
            {
                //hit confirm, waiting for confirmation from creator
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.GONE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("Awaiting confirmation of errand completion from " + otherUser.getDisplayName() +"...");
            }
            else if(noti.getType() == Notification.PENDING_CONFIRMATION && noti.getStatus() == Notification.CLOSED)
            {
                viewHolder.accept.setVisibility(View.GONE);
                viewHolder.decline.setVisibility(View.GONE);
                viewHolder.dismiss.setVisibility(View.VISIBLE);
                viewHolder.confirm.setVisibility(View.GONE);
                viewHolder.message.setText("" + otherUser.getDisplayName() + "has confirmed your completion of his/her errand! Thank you for using errand!");
            }
        }
        //Make sure the user isn't null
        if(otherUser != null) {
            String imgurl = otherUser.getPhotoUrl();
            Glide.with(getContext()).load(imgurl).apply(RequestOptions.circleCropTransform()).into(viewHolder.profile);
        }



        // Return the completed view to render on screen
        return convertView;
    }

    public void updateNoti(String button, Notification noti)
    {
        String nId = noti.getnId();
        String creatorID = noti.getCreator().getUid();
        String requesterID = noti.getRequester().getUid();
        DatabaseReference db = FirebaseDatabase.getInstance().getReference();
        DatabaseReference creatorNotiRef = db.child("testUsers").child(creatorID).child("notifications").child(nId);
        DatabaseReference requesterNotiRef = db.child("testUsers").child(requesterID).child("notifications").child(nId);
        DatabaseReference taskRef = db.child("errands").child(noti.taskID);


        if(noti.getType() == Notification.NEEDS_APPROVAL && noti.getStatus() == Notification.OPEN)
        {
            if(button.equals("Accept"))
            {
                //update noti to a p_A;C/n_A;C
                creatorNotiRef.child("type").setValue(Notification.NEEDS_APPROVAL);
                creatorNotiRef.child("status").setValue(Notification.CLOSED);
                requesterNotiRef.child("type").setValue(Notification.PENDING_APPROVAL);
                requesterNotiRef.child("status").setValue(Notification.CLOSED);
                taskRef.child("status").setValue("2");
            }
            else if(button.equals("Decline"))
            {
                //delete p_A;O/n_A;O from tables
                creatorNotiRef.setValue(null);
                requesterNotiRef.setValue(null);
                taskRef.child("status").setValue(0);
            }
        }
        else if(noti.getType() == Notification.PENDING_APPROVAL && noti.getStatus() == Notification.CLOSED)
        {
            //confirm button pressed
            //update noti to a p_C;O/n_C;O
            creatorNotiRef.child("type").setValue(Notification.NEEDS_CONFIRMATION);
            creatorNotiRef.child("status").setValue(Notification.OPEN);
            requesterNotiRef.child("type").setValue(Notification.PENDING_CONFIRMATION);
            requesterNotiRef.child("status").setValue(Notification.OPEN);
        }
        else if(noti.getType() == Notification.NEEDS_CONFIRMATION && noti.getStatus() == Notification.OPEN)
        {
            if(button.equals("Confirm"))
            {
                //update noti to p_C;C/n_C;C
                 creatorNotiRef.child("type").setValue(Notification.NEEDS_CONFIRMATION);
                 creatorNotiRef.child("status").setValue(Notification.CLOSED);
                 requesterNotiRef.child("type").setValue(Notification.PENDING_CONFIRMATION);
                 requesterNotiRef.child("status").setValue(Notification.CLOSED);
                 taskRef.child("status").setValue(3);
            }
            else if(button.equals("Deny"))
            {
                //update noti back to p_A;C/n_;C
                creatorNotiRef.child("type").setValue(Notification.NEEDS_APPROVAL);
                creatorNotiRef.child("status").setValue(Notification.CLOSED);
                requesterNotiRef.child("type").setValue(Notification.PENDING_APPROVAL);
                requesterNotiRef.child("status").setValue(Notification.CLOSED);
                taskRef.child("status").setValue(2);
            }
        }
        else if(noti.getType() == Notification.PENDING_APPROVAL && noti.getStatus() == Notification.CLOSED)
        {
            //delete notification on button press
            requesterNotiRef.setValue(null);
        }
        else if(noti.getType() == Notification.NEEDS_APPROVAL && noti.getStatus() == Notification.CLOSED)
        {
            //delete notification on button press
            creatorNotiRef.setValue(null);
        }
    }


}

