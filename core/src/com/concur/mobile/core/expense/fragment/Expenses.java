/**
 *
 */
package com.concur.mobile.core.expense.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.dialog.DialogFragmentHandler;
import com.concur.mobile.core.dialog.ProgressDialogFragment;
import com.concur.mobile.core.dialog.SystemUnavailableDialogFragment;
import com.concur.mobile.core.expense.charge.activity.CashExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.CorporateCardExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.EReceiptListItem;
import com.concur.mobile.core.expense.charge.activity.ExpenseItListItem;
import com.concur.mobile.core.expense.charge.activity.ExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.OcrListItem;
import com.concur.mobile.core.expense.charge.activity.PersonalCardExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.QuickExpense;
import com.concur.mobile.core.expense.charge.activity.ReceiptCaptureListItem;
import com.concur.mobile.core.expense.charge.activity.SmartCorporateExpenseListItem;
import com.concur.mobile.core.expense.charge.activity.SmartPersonalExpenseListItem;
import com.concur.mobile.core.expense.charge.data.AttendeesEntryMap;
import com.concur.mobile.core.expense.charge.data.CorporateCardTransaction;
import com.concur.mobile.core.expense.charge.data.EReceipt;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.Expense.ExpenseEntryType;
import com.concur.mobile.core.expense.charge.data.ExpenseComparator;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.charge.data.OCRItem;
import com.concur.mobile.core.expense.charge.data.PersonalCardTransaction;
import com.concur.mobile.core.expense.charge.data.ReceiptCapture;
import com.concur.mobile.core.expense.charge.service.DeleteMobileEntriesRequest;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.report.activity.ActiveReportsListAdapter;
import com.concur.mobile.core.expense.report.activity.ExpenseEntries;
import com.concur.mobile.core.expense.report.activity.ExpenseReportHeader;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportAttendee;
import com.concur.mobile.core.expense.report.fragment.ActiveReportsListDialogFragment;
import com.concur.mobile.core.expense.report.service.AddToReportRequest;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.FeedbackManager;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.expenseit.ExpenseItParseCode;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.ocr.OcrStatusEnum;
import com.concur.mobile.platform.ui.common.dialog.NoConnectivityDialogFragment;
import com.concur.mobile.platform.ui.common.util.PreferenceUtil;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * An extension of <code>ConcurView</code> used to render a combined list of cash and card expenses.
 *
 * @author AndrewK
 */
public class Expenses extends BaseFragment implements INetworkActivityListener {

    private static final String CLS_TAG = Expenses.class.getSimpleName();

    /**
     * @author Chris N. Diaz
     */
    public interface ExpensesCallback {

        public void doGetSmartExpenseList();

        public void onGetSmartExpenseListSuccess();

        public void onGetSmartExpenseListFailed();

//        public void doGetReceiptList();

        public void doGetExpenseItList();

        public void onGetExpenseItListSuccess();

        public void onGetExpenseItListFailed();

        void startBackgroundRefresh();

        void endBackgroundRefresh();

    }

    /**
     * Preference key used to save the user's selected Expense List sort order.
     */
    public final static String PREF_EXPENSE_LIST_SORT_ORDER = "PREF_EXPENSE_LIST_SORT_ORDER";


    private static final int HEADER_VIEW_TYPE = 0;

    private static final int EXPENSE_VIEW_TYPE = 1;

    private static final String SELECTED_EXPENSE_KEY = "selected.expense.key";

    private static final String SMART_EXPENSE_SPLIT_KEY = "smart.expense.split.key";

    private static final String SMART_EXPENSE_TYPE_KEY = "smart.expense.type";

    private static final String FLURRY_HOW_MANY_ADDED_KEY = "flurry.how.many.added";
    private static final String FLURRY_HAS_CREDIT_CARD_KEY = "flurry.has.credit.card";
    private static final String FLURRY_HAS_RECEIPT_KEY = "flurry.has.receipt";

    private static final String CURRENT_VIEW_STATE = "current.view.state";

    private static final String ACTIVE_REPORTS_LIST_TAG = "active.reports.list.tag";

    // Store any receivers we need to register and the fragment tag to send them.
    // TODO: This is due to dialog fragments retaining instance and Expenses not retaining instance, so we need to unregister and
    // re-register receivers hooked to dialog fragments (those receivers often need to reference the tag of the Expenses fragment
    // which added them if orientation is changed).
    private static final String SHOULD_REGISTER_MOBILE_DELETE_RECEIVER = "should.register.mobile.delete.receiver";
    private static final String FRAGMENT_TAG = "fragment.tag";

    /**
     * A reference to the intent filter used to receive broadcast messages of data updates.
     */
    private static IntentFilter ALL_EXPENSE_FILTER = new IntentFilter(Const.ACTION_EXPENSE_ALL_EXPENSE_UPDATED);

    /**
     * Contains a reference to the view flipper.
     */
    protected ViewFlipper viewFlipper;

    /**
     * An enum defining a few view states.
     */
    public enum ViewState {
        LOCAL_DATA, // Indicates there is local data being viewed.
        LOCAL_DATA_REFRESH, // Indicates viewing local data with background
        // fetch on-going.
        RESTORE_APP_STATE, // Indicates the application is restoring state.
        NO_LOCAL_DATA_REFRESH, // No local data present, server refresh
        // happening.
        NO_DATA
        // No data either locally or from the server.
    }

    ;

    /**
     * Contains the current view state.
     */
    protected ViewState viewState;

    /**
     * A reference to the AddToReportRequest from ConcurService
     */
    protected AddToReportRequest addToReportRequest;

    /**
     * A reference to the application instance representing the client.
     */
    private ConcurCore app;

    /**
     * Contains whether or not view needs to be built on an service availability update.
     */
    private boolean buildViewDelay;

    /**
     * Contains a reference to the last saved instance state data.
     */
    protected Bundle savedInstanceState;

    /**
     * Contains whether or not the handling of a call from 'onRestoreInstanceState' was delayed due to the view not being present.
     */
    protected boolean restoreInstanceStateDelay;

    /**
     * Contains a reference to a broadcast receiver for enabling/disabling the progress indicator.
     */
    private NetworkActivityReceiver networkActivityReceiver;

    /**
     * Contains a reference to an intent filter for network activity notification.
     */
    private IntentFilter networkActivityFilter;

    /**
     * Contains a sticky intent that was returned as a result of registering for network activity broadcast messages.
     */
    protected Intent networkActivityStickyIntent;

    /**
     * Contains whether or not the network activity receiver has been registered.
     */
    private boolean networkActivityRegistered;

    /**
     * Contains the last http status message in the event of a non-200 reply.
     */
    protected String lastHttpErrorMessage;

    /**
     * Contains the last action status error message returned from the server.
     */
    protected String actionStatusErrorMessage;

    /**
     * Contains whether or not the button toolbar is currently visible on screen.
     */
    private boolean barVisible;

    /**
     * A reference to the slideable button bar.
     */
    private View buttonBar;

    /**
     * A reference to the list view.
     */
    protected ListView expenseList;

    /**
     * A reference to the smart expense that will be split.
     */
    private Expense smartExpenseToBeSplit;

    /**
     * A reference to a listener for handling checkbox state changes.
     */
    private OnCheckChange onCheckChange;

    /**
     * Contains the set of checked expenses.
     */
    private HashSet<Expense> checkedExpenses;

    /**
     * Contains a map from <code>Expense</code> to <code>CompoundButton</code> objects.
     */
    private HashMap<Expense, CompoundButton> expenseButtonMap;

    /**
     * Contains a reference to the list item adapter.
     */
    private ListItemAdapter<ListItem> listItemAdapter;

    /**
     * Contains a reference to an active report list adapter.
     */
    protected ActiveReportsListAdapter activeReportListAdapter;

    /**
     * Contains a map from the view state to a child index of the view flipper.
     */
    private HashMap<ViewState, Integer> viewStateFlipChild;

    /**
     * Contains a reference to the click listener for an expense entry.
     */
    private ExpenseEntryClickListener expEntClickListener;

    /**
     * Contains a receiver of the outcome of uploading an expense receipt.
     */
    private BroadcastReceiver receiptUpload;

    /**
     * Contains a filter used to register the receipt upload receiver.
     */
    private IntentFilter receiptUploadFilter;

    /**
     * Contains a receiver of the outcome of adding expenses to a report.
     */
    private BroadcastReceiver addToReport;

    /**
     * Contains a filter for registering the "add to report" receiver.
     */
    private IntentFilter addToReportFilter;

    /**
     * Contains the error message associated with the last upload receipt attempt.
     */
    private String uploadReceiptErrorMessage;

    /**
     * Holds the protected PCA_KEY used to filter the expense list if desired
     */
    private String pcaKeyFilter;

    /**
     * Holds whether only corporate card transactions should be displayed.
     */
    private boolean showCorpCardTransOnly;

    /**
     * Contains a receiver to handle the outcome of a request to delete mobile entries (cash expenses).
     */
    private DeleteMobileEntryReceiver mobileEntryDeleteReceiver;

    /**
     * Contains the intent filter used to register the mobile entry delete receiver.
     */
    private IntentFilter mobileEntryDeleteFilter;

    /**
     * Whether or not the DeleteMobileEntryReceiver is registered.
     */
    private boolean mobileEntryDeleteReceiverRegistered;

    /**
     * Should we register this receiver in registerReceivers()
     */
    private boolean shouldRegisterMobileEntryDeleteReceiver;

    /**
     * Contains an outstanding request to delete mobile entries.
     */
    private DeleteMobileEntriesRequest mobileEntryDeleteRequest;

    /**
     * Contains whether or not the mobile entry delete response was received.
     */
    private boolean mobileEntryDeleteResponseReceived;

    /**
     * Contains the report key passed into this activity to which expenses should be added.
     */
    private String reportKey;

    /**
     * Contains the report name passed into this activity to which expenses should be added.
     */
    private String reportName;

    /**
     * On orientation change, the original fragment tag is stored for dialog fragments that are retaining instance state.
     */
    private String originalFragTag;

    /**
     * Contains the count of expenses being added to a report.
     */
    private int flurryHowManyAddedCount = 0;
    /**
     * Contains whether the expenses being added to a report contain at least one credit card expense.
     */
    boolean flurryHasCreditCard = false;
    /**
     * Contains whether the expenses being added to a report contain at least one receipt.
     */
    boolean flurryHasReceipt = false;

    private ExpensesCallback expensesCallback;

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        app = getConcurCore();

        Intent launchIntent = activity.getIntent();

        this.savedInstanceState = savedInstanceState;

        pcaKeyFilter = launchIntent.getStringExtra(Const.EXTRA_PCA_KEY);
        showCorpCardTransOnly = launchIntent.getBooleanExtra(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_FILTER_KEY,
                false);

        networkActivityReceiver = new NetworkActivityReceiver(getActivity(), this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);
        networkActivityStickyIntent = activity.registerReceiver(networkActivityReceiver, networkActivityFilter);
        networkActivityRegistered = true;

