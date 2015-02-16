package com.leibmann.android.myupcommingevents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;

/**
 * Created by mattleib on 2/16/2015.
 */
public class NotificationAlarmService extends Service {

    private final static String TAG = "NotificationAlarmService";

    private static NotificationManager mNotificationManager = null;
    private static final int NOTIFICATIONID = 001;
    private static int mNumMessages = 0;

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
        mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public void onDestroy()
    {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    // @SuppressWarnings("static-access")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, Helpers.LogEnterMethod("onStartCommand"));

        // Read event items from cache and display immediately
        ArrayList<Item> items = EventsCache.read(getApplicationContext());
        if(items == null) {
            Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand") + "::No events in cache");
            return START_NOT_STICKY; // stop service after it's done the work
        }

        Enumeration e = Collections.enumeration(items);
        ArrayList<EventItem> upcomingEvents = new ArrayList<EventItem>();
        while(e.hasMoreElements()) {
            Item item = (Item)e.nextElement();
            if(item.isItemType() == ItemType.Event) {
                EventItem event = (EventItem) item;
                if (Helpers.IsEventNow(event.getStart(), event.getEnd()) && !event.IsAllDay) {
                    upcomingEvents.add(event);
                }
            }
        }

        if(upcomingEvents.size() <= 0) {
            Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand") + "::No upcoming events");
            return START_NOT_STICKY; // stop service after it's done the work
        }

        EventItem event = (EventItem) upcomingEvents.get(0);
        LocalDateTimeConverter startTime = new LocalDateTimeConverter(event.getStart());
        LocalDateTimeConverter endTime = new LocalDateTimeConverter(event.getEnd());

        String s = String.format(
                getApplicationContext().getResources().getString(R.string.event_template_startdate),
                startTime.getLocalTimeString(), endTime.getLocalTimeString(), endTime.getLocalDayString());
        s = "Up next: " + s + " " + event.Subject;

        // Now we do the notification for the events ...
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.ic_event_24px)
                        .setContentTitle("Office 365 Upcoming Events")
                        .setContentText(s).setNumber(upcomingEvents.size());

        // Q: !!! How do I pass the Pending Intent to go back to the apps MainActivity?
        // mBuilder.setContentIntent(intent);

        mNotificationManager.notify(NOTIFICATIONID, mBuilder.build());

        Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand"));
        return START_NOT_STICKY; // stop service after it's done the work
    }

}
