package com.leibmann.android.myupcommingevents;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    //
    // Interface to signal timespan of the preferences have changed
    //
    public interface EventTimespanChanged {
        public void onNewTimeSpan(String timeSpan);
        public void onEnvironmentChanged(Boolean usePPE);
        public void onColorChanged(Boolean useCool);
        public void onNoPastEventsChanged(Boolean doNotShowPastEvents);
    }// end interface

    // Instantiate the Interface Callback
    private EventTimespanChanged mCallback = null;

    // Text for eventSpan summary
    private String mEventsSpanSummary = "";

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try {
            // Attaches the Interface to the Activity
            mCallback = (EventTimespanChanged) activity;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }// end onAttach()


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        // adjust preference summary to current selected value
        Preference preference = findPreference(Constants.PreferenceKeys.CalendarTimeSpan);
        mEventsSpanSummary = preference.getSummary().toString();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
        String eventSpan = sharedPreferences.getString("PREF_CALENDAR_SPAN", "next7days");
        adjustEventSpanSummary(eventSpan);
    }

    private void adjustEventSpanSummary(String eventSpan)
    {
        Preference preference = findPreference(Constants.PreferenceKeys.CalendarTimeSpan);
        if(eventSpan.equals("today")) {
            preference.setSummary(mEventsSpanSummary + " today.");
        } else if (eventSpan.equals("next7days")) {
            preference.setSummary(mEventsSpanSummary + " next seven days.");
        } else {
            preference.setSummary(mEventsSpanSummary + " next 30 days.");
        }
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PreferenceKeys.CalendarTimeSpan)) {
            String newValue = sharedPreferences.getString(key, "");
            adjustEventSpanSummary(newValue);
            mCallback.onNewTimeSpan(newValue);
        } else if (key.equals(Constants.PreferenceKeys.UsePPE)) {
            Boolean newValue = sharedPreferences.getBoolean(key, false);
            mCallback.onEnvironmentChanged(newValue);
        } else if (key.equals(Constants.PreferenceKeys.UseCoolColors)) {
            Boolean newValue = sharedPreferences.getBoolean(key, false);
            mCallback.onColorChanged(newValue);
        } else if (key.equals(Constants.PreferenceKeys.DoNotShowPastEvents)) {
            Boolean newValue = sharedPreferences.getBoolean(key, false);
            mCallback.onNoPastEventsChanged(newValue);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

}
