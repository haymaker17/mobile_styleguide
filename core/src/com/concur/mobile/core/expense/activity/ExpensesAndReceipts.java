package com.concur.mobile.core.expense.activity;

import android.app.Dialog;
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

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.ui.UIUtils;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
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
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.receipt.list.ReceiptListRequestTask;
import com.concur.mobile.platform.expense.smartexpense.SmartExpenseListRequestTask;
import com.concur.mobile.platform.ui.common.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ExpensesAndReceipts extends BaseActivity implements ExpensesCallback, ReceiptStoreFragmentCallback, SortExpensesDialogFragment.SortExpenseDialogListener {

    public final static String CLS_TAG = ExpensesAndReceipts.class.getSimpleName();

    private static final String SMART_EXPENSE_LIST_RECEIVER = "smart.expense.list.request.receiver";

    // Contains the key used to store/retrieve the ReceiptList receiver.
    private static final String GET_RECEIPT_LIST_RECEIVER_KEY = "get.receipt.list.receiver";

    private static final int REQUEST_GET_EXPENSE_LIST = 1;

    private static final int REQUEST_GET_RECEIPT_LIST = 2;

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

    /**
     * Listener used to handle the response for getting the list of SmartExpenses.
     */
    protected AsyncReplyListener smartExpenseListReplyListener = new AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {

            Log.d(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - Successfully retrieved SmartExpenses!");

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
     * Listener for the reply of the ReceiptList MWS call.
     */
    protected AsyncReplyListener getReceiptListReplyListener = new BaseAsyncRequestTask.AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {

            Log.d(Const.LOG_TAG, CLS_TAG + ".GetReceiptListReplyListener - call to get receipt list succeeded!");

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expenses_and_receipts);

        if (savedInstanceState != null) {
            upTime = savedInstanceState.getLong(Const.ACTIVITY_STATE_UPTIME, 0L);
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

                };

                @Override
                protected Boolean doInBackground(Void... params) {
                    boolean returnValue = app.isSessionAvailable();
                    return returnValue;
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    onHandleSuccess(result, app);
                };

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
         * @param isExpenses
         *            Whether we're looking at Expenses or not (if not, we're looking at ReceiptStore).
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

}
