package com.leibmann.android.myupcommingevents;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by mattleib on 1/26/2015.
 */
public class EventItem implements Serializable, Item {

    public DataTypes.ItemType isItemType() {
        return DataTypes.ItemType.event;
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

    public void setSubject(String subject) {
        Subject = subject;
    }

    public String getBodyPreview() {
        return BodyPreview;
    }

    public void setBodyPreview(String bodyPreview) {
        BodyPreview = bodyPreview;
    }

    public String getStart() {
        return Start;
    }

    public void setStart(String start) {
        Start = start;
    }

    public String getEnd() {
        return End;
    }

    public void setEnd(String end) {
        End = end;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public com.leibmann.android.myupcommingevents.Organizer getOrganizer() {
        return Organizer;
    }

    public void setOrganizer(com.leibmann.android.myupcommingevents.Organizer organizer) {
        Organizer = organizer;
    }

    public com.leibmann.android.myupcommingevents.Location getLocation() {
        return Location;
    }

    public void setLocation(com.leibmann.android.myupcommingevents.Location location) {
        Location = location;
    }

    public Boolean getIsAllDay() {
        return IsAllDay;
    }

    public void setIsAllDay(Boolean isAllDay) {
        IsAllDay = isAllDay;
    }

    public Boolean getIsCancelled() {
        return IsCancelled;
    }

    public void setIsCancelled(Boolean isCancelled) {
        IsCancelled = isCancelled;
    }

    public String getImportance() {
        return Importance;
    }

    public void setImportance(String importance) {
        Importance = importance;
    }

    public String getShowAs() {
        return ShowAs;
    }

    public void setShowAs(String showAs) {
        ShowAs = showAs;
    }

    public EventItem(String subject, String bodyPreview, String start, String end, String type, com.leibmann.android.myupcommingevents.Organizer organizer, com.leibmann.android.myupcommingevents.Location location, Boolean isAllDay, Boolean isCancelled, String importance, String showAs) {
        Subject = subject;
        BodyPreview = bodyPreview;
        Start = start;
        End = end;
        Type = type;
        Organizer = organizer;
        Location = location;
        IsAllDay = isAllDay;
        IsCancelled = isCancelled;
        Importance = importance;
        ShowAs = showAs;
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
