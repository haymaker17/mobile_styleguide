package com.concur.mobile.core.expense.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.fragment.Expenses;
import com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback;
import com.concur.mobile.core.expense.fragment.SortExpensesDialogFragment;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment.ReceiptStoreFragmentCallback;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.ExpenseDAOConverter;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ReceiptDAOConverter;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.ReceiptListRequestTask;
import com.concur.mobile.platform.expense.smartexpense.SmartExpenseListRequestTask;
import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.expenseit.ExpenseItImage;
import com.concur.mobile.platform.expenseit.ExpenseItPostReceiptResponse;
import com.concur.mobile.platform.expenseit.GetExpenseItExpenseListAsyncTask;
import com.concur.mobile.platform.expenseit.PostExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.util.PreferenceUtil;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class ExpensesAndReceipts extends BaseActivity implements ExpensesCallback, ReceiptStoreFragmentCallback, SortExpensesDialogFragment.SortExpenseDialogListener {

    public final static String CLS_TAG = ExpensesAndReceipts.class.getSimpleName();

    private static final String SMART_EXPENSE_LIST_RECEIVER = "smart.expense.list.request.receiver";

    private static final String GET_EXPENSE_IT_LIST_RECEIVER = "get.expense.it.list.request.receiver";

    private static final String UPLOAD_EXPENSE_IT_RECEIPT_RECEIVER = "upload.expense.it.receipt.request.receiver";

    // Contains the key used to store/retrieve the ReceiptList receiver.
    private static final String GET_RECEIPT_LIST_RECEIVER_KEY = "get.receipt.list.receiver";

    private static final int REQUEST_GET_EXPENSE_LIST = 1;

    private static final int REQUEST_GET_RECEIPT_LIST = 2;

    //Bundle key for storing / restoring expenseListRequestStartTime
    private static final String BUNDLE_EXPENSE_LIST_REQUEST_START_TIME = "expense.list.request" +
            ".start.time";

    // Holds the time in milliseconds when the last expenseListRequest was sent.
    private long expenseListRequestStartTime = 0L;

    /**
     * Temp variables for record process times related to merging expenses and receipts
     */
    private long receiptListRequestStartTime = 0L;
    private long GSELRequestStartTime = 0L;
    //************

    /**
     * The AsyncTask resulting from the the MWS call to ReceiptList.
     */
    protected AsyncTask<Void, Void, Integer> getReceiptListAsyncTask;

    EandRPagerAdapter pageAdapter;

    ViewPager viewPager;
    protected boolean fromNotification;
    // Determine what pages we have
    protected boolean allowExpenses = true;
    protected boolean allowReceipts = true;

    // Because the EandRPagerAdapter gets called many times, we need to show test drive tips only once.
    protected boolean shouldShowTestDriveReceiptTips = true;

    protected boolean shouldShowTestDriveExpensesTips = true;

    /**
     *
     */
    protected BaseAsyncResultReceiver smartExpenseListReceiver;

    protected ProgressDialog uploadExpenseItReceiptProgress;

    /**
     * Listener used to handle the response for getting the list of SmartExpenses.
     */
    protected AsyncReplyListener smartExpenseListReplyListener = new AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Long smartExpenseListRequestElapsedTime = 0L;

            Log.d(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - Successfully retrieved SmartExpenses!");

            smartExpenseListRequestElapsedTime = Calendar.getInstance().getTimeInMillis() -
                    expenseListRequestStartTime;

            Log.d(Const.LOG_TAG, "GSELReplyTime - " + smartExpenseListRequestElapsedTime);

            ConcurCore concurCore = (ConcurCore) ConcurCore.getContext();

            SessionInfo sessInfo = ConfigUtil.getSessionInfo(concurCore);
            if (sessInfo == null) {
                // Really bad if session info is null here!
                // TODO E-DAO: Show connection error dialog
                return;
            }

            // Get the new list of SmartExpenseDAO from the content provider and convert
            // to the old/exisiting Expense.
            ExpenseDAOConverter.migrateSmartExpenseDAOToExpenseEntryCache(sessInfo.getUserId());

            onGetSmartExpenseListSuccess();
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            Log.e(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - FAILED to retrieve SmartExpenses!");

            onGetSmartExpenseListFailed();
        }

        @Override
        public void onRequestCancel(Bundle resultData) {

            // Update the ExpenseList UI.
            updateExpenseListUI();
        }

        @Override
        public void cleanup() {
            smartExpenseListReceiver = null;
        }
    };

    /**
     *
     */
    protected AsyncTask<Void, Void, Integer> smartExpenseAsyncTask;

    /**
     * Receiver for the ReceiptList call.
     */
    protected BaseAsyncResultReceiver getReceiptListReceiver;

    /**
     * Async task for getting the list of ExpenseIt items.
     */
    protected AsyncTask<Void, Void, Integer> expenseItListAsyncTask;

    /**
     * Receiver for retrieving the list of ExpenseIt items.
     */
    protected BaseAsyncResultReceiver expenseItListReceiver;

    /**
     * Listener used to handle the response for getting the list of ExpenseIt items.
     */
    protected AsyncReplyListener expenseItListReplyListener = new AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Long expenseItListRequestElapsedTime = 0L;

            Log.d(Const.LOG_TAG, CLS_TAG + ".ExpenseItListReplyListener - Successfully retrieved ExpenseIt items!");

            //Record the time taken to retrieve ExpenseItListRequest
            expenseItListRequestElapsedTime = Calendar.getInstance().getTimeInMillis() -
            expenseListRequestStartTime;
            EventTracker.INSTANCE.trackTimings(Flurry.EVENT_RETRIEVE_EXPENSEIT_LIST, Flurry
                            .EVENT_RETRIEVE_EXPENSEIT_LIST, Flurry.EVENT_RETRIEVE_EXPENSEIT_LIST,
                    expenseItListRequestElapsedTime);

            Log.d(Const.LOG_TAG, "ExpenseItListReplyTime - " + expenseItListRequestElapsedTime);

            // Need to update the GSEL.
            doGetSmartExpenseList();
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            Log.e(Const.LOG_TAG, CLS_TAG + ".ExpenseItListReplyListener - FAILED to retrieve ExpenseIt items!");

            onGetSmartExpenseListFailed();
        }

        @Override
        public void onRequestCancel(Bundle resultData) {

            // Update the ExpenseList UI.
            updateExpenseListUI();
        }

        @Override
        public void cleanup() {
            expenseItListReceiver = null;
        }
    };

    /**
     * Listener for the reply of the ReceiptList MWS call.
     */
    protected AsyncReplyListener getReceiptListReplyListener = new BaseAsyncRequestTask.AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            Long receiptListRequestElapsedTime = 0L;

            Log.d(Const.LOG_TAG, CLS_TAG + ".GetReceiptListReplyListener - call to get receipt list succeeded!");

            //Record the time taken to retrieve ExpenseItListRequest
            receiptListRequestElapsedTime = Calendar.getInstance().getTimeInMillis() -
                    receiptListRequestStartTime;
            Log.d(Const.LOG_TAG, "receiptListReplyTime - " + receiptListRequestElapsedTime);

            ConcurCore app = (ConcurCore) ConcurCore.getContext();

            SessionInfo sessInfo = ConfigUtil.getSessionInfo(app);
            if (sessInfo == null) {
                // Really bad if session info is null here!
                // OCR: Show connection error dialog
                Log.e(Const.LOG_TAG,
                    CLS_TAG
                        + ".GetReceiptListReplyListener - SessionInfo is null! Cannot migrate ReceiptDAO to ReceiptInfo.");
            } else {

                // Get the new list of ReceiptDAO from the content provider and convert
                // to the old/existing ReceiptInfo. Calling this method will save the
                // new DAO to the old ReceiptStoreCache.
                ReceiptDAOConverter.migrateReceiptListDAOToReceiptStoreCache(sessInfo.getUserId());

                // Clear re-fetch report list.
                ReceiptStoreCache receiptStoreCache = app.getReceiptStoreCache();
                receiptStoreCache.clearShouldRefetchReceiptList();

                // Set the flag that the list of expenses
                // should be refreshed.
                IExpenseEntryCache expEntCache = getConcurCore().getExpenseEntryCache();
                expEntCache.setShouldFetchExpenseList();
            }

            // Call any listeners that we succeeded.
            onGetReceiptListSuccess();
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            Log.d(Const.LOG_TAG, CLS_TAG + ".GetReceiptListReplyListener - call to get receipt list failed!");

            // Call any listeners that we failed.
            onGetReceiptListFailed();
        }

        @Override
        public void onRequestCancel(Bundle resultData) {

            // Update the ExpenseList in case this ReceiptList update
            // was requested by updating the ExpenseList.
            updateExpenseListUI();
        }

        @Override
        public void cleanup() {
            getReceiptListReceiver = null;
        }

    };

    /**
     * Indicates if the tips overlay is currently showing.
     */
    protected boolean isTipsOverlayVisible = false;

    /**
     * The time, in nanoseconds, this activity has been started.
     */
    protected long startTime = 0L;

    /**
     * The time, in seconds, this activity has been active (showing).
     */
    protected long upTime = 0L;


    protected BaseAsyncResultReceiver uploadExpenseItReceiptReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_and_receipts);

        if (savedInstanceState != null) {
            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
            expenseListRequestStartTime = savedInstanceState.getLong
                    (BUNDLE_EXPENSE_LIST_REQUEST_START_TIME, 0L);
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        Bundle extras = getIntent().getExtras();
        final ConcurCore app = (ConcurCore) getApplication();
        if (extras != null) {
            if (extras.containsKey(Const.EXTRA_EXPENSE_REPORT_KEY)) {
                // This is for expense import in a report
                allowReceipts = false;
                allowExpenses = true;
            }

            if (extras.containsKey(Const.EXTRA_RECEIPT_ONLY_FRAGMENT)
                && extras.getBoolean(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, false)) {
                allowReceipts = true;
                allowExpenses = false;
            }
            if (extras.containsKey(ConcurCore.FROM_NOTIFICATION)) {
                fromNotification = extras.getBoolean(ConcurCore.FROM_NOTIFICATION, false);
            }
        }

        // If only one tab, hide the pager strip and set the title on the action
        // bar
        if (!(allowExpenses && allowReceipts)) {
            findViewById(R.id.pager_tab_strip).setVisibility(View.GONE);
            if (allowExpenses) {
                getSupportActionBar().setTitle(R.string.expenses_title);
            } else if (allowReceipts) {
                getSupportActionBar().setTitle(R.string.receipt_store_title);
            }
        } else {
            // Otherwise, use the default title
            getSupportActionBar().setTitle(R.string.expenses_title);
        }
        if (fromNotification) {
            // app.getStartUpAct(this);
            new AsyncTask<Void, Void, Boolean>() {

                @Override
                protected void onPreExecute() {

                }

                ;

                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean returnValue = app.isSessionAvailable();
                    return returnValue;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    onHandleSuccess(result, app);
                }

                ;

            }.execute();
        } else {
            setPageAdapter(allowExpenses, allowReceipts);
        }
    }

    private void onHandleSuccess(Boolean result, ConcurCore app) {
        if (result != null && result == Boolean.FALSE) {
            app.launchStartUpActivity(ExpensesAndReceipts.this);
        } else {
            setPageAdapter(allowExpenses, allowReceipts);
        }

    }

    private void setPageAdapter(boolean allowExpenses, boolean allowReceipts) {
        // Create the adapter that will return a fragment for each pages.
        pageAdapter = new EandRPagerAdapter(getSupportFragmentManager(), allowExpenses, allowReceipts);

        // Set up the ViewPager with the sections adapter.
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(pageAdapter);
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dlg = null;

        // Proxy to the receipts fragment for now. Eventually, do away with this
        // altogether and use fragment dialogs.
        BaseFragment frag = pageAdapter.getPage(viewPager.getCurrentItem());
        if (frag instanceof ReceiptStoreFragment) {
            ReceiptStoreFragment f = (ReceiptStoreFragment) frag;
            dlg = f.onCreateDialog(id);
        }

        if (dlg == null) {
            dlg = super.onCreateDialog(id);
        }

        return dlg;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);

        // Proxy to the receipts fragment for now. Eventually, do away with this
        // altogether and use fragment dialogs.
        BaseFragment frag = pageAdapter.getPage(viewPager.getCurrentItem());
        if (frag instanceof ReceiptStoreFragment) {
            ReceiptStoreFragment f = (ReceiptStoreFragment) frag;
            f.onPrepareDialog(id, dialog);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isTipsOverlayVisible) {
            // Save the time the user spent on this screen, but
            // perhaps put the app in the background.
            upTime += (System.nanoTime() - startTime) / 1000000000L; // Convert to seconds.
        }

        // Save the ExpenseList receiver.
        if (smartExpenseListReceiver != null) {
            smartExpenseListReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(SMART_EXPENSE_LIST_RECEIVER, smartExpenseListReceiver);
            }
        }

        // Save the ReceiptList receiver.
        if (getReceiptListReceiver != null) {
            getReceiptListReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(GET_RECEIPT_LIST_RECEIVER_KEY, getReceiptListReceiver);
            }
        }

        // Save the ExpenseIt receiver.
        if (expenseItListReceiver != null) {
            expenseItListReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(GET_EXPENSE_IT_LIST_RECEIVER, expenseItListReceiver);
            }
        }

        // Upload ExpenseIt Receipt.
        if(uploadExpenseItReceiptReceiver != null) {

            uploadExpenseItReceiptReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(UPLOAD_EXPENSE_IT_RECEIPT_RECEIVER, uploadExpenseItReceiptReceiver);
            }

            if(uploadExpenseItReceiptProgress != null) {
                uploadExpenseItReceiptProgress.dismiss();
                uploadExpenseItReceiptProgress = null;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (isTipsOverlayVisible) {
            startTime = System.nanoTime();
        }

        if (retainer != null) {

            // Restore ExpenseList receiver.
            if (retainer.contains(SMART_EXPENSE_LIST_RECEIVER)) {
                smartExpenseListReceiver = (BaseAsyncResultReceiver) retainer.get(SMART_EXPENSE_LIST_RECEIVER);
                if (smartExpenseListReceiver != null) {
                    smartExpenseListReceiver.setListener(smartExpenseListReplyListener);
                }
            }

            // Restore the ReceiptList receiver
            if (retainer.contains(GET_RECEIPT_LIST_RECEIVER_KEY)) {
                getReceiptListReceiver = (BaseAsyncResultReceiver) retainer.get(GET_RECEIPT_LIST_RECEIVER_KEY);
                if (getReceiptListReceiver != null) {
                    getReceiptListReceiver.setListener(getReceiptListReplyListener);

                    showReceiptsListLoadingView();
                }
            }

            // Restore ExpenseList receiver.
            if (retainer.contains(GET_EXPENSE_IT_LIST_RECEIVER)) {
                expenseItListReceiver = (BaseAsyncResultReceiver) retainer.get(GET_EXPENSE_IT_LIST_RECEIVER);
                if (expenseItListReceiver != null) {
                    expenseItListReceiver.setListener(expenseItListReplyListener);
                }
            }

            // Restore ExpenseIt Receipt receiver.
            if(retainer.contains(UPLOAD_EXPENSE_IT_RECEIPT_RECEIVER)) {
                uploadExpenseItReceiptReceiver = (BaseAsyncResultReceiver) retainer.get(UPLOAD_EXPENSE_IT_RECEIPT_RECEIVER);
                if (uploadExpenseItReceiptReceiver != null) {

                    String message = getText(R.string.dlg_saving_receipt).toString();
                    uploadExpenseItReceiptProgress = ProgressDialog.show(this, null, message, true, false);

                    uploadExpenseItReceiptReceiver.setListener(new UploadImageAsyncReplyListener(uploadExpenseItReceiptProgress));
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (isTipsOverlayVisible) {
            // Save the uptime so we know how long the user has been on this screen,
            // even if it has been destroyed.
            outState.putLong(Const.ACTIVITY_STATE_UPTIME, upTime);
        }

        outState.putLong(BUNDLE_EXPENSE_LIST_REQUEST_START_TIME, expenseListRequestStartTime);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback#doGetSmartExpenseList()
     */
    @Override
    public void doGetSmartExpenseList() {

        // Show the loading view.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {

            Fragment frag = pageAdapter.getPage(i);
            if (frag != null && frag instanceof Expenses) {
                ((Expenses) frag).showLoadingView();
                break;
            }
        }

        if (smartExpenseListReceiver == null) {
            smartExpenseListReceiver = new BaseAsyncResultReceiver(new Handler());
            smartExpenseListReceiver.setListener(smartExpenseListReplyListener);
        }

        if (smartExpenseAsyncTask != null && smartExpenseAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            smartExpenseListReceiver.setListener(smartExpenseListReplyListener);
        } else {

            SmartExpenseListRequestTask smartExpReqTask = new SmartExpenseListRequestTask(getConcurCore()
                .getApplicationContext(), 0, smartExpenseListReceiver, true);
            smartExpenseAsyncTask = smartExpReqTask.execute();
        }

        //Record the start time of the GSEL request to use for anaylytics
        GSELRequestStartTime = Calendar.getInstance().getTimeInMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback#onGetSmartExpenseListSuccess()
     */
    @Override
    public void onGetSmartExpenseListSuccess() {
        // Load the UI from the expense cache.
        updateExpenseListUI();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback#onGetSmartExpenseListFailed()
     */
    @Override
    public void onGetSmartExpenseListFailed() {
        // Load the UI from the expense cache.
        updateExpenseListUI();

        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expenses_retrieve_failed_title).show(
                getSupportFragmentManager(), null);
    }

    private void updateExpenseListUI() {

        // Update Expense List if necessary.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {

            Fragment frag = pageAdapter.getPage(i);
            if (frag != null && frag instanceof Expenses) {
                ((Expenses) frag).updateExpenseListUI();
                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment.ReceiptStoreFragmentCallback#onStartOcrSuccess()
     */
    @Override
    public void onStartOcrSuccess() {
        // Update the Expense List if an OCR was started
        // so that the list will show the new Processing item.
        if (Preferences.isExpenseItUser()) {

            int count = pageAdapter.getCount();
            for (int i = 0; i < count; i++) {
                Fragment frag = pageAdapter.getPage(i);
                if (frag != null && frag instanceof Expenses) {
                    if (isServiceAvailable()) {
                        ((Expenses) frag).checkForRefreshData(false);
                        return;
                    }
                }
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment.ReceiptStoreFragmentCallback#onStartOcrFailed()
     */
    @Override
    public void onStartOcrFailed() {
        if (Preferences.isExpenseItUser()) {
            // OCR: What to do in this case?
        }
    }

    @Override
    public void doGetExpenseItList() {

        // Show the loading view.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {

            Fragment frag = pageAdapter.getPage(i);
            if (frag != null && frag instanceof Expenses) {
                ((Expenses) frag).showLoadingView();
                break;
            }
        }

        if (expenseItListReceiver == null) {
            expenseItListReceiver = new BaseAsyncResultReceiver(new Handler());
            expenseItListReceiver.setListener(expenseItListReplyListener);
        }

        if (expenseItListAsyncTask != null && expenseItListAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            expenseItListReceiver.setListener(expenseItListReplyListener);
        } else {

            GetExpenseItExpenseListAsyncTask expenseItListReqTask = new GetExpenseItExpenseListAsyncTask(
                    getApplicationContext(), 22, getUserId(), expenseItListReceiver);
            expenseItListAsyncTask = expenseItListReqTask.execute();

            //Record the start time of the expenseItList request to use for anaylytics
            expenseListRequestStartTime = Calendar.getInstance().getTimeInMillis();
        }

    }

    /*
    * (non-Javadoc)
    *
    * @see com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback#onGetSmartExpenseListSuccess()
    */
    @Override
    public void onGetExpenseItListSuccess() {
        // TODO: ANALYTICS - Track ExpenseIt list retrieval success.
        // Load the UI from the expense cache.
        updateExpenseListUI();
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.core.expense.fragment.Expenses.ExpensesCallback#onGetSmartExpenseListFailed()
     */
    @Override
    public void onGetExpenseItListFailed() {
        // TODO: ANALYTICS - Track ExpenseIt list retrieval failure.
        // Load the UI from the expense cache.
        updateExpenseListUI();

        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expenseit_retrieve_failed_title).show(
                getSupportFragmentManager(), null);
    }

    /**
     * Invokes the MWS to retrieve the list of Receipts with OCR status.
     */
    @Override
    public void doGetReceiptList() {

        showReceiptsListLoadingView();

        if (getReceiptListReceiver == null) {
            getReceiptListReceiver = new BaseAsyncResultReceiver(new Handler());
            getReceiptListReceiver.setListener(getReceiptListReplyListener);
        }

        if (getReceiptListAsyncTask != null && getReceiptListAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            getReceiptListReceiver.setListener(getReceiptListReplyListener);
        } else {
            ReceiptListRequestTask receiptListTask = new ReceiptListRequestTask(ConcurCore.getContext(),
                REQUEST_GET_RECEIPT_LIST, getReceiptListReceiver);
            getReceiptListAsyncTask = receiptListTask.execute();
        }

        //Record the start time of the ReceiptList request to use for anaylytics
        receiptListRequestStartTime = Calendar.getInstance().getTimeInMillis();
    }

    private void showReceiptsListLoadingView() {
        // Show the Laoding view on the ReceiptStore list.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Fragment frag = pageAdapter.getPage(i);
            if (frag != null && frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                rsFrag.showLoadingView();

                break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment.ReceiptStoreFragmentCallback#onGetReceiptListSuccess
     * ()
     */
    @Override
    public void onGetReceiptListSuccess() {

        // Update ReceiptStore List and Expense List if necessary.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {

            Fragment frag = pageAdapter.getPage(i);
            if (frag != null) {

                if (frag instanceof ReceiptStoreFragment) {
                    ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                    rsFrag.endUserRefresh = false;
                    rsFrag.initView();
                } else if (frag instanceof Expenses && Preferences.isExpenseItUser()) {
                    if (isServiceAvailable()) {
                        ((Expenses) frag).checkForRefreshData(false);
                    } else {
                        // Need to update the ExpenseList UI so that
                        // the loading view won't continuously show.
                        updateExpenseListUI();
                    }
                }
            }
        }
    }

    @Override
    public void uploadReceiptToExpenseIt(final String filePath) {

        // TODO: EXPIT - should we put an ExpenseIt item place holder in this list?
        String message = getText(R.string.dlg_saving_receipt).toString();
        uploadExpenseItReceiptProgress = ProgressDialog.show(this, null, message, true, false);

        //Setup the image receiver
        if (uploadExpenseItReceiptReceiver == null) {
            uploadExpenseItReceiptReceiver = new BaseAsyncResultReceiver(new Handler());
            uploadExpenseItReceiptReceiver.setListener(new UploadImageAsyncReplyListener(uploadExpenseItReceiptProgress));
        }

        // Run in teh background so we don't block the UI.
        new Thread(new Runnable() {
            @Override
            public void run() {

                //Setup the image information
                String contentType = null;
                if (filePath != null) {
                    File receiptFile = new File(filePath);
                    try {
                        if (receiptFile.exists()) {
                            switch (ViewUtil.getDocumentType(receiptFile)) {
                                case PNG:
                                    contentType = "image/png";
                                    break;
                                case JPG:
                                    contentType = "image/jpeg";
                                    break;
                                case PDF:
                                    contentType = "application/pdf";
                                    break;
                                case UNKNOWN:
                                    Log.d(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToExpenseIt: non jpg/png receipt image type.");
                                    throw new IllegalArgumentException("Receipt image file of non jpg/png type!");
                            }

                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToExpenseIt: receipt image file '" + filePath
                                    + "' does not exist!");
                            throw new IllegalArgumentException("Receipt image file '" + filePath + "' does not exist!");
                        }
                    } catch (SecurityException secExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToExpenseIt: can't access receipt file '" + filePath + ".", secExc);
                        throw new IllegalArgumentException("Receipt image file '" + filePath + "' is not accessible.");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToExpenseIt: receipt image file is null!");
                }

                byte[] imageData = null;
                try {
                    imageData = FileUtils.readFileToByteArray(new File(filePath));
                } catch (Exception e) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToExpenseIt() - problem reading in image file: " + filePath, e);
                    uploadExpenseItReceiptReceiver = null;

                    // TODO: EXPIT - throw up error dialog?
                    Toast.makeText(getBaseContext(),
                            "Failed to upload ExpenseIt Receipt image.", Toast.LENGTH_LONG).show();

                    return;
                }

                ExpenseItImage image = new ExpenseItImage();
                image.setData(imageData, contentType);

                //Make the call
                PostExpenseItReceiptAsyncTask postExpenseItReceiptAsyncTask = new PostExpenseItReceiptAsyncTask(ConcurCore.getContext(),
                        0, uploadExpenseItReceiptReceiver, image);

                postExpenseItReceiptAsyncTask.execute();
                // TODO: ANALYTICS - Log time it takes to upload.
            }
        }).start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment.ReceiptStoreFragmentCallback#onGetReceiptListFailed
     * ()
     */
    @Override
    public void onGetReceiptListFailed() {

        // Update ReceiptStoreList if GetReceiptList failed.
        int count = pageAdapter.getCount();
        for (int i = 0; i < count; i++) {
            Fragment frag = pageAdapter.getPage(i);
            if (frag != null && frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                rsFrag.showRetrieveReceiptUrlsFailedDialog(rsFrag.actionStatusErrorMessage);
                rsFrag.endUserRefresh = false;
                rsFrag.initView();

                break;
            }
        }

        // Update the ExpenseList in case this ReceiptList update
        // was requested by updating the ExpenseList.
        updateExpenseListUI();
    }


    @Override
    public void onSortCriteriaSelected(String sortOrder) {

        // Save the selected sort order.
        PreferenceUtil.savePreference(this, Expenses.PREF_EXPENSE_LIST_SORT_ORDER, sortOrder);

        updateExpenseListUI();
    }


    /**
     *
     */
    public class EandRPagerAdapter extends FragmentPagerAdapter {

        private final ArrayList<Class<? extends BaseFragment>> pageClasses = new ArrayList<Class<? extends BaseFragment>>();
        private final Map<Integer, BaseFragment> pageFragments = new HashMap<Integer, BaseFragment>();

        private boolean isInstantiating;

        public EandRPagerAdapter(FragmentManager fm, boolean allowExpenses, boolean allowReceipts) {
            super(fm);

            if (allowExpenses) {
                pageClasses.add(Expenses.class);
            }

            if (allowReceipts) {
                pageClasses.add(ReceiptStoreFragment.class);
            }
        }

        public BaseFragment getPage(int pos) {
            return pageFragments.get(pos);
        }

        @Override
        public Fragment getItem(int pos) {
            if (!isInstantiating) {
                throw new RuntimeException("Only instantiateItem() can call getItem()");
            }

            return Fragment.instantiate(ExpensesAndReceipts.this, pageClasses.get(pos).getCanonicalName());
        }

        @Override
        public Object instantiateItem(ViewGroup container, int pos) {
            isInstantiating = true;

            BaseFragment f = (BaseFragment) super.instantiateItem(container, pos);

            if (!pageFragments.containsKey(pos)) {
                pageFragments.put(pos, f);
            }

            isInstantiating = false;
            return f;
        }

        @Override
        public int getCount() {
            return pageClasses.size();
        }

        @Override
        public CharSequence getPageTitle(int pos) {
            Class<? extends BaseFragment> pageClass = pageClasses.get(pos);

            if (pageClass.equals(Expenses.class)) {
                return getString(R.string.expenses_title);
            } else if (pageClass.equals(ReceiptStoreFragment.class)) {
                return getString(R.string.receipt_store_title);
            }

            return null;
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);

            boolean isTestDriveUser = Preferences.isTestDriveUser();
            if (isTestDriveUser) {
                boolean isExpenses = true;
                Class<? extends BaseFragment> pageClass = pageClasses.get(position);

                if (pageClass.equals(Expenses.class)) {
                    isExpenses = true;
                } else if (pageClass.equals(ReceiptStoreFragment.class)) {
                    isExpenses = false;
                }

                // If we're a Test Drive User, check if we're expenses or receipts and show tips accordingly.
                if (isExpenses && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSES)
                    && shouldShowTestDriveExpensesTips) {
                    showTestDriveTips(isExpenses);
                    shouldShowTestDriveExpensesTips = false;
                } else if (!isExpenses && Preferences.shouldShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_RECEIPT_STORE)
                    && shouldShowTestDriveReceiptTips) {
                    showTestDriveTips(isExpenses);
                    shouldShowTestDriveReceiptTips = false;
                }
            }
        }

        /**
         * Because Expenses and Receipt Store share the Pager as an access point, this method figures out what we're dealing with,
         * where it was accessed from (IE Receipt Store from a Quick Expense, or in the EandRPager, etc.) and calls the UIUtils
         * overlay method and sets up the overlay accordingly.
         *
         * @param isExpenses Whether we're looking at Expenses or not (if not, we're looking at ReceiptStore).
         */
        protected void showTestDriveTips(final boolean isExpenses) {

            OnClickListener dismissListener = new OnClickListener() {

                public void onClick(View v) {
                    if (isExpenses) {
                        Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_EXPENSES);
                    } else {
                        Preferences.setShouldNotShowTestDriveTips(Const.PREF_TD_SHOW_OVERLAY_RECEIPT_STORE);
                    }

                    isTipsOverlayVisible = false;

                    // Analytics stuff.
                    Map<String, String> flurryParams = new HashMap<String, String>();
                    upTime = ((System.nanoTime() - startTime) / 1000000000L) + upTime; // Convert nanoseconds to seconds.
                    flurryParams.put(Flurry.PARAM_NAME_SECONDS_ON_OVERLAY, Flurry.formatDurationEventParam(upTime));
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_OVERLAYS, (isExpenses ? "Expense Screen"
                        : "Receipts Screen"), flurryParams);

                }
            };

            int overlayResId = isExpenses ? R.layout.td_overlay_expenses : R.layout.td_overlay_receipt_store;
            View overlay = UIUtils.setupOverlay((ViewGroup) getWindow().getDecorView(), overlayResId, dismissListener,
                R.id.td_icon_cancel_button, ExpensesAndReceipts.this, R.anim.fade_out, 300L);

            // Possible cases for changing the overlay are checked here and the overlay hides arrows accordingly.
            if (overlay != null) {
                if (isExpenses && !allowReceipts) {
                    overlay.findViewById(R.id.td_toggle_expenses).setVisibility(View.INVISIBLE);
                } else if (!isExpenses && !allowExpenses) {
                    overlay.findViewById(R.id.td_toggle_receipts).setVisibility(View.INVISIBLE);
                    Intent intent = ExpensesAndReceipts.this.getIntent();
                    // If Receipt store was navigated to from a Quick Expense or Report, there's no "add receipt" option.
                    if (intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_REPORT_RECEIPT_KEY, false)
                        || intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_ENTRY_RECEIPT_KEY, false)
                        || intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY, false)) {
                        overlay.findViewById(R.id.td_rs_capture_new_receipt).setVisibility(View.INVISIBLE);
                    }
                }

            }

            startTime = System.nanoTime();
            isTipsOverlayVisible = true;
        }
    }

    private class UploadImageAsyncReplyListener implements AsyncReplyListener {

        protected String CLS_TAG = UploadImageAsyncReplyListener.class.getSimpleName();

        private ProgressDialog progressDialog;

        UploadImageAsyncReplyListener(ProgressDialog progressDialog) {
            this.progressDialog = progressDialog;
        }

        @Override
        public void onRequestSuccess(Bundle resultData) {

            if(progressDialog != null) {
                progressDialog.dismiss();
            }

            if (resultData != null && resultData.containsKey(PostExpenseItReceiptAsyncTask.POST_EXPENSEIT_OCR_RESULT_KEY)) {

                ExpenseItPostReceiptResponse receiptResponse = (ExpenseItPostReceiptResponse) resultData.get(PostExpenseItReceiptAsyncTask.POST_EXPENSEIT_OCR_RESULT_KEY);
                if (receiptResponse != null) {
                    ErrorResponse error = receiptResponse.getExpenses()[0].getExpenseError();
                    if (error != null) {
                        Toast.makeText(getBaseContext(), String.format(
                            "Failed processing image with the following error %s (%s)",
                            error.getErrorMessage(), error.getErrorCode()), Toast.LENGTH_LONG).show();
                            // TODO: ANALYTICS - Track ExpenseIt image upload failure.
                        return;
                    } else {
//                        long id = receiptResponse.getExpenses()[0].getId();
//                        int totalSecs = receiptResponse.getExpenses()[0].getEta();
//
//                        int hours = totalSecs / 3600;
//                        int minutes = (totalSecs % 3600) / 60;
//                        int seconds = totalSecs % 60;
//
//                        Toast.makeText(getBaseContext(), String.format(
//                            "Successfully uploaded image with id=%d and estimate finish= %02d:%02d:%02d",
//                            id, hours, minutes, seconds), Toast.LENGTH_LONG).show();

                        doGetExpenseItList();
                        // TODO: ANALYTICS - Track ExpenseIt image upload success.
                        return;
                    }
                }
            }
            Toast.makeText(getBaseContext(),"An error occurred while uploading Image", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".OnRequestFail is called");

            if(progressDialog != null) {
                progressDialog.dismiss();
            }

            Toast.makeText(getBaseContext(),"An error occurred uploading Image", Toast.LENGTH_LONG).show();
            // TODO: ANALYTICS - Track ExpenseIt image upload failure.
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".OnRequestCancel is called");
            // TODO: ANALYTICS - Track ExpenseIt image upload cancel.

            if(progressDialog != null) {
                progressDialog.dismiss();
            }
        }

        @Override
        public void cleanup() {
            uploadExpenseItReceiptReceiver = null;
        }
    }
}
