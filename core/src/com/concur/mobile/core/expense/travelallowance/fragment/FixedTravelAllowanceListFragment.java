package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.adapter.FixedTravelAllowanceListAdapter;
import com.concur.mobile.core.expense.travelallowance.adapter.RecyclerViewAdapter;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.AnimationUtil;
import com.concur.mobile.core.expense.travelallowance.util.BundleId;
import com.concur.mobile.core.expense.travelallowance.util.DebugUtils;
import com.concur.mobile.core.util.Const;

import java.util.Locale;

/**
 * Created by Patricius Komarnicki on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener, RecyclerViewAdapter.OnClickListener {

    private static final String CLASS_TAG = FixedTravelAllowanceListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG = CLASS_TAG + ".refreshAllowances";
    public static final String MSG_SELECTION_MODE_SWITCH = CLASS_TAG + ".selection.mode.switch";


    public static final String BUNDLE_ID_IN_SELECTION_MODE = "in.selection.mode";


    private IFragmentCallback callback;

    private FixedTravelAllowanceListAdapter adapter;

    /**
     * The listener to be notified, whenever a fixed travel allowance is selected
     */
    private IFixedTravelAllowanceSelectedListener fixedTASelectedListener;

    private SwipeRefreshLayout swipeRefreshLayout;

    private FixedTravelAllowanceController allowanceController;

    private boolean inSelectionMode;

    private boolean isEditMode;

    /**
     * Container Activity to implement this interface in order to be notified
     * in case any fixed travel allowance was selected from the list
     */
    public interface IFixedTravelAllowanceSelectedListener {
        /**
         * Handles a travel allowance being selected
         * @param allowance The fixed travel allowance selected
         */
        void onFixedTravelAllowanceSelected(FixedTravelAllowance allowance);

        void onMultiAdjust();
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (savedInstanceState == null) {
            inSelectionMode = false;
        } else {
            inSelectionMode = savedInstanceState.getBoolean(BUNDLE_ID_IN_SELECTION_MODE, false);
        }

        Bundle args = getArguments();
        if (args != null) {
            isEditMode = args.getBoolean(BundleId.IS_EDIT_MODE, true);
        }

        final View view = inflater.inflate(R.layout.ta_fixed_travel_allowance_list, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        initializeSelectionModeViews(view);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        this.adapter = new FixedTravelAllowanceListAdapter(this.getActivity(), this, inSelectionMode);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(this.adapter);

        if (!isEditMode) {
            view.findViewById(R.id.fab).setVisibility(View.GONE);
        }

        return view;
    }

    private void initializeSelectionModeViews(View v) {
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fab);
        Toolbar bottomToolbar = (Toolbar) v.findViewById(R.id.toolbar_bottom);
        View cancelButton = v.findViewById(R.id.cancel);
        View selectionCounter = v.findViewById(R.id.tv_selection_counter);

        fab.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        v.findViewById(R.id.btn_adjust).setOnClickListener(this);
        v.findViewById(R.id.btn_adjust_all).setOnClickListener(this);

        if (inSelectionMode) {
            fab.setVisibility(View.GONE);
            bottomToolbar.setVisibility(View.VISIBLE);
            selectionCounter.setVisibility(View.VISIBLE);
            updateSelectionCounter(v);
        } else {
            fab.setVisibility(View.VISIBLE);
            bottomToolbar.setVisibility(View.GONE);
            selectionCounter.setVisibility(View.GONE);
        }
    }
    
    private void updateSelectionCounter(View v) {
        if (v != null && allowanceController != null) {
            int count = allowanceController.getSelectedTravelAllowances().size();
            TextView counter = (TextView) v.findViewById(R.id.tv_selection_counter);
            counter.setText(getResources().getString(R.string.general_no_of_selected_list_items_android, count)
                    .toUpperCase(Locale.getDefault()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(BUNDLE_ID_IN_SELECTION_MODE, inSelectionMode);
        super.onSaveInstanceState(outState);
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
    public void onRefresh() {
        View v = getView();
        if (v == null) {
            return;
        }
        v.setEnabled(false);
        if (this.callback != null) {
            this.callback.handleFragmentMessage(ON_REFRESH_MSG, null);
        } else {
            onRefreshFinished();
        }
    }

    public void onRefreshFinished() {
        if (swipeRefreshLayout != null && getView() != null) {
            swipeRefreshLayout.setRefreshing(false);
            adapter =  new FixedTravelAllowanceListAdapter(getActivity(),this, inSelectionMode);
            RecyclerView recyclerView = getRecyclerView();
            if (recyclerView != null) {
                recyclerView.setAdapter(adapter);
                getView().setEnabled(true);
            }
        }
    }

    public void showRefreshIndicator() {
        if (swipeRefreshLayout != null && getView() != null) {
            swipeRefreshLayout.setRefreshing(true);
            getView().setEnabled(false);
        }
    }

    private void refreshAdapter() {
        if (allowanceController == null) {
            return;
        }
        Log.i(DebugUtils.LOG_TAG_TA, DebugUtils.buildLogText(CLASS_TAG, "refreshAdapter", "Refreshing adapter."));
        adapter =  new FixedTravelAllowanceListAdapter(getActivity(), this, inSelectionMode);
        RecyclerView recyclerView = getRecyclerView();
        if (recyclerView != null) {
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab) {
            switchToSelctionMode(true);
        }

        if (v.getId() == R.id.cancel) {
            switchToSelctionMode(false);
            allowanceController.unselectAll();
        }

        if (v.getId() == R.id.btn_adjust_all) {
            allowanceController.selectAll();
            if (fixedTASelectedListener != null && allowanceController.getSelectedTravelAllowances().size() > 0) {
                fixedTASelectedListener.onMultiAdjust();
            }
        }

        if (v.getId() == R.id.btn_adjust) {
            if (fixedTASelectedListener != null && allowanceController.getSelectedTravelAllowances().size() > 0) {
                fixedTASelectedListener.onMultiAdjust();
            }
        }
    }

    @Override
    public void onClick(View v, int position) {
        if (v.getId() == R.id.row && !inSelectionMode) {
            RecyclerView rv = getRecyclerView();
            if (rv != null) {
                FixedTravelAllowanceListAdapter adapter = (FixedTravelAllowanceListAdapter) rv.getAdapter();
                FixedTravelAllowance allowance = (FixedTravelAllowance) adapter.getItem(position);
                if (fixedTASelectedListener != null) {
                    fixedTASelectedListener.onFixedTravelAllowanceSelected(allowance);
                }
            }
        }

        if (v.getId() == R.id.checkbox) {
            RecyclerView rv = getRecyclerView();
            CheckBox cb = (CheckBox) v;
            if (rv != null) {
                FixedTravelAllowanceListAdapter adapter = (FixedTravelAllowanceListAdapter) rv.getAdapter();
                FixedTravelAllowance allowance = (FixedTravelAllowance) adapter.getItem(position);
                allowance.setIsSelected(cb.isChecked());
                updateSelectionCounter(getView());
            }
        }
    }



    private RecyclerView getRecyclerView() {
        View v = getView();
        if (v != null) {
            return (RecyclerView) v.findViewById(R.id.list);
        } else {
            return null;
        }
    }


    public void switchToSelctionMode(boolean switchToSelectionMode) {
        View v = getView();
        if (v == null) {
            return;
        }
        inSelectionMode = switchToSelectionMode;

        View fab = v.findViewById(R.id.fab);
        View toolbar = v.findViewById(R.id.toolbar_bottom);
        View counter = v.findViewById(R.id.tv_selection_counter);
        View swipeView = v.findViewById(R.id.swipe_refresh_layout);

        if (callback != null) {
            Bundle bundle = new Bundle();
            bundle.putBoolean(BUNDLE_ID_IN_SELECTION_MODE, inSelectionMode);
            callback.handleFragmentMessage(MSG_SELECTION_MODE_SWITCH, bundle);
        }

        if (switchToSelectionMode) {
            swipeView.setEnabled(false);
            AnimationUtil.fabToolbarAnimation(fab, View.GONE, toolbar, View.VISIBLE);
//            AnimationUtil.goneAnimation(fab);
//            AnimationUtil.visibleAnimation(toolbar);
            counter.setVisibility(View.VISIBLE);
            updateSelectionCounter(v);
        } else {
            swipeView.setEnabled(true);
            AnimationUtil.fabToolbarAnimation(fab, View.VISIBLE, toolbar, View.GONE);
//            AnimationUtil.goneAnimation(toolbar);
//            AnimationUtil.visibleAnimation(fab);
            counter.setVisibility(View.GONE);
        }

        refreshAdapter();
    }

    public boolean isInSelectionMode() {
        return inSelectionMode;
    }

}