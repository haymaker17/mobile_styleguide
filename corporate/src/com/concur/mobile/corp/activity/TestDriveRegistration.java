package com.concur.mobile.corp.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.corp.fragment.TestDriveRegistrationFragment;
import com.concur.mobile.platform.ui.common.IProgressBarListener;

import static com.concur.mobile.core.util.Flurry.EVENT_NAME_BACK_BUTTON_CLICK;

@EventTracker.EventTrackerClassName(getClassName = "Test Drive Registration")
public class TestDriveRegistration extends BaseActivity implements IProgressBarListener {

    private static final String FRAGMENT_TEST_DRIVE_REGISTRATION = "FRAGMENT_TEST_DRIVE_REGISTRATION";

    private boolean progressbarVisible;

    private static final String PROGRESSBAR_VISIBLE = "PROGRESSBAR_VISIBLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_container);

        // Enable the progress mask if needed
        if (savedInstanceState != null) {
            progressbarVisible = savedInstanceState.getBoolean(PROGRESSBAR_VISIBLE, false);
            if (progressbarVisible) {
                showProgressBar();
            }
        }

        FragmentManager fm = getSupportFragmentManager();
        BaseFragment registrationFrag = (BaseFragment) fm.findFragmentByTag(FRAGMENT_TEST_DRIVE_REGISTRATION);
        if (registrationFrag == null) {
            registrationFrag = new TestDriveRegistrationFragment();
            ((TestDriveRegistrationFragment) registrationFrag).setProgressBarListener(this);
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, registrationFrag, FRAGMENT_TEST_DRIVE_REGISTRATION);
            ft.commit();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION, EVENT_NAME_BACK_BUTTON_CLICK);
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showProgressBar() {
        // find progressbar and set the visibility
        View v = findViewById(R.id.progress_mask);
        RelativeLayout progressBar = (RelativeLayout) v;
        progressbarVisible = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    public void hideProgressBar() {
        // find progressbar and set the visibility
        View v = findViewById(R.id.progress_mask);
        RelativeLayout progressBar = (RelativeLayout) v;
        progressbarVisible = false;
        progressBar.setVisibility(View.INVISIBLE);
    }

    public boolean isProgressBarShown() {
        return progressbarVisible;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(PROGRESSBAR_VISIBLE, progressbarVisible);
    }
}
