//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by mattleib on 2/16/2015.
 */
public class NotificationAlarmService extends Service {

    private final static String TAG = "NotificationAlarmService";
    private final static int NOTIFICATIONID = 001;

    @Override
    public IBinder onBind(Intent arg0)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, Helpers.LogEnterMethod("onStartCommand"));

        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        Context context = getApplicationContext();

        ArrayList<EventItem> events = Helpers.getAllEventsFromCache(context);
        if(events.size() <= 0) {
            Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand") + "::No events");
            notificationManager.cancel(NOTIFICATIONID);
            return START_NOT_STICKY; // stop service after it's done the work
        }

        int numMessages = 0;
        EventItem firstEvent = null;
        Enumeration e = Collections.enumeration(events);
        while(e.hasMoreElements()) {
            EventItem event = (EventItem)e.nextElement();
            {
                if(event.startsIn15Minutes()) {
                    //if(firstEvent == null) {
                        firstEvent = event;
                    //}
                    numMessages++;
                }
            }
        }

        if(firstEvent == null) {
            Log.d(TAG, Helpers.LogInMethod("onStartCommand") + "::No Event to notify");
        }
        else {
            Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand") + "::Events found for notification");
            LocalDateTimeConverter startTime = new LocalDateTimeConverter(firstEvent.getStart());

            String s = String.format("Next: %s %s",
                    startTime.getLocalTimeString(),
                    firstEvent.getSubject());

            String msg = s;
            if(!firstEvent.getLocation().getDisplayName().isEmpty()) {
                msg = String.format("%s.  Location (%s)",
                        s,
                        firstEvent.getLocation().getDisplayName());
            }

            // Now we do the notification for the events ...
            NotificationCompat.Builder mBuilder =
                    new NotificationCompat.Builder(this)
                            .setSmallIcon(R.drawable.ic_event_24px)
                            .setContentTitle("Office 365 Upcoming Events")
                            .setContentText(s).setNumber(numMessages)
                            .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg));

            // Notification start MainActivity
            /*
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            stackBuilder.addParentStack(MainActivity.class);
            stackBuilder.addNextIntent(mainActivityIntent);
            PendingIntent resultPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
            */
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            mainActivityIntent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
            mainActivityIntent.setAction(Intent.ACTION_MAIN);
            mainActivityIntent.addCategory(Intent.CATEGORY_LAUNCHER);
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, mainActivityIntent, 0));

            // Show on lockscreen
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

            notificationManager.notify(NOTIFICATIONID, mBuilder.build());
        }

        // set the next alarm for next upcoming event...
        MainActivity.startAlarm();

        Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand"));
        return START_NOT_STICKY; // stop service after it's done the work
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