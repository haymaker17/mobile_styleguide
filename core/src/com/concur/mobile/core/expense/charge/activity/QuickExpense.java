/**
 * 
 */
package com.concur.mobile.core.expense.charge.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.expense.activity.ExpenseTypeSpinnerAdapter;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.activity.ListSearch;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.Expense.ExpenseEntryType;
import com.concur.mobile.core.expense.charge.data.ExpenseTypeCategory;
import com.concur.mobile.core.expense.charge.data.MobileEntry;
import com.concur.mobile.core.expense.charge.data.MobileEntryStatus;
import com.concur.mobile.core.expense.charge.service.SaveMobileEntryRequest;
import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.data.ListItemField;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.report.service.AppendReceiptImageRequest;
import com.concur.mobile.core.expense.service.DownloadMobileEntryReceiptRequest;
import com.concur.mobile.core.expense.service.SaveReceiptReply;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.ExpTypeMruAsyncTask;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ListItemMruAsyncTask;
import com.concur.mobile.core.util.MrudataCollector;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.ViewUtil.LocationSelection;
import com.concur.mobile.core.widget.CalendarPicker;
import com.concur.mobile.core.widget.CalendarPickerDialog;
import com.concur.mobile.core.widget.CalendarPickerDialog.OnDateSetListener;
import com.concur.mobile.platform.expense.smartexpense.SaveSmartExpenseRequestTask;
import com.concur.mobile.platform.expense.smartexpense.SmartExpense;
import com.concur.mobile.platform.location.LastLocationTracker;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

import org.apache.http.HttpStatus;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * An extension of <code>BaseActivity</code> for handling quick expenses. This is largely a re-factor of code from the
 * 'ExpenseOutOfPocketEditView' class.
 */
public class QuickExpense extends BaseActivity {

    private static final String CLS_TAG = QuickExpense.class.getSimpleName();
    private static final String TAG_CALENDAR_DIALOG_FRAGMENT = QuickExpense.class.getSimpleName()
            + ".calendar.dialog.fragment";

    /**
     * NOTE: Currently, the receipt image ID coming back from the Connect API is not protected (if at all) in the same way the MWS
     * protects. Using two different protection methods will result in failures between MWS and Connect. Until that is remedied,
     * use of Connect for Quick Expense save receipt will not be used.
     */
    protected static final boolean ADD_RECEIPT_VIA_CONNECT_ENABLED = Boolean.FALSE;

    private static final int DIALOG_DATE_PICKER = 0;

    private static final int DIALOG_RECEIPT_IMAGE = 1;

    private static final int DIALOG_COMMENT = 2;

    private static final int DIALOG_EXPENSE_TYPE = 3;

    private static final int DIALOG_EXPENSE_CURRENCY = 4;

    private static final int DIALOG_SAVE_RECEIPT = 5;

    private static final int DIALOG_SAVE_EXPENSE = 6;

    private static final int DIALOG_SAVE_SMART_EXPENSE = 7;

    private static final int REQUEST_TAKE_PICTURE = 0;

    private static final int REQUEST_CHOOSE_IMAGE = 1;

    private static final int REQUEST_CHOOSE_CLOUD_IMAGE = 2;

    private static final int REQUEST_SAVE_SMART_EXPENSE = 3;

    // Color definitions for required field labels
    private static final int COLOR_RED = 0xfff00000;
    private static final int COLOR_BLACK = 0xff000000;

    // Key for the date value.
    private static final String DATE_KEY = "expense.date";

    private static final String AMOUNT_KEY = "expense.amount";

    private static final String VENDOR_KEY = "expense.vendor";

    private static final String LOCATION_KEY = "expense.location";

    private static final String COMMENT_KEY = "expense.comment";

    private static final String EXPENSE_TYPE_KEY = "expense.type";

    private static final String CURRENCY_KEY = "expense.currency";

    private static final String RECEIPT_IMAGE_ID_KEY = "receipt.image.id";

    private static final String RECEIPT_IMAGE_FILE_PATH_KEY = "expense.receipt.image.file.path";

    private static final String DELETE_RECEIPT_IMAGE_FILE_PATH = "expense.delete.receipt.image.file.path";

    private static final String RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY = "expense.receipt.camera.image.file.path";

    private static final String LAST_RECEIPT_ACTION_KEY = "expense.last.receipt.action";

    private static final String EXTRA_SAVE_EXPENSE_RECEIVER_KEY = "expense.save.receiver";

    private static final String EXTRA_SAVE_RECEIPT_RECEIVER_KEY = "receipt.save.receiver";

    private static final String EXTRA_SAVE_SMART_EXPENSE_RECEIVER_KEY = "receipt.save.smart.expense.receiver";

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

    /**
     * Contains whether a save attempt succeeded.
     */
    private boolean saveSucceeded;

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

    /**
     * Contains a reference to the expense type adapter.
     */
    private ExpenseTypeSpinnerAdapter expTypeAdapter;

    /**
     * Contains a reference to the currently selected expense type.
     */
    private ExpenseType selExpType;

    /**
     * Contains a reference to the currency adapter.
     */
    private CurrencySpinnerAdapter curTypeAdapter;

    /**
     * Contains a reference to the currently selected currency type.
     */
    private ListItem selCurType;

    /**
     * Contains a reference to a mobile entry associated with the expense entry.
     */
    private MobileEntry mobileEntry;

    /**
     * Contains a reference to the expense entry this view was invoked with.
     */
    private Expense expenseEntry;

    /**
     * Contains an outstanding request to save an expense.
     */
    protected SaveMobileEntryRequest saveExpenseRequest;
    /**
     * Contains a receiver to handle the result of saving an expense.
     */
    protected SaveExpenseReceiver saveExpenseReceiver;
    /**
     * Contains a filter used to register the save expense receiver.
     */
    protected IntentFilter saveExpenseFilter;

    /**
     * Contains an outstanding request to save a receipt.
     */
    protected SaveReceiptRequest saveReceiptRequest;
    /**
     * Contains a receiver to handle the result of saving a receipt.
     */
    protected SaveReceiptReceiver saveReceiptReceiver;
    /**
     * Contains a filter used to register the save receipt receiver.
     */
    protected IntentFilter saveReceiptFilter;

    /**
     * Contains the broadcast receiver for handling the result of appending a receipt image to a report entry.
     */
    protected AppendReceiptReceiver appendReceiptReceiver;

    /**
     * Contains the filter used to register the append report entry receipt receiver.
     */
    protected IntentFilter appendReceiptFilter;

    /**
     * Contains a reference to an outstanding request to append a receipt to a report entry.
     */
    protected AppendReceiptImageRequest appendReceiptRequest;

    /**
     * Contains whether or not the handling of a call from 'onActivityResult' was delayed due to the view not being present.
     */
    private boolean activityResultDelay;

    /**
     * Contains the request code from the delayed handling of the 'onActivityResult' call.
     */
    private int activityResultRequestCode;

    /**
     * Contains the result code from the delayed handling of the 'onActivityResult' call.
     */
    private int activityResultResultCode;

    /**
     * Contains the intent data from the delayed handling of the 'onActivityResult' call.
     */
    private Intent activityResultData;

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

    protected SaveSmartExpenseRequestTask saveSmartExpenseRequest;

    protected BaseAsyncResultReceiver saveSmartExpenseReceiver;

    /**
     * Listener used to handle the response for getting the list of SmartExpenses.
     */
    protected class SaveSmartExpenseListener implements BaseAsyncRequestTask.AsyncReplyListener {

