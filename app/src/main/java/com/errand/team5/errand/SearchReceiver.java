package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class SearchReceiver extends BroadcastReceiver {
    final String TAG = CreateReceiver.class.toString();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.e(TAG, "Broadcast Received");


        String term = intent.getStringExtra("SearchTerm");
        String apiKey = intent.getStringExtra("ApiKey");
        String sort = intent.getStringExtra("Sort");

        // Send to Create Task activity
        Intent searchIntent = new Intent(context, Search.class);
        searchIntent.putExtra("SearchTerm", term);
        searchIntent.putExtra("ApiKey", apiKey);
        searchIntent.putExtra("Sort", sort);
        searchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(searchIntent);
    }
}
