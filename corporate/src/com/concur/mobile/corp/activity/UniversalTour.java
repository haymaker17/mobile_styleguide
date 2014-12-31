package com.concur.mobile.corp.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.corp.fragment.UniversalTourFragment;

@EventTracker.EventTrackerClassName(getClassName = "Learn More")
public class UniversalTour extends BaseActivity {

    private static final String FRAGMENT_UNIVERSAL_TOUR_MAIN = "FRAGMENT_UNIVERSAL_TOUR_MAIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_drive_main);

        FragmentManager fm = getSupportFragmentManager();
        BaseFragment universalTourFragment = (BaseFragment) fm.findFragmentByTag(FRAGMENT_UNIVERSAL_TOUR_MAIN);
        if (universalTourFragment == null) {
            universalTourFragment = new UniversalTourFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, universalTourFragment, FRAGMENT_UNIVERSAL_TOUR_MAIN);
            ft.commit();
        }
    }
}
