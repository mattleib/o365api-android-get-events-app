package com.leibmann.android.myupcommingevents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by mattleib on 2/16/2015.
 */
public class NotificationAlarmReceiver extends BroadcastReceiver {

    private final static String TAG = "NotificationAlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d(TAG, Helpers.LogEnterMethod("onReceive"));

        // Receive the alarm and start the service to manage notifications of new events
        Intent service = new Intent(context, NotificationAlarmService.class);
        context.startService(service);

        Toast.makeText(context, "Alarm Received", Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("onReceive"));
    }

}
