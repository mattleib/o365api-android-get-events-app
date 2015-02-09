package com.leibmann.android.myupcommingevents;

import java.io.Serializable;

/**
 * Created by matthias on 1/31/2015.
 */
public class PreferenceSettings implements Serializable {

    public final static String SER_KEY = "com.example.mattleib.myinboxapplication.PreferenceSettings";

    private PreferenceSetting UsePPE;
    private PreferenceSetting EventTimeSpan;
    private PreferenceSetting UseCoolColor;
    private PreferenceSetting DoNotShowPastEvents;

    public PreferenceSetting getDoNotShowPastEvents() {
        return DoNotShowPastEvents;
    }

    public void setDoNotShowPastEvents(PreferenceSetting doNotShowPastEvents) {
        DoNotShowPastEvents = doNotShowPastEvents;
    }

    public PreferenceSetting getUseCoolColor() {
        return UseCoolColor;
    }

    public void setUseCoolColor(PreferenceSetting useCoolColor) {
        UseCoolColor = useCoolColor;
    }

    public PreferenceSetting getUsePPE() {
        return UsePPE;
    }

    public void setUsePPE(PreferenceSetting usePPE) {
        UsePPE = usePPE;
    }

    public PreferenceSetting getEventTimeSpan() {
        return EventTimeSpan;
    }

    public void setEventTimeSpan(PreferenceSetting eventTimeSpan) {
        EventTimeSpan = eventTimeSpan;
    }

    public PreferenceSettings(PreferenceSetting eventTimeSpan, PreferenceSetting usePPE, PreferenceSetting useCoolColor, PreferenceSetting doNotShowPastEvents) {
        UsePPE = usePPE;
        EventTimeSpan = eventTimeSpan;
        UseCoolColor = useCoolColor;
        DoNotShowPastEvents = doNotShowPastEvents;
    }
}
