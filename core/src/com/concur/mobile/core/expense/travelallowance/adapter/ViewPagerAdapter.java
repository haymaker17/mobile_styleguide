package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.fragment.FixedTravelAllowanceListFragment;
import com.concur.mobile.core.expense.travelallowance.fragment.TravelAllowanceItineraryListFragment;

import java.util.List;
import java.util.Map;

/**
 * This is the adapter for the {@code ViewPager} which holds the {@code FixedTravelAllowanceListFragment} and the
 * {@code TravelAllowanceItineraryListFragment}.
 * 
 * If additional fragments should be added the #COUNT needs to be incremented. The instance of the additional fragment needs to be
 * added to the #getItem method on the desired position. Don't forget to adjust the #getPageTitle method which returns the
 * corresponding page title visible on the tabs.
 *
 * @author Patricius Komarnicki Created by on 23.06.2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    public static class ViewPagerItem {
        private String title;
        private Class<? extends Fragment> fragmentClass;
        private Bundle arguments;

        public ViewPagerItem(String title, Class<? extends Fragment> fragmentClass, Bundle arguments) {
            this.title = title;
            this.fragmentClass = fragmentClass;
            this.arguments = arguments;
        }
    }

    private static final String CLASS_TAG = ViewPagerAdapter.class.getSimpleName();

    /**
     * The total number of fragments managed by this adapter.
     */
    private final static int COUNT = 2;

    private FragmentManager fm;
    private Context context;
    private List<ViewPagerItem> itemList;

    /**
     * Creates a new adapter.
     * @param fm is the {@code FragmentManager} from the support lib.
     * @param ctx the application context
     */
    public ViewPagerAdapter(FragmentManager fm, Context ctx, List<ViewPagerItem> itemList) {
        super(fm);
        this.fm = fm;
        this.context = ctx;
        this.itemList = itemList;
    }


    /**
     * {@inheritDoc}
     *
     */
    @Override
    public Fragment getItem(int position) {
        try {
            Fragment fragment = itemList.get(position).fragmentClass.newInstance();
            fragment.setArguments(itemList.get(position).arguments);
            return fragment;
        } catch (InstantiationException e) {
            Log.e(CLASS_TAG, "Instantiation Exception.");
            return null;
        } catch (IllegalAccessException e) {
            Log.e(CLASS_TAG, "Illegal Access Exception.");
            return null;
        }

    }
//    public Fragment getItem(int position) {
//        switch (position) {
//        case 0:
//            return new FixedTravelAllowanceListFragment();
//        case 1:
//            return new TravelAllowanceItineraryListFragment();
//        }
//        return null;
//    }

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return itemList.get(position).title;
//        switch (position) {
//            case 0:
//                return context.getString(R.string.ta_adjustments);
//            case 1:
//                return context.getString(R.string.itin_itineraries);
//        }
//        return null;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public int getCount() {
        return itemList.size();
    }


    /**
     * @return The current {@code TravelAllowanceItineraryListFragment} which was added to the fragment manager.
     *         Returns null in case the fragment can't be found.
     */
    public TravelAllowanceItineraryListFragment getTravelAllowanceItineraryFragment() {
        List<Fragment> fragments = fm.getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof TravelAllowanceItineraryListFragment) {
                return (TravelAllowanceItineraryListFragment) fragment;
            }
        }
        return null;
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
}
