//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;


public class SettingsActivity extends Activity
        implements SettingsFragment.EventTimespanChanged {

    private static PreferenceSettings mPreferenceSettings = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String eventSpan = sharedPreferences.getString(Constants.PreferenceKeys.CalendarTimeSpan, "next7days");
        Boolean usePPE = sharedPreferences.getBoolean(Constants.PreferenceKeys.UsePPE, false);
        Boolean useCoolColors = sharedPreferences.getBoolean(Constants.PreferenceKeys.UseCoolColors, false);
        Boolean doNotShowPastEvents = sharedPreferences.getBoolean(Constants.PreferenceKeys.DoNotShowPastEvents, false);
        Boolean doNotifications = sharedPreferences.getBoolean(Constants.PreferenceKeys.DoNotifications, true);

        mPreferenceSettings = new PreferenceSettings(
                new PreferenceSetting(eventSpan, eventSpan),
                new PreferenceSetting(usePPE.toString(), usePPE.toString()),
                new PreferenceSetting(useCoolColors.toString(), useCoolColors.toString()),
                new PreferenceSetting(doNotShowPastEvents.toString(), doNotShowPastEvents.toString()),
                new PreferenceSetting(doNotifications.toString(), doNotifications.toString())
        );
        setIntent();
    }

    /**
     * The interface for preference Fragment to notify to Refresh events
     */
    private void setIntent()
    {
        Intent returnIntent = new Intent();
        Bundle mBundle = new Bundle();
        mBundle.putSerializable(PreferenceSettings.SER_KEY, mPreferenceSettings);
        returnIntent.putExtras(mBundle);

        setResult(Constants.PICK_PREFERENCE_REQUEST, returnIntent);
    }

    public void onNewTimeSpan(String timeSpan) {
        mPreferenceSettings.getEventTimeSpan().setNewValue(timeSpan);
        setIntent();
    }

    public void onEnvironmentChanged(Boolean usePPE) {
        mPreferenceSettings.getUsePPE().setNewValue(usePPE.toString());
        setIntent();
    }

    public void onColorChanged(Boolean useCool) {
        mPreferenceSettings.getUseCoolColor().setNewValue(useCool.toString());
        setIntent();
    }

    public void onNoPastEventsChanged(Boolean doNotShowPastEvents) {
        mPreferenceSettings.getDoNotShowPastEvents().setNewValue(doNotShowPastEvents.toString());
        setIntent();
    }

    public void onDoNotifications(Boolean doNotifications) {
        mPreferenceSettings.getDoNotifications().setNewValue(doNotifications.toString());
        setIntent();
    }
}
// MIT License:

// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// ""Software""), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:

// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.

// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.