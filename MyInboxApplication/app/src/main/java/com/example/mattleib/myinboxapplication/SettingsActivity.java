package com.example.mattleib.myinboxapplication;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Activity;


public class SettingsActivity extends Activity
        implements SettingsFragment.EventTimespanChanged {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    /**
     * The interface for preference Fragment to notify to Refresh events
     */
    public void onNewTimeSpan(String timeSpan) {
        String s = timeSpan;
    }
}
