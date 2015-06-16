package com.concur.mobile.core.expense.travelallowance.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends ListFragment {


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        setListAdapter(null);
        super.onViewCreated(view, savedInstanceState);
    }
}
