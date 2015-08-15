/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.data.UserConfig;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.data.IExpenseReportCache;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.expense.report.approval.service.ApproveReportRequest;
import com.concur.mobile.core.expense.report.data.ExpenseConfirmation;
import com.concur.mobile.core.expense.report.data.ExpenseReport;
import com.concur.mobile.core.expense.report.data.ExpenseReportApprover;
import com.concur.mobile.core.expense.report.data.ExpenseReportComment;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportException;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField;
import com.concur.mobile.core.expense.report.data.ExpenseReportFormField.AccessType;
import com.concur.mobile.core.expense.report.data.WorkflowAction;
import com.concur.mobile.core.expense.report.service.AddReportReceiptRequest;
import com.concur.mobile.core.expense.report.service.AddReportReceiptV2Request;
import com.concur.mobile.core.expense.report.service.AppendReceiptImageRequest;
import com.concur.mobile.core.expense.report.service.ClearReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.ConditionalFieldAction;
import com.concur.mobile.core.expense.report.service.GetConditionalFieldActionRequest;
import com.concur.mobile.core.expense.report.service.GetTaxFormReply;
import com.concur.mobile.core.expense.report.service.GetTaxFormRequest;
import com.concur.mobile.core.expense.report.service.ReportDetailRequest;
import com.concur.mobile.core.expense.report.service.ReportHeaderDetailRequest;
import com.concur.mobile.core.expense.report.service.SaveReportEntryReceiptRequest;
import com.concur.mobile.core.expense.report.service.SubmitReportRequest;
import com.concur.mobile.core.expense.report.service.TaxForm;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormUtil;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.FormFieldView;
import com.concur.mobile.core.view.FormFieldView.ValidityCheck;
import com.concur.mobile.core.view.FormFieldViewListener;
import com.concur.mobile.core.view.SearchListFormFieldView;
import com.concur.mobile.core.view.ViewOnClickHandler;
import com.concur.mobile.platform.location.LastLocationTracker;
import com.concur.mobile.platform.util.Format;

/**
 * Provides a base activity class for some common operations across various subclasses.
 * 
 * @author AndrewK
 */
public abstract class AbstractExpenseActivity extends BaseActivity {

    private static final String CLS_TAG = AbstractExpenseActivity.class.getSimpleName();

    /**
     * NOTE: Currently, the report ID that must be provided when uploading a receipt via Connect is different then the report key
     * that is returned via the MWS. Until that situation is remedied, this will have to be disabled. Likewise, the receipt image
     * ID returned via Connect when passed up to the 'SetReportEntryReceipt' MWS call results in a failed response due to the MWS
     * expecting a protected receipt image ID.
     */
    protected static final boolean ADD_RECEIPT_VIA_CONNECT_ENABLED = Boolean.FALSE;

    protected static final boolean CLEAR_REPORT_ENTRY_RECEIPT_ENABLED = Boolean.FALSE;

    protected static final int DIALOG_RECEIPT_IMAGE = 1;

    protected static final int DIALOG_CONFIRM_SAVE = 2;
    protected static final int DIALOG_RECEIPT_IMAGE_UNAVAILABLE = 7;

    private static final int REQUEST_TAKE_PICTURE = 0;

    private static final int REQUEST_CHOOSE_IMAGE = 1;

    protected static final int REQUEST_VIEW_ENTRY_DETAILS = 2;

    protected static final int REQUEST_VIEW_SUMMARY = 3;

    protected static final int REQUEST_VIEW_ITEMIZATIONS = 4;

    protected static final int REQUEST_CHOOSE_CLOUD_IMAGE = 5;

    protected static final int REQUEST_VIEW_ATTENDEES = 6;

    protected static final int REQUEST_VIEW_TA_ITINERARY = 7;

    private static final int NO_IMG_OR_REC_REQ = 0;
    private static final int IMG_REQ = 1;
    private static final int REC_REQ = 2;

    // Contains the key used to store/retrieve the state of the 'savingReportReceipt' boolean attribute.
    private static final String SAVING_REPORT_RECEIPT_KEY = "save.report.receipt.key";

    // Contains the key used to store/retrieve the state of the 'savingExpenseReceipt' boolean attribute.
    private static final String SAVING_EXPENSE_RECEIPT_KEY = "save.expense.receipt.key";

    // Contains the key used to store/retrieve the state of the 'savingReceiptImageId' string attribute.
    private static final String SAVING_RECEIPT_IMAGE_ID_KEY = "save.receipt.image.id";

    // Contains the key used to store/retrieve the current selected report entry.
    private static final String SELECTED_REPORT_ENTRY_KEY = "selected.report.entry.key";

    // Contains the key used to store/retrieve the file path given to the camera application.
    private static final String RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY = "expense.receipt.camera.image.file.path";

    // Contains the key used to store/retrieve the 'receiptImageDataLocalFilePath' value.
    private static final String RECEIPT_IMAGE_FILE_PATH_KEY = "expense.receipt.image.file.path";

    // Contains the key used to store/retrieve whether the file stored in 'receiptImageDataLocalFilePath' should
    // be punted after a save attempt.
    private static final String DELETE_RECEIPT_IMAGE_FILE_PATH = "expense.delete.receipt.image.file.path";

    // Contains the key used to store/retrieve the state of the 'processingBackPressed' boolean attribute.
    private static final String PROCESSING_BACK_PRESSED_KEY = "processing.back.pressed.key";

    // Contains the key used to store/retrieve the state of the 'processingSubmitPressed' boolean attribute.
    private static final String PROCESSING_SUBMIT_PRESSED_KEY = "processing.submit.pressed.key";

    // Contains the key used to store/retrieve the state of the 'processingReceiptAction' enum object.
    private static final String PROCESSING_RECEIPT_ACTION_KEY = "processing.receipt.action.key";

    // Contains the key used to store/retrieve the value of the 'receiptSaveAction' enum object.
    private static final String RECEIPT_SAVE_ACTION_KEY = "receipt.save.action.key";

    // Contains the key used to store/retrieve the current form field view set on a form field view listener.
    private static final String CURRENT_FORM_FIELD_VIEW_KEY = "current.form.field.view";

    // Contains the key used to store/retrieve the add report receipt receiver.
    private static final String ADD_REPORT_RECEIPT_RECEIVER_KEY = "add.report.receipt.receiver";

    // Contains the key used to store/retrieve the add report receipt V2 receiver.
    private static final String ADD_REPORT_RECEIPT_V2_RECEIVER_KEY = "add.report.receipt.v2.receiver";

    // Contains the key used to store/retrieve the report approve receiver.
    private static final String REPORT_APPROVE_RECEIVER_KEY = "report.approve.receiver";

    // Contains the key used to store/retrieve the report detail update receiver.
    private static final String REPORT_DETAIL_UPDATE_RECEIVER_KEY = "report.detail.update.receiver";

    // Contains the key used to store/retrieve the report submit receiver.
    private static final String REPORT_SUBMIT_RECEIVER_KEY = "report.submit.receiver";

    private static final String REPORT_HEADER_DETAIL_RECEIVER_KEY = "report.header.detail.receiver";

    // Contains the key used to store/retrieve the save receipt receiver.
    private static final String SAVE_RECEIPT_RECEIVER_KEY = "save.receipt.receiver";

    // Contains the key used to store/retrieve the append receipt receiver.
    private static final String APPEND_REPORT_ENTRY_RECEIPT_RECEIVER_KEY = "append.report.entry.receipt.receiver";

    // Contains the key used to store/retrieve the clear report entry receipt receiver.
    private static final String CLEAR_REPORT_ENTRY_RECEIPT_RECEIVER_KEY = "clear.report.entry.receipt.receiver";

    // Contains the key used to store/retrieve the save report entry receipt receiver.
    private static final String SAVE_REPORT_ENTRY_RECEIPT_RECEIVER_KEY = "save.report.entry.receipt.receiver";
    // Contains the key used to store/retrieve the list of missing/invalid or copy-down form fields.
    private static final String MISSING_INVALID_COPY_DOWN_FIELDS_KEY = "missing.invalid.copy.down.fields";
    // Contains the key used to store/retrieve the tax form receiver.
    private static final String SAVE_TAX_FORM_RECEIVER = "save.taxform.receiver";

    // Contains the key used to store/retrieve the dynamicfield receiver.
    private static final String SAVE_CONDITIONAL_FIELD_ACTION_RECEIVER = "save.conditional.field.action.receiver";

    private static final String SAVE_CONDITIONAL_FIELD_LAST_PAIRS = "save.conditional.field.last.paris";

    // Contains the keys used to store/retrieve the author, date and body for the currently selected comment.
    private static final String SELECTED_COMMENT_AUTHOR_KEY = "selected.comment.author";
    private static final String SELECTED_COMMENT_DATE_KEY = "selected.comment.date";
    private static final String SELECTED_COMMENT_BODY_KEY = "selected.comment.body";

    private static final String IMAGE_RECEIPT_REQUIRED_KEY = "image.receipt.required";

    private static final String REC_REQ_EXP_LIST_KEY = "rec.req.exp.list";

    /** Caching last conditional field entry so that we don't make unnecessary calls */
    protected Map<String, String> lastConditionalFieldEntries = null;

    /**
     * Storage for generated dialogs. This is needed to work around issues that Android has with managed dialogs and configuration
     * changes.
     */
    protected SparseArray<Dialog> dialogs = new SparseArray<Dialog>(5);

    /**
     * Contains a reference to the expense report cache in use by this activity.
     */
    protected IExpenseReportCache expRepCache;

    /**
     * Contains the key identifying the source of this report key. Should be one of
     * <code>Const.EXPENSE_REPORT_SOURCE_ACTIVE</code>, <code>Const.EXPENSE_REPORT_SOURCE_APPROVAL</code> or
     * <code>Const.EXPENSE_REPORT_SOURCE_NEW</code>
     */
    protected int reportKeySource;

    /**
     * Provides a reference to the <code>ExpenseReport</code> this activity was invoked on.
     */
    protected ExpenseReport expRep;

    /**
     * A reference to a <code>View.OnClickListener</code> that will handle view clicks for this activity.
     */
    protected ViewOnClickHandler viewClickHandler;

    /**
     * Contains a reference to a broadcast receiver to receive data update events.
     */
    protected ReportDetailReceiver reportDetailReceiver;

    /**
     * Contains a reference to an outstanding request to retrieve a report detail object.
     */
    protected ReportDetailRequest reportDetailRequest;

    /**
     * Contains the receiver to handle the outcome of submitting a report.
     */
    protected ReportSubmitReceiver reportSubmitReceiver;

    /**
     * Contains the receiver used to handle the result of retrieving dynamic fields actions information.
     */
    protected ConditionalActionReceiver conditionalFieldActionReceiver;

    /**
     * Contains the filter used to register the dynamicAction receiver.
     */
    protected IntentFilter conditionalFieldActionFilter;

    /**
     * Contains an outstanding request to retrieve expense type information.
     */
    protected GetConditionalFieldActionRequest conditionalFieldActionRequest;

    /**
     * Contains the filter to register the report submit receiver.
     */
    protected IntentFilter reportSubmitFilter;

    /**
     * Contains a reference to an outstanding report submit request.
     */
    protected SubmitReportRequest reportSubmitRequest;

    /**
     * Contains a reference to the report details dialog.
     */
    protected Dialog reportDetailDialog;

    /**
     * Contains a reference to a report entry save dialog.
     */
    protected Dialog saveReportEntryReceiptDialog;

    /**
     * Contains the body of the dialog.
     */
    private View dialogView;

    /**
     * Contains the most recent reason as to why a report submission failed.
     */
    private String reportSubmitFailedMessage;

    /**
     * Contains the receiver handling the outcome of approving a report.
     */
    private ReportApproveReceiver reportApproveReceiver;

    /**
     * Contains the filter used to register the report approve receiver.
     */
    private IntentFilter reportApproveFilter;

    /**
     * Contains a reference to an outstanding report approve request.
     */
    private ApproveReportRequest reportApproveRequest;

    /**
     * Contains the report approve status key based on end-user selection of one of <code>workflowActions</code>.
     */
    private String reportApproveStatKey;

    /**
     * A list of expense entries for which receipts are missing and a report is being submitted.
     */
    private ArrayList<ExpenseReportEntry> recReqExpList;

    /**
     * A flag for checking for ImageRequired and ReceiptRequired end points.
     */
    private int imageOrReceiptRequired;

    /**
     * Contains a reference to the last saved instance state.
     */
    protected Bundle lastSavedInstanceState;

    /**
     * Contains a reference to a broadcast receiver for handling the result of uploading a receipt.
     */
    protected SaveReceiptReceiver saveReceiptReceiver;

    /**
     * Contains the intent filter used to register the save receipt receiver.
     */
    protected IntentFilter saveReceiptFilter;

    /**
     * Contains a reference to any outstanding request object to save a receipt.
     */
    protected SaveReceiptRequest saveReceiptRequest;

    /**
     * Contains the broadcast receiver for handling the result of saving a report entry.
     */
    protected SaveReportEntryReceiptReceiver saveReportEntryReceiptReceiver;

    /**
     * Contains the intent filter used to register the save report entry receipt receiver.
     */
    protected IntentFilter saveReportEntryReceiptFilter;

    /**
     * Contains a reference to any outstanding request object to save a report entry receipt.
     */
    protected SaveReportEntryReceiptRequest saveReportEntryReceiptRequest;

    /**
     * Contains a reference to any outstanding request object to get Tax Form
     */
    protected GetTaxFormRequest getTaxFormRequest;

    /**
     * Contains the broadcast receiver for handling the result of tax form
     */
    protected TaxFormReceiver taxFormReceiver;

    /**
     * Contains the filter to register the tax form receiver
     */
    protected IntentFilter taxFormFilter;

    /**
     * Contains a reference to any outstanding request object to clear a receipt.
     */
    protected ClearReportEntryReceiptRequest clearReportEntryReceiptRequest;

    /**
     * Contains the broadcast receiver for handling the result of clearing a report entry receipt.
     */
    protected ClearReportEntryReceiptReceiver clearReportEntryReceiptReceiver;

    /**
     * Contains the intent filter used to register the clear report entry receipt receiver.
     */
    protected IntentFilter clearReportEntryReceiptFilter;

    /**
     * Contains the broadcast receiver for handling the result of appending a receipt image to a report entry.
     */
    protected AppendReportEntryReceiptReceiver appendReportEntryReceiptReceiver;

    /**
     * Contains the filter used to register the append report entry receipt receiver.
     */
    protected IntentFilter appendReportEntryReceiptFilter;

    /**
     * Contains a reference to an outstanding request to append a receipt to a report entry.
     */
    protected AppendReceiptImageRequest appendReportEntryReceiptRequest;

    /**
     * Contains the broadcast receiver for handling the result of adding a receipt to a report.
     */
    protected AddReportReceiptV2Receiver addReportReceiptV2Receiver;

    /**
     * Contains the filter used to register the add report receipt receiver.
     */
    protected IntentFilter addReportReceiptV2Filter;

    /**
     * Contains an outstanding request to add a report receipt.
     */
    protected AddReportReceiptV2Request addReportReceiptV2Request;

    /**
     * Contains the broadcast receiver for handling the result of adding a receipt to a report.
     */
    protected AddReportReceiptReceiver addReportReceiptReceiver;

    /**
     * Contains the filter used to register the add report receipt receiver.
     */
    protected IntentFilter addReportReceiptFilter;

    /**
     * Contains an outstanding request to add a report receipt.
     */
    protected AddReportReceiptRequest addReportReceiptRequest;

    /**
     * Contains the broadcast receiver for handling the result of updating the report header details.
     */
    protected ReportHeaderDetailReceiver reportHeaderDetailReceiver;

    /**
     * Contains the filter used to register the report header detail receiver.
     */
    protected IntentFilter reportHeaderDetailFilter;

    /**
     * Contains an outstanding request to retrieve report header details.
     */
    protected ReportHeaderDetailRequest reportHeaderDetailRequest;

    /**
     * Contains whether or not an expense receipt save is being performed.
     */
    protected boolean savingExpenseReceipt;

    /**
     * Contains whether or not a report receipt save is being performed.
     */
    protected boolean savingReportReceipt;

    /**
     * Contains the list adapter used to populate the options.
     */
    private ReceiptImageOptionListAdapter receiptActionAdapter;

    /**
     * Contains the key of the currently selected expense report entry.
     */
    private String selExpEntKey;

    /**
     * Contains the author of the currently selected comment.
     */
    protected String selComAuthor;

    /**
     * Contains the date of the currently selected comment.
     */
    protected String selComDate;

    /**
     * Contains the body of the currently selected comment.
     */
    protected String selComBody;

    /**
     * Contains the receipt image ID of the receipt to be either appended to, or replacing for the currently selected report
     * entry.
     */
    protected String savingReceiptImageId;

    /**
     * Contains the path within the receipt image directory of the image.
     */
    protected String receiptImageDataLocalFilePath;

    /**
     * Contains the path provided to the camera activity in which to store a captured image.
     */
    private String receiptCameraImageDataLocalFilePath;

    /**
     * Contains whether or not the receipt image file referenced by 'receiptImageDataLocalFilePath'
     */
    private boolean deleteReceiptImageDataLocalFilePath;

    /**
     * Contains a reference to the form field view listener.
     */
    protected FormFieldViewListener frmFldViewListener;

    /**
     * Contains whether the back hardware button was pressed. This flag is used to detect whether the activity should be finished
     * after a report save has taken place.
     */
    protected boolean processingBackPressed;

    /**
     * Contains whether or not a save operation was kicked off as a result of the end-user pressing the submit button with
     * outstanding form edits.
     */
    protected boolean processingSubmitPressed;

    /**
     * A non-null value indicates an itemize operation was kicked off as a result of the end-user pressing the itemize button with
     * outstanding form edits.
     */
    protected View processingItemizePressed;

    /**
     * A non-null value indicates a save operation was kicked-off as a result of the end-user performing a receipt action.
     */
    protected ReceiptPictureSaveAction processingReceiptAction;

    /**
     * Contains the list of <code>FormFieldView</code> objects that are required but with missing values, invalid values or
     * copy-down field values.
     */
    protected List<FormFieldView> missReqInvalidCopyDownFormFieldValues;

    /**
     * Contains the end-user choice whether to copy-down values into related fields.
     */
    protected boolean overWriteCopyDownValues;

    /**
     * Contains whether if after a successful result of either adding a receipt to a report or associating with an expense, the
     * receipt store cache refresh list flag is set.
     */
    protected boolean refreshReceiptStore;

    /**
     * Contains the most recent receipt action, i.e., choose from cloud, choose from device or take picture.
     */
    protected ReceiptPictureSaveAction receiptSaveAction = ReceiptPictureSaveAction.NO_ACTION;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Save the passed in instance state.
        lastSavedInstanceState = savedInstanceState;

        // Construct the listener on any form field view objects.
        frmFldViewListener = createFormFieldViewListener();

        // Construct a view click listener.
        viewClickHandler = new ViewOnClickHandler(this);

