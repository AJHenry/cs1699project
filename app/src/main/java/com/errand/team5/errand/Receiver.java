package com.errand.team5.errand;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * TODO API Trigger
 * Created by Andrew on 3/29/2018.
 */

public class Receiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // assumes WordService is a registered service
        //Intent intent = new Intent(context, WordService.class);
        //context.startService(intent);
    }
}
