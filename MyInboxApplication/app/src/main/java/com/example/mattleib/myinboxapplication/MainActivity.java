package com.example.mattleib.myinboxapplication;

import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

    /**
     * Adapter to fill the events list
     */
    private static EventsAdapter mEventsAdapter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            EventItemsFragment eventItemsFragment = new EventItemsFragment();

            eventItemsFragment.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, eventItemsFragment)
                    .commit();
        }

        /**
         * Login and get AccessToken
         */
        final ProgressDialog mLoginProgressDialog = new ProgressDialog(this);
        mLoginProgressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mLoginProgressDialog.setMessage("Login in progress...");
        mLoginProgressDialog.show();
        // Ask for token and provide callback
        try {
            mAuthContext = new AuthenticationContext(
                    MainActivity.this,
                    Constants.AAD_Authority,
                    false,
                    InMemoryCacheStore.getInstance());

            mAuthContext.getCache().removeAll();

            mAuthContext.acquireToken(
                    MainActivity.this,
                    Constants.O365_ExchangeOnline,
                    Constants.AAD_Client_ID,
                    Constants.AAD_RedirectUri,
                    Constants.O365_UserHint,
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
                                /**
                                 * Get Events for Today
                                 */
                                getAllEvents();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (mAuthContext != null) {
            mAuthContext.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            /**
             * Start preferences
             */
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            Intent intentSetPref = new Intent(getApplicationContext(), SettingsActivity.class);
            startActivity(new Intent(this, SettingsActivity.class));

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * The interface for fragment to notify to refresh events
     */
    public void refreshEvents()
    {
        getAllEvents();
    }

    /**
     * Get the events and Re-Fill the list
     */
    private void getAllEvents()
    {
        if(mCurrentAuthenticationResult == null ||
           mCurrentAuthenticationResult.getAccessToken().isEmpty())
            return; // Nothing to do

        /**
         * Get events and fill the list
         */
        mEventsAdapter = new EventsAdapter(new ArrayList<EventItem>(), getApplicationContext());
        ListView lView = (ListView) findViewById(R.id.eventItemList);

        lView.setAdapter(mEventsAdapter);

        String eventsQuery = Helpers.GetEventsQueryString(DataTypes.EventTimeSpan.Day);
        /**
         * Exec async load task
         */
        (new GetContactsListAsync()).execute(
                eventsQuery,
                mCurrentAuthenticationResult.getAccessToken()
                );

        Toast.makeText(getApplicationContext(), MODNAME + "::GetAllEvents.Complete", Toast.LENGTH_SHORT).show();
    }

    /**
     * Adapter to get Events
     */
    public class EventsAdapter extends ArrayAdapter<EventItem> {

        private List<EventItem> itemList;
        private Context context;
        private String mDate = "ForceDisplay";

        public EventsAdapter(List<EventItem> itemList, Context ctx) {
            super(ctx, android.R.layout.simple_list_item_1, itemList);
            this.itemList = itemList;
            this.context = ctx;
        }

        public int getCount() {
            if (itemList != null)
                return itemList.size();
            return 0;
        }

        public EventItem getItem(int position) {
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
            if (v == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                v = inflater.inflate(R.layout.eventitem_row_layout, null);
            }

            v.setBackgroundColor(getResources().getColor(R.color.Event_Normal));

            EventItem e = itemList.get(position);

            TextView text4 = (TextView) v.findViewById(R.id.organizer);
            if(e.getOrganizer() == null) {
                text4.setVisibility(View.GONE);
                v.setBackgroundColor(getResources().getColor(R.color.Event_OnMyOwn));
            } else {
                text4.setText("Organizer: " + e.getOrganizer().getEmailAddress().getName());
            }

            String eventDay = Helpers.ConvertUtcDateToLocalDay(e.getStart());
            TextView separator = (TextView) v.findViewById(R.id.date_separator);
            if(eventDay.equals(mDate)) {
                separator.setVisibility(View.GONE);
            } else {
                separator.setText(eventDay);
                mDate = eventDay;
            }

            TextView text = (TextView) v.findViewById(R.id.subject);
            text.setText(e.getSubject());

            TextView text1 = (TextView) v.findViewById(R.id.start);
            String localTime = Helpers.ConvertUtcDateToLocalTime(e.getStart());
            text1.setText(localTime);

            TextView text2 = (TextView) v.findViewById(R.id.end);
            if(e.IsAllDay) {
                text2.setText("(All Day Event)");
                v.setBackgroundColor(getResources().getColor(R.color.Event_AllDay));
            } else {
                localTime = Helpers.ConvertUtcDateToLocalTime(e.getEnd());
                text2.setText("("+localTime+")");
            }

            TextView text3 = (TextView) v.findViewById(R.id.location);
            if(e.getLocation() == null) {
                text3.setVisibility(View.GONE);
            } else {
                text3.setText("Location: " + e.getLocation().getDisplayName());
            }

            if(e.getIsCancelled()) {
                v.setBackgroundColor(getResources().getColor(R.color.Event_Canceled));
            }

            return v;
        }

        public List<EventItem> getItemList() {
            return itemList;
        }

        public void setItemList(List<EventItem> itemList) {
            this.itemList = itemList;
        }
    }

    /**
     * Load Events in Background
     */
    public class GetContactsListAsync extends AsyncTask<String, Void, List<EventItem>> {

        private final ProgressDialog dialog = new ProgressDialog(MainActivity.this);

        @Override
        protected void onPostExecute(List<EventItem> result) {
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
        protected List<EventItem> doInBackground(String... params) {

            HttpURLConnection conn = null;
            BufferedReader br = null;
            try {
                List<EventItem> items = new ArrayList<EventItem>();

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
                    items.add(new EventItem(obj));
                }

                return items;
            } catch (Exception e) {
                return new ArrayList<>();
            } finally {
                AppHelper.close(br);
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }

}
