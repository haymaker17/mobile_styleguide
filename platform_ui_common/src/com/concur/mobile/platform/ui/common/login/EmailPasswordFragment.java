package com.concur.mobile.platform.ui.common.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
import com.concur.mobile.platform.authentication.PPLoginLightRequestTask;
import com.concur.mobile.platform.ui.common.IProgressBarListener;
import com.concur.mobile.platform.ui.common.R;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.util.FormUtil;
import com.concur.mobile.platform.ui.common.util.ViewUtil;

import java.util.Locale;

public class EmailPasswordFragment extends PlatformFragment implements OnClickListener{

    /**
     * Class name used for logging.
     */
    public static final String CLS_NAME = EmailPasswordFragment.class.getSimpleName();

    /**
     * Fragment argument used to pass in an email/username to display in the <code>TextView</code>.
     */
    public static final String ARGS_EMAIL_TEXT_VALUE = "args_email_text_value";


    /**
     * An interface containing various callbacks which this Fragment's parent
     * Activities must implement or else a <code>ClassCastException</code> is thrown.
     *
     * @author Chris N. Diaz
     */
    public interface EmailPasswordCallBacks {

        /**
         * "Format Issue" tracking/logging reason.
         */
        public final static String FAILURE_REASON_FORMAT = "Format Issue";

        /**
         * "Offline" tracking/logging reason.
         */
        public final static String FAILURE_REASON_OFFLINE = "Offline";

        /**
         * Invoked when the EmailLookup request has succeeded.
         *
         * @param resultData the data containing results from the successful request.
         */
        public void onEmailLookupRequestSuccess(Bundle resultData);

        /**
         * Invoked when the EmailLookup request has failed.
         *
         * @param resultData the data containing results from the failure.
         */
        public void onEmailLookupRequestFail(Bundle resultData);

        /**
         * Invoked when the Company Code button is pressed.
         */
        public void onCompanyCodeButtonPressed();

        /**
         * Returns <code>true</code> if there currently is a valid network connection,
         * otherwise, <code>false</code> is returned.
         *
         * @return <code>true</code> if there currently is a valid network connection,
         * otherwise, <code>false</code> is returned.
         */
        public boolean isNetworkConnected(); // NOTE: This could be a concrete implementation where connectivity logic can live in the Platform.

        /**
         * Invoked to log/track a failure during the EmailLookup request.
         *
         * @param failureType string indicating the reason for failing the Email Lookup.
         *                    For example: <code>FAILURE_REASON_FORMAT</code> or <code>FAILURE_REASON_OFFLINE</code>.
         */
        public void trackEmailLookupFailure(String failureType);

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
         * Invoked when the Help button is pressed.
         *
         * @param signInMethod the method the user signed in (e.g. PIN, Password, SSO).
         */
        public void onLoginHelpButtonPressed(String signInMethod);

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
         */
        public void setProgressDialogCancelListener(ProgressDialogFragment.OnCancelListener cancelListener);

        /**
         * Sets Message for parent Activity's <code>ProgressDialogFragment</code>
         *
         */
        public void setProgressDialogMessage(String progressDialogString);

        /**
         * Opens Settings page
         *
         */
        public void openSettings();

        /**
         * Two finger Click listener for View.
         *
         */
        public void setOnTouchListenerForView(View view);
    }


    // ========================
    // Private members
    // ========================

    private static final String EMAIL_LOOK_UP_RECEIVER = "email.lookup.request.receiver";

    private EditText emailView;

    private TextView emailIdLabel;

    private Button contdButton;

    private String emailOrUsername;

    private boolean isEnterPassword;

    private String signInMethod;

    private BaseAsyncResultReceiver emailLookupReceiver;

    private IProgressBarListener progressBarListener;

    private AsyncReplyListener emailLookupReplyListener;

    protected EmailPasswordCallBacks emailPasswordCallBacks;

    private PPLoginLightRequestTask ppLoginLightRequestTask;

    private EmailLookUpRequestTask emailLookupTask;

    private LinearLayout or_sso_layout;

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            emailPasswordCallBacks = (EmailPasswordCallBacks) activity;

