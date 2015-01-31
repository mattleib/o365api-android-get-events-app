package com.example.mattleib.myinboxapplication;

import android.app.Activity;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     * Interface to signal timespan of the preferences have changed
     */
    public interface EventTimespanChanged{
        public void onNewTimeSpan(String timeSpan);
    }// end interface

    // Instantiate the Interface Callback
    private EventTimespanChanged mCallback = null;

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
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(Constants.PreferenceKeys.CalendarTimeSpan)) {
            String newValue = sharedPreferences.getString(key, "");
            mCallback.onNewTimeSpan(newValue);
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
