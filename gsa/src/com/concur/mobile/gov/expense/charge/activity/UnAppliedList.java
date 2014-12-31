package com.concur.mobile.gov.expense.charge.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.GovAppMobile;
import com.concur.mobile.gov.expense.charge.data.MobileExpense;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListReply;
import com.concur.mobile.gov.expense.charge.service.MobileExpenseListRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovFlurry;

public class UnAppliedList extends BaseActivity {

    protected final static String CLS_TAG = UnAppliedList.class.getSimpleName();

    public final static String CCEXPID = "ccexpId";
    public final static String QEOBJ = "QuickExpenseDataHolder";
    public final static String ISFINISH = "isfinish.activity";
    protected static final int REFRESH_CODE = 1;

    protected Bundle bundle;
    protected String docName, docType;

    protected MobileExpenseListRequest request = null;
    protected ExpenseListReceiver receiver;
    protected IntentFilter expListFilter;

    protected List<MobileExpense> expList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gov_unapp_expense_list);
    }

    /** set screen title */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(getHeaderTitle());
    }

    protected String getHeaderTitle() {
        return getString(R.string.home_row_expenses).toString();
    }

    /**
     * set view configuration. filter the list and set build the view
     * 
     * @param isRefresh
     */
    // TODO database query...
    protected void setViewConfiguration() {
        GovAppMobile app = (GovAppMobile) getApplication();
        MobileExpenseListReply reply = app.vchCache.getMobileExpenseListReply();
        if (reply != null) {
            if (app.isExpListRefreshReq()) {
                sendExpenseListReq();
            } else if (isUpdateRequiredForList(reply)) {
                sendExpenseListReq();
            } else {
                expList = reply.mobExpList;
                buildView(expList);
            }
        } else {
            sendExpenseListReq();
        }
    }

    /**
     * Override this method inrespective activities.
     * 
     * @param expList
     *            : Mobile expense list
     * 
     * */
    protected void buildView(List<MobileExpense> expList) {
    }

    /**
     * Get expense List
     * */
    protected void sendExpenseListReq() {
        if (GovAppMobile.isConnected()) {
            GovService govService = (GovService) getConcurService();
            if (govService != null) {
                registerExpenseListReceiver();
                request = govService.sendMobileExpenseListReq();
                if (request == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendExpenseListReq: unable to create request to get Mobile expense list!");
                    unregisterExpenseListReceiver();
                } else {
                    // set service request.
                    receiver.setServiceRequest(request);
                    // Show the progress dialog.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_UNAPP_EXP_LIST);
                }
            } else {
                Log.wtf(Const.LOG_TAG, CLS_TAG + getString(R.string.service_not_available).toString());
            }
        } else {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        }
    }

    /**
     * register document receiver
     * */
    protected void registerExpenseListReceiver() {
        if (receiver == null) {
            receiver = new ExpenseListReceiver(this);
            if (expListFilter == null) {
                expListFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_UNAPP_EXPENSE);
            }
            getApplicationContext().registerReceiver(receiver, expListFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerExpenseListReceiver and exp list filter not null");
        }
    }

    /**
     * un-register document list receiver
     * */
    protected void unregisterExpenseListReceiver() {
        if (receiver != null) {
            getApplicationContext().unregisterReceiver(receiver);
            receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterExpenseListReceiver is null!");
        }
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling
     * the response for mobile expense list(unapplied).
     */
    protected class ExpenseListReceiver extends
        BaseBroadcastReceiver<UnAppliedList, MobileExpenseListRequest>
    {

        private final String CLS_TAG = UnAppliedList.CLS_TAG + "."
            + ExpenseListReceiver.class.getSimpleName();

        protected ExpenseListReceiver(UnAppliedList activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(UnAppliedList activity) {
            activity.request = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_GOV_UNAPP_EXP_LIST);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            showNoDataView();
            logFlurryEvents(false, null);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GovAppMobile app = (GovAppMobile) activity.getConcurCore();
            final MobileExpenseListReply reply = app.vchCache.getMobileExpenseListReply();
            if (reply != null) {
                app.setExpListRefreshReq(false);
                onHandleSuccessForActivity(reply);
            } else {
                logFlurryEvents(false, null);
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: successful reply but 'reply' is null!");
            }
        }

        @Override
        protected void setActivityServiceRequest(MobileExpenseListRequest request) {
            activity.request = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterExpenseListReceiver();
        }
    }

    /**
     * Override this method in respective activities.
     * */
    protected void showNoDataView() {
    }

    /**
     * Override this method in respective activities.
     * 
     * @param reply
     *            : reference of MobileExpenseListReply.
     * */
    protected void onHandleSuccessForActivity(MobileExpenseListReply reply) {
        logFlurryEvents(true, reply);
    }

    /**
     * Log flurry events that do you successfully get number of auths or not
     * 
     * @param reply
     * */
    private void logFlurryEvents(boolean isSuccessful, MobileExpenseListReply reply) {
        Map<String, String> params = new HashMap<String, String>();
        if (isSuccessful) {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
        } else {
            params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
        }
        int count = 0;
        if (reply != null && reply.mobExpList != null) {
            count = reply.mobExpList.size();
            params.put(GovFlurry.PARAM_NAME_UNAPP_EXPENSE_COUNT, Integer.toString(count));
        } else {
            params.put(GovFlurry.PARAM_NAME_UNAPP_EXPENSE_COUNT, Integer.toString(count));
        }
        EventTracker.INSTANCE.track(GovFlurry.CATEGORY_UNAPP_EXPLIST,
            Flurry.EVENT_NAME_LIST, params);
    }

    /**
     * Check whether we need to refresh our list or not.
     * 
     * @param reply
     *            :Reference of MobileExpenseListReply contains last refreshed time.
     * */
    protected boolean isUpdateRequiredForList(MobileExpenseListReply reply) {
        if (GovAppMobile.isConnected()) {
            Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            Calendar lastRefresh = reply.lastRefreshTime;
            int minuteDifference = FormatUtil.getMinutesDifference(lastRefresh, now);
            if (minuteDifference == -1 || minuteDifference > 2) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case com.concur.mobile.gov.util.Const.DIALOG_GOV_UNAPP_EXP_LIST: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_retrieve_unapplied_expenses));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
            break;
        }

        case com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_add_to_vch_progress));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            dialog = pDialog;
            break;
        }
        case com.concur.mobile.gov.util.Const.DIALOG_ADD_EXP_TO_VCH_FAIL: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.gov_add_to_vch_fail_title);
            dlgBldr.setMessage(R.string.gov_add_to_vch_fail_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog = dlgBldr.create();
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {
            if (receiver != null) {
                // Clear activity and we will reassigned.
                receiver.setActivity(null);
                retainer.put(com.concur.mobile.gov.util.Const.RETAINER_UNAPP_EXP_RECEIVER_KEY, receiver);
            }
        }
    }

    /**
     * restore any receiver store for this activity into Retainer.
     * */
    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore any segment cancel receiver.
            if (retainer.contains(com.concur.mobile.gov.util.Const.RETAINER_UNAPP_EXP_RECEIVER_KEY)) {
                receiver = (ExpenseListReceiver) retainer
                    .get(com.concur.mobile.gov.util.Const.RETAINER_UNAPP_EXP_RECEIVER_KEY);
                // Reset the activity reference.
                receiver.setActivity(this);
            }
        }
    }
}
