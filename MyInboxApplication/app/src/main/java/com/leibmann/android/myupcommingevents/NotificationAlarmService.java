package com.leibmann.android.myupcommingevents;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
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
    private int mNumMessages = 0;

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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, Helpers.LogEnterMethod("onStartCommand"));

        Context context = getApplicationContext();

        ArrayList<EventItem> events = Helpers.getAllEventsFromCache(context);
        if(events.size() <= 0) {
            Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand") + "::No events");
            mNotificationManager.cancel(NOTIFICATIONID);
            return START_NOT_STICKY; // stop service after it's done the work
        }

        mNumMessages = 0;
        EventItem firstEvent = null;
        Enumeration e = Collections.enumeration(events);
        while(e.hasMoreElements()) {
            EventItem event = (EventItem)e.nextElement();
            {
                if(event.startsIn15Minutes()) {
                    if(firstEvent == null) {
                        firstEvent = event;
                    }
                    mNumMessages++;
                }
            }
        }

        if(firstEvent != null) {
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
                            .setContentText(s).setNumber(mNumMessages)
                            .setStyle(new NotificationCompat.BigTextStyle()
                            .bigText(msg));

            // Notification start MainActivity
            Intent mainActivityIntent = new Intent(context, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mBuilder.setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));

            // Show on lockscreen
            mBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);

            mNotificationManager.notify(NOTIFICATIONID, mBuilder.build());
        }

        // set the next alarm for next upcoming event...
        MainActivity.startAlarm();

        Log.d(TAG, Helpers.LogLeaveMethod("onStartCommand"));
        return START_NOT_STICKY; // stop service after it's done the work
    }

}
