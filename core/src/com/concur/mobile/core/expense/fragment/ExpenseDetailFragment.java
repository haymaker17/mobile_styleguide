package com.concur.mobile.core.expense.fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.charge.activity.CurrencySpinnerAdapter;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.fragment.ExpenseTypeDialogFragment.OnExpenseTypeSelectionListener;
import com.concur.mobile.core.expense.report.service.AppendReceiptImageRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.MrudataCollector;
import com.concur.mobile.platform.authentication.SessionInfo;
import com.concur.mobile.platform.config.provider.ConfigUtil;
import com.concur.mobile.platform.expense.list.CorporateCardTransaction;
import com.concur.mobile.platform.expense.list.ExpenseTypeEnum;
import com.concur.mobile.platform.expense.list.MobileEntry;
import com.concur.mobile.platform.expense.list.PersonalCardTransaction;
import com.concur.mobile.platform.expense.list.ReceiptCapture;
import com.concur.mobile.platform.expense.list.SaveMobileEntryRequestTask;
import com.concur.mobile.platform.expense.list.dao.CorporateCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ExpenseListDAO;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.ReceiptCaptureDAO;
import com.concur.mobile.platform.expense.receipt.list.GetReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.Receipt;
import com.concur.mobile.platform.expense.receipt.list.SaveReceiptRequestTask;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptListDAO;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragment;
import com.concur.mobile.platform.ui.common.util.FormatUtil;
import com.concur.mobile.platform.ui.common.util.ImageUtil;
import com.concur.mobile.platform.ui.common.util.ViewUtil;
import com.concur.mobile.platform.ui.common.util.ViewUtil.LocationSelection;
import com.concur.mobile.platform.ui.common.widget.CalendarPicker;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialog;
import com.concur.mobile.platform.ui.common.widget.CalendarPickerDialog.OnDateSetListener;
import com.concur.mobile.platform.ui.expense.util.ExpensePreferenceUtil;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * 
 * @author yiwenw
 * 
 *         - setFieldNames() replaced by individual setupXXXField() calls. - configureExpenseTypes() no longer needed
 */
public class ExpenseDetailFragment extends PlatformFragment implements OnExpenseTypeSelectionListener {

    /**
     * An interface containing various callbacks which this Fragment's parent Activities must implement or else a
     * <code>ClassCastException</code> is thrown.
     * 
     * @author Chris N. Diaz
     */
    public interface ExpenseDetailCallbacks {

        /**
         * "Offline" tracking/logging reason.
         */
        public final static String FAILURE_REASON_OFFLINE = "Offline";

        /**
         * Invoked when the MobileEntrySave request has succeeded.
         * 
         * @param resultData
         *            the data containing results from the successful request.
         */
        public void onMobileEntrySaveRequestSuccess(Bundle resultData);

        /**
         * Invoked when the MobileEntrySave request has failed.
         * 
         * @param resultData
         *            the data containing results from the failure.
         */
        public void onMobileEntrySaveRequestFail(Bundle resultData);

        /**
         * Invoked to log/track a failure during the EmailLookup request.
         * 
         * @param failureType
         *            string indicating the reason for failing the Email Lookup. For example: <code>FAILURE_REASON_FORMAT</code>
         *            or <code>FAILURE_REASON_OFFLINE</code>.
         */
        public void trackMobileEntrySaveFailure(String failureType);
    }

    private static final String CLS_NAME = ExpenseDetailFragment.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = ExpenseDetailFragment.class.getSimpleName()
            + ".calendar.dialog.fragment";

    private static final int DIALOG_DATE_PICKER = 0;

    private static final int DIALOG_RECEIPT_IMAGE = 1;

    private static final int DIALOG_EXPENSE_TYPE = 3;

    private static final int DIALOG_SAVE_RECEIPT = 5;

    private static final int DIALOG_SAVE_EXPENSE = 6;

    private static final int REQUEST_TAKE_PICTURE = 0;

    private static final int REQUEST_CHOOSE_IMAGE = 1;

    private static final int REQUEST_CHOOSE_CLOUD_IMAGE = 2;

    private static final String TAG_DIALOG_SAVE_EXPENSE = "TAG_DIALOG_SAVE_EXPENSE";

    private static final String EXPENSE_DETAIL_RECEIVER = "expense.detail.request.receiver";

    private boolean progressbarVisible;

    // Constants for instance state bundle
    private static final String PROGRESSBAR_VISIBLE = "PROGRESSBAR_VISIBLE";

    private BaseAsyncResultReceiver expenseDetailReceiver;

    /**
     * NOTE: Currently, the receipt image ID coming back from the Connect API is not protected (if at all) in the same way the MWS
     * protects. Using two different protection methods will result in failures between MWS and Connect. Until that is remedied,
     * use of Connect for Quick Expense save receipt will not be used.
     */
    protected static final boolean ADD_RECEIPT_VIA_CONNECT_ENABLED = Boolean.FALSE;

    // Key for the date value.
    private static final String DATE_KEY = "expense.date";

    private static final String AMOUNT_KEY = "expense.amount";

    private static final String VENDOR_KEY = "expense.vendor";

    private static final String LOCATION_KEY = "expense.location";

    private static final String COMMENT_KEY = "expense.comment";

    private static final String EXPENSE_TYPE_KEY = "expense.type";
    private static final String EXPENSE_TYPE_NAME = "expense.type.name";

    private static final String CURRENCY_KEY = "expense.currency";

    private static final String RECEIPT_IMAGE_ID_KEY = "receipt.image.id";

    private static final String RECEIPT_IMAGE_FILE_PATH_KEY = "expense.receipt.image.file.path";

    private static final String DELETE_RECEIPT_IMAGE_FILE_PATH = "expense.delete.receipt.image.file.path";

    private static final String RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY = "expense.receipt.camera.image.file.path";

    private static final String LAST_RECEIPT_ACTION_KEY = "expense.last.receipt.action";

    private static final String EXTRA_SAVE_EXPENSE_RECEIVER_KEY = "expense.save.receiver";
    private static final String EXTRA_GET_RECEIPT_RECEIVER_KEY = "receipt.get.receiver";

    private static final String EXTRA_SAVE_RECEIPT_RECEIVER_KEY = "receipt.save.receiver";

    // Contains the key used to store/retrieve the append receipt receiver.
    private static final String APPEND_RECEIPT_RECEIVER_KEY = "append.receipt.receiver";

    private static final String LOCATION_SELECTION_LIKEY_KEY = "location.selection.likey";
    private static final String LOCATION_SELECTION_LICODE_KEY = "location.selection.licode";
    private static final String LOCATION_SELECTION_VALUE_KEY = "location.selection.value";

    private static final String PREVIOUS_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH_KEY = "previous.receipt.image.data.local.file.path";
    private static final String PREVIOUS_RECEIPT_IMAGE_ID_KEY = "previous.receipt.image.id";
    private static final String SAVING_FOR_APPEND_KEY = "saving.for.append";
    private static final String PREVIOUS_RECEIPT_IMAGE_ID_FROM_MOBILE_ENTRY = "previous.receipt.image.id.from.mobile.entry";
    // Contains whether path is available or not.
    private boolean isPathAvailable = false;
    private static final String IS_PATH_AVAILABLE_KEY_QE = "is.path.available.in.qe";

    private static final String DIALOG_LAST_CHANGED_TEXT_KEY = "dialog.last.changed";

    /**
     * Contains whether a call to 'saveInstanceState' has been made on this instance of the activity.
     */
    private boolean savedInstanceState = false;

    /**
     * Contains the receipt image id if a receipt picture was chosen from the Receipt Store.
     */
    private String receiptImageId;

    /**
     * Contains the path within the receipt image directory of the image.
     */
    private String receiptImageDataLocalFilePath;

    private String receiptImageDataLocalThumbnailFilePath;

    /**
     * Contains the path provided to the camera activity in which to store a captured image.
     */
    private String receiptCameraImageDataLocalFilePath;

    // Contains whether or not the receipt image file referenced by
    // 'receiptImageDataLocalFilePath'
    // should be deleted post-save.
    private boolean deleteReceiptImageDataLocalFilePath;

    // NEW ATTRIBUTES

    // NOTE: The following variables, 'previousReceiptImageDataLocalFilePath'
    // and 'previousReceiptImageId'
    // are set from 'receiptImageDataLocalFilePath' and 'receiptImageId' at the
    // start of a new
    // receipt capture/selection operation. Their used to hold onto these values
    // to support
    // an append operation.

    /**
     * Contains (at the start of a capture receipt (take picture or choose from gallery)), the current value of
     * 'receiptImageDataLocalFilePath' if it's set.
     */
    private String previousReceiptImageDataLocalFilePath;

    /**
     * Contains the receipt image ID as a result of saving 'previousReceiptImageDataLocalFilePath'. May also contain the previous
     * selected receipt image ID from the Receipt Store when end-user decides to append another image. These values are assigned
     * prior to starting a new capture/selection.
     */
    private String previousReceiptImageId;

    /**
     * Contains whether or not a current save receipt operation is being performed as a pre-cursor to an append operation.
     */
    private boolean savingForAppend;

    /**
     * Contains whether or not the 'previousReceiptImageId' value came from the quick expense.
     */
    private boolean previousReceiptImageIdFromMobileEntry;

    // END NEW ATTRIBUTES

    /**
     * Contains the selected selection option for performing an action on an expense receipt.
     */
    private ReceiptPictureSaveAction lastReceiptAction = ReceiptPictureSaveAction.NO_ACTION;

    // /**
    // * Contains whether a save attempt succeeded.
    // */
    // private boolean saveSucceeded;
    //
    /**
     * Contains the list adapter used to populate the options.
     */
    private ReceiptImageOptionListAdapter receiptActionAdapter;

    /**
     * A reference to the dialog used to select an expense date.
     */
    private CalendarPickerDialog datePickerDlg;

    /**
     * A reference to the date selected via the date picker dialog.
     */
    private Calendar datePickerDate;

    // /**
    // * Contains a reference to the expense type adapter.
    // */
    // private ExpenseTypeSpinnerAdapter expTypeAdapter;

    /**
     * Contains a reference to the currently selected expense type.
     */
    private String selExpKey;
    private String selExpName;

    /**
     * Contains a reference to the currency adapter.
     */
    private CurrencySpinnerAdapter curTypeAdapter;

    /**
     * Contains a reference to the currently selected currency type.
     */
    private ListItem selCurType;

    //
    // Yiwen: We don't need this one. It should be included in ExpenseEntry
    // /**
    // * Contains a reference to a mobile entry associated with the expense entry.
    // */
    // private MobileEntry mobileEntry;

    /**
     * ExpenseEntry is a local private class created to manage four types of expense objects this fragment needs to deal with This
     * This fragment is given expenseType (CASH, RC, CCT or PCT) and expenseId. An ExpenseEntry object is constructed from the
     * tuple, which will manage any combination of four types of expense objects.
     */
    private ExpenseEntry expenseEntry;

    /**
     * Contains an outstanding request to get the receipt.
     */
    private GetReceiptRequestTask getReceiptRequest;
    /**
     * Contains a receiver to handle the result of getting a receipt.
     */
    private BaseAsyncResultReceiver getReceiptReceiver;
    /**
     * Contains a listener to handle the result of getting a receipt.
     */
    private AsyncReplyListener getReceiptReplyListener;

    // private GetReceiptDownloadListener getReceiptDownloadListener;

    /**
     * Contains an outstanding request to save an expense.
     */
    private SaveMobileEntryRequestTask saveExpenseRequest;

    /**
     * Contains a receiver to handle the result of saving an expense.
     */
    protected BaseAsyncResultReceiver saveExpenseReceiver;
    /**
     * Contains a listener to handle the result of saving an expense.
     */
    private AsyncReplyListener saveExpenseReplyListener;

    /**
     * Contains an outstanding request to save a receipt.
     */
    protected SaveReceiptRequestTask saveReceiptRequest;
    /**
     * Contains a receiver to handle the result of saving a receipt.
     */
    protected BaseAsyncResultReceiver saveReceiptReceiver;
    /**
     * Contains a listener to handle the result of saving an expense.
     */
    private AsyncReplyListener saveReceiptReplyListener;
    // /**
    // * Contains a filter used to register the save receipt receiver.
    // */
    // protected IntentFilter saveReceiptFilter;

    // /**
    // * Contains the broadcast receiver for handling the result of appending a receipt image to a report entry.
    // */
    // protected AppendReceiptReceiver appendReceiptReceiver;
    //
    /**
     * Contains the filter used to register the append report entry receipt receiver.
     */
    protected IntentFilter appendReceiptFilter;

    /**
     * Contains a reference to an outstanding request to append a receipt to a report entry.
     */
    protected AppendReceiptImageRequest appendReceiptRequest;

    // /**
    // * Contains whether or not the handling of a call from 'onActivityResult' was delayed due to the view not being present.
    // */
    // private boolean activityResultDelay;
    //
    // /**
    // * Contains the request code from the delayed handling of the 'onActivityResult' call.
    // */
    // private int activityResultRequestCode;
    //
    // /**
    // * Contains the result code from the delayed handling of the 'onActivityResult' call.
    // */
    // private int activityResultResultCode;
    //
    // /**
    // * Contains the intent data from the delayed handling of the 'onActivityResult' call.
    // */
    // private Intent activityResultData;
    //
    private EditText textEdit;

    private String lastChangedText;

    private boolean transDateReadOnly;

    private boolean currencyReadOnly;

    private boolean expenseTypeReadOnly;

    private boolean amountReadOnly;

    private boolean locationReadOnly;

    private boolean vendorReadOnly;

    // contains whether we need to view receipt only
    private boolean receiptViewOnly;
    // contains whether we need to save report or not.
    private boolean saveReadOnly;

    // MOB-11314
    private boolean commentReadOnly;

    // Contains the reference last passed to 'onCreate'.
    private Bundle lastSavedInstanceState;

    // Contains the intent used to launch the location search.
    private Intent locationIntent;

    // Contains the location selection list item code.
    private String locLiCode;
    // Contains the location selection list item key.
    private String locLiKey;
    // Contains the location selection value.
    private String locValue;
    // Contains the location currency code selection value.
    private String locCrnCode;
    // Contains the location currency key selection value.
    private String locCrnKey;

    // MRU data collector
    protected MrudataCollector mrudataCollector;

    protected ExpenseDetailCallbacks expenseDetailCallbacks;

    // @Override
    // public void onActivityCreated(Bundle savedInstanceState) {
    // super.onActivityCreated(savedInstanceState);
    // if (savedInstanceState != null) {
    // // Restore the fragment's state here
    // initState(lastSavedInstanceState, getView());
    // }
    // }
    //
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        lastSavedInstanceState = savedInstanceState;

