package com.example.mattleib.myinboxapplication;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.microsoft.aad.adal.AuthenticationCallback;
import com.microsoft.aad.adal.AuthenticationContext;
import com.microsoft.aad.adal.AuthenticationResult;
import com.microsoft.aad.adal.PromptBehavior;
import com.microsoft.aad.adal.UserInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity implements com.example.mattleib.myinboxapplication.EventItemsFragment.EventRefresh {

    /**
     * Name of the Module for Error display's etc.
     */
    private static String MODNAME = "MainActivity";

    /**
     * ADAL authentication context and result for app
     */
    private static AuthenticationContext mAuthContext = null;
    private static AuthenticationResult mCurrentAuthenticationResult = null;
    private static UserInfo mCurrentUser = null;

    /**
     * Adapter to fill the events list
     */
    private static EventsAdapter mEventsAdapter = null;

    /**
     * In memory holding of event items
     */
    private static ArrayList<Item> mEventsItems = null;

    /**
     * The activities menu
     */
    private static Menu mMenu = null;

    /**
     * Configuration for Runtime: PPE and PROD
     */
    private final static AppConfig[] mAppEnvironment = {
            new AppConfig(
                    Constants.AAD_Authority,
                    Constants.AAD_Client_ID,
                    Constants.AAD_RedirectUri,
                    Constants.O365_ExchangeOnline,
                    Constants.O365_UserHint,
                    Constants.O365_EventsQueryTemplate
            ),
            new AppConfig(
                    Constants.PPE_AAD_Authority,
                    Constants.PPE_AAD_Client_ID,
                    Constants.PPE_AAD_RedirectUri,
                    Constants.PPE_O365_ExchangeOnline,
                    Constants.PPE_O365_UserHint,
                    Constants.PPE_O365_EventsQueryTemplate
            )
    };
    private static int mAppEnvIndex = Constants.IDX_PROD;


    /**
     * Set the current environment the app operates on
     */
    private void GetCurrentAppEnvironmentSettings()
    {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean usePPE = sharedPreferences.getBoolean(Constants.PreferenceKeys.UsePPE, false);
        if (usePPE) {
            mAppEnvIndex = Constants.IDX_PPE;
        } else {
            mAppEnvIndex = Constants.IDX_PROD;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /**
         * Read default preferences. Initialize with defaults.
         */
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        if (savedInstanceState == null) {

            EventItemsFragment eventItemsFragment = new EventItemsFragment();

            eventItemsFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, eventItemsFragment)
                    .commit();
        }

        /**
         * Login, Get AccessToken, and Pre-fill the EventList
         */
        SignOn(true);
        Toast.makeText(getApplicationContext(), "Welcome. Let's get busy!", Toast.LENGTH_SHORT).show();
    }

    private void SignOn(boolean showProgressDialog) {
        /**
         * Read the current environment the app operates on
         */
        GetCurrentAppEnvironmentSettings();

        final ProgressDialog mLoginProgressDialog = new ProgressDialog(this);
        if(showProgressDialog) {
            mLoginProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            mLoginProgressDialog.setMessage("Login in progress...");
            mLoginProgressDialog.show();
        }
        // Ask for token and provide callback
        try {
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    mAppEnvironment[mAppEnvIndex].getAuthority(),
                    false,
                    InMemoryCacheStore.getInstance());

            // why? -- mAuthContext.getCache().removeAll();

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
                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }
                            SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                    "Failed to get token", exc.getMessage());
                        }

                        @Override
                        public void onSuccess(AuthenticationResult result) {
                            if (mLoginProgressDialog.isShowing()) {
                                mLoginProgressDialog.dismiss();
                            }

                            if (result != null && !result.getAccessToken().isEmpty()) {
                                mCurrentAuthenticationResult = result;
                                mCurrentUser = mCurrentAuthenticationResult.getUserInfo();
                                /**
                                 * Get Events for Today
                                 */
                                getAllEvents(true);
                            } else {
                                SimpleAlertDialog.showAlertDialog(MainActivity.this,
                                        "Failed to acquire token", "");
                            }
                        }
                    });
        } catch (Exception e) {
            SimpleAlertDialog.showAlertDialog(getApplicationContext(), "Exception caught", e.getMessage());
        }
    }

    private void SignOut() {
        if (mAuthContext != null) {
            mAuthContext.getCache().removeAll();
            mAuthContext = null;
            mCurrentAuthenticationResult = null;
        }

        if(mEventsAdapter != null) {
            mEventsAdapter.itemList.clear();
            mEventsAdapter.clear();
            mEventsAdapter.notifyDataSetChanged();
        }
    }

    private boolean RefreshToken() {
        if(mCurrentUser == null)
            return false;

        GetCurrentAppEnvironmentSettings();
        try {
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    mAppEnvironment[mAppEnvIndex].getAuthority(),
                    false,
                    InMemoryCacheStore.getInstance());

            mAuthContext.acquireTokenSilentSync(
                    mAppEnvironment[mAppEnvIndex].getResourceExchange(),
                    mAppEnvironment[mAppEnvIndex].getClientId(),
                    mCurrentUser.getUserId()
                    );
            return true;
        } catch (Exception e) {
            SimpleAlertDialog.showAlertDialog(getApplicationContext(), "Exception caught refreshing tokens", e.getMessage());
            return false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }

        if(requestCode == Constants.PICK_PREFERENCE_REQUEST){

            PreferenceSettings preference = (PreferenceSettings)data.getSerializableExtra(PreferenceSettings.SER_KEY);

            if(preference.getUsePPE().getHasChanged()) {
                if (preference.getUsePPE().getNewValue().equals("true")) {
                    mAppEnvIndex = Constants.IDX_PPE;
                } else {
                    mAppEnvIndex = Constants.IDX_PROD;
                }

                MenuItem menuLogin = mMenu.findItem(R.id.action_login);
                menuLogin.setVisible(true);

                MenuItem menuLogout = mMenu.findItem(R.id.action_logout);
                menuLogout.setVisible(false);

                SignOut();

                return;
            }
            if(preference.getEventTimeSpan().getHasChanged()) {
                getAllEvents(true);
                return;
            }
            if(preference.getUseCoolColor().getHasChanged()) {
                getAllEvents(false);
            }
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        boolean mOrientationChange;
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mOrientationChange = true;
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            mOrientationChange = true;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            /**
             * Start preferences selection
             */
            startActivityForResult(new Intent(this, SettingsActivity.class), Constants.PICK_PREFERENCE_REQUEST);
            return true;
        }

        if (id == R.id.action_logout) {
            item.setVisible(false);
            MenuItem m = mMenu.findItem(R.id.action_login);
            m.setVisible(true);

            SignOut();

            return true;
        }

        if (id == R.id.action_login) {
            item.setVisible(false);
            MenuItem m = mMenu.findItem(R.id.action_logout);
            m.setVisible(true);

            SignOn(true);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * The interface for fragment to notify to Refresh events
     */
    public void onRefreshEvents()
    {
        getAllEvents(true);
    }

    /**
     * Get the events and Re-Fill the list
     */
    private void getAllEvents(boolean requery)
    {
        if(!RefreshToken())
            return;

        ArrayList<Item> eventsList;
        if(!requery
                && mEventsItems != null
                && !(mEventsItems.isEmpty())
                ) {
            eventsList = mEventsItems;
        } else {
            eventsList = new ArrayList<Item>();
            requery = true;
        }

        /**
         * Get events and fill the list
         */
        mEventsAdapter = new EventsAdapter(eventsList, getApplicationContext());
        ListView lView = (ListView) findViewById(R.id.eventItemList);
        lView.setAdapter(mEventsAdapter);

        /**
         * Only force a refresh of the current screen,
         * but don't request new events from Office 365
         */
        if(!requery) {
            mEventsAdapter.notifyDataSetChanged();
            return;
        }

        /**
         * Get the preference and build the query to receive events from Office 365 APIs
         */
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String eventSpan = sharedPreferences.getString(Constants.PreferenceKeys.CalendarTimeSpan, "day");
        String eventsQuery = "";

        if(eventSpan.equals("week")) { /// week

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    DataTypes.EventTimeSpan.Week);

        } else if (eventSpan.equals("month")) { /// month

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    DataTypes.EventTimeSpan.Month);
        } else {

            eventsQuery = Helpers.GetEventsQueryString(
                    mAppEnvironment[mAppEnvIndex].getEventsQueryTemplate(),
                    DataTypes.EventTimeSpan.Day);
        }

        /**
         * Exec async load task
         */
        (new GetContactsListAsync()).execute(
                eventsQuery,
                mCurrentAuthenticationResult.getAccessToken()
                );
    }

    /**
     * Adapter to get Events
     */
    public class EventsAdapter extends ArrayAdapter<Item> {

        private ArrayList<Item> itemList;
        private Context context;
        private LayoutInflater vi;

        public EventsAdapter(ArrayList itemList, Context ctx) {
            super(ctx, android.R.layout.simple_list_item_1, itemList);
            this.itemList = itemList;
            this.context = ctx;
            this.vi = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            if (itemList != null)
                return itemList.size();
            return 0;
        }

        public Item getItem(int position) {
            if (itemList != null)
                return itemList.get(position);
            return null;
        }

        public long getItemId(int position) {
            if (itemList != null)
                return itemList.get(position).hashCode();
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;

            final Item i = getItem(position);
            if(i == null)
               return v;

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            boolean useCoolColors = preferences.getBoolean(Constants.PreferenceKeys.UseCoolColors, false);

            if(i.isItemType() == DataTypes.ItemType.section) {
                v = vi.inflate(R.layout.eventsection_row_layout, null);

                if(useCoolColors) {
                    v.setBackgroundColor(getResources().getColor(R.color.Event_Separator_Cool));
                } else {
                    v.setBackgroundColor(getResources().getColor(R.color.Event_Separator_Warm));
                }

                SectionItem si = (SectionItem)i;
                LocalDateTimeConverter startTime = new LocalDateTimeConverter(si.getUtcEventStartTime());

                TextView text = (TextView) v.findViewById(R.id.day_of_week);
                text.setText(
                        startTime.getLocalDayOfWeekString() + ",  " + startTime.getLocalDayString()
                );

            } else if (i.isItemType() == DataTypes.ItemType.event) {
                v = vi.inflate(R.layout.eventitem_row_layout, null);

                EventItem e = (EventItem) i;
                LocalDateTimeConverter startTime = new LocalDateTimeConverter(e.getStart());
                LocalDateTimeConverter endTime = new LocalDateTimeConverter(e.getEnd());

                if (startTime.IsAm()) {
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Normal_AM_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Normal_AM_Warm));
                    }
                } else {
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Normal_PM_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Normal_PM_Warm));
                    }
                }

                TextView subject = (TextView) v.findViewById(R.id.subject);
                subject.setText(e.getSubject());

                TextView start = (TextView) v.findViewById(R.id.startend);
                String localStartTime = startTime.getLocalTimeString();
                String localEndTime = endTime.getLocalTimeString();
                String localEndDay = endTime.getLocalDayString();
                if (e.IsAllDay) {
                    localEndDay = "All Day Event";
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_AllDay_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_AllDay_Warm));
                    }
                }
                start.setText(localStartTime + " to " + localEndTime + " (" + localEndDay + ")");

                TextView location = (TextView) v.findViewById(R.id.location);
                if (e.getLocation() == null) {
                    location.setVisibility(View.GONE);
                } else if (e.getLocation().getDisplayName().isEmpty()) {
                    location.setVisibility(View.GONE);
                } else {
                    location.setText("Location: " + e.getLocation().getDisplayName());
                }

                TextView organizer = (TextView) v.findViewById(R.id.organizer);
                if (e.getOrganizer() == null) {
                    organizer.setVisibility(View.GONE);
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_OnMyOwn_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_OnMyOwn_Warm));
                    }
                } else if (e.getOrganizer().getEmailAddress().getName().isEmpty()) {
                    organizer.setVisibility(View.GONE);
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_OnMyOwn_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_OnMyOwn_Warm));
                    }
                } else {
                    organizer.setText("Organizer: " + e.getOrganizer().getEmailAddress().getName());
                }

                if (e.getIsCancelled()) {
                    if(useCoolColors) {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Canceled_Cool));
                    } else {
                        v.setBackgroundColor(getResources().getColor(R.color.Event_Canceled_Warm));
                    }
                }

            } else { // empty Item
                v = vi.inflate(R.layout.empty_row_layout, null);

                EmptyItem e = (EmptyItem) i;
                if(!e.getErrorMessage().isEmpty())
                {
                    TextView text = (TextView) v.findViewById(R.id.no_events_text);
                    String currentText = text.getText().toString();
                    text.setText(currentText + " Error: " + e.getErrorMessage());
                }

                if(useCoolColors) {
                    v.setBackgroundColor(getResources().getColor(R.color.Event_Separator_Cool));
                } else {
                    v.setBackgroundColor(getResources().getColor(R.color.Event_Separator_Warm));
                }
            }
            return v;
        }

        public ArrayList<Item> getItemList() {
            return itemList;
        }

        public void setItemList(ArrayList<Item> itemList) {
            this.itemList = itemList;
        }
    }

    /**
     * Load Events in Background
     */
    public class GetContactsListAsync extends AsyncTask<String, Void, ArrayList<Item>> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);
        private String mLastSectionDate = "ForceDisplay";

        @Override
        protected void onPostExecute(ArrayList<Item> result) {
            super.onPostExecute(result);
            dialog.dismiss();
            mEventsAdapter.setItemList(result);
            mEventsAdapter.notifyDataSetChanged();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog.setMessage("Downloading events...");
            dialog.show();
        }

        @Override
        protected ArrayList<Item> doInBackground(String... params) {

            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                ArrayList<Item> items = new ArrayList();

                URL url = new URL(params[0]);
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json; odata.metadata=none");
                conn.setRequestProperty("User-Agent", "MSOAuthPlayground/1.0");
                conn.setRequestProperty("Authorization", "Bearer " + params[1]);
                if (conn.getResponseCode() != 200) {
                    throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());
                }

                br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                String apiOutput = br.readLine();
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

                if(items.isEmpty()) {
                    items.add(new EmptyItem());
                }

                // remember the last result ser for refresh
                mEventsItems = items;

                return items;

            } catch (Exception e) {

                ArrayList<Item> items = new ArrayList();
                items.add(new EmptyItem(e.getMessage()));

                // remember the last result ser for refresh
                mEventsItems = items;

                return items;

            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

}
