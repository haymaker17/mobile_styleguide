package com.concur.mobile.corp.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.corp.activity.EmailLookupActivity;
import com.concur.mobile.corp.activity.TestDriveRegistration;
import com.concur.mobile.corp.activity.UniversalTour;

public class PreLoginFragment extends BaseFragment implements OnClickListener {

    public static final int TEST_DRIVE_REQ_CODE = 1;
    public static final int EMAIL_LOOKUP_REQ_CODE = 2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.pre_login, null);

        initButtonView(root);

        initHelpView(root);

        return root;
    }

    /**
     * initialize help view
     * 
     * @param root
     *            view of fragment.
     * */
    private void initHelpView(View root) {
        View help = root.findViewById(R.id.help);

        if (help != null) {
            TextView helpMsg = (TextView) help.findViewById(R.id.helpmsg);

            helpMsg.setText(R.string.test_drive_help);

            help.setOnClickListener(this);
        }
    }

    /**
     * initialize and setonclicklisteners for button
     * 
     * @param root
     *            view of fragment.
     * */
    private void initButtonView(View root) {
        // login button
        Button login = (Button) root.findViewById(R.id.prelogin_login);
        if (login != null) {
            login.setOnClickListener(this);
        }

        // registration button
        Button registration = (Button) root.findViewById(R.id.prelogin_register);
        if (registration != null) {
            registration.setOnClickListener(this);
        }

    }

    public void onClick(View v) {
        int id = v.getId();
        Intent it;
        switch (id) {
        case R.id.prelogin_login:
            EventTracker.INSTANCE.track(Flurry.CATEGORY_START_UP, "Sign In Click");

            it = new Intent(activity, EmailLookupActivity.class);
            //reset user app start and login successful timer for google analytics
            ConcurCore.resetUserTimers();
            getBaseActivity().startActivityForResult(it, EMAIL_LOOKUP_REQ_CODE);
            break;

        case R.id.prelogin_register:
            EventTracker.INSTANCE.track(Flurry.CATEGORY_START_UP, "Test Drive Click");

            it = new Intent(activity, TestDriveRegistration.class);
            getBaseActivity().startActivityForResult(it, TEST_DRIVE_REQ_CODE);
            break;
        case R.id.help:
            EventTracker.INSTANCE.track(Flurry.CATEGORY_START_UP, "Learn More Click");

            it = new Intent(activity, UniversalTour.class);
            startActivity(it);
            break;
        default:
            break;
        }
    }
}
