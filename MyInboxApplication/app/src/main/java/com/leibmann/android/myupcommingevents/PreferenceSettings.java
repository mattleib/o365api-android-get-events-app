//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
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
    private PreferenceSetting DoNotifications;

    public PreferenceSetting getDoNotShowPastEvents() {
        return DoNotShowPastEvents;
    }

    public PreferenceSetting getDoNotifications() { return DoNotifications; }

    public PreferenceSetting getUseCoolColor() {
        return UseCoolColor;
    }

    public PreferenceSetting getUsePPE() {
        return UsePPE;
    }

    public PreferenceSetting getEventTimeSpan() {
        return EventTimeSpan;
    }

    public PreferenceSettings(PreferenceSetting eventTimeSpan,
                              PreferenceSetting usePPE,
                              PreferenceSetting useCoolColor,
                              PreferenceSetting doNotShowPastEvents,
                              PreferenceSetting doNotifications) {
        UsePPE = usePPE;
        EventTimeSpan = eventTimeSpan;
        UseCoolColor = useCoolColor;
        DoNotShowPastEvents = doNotShowPastEvents;
        DoNotifications = doNotifications;
    }
}
