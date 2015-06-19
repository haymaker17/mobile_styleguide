package com.concur.mobile.core.expense.travelallowance.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private Context context;
    private IDateFormat dateFormatter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //TODO: Get expense report ID from instance state and read the allowances associated

        //TODO: Remove mock data
        FixedTravelAllowanceTestData mockData = new FixedTravelAllowanceTestData();
        fixedTravelAllowances = mockData.getAllowances();

        this.context = this.getActivity();
        this.dateFormatter = new DefaultDateFormat(context);
        setListAdapter(new FixedTravelAllowanceListAdapter(this.getActivity(), fixedTravelAllowances));
    }

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

    private void renderSummary() {

        if (fixedTravelAllowances == null || fixedTravelAllowances.size() == 0) {
            return;
        }

        double sum = 0.0;
        Date startDate;
        Date endDate;
        TextView tvTitle = (TextView) getActivity().findViewById(R.id.tv_title);
        TextView tvValue = (TextView) getActivity().findViewById(R.id.tv_value);
        TextView tvSubtitle1 = (TextView) getActivity().findViewById(R.id.tv_subtitle_1);
        TextView tvSubtitle2 = (TextView) getActivity().findViewById(R.id.tv_subtitle_2);

        if (tvSubtitle2 != null) {
            tvSubtitle2.setVisibility(View.GONE);
        }

        if (tvTitle != null) {
            tvTitle.setText(R.string.itin_total_allowance);
        }

        for (FixedTravelAllowance allowance: fixedTravelAllowances) {
            if (!allowance.getExcludedIndicator()) {
                sum += allowance.getAmount();
            }
        }
        Collections.sort(fixedTravelAllowances, Collections.reverseOrder());
        renderAmount(tvValue, sum, fixedTravelAllowances.get(0).getCurrencyCode());

        startDate = fixedTravelAllowances.get(0).getDate();
        endDate = fixedTravelAllowances.get(fixedTravelAllowances.size() - 1).getDate();
        if (tvSubtitle1 != null) {
            tvSubtitle1.setText(DateUtils.startEndDateToString(startDate, endDate, dateFormatter, false, true, true));
        }

        renderAmount(tvValue, sum,  fixedTravelAllowances.get(0).getCurrencyCode());

    }

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
