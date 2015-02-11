package com.leibmann.android.myupcommingevents;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by mattleib on 2/9/2015.
 */
public class EventsAdapter extends ArrayAdapter<Item>
{
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

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        boolean useCoolColors = preferences.getBoolean(Constants.PreferenceKeys.UseCoolColors, false);

        if(i.isItemType() == ItemType.Section) {
            v = vi.inflate(R.layout.eventsection_row_layout, null);

            if(useCoolColors) {
                v.setBackgroundColor(context.getResources().getColor(R.color.Event_Separator_Cool));
            } else {
                v.setBackgroundColor(context.getResources().getColor(R.color.Event_Separator_Warm));
            }

            SectionItem si = (SectionItem)i;
            LocalDateTimeConverter startTime = new LocalDateTimeConverter(si.getUtcEventStartTime());

            TextView text = (TextView) v.findViewById(R.id.day_of_week);
            text.setText(
                    startTime.getLocalDayOfWeekString() + ",  " + startTime.getLocalDayString()
            );

        } else if (i.isItemType() == ItemType.Event) {
            v = vi.inflate(R.layout.eventitem_row_layout, null);

            EventItem e = (EventItem) i;
            LocalDateTimeConverter startTime = new LocalDateTimeConverter(e.getStart());
            LocalDateTimeConverter endTime = new LocalDateTimeConverter(e.getEnd());

            if (startTime.isAM()) {
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Normal_AM_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Normal_AM_Warm));
                }
            } else {
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Normal_PM_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Normal_PM_Warm));
                }
            }

            TextView subject = (TextView) v.findViewById(R.id.subject);
            if(Helpers.IsEventNow(e.getStart(), e.getEnd()) && !e.IsAllDay) {
                // add a visual cue that the event is now
                subject.setText(Constants.VisualCues.EventNow + e.getSubject());
            } else {
                subject.setText(e.getSubject());
            }

            TextView start = (TextView) v.findViewById(R.id.startend);
            String localStartTime = startTime.getLocalTimeString();
            String localEndTime = endTime.getLocalTimeString();
            String localEndDay = endTime.getLocalDayString();
            if (e.IsAllDay) {
                start.setText(context.getResources().getString(R.string.event_all_day));
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_AllDay_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_AllDay_Warm));
                }
            } else {
                String s = String.format(
                        context.getResources().getString(R.string.event_template_startdate),
                        localStartTime, localEndTime, localEndDay);
                start.setText(s);
            }


            TextView location = (TextView) v.findViewById(R.id.location);
            if (e.getLocation() == null) {
                location.setVisibility(View.GONE);
            } else if (e.getLocation().getDisplayName().isEmpty()) {
                location.setVisibility(View.GONE);
            } else {
                String s = String.format(
                        context.getResources().getString(R.string.event_template_location),
                        e.getLocation().getDisplayName());
                location.setText(s);
            }

            TextView organizer = (TextView) v.findViewById(R.id.organizer);
            if (e.getOrganizer() == null) {
                organizer.setVisibility(View.GONE);
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_OnMyOwn_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_OnMyOwn_Warm));
                }
            } else if (e.getOrganizer().getEmailAddress().getName().isEmpty()) {
                organizer.setVisibility(View.GONE);
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_OnMyOwn_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_OnMyOwn_Warm));
                }
            } else {
                String s = String.format(
                        context.getResources().getString(R.string.event_template_organizer),
                        e.getOrganizer().getEmailAddress().getName().isEmpty() ?
                                e.getOrganizer().getEmailAddress().getAddress() :
                                e.getOrganizer().getEmailAddress().getName());
                organizer.setText(s);
            }

            if (e.getIsCancelled()) {
                if(useCoolColors) {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Canceled_Cool));
                } else {
                    v.setBackgroundColor(context.getResources().getColor(R.color.Event_Canceled_Warm));
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
                v.setBackgroundColor(context.getResources().getColor(R.color.Event_Separator_Cool));
            } else {
                v.setBackgroundColor(context.getResources().getColor(R.color.Event_Separator_Warm));
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

    public void clearItemList() {
        this.itemList.clear();
    }
}


