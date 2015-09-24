package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.adapter.FixedTravelAllowanceListAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.util.Const;

/**
 * Created by Patricius Komarnicki on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String CLASS_TAG = FixedTravelAllowanceListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG = CLASS_TAG + ".refreshAllowances";


    private IFragmentCallback callback;

    private FixedTravelAllowanceListAdapter adapter;

    /**
     * The listener to be notified, whenever a fixed travel allowance is selected
     */
    private IFixedTravelAllowanceSelectedListener fixedTASelectedListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FixedTravelAllowanceController allowanceController;

    /**
     * Container Activity to implement this interface in order to be notified
     * in case any fixed travel allowance was selected from the list
     */
    public interface IFixedTravelAllowanceSelectedListener {
        /**
         * Handles a travel allowance being selected
         * @param allowance The fixed travel allowance selected
         */
        public void onFixedTravelAllowanceSelected(FixedTravelAllowance allowance);
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fixed_travel_allowance_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        if (this.allowanceController == null) {
            ConcurCore app = (ConcurCore) activity.getApplicationContext();
            this.allowanceController = app.getTaController()
                    .getFixedTravelAllowanceController();
        }

        this.adapter = new FixedTravelAllowanceListAdapter(this.getActivity().getApplicationContext());
        setListAdapter(adapter);

        try {
            this.callback = (IFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement IFragmentCallback") ;
        }

        try {
            this.fixedTASelectedListener = (IFixedTravelAllowanceSelectedListener) activity;
        } catch (ClassCastException exception) {
            Log.e(Const.LOG_TAG, CLASS_TAG + ".onAttach: Container Activity must implement "
                    + IFixedTravelAllowanceSelectedListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        this.callback = null;
        this.fixedTASelectedListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshAdapter();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onListItemClick(ListView listView, View view, int position, long id) {
        if (listView != null) {
            FixedTravelAllowanceListAdapter listAdapter = (FixedTravelAllowanceListAdapter) listView.getAdapter();
            if (listAdapter.getItemViewType(position) == FixedTravelAllowanceListAdapter.ENTRY_ROW) {
                FixedTravelAllowance allowance = (FixedTravelAllowance) listAdapter.getItem(position);
                if (fixedTASelectedListener != null) {
                    fixedTASelectedListener.onFixedTravelAllowanceSelected(allowance);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onRefresh() {
        getListView().setEnabled(false);
        if (this.callback != null) {
            this.callback.handleFragmentMessage(ON_REFRESH_MSG, null);
        } else {
            onRefreshFinished();
        }
    }

    public void onRefreshFinished() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            adapter =  new FixedTravelAllowanceListAdapter(getActivity());
            setListAdapter(adapter);
            getListView().setEnabled(true);
        }
    }

    public void showRefreshIndicator() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(true);
            getListView().setEnabled(false);
        }
    }

    private void refreshAdapter() {
        if (allowanceController == null) {
            return;
        }
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshAdapter", "Refreshing adapter."));
        this.adapter.clear();
        this.adapter.addAll(this.allowanceController.getLocationsAndAllowances());
        adapter.notifyDataSetChanged();
    }

}