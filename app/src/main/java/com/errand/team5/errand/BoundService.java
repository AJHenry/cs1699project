package com.errand.team5.errand;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Chronometer;
import android.widget.Toast;

import io.nlopez.smartlocation.OnLocationUpdatedListener;
import io.nlopez.smartlocation.SmartLocation;
import io.nlopez.smartlocation.location.config.LocationAccuracy;
import io.nlopez.smartlocation.location.config.LocationParams;

public class BoundService extends Service {
    private IBinder mBinder = new MyBinder();
    private String TAG = "BoundService";
    private Location lastKnownLocation = null;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e(TAG, "in onCreate");
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.e(TAG, "in onBind");

        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "Called onStartCommand");
        String term = intent.getStringExtra("SearchTerm");
        String apiKey = intent.getStringExtra("ApiKey");
        String sort = intent.getStringExtra("Sort");

        Log.e(TAG, "SearchTerm: "+term);

        Intent launchIntent = new Intent("com.errand.team5.errand.SearchReceiver");

        if (launchIntent != null){
            launchIntent.setComponent(new ComponentName("com.errand.team5.errand","com.errand.team5.errand.SearchReceiver"));
            launchIntent.putExtra("SearchTerm", term);
            launchIntent.putExtra("ApiKey", apiKey);
            launchIntent.putExtra("Sort", sort);
            sendBroadcast(launchIntent);
        } else {
            Toast.makeText(this, null, Toast.LENGTH_LONG).show();
        }

        return START_NOT_STICKY;
    }


    @Override
    public void onRebind(Intent intent) {
        Log.e(TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "in onUnbind");
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "in onDestroy");
    }


    public boolean search(String term, String apiKey){

        return true;
    }

    public class MyBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }
}