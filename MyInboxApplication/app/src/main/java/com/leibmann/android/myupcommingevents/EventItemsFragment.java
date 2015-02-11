package com.leibmann.android.myupcommingevents;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ActionMode;
import android.view.ContextMenu;
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

            EventItem eventItem = (EventItem) mCallback.getCurrentSelectedItem(mSelectedItemPosition);
            int id = item.getItemId();
            switch (id) {
                case R.id.ctx_action_cannot_make_it: {
                    mCallback.sendEmail(eventItem, DataTypes.EmailInformType.CannotMakeIt);
                    mode.finish();
                    break;
                }
                case R.id.ctx_action_running_late: {
                    mCallback.sendEmail(eventItem, DataTypes.EmailInformType.RunningLate);
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
        public Item getCurrentSelectedItem(int position);
        public void sendEmail(EventItem eventItem, DataTypes.EmailInformType emailType);
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

        // contextual floating menu
        registerForContextMenu(lView);

        /* no action bar context menu
        lView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            public boolean onItemLongClick (AdapterView parent, View view, int position, long id) {
                Item e = mCallback.getCurrentSelectedItem(position);
                if(e == null || e.isItemType() != DataTypes.ItemType.event) {
                    return false;
                }

                getActivity().startActionMode(mActionModeCallback);
                mSelectedItemPosition = position;
                view.setSelected(true);
                return true;
            }
        });
        */

        return rootView;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getActivity().getMenuInflater().inflate(R.menu.contextual_list_actions, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();

        Item e = mCallback.getCurrentSelectedItem(info.position);
        if(e == null || e.isItemType() != DataTypes.ItemType.event) {
            return false;
        }

        switch (item.getItemId()) {
            case R.id.ctx_action_running_late:
                mCallback.sendEmail((EventItem)e, DataTypes.EmailInformType.RunningLate);
                return true;
            case R.id.ctx_action_cannot_make_it:
                mCallback.sendEmail((EventItem)e, DataTypes.EmailInformType.CannotMakeIt);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }
}
