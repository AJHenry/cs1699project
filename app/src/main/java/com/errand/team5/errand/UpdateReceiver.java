package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * TODO API Trigger
 *
 * Receives task information to update;
 * Queries Geofire content provider for current matching task,
 * replaces fields, displays updated task, returns
 */

public class UpdateReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Broadcast Received");

        String apiKey = intent.getStringExtra("ApiKey");
        String taskID = intent.getStringExtra("TaskID");
        String userID = intent.getStringExtra("UserID");
        String title = intent.getStringExtra("Title");
        String type = intent.getStringExtra("Type");
        String description = intent.getStringExtra("Description");
        String address = intent.getStringExtra("Address");
        String address2 = intent.getStringExtra("Address2");
        String city = intent.getStringExtra("City");
        String state = intent.get

        String term = intent.getStringExtra("SearchTerm");
        String apiKey = intent.getStringExtra("ApiKey");
        String sort = intent.getStringExtra("Sort");

        // Send to Create Task activity
        Intent searchIntent = new Intent(context, Search.class);
        searchIntent.putExtra("SearchTerm", term);
        searchIntent.putExtra("ApiKey", apiKey);
        searchIntent.putExtra("Sort", sort);
        context.startActivity(searchIntent);
    }
}
