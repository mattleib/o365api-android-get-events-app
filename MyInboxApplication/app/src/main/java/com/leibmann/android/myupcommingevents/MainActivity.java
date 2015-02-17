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
    private static String mAuthority = ""; // remember the authority for this session

    private static AlarmManager mAlarmManager = null; // Alarm manager for the event notification service
    private static PendingIntent mPendingIntent = null; // pending intent for event notification receiver

    //
    // Set the current environment the app operates on
    //
    private void GetCurrentAppEnvironmentSettings()
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

    public void SaveUserTenant(String tenantId)
    {
        Log.d(TAG, Helpers.LogEnterMethod("SaveUserTenant"));

        SharedPreferences appPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString(Constants.PreferenceKeys.UserTenant, tenantId);
        editor.commit();

        Log.d(TAG, Helpers.LogLeaveMethod("SaveUserTenant"));
    }

    public String GetUserTenant()
    {
        Log.d(TAG, Helpers.LogEnterMethod("GetUserTenant"));

        SharedPreferences appPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        String userTenant = appPreferences.getString(Constants.PreferenceKeys.UserTenant, Constants.UserTenantDefault);

        Log.d(TAG, Helpers.LogLeaveMethod("GetUserTenant"));
        return userTenant;
    }

    public void SaveRefreshToken(String refreshToken)
    {
        Log.d(TAG, Helpers.LogEnterMethod("SaveRefreshToken"));

        SharedPreferences appPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        SharedPreferences.Editor editor = appPreferences.edit();
        editor.putString(Constants.PreferenceKeys.RefreshToken, refreshToken);
        editor.commit();

        Log.d(TAG, Helpers.LogLeaveMethod("SaveRefreshToken"));
    }

    public String GetRefreshToken()
    {
        Log.d(TAG, Helpers.LogEnterMethod("GetRefreshToken"));

        SharedPreferences appPreferences = getSharedPreferences(Constants.PREFS_NAME, 0);
        String refreshToken = appPreferences.getString(Constants.PreferenceKeys.RefreshToken, "");

        Log.d(TAG, Helpers.LogLeaveMethod("GetRefreshToken"));
        return refreshToken;
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
        SignOn(true);

        //
        // Create Intent for Event Notifications Receiver
        //
        Intent intent = new Intent(MainActivity.this, NotificationAlarmReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(MainActivity.this, 0, intent, 0);

        //
        // Start alarm
        //
        startAlarmForEventNotifications();

        Toast.makeText(getApplicationContext(), "Welcome. Let's get busy!", Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("onCreate"));
    }

    private void SignOn(boolean showProgressDialog)
    {
        Log.d(TAG, Helpers.LogEnterMethod("SignOn"));

        //
        // Read the current environment the app operates on
        //
        GetCurrentAppEnvironmentSettings();

        final ProgressDialog mLoginProgressDialog = new ProgressDialog(this);
        if(showProgressDialog) {
            mLoginProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mLoginProgressDialog.setMessage("Login in progress...");
            mLoginProgressDialog.show();
        }
        // Ask for token and provide callback
        try {
            mAuthority = mAppEnvironment[mAppEnvIndex].getAuthority() + GetUserTenant();
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    mAuthority,
                    false); // use built in cache
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
                                    "Authorization Server returned a failure", exc.getMessage());
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

                                    SaveRefreshToken(mCurrentAuthenticationResult.getRefreshToken());
                                    SaveUserTenant(result.getTenantId());

                                    getEvents = true;

                                } else {
                                    SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                            "Failed to acquire token", "Authorization Server returned success code but no result");
                                }
                            }
                            catch(Exception e)
                            {
                                SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                        "Failed to acquire token", "Authorization Server returned success code but no token");
                                SignOut();
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

            SimpleAlertDialog.showAlertDialog(getApplicationContext(), "Exception caught", e.getMessage());
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

    private void SignOut()
    {
        Log.d(TAG, Helpers.LogEnterMethod("SignOut"));

        if (mAuthContext != null) {
            mAuthContext.getCache().removeAll();
            mAuthContext = null;
            mCurrentAuthenticationResult = null;
        }

        if(mEventsAdapter != null) {
            mEventsAdapter.clearItemList();
            mEventsAdapter.clear();
            mEventsAdapter.notifyDataSetChanged();
        }

        toggleLoginMenuAction(true);

        Log.d(TAG, Helpers.LogLeaveMethod("SignOut"));
    }

    private boolean RefreshToken()
    {
        Log.d(TAG, Helpers.LogEnterMethod("RefreshToken"));

        if(mCurrentUser == null) {
            Log.d(TAG, Helpers.LogInMethod("RefreshToken") + "::mCurrentUser == null");
            return false;
        }

        GetCurrentAppEnvironmentSettings();
        try {
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    mAuthority,
                    false);
                    //InMemoryCacheStore.getInstance());

            mCurrentAuthenticationResult = mAuthContext.acquireTokenSilentSync(
                    mAppEnvironment[mAppEnvIndex].getResourceExchange(),
                    mAppEnvironment[mAppEnvIndex].getClientId(),
                    mCurrentUser.getUserId()
                    );

            return true;

        } catch (Exception e) {
            Log.d(TAG, Helpers.LogInMethod("RefreshToken") + "::Exception", e);

            SimpleAlertDialog.showAlertDialog(getApplicationContext(), "Exception caught refreshing tokens", e.getMessage());
            return false;
        }
        finally {
            Log.d(TAG, Helpers.LogLeaveMethod("RefreshToken"));
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
                startAlarmForEventNotifications();
            }

            if(preference.getUsePPE().getHasChanged()) {
                if (preference.getUsePPE().getNewValue().equals("true")) {
                    mAppEnvIndex = Constants.IDX_PPE;
                } else {
                    mAppEnvIndex = Constants.IDX_PROD;
                }

                SignOut();

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
            item.setVisible(false);

            SignOut();
        }
        else if (id == R.id.action_login) {
            item.setVisible(false);

            SignOn(true);
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

    private class SendEmailAsync extends AsyncTask<String, Void, Void> {

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

        if(!RefreshToken()) {
            Log.d(TAG, Helpers.LogInMethod("sendEmail") + "::No New AccessToken. Re-signon");
            SignOn(false);

            Log.d(TAG, Helpers.LogLeaveMethod("sendEmail"));
            return;
        }

        if(eventItem.getOrganizer() == null) // nothing todo
            return;

        LocalDateTimeConverter startTime = new LocalDateTimeConverter(eventItem.getStart());

        String subject = "";
        String body = "";
        if(emailType == EmailInfoType.RunningLate) {
            subject = "Running late: ";
            body = "I'll be a bit late, but I'm on my way for ";
            body = body + "(" + eventItem.getSubject() + ") at " + startTime.getLocalTimeString() + " on " + startTime.getLocalDayString() + ". See you soon.";
        } else {
            subject = "Cannot make the meeting: ";
            body = "Sorry, I can't make it to the meeting ";
            body = body + "(" + eventItem.getSubject() + ") at " + startTime.getLocalTimeString() + " on " + startTime.getLocalDayString() + ".";
        }
        subject = subject + eventItem.getSubject();


        new SendEmailAsync().execute(
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

        Toast.makeText(getApplicationContext(), "Email sent to " + name, Toast.LENGTH_SHORT).show();

        Log.d(TAG, Helpers.LogLeaveMethod("sendEmail"));
    }


    //
    // Get the events and Re-Fill the list
    //
    private void getAllEvents(boolean requery)
    {
        Log.d(TAG, Helpers.LogEnterMethod("getAllEvents"));

        if(!RefreshToken()) {
            Log.d(TAG, Helpers.LogInMethod("getAllEvents") + "::No New AccessToken. Re-signon");
            SignOn(false);

            Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents"));
            return;
        }

        ArrayList<Item> eventsList;
        if(!requery
                && mEventsItems != null
                && !(mEventsItems.isEmpty())
                ) {
            Log.d(TAG, Helpers.LogInMethod("getAllEvents") + "::Re-using mEventsItems" );
            eventsList = mEventsItems;
        } else {
            Log.d(TAG, Helpers.LogInMethod("getAllEvents") + "::Need to fetch new event items" );
            eventsList = new ArrayList<Item>();
            requery = true;
        }

        //
        // Get events and fill the list
        //
        mEventsAdapter = new EventsAdapter(eventsList, getApplicationContext());
        ListView listView = (ListView) findViewById(R.id.eventItemList);
        listView.setAdapter(mEventsAdapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Toast.makeText(getApplicationContext(),
                        "Refresh display", Toast.LENGTH_SHORT)
                        .show();
                getAllEvents(false);
            }
        });

        //
        // Only force a refresh of the current screen,
        // but don't request new events from Office 365
        //
        if(!requery) {
            mEventsAdapter.notifyDataSetChanged();

            Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents") + "::Notified Adapter to refresh");
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

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.NextSevenDays,
                    doNotShowPastEvents);

        } else if (eventSpan.equals("next30days")) { /// month

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.NextThirtyDays,
                    doNotShowPastEvents);
        } else {

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    EventTimeSpan.Today,
                    doNotShowPastEvents);
        }

        // Read event items from cache and display immediately
        ArrayList<Item> items = EventsCache.read(getApplicationContext());
        if(items != null) {
            mEventsAdapter.setItemList(items);
            mEventsAdapter.notifyDataSetChanged();
        }

        //
        // Exec async load task to get Events from Office 365
        //
        (new GetContactsListAsync()).execute(
                eventsQuery,
                mCurrentAuthenticationResult.getAccessToken()
                );

        Log.d(TAG, Helpers.LogLeaveMethod("getAllEvents") + "::Background refresh of events submitted");
    }

    //
    // Load Events in Background
    //
    public class GetContactsListAsync extends AsyncTask<String, Void, ArrayList<Item>> {

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
                apiOutput = Office365API.getRequestForJSONResponse(restAPI + "&$skip=50", accessToken);
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
                EventsCache.write(items, getApplicationContext());

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


    private void doNullChecks()
    {
        Helpers.LogIfNull(TAG, mAuthContext, "mAuthContext");
        Helpers.LogIfNull(TAG, mCurrentAuthenticationResult, "mCurrentAuthenticationResult");
        Helpers.LogIfNull(TAG, mAuthority, "mAuthority");
        Helpers.LogIfNull(TAG, mCurrentUser, "mCurrentUser");
        Helpers.LogIfNull(TAG, mEventsAdapter, "mEventsAdapter");
        Helpers.LogIfNull(TAG, mEventsItems, "mEventsItems");
        Helpers.LogIfNull(TAG, mMenu, "mMenu");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // The activity has become visible (it is now "resumed").
        Log.d(TAG, Helpers.LogEnterMethod("onResume"));

        doNullChecks();

        Log.d(TAG, Helpers.LogLeaveMethod("onResume"));
    }

    private void startAlarmForEventNotifications(){
        Log.d(TAG, Helpers.LogEnterMethod("startAlarmForEventNotifications"));

        // First cancel the old alarm
        cancelAlarmForEventNotifications();

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean doNotifications = sharedPreferences.getBoolean(Constants.PreferenceKeys.DoNotifications, true);

        if(!doNotifications) {
            Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::Notifications turned off");
            return;
        }

        // Start a new alarm in 30 seconds to allow boot up time
        Calendar c = Calendar.getInstance();
        c.add(Calendar.SECOND, 30);
        long firstTime = c.getTimeInMillis();

        // Repeat every 10 minutes, but don't wake-up device
        mAlarmManager = (AlarmManager)getSystemService(ALARM_SERVICE);
        mAlarmManager.setInexactRepeating(
                AlarmManager.RTC,
                firstTime,
                (Constants.OneMinuteInMilliseconds*10),
                mPendingIntent);

        Log.d(TAG, Helpers.LogLeaveMethod("startAlarmForEventNotifications") + "::Alarm turned on");
    }

    private void cancelAlarmForEventNotifications() {
        if(mAlarmManager != null) {
            mAlarmManager.cancel(mPendingIntent);
            mAlarmManager = null;
        }
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
        cancelAlarmForEventNotifications();
    }

}
