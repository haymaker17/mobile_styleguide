package com.concur.mobile.platform.ui.common.login;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;
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

public class EmailPasswordLookupFragment extends PlatformFragment implements OnClickListener {

    /**
     * Class name used for logging.
     */
    public static final String CLS_NAME = EmailPasswordLookupFragment.class.getSimpleName();

    /**
     * Fragment argument used to pass in an email/username to display in the <code>TextView</code>.
     */
    public static final String ARGS_EMAIL_TEXT_VALUE = "args_email_text_value";

    /**
     * Fragment argument used to pass in sign in method to display in the <code>EditText</code> hint.
     */
    public static final String ARGS_SIGN_IN_METHOD_TEXT_VALUE = "args_sign_in_method_text_value";

    /**
     * An interface containing various callbacks which this Fragment's parent
     * Activities must implement or else a <code>ClassCastException</code> is thrown.
     * 
     * @author sunill
     */
    public interface EmailLookupCallbacks {
        
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
         *  For example: <code>FAILURE_REASON_FORMAT</code> or <code>FAILURE_REASON_OFFLINE</code>.
         */
        public void trackEmailLookupFailure(String failureType);

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

        /**
         * Invoked when the Help button is pressed.
         *
         * @param signInMethod the method the user signed in (e.g. PIN, Password, SSO).
         */
        public void onLoginHelpButtonPressed(String signInMethod);

        /**
         * Returns the parent Activity's <code>ProgressDialogFragment</code>, or <code>null</code. if none was set.
         *
         */
        public void setProgressDialogCancelListener(ProgressDialogFragment.OnCancelListener cancelListener);

    }
    
    
    // ========================
    // Private members
    // ========================
    
    private static final String EMAIL_LOOK_UP_RECEIVER = "email.lookup.request.receiver";

    private EditText emailView;

    private Button contdButton;

    private TextView emailIdLabel;

    private BaseAsyncResultReceiver emailLookupReceiver;

    private IProgressBarListener progressBarListener;
    
    private AsyncReplyListener emailLookupReplyListener;

    protected EmailLookupCallbacks emailLookupCallbacks;

    private LinearLayout or_sso_layout;

    private String signInMethod;

    private EmailLookUpRequestTask emailLookupTask;
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
            emailLookupCallbacks = (EmailLookupCallbacks) activity;
            
            emailLookupReplyListener = new AsyncReplyListener() {
                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestSuccess(android.os.Bundle)
                 */
                @Override
                public void onRequestSuccess(Bundle resultData) {
                    signInMethod = resultData.getString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY);
                    emailLookupCallbacks.onEmailLookupRequestSuccess(resultData);
                }
                
                /*
                 * (non-Javadoc)
                 * @see com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener#onRequestFail(android.os.Bundle)
                 */
                @Override
                public void onRequestFail(Bundle resultData) {
                    String emailOrUsername = emailView.getText().toString().trim();
                    // Set the login id.
                    resultData.putString(EmailLookUpRequestTask.EXTRA_LOGIN_ID_KEY, emailOrUsername);
                    // Set the sign-in method to Password as default.
                    resultData.putString(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY, Const.LOGIN_METHOD_PASSWORD);

                    emailLookupCallbacks.onEmailLookupRequestFail(resultData);
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
                    hideProgressBar();
                    emailLookupReceiver = null;                
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
        emailLookupCallbacks = null;
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

        // set email and password
        emailView = (EditText) root.findViewById(R.id.emailId);
        //set clear locon
        ViewUtil.setClearIconToEditText(emailView);

        // retrieve receiver if needed
        if (retainer != null) {
            emailLookupReceiver = (BaseAsyncResultReceiver) retainer.get(EMAIL_LOOK_UP_RECEIVER);
            if (emailLookupReceiver != null) {
                emailLookupReceiver.setListener(emailLookupReplyListener);
            }
        }

        // If we got text pushed through to pre-load on this screen, set it here.
        Bundle args = getArguments();
        if(args != null) {
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

        setForgotPassword(root);

        setDoubleFingerTap(root);

        setEmailLableText(root);

        // Set the ProgressDialog cancelled listener (if set).
        emailLookupCallbacks.setProgressDialogCancelListener(progressDlgCancelListner);

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

    private void setDoubleFingerTap(View root){
        ImageView concur_logo = (ImageView) root.findViewById(com.concur.mobile.platform.ui.common.R.id.concurlogo);

        concur_logo.setOnLongClickListener(new View.OnLongClickListener() {

            @Override
            public boolean onLongClick(View v) {
                emailLookupCallbacks.openSettings();
                return true;
            }
        });

        emailLookupCallbacks.setOnTouchListenerForView(concur_logo);
    }

    private void setSSOLogin(View root){
        // footer layout
        TextView ssoLoginView = (TextView) root.findViewById(R.id.company_sso_login);
        if (ssoLoginView != null) {
            ssoLoginView.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    emailLookupCallbacks.onCompanyCodeButtonPressed();
                }
            });
        }
    }


    private void setForgotPassword(View root){
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
                    emailLookupCallbacks.onLoginHelpButtonPressed(method);
                }
            });
        }

    }

    private void setEmailLableText(View root){
        //set email id label
        emailIdLabel = (TextView) root.findViewById(R.id.emailIdLabel);
        emailIdLabel.setVisibility(View.VISIBLE);
        emailIdLabel.setText(getString(R.string.email_lookup_label));
    }

    /*
     * (non-Javadoc)
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    @Override
    public void onClick(View v) {
        int id = v.getId();

        if(id == R.id.contdButton) {
            if (!emailLookupCallbacks.isNetworkConnected()){
                new NoConnectivityDialogFragment().show(getFragmentManager(), null);
                emailLookupCallbacks.trackEmailLookupFailure(EmailLookupCallbacks.FAILURE_REASON_OFFLINE);
                return;
            } else  { 
                String emailOrUsername = emailView.getText().toString().trim();
                // We allow work email and username ID. Note, however, that
                // a username ID *must* contain the '@' symbol.
                if (!TextUtils.isEmpty(emailOrUsername) && FormUtil.isLoginUsernameValid(emailOrUsername)) {
                    showProgressBar();
                    emailLookupReceiver = new BaseAsyncResultReceiver(new Handler());
                    emailLookupReceiver.setListener(emailLookupReplyListener);
                    
                    // Invoke web service to lookup the email/username.
                    Locale locale = getResources().getConfiguration().locale;
                    emailLookupTask = new EmailLookUpRequestTask(
                            getActivity().getApplicationContext(), 0, emailLookupReceiver, locale, emailOrUsername);
                    emailLookupTask.execute();
                } else {
                    DialogFragmentFactory.getAlertOkayInstance(
                            getActivity().getText(R.string.email_lookup_wrong_email_format_title).toString(),
                            R.string.email_lookup_wrong_email_username_format_msg).show(getFragmentManager(), null);
                    
                    emailLookupCallbacks.trackEmailLookupFailure(EmailLookupCallbacks.FAILURE_REASON_FORMAT);
                }
                // Hide the keyboard
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(emailView.getWindowToken(), 0);
            }
        }
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

            if(emailLookupTask !=null){
                emailLookupTask.cancel(true);
            }
        }
    };

}

