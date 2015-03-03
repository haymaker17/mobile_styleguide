package com.concur.mobile.core.request;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 30/01/2015.
 */
public class RequestPagerAdapter<T extends AbstractConnectFormFieldActivity> extends FragmentPagerAdapter {

    protected static final String KEY_TAB_ID = "tabId";
    private T ffActivity;
    private Map<Integer, String> tabTitles;
    private Map<Integer, RequestEntryFragment<T>> mapTabs = new HashMap<Integer, RequestEntryFragment<T>>();

    public RequestPagerAdapter(T ffActivity, FragmentManager fm, Map<Integer, String> tabTitles) {
        super(fm);
        this.ffActivity = ffActivity;
        this.tabTitles = tabTitles;
    }

    @Override public Fragment getItem(int i) {
        if (!mapTabs.containsKey(i) || mapTabs.get(i) == null) {
            // --- fragment creation
            final RequestEntryFragment<T> fragment = new RequestEntryFragment<T>();
            mapTabs.put(i, fragment);
            final Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(KEY_TAB_ID, i);
            fragment.setArguments(args);
        }

        return mapTabs.get(i);
    }

    @Override public int getCount() {
        return tabTitles.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles.get(position);
    }
}
