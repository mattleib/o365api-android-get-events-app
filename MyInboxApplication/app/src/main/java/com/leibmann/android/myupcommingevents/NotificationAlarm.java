package com.leibmann.android.myupcommingevents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.usage.UsageEvents;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.format.Time;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Created by matthias on 2/16/2015.
 */
public class NotificationAlarm {

    private final static String TAG = "NotificationAlarm";

    private AlarmManager mAlarmManager = null; // Alarm manager for the event notification service
    private Context mContext;

    public NotificationAlarm(Context context) {
        mContext = context;
        mAlarmManager = (AlarmManager)context.getSystemService(context.ALARM_SERVICE);
    }

    public void startAlarmForEventNotifications(){

        Log.d(TAG, Helpers.LogEnterMethod("startAlarmForEventNotifications"));

        // First cancel the old alarms
        cancelAlarmForEventNotifications();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean doNotifications = sharedPreferences.getBoolean(Constants.PreferenceKeys.DoNotifications, true);

        if(!doNotifications) {
            Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::Notifications turned off");
            return;
        }

        ArrayList<EventItem> upcomingEvents = Helpers.getUpcomingEventsFromCache(mContext);
        if(upcomingEvents.size() <= 0) {
            Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::No events no alarm");
            return;
        }

        EventItem event = (EventItem) upcomingEvents.get(0);
        LocalDateTimeConverter startTime = new LocalDateTimeConverter(event.getStart());
        long eventAlarmTime = startTime.getLocalTime().toMillis(false) - (Constants.OneMinuteInMilliseconds*15);

        mAlarmManager.set(
                AlarmManager.RTC_WAKEUP,
                eventAlarmTime,
                MainActivity.getAlarmPendingIntent());

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(eventAlarmTime);
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String alarmTimeString = fmt.format(cal.getTime());

        Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::Alarm turned on for [" + alarmTimeString + "]");
    }

    public void cancelAlarmForEventNotifications() {
        mAlarmManager.cancel(MainActivity.getAlarmPendingIntent());
    }
}
