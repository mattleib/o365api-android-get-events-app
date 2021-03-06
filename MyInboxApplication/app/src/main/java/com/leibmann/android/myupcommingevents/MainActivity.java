//Copyright (c) Microsoft. All rights reserved. Licensed under the MIT license. See full license at the bottom of this file.
//
package com.leibmann.android.myupcommingevents;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.aad.adal.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Calendar;


public class MainActivity extends ActionBarActivity implements EventItemsFragment.EventRefresh {

    //
    // Name of the Module for Logcat display's etc.
    //
    private final static String TAG = "MainActivity";

    //
    // ADAL authentication context and result for app
    ///
    private static AuthenticationContext mAuthContext = null;
    private static AuthenticationResult mCurrentAuthenticationResult = null;
    private static UserInfo mCurrentUser = null;

    //
    // Adapter to fill the events list
    //
    private static EventsAdapter mEventsAdapter = null;

    //
    // In memory holding of event items
    //
    private static ArrayList<Item> mEventsItems = null;

    //
    // The activities menu
    //
    private static Menu mMenu = null;

    //
    // Configuration for Runtime: PPE and PROD
    ///
    private final static AppConfig[] mAppEnvironment = {
            new AppConfig(
                    Constants.AAD_Authority,
                    Constants.AAD_Client_ID,
                    Constants.AAD_RedirectUri,
                    Constants.O365_ExchangeOnline,
                    Constants.O365_UserHint,
                    Constants.O365_EventsQueryTemplate,
                    Constants.O365_SendEmailUri
            ),
            new AppConfig(
                    Constants.PPE_AAD_Authority,
                    Constants.PPE_AAD_Client_ID,
                    Constants.PPE_AAD_RedirectUri,
                    Constants.PPE_O365_ExchangeOnline,
                    Constants.PPE_O365_UserHint,
                    Constants.PPE_O365_EventsQueryTemplate,
                    Constants.PPE_O365_SendEmailUri
            )
    };
    private static int mAppEnvIndex = Constants.IDX_PROD;
    // private static String mAuthority = ""; // remember the authority for this session

    // Alarm for event notifications
    private static NotificationAlarm mNotificationAlarm = null;
    private static PendingIntent mPendingIntent = null;

    //
    // Set the current environment the app operates on (PROD, PPE)
    //
    private void getCurrentAppEnvironmentSettings()
    {
        Log.d(TAG, Helpers.LogEnterMethod("GetCurrentAppEnvironmentSettings"));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean usePPE = sharedPreferences.getBoolean(Constants.PreferenceKeys.UsePPE, false);
        if (usePPE) {
            mAppEnvIndex = Constants.IDX_PPE;
        } else {
            mAppEnvIndex = Constants.IDX_PROD;
        }

        Log.d(TAG, Helpers.LogLeaveMethod("GetCurrentAppEnvironmentSettings"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, Helpers.LogEnterMethod("onCreate"));

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar_actionbar);
        setSupportActionBar(toolbar);

        //
        // Read default preferences. Initialize with defaults.
        //
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {

            EventItemsFragment eventItemsFragment = new EventItemsFragment();

            eventItemsFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, eventItemsFragment)
                    .commit();
        }

        //
        // Login, Get AccessToken, and Pre-fill the EventList
        //
        signOn(true);

        //
        // Create the notification alarm for sending event reminders as notifications
        //
        if(mNotificationAlarm == null) {
            mNotificationAlarm = new NotificationAlarm(MainActivity.this);
        }

