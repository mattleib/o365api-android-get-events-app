package com.leibmann.android.myupcommingevents;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;


/**
 * Created by mattleib on 1/29/2015.
 */
public class EventItemsFragment extends Fragment {

    private int mSelectedItemPosition = Constants.NO_ITEM_SELECTED;

    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        public void onDestroyActionMode(ActionMode mode) {
            mSelectedItemPosition = Constants.NO_ITEM_SELECTED;
            mode = null;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("Inform Organizer");
            mode.getMenuInflater().inflate(R.menu.contextual_list_actions, menu);
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            int id = item.getItemId();
            EventsAdapter adapter = (EventsAdapter) mCallback.getEventsAdapter();
            if(adapter == null) {
                return false;
            }

            EventItem e = (EventItem)adapter.getItem(mSelectedItemPosition);
            switch (id) {
                case R.id.ctx_action_cannot_make_it: {
                    mode.finish();
                    break;
                }
                case R.id.ctx_action_running_late: {
                    mode.finish();
                    break;
                }
                default:
                    return false;
            }
            return true;
        }
    };

    public interface EventRefresh{
        // Interface method you will call from this fragment
        public void onRefreshEvents();
        public EventsAdapter getEventsAdapter();
    }// end interface


    // Instantiate the new Interface Callback
    private EventRefresh mCallback = null;

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);
        try {
            // Attaches the Interface to the Activity
            mCallback = (EventRefresh) activity;
        } catch(Exception e) {
            e.printStackTrace();
        }
    }// end onAttach()

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        final SwipeRefreshLayout swipeView = (SwipeRefreshLayout) rootView.findViewById(R.id.swipe_container);
        final  ListView lView = (ListView) rootView.findViewById(R.id.eventItemList);

        swipeView.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeView.setRefreshing(true);
                ( new Handler()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mCallback.onRefreshEvents();
                        swipeView.setRefreshing(false);
                    }
                }, 1000);
            }
        });

        lView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem == 0) {
                    swipeView.setEnabled(true);
                } else {
                    swipeView.setEnabled(false);
                }
            }
        });

        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick (AdapterView parent, View view, int position, long id) {
                EventsAdapter adapter = (EventsAdapter) mCallback.getEventsAdapter();
                if(adapter == null) {
                    return false;
                }

                Item e = adapter.getItem(position);
                if(e == null || e.isItemType() != DataTypes.ItemType.event) {
                    return false;
                }

                getActivity().startActionMode(mActionModeCallback);
                mSelectedItemPosition = position;
                view.setSelected(true);
                return true;
            }
        });

        return rootView;
    }
}