            emailLookupReplyListener = new AsyncReplyListener() {
                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestSuccess(android.os.Bundle)
                 */
                @Override
                public void onRequestSuccess(Bundle resultData) {
                    signInMethod = resultData.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                    String passwordHint = getActivity().getString(R.string.login_enter_password_pin);

                    if (signInMethod != null && signInMethod.equalsIgnoreCase(Const.LOGIN_METHOD_PASSWORD)) {
                        passwordHint = getActivity().getString(R.string.login_password);
                    }
                    String text = "";
                    String button = getActivity().getString(R.string.login_button);
                    if (!isEnterPassword) {
                        hideProgressBar();
                        emailPasswordCallBacks.onEmailLookupRequestSuccess(resultData);
                        setEditTextAndLabel(true, passwordHint, emailOrUsername, text, button);
                    } else {
                        emailPasswordCallBacks.onLoginRequestSuccess(resultData);
                        //setEditTextAndLabel(true, passwordHint, emailOrUsername, text, button);
                    }
                }

                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
                 */
                @Override
                public void onRequestFail(Bundle resultData) {
                    String passwordHint = getActivity().getString(R.string.login_password);
                    String text = "";
                    String button = getActivity().getString(R.string.login_button);
                    hideProgressBar();
                    if (!isEnterPassword) {
                        emailPasswordCallBacks.onEmailLookupRequestFail(resultData);
                        setEditTextAndLabel(true, passwordHint, emailOrUsername, text, button);
                    } else {
                        emailPasswordCallBacks.onLoginRequestFail(resultData);
                        setComponentToEmailLookup();
                    }
                }

                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestCancel(android.os.Bundle)
                 */
                @Override
                public void onRequestCancel(Bundle resultData) {
                    cleanup();
                }

                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#cleanup()
                 */
                @Override
                public void cleanup() {
                    if (ppLoginLightRequestTask != null) {
                        ppLoginLightRequestTask.cancel(true);
                    }
                    if (!isEnterPassword) {
                    } else {
                        emailLookupReceiver = null;
                    }
                }

            };

        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement EmailLookupCallbacks");
        }

    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onDetach()
     */
    @Override
    public void onDetach() {
        super.onDetach();

        // Set the callback to null so we don't accidentally leak the  Activity instance.        
        emailPasswordCallBacks = null;
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @SuppressLint("InflateParams")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View root = inflater.inflate(R.layout.email_password_fragment, null);

        or_sso_layout = (LinearLayout) root.findViewById(R.id.or_sso_layout);

        // retrieve receiver if needed
        if (retainer != null) {
            emailLookupReceiver = (BaseAsyncResultReceiver) retainer.get(EMAIL_LOOK_UP_RECEIVER);
            if (emailLookupReceiver != null) {
                emailLookupReceiver.setListener(emailLookupReplyListener);
            }
        }

        setEmailLableAndEditText(root);
        setDoubleFingerTap(root);

        // If we got text pushed through to pre-load on this screen, set it here.
        Bundle args = getArguments();
        if (args != null) {
            String emailText = args.getString(ARGS_EMAIL_TEXT_VALUE);
            if (!TextUtils.isEmpty(emailText)) {
                emailView.setText(emailText);
            }
        }

        // set submit button
        contdButton = (Button) root.findViewById(R.id.contdButton);
        if (contdButton != null) {
            contdButton.setOnClickListener(this);
        }

        setSSOLogin(root);

        setForgorPassword(root);
        // Set the ProgressDialog cancelled listener (if set).
        emailPasswordCallBacks.setProgressDialogCancelListener(progressDlgCancelListner);
        return root;
    }

    /**
     * Sets the <code>IProgressBarListner</code> to show/hide when looking up an email/username.
     *
     * @param proListener the <code>IProgressBarListener</code> to display.
     */
    public void setProgressBarListener(IProgressBarListener proListener) {
        this.progressBarListener = proListener;
    }

    /*
     * (non-Javadoc)
     * @see com.concur.mobile.platform.ui.common.fragment.PlatformFragment#onCreate(android.os.Bundle)
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        //getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    /*
     * (non-Javadoc)
     * @see android.support.v4.app.Fragment#onPause()
     */
    @Override
    public void onPause() {
        super.onPause();

        if (emailLookupReceiver != null) {
            emailLookupReceiver.setListener(null);
            retainer.put(EMAIL_LOOK_UP_RECEIVER, emailLookupReceiver);
        }
    }

    private void setEmailLableAndEditText(View root){
        //set email id label
        emailIdLabel = (TextView) root.findViewById(R.id.emailIdLabel);
        emailIdLabel.setVisibility(View.VISIBLE);
        emailIdLabel.setText(getString(R.string.email_lookup_label));
        // set email and password
        emailView = (EditText) root.findViewById(R.id.emailId);
        ViewUtil.setClearIconToEditText(emailView);
    }

    private void setDoubleFingerTap(View root){
        ImageView concurlogo = (ImageView) root.findViewById(com.concur.mobile.platform.ui.common.R.id.concurlogo);

        concurlogo.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                emailPasswordCallBacks.openSettings();
                return true;
            }
        });

        emailPasswordCallBacks.setOnTouchListenerForView(concurlogo);
    }

    private void setSSOLogin(View root){
        // footer layout
        TextView ssoLoginView = (TextView) root.findViewById(R.id.company_sso_login);
        if (ssoLoginView != null) {
            ssoLoginView.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    emailPasswordCallBacks.onCompanyCodeButtonPressed();
                }
            });
        }
    }


    private void setForgorPassword(View root){
        // footer layout
        TextView forgotPassword = (TextView) root.findViewById(R.id.login_forgot_password);
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    String method;
                    if (signInMethod != null) {
                        method = signInMethod;
                    } else {
                        method = Const.LOGIN_METHOD_PASSWORD;
                    }
                    emailPasswordCallBacks.onLoginHelpButtonPressed(method);
                }
            });
        }

    }


    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.contdButton) {
            String cont = getString(R.string.email_lookup_continue).toString();
            String signin = getString(R.string.login_button).toString();
            String text = contdButton.getText().toString();
            if (text.equalsIgnoreCase(signin)) {
                emailPasswordCallBacks.setProgressDialogMessage(getActivity().getString(R.string.dlg_logging_in).toString());
                if (!emailPasswordCallBacks.isNetworkConnected()) {
                    new NoConnectivityDialogFragment().show(getFragmentManager(), null);
                    emailPasswordCallBacks.trackEmailLookupFailure(EmailPasswordCallBacks.FAILURE_REASON_OFFLINE);
                    return;
                } else {
                    String email = emailIdLabel.getText().toString().trim();
                    String password = emailView.getText().toString().trim();
                    // We allow work email and username ID. Note, however, that
                    // a username ID *must* contain the '@' symbol.
                    if ((!TextUtils.isEmpty(email) && FormUtil.isLoginUsernameValid(email))
                            && (!TextUtils.isEmpty(password))) {

                        if (emailLookupReceiver == null) {
                            emailLookupReceiver = new BaseAsyncResultReceiver(new Handler());
                        }
                        showProgressBar();
                        emailLookupReceiver.setListener(emailLookupReplyListener);
                        emailPasswordCallBacks.onSignInButtonClicked(email, password, signInMethod);
                        // Attempt to authenticate
                        // Re-entry to the UI will be via handleMessage() below
                        Locale locale = Locale.getDefault();
                        ppLoginLightRequestTask = new PPLoginLightRequestTask(getActivity().getApplicationContext(),
                                emailLookupReceiver, 1, locale, email, password);
                        ppLoginLightRequestTask.execute();

                    } else {
                        DialogFragmentFactory.getAlertOkayInstance(
                                getActivity().getText(R.string.email_lookup_wrong_email_format_title).toString(),
                                R.string.email_lookup_wrong_email_username_format_msg).show(getFragmentManager(), null);

                        emailPasswordCallBacks.trackEmailLookupFailure(EmailPasswordCallBacks.FAILURE_REASON_FORMAT);
                    }
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);
                }
            } else if (text.equalsIgnoreCase(cont)) {
                emailPasswordCallBacks.setProgressDialogMessage(getActivity().getString(R.string.dlg_retrieve).toString());
                if (!emailPasswordCallBacks.isNetworkConnected()) {
                    new NoConnectivityDialogFragment().show(getFragmentManager(), null);
                    emailPasswordCallBacks.trackEmailLookupFailure(EmailPasswordCallBacks.FAILURE_REASON_OFFLINE);

                    return;
                } else {
                    emailOrUsername = emailView.getText().toString().trim();
                    // We allow work email and username ID. Note, however, that
                    // a username ID *must* contain the '@' symbol.
                    if (!TextUtils.isEmpty(emailOrUsername) && FormUtil.isLoginUsernameValid(emailOrUsername)) {
                        showProgressBar();
                        emailLookupReceiver = new BaseAsyncResultReceiver(new Handler());
                        emailLookupReceiver.setListener(emailLookupReplyListener);

                        // Invoke web service to lookup the email/username.
                        Locale locale = getResources().getConfiguration().locale;
                        emailLookupTask= new EmailLookUpRequestTask(
                                getActivity().getApplicationContext(), 0, emailLookupReceiver, locale, emailOrUsername);
                        emailLookupTask.execute();
                    } else {
                        DialogFragmentFactory.getAlertOkayInstance(
                                getActivity().getText(R.string.email_lookup_wrong_email_format_title).toString(),
                                R.string.email_lookup_wrong_email_username_format_msg).show(getFragmentManager(), null);

                        //EmailPasswordCallBacks.trackEmailLookupFailure(EmailPasswordCallBacks.FAILURE_REASON_FORMAT);
                    }
                    // Hide the keyboard
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);
                }
            }
        } else if (id == R.id.emailIdLabel)

        {
            setComponentToEmailLookup();
        }
    }


    private void setComponentToEmailLookup() {
        if (!(getString(R.string.email_lookup_label).equalsIgnoreCase(emailIdLabel.getText().toString()))) {
            String hint = getActivity().getString(R.string.email_lookup_hint);
            String text = getString(R.string.email_lookup_label);
            String button = getActivity().getString(R.string.email_lookup_continue);
            setEditTextAndLabel(false, hint, text, emailIdLabel.getText().toString(), button);
        }
    }

    private void setEditTextAndLabel(boolean isPassword, String hint, String labelText, String editText, String buttonText) {
        emailIdLabel.setText(labelText);
        emailIdLabel.setOnClickListener(this);
        isEnterPassword = isPassword;
        emailView.setHint(hint);
        Animation fadeInAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.transition);
        fadeInAnim.setDuration(3000L);
        fadeInAnim.setFillAfter(false);
        emailView.setText(editText);

        if (isEnterPassword) {
            emailView.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_PASSWORD);
            or_sso_layout.setVisibility(View.GONE);
        } else {
            emailView.setInputType(InputType.TYPE_CLASS_TEXT |
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE | InputType.TYPE_TEXT_FLAG_AUTO_CORRECT
                    | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_FILTER | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            or_sso_layout.setVisibility(View.VISIBLE);
        }
        contdButton.setText(buttonText);
    }

    /**
     * If an <code>IProgressBarlistener</code> is attached, then show it.
     */
    protected void showProgressBar() {
        if (progressBarListener != null) {
            progressBarListener.showProgressBar();
        } else {
            Log.w(Const.LOG_TAG, CLS_NAME + ".showProgressBar(): progressBarListener is null!");
        }
    }

    /**
     * If an <code>IProgressBarlistener</code> is attached, then hide it.
     */
    protected void hideProgressBar() {
        if (progressBarListener != null) {
            progressBarListener.hideProgressBar();
        } else {
            Log.w(Const.LOG_TAG, CLS_NAME + ".hideProgressBar(): progressBarListener is null!");
        }
    }

    /**
     * Listener called when an attached Progress Dialog is canceled. This listener cancels any pending
     * <code>PPLoginRequestTask</code>.
     */
    private final ProgressDialogFragment.OnCancelListener progressDlgCancelListner = new ProgressDialogFragment.OnCancelListener() {

        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            if (ppLoginLightRequestTask != null) {
                ppLoginLightRequestTask.cancel(true);
            }

            if(emailLookupTask !=null){
                emailLookupTask.cancel(true);
            }
        }
    };
    
}
