package com.concur.mobile.core.expense.travelallowance.activity;

import android.app.ActionBar;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

import com.concur.core.R;

import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;

/**
 * Created by D049515 on 15.06.2015.
 */
public class TravelAllowanceActivity extends BaseActivity {


    private static final String ADJUSTMENTS_TAG = "adjustments";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.travel_allowance_activity);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.adjustment_fragment_container, new FixedTravelAllowanceListFragment(), ADJUSTMENTS_TAG);
        transaction.commit();
        getSupportFragmentManager().executePendingTransactions();
    }
}
