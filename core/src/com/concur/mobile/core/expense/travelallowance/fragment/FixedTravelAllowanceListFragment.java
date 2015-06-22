package com.concur.mobile.core.expense.travelallowance.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowance;
import com.concur.mobile.core.expense.travelallowance.datamodel.FixedTravelAllowanceTestData;
import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.DefaultDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.IDateFormat;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends ListFragment {

    private static final String CLS_TAG = FixedTravelAllowanceListFragment.class.getSimpleName();

    /**
     * The list of fixed travel allowances associated with the expense report
     */
    private List<FixedTravelAllowance> fixedTravelAllowances;

    /**
     * The activity context
     */
    private Context context;

    /**
     * The date formatter
     */
    private IDateFormat dateFormatter;

    /**
     * The listener to be notified, whenever a fixed travel allowance is selected
     */
    private IFixedTravelAllowanceSelectedListener fixedTASelectedListener;

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

        //TODO: Get expense report ID from instance state and read the allowances associated

        //TODO: Remove mock data
        FixedTravelAllowanceTestData mockData = new FixedTravelAllowanceTestData();
        fixedTravelAllowances = mockData.getAllowances(false);

        this.context = this.getActivity();
        this.dateFormatter = new DefaultDateFormat(context);
        setListAdapter(new FixedTravelAllowanceListAdapter(this.getActivity(), fixedTravelAllowances));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fixed_travel_allowance_list, container, false);
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
            this.fixedTASelectedListener = (IFixedTravelAllowanceSelectedListener) activity;
        } catch (ClassCastException exception) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onAttach: Container Activity must implement "
                    + IFixedTravelAllowanceSelectedListener.class.getSimpleName());
        }
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
     * Renders the summary w.r.t fixed travel allowances
     */
    private void renderSummary() {

        if (fixedTravelAllowances == null || fixedTravelAllowances.size() == 0 || getActivity() == null) {
            return;
        }

        double sum = 0.0;
        Date startDate;
        Date endDate;
        TextView tvTitle = (TextView) getActivity().findViewById(R.id.tv_title);
        TextView tvValue = (TextView) getActivity().findViewById(R.id.tv_value);
        TextView tvSubtitle1 = (TextView) getActivity().findViewById(R.id.tv_subtitle_1);
        TextView tvSubtitle2 = (TextView) getActivity().findViewById(R.id.tv_subtitle_2);
        View vDividerBottom  =  getActivity().findViewById(R.id.v_divider_bottom_bold);

        vDividerBottom.setVisibility(View.VISIBLE);

        if (tvTitle != null) {
            tvTitle.setText(R.string.itin_total_allowance);
        }

        boolean multiLocations = false;
        for (FixedTravelAllowance allowance: fixedTravelAllowances) {
            if (!multiLocations && !allowance.getLocationName().equals(fixedTravelAllowances.get(0).getLocationName())) {
                multiLocations = true;
            }
            if (!allowance.getExcludedIndicator()) {
                sum += allowance.getAmount();
            }
        }
        Collections.sort(fixedTravelAllowances, Collections.reverseOrder());
        renderAmount(tvValue, sum, fixedTravelAllowances.get(0).getCurrencyCode());

        if (tvSubtitle2 != null) {
            if (multiLocations) {
                tvSubtitle2.setVisibility(View.GONE);
            } else {
                tvSubtitle2.setText(fixedTravelAllowances.get(0).getLocationName());
            }

        }

        startDate = fixedTravelAllowances.get(0).getDate();
        endDate = fixedTravelAllowances.get(fixedTravelAllowances.size() - 1).getDate();
        if (tvSubtitle1 != null) {
            tvSubtitle1.setText(DateUtils.startEndDateToString(startDate, endDate, dateFormatter, false, true, true));
        }

        renderAmount(tvValue, sum,  fixedTravelAllowances.get(0).getCurrencyCode());
    }

    /**
     * Renders the amount text view
     * @param tvAmount The reference to the text view
     * @param amount The amount to be rendered
     * @param crnCode The currency code associated with the amount
     */
    private void renderAmount(TextView tvAmount, Double amount, String crnCode) {

        if (tvAmount == null){
            Log.e(Const.LOG_TAG, CLS_TAG + ".renderAmount: TextView null reference!");
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
