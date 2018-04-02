package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;

public class CreateReceiver extends BroadcastReceiver {
    final String TAG = CreateReceiver.class.toString();

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving

        Log.e(TAG, "Broadcast Received");
        // Get information into serializable class
        Gson gson = new Gson();
        String taskDataStr = intent.getStringExtra("taskData");
        TaskData taskData = gson.fromJson(taskDataStr, TaskData.class);

        // Send to Create Task activity
        Intent createIntent = new Intent(context, CreateTask.class);
        createIntent.putExtra("taskData", taskData);
        context.startActivity(createIntent);
    }
}
