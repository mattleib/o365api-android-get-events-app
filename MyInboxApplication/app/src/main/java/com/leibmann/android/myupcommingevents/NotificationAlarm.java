//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
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

        //Get time Now
        Calendar calNow = Calendar.getInstance();
        long calNowTime = calNow.getTimeInMillis();

        //Select the next upcoming event, do not take it if notification alarm time happened in the past
        EventItem event;
        Calendar cal = calNow;
        for(int i=0; i<upcomingEvents.size(); i++) {
            event = (EventItem) upcomingEvents.get(i);
            LocalDateTimeConverter startTime = new LocalDateTimeConverter(event.getStart());
            long eventAlarmTime = startTime.getLocalTime().toMillis(false) - (Constants.OneMinuteInMilliseconds*15);

            // BUGBUG: eventAlarmTime should be local time, but it is past, e.g. for PST it is 8 hours back.
            // no idea why, this fixes it by adding UTC offset.
            Calendar calEvent = Calendar.getInstance();
            calEvent.setTimeInMillis(eventAlarmTime);
            int zoneOffset = calEvent.get(Calendar.ZONE_OFFSET);
            int dstOffset = calEvent.get(Calendar.DST_OFFSET);
            calEvent.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
            long calEventTime = calEvent.getTimeInMillis();

            // never schedule an alarm in the past.
            // this event was handled by a previous Alarm, se we need to get the next one
            if(calEventTime >= calNowTime) {
                cal = calEvent;
                break;
            }
        }

        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String alarmTimeString = fmt.format(cal.getTime());

        mAlarmManager.set(
                AlarmManager.RTC_WAKEUP,
                cal.getTimeInMillis(),
                MainActivity.getAlarmPendingIntent());

        Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::Alarm turned on for [" + alarmTimeString + "]");
    }

    public void cancelAlarmForEventNotifications() {
        mAlarmManager.cancel(MainActivity.getAlarmPendingIntent());
    }
}
// MIT License:

// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// ""Software""), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.