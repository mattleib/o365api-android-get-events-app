package com.example.mattleib.myinboxapplication;

import android.content.SharedPreferences;
import android.text.format.Time;
import android.util.Log;

import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

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

    public static String GetEventsQueryString(String queryTemplate,
                                              DataTypes.EventTimeSpan eventTimeSpan,
                                              Boolean doNotShowPastEvents) {
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        // Get the local time
        Time localTimeNow = new Time(Time.getCurrentTimezone()); // returns time in current TimeZone
        localTimeNow.setToNow(); // set it to now
        long nowLocalTimeMilliseconds = localTimeNow.toMillis(false);

        if(!doNotShowPastEvents) { // get rid of hours, midnight today to start with all events of today
            localTimeNow.set(localTimeNow.monthDay, localTimeNow.month, localTimeNow.year);
        } else { // keep only events starting previous 60 minutes from now
            localTimeNow.set(nowLocalTimeMilliseconds - (Constants.OneMinuteInMilliseconds * 60));
        }

        int utcOffset = TimeZone.getDefault().getOffset(nowLocalTimeMilliseconds);
        long nowUtcTimeOffsetMilliseconds = nowLocalTimeMilliseconds - utcOffset;

        localTimeNow.set(nowUtcTimeOffsetMilliseconds);
        String startDateTime = localTimeNow.format("%Y-%m-%dT%H:%M:%SZ");

        long eventSpan = 0;
        if (eventTimeSpan == DataTypes.EventTimeSpan.Day) {
            eventSpan = Constants.OneHourInMilliseconds * 24;
        } else if (eventTimeSpan == DataTypes.EventTimeSpan.Week) {
            eventSpan = Constants.OneHourInMilliseconds * 24 * 7;
        } else {
            eventSpan = Constants.OneHourInMilliseconds * 24 * 30;
        }

        localTimeNow.set(nowUtcTimeOffsetMilliseconds + eventSpan);
        String endDateTime = localTimeNow.format("%Y-%m-%dT%H:%M:%SZ");

        String queryString = String.format(queryTemplate, startDateTime, endDateTime);

        return queryString;
    }

    public static boolean IsEventNow(String startTimeUtc, String endTimeUtc) {
        //RFC3339
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        Time startTime = new Time();
        startTime.parse3339(startTimeUtc);
        long startTimeMilli = startTime.toMillis(false);
        startTimeMilli = startTimeMilli - (Constants.OneMinuteInMilliseconds * 15); //Give 15 minute buffer
        startTime.set(startTimeMilli);

        Time endTime = new Time();
        endTime.parse3339(endTimeUtc);

        Time now = new Time();
        now.setToNow();

        if(startTime.toMillis(true) < now.toMillis(true) &&
                now.toMillis(true) < endTime.toMillis(true)) {
            return true;
        }

        return false;
    }
}