        if(mPendingIntent == null) {
            Intent intent = new Intent(MainActivity.this, NotificationAlarmReceiver.class);
            mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);
        }

        Toast.makeText(MainActivity.this, "Welcome. Let's get busy!", Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("onCreate"));
    }

    private void signOn(boolean showProgressDialog)
    {
        Log.d(TAG, Helpers.LogEnterMethod("SignOn"));

        toggleLoginMenuAction(true);

        //
        // Read the current environment the app operates on
        //
        getCurrentAppEnvironmentSettings();

        final ProgressDialog mLoginProgressDialog = new ProgressDialog(this);
        if(showProgressDialog) {
            mLoginProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mLoginProgressDialog.setMessage("Login in progress...");
            mLoginProgressDialog.show();
        }
        // Ask for token and provide callback
        try {
            String userTenant = Helpers.getPreferenceValue(
                    Constants.PreferenceKeys.UserTenant,
                    Constants.UserTenantDefault,
                    MainActivity.this
            );
            String authority = mAppEnvironment[mAppEnvIndex].getAuthority() + userTenant;
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    authority,
                    false);
                    //InMemoryCacheStore.getInstance());

            mAuthContext.acquireToken(
                    MainActivity.this,
                    mAppEnvironment[mAppEnvIndex].getResourceExchange(),
                    mAppEnvironment[mAppEnvIndex].getClientId(),
                    mAppEnvironment[mAppEnvIndex].getRedirectUri(),
                    mAppEnvironment[mAppEnvIndex].getUserHint(),
                    PromptBehavior.Auto,
                    "",
                    new AuthenticationCallback<AuthenticationResult>() {

                        @Override
                        public void onError(Exception exc) {
                            Log.d(TAG, Helpers.LogInMethod("SignOn") + "::onError", exc);

                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }
                            SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                    "Failed to acquire token",
                                    "Authorization Server returned a failure" + exc.getMessage());

                            signOut();
                        }

                        @Override
                        public void onSuccess(AuthenticationResult result) {
                            Log.d(TAG, Helpers.LogInMethod("SignOn") + "::onSuccess");

                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }

                            boolean getEvents = false;
                            try {
                                if (result != null && !result.getAccessToken().isEmpty()) {
                                    mCurrentAuthenticationResult = result;
                                    mCurrentUser = mCurrentAuthenticationResult.getUserInfo();

                                    Helpers.savePreferenceValue(
                                            Constants.PreferenceKeys.RefreshToken,
                                            mCurrentAuthenticationResult.getRefreshToken(),
                                            MainActivity.this);

                                    Helpers.savePreferenceValue(
                                            Constants.PreferenceKeys.UserTenant,
                                            mCurrentAuthenticationResult.getTenantId(),
                                            MainActivity.this);

                                    getEvents = true;

                                } else {
                                    SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                            "Failed to acquire token",
                                            "Authorization Server returned success code but no result");

                                    signOut();
                                }
                            }
                            catch(Exception e)
                            {
                                SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                        "Failed to acquire token",
                                        "Authorization Server returned success code but no token");

                                signOut();
                            }

                            //
                            // Get Events for Today
                            //
                            if(getEvents) {
                                toggleLoginMenuAction(false);
                                getAllEvents(true);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, Helpers.LogInMethod("SignOn") + "::Exception", e);

            SimpleAlertDialog.showAlertDialog(MainActivity.this,
                    "Failed to acquire token",
                    "Exception caught" + e.getMessage());

            signOut();
        }
        finally {
            Log.d(TAG, Helpers.LogLeaveMethod("SignOn"));
        }
    }

    private void toggleLoginMenuAction(boolean showLogin)
    {
        Log.d(TAG, Helpers.LogEnterMethod("toggleLoginMenuAction"));

        if(mMenu != null) {
            MenuItem menuLogin = mMenu.findItem(R.id.action_login);
            menuLogin.setVisible(showLogin);

            MenuItem menuLogout = mMenu.findItem(R.id.action_logout);
            menuLogout.setVisible(!showLogin);
        }

        Log.d(TAG, Helpers.LogLeaveMethod("toggleLoginMenuAction"));
    }

    private void signOut()
    {
        Log.d(TAG, Helpers.LogEnterMethod("SignOut"));

        if (mAuthContext != null) {
            CookieSyncManager.createInstance(MainActivity.this);
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            CookieSyncManager.getInstance().sync();
            mAuthContext.getCache().removeAll();
            mAuthContext = null;
            mCurrentAuthenticationResult = null;
            mCurrentUser = null;
        }

        if(mEventsAdapter != null) {
            mEventsAdapter.clearItemList();
            mEventsAdapter.clear();
            mEventsAdapter.notifyDataSetChanged();
        }

        toggleLoginMenuAction(true);

        //Reset to common
        Helpers.savePreferenceValue(Constants.PreferenceKeys.UserTenant,
                Constants.UserTenantDefault, MainActivity.this);

        //Cancel all Alarms
        cancelAlarms();

        Log.d(TAG, Helpers.LogLeaveMethod("SignOut"));
    }

    private boolean refreshAccessToken()
    {
        Log.d(TAG, Helpers.LogEnterMethod("RefreshAccessToken"));

        if(mAuthContext == null) {
            Log.d(TAG, Helpers.LogLeaveMethod("RefreshAccessToken") + "::mAuthContext == null");
            return false;
        }

        if(mCurrentUser == null) {
            Log.d(TAG, Helpers.LogLeaveMethod("RefreshAccessToken") + "::mCurrentUser == null");
            return false;
        }

        try {
            getCurrentAppEnvironmentSettings();

            mCurrentAuthenticationResult = mAuthContext.acquireTokenSilentSync(
                    mAppEnvironment[mAppEnvIndex].getResourceExchange(),
                    mAppEnvironment[mAppEnvIndex].getClientId(),
                    mCurrentUser.getUserId()
                    );

            if(mCurrentAuthenticationResult.getStatus() == AuthenticationResult.AuthenticationStatus.Succeeded) {
                Log.d(TAG, Helpers.LogInMethod("RefreshAccessToken") + "::Got a new token");
                return true;
            }

            SimpleAlertDialog.showAlertDialog(MainActivity.this,
                    "Failed to acquire token",
                    "Could not get a new access tokens. Please Login again.");

            signOut();

            Log.d(TAG, Helpers.LogInMethod("RefreshAccessToken") + "::Failed getting a new token");
            return false;

        } catch (Exception e) {
            Log.d(TAG, Helpers.LogInMethod("RefreshAccessToken") + "::Exception", e);

            SimpleAlertDialog.showAlertDialog(MainActivity.this,
                    "Failed to acquire token",
                    "Could not get a new access tokens. Please Login again." + e.getMessage());

            signOut();

            return false;
        }
        finally {
            Log.d(TAG, Helpers.LogLeaveMethod("RefreshAccessToken"));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        Log.d(TAG, Helpers.LogEnterMethod("onActivityResult"));

        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }

        if(requestCode == Constants.PICK_PREFERENCE_REQUEST){

            PreferenceSettings preference = (PreferenceSettings)data.getSerializableExtra(PreferenceSettings.SER_KEY);

            if(preference.getDoNotifications().getHasChanged())
            {
                mNotificationAlarm.startAlarmForEventNotifications();
            }

            if(preference.getUsePPE().getHasChanged()) {
                if (preference.getUsePPE().getNewValue().equals("true")) {
                    mAppEnvIndex = Constants.IDX_PPE;
                } else {
                    mAppEnvIndex = Constants.IDX_PROD;
                }

                // clear the events cache
                EventsCache.write(new ArrayList<Item>(), MainActivity.this);

                //sign out
                signOut();

                Log.d(TAG, Helpers.LogLeaveMethod("onActivityResult"));
                return;
            }
            if(preference.getEventTimeSpan().getHasChanged()) {
                getAllEvents(true);

                Log.d(TAG, Helpers.LogLeaveMethod("onActivityResult"));
                return;
            }
            if(preference.getDoNotShowPastEvents().getHasChanged()) {
                getAllEvents(true);

                Log.d(TAG, Helpers.LogLeaveMethod("onActivityResult"));
                return;
            }
            if(preference.getUseCoolColor().getHasChanged()) {
                getAllEvents(false);

                Log.d(TAG, Helpers.LogLeaveMethod("onActivityResult"));
                return;
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        Log.d(TAG, Helpers.LogEnterMethod("onConfigurationChanged"));

        super.onConfigurationChanged(newConfig);

        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Toast.makeText(this, "Switching to Landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            //Toast.makeText(this, "Switching to Portrait", Toast.LENGTH_SHORT).show();
        }

        Log.d(TAG, Helpers.LogLeaveMethod("onConfigurationChanged"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        Log.d(TAG, Helpers.LogEnterMethod("onCreateOptionsMenu"));

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;

        Log.d(TAG, Helpers.LogLeaveMethod("onCreateOptionsMenu"));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        Log.d(TAG, Helpers.LogEnterMethod("onOptionsItemSelected"));

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            /**
             * Start preferences selection
             */
            startActivityForResult(new Intent(this, SettingsActivity.class), Constants.PICK_PREFERENCE_REQUEST);
        }
        else if (id == R.id.action_logout) {
            signOut();
        }
        else if (id == R.id.action_login) {
            signOn(true);
        }

        Log.d(TAG, Helpers.LogLeaveMethod("onOptionsItemSelected"));

        return super.onOptionsItemSelected(item);
    }

    //
    // The interface for fragment to notify to Refresh events
    //
    public void onRefreshEvents()
    {
        Log.d(TAG, Helpers.LogEnterMethod("onRefreshEvents"));

        getAllEvents(true);

        Log.d(TAG, Helpers.LogLeaveMethod("onRefreshEvents"));
    }

    public Item getCurrentSelectedItem(int position)
    {
        if(mEventsAdapter == null)
            return null;

        Item e = mEventsAdapter.getItem(position);
        if(e == null) {
            return null;
        }

        return e;
    }

    private class sendEmailAsync extends AsyncTask<String, Void, Void> {

        protected Void doInBackground(String... params) {
            try {
                Office365API.sendEmail(
                        params[0], //uri
                        params[1], //subject
                        params[2], //body
                        params[3], //name
                        params[4], //email
                        params[5] //accesstoken
                );
            }
            catch(Exception e) {
                Log.d(TAG, Helpers.LogInMethod("SendEmailAsync") + "::Exception", e);

            }
            finally {
                return null;
            }
        }
    }

    public void sendEmail(EventItem eventItem, EmailInfoType emailType)
    {
        Log.d(TAG, Helpers.LogEnterMethod("sendEmail"));

        if(!refreshAccessToken()) {
            Log.d(TAG, Helpers.LogLeaveMethod("sendEmail") + "::No new AccessToken. Re-signon");
            return;
        }

        if(eventItem.getOrganizer() == null) // nothing todo
            return;

        LocalDateTimeConverter startTime = new LocalDateTimeConverter(eventItem.getStart());

        String subject = "";
        String body = "";
        if(emailType == EmailInfoType.RunningLate) {
            subject = "Running late: " + eventItem.getSubject();
            body = "I'll be a bit late, but I'm on my way for the meeting ";
            body = body + "'" + eventItem.getSubject() + "' at " + startTime.getLocalTimeString() + " on " + startTime.getLocalDayString() + ". See you soon.";
        } else {
            subject = "Cannot make the meeting: " + eventItem.getSubject();
            body = "Sorry, I can't make it to the meeting ";
            body = body + "'" + eventItem.getSubject() + "' at " + startTime.getLocalTimeString() + " on " + startTime.getLocalDayString() + ".";
        }

        new sendEmailAsync().execute(
                mAppEnvironment[mAppEnvIndex].getSendEmailUri(),
                subject,
                body,
                eventItem.getOrganizer().getEmailAddress().getName(),
                eventItem.getOrganizer().getEmailAddress().getAddress(),
                mCurrentAuthenticationResult.getAccessToken()
        );

        String name = eventItem.getOrganizer().getEmailAddress().getName().isEmpty() ?
                eventItem.getOrganizer().getEmailAddress().getAddress() :
                eventItem.getOrganizer().getEmailAddress().getName();

        Toast.makeText(MainActivity.this, "Email sent to " + name, Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("sendEmail"));
    }


    //
    // Get the events and Re-Fill the list
    //
    private void getAllEvents(boolean requery)
    {
        Log.d(TAG, Helpers.LogEnterMethod("getAllEvents"));

        //
        // Get new events and fill the list
        //
        ArrayList<Item> eventsList = new ArrayList<Item>();
        mEventsAdapter = new EventsAdapter(eventsList, MainActivity.this);
        ListView listView = (ListView) findViewById(R.id.eventItemList);
        listView.setAdapter(mEventsAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(MainActivity.this,
                        "Refresh display", Toast.LENGTH_SHORT)
                        .show();
                getAllEvents(false);
            }
        });

        // Read event items from cache and display immediately
        eventsList = EventsCache.read(MainActivity.this);
        if(eventsList != null && !eventsList.isEmpty()) {
            mEventsItems = eventsList;
            mEventsAdapter.setItemList(eventsList);
            mEventsAdapter.notifyDataSetChanged();
            Log.d(TAG, Helpers.LogInMethod("getAllEvents") + "::Notified Adapter to refresh");
        }

        if(mEventsItems != null) {
            if (requery || mEventsItems.isEmpty()) {
                Log.d(TAG, Helpers.LogInMethod("getAllEvents") + "::Need to fetch new event items");
            } else {
                Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents") + "::Working from the cache. No requery.");
                return;
            }
        }

        // Get a fresh access token
        if(!refreshAccessToken()) {
            Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents") + "::No New AccessToken. Re-signon");
            return;
        }

        //
        // Get the preference and build the query to receive events from Office 365 APIs
        //
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String eventSpan = sharedPreferences.getString(Constants.PreferenceKeys.CalendarTimeSpan, "day");
        Boolean doNotShowPastEvents = sharedPreferences.getBoolean(Constants.PreferenceKeys.DoNotShowPastEvents, false);
        String eventsQuery = "";

        if(eventSpan.equals("next7days")) {

            eventsQuery = Helpers.getEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.NextSevenDays,
                    doNotShowPastEvents);

        } else if (eventSpan.equals("next30days")) { /// month

            eventsQuery = Helpers.getEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.NextThirtyDays,
                    doNotShowPastEvents);
        } else {

            eventsQuery = Helpers.getEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.Today,
                    doNotShowPastEvents);
        }

        //
        // Exec async load task to get Events from Office 365
        //
        (new getContactsListAsync()).execute(
                eventsQuery,
                mCurrentAuthenticationResult.getAccessToken()
                );

        Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents") + "::Background refresh of events submitted");
    }

    //
    // Load Events in Background
    //
    public class getContactsListAsync extends AsyncTask<String, Void, ArrayList<Item>> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        private String mLastSectionDate = "ForceDisplay";

        @Override
        protected void onPostExecute(ArrayList<Item> result) {

            Log.d(TAG, Helpers.LogEnterMethod("GetContactsListAsync") + "::onPostExecute");

            super.onPostExecute(result);
            dialog.dismiss();
            mEventsAdapter.setItemList(result);
            mEventsAdapter.notifyDataSetChanged();

            Log.d(TAG, Helpers.LogLeaveMethod("GetContactsListAsync") + "::onPostExecute");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, Helpers.LogEnterMethod("GetContactsListAsync") + "::onPreExecute");

            super.onPreExecute();
            dialog.setMessage("Pulling new events from Office 365...");
            dialog.show();

            Log.d(TAG, Helpers.LogLeaveMethod("GetContactsListAsync") + "::onPreExecute");
        }

        @Override
        protected ArrayList<Item> doInBackground(String... params) {

            Log.d(TAG, Helpers.LogEnterMethod("GetContactsListAsync") + "::doInBackground");

            try {
                String restAPI = params[0];
                String accessToken = params[1];

                // check if there is a data connection
                ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                Boolean isConnected = cm.getActiveNetworkInfo().isConnected();
                if(!isConnected) {
                    Log.d(TAG, Helpers.LogLeaveMethod("GetContactsListAsync") + "::NotConnectedtoNetwork");
                }

                // try to fetch events from Office 365
                ArrayList<Item> items = new ArrayList();

                // get first batch of 50
                String apiOutput = Office365API.getRequestForJSONResponse(restAPI + "&$top=50", accessToken);
                JSONObject obj = new JSONObject(apiOutput);
                JSONArray jsonArray = obj.getJSONArray("value");
                for (int i = 0; i < jsonArray.length(); i++) {
                    obj = jsonArray.getJSONObject(i);

                    EventItem event = new EventItem(obj) ;

                    // Do we need to add a section item?
                    String eventDay = (new LocalDateTimeConverter(event.getStart())).getLocalDayString();
                    if(!(eventDay.equals(mLastSectionDate))) {
                        mLastSectionDate = eventDay; // new Section
                        items.add(new SectionItem(event.getStart()));
                    }

                    items.add(event);
                }

                // get another batch of 50
                apiOutput = Office365API.getRequestForJSONResponse(restAPI + "&$skip=50&$top=50", accessToken);
                obj = new JSONObject(apiOutput);
                jsonArray = obj.getJSONArray("value");
                for (int i = 0; i < jsonArray.length(); i++) {
                    obj = jsonArray.getJSONObject(i);

                    EventItem event = new EventItem(obj) ;

                    // Do we need to add a section item?
                    String eventDay = (new LocalDateTimeConverter(event.getStart())).getLocalDayString();
                    if(!(eventDay.equals(mLastSectionDate))) {
                        mLastSectionDate = eventDay; // new Section
                        items.add(new SectionItem(event.getStart()));
                    }

                    items.add(event);
                }

                // done
                if(items.isEmpty()) {
                    items.add(new EmptyItem());
                }

                // remember the last result ser for refresh
                mEventsItems = items;

                // write event items to cache
                EventsCache.write(items, MainActivity.this);

                startAlarm();

                Log.d(TAG, Helpers.LogLeaveMethod("GetContactsListAsync") + "::doInBackground");
                return items;

            } catch (Exception e) {

                Log.d(TAG, Helpers.LogInMethod("GetContactsListAsync") + "::Exception", e);

                ArrayList<Item> items = new ArrayList();
                items.add(new EmptyItem(e.getMessage()));

                // remember the last result ser for refresh
                mEventsItems = items;

                Log.d(TAG, Helpers.LogLeaveMethod("GetContactsListAsync") + "::doInBackground");
                return items;

            }
        }
    }

    public static PendingIntent getAlarmPendingIntent() {
        return mPendingIntent;
    }

    public static void startAlarm() {
        if(mNotificationAlarm != null) {
            mNotificationAlarm.startAlarmForEventNotifications();
        }
    }
    public static void cancelAlarms() {
        if(mNotificationAlarm != null) {
            mNotificationAlarm.cancelAlarmForEventNotifications();
        }
    }

    private void doNullChecks()
    {
        Helpers.LogIfNull(TAG, mAuthContext, "mAuthContext");
        Helpers.LogIfNull(TAG, mCurrentAuthenticationResult, "mCurrentAuthenticationResult");
        Helpers.LogIfNull(TAG, mCurrentUser, "mCurrentUser");
        Helpers.LogIfNull(TAG, mEventsAdapter, "mEventsAdapter");
        Helpers.LogIfNull(TAG, mEventsItems, "mEventsItems");
        Helpers.LogIfNull(TAG, mMenu, "mMenu");
        Helpers.LogIfNull(TAG, mNotificationAlarm, "mNotificationAlarm");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        Log.d(TAG, Helpers.LogEnterMethod("onResume"));

        doNullChecks();
        if(mCurrentUser != null) {
            getAllEvents(false);
        }

        Log.d(TAG, Helpers.LogLeaveMethod("onResume"));
    }


    @Override
    protected void onStart() {
        super.onStart();
        // The activity is about to become visible.
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Another activity is taking focus (this activity is about to be "paused").
    }

    @Override
    protected void onStop() {
        super.onStop();
        // The activity is no longer visible (it is now "stopped")
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // The activity is about to be destroyed.
        cancelAlarms();
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