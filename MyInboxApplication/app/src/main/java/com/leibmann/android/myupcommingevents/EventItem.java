package com.leibmann.android.myupcommingevents;

import android.text.format.Time;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class EventItem implements Serializable, Item {

    private static final long serialVersionUID = 0L;

    public ItemType isItemType() {
        return ItemType.Event;
    }

    protected String Subject;
    protected String BodyPreview;
    protected String Start;
    protected String End;
    protected String Type;
    protected com.leibmann.android.myupcommingevents.Organizer Organizer;
    protected com.leibmann.android.myupcommingevents.Location Location;
    protected Boolean IsAllDay;
    protected Boolean IsCancelled;
    protected String Importance;
    protected String ShowAs;

    public String getSubject() {
        return Subject;
    }

    public String getStart() {
        return Start;
    }

    public String getEnd() {
        return End;
    }

    public com.leibmann.android.myupcommingevents.Organizer getOrganizer() {
        return Organizer;
    }

    public com.leibmann.android.myupcommingevents.Location getLocation() {
        return Location;
    }

    public Boolean getIsAllDay() {
        return IsAllDay;
    }

    public Boolean getIsCancelled() {
        return IsCancelled;
    }

    public String getImportance() {
        return Importance;
    }

    public String getShowAs() {
        return ShowAs;
    }

    public boolean startsIn15Minutes()
    {
        if(IsAllDay || IsCancelled) {
            return false;
        }

        //RFC3339
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        Time startTime = new Time();
        startTime.parse3339(Start);
        long startTimeMilli = startTime.toMillis(false);
        startTimeMilli = startTimeMilli - (Constants.OneMinuteInMilliseconds * 15);
        startTime.set(startTimeMilli);

        Time endTime = new Time();
        endTime.parse3339(End);

        Time now = new Time();
        now.setToNow();

        if(startTime.toMillis(true) <= now.toMillis(true) &&
            now.toMillis(true) <= endTime.toMillis(true)) {
            return true;
        }

        return false;
    }

    public boolean isNow()
    {
        if(IsAllDay || IsCancelled) {
            return false;
        }

        //RFC3339
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        Time startTime = new Time();
        startTime.parse3339(Start);
        long startTimeMilli = startTime.toMillis(false);
        startTime.set(startTimeMilli);

        Time endTime = new Time();
        endTime.parse3339(End);

        Time now = new Time();
        now.setToNow();

        if(startTime.toMillis(true) <= now.toMillis(true) &&
                now.toMillis(true) <= endTime.toMillis(true)) {
            return true;
        }

        return false;
    }

    public boolean isUpcoming()
    {
        if(IsAllDay || IsCancelled) {
            return false;
        }

        //RFC3339
        //"Start": "2015-01-23T20:00:00Z",
        //"End": "2015-01-23T21:00:00Z",
        Time startTime = new Time();
        startTime.parse3339(Start);
        long startTimeMilli = startTime.toMillis(false);
        startTime.set(startTimeMilli);

        Time now = new Time();
        now.setToNow();

        if(startTime.toMillis(true) > now.toMillis(true)) {
            return true;
        }

        return false;
    }


    public EventItem(JSONObject event) throws JSONException {
        Subject = Helpers.TryGetJSONValue(event, "Subject");
        BodyPreview = Helpers.TryGetJSONValue(event, "BodyPreview");
        Start = Helpers.TryGetJSONValue(event, "Start");
        End = Helpers.TryGetJSONValue(event, "End");
        Type = Helpers.TryGetJSONValue(event, "Type");
        IsAllDay = Helpers.TryGetJSONValue(event, "IsAllDay") == "true" ? true : false;
        IsCancelled = Helpers.TryGetJSONValue(event, "IsCancelled") == "true" ? true : false;
        Importance = Helpers.TryGetJSONValue(event, "Importance");
        ShowAs = Helpers.TryGetJSONValue(event, "ShowAs");

        JSONObject objLocation = Helpers.TryGetJSONObject(event, "Location");
        if(objLocation == null){
            Location = null;
        } else {
            Location = new com.leibmann.android.myupcommingevents.Location(Helpers.TryGetJSONValue(objLocation, "DisplayName"));
        }

        JSONObject objOrganizer = Helpers.TryGetJSONObject(event, "Organizer");
        if(objOrganizer == null){
            Organizer = null;
        } else {
            JSONObject objEmailAddress = Helpers.TryGetJSONObject(objOrganizer, "EmailAddress");
            EmailAddress email = new EmailAddress(
                    Helpers.TryGetJSONValue(objEmailAddress, "Address"),
                    Helpers.TryGetJSONValue(objEmailAddress, "Name")
            );
            Organizer = new com.leibmann.android.myupcommingevents.Organizer(email);
        }
    }
}
