package com.concur.mobile.core.request;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.concur.core.R;
import com.concur.mobile.core.activity.AbstractConnectFormFieldActivity;

/**
 * Created by OlivierB on 30/01/2015.
 * Note : should be moved to a platform_ui_request
 */
public class RequestEntryFragment<T extends AbstractConnectFormFieldActivity> extends Fragment {

    private static final String CLS_TAG = "RequestEntryFragment";

    private T activity;

    public RequestEntryFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //title = getArguments().getString(RequestPagerAdapter.KEY_TITLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (T) getActivity();
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        final View rootView = inflater.inflate(R.layout.request_entry_fragment_layout, container, false);
        Bundle args = getArguments();
        if (args != null) {
            final int tabId = args.getInt(RequestPagerAdapter.KEY_TAB_ID);
            final LinearLayout entryFields = (LinearLayout) rootView.findViewById(R.id.entryFields);
            final RelativeLayout saveButton = (RelativeLayout) rootView.findViewById(R.id.saveButton);
            activity.setDisplayFields(entryFields, tabId);
            activity.applySaveButtonPolicy(saveButton);
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }
}
