package com.concur.mobile.platform.ui.common.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.PPLoginLightRequestTask;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment.OnCancelListener;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.util.ViewUtil;

import java.util.Locale;

/**
 * Fragment used to perform an authentication of a user, given a login ID.
 *
 * @author
 */
public class NewLoginPasswordFragment extends PlatformFragment implements View.OnClickListener {

    /**
     * Tag used for logging in this class.
     */
    public static final String CLS_TAG = NewLoginPasswordFragment.class.getSimpleName();

    /**
     * An interface containing various callbacks which this Fragment's parent Activities must implement or else a
     * <code>ClassCastException</code> is thrown.
     *
     * @author sunill
     */
    public interface LoginPasswordCallbacks {

        /**
         * "Offline" tracking/logging reason.
         */
        public final static String FAILURE_REASON_OFFLINE = "Offline";

        /**
         * Invoked when the PIN/Password Login request has succeeded.
         *
         * @param resultData the data containing results from the successful request.
         */
        public void onLoginRequestSuccess(Bundle resultData);

        /**
         * Invoked when the PIN/Password Login request has failed.
         *
         * @param resultData the data containing results from the failure.
         */
        public void onLoginRequestFail(Bundle resultData);

        /**
         * Returns <code>true</code> if there currently is a valid network connection, otherwise, <code>false</code> is returned.
         *
         * @return <code>true</code> if there currently is a valid network connection, otherwise, <code>false</code> is returned.
         */
        public boolean isNetworkConnected(); // NOTE: This could be a concrete implementation where connectivity logic can live in
        // the Platform.

        /**
         * Invoked when the Help button is pressed.
         *
         * @param signInMethod the method the user signed in (e.g. PIN, Password, SSO).
         */
        public void onLoginHelpButtonPressed(String signInMethod);

        /**
         * Returns <code>true</code> if the current user has a previously saved userId and pin/password. Otherwise,
         * <code>false</code> is returned.
         *
         * @return <code>true</code> if the current user has a previously saved userId and pin/password. Otherwise,
         * <code>false</code> is returned.
         */
        public boolean hasSavedCredentials();

        /**
         * Invoked to log/track a success or failure during the Authentication request.
         *
         * @param success <code>true</code> if the Login request
         * @param message string indicating the reason for failing the Email Lookup. For example: <code>FAILURE_REASON_OFFLINE</code>.
         */
        public void trackLoginStatus(boolean success, String message);

        /**
         * Invoked when the user presses the Sign-In button to authenticate. Implementing classes should be responsible for things
         * like displaying a progress dialog, saving the credentials to the DB store, etc.
         *
         * @param userId        The login username/email.
         * @param pinOrPassword The user's PIN or Password used for authentication.
         * @param signInMethod  The sign-in method used by this user. Either one of the following: <code>Const.LOGIN_METHOD_SSO</code>,
         *                      <code>Const.LOGIN_METHOD_MOBILE_PASSWORD</code>, or <code>Const.LOGIN_METHOD_PASSWORD</code>.
         */
        public void onSignInButtonClicked(String userId, String pinOrPassword, String signInMethod);

        /**
         * Returns the parent Activity's <code>ProgressDialogFragment</code>, or <code>null</code. if none was set.
         *
         * @return the parent Activity's <code>ProgressDialogFragment</code>, or <code>null</code. if none was set.
         */
        public void setProgressDialogCancelListener(OnCancelListener cancelListener);

        /**
         * Opens Settings page
         */
        public void openSettings();

        /**
         * Two finger Click listener for View.
         */
        public void setOnTouchListenerForView(View view);
    }

    /**
     * Argument flag used to show/hide the login help link as well as the "I Forgot" button when the user enters an incorrect
     * password. By default, this is diabled (i.e. the link and button is hidden).
     */
    public final static String ARGS_SHOW_LOGIN_HELP = "show_login_help_arg";

    /**
     * Contains whether getActivity() activity was launched with a logged out flag.
     */
    protected boolean loggedOut;