        receiptUpload = new ReceiptUploadReceiver();
        receiptUploadFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_UPLOADED);

        addToReport = new AddToReportReceiver();
        addToReportFilter = new IntentFilter(Const.ACTION_EXPENSE_ADDED_TO_REPORT);

        // Construct and populate map from view state to child index.
        viewStateFlipChild = new HashMap<ViewState, Integer>();
        viewStateFlipChild.put(ViewState.LOCAL_DATA, 0);
        viewStateFlipChild.put(ViewState.NO_DATA, 2);
        viewStateFlipChild.put(ViewState.RESTORE_APP_STATE, 1);
        // The last two states here map to the same view.
        viewStateFlipChild.put(ViewState.NO_LOCAL_DATA_REFRESH, 3);
        viewStateFlipChild.put(ViewState.LOCAL_DATA_REFRESH, 3);

        // Init to local data.
        viewState = ViewState.LOCAL_DATA;

        // Set any passed in report key.
        Intent intent = launchIntent;
        if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
            reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY).trim();
            if (reportKey != null) {
                if (reportKey.length() == 0) {
                    reportKey = null;
                }
            }
        }
        if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
            reportName = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME).trim();
            if (reportName != null) {
                if (reportName.length() == 0) {
                    reportName = null;
                }
            }
        }

        setHasOptionsMenu(true);

        return buildView(inflater, savedInstanceState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.fragment.BaseFragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            expensesCallback = (ExpensesCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ExpensesCallback.");
        }

    }

    /**
     * Will add a new quick expense.
     */
    public void onAddQuickExpense() {
        // Verify that we have expense types
        ConcurCore concurCore = getConcurCore();
        IExpenseEntryCache expEntCache = concurCore.getExpenseEntryCache();
        ArrayList<ExpenseType> expenseTypes = expEntCache.getExpenseTypes();
        if (expenseTypes != null) {
            Intent intent = new Intent(activity, QuickExpense.class);
            intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, Expense.ExpenseEntryType.CASH.name());
            intent.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_EXPENSE_LIST);
            intent.putExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, Const.CREATE_MOBILE_ENTRY);
            startActivityForResult(intent, Const.CREATE_MOBILE_ENTRY);
        } else {
            // Can't go there
            DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_no_expense_types_title,
                    R.string.dlg_expense_no_expense_types_message).show(getFragmentManager(), null);
        }

    }

    private View buildView(LayoutInflater inflater, Bundle savedInstanceState) {

        // Inflate our view
        View root = inflater.inflate(R.layout.expenses, null);

        viewFlipper = (ViewFlipper) root.findViewById(R.id.view_flipper);
        if (viewFlipper != null) {
            // Animation anim = AnimationUtils.loadAnimation(this,
            // R.anim.fade_out);
            // anim.setDuration(400L);
            // viewFlipper.setOutAnimation(anim);
        }

        // Handle the null state button
        Button addQEButton = (Button) root.findViewById(R.id.new_quick_expense);
        addQEButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onAddQuickExpense();
            }
        });

        // Init the listener to handle checkbox changes.
        onCheckChange = new OnCheckChange();

        // Configure the button bar.
        configureButtonBar(root);

        return root;
    }

    @Override
    public Integer getTitleResource() {
        return R.string.expenses_title;
    }

    @Override
    public void onStart() {
        super.onStart();

        // Determine whether we have to delay building the view until the
        // service
        // becomes available.
        buildViewDelay = !activity.isServiceAvailable();
        if (!buildViewDelay) {
            initView(savedInstanceState);
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".buildView: service is unavailable, waiting for 'onServiceAvailable' call.");
            // Flip to view indicating the app state is being restored.
            viewState = ViewState.RESTORE_APP_STATE;
            flipViewForViewState();
        }

        // Check if we need to prompt to rate
        //
        boolean shouldCheck = activity.getIntent().getBooleanExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, false);

        if (shouldCheck) {
            // Prompt for rating
            //
            FeedbackManager.with(getActivity()).showRatingsPrompt();

            /*
             * SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext()); if
             * (Preferences.shouldPromptToRate(prefs, false)) { if (ConcurCore.isConnected()) { new
             * PromptToRateDialogFragment().show(getFragmentManager(), null); } else { Log.d(Const.LOG_TAG,
             * "showdialog.rate : offline"); } }
             */
        }
    }

    /**
     * Will initialize the main view of this activity.
     */
    private void initView(Bundle savedInstanceState) {

        // Handle any sticky network activity requests.
        showNetworkActivityStickyIntent();

        // If there is no local cached data, then flip to view indicating this.
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        ArrayList<Expense> cacheList = expEntCache.getExpenseEntries();
        if (cacheList != null && (cacheList.size() > 0) && (cacheList.size() > getOfflineItemCount(cacheList))) {
            viewState = ViewState.LOCAL_DATA;
            flipViewForViewState();

            // Populate the expense list.
            configureExpenseEntries();
        } else {
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
        }
        if (savedInstanceState != null) {
            restoreView(savedInstanceState);
        }
    }

    /**
     * Will flip the current view based on the value of <code>viewState</code>.
     */
    protected void flipViewForViewState() {
        if (viewFlipper != null) {
            HashMap<ViewState, Integer> viewStateFlipMap = getViewStateFlipChildIndexMap();
            if (viewStateFlipMap != null) {
                if (viewStateFlipMap.containsKey(viewState)) {
                    int newChildInd = viewStateFlipMap.get(viewState);
                    int curChildInd = viewFlipper.getDisplayedChild();
                    if (newChildInd != curChildInd) {
                        viewFlipper.setDisplayedChild(newChildInd);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: current view state '" + viewState
                            + "' not in map!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: null view state flip child map!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: can't find view flipper in current view!");
        }
    }

    /**
     * Will start to display any sticky intents that were returned as a result of registering for network activity.
     */
    protected void showNetworkActivityStickyIntent() {
        if (networkActivityStickyIntent != null) {
            networkActivityReceiver.onReceive(activity, networkActivityStickyIntent);
            networkActivityStickyIntent = null;
        }
    }

    /**
     * Will initiate refreshing the data if needbe.
     *
     * @param showNoConnectivityDialog whether to show the "no connectivity" dialog if the client is not connected.
     */
    public void checkForRefreshData(boolean showNoConnectivityDialog) {
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        ArrayList<Expense> cacheList = expEntCache.getExpenseEntries();
        // Check for no list or an expired one.
        if (!expEntCache.hasExpenseList() || expEntCache.shouldRefetchExpenseList() || cacheList == null
                || cacheList.size() == 0) {
            if (ConcurCore.isConnected()) {
                if (app.getService() != null) {

//                    // Fixed issue when this is a new user or one with empty data.
//                    // If the RS is empty, the refetch it so the OCR items show
//                    // in the Expense List!
//                    boolean refreshReceiptList = false;
//                    if (Preferences.isExpenseItUser()) {
//                        ReceiptStoreCache rsCache = app.getReceiptStoreCache();
//                        if (rsCache == null || rsCache.shouldRefetchReceiptList() || !rsCache.hasLastReceiptList()
//                                || rsCache.getReceiptInfoList() == null
//                                || rsCache.getLastReceiptInfoListUpdateTime() == null) {
//                            refreshReceiptList = true;
//                        }
//                    }

                    getSmartExpenses(Preferences.isExpenseItUser());
                }

                // Clear the refetch flag.
                expEntCache.clearShouldRefetchExpenseList();
            }
        } else {
            // Check for where there is an expense list, but it's empty!
            if (expEntCache.hasExpenseList() && (cacheList == null || cacheList.size() == 0)) {
                // Flip to view indicating no data currently exists.
                viewState = ViewState.NO_DATA;
                flipViewForViewState();
            }
        }
    }

    protected int getDataLoadingTextResourceId() {
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        int retVal = getRetrievingDataTextResourceId();
        if (expEntCache.hasExpenseList()) {
            retVal = getUpdatingDataTextResourceId();
        }
        return retVal;
    }

    protected int getRetrievingDataTextResourceId() {
        return R.string.retrieving_expenses;
    }

    protected int getUpdatingDataTextResourceId() {
        return R.string.updating_expenses;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#registerReceivers()
     */
    public void registerReceivers() {

        // Register the network activity receiver.
        if (!networkActivityRegistered) {
            networkActivityStickyIntent = activity.registerReceiver(networkActivityReceiver, networkActivityFilter);
            networkActivityRegistered = true;
            if (networkActivityStickyIntent != null) {
                networkActivityReceiver.onReceive(activity, networkActivityStickyIntent);
                networkActivityStickyIntent = null;
            }
        }

        if (shouldRegisterMobileEntryDeleteReceiver) {
            if (mobileEntryDeleteFilter == null) {
                mobileEntryDeleteFilter = new IntentFilter(Const.ACTION_EXPENSE_MOBILE_ENTRIES_DELETED);
            }
            if (mobileEntryDeleteReceiver == null) {
                mobileEntryDeleteReceiver = new DeleteMobileEntryReceiver(originalFragTag);
            }
            activity.registerReceiver(mobileEntryDeleteReceiver, mobileEntryDeleteFilter);
            mobileEntryDeleteReceiverRegistered = true;
            shouldRegisterMobileEntryDeleteReceiver = false;
        }

        activity.registerReceiver(receiptUpload, receiptUploadFilter);
        activity.registerReceiver(addToReport, addToReportFilter);

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#unregisterReceivers()
     */
    public void unregisterReceivers() {
        // Unregister the network activity receiver.
        if (networkActivityRegistered) {
            activity.unregisterReceiver(networkActivityReceiver);
            networkActivityRegistered = false;
        }

        if (mobileEntryDeleteReceiverRegistered) {
            activity.unregisterReceiver(mobileEntryDeleteReceiver);
            mobileEntryDeleteReceiverRegistered = false;
            shouldRegisterMobileEntryDeleteReceiver = true;
        }

        activity.unregisterReceiver(receiptUpload);
        activity.unregisterReceiver(addToReport);
        if (activeReportListAdapter != null) {
            activeReportListAdapter.unregisterReceivers();
        }

    }

    protected HashMap<ViewState, Integer> getViewStateFlipChildIndexMap() {
        return viewStateFlipChild;
    }

    protected boolean isDataUpdateRequired() {
        return true;
    }

    /**
     * Updates the ExpneseList UI with the latest data.
     */
    public void updateExpenseListUI() {

        // If there is no local cached data, then flip to view indicating this.
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        ArrayList<Expense> cacheList = expEntCache.getExpenseEntries();

        // If we are filtering we need to see if the filter will have anything
        // in order
        // to flip the view now before we enter the list handling stuff later.
        boolean filterHasItems = (pcaKeyFilter == null);
        if (pcaKeyFilter != null) {
            int size = (cacheList != null) ? cacheList.size() : 0;
            for (int i = 0; i < size; i++) {
                Expense e = cacheList.get(i);
                if (ExpenseEntryType.PERSONAL_CARD == e.getExpenseEntryType()
                        && pcaKeyFilter.equals(e.getPersonalCard().pcaKey)) {
                    filterHasItems = true;
                    break;
                }
            }
        } else if (showCorpCardTransOnly) {
            int size = (cacheList != null) ? cacheList.size() : 0;
            for (int i = 0; i < size; i++) {
                Expense e = cacheList.get(i);
                if (ExpenseEntryType.CORPORATE_CARD == e.getExpenseEntryType()) {
                    filterHasItems = true;
                    break;
                }
            }
        }

        // Query to see if there are any ExpenseIt items.
        boolean hasExpenseItItems = false;
        Cursor cursor = null;
        try {
            String where = com.concur.mobile.platform.expense.provider.Expense.ExpenseItReceiptColumns.USER_ID + " = ?";
            String[] selectionArgs = new String[]{activity.getUserId()};
            cursor = getActivity().getContentResolver().query(com.concur.mobile.platform.expense.provider.Expense.ExpenseItReceiptColumns.CONTENT_URI,
                    null, where, selectionArgs, null);
            hasExpenseItItems = (cursor != null && cursor.getCount() > 0);

        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        if ((filterHasItems && cacheList != null && cacheList.size() > 0) || hasExpenseItItems) {
            // Ensure the view containing the expense list is displayed.
            if (viewState != ViewState.LOCAL_DATA) {
                viewState = ViewState.LOCAL_DATA;
                flipViewForViewState();
            }
            if (listItemAdapter != null) {
                refreshExpenseList();
            } else {
                // Populate the expense list.
                configureExpenseEntries();
            }
        } else {
            // Flip to view indicating no data currently exists.
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
        }
    }

    protected String getHeaderTitle() {
        String retVal = null;
        CharSequence cs = getText(R.string.expenses_title);
        if (cs != null) {
            retVal = cs.toString();
        }
        return retVal;
    }

    protected void showAddToReportFailure(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_add_to_report_failed_title, message).show(
                getFragmentManager(), null);
    }

    protected void showExpenseDeleteFailure(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_delete_failed_title, message).show(
                getFragmentManager(), null);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onServiceAvailable()
     */
    @Override
    public void onServiceAvailable() {

        // Check whether the app needs to re-construct its view.
        if (buildViewDelay) {
            initView(savedInstanceState);
            buildViewDelay = false;
            if (restoreInstanceStateDelay) {
                restoreView(savedInstanceState);
                restoreInstanceStateDelay = false;
                savedInstanceState = null;
            }
        }
    }

    @Override
    public boolean isNetworkRequestInteresting(int networkRequestType) {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #getNetworkActivityText(java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return defaultText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStarted(int)
     */
    @Override
    public void networkActivityStarted(int networkMsgType) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStopped(int)
     */
    @Override
    public void networkActivityStopped(int networkMsgType) {
        // No-op.
    }

    /**
     * Displays a prompt for the user to choose whether or not to Smart Match expenses they are adding.
     */
    public void onAddToReport() {

        // Check for connectivity, if none, then display dialog and return.
        if (!ConcurCore.isConnected()) {
            new NoConnectivityDialogFragment().show(getFragmentManager(), null);
            return;
        }

        getReportList();

    }

    /**
     * If adding expenses to a report from the Expenses list, creates an alert dialog displaying all available Expense Reports. If
     * adding from an Expense Report, simply calls handleAddToReport.
     */
    private void getReportList() {

        if (reportKey == null) {
            if (activeReportListAdapter == null) {
                activeReportListAdapter = new ActiveReportsListAdapter(activity, true, false);
            }
            // Build the report list dialog fragment.
            ActiveReportsListDialogFragment frag = new ActiveReportsListDialogFragment();

            frag.setActiveReportsListAdapter(activeReportListAdapter);
            frag.setClickListener(new SelectReportDialogClickListener());
            frag.setCancelListener(new DialogCancelListener());

            frag.show(getFragmentManager(), ACTIVE_REPORTS_LIST_TAG);

        } else {
            handleAddToReport(reportKey, reportName);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        registerReceivers();

        if (activity.isServiceAvailable()) {
            checkForRefreshData(false);
        }

        activity.updateOfflineQueueBar();

    }

    @Override
    public void onPause() {
        super.onPause();

        unregisterReceivers();
    }

    private void restoreView(Bundle inState) {

        if (inState != null) {
            if (!buildViewDelay) {
                restoreInstanceStateDelay = false;
                // Restore the list of selected expenses.
                if (inState.containsKey(SELECTED_EXPENSE_KEY)) {
                    ArrayList<Integer> selExpList = inState.getIntegerArrayList(SELECTED_EXPENSE_KEY);
                    // Clear out the set of checked expenses.
                    if (checkedExpenses != null) {
                        checkedExpenses.clear();
                    } else {
                        checkedExpenses = new HashSet<Expense>();
                    }
                    for (int expInd = 0; expInd < selExpList.size(); ++expInd) {
                        int selExpInd = selExpList.get(expInd);
                        Expense exp = ((ExpenseListItem) listItemAdapter.getItem(selExpInd)).expense;
                        if (exp != null) {
                            checkedExpenses.add(exp);
                            // It's possible that the view for the expense
                            // hasn't actually been built
                            // yet so there won't be an entry in
                            // 'expenseButtonMap' for this expense.
                            CompoundButton cmpBut = expenseButtonMap.get(exp);
                            if (cmpBut != null && !cmpBut.isChecked()) {
                                cmpBut.setChecked(true);
                            }
                        }
                    }
                    toggleButtonBar();
                }

                // Reset the smart expense to be split if set.
                if (inState.containsKey(SMART_EXPENSE_SPLIT_KEY)) {
                    String smartExpenseKey = inState.getString(SMART_EXPENSE_SPLIT_KEY);
                    ConcurCore ConcurCore = getConcurCore();
                    IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
                    String smartExpenseTypeStr = inState.getString(SMART_EXPENSE_TYPE_KEY);
                    Expense.ExpenseEntryType expEntType = Expense.ExpenseEntryType.valueOf(smartExpenseTypeStr);
                    if (expEntType == Expense.ExpenseEntryType.SMART_CORPORATE) {
                        smartExpenseToBeSplit = expEntCache.findSmartCorpExpenseEntry(smartExpenseKey);
                    } else if (expEntType == Expense.ExpenseEntryType.SMART_PERSONAL) {
                        smartExpenseToBeSplit = expEntCache.findSmartPersExpenseEntry(smartExpenseKey);
                    }
                    if (smartExpenseToBeSplit == null) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG
                                        + ".onRestoreInstanceState: unable to locate smart expense to be split in expense entry cache!");
                    }
                }

                // If we're showing the ActiveReportsListDialogFragment, we want to update its click and cancel listener because
                // the fragment does not retain instance state.
                Fragment reportListDialogFrag = getFragmentManager().findFragmentByTag(ACTIVE_REPORTS_LIST_TAG);
                if (reportListDialogFrag instanceof ActiveReportsListDialogFragment) {
                    ActiveReportsListDialogFragment dlgFrag = (ActiveReportsListDialogFragment) reportListDialogFrag;

                    dlgFrag.setClickListener(new SelectReportDialogClickListener());
                    dlgFrag.setCancelListener(new DialogCancelListener());
                } else {
                    Log.d(FRAGMENT_TAG, "Failed to find fragment for ACTIVE_REPORTS_LIST_TAG");
                }
                savedInstanceState = null;
            } else {
                restoreInstanceStateDelay = true;
            }

            // Used to restore the mobileEntryDeleteReceiver
            shouldRegisterMobileEntryDeleteReceiver = inState.getBoolean(SHOULD_REGISTER_MOBILE_DELETE_RECEIVER);
            originalFragTag = inState.getString(FRAGMENT_TAG);

            // Restore 'flurryHowManyAddedCount'.
            flurryHowManyAddedCount = inState.getInt(FLURRY_HOW_MANY_ADDED_KEY);
            // Restore 'flurryHasCreditCard'.
            flurryHasCreditCard = inState.getBoolean(FLURRY_HAS_CREDIT_CARD_KEY);
            // Restore 'flurryHasReceipt'.
            flurryHasReceipt = inState.getBoolean(FLURRY_HAS_RECEIPT_KEY);

            // Restore the view state.
            if (inState != null && inState.containsKey(CURRENT_VIEW_STATE)) {
                viewState = (ViewState) inState.getSerializable(CURRENT_VIEW_STATE);
                if (viewState == ViewState.LOCAL_DATA_REFRESH) {
                    // This also updates the text to "Updating Expenses...".
                    showLoadingView();
                } else {
                    flipViewForViewState();
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onSaveInstanceState(android .os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        // Iterate over the set of selected expenses and write them to
        // 'outState'.
        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> ckExpIter = checkedExpenses.iterator();
            ArrayList<Integer> selectedPositions = new ArrayList<Integer>();
            while (ckExpIter.hasNext()) {
                Expense exp = ckExpIter.next();
                int adapterPos = -1;
                for (int listItemInd = 0; listItemInd < listItemAdapter.getCount(); ++listItemInd) {
                    if (listItemAdapter.getItem(listItemInd) instanceof ExpenseListItem) {
                        if (((ExpenseListItem) listItemAdapter.getItem(listItemInd)).expense == exp) {
                            adapterPos = listItemInd;
                            break;
                        }
                    }
                }
                if (adapterPos != -1) {
                    selectedPositions.add(adapterPos);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onSaveInstanceState: selected expense has -1 position!");
                }
            }
            // Write out the list of selected item positions.
            outState.putIntegerArrayList(SELECTED_EXPENSE_KEY, selectedPositions);
        }

        if (shouldRegisterMobileEntryDeleteReceiver) {
            outState.putBoolean(SHOULD_REGISTER_MOBILE_DELETE_RECEIVER, shouldRegisterMobileEntryDeleteReceiver);
        }
        outState.putString(FRAGMENT_TAG, originalFragTag);

        // Save out the key of the smart expense to be split, if non-null.
        if (smartExpenseToBeSplit != null) {
            outState.putString(SMART_EXPENSE_TYPE_KEY, smartExpenseToBeSplit.getExpenseEntryType().name());
            if (smartExpenseToBeSplit.getExpenseEntryType() == Expense.ExpenseEntryType.SMART_CORPORATE) {
                outState.putString(SMART_EXPENSE_SPLIT_KEY, smartExpenseToBeSplit.getCorporateCardTransaction()
                        .getCctKey());
            } else if (smartExpenseToBeSplit.getExpenseEntryType() == Expense.ExpenseEntryType.SMART_PERSONAL) {
                outState.putString(SMART_EXPENSE_SPLIT_KEY, smartExpenseToBeSplit.getPersonalCardTransaction().pctKey);
            }
        }

        // Write out the 'flurryHowManyAddedCount'.
        outState.putInt(FLURRY_HOW_MANY_ADDED_KEY, flurryHowManyAddedCount);
        // Write out the 'flurryHasCreditCard'.
        outState.putBoolean(FLURRY_HAS_CREDIT_CARD_KEY, flurryHasCreditCard);
        // Write out the 'flurryHasReceipt'.
        outState.putBoolean(FLURRY_HAS_RECEIPT_KEY, flurryHasReceipt);

        // Save the current view state.
        outState.putSerializable(CURRENT_VIEW_STATE, viewState);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            // Ensure the view containing the expense list is displayed.
            if (viewState != ViewState.LOCAL_DATA) {
                viewState = ViewState.LOCAL_DATA;
                flipViewForViewState();
            }
            if (listItemAdapter != null) {
                refreshExpenseList();
            } else {
                // Populate the expense list.
                configureExpenseEntries();
            }

            if (requestCode == Const.CREATE_MOBILE_ENTRY) {
                // Prompt for rating
                //
                FeedbackManager.with(getActivity()).showRatingsPrompt();

                /*
                 * SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ConcurCore.getContext()); if
                 * (Preferences.shouldPromptToRate(prefs, false)) { if (ConcurCore.isConnected()) { new
                 * PromptToRateDialogFragment().show(getFragmentManager(), null); } else { Log.d(Const.LOG_TAG,
                 * "showdialog.rate : offline"); } }
                 */
            }

            // Get the reportKey and add the selected expense entry(s)
            // to the newly created report.
            if (requestCode == Const.CREATE_NEW_REPORT && data != null && data.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {

                String reportKey = data.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
                handleAddToReport(reportKey, null);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expenses, menu);
    }

    private boolean allExpensesSelected() {
        boolean allSelected = false;
        // First, grab a count of all ListItem objects that are sub-classes of
        // ExpenseListItem.
        // The list may contain sub-classes of ListItem that are not
        // ExpenseListItem, like HeaderListItem objects.
        int totalExpenseListItems = 0;
        if (listItemAdapter != null) {
            for (int listItemInd = 0; listItemInd < listItemAdapter.getCount(); ++listItemInd) {
                if (listItemAdapter.getItem(listItemInd) instanceof ExpenseListItem) {
                    ++totalExpenseListItems;
                }
            }
        }
        allSelected = (checkedExpenses != null && listItemAdapter != null
                && checkedExpenses.size() == totalExpenseListItems && totalExpenseListItems > 0);
        return allSelected;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (viewState != ViewState.RESTORE_APP_STATE) {
            switch (viewState) {
                case LOCAL_DATA:
                case LOCAL_DATA_REFRESH: {
                    // Show 'Select All' option.
                    MenuItem menuItem = menu.findItem(R.id.select_all);
                    if (menuItem != null) {
                        menuItem.setVisible(true);
                        if (allExpensesSelected()) {
                            menuItem.setTitle(R.string.deselect_all);
                        } else {
                            menuItem.setTitle(R.string.select_all);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareOptionsMenu: missing 'select_all' menu item!");
                    }
                    break;
                }
                case NO_DATA:
                case NO_LOCAL_DATA_REFRESH: {
                    // Hide 'Select All' option.
                    MenuItem menuItem = menu.findItem(R.id.select_all);
                    if (menuItem != null) {
                        menuItem.setVisible(false);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareOptionsMenu: missing 'select_all' menu item!");
                    }
                    break;
                }
                case RESTORE_APP_STATE:
                    // No-op.
                    break;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onOptionsItemSelected(android .view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        boolean retVal = false;
        final int itemId = menuItem.getItemId();
        if (itemId == R.id.select_all) {
            if (!allExpensesSelected()) {
                // Select all.
                // Clear out the set of checked expenses.
                if (checkedExpenses != null) {
                    checkedExpenses.clear();
                } else {
                    checkedExpenses = new HashSet<Expense>();
                }
                if (listItemAdapter != null) {
                    for (int expInd = 0; expInd < listItemAdapter.getCount(); ++expInd) {
                        if (listItemAdapter.getItem(expInd) instanceof ExpenseListItem) {
                            Expense exp = ((ExpenseListItem) listItemAdapter.getItem(expInd)).expense;
                            if (exp != null) {
                                checkedExpenses.add(exp);
                                // It's possible that the view for the expense
                                // hasn't actually been built
                                // yet so there won't be an entry in
                                // 'expenseButtonMap' for this expense.
                                CompoundButton cmpBut = expenseButtonMap.get(exp);
                                if (cmpBut != null && !cmpBut.isChecked()) {
                                    cmpBut.setChecked(true);
                                }
                            }
                        }
                    }
                }
            } else {
                // Deselect all.
                if (checkedExpenses != null) {
                    Iterator<Expense> ckExpIter = checkedExpenses.iterator();
                    while (ckExpIter.hasNext()) {
                        Expense ckExp = ckExpIter.next();
                        // Punt from the underlying set.
                        ckExpIter.remove();
                        if (expenseButtonMap != null && expenseButtonMap.containsKey(ckExp)) {
                            CompoundButton cmpBut = expenseButtonMap.get(ckExp);
                            if (cmpBut != null && cmpBut.isChecked()) {
                                cmpBut.setChecked(false);
                            }
                        }
                    }
                    // MOB-18825 - Add/Remove buttons should hide after deselecting all.
                    toggleButtonBar();
                }
            }
            retVal = true;
        } else if (itemId == R.id.new_cash_expense) {
            onAddQuickExpense();
            retVal = true;
        } else if (itemId == R.id.refresh) {
            if (ConcurCore.isConnected()) {

                // Also refresh the ExpenseIt list (if ExpenseIt user).
                getSmartExpenses(true);

            } else {
                new NoConnectivityDialogFragment().show(getFragmentManager(), null);
            }
        } else if (itemId == R.id.delete) {

        } else if (itemId == R.id.sort_by) {
            String sortOrder = PreferenceUtil.getStringPreference(getBaseActivity(), PREF_EXPENSE_LIST_SORT_ORDER,
                    com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER);
            SortExpensesDialogFragment dialog = SortExpensesDialogFragment.newInstance(sortOrder);
            dialog.show(getBaseActivity().getFragmentManager(), "sort_expense_dialog_fragment");
        }

        return retVal;
    }

    /**
     * Will determine whether all responses have been received for pending expense delete requests.
     *
     * @return whether all responses have been received for pending expense delete requests.
     */
    private boolean allExpenseDeleteResponsesReceived() {
        boolean retVal = false;
        if ((mobileEntryDeleteRequest == null || (mobileEntryDeleteRequest != null && mobileEntryDeleteResponseReceived))) {
            retVal = true;
        }
        return retVal;
    }

    /**
     * Will display a dialog box
     */
    public void onDelete() {

        // Check for connectivity, if none, then display dialog and return.
        if (!ConcurCore.isConnected()) {
            new NoConnectivityDialogFragment().show(getFragmentManager(), null);
            return;
        }

        // First, construct a list of corporate and personal card transaction keys, and cash expenses to be hidden/deleted.
        // List of selected personal card transaction keys.
        final ArrayList<String> pctKeys = new ArrayList<String>();
        // List of selected corporate card transaction keys.
        final ArrayList<String> cctKeys = new ArrayList<String>();
        // List of selected cash transactions.
        final ArrayList<MobileEntry> mobileEntries = new ArrayList<MobileEntry>();
        Iterator<Expense> selExpIter = checkedExpenses.iterator();
        while (selExpIter.hasNext()) {
            Expense exp = selExpIter.next();
            switch (exp.getExpenseEntryType()) {
                case CASH:
                    mobileEntries.add(exp.getCashTransaction());
                    break;
                case PERSONAL_CARD:
                    // Per MOB-4286, deletion from mobile is not
                    // supported.
                    // pctKeys.add(exp.getPersonalCardTransaction().pctKey);
                    break;
                case CORPORATE_CARD:
                    // Per MOB-4286, deletion from mobile is not
                    // supported.
                    // cctKeys.add(exp.getCorporateCardTransaction().getCctKey());
                    break;
                case SMART_CORPORATE:
                    // Per MOB-4286, deletion from mobile is not
                    // supported.
                    // mobileEntries.add(exp.getCashTransaction());
                    // cctKeys.add(exp.getCorporateCardTransaction().getCctKey());
                    break;
                case SMART_PERSONAL:
                    // Per MOB-4286, deletion from mobile is not
                    // supported.
                    // mobileEntries.add(exp.getCashTransaction());
                    // pctKeys.add(exp.getPersonalCardTransaction().pctKey);
                    break;
                case RECEIPT_CAPTURE:
                    // deletion from mobile is not supported.
                    break;
            }
        }

        boolean hasCashExpenses = (mobileEntries != null && mobileEntries.size() > 0);
        int cashExpensesSelected = 0;
        if (hasCashExpenses) {
            cashExpensesSelected = mobileEntries.size();
        }

        // If there's more checked expenses than just cash expenses, they are card charges. If no cash expenses, all are cards.
        // This can be simplified when card charge deletion is enabled by checking their combined array sizes.
        boolean hasCardCharges = false;
        if (hasCashExpenses) {
            hasCardCharges = (checkedExpenses.size() > mobileEntries.size());
        } else {
            hasCardCharges = (checkedExpenses != null && checkedExpenses.size() > 0);
        }

        /*
         * The dialog message and title will vary depending on what combination of card and cash expenses are to be deleted.
         * Currently only card charges are checked for quantity to produce a string from plurals because we are not supporting
         * card charges. Should we support card charges in the future, the same changes will have to apply to them.
         */
        String dialogMessage;
        String dialogTitle;

        if (hasCashExpenses & !hasCardCharges) {
            dialogMessage = getResources().getQuantityString(R.plurals.dlg_expense_remove_confirm_message,
                    cashExpensesSelected).toString();
            dialogTitle = getString(R.string.dlg_expense_confirm_report_delete_title);
        } else if (hasCardCharges & !hasCashExpenses) {
            dialogMessage = getString(R.string.dlg_expense_remove_card_charge_not_supported);
            dialogTitle = getString(R.string.dlg_expense_delete_failed_title);
        } else {
            // Must be a mix
            dialogMessage = getResources().getQuantityString(R.plurals.dlg_expense_remove_mixed_confirm,
                    cashExpensesSelected);
            dialogTitle = getString(R.string.dlg_expense_confirm_report_delete_title);
        }

        final String userId = activity.getUserId();

        /*
         * If there aren't cash expenses selected, then we just throw up text saying they have to delete card charges on mobile.
         * Either way is an alert dialog, but it's pointless to do all of the positive button building if it isn't used.
         * 
         * The reason we're passing null for the negative listener, but still building a negative button is that the factory will
         * build the button and by default that button will dismiss the dialog onClick. Since that's all we want our negative
         * listener to do, passing null is sufficient here.
         */
        if (hasCashExpenses) {

            originalFragTag = getTag();

            DialogFragmentFactory.getAlertDialog(dialogTitle, dialogMessage, R.string.cardlist_btn_delete, -1,
                    R.string.general_cancel, new AlertDialogFragment.OnClickListener() {

                        @Override
                        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                        }

                        // Card deletion is unsupported, but the code remains for when we do support it in the future.
                        @Override
                        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                            boolean pendingRequest = false;

                            mobileEntryDeleteRequest = null;
                            mobileEntryDeleteResponseReceived = false;

                            // Get the total number of expenses that will be deleted
                            int numOfExpensesToDelete = 0;

                            // Second, handle any selected personal card transactions.
                            if (!pctKeys.isEmpty()) {
                                numOfExpensesToDelete += pctKeys.size();
                            }

                            // Third, handle any selected corporate card transactions.
                            if (!cctKeys.isEmpty()) {
                                numOfExpensesToDelete += cctKeys.size();
                            }

                            // Fourth, handle any selected cash entries.
                            if (!mobileEntries.isEmpty()) {
                                if (mobileEntryDeleteReceiver == null) {
                                    mobileEntryDeleteReceiver = new DeleteMobileEntryReceiver(originalFragTag);
                                }
                                if (mobileEntryDeleteFilter == null) {
                                    mobileEntryDeleteFilter = new IntentFilter(
                                            Const.ACTION_EXPENSE_MOBILE_ENTRIES_DELETED);
                                }
                                activity.registerReceiver(mobileEntryDeleteReceiver, mobileEntryDeleteFilter);
                                mobileEntryDeleteReceiverRegistered = true;
                                mobileEntryDeleteResponseReceived = false;
                                mobileEntryDeleteRequest = app.getService().sendMobileEntryDeleteRequest(userId,
                                        mobileEntries);
                                if (mobileEntryDeleteRequest == null) {
                                    activity.unregisterReceiver(mobileEntryDeleteReceiver);
                                    mobileEntryDeleteReceiverRegistered = false;
                                }
                                pendingRequest = (pendingRequest || (mobileEntryDeleteRequest != null));
                                numOfExpensesToDelete += mobileEntries.size();
                            } else {
                                mobileEntryDeleteRequest = null;
                                mobileEntryDeleteResponseReceived = false;
                            }
                            toggleButtonBar();

                            // Fifth, requests were sent out, display a dialog box.
                            if (pendingRequest) {
                                // Log the event
                                EventTracker.INSTANCE.track("Expenses", "Delete Mobile Entry");

                                checkedExpenses.clear();

                                // TODO: Crashes on orientation change
                                // Display the "deleting expense" dialog.
                                Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(originalFragTag);

                                if (frag instanceof Expenses) {
                                    DeleteExpenseProgressDialogHandler.show(frag, numOfExpensesToDelete);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + "Expenses.onDelete getAlertDialog can't reference Expenses!");
                                }
                            }
                        }
                    }, null, null, null).show(getFragmentManager(), null);
        } else {
            // There are only card charges here, so throw up an "ok" alert dialog.
            DialogFragmentFactory.getAlertOkayInstance(dialogTitle, dialogMessage).show(getFragmentManager(), null);
        }
    }

    /**
     * Gets the number of mobile entries in the cache list that haven't been uploaded.
     *
     * @param cacheList List of expense entries in ExpenseEntryCache
     * @return number of offline mobile entries
     */
    protected int getOfflineItemCount(ArrayList<Expense> cacheList) {
        int count = 0;
        for (Expense exp : cacheList) {
            if (ExpenseEntryType.CASH.equals(exp.getExpenseEntryType())) {
                MobileEntry ent = exp.getCashTransaction();
                if (ent != null)
                    if (ent.getMeKey() == null)
                        count++;
            }
        }
        return count;
    }

    /**
     * Determines whether the current list of selected expenses contain any cash expenses.
     *
     * @return <code>true</code> if the current list of selected expenses contain at least one cash expense; <code>false</code>
     * otherwise.
     */
    private boolean isCashExpenseSelected() {
        boolean retVal = false;
        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> expIter = checkedExpenses.iterator();
            while (expIter.hasNext()) {
                Expense checkedExpense = expIter.next();
                if (checkedExpense.getExpenseEntryType() == Expense.ExpenseEntryType.CASH) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    private int areMultipleCashExpensesSelected() {
        int numOfCashExpensesRead = 0;
        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> expIter = checkedExpenses.iterator();
            while (expIter.hasNext()) {
                Expense checkedExpense = expIter.next();
                if (checkedExpense.getExpenseEntryType() == Expense.ExpenseEntryType.CASH) {
                    numOfCashExpensesRead++;
                }
            }
        }
        return numOfCashExpensesRead;
    }

    /**
     * Determines whether the current list of selected expenses contain any non-cash expenses.
     *
     * @return <code>true</code> if the current list of selected expenses contain at least one non-cash expense;
     * <code>false</code> otherwise.
     */
    private boolean isCardChargeSelected() {
        boolean retVal = false;
        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> expIter = checkedExpenses.iterator();
            while (expIter.hasNext()) {
                Expense checkedExpense = expIter.next();
                if (checkedExpense.getExpenseEntryType() != Expense.ExpenseEntryType.CASH) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Determines whether the current list of selected expenses contain at least one "smart" expense.
     *
     * @return <code>true</code> if the current list of selected expenses contain at least one "smart" expense; <code>false</code>
     * otherwise.
     */
    private boolean isSmartExpenseSelected() {
        boolean retVal = false;
        if (checkedExpenses != null && checkedExpenses.size() > 0) {
            Iterator<Expense> expIter = checkedExpenses.iterator();
            while (expIter.hasNext()) {
                Expense checkedExpense = expIter.next();
                if (checkedExpense.getExpenseEntryType() == Expense.ExpenseEntryType.SMART_CORPORATE
                        || checkedExpense.getExpenseEntryType() == Expense.ExpenseEntryType.SMART_PERSONAL) {
                    retVal = true;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Configures the button bar.
     */
    private void configureButtonBar(final View root) {

        // Obtain a reference to the list for focus purposes
        expenseList = (ListView) root.findViewById(R.id.list_view);

        // Obtain a reference to the bar for sliding purposes.
        buttonBar = root.findViewById(R.id.expenseListButtonBar);

        // Hook up our handlers
        Button deleteButton = (Button) buttonBar.findViewById(R.id.expenseListBtnDelete);
        deleteButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onDelete();
            }
        });

        Button addButton = (Button) buttonBar.findViewById(R.id.expenseListBtnAdd);
        addButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onAddToReport();
            }
        });

        // Ensure bar is not initially visible.
        slideButtonBar(false);
        barVisible = false;

        // Set the "Add To Report" visibility to "GONE" if reports are not
        // allowed.
        if (!Preferences.shouldAllowReports()) {
            ViewUtil.setVisibility(buttonBar, R.id.expenseListBtnAdd, View.GONE);
        }
    }

    /**
     * Configures the list of expense entries.
     */
    private void configureExpenseEntries() {

        // Clear out the mapping from compound buttons to expense entries.
        if (expenseButtonMap != null) {
            expenseButtonMap.clear();
        } else {
            expenseButtonMap = new HashMap<Expense, CompoundButton>();
        }
        // Clear out the current set of checked expenses.
        // MOB-16486 : We should retain checked boxes even if a user opens an expense.
        if (checkedExpenses == null) {
            checkedExpenses = new HashSet<Expense>();
        }
        if (expenseList != null) {
            expEntClickListener = new ExpenseEntryClickListener();
            expenseList.setOnItemClickListener(expEntClickListener);

            // If the activity was started with a card filter then pass that in
            listItemAdapter = new ListItemAdapter<ListItem>(activity, getListItems());
            expenseList.setAdapter(listItemAdapter);
            // Enable context menu on the list.
            registerForContextMenu(expenseList);
            expenseList.setOnItemLongClickListener(new OnItemLongClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.widget.AdapterView.OnItemLongClickListener #onItemLongClick(android.widget.AdapterView,
                 * android.view.View, int, long)
                 */
                @Override
                public boolean onItemLongClick(AdapterView<?> adapterView, View itemView, int position, long id) {
                    boolean retVal = false;
                    // Basically return that we've consumed the event if
                    // the expense is a non-smart expense.
                    Expense exp = ((ExpenseListItem) listItemAdapter.getItem(position)).expense;
                    if (exp != null) {
                        retVal = (exp.getExpenseEntryType() != Expense.ExpenseEntryType.SMART_CORPORATE)
                                && (exp.getExpenseEntryType() != Expense.ExpenseEntryType.SMART_PERSONAL);
                    }
                    return retVal;
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureExpenseEntries: unable to locate the expense entry list view!");
        }
    }

    /**
     * Will animate sliding of the button bar depending upon the <code>onScreen</code> parameter.
     *
     * @param onScreen
     */
    protected void slideButtonBar(final boolean onScreen) {
        Float fromY = (onScreen) ? 1.0f : 0.0f;
        Float toY = (onScreen) ? 0.0f : 1.0f;
        TranslateAnimation slide = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, fromY, Animation.RELATIVE_TO_SELF, toY);
        slide.setDuration(400);
        slide.setFillAfter(true);

        slide.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
                // No-op
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No-op
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (onScreen) {
                    buttonBar.setVisibility(View.VISIBLE);
                    buttonBar.bringToFront();
                } else {
                    buttonBar.setVisibility(View.GONE);
                    expenseList.bringToFront();
                }
            }
        });

        buttonBar.startAnimation(slide);

    }

    /**
     * Will either display or hide the slideable button bar depending upon whether any expense checkboxes have been checked.
     */
    protected void toggleButtonBar() {
        boolean onScreen;
        if (checkedExpenses.size() > 0) {
            onScreen = true;
        } else {
            onScreen = false;
        }
        if (onScreen != barVisible) {
            slideButtonBar(onScreen);
            barVisible = onScreen;
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for responding to the outcome of adding expenses to a report.
     *
     * @author AndrewK
     */
    class AddToReportReceiver extends BroadcastReceiver {

        final String CLS_TAG = Expenses.CLS_TAG + "." + AddToReportReceiver.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            AddExpenseProgressDialogHandler.dismiss(Expenses.this);
            Expenses.this.addToReportRequest = null;
            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if ((intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS))
                                    || (intent.getStringExtra(Const.REPLY_STATUS)
                                    .equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS_SMARTEXP))) {
                                if (intent.hasExtra(Const.REPLY_ERROR_MESSAGE)) {
                                    actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                }

                                // Start the activity to submit the report.
                                String reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
                                if (reportKey != null) {

                                    // Set the flag on the active report list
                                    // that it should be re-freshed.
                                    ConcurCore concurCore = getConcurCore();
                                    IExpenseReportCache expCache = concurCore.getExpenseActiveCache();
                                    expCache.setShouldRefreshReportList();

                                    // Set the flag that the list of expenses
                                    // should be refreshed.
                                    IExpenseEntryCache expEntCache = concurCore.getExpenseEntryCache();
                                    expEntCache.setShouldFetchExpenseList();

                                    if (Expenses.this.reportKey == null) {
                                        // Create the intent and start the
                                        // activity.
                                        Intent reportSubIntent = new Intent(activity, ExpenseEntries.class);
                                        reportSubIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportKey);
                                        reportSubIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE,
                                                Const.EXPENSE_REPORT_SOURCE_ACTIVE);
                                        startActivity(reportSubIntent);
                                    } else {
                                        Intent it = new Intent();
                                        activity.setResult(Activity.RESULT_OK, it);
                                    }

                                    // Flurry Notification
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_HOW_MANY_ADDED,
                                            Integer.toString(flurryHowManyAddedCount));
                                    params.put(Flurry.PARAM_NAME_HAS_CREDIT_CARD,
                                            ((flurryHasCreditCard) ? Flurry.PARAM_VALUE_YES : Flurry.PARAM_VALUE_NO));
                                    params.put(Flurry.PARAM_NAME_HAS_RECEIPT,
                                            ((flurryHasReceipt) ? Flurry.PARAM_VALUE_YES : Flurry.PARAM_VALUE_NO));
                                    String cameFromParamValue = Flurry.PARAM_VALUE_EXPENSE_LIST;
                                    if (activity.getIntent().hasExtra(Flurry.PARAM_NAME_CAME_FROM)) {
                                        cameFromParamValue = activity.getIntent().getStringExtra(
                                                Flurry.PARAM_NAME_CAME_FROM);
                                    }
                                    params.put(Flurry.PARAM_NAME_CAME_FROM, cameFromParamValue);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY,
                                            Flurry.EVENT_NAME_ADD_TO_REPORT, params);

                                    // Finish this activity.
                                    // TODO This needs re-engineering. The
                                    // fragment should be just finishing its
                                    // parent.
                                    activity.finish();
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceiver: missing report key!");
                                }
                            } else {
                                actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- '"
                                        + actionStatusErrorMessage + "'.");

                                showAddToReportFailure(actionStatusErrorMessage);
                            }
                        } else {
                            lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage + ".");
                            new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                    }
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: service request error -- "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                    new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for responding to the results of an expense receipt upload attempt.
     *
     * @author AndrewK
     */
    class ReceiptUploadReceiver extends BroadcastReceiver {

        final String CLS_TAG = Expenses.CLS_TAG + "." + ReceiptUploadReceiver.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // Request a new set of expenses.
                                refreshExpenseList();
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: mobile web service error -- "
                                                + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                            }
                        } else {
                            uploadReceiptErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + uploadReceiptErrorMessage
                                    + ".");
                            DialogFragmentFactory.getAlertOkayInstance(
                                    R.string.dlg_expense_upload_receipt_failed_title,
                                    R.string.dlg_expense_upload_receipt_failed_message)
                                    .show(getFragmentManager(), null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                    }
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: service request error -- "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                    DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_upload_receipt_failed_title,
                            R.string.dlg_expense_upload_receipt_failed_message).show(getFragmentManager(), null);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> used to handle the result of a request to delete cash expenses.
     *
     * @author AndrewK
     */
    class DeleteMobileEntryReceiver extends BroadcastReceiver {

        public String fragTag;

        public DeleteMobileEntryReceiver(String fragTag) {
            this.fragTag = fragTag;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {
            // Unregister this receiver.
            context.unregisterReceiver(this);
            // Set the flag indicating a response has been received.
            mobileEntryDeleteResponseReceived = true;
            mobileEntryDeleteReceiverRegistered = false;

            Fragment frag = ((FragmentActivity) context).getSupportFragmentManager().findFragmentByTag(fragTag);

            if (allExpenseDeleteResponsesReceived()) {
                DeleteExpenseProgressDialogHandler.dismiss(frag);
            }
            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // Have all responses been received?
                                if (allExpenseDeleteResponsesReceived()) {

                                    getSmartExpenses(false);
                                }

                                // Flurry Notification
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_MOBILE_ENTRY);
                                EventTracker.INSTANCE.track(Flurry.CATEGORY_DELETE, Flurry.EVENT_NAME_ACTION, params);

                            } else {
                                if (allExpenseDeleteResponsesReceived()) {
                                    actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + actionStatusErrorMessage + ".");
                                    showExpenseDeleteFailure(actionStatusErrorMessage);
                                }
                            }
                        } else {
                            if (allExpenseDeleteResponsesReceived()) {
                                lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage
                                        + ".");
                                new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                    }
                } else {
                    if (allExpenseDeleteResponsesReceived()) {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: service request error -- "
                                        + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for responding to the result of deleting a set of expenses.
     *
     * @author AndrewK
     */
    class DeleteExpenseReceiver extends BroadcastReceiver {

        final String CLS_TAG = Expenses.CLS_TAG + "." + DeleteExpenseReceiver.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Upon success, we want to request a new list from the server.
            // Dismiss the dialog.
            DeleteExpenseProgressDialogHandler.dismiss(Expenses.this);

            int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
            if (serviceRequestStatus != -1) {
                if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                    int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                    if (httpStatusCode != -1) {
                        if (httpStatusCode == HttpStatus.SC_OK) {
                            if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {

                                getSmartExpenses(false);

                            } else {
                                actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                        + actionStatusErrorMessage + ".");
                                showExpenseDeleteFailure(actionStatusErrorMessage);
                            }
                        } else {
                            lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- " + lastHttpErrorMessage + ".");
                            new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                    }
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".onReceive: service request error -- "
                                    + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                    new SystemUnavailableDialogFragment().show(getFragmentManager(), null);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
            }
        }
    }

    /**
     * An implementation of <code>AdapterView.OnItemClickListener</code> to handle the selection of expense entries.
     *
     * @author AndrewK
     */
    class ExpenseEntryClickListener implements AdapterView.OnItemClickListener {

        private final String CLS_TAG = Expenses.CLS_TAG + "." + ExpenseEntryClickListener.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.AdapterView.OnItemClickListener#onItemClick(android .widget.AdapterView, android.view.View, int,
         * long)
         */
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (listItemAdapter != null) {
                Expense exp = ((ExpenseListItem) listItemAdapter.getItem(position)).expense;
                switch (exp.getExpenseEntryType()) {
                    case CASH: {
                        MobileEntry mobileEntry = exp.getCashTransaction();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY, mobileEntry.getMeKey());
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case PERSONAL_CARD: {
                        PersonalCardTransaction persCardTrans = exp.getPersonalCardTransaction();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_PERSONAL_CARD_ACCOUNT_KEY,
                                exp.getPersonalCard().pcaKey);
                        intent.putExtra(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY, persCardTrans.pctKey);
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case CORPORATE_CARD: {
                        CorporateCardTransaction corpCardTrans = exp.getCorporateCardTransaction();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY, corpCardTrans.getCctKey());
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case SMART_CORPORATE: {
                        CorporateCardTransaction corpCardTrans = exp.getCorporateCardTransaction();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY, corpCardTrans.getCctKey());
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case SMART_PERSONAL: {
                        PersonalCardTransaction persCardTrans = exp.getPersonalCardTransaction();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY, persCardTrans.pctKey);
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case RECEIPT_CAPTURE: {
                        ReceiptCapture receiptCaptures = exp.getReceiptCapture();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_CAPTURE_KEY, receiptCaptures.rcKey);
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case E_RECEIPT: {
                        EReceipt eReceipt = exp.getEReceipt();
                        Intent intent = new Intent(activity, QuickExpense.class);
                        intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, exp.getExpenseEntryType().name());
                        intent.putExtra(Const.EXTRA_EXPENSE_E_RECEIPT_KEY, eReceipt.getEReceiptId());
                        startActivityForResult(intent, Const.EDIT_MOBILE_ENTRY);
                        break;
                    }
                    case OCR_NOT_DONE: {

                        OCRItem item = exp.getOcrItem();

                        if (OcrStatusEnum.isFailed(item.getOcrStatus())) {
                            // Convert Failed OCR to mobile entry
                            Intent intent = new Intent(activity, QuickExpense.class);
                            intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, ExpenseEntryType.CASH);
                            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, item.getReceiptImageId());
                            // If we have upload date, we will use that as transaction date
                            if (item.getUploadDate() != null)
                                intent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY, item.getUploadDate()
                                        .getTimeInMillis());

                            intent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, Expense.ExpenseEntryType.CASH.name());
                            intent.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_EXPENSE_LIST);
                            intent.putExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, Const.CREATE_MOBILE_ENTRY);
                            startActivityForResult(intent, Const.CREATE_MOBILE_ENTRY);
                        }
                        break;
                    }

                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onItemClick: null expense list adapter!");
            }
        }
    }

    /**
     * An implementation of <code>DialogInterface.OnCancelListener</code> to handle canceling the "add to report" op.
     */
    class DialogCancelListener implements DialogInterface.OnCancelListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnCancelListener#onCancel(android .content.DialogInterface)
         */
        @Override
        public void onCancel(DialogInterface dialog) {
            dialog.dismiss();
        }

    }

    /**
     * Will handle adding the current set of selected expenses to either an existing report or a new report.
     *
     * @param reportKey  the key of an existing report.
     * @param reportName the name of a new report to create.
     */
    private void handleAddToReport(String reportKey, String reportName) {
        // Iterate over the list of selected expenses and seperate into two
        // lists of expense keys, one for personal card transactions, the other
        // for cash expenses.
        ArrayList<String> cctKeys = new ArrayList<String>();
        ArrayList<String> pctKeys = new ArrayList<String>();
        ArrayList<String> meKeys = new ArrayList<String>();
        ArrayList<Expense> smartCorpExpenses = new ArrayList<Expense>();
        ArrayList<Expense> smartPersExpenses = new ArrayList<Expense>();
        ArrayList<String> smartExpIds = new ArrayList<String>();
        // NOTE: At this point, 6/14/2011, adding specific attendees to expenses
        // when adding
        // to a report is not currently supported. Therefore, the only attendee
        // list that will be
        // added to each instance of 'AttendeeEntryMap' will be the default
        // attendee.
        List<AttendeesEntryMap> attendeesEntryMaps = new ArrayList<AttendeesEntryMap>();
        ExpenseReportAttendee defAtt = getDefaultAttendee();
        if (defAtt == null) {
            Log.i(Const.LOG_TAG, CLS_TAG + ".handleAddToReport: default attendee information is unavailable!");
        }
        Iterator<Expense> expIter = checkedExpenses.iterator();
        flurryHowManyAddedCount = 0;
        flurryHasCreditCard = false;
        flurryHasReceipt = false;
        boolean isEreceipt = false;

        while (expIter.hasNext()) {
            Expense exp = expIter.next();
            Double atnAmt = null;
            String atnMeKey = null;
            switch (exp.getExpenseEntryType()) {
                case CASH:
                    // Add the mobile entry key.
                    MobileEntry transctn = exp.getCashTransaction();
                    meKeys.add(transctn.getMeKey());
                    smartExpIds.add(transctn.smartExpenseId);
                    // Add an attendee entry map, if needbe.
                    if (transctn.getExpKey() != null && expenseTypeSupportsDefaultAttendee(transctn.getExpKey())) {
                        atnAmt = transctn.getTransactionAmount();
                        atnMeKey = transctn.getMeKey();
                    }
                    ++flurryHowManyAddedCount;
                    if (!flurryHasReceipt) {
                        if (transctn != null && transctn.getReceiptImageId() != null) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;
                case PERSONAL_CARD:
                    // Add the personal card transaction key.
                    PersonalCardTransaction personalTrans = exp.getPersonalCardTransaction();
                    pctKeys.add(personalTrans.pctKey);
                    smartExpIds.add(personalTrans.smartExpenseId);
                    // Add an attendee map, if needbe.
                    if (personalTrans.mobileEntry != null) {
                        if (personalTrans.mobileEntry.getExpKey() != null
                                && expenseTypeSupportsDefaultAttendee(personalTrans.mobileEntry.getExpKey())) {
                            if (personalTrans.mobileEntry.getMeKey() != null
                                    && personalTrans.mobileEntry.getTransactionAmount() != null) {
                                atnAmt = personalTrans.mobileEntry.getTransactionAmount();
                                atnMeKey = personalTrans.mobileEntry.getMeKey();
                            }
                        }
                    }
                    ++flurryHowManyAddedCount;
                    flurryHasCreditCard = true;
                    if (!flurryHasReceipt) {
                        if (personalTrans != null && personalTrans.mobileEntry != null
                                && personalTrans.mobileEntry.getReceiptImageId() != null) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;
                case CORPORATE_CARD:
                    // Add the corporate card transaction key.
                    CorporateCardTransaction ccTrans = exp.getCorporateCardTransaction();
                    MobileEntry ccMobEntry = ccTrans.getMobileEntry();
                    cctKeys.add(ccTrans.getCctKey());
                    smartExpIds.add(ccTrans.smartExpenseId);
                    // Add an attendee map, if needbe.
                    if (ccMobEntry != null) {
                        if (ccMobEntry.getExpKey() != null && expenseTypeSupportsDefaultAttendee(ccMobEntry.getExpKey())) {
                            if (ccMobEntry.getMeKey() != null && ccMobEntry.getTransactionAmount() != null) {
                                atnAmt = ccMobEntry.getTransactionAmount();
                                atnMeKey = ccMobEntry.getMeKey();
                            }
                        }
                    }
                    ++flurryHowManyAddedCount;
                    flurryHasCreditCard = true;
                    if (!flurryHasReceipt) {
                        if (ccTrans != null && ccMobEntry != null && ccMobEntry.getReceiptImageId() != null) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;
                case SMART_CORPORATE:
                    smartCorpExpenses.add(exp);
                    // Add an attendee map, if needbe.
                    CorporateCardTransaction smartCCTrans = exp.getCorporateCardTransaction();
                    MobileEntry smartMobEntry = smartCCTrans.getMobileEntry();
                    MobileEntry cashTrans = exp.getCashTransaction();
                    smartExpIds.add(smartCCTrans.smartExpenseId);
                    if (smartMobEntry != null) {
                        if (smartMobEntry.getExpKey() != null
                                && expenseTypeSupportsDefaultAttendee(smartMobEntry.getExpKey())) {
                            if (smartMobEntry.getMeKey() != null && smartMobEntry.getTransactionAmount() != null) {
                                atnAmt = smartMobEntry.getTransactionAmount();
                                atnMeKey = smartMobEntry.getMeKey();
                            }
                        }
                    } else if (cashTrans != null) {
                        if (cashTrans.getExpKey() != null && expenseTypeSupportsDefaultAttendee(cashTrans.getExpKey())) {
                            atnAmt = cashTrans.getTransactionAmount();
                            atnMeKey = cashTrans.getMeKey();
                        }
                    }
                    ++flurryHowManyAddedCount;
                    flurryHasCreditCard = true;
                    if (!flurryHasReceipt) {
                        if ((smartCCTrans != null && smartMobEntry != null && smartMobEntry.getReceiptImageId() != null)
                                || (cashTrans != null && cashTrans.getReceiptImageId() != null)) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;
                case SMART_PERSONAL:
                    smartPersExpenses.add(exp);
                    // Add an attendee map, if needbe.
                    MobileEntry smartPersonalMobEntry = exp.getPersonalCardTransaction().mobileEntry;
                    MobileEntry smartCashTrans = exp.getCashTransaction();
                    smartExpIds.add(smartPersonalMobEntry.smartExpenseId);
                    if (smartPersonalMobEntry != null) {
                        if (smartPersonalMobEntry.getExpKey() != null
                                && expenseTypeSupportsDefaultAttendee(smartPersonalMobEntry.getExpKey())) {
                            if (smartPersonalMobEntry.getMeKey() != null
                                    && smartPersonalMobEntry.getTransactionAmount() != null) {
                                atnAmt = smartPersonalMobEntry.getTransactionAmount();
                                atnMeKey = smartPersonalMobEntry.getMeKey();
                            }
                        }
                    } else if (smartCashTrans != null) {
                        if (smartCashTrans.getExpKey() != null
                                && expenseTypeSupportsDefaultAttendee(smartCashTrans.getExpKey())) {
                            atnAmt = smartCashTrans.getTransactionAmount();
                            atnMeKey = smartCashTrans.getMeKey();
                        }
                    }
                    ++flurryHowManyAddedCount;
                    flurryHasCreditCard = true;
                    if (!flurryHasReceipt) {
                        if ((exp.getPersonalCardTransaction() != null && smartPersonalMobEntry != null && smartPersonalMobEntry
                                .getReceiptImageId() != null)
                                || (smartCashTrans != null && smartCashTrans.getReceiptImageId() != null)) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;
                case RECEIPT_CAPTURE:
                    // Add the corporate card transaction key.
                    smartExpIds.add(exp.getReceiptCapture().smartExpenseId);
                    ++flurryHowManyAddedCount;
                    flurryHasCreditCard = false;
                    if (!flurryHasReceipt) {
                        if (exp.getReceiptCapture() != null && exp.getReceiptCapture().receiptImageId != null
                                && exp.getReceiptCapture().receiptImageId.length() > 0) {
                            flurryHasReceipt = true;
                        }
                    }
                    break;

                case E_RECEIPT:
                    // Add the mobile entry key.
                    EReceipt eReceipt = exp.getEReceipt();
                    smartExpIds.add(eReceipt.smartExpenseId);
                    // Add an attendee entry map, if needbe.
                    if (eReceipt.getExpKey() != null && expenseTypeSupportsDefaultAttendee(eReceipt.getExpKey())) {
                        atnAmt = eReceipt.getTransactionAmount();
                        atnMeKey = eReceipt.getEReceiptId();
                    }

                    isEreceipt = true;
                    ++flurryHowManyAddedCount;
                    if (!flurryHasReceipt) {
                        if (eReceipt != null && eReceipt.getEReceiptImageId() != null) {
                            flurryHasReceipt = true;
                        }
                    }

                    break;
            }
            // Add to the AttendeeEntryMap.
            if (atnAmt != null && atnMeKey != null && defAtt != null) {
                AttendeesEntryMap attEntMap = new AttendeesEntryMap();
                attEntMap.attendees = new ArrayList<ExpenseReportAttendee>();
                ExpenseReportAttendee att = new ExpenseReportAttendee(defAtt);
                att.amount = atnAmt;
                att.instanceCount = 1;
                att.isAmountEdited = false;
                attEntMap.attendees.add(att);
                attEntMap.meKey = atnMeKey;
                attendeesEntryMaps.add(attEntMap);
            }
            expIter.remove();
        }

        // Fire off the server request
        addToReportRequest = app.getService().addToReport(reportKey, reportName, meKeys, pctKeys, cctKeys,
                smartCorpExpenses, smartPersExpenses, attendeesEntryMaps, smartExpIds);

        toggleButtonBar();

        // Display the progress dialog.
        AddExpenseProgressDialogHandler.show(this, flurryHowManyAddedCount);

        // Track Ereceipt event.
        if (isEreceipt) {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, "Add To Report", "E-Receipt");
        }
    }

    /**
     * Gets the default attendee list.
     *
     * @return the default attendee list.
     */
    private ExpenseReportAttendee getDefaultAttendee() {
        IExpenseReportCache expRepCache = app.getExpenseActiveCache();
        return expRepCache.getDefaultAttendee();
    }

    /**
     * Gets whether or not an expense key supports attendees and whether the default attendee should be added.
     *
     * @param expKey the expense key to examine.
     * @return whether or not <code>expKey</cdoe> supports attendees and whether the default
     * attendee should be added.
     */
    private boolean expenseTypeSupportsDefaultAttendee(String expKey) {
        boolean defAttSupported = false;
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        List<ExpenseType> expTypes = expEntCache.getExpenseTypes();
        if (expTypes != null) {
            for (ExpenseType expType : expTypes) {
                if (expKey != null && expType.key != null && expKey.equalsIgnoreCase(expType.key)) {
                    if (expType.supportsAttendees != null && expType.supportsAttendees == true
                            && expType.userAsAtnDefault != null && expType.userAsAtnDefault == true) {
                        defAttSupported = true;
                        break;
                    }
                }
            }
        }
        return defAttSupported;
    }

    /**
     * Gets a list of list items to be set on the list item adapter.
     *
     * @return the set of list items.
     */
    private List<ListItem> getListItems() {
        List<ListItem> listItems = null;
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        ArrayList<Expense> cacheList = expEntCache.getExpenseEntries();

        if (cacheList != null) {
            listItems = populateExpenseListItems(cacheList, pcaKeyFilter, showCorpCardTransOnly);
        }
        if (listItems == null || listItems.size() == 0) {
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
        }
        return listItems;
    }

    private void mergeExpenseItItems(List<Expense> expenses) {

        if (Preferences.isExpenseItUser()) {

            // Query the DB for the list of ExpenseIt items.
            ContentResolver resolver = activity.getContentResolver();
            Cursor cursor = null;
            try {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(com.concur.mobile.platform.expense.provider.Expense.ExpenseItReceiptColumns.USER_ID);
                strBldr.append(" = ?");
                String where = strBldr.toString();
                String[] whereArgs = {activity.getUserId()};

                cursor = resolver.query(com.concur.mobile.platform.expense.provider.Expense.ExpenseItReceiptColumns.CONTENT_URI,
                        null, where, whereArgs,
                        com.concur.mobile.platform.expense.provider.Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        ExpenseItReceipt expIt = new ExpenseItReceipt(activity, cursor);
                        //While the expenseIt item is being exported to Concur. We get Eta=0 and the Processing status is processed.
                        //This is a temporary stage until that item which succeeded OCRing is moved.
                        //However, in the UI we want to show the item as still analyzing until this export process has finished.
                        if (expIt.getParsingStatusCode() == ExpenseItParseCode.PARSED.value() &&
                            expIt.getEta() == 0 &&
                            expIt.getErrorCode() == ErrorResponse.ERROR_CODE_NO_ERROR) {
                            expIt.setParsingStatusCode(ExpenseItParseCode.UNPARSED.value());
                            expIt.setEta(30 /*secs*/);
                        }
                        expenses.add(new Expense(expIt));
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

    }

    public ListItemAdapter<ListItem> getListItemAdapter() {
        return listItemAdapter;
    }

    /**
     * Will refresh the expense list.
     */
    private void refreshExpenseList() {
        List<ListItem> listItems = getListItems();
        if (listItems != null) {
            listItemAdapter.setItems(listItems);
            listItemAdapter.notifyDataSetChanged();
        }
    }

    public void showLoadingView() {

        // TODO: getView() spits back null if we rotate with the alert dialog from onDelete()
        // displaying. If we try to say frag.getView() it comes back with a view, but doesn't
        // display "Updating Expenses" so as of now, if the alert dialog shows in onDelete() we
        // just don't show the updating expenses screen.
        if (getView() != null) {
            ViewUtil.setTextViewText(getView(), R.id.loading_data, R.id.data_loading_text,
                    getText(getDataLoadingTextResourceId()).toString(), true);
        }
        // Set the view state to indicate data being loaded.
        viewState = ViewState.LOCAL_DATA_REFRESH;
        flipViewForViewState();
    }

    /**
     * Populate the list of displayed expenses with the whole or filtered list of all expenses
     *
     * @param cacheList
     * @param pcaKey
     */
    @SuppressWarnings("unchecked")
    private List<ListItem> populateExpenseListItems(ArrayList<Expense> cacheList, String pcaKey,
                                                    boolean corpCardTransOnly) {
        List<ListItem> listItems = null;
        List<Expense> expenses = null;
        if (pcaKey != null) {
            // Filter the list down to only include the specified card charges
            int size = cacheList.size();
            expenses = new ArrayList<Expense>(size);
            for (int i = 0; i < size; i++) {
                Expense e = cacheList.get(i);
                if (ExpenseEntryType.PERSONAL_CARD == e.getExpenseEntryType()
                        || ExpenseEntryType.SMART_PERSONAL == e.getExpenseEntryType()) {
                    if (pcaKey.equals(e.getPersonalCard().pcaKey)) {
                        expenses.add(e);
                    }
                }
            }
        } else if (corpCardTransOnly) {
            // Filter the list down so that only corp card charges will be
            // shown.
            int size = cacheList.size();
            expenses = new ArrayList<Expense>(size);
            for (int i = 0; i < size; i++) {
                Expense e = cacheList.get(i);
                if (ExpenseEntryType.CORPORATE_CARD == e.getExpenseEntryType()
                        || ExpenseEntryType.SMART_CORPORATE == e.getExpenseEntryType()) {
                    expenses.add(e);
                }
            }
        } else {
            expenses = (ArrayList<Expense>) cacheList.clone();
        }

        // Add ExpenseIt items.
        mergeExpenseItItems(expenses);

        // MOB-15855
        // With some of the ExpenseIt items, ExpenseComparator seems to be violating the Comparison contract. This will be fixed
        // in 9.8 but the try/catch prevents a crash in the meantime.
        try {
            String sortOrder = PreferenceUtil.getStringPreference(getBaseActivity(), PREF_EXPENSE_LIST_SORT_ORDER,
                    com.concur.mobile.platform.expense.provider.Expense.SmartExpenseColumns.DATE_NEWEST_SORT_ORDER);
            Collections.sort(expenses, new ExpenseComparator(sortOrder));
        } catch (IllegalArgumentException e) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + "populateExpenseListItems Collections.sort violates Comparison general contract");
        }

        // Ensure any previously checked expenses that are still present in our
        // new data
        // appear selected.
        if (checkedExpenses != null) {
            reselectCheckedExpenses(expenses);
        }
        // Ensure we clear out the expense to button map.
        if (expenseButtonMap != null) {
            expenseButtonMap.clear();
        }

        // Google Analytics counts.
        long totalCount = 0L;
        long eReceiptCount = 0L;
        long smartMatchedCount = 0L;
        long smartMatchedEreceiptCount = 0L;
        long cctCount = 0L;

        // Iterate through the expenses and create the appropriate ListItem
        // objects.
        if (expenses != null) {
            totalCount = expenses.size();
            listItems = new ArrayList<ListItem>(expenses.size());
            int curYear = -1;
            int curMonth = -1;
            int flurryCardCount = 0;
            int flurryMobileEntryCount = 0;
            int flurryReceiptCount = 0;
            for (Expense expense : expenses) {
                switch (expense.getExpenseEntryType()) {
                    case CASH: {
                        // Offline expenses do not display
                        if (!MobileEntryStatus.NEW.equals(expense.getCashTransaction().getStatus())) {
                            CashExpenseListItem listItem = new CashExpenseListItem(expense, expenseButtonMap,
                                    checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                            Calendar transDate = listItem.getTransactionDate();
                            if (curYear == -1 || curYear != transDate.get(Calendar.YEAR)
                                    || curMonth != transDate.get(Calendar.MONTH)) {
                                curYear = transDate.get(Calendar.YEAR);
                                curMonth = transDate.get(Calendar.MONTH);
                                String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                                listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                            }
                            listItems.add(listItem);
                            ++flurryMobileEntryCount;
                            if (listItem.showReceipt()) {
                                ++flurryReceiptCount;
                            }
                        }
                        break;
                    }
                    case CORPORATE_CARD: {
                        CorporateCardExpenseListItem listItem = new CorporateCardExpenseListItem(expense, expenseButtonMap,
                                checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if (curYear == -1 || curYear != transDate.get(Calendar.YEAR)
                                || curMonth != transDate.get(Calendar.MONTH)) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        ++flurryCardCount;
                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }

                        // GA tracking
                        cctCount++;
                        break;
                    }
                    case PERSONAL_CARD: {
                        PersonalCardExpenseListItem listItem = new PersonalCardExpenseListItem(expense, expenseButtonMap,
                                checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if (curYear == -1 || curYear != transDate.get(Calendar.YEAR)
                                || curMonth != transDate.get(Calendar.MONTH)) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        ++flurryCardCount;
                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }
                        break;
                    }
                    case SMART_CORPORATE: {
                        SmartCorporateExpenseListItem listItem = new SmartCorporateExpenseListItem(expense,
                                expenseButtonMap, checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if (curYear == -1 || curYear != transDate.get(Calendar.YEAR)
                                || curMonth != transDate.get(Calendar.MONTH)) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        ++flurryCardCount;
                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }

                        // GA tracking
                        smartMatchedCount++;
                        break;
                    }
                    case SMART_PERSONAL: {
                        SmartPersonalExpenseListItem listItem = new SmartPersonalExpenseListItem(expense, expenseButtonMap,
                                checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if (curYear == -1 || curYear != transDate.get(Calendar.YEAR)
                                || curMonth != transDate.get(Calendar.MONTH)) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        ++flurryCardCount;
                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }

                        // GA tracking
                        smartMatchedCount++;
                        break;
                    }
                    case RECEIPT_CAPTURE: {
                        ReceiptCaptureListItem listItem = new ReceiptCaptureListItem(expense, expenseButtonMap,
                                checkedExpenses, onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if ((transDate != null)
                                && (curYear == -1 || curYear != transDate.get(Calendar.YEAR) || curMonth != transDate
                                .get(Calendar.MONTH))) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        ++flurryCardCount;
                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }
                        break;
                    }
                    case E_RECEIPT: {
                        EReceiptListItem listItem = new EReceiptListItem(expense, expenseButtonMap, checkedExpenses,
                                onCheckChange, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if ((transDate != null)
                                && (curYear == -1 || curYear != transDate.get(Calendar.YEAR) || curMonth != transDate
                                .get(Calendar.MONTH))) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);

                        if (listItem.showReceipt()) {
                            ++flurryReceiptCount;
                        }

                        // GA tracking
                        eReceiptCount++;
                        if (expense.isSmartMatched()) {
                            smartMatchedEreceiptCount++;
                        }
                        break;
                    }
                    case OCR_NOT_DONE: {

                        OcrListItem listItem = new OcrListItem(expense, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if ((transDate != null)
                                && (curYear == -1 || curYear != transDate.get(Calendar.YEAR) || curMonth != transDate
                                .get(Calendar.MONTH))) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        break;
                    }
                    case EXPENSEIT_NOT_DONE: {
                        ExpenseItListItem listItem = new ExpenseItListItem(expense, EXPENSE_VIEW_TYPE);
                        Calendar transDate = listItem.getTransactionDate();
                        if ((transDate != null)
                                && (curYear == -1 || curYear != transDate.get(Calendar.YEAR) || curMonth != transDate
                                .get(Calendar.MONTH))) {
                            curYear = transDate.get(Calendar.YEAR);
                            curMonth = transDate.get(Calendar.MONTH);
                            String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(transDate.getTime());
                            listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                        }
                        listItems.add(listItem);
                        break;
                    }
                }

                // Flurry Notification.
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_CARD_COUNT, Integer.toString(flurryCardCount));
                params.put(Flurry.PARAM_NAME_MOBILE_ENTRY_COUNT, Integer.toString(flurryMobileEntryCount));
                params.put(Flurry.PARAM_NAME_RECEIPT_COUNT, Integer.toString(flurryReceiptCount));
                EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_LIST, params);

                // MOB-21114 Google Analytics tracking.
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_EXPENSE_LIST,
                        Flurry.LABEL_ALL_EXPENSES, totalCount);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_EXPENSE_LIST,
                        Flurry.PARAM_VALUE_E_RECEIPT, eReceiptCount);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_EXPENSE_LIST,
                        Flurry.LABEL_SMARTMATCHED_EXPENSE, smartMatchedCount);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_EXPENSE_LIST,
                        Flurry.LABEL_SMARTMATCHED_EXPENSE_ERECEIPT, smartMatchedEreceiptCount);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_EXPENSE_LIST,
                        Flurry.LABEL_CORPORATE_CARD_EXPENSE, cctCount);

            }
        }
        return listItems;
    }

    /**
     * Upon an update in the list of expenses, newly parsed data can actually represent the same expense, but with two different
     * instances of <code>Expense</code> objects. So, this method will iterate over the old set of selected expenses and ensure
     * any new expense object representing the same data is placed within a new hash set of checked expenses becoming the official
     * set.
     */

    private void reselectCheckedExpenses(List<Expense> expenses) {
        Iterator<Expense> ckExpIter = checkedExpenses.iterator();
        HashSet<Expense> newCheckedExpenses = new HashSet<Expense>();
        while (ckExpIter.hasNext()) {
            Expense ckExp = ckExpIter.next();
            Iterator<Expense> expIter = expenses.iterator();
            while (expIter.hasNext()) {
                Expense listExp = expIter.next();
                // Check for expense type equality.
                if (ckExp.getExpenseEntryType().equals(listExp.getExpenseEntryType())) {
                    switch (ckExp.getExpenseEntryType()) {
                        case CASH: {
                            // Do the expense keys match?
                            MobileEntry cashTrans = ckExp.getCashTransaction();
                            if (cashTrans.getMeKey().equalsIgnoreCase(listExp.getCashTransaction().getMeKey())) {
                                newCheckedExpenses.add(listExp);
                            }
                            break;
                        }
                        case PERSONAL_CARD: {
                            // Do the personal card accounts match?
                            if (ckExp.getPersonalCard().pcaKey.equalsIgnoreCase(listExp.getPersonalCard().pcaKey)) {
                                // Do the card transactions match?
                                if (ckExp.getPersonalCardTransaction().pctKey.equalsIgnoreCase(listExp
                                        .getPersonalCardTransaction().pctKey)) {
                                    newCheckedExpenses.add(listExp);
                                }
                            }
                            break;
                        }
                        case CORPORATE_CARD: {
                            // Do the corporate card transaction keys match?
                            if (ckExp.getCorporateCardTransaction().getCctKey()
                                    .equalsIgnoreCase(listExp.getCorporateCardTransaction().getCctKey())) {
                                newCheckedExpenses.add(listExp);
                            }
                            break;
                        }
                        case SMART_CORPORATE: {
                            // Do the corporate card transaction keys match?
                            if (ckExp.getCorporateCardTransaction().getCctKey()
                                    .equalsIgnoreCase(listExp.getCorporateCardTransaction().getCctKey())) {
                                newCheckedExpenses.add(listExp);
                            }
                            break;
                        }
                        case SMART_PERSONAL: {
                            // Do the personal card accounts match?
                            if (ckExp.getPersonalCard().pcaKey.equalsIgnoreCase(listExp.getPersonalCard().pcaKey)) {
                                // Do the card transactions match?
                                if (ckExp.getPersonalCardTransaction().pctKey.equalsIgnoreCase(listExp
                                        .getPersonalCardTransaction().pctKey)) {
                                    newCheckedExpenses.add(listExp);
                                }
                            }
                            break;
                        }
                        case RECEIPT_CAPTURE: {
                            if (ckExp.getReceiptCapture().rcKey.equalsIgnoreCase(listExp.getReceiptCapture().rcKey)) {
                                newCheckedExpenses.add(listExp);
                            }
                            break;
                        }
                        case E_RECEIPT: {
                            if (ckExp.getEReceipt().getEReceiptId().equalsIgnoreCase(listExp.getEReceipt().getEReceiptId())) {
                                newCheckedExpenses.add(listExp);
                            }
                        }
                    }
                }
            }
        }
        // Reset the the list of checked expenses based on the above.
        checkedExpenses = newCheckedExpenses;
        // Toggle the bar.
        toggleButtonBar();
    }

    private void getSmartExpenses(boolean refreshExpenseItList) {

        if (Preferences.isExpenseItUser() && refreshExpenseItList) {

            showLoadingView();
            expensesCallback.doGetExpenseItList();

            return;
        }

        expensesCallback.doGetSmartExpenseList();
    }

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> to handle adding expenses to a report.
     */
    class SelectReportDialogClickListener implements DialogInterface.OnClickListener {

        @Override
        public void onClick(DialogInterface dialog, int which) {

            // Dismiss the dialog.
            dialog.dismiss();

            // When we're displaying the Active Reports List for the user to attach expense(s) to, we want to make sure that the
            // list adapter inside of the SelectReportDialogClickListener is the same adapter used in the dialog fragment.
            Fragment reportListDialogFrag = getFragmentManager().findFragmentByTag(ACTIVE_REPORTS_LIST_TAG);
            if (reportListDialogFrag instanceof ActiveReportsListDialogFragment) {
                activeReportListAdapter = ((ActiveReportsListDialogFragment) reportListDialogFrag)
                        .getActiveReportsListAdapter();
                if (!activeReportListAdapter.isNewOptionSelected(which)) {
                    // An existing report was selected, just start the "add" process.
                    ExpenseReport report = (ExpenseReport) activeReportListAdapter.getItem(which);
                    handleAddToReport(report.reportKey, null);
                } else {
                    // Invoke the ExpenseHeader class and specify that source as "New".
                    Intent intent = new Intent(activity, ExpenseReportHeader.class);
                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_NEW);
                    Expenses.this.startActivityForResult(intent, Const.CREATE_NEW_REPORT);
                }
            } else {
                Log.e(FRAGMENT_TAG, "ActiveReportsListDialog does not exist!");
            }
        }
    }

    /**
     * An implementation of <code>CompountButton.onCheckedChangeListener</code> to control showing/hiding of the button bar and
     * maintaining a set of selected expenses.
     */
    class OnCheckChange implements CompoundButton.OnCheckedChangeListener {

        private final String CLS_TAG = Expenses.CLS_TAG + "." + OnCheckChange.class.getSimpleName();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.CompoundButton.OnCheckedChangeListener#onCheckedChanged (android.widget.CompoundButton, boolean)
         */
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            // Find the expense entry in the map whose value is 'buttonView'.
            Expense exp = null;
            Iterator<Expense> expIter = expenseButtonMap.keySet().iterator();
            while (expIter.hasNext()) {
                exp = expIter.next();
                CompoundButton cmpBut = expenseButtonMap.get(exp);
                if (cmpBut == buttonView) {
                    break;
                }
                exp = null;
            }
            if (exp != null) {
                if (isChecked) {
                    if (!checkedExpenses.add(exp)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChange: expense already in checked set!");
                    }
                } else {
                    if (!checkedExpenses.remove(exp)) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChanged: expense not in checked set!");
                    }
                }
                // Handle potentially toggling the button bar.
                toggleButtonBar();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onCheckedChanged: expense not in button/expense map!");
            }
        }
    }

    // /**
    // * An extension of <code>BroadcastReceiver</code> for handling messages related to data updates.
    // *
    // * @author AndrewK
    // */
    // class DataUpdateReceiver extends BroadcastReceiver {
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
    // */
    // @Override
    // public void onReceive(Context context, Intent intent) {
    // if (isDataUpdateRequired()) {
    // onDataUpdate(context, intent);
    // }
    // }
    // }

    static class AddExpenseProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "AddExpenseProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag, int quantity) {
            // Construct the callback handler.
            AddExpenseProgressDialogHandler dlgHndlr = new AddExpenseProgressDialogHandler();
            // Construct and show the dialog fragment based on whether or not
            // Smart Matching is enabled.
            String message = ConcurCore.getContext().getResources()
                    .getQuantityString(R.plurals.dlg_expense_add_to_report, quantity);

            ProgressDialogFragment dlgFrag = DialogFragmentFactory.getProgressDialog(message, true, true, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof Expenses) {
                Expenses exFrag = (Expenses) frag;
                if (exFrag.addToReportRequest != null) {
                    exFrag.addToReportRequest.cancel();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".AddExpenseProgressDialogHandler.onCancel: addToReportRequest is null!");
                }
            }
        }
    }

    static class DeleteExpenseProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "DeleteExpenseProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag, int quantity) {
            // Construct the callback handler.
            DeleteExpenseProgressDialogHandler dlgHndlr = new DeleteExpenseProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = ConcurCore.getContext().getResources()
                    .getQuantityString(R.plurals.dlg_expense_delete_fragment, quantity);
            ProgressDialogFragment dlgFrag = DialogFragmentFactory.getProgressDialog(message, true, true, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof Expenses) {
                Expenses exFrag = (Expenses) frag;
                if (exFrag.mobileEntryDeleteRequest != null) {
                    exFrag.mobileEntryDeleteRequest.cancel();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".DeleteExpenseProgressDialogHandler.onCancel: mobileEntryDeleteRequest is null!");
                }
            }
        }
    }

}
