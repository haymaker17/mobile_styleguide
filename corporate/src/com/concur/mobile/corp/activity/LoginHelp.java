package com.concur.mobile.corp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.service.BaseRequestPasswordReset;
import com.concur.mobile.core.service.RequestMobilePasswordReset;
import com.concur.mobile.core.service.RequestPasswordReset;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.corp.fragment.LoginHelpMain;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.ui.common.util.ViewUtil;

import java.util.HashMap;
import java.util.Map;

public class LoginHelp extends BaseActivity implements OnClickListener {

    private static final String PROGRESS_MASK_SHOWN = "progress.mask.shown";

    private static final String CLS_TAG = LoginHelp.class.getSimpleName();

    private static final String RESET_PASSWORD_REQUEST_RECEIVER = "password.request.receiver.token";
    private static final String FRAGMENT_LOGIN_HELP_MAIN = "FRAGMENT_LOGIN_HELP_MAIN";

    BaseAsyncResultReceiver resetPasswordRequestReceiver;

    protected boolean progressMaskVisible;

    protected String resetEmail;

    private BaseFragment loginHelpFragment;

    private View footer;

    protected String signInMethod;

    // Double Tap Finger
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;

    private long mFirstDownTime = 0;

    private boolean mSeparateTouches = false;

    private byte mTwoFingerTapCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_help);

        signInMethod = getIntent().getExtras().getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);

        if (retainer != null) {
            resetPasswordRequestReceiver = (BaseAsyncResultReceiver) retainer.get(RESET_PASSWORD_REQUEST_RECEIVER);
            if (resetPasswordRequestReceiver != null) {
                resetPasswordRequestReceiver.setListener(new RequestPasswordResetListener());
            }
        }
        initiateFooterLayout();
        setDoubleFingerTap();
        // We have an email, shoot it to the fragment which will show it in a Readonly field.
        FragmentManager fm = getSupportFragmentManager();
        loginHelpFragment = (BaseFragment) fm.findFragmentByTag(FRAGMENT_LOGIN_HELP_MAIN);
        if (loginHelpFragment == null) {
            loginHelpFragment = new LoginHelpMain();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.topicContainer, loginHelpFragment, FRAGMENT_LOGIN_HELP_MAIN);
            ft.commit();
        }

        // Enable the progress mask if needed
        if (savedInstanceState != null) {
            progressMaskVisible = savedInstanceState.getBoolean(PROGRESS_MASK_SHOWN, false);
            if (progressMaskVisible) {
                showProgressMask();
            }
        }

    }

    private void initiateFooterLayout() {
        if (footer == null) {
            footer = findViewById(R.id.footer);
            TextView footerTxtView = (TextView) footer.findViewById(R.id.helpmsg);
            if (footerTxtView != null) {
                // Set the style.
                footerTxtView.setText(getText(R.string.login_more_help).toString());
                footer.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        Intent it = new Intent(LoginHelp.this, LoginHelpTopic.class);
                        it.putExtra(Const.EXTRA_LOGIN_HELP_TOPIC_TITLE, getText(R.string.login_more_help_title)
                                .toString());
                        it.putExtra(Const.EXTRA_LOGIN_HELP_TOPIC_SUBHEADER, getText(R.string.login_more_help_subheader)
                                .toString());
                        it.putExtra(Const.EXTRA_LOGIN_HELP_TOPIC_MESSAGE, getText(R.string.login_more_help_message)
                                .toString());
                        startActivity(it);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'footer_navigation_bar_status'!");
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (resetPasswordRequestReceiver != null) {
            resetPasswordRequestReceiver.setListener(null);
            retainer.put(RESET_PASSWORD_REQUEST_RECEIVER, resetPasswordRequestReceiver);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(PROGRESS_MASK_SHOWN, progressMaskVisible);
    }

    public void onClick(View v) {

        EditText emailView = (EditText) findViewById(R.id.email);
        ViewUtil.setClearIconToEditText(emailView);
        resetEmail = emailView.getText().toString();

        if (resetEmail.trim().length() > 0) {
            showProgressMask();

            // Hide the keyboard, looks ugly staying up
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);

            resetPasswordRequestReceiver = new BaseAsyncResultReceiver(new Handler());
            resetPasswordRequestReceiver.setListener(new RequestPasswordResetListener());

            // We execute the PasswordReset request based on the type of user sign in.
            if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD.equals(signInMethod)) {
                new RequestMobilePasswordReset(getApplicationContext(), 1, resetPasswordRequestReceiver, resetEmail,
                        getResources().getConfiguration().locale.toString()).execute();
                trackResetRequest(signInMethod);
            } else if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD.equals(signInMethod)) {
                new RequestPasswordReset(getApplicationContext(), 1, resetPasswordRequestReceiver, resetEmail,
                        getResources().getConfiguration().locale.toString()).execute();
                trackResetRequest(signInMethod);
            } else {
                Log.w(CLS_TAG, "User signInMethod unkown. Sign in method: " + signInMethod);
            }

            EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_REQUEST_PIN_RESET);

        } else {
            DialogFragmentFactory.getAlertOkayInstance(getString(R.string.login_help_bad_email_dialog_title),
                    R.string.login_help_bad_email_dialog_message).show(getSupportFragmentManager(), CLS_TAG);
        }
    }

    /**
     * Track GA events for requesting password/pin aka mobile password.
     * 
     * @param signInMethod
     */
    public static void trackResetRequest(String signInMethod) {

        Map<String, String> params = new HashMap<String, String>();
        if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD.equalsIgnoreCase(signInMethod)) {
            params.put("Type", Flurry.PARAM_VALUE_LOGIN_USING_MOBILE_PASSWORD);
        } else if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD.equalsIgnoreCase(signInMethod)) {
            params.put("Type", Flurry.PARAM_VALUE_LOGIN_USING_PASSWORD);
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_REQUEST_PIN_RESET, params);

    }

    /**
     * Track GA events for requesting password/pin aka mobile password.
     * 
     * @param signInMethod
     */
    public static void trackResetResponse(String signInMethod, String successFailureAction) {

        Map<String, String> params = new HashMap<String, String>();
        if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD.equalsIgnoreCase(signInMethod)) {
            params.put("Type", Flurry.PARAM_VALUE_LOGIN_USING_MOBILE_PASSWORD);
        } else if (com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD.equalsIgnoreCase(signInMethod)) {
            params.put("Type", Flurry.PARAM_VALUE_LOGIN_USING_PASSWORD);
        }
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, successFailureAction, params);

    }

    private void trackResetSuccessResponse(String signInMethod) {
        trackResetResponse(signInMethod, Flurry.EVENT_NAME_RESET_PIN_SUCCESS);
    }

    private void trackResetFailureResponse(String signInMethod) {
        trackResetResponse(signInMethod, Flurry.EVENT_NAME_RESET_PIN_FAILURE);
    }

    protected void showProgressMask() {
        progressMaskVisible = true;
        View v = findViewById(R.id.progress_mask);
        v.setVisibility(View.VISIBLE);
    }

    protected void hideProgressMask() {
        progressMaskVisible = false;
        View v = findViewById(R.id.progress_mask);
        v.setVisibility(View.INVISIBLE);
    }

    protected class RequestPasswordResetListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            boolean success = resultData.getBoolean(BaseRequestPasswordReset.IS_SUCCESS, false);

            if (success) {
                // Save key and email for later use in reset call
                String keyPartA = resultData.getString(BaseRequestPasswordReset.KEY_PART);
                Preferences.setPinResetKeyPart(keyPartA);
                Preferences.setPinResetEmail(resetEmail);

                DialogFragmentFactory.getAlertDialog(getString(R.string.login_help_submit_confirm_title),
                        getString(R.string.login_help_submit_confirm_message), R.string.close, -1, -1,
                        new AlertDialogFragment.OnClickListener() {

                            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                            }

                            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                                trackResetSuccessResponse(signInMethod);
                                finish();
                            }

                        }, null, null, null).show(getSupportFragmentManager(), null);

            } else {
                String errMessage = resultData.getString(BaseRequestPasswordReset.ERROR_MESSAGE);
                if (errMessage == null) {
                    errMessage = getText(R.string.general_error_message).toString();
                }

                DialogFragmentFactory.getAlertOkayInstance(R.string.general_error, errMessage).show(
                        getSupportFragmentManager(), null);
                trackResetFailureResponse(signInMethod);
            }
        }

        public void onRequestFail(Bundle resultData) {
            DialogFragmentFactory.getAlertOkayInstance(R.string.general_network_error, R.string.general_error_message)
                    .show(getSupportFragmentManager(), null);
        }

        public void onRequestCancel(Bundle resultData) {
        }

        public void cleanup() {
            hideProgressMask();
            resetPasswordRequestReceiver = null;
        }

    }

    private void setDoubleFingerTap(){
        ImageView concur_logo = (ImageView) findViewById(R.id.concurlogo);

        setOnTouchListenerForView(concur_logo);
    }

    public void setOnTouchListenerForView(View view) {
        ImageView img = (ImageView) view;
        img.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                onTouchEvent(event);
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = MotionEventCompat.getActionMasked(event);

        switch (action) {
            case (MotionEvent.ACTION_MOVE):
                Log.d(CLS_TAG, "Action was MOVE");
                return true;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(CLS_TAG, "Action was CANCEL");
                return true;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(CLS_TAG, "Movement occurred outside bounds " +
                        "of current screen element");
                return true;
            case MotionEvent.ACTION_DOWN:
                if (mFirstDownTime == 0 || event.getEventTime() - mFirstDownTime > TIMEOUT)
                    reset(event.getDownTime());
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2)
                    mTwoFingerTapCount++;
                else
                    mFirstDownTime = 0;
                return true;
            case MotionEvent.ACTION_UP:
                if (!mSeparateTouches)
                    mSeparateTouches = true;
                else if (mTwoFingerTapCount == 2 && event.getEventTime() - mFirstDownTime < TIMEOUT) {
                    onTwoFingerDoubleTap();
                    mFirstDownTime = 0;
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    private void reset(long time) {
        mFirstDownTime = time;
        mSeparateTouches = false;
        mTwoFingerTapCount = 0;
    }

    public void onTwoFingerDoubleTap() {
        openSettings();
    }

    public void openSettings() {
        Intent i = new Intent(this, Preferences.class);
        i.putExtra(Preferences.OPEN_SOURCE_LIBRARY_CLASS, OpenSourceLicenseInfo.class);
        startActivity(i);
    }
}
