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

        mPreferenceSettings = new PreferenceSettings(
                new PreferenceSetting(eventSpan, eventSpan),
                new PreferenceSetting(usePPE.toString(), usePPE.toString()),
                new PreferenceSetting(useCoolColors.toString(), useCoolColors.toString()),
                new PreferenceSetting(doNotShowPastEvents.toString(), doNotShowPastEvents.toString())
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
}
