package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.gson.Gson;

/**
 * TODO API Trigger
 *
 * Receives task information to update;
 * Queries Geofire content provider for current matching task,
 * replaces fields, displays updated task, returns
 */

public class UpdateReceiver extends BroadcastReceiver {

    final String TAG = "UpdateReceiver class";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Broadcast Received -- UPDATE TASK");

        // Get all fields from broadcast
        Gson gson = new Gson();
        String taskDataStr = intent.getStringExtra("taskData");
        boolean delete = intent.getBooleanExtra("delete", false);
        TaskData taskData = gson.fromJson(taskDataStr, TaskData.class);

        // Send to Create Task activity
        Intent updateIntent = new Intent(context, UpdateTask.class);
        updateIntent.putExtra("taskData", taskData);
        updateIntent.putExtra("delete", delete);
        updateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(updateIntent);
    }
}