        // Only start the process of initializing the view if the service component is available.
        if (isServiceAvailable()) {
            Intent intent = getIntent();
            setExpenseReport(intent);
            initializeState();
        } else {
            buildViewDelay = true;
        }

    }

    /**
     * Will construct an instance of <code>FormFieldViewListener</code> to use with form editing.
     * 
     * @return an instance of <code>FormFieldViewListener</code> to use with form editing.
     */
    protected FormFieldViewListener createFormFieldViewListener() {
        return new FormFieldViewListener(this);
    }

    /**
     * Will intialize the state of any currently selected expense report entry + any application context level broadcast
     * receivers.
     */
    @SuppressWarnings("unchecked")
    private void initializeState() {

        // Check for saved state.
        if (lastSavedInstanceState != null) {
            savingReportReceipt = lastSavedInstanceState.getBoolean(SAVING_REPORT_RECEIPT_KEY, false);
            savingExpenseReceipt = lastSavedInstanceState.getBoolean(SAVING_EXPENSE_RECEIPT_KEY, false);
            savingReceiptImageId = lastSavedInstanceState.getString(SAVING_RECEIPT_IMAGE_ID_KEY);
            processingBackPressed = lastSavedInstanceState.getBoolean(PROCESSING_BACK_PRESSED_KEY, false);
            processingSubmitPressed = lastSavedInstanceState.getBoolean(PROCESSING_SUBMIT_PRESSED_KEY, false);
            imageOrReceiptRequired = lastSavedInstanceState.getInt(IMAGE_RECEIPT_REQUIRED_KEY);

            if (lastSavedInstanceState.containsKey(PROCESSING_RECEIPT_ACTION_KEY)) {
                String receiptActionStr = lastSavedInstanceState.getString(PROCESSING_RECEIPT_ACTION_KEY);
                if (receiptActionStr != null) {
                    processingReceiptAction = ReceiptPictureSaveAction.valueOf(receiptActionStr);
                }
            }
            if (lastSavedInstanceState.containsKey(RECEIPT_SAVE_ACTION_KEY)) {
                String receiptSaveActionStr = lastSavedInstanceState.getString(RECEIPT_SAVE_ACTION_KEY);
                if (receiptSaveActionStr != null) {
                    receiptSaveAction = ReceiptPictureSaveAction.valueOf(receiptSaveActionStr);
                }
            }

            if (lastSavedInstanceState.containsKey(SELECTED_REPORT_ENTRY_KEY)) {
                selExpEntKey = lastSavedInstanceState.getString(SELECTED_REPORT_ENTRY_KEY);
                // Has the expense report been set yet?
                if (expRep != null) {
                    if (expRepCache != null) {
                        ExpenseReportEntry expRepEnt = expRepCache.getReportEntry(expRep, selExpEntKey);
                        if (expRepEnt != null) {
                            setSelectedExpenseReportEntry(expRepEnt);
                        } else {
                            Log.w(Const.LOG_TAG, CLS_TAG
                                    + ".initializeState: unable to locate expense report entry in cache!");
                        }
                    } else {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".initializeState: expense report cache not set!");
                    }
                    selExpEntKey = null;
                }
            }

            if (lastSavedInstanceState.containsKey(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY)) {
                // Restore the receipt camera image file path.
                receiptCameraImageDataLocalFilePath = lastSavedInstanceState
                        .getString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY);
            }
            if (lastSavedInstanceState.containsKey(RECEIPT_IMAGE_FILE_PATH_KEY)) {
                // Restore the receipt image file path.
                receiptImageDataLocalFilePath = lastSavedInstanceState.getString(RECEIPT_IMAGE_FILE_PATH_KEY);
            }
            if (lastSavedInstanceState.containsKey(DELETE_RECEIPT_IMAGE_FILE_PATH)) {
                // Restore whether the receipt image file should be punted post save.
                deleteReceiptImageDataLocalFilePath = lastSavedInstanceState.getBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH);
            }
            // Restore comment information.
            if (lastSavedInstanceState.containsKey(SELECTED_COMMENT_AUTHOR_KEY)) {
                selComAuthor = lastSavedInstanceState.getString(SELECTED_COMMENT_AUTHOR_KEY);
            }
            if (lastSavedInstanceState.containsKey(SELECTED_COMMENT_DATE_KEY)) {
                selComDate = lastSavedInstanceState.getString(SELECTED_COMMENT_DATE_KEY);
            }
            if (lastSavedInstanceState.containsKey(SELECTED_COMMENT_BODY_KEY)) {
                selComBody = lastSavedInstanceState.getString(SELECTED_COMMENT_BODY_KEY);
            }
        }

        // Restore any receivers.
        restoreReceivers();
        // Restore any non-configuration data.
        if (retainer != null) {
            // Restore any missing/invalid/copy-down form fields.
            if (retainer.contains(MISSING_INVALID_COPY_DOWN_FIELDS_KEY)) {
                missReqInvalidCopyDownFormFieldValues = (List<FormFieldView>) retainer
                        .get(MISSING_INVALID_COPY_DOWN_FIELDS_KEY);
            }
            // Restore recReqExpList
            if (retainer.contains(REC_REQ_EXP_LIST_KEY)) {
                recReqExpList = (ArrayList<ExpenseReportEntry>) retainer.get(REC_REQ_EXP_LIST_KEY);
            }
        }
    }

    /**
     * Will restore the state of any form fields contained in a form field listener.
     */
    protected void restoreFormFieldState() {

        // Restore any values to the various form field views.
        if (frmFldViewListener != null && lastSavedInstanceState != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onRestoreInstanceState(lastSavedInstanceState);
                }
            }
        }

        // Restore any values to the various tax form field views.
        if (frmFldViewListener != null && lastSavedInstanceState != null) {
            if (frmFldViewListener.getTaxFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getTaxFormFieldViews()) {
                    frmFldView.onRestoreInstanceState(lastSavedInstanceState);
                }
            }
        }

        // Restore any current form field view.
        if (frmFldViewListener != null && lastSavedInstanceState != null
                && lastSavedInstanceState.containsKey(CURRENT_FORM_FIELD_VIEW_KEY)) {
            String curFrmFldViewId = lastSavedInstanceState.getString(CURRENT_FORM_FIELD_VIEW_KEY);
            FormFieldView curFrmFldView = frmFldViewListener.findFormFieldViewById(curFrmFldViewId);
            if (curFrmFldView != null) {
                frmFldViewListener.setCurrentFormFieldView(curFrmFldView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".restoreFormFieldState: lastSavedState contains a value for '"
                        + CURRENT_FORM_FIELD_VIEW_KEY + "' of '" + curFrmFldViewId
                        + "' but unable to locate form field view!");
            }
        }

        // Restore any application context registered receivers.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onApplyNonConfigurationInstance(retainer);
                }
            }
            if (frmFldViewListener.getTaxFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getTaxFormFieldViews()) {
                    frmFldView.onApplyNonConfigurationInstance(retainer);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {

        // Ensure the super classes implementation is called!
        super.onSaveInstanceState(outState);

        // Save out various boolean attributes.
        outState.putBoolean(SAVING_REPORT_RECEIPT_KEY, savingReportReceipt);
        outState.putBoolean(SAVING_EXPENSE_RECEIPT_KEY, savingExpenseReceipt);
        outState.putString(SAVING_RECEIPT_IMAGE_ID_KEY, savingReceiptImageId);
        outState.putBoolean(PROCESSING_BACK_PRESSED_KEY, processingBackPressed);
        outState.putBoolean(PROCESSING_SUBMIT_PRESSED_KEY, processingSubmitPressed);
        outState.putInt(IMAGE_RECEIPT_REQUIRED_KEY, imageOrReceiptRequired);
        if (processingReceiptAction != null) {
            outState.putString(PROCESSING_RECEIPT_ACTION_KEY, processingReceiptAction.name());
        }
        if (receiptSaveAction != null) {
            outState.putString(RECEIPT_SAVE_ACTION_KEY, receiptSaveAction.name());
        }

        // Save the local camera receipt image data file path.
        outState.putString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY, receiptCameraImageDataLocalFilePath);
        // Save the receipt image data local file path.
        outState.putString(RECEIPT_IMAGE_FILE_PATH_KEY, receiptImageDataLocalFilePath);
        // Save whether the receipt image file should be punted post save.
        outState.putBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH, deleteReceiptImageDataLocalFilePath);

        // Save current comment information.
        if (selComAuthor != null) {
            outState.putString(SELECTED_COMMENT_AUTHOR_KEY, selComAuthor);
        }
        if (selComDate != null) {
            outState.putString(SELECTED_COMMENT_DATE_KEY, selComDate);
        }
        if (selComBody != null) {
            outState.putString(SELECTED_COMMENT_BODY_KEY, selComBody);
        }

        // Save out any currently selected report entry.
        ExpenseReportEntry expRepEnt = getSelectedExpenseReportEntry();
        if (expRepEnt != null) {
            outState.putString(SELECTED_REPORT_ENTRY_KEY, expRepEnt.reportEntryKey);
        }

        // Save out any form field view objects.
        if (frmFldViewListener != null && frmFldViewListener.getFormFieldViews() != null) {
            for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                frmFldView.onSaveInstanceState(outState);
            }
        }

        // Save out any tax form field view objects.
        if (frmFldViewListener != null && frmFldViewListener.getTaxFormFieldViews() != null) {
            for (FormFieldView frmFldView : frmFldViewListener.getTaxFormFieldViews()) {
                frmFldView.onSaveInstanceState(outState);
            }
        }

        // Save out any current form field view.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
            outState.putString(CURRENT_FORM_FIELD_VIEW_KEY, frmFldViewListener.getCurrentFormFieldView().getFormField()
                    .getId());
        }
    }

    /**
     * Will register a new instance of <code>ReportApproveReceiver</code> with the application context.
     */
    protected void registerReportApproveReceiver() {
        if (reportApproveReceiver == null) {
            reportApproveReceiver = new ReportApproveReceiver(this);
            if (reportApproveFilter == null) {
                reportApproveFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_APPROVE);
            }
            getApplicationContext().registerReceiver(reportApproveReceiver, reportApproveFilter);
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".registerReportApproveReceiver: reportApproveReceiver is *not* null!");
        }
    }

    /**
     * Will unregister the current instance of <code>ReportApproveReceiver</code> with the application context and set the
     * reference to <code>null</code>.
     */
    protected void unregisterReportApproveReceiver() {
        if (reportApproveReceiver != null) {
            getApplicationContext().unregisterReceiver(reportApproveReceiver);
            reportApproveReceiver = null;
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".unregisterReportApproveReceiver: reportApproveReceiver is null!");
        }
    }

    /**
     * Will register a new instance of <code>ReportSubmitReceiver</code> with the application context.
     */
    protected void registerReportSubmitReceiver() {
        if (reportSubmitReceiver == null) {
            reportSubmitReceiver = new ReportSubmitReceiver(this);
            if (reportSubmitFilter == null) {
                reportSubmitFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_SUBMIT_UPDATE);
            }
            getApplicationContext().registerReceiver(reportSubmitReceiver, reportSubmitFilter);
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".registerReportSubmitReceiver: reportSubmitReceiver is *not* null!");
        }
    }

    /**
     * Will unregister the current instance of <code>ReportSubmitReceiver</code> with the application context and set the
     * reference to <code>null</code>.
     */
    protected void unregisterReportSubmitReceiver() {
        if (reportSubmitReceiver != null) {
            getApplicationContext().unregisterReceiver(reportSubmitReceiver);
            reportSubmitReceiver = null;
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".unregisterReportSubmitReceiver: reportSubmitReceiver is null!");
        }
    }

    /**
     * Determines whether or not there is an outstanding report detail request.
     * 
     * @return whether there is an outstanding report detail request.
     */
    protected boolean isReportDetailRequestOutstanding() {
        return (reportDetailRequest != null);
    }

    /**
     * Will register a new instance of <code>ReportDetailReceiver</code> with the application context.
     */
    protected void registerReportDetailReceiver() {
        if (reportDetailReceiver == null) {
            reportDetailReceiver = new ReportDetailReceiver(this);
            getApplicationContext().registerReceiver(reportDetailReceiver, getBroadcastReceiverIntentFilter());
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".registerReportDetailReceiver: reportDetailReceiver is *not* null!");
        }
    }

    /**
     * Will unregister the current instance of <code>ReportDetailReceiver</code> with the application context and set the
     * reference to <code>null</code>.
     */
    protected void unregisterReportDetailReceiver() {
        if (reportDetailReceiver != null) {
            getApplicationContext().unregisterReceiver(reportDetailReceiver);
            reportDetailReceiver = null;
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".unregisterReportDetailReceiver: reportDetailReceiver is null!");
        }
    }

    /**
     * Will send a request to obtain an updated report header detail.
     */
    protected void sendReportHeaderDetailRequest() {
        if (ConcurCore.isConnected()) {
            ConcurCore ConcurCore = (ConcurCore) getApplication();
            ConcurService concurService = ConcurCore.getService();
            // Register the network activity receiver.
            registerNetworkActivityReceiver();
            // Register the report header detail receiver.
            registerReportHeaderDetailReceiver();
            reportHeaderDetailRequest = concurService.sendReportHeaderDetailRequest(expRep.reportKey, reportKeySource);
            if (reportHeaderDetailRequest != null) {
                showDialog(Const.DIALOG_EXPENSE_REFRESH_EXPENSES);
                // Set the request on the receiver.
                reportHeaderDetailReceiver.setRequest(reportHeaderDetailRequest);
            } else {
                // Unregister the network activity receiver.
                unregisterNetworkActivityReceiver();
                // unregister the report header detail receiver.
                unregisterReportDetailReceiver();
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create report header detail request.");
            }
        } else {
            Log.i(Const.LOG_TAG, CLS_TAG + ".sendReportHeaderDetailRequest: client is offline!");
        }
    }

    /**
     * Will create and register with the application context an instance of 'ReportHeaderDetailReceiver' and update the
     * 'reportHeaderDetailReceiver' attribute.
     */
    protected void registerReportHeaderDetailReceiver() {
        if (reportHeaderDetailReceiver == null) {
            reportHeaderDetailReceiver = new ReportHeaderDetailReceiver(this);
            if (reportHeaderDetailFilter == null) {
                reportHeaderDetailFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_HEADER_DETAIL_UPDATED);
            }
            getApplicationContext().registerReceiver(reportHeaderDetailReceiver, reportHeaderDetailFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerReportHeaderDetailReceiver: reportHeaderDetailReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'ReportHeaderDetailReceiver' and set the
     * 'reportHeaderDetailReceiver' attribute to 'null'.
     */
    protected void unregisterReportHeaderDetailReceiver() {
        if (reportHeaderDetailReceiver != null) {
            getApplicationContext().unregisterReceiver(reportHeaderDetailReceiver);
            reportHeaderDetailReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReportHeaderDetailReceiver: reportHeaderDetailReceiver is null!");
        }
    }

    /**
     * Will create and register with the application context an instance of 'SaveReceiptReceiver' and update the
     * 'saveReceiptReceiver' attribute.
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
     * Will unregister with the application context the current instance of 'SaveReceiptReceiver' and set the
     * 'saveReceiptReceiver' attribute to 'null'.
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
     * Will send a request to append a receipt to another receipt based on receipt image ID's.
     * 
     * @param fromReceiptImageId
     *            contains the receipt image ID of the receipt being appended.
     * @param toReceiptImageId
     *            contains the receipt image ID of the receipt being appended to.
     */
    protected void sendAppendReportEntryReceiptRequest(String fromReceiptImageId, String toReceiptImageId) {
        ConcurService concurService = getConcurService();
        registerAppendReportEntryReceiptReceiver();
        appendReportEntryReceiptRequest = concurService.sendAppendReceiptImageRequest(getUserId(), fromReceiptImageId,
                toReceiptImageId);
        if (appendReportEntryReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".sendAppendReportEntryReceiptRequest: unable to create 'AppendReceiptImage' request!");
            unregisterAppendReportEntryReceiptReceiver();
        } else {
            appendReportEntryReceiptReceiver.setServiceRequest(appendReportEntryReceiptRequest);
        }
    }

    /**
     * Will create and register with the application context an instance of <code>AppendReportEntryReceiptReceiver</code> and set
     * it on <code>appendReportEntryReceiptReceiver</code> attribute.
     */
    protected void registerAppendReportEntryReceiptReceiver() {
        if (appendReportEntryReceiptReceiver == null) {
            appendReportEntryReceiptReceiver = new AppendReportEntryReceiptReceiver(this);
            if (appendReportEntryReceiptFilter == null) {
                appendReportEntryReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_APPENDED);
            }
            getApplicationContext().registerReceiver(appendReportEntryReceiptReceiver, appendReportEntryReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerAppendReportEntryReceiver: appendReportEntryReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of <code>AppendReportEntryReceiptReceiver</code> and set
     * the <code>appendReportEntryReceiptReceiver</code> attribute to 'null'.
     */
    protected void unregisterAppendReportEntryReceiptReceiver() {
        if (appendReportEntryReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(appendReportEntryReceiptReceiver);
            appendReportEntryReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterAppendReportEntryReceiptReceiver: appendReportEntryReceiptReceiver is null!");
        }
    }

    /**
     * Will send a request to save a report entry receipt to the currently selected report entry. The currently selected report
     * entry is obtained via a call to <code>getSelectedExpenseReportEntry</code>.
     * 
     * @param receiptImageId
     *            contains the receipt image ID of the receipt to associate with the report entry.
     */
    protected void sendSaveReportEntryReceiptRequest(String receiptImageId) {
        if (receiptImageId != null && receiptImageId.length() > 0) {
            ConcurService concurService = getConcurService();
            registerSaveReportEntryReceiptReceiver();
            saveReportEntryReceiptRequest = concurService.sendSaveReportEntryReceiptRequest(getUserId(), expRep,
                    getSelectedExpenseReportEntry(), receiptImageId);
            if (saveReportEntryReceiptRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendSaveReportEntryReceiptRequest: unable to create 'SaveReportEntryReceipt' request!");
                unregisterSaveReportEntryReceiptReceiver();
            } else {
                saveReportEntryReceiptReceiver.setRequest(saveReportEntryReceiptRequest);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendSaveReportEntryReceiptRequest: receiptImageId is null or empty");
        }
    }

    /**
     * Will create and register with the application context an instance of 'SaveReportEntryReceiptReceiver' and update the
     * 'saveReportEntryReceiptReceiver' attribute.
     */
    protected void registerSaveReportEntryReceiptReceiver() {
        if (saveReportEntryReceiptReceiver == null) {
            saveReportEntryReceiptReceiver = new SaveReportEntryReceiptReceiver(this);
            if (saveReportEntryReceiptFilter == null) {
                saveReportEntryReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_SAVE);
            }
            getApplicationContext().registerReceiver(saveReportEntryReceiptReceiver, saveReportEntryReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerSaveReportEntryReceiver: saveReportEntryReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'SaveReportEntryReceiptReceiver' and set the
     * 'saveReportEntryReceiptReceiver' attribute to 'null'.
     */
    protected void unregisterSaveReportEntryReceiptReceiver() {
        if (saveReportEntryReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(saveReportEntryReceiptReceiver);
            saveReportEntryReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterSaveReportEntryReceiptReceiver: saveReportEntryReceiptReceiver is null!");
        }
    }

    /**
     * Will send a request to clear a report entry receipt for the currently selected report entry. The currently selected report
     * entry is obtained via a call to <code>getSelectedExpenseReportEntry</code>.
     */
    protected void sendClearReportEntryReceiptRequest() {
        ConcurService concurService = getConcurService();
        registerClearReportEntryReceiptReceiver();
        clearReportEntryReceiptRequest = concurService.sendClearReportEntryReceiptRequest(getUserId(), expRep,
                getSelectedExpenseReportEntry());
        if (clearReportEntryReceiptRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".clearSaveReportEntryReceiptRequest: unable to create 'ClearReportEntryReceipt' request!");
            unregisterClearReportEntryReceiptReceiver();
        } else {
            clearReportEntryReceiptReceiver.setServiceRequest(clearReportEntryReceiptRequest);
        }
    }

    /**
     * Will create and register with the application context an instance of 'ClearReportEntryReceiptReceiver' and update the
     * 'clearReportEntryReceiptReceiver' attribute.
     */
    protected void registerClearReportEntryReceiptReceiver() {
        if (clearReportEntryReceiptReceiver == null) {
            clearReportEntryReceiptReceiver = new ClearReportEntryReceiptReceiver(this);
            if (clearReportEntryReceiptFilter == null) {
                clearReportEntryReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_REPORT_ENTRY_RECEIPT_CLEAR);
            }
            getApplicationContext().registerReceiver(clearReportEntryReceiptReceiver, clearReportEntryReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerClearReportEntryReceiver: clearReportEntryReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'ClearReportEntryReceiptReceiver' and set the
     * 'clearReportEntryReceiptReceiver' attribute to 'null'.
     */
    protected void unregisterClearReportEntryReceiptReceiver() {
        if (clearReportEntryReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(clearReportEntryReceiptReceiver);
            clearReportEntryReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".unregisterClearReportEntryReceiptReceiver: clearReportEntryReceiptReceiver is null!");
        }
    }

    protected void sendConnectPostImageReportRequest(String filePath) {
        ConcurService concurService = getConcurService();
        registerSaveReceiptReceiver();
        saveReceiptRequest = concurService.sendConnectPostImageReportRequest(getUserId(), filePath,
                deleteReceiptImageDataLocalFilePath, null, expRep.reportKey, false);
        if (saveReceiptRequest != null) {
            saveReceiptReceiver.setRequest(saveReceiptRequest);
            showDialog(getSavingReceiptDialogId());
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".sendConnectPostImageReportRequest: unable to create 'SaveReceiptRequest'!");
            unregisterSaveReceiptReceiver();
        }

    }

    /**
     * Will send a request to save a receipt to a report.
     * 
     * @param filePath
     *            contains the receipt image file path of the receipt to be added to a report.
     */
    protected void sendAddReportReceiptV2Request(String filePath) {
        if (filePath != null && filePath.length() > 0) {
            ConcurService concurService = getConcurService();
            registerAddReportReceiptV2Receiver();
            addReportReceiptV2Request = concurService.sendAddReportReceiptV2Request(getUserId(), expRep.reportKey,
                    filePath, null, true);
            if (addReportReceiptV2Request == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendAddReportReceiptV2Request: unable to create 'sendAddReportReceipt' request!");
                unregisterAddReportReceiptV2Receiver();
            } else {
                addReportReceiptV2Receiver.setRequest(addReportReceiptV2Request);
                showDialog(getSavingReceiptDialogId());
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendAddReportReceiptV2Request: img filePath is null or empty");
        }
    }

    /**
     * Will create and register with the application context an instance of 'AddReportReceiptReceiver' and update the
     * 'addReportReceiptReceiver' attribute.
     */
    protected void registerAddReportReceiptV2Receiver() {
        if (addReportReceiptV2Receiver == null) {
            addReportReceiptV2Receiver = new AddReportReceiptV2Receiver(this);
            if (addReportReceiptV2Filter == null) {
                addReportReceiptV2Filter = new IntentFilter(Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT);
            }
            getApplicationContext().registerReceiver(addReportReceiptV2Receiver, addReportReceiptV2Filter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".registerAddReportReceiptV2Receiver: addReportReceiptV2Receiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'AddReportReceiptReceiver' and set the
     * 'addReportReceiptReceiver' attribute to 'null'.
     */
    protected void unregisterAddReportReceiptV2Receiver() {
        if (addReportReceiptV2Receiver != null) {
            getApplicationContext().unregisterReceiver(addReportReceiptV2Receiver);
            addReportReceiptV2Receiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAddReportReceiptV2Receiver: addReportReceiptV2Receiver is null!");
        }
    }

    /**
     * Will send a request to save a receipt to a report.
     * 
     * @param receiptImageId
     *            contains the receipt image ID of the receipt to be added to a report.
     */
    protected void sendAddReportReceiptRequest(String receiptImageId) {
        if (receiptImageId != null && receiptImageId.length() > 0) {
            ConcurService concurService = getConcurService();
            registerAddReportReceiptReceiver();
            addReportReceiptRequest = concurService.sendAddReportReceiptRequest(getUserId(), expRep.reportKey,
                    receiptImageId);
            if (addReportReceiptRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendAddReportReceiptRequest: unable to create 'sendAddReportReceipt' request!");
                unregisterAddReportReceiptReceiver();
            } else {
                addReportReceiptReceiver.setRequest(addReportReceiptRequest);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendAddReportReceiptRequest: receiptImageId is null or empty ");
        }
    }

    /**
     * Will create and register with the application context an instance of 'AddReportReceiptReceiver' and update the
     * 'addReportReceiptReceiver' attribute.
     */
    protected void registerAddReportReceiptReceiver() {
        if (addReportReceiptReceiver == null) {
            addReportReceiptReceiver = new AddReportReceiptReceiver(this);
            if (addReportReceiptFilter == null) {
                addReportReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_ADD_REPORT_RECEIPT);
            }
            getApplicationContext().registerReceiver(addReportReceiptReceiver, addReportReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerAddReportReceiptReceiver: addReportReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'AddReportReceiptReceiver' and set the
     * 'addReportReceiptReceiver' attribute to 'null'.
     */
    protected void unregisterAddReportReceiptReceiver() {
        if (addReportReceiptReceiver != null) {
            getApplicationContext().unregisterReceiver(addReportReceiptReceiver);
            addReportReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAddReportReceiptReceiver: addReportReceiptReceiver is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();

        // If view building was delayed, then build it now.
        if (buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: build view was delayed, constructing view now.");
            Intent intent = getIntent();
            setExpenseReport(intent);
            buildViewDelay = false;
        }

        // If there was a delay to handling an activity result, then process it now.
        if (activityResultDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: activity result was delayed, handling result now.");
            onActivityResult(activityResultRequestCode, activityResultResultCode, activityResultData);
            activityResultDelay = false;
            activityResultData = null;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {

        super.onPause();
        unregisterReceivers();

        // Check for 'SaveReceiptReceiver'.
        if (saveReceiptReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate' method.
            saveReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_RECEIPT_RECEIVER_KEY, saveReceiptReceiver);
        }
        // Check for 'SaveReportEntryReceiptReceiver'.
        if (saveReportEntryReceiptReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            saveReportEntryReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_REPORT_ENTRY_RECEIPT_RECEIVER_KEY, saveReportEntryReceiptReceiver);
        }
        // Check for 'AppendReportEntryReceiptReceiver'.
        if (appendReportEntryReceiptReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            appendReportEntryReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(APPEND_REPORT_ENTRY_RECEIPT_RECEIVER_KEY, appendReportEntryReceiptReceiver);
        }
        // Check for 'ClearReportEntryReceiptReceiver'.
        if (clearReportEntryReceiptReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            clearReportEntryReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(CLEAR_REPORT_ENTRY_RECEIPT_RECEIVER_KEY, clearReportEntryReceiptReceiver);
        }
        // Check for 'AddReportReceiptReceiver'.
        if (addReportReceiptReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            addReportReceiptReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(ADD_REPORT_RECEIPT_RECEIVER_KEY, addReportReceiptReceiver);
        }
        // Check for 'AddReportReceiptReceiver'.
        if (addReportReceiptV2Receiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            addReportReceiptV2Receiver.setActivity(null);
            // Add it to the retainer
            retainer.put(ADD_REPORT_RECEIPT_V2_RECEIVER_KEY, addReportReceiptV2Receiver);
        }
        // Check for 'ReportApproveReceiver'.
        if (reportApproveReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            reportApproveReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(REPORT_APPROVE_RECEIVER_KEY, reportApproveReceiver);
        }
        // Check For 'ReportDetailDataUpdateReceiver'.
        if (reportDetailReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            reportDetailReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(REPORT_DETAIL_UPDATE_RECEIVER_KEY, reportDetailReceiver);
        }
        // Check for 'ReportSubmitReceiver'.
        if (reportSubmitReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            reportSubmitReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(REPORT_SUBMIT_RECEIVER_KEY, reportSubmitReceiver);
        }
        // Check for 'ReportHeaderDetailReceiver'.
        if (reportHeaderDetailReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            reportHeaderDetailReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(REPORT_HEADER_DETAIL_RECEIVER_KEY, reportHeaderDetailReceiver);
        }

        // Store any form field view non-configuration data.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.onRetainNonConfigurationInstance(retainer);
                }
            }
        }

        // Store any tax form field view non-configuration data.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getTaxFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getTaxFormFieldViews()) {
                    frmFldView.onRetainNonConfigurationInstance(retainer);
                }
            }
        }

        // Store any list of missing/invalid/copy-down form fields.
        if (missReqInvalidCopyDownFormFieldValues != null) {
            // Add it to the retainer
            retainer.put(MISSING_INVALID_COPY_DOWN_FIELDS_KEY, missReqInvalidCopyDownFormFieldValues);
        }

        // Store list of expense entries requiring receipts
        if (recReqExpList != null) {
            retainer.put(REC_REQ_EXP_LIST_KEY, recReqExpList);
        }

        // Check for 'SAVE_TAX_FORM_RECEIVER'.
        if (taxFormReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            taxFormReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_TAX_FORM_RECEIVER, taxFormReceiver);
        }

        // Check for 'SAVE_CONDITIONAL_FIELD_ACTION_RECEIVER'
        if (conditionalFieldActionReceiver != null) {
            // Clear the activity reference, it will be reset in the 'onCreate' method.
            conditionalFieldActionReceiver.setActivity(null);
            // Add it to the retainer
            retainer.put(SAVE_CONDITIONAL_FIELD_ACTION_RECEIVER, conditionalFieldActionReceiver);
        }

        // MOB-19669 - Save any conditional field values so we don't invoke the MWS every time.
        if (lastConditionalFieldEntries != null && !lastConditionalFieldEntries.isEmpty()) {
            retainer.put(SAVE_CONDITIONAL_FIELD_LAST_PAIRS, lastConditionalFieldEntries);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Restore any non-configuration data.
        if (retainer != null) {
            // Restore 'AddReportReceiptReceiver'.
            if (retainer.contains(ADD_REPORT_RECEIPT_RECEIVER_KEY)) {
                addReportReceiptReceiver = (AddReportReceiptReceiver) retainer.get(ADD_REPORT_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                addReportReceiptReceiver.setActivity(this);
            }
            // Restore 'AddReportReceiptV2Receiver'.
            if (retainer.contains(ADD_REPORT_RECEIPT_V2_RECEIVER_KEY)) {
                addReportReceiptV2Receiver = (AddReportReceiptV2Receiver) retainer
                        .get(ADD_REPORT_RECEIPT_V2_RECEIVER_KEY);
                // Reset the activity reference.
                addReportReceiptV2Receiver.setActivity(this);
            }
            // Restore 'ReportApproveReceiver'.
            if (retainer.contains(REPORT_APPROVE_RECEIVER_KEY)) {
                reportApproveReceiver = (ReportApproveReceiver) retainer.get(REPORT_APPROVE_RECEIVER_KEY);
                // Reset the activity reference.
                reportApproveReceiver.setActivity(this);
            }
            // Restore the 'ReportDetailDataUpdateReceiver'.
            if (retainer.contains(REPORT_DETAIL_UPDATE_RECEIVER_KEY)) {
                reportDetailReceiver = (ReportDetailReceiver) retainer.get(REPORT_DETAIL_UPDATE_RECEIVER_KEY);
                // Reset the activity reference.
                reportDetailReceiver.setActivity(this);
            }
            // Restore 'ReportSubmitReceiver'.
            if (retainer.contains(REPORT_SUBMIT_RECEIVER_KEY)) {
                reportSubmitReceiver = (ReportSubmitReceiver) retainer.get(REPORT_SUBMIT_RECEIVER_KEY);
                // Reset the activity reference.
                reportSubmitReceiver.setActivity(this);
            }
            // Restore 'SaveReceiptReceiver'.
            if (retainer.contains(SAVE_RECEIPT_RECEIVER_KEY)) {
                saveReceiptReceiver = (SaveReceiptReceiver) retainer.get(SAVE_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                saveReceiptReceiver.setActivity(this);
            }
            // Restore the 'SaveReportEntryReceiptReceiver'.
            if (retainer.contains(SAVE_REPORT_ENTRY_RECEIPT_RECEIVER_KEY)) {
                saveReportEntryReceiptReceiver = (SaveReportEntryReceiptReceiver) retainer
                        .get(SAVE_REPORT_ENTRY_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                saveReportEntryReceiptReceiver.setActivity(this);
            }
            // Restore the 'AppendReportEntryReceiptReceiver'.
            if (retainer.contains(APPEND_REPORT_ENTRY_RECEIPT_RECEIVER_KEY)) {
                appendReportEntryReceiptReceiver = (AppendReportEntryReceiptReceiver) retainer
                        .get(APPEND_REPORT_ENTRY_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                appendReportEntryReceiptReceiver.setActivity(this);
            }
            // Restore the 'ClearReportEntryReceiptReceiver'.
            if (retainer.contains(CLEAR_REPORT_ENTRY_RECEIPT_RECEIVER_KEY)) {
                clearReportEntryReceiptReceiver = (ClearReportEntryReceiptReceiver) retainer
                        .get(CLEAR_REPORT_ENTRY_RECEIPT_RECEIVER_KEY);
                // Reset the activity reference.
                clearReportEntryReceiptReceiver.setActivity(this);
            }
            // Restore 'ReportHeaderDetailReceiver'.
            if (retainer.contains(REPORT_HEADER_DETAIL_RECEIVER_KEY)) {
                reportHeaderDetailReceiver = (ReportHeaderDetailReceiver) retainer
                        .get(REPORT_HEADER_DETAIL_RECEIVER_KEY);
                // Reset the activity reference.
                reportHeaderDetailReceiver.setActivity(this);
            }

            // Restore the 'taxFormreceiver'.
            if (retainer.contains(SAVE_TAX_FORM_RECEIVER)) {
                taxFormReceiver = (TaxFormReceiver) retainer.get(SAVE_TAX_FORM_RECEIVER);
                // Reset the activity reference.
                taxFormReceiver.setActivity(this);
            }

            // Restore 'ConditionalActionReceiver'
            if (retainer.contains(SAVE_CONDITIONAL_FIELD_ACTION_RECEIVER)) {
                conditionalFieldActionReceiver = (ConditionalActionReceiver) retainer
                        .get(SAVE_CONDITIONAL_FIELD_ACTION_RECEIVER);
                // Reset the activity reference
                conditionalFieldActionReceiver.setActivity(this);
            }

            // Restore Conditional field values.
            if (retainer.contains(SAVE_CONDITIONAL_FIELD_LAST_PAIRS)) {
                lastConditionalFieldEntries = (Map<String, String>) retainer.get(SAVE_CONDITIONAL_FIELD_LAST_PAIRS);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }

    /**
     * Will unregister the various broadcast receivers.
     */
    protected void unregisterReceivers() {
        if (networkActivityReceiver != null) {
            unregisterReceiver(networkActivityReceiver);
            networkActivityReceiver = null;
        }
    }

    /**
     * Will broadcast a message that the application is accessing the network.
     */
    protected void broadcastStartNetworkActivity(int actType, String actText) {
        Intent i = new Intent(Const.ACTION_NETWORK_ACTIVITY_START);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, actType);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TEXT, actText);
        ((ConcurCore) getApplication()).getService().sendBroadcast(i);
    }

    /**
     * Will broadcast a message that the application is no longer accessing the network.
     */
    protected void broadcastStopNetworkActivity(int actType) {
        Intent i = new Intent(Const.ACTION_NETWORK_ACTIVITY_STOP);
        i.putExtra(Const.ACTION_NETWORK_ACTIVITY_TYPE, actType);
        ((ConcurCore) getApplication()).getService().sendBroadcast(i);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;

        // Check whether there a form field view should handle the dialog creation.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            dlg = frmFldViewListener.getCurrentFormFieldView().onCreateDialog(id);
        } else {
            dlg = dialogs.get(id);
            if (dlg == null) {
                switch (id) {
                case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_expense_save_report_entry_receipt));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (saveReportEntryReceiptRequest != null) {
                                // Cancel the request, this will result in the 'SaveReportEntryReceiptReceiver.onReceiver' being
                                // invoked and handled as a cancellation.
                                saveReportEntryReceiptRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onCreateDialog: saveReportEntryReceiptRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_CLEAR_RECEIPT_PROGRESS: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_clear_report_entry_receipt_progress_message));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (clearReportEntryReceiptRequest != null) {
                                clearReportEntryReceiptRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onCreateDialog: clearReportEntryReceiptRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_SAVE_REPORT_RECEIPT: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_expense_save_report_receipt));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (addReportReceiptRequest != null) {
                                // Cancel the request, this will result in the 'AddReportReceiptReceiver.onReceiver' being
                                // invoked and handled as a cancellation.
                                addReportReceiptRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: addReportReceiptRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_APPROVE_REPORT_PROGRESS: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_report_approve_progress));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (reportApproveRequest != null) {
                                // Cancel the request, this will result in the 'ReportApproveReceiver.onReceiver' being
                                // invoked and handled as a cancellation.
                                reportApproveRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportApproveRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_SELECT_WORKFLOW_ACTION: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_select_approver_action_title);

                    // Construct a simple array adapter used by the end-user.
                    final ArrayAdapter<WorkflowAction> actionAdapter = new ArrayAdapter<WorkflowAction>(this,
                            R.layout.workflow_action, R.id.action_name, expRep.workflowActions);

                    builder.setSingleChoiceItems(actionAdapter, -1, new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dismissDialog(Const.DIALOG_EXPENSE_SELECT_WORKFLOW_ACTION);
                            if (which != -1) {
                                // Set the workflow action stat key.
                                WorkflowAction action = actionAdapter.getItem(which);
                                reportApproveStatKey = action.statKey;
                                // Prompt the end-user to confirm the approval.
                                showDialog(Const.DIALOG_EXPENSE_APPROVE_REPORT);
                            }
                        }
                    });
                    AlertDialog alertDlg = builder.create();
                    dlg = alertDlg;
                    dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            removeDialog(Const.DIALOG_EXPENSE_SELECT_WORKFLOW_ACTION);
                        }
                    });
                    break;
                }
                case Const.DIALOG_EXPENSE_SUBMITTING_REPORT: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_submitting_report));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (reportSubmitRequest != null) {
                                // Cancel the request, this will result in the 'ReportSubmitReceiver.onReceiver' being
                                // invoked and handled as a cancellation.
                                reportSubmitRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: reportSubmitRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_APPROVE_REPORT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String approveReportTitle = getText(R.string.please_confirm).toString();
                    String approveReportMessage = null;
                    ConcurCore concurCore = getConcurCore();
                    UserConfig userConfig = concurCore.getUserConfig();
                    if (userConfig != null) {
                        ExpenseConfirmation expConf = userConfig.getApproveConfirmation(expRep.polKey);
                        if (expConf != null) {
                            if (expConf.title != null && expConf.title.length() > 0) {
                                approveReportTitle = expConf.title;
                            }
                            if (expConf.text != null && expConf.text.length() > 0) {
                                approveReportMessage = expConf.text;
                            }
                        }
                    }
                    builder.setTitle(Html.fromHtml(approveReportTitle));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            handleApproveReport();
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            // Clear the stat key.
                            reportApproveStatKey = null;
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_approve_confirmation, null);
                    // Replace the default message.
                    if (approveReportMessage != null) {
                        TextView txtView = (TextView) view.findViewById(R.id.report_approve_message);
                        if (txtView != null) {
                            txtView.setText(Html.fromHtml(approveReportMessage));
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onCreateDialog: unable to locate 'report_approve_message' text view!");
                        }
                    }
                    builder.setView(view);
                    dlg = builder.create();
                    dlg.setOnDismissListener(new OnDismissListener() {

                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            // Clear the stat key.
                            reportApproveStatKey = null;
                        }
                    });
                    break;
                }
                case Const.DIALOG_EXPENSE_SUBMIT_REPORT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.report_submitted_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            // Set the flag that the active reports list should be refreshed.
                            ConcurCore concurCore = getConcurCore();
                            IExpenseReportCache actRepCache = concurCore.getExpenseActiveCache();
                            actRepCache.setShouldFetchReportList();
                            // Launch the 'ExpenseActiveReports' activity with the flag
                            // 'Intent.FLAG_ACTIVITY_CLEAR_TOP' which will unwind the activity
                            // stack and ensure 'ExpenseActiveReports' is on top of the stack.
                            Intent intent = new Intent(AbstractExpenseActivity.this, ExpenseActiveReports.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            intent.putExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, true);
                            startActivity(intent);
                            finish();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    dialogView = inflater.inflate(R.layout.submit_report_dialog, null);
                    builder.setView(dialogView);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_ORIGINAL_RECEIPT_REQUIRED:
                case Const.DIALOG_EXPENSE_MISSING_RECEIPT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String submitReportTitle = getText(R.string.report_submit_warning).toString();
                    String submitReportMessage = null;
                    ConcurCore concurCore = getConcurCore();
                    UserConfig userConfig = concurCore.getUserConfig();
                    if (userConfig != null) {
                        ExpenseConfirmation expConf = userConfig.getSubmitConfirmation(expRep.polKey);
                        if (expConf != null) {
                            if (expConf.title != null && expConf.title.length() > 0) {
                                submitReportTitle = expConf.title;
                            }
                            if (expConf.text != null && expConf.text.length() > 0) {
                                submitReportMessage = expConf.text;
                            }
                        }
                    }
                    builder.setTitle(Html.fromHtml(submitReportTitle));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.proceed), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss this dialog, and proceed to confirm submit.
                            dialog.dismiss();
                            handleSubmitReport(null);
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_submit_missing_receipt, null);
                    // Replace the default message.
                    if (submitReportMessage != null) {
                        TextView txtView = (TextView) view.findViewById(R.id.missing_receipt_title);
                        if (txtView != null) {
                            txtView.setText(Html.fromHtml(submitReportMessage));
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onCreateDialog: unable to locate 'missing_receipt_title' text view!");
                        }
                    }
                    // If ReceiptRequired flag, we want to display messages comparable to the web messages in place of default.
                    if (imageOrReceiptRequired == REC_REQ) {
                        TextView txtView = (TextView) view.findViewById(R.id.provide_receipt);
                        if (txtView != null) {
                            txtView.setText(getResources().getQuantityString(R.plurals.report_submit_receipt_required,
                                    recReqExpList.size()));
                        }
                        txtView = (TextView) view.findViewById(R.id.fax_or_attach);
                        if (txtView != null) {
                            txtView.setText(R.string.report_submit_original_receipt_required);
                        }
                    } else {
                        TextView txtView = (TextView) view.findViewById(R.id.provide_receipt);
                        if (txtView != null) {
                            txtView.setText(getResources().getQuantityString(R.plurals.provide_receipt_reminder_line,
                                    recReqExpList.size()));
                        }
                        txtView = (TextView) view.findViewById(R.id.fax_or_attach);
                        if (txtView != null) {
                            txtView.setText(getResources().getQuantityString(R.plurals.fax_or_attach_line,
                                    recReqExpList.size()));
                        }
                    }
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_SUBMIT_REPORT_CONFIRM: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    String submitReportTitle = getText(R.string.report_submit_warning).toString();
                    String submitReportMessage = null;
                    ConcurCore concurCore = getConcurCore();
                    UserConfig userConfig = concurCore.getUserConfig();
                    if (userConfig != null) {
                        ExpenseConfirmation expConf = userConfig.getSubmitConfirmation(expRep.polKey);
                        if (expConf != null) {
                            if (expConf.title != null && expConf.title.length() > 0) {
                                submitReportTitle = expConf.title;
                            }
                            if (expConf.text != null && expConf.text.length() > 0) {
                                submitReportMessage = expConf.text;
                            }
                        }
                    }
                    builder.setTitle(Html.fromHtml(submitReportTitle));
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss this dialog, and proceed to confirm submit.
                            dialog.dismiss();
                            handleSubmitReport(null);
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_submit_confirmation, null);
                    // Replace the default message.
                    if (submitReportMessage != null) {
                        TextView txtView = (TextView) view.findViewById(R.id.missing_receipt_title);
                        if (txtView != null) {
                            txtView.setText(Html.fromHtml(submitReportMessage));
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onCreateDialog: unable to locate 'missing_receipt_title' text view!");
                        }
                    }
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_SUBMIT_REPORT_FAILED: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.report_submit_fail_dialog_message);
                    builder.setCancelable(true);
                    builder.setMessage(R.string.report_submit_fail_dialog_message);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Dismiss this dialog
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_REPORT_NO_ENTRIES: {
                    AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
                    dlgBldr.setTitle(R.string.dlg_expense_report_no_entries_title);
                    dlgBldr.setMessage(getText(R.string.dlg_expense_report_no_entries_message));
                    dlgBldr.setPositiveButton(getText(R.string.view_expenses), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            // Launch the 'AllExpense' activity with the flag
                            // 'Intent.FLAG_ACTIVITY_CLEAR_TOP' which will unwind the activity
                            // stack and ensure 'AllExpense' is on top of the stack.
                            Intent intent = new Intent(AbstractExpenseActivity.this, ExpensesAndReceipts.class);
                            intent.putExtra(Flurry.PARAM_NAME_CAME_FROM, Flurry.PARAM_VALUE_REPORT_HEADER);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                    });
                    dlgBldr.setNeutralButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    return dlgBldr.create();
                }
                case Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getText(R.string.dlg_append_receipt_confirmation_title));
                    builder.setMessage(getText(R.string.dlg_append_receipt_confirmation_message));
                    builder.setPositiveButton(getText(R.string.general_append), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and start the save.
                            dialog.dismiss();
                            sendAppendReportEntryReceiptRequest(savingReceiptImageId,
                                    getSelectedExpenseReportEntry().receiptImageId);
                            // Show the saving receipt dialog id.
                            showDialog(getSavingReceiptDialogId());
                        }
                    });
                    // MOB-12750
                    // If the report is submitted and of Hold for Receipt Image status, then we cannot replace receipts in the
                    // expense.
                    if (!(expRep.isHoldForReceiptImageStatus())) {
                        builder.setNegativeButton(getText(R.string.general_replace), new OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                sendSaveReportEntryReceiptRequest(savingReceiptImageId);
                                // Show the saving receipt dialog id.
                                showDialog(getSavingReceiptDialogId());
                            }
                        });
                    }
                    dlg = builder.create();
                    break;
                }

                case Const.DIALOG_EXPENSE_RECEIPT_APPEND_FAIL: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(getText(R.string.general_error));
                    builder.setMessage(getText(R.string.dlg_append_receipt_fail_title));
                    builder.setPositiveButton(getText(R.string.okay), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and start the save.
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }

                case DIALOG_RECEIPT_IMAGE: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getText(R.string.expense_receipt_options));
                    receiptActionAdapter = new ReceiptImageOptionListAdapter();
                    builder.setSingleChoiceItems(receiptActionAdapter, -1, new ReceiptImageDialogListener());
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_CONFIRM_CLEAR_RECEIPT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getText(R.string.dlg_clear_receipt_confirmation_title));
                    builder.setMessage(getText(R.string.dlg_clear_receipt_confirmation_message));
                    builder.setPositiveButton(getText(R.string.okay), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and start the clear.
                            dialog.dismiss();
                            sendClearReportEntryReceiptRequest();
                            // Show the clearing receipt dialog id.
                            showDialog(Const.DIALOG_EXPENSE_CLEAR_RECEIPT_PROGRESS);
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_VIEW_COMMENT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_expense_view_comment_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.dialog_comment_body, null);
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getText(R.string.confirm));
                    builder.setMessage(getText(R.string.confirm_save_report_message));
                    builder.setPositiveButton(getText(R.string.general_ok), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and start the save.
                            dialog.dismiss();
                            save();
                        }
                    });
                    builder.setNeutralButton(getText(R.string.general_cancel), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and clear the 'processingBackPressed' and
                            // 'processingSubmitPressed' flag.
                            dialog.dismiss();
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                        }
                    });
                    builder.setNegativeButton(getText(R.string.general_no), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog, clear the 'processingBackPressed' flag and finish the activity.
                            dialog.dismiss();
                            if (processingBackPressed) {
                                // End-user hit the hardware back button which triggered this dialog.
                                processingBackPressed = false;
                                finish();
                            } else if (processingSubmitPressed) {
                                // End-user hit the submit button which triggered this dialog.
                                processingSubmitPressed = false;
                                // Start the report submit process.
                                startSubmitReportConfirmation();
                            } else if (processingItemizePressed != null) {
                                // They hit itemize so go on in, discarding changes.
                                viewClickHandler.onClick(processingItemizePressed);
                                processingItemizePressed = null;
                            } else if (processingReceiptAction != null) {
                                // No-op.
                                processingReceiptAction = null;
                            }
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(getText(R.string.confirm));
                    builder.setMessage(getText(R.string.confirm_entry_save_required));
                    builder.setPositiveButton(getText(R.string.general_ok), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and start the save.
                            dialog.dismiss();
                            save();
                        }
                    });
                    builder.setNegativeButton(getText(R.string.general_no), new OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Dismiss the dialog and clear the 'processingBackPressed' and
                            // 'processingSubmitPressed' flag.
                            dialog.dismiss();
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_INVALID_FORM_FIELD_VALUES: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_expense_invalid_field_values_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_save_invalid_field, null);
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_MISSING_HARD_STOP_FORM_FIELD_VALUES: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_expense_missing_fields_hard_stop_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_save_missing_field, null);
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_MISSING_SOFT_STOP_FORM_FIELD_VALUES: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_expense_missing_fields_soft_stop_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.proceed), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            if (hasCopyDownChildren()) {
                                // Check for copy-down values.
                                List<FormFieldView> copyDownValues = checkForChangedCopyDownValues();
                                if (copyDownValues != null) {
                                    // Set the reference used in 'onPrepareDialog' to dynamically populate
                                    // the main dialog view.
                                    missReqInvalidCopyDownFormFieldValues = copyDownValues;
                                    // Display the copy-down values dialog.
                                    showDialog(Const.DIALOG_EXPENSE_COPY_DOWN_FIELD_VALUES);
                                } else {
                                    // Check for connectivity, if none, then display dialog and return.
                                    if (ConcurCore.isConnected()) {
                                        // Commit form field values to their backed domain objects.
                                        commitEditedValues();
                                        // Instruct the actual save request to be sent to the server.
                                        sendSaveRequest();
                                    } else {
                                        // Reset the 'processingXXX' flags.
                                        processingBackPressed = false;
                                        processingSubmitPressed = false;
                                        processingItemizePressed = null;
                                        processingReceiptAction = null;
                                        // Inform end-user of the lack of connectivity.
                                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                                    }
                                }
                            } else {
                                // Check for connectivity, if none, then display dialog and return.
                                if (ConcurCore.isConnected()) {
                                    // Commit form field values to their backed domain objects.
                                    commitEditedValues();
                                    // Instruct the actual save request to be sent to the server.
                                    sendSaveRequest();
                                } else {
                                    // Reset the 'processingXXX' flags.
                                    processingBackPressed = false;
                                    processingSubmitPressed = false;
                                    processingItemizePressed = null;
                                    processingReceiptAction = null;
                                    // Inform end-user of the lack of connectivity.
                                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                                }
                            }
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Reset the 'processingXXX' flags.
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                            dialog.dismiss();
                        }
                    });
                    builder.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // Reset the 'processingXXX' flags.
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                            dialog.dismiss();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_save_missing_field, null);
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_COPY_DOWN_FIELD_VALUES: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.dlg_expense_copy_down_field_values_title);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.general_yes), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            // Check for connectivity, if none, then display dialog and return.
                            if (ConcurCore.isConnected()) {
                                // Set the overwrite copy-down value. Sub-classes in 'sendSaveRequest' may use
                                // this value.
                                overWriteCopyDownValues = true;
                                // Commit form field values to their backed domain objects.
                                commitEditedValues();
                                // Instruct the actual save request to be sent to the server.
                                sendSaveRequest();
                            } else {
                                // Reset the 'processingXXX' flags.
                                processingBackPressed = false;
                                processingSubmitPressed = false;
                                processingItemizePressed = null;
                                processingReceiptAction = null;
                                // Inform end-user of the lack of connectivity.
                                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            }
                        }
                    });
                    builder.setNeutralButton(getText(R.string.general_no), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            // Check for connectivity, if none, then display dialog and return.
                            if (ConcurCore.isConnected()) {
                                // Set the overwrite copy-down value. Sub-classes in 'sendSaveRequest' may use
                                // this value.
                                overWriteCopyDownValues = false;
                                // Commit form field values to their backed domain objects.
                                commitEditedValues();
                                // Instruct the actual save request to be sent to the server.
                                sendSaveRequest();
                            } else {
                                // Reset the 'processingXXX' flags.
                                processingBackPressed = false;
                                processingSubmitPressed = false;
                                processingItemizePressed = null;
                                processingReceiptAction = null;
                                // Inform end-user of the lack of connectivity.
                                showDialog(Const.DIALOG_NO_CONNECTIVITY);
                            }
                        }
                    });
                    builder.setNegativeButton(getText(R.string.cancel), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            // Reset the 'processingXXX' flags.
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                            dialog.dismiss();
                        }
                    });
                    builder.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            // Reset the 'processingXXX' flags.
                            processingBackPressed = false;
                            processingSubmitPressed = false;
                            processingItemizePressed = null;
                            processingReceiptAction = null;
                            dialog.dismiss();
                        }
                    });
                    LayoutInflater inflater = LayoutInflater.from(this);
                    View view = inflater.inflate(R.layout.report_save_invalid_field, null);
                    builder.setView(view);
                    dlg = builder.create();
                    break;
                }
                // MOB-10897
                case DIALOG_RECEIPT_IMAGE_UNAVAILABLE: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.no_receipts);
                    builder.setMessage(getText(R.string.no_receipts_attached_to_expenses).toString());
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.retrieve_conditional_field_action));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS_FAILURE: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.retrieve_conditional_field_action_failure_title);
                    builder.setMessage(R.string.retrieve_conditional_field_action_failure);
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_TAX_FORM_PROGRESS: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.retrieve_taxForm));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(true);
                    // Register a cancel listener to cancel the request.
                    dialog.setOnCancelListener(new OnCancelListener() {

                        @Override
                        public void onCancel(DialogInterface dialog) {
                            if (getTaxFormRequest != null) {
                                // Cancel the request, this will result in the 'SaveReportEntryReceiptReceiver.onReceiver' being
                                // invoked and handled as a cancellation.
                                getTaxFormRequest.cancel();
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onCreateDialog: getTaxFormRequest is null!");
                            }
                        }
                    });
                    dlg = dialog;
                    break;
                }
                case Const.DIALOG_EXPENSE_TAX_FORM_PROGRESS_FAILURE: {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle(R.string.retrieve_taxForm_failure);
                    builder.setMessage("");
                    builder.setCancelable(true);
                    builder.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                        }
                    });
                    dlg = builder.create();
                    break;
                }
                case Const.DIALOG_EXPENSE_REFRESH_EXPENSES: {
                    ProgressDialog dialog = new ProgressDialog(this);
                    dialog.setMessage(getText(R.string.dlg_refresh_expenses));
                    dialog.setIndeterminate(true);
                    dialog.setCancelable(false);
                    dlg = dialog;
                    break;
                }
                default:
                    dlg = ((ConcurCore) getApplication()).createDialog(this, id);
                    break;
                }
                dialogs.put(id, dlg);
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

        // Check whether a form field view will handle the dialog preparation.
        if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()
                && id >= FormFieldView.DIALOG_ID_BASE) {
            frmFldViewListener.getCurrentFormFieldView().onPrepareDialog(id, dialog);
        } else {
            switch (id) {
            case Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL:
                reportDetailDialog = dialog;
                break;
            case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT:
                saveReportEntryReceiptDialog = dialog;
                break;
            case Const.DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_CLEAR_RECEIPT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_VIEW_COMMENT: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                TextView txtView = (TextView) alertDlg.findViewById(R.id.comment_author);
                if (txtView != null) {
                    if (selComAuthor != null) {
                        txtView.setText(selComAuthor);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate comment author text view!");
                }
                txtView = (TextView) alertDlg.findViewById(R.id.comment_date);
                if (txtView != null) {
                    if (selComDate != null) {
                        txtView.setText(selComDate);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate comment date text view!");
                }
                txtView = (TextView) alertDlg.findViewById(R.id.comment_text);
                if (txtView != null) {
                    if (selComBody != null) {
                        txtView.setText(selComBody);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate comment body text view!");
                }
                break;
            }
            case Const.DIALOG_EXPENSE_SUBMIT_REPORT: {
                // Configure the field information.
                Locale locale = this.getResources().getConfiguration().locale;
                TextView txtView = (TextView) dialogView.findViewById(R.id.report_total_amount);
                if (txtView != null) {
                    txtView.setText(FormatUtil.formatAmount(expRep.totalClaimedAmount, locale, expRep.crnCode, true));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate total amount text view!");
                }
                txtView = (TextView) dialogView.findViewById(R.id.less_personal_amount_total);
                if (txtView != null) {
                    txtView.setText(FormatUtil.formatAmount(expRep.totalPersonalAmount, locale, expRep.crnCode, true));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate less personal amount text view!");
                }
                txtView = (TextView) dialogView.findViewById(R.id.amount_claimed_total);
                if (txtView != null) {
                    txtView.setText(FormatUtil.formatAmount(expRep.totalClaimedAmount, locale, expRep.crnCode, true));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate less personal amount text view!");
                }
                break;
            }
            case Const.DIALOG_EXPENSE_SUBMIT_REPORT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(reportSubmitFailedMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_APPROVE_REPORT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_ORIGINAL_RECEIPT_REQUIRED:
            case Const.DIALOG_EXPENSE_MISSING_RECEIPT: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                TableLayout tblLayout = (TableLayout) alertDlg.findViewById(R.id.expense_list_table);
                if (tblLayout != null) {
                    // First, clear out all rows in 'tblLayout' except for the header row.
                    for (int rowInd = tblLayout.getChildCount() - 1; rowInd >= 0; --rowInd) {
                        View tblRowView = tblLayout.getChildAt(rowInd);
                        if (tblRowView.getId() != R.id.expense_list_table_header) {
                            tblLayout.removeViewAt(rowInd);
                        }
                    }
                    // Iterate over the list of expenses that require receipts and inflate one instance of
                    // 'report_submit_missing_receipt_row' view per expense.
                    if (recReqExpList != null && recReqExpList.size() > 0) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        Iterator<ExpenseReportEntry> expRepIter = recReqExpList.iterator();
                        while (expRepIter.hasNext()) {
                            ExpenseReportEntry expRepEnt = expRepIter.next();

                            TableRow tblRow = (TableRow) inflater.inflate(R.layout.report_submit_missing_receipt_row,
                                    null);
                            // Set the expense type.
                            TextView txtView = (TextView) tblRow.findViewById(R.id.receipt_row_expense_type);
                            if (txtView != null) {
                                txtView.setText(expRepEnt.expenseName);
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing receipt row expense type text view!");
                            }
                            // Set the expense date.
                            txtView = (TextView) tblRow.findViewById(R.id.receipt_row_expense_date);
                            if (txtView != null) {
                                txtView.setText(expRepEnt.getFormattedTransactionDate());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing receipt row expense date text view!");
                            }
                            // Set the expense amount.
                            txtView = (TextView) tblRow.findViewById(R.id.receipt_row_expense_amount);
                            if (txtView != null) {
                                Locale loc = this.getResources().getConfiguration().locale;
                                String formattedAmount = FormatUtil.formatAmount(expRepEnt.transactionAmount, loc,
                                        expRepEnt.transactionCrnCode, true);
                                if (formattedAmount != null) {
                                    txtView.setText(formattedAmount);
                                    txtView.setGravity(Gravity.RIGHT);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG
                                            + ".onPrepareDialog: unable to format transaction amount of '"
                                            + expRepEnt.transactionAmount + "'.");
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing receipt row expense amount text view!");
                            }
                            // Add it to the table layout.
                            tblLayout.addView(tblRow);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: empty list of expenses requiring receipts!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing table layout view!");
                }
                break;
            }
            case Const.DIALOG_EXPENSE_REMOVE_REPORT_EXPENSE_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL_FAILED: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                alertDlg.setMessage(actionStatusErrorMessage);
                break;
            }
            case DIALOG_RECEIPT_IMAGE: {
                // Set the current list of options.
                // Check for whether the download option should present.
                if (receiptActionAdapter != null) {
                    receiptActionAdapter.options.clear();

                    if (canEditReceipt()) {
                        // Check for whether Receipt Store is accessible.
                        if (!ViewUtil.isReceiptStoreHidden(this)) {
                            receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD);
                        }
                        // Add "Choose Picture".
                        receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE);
                        // Add "Take Picture".
                        receiptActionAdapter.options.add(ReceiptPictureSaveAction.TAKE_PICTURE);
                    }
                    if (isReportLevelReceiptDialog()) {
                        if (expRep.isReceiptImageAvailable()) {
                            // Report level dialog and receipt image available.
                            receiptActionAdapter.options.add(ReceiptPictureSaveAction.VIEW);
                        }
                    } else if (isReportEntryWithReceipt() || isReportEntryWithEReceipt()) {
                        // Report entry with receipt.
                        receiptActionAdapter.options.add(ReceiptPictureSaveAction.VIEW);
                    }
                    // Notify any listeners.
                    receiptActionAdapter.notifyDataSetChanged();
                } else {
                    Log.w(CLS_TAG, "ReceiptImageOptionListAdapter is null!");
                }
                break;
            }
            case Const.DIALOG_EXPENSE_COPY_DOWN_FIELD_VALUES:
            case Const.DIALOG_EXPENSE_INVALID_FORM_FIELD_VALUES: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                // Set the main message of the dialog.
                TextView txtView = (TextView) alertDlg.findViewById(R.id.invalid_field_message);
                if (txtView != null) {
                    if (id == Const.DIALOG_EXPENSE_INVALID_FORM_FIELD_VALUES) {
                        txtView.setText(getResources().getQuantityString(
                                R.plurals.dlg_expense_invalid_field_values_message,
                                missReqInvalidCopyDownFormFieldValues.size()));
                    } else {
                        txtView.setText(getCopyDownPromptCharSequence());
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: unable to locate text view!");
                }
                // Update the table contents.
                TableLayout tblLayout = (TableLayout) alertDlg.findViewById(R.id.field_list_table);
                if (tblLayout != null) {
                    // First, clear out all rows in 'tblLayout' except for the header row.
                    for (int rowInd = tblLayout.getChildCount() - 1; rowInd >= 0; --rowInd) {
                        View tblRowView = tblLayout.getChildAt(rowInd);
                        if (tblRowView.getId() != R.id.field_list_table_header) {
                            tblLayout.removeViewAt(rowInd);
                        }
                    }
                    // Iterate over the list of form field views that have invalid values and construct
                    // an instance of 'report_save_invalid_field_row' view per form field.
                    if (missReqInvalidCopyDownFormFieldValues != null
                            && missReqInvalidCopyDownFormFieldValues.size() > 0) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        for (FormFieldView frmFldView : missReqInvalidCopyDownFormFieldValues) {

                            TableRow tblRow = (TableRow) inflater.inflate(R.layout.report_save_invalid_field_row, null);
                            // Set the field name.
                            txtView = (TextView) tblRow.findViewById(R.id.field_row_field_name);
                            if (txtView != null) {
                                txtView.setText(frmFldView.getFieldLabel());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing field row field name text view!");
                            }
                            // Set the current value.
                            txtView = (TextView) tblRow.findViewById(R.id.field_row_current_value);
                            if (txtView != null) {
                                txtView.setText(frmFldView.getCurrentValue());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing field row field name text view!");
                            }
                            // Add it to the table layout.
                            tblLayout.addView(tblRow);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onPrepareDialog: empty list of form fields with invalid/copy-down values!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing table layout view!");
                }
                break;
            }
            case Const.DIALOG_EXPENSE_MISSING_SOFT_STOP_FORM_FIELD_VALUES:
            case Const.DIALOG_EXPENSE_MISSING_HARD_STOP_FORM_FIELD_VALUES: {
                AlertDialog alertDlg = (AlertDialog) dialog;
                // Set the main message of the dialog.
                TextView txtView = (TextView) alertDlg.findViewById(R.id.missing_field_message);
                if (txtView != null) {
                    int strResId = -1;
                    if (id == Const.DIALOG_EXPENSE_MISSING_HARD_STOP_FORM_FIELD_VALUES) {
                        strResId = R.string.dlg_expense_missing_fields_hard_stop_message;
                    } else {
                        strResId = R.string.dlg_expense_missing_fields_soft_stop_message;
                    }
                    if (strResId != -1) {
                        txtView.setText(strResId);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".onPrepareDialog: unable to locate missing_field_message text view!");
                }
                // Update the table contents.
                TableLayout tblLayout = (TableLayout) alertDlg.findViewById(R.id.field_list_table);
                if (tblLayout != null) {
                    // First, clear out all rows in 'tblLayout' except for the header row.
                    for (int rowInd = tblLayout.getChildCount() - 1; rowInd >= 0; --rowInd) {
                        View tblRowView = tblLayout.getChildAt(rowInd);
                        if (tblRowView.getId() != R.id.field_list_table_header) {
                            tblLayout.removeViewAt(rowInd);
                        }
                    }
                    // Iterate over the list of form field views that have missing values and construct
                    // an instance of 'report_save_missing_field_row' view per form field.
                    if (missReqInvalidCopyDownFormFieldValues != null
                            && missReqInvalidCopyDownFormFieldValues.size() > 0) {
                        LayoutInflater inflater = LayoutInflater.from(this);
                        for (FormFieldView frmFldView : missReqInvalidCopyDownFormFieldValues) {

                            TableRow tblRow = (TableRow) inflater.inflate(R.layout.report_save_missing_field_row, null);
                            // Set the field name.
                            txtView = (TextView) tblRow.findViewById(R.id.field_row_field_name);
                            if (txtView != null) {
                                txtView.setText(frmFldView.getFieldLabel());
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG
                                        + ".onPrepareDialog: missing field row field name text view!");
                            }
                            // Add it to the table layout.
                            tblLayout.addView(tblRow);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".onPrepareDialog: empty list of form fields with missing values!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onPrepareDialog: missing table layout view!");
                }
                break;
            }
            }
        }
    }

    /**
     * Gets whether this expense activity has a report entry that has a receipt.
     * 
     * @return whether this expense activity has a report entry with a receipt.
     */
    protected boolean isReportEntryWithReceipt() {
        return false;
    }

    /**
     * Send the report off for approval.
     */
    protected void handleApproveReport() {
        // Send expense approval off for processing.
        ConcurCore app = (ConcurCore) getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
        // Register the receiver.
        registerReportApproveReceiver();
        reportApproveRequest = app.getService().sendReportApprove(expRep, "",
                prefs.getString(Const.PREF_USER_ID, null), reportApproveStatKey);
        if (reportApproveRequest != null) {
            showDialog(Const.DIALOG_EXPENSE_APPROVE_REPORT_PROGRESS);
            reportApproveReceiver.setRequest(reportApproveRequest);
        } else {
            unregisterReportApproveReceiver();
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleApproveReport: unable to create report approve request!");
        }
    }

    /**
     * Send the report off for submission for approval.
     * 
     * @param approver
     *            an optional approver.
     */
    protected void handleSubmitReport(ExpenseReportApprover approver) {
        // Send expense submittal off for processing.
        ConcurCore app = (ConcurCore) getApplication();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app.getApplicationContext());
        registerReportSubmitReceiver();
        reportSubmitRequest = app.getService().sendReportSubmit(expRep, prefs.getString(Const.PREF_USER_ID, null),
                approver);
        if (reportSubmitRequest != null) {
            showDialog(Const.DIALOG_EXPENSE_SUBMITTING_REPORT);
            reportSubmitReceiver.setRequest(reportSubmitRequest);
        } else {
            unregisterReportSubmitReceiver();
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleSubmitReport: unable to create report approve request!");
        }
    }

    /**
     * Gets the copy-down prompt char sequence to be displayed when saving form fields that provide copy-down values for related
     * fields.
     * 
     * @return the prompt to be displayed.
     */
    protected CharSequence getCopyDownPromptCharSequence() {
        return null;
    }

    /**
     * Determines whether this activity requires detailed report information.
     * 
     * @return whether this activity requires detailed report information.
     */
    protected abstract boolean isDetailReportRequired();

    /**
     * Gets the title string resource id of the text string that should be displayed in the header navigation bar.
     * 
     * @return the string resource id of the text string to be rendered in the header navigation bar.
     */
    protected abstract int getHeaderNavBarTitleResourceId();

    protected void setExpenseReport(Intent intent) {
        if (setExpenseReportWithoutBuildView(intent)) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".setExpenseReport: calling 'buildView'.");
            buildView();
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".setExpenseReport: not calling 'buildView'.");
        }
    }

    /**
     * Will hide the screen footer based on examining the visibility of the approve/reject buttons contained in the footer.
     */
    protected void hideFooterIfNecessary() {
        Button rejectButton = (Button) findViewById(R.id.reject_button);
        Button approveButton = (Button) findViewById(R.id.approve_button);
        if ((rejectButton == null || rejectButton.getVisibility() == View.INVISIBLE)
                && (approveButton == null || approveButton.getVisibility() == View.INVISIBLE)) {
            View footer = findViewById(R.id.expense_nav_bar_footer);
            if (footer != null) {
                footer.setVisibility(View.GONE);
            }
        }
    }

    /**
     * Set the instance of <code>ExpenseReport</code> based on information in <code>intent</code>.
     * 
     * @param intent
     *            the intent containing expense report information.
     * @return A boolean indicating whether it is safe to build the view.
     */
    protected boolean setExpenseReportWithoutBuildView(Intent intent) {

        boolean viewCanBeBuilt = false;

        String reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
        reportKeySource = intent.getIntExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, -1);
        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                || reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {

            ConcurCore app = (ConcurCore) getApplication();
            switch (reportKeySource) {
            case Const.EXPENSE_REPORT_SOURCE_ACTIVE:
                expRepCache = app.getExpenseActiveCache();
                break;
            case Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL:
                expRepCache = app.getExpenseApprovalCache();
                break;
            }

            // Determine if this activity wants to know of network activity updates.
            if (shouldListenForNetworkActivity()) {
                registerNetworkActivityReceiver();
            }

            // Check to see whether this activity requires a detailed report model.
            if (isDetailReportRequired()) {
                if (reportKey != null && expRepCache.hasReportDetail(reportKey)) {
                    expRep = expRepCache.getReportDetail(reportKey);
                    // Ensure the expense report is set on any view listener prior to building the view.
                    if (frmFldViewListener != null) {
                        frmFldViewListener.setExpenseReport(expRep);
                    }
                    // Let the caller know that it is safe to build the view
                    viewCanBeBuilt = true;
                    // Intialize the state based on the 'lastSavedInstanceState' attribute.
                    initializeState();

                    // If the 'Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE' is present in the intent and it's value is 'true', then
                    // perform a background update of the detailed report data.
                    if (intent.getBooleanExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, false)) {
                        // Make a request to retrieve a detailed report.
                        // Get report detail data. When it is available, a broadcast will be
                        // sent.
                        // Register the network activity receiver.
                        registerNetworkActivityReceiver();
                        if (lastSavedInstanceState == null) {
                            if (ConcurCore.isConnected()) {
                                // Register the receiver.
                                registerReportDetailReceiver();
                                if (ViewUtil.shouldFetchDetailSummaryReport(expRep)) {
                                    reportDetailRequest = ((ConcurCore) getApplication()).getService()
                                            .sendReportDetailSummaryRequest(reportKey, reportKeySource);

                                } else {
                                    reportDetailRequest = ((ConcurCore) getApplication()).getService()
                                            .sendReportDetailRequest(reportKey, reportKeySource);
                                }
                                if (reportDetailRequest != null) {
                                    // Set the request on the receiver.
                                    reportDetailReceiver.setRequest(reportDetailRequest);
                                } else {
                                    // Unregister the receiver.
                                    unregisterReportDetailReceiver();
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".setExpenseReportWithoutBuildView: unable to create report detail request!");
                                }
                            } else {
                                Log.i(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".setExpenseReportWithoutBuildView: unable to refresh detail expense report due to client being offline.");
                            }
                        }
                    }
                } else {
                    // Only show the dialog and request if there is no request pending.
                    if (expRepCache != null && !expRepCache.isDetailedReportRequestPending()) {
                        // Display a dialog regarding retrieving detailed report data.
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);

                        // Make a request to retrieve a detailed report.
                        // Get report detail data. When it is available, a broadcast will be
                        // sent.
                        // Register the receiver.
                        registerReportDetailReceiver();
                        ExpenseReport report = expRepCache.getReport(reportKey);
                        if (ViewUtil.shouldFetchDetailSummaryReport(report)) {
                            reportDetailRequest = ((ConcurCore) getApplication()).getService()
                                    .sendReportDetailSummaryRequest(reportKey, reportKeySource);
                        } else {
                            reportDetailRequest = ((ConcurCore) getApplication()).getService().sendReportDetailRequest(
                                    reportKey, reportKeySource);
                        }
                        if (reportDetailRequest != null) {
                            // Set the request on the receiver.
                            reportDetailReceiver.setRequest(reportDetailRequest);
                        } else {
                            // Unregister the receiver.
                            unregisterReportDetailReceiver();
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".setExpenseReportWithoutBuildView: unable to create report detail request!");
                        }
                    } else {
                        // Display a dialog regarding retrieving detailed report data.
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                    }
                }
            } else {

                // Even though a non-detailed report is required, give preference to a detailed report if one
                // exists. Some actions on reports from the MWS return detailed reports with updated information.
                expRep = expRepCache.getReportDetail(reportKey);
                if (expRep == null) {
                    // Fall-back to a non-detailed report.
                    expRep = expRepCache.getReport(reportKey);
                }

                // Ensure the expense report is set on any view listener prior to building the view.
                if (frmFldViewListener != null) {
                    frmFldViewListener.setExpenseReport(expRep);
                }
                // Let the caller know that it is safe to build the view
                viewCanBeBuilt = true;
                // Intialize the state based on the 'lastSavedInstanceState' attribute.
                initializeState();

                // If this activity should pre-fetch detailed report data.
                if (!expRep.isDetail() && shouldPrefetchDetailedReport()) {
                    // Make a request to retrieve a detailed report.
                    // Get report detail data. When it is available, a broadcast will be
                    // sent.
                    // Register the receiver.
                    registerReportDetailReceiver();
                    if (ViewUtil.shouldFetchDetailSummaryReport(expRep)) {
                        reportDetailRequest = ((ConcurCore) getApplication()).getService()
                                .sendReportDetailSummaryRequest(reportKey, reportKeySource);
                    } else {
                        reportDetailRequest = ((ConcurCore) getApplication()).getService().sendReportDetailRequest(
                                reportKey, reportKeySource);
                    }
                    if (reportDetailRequest != null) {
                        // Set the request on the receiver.
                        reportDetailReceiver.setRequest(reportDetailRequest);
                    } else {
                        // Unregister the receiver.
                        unregisterReportDetailReceiver();
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".setExpenseReportWithoutBuildView: unable to create report detail request!");
                    }
                }
            }

        } else if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
            viewCanBeBuilt = true;
            // Intialize the state based on the 'lastSavedInstanceState' attribute.
            initializeState();

            // Determine if this activity wants to know of network activity updates.
            if (shouldListenForNetworkActivity()) {
                registerNetworkActivityReceiver();
            }

        } else {
            Log.e(Const.LOG_TAG,
                    CLS_TAG
                            + ".setExpenseReportWithoutBuildView: invalid intent key of 'Const.EXPENSE_REPORT_SOURCE_KEY' - value '"
                            + reportKeySource + "'.");
        }

        return viewCanBeBuilt;
    }

    /**
     * Gets whether this activity is interested in network activitiy updates.
     * 
     * @return whether this activity is interested in network activity updates.
     */
    protected boolean shouldListenForNetworkActivity() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#isNetworkRequestInteresting(int)
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_EXPENSE_REPORT_DETAIL_REQUEST || networkMsgType == Const.MSG_EXPENSE_REPORT_HEADER_DETAIL_REQUEST);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener#getNetworkActivityText(java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return getText(R.string.update_report_detail).toString();
    }

    /**
     * Builds the view dynamically based on the expense report model.
     */
    protected abstract void buildView();

    /**
     * Gets the instance of <code>Intent</code> used to set up a broadcast receiver to receive notifications of data updates
     * relevant to this activity.
     * 
     * @return an instance of <code>Intent</code> appropriate to this activity to receive data updates.
     */
    protected abstract IntentFilter getBroadcastReceiverIntentFilter();

    /**
     * Gets whether this activity should listen for data update broadcast events.
     * 
     * @return whether this activity should listen for data update broadcast events.
     */
    protected abstract boolean shouldReceiveDataEvents();

    /**
     * Gets whether this activity should retrieve a detailed report.
     * 
     * @return whether this activity should retrieve a detailed report.
     */
    protected boolean shouldPrefetchDetailedReport() {
        return false;
    }

    /**
     * Gets whether or not the title bar should show the action button.
     * 
     * @return whether the title bar should show the action button.
     */
    protected boolean showTitleBarActionButton() {
        return false;
    }

    /**
     * Handles when the end-user has selected the add action button.
     */
    protected void onAddActionButton() {
        // No-op.
    }

    /**
     * Will set report specific information on the expense header navigation bar.
     * 
     * @param expRep
     *            the expense report.
     */
    protected void configureScreenHeader(final ExpenseReport expRep) {

        // Show/Hide the screen title action button.
        ImageView imgView = (ImageView) findViewById(R.id.action_button);
        if (imgView != null) {
            if ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE && isReportEditable() && isSaveReportEnabled())
                    || (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW)) {
                // Set the action button image to the save icon.
                imgView.setImageResource(R.drawable.actionbar_save);
                imgView.setVisibility(View.VISIBLE);
                imgView.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        save();
                    }
                });
            } else {
                if (showTitleBarActionButton()) {
                    imgView.setVisibility(View.VISIBLE);
                    imgView.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            onAddActionButton();
                        }
                    });
                } else {
                    imgView.setVisibility(View.GONE);
                }
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderNavBarInfo: unable to locate action button image view!");
        }

        // Set the expense header navigation bar information.
        try {
            String headerNavBarTitle = getResources().getString(getHeaderNavBarTitleResourceId());
            getSupportActionBar().setTitle(headerNavBarTitle);
        } catch (Resources.NotFoundException resNotFndExc) {
            Log.e(Const.LOG_TAG, CLS_TAG
                    + ".populateExpenseHeaderNavBarInfo: missing navigation bar title text resource!", resNotFndExc);
        }

    }

    /**
     * Will configure the screen footer.
     */
    protected void configureScreenFooter() {

        // Set up a click listener on the reject report button.
        Button rejectButton = (Button) findViewById(R.id.reject_button);
        if (rejectButton != null) {
            if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                if (isRejectReportEnabled()) {
                    Intent clickIntent = new Intent(this, ExpenseSendBack.class);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                    viewClickHandler.addViewLauncher(rejectButton, clickIntent);
                    rejectButton.setOnClickListener(viewClickHandler);
                } else {
                    rejectButton.setVisibility(View.INVISIBLE);
                }
            } else {
                // Dealing with active reports...
                Class<? extends Activity> exporter = ((ConcurCore) getApplication()).getExportActivity();
                if (exporter == null || !isExportReportEnabled()) {
                    // And no export ability so hide the button
                    rejectButton.setVisibility(View.INVISIBLE);
                } else {
                    // Make it the export button
                    rejectButton.setText(R.string.general_export);
                    // And set the handler
                    Intent clickIntent = new Intent(this, exporter);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                    viewClickHandler.addViewLauncher(rejectButton, clickIntent);
                    rejectButton.setOnClickListener(viewClickHandler);
                }
            }
        }

        // Set up click listener on the approve report button.
        Button approveButton = (Button) findViewById(R.id.approve_button);
        if (approveButton != null) {
            boolean buttonVisible = ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE)
                    && (expRep.apsKey.equalsIgnoreCase("A_NOTF") || expRep.apsKey.equalsIgnoreCase("A_RESU")) && isSubmitReportEnabled());
            if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE) {
                approveButton.setText(R.string.submit);
            }
            approveButton.setOnClickListener(new View.OnClickListener() {

                /*
                 * (non-Javadoc)
                 * 
                 * @see android.view.View.OnClickListener#onClick(android.view.View)
                 */
                @Override
                public void onClick(View v) {
                    // Check for connectivity, if none, then display dialog and return.
                    if (!ConcurCore.isConnected()) {
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                        return;
                    }
                    if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                        if (expRep.workflowActions != null && expRep.workflowActions.size() > 0) {
                            showDialog(Const.DIALOG_EXPENSE_SELECT_WORKFLOW_ACTION);
                        } else {
                            showDialog(Const.DIALOG_EXPENSE_APPROVE_REPORT);
                        }
                    } else {
                        if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE && isReportEditable()
                                && isSaveReportEnabled() && (hasFormFieldsChanged() || changesPending())) {
                            // Set the flag indicating the save report process was kicked off due
                            // to a report submit with changed fields.
                            processingSubmitPressed = true;
                            // Show the dialog confirming about save.
                            showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                        } else {
                            // Start the report submit checks.
                            startSubmitReportConfirmation();
                        }
                    }
                }
            });
            // Hide the approve button, if needbe.
            if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE && !buttonVisible) {
                approveButton.setVisibility(View.INVISIBLE);
            } else if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL && !isApproveReportEnabled()) {
                approveButton.setVisibility(View.INVISIBLE);
            } else if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
                approveButton.setVisibility(View.INVISIBLE);
            }
        }

        hideFooterIfNecessary();

    }

    /**
     * Will start the process of performing a series of checks when the end-user decides to submit the report.
     */
    protected void startSubmitReportConfirmation() {
        // Sanity check that we're dealing with a report detail object.
        if (expRep.isDetail()) {
            ExpenseReportDetail expRepDet = (ExpenseReportDetail) expRep;

            if (expRepDet.getExpenseEntries() == null || expRepDet.getExpenseEntries().size() == 0) {
                // Report has no expense entries.
                showDialog(Const.DIALOG_EXPENSE_REPORT_NO_ENTRIES);
            } else if (ViewUtil.expenseReportHasUndefinedExpenseTypes(expRepDet)) {
                // An expense containing an undefined expense type is a "hard" stop.
                showDialog(Const.DIALOG_EXPENSE_UNDEFINED_EXPENSE_TYPE);
                // } else if ((recReqExpList = isMissingReceipts(expRepDet)) != null) {
                // A report that requires receipts, but is lacking them is a "soft" stop.
            } else if ((imageOrReceiptRequired = isMissingImageOrReceipt(expRepDet)) != NO_IMG_OR_REC_REQ) {
                if (imageOrReceiptRequired == IMG_REQ) {
                    showDialog(Const.DIALOG_EXPENSE_MISSING_RECEIPT);
                } else if (imageOrReceiptRequired == REC_REQ) {
                    showDialog(Const.DIALOG_EXPENSE_ORIGINAL_RECEIPT_REQUIRED);
                }
            } else {
                // Display a confirmation dialog.
                showDialog(Const.DIALOG_EXPENSE_SUBMIT_REPORT_CONFIRM);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + "onClick: non detail expense report!");
        }
    }

    /**
     * Determines whether or not any values stored in <code>FormFieldView</code> objects have changed from their underlying
     * <code>ExpenseReportFormField</code> objects.
     * 
     * @return whether any values have been edited.
     */
    protected boolean hasFormFieldsChanged() {
        boolean retVal = false;
        if (isReportEditable()) {
            if (frmFldViewListener != null) {
                List<FormFieldView> frmFldViews = frmFldViewListener.getFormFieldViews();
                if (frmFldViews != null) {
                    for (FormFieldView frmFldView : frmFldViews) {
                        // Only look at views that actually made it to the screen
                        if (frmFldView.view != null && frmFldView.view.isShown()) {
                            if (frmFldView.hasValueChanged()) {
                                retVal = true;
                                break;
                            }
                        }
                    }
                } else {
                    // Not a normal situation. Log it.
                    Log.e(Const.LOG_TAG, CLS_TAG + ".hasFormFieldsChanged: null form field list returned");
                }
            }
        }
        boolean taxRetValue = hasTaxFormFieldsChanged();
        return (retVal || taxRetValue);
    }

    /**
     * Determines whether or not any values stored in <code>FormFieldView</code> objects have changed from their underlying
     * <code>ExpenseReportFormField</code> objects of <code>TaxForm</code>
     * 
     * @return whether any values have been edited.
     */
    private boolean hasTaxFormFieldsChanged() {
        boolean retVal = false;
        if (isReportEditable()) {
            if (frmFldViewListener != null) {
                List<FormFieldView> frmFldViews = frmFldViewListener.getTaxFormFieldViews();
                if (frmFldViews != null) {
                    for (FormFieldView frmFldView : frmFldViews) {
                        // Only look at views that actually made it to the screen
                        if (frmFldView.view != null && frmFldView.view.isShown()) {
                            if (frmFldView.hasValueChanged()) {
                                retVal = true;
                                break;
                            }
                        }
                    }
                } else {
                    // Not a normal situation. Log it.
                    Log.e(Const.LOG_TAG, CLS_TAG + ".hasTaxFormFieldsChanged: null form field list returned");
                }
            }
        }
        return retVal;
    }

    /**
     * Gets whether there are non form-field changes pending. <br>
     * <b>NOTE:</b><br>
     * This method returns <code>false</code>, sub-classes may override this method to check for non form-field oriented changes.
     * 
     * @return whether or not there are non form-field changes pending.
     */
    protected boolean changesPending() {
        return false;
    }

    /**
     * Will start the process of saving a report/report entry.
     */
    protected void save() {

        // Perform various data validation checks.
        List<FormFieldView> invalidFieldValues = checkForInvalidValues();
        if (invalidFieldValues != null) {
            // Reset the 'processingXXX' flags.
            processingBackPressed = false;
            processingSubmitPressed = false;
            processingItemizePressed = null;
            // Set the reference used in the 'onPrepareDialog' to dynamically populate
            // the main dialog view.
            missReqInvalidCopyDownFormFieldValues = invalidFieldValues;
            // Display a dialog about the invalid field values.
            showDialog(Const.DIALOG_EXPENSE_INVALID_FORM_FIELD_VALUES);
        } else {
            // First, obtain a list of form field views that are required, but have missing
            // values.
            List<FormFieldView> reqMissingValues = checkForMissingValues();
            if (reqMissingValues != null) {
                // Check for hard-stop missing values.
                List<FormFieldView> hardStopMissingValues = checkForHardStopMissingFieldValues(reqMissingValues);
                if (hardStopMissingValues != null) {
                    // Reset the 'processingXXX' flags.
                    processingBackPressed = false;
                    processingSubmitPressed = false;
                    processingItemizePressed = null;
                    // Set the reference used in 'onPrepareDialog' to dynamically populate
                    // the main dialog view.
                    missReqInvalidCopyDownFormFieldValues = hardStopMissingValues;
                    // Display the missing values hard-stop dialog.
                    showDialog(Const.DIALOG_EXPENSE_MISSING_HARD_STOP_FORM_FIELD_VALUES);
                } else {
                    // Set the reference used in the 'onPrepareDialog' to dynamically populate
                    // the main dialog view.
                    missReqInvalidCopyDownFormFieldValues = reqMissingValues;
                    // Display the missing values soft-stop dialog.
                    showDialog(Const.DIALOG_EXPENSE_MISSING_SOFT_STOP_FORM_FIELD_VALUES);
                }
            } else if (hasCopyDownChildren()) {
                // Check for copy-down values.
                List<FormFieldView> copyDownValues = checkForChangedCopyDownValues();
                if (copyDownValues != null) {
                    // Set the reference used in 'onPrepareDialog' to dynamically populate
                    // the main dialog view.
                    missReqInvalidCopyDownFormFieldValues = copyDownValues;
                    // Display the copy-down values dialog.
                    showDialog(Const.DIALOG_EXPENSE_COPY_DOWN_FIELD_VALUES);
                } else {
                    // Check for connectivity, if none, then display dialog and return.
                    if (ConcurCore.isConnected()) {
                        // Commit form field values to their backed domain objects.
                        commitEditedValues();
                        // Instruct the actual save request to be sent to the server.
                        sendSaveRequest();
                    } else {
                        // Reset the 'processingXXX' flags.
                        processingBackPressed = false;
                        processingSubmitPressed = false;
                        processingItemizePressed = null;
                        showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                }
            } else {
                // Check for connectivity, if none, then display dialog and return.
                if (ConcurCore.isConnected()) {
                    // Commit form field values to their backed domain objects.
                    commitEditedValues();
                    // Instruct the actual save request to be sent to the server.
                    sendSaveRequest();
                } else {
                    // Reset the 'processingXXX' flags.
                    processingBackPressed = false;
                    processingSubmitPressed = false;
                    processingItemizePressed = null;
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
            }
        }
    }

    /**
     * Will return whether the current report header/entry being edited has copy-down children.
     * 
     * @return whether the current report header/entry being edited has copy-down children.
     */
    protected boolean hasCopyDownChildren() {
        return false;
    }

    /**
     * Will commit edited values in form field view objects to their underlying form field objects.
     */
    protected void commitEditedValues() {
        // Commit form field values to their backed domain objects.
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    frmFldView.commit();
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".commitEditedValues: frmFldViewListener is null!");
        }
        commitEditedValuesForTax();
    }

    /**
     * Will commit edited values in tax form field view objects to their underlying form field objects.
     */
    protected void commitEditedValuesForTax() {
        // Commit form field values to their backed domain objects.
        if (frmFldViewListener != null) {
            List<FormFieldView> listTaxFrmField = frmFldViewListener.getTaxFormFieldViews();
            if (listTaxFrmField != null && listTaxFrmField.size() > 0) {
                for (FormFieldView frmFldView : listTaxFrmField) {
                    frmFldView.commit();
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".commitEditedTaxValues: frmFldViewListener is null!");
        }
    }

    /**
     * Will send the actual save request to the server. This implementation is a no-op, sub-classes should override this method to
     * send the appropriate save request to the server.
     */
    protected void sendSaveRequest() {
        // No-op.
    }

    protected List<FormFieldView> checkForChangedCopyDownValues() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    if (frmFldView.hasValueChanged() && frmFldView.getFormField().isCopyDownSourceForOtherForms()) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        List<FormFieldView> taxRetVal = checkForChangedCopyDownValuesForTax();
        if (taxRetVal != null && taxRetVal.size() > 0) {
            if (retVal == null) {
                retVal = new ArrayList<FormFieldView>();
            }
            retVal.addAll(retVal.size(), taxRetVal);
        }
        return retVal;
    }

    protected List<FormFieldView> checkForChangedCopyDownValuesForTax() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            List<FormFieldView> listFrmFld = frmFldViewListener.getTaxFormFieldViews();
            if (listFrmFld != null && listFrmFld.size() > 0) {
                for (FormFieldView frmFldView : listFrmFld) {
                    if (frmFldView.hasValueChanged() && frmFldView.getFormField().isCopyDownSourceForOtherForms()) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will examine all filled in form field values for valid values.
     * 
     * @return the list of form field view objects containing invalid data.
     */
    protected List<FormFieldView> checkForInvalidValues() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    ValidityCheck check = frmFldView.isValueValid();
                    if (!check.result) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        List<FormFieldView> taxRetVal = checkForInvalidValuesForTax();
        if (taxRetVal != null && taxRetVal.size() > 0) {
            if (retVal == null) {
                retVal = new ArrayList<FormFieldView>();
            }
            retVal.addAll(retVal.size(), taxRetVal);
        }
        return retVal;
    }

    /**
     * Will examine all filled in form field values for valid tax values.
     * 
     * @return the list of form field view objects containing invalid data.
     */
    protected List<FormFieldView> checkForInvalidValuesForTax() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            List<FormFieldView> listFrmFld = frmFldViewListener.getTaxFormFieldViews();
            if (listFrmFld != null && listFrmFld.size() > 0) {
                for (FormFieldView frmFldView : listFrmFld) {
                    ValidityCheck check = frmFldView.isValueValid();
                    if (!check.result) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will return a list of form field objects with required but missing values.
     * 
     * @return the list of form field objects with required but missing values.
     */
    protected List<FormFieldView> checkForMissingValues() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            if (frmFldViewListener.getFormFieldViews() != null) {
                for (FormFieldView frmFldView : frmFldViewListener.getFormFieldViews()) {
                    // Check for required, no-value, with visibile views.
                    if (frmFldView.getFormField().isRequired() && !frmFldView.hasValue() && frmFldView.view != null
                            && frmFldView.view.getVisibility() == View.VISIBLE
                            && frmFldView.getFormField().getAccessType() == AccessType.RW) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        List<FormFieldView> taxRetVal = checkForMissingValuesForTax();
        if (taxRetVal != null && taxRetVal.size() > 0) {
            if (retVal == null) {
                retVal = new ArrayList<FormFieldView>();
            }
            retVal.addAll(retVal.size(), taxRetVal);
        }
        return retVal;
    }

    /**
     * Will return a list of tax form field objects with required but missing values.
     * 
     * @return the list of form field objects with required but missing values.
     */
    protected List<FormFieldView> checkForMissingValuesForTax() {
        List<FormFieldView> retVal = null;
        if (frmFldViewListener != null) {
            List<FormFieldView> listFrmFld = frmFldViewListener.getTaxFormFieldViews();
            if (listFrmFld != null && listFrmFld.size() > 0) {
                for (FormFieldView frmFldView : listFrmFld) {
                    // Check for required, no-value, with visibile views.
                    if (frmFldView.getFormField().isRequired() && !frmFldView.hasValue() && frmFldView.view != null
                            && frmFldView.view.getVisibility() == View.VISIBLE
                            && frmFldView.getFormField().getAccessType() == AccessType.RW) {
                        if (retVal == null) {
                            retVal = new ArrayList<FormFieldView>();
                        }
                        retVal.add(frmFldView);
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will check for any hard-stop missing field values. <br>
     * <b>NOTE:</b><br>
     * Hard-stops are notifications of fields that require values but which do not currently have a value. The end-user can not
     * proceed.
     * 
     * @param frmFldView
     *            the list of form field views from which to retrieve hard-stop and missing values.
     * 
     * @return a list of form field view objects with missing values that are required and do cause a hard-stop on saving. If
     *         <code>null</code> the no such fields were detected.
     */
    protected List<FormFieldView> checkForHardStopMissingFieldValues(List<FormFieldView> frmFldViews) {
        List<FormFieldView> retVal = null;
        String[] hardStopFieldIds = getHardStopFieldIds();
        if (hardStopFieldIds != null) {
            for (FormFieldView frmFldView : frmFldViews) {
                if (frmFldView.getFormField().isRequired() && !frmFldView.hasValue()) {
                    // Check whether 'frmFldView' is a hard-stop field.
                    for (int fieldInd = 0; fieldInd < hardStopFieldIds.length; ++fieldInd) {
                        if (frmFldView.getFormField().getId().equalsIgnoreCase(hardStopFieldIds[fieldInd])) {
                            if (retVal == null) {
                                retVal = new ArrayList<FormFieldView>();
                            }
                            retVal.add(frmFldView);
                            break;
                        }
                    }
                }
            }
        }
        return retVal;
    }

    /**
     * Will get a list of field ids for which a required missing value is a hard-stop, i.e., does not permit a save operation.
     * 
     * @return the list of hard-stop form field ids.
     */
    protected String[] getHardStopFieldIds() {
        return null;
    }

    /**
     * Gets whether a report save operation is enabled.
     * 
     * @return whether or not report save operation is enabled.
     */
    protected boolean isSaveReportEnabled() {
        return false;
    }

    /**
     * Whether a report submit operation is enabled.
     * 
     * @return whether a report submit operation is enabled.
     */
    protected boolean isSubmitReportEnabled() {
        return false;
    }

    /**
     * Whether a report approve operation is enabled.
     * 
     * @return whether a report approve operation is enabled.
     */
    protected boolean isApproveReportEnabled() {
        return false;
    }

    /**
     * Whether a report reject operation is enabled.
     * 
     * @return whether a report reject operation is enabled.
     */
    protected boolean isRejectReportEnabled() {
        return false;
    }

    protected boolean isExportReportEnabled() {
        return false;
    }

    /**
     * Determines whether the expense report detail has "missing receipts" by examining whether the report requires receipts and
     * if so whether their exists expense entries that require them.
     * 
     * @param expRepDet
     *            the expense report detail.
     * @return whether the expense report detail report is missing receipts.
     */
    private int isMissingImageOrReceipt(ExpenseReportDetail expRepDet) {
        ArrayList<ExpenseReportEntry> entriesMissingImages = null;
        ArrayList<ExpenseReportEntry> entriesMissingReceipts = null;
        if (expRepDet.isImageRequired()) {
            if (expRepDet.getExpenseEntries() != null) {
                Iterator<ExpenseReportEntry> expRepEntIter = expRepDet.getExpenseEntries().iterator();
                while (expRepEntIter.hasNext()) {
                    ExpenseReportEntry expRepEnt = expRepEntIter.next();
                    if (expRepEnt.isImageRequired()
                            && (expRepEnt.eReceiptId == null || expRepEnt.eReceiptId.length() == 0)
                            && !expRepEnt.hasMobileReceipt()
                            && (expRepEnt.receiptImageId == null || expRepEnt.receiptImageId.length() == 0)) {
                        if (entriesMissingImages == null) {
                            entriesMissingImages = new ArrayList<ExpenseReportEntry>();
                        }
                        entriesMissingImages.add(expRepEnt);
                    }
                    if (expRepEnt.isReceiptRequired()) {
                        if (entriesMissingReceipts == null) {
                            entriesMissingReceipts = new ArrayList<ExpenseReportEntry>();
                        }
                        entriesMissingReceipts.add(expRepEnt);
                    }
                }
                if (entriesMissingImages != null) {
                    recReqExpList = entriesMissingImages;
                    return IMG_REQ;
                } else if (entriesMissingReceipts != null) {
                    recReqExpList = entriesMissingReceipts;
                    return REC_REQ;
                }
            }
        }
        return NO_IMG_OR_REC_REQ;
    }

    /**
     * Will set information on a report header view from <code>expRep</code>.
     * 
     * @param expRep
     *            the expense report.
     */
    protected void populateReportHeaderInfo(ExpenseReport expRep) {

        // Set the report name.
        TextView txtView = (TextView) findViewById(R.id.report_name);
        if (txtView != null) {
            if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                txtView.setText(expRep.employeeName);
            } else {
                txtView.setText((expRep.reportName != null) ? expRep.reportName : "");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report name text view!");
        }

        // Set the report date.
        txtView = (TextView) findViewById(R.id.report_date);
        if (txtView != null) {
            if (expRep.reportDateCalendar != null) {
                txtView.setText(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(expRep.reportDateCalendar.getTime()));
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report date text view!");
        }

        // Set the report status.
        txtView = (TextView) findViewById(R.id.report_status);
        if (txtView != null) {
            if (expRep.apvStatusName != null) {
                if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
                    StringBuilder strBldr = new StringBuilder();
                    strBldr.append("  -  ");
                    strBldr.append(expRep.reportName);
                    txtView.setText(strBldr.toString());
                } else {
                    StringBuilder strBldr = new StringBuilder();
                    strBldr.append("  -  ");
                    strBldr.append(expRep.apvStatusName);
                    txtView.setText(strBldr.toString());
                }
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report status text view!");
        }

        // Set the report approver name.
        txtView = (TextView) findViewById(R.id.approver_name);
        if (txtView != null) {
            // Check for report submitted and defined approver name.
            if (expRep.isSubmitted()) {
                if (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE
                        || reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW) {
                    String apprName = expRep.aprvEmpName;
                    if (apprName == null || apprName.length() == 0) {
                        // NOTE: As of 6/19/2012, approver name may not be in detailed report. So, attempt
                        // to obtain approver name from summary report.
                        // AVK.
                        ConcurCore concurCore = getConcurCore();
                        IExpenseReportCache expRepCache = concurCore.getExpenseActiveCache();
                        if (expRepCache != null) {
                            ExpenseReport sumRep = expRepCache.getReport(expRep.reportKey);
                            if (sumRep != null && sumRep.aprvEmpName != null && sumRep.aprvEmpName.length() > 0) {
                                apprName = sumRep.aprvEmpName;
                            }
                        }
                    }
                    if (apprName != null && apprName.length() > 0) {
                        txtView.setText(com.concur.mobile.base.util.Format.localizeText(this,
                                R.string.general_approver, apprName));
                        txtView.setVisibility(View.VISIBLE);
                    } else {
                        txtView.setVisibility(View.GONE);
                    }
                } else {
                    txtView.setVisibility(View.GONE);
                }
            } else {
                txtView.setVisibility(View.GONE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate 'approver_name' field!");
        }

        // Set the report amount.
        txtView = (TextView) findViewById(R.id.report_amount);
        if (txtView != null) {
            if (expRep.totalPersonalAmount != null) {
                txtView.setText(FormatUtil.formatAmount(expRep.totalClaimedAmount,
                        getResources().getConfiguration().locale, expRep.crnCode, true, true));
            } else {
                txtView.setText("");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report amount text view!");
        }

        // Set the report status icons.
        // Exception icon.
        ImageView imgView = (ImageView) findViewById(R.id.report_exception_icon);
        if (imgView != null) {
            switch (ViewUtil.getExpenseReportExceptionSeverityLevel(expRep)) {
            case NONE:
                imgView.setVisibility(View.GONE);
                break;
            case WARN:
                imgView.setImageResource(R.drawable.icon_yellowex);
                imgView.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                imgView.setImageResource(R.drawable.icon_redex);
                imgView.setVisibility(View.VISIBLE);
                break;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report receipt image view!");
        }
        // Receipt icon.
        imgView = (ImageView) findViewById(R.id.report_receipt_icon);
        if (imgView != null) {
            if (!expRep.isReceiptImageAvailable()) {
                imgView.setVisibility(View.GONE);
            } else {
                imgView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report exception image view!");
        }
        // Ready to Submit icon
        imgView = (ImageView) findViewById(R.id.report_readytosubmit);
        if (imgView != null) {
            if (!expRep.isReadyToSubmit()) {
                imgView.setVisibility(View.GONE);
            } else {
                imgView.setVisibility(View.VISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateReportHeaderInfo: unable to locate report exception image view!");
        }
    }

    /**
     * Will populate the expense list header with information such as employee name, report name, etc. If the intent that launched
     * this activity contains a value for <code>Const.EXPENSE_PARENT_REPORT_ENTRY_KEY</code>, then the header will reflect the
     * expense name and amount only.
     * 
     * @param expRep
     *            the expense report.
     */
    protected void populateExpenseTitleHeaderInfo(ExpenseReport expRep) {

        if (expRep != null) {
            ExpenseReportEntry parentExpenseEntry = null;
            String parentEntryKey = getIntent().getExtras().getString(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY);
            if (parentEntryKey != null) {
                parentExpenseEntry = expRepCache.getReportEntry(expRep, parentEntryKey);
                if (parentExpenseEntry == null) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG
                                    + ".populateExpenseTitleHeader: unable to find report parent expense entry passed in intent!");
                }
            }
            TextView txtView = null;
            if (parentExpenseEntry == null) {
                // Set the report name.
                txtView = (TextView) findViewById(R.id.expense_list_report_name);
                if (txtView != null) {
                    txtView.setText(expRep.reportName);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate report name text view!");
                }
            } else {
                // Hide the report name.
                txtView = (TextView) findViewById(R.id.expense_list_report_name);
                if (txtView != null) {
                    txtView.setVisibility(View.INVISIBLE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate report name text view!");
                }
            }
            if (parentExpenseEntry == null) {
                // Set the report employee name.
                txtView = (TextView) findViewById(R.id.expense_list_employee_name);
                if (txtView != null) {
                    txtView.setText(FormatUtil.formatEmployeeName(expRep.employeeName));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate employee name text view!");
                }
            } else {
                // Set the employee name to the expense name.
                txtView = (TextView) findViewById(R.id.expense_list_employee_name);
                if (txtView != null) {
                    txtView.setText(parentExpenseEntry.expenseName);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate employee name text view!");
                }
                // Change the name from 'Report Total' to 'Entry Total'.
                txtView = (TextView) findViewById(R.id.expense_list_report_total);
                if (txtView != null) {
                    txtView.setText(R.string.entry_total);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate 'expense_list_report_total' text view!");
                }
            }
            // Set the report total amount.
            Double amount = null;
            String currency = null;
            if (parentExpenseEntry != null) {
                amount = parentExpenseEntry.transactionAmount;
                currency = parentExpenseEntry.transactionCrnCode;
            } else {
                amount = expRep.totalClaimedAmount;
                currency = expRep.crnCode;
            }
            final Locale locale = getResources().getConfiguration().locale;
            String formattedAmount = FormatUtil.formatAmount(amount, locale, currency, true);
            if (formattedAmount != null) {
                txtView = (TextView) findViewById(R.id.expense_list_report_total_amount);
                if (txtView != null) {
                    txtView.setText(formattedAmount);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeader: unable to locate total amount text view!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseTitleHeaderInfo: unable to format '" + amount
                        + "' for currency code '" + currency + "'.");
            }

            // Handle whether to display exception icons.
            if (showExceptionIconInHeader()) {
                if (parentExpenseEntry == null) {
                    ImageView imgView = (ImageView) findViewById(R.id.expense_list_header_exception_icon);
                    if (imgView != null) {
                        switch (ViewUtil.getExpenseReportExceptionSeverityLevel(expRep)) {
                        case NONE:
                            imgView.setVisibility(View.GONE);
                            break;
                        case WARN:
                            imgView.setImageResource(R.drawable.warning_24);
                            imgView.setVisibility(View.VISIBLE);
                            break;
                        case ERROR:
                            imgView.setImageResource(R.drawable.alert_24);
                            imgView.setVisibility(View.VISIBLE);
                            break;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".populateExpenseTitleHeaderInfo: missing report level exception icon image view.");
                    }
                } else {
                    ImageView imgView = (ImageView) findViewById(R.id.expense_list_header_exception_icon);
                    if (imgView != null) {
                        switch (ViewUtil.getExpenseEntryExceptionSeverityLevel(parentExpenseEntry)) {
                        case NONE:
                            imgView.setVisibility(View.GONE);
                            break;
                        case WARN:
                            imgView.setImageResource(R.drawable.warning_24);
                            imgView.setVisibility(View.VISIBLE);
                            break;
                        case ERROR:
                            imgView.setImageResource(R.drawable.alert_24);
                            imgView.setVisibility(View.VISIBLE);
                            break;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".populateExpenseTitleHeaderInfo: missing report level exception icon image view.");
                    }
                }
            } else {
                // Ensure we hide the exception icon.
                ImageView imgView = (ImageView) findViewById(R.id.expense_list_header_exception_icon);
                if (imgView != null) {
                    imgView.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".populateExpenseTitleHeaderInfo: missing report level exception icon image view.");
                }
            }

            // Display the report-level icon in the title header based on the existence of exceptions and they're
            // max severity level.
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseTitleHeader: expense report is null!");
        }

    }

    /**
     * Whether the exception icon should be displayed in the title header.
     * 
     * @return whether the exception icon should be displayed in the header.
     */
    protected boolean showExceptionIconInHeader() {
        return false;
    }

    /**
     * Will populate <code>excGroup</code> with information from <code>excList</code>.
     * 
     * @param excList
     *            the list of exceptions.
     * @param excGroup
     *            the view group to contain the exceptions.
     */
    protected void populateExceptionViewGroup(List<ExpenseReportException> excList, ViewGroup excGroup) {

        if (excList != null) {
            ListIterator<ExpenseReportException> excIter = excList.listIterator();
            LayoutInflater inflater = LayoutInflater.from(this);
            while (excIter.hasNext()) {
                ExpenseReportException expRepExc = excIter.next();
                ViewGroup excRow = (ViewGroup) inflater.inflate(R.layout.expense_exception_row, null);
                // Set the icon resource.
                ImageView imgView = (ImageView) excRow.findViewById(R.id.exception_icon);
                if (imgView != null) {
                    if (expRepExc.getSeverityLevel() != null) {
                        if (expRepExc.getSeverityLevel().equalsIgnoreCase("error")) {
                            imgView.setImageResource(R.drawable.icon_redex);
                        } else if (expRepExc.getSeverityLevel().equalsIgnoreCase("warn")
                                || expRepExc.getSeverityLevel().equalsIgnoreCase("warning")) {
                            imgView.setImageResource(R.drawable.icon_yellowex);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExceptionViewGroup: unrecognized error level '"
                                    + expRepExc.getSeverityLevel() + "'.");
                            imgView.setVisibility(View.GONE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".populateExceptionViewGroup: null exception severity level!");
                        imgView.setVisibility(View.GONE);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExceptionViewGroup: unable to locate exception icon view!");
                }
                // Set the exception text.
                TextView txtView = (TextView) excRow.findViewById(R.id.exception_text);
                if (txtView != null) {
                    txtView.setText(expRepExc.getException());
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".populateExceptionViewGroup: unable to locate exception text view!");
                }
                excGroup.addView(excRow);
                if (excIter.hasNext()) {
                    ViewUtil.addSeparatorView(this, excGroup);
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExceptionViewGroup: null exception list!");
        }
    }

    /**
     * Builds a view object that consists of an icon, text and a pull-right icon layed out horizontally in that order.
     * 
     * @param layoutResId
     *            the resource id the layout to inflate.
     * @param titleFieldResId
     *            the resource id of the title field within the layout.
     * @param strResId
     *            the resource id of the string title.
     * @param iconResId
     *            the resource id of the icon.
     * @param iconFldResId
     *            the resource id of the image view to contain the icon.
     * 
     * @return an instance of <code>View</code> containing the title and icon.
     */
    protected View buildIconTextPullRightView(int layoutResId, int titleFieldResId, int strResId, int iconResId,
            int iconFldResId) {

        View view = null;

        // Grab a references to the application resources.
        Resources resources = getResources();

        // Inflate the view.
        LayoutInflater inflater = LayoutInflater.from(this);
        view = inflater.inflate(layoutResId, null);

        // Grab header title text string and set it on the view.
        try {
            String headerTitle = resources.getString(strResId);
            TextView txtView = (TextView) view.findViewById(titleFieldResId);
            txtView.setText(headerTitle);
        } catch (Resources.NotFoundException resNotFoundExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildIconTextPullRightView: ", resNotFoundExc);
        }

        // Grab a reference to the header icon and set it on the view.
        try {
            Drawable headerIcon = resources.getDrawable(iconResId);
            ImageView imgView = (ImageView) view.findViewById(iconFldResId);
            imgView.setImageDrawable(headerIcon);
        } catch (Resources.NotFoundException resNotFoundExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildIconTextPullRightView: ", resNotFoundExc);
        }

        return view;

    }

    /**
     * Will populate a view group with comment information.
     * 
     * @param viewGroup
     *            the view group to be populated.
     * @param comRowLayoutResId
     *            the layout file resource id.
     * @param comTxtFldResId
     *            the comment text field resource id.
     * @param comList
     *            the comment list.
     */
    protected void populateCommentViewGroup(ViewGroup viewGroup, int comRowLayoutResId, int comTxtFldResId,
            List<ExpenseReportComment> comList) {

        if (viewGroup != null) {
            if (comList != null) {
                LayoutInflater inflater = LayoutInflater.from(this);
                Iterator<ExpenseReportComment> comIter = comList.iterator();
                while (comIter.hasNext()) {
                    ExpenseReportComment expCom = comIter.next();
                    // Inflate the view.
                    View view = inflater.inflate(comRowLayoutResId, null);
                    if (view != null) {
                        TextView txtView = (TextView) view.findViewById(comTxtFldResId);
                        if (txtView != null) {
                            txtView.setText(expCom.getComment());
                            ViewUtil.addSeparatorView(this, viewGroup);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".populateCommentViewGroup: unable to find comment text field view!");
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".populateCommentViewGroup: unable to inflate layout file!");
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".populateCommentViewGroup: null comment list!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateCommentViewGroup: null view group!");
        }
    }

    /**
     * Will populate a view group with expense report entry detail information.
     * 
     * @param viewGroup
     *            the view group to be populated.
     * @param expRepEntDet
     *            the expense report entry detail.
     */
    protected List<FormFieldView> populateExpenseDetailViewGroup(ViewGroup viewGroup,
            ExpenseReportEntryDetail expRepEntDet) {

        List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        if (viewGroup != null) {
            if (!isReportEditable()) {
                List<ExpenseReportFormField> expRepFrmFlds = expRepEntDet.getFormFields();
                if (expRepFrmFlds != null) {
                    // Ensure any form field with an access type of 'RW' is set to 'RO'.
                    for (ExpenseReportFormField expRepFrmFld : expRepFrmFlds) {
                        if (expRepFrmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
                            expRepFrmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                        }
                    }
                }
                frmFldViews.addAll(populateViewWithFormFields(viewGroup, expRepEntDet.getFormFields(), null));
            } else {
                frmFldViews.addAll(populateViewWithFormFields(viewGroup, expRepEntDet.getFormFields(), null));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetailViewGroup: null view group!");
        }

        return frmFldViews;
    }

    /**
     * Will populate a view group with expense report entry detail information.
     * 
     * @param viewGroup
     *            the view group to be populated.
     * @param expRepEntDet
     *            the expense report entry detail.
     */
    protected List<FormFieldView> populateTaxFormFieldsViewGroup(ViewGroup viewGroup,
            ExpenseReportEntryDetail expRepEntDet) {

        List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        if (viewGroup != null) {
            ViewUtil.addSeparatorView(this, viewGroup);
            List<ExpenseReportFormField> resultTaxFrmFlds = new ArrayList<ExpenseReportFormField>();
            List<TaxForm> listOfTaxForms = expRepEntDet.getTaxForm();
            if (listOfTaxForms != null && listOfTaxForms.size() > 0) {
                for (TaxForm taxForm : listOfTaxForms) {
                    List<ExpenseReportFormField> formFieldTax = taxForm.taxFormField;
                    if (formFieldTax != null && formFieldTax.size() > 0) {
                        resultTaxFrmFlds.addAll(resultTaxFrmFlds.size(), formFieldTax);
                    }
                }
            }
            if (!isReportEditable()) {
                if (resultTaxFrmFlds != null) {
                    // Ensure any form field with an access type of 'RW' is set to 'RO'.
                    for (ExpenseReportFormField taxFrmFld : resultTaxFrmFlds) {
                        if (taxFrmFld.getAccessType() == ExpenseReportFormField.AccessType.RW) {
                            taxFrmFld.setAccessType(ExpenseReportFormField.AccessType.RO);
                        }
                    }
                }
                frmFldViews.addAll(populateViewWithFormFields(viewGroup, resultTaxFrmFlds, null));
            } else {
                frmFldViews.addAll(populateViewWithFormFields(viewGroup, resultTaxFrmFlds, null));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateExpenseDetailViewGroup: null view group!");
        }

        return frmFldViews;
    }

    protected void ensureTwoVendorNameFields(List<ExpenseReportFormField> expRepFrmFlds) {

    }

    /**
     * Will update an expense entry view with <code>expRepEntry</code>.
     * 
     * @param view
     *            the expense entry view.
     * @param expRepEntry
     *            the expense entry.
     */
    protected void updateExpenseEntryRowView(View view, ExpenseReportEntry expRepEntry) {

        // Set the expense type.
        TextView txtView = (TextView) view.findViewById(R.id.transaction_type);
        txtView.setText(getReportEntryExpenseName(expRepEntry));

        // Set the expense date.
        txtView = (TextView) view.findViewById(R.id.transaction_date);
        if (expRepEntry.reportEntryKey != null && expRepEntry.reportEntryKey.length() > 0) {
            txtView.setText(expRepEntry.getFormattedTransactionDate());
        } else {
            // New entries should have their dates formatted using a device local date formatter.
            txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL,
                    expRepEntry.transactionDateCalendar));
        }

        // Set the vendor description and location.
        txtView = (TextView) view.findViewById(R.id.transaction_description);
        txtView.setText((expRepEntry.vendorDescription != null) ? expRepEntry.vendorDescription : "");

        // Set the expense transaction amount and currency code character.
        txtView = (TextView) view.findViewById(R.id.transaction_amount);
        final Locale locale = getResources().getConfiguration().locale;
        double transAmtDbl = getTransactionAmount(expRepEntry);
        String transCurCode = getTransactionCurrencyCode(expRepEntry);
        String formattedAmount = FormatUtil.formatAmount(transAmtDbl, locale, transCurCode, true, true);
        if (formattedAmount != null) {
            txtView.setText(formattedAmount);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildExpenseEntryRowView: unable to format transaction amount of '"
                    + expRepEntry.transactionAmount + "'.");
        }

        // Show/Hide credit card icon.
        ImageView imgView = (ImageView) view.findViewById(R.id.expense_entry_card_icon);
        if (expRepEntry.isCreditCardCharge() || expRepEntry.isPersonalCardCharge()) {
            imgView.setVisibility(View.VISIBLE);
        } else {
            imgView.setVisibility(View.GONE);
        }

        // Show/hide receipt icon.
        imgView = (ImageView) view.findViewById(R.id.expense_entry_receipt_icon);

        if ((expRepEntry.hasEReceiptImageId()) || (!TextUtils.isEmpty(expRepEntry.receiptImageId))) {
            imgView.setVisibility(View.VISIBLE);
        } else {
            imgView.setVisibility(View.GONE);
        }

        // Show/hide exception icon.
        imgView = (ImageView) view.findViewById(R.id.expense_entry_exception_icon);
        if (imgView != null) {
            switch (ViewUtil.getExpenseEntryExceptionSeverityLevel(expRepEntry)) {
            case NONE:
                // No exceptions so just hide the icon.
                imgView.setVisibility(View.GONE);
                break;
            case WARN:
                imgView.setImageResource(R.drawable.icon_yellowex);
                imgView.setVisibility(View.VISIBLE);
                break;
            case ERROR:
                imgView.setImageResource(R.drawable.icon_redex);
                imgView.setVisibility(View.VISIBLE);
                break;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".updateExpenseEntryRowView: unable to locate expense entry exception icon.");
        }
    }

    /**
     * Gets the transaction amount.
     * 
     * @return a transaction amount.
     */
    protected double getTransactionAmount(ExpenseReportEntry expRepEntry) {
        double transAmt = (expRepEntry.transactionAmount != null) ? expRepEntry.transactionAmount : 0.0D;
        return transAmt;
    }

    /**
     * Gets the transaction currency code.
     * 
     * @return the transaction currency code.
     */
    protected String getTransactionCurrencyCode(ExpenseReportEntry expRepEntry) {
        String transCurCode = expRepEntry.transactionCrnCode;
        return transCurCode;
    }

    /**
     * Will build an expense entry row view object.
     * 
     * @param expRepEnt
     *            the expense entry.
     * 
     * @return a view depicting an expense entry.
     */
    protected View buildExpenseEntryRowView(ExpenseReportEntry expRepEntry) {

        View view = null;

        if (expRepEntry != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            view = inflater.inflate(R.layout.report_expense_row, null);
            updateExpenseEntryRowView(view, expRepEntry);
        }
        return view;
    }

    /**
     * Gets the report entry key.
     * 
     * @return the report entry key.
     */
    protected String getReportEntryKey() {
        return null;
    }

    /**
     * Gets the report entry name.
     * 
     * @return the report entry name.
     */
    protected String getReportEntryName() {
        return null;
    }

    /**
     * Gets the report entry name.
     * 
     * @return the report entry name.
     */
    protected String getFormattedReportEntryAmount() {
        return null;
    }

    /**
     * Gets the report entry receipt image id.
     * 
     * @return the report entry receipt image id.
     */
    protected String getReportEntryReceiptImageId() {
        return null;
    }

    /**
     * Will get the expense type name for the report entry.
     * 
     * @param expRepEntry
     *            the expense report entry.
     * @return the expense type name for the report entry.
     */
    protected String getReportEntryExpenseName(ExpenseReportEntry expRepEntry) {
        return expRepEntry.expenseName;
    }

    /**
     * Sets a string value in a text view contained within a parent view.
     * 
     * @param viewResId
     *            the resource id of the parent view.
     * @param txtViewResId
     *            the resource id of the text view within the parent view.
     * @param value
     *            the string value to set.
     */
    protected void setFieldValue(int viewResId, int txtViewResId, String value) {

        if (value != null) {
            // Obtain a reference to the view.
            View view = findViewById(viewResId);
            if (view != null) {
                setFieldValue(view, txtViewResId, value);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldValue: unable to locate view by resource id '" + viewResId
                        + "'.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldValue: null string value!");
        }
    }

    protected void setFieldValue(View fieldLayout, int txtViewResId, String value) {
        if (fieldLayout != null) {
            TextView txtView = (TextView) fieldLayout.findViewById(txtViewResId);
            if (txtView != null) {
                txtView.setText(value);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".setFieldValue: unable to locate text view by resource id '"
                        + txtViewResId + "'.");
            }
        }
    }

    /**
     * Will hide a view by setting its visibility state to 'View.GONE'.
     * 
     * @param viewId
     *            the android id associated with the view.
     */
    protected void hideView(int viewId) {
        // Obtain a reference to the view.
        View view = findViewById(viewId);
        if (view != null) {
            view.setVisibility(View.GONE);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".hidView: unable to locate view by resource id'" + viewId + "'.");
        }
    }

    /**
     * Will populate <code>view</code> with form editing views and will return back a list of <code>FormFieldView</code> objects
     * that manage the created views.
     * 
     * @param viewGroup
     *            the view group to contain the created form fields.
     * @param frmFlds
     *            the list of form fields used to create views.
     * @param ignoreFldIds
     *            the list of form field ids that should be ignored.
     * @return a list of <code>FormFieldView</code> objects that manage the created form field views.
     */
    protected List<FormFieldView> populateViewWithFormFields(ViewGroup viewGroup, List<ExpenseReportFormField> frmFlds,
            List<String> ignoreFldIds) {
        return FormUtil.populateViewWithFormFields(this, viewGroup, frmFlds, ignoreFldIds, frmFldViewListener);
        // List<FormFieldView> frmFldViews = new ArrayList<FormFieldView>();
        // if (viewGroup != null) {
        // if (frmFlds != null) {
        // ListIterator<ExpenseReportFormField> frmFldIter = frmFlds.listIterator();
        // boolean addedFormFieldView = false;
        // while (frmFldIter.hasNext()) {
        // ExpenseReportFormField frmFld = frmFldIter.next();
        // // Check for non-hidden and non-ignored form field.
        // if ((ignoreFldIds == null) || (!ignoreFldIds.contains(frmFld.getId()))) {
        // // Construct the 'FormFieldView' object.
        // FormFieldView frmFldView = ViewUtil.buildFormFieldView(frmFld, frmFldViewListener);
        // if (frmFldView != null) {
        // frmFldViews.add(frmFldView);
        // if (frmFld.getAccessType() != ExpenseReportFormField.AccessType.HD) {
        // if (addedFormFieldView) {
        // // Add a separator and the view.
        // ViewUtil.addSeparatorView(this, viewGroup);
        // }
        // viewGroup.addView(frmFldView.getView(this));
        // addedFormFieldView = true;
        // }
        // } else {
        // Log.e(Const.LOG_TAG, CLS_TAG
        // + ".populateViewWithFormFields: unable to build 'FormFieldView' object for field '"
        // + frmFld.getLabel() + "'.");
        // }
        // }
        // }
        // }
        // } else {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFields: null view to populate!");
        // }
        // return frmFldViews;
    }

    /**
     * Will populate <code>view</code> with form fields from <code>expRepDet</code> ignoring field ids contained in
     * 
     * @param viewGroup
     * @param expRepDet
     * @param ignoreFldIds
     */
    protected void populateViewWithFields(ViewGroup viewGroup, List<ExpenseReportFormField> frmFlds,
            List<String> ignoreFldIds) {
        if (viewGroup != null) {
            if (frmFlds != null) {
                LayoutInflater inflater = LayoutInflater.from(this);
                ListIterator<ExpenseReportFormField> frmFldIter = frmFlds.listIterator();
                while (frmFldIter.hasNext()) {
                    int frmFldInd = frmFldIter.nextIndex();
                    ExpenseReportFormField frmFld = frmFldIter.next();

                    // Check if field is hidden by the ctrlType
                    if (frmFld.getControlType() == ExpenseReportFormField.ControlType.HIDDEN) {
                        frmFld.setAccessType(ExpenseReportFormField.AccessType.HD);
                    }
                    // Check for non-hidden and non-ignored form field.
                    if ((frmFld.getAccessType() != ExpenseReportFormField.AccessType.HD)
                            && ((ignoreFldIds == null) || (!ignoreFldIds.contains(frmFld.getId())))) {
                        // Inflate the new form field cell.
                        View formFldView = inflater.inflate(R.layout.static_text_form_field, null);
                        // Set the field label.
                        TextView txtView = (TextView) formFldView.findViewById(R.id.field_name);
                        if (txtView != null) {
                            txtView.setText(frmFld.getLabel());
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFields: null field label view!");
                        }
                        // Set the field value.
                        txtView = (TextView) formFldView.findViewById(R.id.field_value);
                        if (txtView != null) {
                            String value = frmFld.getValue();
                            if (frmFld.isTimestampField()) {
                                value = frmFld.formatTimestampValue(FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY);
                            }
                            txtView.setText(value);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFields: null field value view!");
                        }
                        if (frmFldInd > 0) {
                            // Add a separator and the view.
                            ViewUtil.addSeparatorView(this, viewGroup);
                        }
                        viewGroup.addView(formFldView);
                    }
                }
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".populateViewWithFields: null view to populate!");
        }
    }

    /**
     * Will build a view
     * 
     * @param expRepDet
     * @return
     */
    protected View buildCommentView(ExpenseReportComment expRepComment) {

        View commentView = null;
        if (expRepComment != null) {
            LayoutInflater inflater = LayoutInflater.from(this);
            commentView = inflater.inflate(R.layout.expense_comment_entry, null);
            // Set the comment author.
            TextView txtView = (TextView) commentView.findViewById(R.id.comment_author);
            if (txtView != null) {
                String authorStr = FormatUtil.formatEmployeeName(expRepComment.getCommentBy());
                if (authorStr == null)
                    authorStr = "";
                txtView.setText(authorStr);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildCommentView: unable to find comment author text view!");
            }
            // Set the comment date.
            txtView = (TextView) commentView.findViewById(R.id.comment_date);
            if (txtView != null) {
                txtView.setText(expRepComment.getFormattedCreationDate());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildCommentView: unable to find comment date text view!");
            }
            txtView = (TextView) commentView.findViewById(R.id.comment_text);
            if (txtView != null) {
                String commentText = expRepComment.getComment();
                if (commentText == null)
                    commentText = "";
                txtView.setText(commentText);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildCommentView: unable to find comment copy text view!");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildCommentView: null report comment!");
        }
        return commentView;
    }

    /**
     * Captures a receipt image to associate with the currently selected report entry.
     */
    protected void captureReportEntryReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Create a place for the camera to write its output.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, CLS_TAG + ".captureReportEntryReceipt: receipt image path -> '"
                    + receiptCameraImageDataLocalFilePath + "'.");
            // Launch the camera application.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            try {
                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (Exception e) {
                // Device has no camera, see MOB-16872
            }
            clearSavingReceiptFlags();
            savingExpenseReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Captures a receipt image to associate with the currently selected report entry.
     */
    protected void captureReportReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // Create a place for the camera to write its output.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, CLS_TAG + ".captureReportEntryReceipt: receipt image path -> '"
                    + receiptCameraImageDataLocalFilePath + "'.");
            // Launch the camera application.
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            try {
                startActivityForResult(intent, REQUEST_TAKE_PICTURE);
            } catch (Exception e) {
                // Device has no camera, see MOB-16872
            }
            clearSavingReceiptFlags();
            savingReportReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Selects a receipt image to associate with the currently selected report entry.
     */
    protected void selectReportEntryReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
            clearSavingReceiptFlags();
            savingExpenseReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Selects a receipt image to associate with the currently selected report entry.
     */
    protected void selectReportReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
            clearSavingReceiptFlags();
            savingReportReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Selects a receipt image from one in the receipt store cloud for use at the report level.
     */
    protected void selectCloudReportReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(this, ExpensesAndReceipts.class);
            intent.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
            intent.setAction(Intent.ACTION_PICK);
            intent.putExtra(Const.EXTRA_EXPENSE_SELECT_REPORT_RECEIPT_KEY, true);
            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_NAME, expRep.reportName);
            intent.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_REPORT);
            startActivityForResult(intent, REQUEST_CHOOSE_CLOUD_IMAGE);
            clearSavingReceiptFlags();
            savingReportReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Selects a receipt image from one in the receipt store cloud for use at the report entry level.
     */
    protected void selectCloudReportEntryReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(this, ExpensesAndReceipts.class);
            intent.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
            intent.putExtra(Const.EXTRA_EXPENSE_SELECT_ENTRY_RECEIPT_KEY, true);
            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, getReportEntryKey());
            intent.putExtra(Const.EXTRA_EXPENSE_EXPENSE_NAME, getReportEntryName());
            intent.putExtra(Const.EXTRA_EXPENSE_EXPENSE_AMOUNT, getFormattedReportEntryAmount());
            intent.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_REPORT_ENTRY);
            intent.setAction(Intent.ACTION_PICK);
            startActivityForResult(intent, REQUEST_CHOOSE_CLOUD_IMAGE);
            clearSavingReceiptFlags();
            savingExpenseReceipt = true;
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.ConcurView#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (!buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view present, handling result.");
            // Check whether a form field view should handle the activity result.
            if (frmFldViewListener != null && frmFldViewListener.isCurrentFormFieldViewSet()) {
                frmFldViewListener.getCurrentFormFieldView().onActivityResult(requestCode, resultCode, data);
            } else {
                if (requestCode == REQUEST_TAKE_PICTURE) {
                    if (resultCode == Activity.RESULT_OK) {
                        // This flag is always set to 'true' for captured pictures.
                        deleteReceiptImageDataLocalFilePath = true;
                        if (copyCapturedImage()) {
                            // Set the receipt action flag.
                            receiptSaveAction = ReceiptPictureSaveAction.TAKE_PICTURE;

                            if (isSavingReportReceipt()) {
                                boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
                                if (useConnect) {
                                    sendConnectPostImageReportRequest(receiptImageDataLocalFilePath);
                                } else {
                                    sendAddReportReceiptV2Request(receiptImageDataLocalFilePath);
                                }
                            } else {
                                boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
                                registerSaveReceiptReceiver();
                                // Send request.
                                ConcurCore ConcurCore = (ConcurCore) getApplication();
                                ConcurService concurService = ConcurCore.getService();
                                if (useConnect) {
                                    saveReceiptRequest = concurService.sendConnectPostImageRequest(getUserId(),
                                            receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null,
                                            false);
                                } else {
                                    saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(),
                                            receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null,
                                            false);
                                }
                                if (saveReceiptRequest != null) {
                                    saveReceiptReceiver.setRequest(saveReceiptRequest);
                                    showDialog(getSavingReceiptDialogId());
                                } else {
                                    Log.w(Const.LOG_TAG, CLS_TAG
                                            + ".onActivityResult(TakePicture): unable to create 'SaveReceiptRequest'!");
                                    unregisterSaveReceiptReceiver();
                                }
                            }
                        } else {
                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE,
                                    Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                            showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                        }
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        // Treat canceling taking a photo as canceling the action.
                        Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(TakePicture): unhandled result code '"
                                + resultCode + "'.");
                    }
                } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
                    if (resultCode == Activity.RESULT_OK) {
                        if (copySelectedImage(data)) {
                            // Set the receipt action flag.
                            receiptSaveAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;
                            if (isSavingReportReceipt()) {
                                boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
                                if (useConnect) {
                                    sendConnectPostImageReportRequest(receiptImageDataLocalFilePath);
                                } else {
                                    sendAddReportReceiptV2Request(receiptImageDataLocalFilePath);
                                }
                            } else {
                                boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_RECEIPT_VIA_CONNECT_ENABLED);
                                registerSaveReceiptReceiver();
                                // Send request.
                                ConcurCore ConcurCore = (ConcurCore) getApplication();
                                ConcurService concurService = ConcurCore.getService();

                                if (useConnect) {
                                    saveReceiptRequest = concurService.sendConnectPostImageRequest(getUserId(),
                                            receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null,
                                            false);
                                } else {
                                    saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(),
                                            receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null,
                                            false);
                                }
                                if (saveReceiptRequest != null) {
                                    saveReceiptReceiver.setRequest(saveReceiptRequest);
                                    showDialog(getSavingReceiptDialogId());
                                } else {
                                    Log.w(Const.LOG_TAG,
                                            CLS_TAG
                                                    + ".onActivityResult(ChoosePicture): unable to create 'SaveReceiptRequest'!");
                                    unregisterSaveReceiptReceiver();
                                }
                            }
                        } else {
                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE,
                                    Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                            showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                        }
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        // Treat canceling choosing a photo as canceling the action.
                        Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChoosePicture): unhandled result code '"
                                + resultCode + "'.");
                    }
                } else if (requestCode == REQUEST_CHOOSE_CLOUD_IMAGE) {
                    if (resultCode == Activity.RESULT_OK) {
                        String receiptImageId = data.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                        if (receiptImageId != null) {
                            // Set the receipt action flag.
                            receiptSaveAction = ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD;
                            // Set the flag indicating receipt selection from the receipt store.
                            refreshReceiptStore = true;
                            showDialog(getSavingReceiptDialogId());
                            // Handle the post-action of saving a receipt to the cloud.
                            handlePostReceiptSave(receiptImageId);
                        } else {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG
                                            + ".onActivityResult(ChooseCloudPicture): ok result intent missing receipt image id!");
                        }
                    } else if (resultCode == Activity.RESULT_CANCELED) {
                        // Treat canceling choosing a photo as canceling the action.
                        Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChooseCloudPicture): unhandled result code '"
                                + resultCode + "'.");
                    }
                } else if (requestCode == REQUEST_VIEW_ENTRY_DETAILS || requestCode == REQUEST_VIEW_SUMMARY
                        || requestCode == REQUEST_VIEW_ITEMIZATIONS) {
                    // Indicates an expense entry was viewed.
                    if (resultCode == Activity.RESULT_OK) {
                        Intent intent = getIntent();
                        if (data != null) {
                            boolean refreshHeader = data.getBooleanExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, false);
                            // Check whether the activity returning the result detected an application re-start.
                            // If so, then do not call 'setExpenseReport' as the activity re-creation will take
                            // care of the screen re-fresh.
                            if (!data.getBooleanExtra(Const.EXTRA_APP_RESTART, false)) {
                                if (!refreshHeader) {
                                    intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, data.getBooleanExtra(
                                            Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.FALSE));
                                    setExpenseReport(intent);
                                } else {
                                    sendReportHeaderDetailRequest();
                                }
                            }
                        } else {
                            setExpenseReport(intent);
                        }
                    }
                } else if (requestCode == Const.SEARCH_APPROVER) {
                    // Indicates the user selected an approver.
                    if (resultCode == Activity.RESULT_OK) {
                        if (data != null) {
                            // Perform the submit process again, but with the selected approver.
                            ExpenseReportApprover approver = (ExpenseReportApprover) data
                                    .getSerializableExtra(Const.EXTRA_EXPENSE_REPORT_SELECTED_APPROVER);
                            handleSubmitReport(approver);
                        }
                    } else if (resultCode == Activity.RESULT_CANCELED) {

                        Toast toast = Toast
                                .makeText(this, getText(R.string.report_submit_canceled), Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(SearchAPprover): unhandled result code '"
                                + resultCode + "'.");
                    }
                } else if (requestCode == Const.REQUEST_CODE_ADD_EXPENSES) {
                    // Indicates the end-user has added expenses to this report via expense selection
                    // rather than defining a new expense.
                    if (resultCode == Activity.RESULT_OK) {
                        Intent intent = getIntent();
                        if (data != null) {
                            intent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE,
                                    data.getBooleanExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, Boolean.FALSE));
                        }
                        setExpenseReport(intent);
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
     * Gets whether after a report entry receipt image update, whether a request should be sent to refresh the report.
     * 
     * @return returns <code>true</code> if a request to update the report header should be made; <code>false</code> otherwise. If
     *         this method returns <code>false</code> then sub-classes will typically just re-build the view.
     */
    protected boolean refreshReportHeaderAfterEntryReceiptUpdate() {
        boolean retVal = true;
        return retVal;
    }

    /**
     * Gets any currently selected expense report entry.
     * 
     * @return any currently selected expense report entry.
     */
    protected ExpenseReportEntry getSelectedExpenseReportEntry() {
        return null;
    }

    /**
     * Sets the currently selected expense report entry.
     * 
     * @param expRepEnt
     *            the currently selected expense report entry.
     */
    protected void setSelectedExpenseReportEntry(ExpenseReportEntry expRepEnt) {
        // No-op.
    }

    /**
     * Clears any currently selected expense report entry.
     */
    protected void clearSelectedExpenseReportEntry() {
        // no-op.
    }

    /**
     * Will construct an intent used to view the receipt for an expense entry.
     * 
     * @param reportKey
     *            the report key.
     * @param reportEntryKey
     *            the report entry key.
     * @return an instance of <code>Intent</code> used to view the expense entry receipt.
     */
    protected Intent buildExpenseEntryReceiptClickIntent(String reportKey, String reportEntryKey) {
        Intent clickIntent = null;
        clickIntent = new Intent(this, ExpenseReceipt.class);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, reportKey);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, reportEntryKey);
        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
        return clickIntent;
    }

    /**
     * Gets whether an expense receipt is being saved.
     * 
     * @return whether or not an expense receipt is being saved.
     */
    protected boolean isSavingExpenseReceipt() {
        return savingExpenseReceipt;
    }

    /**
     * Gets whether a report receipt is being saved.
     * 
     * @return whether or not a report receipt is being saved.
     */
    protected boolean isSavingReportReceipt() {
        return savingReportReceipt;
    }

    /**
     * Gets whether an expense represents an itemization.
     * 
     * @return whether the current expense represents an itemization list.
     */
    protected boolean isItemizationExpense() {
        return (getIntent().getExtras().containsKey(Const.EXTRA_EXPENSE_PARENT_REPORT_ENTRY_KEY));
    }

    /**
     * Gets whether receipt editing is supported for this report.
     * 
     * @return whether receipt editing is supported for this report.
     */
    protected boolean canEditReceipt() {
        return (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE)
                && (expRep.apsKey.equalsIgnoreCase("A_NOTF") || expRep.apsKey.equalsIgnoreCase("A_RESU") || expRep.apsKey
                        .equalsIgnoreCase("A_RHLD"));
    }

    /**
     * Gets whether this report is currently editable.
     * 
     * @return whether this report is currently editable.
     */
    protected boolean isReportEditable() {
        return ((reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE)
                && (expRep.apsKey.equalsIgnoreCase("A_NOTF") || expRep.apsKey.equalsIgnoreCase("A_RESU")) || (reportKeySource == Const.EXPENSE_REPORT_SOURCE_NEW));
    }

    /**
     * Determines whether the receipt dialog being displayed is at the report level.
     * 
     * @return returns <code>true</code> if the receipt dialog being presented is for report level receipts; otherwise
     *         <code>false</code> is returned.
     */
    protected boolean isReportLevelReceiptDialog() {
        return false;
    }

    /**
     * Determines whether an expense entry is a new expense.
     * 
     * @return whether or not the current expense entry is "new".
     */
    protected boolean isNewExpense() {
        return false;
    }

    /**
     * Determines whether the a e-receipt is associated with this expense
     * 
     * @return returns <code>true</code> if the expense has a e-receipt; otherwise <code>false</code> is returned.
     */
    protected boolean isReportEntryWithEReceipt() {
        return false;
    }

    /**
     * Clears any flags indicating report/expense receipts are being saved.
     */
    protected void clearSavingReceiptFlags() {
        savingExpenseReceipt = savingReportReceipt = false;
    }

    /**
     * Gets the ID of the current save receipt dialog.
     * 
     * @return the ID of the current save receipt dialog.
     */
    protected int getSavingReceiptDialogId() {
        int receiptDialogId = -1;
        if (isSavingExpenseReceipt()) {
            receiptDialogId = Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT;
        }
        if (isSavingReportReceipt()) {
            receiptDialogId = Const.DIALOG_EXPENSE_SAVE_REPORT_RECEIPT;
        }
        if (receiptDialogId == -1) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getSavingReceiptDialogId: id is -1.");
        }
        return receiptDialogId;
    }

    /**
     * This method will handle a post receipt save, meaning the receipt has already been saved and a receipt image id is provided.
     * The method will either associate the receipt image id at the report level or report entry level.
     * 
     * @param receiptImageId
     *            the receipt image id.
     */
    private void handlePostReceiptSave(String receiptImageId) {
        if (isSavingExpenseReceipt()) {
            if (getReportEntryReceiptImageId() != null) {
                // Since the report entry already has a receipt image ID, prompt the end-user as to whether
                // they want to append or replace the existing receipt.
                // Hang onto the receipt image ID to either append or replace.
                savingReceiptImageId = receiptImageId;
                showDialog(Const.DIALOG_EXPENSE_CONFIRM_RECEIPT_APPEND);
                // Dismiss the current saving receipt dialog.
                dismissDialog(getSavingReceiptDialogId());
            } else {
                // Since the report entry has no receipt image ID, just perform a request to save the report
                // entry receipt.
                sendSaveReportEntryReceiptRequest(receiptImageId);
            }
        } else if (isSavingReportReceipt()) {
            // If either take/choose picture action was chosen, then the picture had to be up-loaded
            // to the Receipt Store.
            if (receiptSaveAction == ReceiptPictureSaveAction.TAKE_PICTURE
                    || receiptSaveAction == ReceiptPictureSaveAction.CHOOSE_PICTURE) {
                // boolean useConnect = ((Preferences.getAccessToken() != null) && ADD_REPORT_RECEIPT_VIA_CONNECT_ENABLED);
                // if( !useConnect ) {
                // // If not using the V2 end-point to add a report receipt image, then
                // // send the request to associate the receipt image ID with the report.
                // if (!ADD_REPORT_RECEIPT_V2_ENABLED) {
                // // Add Report Receipt V2 end-point was not enabled; thus, a seperate call
                // // needs to be made to associate the receipt ID with the report.
                // sendAddReportReceiptRequest(receiptImageId);
                // } else {
                // // No-op: The V2 MWS end-point to associate a receipt image with a report
                // // both uploads the image and associates it with the report.
                // }
                // } else {
                // // No-op: The receipt was uploaded via Connect which will perform the receipt
                // // association with the report.
                // }
            } else if (receiptSaveAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD) {
                // A cloud selected receipt doesn't involve pushing image bits to the server, so
                // a call to associated the receipt image ID with the report is needed.
                sendAddReportReceiptRequest(receiptImageId);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: neither report or report entry receipt save is detected!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the results of report submission.
     * 
     * @author AndrewK
     */
    static class ReportApproveReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + ReportApproveReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the approve report request.
        private ApproveReportRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ReportApproveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportApproveReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportApproveRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report approve request object associated with this broadcast receiver.
         * 
         * @param request
         *            the approve report request object associated with this broadcast receiver.
         */
        void setRequest(ApproveReportRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterReportApproveReceiver();

                try {
                    // Dismiss the dialog.
                    activity.dismissDialog(Const.DIALOG_EXPENSE_APPROVE_REPORT_PROGRESS);
                } catch (IllegalArgumentException ilaExc) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                }

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    // Flurry Notification
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS,
                                            Flurry.EVENT_NAME_APPROVE_REPORT);

                                    // Punt any client-side detailed report information.
                                    ConcurCore app = (ConcurCore) activity.getApplication();
                                    app.getExpenseApprovalCache().deleteDetailReport(activity.expRep.reportKey);
                                    // Launch the 'ExpenseApproval' activity with the flag
                                    // 'Intent.FLAG_ACTIVITY_CLEAR_TOP' which will unwind the activity
                                    // stack and ensure 'ExpenseApproval' is on top of the stack.
                                    Intent approvalsIntent = new Intent(activity, Approval.class);
                                    ConcurCore ConcurCore = (ConcurCore) activity.getApplication();
                                    IExpenseReportCache expRepCache = ConcurCore.getExpenseApprovalCache();
                                    expRepCache.setShouldFetchReportList();
                                    approvalsIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    approvalsIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE,
                                            activity.reportKeySource);
                                    boolean approvalListUpdated = intent.getBooleanExtra(
                                            Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING, false);
                                    approvalsIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_TO_APPROVE_LIST_PENDING,
                                            approvalListUpdated);
                                    approvalsIntent.putExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, true);
                                    activity.startActivity(approvalsIntent);
                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    activity.showDialog(Const.DIALOG_EXPENSE_APPROVE_REPORT_FAILED);
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                activity.lastHttpErrorMessage = activity.getText(R.string.service_not_available)
                                        .toString();
                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Clear out the request reference.
                activity.reportApproveRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the results of report submission.
     * 
     * @author AndrewK
     */
    static class ReportSubmitReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + ReportSubmitReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the submit report request.
        private SubmitReportRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ReportSubmitReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportSubmitReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportSubmitRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report submit request object associated with this broadcast receiver.
         * 
         * @param request
         *            the submit report request object associated with this broadcast receiver.
         */
        void setRequest(SubmitReportRequest request) {
            this.request = request;
        }

        /**
         * Returns the intent that was passed to the receiver's 'onReceive' method.
         * 
         * @return the intent that was passed to the receiver's 'onReceive' method.
         */
        Intent getIntent() {
            return intent;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterReportSubmitReceiver();

                // Dismiss the dialog.
                activity.dismissDialog(Const.DIALOG_EXPENSE_SUBMITTING_REPORT);

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Show the dialog indicating the expense report has been submitted.
                                    activity.showDialog(Const.DIALOG_EXPENSE_SUBMIT_REPORT);

                                    // Flurry Notification
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_YES);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS, Flurry.EVENT_NAME_SUBMIT,
                                            params);

                                } else if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_REVIEW_APPROVAL_FLOW_APPROVER)
                                        || intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                                Const.REPLY_STATUS_NO_APPROVER)) {

                                    // Clear out the request reference.
                                    activity.reportSubmitRequest = null;

                                    // Need to handle when the user submitted a report and
                                    // they need to manually select an approver.
                                    // Invoke the Approver Search view.

                                    ApproverSearchDialogFragment ApproverSearchDialogFragment = new ApproverSearchDialogFragment();
                                    Bundle bundle = new Bundle();

                                    Intent newIntent = new Intent(activity, ApproverSearchDialogFragment.class);
                                    // Set the key of the report being submitted for approval.
                                    String reportKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_KEY);
                                    // newIntent.putExtra(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY, reportKey);
                                    bundle.putString(Const.EXTRA_EXPENSE_LIST_SEARCH_REPORT_KEY, reportKey);

                                    // If the response contained a default Approver, pass it along to the Approver Search screen.
                                    if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER)) {
                                        ExpenseReportApprover defaultApprover = (ExpenseReportApprover) intent
                                                .getSerializableExtra((Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER));
                                        // newIntent
                                        // .putExtra(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER, defaultApprover);
                                        bundle.putSerializable(Const.EXTRA_EXPENSE_REPORT_DEFAULT_APPROVER,
                                                defaultApprover);
                                    }
                                    // activity.startActivityForResult(newIntent, Const.SEARCH_APPROVER);

                                    ApproverSearchDialogFragment.setArguments(bundle);
                                    ApproverSearchDialogFragment
                                            .setOnDialogFragmentResultListener(new OnDialogFragmentResultListener() {

                                                @Override
                                                public void onDialogFragmentResult(int iResult, Intent intent) {
                                                    if (iResult == RESULT_OK) {
                                                        activity.onActivityResult(Const.SEARCH_APPROVER, iResult,
                                                                intent);
                                                    }
                                                }
                                            });
                                    ApproverSearchDialogFragment.show(activity.getSupportFragmentManager(), null);
                                    return;

                                } else {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".onReceive: mobile web service error -- "
                                                    + intent.getStringExtra(Const.REPLY_ERROR_MESSAGE));
                                    // Show the dialog indicating the submit failed.
                                    activity.reportSubmitFailedMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    activity.showDialog(Const.DIALOG_EXPENSE_SUBMIT_REPORT_FAILED);

                                    // Flurry Notification
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_NO);
                                    params.put(Flurry.PARAM_NAME_FAILURE, activity.reportSubmitFailedMessage);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_REPORTS, Flurry.EVENT_NAME_SUBMIT,
                                            params);
                                }
                                // Was a detailed report updated?
                                if (intent.getBooleanExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, false)) {

                                    // Update the display.
                                    Intent updateIntent = (Intent) activity.getIntent().clone();
                                    updateIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_DETAIL_UPDATE, false);
                                    activity.setExpenseReport(updateIntent);

                                    // Set the refresh report list flag on the active cache.
                                    activity.expRepCache.setShouldRefreshReportList();
                                }
                            } else {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG + ".onReceive: http error -- "
                                                + intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT));
                                activity.reportSubmitFailedMessage = activity.getText(R.string.service_not_available)
                                        .toString();
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            Log.e(Const.LOG_TAG,
                                    CLS_TAG + ".onReceive: service request error -- "
                                            + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }
                // Clear out the request reference.
                activity.reportSubmitRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle report detail data updates.
     * 
     * @author AndrewK
     */
    static class ReportDetailReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + ReportDetailReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the report detail request.
        private ReportDetailRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ReportDetailDataUpdateReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportDetailReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportDetailRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report detail request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report detail request object associated with this broadcast receiver.
         */
        void setRequest(ReportDetailRequest request) {
            this.request = request;
        }

        /**
         * Receive notification that detailed report data has been updated. This method may be called any number of times while
         * the Activity is running.
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterReportDetailReceiver();

                // Unregister the network receiver if non-null.
                if (activity.networkActivityReceiver != null) {
                    activity.unregisterReceiver(activity.networkActivityReceiver);
                    activity.networkActivityReceiver = null;
                }

                // Remove the dialog if it was necessary to display it the first time!
                if (activity.reportDetailDialog != null && activity.reportDetailDialog.isShowing()) {
                    activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_DETAIL);
                }

                if (activity.saveReportEntryReceiptDialog != null && activity.saveReportEntryReceiptDialog.isShowing()) {
                    activity.dismissDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT);
                }

                // Pre-fetching of a detailed report from a non-detail report activity can
                // result in this receiver being invoked. Perhaps this receiver shouldn't be set
                // up at all if no detailed report is required!
                if (activity.isDetailReportRequired()) {

                    int requestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                    if (requestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatus = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, 1);
                        if (httpStatus == HttpStatus.SC_OK) {
                            String mwsStatus = intent.getStringExtra(Const.REPLY_STATUS);
                            if (mwsStatus.equalsIgnoreCase(Const.REPLY_STATUS_SUCCESS)) {
                                // Retrieve the updated report and re-set the local reference.
                                String reportKey = intent.getExtras().getString(Const.EXTRA_EXPENSE_REPORT_KEY);
                                if (reportKey != null) {
                                    // Grab the updated report.
                                    activity.expRep = activity.expRepCache.getReportDetail(reportKey);
                                    // Update the form field view listener.
                                    if (activity.frmFldViewListener != null) {
                                        activity.frmFldViewListener.setExpenseReport(activity.expRep);
                                    }
                                    Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: calling 'buildView'");
                                    // Build the view.
                                    activity.buildView();
                                    Log.d(Const.LOG_TAG, CLS_TAG + ".onReceive: done calling 'buildView'");
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: intent missing report key!");
                                }
                            } else {
                                activity.actionStatusErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: MWS error '"
                                        + activity.actionStatusErrorMessage + "'.");
                                activity.showDialog(Const.DIALOG_EXPENSE_REPORT_DETAIL_RETRIEVE_FAILED);
                            }
                        } else {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: non HTTP status: '"
                                    + activity.lastHttpErrorMessage + "'.");
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                        }
                    } else {
                        Log.e(Const.LOG_TAG,
                                CLS_TAG + ".onReceive: service request error -- "
                                        + intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT));
                        activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                    }
                }
                // Clear out the request reference.
                activity.reportDetailRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save receipt action.
     * 
     * @author AndrewK
     */
    static class SaveReceiptReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + SaveReceiptReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the save receipt request.
        private SaveReceiptRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>SaveReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReceiptReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.saveReceiptRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the save receipt request object associated with this broadcast receiver.
         * 
         * @param request
         *            the save receipt request object associated with this broadcast receiver.
         */
        void setRequest(SaveReceiptRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterSaveReceiptReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    String receiptImageId = intent
                                            .getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                                    if (receiptImageId != null) {
                                        receiptImageId = receiptImageId.trim();
                                    }
                                    if (receiptImageId != null && receiptImageId.length() > 0) {
                                        // Handle the post-action of saving a receipt to the cloud.
                                        activity.handlePostReceiptSave(receiptImageId);

                                        // MOB-22375 - Google Analytics for Receipt Upload.
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

                                    } else {
                                        Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: null receipt image id!");

                                        // Dismiss the dialog.
                                        activity.dismissDialog(activity.getSavingReceiptDialogId());

                                        // Display an error dialog.
                                        activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);

                                    // Flurry Notification.
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_FAILURE,
                                            Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE,
                                            params);

                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);

                                    // Dismiss the dialog.
                                    activity.dismissDialog(activity.getSavingReceiptDialogId());
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                boolean handled = false;
                                if (httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                                    String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    if (mwsErrorMessage != null
                                            && mwsErrorMessage
                                                    .equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                                        activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                                        handled = true;
                                    }
                                }

                                // Flurry Notification.
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
                                EventTracker.INSTANCE
                                        .track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                                if (!handled) {
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            }
                        } else {
                            // Dismiss the dialog.
                            activity.dismissDialog(activity.getSavingReceiptDialogId());

                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);

                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            // Dismiss the dialog.
                            activity.dismissDialog(activity.getSavingReceiptDialogId());
                        }
                    }
                } else {
                    // Dismiss the dialog.
                    activity.dismissDialog(activity.getSavingReceiptDialogId());

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the request reference.
                activity.saveReceiptRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of appending a receipt to a
     * report entry.
     */
    static class AppendReportEntryReceiptReceiver extends
            BaseBroadcastReceiver<AbstractExpenseActivity, AppendReceiptImageRequest> {

        /**
         * Constructs an instance of <code>AppendReportEntryReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AppendReportEntryReceiptReceiver(AbstractExpenseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AbstractExpenseActivity activity) {
            activity.appendReportEntryReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(activity.getSavingReceiptDialogId());
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_RECEIPT_APPEND_FAIL);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // Show a success dialog.
            // TODO: show a successful append dialog.
            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_REPORT_ENTRY);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_APPEND, params);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(AppendReceiptImageRequest request) {
            activity.appendReportEntryReceiptRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterAppendReportEntryReceiptReceiver();
        }

    }

    /**
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of handling the results of clearing a report entry
     * receipt.
     */
    static class ClearReportEntryReceiptReceiver extends
            BaseBroadcastReceiver<AbstractExpenseActivity, ClearReportEntryReceiptRequest> {

        /**
         * Constructs an instance of <code>ClearReportEntryReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ClearReportEntryReceiptReceiver(AbstractExpenseActivity activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(ClearReportEntryReceiptRequest request) {
            activity.clearReportEntryReceiptRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#clearActivityServiceRequest(com.concur.mobile.activity
         * .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(AbstractExpenseActivity activity) {
            activity.clearReportEntryReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_CLEAR_RECEIPT_PROGRESS);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // Display an error dialog.
            activity.showDialog(Const.DIALOG_EXPENSE_CLEAR_RECEIPT_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            // TODO: Generate the Flurry event
            // if (activity.receiptSaveAction == ReceiptPictureSaveAction.CHOOSE_PICTURE) {
            // HashMap<String, String> params = new HashMap<String, String>();
            // params.put(Const.FLURRY_PARAM_ADD_RECEIPT_USING, Const.FLURRY_PARAM_VALUE_ALBUM);
            // FlurryAgent.onEvent("Add Receipt To Report Entry", params);
            // } else if (activity.receiptSaveAction == ReceiptPictureSaveAction.CHOOSE_PICTURE_CLOUD) {
            // HashMap<String, String> params = new HashMap<String, String>();
            // params.put(Const.FLURRY_PARAM_ADD_RECEIPT_USING,
            // Const.FLURRY_PARAM_VALUE_RECEIPT_STORE);
            // FlurryAgent.onEvent("Add Receipt To Report Entry", params);
            // } else if (activity.receiptSaveAction == ReceiptPictureSaveAction.TAKE_PICTURE) {
            // HashMap<String, String> params = new HashMap<String, String>();
            // params.put(Const.FLURRY_PARAM_ADD_RECEIPT_USING,
            // Const.FLURRY_PARAM_VALUE_CAMERA);
            // FlurryAgent.onEvent("Add Receipt To Report Entry", params);
            // }

            // Set the refresh flag on the receipt store cache, if needbe.
            if (activity.refreshReceiptStore) {
                ReceiptStoreCache rsCache = activity.getConcurCore().getReceiptStoreCache();
                rsCache.setShouldFetchReceiptList();
                activity.refreshReceiptStore = false;
            }

            // Ensure that a result of OK is passed back to the calling
            // activity which will result in the screen refreshing.
            Intent data = new Intent();
            data.putExtra(Const.EXTRA_APP_RESTART, activity.appRestarted);
            data.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);
            activity.setResult(Activity.RESULT_OK, data);

            // Upon a report entry receipt image update, only update the report header if clearing
            // a receipt from outside of a report entries view; otherwise, only re-build the view.
            if (activity.refreshReportHeaderAfterEntryReceiptUpdate()) {
                activity.sendReportHeaderDetailRequest();
            } else {
                activity.buildView();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterClearReportEntryReceiptReceiver();
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save report entry action.
     * 
     * @author AndrewK
     */
    static class SaveReportEntryReceiptReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + SaveReportEntryReceiptReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the save report entry receipt request.
        private SaveReportEntryReceiptRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>SaveReportEntryReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReportEntryReceiptReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.saveReportEntryReceiptRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the save report entry request object associated with this broadcast receiver.
         * 
         * @param request
         *            the save report entry request object associated with this broadcast receiver.
         */
        void setRequest(SaveReportEntryReceiptRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterSaveReportEntryReceiptReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {

                                    // MOB-22375 - Google Analytics for Receipt Upload.
                                    if (request != null && request.receiptImageId != null
                                            && request.receiptImageId.trim().length() > 0) {

                                        ConcurCore concurCore = ((ConcurCore) activity.getApplication());
                                        LastLocationTracker locTracker = concurCore.getLocationTracker();
                                        Location loc = locTracker.getCurrentLocaton();
                                        String lat = "0";
                                        String lon = "0";
                                        if (loc != null) {
                                            lat = Double.toString(loc.getLatitude());
                                            lon = Double.toString(loc.getLongitude());
                                        }
                                        String eventLabel = request.receiptImageId + "|" + lat + "|" + lon;
                                        EventTracker.INSTANCE.track("Receipts", "Receipt Capture Location", eventLabel);
                                    }

                                    // Flurry Notification
                                    if (activity.receiptSaveAction != null) {
                                        Map<String, String> params = new HashMap<String, String>();
                                        String paramValue = null;
                                        switch (activity.receiptSaveAction) {
                                        case CHOOSE_PICTURE: {
                                            paramValue = Flurry.PARAM_VALUE_ALBUM;
                                            break;
                                        }
                                        case CHOOSE_PICTURE_CLOUD: {
                                            paramValue = Flurry.PARAM_VALUE_RECEIPT_STORE;
                                            break;
                                        }
                                        case TAKE_PICTURE: {
                                            paramValue = Flurry.PARAM_VALUE_CAMERA;
                                            break;
                                        }
                                        default:
                                            break;
                                        }
                                        if (paramValue != null) {
                                            params.put(Flurry.PARAM_NAME_ADDED_USING, paramValue);
                                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS,
                                                    Flurry.EVENT_NAME_ADD_RECEIPT_TO_REPORT_ENTRY, params);
                                        }
                                    }

                                    // Set the refresh flag on the receipt store cache, if needbe.
                                    if (activity.refreshReceiptStore) {
                                        ReceiptStoreCache rsCache = activity.getConcurCore().getReceiptStoreCache();
                                        rsCache.setShouldFetchReceiptList();
                                        activity.refreshReceiptStore = false;
                                    }

                                    // Dismiss the dialog.
                                    activity.dismissDialog(activity.getSavingReceiptDialogId());

                                    // Ensure that a result of OK is passed back to the calling
                                    // activity which will result in the screen refreshing.
                                    Intent data = new Intent();
                                    data.putExtra(Const.EXTRA_APP_RESTART, activity.appRestarted);
                                    data.putExtra(Const.EXTRA_EXPENSE_REFRESH_HEADER, true);

                                    // MOB-18973 copy over report key in result data. We need report key to launch to report
                                    // detail page from home.
                                    Intent it = activity.getIntent();
                                    if (it.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY) != null) {
                                        data.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY,
                                                it.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY));
                                        data.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, it.getIntExtra(
                                                Const.EXTRA_EXPENSE_REPORT_SOURCE, Const.EXPENSE_REPORT_SOURCE_ACTIVE));
                                    }
                                    activity.setResult(Activity.RESULT_OK, data);

                                    // Upon a report entry receipt image update, only update the report header if attaching
                                    // a receipt from outside of a report entries view; otherwise, only re-build the view.
                                    if (activity.refreshReportHeaderAfterEntryReceiptUpdate()) {
                                        activity.sendReportHeaderDetailRequest();
                                    } else {
                                        activity.buildView();
                                    }
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);

                                    // Flurry Notification.
                                    Map<String, String> params = new HashMap<String, String>();
                                    params.put(Flurry.PARAM_NAME_FAILURE,
                                            Flurry.PARAM_VALUE_FAILED_TO_SAVE_RECEIPT_IMAGE_ID_FOR_ENTRY);
                                    EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE,
                                            params);

                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_SAVE_REPORT_ENTRY_RECEIPT_FAILED);

                                    // Dismiss the dialog.
                                    activity.dismissDialog(activity.getSavingReceiptDialogId());
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);

                                // Flurry Notification.
                                Map<String, String> params = new HashMap<String, String>();
                                params.put(Flurry.PARAM_NAME_FAILURE,
                                        Flurry.PARAM_VALUE_FAILED_TO_SAVE_RECEIPT_IMAGE_ID_FOR_ENTRY);
                                EventTracker.INSTANCE
                                        .track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                                activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            // Dismiss the dialog.
                            activity.dismissDialog(activity.getSavingReceiptDialogId());
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);

                            // Flurry Notification.
                            Map<String, String> params = new HashMap<String, String>();
                            params.put(Flurry.PARAM_NAME_FAILURE,
                                    Flurry.PARAM_VALUE_FAILED_TO_SAVE_RECEIPT_IMAGE_ID_FOR_ENTRY);
                            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            // Dismiss the dialog.
                            activity.dismissDialog(activity.getSavingReceiptDialogId());
                        }
                    }
                } else {
                    // Dismiss the dialog.
                    activity.dismissDialog(activity.getSavingReceiptDialogId());

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the request reference.
                activity.saveReportEntryReceiptRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling the result of a request to retrieve report header details.
     * 
     * @author andy
     */
    static class ReportHeaderDetailReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + ReportHeaderDetailReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the report header detail request.
        private ReportHeaderDetailRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>ReportHeaderDetailReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReportHeaderDetailReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.reportHeaderDetailRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the report header detail request object associated with this broadcast receiver.
         * 
         * @param request
         *            the report header detail request object associated with this broadcast receiver.
         */
        void setRequest(ReportHeaderDetailRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Remove Progress Dialog
                activity.dismissDialog(Const.DIALOG_EXPENSE_REFRESH_EXPENSES);

                // Unregister the network activity receiver.
                activity.unregisterNetworkActivityReceiver();

                // Unregister the report header receiver.
                activity.unregisterReportHeaderDetailReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Set the flag to refresh the active report list.
                                    IExpenseReportCache expRepCache = ((ConcurCore) activity.getApplication())
                                            .getExpenseActiveCache();
                                    expRepCache.setShouldRefreshReportList();

                                    // Re-build the display.
                                    activity.buildView();
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the activity request reference.
                activity.reportHeaderDetailRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of an add to report receipt action.
     * 
     * @author AndrewK
     */
    static class AddReportReceiptV2Receiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + AddReportReceiptV2Receiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the save report entry receipt request.
        private AddReportReceiptV2Request request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>AddReportReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AddReportReceiptV2Receiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.addReportReceiptV2Request = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the add report receipt request object associated with this broadcast receiver.
         * 
         * @param request
         *            the add report receipt request object associated with this broadcast receiver.
         */
        void setRequest(AddReportReceiptV2Request request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterAddReportReceiptV2Receiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Set the refresh flag on the receipt store cache, if needbe.
                                    if (activity.refreshReceiptStore) {
                                        ReceiptStoreCache rsCache = activity.getConcurCore().getReceiptStoreCache();
                                        rsCache.setShouldFetchReceiptList();
                                        activity.refreshReceiptStore = false;
                                    }
                                    activity.showDialog(Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_SUCCEEDED);
                                    try {
                                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    // Kick-off a background request to update the report header details.
                                    activity.sendReportHeaderDetailRequest();
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                boolean handled = false;
                                if (httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                                    String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    if (mwsErrorMessage != null
                                            && mwsErrorMessage
                                                    .equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                                        activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                                        handled = true;
                                    }
                                }
                                if (!handled) {
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                                try {
                                    // Dismiss the dialog.
                                    activity.dismissDialog(activity.getSavingReceiptDialogId());
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the activity request reference.
                activity.addReportReceiptV2Request = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save report entry action.
     * 
     * @author AndrewK
     */
    static class AddReportReceiptReceiver extends BroadcastReceiver {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + AddReportReceiptReceiver.class.getSimpleName();

        // A reference to the activity.
        private AbstractExpenseActivity activity;

        // A reference to the save report entry receipt request.
        private AddReportReceiptRequest request;

        // Contains the intent that was passed to the receiver's 'onReceive' method.
        private Intent intent;

        /**
         * Constructs an instance of <code>AddReportReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        AddReportReceiptReceiver(AbstractExpenseActivity activity) {
            this.activity = activity;
        }

        /**
         * Sets the activity associated with this broadcast receiver.
         * 
         * @param activity
         *            the activity associated with this broadcast receiver.
         */
        void setActivity(AbstractExpenseActivity activity) {
            this.activity = activity;
            if (this.activity != null) {
                this.activity.addReportReceiptRequest = request;
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        /**
         * Sets the add report receipt request object associated with this broadcast receiver.
         * 
         * @param request
         *            the add report receipt request object associated with this broadcast receiver.
         */
        void setRequest(AddReportReceiptRequest request) {
            this.request = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.content.BroadcastReceiver#onReceive(android.content.Context, android.content.Intent)
         */
        @Override
        public void onReceive(Context context, Intent intent) {

            // Does this receiver have a current activity?
            if (activity != null) {

                // Unregister the receiver.
                activity.unregisterAddReportReceiptReceiver();

                int serviceRequestStatus = intent.getIntExtra(Const.SERVICE_REQUEST_STATUS, -1);
                if (serviceRequestStatus != -1) {
                    if (serviceRequestStatus == Const.SERVICE_REQUEST_STATUS_OKAY) {
                        int httpStatusCode = intent.getIntExtra(Const.REPLY_HTTP_STATUS_CODE, -1);
                        if (httpStatusCode != -1) {
                            if (httpStatusCode == HttpStatus.SC_OK) {
                                if (intent.getStringExtra(Const.REPLY_STATUS).equalsIgnoreCase(
                                        Const.REPLY_STATUS_SUCCESS)) {
                                    // Set the refresh flag on the receipt store cache, if needbe.
                                    if (activity.refreshReceiptStore) {
                                        ReceiptStoreCache rsCache = activity.getConcurCore().getReceiptStoreCache();
                                        rsCache.setShouldFetchReceiptList();
                                        activity.refreshReceiptStore = false;
                                    }
                                    activity.showDialog(Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_SUCCEEDED);
                                    try {
                                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                    // Kick-off a background request to update the report header details.
                                    activity.sendReportHeaderDetailRequest();
                                } else {
                                    activity.actionStatusErrorMessage = intent
                                            .getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: mobile web service error -- "
                                            + activity.actionStatusErrorMessage);
                                    // Display an error dialog.
                                    activity.showDialog(Const.DIALOG_EXPENSE_ADD_REPORT_RECEIPT_FAILED);

                                    try {
                                        // Dismiss the dialog.
                                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                                    } catch (IllegalArgumentException ilaExc) {
                                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                    }
                                }
                            } else {
                                activity.lastHttpErrorMessage = intent.getStringExtra(Const.REPLY_HTTP_STATUS_TEXT);
                                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: http error -- "
                                        + activity.lastHttpErrorMessage);
                                boolean handled = false;
                                if (httpStatusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                                    String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                                    if (mwsErrorMessage != null
                                            && mwsErrorMessage
                                                    .equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                                        activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                                        handled = true;
                                    }
                                }
                                if (!handled) {
                                    activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);
                                }
                                try {
                                    // Dismiss the dialog.
                                    activity.dismissDialog(activity.getSavingReceiptDialogId());
                                } catch (IllegalArgumentException ilaExc) {
                                    Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                                }
                            }
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing http reply code!");
                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    } else {
                        if (request != null && !request.isCanceled()) {
                            activity.lastHttpErrorMessage = intent.getStringExtra(Const.SERVICE_REQUEST_STATUS_TEXT);
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: service request error -- "
                                    + activity.lastHttpErrorMessage);
                            activity.showDialog(Const.DIALOG_SYSTEM_UNAVAILABLE);

                            try {
                                // Dismiss the dialog.
                                activity.dismissDialog(activity.getSavingReceiptDialogId());
                            } catch (IllegalArgumentException ilaExc) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                            }
                        }
                    }
                } else {
                    try {
                        // Dismiss the dialog.
                        activity.dismissDialog(activity.getSavingReceiptDialogId());
                    } catch (IllegalArgumentException ilaExc) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".onReceive: dismissRequestDialog: ", ilaExc);
                    }

                    Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: missing service request status!");
                }

                // Clear out the activity request reference.
                activity.addReportReceiptRequest = null;
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = intent;
            }
        }

    }

    // NOTE: The code below was lifted pretty heavily from the 'ExpenseOutOfPocketEditView' class. The code
    // below here should be placed into it's own files and having both 'AbstractExpenseActivity' and
    // 'ExpenseOutOfPocketEditView' reference it.

    /**
     * An implementation of <code>DialogInterface.OnClickListener</code> for handling user selection receipt option.
     * 
     * @author AndrewK
     */
    class ReceiptImageDialogListener implements DialogInterface.OnClickListener {

        /*
         * (non-Javadoc)
         * 
         * @see android.content.DialogInterface.OnClickListener#onClick(android.content.DialogInterface, int)
         */
        @Override
        public void onClick(DialogInterface dialog, int which) {
            ReceiptPictureSaveAction pictureAction = (ReceiptPictureSaveAction) receiptActionAdapter.getItem(which);
            switch (pictureAction) {
            case CHOOSE_PICTURE: {
                if (ConcurCore.isConnected()) {
                    if (isReportLevelReceiptDialog()) {
                        selectReportReceipt();
                    } else {
                        if (!isNewExpense()) {
                            if (hasFormFieldsChanged() || changesPending()) {
                                processingReceiptAction = pictureAction;
                                showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                            } else {
                                selectReportEntryReceipt();
                            }
                        } else {
                            processingReceiptAction = pictureAction;
                            showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                        }
                    }
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case CHOOSE_PICTURE_CLOUD: {
                if (ConcurCore.isConnected()) {
                    if (isReportLevelReceiptDialog()) {
                        selectCloudReportReceipt();
                    } else {
                        if (!isNewExpense()) {
                            if (hasFormFieldsChanged() || changesPending()) {
                                processingReceiptAction = pictureAction;
                                showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                            } else {
                                selectCloudReportEntryReceipt();
                            }
                        } else {
                            processingReceiptAction = pictureAction;
                            showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                        }
                    }
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case TAKE_PICTURE: {
                if (ConcurCore.isConnected()) {
                    if (isReportLevelReceiptDialog()) {
                        captureReportReceipt();
                    } else {
                        if (!isNewExpense()) {
                            if (hasFormFieldsChanged() || changesPending()) {
                                processingReceiptAction = pictureAction;
                                showDialog(Const.DIALOG_EXPENSE_CONFIRM_SAVE_REPORT);
                            } else {
                                captureReportEntryReceipt();
                            }
                        } else {
                            processingReceiptAction = pictureAction;
                            showDialog(Const.DIALOG_EXPENSE_REQUIRE_ENTRY_SAVE_CONFIRM);
                        }
                    }
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case VIEW: {
                if (ConcurCore.isConnected()) {
                    Intent clickIntent = new Intent(AbstractExpenseActivity.this, ExpenseReceipt.class);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_KEY, expRep.reportKey);
                    clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_SOURCE, reportKeySource);
                    if (isReportEntryWithReceipt()) {
                        clickIntent.putExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY, getReportEntryKey());
                        if (getReportEntryReceiptImageId() != null) {
                            clickIntent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY,
                                    getReportEntryReceiptImageId());
                        }
                    }
                    clickIntent.putExtra(Const.EXTRA_E_RECEIPT_EXPENSE, isEreceiptExpense());
                    startActivity(clickIntent);
                } else {
                    showDialog(Const.DIALOG_NO_CONNECTIVITY);
                }
                break;
            }
            case CLEAR_PICTURE: {
                showDialog(Const.DIALOG_EXPENSE_CONFIRM_CLEAR_RECEIPT);
                break;
            }
            default:
                break;
            }
            dismissDialog(DIALOG_RECEIPT_IMAGE);
            removeDialog(DIALOG_RECEIPT_IMAGE);
        }

    }

    public boolean isEreceiptExpense() {
        return false;
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

            LayoutInflater inflater = LayoutInflater.from(AbstractExpenseActivity.this);

            // int iconResId = 0;
            int textResId = 0;
            switch (options.get(position)) {
            case TAKE_PICTURE:
                textResId = R.string.take_picture;
                break;
            case CHOOSE_PICTURE:
                textResId = R.string.select_from_device;
                break;
            case CHOOSE_PICTURE_CLOUD:
                textResId = R.string.select_from_cloud;
                break;
            case VIEW:
                textResId = R.string.view_receipts;
                break;
            case CLEAR_PICTURE:
                textResId = R.string.clear_picture;
                break;
            default:
                break;
            }
            view = inflater.inflate(R.layout.expense_receipt_option, null);
            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                if (txtView != null) {
                    txtView.setPadding(10, 8, 0, 8);
                    txtView.setText(AbstractExpenseActivity.this.getText(textResId));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate text view!");
                }
            }
            return view;
        }
    }

    /**
     * Will create and register with the application context an instance of 'taxFormReceiver' and update the 'taxFormReceiver'
     * attribute.
     */
    protected void registerTaxFormReceiver() {
        if (taxFormReceiver == null) {
            taxFormReceiver = new TaxFormReceiver(this);
            if (taxFormFilter == null) {
                taxFormFilter = new IntentFilter(Const.ACTION_EXPENSE_TAX_FORM_FILTER);
            }
            getApplicationContext().registerReceiver(taxFormReceiver, taxFormFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerTaxFormReceiver: taxFormReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'taxFormReceiver' and set the 'taxFormReceiver'
     * attribute to 'null'.
     */
    protected void unregisterTaxFormReceiver() {
        if (taxFormReceiver != null) {
            getApplicationContext().unregisterReceiver(taxFormReceiver);
            taxFormReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterTaxFormReceiver: taxFormReceiver is null!");
        }
    }

    /**
     * Will create and register with the application context an instance of 'conditionalFieldActionReceiver' and update the
     * 'conditionalFieldActionReceiver' attribute.
     */
    protected void registerConditionalFieldActionReceiver() {
        if (conditionalFieldActionReceiver == null) {
            conditionalFieldActionReceiver = new ConditionalActionReceiver(this);
            if (conditionalFieldActionFilter == null) {
                conditionalFieldActionFilter = new IntentFilter(Const.ACTION_EXPENSE_CONDITIONAL_FIELDS_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(conditionalFieldActionReceiver, conditionalFieldActionFilter);
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG
                    + ".registerConditionalFieldActionReceiver: conditionalFieldActionReceiver is *not* null!");
        }
    }

    /**
     * Will unregister with the application context the current instance of 'conditionalFieldActionReceiver' and set the
     * 'conditionalFieldActionReceiver' attribute to 'null'.
     */
    protected void unregisterConditionalFieldActionReceiver() {
        if (conditionalFieldActionReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(conditionalFieldActionReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterConditionalFieldActionReceiver: invalid receiver!", ilaExc);
            }
            conditionalFieldActionReceiver = null;
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG
                    + ".unregisterConditionalFieldActionReceiver: conditionalFieldActionReceiver is null!");
        }
    }

    protected void checkConditionalFieldActions(FormFieldView frmFldView) {
        if (isReportEditable() && ViewUtil.isConditionalFieldEvaluationEnabled(ConcurCore.getContext())
                && frmFldView.getFormField().getIsConditionalField() != null
                && frmFldView.getFormField().getIsConditionalField()
                && frmFldView.getFormField().getFormFieldKey() != null) {

            // Lazy initialization.
            if (lastConditionalFieldEntries == null) {
                lastConditionalFieldEntries = new HashMap<String, String>();
            }

            String value = (frmFldView instanceof SearchListFormFieldView) ? ((SearchListFormFieldView) frmFldView)
                    .getLiKey() : frmFldView.getCurrentValue();
            if (value != null) {
                String key = frmFldView.getFormField().getFormFieldKey();

                // ignore if the last call matches
                if (lastConditionalFieldEntries.containsKey(key)) {

                    String lastValue = lastConditionalFieldEntries.get(key);
                    if (!value.trim().equals(lastValue.trim())
                            && (frmFldView.getFormField().getAccessType() != AccessType.HD)) {
                        sendConditionalFieldActionRequest(key, value.trim());
                    }
                } else {
                    sendConditionalFieldActionRequest(key, value.trim());
                }

                lastConditionalFieldEntries.put(key, value.trim());
            }
        }
    }

    protected void sendConditionalFieldActionRequest(String ffKey, String fieldValue) {
        if (ConcurCore.isConnected()) {
            ConcurService concurService = getConcurService();
            registerConditionalFieldActionReceiver();
            conditionalFieldActionRequest = concurService.sendGetDynamicActionRequest(ffKey, fieldValue);
            if (conditionalFieldActionRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onReceive: unable to create 'sendConditionalFieldActionRequest' request!");
                unregisterConditionalFieldActionReceiver();
            } else {
                showConditionalFieldActionDialog(Const.DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS);

                // Set the request object on the receiver.
                conditionalFieldActionReceiver.setServiceRequest(conditionalFieldActionRequest);
            }
        } else {
            Log.i(Const.LOG_TAG,
                    CLS_TAG
                            + ".sendConditionalFieldActionRequest: client off-line, unable to request policy-specific expense types!");
        }
    }

    class ConditionalActionReceiver extends
            BaseBroadcastReceiver<AbstractExpenseActivity, GetConditionalFieldActionRequest> {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "."
                + ConditionalActionReceiver.class.getSimpleName();

        protected ConditionalActionReceiver(AbstractExpenseActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(AbstractExpenseActivity activity) {
            activity.conditionalFieldActionRequest = null;
        }

        @Override
        protected void setActivityServiceRequest(GetConditionalFieldActionRequest request) {
            activity.conditionalFieldActionRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterConditionalFieldActionReceiver();
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            List<ConditionalFieldAction> conditionalFieldActions = activity.getConcurCore()
                    .getConditionalFieldActionsResults();
            onHandleSuccessConditionalFieldActions(conditionalFieldActions);
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess");
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS_FAILURE);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_CONDITIONAL_FIELD_ACTIONS_PROGRESS);
        }
    }

    protected void onHandleSuccessConditionalFieldActions(List<ConditionalFieldAction> actions) {
        // Virtual function stub
    }

    protected void showConditionalFieldActionDialog(int id) {
        showDialog(id);
    }

    /**
     * An extension of {@link BaseBroadcastReceiver} for the purposes of handling the response for gov. document
     */
    class TaxFormReceiver extends BaseBroadcastReceiver<AbstractExpenseActivity, GetTaxFormRequest> {

        private final String CLS_TAG = AbstractExpenseActivity.CLS_TAG + "." + TaxFormReceiver.class.getSimpleName();

        protected TaxFormReceiver(AbstractExpenseActivity activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(AbstractExpenseActivity activity) {
            activity.getTaxFormRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(Const.DIALOG_EXPENSE_TAX_FORM_PROGRESS);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            // TODO handle fail case..show dialog...
            activity.showDialog(Const.DIALOG_EXPENSE_TAX_FORM_PROGRESS_FAILURE);
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure");
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            GetTaxFormReply taxForm = activity.getConcurCore().getTaxFormReply();
            onHandleSuccessTaxForm(taxForm);
        }

        @Override
        protected void setActivityServiceRequest(GetTaxFormRequest request) {
            activity.getTaxFormRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterTaxFormReceiver();
        }
    }

    /**
     * Helper Method : Implement this method in respective activity to build respective page.
     * 
     * @param taxForm
     * 
     * @param {@link GetTaxFormReply} reply
     * 
     * */
    protected void onHandleSuccessTaxForm(GetTaxFormReply taxForm) {
        // TODO Auto-generated method stub
    }

    protected void showTaxFormDialog(int id) {
        showDialog(id);
    }

}
