package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;

import java.util.HashMap;
import java.util.List;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    private final static int COUNT = 2;
    private FragmentManager fm;
    private Context context;

    public ViewPagerAdapter(FragmentManager fm, Context ctx) {
        super(fm);
        this.fm = fm;
        this.context = ctx;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                FixedTravelAllowanceListFragment fixedAllowFrag = new FixedTravelAllowanceListFragment();
                return fixedAllowFrag;
            case 1:
                TravelAllowanceItineraryListFragment itinFragment = new TravelAllowanceItineraryListFragment();
                return itinFragment;
        }
        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return context.getString(R.string.itin_adjustments);
            case 1:
                return context.getString(R.string.itin_itineraries);
        }
        return null;
    }

    @Override
    public int getCount() {
        return COUNT;
    }

    public FixedTravelAllowanceListFragment getFixedTravelAllowanceFragment() {
        List<Fragment> fragments = fm.getFragments();
        for(Fragment fragment: fragments) {
            if (fragment instanceof FixedTravelAllowanceListFragment) {
                return (FixedTravelAllowanceListFragment) fragment;
            }
        }
        return null;
    }

    public TravelAllowanceItineraryListFragment getTravelAllowanceItineraryFragment () {
        List<Fragment> fragments = fm.getFragments();
        for(Fragment fragment: fragments) {
            if (fragment instanceof TravelAllowanceItineraryListFragment) {
                return (TravelAllowanceItineraryListFragment) fragment;
            }
        }
        return null;
    }
}
