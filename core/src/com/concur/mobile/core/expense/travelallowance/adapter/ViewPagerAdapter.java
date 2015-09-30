package com.concur.mobile.core.expense.travelallowance.adapter;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

import java.util.List;

/**
 * This is a generic adapter for the {@code ViewPager} which can hold differen {@code Fragment} objects.
 * 
 * In order to use this adapter you need to wrap your {@code Fragment} in a {@code ViewPagerItem}.
 *
 * @author Patricius Komarnicki Created by on 23.06.2015.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    /**
     * Needed to wrap {@code Fragment} objects with additional info.
     */
    public static class ViewPagerItem {
        private String title;
        private Class<? extends Fragment> fragmentClass;
        private Bundle arguments;

        /**
         *
         * @param title The title of the page where the fragment is displayed. This title can show up in a tab if you use a tab bar.
         * @param fragmentClass The fragment class of your fragment.
         * @param arguments Optional arguments which will be passed to your fragment after instantiation.
         */
        public ViewPagerItem(String title, Class<? extends Fragment> fragmentClass, Bundle arguments) {
            this.title = title;
            this.fragmentClass = fragmentClass;
            this.arguments = arguments;
        }
    }

    private static final String CLASS_TAG = ViewPagerAdapter.class.getSimpleName();

    private List<ViewPagerItem> itemList;

    /**
     * Creates a new adapter.
     * @param fm is the {@code FragmentManager} from the support lib.
     * @param ctx the application context
     */
    public ViewPagerAdapter(FragmentManager fm, Context ctx, List<ViewPagerItem> itemList) {
        super(fm);
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

    /**
     * {@inheritDoc}
     *
     */
    @Override
    public CharSequence getPageTitle(int position) {
        return itemList.get(position).title;
    }

    /**
     * {@inheritDoc}
     * 
     */
    @Override
    public int getCount() {
        return itemList.size();
    }

}