    /**
     * A reference to the parent Activity that implements the <code>LoginPasswordCallbacks</code>.
     */
    protected LoginPasswordCallbacks loginPasswordCallbacks;

    private EditText passwordView;

    private TextView passwordLabel;

    private Button loginButton;

    private String loginId;

    private String signInMethod;

    /**
     * BroadcastReceiver used to detect network connectivity and update the UI accordingly.
     */
    private BroadcastReceiver connectivityReceiver;

    private IntentFilter connectivityFilter;

    private boolean connectivityReceiverRegistered;

    private BaseAsyncResultReceiver loginReceiver;

    private Bundle emailLookupBundle;

    private PPLoginLightRequestTask ppLoginLightRequestTask;

    private AsyncReplyListener loginPasswordReplyListener;

    /**
     * Listener called when an attached Progress Dialog is canceled. This listener cancels any pending
     * <code>PPLoginRequestTask</code>.
     */
    private final OnCancelListener progressDlgCancelListner = new OnCancelListener() {

        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            if (ppLoginLightRequestTask != null) {
                ppLoginLightRequestTask.cancel(true);
            }

            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }
    };

    /**
     * Listener which is invoked when the user presses the "forgot password" link or the "I forgot" button in the alert dialog
     * after entering an incorrect password. This listener is only invoked if the argument <code>ARGS_SHOW_LOGIN_HELP</code> is
     * set to <code>true</code>
     */
    private final AlertDialogFragment.OnClickListener loginHelpListener = new AlertDialogFragment.OnClickListener() {

        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }

        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            loginPasswordCallbacks.onLoginHelpButtonPressed(signInMethod);
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }
    };

    /**
     * Listener which is invoked when the user presses the "Cancel" button in the alert dialog after entering an incorrect
     * password.
     */
    private final AlertDialogFragment.OnClickListener cancelListener = new AlertDialogFragment.OnClickListener() {

        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }

        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }
    };

    /**
     * Listener which is invoked if the user enters an incorrect password and then presses the "Try Again" button in the alert
     * dialog.
     */
    private final AlertDialogFragment.OnClickListener retryLoginListener = new AlertDialogFragment.OnClickListener() {

        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }

        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            EditText passwordTextView = (EditText) activity.findViewById(R.id.loginPin);
            if (passwordTextView != null) {
                passwordTextView.setText("");
                // Show the keyboard
                ViewUtil.showSoftKeyboard(activity, activity.getResources().getConfiguration());
                passwordTextView.requestFocus();
            }
            // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
            // when auth cancels
            loginButton.setEnabled(true);
        }
    };

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            loginPasswordCallbacks = (LoginPasswordCallbacks) activity;

            loginPasswordReplyListener = new AsyncReplyListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestSuccess(android.os.Bundle)
                 */
                public void onRequestSuccess(Bundle resultData) {
                    loginPasswordCallbacks.onLoginRequestSuccess(resultData);

                    // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
                    // when auth cancels
                    loginButton.setEnabled(true);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
                 */
                public void onRequestFail(Bundle resultData) {
                    loginPasswordCallbacks.onLoginRequestFail(resultData);

                    // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
                    // when auth fails
                    loginButton.setEnabled(true);
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestCancel(android.os.Bundle)
                 */
                public void onRequestCancel(Bundle resultData) {
                    // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
                    // when auth cancels
                    loginButton.setEnabled(true);

                    return;
                }

                /*
                 * (non-Javadoc)
                 * 
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#cleanup()
                 */
                public void cleanup() {
                    return;
                }
            };

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement LoginPasswordCallbacks");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        super.onDetach();

        // Set the callback to null so we don't accidentally leak the Activity instance.
        loginPasswordCallbacks = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.ui.common.fragment.PlatformFragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.new_password_fragment, null);

        // Get arguments.
        Bundle args = getArguments();
        if (args != null) {
            emailLookupBundle = args.getBundle(EmailLookUpRequestTask.EXTRA_LOGIN_BUNDLE);
        } else {
            emailLookupBundle = null;
        }

        if (emailLookupBundle != null) {
            loginId = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY);
            signInMethod = emailLookupBundle.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
        }

        // Set the 'loggedOut' state.
        if (savedInstanceState != null) {
            loggedOut = savedInstanceState.getBoolean(Const.EXTRA_LOGOUT, false);
        } else {
            loggedOut = getActivity().getIntent().getBooleanExtra(Const.EXTRA_LOGOUT, false);
        }

        loginButton = (Button) root.findViewById(R.id.loginButton);
        loginButton.setOnClickListener(this);

        connectivityReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                updateUIForConnectivity(intent.getAction());
            }
        };
        connectivityFilter = new IntentFilter(Const.ACTION_DATA_CONNECTIVITY_AVAILABLE);
        connectivityFilter.addAction(Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE);

        passwordView = (EditText) root.findViewById(R.id.loginPin);
        // set default type face so user can see login and password editview hint in same font style.
        passwordView.setTypeface(Typeface.DEFAULT);
        //set clear icon
        ViewUtil.setClearIconToEditText(passwordView);

        //set passwordLabel with email id
        passwordLabel = (TextView) root.findViewById(R.id.passwordLabel);
        passwordLabel.setText(loginId);
        passwordLabel.setOnClickListener(this);

        if (signInMethod == null || signInMethod.equalsIgnoreCase(Const.LOGIN_METHOD_PASSWORD)) {
            passwordView.setHint(R.string.login_password);
        }
        // Set the default action for the pin view to do the submit
        passwordView.setOnEditorActionListener(new OnEditorActionListener() {

            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // If the user presses Go or Enter, then proceed to login.
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    onClick(loginButton);
                    return true;
                }
                return false;
            }
        });

        // Register the data connectivity receiver.
        Intent stickyIntent = getActivity().registerReceiver(connectivityReceiver, connectivityFilter);
        if (stickyIntent != null) {
            updateUIForConnectivity(stickyIntent.getAction());
        } else {
            updateUIForConnectivity(null);
        }
        connectivityReceiverRegistered = true;

        loginReceiver = new BaseAsyncResultReceiver(new Handler());
        loginReceiver.setListener(loginPasswordReplyListener);

        // Set the ProgressDialog cancelled listener (if set).
        loginPasswordCallbacks.setProgressDialogCancelListener(progressDlgCancelListner);

        //set two finger double tap settings
        setDoubleFingerTap(root);

        setForgotPassword(root);

        return root;
    }

    private void setDoubleFingerTap(View root) {
        ImageView concur_logo = (ImageView) root.findViewById(com.concur.mobile.platform.ui.common.R.id.concurlogo);

        concur_logo.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                loginPasswordCallbacks.openSettings();
                return true;
            }
        });

        loginPasswordCallbacks.setOnTouchListenerForView(concur_logo);
    }

    private void setForgotPassword(View root) {
        // footer layout
        TextView forgotPassword = (TextView) root.findViewById(R.id.login_help);
        forgotPassword.setOnClickListener(this);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    String method;
                    if (signInMethod != null) {
                        method = signInMethod;
                    } else {
                        method = Const.LOGIN_METHOD_PASSWORD;
                    }
                    loginPasswordCallbacks.onLoginHelpButtonPressed(method);
                }
            });
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();
        // Unregister the data connectivity receiver.
        if (connectivityReceiverRegistered) {
            getActivity().unregisterReceiver(connectivityReceiver);
            connectivityReceiverRegistered = false;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        // Re-register connectivity receiver.
        if (!connectivityReceiverRegistered) {
            // Register the receiver.
            Intent stickyIntent = getActivity().registerReceiver(connectivityReceiver, connectivityFilter);
            if (stickyIntent != null) {
                updateUIForConnectivity(stickyIntent.getAction());
            }
            connectivityReceiverRegistered = true;
        }

        if (loginId != null && loginId.length() > 0) {
            // Focus on the pin
            passwordView.requestFocus();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.loginButton) {

            if (loginPasswordCallbacks.isNetworkConnected()) {
                // MOB-20038 prevent repeated clicking on login button by disabling it
                // when authenticating, and re-enabling it later
                loginButton.setEnabled(false);

                // Grab values
                String pinOrPassword = passwordView.getText().toString();

                // MOB-16163 - validate username and password
                if (pinOrPassword.trim().length() > 0) {

                    // Display a progress dialog and save credentials.
                    loginPasswordCallbacks.onSignInButtonClicked(loginId, pinOrPassword, signInMethod);
                    // Attempt to authenticate
                    // Re-entry to the UI will be via handleMessage() below
                    Locale locale = Locale.getDefault();
                    ppLoginLightRequestTask = new PPLoginLightRequestTask(getActivity().getApplicationContext(),
                            loginReceiver, 1, locale, loginId, pinOrPassword);
                    ppLoginLightRequestTask.execute();

                } else {
                    StringBuilder msgBuilder = new StringBuilder();
                    msgBuilder.append(getText(R.string.email_lookup_unable_to_login_msg));
                    showInvalidPasswordError(msgBuilder);
                }

            } else {
                new NoConnectivityDialogFragment().show(getFragmentManager(), null);
                loginPasswordCallbacks.trackLoginStatus(false, LoginPasswordCallbacks.FAILURE_REASON_OFFLINE);
            }
            // Hide the keyboard
            ViewUtil.hideSoftKeyboard(getActivity(), passwordView.getWindowToken());

        } else if (v.getId() == R.id.passwordLabel) {
            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    /**
     * Convenience method for displaying an error dialog about invalid username/password using the given message.
     *
     * @param msgBuilder the error message (reason) to display in the error dialog.
     */
    public void showInvalidPasswordError(StringBuilder msgBuilder) {

        DialogFragmentFactory.getAlertOkayInstance(
                getActivity().getText(R.string.email_lookup_unable_to_login_title).toString(),
                msgBuilder.toString()).show(getFragmentManager(), null);

        // MOB-20038 prevent repeated clicking on login button by disabling it when authenticating, and re-enabling it
        // when auth cancels
        loginButton.setEnabled(true);
        passwordView.setText("");
    }


    public void removeFragment(){
        getActivity().getSupportFragmentManager().popBackStack();
    }
    // ################# HELPER METHODS #################### //

    /**
     * Will update the UI based on the current state of connectivity.
     */
    private void updateUIForConnectivity(String action) {

        boolean connected = false;
        if (action != null) {
            if (action.equalsIgnoreCase(Const.ACTION_DATA_CONNECTIVITY_AVAILABLE)) {
                connected = true;
            } else if (action.equalsIgnoreCase(Const.ACTION_DATA_CONNECTIVITY_UNAVAILABLE)) {
                connected = false;
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateUIForConnectivity: unhandled action '" + action + "'.");
                connected = loginPasswordCallbacks.isNetworkConnected();
            }
        } else {
            connected = loginPasswordCallbacks.isNetworkConnected();
        }

        if (connected) {
            loginButton.setEnabled(true);

            // Hide the "offline" header.
            View view = getActivity().findViewById(R.id.login_offline_header);
            if (view != null) {
                View offlineHeader = view.findViewById(R.id.offline_header);
                if (offlineHeader != null && offlineHeader.getVisibility() == View.VISIBLE) {
                    offlineHeader.setVisibility(View.GONE);
                }
            }

        } else {

            // Hide show the "offline" header.
            View offlineHeader = getActivity().findViewById(R.id.offline_header);
            if (offlineHeader != null && offlineHeader.getVisibility() == View.GONE) {
                offlineHeader.setVisibility(View.VISIBLE);
            }

            if (loginPasswordCallbacks.hasSavedCredentials() && !loggedOut) {
                // If there are saved credentials and the user hasn't logged
                // out, then hide the notice and allow login
                loginButton.setEnabled(true);
            }
        }
    }

}
