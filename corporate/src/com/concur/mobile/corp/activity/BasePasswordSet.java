/**
 * 
 */
package com.concur.mobile.corp.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.concur.breeze.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.service.BaseResetPassword;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.net.LoginRequest;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.util.Format;

import java.util.HashMap;
import java.util.Map;

/**
 * The superclass for mobile password (PIN) reset, as well as the standard password reset. Activities to reset any type of
 * password will extend this class.
 * 
 * @author westonw
 * 
 */
public abstract class BasePasswordSet extends BaseActivity implements OnClickListener {

    protected static final String PROGRESS_MASK_SHOWN = "progress.mask.shown";
    protected static final String ATTEMPT_COUNT = "attempt.count";
    protected static final String RESET_USER_PASSWORD_RECEIVER = "password.reset.receiver.token";
    protected static final String KEY_PART_B_TAG = "key_part_b";

    protected BaseAsyncResultReceiver resetUserPasswordReceiver;

    protected boolean progressMaskVisible;

    protected int attemptCount;

    protected String password;

    // Done
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Any subclass will use the same layout and simply change Text Fields
        setContentView(R.layout.password_set);

        setActionBarDetails();

        if (retainer != null) {
            resetUserPasswordReceiver = (BaseAsyncResultReceiver) retainer.get(RESET_USER_PASSWORD_RECEIVER);
            if (resetUserPasswordReceiver != null) {
                resetUserPasswordReceiver.setListener(new ResetUserPasswordListener());
            }
        }

