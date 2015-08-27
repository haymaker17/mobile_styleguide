package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.travelallowance.controller.FixedTravelAllowanceController;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

import java.util.List;
import java.util.Locale;

/**
 * Created by Patricius Komarnicki on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends ListFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final String CLASS_TAG = FixedTravelAllowanceListFragment.class.getSimpleName();
    public static final String ON_REFRESH_MSG = CLASS_TAG + ".refreshAllowances";

    /**
     * The activity context
     */
    private Context context;

    private IFragmentCallback callback;

    /**
     * The date formatter
     */
    private IDateFormat dateFormatter;

    private ListAdapter adapter;

    /**
     * The listener to be notified, whenever a fixed travel allowance is selected
     */
    private IFixedTravelAllowanceSelectedListener fixedTASelectedListener;

    private SwipeRefreshLayout swipeRefreshLayout;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        this.context = this.getActivity();
        this.dateFormatter = new DefaultDateFormat(context);

        this.adapter = new FixedTravelAllowanceListAdapter(this.getActivity());
        setListAdapter(adapter);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fixed_travel_allowance_list, container, false);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            view.findViewById(R.id.v_divider_bottom).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.v_divider_bottom).setVisibility(View.GONE);
            view.findViewById(R.id.ta_summary).setElevation(3);
        }

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(this);

        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        renderSummary();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

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
        if (this.callback != null) {
            this.callback.sendMessage(ON_REFRESH_MSG);
        } else {
            onRefreshFinished();
        }
    }

    public void onRefreshFinished() {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
            adapter =  new FixedTravelAllowanceListAdapter(getActivity());
            setListAdapter(adapter);
            renderSummary();
        }
    }

    /**
     * Renders the summary w.r.t fixed travel allowances
     */
    private void renderSummary() {

        if ( getActivity() == null) {
            return;
        }

        ConcurCore app = (ConcurCore) context.getApplicationContext();
        FixedTravelAllowanceController allowanceController = app.getFixedTravelAllowanceController();
        List<FixedTravelAllowance> allowances = allowanceController.getFixedTravelAllowances();
        if (allowances == null || allowances.size() == 0) {
            return;
        }

        TextView tvTitle = (TextView) getActivity().findViewById(R.id.tv_title);
        TextView tvValue = (TextView) getActivity().findViewById(R.id.tv_value);
        TextView tvSubtitle1 = (TextView) getActivity().findViewById(R.id.tv_subtitle_1);
        TextView tvSubtitle2 = (TextView) getActivity().findViewById(R.id.tv_subtitle_2);
        View vDividerBottom  =  getActivity().findViewById(R.id.v_divider_bottom);

        vDividerBottom.setVisibility(View.VISIBLE);

        if (tvTitle != null) {
            tvTitle.setText(R.string.ta_total_allowance);
        }

        Double sum = allowanceController.getSum();
        boolean multiLocations = allowanceController.hasMultipleGroups();
        renderAmount(tvValue, sum, allowances.get(0).getCurrencyCode());

        if (tvSubtitle2 != null) {
            if (multiLocations) {
                tvSubtitle2.setVisibility(View.GONE);
            } else {
                tvSubtitle2.setText(allowances.get(0).getLocationName());
            }
        }

        if (tvSubtitle1 != null) {
            tvSubtitle1.setText(allowanceController.getPeriod(dateFormatter));
        }

        renderAmount(tvValue, sum, allowances.get(0).getCurrencyCode());
    }

    /**
     * Renders the amount text view
     * @param tvAmount The reference to the text view
     * @param amount The amount to be rendered
     * @param crnCode The currency code associated with the amount
     */
    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null){
            Log.e(Const.LOG_TAG, CLASS_TAG + ".renderAmount: TextView null reference!");
            return;
        }
        if (amount != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            tvAmount.setText(FormatUtil.formatAmount(amount, locale, crnCode, true, true));
        } else {
            tvAmount.setText(StringUtilities.EMPTY_STRING);
        }
    }

}