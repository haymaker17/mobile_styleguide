/**
 *
 */
package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.text.Editable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.concur.breeze.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.service.CorpSsoQueryReply;
import com.concur.mobile.core.service.CorpSsoQueryRequest;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.corp.ConcurMobile;
import com.concur.mobile.platform.ui.common.util.ViewUtil;

/**
 * An extension of <code>BaseActivity</code> supporting SSO-based corporate login.
 */
public class CompanyCodeLoginActivity extends BaseActivity {

    private static final String CLS_TAG = CompanyCodeLoginActivity.class.getSimpleName();

    private static final String CORP_SSO_QUERY_RECEIVER_KEY = "corp.sso.query.receiver";
    private static final String CORP_COMPANY_CODE_KEY = "corp.company.code";

    // Progress dialog.
    private static final int DIALOG_SSO_QUERY_PROGRESS = 1;
    // Company SSO not enabled.
    private static final int DIALOG_SSO_QUERY_DISABLED = 2;
    // Company code not found.
    private static final int DIALOG_SSO_QUERY_NOT_FOUND = 3;
    // End-user has not entered a company code.
    private static final int DIALOG_NO_COMPANY_CODE = 4;
    // Corp SSO query has failed.
    private static final int DIALOG_SSO_QUERY_FAILED = 5;

    private Button loginButton;

    protected EditText companyCodeView;

    // Contains the receiver used to handle the results of a corporate SSO
    // query.
    private CorpSsoQueryReceiver corpSsoQueryReceiver;

    // Contains the filter used to register the corporate SSO query receiver.
    private IntentFilter corpSsoQueryFilter;

    // Contains a reference to an outstanding request to retrieve corp SSO
    // information.
    private CorpSsoQueryRequest corpSsoQueryRequest;

    private boolean fromNotification;

    private String companyCode;

    // Double Tap Finger
    private static final int TIMEOUT = ViewConfiguration.getDoubleTapTimeout() + 100;

    private long mFirstDownTime = 0;

    private boolean mSeparateTouches = false;

    private byte mTwoFingerTapCount = 0;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // notification
        if (getIntent() != null && getIntent().getExtras() != null) {
            fromNotification = getIntent().getExtras().getBoolean(ConcurMobile.FROM_NOTIFICATION);
        }

        // company code.
        companyCode = null;
        if (getIntent() != null && getIntent().getExtras() != null) {
            companyCode = getIntent().getExtras().getString(Const.EXTRA_SSO_COMPANY_CODE);
        }