        @Override
        public void onRequestSuccess(Bundle resultData) {
            dismissProgressDialog();

            Log.d(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - Successfully saved SmartExpenses!");

            // Finish the activity.
            saveSucceeded = true;

            // Set the flag that the expense entry cache should be refetched.
            ConcurCore ConcurCore = getConcurCore();

            // Set the refresh list flag on the expense entry.
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();

            setResult(Activity.RESULT_OK);
            finish();
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            dismissProgressDialog();
            Log.e(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - FAILED to save SmartExpenses!");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            dismissProgressDialog();
            Log.d(Const.LOG_TAG, CLS_TAG + ".SmartExpenseListReplyListener - Cancelled saving SmartExpenses!");
        }

        @Override
        public void cleanup() {
            saveSmartExpenseRequest = null;
        }

        protected void dismissProgressDialog() {
            dismissDialog(DIALOG_SAVE_SMART_EXPENSE);
        }

    };


    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        if (isServiceAvailable()) {
            buildView();
            initState(lastSavedInstanceState);
        } else {
            buildViewDelay = true;
        }

        // Restore any receivers.
        restoreReceivers();

        datePickerDlg = (CalendarPickerDialog) getSupportFragmentManager().findFragmentByTag(
                TAG_CALENDAR_DIALOG_FRAGMENT);
        if (datePickerDlg != null) {
            datePickerDlg.setOnDateSetListener(new DatePickerDialogListener());
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onPause()
     */
    @Override
    protected void onPause() {
        super.onPause();

        if (retainer != null) {

            // Store any outstanding save expense receiver.
            if (saveExpenseReceiver != null) {
                // Clear the activity reference, it will be reset in the
                // 'onCreate' method.
                saveExpenseReceiver.setActivity(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_SAVE_EXPENSE_RECEIVER_KEY, saveExpenseReceiver);
            }

            // Store any outstanding save receipt receiver.
            if (saveReceiptReceiver != null) {
                // Clear the activity reference, it will be reset in the
                // 'onCreate' method.
                saveReceiptReceiver.setActivity(null);
                // Store the reference in the retainer.
                retainer.put(EXTRA_SAVE_RECEIPT_RECEIVER_KEY, saveReceiptReceiver);
            }

            // Check for 'AppendReceiptReceiver'.
            if (appendReceiptReceiver != null) {
                // Clear the activity reference, it will be reset in the
                // 'onCreate' method.
                appendReceiptReceiver.setActivity(null);
                // Add it to the retainer
                retainer.put(APPEND_RECEIPT_RECEIVER_KEY, appendReceiptReceiver);
            }

            //Retain the SaveExpense receiver
            if (saveSmartExpenseReceiver != null) {
                saveSmartExpenseReceiver.setListener(null);
                if (retainer != null) {
                    retainer.put(EXTRA_SAVE_SMART_EXPENSE_RECEIVER_KEY, saveSmartExpenseReceiver);
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    @Override
    public void onBackPressed() {
        if (getIntent().hasExtra(ExpenseItDetailActivity.EXTRA_PREFERENCE_CONFIRM_USER_CHOICE_KEY)) {
            showChangeLossConfirmationPrompt();
        } else {
            close();
        }
    }

    private void close() {
        finish();
    }

    private void showChangeLossConfirmationPrompt() {
        String title = getString(R.string.confirm);
        String message = getString(R.string.dlg_expense_save_cancel_confirmation_message);
        int yes = R.string.okay;
        int no = R.string.cancel;
        AlertDialogFragment.OnClickListener yesListener = new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                close();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                // non-op.
            }
        };
        DialogFragmentFactory.getAlertDialog(title, message, yes, -1, no, yesListener, null, null, null)
                .show(getSupportFragmentManager(), CLS_TAG);
    }

    protected void restoreReceivers() {
        // Restore any non-configuration data.
        if (retainer != null) {
            // Restore any receiver waiting on a save expense response.
            if (retainer.contains(EXTRA_SAVE_EXPENSE_RECEIVER_KEY)) {
                saveExpenseReceiver = (SaveExpenseReceiver) retainer.get(EXTRA_SAVE_EXPENSE_RECEIVER_KEY);
                if (saveExpenseReceiver != null) {
                    saveExpenseReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer has null value for save receipt receiver!");
                }
            }
            // Restore any receiver waiting on a save receipt response.
            if (retainer.contains(EXTRA_SAVE_RECEIPT_RECEIVER_KEY)) {
                saveReceiptReceiver = (SaveReceiptReceiver) retainer.get(EXTRA_SAVE_RECEIPT_RECEIVER_KEY);
                if (saveReceiptReceiver != null) {
                    saveReceiptReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer has null value for save receipt receiver!");
                }
            }
            // Restore the 'AppendReceiptReceiver'.
            if (retainer.contains(APPEND_RECEIPT_RECEIVER_KEY)) {
                appendReceiptReceiver = (AppendReceiptReceiver) retainer.get(APPEND_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                appendReceiptReceiver.setActivity(this);
            }
            //Restore saveSmartExpense Receiver
            if (retainer.contains(EXTRA_SAVE_SMART_EXPENSE_RECEIVER_KEY)) {
                saveSmartExpenseReceiver = (BaseAsyncResultReceiver) retainer.get(EXTRA_SAVE_SMART_EXPENSE_RECEIVER_KEY);
                if (saveSmartExpenseReceiver != null) {
                    saveSmartExpenseReceiver.setListener(new SaveSmartExpenseListener());
                }
            }
        }
    }

    /**
     * Will send the appropriate Flurry notification if this activity is creating a new quick expense.
     */
    protected void sendFlurryNotificationIfCreate() {
        if (!orientationChange) {
            Intent intent = getIntent();
            if (intent.hasExtra(Flurry.PARAM_NAME_CAME_FROM)) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION)) {
                    int mobileEntryAction = intent.getIntExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, -1);
                    if (mobileEntryAction == Const.CREATE_MOBILE_ENTRY) {

                        boolean isOCRConversion = intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);

                        String paramValue = intent.getStringExtra(Flurry.PARAM_NAME_CAME_FROM);
                        if (isOCRConversion) {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_CAME_FROM, paramValue);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY,
                                    Flurry.EVENT_NAME_CONVERT_FAILED_OCR, params);
                        } else if (paramValue != null) {
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_CAME_FROM, paramValue);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_CREATE, params);
                        }

                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".sendFlurryNotificationIfCreate: intent missing mobile entry action!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendFlurryNotificationIfCreate: intent missing came from extra!");
            }
        }
    }

    /**
     * Initializes the view.
     */
    public void buildView() {

        // Instruct the window manager to only show the soft keyboard when the
        // end-user clicks on it.
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Set the content view.
        setContentView(R.layout.quick_expense_edit);

        // Flurry Notification.
        sendFlurryNotificationIfCreate();

        Intent intent = getIntent();

        // Obtain the expense type.
        String expType = intent.getStringExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY);

        View receiptButton = findViewById(R.id.header_view_attach_receipts);

        Expense.ExpenseEntryType expEntType = Expense.ExpenseEntryType.CASH;
        if (expType != null) {
            try {
                Expense.ExpenseEntryType passedType = Expense.ExpenseEntryType.valueOf(expType);
                if (passedType != null) {
                    expEntType = passedType;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: invalid passed expense type of '" + expType
                            + "'...defaulting to cash!");
                }
            } catch (IllegalArgumentException illArgExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: invalid passed expense type of '" + expType
                        + "'...defaulting to cash!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: no expense entry type passed in intent...defaulting to cash!");
        }

        // Obtain a reference to the expense object.
        final ConcurCore app = getConcurCore();
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();

        // Obtain the expense entry key based on the expense entry type.
        switch (expEntType) {
        case CASH: {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_RECEIPT_DETAILS,
                    Flurry.PARAM_VALUE_CASH);

            Bundle extras = intent.getExtras();
            if (extras.containsKey(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY)) {
                String meKey = intent.getStringExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_KEY);
                if (meKey != null) {
                    expenseEntry = expEntCache.findCashExpenseEntry(meKey);
                    if (expenseEntry == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".buildView: unable to locate cash expense entry in in-memory cache with ME_KEY!");
                    }
                }
            } else if (extras.containsKey(Const.EXTRA_EXPENSE_LOCAL_KEY)) {
                String localKey = intent.getStringExtra(Const.EXTRA_EXPENSE_LOCAL_KEY);
                if (localKey != null) {
                    final MobileEntry offlineExpense = expEntCache.findMobileEntryByLocalKey(localKey);
                    if (offlineExpense != null) {
                        expenseEntry = new Expense(offlineExpense);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".buildView: unable to locate cash expense entry in in-memory cache with local key!");
                    }
                }
            }
            break;
        }
        case PERSONAL_CARD: {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_RECEIPT_DETAILS,
                    Flurry.PARAM_VALUE_PERSONAL_CARD);

            String pcaKey = intent.getStringExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_PERSONAL_CARD_ACCOUNT_KEY);
            String pctKey = intent.getStringExtra(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY);
            if (pcaKey != null) {
                if (pctKey != null) {
                    expenseEntry = expEntCache.findPersonalCardExpenseEntry(pcaKey, pctKey);
                    if (expenseEntry == null) {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".buildView: unable to locate personal card expense entry in in-memory cache!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: personal card expense edit missing transaction key!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: personal card expense edit missing card key!");
            }
            break;
        }
        case CORPORATE_CARD: {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_RECEIPT_DETAILS,
                    Flurry.PARAM_VALUE_CORPORATE_CARD);

            String cctKey = intent.getStringExtra(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY);
            if (cctKey != null) {
                expenseEntry = expEntCache.findCorporateCardExpenseEntry(cctKey);
                if (expenseEntry == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".buildView: unable to locate corporate card expense entry in in-memory cache!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: corporate card expense edit missing transaction key!");
            }
            break;
        }
        case SMART_CORPORATE: {
            String cctKey = intent.getStringExtra(Const.EXTRA_EXPENSE_CORPORATE_CARD_TRANSACTION_KEY);
            if (cctKey != null) {
                expenseEntry = expEntCache.findSmartCorpExpenseEntry(cctKey);
                if (expenseEntry == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".buildView: unable to locate smart corporate expense entry in in-memory cache!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: corporate card expense edit missing transaction key!");
            }
            break;
        }
        case SMART_PERSONAL: {
            String pctKey = intent.getStringExtra(Const.EXTRA_EXPENSE_PERSONAL_CARD_TRANSACTION_KEY);
            if (pctKey != null) {
                expenseEntry = expEntCache.findSmartPersExpenseEntry(pctKey);
                if (expenseEntry == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".buildView: unable to locate smart personal expense entry in in-memory cache!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: personal card expense edit missing transaction key!");
            }
            break;
        }
        case RECEIPT_CAPTURE: {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_RECEIPT_DETAILS,
                    Flurry.PARAM_VALUE_EXPENSE_IT);

            // TODO: OCR Change the title to Receipt Capture.

            String rcKey = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_CAPTURE_KEY);
            if (rcKey != null) {
                expenseEntry = expEntCache.findReceiptCaptureExpenseEntry(rcKey);
                if (expenseEntry == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".buildView: unable to locate receipt capture expense in in-memory cache!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView:receipt capture expense edit missing transaction key!");
            }
            break;
        }
        case E_RECEIPT: {
            EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES, Flurry.ACTION_RECEIPT_DETAILS,
                    Flurry.PARAM_VALUE_E_RECEIPT);

            String eReceiptId = intent.getStringExtra(Const.EXTRA_EXPENSE_E_RECEIPT_KEY);
            if (eReceiptId != null) {
                expenseEntry = expEntCache.findEReceiptExpenseEntry(eReceiptId);
                if (expenseEntry == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".buildView: unable to locate receipt capture expense in in-memory cache!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView:receipt capture expense edit missing transaction key!");
            }
            break;
        }
        }

        // Create a new expense entry for editing (defaults to cash).
        if (expenseEntry == null) {
            mobileEntry = new MobileEntry();
            mobileEntry.setTransactionDateCalendar(datePickerDate);
            mobileEntry.setTransactionDate(Format.safeFormatCalendar(FormatUtil.XML_DF,
                    mobileEntry.getTransactionDateCalendar()));
            LocationSelection lastLoc = ViewUtil.getLocationSelection(this);
            String userCrnCode;
            if (lastLoc != null) {
                locLiCode = lastLoc.liCode;
                locLiKey = lastLoc.liKey;
                locValue = lastLoc.value;
                mobileEntry.setLocationName(locValue);
                userCrnCode = ViewUtil.getLastUsedCrnCode(this);
                mobileEntry.setCrnCode(userCrnCode);
            } else if ((userCrnCode = ViewUtil.getLastUsedCrnCode(this)) != null && userCrnCode.length() >= 0) {
                mobileEntry.setCrnCode(userCrnCode);
            } else {
                mobileEntry.setCrnCode(getDefaultCurrency());
            }

            // Obtain the receipt image id of the failed OCR
            String ocrReceiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
            if (!TextUtils.isEmpty(ocrReceiptImageId)) {
                // Set the receipt date as transaction date
                if (intent.hasExtra(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY)) {
                    long transDateMillis = intent.getLongExtra(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY, 0);
                    if (transDateMillis > 0) {
                        Calendar transactionDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        transactionDate.setTimeInMillis(transDateMillis);
                        transactionDate.set(Calendar.MILLISECOND, 0);
                        mobileEntry.setTransactionDateCalendar(transactionDate);
                        mobileEntry.setTransactionDate(Format.safeFormatCalendar(FormatUtil.XML_DF,
                                mobileEntry.getTransactionDateCalendar()));
                    }
                }
                if (intent.hasExtra(ExpenseItDetailActivity.EXTRA_EXPENSEIT_COMMENT_KEY)) {
                    String comment = intent.getStringExtra(ExpenseItDetailActivity.EXTRA_EXPENSEIT_COMMENT_KEY);
                    mobileEntry.setComment(comment);
                }

                mobileEntry.setReceiptImageId(ocrReceiptImageId);
                receiptViewOnly = true;

                // Removed 'OCR failed' banner on manual expense conversion from ExpenseIt item.
                // showOCRConversionInstructions();
            }

            expenseEntry = new Expense(mobileEntry);
        } else if (expEntType.equals(Expense.ExpenseEntryType.PERSONAL_CARD)) {
            // If there isn't already a mobile entry associated with the
            // personal card
            // transaction, then create one, but don't associate it until the
            // point of
            // saving.
            mobileEntry = expenseEntry.getPersonalCardTransaction().mobileEntry;
            if (mobileEntry == null) {
                mobileEntry = new MobileEntry(expenseEntry.getPersonalCard(), expenseEntry.getPersonalCardTransaction());
            } else {
                // We save a clone as we update the object model unless the save
                // succeeds.
                mobileEntry = mobileEntry.clone();
            }
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptButton.setEnabled(false);
            } else {
                receiptButton.setEnabled(true);
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            }
        } else if (expEntType.equals(Expense.ExpenseEntryType.CORPORATE_CARD)) {
            // If there isn't already a mobile entry associated with the
            // corporate card
            // transaction, then create one, but don't associate it until the
            // point of
            // saving.
            mobileEntry = expenseEntry.getCorporateCardTransaction().getMobileEntry();
            if (mobileEntry == null) {
                mobileEntry = new MobileEntry(expenseEntry.getCorporateCardTransaction());
            } else {
                mobileEntry = mobileEntry.clone();
            }
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptButton.setEnabled(false);
            } else {
                receiptButton.setEnabled(true);
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = vendorReadOnly = true;
                // Transaction date readonly controlled by site setting.
                transDateReadOnly = !ViewUtil.isCardTransDateEditable(this);
            }
        } else if (expEntType.equals(Expense.ExpenseEntryType.SMART_CORPORATE)) {
            // Construct a mobile entry based on the corporate card transaction.
            mobileEntry = new MobileEntry(expenseEntry.getCorporateCardTransaction());
            // Migrate expense type, comment and whether a receipt exists from
            // the cash transaction.
            MobileEntry trns = expenseEntry.getCashTransaction();
            mobileEntry.setComment(trns.getComment());
            mobileEntry.setEntryType(Expense.ExpenseEntryType.SMART_CORPORATE);
            mobileEntry.setExpKey(trns.getExpKey());
            mobileEntry.setExpName(trns.getExpName());
            mobileEntry.setHasReceiptImage(trns.hasReceiptImage());
            mobileEntry.setMeKey(trns.getMeKey());
            mobileEntry.setLocalKey(trns.getLocalKey());
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptButton.setEnabled(false);
            } else {
                receiptButton.setEnabled(true);
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                // Transaction date readonly controlled by site setting.
                transDateReadOnly = !ViewUtil.isCardTransDateEditable(this);
            }

        } else if (expEntType.equals(Expense.ExpenseEntryType.SMART_PERSONAL)) {
            // Construct a mobile entry based on the corporate card transaction.
            mobileEntry = new MobileEntry(expenseEntry.getPersonalCard(), expenseEntry.getPersonalCardTransaction());
            // Migrate expense type, comment and whether a receipt exists from
            // the cash transaction.
            MobileEntry trns = expenseEntry.getCashTransaction();
            mobileEntry.setComment(trns.getComment());
            mobileEntry.setEntryType(Expense.ExpenseEntryType.SMART_PERSONAL);
            mobileEntry.setExpKey(trns.getExpKey());
            mobileEntry.setExpName(trns.getExpName());
            mobileEntry.setHasReceiptImage(trns.hasReceiptImage());
            mobileEntry.setMeKey(trns.getMeKey());
            mobileEntry.setLocalKey(trns.getLocalKey());
            // MOB-11314
            if (!ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptButton.setEnabled(false);
            } else {
                receiptButton.setEnabled(true);
                // Set read-only for all but expense type, receipt and comment.
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            }
        } else if (expEntType.equals(Expense.ExpenseEntryType.RECEIPT_CAPTURE)) {
            mobileEntry = new MobileEntry(expenseEntry.getReceiptCapture());
            if (mobileEntry.getRcKey() != null) {

                //ExpKey
                expenseTypeReadOnly = false;
                //Comment
                commentReadOnly = false;
                //LocName
                locationReadOnly = false;
                //Enable Save
                saveReadOnly = false;
                //cannot update receipt
                receiptViewOnly = true;

                // If this ReceiptCapture is smart matched, then we shouldn't enabled certain fields.
                // MOB-25324: Allow editing for all fields in SmartExpense when it is a Mobile_entry
                if(expenseEntry.isSmartMatched() &&
                    (mobileEntry.getCctKey() != null || mobileEntry.getPctKey() != null || mobileEntry.getPcaKey() != null)) {
                    //CrnCode
                    currencyReadOnly = true;
                    //VendorDescription
                    vendorReadOnly = true;
                    //TransactionDate
                    transDateReadOnly = true;
                    //TransactionAmount
                    amountReadOnly = true;
                }

                String headerText = this.getResources().getString(R.string.quick_expenseit_title);
                showViewSubHeader(headerText);
            } else {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = saveReadOnly = receiptViewOnly = true;
                showViewSubHeader(null);
            }
            receiptButton.setEnabled(true);
        } else if (expEntType.equals(Expense.ExpenseEntryType.E_RECEIPT)) {
            mobileEntry = new MobileEntry(expenseEntry.getEReceipt(), false);
            currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
            transDateReadOnly = receiptViewOnly = expenseTypeReadOnly = commentReadOnly = saveReadOnly = true;
            String headerText = this.getResources().getString(R.string.quick_ereceipt_title);
            showViewSubHeader(headerText);
            receiptButton.setEnabled(true);
        } else if (expEntType.equals(Expense.ExpenseEntryType.CASH)) {
            // We save a clone as we update the object model unless the save
            // succeeds.
            // MOB-11314
            mobileEntry = expenseEntry.getCashTransaction().clone();
            if (mobileEntry.getStatus() == MobileEntryStatus.NORMAL && !ConcurCore.isConnected()) {
                currencyReadOnly = amountReadOnly = locationReadOnly = vendorReadOnly = true;
                expenseTypeReadOnly = transDateReadOnly = commentReadOnly = true;
                receiptButton.setEnabled(false);
            } else {
                receiptButton.setEnabled(true);
            }
        }

        if (expenseEntry.isSmartMatched()) {
            String headerText = getString(R.string.matched_expenses);
            showViewSubHeader(headerText);
        }

        // Set the title in the header bar.
        configureViewHeader();

        // Set the editable field names.
        setFieldNames();

        // Configure expense type support.
        configureExpenseTypeSelection();

        // Set up support for currency selection.
        configureCurrencySelection();

        // Set up the date picker dialog.
        configureDatePicker();

        // after setup UI, create MRU collector
        if (mrudataCollector == null) {
            mrudataCollector = new MrudataCollector(mobileEntry.getMeKey());
            if (selExpType != null) {
                mrudataCollector.setOldExpType(selExpType.getKey());
            }
            if (selCurType != null) {
                mrudataCollector.setOldCurType(selCurType.code);
            }
            mrudataCollector.setOldLoc(mobileEntry.getLocationName());
        }

        // Display an alert if the app is not connected and there is no cached
        // data for expense/currency.
        if (!ConcurCore.isConnected() && (curTypeAdapter.getCount() == 0 || expTypeAdapter.getCount() == 0)) {
            showDialog(Const.DIALOG_EXPENSE_NO_EXPENSE_TYPE_CURRENCY);
        }

        if (getIntent() != null && getIntent().getExtras() != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String path = extras.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
                if (path != null && path.length() > 0) {
                    receiptCameraImageDataLocalFilePath = path;
                    isPathAvailable = true;
                    receiptCaptureSuccess(null);
                }
            }
        }
    }

    /**
     * Handles a generic on click message for a view.
     * 
     * @param view
     *            the view that was clicked on.
     */
    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.header_view_attach_receipts) {
            if (receiptViewOnly) {
                viewReceiptPicture();
            } else {
                showDialog(DIALOG_RECEIPT_IMAGE);
            }
        }
    }

    protected boolean isSaveActionEnabled() {
        return !saveReadOnly;
    }

    /**
     * Configures support for currency selection.
     */
    private void configureCurrencySelection() {

        View view = findViewById(R.id.expense_currency);
        if (view != null) {
            curTypeAdapter = new CurrencySpinnerAdapter(this);
            if (mobileEntry != null) {
                int curTypeInd = curTypeAdapter.getPositionForCurrency(mobileEntry.getCrnCode());
                if (curTypeInd != -1) {
                    Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                    if (curTypeObj instanceof ListItem) {
                        setSelectedCurrencyType((ListItem) curTypeObj);
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureCurrencySelection: can't locate expense currency field!");
        }
    }

    /**
     * Will set the text on the receipt label depending upon whether a receipt has been selected.
     */
    private void configureReceiptLabel() {

        TextView txtView = (TextView) findViewById(R.id.view_receipts);
        if (txtView != null) {
            int receiptLabelResId = R.string.attach_receipt;
            if (receiptImageId != null
                    || receiptImageDataLocalFilePath != null
                    || (mobileEntry != null
                            && (mobileEntry.hasReceiptImage() || mobileEntry.hasReceiptImageDataLocal() || mobileEntry
                                    .getReceiptImageId() != null) || mobileEntry.getEntryType() == ExpenseEntryType.E_RECEIPT)
                    && (lastReceiptAction != null && lastReceiptAction != ReceiptPictureSaveAction.CLEAR_PICTURE)) {
                receiptLabelResId = R.string.view_receipt;
            }
            txtView.setText(receiptLabelResId);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureReceiptLabel: unable to locate 'view_receipts' view!");
        }
    }

    /**
     * Configures support for expense type selection.
     */
    @SuppressWarnings("unchecked")
    private void configureExpenseTypeSelection() {
        View view = findViewById(R.id.expense_type);
        if (view != null) {
            // ArrayList<ExpenseType> dbList =
            // getExpenseForQuickExpenseFromDB();
            expTypeAdapter = new ExpenseTypeSpinnerAdapter(this, null);
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            ArrayList<ExpenseType> expenseTypes = (ArrayList<ExpenseType>) expEntCache.getExpenseTypes().clone();
            expTypeAdapter.addQuickExpenses(expenseTypes);
            if (mobileEntry != null) {
                // Check whether the mobileEntry has an expense type that is not
                // in our downloaded list.
                if (mobileEntry.getExpKey() != null
                        && expTypeAdapter.getPositionForExpenseType(mobileEntry.getExpKey()) == -1
                        && !mobileEntry.getExpKey().equalsIgnoreCase("UNDEF")) {
                    // An expense type not of type 'undefined' is associated
                    // with this mobile entry and the expense key
                    // can't be found in the adapter (downloaded via the
                    // SystemConfig request). Hence, we'll create a
                    // temporary
                    // expense type that matches that found in the mobile entry
                    // and add it to our list.
                    List<ExpenseType> customExpenseType = createCustomExpenseType(mobileEntry.getExpKey(),
                            mobileEntry.getExpName());
                    expTypeAdapter.appendExpenseTypes(customExpenseType);
                } else if (mobileEntry.getExpKey() != null
                        && expTypeAdapter.getPositionForExpenseType(mobileEntry.getExpKey()) == -1
                        && mobileEntry.getExpKey().equalsIgnoreCase("UNDEF") && expenseTypeReadOnly) {
                    // If expense type cannot be edited and it is UNDEF, we still want to show
                    // Undefined text.
                    List<ExpenseType> customExpenseType = createCustomExpenseType(mobileEntry.getExpKey(),
                            mobileEntry.getExpName());
                    expTypeAdapter.appendExpenseTypes(customExpenseType);
                }
                int curExpPos = expTypeAdapter.getPositionForExpenseType(mobileEntry.getExpKey());
                if (curExpPos != -1) {
                    Object curExpObj = expTypeAdapter.getItem(curExpPos);
                    if (curExpObj instanceof ExpenseType) {
                        setSelectedExpenseType((ExpenseType) curExpObj);
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".configureCurrencySelection: can't locate expense currency field!");
        }
    }

    /**
     * Will set the currently selected expense type and update the display.
     * 
     * @param selExpType
     *            the expense type.
     */
    protected void setSelectedExpenseType(ExpenseType selExpType) {
        // Set the reference.
        this.selExpType = selExpType;
        // Update the display.
        if (this.selExpType != null) {
            ViewUtil.setTextViewText(this, R.id.expense_type, R.id.field_value, selExpType.name, true);
        } else {
            ViewUtil.setTextViewText(this, R.id.expense_type, R.id.field_value, "", true);
        }
    }

    /**
     * Will set the currently selcted currency type and update the display. If the given <code>crn</code> code is null or cannot
     * be found (i.e. is not a valid CRN code in the list of currency selections), then nothing is done.
     * 
     * @param crn
     *            the CRN code.
     */
    protected void setSelectedCurrencyType(String crn) {

        if (crn != null) {
            int curTypeInd = curTypeAdapter.getPositionForCurrency(crn);
            if (curTypeInd != -1) {
                Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                if (curTypeObj instanceof ListItem) {
                    setSelectedCurrencyType((ListItem) curTypeObj);
                }
            }
        }
    }

    /**
     * Will set the currently selected currency type and update the display.
     * 
     * @param selCurType
     *            the expense type.
     */
    protected void setSelectedCurrencyType(ListItem selCurType) {
        // Set the reference.
        this.selCurType = selCurType;
        // Update the display.
        if (this.selCurType != null) {
            ViewUtil.setTextViewText(this, R.id.expense_currency, R.id.field_value, selCurType.text, true);
        } else {
            ViewUtil.setTextViewText(this, R.id.expense_currency, R.id.field_value, "", true);
        }
    }

    /**
     * For the first time the app is installed, get the currency code based on their phone's locale and set that as the default
     * currency type.
     * 
     * @return The selected currency type if it is in our list of Currency Types, otherwise USD.
     */
    private String getDefaultCurrency() {
        ConcurCore ConcurCore = (ConcurCore) getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        ArrayList<ListItem> curTypes = expEntCache.getCurrencyTypes();

        // Get currency code based on the locale of the user
        Locale loc = ViewUtil.getResourcesConfigurationLocale(this);
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
    private void configureDatePicker() {
        // Set the expense date.
        if (mobileEntry != null) {
            datePickerDate = mobileEntry.getTransactionDateCalendar();
            updateDatePickerFieldValue();
        }
    }

    /**
     * Configures the view header.
     */
    protected void configureViewHeader() {
        String title = getText(R.string.quick_expense_title).toString();
        getSupportActionBar().setTitle(title);
    }

    /**
     * Show view subheader for receipt capture items.
     */
    private void showViewSubHeader(String headerText) {
        LinearLayout subHeader = (LinearLayout) findViewById(R.id.quick_expense_subtitle_layout);
        subHeader.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(headerText)) {
            // Update headerText
            TextView headerTextVw = (TextView) subHeader.findViewById(R.id.quick_expense_subtitle);
            headerTextVw.setText(headerText);
        }
    }

    /**
     * Show ocr conversion instruction for failed ocr items.
     */
    private void showOCRConversionInstructions() {
        View instrView = findViewById(R.id.ocr_conversion);
        instrView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_save, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

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

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menuSave) {
            save();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setRequiredFieldLabelColors() {
        // Set expense_type label field color
        if (selExpType != null) {
            setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_BLACK);
        } else {
            setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_RED);
        }

        // Set amount field label color
        String amount = ViewUtil.getTextViewText(this, R.id.expense_amount, R.id.field_value);
        if (amount != null && amount.length() > 0) {
            setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_BLACK);
        } else {
            setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
        }
    }

    // Sets the label color of a field.
    private void setFieldLabelColor(int field_view_res_id, int field_name_res_id, int color) {
        View view = findViewById(field_view_res_id);
        TextView textView = (TextView) view.findViewById(field_name_res_id);
        textView.setTextColor(color);
    }

    // Appends "(Required)" to the label of a field
    public CharSequence buildRequiredLabel(CharSequence label) {
        if (label != null) {
            SpannableStringBuilder strBldr = new SpannableStringBuilder(label);
            strBldr.append(' ');
            int spanStart = strBldr.length();
            strBldr.append('(');
            strBldr.append(getText(R.string.required).toString());
            strBldr.append(')');
            int requiredStyle = R.style.FormFieldLabelRequired;

            strBldr.setSpan(new TextAppearanceSpan(this.getApplicationContext(), requiredStyle), spanStart,
                    strBldr.length(), 0);

            label = strBldr;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildLabel: form fields label is null!");
        }
        return label;
    }

    // Sets the label of a TextView field that is required to be filled out.
    // This will call the buildRequiredLabel method in order
    // to append "(Required)" on to the field in italics.
    public void setRequiredTextView(int field_view_res_id, int field_name_res_id, CharSequence labelName) {
        CharSequence appendedLabel = buildRequiredLabel(labelName);
        View view = findViewById(field_view_res_id);
        TextView textView = (TextView) view.findViewById(field_name_res_id);
        textView.setText(appendedLabel);
    }

    /**
     * Will set the field names on several editable fields.
     */
    private void setFieldNames() {

        // Configure the receipt label.
        configureReceiptLabel();

        // Set the date.
        setRequiredTextView(R.id.expense_date, R.id.field_name, getText(R.string.date).toString());
        View view = findViewById(R.id.expense_date);
        if (view != null) {
            if (!transDateReadOnly) {
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showCalendarDialog();
                    }
                });
            } else {
                // Read-only, so remove the arrow indicator.
                ViewUtil.setVisibility(view, R.id.field_image, View.INVISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: expense_date field not found!");
        }
        // Set the type.
        setRequiredTextView(R.id.expense_type, R.id.field_name, getText(R.string.expense_type).toString());
        // Enable/disable type.
        ViewUtil.setViewEnabled(this, R.id.expense_type, R.id.field_value, !expenseTypeReadOnly);
        if (!expenseTypeReadOnly) {
            view = findViewById(R.id.expense_type);
            if (view != null) {
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_EXPENSE_TYPE);
                    }

                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: expense_type field not found!");
            }
        }
        // Set the vendor.
        ViewUtil.setTextViewText(this, R.id.expense_vendor, R.id.field_name, getText(R.string.vendor).toString(), true);
        if (mobileEntry != null && mobileEntry.getVendorName() != null) {
            ViewUtil.setTextViewText(this, R.id.expense_vendor, R.id.field_value, mobileEntry.getVendorName(), true);
        } else {
            ViewUtil.setTextViewText(this, R.id.expense_vendor, R.id.field_value, "", true);
        }
        // Enabled/disable the control.
        ViewUtil.setViewEnabled(this, R.id.expense_vendor, R.id.field_value, !vendorReadOnly);
        // Set the amount.
        setRequiredTextView(R.id.expense_amount, R.id.field_name, getText(R.string.amount).toString());
        if (expenseEntry != null && expenseEntry.getExpenseEntryType() == ExpenseEntryType.CASH && mobileEntry != null
                && (mobileEntry.getMeKey() == null || mobileEntry.getMeKey().length() == 0)
                && (mobileEntry.getLocalKey() == null)) {
            // A new cash-based quick expense should have the amount field
            // cleared.
            ViewUtil.setTextViewText(this, R.id.expense_amount, R.id.field_value, "", true);
        } else if (mobileEntry != null && mobileEntry.getTransactionAmount() != null) {
            // An existing transaction with an amount value.
            String transAmt = FormatUtil.formatAmount(mobileEntry.getTransactionAmount(), getResources()
                    .getConfiguration().locale, mobileEntry.getCrnCode(), false);
            ViewUtil.setTextViewText(this, R.id.expense_amount, R.id.field_value, transAmt, true);
        } else {
            ViewUtil.setTextViewText(this, R.id.expense_amount, R.id.field_value, "", true);
        }
        // Set the amount to be a numeric input only.
        final TextView txtView = (TextView) ViewUtil.findSubView(this, R.id.expense_amount, R.id.field_value);
        if (txtView != null) {

            // Put in a proper key listener since Android is busted for
            // locale-specific decimal formatting
            txtView.setKeyListener(FormatUtil.getLocaleDecimalListener(this));
            // According to the API javadocs, we need to call this again to
            // regain focusability.
            txtView.setFocusable(true);
            txtView.setEnabled(!amountReadOnly);

            // MOB-10783 - Add a listener to make sure amount is a valid value.
            txtView.addTextChangedListener(new TextWatcher() {

                @Override
                public void afterTextChanged(Editable s) {

                    View topView = findViewById(R.id.expense_amount);
                    TextView view = (TextView) topView.findViewById(R.id.field_note);
                    if (s != null && view != null) {
                        String curValue = s.toString().trim();
                        // Check the amount and make sure it is less than 1
                        // quadrillion
                        Double finalAmount = FormatUtil.parseAmount(curValue, ConcurCore.getContext().getResources()
                                .getConfiguration().locale);
                        // MOB-10928 - negative amounts are okay.
                        if (finalAmount == null) {
                            // Show notification.
                            String txt = com.concur.mobile.base.util.Format.localizeText(QuickExpense.this,
                                    R.string.general_field_value_invalid, curValue);
                            view.setText(txt);
                            view.setTextAppearance(QuickExpense.this, R.style.FormFieldNoteRedText);
                            ViewUtil.setVisibility(view, R.id.field_note, View.VISIBLE);
                            setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
                        } else if (finalAmount > Double.valueOf(1000000000000000.00)) {
                            // Show notification.
                            String txt = com.concur.mobile.base.util.Format.localizeText(QuickExpense.this,
                                    R.string.general_field_value_too_large, curValue);
                            view.setText(txt);
                            view.setTextAppearance(QuickExpense.this, R.style.FormFieldNoteRedText);
                            ViewUtil.setVisibility(view, R.id.field_note, View.VISIBLE);
                            setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_RED);
                        } else {
                            // Hide notification.
                            ViewUtil.setVisibility(view, R.id.field_note, View.GONE);
                            setFieldLabelColor(R.id.expense_amount, R.id.field_name, COLOR_BLACK);
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
            Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: unable to locate expense amount field value text view!");
        }

        // Set the currency.
        setRequiredTextView(R.id.expense_currency, R.id.field_name, getText(R.string.currency).toString());
        ViewUtil.setViewEnabled(this, R.id.expense_currency, R.id.field_value, !currencyReadOnly);
        if (!currencyReadOnly) {
            view = findViewById(R.id.expense_currency);
            if (view != null) {
                view.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDialog(DIALOG_EXPENSE_CURRENCY);
                    }

                });
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: expense_currency field not found!");
            }
        } else {
            view = findViewById(R.id.expense_currency);
            if (view != null) {
                // Read-only, so remove the arrow indicator.
                ViewUtil.setVisibility(view, R.id.field_image, View.INVISIBLE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: expense_currency field not found!");
            }
        }
        // Set the location.
        ViewUtil.setTextViewText(this, R.id.expense_location, R.id.field_name, getText(R.string.location).toString(),
                true);
        view = findViewById(R.id.expense_location);
        if (view != null && !locationReadOnly) {
            // Set up the click listener to launch the location search activity.
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (locationIntent == null) {
                        locationIntent = new Intent(QuickExpense.this, ListSearch.class);
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
            Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: unable to locate 'expense_location' view!");
        }
        if (mobileEntry != null) {
            String location = mobileEntry.getLocationName();
            if (location != null) {
                ViewUtil.setTextViewText(this, R.id.expense_location, R.id.field_value, location, true);
            } else {
                ViewUtil.setTextViewText(this, R.id.expense_location, R.id.field_value, "", true);
            }
        } else {
            ViewUtil.setTextViewText(this, R.id.expense_location, R.id.field_value, "", true);
        }
        // Enable/Disable location name.
        ViewUtil.setViewEnabled(this, R.id.expense_location, R.id.field_value, !locationReadOnly);

        // Set the comment.
        ViewUtil.setTextViewText(this, R.id.expense_comment, R.id.field_name, getText(R.string.comment).toString(),
                true);
        if (mobileEntry != null) {
            String comment = mobileEntry.getComment();
            if (comment != null) {
                ViewUtil.setTextViewText(this, R.id.expense_comment, R.id.field_value, comment, true);
            } else {
                ViewUtil.setTextViewText(this, R.id.expense_comment, R.id.field_value, "", true);
            }
        }
        // Add a handler to launch the comment dialog.
        view = findViewById(R.id.expense_comment);
        if (view != null) {
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!commentReadOnly) {
                        showDialog(DIALOG_COMMENT);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldNames: unable to locate 'expense_comment' view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#save()
     */
    public void save() {

        if (mobileEntry != null) {

            // Pull the values from the screen controls.
            Calendar transDate = datePickerDate;
            String transDateStr = Format.safeFormatCalendar(FormatUtil.XML_DF, datePickerDate);
            String vendorName = ViewUtil.getTextViewText(this, R.id.expense_vendor, R.id.field_value).trim();
            String locationName = ViewUtil.getTextViewText(this, R.id.expense_location, R.id.field_value).trim();
            String amount = ViewUtil.getTextViewText(this, R.id.expense_amount, R.id.field_value).trim();

            if (transDate == null) {
                showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_TRANSACTION_DATE);
                return;
            }
            if (selExpType == null) {
                View view = findViewById(R.id.expense_type);
                if (view != null) {
                    view.requestFocus();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".save: unable to locate expense_type view!");
                }
                showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_TYPE);
                return;
            }
            if (selCurType == null) {
                showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_CURRENCY);
                return;
            }
            Double transAmt = null;
            if (amount == null || amount.length() == 0) {
                View amountView = ViewUtil.findSubView(this, R.id.expense_amount, R.id.field_value);
                amountView.requestFocus();
                showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT);
                return;
            } else {
                transAmt = FormatUtil.parseAmount(amount, getResources().getConfiguration().locale);
                if (transAmt == null) {
                    View amountView = ViewUtil.findSubView(this, R.id.expense_amount, R.id.field_value);
                    amountView.requestFocus();
                    showDialog(Const.DIALOG_OUT_OF_POCKET_EXPENSE_AMOUNT);
                    return;
                }
            }

            // Set the expense date.
            mobileEntry.setTransactionDateCalendar(datePickerDate);
            mobileEntry.setTransactionDate(transDateStr);

            // Set the expense type.
            mobileEntry.setExpKey(selExpType.getKey());
            mobileEntry.setExpName(selExpType.getName());

            // Set the vendor name.
            mobileEntry.setVendorName(vendorName);

            // Set the currency code.
            mobileEntry.setCrnCode(selCurType.code);

            // Set the location name.
            mobileEntry.setLocationName(locationName);

            // Set the amount.
            mobileEntry.setTransactionAmount(transAmt);

            // Set the comment.
            String comment = ViewUtil.getTextViewText(this, R.id.expense_comment, R.id.field_value);
            if (comment != null && comment.length() > 0) {
                mobileEntry.setComment(comment);
            } else {
                mobileEntry.setComment(null);
            }

            // Set the update time.
            mobileEntry.setUpdateDate(Calendar.getInstance(TimeZone.getTimeZone("UTC")));

            // Update the receipt image id if set.
            if (receiptImageId != null) {
                mobileEntry.setReceiptImageId(receiptImageId);
            }

            // If the last receipt action was either choose or take picture,
            // then first save the receipt.
            // The quick expense will be saved after saving the receipt.
            if (lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE
                    || lastReceiptAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
                sendSaveReceiptRequest(receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
            } else if (mobileEntry.getRcKey()!= null) {
                saveSmartExpense();
            } else {
                sendSaveExpenseRequest();
            }

            if (locationName != null && locationName.length() > 0) {
                ViewUtil.saveLocationSelection(getConcurCore(), this, locLiKey, locLiCode, locationName);
            }

            if (selCurType != null) {
                ViewUtil.saveLastUsedCrnCode(getConcurCore(), this, selCurType.code);
            }

            // Log the event
            if (mobileEntry.getMeKey() == null && mobileEntry.getLocalKey() == null) {
                EventTracker.INSTANCE.track(getClass().getSimpleName(), "Create Mobile Entry");
            } else {
                EventTracker.INSTANCE.track(getClass().getSimpleName(), "Save Mobile Entry");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onCreateDialog(int)
     */
    @Override
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_RECEIPT_IMAGE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.expense_receipt_options));
            receiptActionAdapter = new ReceiptImageOptionListAdapter();
            builder.setSingleChoiceItems(receiptActionAdapter, -1, new ReceiptImageDialogListener());
            dialog = builder.create();
            break;
        }
        case DIALOG_COMMENT: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(getText(R.string.comment));
            dlgBldr.setCancelable(true);
            dlgBldr.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    removeDialog(DIALOG_COMMENT);
                    lastChangedText = null;
                }
            });
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(DIALOG_COMMENT);
                    String comment = textEdit.getText().toString().trim();
                    ViewUtil.setTextViewText(QuickExpense.this, R.id.expense_comment, R.id.field_value, comment, true);
                    lastChangedText = null;
                }
            });
            dlgBldr.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removeDialog(DIALOG_COMMENT);
                    lastChangedText = null;
                }
            });

            textEdit = new EditText(this);
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
            dialog = dlgBldr.create();
            break;
        }
        case DIALOG_EXPENSE_TYPE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.expense_type_prompt);
            expTypeAdapter = new ExpenseTypeSpinnerAdapter(this, null);
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();

            @SuppressWarnings("unchecked")
            ArrayList<ExpenseType> expenseTypes = (ArrayList<ExpenseType>) expEntCache.getExpenseTypes().clone();
            expTypeAdapter.addQuickExpenses(expenseTypes);
            LayoutInflater inflater = LayoutInflater.from(this);
            View customView = inflater.inflate(R.layout.expense_mru, null);

            ListView customListView = (ListView) customView.findViewById(R.id.list_expense_mru);
            EditText customEditText = (EditText) customView.findViewById(R.id.list_search_mru);
            customListView.setAdapter(expTypeAdapter);

            customListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            builder.setView(customView);

            customEditText.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    expTypeAdapter.clearSearchFilter();
                    expTypeAdapter.getFilter().filter(s);

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }
            });

            customListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                    dismissDialog(DIALOG_EXPENSE_TYPE);
                    if (which != -1) {
                        Object selExpObj = expTypeAdapter.getItem(which);
                        if (selExpObj instanceof ExpenseType) {
                            setSelectedExpenseType((ExpenseType) selExpObj);
                        }
                    }
                    if (selExpType == null) {
                        setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_RED);
                    } else {
                        setFieldLabelColor(R.id.expense_type, R.id.field_name, COLOR_BLACK);
                    }
                }
            });

            AlertDialog alertDlg = builder.create();
            dialog = alertDlg;
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(DIALOG_EXPENSE_TYPE);
                }
            });
            break;
        }
        case DIALOG_EXPENSE_CURRENCY: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.currency_prompt);
            curTypeAdapter = new CurrencySpinnerAdapter(this);
            builder.setSingleChoiceItems(curTypeAdapter, -1, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dismissDialog(DIALOG_EXPENSE_CURRENCY);
                    if (which != -1) {
                        Object selCurObj = curTypeAdapter.getItem(which);
                        if (selCurObj instanceof ListItem) {
                            setSelectedCurrencyType((ListItem) selCurObj);
                        }
                    }
                }
            });
            AlertDialog alertDlg = builder.create();
            ListView listView = alertDlg.getListView();
            listView.setTextFilterEnabled(true);
            dialog = alertDlg;
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    removeDialog(DIALOG_EXPENSE_CURRENCY);
                }
            });
            break;
        }
        case DIALOG_SAVE_RECEIPT: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.saving_receipt));
            progDlg.setIndeterminate(true);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (saveReceiptRequest != null) {
                        saveReceiptRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCancel(SaveReceiptDialog): saveReceiptRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case DIALOG_SAVE_EXPENSE: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_save));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (saveExpenseRequest != null) {
                        saveExpenseRequest.cancel();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCancel(SaveExpenseDialog): saveExpenseRequest is null!");
                    }
                }
            });
            dialog = progDlg;
            break;
        }
        case DIALOG_SAVE_SMART_EXPENSE: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(getText(R.string.dlg_expense_save));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            progDlg.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    if (saveSmartExpenseRequest != null) {
                        saveSmartExpenseRequest.cancel(true);
                    } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCancel(SaveExpenseDialog): saveExpenseRequest is null!");
                }
                }
            });
            dialog = progDlg;
            break;
        }
        case Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
                        showDialog(DIALOG_SAVE_RECEIPT);
                    } else if (receiptImageDataLocalFilePath != null) {
                        // Set the 'savingForAppend' flag to true and
                        // kick-off a receipt save request.
                        savingForAppend = true;
                        sendSaveReceiptRequest(receiptImageDataLocalFilePath, true);
                        // Show the saving receipt dialog id.
                        showDialog(DIALOG_SAVE_RECEIPT);
                    } else if (previousReceiptImageId != null && receiptImageId != null) {
                        sendAppendReceiptRequest(receiptImageId, previousReceiptImageId);
                        // Show the saving receipt dialog id.
                        showDialog(DIALOG_SAVE_RECEIPT);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
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
            dialog = builder.create();
            dialog.setCancelable(true);
            dialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    resetPreviousReceiptImageDataValues();
                }
            });
            break;
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onPrepareDialog(int, android.app.Dialog)
     */
    @Override
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case DIALOG_RECEIPT_IMAGE:
            // Set the current list of options.
            // Check for whether the download option should present.
            receiptActionAdapter.options.clear();
            // Check for whether Receipt Store access has been enabled.
            if (ConcurCore.isConnected() && !ViewUtil.isReceiptStoreHidden(this)) {
                // Add "Choose From Cloud" option.
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD);
            }
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE);
            // Add both "Take Picture" and "Choose Picture".
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.TAKE_PICTURE);
            // Check whether the "Clear Picture"|"View" option should be
            // present.
            if (lastReceiptAction != ReceiptPictureSaveAction.CLEAR_PICTURE
                    && (mobileEntry.hasReceiptImage() || mobileEntry.hasReceiptImageDataLocal()
                            || receiptImageDataLocalFilePath != null || receiptImageId != null || mobileEntry
                            .getReceiptImageId() != null)) {
                // Add in the clear/view options.
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.VIEW);
                receiptActionAdapter.options.add(ReceiptPictureSaveAction.CLEAR_PICTURE);
            }

            // Notify any listeners.
            receiptActionAdapter.notifyDataSetChanged();
            break;
        case Const.DIALOG_EXPENSE_SAVE_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        case DIALOG_COMMENT: {
            String comment = ViewUtil.getTextViewText(this, R.id.expense_comment, R.id.field_value);
            if (textEdit != null) {
                String txtVal = (comment != null) ? comment : "";
                if (lastChangedText != null) {
                    txtVal = lastChangedText;
                }
                textEdit.setText(txtVal);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: textEdit is null!");
            }
            break;
        }
        case DIALOG_EXPENSE_TYPE: {
            // No-op.
            break;
        }
        case DIALOG_EXPENSE_CURRENCY: {
            break;
        }
        case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setMessage(actionStatusErrorMessage);
            break;
        }
        }
    }

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
        datePickerDlg.show(getSupportFragmentManager(), TAG_CALENDAR_DIALOG_FRAGMENT);

    }

    /**
     * Sets the text on the date picker button to the latest value in <code>datePickerDate</code>.
     */
    private void updateDatePickerFieldValue() {
        View view = findViewById(R.id.expense_date);
        if (view != null) {
            TextView txtView = (TextView) view.findViewById(R.id.field_value);
            if (txtView != null) {
                String dateStr = Format.safeFormatCalendar(FormatUtil.MONTH_DAY_FULL_YEAR_DISPLAY, datePickerDate);
                txtView.setText(dateStr);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".updateDatePickerButtonText: can't find date text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateDatePickerFieldValue: can't locate expense date field!");
        }
    }

    /**
     * Will choose a picture from the devices media gallery.
     */
    private void chooseReceiptPicture() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            // Hang-on to current values.
            setPreviousReceiptImageDataValues();

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }

    }

    /**
     * Selects a receipt image from one in the receipt store cloud for use at the report level.
     */
    protected void selectCloudReceipt() {

        // Hang-on to current values.
        setPreviousReceiptImageDataValues();

        Intent intent = new Intent(this, ExpensesAndReceipts.class);
        intent.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
        intent.setAction(Intent.ACTION_PICK);
        intent.putExtra(Const.EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY, true);
        intent.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_MOBILE_ENTRY);
        startActivityForResult(intent, REQUEST_CHOOSE_CLOUD_IMAGE);
    }

    /**
     * Will clear the current receipt picture.
     */
    private void clearReceiptPicture() {
        lastReceiptAction = ReceiptPictureSaveAction.CLEAR_PICTURE;

        // Ensure a previous copied picture is removed.
        removePreviousCopiedPicture(receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
        receiptImageDataLocalFilePath = null;
        deleteReceiptImageDataLocalFilePath = false;

        // Clear the receipt image id.
        receiptImageId = null;

        // Clear the local data path
        receiptImageDataLocalFilePath = null;
        if (mobileEntry != null) {
            // For offline expenses we need to clear this as well since the
            // entry object would not otherwise be updated until save
            mobileEntry.setReceiptImageDataLocalFilePath(null);
            mobileEntry.setReceiptImageDataLocal(false);
            mobileEntry.setReceiptImageId(null);
        }

        // Configure the receipt label.
        configureReceiptLabel();
    }

    /**
     * Will download the receipt image from the server.
     */
    private void downloadReceiptPicture() {
        lastReceiptAction = ReceiptPictureSaveAction.DOWNLOAD_PICTURE;
        // Show the dialog.
        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
        // Request the service download the receipt image.
        ConcurService concurService = getConcurService();
        concurService.downloadMobileEntryReceipt(mobileEntry);
    }

    /**
     * Will launch the 'ViewImage' activity to either display a locally captured/selected image or download the receipt image.
     */
    protected void viewReceiptPicture() {
        // Launch an intent to view an expense receipt.
        Intent intent = new Intent(this, ViewImage.class);

        boolean requiresConnectivity = false;

        // First check for locally captured/selected (from device gallery)
        // picture.
        if (receiptImageDataLocalFilePath != null) {
            // Current locally captured/selected (from device gallery) picture.
            StringBuilder strBldr = new StringBuilder("file://");
            strBldr.append(receiptImageDataLocalFilePath);
            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
        } else if (receiptImageId != null) {
            // Current selected (from receipt store) picture.
            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, receiptImageId);
            intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
            intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
            requiresConnectivity = true;
        } else if (mobileEntry.getReceiptImageDataLocalFilePath() != null) {
            // Locally saved receipt image file, not yet saved to the server.
            StringBuilder strBldr = new StringBuilder("file://");
            strBldr.append(mobileEntry.getReceiptImageDataLocalFilePath());
            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
        } else if (mobileEntry.getReceiptImageId() != null) {
            // Referenced receipt id from the receipt store.
            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, mobileEntry.getReceiptImageId());
            intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
            intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
            requiresConnectivity = true;
        } else {
            String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(serverAdd);
            strBldr.append(DownloadMobileEntryReceiptRequest.getServiceEndPointURI(mobileEntry.getMeKey()));
            String urlStr = strBldr.toString();
            intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, urlStr);
            requiresConnectivity = true;
        }

        // MOB-21299 : If it is only e-receipt and/or it is matched expense then send e-reciept = true.
        if ((mobileEntry != null && mobileEntry.getEntryType() == ExpenseEntryType.E_RECEIPT)
                || (expenseEntry.isSmartMatched() && expenseEntry.getEReceipt() != null)) {
            intent.putExtra(Const.EXTRA_E_RECEIPT_EXPENSE, true);
        }

        if (!ConcurCore.isConnected() && requiresConnectivity) {
            showDialog(Const.DIALOG_NO_CONNECTIVITY);
        } else {
            // Launch the activity.
            startActivity(intent);
        }
    }

    /**
     * Will take a new receipt picture.
     */
    private void takeReceiptPicture() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".takeReceiptPicture: ");
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {

            // Create a file name based on the current date.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, CLS_TAG + ".takeReceiptPicture: receipt image path -> '"
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
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Will create a new list of <code>ExpenseType</code> objects with one custom category containing the expense type defined by
     * <code>expKey</code> and <code>expName</code>.
     * 
     * @param expKey
     *            the custom expense key.
     * @param expName
     *            the custom expense name.
     * @return a list of <code>ExpenseType</code> with the first entry being an instance of <code>ExpenseTypeCategory</code> and
     *         the second being an instance of <code>ExpenseType</code>.
     */
    private List<ExpenseType> createCustomExpenseType(String expKey, String expName) {
        List<ExpenseType> customExpenseType = null;

        customExpenseType = new ArrayList<ExpenseType>();
        ExpenseTypeCategory expTypeCat = new ExpenseTypeCategory(getText(R.string.general).toString(),
                R.drawable.help_24);
        customExpenseType.add(expTypeCat);
        ExpenseType expType = new ExpenseType(expName, expKey);
        expTypeCat.addExpenseType(expType);
        customExpenseType.add(expType);
        return customExpenseType;
    }

    /**
     * Will inititalize the state of the UI.
     * 
     * @param inState
     *            the bundle containing state information.
     */
    private void initState(Bundle inState) {
        if (inState != null) {
            // Restore the transaction date.
            String transDateStr = inState.getString(DATE_KEY);
            if (transDateStr != null && transDateStr.length() > 0) {
                datePickerDate = Parse.parseXMLTimestamp(transDateStr);
                updateDatePickerFieldValue();
            }
            // Restore the expense type.
            String expTypeKey = inState.getString(EXPENSE_TYPE_KEY);
            if (expTypeKey != null && expTypeKey.length() > 0) {
                int expTypeInd = expTypeAdapter.getPositionForExpenseType(expTypeKey);
                if (expTypeInd != -1) {
                    Object expTypeObj = expTypeAdapter.getItem(expTypeInd);
                    if (expTypeObj instanceof ExpenseType) {
                        setSelectedExpenseType((ExpenseType) expTypeObj);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onRestoreInstanceState: invalid expense type position!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onRestoreInstanceState: restored expense type not found!");
                }
            }
            // Restore the vendor name.
            String vendorName = inState.getString(VENDOR_KEY);
            if (vendorName != null && vendorName.length() > 0) {
                ViewUtil.setTextViewText(this, R.id.expense_vendor, R.id.field_value, vendorName, true);
            }
            // Restore the location name.
            String locationName = inState.getString(LOCATION_KEY);
            if (locationName != null && locationName.length() > 0) {
                ViewUtil.setTextViewText(this, R.id.expense_location, R.id.field_value, locationName, true);
            }
            // Restore the currency type.
            String curCode = inState.getString(CURRENCY_KEY);
            if (curCode != null && curCode.length() > 0) {
                int curTypeInd = curTypeAdapter.getPositionForCurrency(curCode);
                if (curTypeInd != -1) {
                    Object curTypeObj = curTypeAdapter.getItem(curTypeInd);
                    if (curTypeObj instanceof ListItem) {
                        setSelectedCurrencyType((ListItem) curTypeObj);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onRestoreInstanceState: invalid currency type position!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onRestoreInstanceState: restored currency type not found!");
                }
            }
            // Restore the amount.
            String amount = inState.getString(AMOUNT_KEY);
            if (amount != null && amount.length() > 0) {
                ViewUtil.setTextViewText(this, R.id.expense_amount, R.id.field_value, amount, true);
            }
            // Restore the comment.
            String comment = inState.getString(COMMENT_KEY);
            if (comment != null && comment.length() > 0) {
                ViewUtil.setTextViewText(this, R.id.expense_comment, R.id.field_value, comment, true);
            }
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
                configureReceiptLabel();
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

        // MOB-15824
        // In QuickExpense, view is built before state is initialized, and we don't have values set until state is initialized.
        // Set text color for required fields without default values.
        setRequiredFieldLabelColors();
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
        if (selExpType != null) {
            outState.putString(EXPENSE_TYPE_KEY, selExpType.getKey());
        }
        // Save the vendor.
        String vendorName = ViewUtil.getTextViewText(this, R.id.expense_vendor, R.id.field_value);
        if (vendorName != null && vendorName.length() > 0) {
            vendorName = vendorName.trim();
        }
        outState.putString(VENDOR_KEY, vendorName);
        // Save the location.
        String locationName = ViewUtil.getTextViewText(this, R.id.expense_location, R.id.field_value);
        if (locationName != null && locationName.length() > 0) {
            locationName = locationName.trim();
        }
        outState.putString(LOCATION_KEY, locationName);
        // Save the currency type.
        if (selCurType != null) {
            outState.putString(CURRENCY_KEY, selCurType.code);
        }
        // Set the amount.
        String amount = ViewUtil.getTextViewText(this, R.id.expense_amount, R.id.field_value);
        if (amount != null && amount.length() > 0) {
            amount = amount.trim();
        }
        outState.putString(AMOUNT_KEY, amount);
        // Set the comment.
        String comment = ViewUtil.getTextViewText(this, R.id.expense_comment, R.id.field_value);
        if (comment != null && comment.length() > 0) {
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
            if (!isMobileEntryOfflineReceiptData() && mobileEntry.getReceiptImageId() != null) {
                previousReceiptImageId = mobileEntry.getReceiptImageId();
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
        boolean retVal = (mobileEntry.hasReceiptImageDataLocal() || mobileEntry.getReceiptImageDataLocalFilePath() != null);
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view present, handling result.");
            if (requestCode == REQUEST_TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    receiptCaptureSuccess(data);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                    toast.show();
                    resetPreviousReceiptImageDataValues();
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(TakePicture): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (!copySelectedImage(data)) {
                        // Flurry Notification.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_FAILURE,
                                Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                        showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                        resetPreviousReceiptImageDataValues();
                        configureReceiptLabel();
                    } else {
                        // Set the last receipt action.
                        lastReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;

                        configureReceiptLabel();
                        // Only prompt for receipt append if we're currently
                        // connected, the mobile entry does not
                        // have offline receipt data and a previous image was
                        // captured/selected from Gallery/Receipt Store.
                        if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                            if (hasPreviousReceiptImageDataValues()) {
                                showDialog(Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND);
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
                    Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                    toast.show();
                    resetPreviousReceiptImageDataValues();
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChoosePicture): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_CHOOSE_CLOUD_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    receiptImageId = data.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                    if (receiptImageId != null) {
                        // Set the receipt action flag.
                        lastReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD;

                        configureReceiptLabel();
                        // Only prompt for receipt append if we're currently
                        // connected, the mobile entry does not
                        // have offline receipt data and a previous image was
                        // captured/selected from Gallery/Receipt Store.
                        if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                            if (hasPreviousReceiptImageDataValues()) {
                                showDialog(Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND);
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
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onActivityResult(ChooseCloudPicture): ok result intent missing receipt image id!");
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                    toast.show();
                    resetPreviousReceiptImageDataValues();
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChooseCloudImage): unhandled result code '"
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
                        ViewUtil.setTextViewText(QuickExpense.this, R.id.expense_location, R.id.field_value, locValue,
                                true);
                        // MOB-11190 Automatically update the
                        // Currency field
                        // to the selected Country/currency.
                        if (crnCode != null && !currencyReadOnly) {
                            setSelectedCurrencyType(crnCode);
                        }

                    } else {
                        ViewUtil.setTextViewText(QuickExpense.this, R.id.expense_location, R.id.field_value, "", true);
                    }
                }
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view delayed, delaying handling of result.");
            activityResultDelay = true;
            activityResultRequestCode = requestCode;
            activityResultResultCode = resultCode;
            activityResultData = data;
        }
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

            showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
        } else {
            // Set the last receipt action.
            lastReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;

            configureReceiptLabel();
            // Only prompt for receipt append if we're currently connected, the
            // mobile entry does not
            // have offline receipt data and a previous image was
            // captured/selected from Gallery/Receipt Store.
            if (ConcurCore.isConnected() && !isMobileEntryOfflineReceiptData()) {
                if (hasPreviousReceiptImageDataValues()) {
                    showDialog(Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND);
                }
            } else {
                // Delete the previously copied picture.
                removePreviousCopiedPicture(previousReceiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath);
                previousReceiptImageDataLocalFilePath = null;
                previousReceiptImageId = null;
                deleteReceiptImageDataLocalFilePath = false;
            }
        }
    }

    /**
     * Will copy the image data captured by the camera.
     * 
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // Assign the path written by the camera application.
        receiptImageDataLocalFilePath = receiptCameraImageDataLocalFilePath;
        retVal = ViewUtil.compressAndRotateImage(receiptImageDataLocalFilePath);
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
        InputStream inputStream = ViewUtil.getInputStream(this, data.getData());
        int angle = ViewUtil.getOrientaionAngle(this, data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                    .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ViewUtil.closeInputStream(inputStream);
            inputStream = null;
            if (recConf != null) {
                // Copy from the input stream to an external file.
                receiptImageDataLocalFilePath = ViewUtil.createExternalMediaImageFilePath();
                inputStream = new BufferedInputStream(ViewUtil.getInputStream(this, data.getData()), (8 * 1024));
                if (!ViewUtil.copySampledBitmap(inputStream, receiptImageDataLocalFilePath, recConf.sampleSize,
                        recConf.compressFormat, recConf.compressQuality, angle)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to copy sampled image from '"
                            + inputStream + "' to '" + receiptImageDataLocalFilePath + "'");
                    receiptImageDataLocalFilePath = null;
                    deleteReceiptImageDataLocalFilePath = false;
                    retVal = false;
                } else {
                    deleteReceiptImageDataLocalFilePath = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copySelectedImage: unable to obtain recommended samplesize, etc.!");
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
                    Log.w(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: failed to delete file '"
                            + imageFilePath + "'.");
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: deleted file '" + imageFilePath
                            + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: picture file '" + imageFilePath
                        + "does not exist!");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onServiceAvailable()
     */
    @Override
    public void onServiceAvailable() {

        if (buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: build view was delayed, constructing view now.");
            // Construct the view.
            buildView();
            initState(lastSavedInstanceState);
            buildViewDelay = false;

            // If 'onActivityResult' call was delayed due to the build view
            // delay, then
            // register them as well.
            if (activityResultDelay) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: activity result was delayed, handling result now.");
                onActivityResult(activityResultRequestCode, activityResultResultCode, activityResultData);
                activityResultDelay = false;
                // Ensure we release reference to the data!
                activityResultData = null;
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onServiceUnavailable()
     */
    @Override
    public void onServiceUnavailable() {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceUnavailable: ");
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity# getNetworkActivityText(java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        String retVal;
        switch (networkMsgType) {
        case Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST:
            retVal = getText(R.string.retrieve_mini_receipt).toString();
            break;
        default:
            retVal = defaultText;
            break;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#isNetworkRequestInteresting (int)
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkRequestType) {
        return (networkRequestType == Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onDestroy()
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (receiptImageDataLocalFilePath != null && !saveSucceeded && !savedInstanceState
                && deleteReceiptImageDataLocalFilePath) {
            Log.i(Const.LOG_TAG, CLS_TAG + ".onDestroy: deleting receipt image file '" + receiptImageDataLocalFilePath
                    + "'.");
            File receiptImageFile = new File(receiptImageDataLocalFilePath);
            if (receiptImageFile.exists()) {
                receiptImageFile.delete();
                Log.d(Const.LOG_TAG, CLS_TAG + ".onDestroy: deleted receipt image file '"
                        + receiptImageDataLocalFilePath + "'.");
            }
        }
    }

    // /**
    // * Get Expense Type for Quick Expense. Here POL_KEY is -1.
    // *
    // * @return expense type list from database.
    // */
    // private ArrayList<ExpenseType> getExpenseForQuickExpenseFromDB() {
    // List<ExpenseType> expTypesFromDB = new ArrayList<ExpenseType>();
    // ConcurCore concurCore = (ConcurCore) getApplication();
    // IExpenseEntryCache expEntCache = concurCore.getExpenseEntryCache();
    // ConcurService concurService = concurCore.getService();
    // expTypesFromDB = expEntCache.getExpenseTypesFromDB(POL_KEY,
    // concurService);
    // if (expTypesFromDB != null && expTypesFromDB.size() > 0) {
    // expTypesFromDB = expEntCache.sortExpenseList(expTypesFromDB);
    // }
    // return (ArrayList<ExpenseType>) expTypesFromDB;
    // }
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
            updateDatePickerFieldValue();

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
                chooseReceiptPicture();
                break;
            }
            case CHOOSE_PICTURE_CLOUD: {
                selectCloudReceipt();
                break;
            }
            case CLEAR_PICTURE: {
                clearReceiptPicture();
                break;
            }
            case DOWNLOAD_PICTURE: {
                downloadReceiptPicture();
                break;
            }
            case TAKE_PICTURE: {
                takeReceiptPicture();
                break;
            }
            case VIEW: {
                viewReceiptPicture();
                break;
            }
            }
            dismissDialog(DIALOG_RECEIPT_IMAGE);
            removeDialog(DIALOG_RECEIPT_IMAGE);
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

            LayoutInflater inflater = LayoutInflater.from(QuickExpense.this);

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
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }
            return view;
        }
    }

    /**
     * Will send a request to save the receipt.
     */
    private void sendSaveExpenseRequest() {
        ConcurService concurService = getConcurService();
        registerSaveExpenseReceiver();
        // Save the mobile entry.
        saveExpenseRequest = concurService.saveMobileEntry(getUserId(), mobileEntry, lastReceiptAction,
                receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, false);
        if (saveExpenseRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveExpenseRequest: unable to create request to save expense!");
            unregisterSaveExpenseReceiver();
        } else {
            // Set the request object on the receiver.
            saveExpenseReceiver.setServiceRequest(saveExpenseRequest);
            showDialog(DIALOG_SAVE_EXPENSE);
        }
    }

    /**
     * Will send a request to save the receipt.
     */
    private void saveSmartExpense() {

        if (saveSmartExpenseRequest != null) {
            //We are in the middle of another save
            return;
        }

        if (saveSmartExpenseReceiver == null) {
            saveSmartExpenseReceiver = new BaseAsyncResultReceiver(new Handler());
            saveSmartExpenseReceiver.setListener(new SaveSmartExpenseListener());
        }

        //Setup SmartExpense
        SmartExpense smartExpense = new SmartExpense(this, getUserId());
        smartExpense.setExpKey(mobileEntry.getExpKey());
        smartExpense.setExpenseName(mobileEntry.getExpName());
        smartExpense.setTransactionAmount(mobileEntry.getTransactionAmount());
        smartExpense.setCrnCode(mobileEntry.getCrnCode());
        smartExpense.setTransactionDate(mobileEntry.getTransactionDateCalendar());
        smartExpense.setLocName(mobileEntry.getLocationName());
        smartExpense.setVendorDescription(mobileEntry.getVendorName());
        smartExpense.setComment(mobileEntry.getComment());
        smartExpense.setMeKey(mobileEntry.getMeKey());
        smartExpense.setSmartExpenseId(mobileEntry.smartExpenseId);

        saveSmartExpenseRequest = new SaveSmartExpenseRequestTask(this.getApplicationContext(),
            REQUEST_SAVE_SMART_EXPENSE, saveSmartExpenseReceiver, smartExpense, false);
        saveSmartExpenseRequest.execute();

        //Show the Save progress dialog
        showDialog(DIALOG_SAVE_SMART_EXPENSE);
    }

    /**
     * Will register an instance of <code>SaveExpenseReceiver</code> with the application context and set the
     * <code>saveExpenseReceiver</code> attribute.
     */
    protected void registerSaveExpenseReceiver() {
        if (saveExpenseReceiver == null) {
            saveExpenseReceiver = new SaveExpenseReceiver(this);
            if (saveExpenseFilter == null) {
                saveExpenseFilter = new IntentFilter(Const.ACTION_EXPENSE_MOBILE_ENTRY_SAVED);
            }
            getApplicationContext().registerReceiver(saveExpenseReceiver, saveExpenseFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveExpenseReceiver: saveExpenseReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveExpenseReceiver</code> with the application context and set the
     * <code>saveExpenseReceiver</code> to <code>null</code>.
     */
    protected void unregisterSaveExpenseReceiver() {
        if (saveExpenseReceiver != null) {
            getApplicationContext().unregisterReceiver(saveExpenseReceiver);
            saveExpenseReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveExpenseReceiver: saveExpenseReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving an expense.
     */
    static class SaveExpenseReceiver extends BaseBroadcastReceiver<QuickExpense, SaveMobileEntryRequest> {

        /**
         * Constructs an instance of <code>SaveExpenseReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected SaveExpenseReceiver(QuickExpense activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(QuickExpense activity) {
            activity.saveExpenseRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_SAVE_EXPENSE);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_SAVE_FAILED);
            activity.unregisterSaveExpenseReceiver();
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            // Finish the activity.
            activity.saveSucceeded = true;
            // Set the flag that the expense entry cache should be refetched.
            ConcurCore ConcurCore = activity.getConcurCore();
            // Set the refresh list flag on the expense entry.
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();

            // If this is a failed OCR conversion, we need to clear the receipt store cache, too
            boolean isOCRConversion = false;
            Intent actIntent = activity.getIntent();
            if (actIntent.hasExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION)) {
                int mobileEntryAction = actIntent.getIntExtra(Const.EXTRA_EXPENSE_MOBILE_ENTRY_ACTION, -1);
                if (mobileEntryAction == Const.CREATE_MOBILE_ENTRY) {
                    isOCRConversion = actIntent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                }
            }

            // If the last receipt action was to select a receipt from the
            // Receipt Store (or clear the
            // receipt),
            // then set the flag that the list should be refetched.
            if (isOCRConversion || activity.lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD
                    || activity.lastReceiptAction == ReceiptPictureSaveAction.CLEAR_PICTURE) {
                ReceiptStoreCache rsCache = ConcurCore.getReceiptStoreCache();
                rsCache.setShouldFetchReceiptList();
            }

            if (activity.saveExpenseRequest != null) {

                // MOB-22375 - Google Analytics for Receipt Upload.
                if (activity.lastReceiptAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD
                        && activity.receiptImageId != null && activity.receiptImageId.trim().length() > 0) {

                    ConcurCore concurCore = ((ConcurCore) activity.getApplication());
                    LastLocationTracker locTracker = concurCore.getLocationTracker();
                    Location loc = locTracker.getCurrentLocaton();
                    String lat = "0";
                    String lon = "0";
                    if (loc != null) {
                        lat = Double.toString(loc.getLatitude());
                        lon = Double.toString(loc.getLongitude());
                    }
                    String eventLabel = activity.receiptImageId + "|" + lat + "|" + lon;
                    EventTracker.INSTANCE.track("Receipts", "Receipt Capture Location", eventLabel);
                }

                // Flurry Notification.
                boolean offlineCreate = intent.getBooleanExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, false);
                if (!offlineCreate) {
                    Map<String, String> params = new HashMap<String, String>();
                    String meKey = activity.saveExpenseRequest.getMobileEntryKey();
                    params.put(Flurry.PARAM_NAME_EDIT_NEW, ((meKey != null) ? Flurry.PARAM_VALUE_NEW
                            : Flurry.PARAM_VALUE_EDIT));
                    boolean hasReceipt = (!activity.saveExpenseRequest.getClearImage() && activity.saveExpenseRequest
                            .getMobileEntry().getReceiptImageId() != null);
                    params.put(Flurry.PARAM_NAME_CONTAINS_RECEIPT, ((hasReceipt) ? Flurry.PARAM_VALUE_YES
                            : Flurry.PARAM_VALUE_NO));
                    EventTracker.INSTANCE.track(Flurry.CATEGORY_MOBILE_ENTRY, Flurry.EVENT_NAME_SAVED, params);
                } else {
                    // Offline Create.
                }
            }
            activity.unregisterSaveExpenseReceiver();
            activity.updateMRUs(activity.saveExpenseRequest);
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }

        @Override
        protected void setActivityServiceRequest(SaveMobileEntryRequest request) {
            activity.saveExpenseRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterSaveExpenseReceiver();
        }

    }

    private void updateMRUs(SaveMobileEntryRequest saveExpenseRequest) {
        if (mrudataCollector != null) {
            if (saveExpenseRequest != null && saveExpenseRequest.mobileEntry != null) {
                if (getUserId() != null && getUserId().length() > 0) {
                    MobileEntry mobileEntryFromReq = saveExpenseRequest.mobileEntry;
                    mrudataCollector.setNewExpType(mobileEntryFromReq.getExpKey());
                    mrudataCollector.setNewCurType(mobileEntryFromReq.getCrnCode());
                    mrudataCollector.setNewLoc(mobileEntryFromReq.getLocationName());

                    // Currency MRU
                    MobileDatabase mdb = getConcurService().getMobileDatabase();
                    if (mrudataCollector.isNewExpType()) {
                        new ExpTypeMruAsyncTask(mdb, getUserId(), saveExpenseRequest.expKey, "-1", getConcurService())
                                .execute();
                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".updateMRu: user didnt select new expeType so no need to update MRU");
                    }
                    if (mrudataCollector.isNewCurType()) {
                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        if (selCurType != null) {
                            selCurType.setUserID(getUserId());
                            selCurType.setLastUseCount(1);
                            selCurType.setLastUsed(now);
                            selCurType.fieldId = ListItem.DEFAULT_KEY_CURRENCY;
                        }
                        new ListItemMruAsyncTask(selCurType, mdb, getUserId(), getConcurService()).execute();
                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".updateMRu: user didnt select new currency Type so no need to update MRU");
                    }
                    if (mrudataCollector.isNewLocation()) {
                        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                        ListItem locationItem = new ListItem();
                        locationItem.setUserID(getUserId());
                        locationItem.setLastUseCount(1);
                        locationItem.setLastUsed(now);
                        locationItem.fieldId = ListItem.DEFAULT_KEY_LOCATION;
                        if (locLiKey == null) {
                            locLiKey = "";
                        }
                        locationItem.code = locLiKey;
                        locationItem.key = locLiKey;
                        if (locValue == null) {
                            locValue = "";
                        }
                        locationItem.text = locValue;
                        List<ListItemField> fieldItems = new ArrayList<ListItemField>();
                        if (locCrnCode != null) {
                            fieldItems.add(new ListItemField(ListSearch.CODE_ID, locCrnCode));
                        }
                        if (locCrnKey != null) {
                            fieldItems.add(new ListItemField(ListSearch.KEY_ID, locCrnKey));
                        }
                        if (fieldItems != null) {
                            locationItem.fields = fieldItems;
                        }
                        new ListItemMruAsyncTask(locationItem, mdb, getUserId(), getConcurService()).execute();

                    } else {
                        Log.d(Const.LOG_TAG, CLS_TAG
                                + ".updateMRu: user didnt select new location so no need to update MRU");
                    }
                } else {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".updateMRu: userID is null");
                }
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".updateMRu: saveExpenseRequest is null || saveExpenseRequest.mobileEntry is null ");
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".updateMRu: mrudataCollector is null");
        }
    }

    /**
     * Will send a request to save the receipt.
     */
    private void sendSaveReceiptRequest(String imageFilePath, boolean deleteImageFile) {
        ConcurService concurService = getConcurService();
        registerSaveReceiptReceiver();
        boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
        if (useConnect) {
            saveReceiptRequest = concurService.sendConnectPostImageRequest(getUserId(), imageFilePath, deleteImageFile,
                    null, false);
        } else {
            saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(), imageFilePath, deleteImageFile,
                    null, false);
        }
        if (saveReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveReceiptRequest: unable to create request to save receipt!");
            unregisterSaveReceiptReceiver();
        } else {
            // Set the request object on the receiver.
            saveReceiptReceiver.setServiceRequest(saveReceiptRequest);
            showDialog(DIALOG_SAVE_RECEIPT);
        }
    }

    /**
     * Will register an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> attribute.
     */
    protected void registerSaveReceiptReceiver() {
        if (saveReceiptReceiver == null) {
            saveReceiptReceiver = new SaveReceiptReceiver(this);
            if (saveReceiptFilter == null) {
                saveReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_SAVE);
            }
            getApplicationContext().registerReceiver(saveReceiptReceiver, saveReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReceiptReceiver: saveReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> to <code>null</code>.
     */
    protected void unregisterSaveReceiptReceiver() {
        if (saveReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(saveReceiptReceiver);
            saveReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReceiptReceiver: saveReceiptReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the response to saving a receipt.
     */
    static class SaveReceiptReceiver extends BaseBroadcastReceiver<QuickExpense, SaveReceiptRequest> {

        /**
         * Constructs an instance of <code>ReceiptSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected SaveReceiptReceiver(QuickExpense activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(QuickExpense activity) {
            activity.saveReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            if (!activity.savingForAppend) {
                activity.dismissDialog(DIALOG_SAVE_RECEIPT);
            }
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {

            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

            activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            boolean handled = false;
            if (httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                if (mwsErrorMessage != null
                        && mwsErrorMessage.equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                    activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                    handled = true;
                }
            }
            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

            return handled;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.core.activity.BaseActivity.BaseBroadcastReceiver #handleRequestFailure(android.content.Context,
         * android.content.Intent, int)
         */
        @Override
        protected void handleRequestFailure(Context context, Intent intent, int requestStatus) {
            super.handleRequestFailure(context, intent, requestStatus);

            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                // Set the Receipt Image ID on the local reference and in the
                // mobile entry reference.
                String receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                if (receiptImageId != null) {
                    receiptImageId = receiptImageId.trim();

                    // MOB-22375 - Google Analytics for Receipt Upload.
                    // If we're online, track the location right away!
                    // If we're offline, then location will be saved in the DB for later
                    // reference in the Offline Queue.
                    if (!SaveReceiptReply.OFFLINE_RECEIPT_ID.equals(receiptImageId)) {
                        LastLocationTracker locTracker = activity.getConcurCore().getLocationTracker();
                        Location loc = locTracker.getCurrentLocaton();
                        String lat = "0";
                        String lon = "0";
                        if (loc != null) {
                            lat = Double.toString(loc.getLatitude());
                            lon = Double.toString(loc.getLongitude());
                        }

                        String eventLabel = receiptImageId + "|" + lat + "|" + lon;
                        EventTracker.INSTANCE.track("Receipts", "Receipt Capture Location", eventLabel);
                    }
                }
                if (activity.isPathAvailable) {
                    // Set the variable to 'false'.
                    // Store the new value in the 'retainer' object where it
                    // will now override the
                    // value in the 'intent'.
                    activity.isPathAvailable = false;
                }
                if (!activity.savingForAppend) {

                    if (receiptImageId != null && receiptImageId.length() > 0) {
                        if (!SaveReceiptReply.OFFLINE_RECEIPT_ID.equals(receiptImageId)) {
                            activity.receiptImageId = receiptImageId;
                            activity.mobileEntry.setReceiptImageId(receiptImageId);
                        } else {
                            activity.mobileEntry.setReceiptImageDataLocalFilePath(serviceRequest.filePath);
                            activity.mobileEntry.setReceiptImageDataLocal(true);
                        }
                        // Proceed with saving the expense.
                        activity.sendSaveExpenseRequest();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".handleSuccess: save receipt result intent has null/empty receipt image id!");
                        handleFailure(context, intent);
                    }

                    // Flurry Notification
                    boolean offlineCreate = intent.getBooleanExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, false);
                    if (!offlineCreate) {
                        activity.postFlurryEvent();
                    }
                } else {
                    // Post the Flurry event even though an append operation.
                    activity.postFlurryEvent();

                    boolean setReceiptImageId = false;
                    // First, check whether the previous receipt image data was
                    // saved.
                    if (activity.previousReceiptImageDataLocalFilePath != null
                            && activity.previousReceiptImageId == null) {
                        activity.previousReceiptImageId = receiptImageId;
                        setReceiptImageId = true;
                    }
                    // Second, check whether the current receipt image data was
                    // saved.
                    if (activity.receiptImageDataLocalFilePath != null && activity.receiptImageId == null) {
                        if (!setReceiptImageId) {
                            activity.receiptImageId = receiptImageId;
                        } else {
                            activity.sendSaveReceiptRequest(activity.receiptImageDataLocalFilePath, true);
                        }
                    }
                    // Third, check whether both receipt image id's are
                    // available, in that case, just perform
                    // the append.
                    if (activity.previousReceiptImageId != null && activity.receiptImageId != null) {
                        // if(!setReceiptImageId) {
                        activity.sendAppendReceiptRequest(activity.receiptImageId, activity.previousReceiptImageId);
                        // }
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: save receipt succeeded but missing receipt image id!");
                handleFailure(context, intent);
            }
        }

        @Override
        protected void setActivityServiceRequest(SaveReceiptRequest request) {
            activity.saveReceiptRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterSaveReceiptReceiver();
        }

    }

    protected void postFlurryEvent() {
        Map<String, String> params = new HashMap<String, String>();
        if (lastReceiptAction != null) {
            String paramValue = null;
            switch (lastReceiptAction) {
            case CHOOSE_PICTURE: {
                paramValue = Flurry.PARAM_VALUE_ALBUM;
                break;
            }
            case TAKE_PICTURE: {
                paramValue = Flurry.PARAM_VALUE_CAMERA;
                break;
            }
            case CHOOSE_PICTURE_CLOUD: {
                paramValue = Flurry.PARAM_VALUE_RECEIPT_STORE;
                break;
            }
            }
            if (paramValue != null) {
                params.put(Flurry.PARAM_NAME_ADDED_USING, paramValue);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_ADD_TO_MOBILE_ENTRY, params);
            }
        }
    }

    /**
     * Will send a request to append a receipt to another receipt based on receipt image ID's.
     * 
     * @param fromReceiptImageId
     *            contains the receipt image ID of the receipt being appended.
     * @param toReceiptImageId
     *            contains the receipt image ID of the receipt being appended to.
     */
    protected void sendAppendReceiptRequest(String fromReceiptImageId, String toReceiptImageId) {
        ConcurService concurService = getConcurService();
        registerAppendReceiptReceiver();
        appendReceiptRequest = concurService.sendAppendReceiptImageRequest(getUserId(), fromReceiptImageId,
                toReceiptImageId);
        if (appendReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendAppendReceiptRequest: unable to create 'AppendReceiptImage' request!");
            unregisterAppendReceiptReceiver();
        } else {
            appendReceiptReceiver.setServiceRequest(appendReceiptRequest);
        }
    }

    /**
     * Will create and register with the application context an instance of <code>AppendReceiptReceiver</code> and set it on
     * <code>appendReceiptReceiver</code> attribute.
     */
    protected void registerAppendReceiptReceiver() {
        if (appendReceiptReceiver == null) {
            appendReceiptReceiver = new AppendReceiptReceiver(this);
            if (appendReceiptFilter == null) {
                appendReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_APPENDED);
            }
            getApplicationContext().registerReceiver(appendReceiptReceiver, appendReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAppendReportEntryReceiver: appendReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of <code>AppendReceiptReceiver</code> and set the
     * <code>appendReportEntryReceiptReceiver</code> attribute to 'null'.
     */
    protected void unregisterAppendReceiptReceiver() {
        if (appendReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(appendReceiptReceiver);
            appendReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAppendReceiptReceiver: appendReceiptReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of appending a receipt to a
     * report entry.
     */
    static class AppendReceiptReceiver extends BaseBroadcastReceiver<QuickExpense, AppendReceiptImageRequest> {

        /**
         * Constructs an instance of <code>AppendReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AppendReceiptReceiver(QuickExpense activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(QuickExpense activity) {
            activity.appendReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_SAVE_RECEIPT);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // activity.showDialog(Const.DIALOG_TRAVEL_RETRIEVE_ITINERARY_FAILED);
            // TODO: show failure dialog.
            activity.resetPreviousReceiptImageDataValues();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Show a success dialog.
            // TODO: show a successful append dialog.
            // Reset 'activity.receiptImageId' to the receipt image ID that was
            // appended to.
            // This will ensure at Quick Expense save time that this receipt
            // image ID will be
            // reset on the mobile entry object.
            String toReceiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_TO_RECEIPT_IMAGE_ID_KEY);
            if (toReceiptImageId != null) {

                // Set the 'receiptImageId' on the activity.
                toReceiptImageId = toReceiptImageId.trim();
                activity.receiptImageId = toReceiptImageId;

                // Set the last action to 'APPEND'.
                activity.lastReceiptAction = ReceiptPictureSaveAction.APPEND;

                // Clear receipt related data values.
                activity.clearReceiptImageDataValues();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent data has no value for 'toReceiptImageId'!");
            }

            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_QUICK_EXPENSE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_APPEND, params);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AppendReceiptImageRequest request) {
            activity.appendReceiptRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAppendReceiptReceiver();
        }

    }

}
