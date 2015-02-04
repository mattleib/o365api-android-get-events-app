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

    public static String GetEventsQueryString(String queryTemplate, DataTypes.EventTimeSpan eventTimeSpan) {
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        // Get the local time
        //Time now = new Time(Time.getCurrentTimezone()); // returns time in current TimeZone
        Time now = new Time("UTC"); // returns time in UTC
        now.setToNow();

        String startDate = now.format("%Y-%m-%d");
        String startTime = "00:00:00";
        String endTime = "23:59:59";

        Calendar cal = new GregorianCalendar();
        cal.set(now.year, now.month, now.monthDay);
        if (eventTimeSpan == DataTypes.EventTimeSpan.Day) {
            cal.add(Calendar.DAY_OF_MONTH, 0);
        } else if (eventTimeSpan == DataTypes.EventTimeSpan.Week) {
            cal.add(Calendar.DAY_OF_MONTH, 6);
        } else {
            cal.add(Calendar.MONTH, 1);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setCalendar(cal);
        String endDate = sdf.format(cal.getTime());

        String s = String.format(queryTemplate, startDate, startTime, endDate, endTime);

        return s;
    }
}