        // Determine whether this activity should immediately forward to the
        // Company SignIn activity.
        boolean advanceToCompanySignOn = getIntent().getBooleanExtra(EmailLookupActivity.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, false);
        CorpSsoQueryReply ssoReply = null;
        if (!orientationChange && advanceToCompanySignOn) {
            // If 'advanceToCompanyLogon' is 'true', then this implies that a
            // previous response to a request
            // to retrieve SSO login information has been retrieved. Sanity
            // check that this reply is available
            // indicating SSO is enabled *and* a URL has been provided. Also,
            // check that no company code value
            // has been passed in, if it has, then that will pre-empt using
            // cached SSO URL information.
            ConcurMobile concurMobile = (ConcurMobile) getApplication();
            ConcurService concurService = concurMobile.getService();
            ssoReply = concurService.getCorpSsoQueryReply();
            if (ssoReply != null && ssoReply.ssoEnabled && ssoReply.ssoUrl != null
                    && (companyCode == null || companyCode.length() == 0)) {
                // Launch the company sign-on activity.
                Intent i = new Intent(this, CompanySignOnActivity.class);
                i.putExtra(ConcurMobile.FROM_NOTIFICATION, fromNotification);
                startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
            } else {
                // Set this flag to false.
                advanceToCompanySignOn = false;
            }
        }

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.company_sso_login);

        loginButton = (Button) findViewById(R.id.loginButton);
        if (loginButton == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'loginButton'!");
        }


        setDoubleFingerTap();

        companyCodeView = (EditText) findViewById(R.id.companyCode);
        if (companyCodeView != null) {
            ViewUtil.setClearIconToEditText(companyCodeView);
            // Set the default action for the pin view to do the submit
            companyCodeView.setOnEditorActionListener(new OnEditorActionListener() {

                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    if (actionId == EditorInfo.IME_ACTION_GO) {
                        CompanyCodeLoginActivity.this.onClick(loginButton);
                        return true;
                    }
                    return false;
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to locate 'companyCode'!");
        }

        // Restore any saved value for company code.
        if (savedInstanceState != null && savedInstanceState.containsKey(CORP_COMPANY_CODE_KEY)) {
            companyCodeView.setText(savedInstanceState.getString(CORP_COMPANY_CODE_KEY));
        } else {
            // If 'companyCode' is set, the fill it in the text view.
            if (companyCode != null) {
                companyCodeView.setText(companyCode);
            }
        }

        // Skip this logic if re-creation is due to an orientation change.
        if (!orientationChange) {
            // If the "advance to company signon" did not happen from above and
            // we have a non-null
            // company code, then automatically kick-off a new request for SSO
            // URL information.
            if (!advanceToCompanySignOn && companyCode != null && companyCode.length() > 0) {
                onClick(loginButton);
            }
        }

        // Restore any receivers.
        restoreReceivers();
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
    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
            case DIALOG_SSO_QUERY_PROGRESS: {
                ProgressDialog dialog = new ProgressDialog(this);
                dialog.setMessage(this.getText(R.string.login_sso_query));
                dialog.setIndeterminate(true);
                dialog.setCancelable(true);
                dialog.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        if (corpSsoQueryRequest != null) {
                            corpSsoQueryRequest.cancel();
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: corpSsoQueryRequest is null!");
                        }
                    }
                });
                dlg = dialog;
                break;
            }
            case DIALOG_SSO_QUERY_DISABLED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.login_sso_disabled_title);
                dlgBldr.setMessage(getText(R.string.login_sso_disabled_msg));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        // Finish the activity.
                        finish();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DIALOG_SSO_QUERY_NOT_FOUND: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.general_not_found);
                dlgBldr.setMessage(getText(R.string.login_sso_company_code_not_found_msg));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DIALOG_NO_COMPANY_CODE: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.general_missing_value);
                dlgBldr.setMessage(getText(R.string.login_sso_no_company_code_msg));
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            case DIALOG_SSO_QUERY_FAILED: {
                AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                dlgBldr.setTitle(R.string.login_sso_query_failed_title);
                dlgBldr.setMessage("");
                dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dlg = dlgBldr.create();
                break;
            }
            default: {
                super.onCreateDialog(id);
                break;
            }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
            case DIALOG_SSO_QUERY_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            default: {
                super.onPrepareDialog(id, dialog);
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Const.REQUEST_CODE_SSO_LOGIN) {
            if (resultCode == Activity.RESULT_OK) {
                setResult(resultCode, data);
                finish();
            }
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.loginButton: {
                if (companyCodeView.getText() != null) {
                    Editable ccEditable = companyCodeView.getText();
                    if (ccEditable != null) {
                        String companyCode = null;
                        if ((companyCode = ccEditable.toString()) != null) {
                            companyCode = companyCode.trim();
                            if (companyCode.length() > 0) {
                                if (ConcurMobile.isConnected()) {
                                    sendCorpSsoQueryRequest(companyCode);
                                } else {
                                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                                }
                            } else {
                                showDialog(DIALOG_NO_COMPANY_CODE);
                            }
                        } else {
                            showDialog(DIALOG_NO_COMPANY_CODE);
                        }
                    } else {
                        showDialog(DIALOG_NO_COMPANY_CODE);
                    }
                }
                break;
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save the corp SSO query receiver
        if (corpSsoQueryReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            corpSsoQueryReceiver.setActivity(null);
            // Store it in the retainer
            retainer.put(CORP_SSO_QUERY_RECEIVER_KEY, corpSsoQueryReceiver);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.corp.activity.BaseActivity#onSaveInstanceState(android .os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (companyCodeView != null && companyCodeView.getText() != null) {
            Editable ccEditable = companyCodeView.getText();
            String ccCode = ccEditable.toString();
            outState.putString(CORP_COMPANY_CODE_KEY, ccCode);
        }
    }

    protected void restoreReceivers() {
        // Restore any retained data
        if (retainer.contains(CORP_SSO_QUERY_RECEIVER_KEY)) {
            corpSsoQueryReceiver = (CorpSsoQueryReceiver) retainer.get(CORP_SSO_QUERY_RECEIVER_KEY);
            corpSsoQueryReceiver.setActivity(this);
        }
    }

    /**
     * Will send a request to obtain an itinerary list.
     */
    private void sendCorpSsoQueryRequest(String companyCode) {
        ConcurService concurService = getConcurService();
        registerCorpSsoQueryReceiver();
        corpSsoQueryRequest = concurService.sendCorpSsoQueryRequest(companyCode);
        if (corpSsoQueryRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendCorpSsoQueryRequest: unable to create corp sso query request.");
            unregisterCorpSsoQueryReceiver();
        } else {
            // Set the request object on the receiver.
            corpSsoQueryReceiver.setServiceRequest(corpSsoQueryRequest);
            // Show the progress dialog.
            showDialog(DIALOG_SSO_QUERY_PROGRESS);
        }
    }

    /**
     * Will register an itinerary summary list receiver.
     */
    private void registerCorpSsoQueryReceiver() {
        if (corpSsoQueryReceiver == null) {
            corpSsoQueryReceiver = new CorpSsoQueryReceiver(this);
            if (corpSsoQueryFilter == null) {
                corpSsoQueryFilter = new IntentFilter(Const.ACTION_CORP_SSO_QUERY);
            }
            getApplicationContext().registerReceiver(corpSsoQueryReceiver, corpSsoQueryFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerCorpSsoQueryReceiver: corpSsoQueryReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an itinerary summary list receiver.
     */
    private void unregisterCorpSsoQueryReceiver() {
        if (corpSsoQueryReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(corpSsoQueryReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCorpSsoQueryReceiver: illegal argument", ilaExc);
            }
            corpSsoQueryReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterCorpSsoQueryReceiver: corpSsoQueryReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of a corporate SSO query.
     */
    static class CorpSsoQueryReceiver extends BaseBroadcastReceiver<CompanyCodeLoginActivity, CorpSsoQueryRequest> {

        private static final String CLS_TAG = CompanyCodeLoginActivity.CLS_TAG + "." + CorpSsoQueryReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>CorpSsoQueryReceiver</code>.
         *
         * @param activity the activity.
         */
        CorpSsoQueryReceiver(CompanyCodeLoginActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver
         * #clearActivityServiceRequest(com.concur.mobile.corp .activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(CompanyCodeLoginActivity activity) {
            activity.corpSsoQueryRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver #dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_SSO_QUERY_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver #handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_SSO_QUERY_NOT_FOUND);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver #handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            ConcurCore concurMobile = activity.getConcurCore();
            CorpSsoQueryReply reply = concurMobile.getCorpSsoQueryReply();
            if (reply != null) {
                if (reply.ssoEnabled) {
                    if (reply.ssoUrl != null) {
                        // Launch the company sign-on activity.
                        Intent i = new Intent(activity, CompanySignOnActivity.class);
                        i.putExtra(ConcurMobile.FROM_NOTIFICATION, activity.fromNotification);
                        activity.startActivityForResult(i, Const.REQUEST_CODE_SSO_LOGIN);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: reply sso url is null!");
                    }
                } else {
                    activity.showDialog(DIALOG_SSO_QUERY_DISABLED);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: reply is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver
         * #setActivityServiceRequest(com.concur.mobile.corp .activity. BaseActivity,
         * com.concur.mobile.corp.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(CorpSsoQueryRequest request) {
            activity.corpSsoQueryRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.corp.activity.BaseActivity.BaseBroadcastReceiver #unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterCorpSsoQueryReceiver();
        }

    }

}
