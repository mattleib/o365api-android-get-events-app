package com.leibmann.android.myupcommingevents;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;

/**
 * Created by mattleib on 1/28/2015.
 */
public class Helpers {

    public static String LogEnterMethod(String methodName)
    {
        return "*Entering [" + methodName + "]";
    }

    public static String LogLeaveMethod(String methodName)
    {
        return "*Leaving [" + methodName + "]";
    }

    public static String LogInMethod(String methodName)
    {
        return "**Working in [" + methodName + "]";
    }

    public static void LogIfNull(String tag, Object obj, String objectName)
    {
        if(obj == null){
            Log.d(tag, "NullObject::" + objectName);
        }
    }

    public static JSONObject TryGetJSONObject(JSONObject sourceObject, String name)
    {
        try {
            JSONObject obj = sourceObject.getJSONObject(name);
            return obj;
        }
        catch(Exception e){
            return null;
        }
    }

    public static String TryGetJSONValue(JSONObject sourceObject, String name)
    {
        try {
            String value = sourceObject.getString(name);
            return value;
        }
        catch(Exception e){
            return "";
        }
    }

    public static void savePreferenceValue(String preferenceKey, String preferenceValue, Context context)
    {
        SharedPreferences appPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString(preferenceKey, preferenceValue);
        editor.commit();
    }

    public static String getPreferenceValue(String preferenceKey, String preferenceDefault, Context context)
    {
        SharedPreferences appPreferences = context.getSharedPreferences(Constants.PREFS_NAME, 0);
        String s = appPreferences.getString(preferenceKey, preferenceDefault);
        return s;
    }

    public static String getEventsQueryString(String queryTemplate,
                                              EventTimeSpan eventTimeSpan,
                                              Boolean doNotShowPastEvents) {
        // "Start": "2015-01-23T20:00:00Z",
        // "End": "2015-01-23T21:00:00Z",
        // Get the local time
        Time localTimeNow = new Time(Time.getCurrentTimezone()); // returns time in current TimeZone
        localTimeNow.setToNow(); // set it to now
        long nowLocalTimeMilliseconds = localTimeNow.toMillis(false);

        if(!doNotShowPastEvents) { // get rid of hours, midnight today to start with all events of today
            localTimeNow.set(localTimeNow.monthDay, localTimeNow.month, localTimeNow.year);
        } else { // keep only events starting previous 60 minutes from now
            localTimeNow.set(nowLocalTimeMilliseconds - (Constants.OneMinuteInMilliseconds * 60));
        }

        Calendar cal = Calendar.getInstance();
        cal.set(localTimeNow.year, localTimeNow.month, localTimeNow.monthDay, localTimeNow.hour, localTimeNow.minute, localTimeNow.second);
        // convert to UTC
        int zoneOffset = cal.get(Calendar.ZONE_OFFSET);
        int dstOffset = cal.get(Calendar.DST_OFFSET);
        cal.add(Calendar.MILLISECOND, -(zoneOffset + dstOffset));
        Date startDate = cal.getTime();
        SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        String startDateTime = fmt.format(startDate);

        if (eventTimeSpan == EventTimeSpan.Today) {
            cal.add(Calendar.HOUR, 24);
        } else if (eventTimeSpan == EventTimeSpan.NextSevenDays) {
            cal.add(Calendar.HOUR, 24 * 7);
        } else { // 30 days
            cal.add(Calendar.HOUR, 24 * 30);
        }

        Date endDate = cal.getTime();
        String endDateTime = fmt.format(endDate);

        String queryString = String.format(queryTemplate, startDateTime, endDateTime);

        return queryString;
    }

    public static ArrayList<EventItem> getUpcomingEventsFromCache(Context context) {
        ArrayList<EventItem> upcomingEvents = new ArrayList<EventItem>();
        ArrayList<Item> items = EventsCache.read(context);
        if(items == null) {
            return upcomingEvents;
        }

        Enumeration e = Collections.enumeration(items);
        while(e.hasMoreElements()) {
            Item item = (Item)e.nextElement();
            if(item.isItemType() == ItemType.Event) {
                EventItem event = (EventItem) item;
                if (event.isUpcoming()) {
                    upcomingEvents.add(event);
                }
            }
        }
        return upcomingEvents;
    }

    public static ArrayList<EventItem> getAllEventsFromCache(Context context) {
        ArrayList<EventItem> events = new ArrayList<EventItem>();
        ArrayList<Item> items = EventsCache.read(context);
        if(items == null) {
            return events;
        }

        Enumeration e = Collections.enumeration(items);
        while(e.hasMoreElements()) {
            Item item = (Item)e.nextElement();
            if(item.isItemType() == ItemType.Event) {
                EventItem event = (EventItem) item;
                events.add(event);
            }
        }
        return events;
    }

}