        if (savedInstanceState != null) {
            // Enable the progress mask if needed
            progressMaskVisible = savedInstanceState.getBoolean(PROGRESS_MASK_SHOWN, false);
            if (progressMaskVisible) {
                showProgressMask();
            }

            // Keep up our attempt count
            attemptCount = savedInstanceState.getInt(ATTEMPT_COUNT);

        }
    }

    // Done
    @Override
    protected void onPause() {
        super.onPause();

        if (resetUserPasswordReceiver != null) {
            resetUserPasswordReceiver.setListener(null);
            retainer.put(RESET_USER_PASSWORD_RECEIVER, resetUserPasswordReceiver);
        }
    }

    // Done
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(PROGRESS_MASK_SHOWN, progressMaskVisible);
        outState.putInt(ATTEMPT_COUNT, attemptCount);
    }

    // TODO: Update analytics for new password reset stuff.
    protected void trackFailedAttempt(String failureType) {

        Map<String, String> params = new HashMap<String, String>();
        params.put("Type", failureType);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_RESET_PIN_ATTEMPT, params);

    }

    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.setPasswordButton: {
            if (ConcurMobile.isConnected()) {
                // Increment our attempt count
                attemptCount++;

                // Get set password values from TextView fields.
                password = ((TextView) findViewById(R.id.setPassword)).getText().toString();
                String passwordConfirm = ((TextView) findViewById(R.id.setPasswordConfirm)).getText().toString();

                // does this actually pop up and inform them anywhere? wtf???
                if (!validatePasswords(password, passwordConfirm)) {
                    clearPasswords();
                    String signInMethod = getIntent().getExtras().getString(
                            EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                    if (signInMethod.equalsIgnoreCase(com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD)) {
                        trackFailedAttempt("Bad Pin");
                    } else {
                        trackFailedAttempt("Bad Password");
                    }
                    return;
                }

                // Good pins, send them to the server
                // Gather our bits
                String keyPartA = Preferences.getPinResetKeyPart();
                String keyPartB = getIntent().getStringExtra(KEY_PART_B_TAG);
                String email = Preferences.getPinResetEmail();

                if (keyPartA != null && keyPartB != null && email != null) {
                    showProgressMask();

                    resetUserPasswordReceiver = new BaseAsyncResultReceiver(new Handler());
                    resetUserPasswordReceiver.setListener(new ResetUserPasswordListener());

                    // This is in onclick where we say new ResetUserPin(...).execute();
                    executeResetUserPassword(getApplicationContext(), 1, resetUserPasswordReceiver, email, keyPartA,
                            keyPartB, password);
                } else {
                    // If we're here, all else is valid but keyPart A and B don't match.
                    trackFailedAttempt("Invalid Device");
                    showInvalidDeviceDialog();
                }

            } else {
                new NoConnectivityDialogFragment().show(getSupportFragmentManager(), null);
            }
            break;
        }
        default: {
            break;
        }
        }
    }

    // Done
    /**
     * Clear all text from both EditText boxes (IE, they didn't match, so clear them so user can retry)
     */
    protected void clearPasswords() {
        TextView setPasswordEditText = (TextView) findViewById(R.id.setPassword);
        setPasswordEditText.setText("");
        setPasswordEditText.requestFocus();
        TextView setPasswordConfirmEditText = (TextView) findViewById(R.id.setPasswordConfirm);
        setPasswordConfirmEditText.setText("");
    }

    // Done
    protected void showProgressMask() {
        progressMaskVisible = true;
        View v = findViewById(R.id.progressMask);
        v.setVisibility(View.VISIBLE);
    }

    // Done (Perhaps change to GONE?)
    protected void hideProgressMask() {
        progressMaskVisible = false;
        View v = findViewById(R.id.progressMask);
        v.setVisibility(View.INVISIBLE);
    }

    /**
     * Since we have an explicit check for expired email, the only other way the keys mismatch should be that they sent an email
     * from one device and opened it on another device. If that's the case, throw up that dialog.
     */
    protected void showInvalidDeviceDialog() {
        AlertDialogFragment frag = DialogFragmentFactory.getAlertOkayInstance(R.string.password_invalid_device_title,
                R.string.password_invalid_device_message);
        frag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                Intent i = new Intent(BasePasswordSet.this, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Intent i = new Intent(BasePasswordSet.this, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }
        });
        frag.show(getSupportFragmentManager(), null);
    }

    /**
     * Set up the background and title of the ActionBar. The ActionBar title will be specific to subclass.
     */
    protected abstract void setActionBarDetails();

    /**
     * Check the passwords entered in both fields to make sure that they match and do whatever validation checks are necessary.
     * Validation criteria will be specific to subclass.
     * 
     * @param passwordField
     *            The top field (password they originally entered)
     * @param passwordConfirmField
     *            The bottom field (where they confirm what they've just entered)
     * @return whether or not passwords are equal to eachother and valid.
     */
    protected abstract boolean validatePasswords(String passwordField, String passwordConfirmField);

    // This is new ResetUserPin() inside of PinCreate
    protected abstract void executeResetUserPassword(Context context, int id, BaseAsyncResultReceiver receiver,
            String email, String keyPartA, String keyPartB, String password);

    /**
     * If an error comes back that the Password Reset Email is expired, this method will show the appropriate dialog.
     */
    protected void showEmailExpiredDialog() {

        AlertDialogFragment frag = new AlertDialogFragment();
        frag.setTitle(R.string.password_expired_title);
        frag.setMessage(R.string.password_expired_message);
        frag.setPositiveButtonText(R.string.try_again);
        frag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                String signInMethod = getIntent().getExtras()
                        .getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                Intent i = new Intent(BasePasswordSet.this, LoginHelp.class);
                i.putExtra(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, signInMethod);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();

            }
        });
        frag.setNegativeButtonText(R.string.login_title);
        frag.setNegativeButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Intent i = new Intent(BasePasswordSet.this, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }
        });
        frag.show(getSupportFragmentManager(), null);
    }

    /**
     * If an error comes back that Mobile is Disabled, this method will show the appropriate dialog.
     */
    protected void showMobileDisabledDialog() {
        AlertDialogFragment frag = DialogFragmentFactory.getAlertOkayInstance(R.string.password_mobile_disabled_title,
                R.string.password_mobile_disabled_message);
        frag.setPositiveButtonListener(new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                Intent i = new Intent(BasePasswordSet.this, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Intent i = new Intent(BasePasswordSet.this, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }
        });
        frag.show(getSupportFragmentManager(), null);
    }

    /**
     * When we shoot off a request to reset a password, the response gets picked up here.
     */
    protected class ResetUserPasswordListener implements AsyncReplyListener {

        // This is mostly solid.
        public void onRequestSuccess(Bundle resultData) {
            // (isSuccess)
            if (resultData.getBoolean(BaseResetPassword.IS_SUCCESS, false)) {

                // XXX: Just tracks the number of attempts. Possibly extraneous code? If not, rename
                Map<String, String> params = new HashMap<String, String>();
                String bucket = (attemptCount <= 3 ? Integer.toString(attemptCount) : "Over 3");
                params.put("Attempt Count", bucket);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_SIGN_IN, Flurry.EVENT_NAME_RESET_PIN_SUCCESS, params);

                // Log user in asynchronously and land on Home screen
                new LoginOnSuccess((ConcurMobile) ConcurMobile.getContext(),
                        resultData.getString(BaseResetPassword.LOGIN_ID)).execute();

                // TODO: Rename these classes once password subclass is built if we can make them more generic.
                Preferences.clearPinResetEmail();
                Preferences.clearPinResetKeyPart();

            } else {

                // This first if is probably okay once the string formatting is fixed. (change pin strings of course)
                // MinLength always comes back so we need to actually compare lengths
                int minLength = resultData.getInt(BaseResetPassword.MIN_LENGTH, 0);

                if (password.length() < minLength) {
                    trackFailedAttempt("Invalid Length");

                    String lengthMessage = com.concur.mobile.base.util.Format.localizeText(BasePasswordSet.this,
                            R.string.password_short_message, minLength);
                    DialogFragmentFactory.getAlertOkayInstance(R.string.password_short_title, lengthMessage).show(
                            getSupportFragmentManager(), null);

                } else {
                    String errorMessage = resultData.getString(BaseResetPassword.ERROR_MESSAGE);
                    if ("error.request_expired".equalsIgnoreCase(errorMessage)) {
                        trackFailedAttempt("Request Expired");
                        showEmailExpiredDialog();

                    } else if ("error.mismatched_keys".equalsIgnoreCase(errorMessage)) {
                        trackFailedAttempt("Invalid Device");
                        showInvalidDeviceDialog();

                    } else if ("error.mobile_disabled".equalsIgnoreCase(errorMessage)) {
                        trackFailedAttempt("Mobile Disabled");
                        showMobileDisabledDialog();

                    } else {
                        trackFailedAttempt("Other Error");
                        DialogFragmentFactory.getAlertOkayInstance(R.string.general_server_error, errorMessage).show(
                                getSupportFragmentManager(), null);
                    }
                }
                clearPasswords();
            }
        }

        // Done
        public void onRequestFail(Bundle resultData) {
            trackFailedAttempt(Flurry.EVENT_OTHER_ERROR);

            DialogFragmentFactory.getAlertOkayInstance(R.string.general_network_error, R.string.general_error_message)
                    .show(getSupportFragmentManager(), null);
        }

        // Done
        public void onRequestCancel(Bundle resultData) {
            // No-op
        }

        // FIXME: Calling hideProgressMask() here causes it to hide before logging user in on successful reset. Log as bug
        public void cleanup() {
            hideProgressMask();
            resetUserPasswordReceiver = null;
        }

    }

    // Upon successful Password Reset, log the user in and take them to home.
    protected class LoginOnSuccess extends AsyncTask<Integer, Double, HashMap<String, Object>> {

        ConcurMobile concurMobile;
        String loginId;

        // Done.
        protected LoginOnSuccess(ConcurMobile concurMobile, String loginId) {
            this.concurMobile = concurMobile;
            this.loginId = loginId;
        }

        // Done.
        @Override
        protected void onPreExecute() {
            showProgressMask();
        }

        // This should be fine?
        @Override
        protected HashMap<String, Object> doInBackground(Integer... params) {
            String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());

            // FIXME: Update to use platform Web Services calls
            LoginRequest doLogin = new LoginRequest(concurMobile, null, concurMobile.getProduct(), serverAdd, loginId,
                    password);
            doLogin.run();
            return doLogin.getResponseMap();
        }

        // Done but needs some comments/todos cleaned up
        @Override
        protected void onPostExecute(HashMap<String, Object> responses) {

            String sessionId = (String) responses.get(Const.LR_SESSION_ID);

            if (sessionId != null) {
                // Save the login response information.
                // FIXME: Does this need to be saved in platform response?
                ConcurCore.saveLoginResponsePreferences(sessionId, concurMobile, responses);

                // Track the success
                Login.trackLoginSuccess(Flurry.PARAM_VALUE_PIN_OR_PASSWORD);

                // On success, start home at top and finish here
                // Prior to the starting the home screen, initialize the system/user config
                // XXX: Try below instead of getting application
                // concurMobile.initSystemConfig();
                // concurMobile.initUserConfig();
                ((ConcurMobile) getApplication()).initSystemConfig();
                ((ConcurMobile) getApplication()).initUserConfig();

                Intent i = new Intent(concurMobile, Home.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            } else {
                // If we fail to login, just punt the user to Login to try again with new Password.
                DialogFragmentFactory.getAlertOkayInstance("", R.string.login_failure).show(
                        getSupportFragmentManager(), null);

                Intent i = new Intent(concurMobile, EmailPasswordLookupActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);

                finish();
            }

        }

    }
}
