package com.concur.mobile.gov.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.service.GovMessagesReply;
import com.concur.mobile.gov.service.GovRulesAgreementRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovScrollViewExtension;
import com.concur.mobile.gov.util.IScrollViewListener;

/**
 * This class will show Gov rules and regulations. This activity invokes once in 6 month and/or user is not agree with terms and
 * conditions.
 * 
 * @author sunill
 * 
 */
public class GovRulesActivity extends BaseActivity implements IScrollViewListener {

    protected final static String CLS_TAG = GovRulesActivity.class.getSimpleName();

    private final static String ON_ROTATION_SCROLL_POSITION = "scroller position";
    private final static String ON_ROTATION_ISAGREE_RESULT = "Agreement Selection";

    private Button agree, noAgree;

    private boolean isAgree;

    private GovScrollViewExtension scroller;

    private GovRulesAgreementRequest request;
    private SafeHarborAGreementReceiver receiver;
    private IntentFilter agreementFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gov_msg_rules_detail);
        initScreenHeader();
        buildView();
    }

    /** set screen title */
    private void initScreenHeader() {
        final View header = findViewById(R.id.header);
        if (header != null) {
            TextView txtView = (TextView) (header.findViewById(R.id.header_navigation_bar_title));
            if (txtView != null) {
                txtView.setText(getString(R.string.gov_login_rules_title).toString());
            }
        }
    }

    private void buildView() {
        GovAppMobile app = ((GovAppMobile) getApplication());
        GovMessagesReply reply = app.getMsgs();
        if (reply != null) {
            scroller = (GovScrollViewExtension) findViewById(R.id.gov_rules_scrollview);
            scroller.setOnScrollViewListener(GovRulesActivity.this);
            TextView txt = (TextView) findViewById(R.id.gov_rules_title);
            txt.setText(reply.behaviorTitle);
            txt = (TextView) findViewById(R.id.gov_rules_msg);
            txt.setText(reply.behaviorText);
            setFooterBar();
        } else {
            Log.e(CLS_TAG, ".BuildView : GovMessagesReply is null from application context");
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    private void setFooterBar() {
        final View footer = findViewById(R.id.footer);
        if (footer != null) {
            agree = (Button) (footer.findViewById(R.id.footer_button_left));
            if (agree != null) {
                agree.setEnabled(false);
                agree.setText(getString(R.string.gov_agree).toString());
                agree.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO call service
                        isAgree = true;
                        sendAgreement(isAgree);
                        // setResult(RESULT_OK);
                        // finish();
                    }
                });
            }

            noAgree = (Button) (footer.findViewById(R.id.footer_button_right));
            if (noAgree != null) {
                noAgree.setEnabled(false);
                noAgree.setText(getString(R.string.gov_not_agree).toString());
                noAgree.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO cal service.
                        // ((GovAppMobile) getApplication()).setShowPrivacyActNotice(true);
                        isAgree = false;
                        sendAgreement(isAgree);
                        // setResult(RESULT_CANCELED);
                        // finish();
                    }
                });
            }
        }

    }

    @Override
    public void onBackPressed() {
        // Do not do anything
    }

    @Override
    public void onScrollChanged(int xScroll, int yScroll, int oldXScroll, int oldYScroll) {
        if (agree != null) {
            agree.setEnabled(true);
        }
        if (noAgree != null) {
            noAgree.setEnabled(true);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        outState.putBoolean(ON_ROTATION_ISAGREE_RESULT, isAgree);

        if (scroller != null) {
            outState.putIntArray(ON_ROTATION_SCROLL_POSITION,
                new int[] {scroller.getScrollX(), scroller.getScrollY()});
        }

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(ON_ROTATION_ISAGREE_RESULT)) {
            isAgree = savedInstanceState.getBoolean(ON_ROTATION_ISAGREE_RESULT);
        }

        if (savedInstanceState.containsKey(ON_ROTATION_SCROLL_POSITION)) {
            if (scroller != null) {
                // here get the scroll position we have saved and then scroll up to the position.
                // you can do it on main UI thread but according to google doc it is safe to use
                // Separate thread for this operation.
                final int[] position = savedInstanceState.getIntArray(ON_ROTATION_SCROLL_POSITION);
                if (position != null)
                    scroller.post(new Runnable() {

                        public void run() {
                            scroller.scrollTo(position[0], position[1]);
                        }
                    });
            }
        }
    }

    /**
     * Send agreement to server..
     * 
     * @param isAgree
     *            : is agree to gov rules or not
     * */
    private void sendAgreement(boolean isAgree) {
        if (GovAppMobile.isConnected()) {
            GovAppMobile concurMobile = (GovAppMobile) getApplication();
            GovService govService = concurMobile.getService();
            if (govService != null) {
                registerAgreementReceiver();
                request = govService.sendSafeHarborAgreement(isAgree);
                if (request == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendAgreement: unable to create request to send safe harbor agreement!");
                    unregisterAgreementReceiver();
                } else {
                    // set service request.
                    receiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register Safe Harbor Agreement receiver
     * */
    protected void registerAgreementReceiver() {
        if (receiver == null) {
            receiver = new SafeHarborAGreementReceiver(this);
            if (agreementFilter == null) {
                agreementFilter = new IntentFilter(
                    com.concur.mobile.gov.util.Const.ACTION_SEND_AGREEMENT);
            }
            getApplicationContext().registerReceiver(receiver, agreementFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerAgreementReceiver is not null");
        }
    }

    /**
     * unregister Safe Harbor Agreement receiver
     * */
    protected void unregisterAgreementReceiver() {
        if (receiver != null) {
            getApplicationContext().unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAgreementReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for agreement.
     */
    class SafeHarborAGreementReceiver extends
        BaseBroadcastReceiver<GovRulesActivity, GovRulesAgreementRequest>
    {

        private final String CLS_TAG = GovRulesActivity.CLS_TAG + "."
            + SafeHarborAGreementReceiver.class.getSimpleName();

        protected SafeHarborAGreementReceiver(GovRulesActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(GovRulesActivity activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
            activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT_FAIL);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess");
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT_SUCCESS);
        }

        @Override
        protected void setActivityServiceRequest(GovRulesAgreementRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAgreementReceiver();
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getString(R.string.gov_sending_agreement).toString());
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                public void onCancel(DialogInterface dialog) {
                    if (request != null) {
                        request.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog.onCancel: null entry form request!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT_SUCCESS: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getString(R.string.gov_sending_agreement_succeeded_failed_title).toString());
            dlgBldr.setMessage(getString(R.string.gov_sending_agreement_succeeded_msg).toString());
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (isAgree) {
                        ((GovAppMobile) getApplication()).setShowPrivacyActNotice(false);
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        ((GovAppMobile) getApplication()).setShowPrivacyActNotice(true);
                        setResult(RESULT_CANCELED);
                        finish();
                    }
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getString(R.string.gov_sending_agreement_succeeded_failed_title).toString());
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(R.string.okay, new Dialog.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ((GovAppMobile) getApplication()).setShowPrivacyActNotice(true);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        // TODO @Walt : this is required here...
        case Const.DIALOG_SYSTEM_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_system_unavailable_title);
            dlgBldr.setMessage(getText(R.string.dlg_system_unavailable_message));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    ((GovAppMobile) getApplication()).setShowPrivacyActNotice(true);
                    setResult(RESULT_CANCELED);
                    finish();
                }
            });
            dialog = dlgBldr.create();
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_SEND_AGREEMENT_FAIL: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            if (actionStatusErrorMessage != null) {
                alertDlg.setMessage(actionStatusErrorMessage);
            }
            break;
        }
        default: {
            super.onPrepareDialog(id, dialog);
            break;
        }
        }
    }
}
