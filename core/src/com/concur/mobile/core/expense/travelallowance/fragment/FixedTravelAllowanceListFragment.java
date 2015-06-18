package com.concur.mobile.core.expense.travelallowance.fragment;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.concur.core.R;

/**
 * Created by D049515 on 15.06.2015.
 */
public class FixedTravelAllowanceListFragment extends ListFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setListAdapter(new FixedTravelAllowanceListAdapter(this.getActivity(), null));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.fixed_travel_allowance_list, container, false);
    }


}
