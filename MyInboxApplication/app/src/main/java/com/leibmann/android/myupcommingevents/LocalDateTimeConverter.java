package com.leibmann.android.myupcommingevents;

import android.text.format.Time;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by matthias on 2/1/2015.
 */
public class LocalDateTimeConverter {

    private Time mLocalTime;

    public LocalDateTimeConverter(String utcDateRFC3339) {
        // RFC3339
        // "Start": "2015-01-23T20:00:00Z",
        // "End": "2015-01-23T21:00:00Z",
        Time localTime = new Time();
        localTime.parse3339(utcDateRFC3339);
        int utcOffset = TimeZone.getDefault().getOffset(localTime.toMillis(false));
        localTime.set(localTime.toMillis(false) + utcOffset);
        this.mLocalTime = localTime;
    }

    public String getLocalDayString() {
        String s = mLocalTime.format("%m/%d/%Y");
        return s;
    }
    public String getLocalTimeString() {
        //String s = mLocalTime.format("%H:%M:%S");
        String s = mLocalTime.format("%H:%M");
        return s;
    }

    public String getLocalDayTimeString() {
        String s = mLocalTime.format("%m/%d/%Y %H:%M:%S");
        return s;
    }

    public boolean IsAm() {
        String s = mLocalTime.format("%p");
        boolean am = s.toLowerCase().equals("am");
        return am;
    }

    public String getLocalDayOfWeekString() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
        Date d = new Date();
        d.setTime(mLocalTime.toMillis(false));
        String dayOfTheWeek = sdf.format(d);
        return dayOfTheWeek;
    }

}