        // We're specifically getting 'now' in the device local timezone because
        // then
        // we just pull the date values out and use them to populate our
        // UTC-standard
        // calendar.
        Calendar now = Calendar.getInstance();
        datePickerDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        datePickerDate.clear();
        datePickerDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);

        // Yiwen : Let the parent activity handle the delay
        // if (isServiceAvailable()) {
        // buildView();
        // initState(lastSavedInstanceState);
        // } else {
        // buildViewDelay = true;
        // }
        //

        // inflate the details fragment
        View view = inflater.inflate(R.layout.expense_detail_fragment, container, false);
        try {
            // Create the expense object per data passed to the activity
            initExpenseEntry();
        } catch (Exception e) {
            // TODO Handle error
            Log.e(Const.LOG_TAG, CLS_NAME + ".onCreateView: Unable to init expense entry", e);

        } finally {
            buildView(view);
        }

        // Enable the progress mask if needed
        if (lastSavedInstanceState != null) {
            progressbarVisible = lastSavedInstanceState.getBoolean(PROGRESSBAR_VISIBLE, false);
            if (progressbarVisible) {
                showProgressBar(view);
            }
        }

        // Restore any receivers.
        restoreReceivers();

        datePickerDlg = (CalendarPickerDialog) getFragmentManager().findFragmentByTag(TAG_CALENDAR_DIALOG_FRAGMENT);
        if (datePickerDlg != null) {
            datePickerDlg.setOnDateSetListener(new DatePickerDialogListener());
        }

        updateUI(view);

        initState(lastSavedInstanceState, view);

        return view;
    }

    private boolean isOCR() {
        return expenseEntry.type == ExpenseTypeEnum.RECEIPT_CAPTURE;
    }

    private boolean isOCRInProcessing() {
        return true;
    }

    private boolean isOCRComplete() {
        return true;
    }

    // Called from onCreateView and when expense changes status
    private void updateUI(View view) {
        if (isOCR()) {
            if (isOCRInProcessing())
                showOCRInProcessing(view);
            else if (isOCRComplete())
                showOCRComplete(view);
            else
                showOCRFailed(view);
        } else
            showExpense(view);

    }

    private void showExpense(View view) {
        // Show all fields
        this.setupCommonDetailFields(view, View.VISIBLE, false);

        this.setupVendorField(view, View.VISIBLE);
        this.setupCommentField(view, View.VISIBLE);
        this.setupAmountField(view, View.VISIBLE);

        // Hide processing status bar
        View ocrProgress = view.findViewById(R.id.ocr_progress);
        ocrProgress.setVisibility(View.GONE);
        View bar = view.findViewById(R.id.group_separator2);
        bar.setVisibility(View.GONE);

        if (this.expenseEntry.expenseId == null) // New entry
        {
            View addToRptBtn = view.findViewById(R.id.add_to_report_button);
            addToRptBtn.setVisibility(View.GONE);
        }
    }

    private void showOCRFailed(View view) {
        this.setupVendorField(view, View.GONE);

        this.setupOCRSentForDateField(view, view.VISIBLE);

        this.setupCommentField(view, View.VISIBLE);

        setupCommonDetailFields(view, View.GONE, true);

        // Update processing status
        ImageView procIcon = (ImageView) view.findViewById(R.id.progress_icon);
        procIcon.setImageResource(R.drawable.expense_icon_cloud_red);

        TextView procStatus = (TextView) view.findViewById(R.id.progress_status);
        procStatus.setText(R.string.ocr_progress_status);

        TextView procMsg = (TextView) view.findViewById(R.id.progress_msg);
        procMsg.setText(R.string.ocr_progress_msg);
    }

    private void setupCommonDetailFields(View view, int viewState, boolean isOCR) {

        this.setupAmountField(view, viewState);

        this.setupCurrencyField(view, viewState);

        this.setupTransactionDateField(view, viewState, isOCR);

        this.setupLocationField(view, viewState);

        this.setupExpenseTypeField(view, View.VISIBLE);

    }

    private void showOCRComplete(View view) {

        this.setupVendorField(view, View.VISIBLE);
        this.setupAmountField(view, View.VISIBLE);
        this.setupCommentField(view, View.VISIBLE);

        this.setupOCRSentForDateField(view, view.GONE);

        setupCommonDetailFields(view, View.VISIBLE, true);

        // Update processing status
        ImageView procIcon = (ImageView) view.findViewById(R.id.progress_icon);
        procIcon.setImageResource(R.drawable.expense_icon_green_checker);

        TextView procStatus = (TextView) view.findViewById(R.id.progress_status);
        procStatus.setText(R.string.ocr_progress_status_complete);

    }

    private void showOCRInProcessing(View view) {

        this.setupVendorField(view, View.GONE);
        this.setupOCRSentForDateField(view, View.VISIBLE);

        this.setupCommentField(view, View.VISIBLE);

        setupCommonDetailFields(view, View.GONE, true);

        View grpSpacer1 = view.findViewById(R.id.expense_group_spacer_1);
        grpSpacer1.setVisibility(View.GONE);
        View fldSep = view.findViewById(R.id.expense_group_2_top_fld_separator);
        fldSep.setVisibility(View.GONE);

        // Update processing status
        ImageView procIcon = (ImageView) view.findViewById(R.id.progress_icon);
        procIcon.setImageResource(R.drawable.expense_icon_cloud_green);

        TextView procStatus = (TextView) view.findViewById(R.id.progress_status);
        procStatus.setText(R.string.ocr_progress_status);

        TextView procMsg = (TextView) view.findViewById(R.id.progress_msg);
        procMsg.setText(R.string.ocr_progress_msg);

    }

    private ExpenseTypeEnum getExpenseEntryType(String expType) {
        ExpenseTypeEnum expEntType = ExpenseTypeEnum.CASH;
        if (expType != null) {
            try {
                ExpenseTypeEnum passedType = ExpenseTypeEnum.valueOf(expType);
                if (passedType != null) {
                    expEntType = passedType;
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".initExpenseEntry: invalid passed expense type of '" + expType
                            + "'...defaulting to cash!");
                }
            } catch (IllegalArgumentException illArgExc) {
                Log.e(Const.LOG_TAG, CLS_NAME + ".initExpenseEntry: invalid passed expense type of '" + expType
                        + "'...defaulting to cash!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_NAME
                    + ".initExpenseEntry: no expense entry type passed in intent...defaulting to cash!");
        }

        return expEntType;
    }

    private ExpenseEntry generateNewCashExpense() {
        MobileEntryDAO mobileEntry = new MobileEntry();
        mobileEntry.setTransactionDate(datePickerDate);
        LocationSelection lastLoc = ViewUtil.getLocationSelection(getActivity());
        String userCrnCode;
        if (lastLoc != null) {
            locLiCode = lastLoc.liCode;
            locLiKey = lastLoc.liKey;
            locValue = lastLoc.value;
            mobileEntry.setLocationName(locValue);
            userCrnCode = ViewUtil.getLastUsedCrnCode(getActivity());
            mobileEntry.setCrnCode(userCrnCode);
        } else if ((userCrnCode = ViewUtil.getLastUsedCrnCode(getActivity())) != null && userCrnCode.length() >= 0) {
            mobileEntry.setCrnCode(userCrnCode);
        } else {
            mobileEntry.setCrnCode(getDefaultCurrency());
        }
        ExpenseEntry expEntry = new ExpenseEntry((Long) null, ExpenseTypeEnum.CASH);
        expEntry.cashTransaction = mobileEntry;

        return expEntry;
    }

    /**
     * Create the expense object per data passed to the activity
     * 
     * We need two pieces of information to identify the expense, expenseType(CASH, RC, CCT, PCT) & ExpenseId
     * 
     */
    private void initExpenseEntry() {
        Bundle data = getActivity().getIntent().getExtras();
        // Obtain the expense entry type.
        String expType = data.getString(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY);
        ExpenseTypeEnum expEntType = getExpenseEntryType(expType);

        // Obtain a reference to the expense object.
        // Use uri to locate the entry
        Long expenseId = null;
        if (data.containsKey(Const.EXTRA_EXPENSE_EXPENSE_ID_KEY)) {
            expenseId = data.getLong(Const.EXTRA_EXPENSE_EXPENSE_ID_KEY);
        }

        // No expenseId passed in, then try to get type specific ids from intent
        String expenseKey = null;
        if (expenseId != null) {
            expenseEntry = new ExpenseEntry(expenseId, expEntType);
        } else {

            // Create param map for Flurry Notification based on expense type
            // viewed.
            Map<String, String> flurryParams = new HashMap<String, String>();
            switch (expEntType) {
            case CASH:
                flurryParams.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_CASH);
                EventTracker.INSTANCE
                        .track(Flurry.CATEGORY_QUICK_EXPENSE, Flurry.PARAM_NAME_VIEW_EXPENSE, flurryParams);
                if (data.containsKey(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY)) {
                    expenseKey = data.getString(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY);
                    // } else if (data.containsKey(Const.EXTRA_EXPENSE_LOCAL_KEY)) {
                    // String localKey = data.getString(Const.EXTRA_EXPENSE_LOCAL_KEY);
                    // if (localKey != null) {
                    // final MobileEntry offlineExpense = expEntCache.findMobileEntryByLocalKey(localKey);
                    // if (offlineExpense != null) {
                    // expenseEntry = new Expense(offlineExpense);
                    // } else {
                    // Log.e(Const.LOG_TAG, CLS_NAME
                    // + ".buildView: unable to locate cash expense entry in in-memory cache with local key!");
                    // }
                    // }
                }
                Log.d(Const.LOG_TAG, CLS_NAME + ".initExpenseEntry: " + (expEntType.toString())
                        + " expense edit with native key " + expenseKey);

                break;

            case SMART_PERSONAL:
            case PERSONAL_CARD:
                // Flurry notification for view personal card.
                flurryParams.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_PERSONAL_CARD);
                EventTracker.INSTANCE
                        .track(Flurry.CATEGORY_QUICK_EXPENSE, Flurry.PARAM_NAME_VIEW_EXPENSE, flurryParams);

                if (data.containsKey(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY)) {
                    expenseKey = data.getString(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY);
                }

                break;

            case SMART_CORPORATE:
            case CORPORATE_CARD:
                // Flurry notification for view Corporate card.
                flurryParams.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_CORPORATE_CARD);
                EventTracker.INSTANCE
                        .track(Flurry.CATEGORY_QUICK_EXPENSE, Flurry.PARAM_NAME_VIEW_EXPENSE, flurryParams);

                if (data.containsKey(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY)) {
                    expenseKey = data.getString(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY);
                }

                break;

            case RECEIPT_CAPTURE:
                // Flurry notification for view receipt capture.
                flurryParams.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_RECEIPT_CAPTURE);
                EventTracker.INSTANCE
                        .track(Flurry.CATEGORY_QUICK_EXPENSE, Flurry.PARAM_NAME_VIEW_EXPENSE, flurryParams);
                if (data.containsKey(Const.EXTRA_EXPENSE_RECEIPT_CAPTURE_KEY)) {
                    expenseKey = data.getString(Const.EXTRA_EXPENSE_RECEIPT_CAPTURE_KEY);
                }

                break;
            default:
                break;
            }

            // TODO - Handle offline item with LOCAL_KEY
            if (expenseKey != null) {
                expenseEntry = new ExpenseEntry(expenseKey, expEntType);
            } else if (expEntType == ExpenseTypeEnum.CASH) {
                // Create a new cash expense
                expenseEntry = generateNewCashExpense();
            } else if (expEntType == ExpenseTypeEnum.RECEIPT_CAPTURE) {
                // TODO If contains receipt for new OCR expense, get image file path

            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".initExpenseEntry: " + (expEntType.toString())
                        + " expense edit missing native key!");
            }

            if (expenseEntry != null && expenseEntry.getReceiptImageId() != null) {
                receiptImageId = expenseEntry.getReceiptImageId();
                Uri receiptUri = ContentUtils.getContentUri(getActivity(),
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptColumns.ID, receiptImageId);

                if (receiptUri == null) {
                    sendGetReceiptRequest(expenseEntry.getReceiptImageId());
                } else {
                    expenseEntry.receipt = new Receipt(getActivity(), receiptUri);
                }
            }
        }
    }

    protected void showProgressBar(View view) {
        View progressBar = view.findViewById(R.id.search_progress);
        progressbarVisible = true;
        progressBar.setVisibility(View.VISIBLE);
        progressBar.bringToFront();
    }

    protected void hideProgressBar(View view) {
        View progressBar = view.findViewById(R.id.search_progress);
        progressbarVisible = false;
        progressBar.setVisibility(View.INVISIBLE);
    }

    protected boolean isProgressBarShown() {
        return progressbarVisible;
    }

    // TODO - write AsyncTask and populate details
    protected class GetReceiptListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            String receiptUriStr = resultData.getString(SaveReceiptRequestTask.RECEIPT_URI_KEY);
            Uri receiptUri = Uri.parse(receiptUriStr);
            ReceiptDAO receipt = new Receipt(getActivity(), receiptUri);
            receipt.getThumbnailReceiptData();
            receiptImageDataLocalFilePath = receipt.getLocalPath();
            receiptImageDataLocalThumbnailFilePath = receipt.getThumbnailLocalPath();
            expenseEntry.receipt = receipt;
            displayThumbnail(getView());
            cleanup();
        }

        public void onRequestFail(Bundle resultData) {
            // expenseDetailCallbacks.onMobileEntrySaveRequestFail(resultData);
            // // hideProgressBar(getView());
            // ProgressDialogFragment prog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(
            // TAG_DIALOG_SAVE_EXPENSE);
            // prog.dismiss();
        }

        public void onRequestCancel(Bundle resultData) {
            cleanup();
        }

        public void cleanup() {
            // ProgressDialogFragment prog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(
            // TAG_DIALOG_SAVE_EXPENSE);
            // prog.dismiss();
            getReceiptReceiver = null;
            getReceiptRequest = null;
        }

    }

    protected class SaveExpenseListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {

            // Finish the activity.
            // activity.saveSucceeded = true;
            // TODO // Set the refresh list flag on the expense entry.
            // IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            // expEntCache.setShouldFetchExpenseList();
            //
            // // If the last receipt action was to select a receipt from the
            // // Receipt Store (or clear the
            // // receipt),
            // // then set the flag that the list should be refetched.
            // if (activity.lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD
            // || activity.lastReceiptAction == ReceiptPictureSaveAction.CLEAR_PICTURE) {
            // ReceiptStoreCache rsCache = ConcurCore.getReceiptStoreCache();
            // rsCache.setShouldFetchReceiptList();
            // }
            //
            // Flurry Notification.
            // Map<String, String> params = new HashMap<String, String>();
            // String meKey = saveExpenseRequest.getMobileEntryKey();
            // params.put(Flurry.PARAM_NAME_EDIT_NEW, ((meKey != null) ? Flurry.PARAM_VALUE_NEW : Flurry.PARAM_VALUE_EDIT));
            // boolean hasReceipt = (!activity.saveExpenseRequest.getClearImage() && activity.saveExpenseRequest
            // .getMobileEntry().getReceiptImageId() != null);
            // params.put(Flurry.PARAM_NAME_CONTAINS_RECEIPT, ((hasReceipt) ? Flurry.PARAM_VALUE_YES
            // : Flurry.PARAM_VALUE_NO));
            // EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_SAVED, params);
            expenseDetailCallbacks.onMobileEntrySaveRequestSuccess(resultData);
            // TODO activity.updateMRUs(activity.saveExpenseRequest);
            // activity.setResult(Activity.RESULT_OK);
            // activity.finish();

            ProgressDialogFragment prog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(
                    TAG_DIALOG_SAVE_EXPENSE);
            prog.dismiss();
            // hideProgressBar(getView());
        }

        public void onRequestFail(Bundle resultData) {
            expenseDetailCallbacks.onMobileEntrySaveRequestFail(resultData);
            // hideProgressBar(getView());
            cleanup();
        }

        public void onRequestCancel(Bundle resultData) {
            cleanup();
        }

        public void cleanup() {
            // hideProgressBar(getView());
            ProgressDialogFragment prog = (ProgressDialogFragment) getFragmentManager().findFragmentByTag(
                    TAG_DIALOG_SAVE_EXPENSE);
            prog.dismiss();
            saveExpenseReceiver = null;
            saveExpenseRequest = null;
        }

    }

    // Use platform SaveReceiptAsyncTask
    protected class SaveReceiptListener implements AsyncReplyListener {

        public void onRequestSuccess(Bundle resultData) {
            // hideProgressBar(getView());
            String receiptUriStr = resultData.getString(SaveReceiptRequestTask.RECEIPT_URI_KEY);
            Uri receiptUri = Uri.parse(receiptUriStr);
            ReceiptDAO receipt = new Receipt(getActivity(), receiptUri);
            // receipt.getThumbnailReceiptData();
            // receiptImageDataLocalFilePath = receipt.getLocalPath();
            // receiptImageDataLocalThumbnailFilePath = receipt.getThumbnailLocalPath();
            expenseEntry.receipt = receipt;

            cleanup();
        }

        public void onRequestFail(Bundle resultData) {
            // hideProgressBar(getView());
            cleanup();
        }

        public void onRequestCancel(Bundle resultData) {
            cleanup();
        }

        public void cleanup() {
            DialogFragment prog = (DialogFragment) getFragmentManager().findFragmentByTag(
                    SaveReceiptDialogFragment.DIALOG_FRAGMENT_ID);
            prog.dismiss();

            saveReceiptReceiver = null;
            saveReceiptRequest = null;
        }

    }

    /**
     * ExpenseEntry is a local private class created to manage four types of expense objects this fragment needs to deal with This
     * 
     */
    protected class ExpenseEntry {

        /**
         * Contains the expense type.
         */
        public ExpenseTypeEnum type;

        /**
         * Current status of this entry, saved, being deleted, or pending add to report.
         */
        public MobileEntryStatus status;

        /**
         * Contains the expense id.
         */
        public Long expenseId;

        /**
         * Contains the latest (local or server) receipt associated with the new/existing expense.
         * 
         * TODO save to this receipt object when attach/update receipt
         */
        public ReceiptDAO receipt;

        /**
         * Contains the cash transaction.
         */
        public MobileEntryDAO cashTransaction;

        /**
         * Contains the personal card transaction.
         */
        public PersonalCardTransactionDAO personalCardTransaction;

        /**
         * Contains the corporate card transaction.
         */
        public CorporateCardTransactionDAO corporateCardTransaction;

        /**
         * Contains the receipt capture.
         */
        public ReceiptCaptureDAO receiptCapture;

        public ExpenseEntry(String expenseKey, ExpenseTypeEnum expEntType) {
            this.type = expEntType;
            Uri expenseUri = null;
            switch (expEntType) {
            case CASH:
                expenseUri = ContentUtils.getContentUri(getActivity(),
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.MOBILE_ENTRY_KEY,
                        expenseKey);
                this.cashTransaction = new MobileEntry(getActivity(), expenseUri);
                this.expenseId = Long.parseLong(this.cashTransaction.getContentURI(getActivity()).getLastPathSegment());
                break;
            case CORPORATE_CARD:
                expenseUri = ContentUtils
                        .getContentUri(
                                getActivity(),
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CCT_KEY,
                                expenseKey);
                this.corporateCardTransaction = new CorporateCardTransaction(getActivity(), expenseUri);
                break;
            case SMART_CORPORATE:
                expenseUri = ContentUtils
                        .getContentUri(
                                getActivity(),
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CONTENT_URI,
                                com.concur.mobile.platform.expense.provider.Expense.CorporateCardTransactionColumns.CCT_KEY,
                                expenseKey);
                this.corporateCardTransaction = new CorporateCardTransaction(getActivity(), expenseUri);
                this.cashTransaction = this.corporateCardTransaction.getMobileEntryDAO();
                break;
            case PERSONAL_CARD:
                expenseUri = ContentUtils.getContentUri(getActivity(),
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.PCT_KEY,
                        expenseKey);
                this.personalCardTransaction = new PersonalCardTransaction(getActivity(), expenseUri);
                break;
            case SMART_PERSONAL:
                expenseUri = ContentUtils.getContentUri(getActivity(),
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardTransactionColumns.PCT_KEY,
                        expenseKey);
                this.personalCardTransaction = new PersonalCardTransaction(getActivity(), expenseUri);
                this.cashTransaction = this.personalCardTransaction.getMobileEntryDAO();
                break;
            case RECEIPT_CAPTURE:
                expenseUri = ContentUtils.getContentUri(getActivity(),
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.ReceiptCaptureColumns.RC_KEY, expenseKey);
                this.receiptCapture = new ReceiptCapture(getActivity(), expenseUri);
                break;
            default:
                break;
            }
        }

        public ExpenseEntry(Long expenseId, ExpenseTypeEnum expEntType) {
            this.type = expEntType;

            if (expenseId != null) {
                status = MobileEntryStatus.NORMAL;
                SessionInfo sessInfo = ConfigUtil.getSessionInfo(getActivity());
                ExpenseListDAO expListDAO = new ExpenseListDAO(getActivity(), sessInfo.getUserId());
                switch (expEntType) {
                case CASH:
                    this.cashTransaction = expListDAO.getCashTransaction(expenseId);
                    break;
                case CORPORATE_CARD:
                    this.corporateCardTransaction = expListDAO.getCorporateCardTransaction(expenseId);
                    break;
                case PERSONAL_CARD:
                    this.personalCardTransaction = expListDAO.getPersonalCardTransaction(expenseId);
                    break;

                case RECEIPT_CAPTURE:
                    this.receiptCapture = expListDAO.getReceiptCapture(expenseId);
                    break;
                default:
                    break;
                }
            } else
                status = MobileEntryStatus.NEW;
        }

        public MobileEntryDAO convertPctToMobileEntry(PersonalCardTransactionDAO persCardTrans) {
            MobileEntryDAO entry = new MobileEntry();
            entry.setCrnCode(persCardTrans.getCrnCode());
            entry.setExpKey(persCardTrans.getExpKey());
            entry.setExpName(persCardTrans.getExpName());
            entry.setLocationName("");
            entry.setVendorName(persCardTrans.getDescription());
            entry.setTransactionAmount(persCardTrans.getAmount());
            entry.setTransactionDate(persCardTrans.getDatePosted());
            entry.setPctKey(persCardTrans.getPctKey());

            // Initialize the create/update times to the save value (now).
            // this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
            // information about the card transaction.
            this.status = MobileEntryStatus.NORMAL;
            return entry;
        }

        public MobileEntryDAO convertCctToMobileEntry(CorporateCardTransactionDAO corpCardTrans) {
            MobileEntryDAO entry = new MobileEntry();
            entry.setCrnCode(corpCardTrans.getTransactionCrnCode());
            entry.setExpKey(corpCardTrans.getExpenseKey());
            entry.setExpName(corpCardTrans.getExpenseName());
            StringBuilder strBldr = new StringBuilder();
            if (corpCardTrans.getMerchantCity() != null) {
                strBldr.append(corpCardTrans.getMerchantCity());
            }
            if (corpCardTrans.getMerchantState() != null && corpCardTrans.getMerchantState().length() > 0) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(corpCardTrans.getMerchantState());
            }
            if (corpCardTrans.getMerchantCountryCode() != null && corpCardTrans.getMerchantCountryCode().length() > 0) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(corpCardTrans.getMerchantCountryCode());
            }
            entry.setLocationName(strBldr.toString());
            entry.setVendorName(corpCardTrans.getMerchantName());
            entry.setTransactionAmount(corpCardTrans.getTransactionAmount());
            entry.setTransactionDate(corpCardTrans.getTransactionDate());
            entry.setCctKey(corpCardTrans.getCctKey());
            // this.entryType = Expense.ExpenseEntryType.CORPORATE_CARD;
            // // Initialize the create/update times to the save value (now).
            // this.createDate = this.updateDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            // MOB-13441 - Init 'status' to normal since this MobileEntry is being created to refer to
            // information about the card transaction.
            this.status = MobileEntryStatus.NORMAL;
            return entry;
        }

        public MobileEntryDAO getMobileEntryToSave() {
            MobileEntryDAO entry = null;
            switch (type) {
            case CASH:
                entry = this.cashTransaction;
                break;
            case CORPORATE_CARD:
                entry = corporateCardTransaction.getMobileEntryDAO();
                if (entry == null) {
                    // Create a mobile entry from a cct
                    entry = convertCctToMobileEntry(corporateCardTransaction);
                }
                break;
            case SMART_CORPORATE:
                entry = corporateCardTransaction.getSmartMatchedMobileEntryDAO();
                break;
            case PERSONAL_CARD:
                entry = personalCardTransaction.getMobileEntryDAO();
                if (entry == null) {
                    // Create a mobile entry from a pct
                    entry = convertPctToMobileEntry(personalCardTransaction);
                }

                break;
            case SMART_PERSONAL:
                entry = personalCardTransaction.getSmartMatchedMobileEntryDAO();
                break;
            default:
                break;
            }
            // TODO ? save entry to cashTransaction?
            return entry;

        }

        public Calendar getSentForDate() {
            // TODO Get real date from provider
            Calendar date = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            date.setTimeInMillis(System.currentTimeMillis());
            date.set(Calendar.MILLISECOND, 0);
            return date;
        }

        public Calendar getTransactionDate() {
            if (cashTransaction != null)
                return cashTransaction.getTransactionDate();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getTransactionDate();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getDatePosted();
            else if (receiptCapture != null)
                return receiptCapture.getTransactionDate();
            return null;
        }

        public String getVendorName() {
            if (cashTransaction != null)
                return cashTransaction.getVendorName();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getMerchantName();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getDescription();
            else if (receiptCapture != null)
                return receiptCapture.getVendorName();
            return null;
        }

        private String getCctLocation(CorporateCardTransactionDAO corpCardTrans) {
            StringBuilder strBldr = new StringBuilder();
            if (corpCardTrans.getMerchantCity() != null) {
                strBldr.append(corpCardTrans.getMerchantCity());
            }
            if (corpCardTrans.getMerchantState() != null && corpCardTrans.getMerchantState().length() > 0) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(corpCardTrans.getMerchantState());
            }
            if (corpCardTrans.getMerchantCountryCode() != null && corpCardTrans.getMerchantCountryCode().length() > 0) {
                if (strBldr.length() > 0) {
                    strBldr.append(',');
                }
                strBldr.append(corpCardTrans.getMerchantCountryCode());
            }
            return strBldr.toString();
        }

        public String getLocationName() {
            if (cashTransaction != null)
                return cashTransaction.getLocationName();
            else if (corporateCardTransaction != null)
                return getCctLocation(corporateCardTransaction);
            else if (personalCardTransaction != null)
                return "";
            else if (receiptCapture != null)
                return "";

            return null;
        }

        public String getExpName() {
            if (cashTransaction != null)
                return cashTransaction.getExpName();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getExpenseName();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getExpName();
            else if (receiptCapture != null)
                return receiptCapture.getExpName();
            return null;
        }

        public String getExpKey() {
            if (cashTransaction != null)
                return cashTransaction.getExpKey();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getExpenseKey();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getExpKey();
            else if (receiptCapture != null)
                return receiptCapture.getExpKey();

            return null;
        }

        public String getComment() {
            if (cashTransaction != null)
                return cashTransaction.getComment();
            else if (corporateCardTransaction != null && corporateCardTransaction.getMobileEntryDAO() != null)
                return corporateCardTransaction.getMobileEntryDAO().getComment();
            else if (personalCardTransaction != null && personalCardTransaction.getMobileEntryDAO() != null)
                return personalCardTransaction.getMobileEntryDAO().getComment();
            else if (receiptCapture != null)
                return null;
            // return receiptCapture.getComment();
            return null;
        }

        public String getCrnCode() {
            if (cashTransaction != null)
                return cashTransaction.getCrnCode();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getTransactionCrnCode();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getCrnCode();
            // else if (receiptCapture != null)
            // return receiptCapture.getCrnCode();
            // TODO
            return null;

        }

        public Double getTransactionAmount() {
            if (cashTransaction != null)
                return cashTransaction.getTransactionAmount();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getTransactionAmount();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getAmount();
            else if (receiptCapture != null)
                return receiptCapture.getTransactionAmount();
            return null;

        }

        public boolean hasReceiptImage() {
            if (cashTransaction != null)
                return cashTransaction.hasReceiptImage();
            else if (corporateCardTransaction != null)
                return corporateCardTransaction.getMobileEntryDAO() != null
                        && corporateCardTransaction.getMobileEntryDAO().hasReceiptImage();
            else if (personalCardTransaction != null)
                return personalCardTransaction.getMobileEntryDAO() != null
                        && personalCardTransaction.getMobileEntryDAO().hasReceiptImage();
            if (receiptCapture != null)
                return true;

            return false;

        }

        public String getReceiptImageDataLocalFilePath() {
            if (cashTransaction != null)
                return cashTransaction.getReceiptImageDataLocalFilePath();
            // else if (receiptCapture != null)
            // return true;
            // TODO
            return null;
        }

        public String getReceiptImageId() {
            if (cashTransaction != null)
                return cashTransaction.getReceiptImageId();
            else if (receiptCapture != null)
                return receiptCapture.getReceiptImageId();
            return null;
        }

        // TODO - add setters
    }

    // Yiwen: Moved to onCreateView()
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
    // */
    // @Override
    // protected void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    //
    // lastSavedInstanceState = savedInstanceState;
    //
    // // We're specifically getting 'now' in the device local timezone because
    // // then
    // // we just pull the date values out and use them to populate our
    // // UTC-standard
    // // calendar.
    // Calendar now = Calendar.getInstance();
    // datePickerDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    // datePickerDate.clear();
    // datePickerDate.set(now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH), 0, 0, 0);
    //
    // if (isServiceAvailable()) {
    // buildView();
    // initState(lastSavedInstanceState);
    // } else {
    // buildViewDelay = true;
    // }
    //
    // // Restore any receivers.
    // restoreReceivers();
    //
    // datePickerDlg = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
    // TAG_CALENDAR_DIALOG_FRAGMENT);
    // if (datePickerDlg != null) {
    // datePickerDlg.setOnDateSetListener(new DatePickerDialogListener());
    // }
    // }
    //

    @Override
    public void onPause() {
        super.onPause();

        if (retainer != null) {

            // Store any outstanding save expense receiver.
            if (saveExpenseReceiver != null) {
                // Clear the listener reference, it will be reset in the
                // 'onCreate' method.
                saveExpenseReceiver.setListener(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_SAVE_EXPENSE_RECEIVER_KEY, saveExpenseReceiver);
            }

            // Store any outstanding save receipt receiver.
            if (saveReceiptReceiver != null) {
                // Clear the activity reference, it will be reset in the
                // 'onCreate' method.
                saveReceiptReceiver.setListener(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_SAVE_RECEIPT_RECEIVER_KEY, saveReceiptReceiver);
            }

            // // Check for 'AppendReceiptReceiver'.
            // if (appendReceiptReceiver != null) {
            // // Clear the activity reference, it will be reset in the
            // // 'onCreate' method.
            // appendReceiptReceiver.setListener(null);
            // // Add it to the retainer
            // retainer.put(APPEND_RECEIPT_RECEIVER_KEY, appendReceiptReceiver);
            // }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.support.v4.app.Fragment#onAttach(android.app.Activity)
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        saveExpenseReplyListener = new SaveExpenseListener();
        getReceiptReplyListener = new GetReceiptListener();
        saveReceiptReplyListener = new SaveReceiptListener();
        try {
            expenseDetailCallbacks = (ExpenseDetailCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ExpenseDetailCallbacks");
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
        expenseDetailCallbacks = null;
    }

    protected void restoreReceivers() {

        // retrieve receiver if needed

        // Restore any non-configuration data.
        if (getRetainer() != null) {

            // Restore any receiver waiting on a save expense response.
            if (retainer.contains(EXTRA_GET_RECEIPT_RECEIVER_KEY)) {
                getReceiptReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_GET_RECEIPT_RECEIVER_KEY);
                if (getReceiptReceiver != null) {
                    getReceiptReceiver.setListener(getReceiptReplyListener);
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME
                            + ".restoreReceivers: retainer has null value for get receipt receiver!");
                }
            }

            // Restore any receiver waiting on a save expense response.
            if (retainer.contains(EXTRA_SAVE_EXPENSE_RECEIVER_KEY)) {
                saveExpenseReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_SAVE_EXPENSE_RECEIVER_KEY);
                if (saveExpenseReceiver != null) {
                    saveExpenseReceiver.setListener(saveExpenseReplyListener);
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME
                            + ".restoreReceivers: retainer has null value for save mobile entry receiver!");
                }
            }
            // Restore any receiver waiting on a save receipt response.
            if (retainer.contains(EXTRA_SAVE_RECEIPT_RECEIVER_KEY)) {
                saveReceiptReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_SAVE_RECEIPT_RECEIVER_KEY);
                if (saveReceiptReceiver != null) {
                    saveReceiptReceiver.setListener(new SaveReceiptListener());
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME
                            + ".restoreReceivers: retainer has null value for save receipt receiver!");
                }
            }
        }

        // // Restore the 'AppendReceiptReceiver'.
        // if (retainer.contains(APPEND_RECEIPT_RECEIVER_KEY)) {
        // appendReceiptReceiver = (AppendReceiptReceiver) retainer.get(APPEND_RECEIPT_RECEIVER_KEY);
        // // Reset the activity reference.
        // appendReceiptReceiver.setActivity(this);
        // }
        // }
    }

    //
    // /**
    // * Will send the appropriate Flurry notification if this activity is creating a new quick expense.
    // */
    // protected void sendFlurryNotificationIfCreate() {
    // if (!orientationChange) {
    // Intent intent = getIntent();
    // if (intent.hasExtra(Flurry.PARAM_NAME_CAME_FROM)) {
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION)) {
    // int mobileEntryAction = intent.getIntExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, -1);
    // if (mobileEntryAction == Const.CREATE_MOBILE_ENTRY) {
    // String paramValue = intent.getStringExtra(Flurry.PARAM_NAME_CAME_FROM);
    // if (paramValue != null) {
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(Flurry.PARAM_NAME_CAME_FROM, paramValue);
    // FlurryAgent.onEvent(
    // Flurry.formatFlurryEvent(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_CREATE),
    // params);
    // }
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG
    // + ".sendFlurryNotificationIfCreate: intent missing mobile entry action!");
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".sendFlurryNotificationIfCreate: intent missing came from extra!");
    // }
    // }
    // }
    //
    /**
     * Initializes the view.
     */
    public void buildView(View view) {

        // // Flurry Notification.
        // if (!ConcurCore.isConnected()) {
        // Map<String, String> params = new HashMap<String, String>();
        // params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_QUICK_EXPENSE);
        // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_OFFLINE, Flurry.EVENT_NAME_VIEWED), params);
        // }
        //
        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Flurry Notification.
        // TODO sendFlurryNotificationIfCreate();
        //
        // View receiptButton = view.findViewById(R.id.header_view_attach_receipts);

        // Yiwen: Moved to initExpenseEntry()
        // Obtain the expense entry key based on the expense entry type.

        boolean receiptBtnEnabled = true;

        if (expenseEntry.type.equals(ExpenseTypeEnum.PERSONAL_CARD)) {
            // // If there isn't already a mobile entry associated with the
            // // personal card
            // // transaction, then create one, but don't associate it until the
            // // point of
            // // saving.
            // mobileEntry = expenseEntry.getPersonalCardTransaction().mobileEntry;
            // if (mobileEntry == null) {
            // mobileEntry = new MobileEntry(expenseEntry.getPersonalCard(), expenseEntry.getPersonalCardTransaction());
            // } else {
            // // We save a clone as we update the object model unless the save
            // // succeeds.
            // mobileEntry = mobileEntry.clone();
            // }
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptBtnEnabled = false;
            } else {
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            }
        } else if (expenseEntry.type.equals(ExpenseTypeEnum.CORPORATE_CARD)) {
            // // If there isn't already a mobile entry associated with the
            // // corporate card
            // // transaction, then create one, but don't associate it until the
            // // point of
            // // saving.
            // mobileEntry = expenseEntry.getCorporateCardTransaction().getMobileEntry();
            // if (mobileEntry == null) {
            // mobileEntry = new MobileEntry(expenseEntry.getCorporateCardTransaction());
            // } else {
            // mobileEntry = mobileEntry.clone();
            // }
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptBtnEnabled = false;
            } else {
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = vendorReadOnly = true;
                // Transaction date readonly controlled by site setting.
                transDateReadOnly = !ExpensePreferenceUtil.isCardTransDateEditable(getActivity());
            }
        } else if (expenseEntry.type.equals(ExpenseTypeEnum.SMART_CORPORATE)) {
            // // Construct a mobile entry based on the corporate card transaction.
            // mobileEntry = new MobileEntry(expenseEntry.getCorporateCardTransaction());
            // // Migrate expense type, comment and whether a receipt exists from
            // // the cash transaction.
            // MobileEntry trns = expenseEntry.getCashTransaction();
            // mobileEntry.setComment(trns.getComment());
            // mobileEntry.setEntryType(Expense.ExpenseEntryType.SMART_CORPORATE);
            // mobileEntry.setExpKey(trns.getExpKey());
            // mobileEntry.setExpName(trns.getExpName());
            // mobileEntry.setHasReceiptImage(trns.hasReceiptImage());
            // mobileEntry.setMeKey(trns.getMeKey());
            // mobileEntry.setLocalKey(trns.getLocalKey());
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptBtnEnabled = false;
            } else {
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                // Transaction date readonly controlled by site setting.
                transDateReadOnly = !ExpensePreferenceUtil.isCardTransDateEditable(getActivity());
            }

        } else if (expenseEntry.type.equals(ExpenseTypeEnum.SMART_PERSONAL)) {
            // // Construct a mobile entry based on the corporate card transaction.
            // mobileEntry = new MobileEntry(expenseEntry.getPersonalCard(), expenseEntry.getPersonalCardTransaction());
            // // Migrate expense type, comment and whether a receipt exists from
            // // the cash transaction.
            // MobileEntry trns = expenseEntry.getCashTransaction();
            // mobileEntry.setComment(trns.getComment());
            // mobileEntry.setEntryType(Expense.ExpenseEntryType.SMART_PERSONAL);
            // mobileEntry.setExpKey(trns.getExpKey());
            // mobileEntry.setExpName(trns.getExpName());
            // mobileEntry.setHasReceiptImage(trns.hasReceiptImage());
            // mobileEntry.setMeKey(trns.getMeKey());
            // mobileEntry.setLocalKey(trns.getLocalKey());
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptBtnEnabled = false;
            } else {
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            }
        } else if (expenseEntry.type.equals(ExpenseTypeEnum.RECEIPT_CAPTURE)) {
            // TODO mobileEntry = new MobileEntry(expenseEntry.getReceiptCapture());
            currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            transDateReadOnly = saveReadOnly = receiptViewOnly = true;
            expenseTypeReadOnly = commentReadOnly = false;
            showOCRProgress(view);
        } else if (expenseEntry.type.equals(ExpenseTypeEnum.CASH)) {

            // TODO necessary now that ME is stored in Expense DB?
            // // We save a clone as we update the object model unless the save
            // // succeeds.
            // // MOB-11314
            // mobileEntry = expenseEntry.getCashTransaction().clone();
            MobileEntryDAO mobileEntry = expenseEntry.cashTransaction;
            if (expenseEntry.status == MobileEntryStatus.NORMAL && !ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptBtnEnabled = false;
            }
        }

        if (receiptBtnEnabled) {
            View receiptButton = view.findViewById(R.id.expense_receipt);
            receiptButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!receiptViewOnly) {
                        if (ExpenseDetailFragment.this.receiptImageDataLocalFilePath == null) {
                            ReceiptOptionsDialogFragment df = new ReceiptOptionsDialogFragment();
                            df.show(getActivity().getSupportFragmentManager(),
                                    ReceiptOptionsDialogFragment.DIALOG_FRAGMENT_ID);
                        }
                    }
                }
            });
        }

        // Set current expense type
        setSelectedExpenseType(view, expenseEntry.getExpKey(), expenseEntry.getExpName());

        // Set up support for currency selection.
        configureCurrencySelection(view);

        // Set up the date picker dialog.
        configureDatePicker(view);

        // after setup UI, create MRU collector
        if (mrudataCollector == null && expenseEntry.cashTransaction != null) {
            mrudataCollector = new MrudataCollector(expenseEntry.cashTransaction.getMeKey());
            if (selExpKey != null) {
                mrudataCollector.setOldExpType(selExpKey);
            }
            if (selCurType != null) {
                mrudataCollector.setOldCurType(selCurType.code);
            }
            mrudataCollector.setOldLoc(expenseEntry.getLocationName());
        }

        // Display an alert if the app is not connected and there is no cached
        // data for expense/currency.
        if (!ConcurCore.isConnected() && (curTypeAdapter.getCount() == 0 /* TODO || expTypeAdapter.getCount() == 0 */)) {
            getActivity().showDialog(Const.DIALOG_EXPENSE_NO_EXPENSE_TYPE_CURRENCY);
        }

        Bundle data = getActivity().getIntent().getExtras();

        if (data != null) {
            String path = data.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
            if (path != null && path.length() > 0) {
                receiptCameraImageDataLocalFilePath = path;
                isPathAvailable = true;
                receiptCaptureSuccess(null);
            }
        }
    }

    //
    // /**
    // * Handles a generic on click message for a view.
    // *
    // * @param view
    // * the view that was clicked on.
    // */
    // public void onClick(View view) {
    // final int id = view.getId();
    // if (id == R.id.header_view_attach_receipts) {
    // if (receiptViewOnly) {
    // viewReceiptPicture();
    // } else {
    // showDialog(DIALOG_RECEIPT_IMAGE);
    // }
    // }
    // }
    //
    protected boolean isSaveActionEnabled() {
        return !saveReadOnly;
    }

    protected boolean isDeleteActionEnabled() {
        return expenseEntry != null
                && (expenseEntry.cashTransaction != null && expenseEntry.cashTransaction.getMeKey() != null || expenseEntry.receiptCapture != null
                        && expenseEntry.receiptCapture.getRCKey() != null);
    }

    /**
     * Configures support for currency selection.
     */
    private void configureCurrencySelection(View view) {

        View currencyView = view.findViewById(R.id.expense_currency);
        if (currencyView != null) {
            curTypeAdapter = new CurrencySpinnerAdapter(getActivity());
            if (expenseEntry != null && expenseEntry.getCrnCode() != null) {
                int curTypeInd = curTypeAdapter.getPositionForCurrency(expenseEntry.getCrnCode());
                if (curTypeInd != -1) {
                    Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                    if (curTypeObj instanceof ListItem) {
                        setSelectedCurrencyType((ListItem) curTypeObj, view);
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_NAME + ".configureCurrencySelection: can't locate expense currency field!");
        }
    }

    //
    // /**
    // * Will set the text on the receipt label depending upon whether a receipt has been selected.
    // */
    // private void configureReceiptLabel() {
    //
    // TextView txtView = (TextView) findViewById(R.id.view_receipts);
    // if (txtView != null) {
    // int receiptLabelResId = R.string.attach_receipt;
    // if (receiptImageId != null
    // || receiptImageDataLocalFilePath != null
    // || (mobileEntry != null && (mobileEntry.hasReceiptImage() || mobileEntry.hasReceiptImageDataLocal() || mobileEntry
    // .getReceiptImageId() != null))
    // && (lastReceiptAction != null && lastReceiptAction != ReceiptPictureSaveAction.CLEAR_PICTURE)) {
    // receiptLabelResId = R.string.view_receipt;
    // }
    // txtView.setText(receiptLabelResId);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".configureReceiptLabel: unable to locate 'view_receipts' view!");
    // }
    // }

    /**
     * Will set the currently selected expense type and update the display.
     * 
     * @param oldExpType
     *            the expense type.
     */
    protected void setSelectedExpenseType(View view, String expKey, String expName) {
        // Set the reference.
        this.selExpKey = expKey;
        this.selExpName = expName;

        // Update the display.
        if (this.selExpName != null) {
            ViewUtil.setTextViewText(view, R.id.expense_type, R.id.field_value, expName, true);
        } else {
            ViewUtil.setTextViewText(view, R.id.expense_type, R.id.field_value, "", true);
        }
    }

    /**
     * Will set the currently selcted currency type and update the display. If the given <code>crn</code> code is null or cannot
     * be found (i.e. is not a valid CRN code in the list of currency selections), then nothing is done.
     * 
     * @param crn
     *            the CRN code.
     */
    protected void setSelectedCurrencyType(String crn, View view) {

        if (crn != null) {
            int curTypeInd = curTypeAdapter.getPositionForCurrency(crn);
            if (curTypeInd != -1) {
                Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                if (curTypeObj instanceof ListItem) {
                    setSelectedCurrencyType((ListItem) curTypeObj, view);
                }
            }
        }
    }

    /**
     * Will set the currently selected currency type and update the display.
     * 
     * @param curType
     *            the expense type.
     */
    protected void setSelectedCurrencyType(ListItem selCurType, View view) {
        // Set the reference.
        this.selCurType = selCurType;
        // Update the display.
        if (this.selCurType != null) {
            ViewUtil.setTextViewText(view, R.id.expense_currency, R.id.field_value, selCurType.text, true);
        } else {
            ViewUtil.setTextViewText(view, R.id.expense_currency, R.id.field_value, "", true);
        }
    }

    /**
     * For the first time the app is installed, get the currency code based on their phone's locale and set that as the default
     * currency type.
     * 
     * @return The selected currency type if it is in our list of Currency Types, otherwise USD.
     */
    private String getDefaultCurrency() {
        ConcurCore ConcurCore = (ConcurCore) getActivity().getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        ArrayList<ListItem> curTypes = expEntCache.getCurrencyTypes();

        // Get currency code based on the locale of the user
        Locale loc = ViewUtil.getResourcesConfigurationLocale(getActivity());
        Currency cur = Currency.getInstance(loc);
        String curCode = cur.toString();

        // Does our Currency Type list have the Currency Code associated with
        // this locale
        boolean hasCurType = false;

        if (curTypes != null) {
            for (ListItem listItem : curTypes) {
                if (listItem.code != null && listItem.code.equals(curCode)) {
                    hasCurType = true;
                    break;
                }
            }
        }

        if (hasCurType) {
            return curCode;
        } else {
            return "USD";
        }
    }

    /**
     * Configures support for selecting a date.
     */
    private void configureDatePicker(View mainView) {
        // Set the expense date.
        if (expenseEntry != null) {
            datePickerDate = expenseEntry.getTransactionDate();
            updateDatePickerFieldValue(mainView);
        }
    }

    /**
     * Show view progress bar for receipt capture items.
     */
    private void showOCRProgress(View view) {
        View subHeader = view.findViewById(R.id.ocr_progress);
        subHeader.setVisibility(View.VISIBLE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_detail, menu);
        MenuItem item = menu.findItem(R.id.menuSave);
        if (item != null) {
            if (isSaveActionEnabled()) {
                item.setVisible(true);
                item.setEnabled(true);
            } else {
                item.setVisible(false);
                item.setEnabled(false);
            }
        }

        item = menu.findItem(R.id.menuDelete);
        if (item != null) {
            if (isDeleteActionEnabled()) {
                item.setVisible(true);
                item.setEnabled(true);
            } else {
                item.setVisible(false);
                item.setEnabled(false);

            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSave) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // private void setRequiredFieldLabelColors() {
    // // Set expense_type label field color
    // if (selExpType != null) {
    // setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_BLACK);
    // } else {
    // setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_RED);
    // }
    //
    // // Set amount field label color
    // String amount = ViewUtil.getTextViewText(this, R.id.expense_amount, R.id.field_value);
    // if (amount != null && amount.length() > 0) {
    // setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_BLACK);
    // } else {
    // setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
    // }
    // }
    //
    // // Sets the label color of a field.
    // private void setFieldLabelColor(int field_view_res_id, int field_name_res_id, int color) {
    // View view = findViewById(field_view_res_id);
    // TextView textView = (TextView) view.findViewById(field_name_res_id);
    // textView.setTextColor(color);
    // }
    //
    // // Appends "(Required)" to the label of a field
    // public CharSequence buildRequiredLabel(CharSequence label) {
    // if (label != null) {
    // SpannableStringBuilder strBldr = new SpannableStringBuilder(label);
    // strBldr.append(' ');
    // int spanStart = strBldr.length();
    // strBldr.append('(');
    // strBldr.append(getText(R.string.required).toString());
    // strBldr.append(')');
    // int requiredStyle = R.style.FormFieldLabelRequired;
    //
    // strBldr.setSpan(new TextAppearanceSpan(this.getApplicationContext(), requiredStyle), spanStart,
    // strBldr.length(), 0);
    //
    // label = strBldr;
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".buildLabel: form fields label is null!");
    // }
    // return label;
    // }
    //
    // // Sets the label of a TextView field that is required to be filled out.
    // // This will call the buildRequiredLabel method in order
    // // to append "(Required)" on to the field in italics.
    // public void setRequiredTextView(int field_view_res_id, int field_name_res_id, CharSequence labelName) {
    // CharSequence appendedLabel = buildRequiredLabel(labelName);
    // View view = findViewById(field_view_res_id);
    // TextView textView = (TextView) view.findViewById(field_name_res_id);
    // textView.setText(appendedLabel);
    // }

    private void setupTransactionDateField(View view, int viewState, boolean isOCR) {
        View dateView = view.findViewById(R.id.expense_date);
        dateView.setVisibility(viewState);
        View sep = view.findViewById(R.id.expense_date_fld_separator);
        sep.setVisibility(viewState);

        if (viewState == View.VISIBLE) {

            TextView labelView = (TextView) dateView.findViewById(R.id.field_name);
            labelView.setText(R.string.date);
            ImageView iconView = (ImageView) dateView.findViewById(R.id.field_icon);
            iconView.setImageResource(R.drawable.expense_icon_calendar);

            if (isOCR) {
                View sentForTime = dateView.findViewById(R.id.time_area);
                sentForTime.setVisibility(View.VISIBLE);
            } else {
                View sentForTime = dateView.findViewById(R.id.time_area);
                sentForTime.setVisibility(View.GONE);
            }

            if (!transDateReadOnly) {
                dateView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        removeEditFocus(); // Remove cursor focus from edit field

                        showCalendarDialog();
                    }
                });
            }
        }
    }

    private void setupExpenseTypeField(View view, int viewState) {
        View expTypeView = view.findViewById(R.id.expense_type);
        expTypeView.setVisibility(viewState);
        // Expense Type will always shown
        if (viewState == View.VISIBLE) {

            TextView labelView = (TextView) expTypeView.findViewById(R.id.field_name);
            labelView.setText(R.string.field_label_expense_type);

            if (expenseEntry != null && expenseEntry.getExpName() != null) {
                ViewUtil.setTextViewText(view, R.id.expense_type, R.id.field_value, expenseEntry.getExpName(), false);
            } else {
                ViewUtil.setTextViewText(view, R.id.expense_type, R.id.field_value, "", false);
            }
            // Enable/disable type.
            ViewUtil.setViewEnabled(view, R.id.expense_type, R.id.field_value, !expenseTypeReadOnly);

            // Set max lines to display to 2. Needs to be done after setSingleLine() in setTextViewText
            TextView valueView = (TextView) expTypeView.findViewById(R.id.field_value);
            valueView.setMaxLines(2);

            if (!expenseTypeReadOnly) {
                expTypeView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        removeEditFocus();
                        // TODO new expense type dialog
                        ExpenseTypeDialogFragment df = new ExpenseTypeDialogFragment();
                        ConcurCore ConcurCore = (ConcurCore) getActivity().getApplication();
                        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();

                        @SuppressWarnings("unchecked")
                        ArrayList<com.concur.mobile.core.expense.data.ExpenseType> expenseTypes = (ArrayList<com.concur.mobile.core.expense.data.ExpenseType>) expEntCache
                                .getExpenseTypes().clone();
                        df.setExpenseTypeList(expenseTypes);
                        df.setExpTypeSelectionListener(ExpenseDetailFragment.this);
                        df.show(getActivity().getSupportFragmentManager(),
                                ReceiptChoiceDialogFragment.DIALOG_FRAGMENT_ID);

                    }

                });
            }
        }
    }

    public void selectExpenseType(com.concur.mobile.core.expense.data.ExpenseType expType) {
        if (expType != null)
            setSelectedExpenseType(getView(), expType.key, expType.name);
        else
            setSelectedExpenseType(getView(), null, null);
        // TODO
        // if (selExpKey == null) {
        // setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_RED);
        // } else {
        // setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_BLACK);
        // }
    }

    private void removeEditFocus() {
        View vendor = getView().findViewById(R.id.expense_vendor);
        TextView vendorValue = (TextView) vendor.findViewById(R.id.field_value);
        vendorValue.clearFocus();
        View amountView = getView().findViewById(R.id.expense_amount);
        TextView amountValue = (TextView) amountView.findViewById(R.id.field_value);
        amountValue.clearFocus();

    }

    private void setupOCRSentForDateField(View view, int viewState) {
        View sentForDate = view.findViewById(R.id.sent_for_date);
        sentForDate.setVisibility(viewState);
        View sep = view.findViewById(R.id.expense_sent_for_date_fld_separator);
        sep.setVisibility(viewState);

        TextView sentForDateLabel = (TextView) sentForDate.findViewById(R.id.field_name);
        sentForDateLabel.setText(R.string.ocr_field_label_sent_date);
        ImageView iconView = (ImageView) sentForDate.findViewById(R.id.time_icon);
        iconView.setImageResource(R.drawable.expense_icon_clock);

        if (viewState == View.VISIBLE) {
            Calendar date = null;
            if (expenseEntry != null && ((date = expenseEntry.getSentForDate()) != null)) {
                String dateStr = Format.safeFormatCalendar(FormatUtil.FULL_WEEKDAY_SHORT_MONTH_DAY_FULL_YEAR, date);
                ViewUtil.setTextViewText(view, R.id.sent_for_date, R.id.field_value, dateStr, true);

                String timeStr = Format.safeFormatCalendar(FormatUtil.SHORT_TIME_DISPLAY, date);
                ViewUtil.setTextViewText(view, R.id.sent_for_date, R.id.time_value, timeStr, true);
            } else {
                ViewUtil.setTextViewText(view, R.id.sent_for_date, R.id.field_value, "", true);
            }
            // Enabled/disable the control.
            ViewUtil.setViewEnabled(view, R.id.sent_for_date, R.id.field_value, false);
        }
    }

    private void updateVendor(View view, String vendorName) {
        View vendor = view.findViewById(R.id.expense_vendor);
        TextView labelView = (TextView) vendor.findViewById(R.id.field_name);
        TextView valueView = (TextView) vendor.findViewById(R.id.field_value);
        TextView hintView = (TextView) vendor.findViewById(R.id.field_hint);

        if (!TextUtils.isEmpty(vendorName)) {
            valueView.setText(vendorName);
            labelView.setVisibility(View.VISIBLE);
            valueView.setVisibility(View.VISIBLE);
            hintView.setVisibility(View.GONE);
        } else {
            valueView.setText("");
            labelView.setVisibility(View.GONE);
            valueView.setVisibility(View.GONE);
            hintView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Will set the input length on a <code>EditText</code> view based on the data type backing this form.
     * 
     * @param editText
     *            the edit text.
     */
    protected void setEditTextInputLength(EditText editText, int length) {
        InputFilter.LengthFilter filter = new InputFilter.LengthFilter(length);
        InputFilter[] inputFilters = editText.getFilters();
        if (inputFilters == null) {
            inputFilters = new InputFilter[1];
            inputFilters[0] = filter;
        } else {
            // Attempt to replace all existing length filters with 'filter'.
            boolean replacedExistingLengthFilter = false;
            for (int filtInd = 0; filtInd < inputFilters.length; ++filtInd) {
                if (inputFilters[filtInd] instanceof InputFilter.LengthFilter) {
                    replacedExistingLengthFilter = true;
                    inputFilters[filtInd] = filter;
                }
            }
            if (!replacedExistingLengthFilter) {
                // If there was no replacement, then add onto the end.
                InputFilter[] replacementFilters = new InputFilter[inputFilters.length + 1];
                System.arraycopy(inputFilters, 0, replacementFilters, 0, inputFilters.length);
                replacementFilters[replacementFilters.length - 1] = filter;
                inputFilters = replacementFilters;
            }
        }
        // Reset the input filters.
        editText.setFilters(inputFilters);
    }

    private void setupVendorField(View view, int viewState) {
        View vendor = view.findViewById(R.id.expense_vendor);
        vendor.setVisibility(viewState);
        final TextView labelView = (TextView) vendor.findViewById(R.id.field_name);
        labelView.setText(R.string.vendor);
        View sep = view.findViewById(R.id.expense_vendor_fld_separator);
        sep.setVisibility(viewState);
        final EditText valueView = (EditText) vendor.findViewById(R.id.field_value);
        final TextView hintView = (TextView) vendor.findViewById(R.id.field_hint);
        hintView.setText(R.string.field_hint_add_vendor);

        if (viewState == View.VISIBLE) {
            if (expenseEntry != null && expenseEntry.getVendorName() != null
                    && expenseEntry.getVendorName().length() > 0) {

                valueView.setText(expenseEntry.getVendorName());
            } else {
                valueView.setText("");

                if (!vendorReadOnly) {
                    labelView.setVisibility(View.GONE);
                    valueView.setVisibility(View.GONE);
                    hintView.setVisibility(View.VISIBLE);
                    // Add a handler to launch the comment dialog.
                    hintView.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            if (!vendorReadOnly) {
                                hintView.setVisibility(View.GONE);
                                labelView.setVisibility(View.VISIBLE);
                                valueView.setVisibility(View.VISIBLE);
                                valueView.requestFocus();
                            }
                        }
                    });

                }

            }
            valueView.setSingleLine(false);

            if (!vendorReadOnly) {
                // Restrict vendor to 128 character per db column lenghth
                InputFilter.LengthFilter filter = new InputFilter.LengthFilter(128);
                InputFilter[] inputFilters = new InputFilter[1];
                inputFilters[0] = filter;
                valueView.setFilters(inputFilters);

                valueView.setOnFocusChangeListener(new OnFocusChangeListener() {

                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (!hasFocus) {
                            if (valueView.getText() == null || valueView.getText().length() == 0) {
                                hintView.setVisibility(View.VISIBLE);
                                labelView.setVisibility(View.GONE);
                                valueView.setVisibility(View.GONE);
                            }
                            if (isResumed()) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            }

                        } else {
                            if (!vendorReadOnly && isResumed()) {
                                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                        Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                            }
                        }
                    }
                });

            }
            // Enabled/disable the control.
            ViewUtil.setViewEnabled(view, R.id.expense_vendor, R.id.field_value, !vendorReadOnly);

            // Set max lines to display to 2. Needs to be done after setSingleLine() in setTextViewText
            valueView.setMaxLines(2);
            valueView.setMovementMethod(new ScrollingMovementMethod());
            valueView.setVerticalScrollBarEnabled(true);
        }
    }

    private void updateAmount(View view, String amount) {
        View amountView = view.findViewById(R.id.expense_amount);
        TextView labelView = (TextView) amountView.findViewById(R.id.field_name);
        TextView valueView = (TextView) amountView.findViewById(R.id.field_value);
        TextView hintView = (TextView) amountView.findViewById(R.id.field_hint);
        // TextView errView = (TextView) amountView.findViewById(R.id.field_note_frame);
        ViewUtil.setTextViewText(view, R.id.expense_amount, R.id.field_value, amount, true);
        if (!TextUtils.isEmpty(amount)) {
            labelView.setVisibility(View.VISIBLE);
            valueView.setVisibility(View.VISIBLE);
            hintView.setVisibility(View.GONE);
        } else {
            labelView.setVisibility(View.GONE);
            valueView.setVisibility(View.GONE);
            hintView.setVisibility(View.VISIBLE);
        }
    }

    private void setupAmountField(View view, int viewState) {
        View amountView = view.findViewById(R.id.expense_amount);
        amountView.setVisibility(viewState);
        View sep = view.findViewById(R.id.expense_amount_top_fld_separator);
        sep.setVisibility(viewState);
        // Use a bigger font
        final TextView valueView = (TextView) amountView.findViewById(R.id.field_value);
        if (valueView != null) {
            valueView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 28);
            valueView.setTypeface(null, Typeface.BOLD);
        }

        final TextView labelView = (TextView) amountView.findViewById(R.id.field_name);
        labelView.setText(R.string.amount);

        final TextView hintView = (TextView) amountView.findViewById(R.id.field_hint);
        hintView.setText(R.string.field_hint_add_amount);

        final View noteView = amountView.findViewById(R.id.field_note_frame);

        if (viewState == View.VISIBLE) {

            MobileEntryDAO mobileEntry = expenseEntry.cashTransaction;
            String transAmt = null;
            // Set the amount.
            if (expenseEntry != null && expenseEntry.type == ExpenseTypeEnum.CASH && mobileEntry != null
                    && (mobileEntry.getMeKey() == null || mobileEntry.getMeKey().length() == 0)
            /* TODO - Offline? && (mobileEntry.getLocalKey() == null) */) {
                // A new cash-based quick expense should have the amount field
                // cleared.
                ViewUtil.setTextViewText(view, R.id.expense_amount, R.id.field_value, "", true);
            } else if (expenseEntry != null && expenseEntry.getTransactionAmount() != null) {
                // An existing transaction with an amount value.
                transAmt = FormatUtil.formatAmount(expenseEntry.getTransactionAmount(), getResources()
                        .getConfiguration().locale, expenseEntry.getCrnCode(), false);
                ViewUtil.setTextViewText(view, R.id.expense_amount, R.id.field_value, transAmt, true);
            } else {
                ViewUtil.setTextViewText(view, R.id.expense_amount, R.id.field_value, "", true);
            }

            if (!amountReadOnly && (transAmt == null)) {
                labelView.setVisibility(View.GONE);
                valueView.setVisibility(View.GONE);
                hintView.setVisibility(View.VISIBLE);
                // Add a handler to launch the comment dialog.
                hintView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!amountReadOnly) {
                            hintView.setVisibility(View.GONE);
                            labelView.setVisibility(View.VISIBLE);
                            valueView.setVisibility(View.VISIBLE);
                            valueView.requestFocus();
                        }
                    }
                });

            }

            // Set the amount to be a numeric input only.
            if (valueView != null) {

                // Put in a proper key listener since Android is busted for
                // locale-specific decimal formatting
                valueView.setKeyListener(FormatUtil.getLocaleDecimalListener(getActivity()));
                // According to the API javadocs, we need to call this again to
                // regain focusability.
                valueView.setFocusable(true);
                valueView.setEnabled(!amountReadOnly);

                if (!amountReadOnly) {
                    valueView.setOnFocusChangeListener(new OnFocusChangeListener() {

                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (!hasFocus) {
                                if (TextUtils.isEmpty(valueView.getText())) {
                                    hintView.setVisibility(View.VISIBLE);
                                    labelView.setVisibility(View.GONE);
                                    valueView.setVisibility(View.GONE);
                                    noteView.setVisibility(View.GONE);
                                }

                                if (isResumed()) {
                                    // Check isResumed() to prevent keyboard popup after activity dismissed.
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                }

                            } else {
                                if (!amountReadOnly && isResumed()) {
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                                            Context.INPUT_METHOD_SERVICE);
                                    imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
                                }
                            }
                        }
                    });
                }

                // MOB-10783 - Add a listener to make sure amount is a valid value.
                valueView.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {

                        // need to handle view before buildView() completes.
                        if (getView() == null)
                            return;

                        View topView = getView().findViewById(R.id.expense_amount);
                        TextView noteView = (TextView) topView.findViewById(R.id.field_note);
                        if (s != null && noteView != null) {
                            String curValue = s.toString().trim();
                            // Check the amount and make sure it is less than 1
                            // quadrillion
                            Double finalAmount = FormatUtil.parseAmount(curValue, getActivity().getResources()
                                    .getConfiguration().locale);
                            // MOB-10928 - negative amounts are okay.
                            if (finalAmount == null) {
                                // Show notification.
                                String txt = com.concur.mobile.base.util.Format.localizeText(getActivity(),
                                        R.string.general_field_value_invalid, curValue);
                                noteView.setText(txt);
                                noteView.setTextAppearance(getActivity(), R.style.FormFieldNoteRedText);
                                ViewUtil.setVisibility(topView, R.id.field_note_frame, View.VISIBLE);
                                // setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
                            } else if (finalAmount > Double.valueOf(1000000000000000.00)) {
                                // Show notification.
                                String txt = com.concur.mobile.base.util.Format.localizeText(getActivity(),
                                        R.string.general_field_value_too_large, curValue);
                                noteView.setText(txt);
                                noteView.setTextAppearance(getActivity(), R.style.FormFieldNoteRedText);
                                ViewUtil.setVisibility(topView, R.id.field_note_frame, View.VISIBLE);
                                // setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
                            } else {
                                // Hide notification.
                                ViewUtil.setVisibility(topView, R.id.field_note_frame, View.GONE);
                                // setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_BLACK);
                            }
                        }
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                });

            } else {
                Log.e(Const.LOG_TAG, CLS_NAME
                        + ".setFieldNames: unable to locate expense amount field value text view!");
            }

        }
    }

    private void setupCurrencyField(View view, int viewState) {
        View currencyView = view.findViewById(R.id.expense_currency);
        currencyView.setVisibility(viewState);
        View sep = view.findViewById(R.id.expense_currency_fld_separator);
        sep.setVisibility(viewState);

        ImageView iconView = (ImageView) currencyView.findViewById(R.id.field_icon);
        iconView.setImageResource(R.drawable.expense_icon_card_grey);
        TextView labelView = (TextView) currencyView.findViewById(R.id.field_name);
        labelView.setText(R.string.currency);

        if (viewState == View.VISIBLE) {
            // Set the currency.
            // setRequiredTextView(R.id.expense_currency, R.id.field_name, getText(R.string.currency).toString());
            ViewUtil.setViewEnabled(view, R.id.expense_currency, R.id.field_value, !currencyReadOnly);

            if (!currencyReadOnly) {
                currencyView.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        removeEditFocus(); // Remove cursor focus from edit field

                        CurrencyDialogFragment df = new CurrencyDialogFragment();
                        df.show(getActivity().getSupportFragmentManager(), CurrencyDialogFragment.DIALOG_FRAGMENT_ID);
                    }

                });
            } else {
                // Read-only, so remove the arrow indicator.
                ViewUtil.setVisibility(view, R.id.field_image, View.INVISIBLE);
            }

        }
    }

    private void setupLocationField(View view, int viewState) {
        View locView = view.findViewById(R.id.expense_location);
        locView.setVisibility(viewState);
        View sep = view.findViewById(R.id.expense_location_fld_separator);
        sep.setVisibility(viewState);

        ImageView iconView = (ImageView) locView.findViewById(R.id.field_icon);
        iconView.setImageResource(R.drawable.expense_icon_locator);

        if (viewState == View.VISIBLE) {

            // Set the location.
            ViewUtil.setTextViewText(view, R.id.expense_location, R.id.field_name, getText(R.string.location)
                    .toString(), true);
            view = view.findViewById(R.id.expense_location);
            if (view != null && !locationReadOnly) {
                // Set up the click listener to launch the location search activity.
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        removeEditFocus(); // Remove cursor focus from edit field

                        if (locationIntent == null) {
                            locationIntent = new Intent(getActivity(), ListSearch.class);
                            locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_IS_MRU, true);
                            locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FIELD_ID, "LocName");
                            locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_FT_CODE, "RPTINFO");
                            locationIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_TITLE, getText(R.string.location)
                                    .toString());
                        }
                        startActivityForResult(locationIntent, Const.REQUEST_CODE_LOCATION);
                    }
                });
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".setFieldNames: unable to locate 'expense_location' view!");
            }
            if (expenseEntry != null) {
                String location = expenseEntry.getLocationName();
                if (location != null) {
                    ViewUtil.setTextViewText(view, R.id.expense_location, R.id.field_value, location, true);
                } else {
                    ViewUtil.setTextViewText(view, R.id.expense_location, R.id.field_value, "", true);
                }
            } else {
                ViewUtil.setTextViewText(view, R.id.expense_location, R.id.field_value, "", true);
            }
            // Enable/Disable location name.
            ViewUtil.setViewEnabled(view, R.id.expense_location, R.id.field_value, !locationReadOnly);

        }
    }

    private void updateComment(View view, String comment) {
        View comView = view.findViewById(R.id.expense_comment);
        TextView valueView = (TextView) comView.findViewById(R.id.field_value);

        if (!TextUtils.isEmpty(comment)) {
            valueView.setText(comment);
            TextView labelView = (TextView) comView.findViewById(R.id.field_name);
            labelView.setVisibility(View.VISIBLE);
            TextView hintView = (TextView) comView.findViewById(R.id.field_hint);
            hintView.setVisibility(View.GONE);
        } else {
            valueView.setText("");
            if (!commentReadOnly) {
                TextView labelView = (TextView) comView.findViewById(R.id.field_name);
                labelView.setVisibility(View.GONE);
                TextView hintView = (TextView) comView.findViewById(R.id.field_hint);
                hintView.setText(R.string.field_hint_add_comment);
                hintView.setVisibility(View.VISIBLE);
            }

        }

    }

    private void setupCommentField(View view, int viewState) {
        View comView = view.findViewById(R.id.expense_comment);
        comView.setVisibility(viewState);
        ImageView iconView = (ImageView) comView.findViewById(R.id.field_icon);
        iconView.setImageResource(R.drawable.expense_icon_comment);

        if (viewState == View.VISIBLE) {

            // Set the comment.
            ViewUtil.setTextViewText(view, R.id.expense_comment, R.id.field_name, getText(R.string.comment).toString(),
                    true);
            if (expenseEntry != null) {
                String comment = expenseEntry.getComment();
                if (!TextUtils.isEmpty(comment)) {
                    ViewUtil.setTextViewText(view, R.id.expense_comment, R.id.field_value, comment, false);
                } else {
                    ViewUtil.setTextViewText(view, R.id.expense_comment, R.id.field_value, "", false);
                    if (!commentReadOnly) {
                        TextView labelView = (TextView) comView.findViewById(R.id.field_name);
                        labelView.setVisibility(View.GONE);
                        TextView hintView = (TextView) comView.findViewById(R.id.field_hint);
                        hintView.setText(R.string.field_hint_add_comment);
                        hintView.setVisibility(View.VISIBLE);
                    }

                }
                TextView valueView = (TextView) comView.findViewById(R.id.field_value);
                valueView.setMaxLines(2);
            }
            // Add a handler to launch the comment dialog.
            comView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    removeEditFocus(); // Remove cursor focus from edit field

                    if (!commentReadOnly) {
                        CommentDialogFragment df = new CommentDialogFragment();
                        df.show(getActivity().getSupportFragmentManager(), CommentDialogFragment.DIALOG_FRAGMENT_ID);
                    }
                }
            });
        }
    }

    public void save() {

        if (this.isOCR()) {

        } else {
            MobileEntryDAO mobileEntry = this.expenseEntry.getMobileEntryToSave();
            saveMobileEntry(mobileEntry);
        }

    }

    /*
     * Save a mobile entry for out of pocket, cct or pct expense
     */
    public void saveMobileEntry(MobileEntryDAO mobileEntry) {

        if (mobileEntry != null) {

            // Pull the values from the screen controls.
            Calendar transDate = datePickerDate;
            String transDateStr = Format.safeFormatCalendar(FormatUtil.XML_DF, datePickerDate);
            String vendorName = ViewUtil.getTextViewText(getView(), R.id.expense_vendor, R.id.field_value).trim();
            String locationName = ViewUtil.getTextViewText(getView(), R.id.expense_location, R.id.field_value).trim();
            String amount = ViewUtil.getTextViewText(getView(), R.id.expense_amount, R.id.field_value).trim();

            if (transDate == null) {
                getActivity().showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_TRANSACTION_DATE);
                return;
            }
            if (selExpKey == null) {
                View view = getView().findViewById(R.id.expense_type);
                if (view != null) {
                    view.requestFocus();
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".save: unable to locate expense_type view!");
                }
                getActivity().showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_TYPE);
                return;
            }
            if (selCurType == null) {
                getActivity().showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_CURRENCY);
                return;
            }
            Double transAmt = null;
            if (amount == null || amount.length() == 0) {
                View amountView = ViewUtil.findSubView(getView(), R.id.expense_amount, R.id.field_value);
                amountView.requestFocus();
                getActivity().showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT);
                return;
            } else {
                transAmt = FormatUtil.parseAmount(amount, getResources().getConfiguration().locale);
                if (transAmt == null) {
                    View amountView = ViewUtil.findSubView(getView(), R.id.expense_amount, R.id.field_value);
                    amountView.requestFocus();
                    getActivity().showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT);
                    return;
                }
            }

            // Set the expense date.
            mobileEntry.setTransactionDate(datePickerDate);

            // Set the expense type.
            mobileEntry.setExpKey(selExpKey);
            mobileEntry.setExpName(selExpName);

            // Set the vendor name.
            mobileEntry.setVendorName(vendorName);

            // Set the currency code.
            mobileEntry.setCrnCode(selCurType.code);

            // Set the location name.
            mobileEntry.setLocationName(locationName);

            // Set the amount.
            mobileEntry.setTransactionAmount(transAmt);

            // Set the comment.
            String comment = ViewUtil.getTextViewText(getView(), R.id.expense_comment, R.id.field_value);
            if (!TextUtils.isEmpty(comment)) {
                mobileEntry.setComment(comment);
            } else {
                mobileEntry.setComment(null);
            }

            // TODO? // Set the update time.
            // mobileEntry.setUpdateDate(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            // Update the receipt image id if set.
            if (receiptImageId != null) {
                mobileEntry.setReceiptImageId(receiptImageId);
            }

            // Save the mobile entry DAO
            SessionInfo sessInfo = ConfigUtil.getSessionInfo(getActivity());
            mobileEntry.update(getActivity(), sessInfo.getUserId());

            // If the last receipt action was either choose or take picture,
            // then first save the receipt.
            // The quick expense will be saved after saving the receipt.
            if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE
                    || lastReceiptAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
                sendSaveReceiptRequest(receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
            } else {
                sendSaveExpenseRequest(mobileEntry);
            }

            if (locationName != null && locationName.length() > 0) {
                ExpensePreferenceUtil.saveLocationSelection(getActivity(), locLiKey, locLiCode, locationName);
            }

            if (selCurType != null) {
                ExpensePreferenceUtil.saveLastUsedCrnCode(getActivity(), selCurType.code);
            }

            // Log the event
            if (mobileEntry.getMeKey() == null /* && mobileEntry.getLocalKey() == null */) {
                EventTracker.INSTANCE.track(getClass().getSimpleName(), "Create Mobile Entry");
            } else {
                EventTracker.INSTANCE.track(getClass().getSimpleName(), "Save Mobile Entry");
            }
        }
    }

    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#onCreateDialog(int)
    // */
    // @Override
    // public Dialog onCreateDialog(int id) {
    // Dialog dialog = null;
    // switch (id) {
    // case DIALOG_RECEIPT_IMAGE: {
    // AlertDialog.Builder builder = new AlertDialog.Builder(this);
    // builder.setTitle(getText(R.string.expense_receipt_options));
    // receiptActionAdapter = new ReceiptImageOptionListAdapter();
    // builder.setSingleChoiceItems(receiptActionAdapter, -1, new ReceiptImageDialogListener());
    // dialog = builder.create();
    // break;
    // }

    // case DIALOG_SAVE_RECEIPT: {
    // ProgressDialog progDlg = new ProgressDialog(this);
    // progDlg.setMessage(getText(R.string.saving_receipt));
    // progDlg.setIndeterminate(true);
    // progDlg.setOnCancelListener(new OnCancelListener() {
    //
    // @Override
    // public void onCancel(DialogInterface dialog) {
    // if (saveReceiptRequest != null) {
    // saveReceiptRequest.cancel();
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".onCancel(SaveReceiptDialog): saveReceiptRequest is null!");
    // }
    // }
    // });
    // dialog = progDlg;
    // break;
    // }
    // case Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND: {
    // AlertDialog.Builder builder = new AlertDialog.Builder(this);
    // builder.setTitle(getText(R.string.dlg_append_receipt_confirmation_title));
    // builder.setMessage(getText(R.string.dlg_append_receipt_confirmation_message));
    // builder.setPositiveButton(getText(R.string.general_append), new Dialog.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // // Dismiss the dialog and start the save.
    // dialog.dismiss();
    //
    // // Check whether the previous receipt data is local
    // // only.
    // if (previousReceiptImageDataLocalFilePath != null) {
    // // Set the 'savingForAppend' flag to true and
    // // kick-off a receipt save request.
    // savingForAppend = true;
    // sendSaveReceiptRequest(previousReceiptImageDataLocalFilePath, true);
    // // Show the saving receipt dialog id.
    // showDialog(DIALOG_SAVE_RECEIPT);
    // } else if (receiptImageDataLocalFilePath != null) {
    // // Set the 'savingForAppend' flag to true and
    // // kick-off a receipt save request.
    // savingForAppend = true;
    // sendSaveReceiptRequest(receiptImageDataLocalFilePath, true);
    // // Show the saving receipt dialog id.
    // showDialog(DIALOG_SAVE_RECEIPT);
    // } else if (previousReceiptImageId != null && receiptImageId != null) {
    // sendAppendReceiptRequest(receiptImageId, previousReceiptImageId);
    // // Show the saving receipt dialog id.
    // showDialog(DIALOG_SAVE_RECEIPT);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG
    // + ".onCreateDialog(APPEND).onClick: missing previous/current receipt data!");
    // }
    // }
    // });
    // builder.setNegativeButton(getText(R.string.general_replace), new Dialog.OnClickListener() {
    //
    // @Override
    // public void onClick(DialogInterface dialog, int which) {
    // dialog.dismiss();
    // removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath, true);
    // previousReceiptImageDataLocalFilePath = null;
    // previousReceiptImageId = null;
    // if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE
    // || lastReceiptAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
    // receiptImageId = null;
    // } else if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD) {
    // receiptImageDataLocalFilePath = null;
    // }
    // }
    // });
    // dialog = builder.create();
    // dialog.setCancelable(true);
    // dialog.setOnCancelListener(new OnCancelListener() {
    //
    // @Override
    // public void onCancel(DialogInterface dialog) {
    // resetPreviousReceiptImageDataValues();
    // }
    // });
    // break;
    // }
    // default: {
    // dialog = super.onCreateDialog(id);
    // break;
    // }
    // }
    // return dialog;
    // }

    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#onPrepareDialog(int, android.app.Dialog)
    // */
    // @Override
    // public void onPrepareDialog(int id, Dialog dialog) {
    // switch (id) {
    // case Const.DIALOG_EXPENSE_SAVE_FAILED: {
    // AlertDialog alertDlg = (AlertDialog) dialog;
    // alertDlg.setMessage(actionStatusErrorMessage);
    // break;
    // }
    // case DIALOG_COMMENT: {
    // String comment = ViewUtil.getTextViewText(this, R.id.expense_comment, R.id.field_value);
    // if (textEdit != null) {
    // String txtVal = (comment != null) ? comment : "";
    // if (lastChangedText != null) {
    // txtVal = lastChangedText;
    // }
    // textEdit.setText(txtVal);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: textEdit is null!");
    // }
    // break;
    // }
    // case DIALOG_EXPENSE_TYPE: {
    // // No-op.
    // break;
    // }
    // case DIALOG_EXPENSE_CURRENCY: {
    // break;
    // }
    // case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
    // AlertDialog alertDlg = (AlertDialog) dialog;
    // alertDlg.setMessage(actionStatusErrorMessage);
    // break;
    // }
    // }
    // }
    //
    private void showCalendarDialog() {
        Bundle bundle;

        datePickerDlg = new CalendarPickerDialog();
        bundle = new Bundle();

        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_YEAR, datePickerDate.get(Calendar.YEAR));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_MONTH, datePickerDate.get(Calendar.MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_INITIAL_DAY, datePickerDate.get(Calendar.DAY_OF_MONTH));
        bundle.putInt(CalendarPickerDialog.KEY_TEXT_COLOR, 0xFF000000);

        datePickerDlg.setOnDateSetListener(new DatePickerDialogListener());
        datePickerDlg.setArguments(bundle);
        datePickerDlg.show(getFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);

    }

    /**
     * Sets the text on the date picker button to the latest value in <code>datePickerDate</code>.
     */
    private void updateDatePickerFieldValue(View mainView) {
        View view = mainView.findViewById(R.id.expense_date);
        if (view != null) {
            TextView txtView = (TextView) view.findViewById(R.id.field_value);
            if (txtView != null) {
                String dateStr = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY, datePickerDate);
                txtView.setText(dateStr);
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".updateDatePickerButtonText: can't find date text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_NAME + ".updateDatePickerFieldValue: can't locate expense date field!");
        }
    }

    // /**
    // * Will choose a picture from the devices media gallery.
    // */
    // private void chooseReceiptPicture() {
    // if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
    //
    // // Hang-on to current values.
    // setPreviousReceiptImageDataValues();
    //
    // Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
    // startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
    // } else {
    // showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
    // }
    //
    // }
    //
    // /**
    // * Selects a receipt image from one in the receipt store cloud for use at the report level.
    // */
    // protected void selectCloudReceipt() {
    //
    // // Hang-on to current values.
    // setPreviousReceiptImageDataValues();
    //
    // Intent intent = new Intent(this, ExpensesAndReceipts.class);
    // intent.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
    // intent.setAction(Intent.ACTION_PICK);
    // intent.putExtra(Const.EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY, true);
    // intent.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_MOBILE_ENTRY);
    // startActivityForResult(intent, REQUEST_CHOOSE_CLOUD_IMAGE);
    // }
    //
    // /**
    // * Will clear the current receipt picture.
    // */
    // private void clearReceiptPicture() {
    // lastReceiptAction = ReceiptPictureSaveAction.CLEAR_PICTURE;
    //
    // // Ensure a previous copied picture is removed.
    // removePreviousCopiedPicture(receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
    // receiptImageDataLocalFilePath = null;
    // deleteReceiptImageDataLocalFilePath = false;
    //
    // // Clear the receipt image id.
    // receiptImageId = null;
    //
    // // Clear the local data path
    // receiptImageDataLocalFilePath = null;
    // if (mobileEntry != null) {
    // // For offline expenses we need to clear this as well since the
    // // entry object would not otherwise be updated until save
    // mobileEntry.setReceiptImageDataLocalFilePath(null);
    // mobileEntry.setReceiptImageDataLocal(false);
    // mobileEntry.setReceiptImageId(null);
    // }
    //
    // // Configure the receipt label.
    // configureReceiptLabel();
    // }
    //
    // /**
    // * Will download the receipt image from the server.
    // */
    // private void downloadReceiptPicture() {
    // lastReceiptAction = ReceiptPictureSaveAction.DOWNLOAD_PICTURE;
    // // Show the dialog.
    // showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
    // // Request the service download the receipt image.
    // ConcurService concurService = getConcurService();
    // concurService.downloadMobileEntryReceipt(mobileEntry);
    // }
    //
    // /**
    // * Will launch the 'ViewImage' activity to either display a locally captured/selected image or download the receipt image.
    // */
    // protected void viewReceiptPicture() {
    // // Launch an intent to view an expense receipt.
    // Intent intent = new Intent(this, ViewImage.class);
    //
    // boolean requiresConnectivity = false;
    //
    // // First check for locally captured/selected (from device gallery)
    // // picture.
    // if (receiptImageDataLocalFilePath != null) {
    // // Current locally captured/selected (from device gallery) picture.
    // StringBuilder strBldr = new StringBuilder("file://");
    // strBldr.append(receiptImageDataLocalFilePath);
    // intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
    // } else if (receiptImageId != null) {
    // // Current selected (from receipt store) picture.
    // intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, receiptImageId);
    // intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
    // intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
    // requiresConnectivity = true;
    // } else if (mobileEntry.getReceiptImageDataLocalFilePath() != null) {
    // // Locally saved receipt image file, not yet saved to the server.
    // StringBuilder strBldr = new StringBuilder("file://");
    // strBldr.append(mobileEntry.getReceiptImageDataLocalFilePath());
    // intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
    // } else if (mobileEntry.getReceiptImageId() != null) {
    // // Referenced receipt id from the receipt store.
    // intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, mobileEntry.getReceiptImageId());
    // intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
    // intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
    // requiresConnectivity = true;
    // } else {
    // String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
    // StringBuilder strBldr = new StringBuilder();
    // strBldr.append(serverAdd);
    // strBldr.append(DownloadMobileEntryReceiptRequest.getServiceEndPointURI(mobileEntry.getMeKey()));
    // String urlStr = strBldr.toString();
    // intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, urlStr);
    // requiresConnectivity = true;
    // }
    //
    // if (!ConcurCore.isConnected() && requiresConnectivity) {
    // showDialog(Const.DIALOG_NO_CONNECTIVITY);
    // } else {
    // // Launch the activity.
    // startActivity(intent);
    // }
    // }
    //
    /**
     * Will take a new receipt picture.
     */
    private void takeReceiptPicture() {
        Log.d(Const.LOG_TAG, CLS_NAME + ".takeReceiptPicture: ");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            // Create a file name based on the current date.
            String receiptFilePath = ImageUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, CLS_NAME + ".takeReceiptPicture: receipt image path -> '"
                    + receiptCameraImageDataLocalFilePath + "'.");

            // Hang-on to current values.
            setPreviousReceiptImageDataValues();

            // Launch the camera application.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            try {
                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (Exception e) {
                // Device has no camera, see MOB-16872
            }
        } else {
            getActivity().showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    //
    // /**
    // * Will create a new list of <code>ExpenseType</code> objects with one custom category containing the expense type defined
    // by
    // * <code>expKey</code> and <code>expName</code>.
    // *
    // * @param expKey
    // * the custom expense key.
    // * @param expName
    // * the custom expense name.
    // * @return a list of <code>ExpenseType</code> with the first entry being an instance of <code>ExpenseTypeCategory</code> and
    // * the second being an instance of <code>ExpenseType</code>.
    // */
    // private List<ExpenseType> createCustomExpenseType(String expKey, String expName) {
    // List<ExpenseType> customExpenseType = null;
    //
    // customExpenseType = new ArrayList<ExpenseType>();
    // ExpenseTypeCategory expTypeCat = new ExpenseTypeCategory(getText(R.string.general).toString(),
    // R.drawable.help_24);
    // customExpenseType.add(expTypeCat);
    // ExpenseType expType = new ExpenseType(expName, expKey);
    // expTypeCat.addExpenseType(expType);
    // customExpenseType.add(expType);
    // return customExpenseType;
    // }
    //
    /**
     * Will inititalize the state of the UI.
     * 
     * @param inState
     *            the bundle containing state information.
     */
    private void initState(Bundle inState, View mainView) {
        if (inState != null) {

            // Restore the transaction date.
            String transDateStr = inState.getString(DATE_KEY);
            if (transDateStr != null && transDateStr.length() > 0) {
                datePickerDate = Parse.parseXMLTimestamp(transDateStr);
                updateDatePickerFieldValue(mainView);
            }
            // Restore the expense type.
            String expTypeKey = inState.getString(EXPENSE_TYPE_KEY);
            String expTypeName = inState.getString(EXPENSE_TYPE_NAME);
            // Note: Expense object has both expense type key and name and we don't need the adapter to populate the field
            if (expTypeKey != null && expTypeKey.length() > 0 && expTypeName != null && expTypeName.length() > 0) {
                setSelectedExpenseType(mainView, expTypeKey, expTypeName);
            }
            // Restore the vendor name.
            String vendorName = inState.getString(VENDOR_KEY);
            if (vendorName != null) {
                updateVendor(mainView, vendorName);
            }
            // Restore the location name.
            String locationName = inState.getString(LOCATION_KEY);
            if (locationName != null) {
                ViewUtil.setTextViewText(mainView, R.id.expense_location, R.id.field_value, locationName, true);
            }
            // Restore the currency type.
            String curCode = inState.getString(CURRENCY_KEY);
            if (curCode != null) {
                int curTypeInd = curTypeAdapter.getPositionForCurrency(curCode);
                if (curTypeInd != -1) {
                    Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                    if (curTypeObj instanceof ListItem) {
                        setSelectedCurrencyType((ListItem) curTypeObj, mainView);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_NAME + ".onRestoreInstanceState: invalid currency type position!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".onRestoreInstanceState: restored currency type not found!");
                }
            }
            // Restore the amount.
            String amount = inState.getString(AMOUNT_KEY);
            if (amount != null) {
                updateAmount(mainView, amount);
            }
            // Restore the comment.
            String comment = inState.getString(COMMENT_KEY);
            updateComment(mainView, comment);

            // Restore receipt image id.
            receiptImageId = inState.getString(RECEIPT_IMAGE_ID_KEY);
            // Restore whether the receipt image should be punted post save.
            deleteReceiptImageDataLocalFilePath = inState.getBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH);
            // Restore the receipt image file path.
            String restoreImageFilePath = inState.getString(RECEIPT_IMAGE_FILE_PATH_KEY);
            // If our receipt image file path is 'null', then just assign it to
            // the one
            // restored.
            if (receiptImageDataLocalFilePath == null) {
                receiptImageDataLocalFilePath = restoreImageFilePath;
            }

            // Restore the receipt camera image file path.
            receiptCameraImageDataLocalFilePath = inState.getString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY);
            // Restore the last receipt action.
            lastReceiptAction = ReceiptPictureSaveAction.valueOf(inState.getString(LAST_RECEIPT_ACTION_KEY));

            // Restore location selection information.
            if (inState.containsKey(LOCATION_SELECTION_LICODE_KEY)) {
                locLiCode = inState.getString(LOCATION_SELECTION_LICODE_KEY);
            }
            if (inState.containsKey(LOCATION_SELECTION_LIKEY_KEY)) {
                locLiKey = inState.getString(LOCATION_SELECTION_LIKEY_KEY);
            }
            if (inState.containsKey(LOCATION_SELECTION_VALUE_KEY)) {
                locValue = inState.getString(LOCATION_SELECTION_VALUE_KEY);
            }

            // Restore the "previous receipt image data local file path".
            previousReceiptImageDataLocalFilePath = inState.getString(PREVIOUS_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH_KEY);
            // Restore the "previous receipt image id".
            inState.getString(PREVIOUS_RECEIPT_IMAGE_ID_KEY);
            // Restore the "saving for append" value.
            savingForAppend = inState.getBoolean(SAVING_FOR_APPEND_KEY);
            // Restore the "previous receipt image id from mobile entry".
            previousReceiptImageIdFromMobileEntry = inState.getBoolean(PREVIOUS_RECEIPT_IMAGE_ID_FROM_MOBILE_ENTRY);

            lastChangedText = inState.getString(DIALOG_LAST_CHANGED_TEXT_KEY);
        }

        // Initialize thumbnail here
        displayThumbnail(mainView);

        // // MOB-15824
        // // In QuickExpense, view is built before state is initialized, and we don't have values set until state is initialized.
        // // Set text color for required fields without default values.
        // setRequiredFieldLabelColors();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onSaveInstanceState(android.os .Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {

        super.onSaveInstanceState(outState);

        savedInstanceState = true;
        // Save the expense date.
        String transDateStr = Format.safeFormatCalendar(FormatUtil.XML_DF, datePickerDate);
        outState.putString(DATE_KEY, transDateStr);
        // Save the expense type.
        if (selExpKey != null) {
            outState.putString(EXPENSE_TYPE_KEY, selExpKey);
        }
        if (selExpName != null) {
            outState.putString(EXPENSE_TYPE_NAME, selExpName);
        }

        // Save the vendor.
        String vendorName = ViewUtil.getTextViewText(getView(), R.id.expense_vendor, R.id.field_value);
        if (!TextUtils.isEmpty(vendorName)) {
            vendorName = vendorName.trim();
        }
        outState.putString(VENDOR_KEY, vendorName);
        // Save the location.
        String locationName = ViewUtil.getTextViewText(getView(), R.id.expense_location, R.id.field_value);
        if (locationName != null && locationName.length() > 0) {
            locationName = locationName.trim();
        }
        outState.putString(LOCATION_KEY, locationName);
        // Save the currency type.
        if (selCurType != null) {
            outState.putString(CURRENCY_KEY, selCurType.code);
        }
        // Set the amount.
        String amount = ViewUtil.getTextViewText(getView(), R.id.expense_amount, R.id.field_value);
        if (!TextUtils.isEmpty(amount)) {
            amount = amount.trim();
        }
        outState.putString(AMOUNT_KEY, amount);
        // Set the comment.
        String comment = ViewUtil.getTextViewText(getView(), R.id.expense_comment, R.id.field_value);
        if (!TextUtils.isEmpty(comment)) {
            comment = comment.trim();
        }
        outState.putString(COMMENT_KEY, comment);
        // Save receipt image id.
        outState.putString(RECEIPT_IMAGE_ID_KEY, receiptImageId);
        // Save the local receipt image data file path.
        outState.putString(RECEIPT_IMAGE_FILE_PATH_KEY, receiptImageDataLocalFilePath);
        // Save the boolean flag indicating whether the receipt should be punted
        // post save.
        outState.putBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH, deleteReceiptImageDataLocalFilePath);
        // Save the local camera receipt image data file path.
        outState.putString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY, receiptCameraImageDataLocalFilePath);
        // Save the last receipt action.
        outState.putString(LAST_RECEIPT_ACTION_KEY, lastReceiptAction.name());

        // Save the "previous receipt image data local file path".
        outState.putString(PREVIOUS_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH_KEY, previousReceiptImageDataLocalFilePath);
        // Save the "previous receipt image id".
        outState.putString(PREVIOUS_RECEIPT_IMAGE_ID_KEY, previousReceiptImageId);
        // Save the "saving for append" value.
        outState.putBoolean(SAVING_FOR_APPEND_KEY, savingForAppend);
        // Save the "previous receipt image id from mobile entry".
        outState.putBoolean(PREVIOUS_RECEIPT_IMAGE_ID_FROM_MOBILE_ENTRY, previousReceiptImageIdFromMobileEntry);

        // Save any location selection information.
        // List item code.
        if (locLiCode != null) {
            outState.putString(LOCATION_SELECTION_LICODE_KEY, locLiCode);
        }
        // List item key.
        if (locLiKey != null) {
            outState.putString(LOCATION_SELECTION_LIKEY_KEY, locLiKey);
        }
        // Location value.
        if (locValue != null) {
            outState.putString(LOCATION_SELECTION_VALUE_KEY, locValue);
        }

        if (lastChangedText != null) {
            outState.putString(DIALOG_LAST_CHANGED_TEXT_KEY, lastChangedText);
        }

    }

    /**
     * Will set 'receiptImageDataLocalFilePath' and 'receiptImageId' to the values of 'previousReceiptImageDataLocalFilePath' and
     * 'previousReceiptImageId'; respectfully.
     */
    private void resetPreviousReceiptImageDataValues() {
        receiptImageDataLocalFilePath = previousReceiptImageDataLocalFilePath;
        if (!previousReceiptImageIdFromMobileEntry) {
            receiptImageId = previousReceiptImageId;
        }
        previousReceiptImageDataLocalFilePath = null;
        previousReceiptImageId = null;
        savingForAppend = false;
        previousReceiptImageIdFromMobileEntry = false;
    }

    /**
     * Will set 'previousReceiptImageDataLocalFilePath' and 'previousReceiptImageId' to 'receiptImageDataLocalFilePath' and
     * 'receiptImageId'; respectfully.
     */
    private void setPreviousReceiptImageDataValues() {
        previousReceiptImageDataLocalFilePath = receiptImageDataLocalFilePath;
        receiptImageDataLocalFilePath = null;
        previousReceiptImageId = receiptImageId;
        receiptImageId = null;
        if (previousReceiptImageDataLocalFilePath == null && previousReceiptImageId == null) {
            if (!isMobileEntryOfflineReceiptData() && expenseEntry.getReceiptImageId() != null) {
                previousReceiptImageId = expenseEntry.getReceiptImageId();
                previousReceiptImageIdFromMobileEntry = true;
            }
        }
        savingForAppend = false;
    }

    /**
     * Clears (sets to null) 'previousReceiptImageDataLocalFilePath', 'previousReceiptImageId' and
     * 'receiptImageDataLocalFilePath'.
     */
    private void clearReceiptImageDataValues() {
        previousReceiptImageDataLocalFilePath = null;
        previousReceiptImageId = null;
        savingForAppend = false;
        previousReceiptImageIdFromMobileEntry = false;
        receiptImageDataLocalFilePath = null;
    }

    /**
     * Gets whether or not there are previous values for receipt image data.
     * 
     * @return whether or not there are previous values for receipt image data.
     */
    private boolean hasPreviousReceiptImageDataValues() {
        boolean retVal = false;
        retVal = ((previousReceiptImageDataLocalFilePath != null) || (previousReceiptImageId != null));
        return retVal;
    }

    /**
     * Gets whether or not the mobile entry has offine receipt data that has not been uploaded.
     * 
     * @return returns whether or not the mobile entry has offline receipt data that has not been uploaded.
     */
    private boolean isMobileEntryOfflineReceiptData() {
        boolean retVal = (/* TODO: mobileEntry.hasReceiptImageDataLocal() || */expenseEntry
                .getReceiptImageDataLocalFilePath() != null);
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if (!buildViewDelay) {
        // Log.d(Const.LOG_TAG, CLS_NAME + ".onActivityResult: build view present, handling result.");
        if (requestCode == REQUEST_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                receiptCaptureSuccess(data);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(getActivity(), getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                toast.show();
                resetPreviousReceiptImageDataValues();
            } else {
                Log.d(Const.LOG_TAG, CLS_NAME + "onActivityResult(TakePicture): unhandled result code '" + resultCode
                        + "'.");
            }
        } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (!copySelectedImage(data)) {
                    // Flurry Notification.
                    Map<String, String> params = new HashMap<String, String>();
                    params.put(Flurry.PARAM_NAME_FAILURE,
                            Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                    getActivity().showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                    resetPreviousReceiptImageDataValues();
                    // configureReceiptLabel();
                } else {
                    // Set the last receipt action.
                    lastReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;

                    // configureReceiptLabel();
                    // Only prompt for receipt append if we're currently
                    // connected, the mobile entry does not
                    // have offline receipt data and a previous image was
                    // captured/selected from Gallery/Receipt Store.
                    if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                        if (hasPreviousReceiptImageDataValues()) {
                            ReceiptAppendConfirmDialogFragment df = new ReceiptAppendConfirmDialogFragment();
                            df.show(getActivity().getSupportFragmentManager(),
                                    ReceiptAppendConfirmDialogFragment.DIALOG_FRAGMENT_ID);
                        }
                    } else {
                        // Delete the previously copied picture.
                        removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath,
                                deleteReceiptImageDataLocalFilePath);
                        previousReceiptImageDataLocalFilePath = null;
                        previousReceiptImageId = null;
                        deleteReceiptImageDataLocalFilePath = false;
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(this.getActivity(), getText(R.string.activity_canceled),
                        Toast.LENGTH_SHORT);
                toast.show();
                resetPreviousReceiptImageDataValues();
            } else {
                Log.d(Const.LOG_TAG, CLS_NAME + "onActivityResult(ChoosePicture): unhandled result code '" + resultCode
                        + "'.");
            }
        } else if (requestCode == REQUEST_CHOOSE_CLOUD_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                receiptImageId = data.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                if (receiptImageId != null) {
                    // Set the receipt action flag.
                    lastReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD;

                    // configureReceiptLabel();
                    // Only prompt for receipt append if we're currently
                    // connected, the mobile entry does not
                    // have offline receipt data and a previous image was
                    // captured/selected from Gallery/Receipt Store.
                    if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                        if (hasPreviousReceiptImageDataValues()) {
                            ReceiptAppendConfirmDialogFragment df = new ReceiptAppendConfirmDialogFragment();
                            df.show(getActivity().getSupportFragmentManager(),
                                    ReceiptAppendConfirmDialogFragment.DIALOG_FRAGMENT_ID);
                        }
                    } else {
                        // Delete the previously copied picture.
                        removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath,
                                deleteReceiptImageDataLocalFilePath);
                        previousReceiptImageDataLocalFilePath = null;
                        previousReceiptImageId = null;
                        deleteReceiptImageDataLocalFilePath = false;
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME
                            + ".onActivityResult(ChooseCloudPicture): ok result intent missing receipt image id!");
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast toast = Toast.makeText(this.getActivity(), getText(R.string.activity_canceled),
                        Toast.LENGTH_SHORT);
                toast.show();
                resetPreviousReceiptImageDataValues();
            } else {
                Log.d(Const.LOG_TAG, CLS_NAME + "onActivityResult(ChooseCloudImage): unhandled result code '"
                        + resultCode + "'.");
            }
        } else if (requestCode == Const.REQUEST_CODE_LOCATION) {
            if (resultCode == Activity.RESULT_OK) {
                String selectedListItemKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_KEY);
                String selectedListItemCode = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CODE);
                String selectedListItemText = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_TEXT);
                String crnCode = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CRN_CODE);
                String crnKey = data.getStringExtra(Const.EXTRA_EXPENSE_LIST_SELECTED_LIST_ITEM_CRN_KEY);
                if (selectedListItemKey != null || selectedListItemCode != null) {
                    locLiCode = selectedListItemCode;
                    locLiKey = selectedListItemKey;
                    locValue = selectedListItemText;
                    locCrnCode = crnCode;
                    locCrnKey = crnKey;
                    ViewUtil.setTextViewText(ExpenseDetailFragment.this.getView(), R.id.expense_location,
                            R.id.field_value, locValue, true);
                    // MOB-11190 Automatically update the
                    // Currency field
                    // to the selected Country/currency.
                    if (crnCode != null && !currencyReadOnly) {
                        setSelectedCurrencyType(crnCode, getView());
                    }

                } else {
                    ViewUtil.setTextViewText(ExpenseDetailFragment.this.getView(), R.id.expense_location,
                            R.id.field_value, "", true);
                }
            }
        }
        // } else {
        // Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view delayed, delaying handling of result.");
        // activityResultDelay = true;
        // activityResultRequestCode = requestCode;
        // activityResultResultCode = resultCode;
        // activityResultData = data;
        // }
    }

    /**
     * After receipt capture success.
     * */
    private void receiptCaptureSuccess(Intent data) {
        // This flag is always set to 'true' for captured pictures.
        deleteReceiptImageDataLocalFilePath = true;
        if (!copyCapturedImage()) {
            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FAILURE,
                    Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

            getActivity().showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
        } else {
            // Set the last receipt action.
            lastReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;

            // configureReceiptLabel();
            // Only prompt for receipt append if we're currently connected, the
            // mobile entry does not
            // have offline receipt data and a previous image was
            // captured/selected from Gallery/Receipt Store.
            if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                if (hasPreviousReceiptImageDataValues()) {

                    ReceiptAppendConfirmDialogFragment df = new ReceiptAppendConfirmDialogFragment();
                    df.show(getActivity().getSupportFragmentManager(),
                            ReceiptAppendConfirmDialogFragment.DIALOG_FRAGMENT_ID);

                } else {
                    // Save receipt and thumbnail to content provider
                    storeReceiptAndThumbnail();
                    displayThumbnail(getView());
                }
            } else {
                // Save receipt and thumbnail to content provider
                storeReceiptAndThumbnail();
                displayThumbnail(getView());
                // Delete the previously copied picture.
                removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
                previousReceiptImageDataLocalFilePath = null;
                previousReceiptImageId = null;
                deleteReceiptImageDataLocalFilePath = false;
            }
        }
    }

    private void storeReceiptAndThumbnail() {
        // Obtain a receipt list DAO object.
        SessionInfo sessInfo = ConfigUtil.getSessionInfo(getActivity());
        ReceiptListDAO receiptListDAO = new ReceiptListDAO(getActivity(), sessInfo.getUserId());

        Uri receiptUri = null;
        // Construct a local DAO object.
        ReceiptDAO receiptDAO = receiptListDAO.createReceipt();
        if (receiptDAO.update()) {
            receiptUri = receiptDAO.getContentUri();
        } else {
            Log.e(Const.LOG_TAG, CLS_NAME + ".getReceiptOutputStream: unable to save new receipt object!");
        }

        BufferedOutputStream rcptOut = null;
        if (receiptUri != null) {
            // Obtain the receipt DAO object for 'receiptUri'.
            receiptDAO = receiptListDAO.getReceipt(receiptUri);
            if (receiptDAO != null) {
                ContentResolver resolver = getActivity().getContentResolver();
                try {
                    rcptOut = new BufferedOutputStream(resolver.openOutputStream(receiptUri));
                } catch (FileNotFoundException fnfExc) {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".getReceiptOutputStream: unable to open output stream for uri '"
                            + receiptUri.toString() + "'.", fnfExc);
                }
            } else {
                Log.e(Const.LOG_TAG,
                        CLS_NAME + ".getReceiptOutputStream: unable to obtain receipt DAO object for uri '"
                                + receiptUri.toString() + "'.");
            }
        }

        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(new File(receiptImageDataLocalFilePath)));
            byte[] buf = new byte[(16 * 1024)];
            int bytesRead = -1;

            // // Notify the listener.
            // if (listener != null) {
            // try {
            // listener.onStart(contentLength);
            // } catch (Throwable t) {
            // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: listener.onStart -- ", t);
            // }
            // }
            while ((bytesRead = in.read(buf)) != -1) {
                // Write out to receipt output stream.
                if (rcptOut != null) {
                    rcptOut.write(buf, 0, bytesRead);
                }
                // // Notify the listener.
                // if (listener != null) {
                // try {
                // listener.onDownload(bytesRead);
                // } catch (Throwable t) {
                // Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: listener.onWrite -- ", t);
                // }
                // }
            }
            // Flush the output stream.
            if (rcptOut != null) {
                rcptOut.flush();
            }
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_NAME + ".storeReceiptAndThumbnail: I/O exception reading receipt data!", ioExc);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".storeReceiptAndThumbnail: I/O exception closing input stream.",
                            ioExc);
                } finally {
                    in = null;
                }
            }

            if (rcptOut != null) {
                try {
                    rcptOut.close();
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".storeReceiptAndThumbnail: I/O exception closing output stream.",
                            ioExc);
                } finally {
                    rcptOut = null;
                }
            }
        }
        // TODO - take care of append/replace
        if (expenseEntry.receipt == null)
            expenseEntry.receipt = receiptDAO;
    }

    private void displayThumbnail(View mainView) {
        if (receiptImageDataLocalFilePath == null) {
            ImageView clipIcon = (ImageView) mainView.findViewById(R.id.expense_receipt_clip);
            clipIcon.setVisibility(View.GONE);

        } else {
            Bitmap bmp = null;
            if (TextUtils.isEmpty(receiptImageDataLocalThumbnailFilePath)) {
                bmp = ImageUtil.loadScaledBitmap(receiptImageDataLocalFilePath, 94);
                // receiptImageDataLocalThumbnailFilePath = receiptImageDataLocalFilePath + "-thm.bmp";
            } else {
                bmp = ImageUtil.loadScaledBitmap(receiptImageDataLocalThumbnailFilePath, 100);
            }
            // Get Thumbnail

            // ViewUtil.writeBitmapToFile(bmp, Const.RECEIPT_COMPRESS_BITMAP_FORMAT, 100,
            // receiptImageDataLocalThumbnailFilePath);

            ImageView clipIcon = (ImageView) mainView.findViewById(R.id.expense_receipt_clip);
            clipIcon.setVisibility(View.VISIBLE);
            ImageView procIcon = (ImageView) mainView.findViewById(R.id.expense_receipt);
            procIcon.setImageBitmap(bmp);
        }
    }

    /**
     * Will copy the image data captured by the camera.
     * 
     * @param data
     *            the intent object containing capture information.
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // Assign the path written by the camera application.
        receiptImageDataLocalFilePath = receiptCameraImageDataLocalFilePath;
        retVal = ImageUtil.compressAndRotateImage(receiptImageDataLocalFilePath);
        if (!retVal) {
            receiptImageDataLocalFilePath = null;
            deleteReceiptImageDataLocalFilePath = false;
        }
        return retVal;
    }

    /**
     * Will copy the image data selected within the gallery.
     * 
     * @param data
     *            the intent object containing the selection information.
     */
    private boolean copySelectedImage(Intent data) {

        boolean retVal = true;

        // First, obtain the stream of the selected gallery image.
        InputStream inputStream = ImageUtil.getInputStream(getActivity(), data.getData());
        int angle = ImageUtil.getOrientaionAngle(getActivity(), data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ImageUtil.SampleSizeCompressFormatQuality recConf = ImageUtil
                    .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ImageUtil.closeInputStream(inputStream);
            inputStream = null;
            if (recConf != null) {
                // Copy from the input stream to an external file.
                receiptImageDataLocalFilePath = ImageUtil.createExternalMediaImageFilePath();
                inputStream = new BufferedInputStream(ImageUtil.getInputStream(getActivity(), data.getData()),
                        (8 * 1024));
                if (!ImageUtil.copySampledBitmap(inputStream, receiptImageDataLocalFilePath, recConf.sampleSize,
                        recConf.compressFormat, recConf.compressQuality, angle)) {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".copySelectedImage: unable to copy sampled image from '"
                            + inputStream + "' to '" + receiptImageDataLocalFilePath + "'");
                    receiptImageDataLocalFilePath = null;
                    deleteReceiptImageDataLocalFilePath = false;
                    retVal = false;
                } else {
                    deleteReceiptImageDataLocalFilePath = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".copySelectedImage: unable to obtain recommended samplesize, etc.!");
                receiptImageDataLocalFilePath = null;
                deleteReceiptImageDataLocalFilePath = false;
                retVal = false;
            }
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will remove the previously copied picture from the receipts directory.
     */
    private void removePreviousCopiedPicture(String imageFilePath, boolean deleteFile) {
        if (imageFilePath != null && deleteFile) {
            File file = new File(imageFilePath);
            if (file.exists()) {
                if (!file.delete()) {
                    Log.w(Const.LOG_TAG, CLS_NAME + ".removePreviousCopiedPicture: failed to delete file '"
                            + imageFilePath + "'.");
                } else {
                    Log.d(Const.LOG_TAG, CLS_NAME + ".removePreviousCopiedPicture: deleted file '" + imageFilePath
                            + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".removePreviousCopiedPicture: picture file '" + imageFilePath
                        + "does not exist!");
            }
        }
    }

    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#onServiceAvailable()
    // */
    // @Override
    // public void onServiceAvailable() {
    //
    // if (buildViewDelay) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: build view was delayed, constructing view now.");
    // // Construct the view.
    // buildView();
    // initState(lastSavedInstanceState);
    // buildViewDelay = false;
    //
    // // If 'onActivityResult' call was delayed due to the build view
    // // delay, then
    // // register them as well.
    // if (activityResultDelay) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: activity result was delayed, handling result now.");
    // onActivityResult(activityResultRequestCode, activityResultResultCode, activityResultData);
    // activityResultDelay = false;
    // // Ensure we release reference to the data!
    // activityResultData = null;
    // }
    // }
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#onServiceUnavailable()
    // */
    // @Override
    // public void onServiceUnavailable() {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceUnavailable: ");
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getNetworkActivityText(java.lang.String)
    // */
    // @Override
    // public String getNetworkActivityText(int networkMsgType, String defaultText) {
    // String retVal;
    // switch (networkMsgType) {
    // case Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST:
    // retVal = getText(R.string.retrieve_mini_receipt).toString();
    // break;
    // default:
    // retVal = defaultText;
    // break;
    // }
    // return retVal;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#isNetworkRequestInteresting (int)
    // */
    // @Override
    // public boolean isNetworkRequestInteresting(int networkRequestType) {
    // return (networkRequestType == Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStarted(int)
    // */
    // @Override
    // public void networkActivityStarted(int networkMsgType) {
    // // No-op.
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #networkActivityStopped(int)
    // */
    // @Override
    // public void networkActivityStopped(int networkMsgType) {
    // // No-op.
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.expense.ConcurView#onDestroy()
    // */
    // @Override
    // public void onDestroy() {
    // super.onDestroy();
    // if (receiptImageDataLocalFilePath != null && !saveSucceeded && !savedInstanceState
    // && deleteReceiptImageDataLocalFilePath) {
    // Log.i(Const.LOG_TAG, CLS_TAG + ".onDestroy: deleting receipt image file '" + receiptImageDataLocalFilePath
    // + "'.");
    // File receiptImageFile = new File(receiptImageDataLocalFilePath);
    // if (receiptImageFile.exists()) {
    // receiptImageFile.delete();
    // Log.d(Const.LOG_TAG, CLS_TAG + ".onDestroy: deleted receipt image file '"
    // + receiptImageDataLocalFilePath + "'.");
    // }
    // }
    // }
    //
    // // /**
    // // * Get Expense Type for Quick Expense. Here POL_KEY is -1.
    // // *
    // // * @return expense type list from database.
    // // */
    // // private ArrayList<ExpenseType> getExpenseForQuickExpenseFromDB() {
    // // List<ExpenseType> expTypesFromDB = new ArrayList<ExpenseType>();
    // // ConcurCore concurCore = (ConcurCore) getApplication();
    // // IExpenseEntryCache expEntCache = concurCore.getExpenseEntryCache();
    // // ConcurService concurService = concurCore.getService();
    // // expTypesFromDB = expEntCache.getExpenseTypesFromDB(POL_KEY,
    // // concurService);
    // // if (expTypesFromDB != null && expTypesFromDB.size() > 0) {
    // // expTypesFromDB = expEntCache.sortExpenseList(expTypesFromDB);
    // // }
    // // return (ArrayList<ExpenseType>) expTypesFromDB;
    // // }
    /**
     * An implementation of <code>DatePickerDialog.OnDateSetListener</code> to handle setting the date.
     * 
     * @author AndrewK
     */
    class DatePickerDialogListener implements OnDateSetListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.app.DatePickerDialog.OnDateSetListener#onDateSet(android. widget.DatePicker, int, int, int)
         */
        @Override
        public void onDateSet(CalendarPicker view, int year, int monthOfYear, int dayOfMonth) {
            datePickerDate.set(year, monthOfYear, dayOfMonth);
            updateDatePickerFieldValue(getView());

            // Remove the dialog. This will force recreation and a reset of the
            // displayed values.
            // If we do not do this then the dialog may be shown with it's old
            // values (from the first
            // time around) instead of the newest values (if the underlying
            // Calendar value changes).
            datePickerDlg.dismiss();
        }

    }

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> for handling user selection receipt option.
     * 
     * @author AndrewK
     */
    class ReceiptImageDialogListener implements DialogInterface.OnClickListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content .DialogInterface, int)
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ReceiptPictureSaveAction pictureAction = (ReceiptPictureSaveAction) receiptActionAdapter.getItem(which);
            switch (pictureAction) {
            case CHOOSE_PICTURE: {
                // chooseReceiptPicture();
                break;
            }
            case CHOOSE_PICTURE_CLOUD: {
                // selectCloudReceipt();
                break;
            }
            case CLEAR_PICTURE: {
                // clearReceiptPicture();
                break;
            }
            case DOWNLOAD_PICTURE: {
                // downloadReceiptPicture();
                break;
            }
            case TAKE_PICTURE: {
                takeReceiptPicture();
                break;
            }
            case VIEW: {
                // viewReceiptPicture();
                break;
            }
            }
            dialog.dismiss();
        }

    }

    /**
     * An extension of <code>BaseAdapter</code> for selecting a receipt image option.
     * 
     * @author AndrewK
     */
    class ReceiptImageOptionListAdapter extends BaseAdapter {

        /**
         * Contains a list of available options.
         */
        ArrayList<ReceiptPictureSaveAction> options = new ArrayList<ReceiptPictureSaveAction>();

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            return options.size();
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return options.get(position);
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItemId(int)
         */
        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;

            LayoutInflater inflater = LayoutInflater.from(ExpenseDetailFragment.this.getActivity());

            int textResId = 0;
            switch (options.get(position)) {
            case DOWNLOAD_PICTURE:
                textResId = R.string.download_picture;
                break;
            case TAKE_PICTURE:
                textResId = R.string.take_picture;
                break;
            case CHOOSE_PICTURE:
                textResId = R.string.select_from_device;
                break;
            case CHOOSE_PICTURE_CLOUD:
                textResId = R.string.select_from_cloud;
                break;
            case CLEAR_PICTURE:
                textResId = R.string.clear_picture;
                break;
            case VIEW:
                textResId = R.string.view_receipt;
                break;
            }
            view = inflater.inflate(R.layout.expense_receipt_option, null);

            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setPadding(10, 8, 0, 8);
                    txtView.setText(getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_NAME + ".getView: can't locate text view!");
                }
            }
            return view;
        }
    }

    private void sendGetReceiptRequest(String receiptImageId) {
        this.getReceiptReceiver = new BaseAsyncResultReceiver(new Handler());
        getReceiptReceiver.setListener(getReceiptReplyListener);

        getReceiptRequest = new GetReceiptRequestTask(getActivity().getApplicationContext(), 0, getReceiptReceiver,
                null, receiptImageId, null);
        getReceiptRequest.execute();
    }

    /**
     * Will send a request to save the mobile entry.
     */
    private void sendSaveExpenseRequest(MobileEntryDAO mobileEntry) {

        saveExpenseReceiver = new BaseAsyncResultReceiver(new Handler());
        saveExpenseReceiver.setListener(saveExpenseReplyListener);

        SaveMobileEntryRequestTask saveMETask = new SaveMobileEntryRequestTask(getActivity().getApplicationContext(),
                0, saveExpenseReceiver, mobileEntry.getContentURI(getActivity()), true);
        saveMETask.execute();

        String message = getText(R.string.dlg_expense_save).toString();
        ProgressDialogFragment dlgFrag = DialogFragmentFactory.getProgressDialog(message, true, true,
                new ProgressDialogFragment.OnCancelListener() {

                    @Override
                    public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                        if (saveExpenseRequest != null) {
                            saveExpenseRequest.cancel(true);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_NAME + ".onCancel(SaveExpenseDialog): saveExpenseRequest is null!");
                        }

                    }
                });
        dlgFrag.show(getFragmentManager(), TAG_DIALOG_SAVE_EXPENSE);

    }

    //
    // /**
    // * Will register an instance of <code>SaveExpenseReceiver</code> with the application context and set the
    // * <code>saveExpenseReceiver</code> attribute.
    // */
    // protected void registerSaveExpenseReceiver() {
    // if (saveExpenseReceiver == null) {
    // saveExpenseReceiver = new SaveExpenseReceiver(this);
    // if (saveExpenseFilter == null) {
    // saveExpenseFilter = new IntentFilter(Const.ACTION_EXPENSE_MOBILE_ENTRY_SAVED);
    // }
    // getApplicationContext().registerReceiver(saveExpenseReceiver, saveExpenseFilter);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveExpenseReceiver: saveExpenseReceiver is *not* null!");
    // }
    // }
    //
    // /**
    // * Will unregister an instance of <code>SaveExpenseReceiver</code> with the application context and set the
    // * <code>saveExpenseReceiver</code> to <code>null</code>.
    // */
    // protected void unregisterSaveExpenseReceiver() {
    // if (saveExpenseReceiver != null) {
    // getApplicationContext().unregisterReceiver(saveExpenseReceiver);
    // saveExpenseReceiver = null;
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveExpenseReceiver: saveExpenseReceiver is null!");
    // }
    // }
    //
    // /**
    // * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving an expense.
    // */
    // static class SaveExpenseReceiver extends BaseBroadcastReceiver<QuickExpense, SaveMobileEntryRequest> {
    //
    // /**
    // * Constructs an instance of <code>SaveExpenseReceiver</code>.
    // *
    // * @param activity
    // * the activity.
    // */
    // protected SaveExpenseReceiver(QuickExpense activity) {
    // super(activity);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
    // * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
    // */
    // @Override
    // protected void clearActivityServiceRequest(QuickExpense activity) {
    // activity.saveExpenseRequest = null;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
    // * android.content.Intent)
    // */
    // @Override
    // protected void dismissRequestDialog(Context context, Intent intent) {
    // activity.dismissDialog(DIALOG_SAVE_EXPENSE);
    // }
    //
    // @Override
    // protected void handleFailure(Context context, Intent intent) {
    // activity.showDialog(Const.DIALOG_EXPENSE_SAVE_FAILED);
    // }
    //
    // @Override
    // protected void handleSuccess(Context context, Intent intent) {
    //
    // // Finish the activity.
    // activity.saveSucceeded = true;
    // // Set the flag that the expense entry cache should be refetched.
    // ConcurCore ConcurCore = activity.getConcurCore();
    // // Set the refresh list flag on the expense entry.
    // IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
    // expEntCache.setShouldFetchExpenseList();
    //
    // // If the last receipt action was to select a receipt from the
    // // Receipt Store (or clear the
    // // receipt),
    // // then set the flag that the list should be refetched.
    // if (activity.lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD
    // || activity.lastReceiptAction == ReceiptPictureSaveAction.CLEAR_PICTURE) {
    // ReceiptStoreCache rsCache = ConcurCore.getReceiptStoreCache();
    // rsCache.setShouldFetchReceiptList();
    // }
    //
    // // Flurry Notification.
    // if (activity.saveExpenseRequest != null) {
    // boolean offlineCreate = intent.getBooleanExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, false);
    // if (!offlineCreate) {
    // Map<String, String> params = new HashMap<String, String>();
    // String meKey = activity.saveExpenseRequest.getMobileEntryKey();
    // params.put(Flurry.PARAM_NAME_EDIT_NEW, ((meKey != null) ? Flurry.PARAM_VALUE_NEW
    // : Flurry.PARAM_VALUE_EDIT));
    // boolean hasReceipt = (!activity.saveExpenseRequest.getClearImage() && activity.saveExpenseRequest
    // .getMobileEntry().getReceiptImageId() != null);
    // params.put(Flurry.PARAM_NAME_CONTAINS_RECEIPT, ((hasReceipt) ? Flurry.PARAM_VALUE_YES
    // : Flurry.PARAM_VALUE_NO));
    // FlurryAgent.onEvent(
    // Flurry.formatFlurryEvent(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_SAVED), params);
    // } else {
    // // Offline Create.
    // Map<String, String> params = new HashMap<String, String>();
    // String paramValue = Flurry.PARAM_VALUE_QUICK_EXPENSE;
    // if (activity.mobileEntry != null && activity.mobileEntry.getReceiptImageId() != null
    // && activity.mobileEntry.getReceiptImageId().length() > 0) {
    // paramValue = Flurry.PARAM_VALUE_QUICK_EXPENSE_WITH_RECEIPT;
    // }
    // params.put(Flurry.PARAM_NAME_TYPE, paramValue);
    // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_OFFLINE, Flurry.EVENT_NAME_CREATE),
    // params);
    // }
    // }
    // activity.updateMRUs(activity.saveExpenseRequest);
    // activity.setResult(Activity.RESULT_OK);
    // activity.finish();
    // }
    //
    // @Override
    // protected void setActivityServiceRequest(SaveMobileEntryRequest request) {
    // activity.saveExpenseRequest = request;
    // }
    //
    // @Override
    // protected void unregisterReceiver() {
    // activity.unregisterSaveExpenseReceiver();
    // }
    //
    // }
    //
    // private void updateMRUs(SaveMobileEntryRequest saveExpenseRequest) {
    // if (mrudataCollector != null) {
    // if (saveExpenseRequest != null && saveExpenseRequest.mobileEntry != null) {
    // if (getUserId() != null && getUserId().length() > 0) {
    // MobileEntry mobileEntryFromReq = saveExpenseRequest.mobileEntry;
    // mrudataCollector.setNewExpType(mobileEntryFromReq.getExpKey());
    // mrudataCollector.setNewCurType(mobileEntryFromReq.getCrnCode());
    // mrudataCollector.setNewLoc(mobileEntryFromReq.getLocationName());
    //
    // // Currency MRU
    // MobileDatabase mdb = getConcurService().getMobileDatabase();
    // if (mrudataCollector.isNewExpType()) {
    // new ExpTypeMruAsyncTask(mdb, getUserId(), saveExpenseRequest.expKey, "-1", getConcurService())
    // .execute();
    // } else {
    // Log.d(Const.LOG_TAG, CLS_TAG
    // + ".updateMRu: user didnt select new expeType so no need to update MRU");
    // }
    // if (mrudataCollector.isNewCurType()) {
    // Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    // if (selCurType != null) {
    // selCurType.setUserID(getUserId());
    // selCurType.setLastUseCount(1);
    // selCurType.setLastUsed(now);
    // selCurType.fieldId = ListItem.DEFAULT_KEY_CURRENCY;
    // }
    // new ListItemMruAsyncTask(selCurType, mdb, getUserId(), getConcurService()).execute();
    // } else {
    // Log.d(Const.LOG_TAG, CLS_TAG
    // + ".updateMRu: user didnt select new currency Type so no need to update MRU");
    // }
    // if (mrudataCollector.isNewLocation()) {
    // Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    // ListItem locationItem = new ListItem();
    // locationItem.setUserID(getUserId());
    // locationItem.setLastUseCount(1);
    // locationItem.setLastUsed(now);
    // locationItem.fieldId = ListItem.DEFAULT_KEY_LOCATION;
    // if (locLiKey == null) {
    // locLiKey = "";
    // }
    // locationItem.code = locLiKey;
    // locationItem.key = locLiKey;
    // if (locValue == null) {
    // locValue = "";
    // }
    // locationItem.text = locValue;
    // List<ListItemField> fieldItems = new ArrayList<ListItemField>();
    // if (locCrnCode != null) {
    // fieldItems.add(new ListItemField(ListSearch.CODE_ID, locCrnCode));
    // }
    // if (locCrnKey != null) {
    // fieldItems.add(new ListItemField(ListSearch.KEY_ID, locCrnKey));
    // }
    // if (fieldItems != null) {
    // locationItem.fields = fieldItems;
    // }
    // new ListItemMruAsyncTask(locationItem, mdb, getUserId(), getConcurService()).execute();
    //
    // } else {
    // Log.d(Const.LOG_TAG, CLS_TAG
    // + ".updateMRu: user didnt select new location so no need to update MRU");
    // }
    // } else {
    // Log.w(Const.LOG_TAG, CLS_TAG + ".updateMRu: userID is null");
    // }
    // } else {
    // Log.w(Const.LOG_TAG, CLS_TAG
    // + ".updateMRu: saveExpenseRequest is null || saveExpenseRequest.mobileEntry is null ");
    // }
    // } else {
    // Log.w(Const.LOG_TAG, CLS_TAG + ".updateMRu: mrudataCollector is null");
    // }
    // }
    //
    /**
     * Will send a request to save the receipt.
     */
    private void sendSaveReceiptRequest(String imageFilePath, boolean deleteImageFile) {
        boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
        saveReceiptReceiver = new BaseAsyncResultReceiver(new Handler());
        saveReceiptReceiver.setListener(saveReceiptReplyListener);

        // TODO - use uploadListener?
        saveReceiptRequest = new SaveReceiptRequestTask(getActivity(), 1, saveReceiptReceiver,
                expenseEntry.receipt.getContentUri(), null);
        saveReceiptRequest.execute();
        // if (useConnect) {
        // saveReceiptRequest = concurService.sendConnectPostImageRequest(((BaseActivity) getActivity()).getUserId(),
        // imageFilePath, deleteImageFile, null, false);
        // } else {
        // saveReceiptRequest = concurService.sendSaveReceiptRequest(((BaseActivity) getActivity()).getUserId(),
        // imageFilePath, deleteImageFile, null, false);
        // }
        if (saveReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_NAME + ".sendSaveReceiptRequest: unable to create request to save receipt!");
            // TODO unregisterSaveReceiptReceiver();
        } else {
            // Set the request object on the receiver.
            // TODO saveReceiptReceiver.setServiceRequest(saveReceiptRequest);

            SaveReceiptDialogFragment df = new SaveReceiptDialogFragment();
            df.show(getActivity().getSupportFragmentManager(), SaveReceiptDialogFragment.DIALOG_FRAGMENT_ID);
        }
    }

    // /**
    // * Will register an instance of <code>SaveReceiptReceiver</code> with the application context and set the
    // * <code>saveReceiptReceiver</code> attribute.
    // */
    // protected void registerSaveReceiptReceiver() {
    // if (saveReceiptReceiver == null) {
    // saveReceiptReceiver = new SaveReceiptReceiver(this);
    // if (saveReceiptFilter == null) {
    // saveReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_SAVE);
    // }
    // getApplicationContext().registerReceiver(saveReceiptReceiver, saveReceiptFilter);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReceiptReceiver: saveReceiptReceiver is *not* null!");
    // }
    // }
    //
    // /**
    // * Will unregister an instance of <code>SaveReceiptReceiver</code> with the application context and set the
    // * <code>saveReceiptReceiver</code> to <code>null</code>.
    // */
    // protected void unregisterSaveReceiptReceiver() {
    // if (saveReceiptReceiver != null) {
    // getApplicationContext().unregisterReceiver(saveReceiptReceiver);
    // saveReceiptReceiver = null;
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReceiptReceiver: saveReceiptReceiver is null!");
    // }
    // }
    //
    // /**
    // * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving a receipt.
    // */
    // static class SaveReceiptReceiver extends BaseBroadcastReceiver<QuickExpense, SaveReceiptRequest> {
    //
    // /**
    // * Constructs an instance of <code>ReceiptSaveReceiver</code>.
    // *
    // * @param activity
    // * the activity.
    // */
    // protected SaveReceiptReceiver(QuickExpense activity) {
    // super(activity);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
    // * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
    // */
    // @Override
    // protected void clearActivityServiceRequest(QuickExpense activity) {
    // activity.saveReceiptRequest = null;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
    // * android.content.Intent)
    // */
    // @Override
    // protected void dismissRequestDialog(Context context, Intent intent) {
    // if (!activity.savingForAppend) {
    // activity.dismissDialog(DIALOG_SAVE_RECEIPT);
    // }
    // }
    //
    // @Override
    // protected void handleFailure(Context context, Intent intent) {
    //
    // // Flurry Notification.
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
    // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE), params);
    //
    // activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);
    // }
    //
    // @Override
    // protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
    // boolean handled = false;
    // if (httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
    // String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
    // if (mwsErrorMessage != null
    // && mwsErrorMessage.equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
    // activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
    // handled = true;
    // }
    // }
    // // Flurry Notification.
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
    // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE), params);
    //
    // return handled;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.core.activity.BaseActivity.BaseBroadcastReceiver #handleRequestFailure(android.content.Context,
    // * android.content.Intent, int)
    // */
    // @Override
    // protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
    // super.handleRequestFailure(context, intent, requestStatus);
    //
    // // Flurry Notification.
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
    // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE), params);
    // }
    //
    // @Override
    // protected void handleSuccess(Context context, Intent intent) {
    //
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
    // // Set the Receipt Image ID on the local reference and in the
    // // mobile entry reference.
    // String receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
    // if (receiptImageId != null) {
    // receiptImageId = receiptImageId.trim();
    // }
    // if (activity.isPathAvailable) {
    // // Set the variable to 'false'.
    // // Store the new value in the 'retainer' object where it
    // // will now override the
    // // value in the 'intent'.
    // activity.isPathAvailable = false;
    // }
    // if (!activity.savingForAppend) {
    //
    // if (receiptImageId != null && receiptImageId.length() > 0) {
    // if (!SaveReceiptReply.OFFLINE_RECEIPT_ID.equals(receiptImageId)) {
    // activity.receiptImageId = receiptImageId;
    // activity.mobileEntry.setReceiptImageId(receiptImageId);
    // } else {
    // activity.mobileEntry.setReceiptImageDataLocalFilePath(serviceRequest.filePath);
    // activity.mobileEntry.setReceiptImageDataLocal(true);
    // }
    // // Proceed with saving the expense.
    // activity.sendSaveExpenseRequest();
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG
    // + ".handleSuccess: save receipt result intent has null/empty receipt image id!");
    // handleFailure(context, intent);
    // }
    //
    // // Flurry Notification
    // boolean offlineCreate = intent.getBooleanExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, false);
    // if (!offlineCreate) {
    // activity.postFlurryEvent();
    // }
    // } else {
    // // Post the Flurry event even though an append operation.
    // activity.postFlurryEvent();
    //
    // boolean setReceiptImageId = false;
    // // First, check whether the previous receipt image data was
    // // saved.
    // if (activity.previousReceiptImageDataLocalFilePath != null
    // && activity.previousReceiptImageId == null) {
    // activity.previousReceiptImageId = receiptImageId;
    // setReceiptImageId = true;
    // }
    // // Second, check whether the current receipt image data was
    // // saved.
    // if (activity.receiptImageDataLocalFilePath != null && activity.receiptImageId == null) {
    // if (!setReceiptImageId) {
    // activity.receiptImageId = receiptImageId;
    // } else {
    // activity.sendSaveReceiptRequest(activity.receiptImageDataLocalFilePath, true);
    // }
    // }
    // // Third, check whether both receipt image id's are
    // // available, in that case, just perform
    // // the append.
    // if (activity.previousReceiptImageId != null && activity.receiptImageId != null) {
    // // if(!setReceiptImageId) {
    // activity.sendAppendReceiptRequest(activity.receiptImageId, activity.previousReceiptImageId);
    // // }
    // }
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: save receipt succeeded but missing receipt image id!");
    // handleFailure(context, intent);
    // }
    // }
    //
    // @Override
    // protected void setActivityServiceRequest(SaveReceiptRequest request) {
    // activity.saveReceiptRequest = request;
    // }
    //
    // @Override
    // protected void unregisterReceiver() {
    // activity.unregisterSaveReceiptReceiver();
    // }
    //
    // }
    //
    // protected void postFlurryEvent() {
    // Map<String, String> params = new HashMap<String, String>();
    // if (lastReceiptAction != null) {
    // String paramValue = null;
    // switch (lastReceiptAction) {
    // case CHOOSE_PICTURE: {
    // paramValue = Flurry.PARAM_VALUE_ALBUM;
    // break;
    // }
    // case TAKE_PICTURE: {
    // paramValue = Flurry.PARAM_VALUE_CAMERA;
    // break;
    // }
    // case CHOOSE_PICTURE_CLOUD: {
    // paramValue = Flurry.PARAM_VALUE_RECEIPT_STORE;
    // break;
    // }
    // }
    // if (paramValue != null) {
    // params.put(Flurry.PARAM_NAME_ADDED_USING, paramValue);
    // FlurryAgent.onEvent(
    // Flurry.formatFlurryEvent(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_ADD_TO_MOBILE_ENTRY),
    // params);
    // }
    // }
    // }

    /**
     * Will send a request to append a receipt to another receipt based on receipt image ID's.
     * 
     * @param fromReceiptImageId
     *            contains the receipt image ID of the receipt being appended.
     * @param toReceiptImageId
     *            contains the receipt image ID of the receipt being appended to.
     */
    protected void sendAppendReceiptRequest(String fromReceiptImageId, String toReceiptImageId) {
        ConcurService concurService = ((BaseActivity) getActivity()).getConcurService();
        // TODO registerAppendReceiptReceiver();
        appendReceiptRequest = concurService.sendAppendReceiptImageRequest(((BaseActivity) getActivity()).getUserId(),
                fromReceiptImageId, toReceiptImageId);
        if (appendReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_NAME + ".sendAppendReceiptRequest: unable to create 'AppendReceiptImage' request!");
            // TODO unregisterAppendReceiptReceiver();
        } else {
            // TODO appendReceiptReceiver.setServiceRequest(appendReceiptRequest);
        }
    }

    // /**
    // * Will create and register with the application context an instance of <code>AppendReceiptReceiver</code> and set it on
    // * <code>appendReceiptReceiver</code> attribute.
    // */
    // protected void registerAppendReceiptReceiver() {
    // if (appendReceiptReceiver == null) {
    // appendReceiptReceiver = new AppendReceiptReceiver(this);
    // if (appendReceiptFilter == null) {
    // appendReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_APPENDED);
    // }
    // getApplicationContext().registerReceiver(appendReceiptReceiver, appendReceiptFilter);
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".registerAppendReportEntryReceiver: appendReceiptReceiver is *not* null!");
    // }
    // }
    //
    // /**
    // * Will unregister with the application context the current instance of <code>AppendReceiptReceiver</code> and set the
    // * <code>appendReportEntryReceiptReceiver</code> attribute to 'null'.
    // */
    // protected void unregisterAppendReceiptReceiver() {
    // if (appendReceiptReceiver != null) {
    // getApplicationContext().unregisterReceiver(appendReceiptReceiver);
    // appendReceiptReceiver = null;
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAppendReceiptReceiver: appendReceiptReceiver is null!");
    // }
    // }
    //
    // /**
    // * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of appending a receipt to a
    // * report entry.
    // */
    // static class AppendReceiptReceiver extends BaseBroadcastReceiver<QuickExpense, AppendReceiptImageRequest> {
    //
    // /**
    // * Constructs an instance of <code>AppendReceiptReceiver</code>.
    // *
    // * @param activity
    // * the activity.
    // */
    // AppendReceiptReceiver(QuickExpense activity) {
    // super(activity);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
    // * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
    // */
    // @Override
    // protected void clearActivityServiceRequest(QuickExpense activity) {
    // activity.appendReceiptRequest = null;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
    // * android.content.Intent)
    // */
    // @Override
    // protected void dismissRequestDialog(Context context, Intent intent) {
    // activity.dismissDialog(DIALOG_SAVE_RECEIPT);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
    // * android.content.Intent)
    // */
    // @Override
    // protected void handleFailure(Context context, Intent intent) {
    // // activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
    // // TODO: show failure dialog.
    // activity.resetPreviousReceiptImageDataValues();
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
    // * android.content.Intent)
    // */
    // @Override
    // protected void handleSuccess(Context context, Intent intent) {
    // // Show a success dialog.
    // // TODO: show a successful append dialog.
    // // Reset 'activity.receiptImageId' to the receipt image ID that was
    // // appended to.
    // // This will ensure at Quick Expense save time that this receipt
    // // image ID will be
    // // reset on the mobile entry object.
    // String toReceiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_TO_RECEIPT_IMAGE_ID_KEY);
    // if (toReceiptImageId != null) {
    //
    // // Set the 'receiptImageId' on the activity.
    // toReceiptImageId = toReceiptImageId.trim();
    // activity.receiptImageId = toReceiptImageId;
    //
    // // Set the last action to 'APPEND'.
    // activity.lastReceiptAction = ReceiptPictureSaveAction.APPEND;
    //
    // // Clear receipt related data values.
    // activity.clearReceiptImageDataValues();
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent data has no value for 'toReceiptImageId'!");
    // }
    //
    // // Flurry Notification.
    // Map<String, String> params = new HashMap<String, String>();
    // params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_QUICK_EXPENSE);
    // FlurryAgent.onEvent(Flurry.formatFlurryEvent(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_APPEND), params);
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
    // * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
    // */
    // @Override
    // protected void setActivityServiceRequest(AppendReceiptImageRequest request) {
    // activity.appendReceiptRequest = request;
    // }
    //
    // /*
    // * (non-Javadoc)
    // *
    // * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
    // */
    // @Override
    // protected void unregisterReceiver() {
    // activity.unregisterAppendReceiptReceiver();
    // }
    //
    // }

    public class ReceiptAppendConfirmDialogFragment extends DialogFragment {

        public static final String DIALOG_FRAGMENT_ID = "ReceiptAppendConfirmDialog";

        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.dlg_append_receipt_confirmation_title));
            builder.setMessage(getText(R.string.dlg_append_receipt_confirmation_message));
            builder.setPositiveButton(getText(R.string.general_append), new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // Dismiss the dialog and start the save.
                    dialog.dismiss();

                    // Check whether the previous receipt data is local
                    // only.
                    if (previousReceiptImageDataLocalFilePath != null) {
                        // Set the 'savingForAppend' flag to true and
                        // kick-off a receipt save request.
                        savingForAppend = true;
                        sendSaveReceiptRequest(previousReceiptImageDataLocalFilePath, true);
                        // Show the saving receipt dialog id.
                        SaveReceiptDialogFragment df = new SaveReceiptDialogFragment();
                        df.show(getActivity().getSupportFragmentManager(), SaveReceiptDialogFragment.DIALOG_FRAGMENT_ID);
                    } else if (receiptImageDataLocalFilePath != null) {
                        // Set the 'savingForAppend' flag to true and
                        // kick-off a receipt save request.
                        savingForAppend = true;
                        sendSaveReceiptRequest(receiptImageDataLocalFilePath, true);
                        // Show the saving receipt dialog id.
                        SaveReceiptDialogFragment df = new SaveReceiptDialogFragment();
                        df.show(getActivity().getSupportFragmentManager(), SaveReceiptDialogFragment.DIALOG_FRAGMENT_ID);
                    } else if (previousReceiptImageId != null && receiptImageId != null) {
                        sendAppendReceiptRequest(receiptImageId, previousReceiptImageId);
                        // Show the saving receipt dialog id.
                        SaveReceiptDialogFragment df = new SaveReceiptDialogFragment();
                        df.show(getActivity().getSupportFragmentManager(), SaveReceiptDialogFragment.DIALOG_FRAGMENT_ID);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_NAME
                                + ".onCreateDialog(APPEND).onClick: missing previous/current receipt data!");
                    }
                }
            });
            builder.setNegativeButton(getText(R.string.general_replace), new Dialog.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath, true);
                    previousReceiptImageDataLocalFilePath = null;
                    previousReceiptImageId = null;
                    if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE
                            || lastReceiptAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
                        receiptImageId = null;
                    } else if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD) {
                        receiptImageDataLocalFilePath = null;
                    }
                }
            });
            Dialog dialog = builder.create();
            dialog.setCancelable(true);
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            resetPreviousReceiptImageDataValues();
        }
    }

    public class SaveReceiptDialogFragment extends DialogFragment {

        public static final String DIALOG_FRAGMENT_ID = "SaveReceiptDialog";

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progDlg = new ProgressDialog(getActivity());
            progDlg.setMessage(getText(R.string.saving_receipt));
            progDlg.setIndeterminate(true);
            return progDlg;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            if (saveReceiptRequest != null) {
                saveReceiptRequest.cancel(true);
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".onCancel(SaveReceiptDialog): saveReceiptRequest is null!");
            }
        }

    }

    public class ReceiptOptionsDialogFragment extends DialogFragment {

        public static final String DIALOG_FRAGMENT_ID = "ReceiptOptionsDialog";

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(getText(R.string.expense_receipt_options));
            receiptActionAdapter = new ReceiptImageOptionListAdapter();
            // Set the current list of options.
            // Check for whether the download option should present.
            // receiptActionAdapter.options.clear();
            // Check for whether Receipt Store access has been enabled.
            if (ConcurCore.isConnected() && !com.concur.mobile.core.util.ViewUtil.isReceiptStoreHidden(getActivity())) {
                // Add "Choose From Cloud" option.
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD);
            }
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE);
            // Add both "Take Picture" and "Choose Picture".
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.TAKE_PICTURE);
            // Check whether the "Clear Picture"|"View" option should be
            // present.
            if (lastReceiptAction != ReceiptPictureSaveAction.CLEAR_PICTURE
                    && (expenseEntry.hasReceiptImage() || expenseEntry.getReceiptImageDataLocalFilePath() != null
                            || receiptImageDataLocalFilePath != null || receiptImageId != null || expenseEntry
                            .getReceiptImageId() != null)) {
                // Add in the clear/view options.
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.VIEW);
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.CLEAR_PICTURE);
            }

            // Notify any listeners.
            // receiptActionAdapter.notifyDataSetChanged();
            builder.setSingleChoiceItems(receiptActionAdapter, -1, new ReceiptImageDialogListener());

            return builder.create();
        }
    }

    public class CurrencyDialogFragment extends DialogFragment {

        public static final String DIALOG_FRAGMENT_ID = "CurrencyDialog";

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.currency_prompt);
            curTypeAdapter = new CurrencySpinnerAdapter(getActivity());
            builder.setSingleChoiceItems(curTypeAdapter, -1, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which != -1) {
                        Object selCurObj = curTypeAdapter.getItem(which);
                        if (selCurObj instanceof ListItem) {
                            setSelectedCurrencyType((ListItem) selCurObj, ExpenseDetailFragment.this.getView());
                        }
                    }
                    dismiss();
                }
            });
            AlertDialog alertDlg = builder.create();
            ListView listView = alertDlg.getListView();
            listView.setTextFilterEnabled(true);
            return alertDlg;
        }
    }

    public class CommentDialogFragment extends DialogFragment {

        public CommentDialogFragment() {
            super();
        }

        public static final String DIALOG_FRAGMENT_ID = "CommentDialog";

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(getActivity());

            TextView title = new TextView(getActivity());
            title.setText(getText(R.string.comment));
            title.setGravity(Gravity.LEFT);
            title.setTextSize(28);
            title.setBackgroundColor(Color.WHITE);
            title.setTextColor(getResources().getColor(R.color.expenseFieldValueText));
            title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            title.setPadding(10, 10, 10, 10);

            dlgBldr.setCustomTitle(title);

            // dlgBldr.setTitle(getText(R.string.comment));
            dlgBldr.setCancelable(true);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String comment = textEdit.getText().toString().trim();
                    updateComment(ExpenseDetailFragment.this.getView(), comment);
                    lastChangedText = null;

                    dismiss();
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    lastChangedText = null;
                    dismiss();
                }
            });

            textEdit = new EditText(getActivity());
            textEdit.setMinLines(3);
            textEdit.setMaxLines(3);
            textEdit.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
            textEdit.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {
                    lastChangedText = s.toString();
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    // No-op.
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    // No-op.
                }
            });
            dlgBldr.setView(textEdit);

            String comment = ViewUtil.getTextViewText(ExpenseDetailFragment.this.getView(), R.id.expense_comment,
                    R.id.field_value);
            if (textEdit != null) {
                String txtVal = (comment != null) ? comment : "";
                if (lastChangedText != null) {
                    txtVal = lastChangedText;
                }
                textEdit.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_NAME + ".onPrepareDialog: textEdit is null!");
            }

            return dlgBldr.create();
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            lastChangedText = null;
        }
    }
}
