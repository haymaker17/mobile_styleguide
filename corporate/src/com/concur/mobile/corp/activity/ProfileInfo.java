package com.concur.mobile.corp.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.corp.fragment.ProfileInfoFragment;

@EventTracker.EventTrackerClassName(getClassName = "Profile Info")
public class ProfileInfo extends BaseActivity {

    private static final String FRAGMENT_PROFILE_INFO_FRAGMENT = "FRAGMENT_PROFILE_INFO_FRAGMENT";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.general_fragment);

        FragmentManager fm = getSupportFragmentManager();
        BaseFragment universalTourFragment = (BaseFragment) fm.findFragmentByTag(FRAGMENT_PROFILE_INFO_FRAGMENT);
        if (universalTourFragment == null) {
            universalTourFragment = new ProfileInfoFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, universalTourFragment, FRAGMENT_PROFILE_INFO_FRAGMENT);
            ft.commit();
        }
    }
}
