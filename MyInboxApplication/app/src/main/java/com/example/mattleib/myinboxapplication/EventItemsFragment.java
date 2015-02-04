package com.example.mattleib.myinboxapplication;

import android.app.Activity;
// import android.app.Fragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Created by mattleib on 1/29/2015.
 */
public class EventItemsFragment extends Fragment {

    public interface EventRefresh{
        // Interface method you will call from this fragment
        public void onRefreshEvents();
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
                if (firstVisibleItem == 0)
                    swipeView.setEnabled(true);
                else
                    swipeView.setEnabled(false);
            }
        });

        return rootView;
    }
}
