package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.corp.fragment.PreLoginFragment;

public class PreLogin extends BaseActivity {

    private static final String FRAGMENT_PRE_LOGIN = "FRAGMENT_PRE_LOGIN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_drive_main);

        FragmentManager fm = getSupportFragmentManager();
        BaseFragment preLoginFragment = (BaseFragment) fm.findFragmentByTag(FRAGMENT_PRE_LOGIN);
        if (preLoginFragment == null) {
            preLoginFragment = new PreLoginFragment();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, preLoginFragment, FRAGMENT_PRE_LOGIN);
            ft.commit();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == PreLoginFragment.TEST_DRIVE_REQ_CODE 
                    && data != null) { // MOB-18672 this can be null for a Test Drive user.
                // This scenario happens if user tries to create a new TestDrive account
                // which already exists.
                Intent i = new Intent(this, EmailLookupActivity.class);
                i.putExtras(data.getExtras());
                //reset user app start and login successful timer for google analytics
                ConcurCore.resetUserTimers();
                startActivityForResult(i, PreLoginFragment.EMAIL_LOOKUP_REQ_CODE);
                
            } else if (requestCode == PreLoginFragment.EMAIL_LOOKUP_REQ_CODE
                    || requestCode == PreLoginFragment.TEST_DRIVE_REQ_CODE) {
                
                finish();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
