package com.concur.mobile.corp.fragment;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.corp.activity.EmailLookupActivity;
import com.concur.mobile.corp.activity.TestDriveTour;
import com.concur.mobile.corp.service.TestDriveRegistrationAsyncTask;
import com.concur.mobile.platform.ui.common.IProgressBarListener;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.login.EmailLookupFragment;

public class TestDriveRegistrationFragment extends BaseFragment implements OnClickListener {

    private static final String TERMS_LINK = "https://www.concur.com/en-us/termsandconditions.html";
    private static final String POLICY_LINK = "https://www.concur.com/en-us/privacy-policy";

    private static final String TEST_DRIVE_REGISTRATION_REQUEST_RECEIVER = "test.drive.registration.request.receiver";

    private EditText emailView, passwordView;

    private Button submit;

    private BaseAsyncResultReceiver testRegReceiver;

    private TestDriveRegistrationAsyncTask doLogin;

    private IProgressBarListener progressBarListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View root = inflater.inflate(R.layout.test_drive_registration, null);

        // Tweak the action bar
        final ActionBar actionBar = getBaseActivity().getSupportActionBar();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_white_background));
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO,
                ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
        actionBar.setLogo(R.drawable.concurlogo);

        // set email and password
        emailView = (EditText) root.findViewById(R.id.test_drive_email);
        passwordView = (EditText) root.findViewById(R.id.test_drive_password);
        // set default type face so user can see login and password editview hint in same font style.
        passwordView.setTypeface(Typeface.DEFAULT);
        // retrieve reciever if needed
        if (activity.retainer != null) {
            testRegReceiver = (BaseAsyncResultReceiver) activity.retainer.get(TEST_DRIVE_REGISTRATION_REQUEST_RECEIVER);
            if (testRegReceiver != null) {
                testRegReceiver.setListener(new TestDriveRegListener());
            }
        }

        // set up terms and condition
        setTermAndConditionView(root);

        // set submit button
        setSubmitRegView(root);

        // Set the default action for the pin view to do the submit
        passwordView.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    TestDriveRegistrationFragment.this.onClick(submit);
                    return true;
                }
                return false;
            }
        });

        return root;
    }

    public void setProgressBarListener(IProgressBarListener proListener) {
        this.progressBarListener = proListener;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        // this controls whether a fragment instance is retained across Activity re-creation.
        // I have used this to persist the progressBarListener reference. I have also implemented retainer pattern to persist
        // receiver reference(like old pattern does). According to google document if we set it then fragment lifecycle will be
        // slightly different. For this fragment we dont required any life cycle change. make sure if you set it true you will
        // also change fragment life cycle.
        // http://developer.android.com/reference/android/app/Fragment.html#setRetainInstance%28boolean%29
        setRetainInstance(true);
    }

    private void setTermAndConditionView(View root) {
        TextView tacView = (TextView) root.findViewById(R.id.test_drive_termsandcondition);

        String msg = getActivity().getText(R.string.test_drive_terms_and_condition).toString();
        String and = getActivity().getText(R.string.test_drive_and).toString();
        String terms = getActivity().getText(R.string.test_drive_terms).toString();
        String policy = getActivity().getText(R.string.test_drive_policy).toString();

        StringBuilder strbldr = new StringBuilder(msg);
        strbldr.append(' ');

        // append terms
        strbldr.append("<font color=#A9DEFF> <a href=").append(TERMS_LINK).append(">").append(terms)
                .append("</a> </font>");

        // append and
        strbldr.append(' ').append("<br/>").append(and);

        // append policy
        strbldr.append("<font color=#A9DEFF> <a href=").append(POLICY_LINK).append(">").append(policy)
                .append("</a> </font>.");

        tacView.setMovementMethod(LinkMovementMethod.getInstance());

        tacView.setText(Html.fromHtml(strbldr.toString()));
    }

    private void setSubmitRegView(View root) {
        submit = (Button) root.findViewById(R.id.test_drive_submit);

        if (submit != null) {
            submit.setOnClickListener(this);
        }

    }

    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
        case R.id.test_drive_submit:
            // Increment the number of times the user tries to register.
            Preferences.incrementTestDriveRegistrationAttemptCount();

            if (!ConcurCore.isConnected()) {
                new NoConnectivityDialogFragment().show(getFragmentManager(), null);

                logFailureEvent("Offline");

                return;
            } else {

                String email = emailView.getText().toString().trim();
                String password = passwordView.getText().toString().trim();

                if (email != null && email.length() > 0) {
                    if (FormUtil.isEmailValid(email)) {
                        if (password != null && password.length() > 0) {

                            showProgressBar();

                            testRegReceiver = new BaseAsyncResultReceiver(new Handler());
                            testRegReceiver.setListener(new TestDriveRegListener());

                            Locale locale = activity.getResources().getConfiguration().locale;

                            String cntryCode = ViewUtil.getUserCountryCode(activity);

                            doLogin = new TestDriveRegistrationAsyncTask(activity.getApplicationContext(), 1,
                                    testRegReceiver, email, password, cntryCode, locale);

                            doLogin.execute();
                        } else {
                            DialogFragmentFactory.getAlertOkayInstance(
                                    getActivity().getText(R.string.test_drive_password_error_title).toString(),
                                    R.string.test_drive_password_error).show(getFragmentManager(), null);

                            logFailureEvent("Empty Password");
                        }
                    } else {
                        DialogFragmentFactory.getAlertOkayInstance(
                                getActivity().getText(R.string.test_drive_email_error_title).toString(),
                                R.string.test_drive_email_error).show(getFragmentManager(), null);

                        logFailureEvent("Not an Email");
                    }
                } else {
                    DialogFragmentFactory.getAlertOkayInstance(
                            getActivity().getText(R.string.test_drive_email_error_title).toString(),
                            R.string.test_drive_email_error).show(getFragmentManager(), null);

                    if (password == null || password.length() > 0) {
                        logFailureEvent("Empty Email and Password");
                    } else {
                        logFailureEvent("Empty Email");
                    }
                }

                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);

            }
            break;

        default:
            break;
        }
    }

    protected void showProgressBar() {
        progressBarListener.showProgressBar();
    }

    protected void hideProgressBar() {
        progressBarListener.hideProgressBar();
    }

    @Override
    public void onPause() {
        super.onPause();

        if (testRegReceiver != null) {
            testRegReceiver.setListener(null);
            activity.retainer.put(TEST_DRIVE_REGISTRATION_REQUEST_RECEIVER, testRegReceiver);
        }
    }

    protected class TestDriveRegListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            HashMap<String, Object> responses = doLogin.getParseMap();
            String sessionId = (String) responses.get(Const.LR_SESSION_ID);
            Map<String, String> eventParams = new HashMap<String, String>();
            if (sessionId != null) {

                // Save the login response information.
                ConcurCore.saveLoginResponsePreferences(sessionId, (ConcurMobile) activity.getApplicationContext(),
                        responses);

                // go to home
                gotoTestDriveTour();

                eventParams.put("Success:", "Yes");
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                        Flurry.EVENT_NAME_SUBMIT_REGISTRATION, eventParams);

                eventParams.clear();
                eventParams.put(
                        "Sign In Try Again Count:",
                        (Preferences.getTestDriveSigninTryAgainCount() > 5) ? "Over 5" : Integer.toString(Preferences
                                .getTestDriveSigninTryAgainCount()));
                eventParams.put(
                        "Registration Attempt Count:",
                        (Preferences.getTestDriveRegistrationAttemptCount() > 5) ? "Over 5" : Integer
                                .toString(Preferences.getTestDriveRegistrationAttemptCount()));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                        Flurry.EVENT_NAME_SUBMIT_REGISTRATION_SUCCESS, eventParams);
                // Be sure to reset the count.
                Preferences.setTestDriveRegistrationAttemptCount(0);
                Preferences.setTestDriveSigninTryAgainCount(0);
            } else {
                DialogFragmentFactory.getAlertOkayInstance(
                        activity.getString(R.string.general_server_error).toString(),
                        activity.getString(R.string.general_error_message).toString()).show(getFragmentManager(), null);

                passwordView.setText("");

                eventParams.put("Success:", "No");
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                        Flurry.EVENT_NAME_SUBMIT_REGISTRATION, eventParams);
            }

        }

        public void onRequestFail(Bundle resultData) {

            Map<String, String> eventParams = new HashMap<String, String>();
            eventParams.put("Success:", "No");
            EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION, Flurry.EVENT_NAME_SUBMIT_REGISTRATION,
                    eventParams);

            if (resultData != null) {

                com.concur.mobile.platform.service.parser.Error error = (com.concur.mobile.platform.service.parser.Error) resultData
                        .getSerializable(TestDriveRegistrationAsyncTask.ERROR);

                String errorTitle = getString(R.string.general_network_error);
                String errorMessage = getString(R.string.general_error_message);
                // Flag is set if "Account Already Exists" dialog is thrown up.
                boolean errorAlertShown = false;

                if (error != null) {

                    if (!TextUtils.isEmpty(error.getUserMessage())) {
                        errorMessage = error.getUserMessage();
                    }

                    // If account exists, throw up a dialog to push the user to Sign In.
                    // Else, find error reason, set error title and message and use "OK" dialog.
                    if (error.getCode() != null && error.getCode().equalsIgnoreCase("RegTestDriveUserExistError")) {
                        logFailureEvent("Account Already Exists");
                        errorTitle = getString(R.string.test_drive_reg_error_title);

                        AlertDialogFragment frag = DialogFragmentFactory.getAlertDialog(errorTitle, errorMessage,
                                R.string.test_drive_sign_in, -1, -1, new AlertDialogFragment.OnClickListener() {

                                    public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                                        // no-op
                                    }

                                    public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                                        // If we push to Sign In, load their email address automatically.
                                        String emailText;
                                        Intent intent = new Intent();
                                        if (emailView != null
                                                && !TextUtils.isEmpty(emailText = emailView.getText().toString())) {
                                            intent.putExtra(EmailLookupFragment.ARGS_EMAIL_TEXT_VALUE, emailText);
                                        }
                                        // We can't push the user directly to EmailLookupActivity or PreLogin will hang. Set the result
                                        // and shoot down the User ID to PreLogin, which will automatically launch EmailLookupActivity.
                                        getBaseActivity().setResult(Activity.RESULT_OK, intent);
                                        getBaseActivity().finish();
                                    }

                                }, null, null, null);
                        frag.setCancelable(false);
                        frag.show(getFragmentManager(), null);

                        errorAlertShown = true; // Don't show any other dialog.

                    } else if (errorMessage.toUpperCase().contains("TOO SHORT")) { // TODO: Replace with endpoint check
                        logFailureEvent("Password Too Short");
                        errorTitle = getString(R.string.test_drive_password_too_short_title);
                    } else {
                        logFailureEvent("Server Error");
                    }

                } else {
                    logFailureEvent("Server Error");
                }

                if (!errorAlertShown) {
                    DialogFragmentFactory.getAlertOkayInstance(errorTitle, errorMessage).show(getFragmentManager(),
                            null);
                }
            }
        }

        public void onRequestCancel(Bundle resultData) {
        }

        public void cleanup() {
            hideProgressBar();
            testRegReceiver = null;
        }

        AlertDialogFragment.OnClickListener signInListener = new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                Intent it = new Intent(activity, EmailLookupActivity.class);
                it.putExtra(Const.EXTRA_LOGIN_LAUNCHED_FROM_TEST_DRIVE_REGISTRATION, true);

                TestDriveRegistrationFragment.this.activity.setResult(Activity.RESULT_OK);
                activity.finish();
                startActivity(it);

                Map<String, String> eventParams = new HashMap<String, String>();
                eventParams.put("Choice:", "Sign In");
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                        "Submit Registration Account Already Exists", eventParams);
            }
        };

        AlertDialogFragment.OnClickListener retryListener = new AlertDialogFragment.OnClickListener() {

            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            }

            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                emailView.setText("");
                passwordView.setText("");
                emailView.requestFocus();

                Map<String, String> eventParams = new HashMap<String, String>();
                eventParams.put("Choice:", "Retry");
                EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                        "Submit Registration Account Already Exists", eventParams);
            }
        };
    }

    /**
     * Start Test Drive tour after successfull test drive registration.
     * */
    protected void gotoTestDriveTour() {
        Intent i = new Intent(activity, TestDriveTour.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        TestDriveRegistrationFragment.this.activity.setResult(Activity.RESULT_OK);
        TestDriveRegistrationFragment.this.activity.finish();
        startActivity(i);
    }

    private void logFailureEvent(String failureParam) {
        Map<String, String> eventParams = new HashMap<String, String>();
        eventParams.put("Failure:", failureParam);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_TEST_DRIVE_REGISTRATION,
                Flurry.EVENT_NAME_SUBMIT_REGISTRATION_FAILURE, eventParams);
    }

}
