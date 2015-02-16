package com.leibmann.android.myupcommingevents;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by mattleib on 2/16/2015.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {

    private final static String TAG = "NotificationAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, Helpers.LogEnterMethod("onReceive"));

        // Receive the alarm and start the service to manage notifications of new events

        // !!! Q: How to pass the pending intent to the service for the notification ???
        //
        Intent service = new Intent(context, NotificationAlarmService.class);
        context.startService(service);

        // Toast.makeText(context, "Alarm Received", Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("onReceive"));
    }

}
