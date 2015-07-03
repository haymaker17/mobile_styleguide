package com.concur.mobile.core.expense.receiptstore.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.dialog.AlertDialogFragment;
import com.concur.mobile.core.dialog.DialogFragmentFactory;
import com.concur.mobile.core.dialog.DialogFragmentHandler;
import com.concur.mobile.core.dialog.ProgressDialogFragment;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfoComparator;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.expense.receiptstore.service.DeleteReceiptImageRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlsRequest;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService;
import com.concur.mobile.core.expense.receiptstore.service.RetrieveURLRequest;
import com.concur.mobile.core.expense.service.SaveReceiptReply;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.fragment.BaseFragment;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.ContentFetcher;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.HeaderListItem;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.core.view.ListItemAdapter;
import com.concur.mobile.platform.expense.receipt.list.Receipt;
import com.concur.mobile.platform.expense.receipt.ocr.StartOCR;
import com.concur.mobile.platform.expense.receipt.ocr.StartOCRRequestTask;
import com.concur.mobile.platform.location.LastLocationTracker;
import com.concur.mobile.platform.util.Format;

public class ReceiptStoreFragment extends BaseFragment {

    private static final String CLS_TAG = ReceiptStoreFragment.class.getSimpleName();

    /**
     * 
     * @author Chris N. Diaz
     *
     */
    public interface ReceiptStoreFragmentCallback {

        public void doGetReceiptList();

        public void onStartOcrSuccess();

        public void onStartOcrFailed();

        public void onGetReceiptListSuccess();

        public void onGetReceiptListFailed();

        void uploadReceiptToExpenseIt(String filePath);

    }

    public static final String EXTRA_START_OCR_ON_UPLOAD = "extra.start.ocr.on.upload";

    public static final String EXTRA_USE_EXPENSEIT = "extra.use.expenseit";

    // Contains the key used to store/retrieve the delete receipt receiver.
    private static final String DELETE_RECEIPT_RECEIVER_KEY = "delete.receipt.receiver";

    // Contains the key used to store/retrieve the save receipt receiver.
    private static final String SAVE_RECEIPT_RECEIVER_KEY = "save.receipt.receiver";

    // Contains the key used to store/retrieve the receipt urls receiver.
    private static final String RECEIPT_URLS_RECEIVER_KEY = "retrieve.receipt.urls.receiver";

    // Contains the key used to store/retrieve the receipt url receiver.
    private static final String RECEIPT_URL_RECEIVER_KEY = "retrieve.receipt.url.receiver";

    // Contains the key used to store/retrieve the retrieve url receiver.
    private static final String RETRIEVE_URL_RECEIVER_KEY = "retrieve.url.receiver";

    // Contains the key used to store/retrieve the start OCR receiver.
    private static final String START_OCR_RECEIVER_KEY = "start.ocr.receiver";

    // Contains the key used to store/retrieve the file path given to the camera
    // application.
    private static final String RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY = "expense.receipt.camera.image.file.path";

    // Contains the key used to store/retrieve the
    // 'receiptImageDataLocalFilePath' value.
    private static final String RECEIPT_IMAGE_FILE_PATH_KEY = "expense.receipt.image.file.path";

    // Contains the key used to store/retrieve whether the file stored in
    // 'receiptImageDataLocalFilePath' should
    // be punted after a save attempt.
    private static final String DELETE_RECEIPT_IMAGE_FILE_PATH = "expense.delete.receipt.image.file.path";

    // Contains the key used to store/retrieve the currently selected receipt
    // info object.
    private static final String SELECTED_RECEIPT_INFO_KEY = "expense.selected.receipt.info.key";

    // Contains the key used to store/retrieve the report key.
    private static final String REPORT_KEY = "expense.report.key";

    // Contains the key used to store/retrieve the entry key.
    private static final String REPORT_ENTRY_KEY = "expense.report.entry.key";

    // Contains the key used to store whether or not the end-user initiated a
    // refresh operation.
    private static final String END_USER_REFRESH_KEY = "end.user.refresh";

    // Contains the key used to store/retrieve the receipt share status
    // receiver.
    private static final String SERVICE_STATUS_RECEIVER_KEY = "receipt.share.service.status.receiver";

    // Contains the key used to store/retrieve the last receipt action.
    private static final String LAST_RECEIPT_ACTION_KEY = "expense.last.receipt.action";

    private static final int REQUEST_TAKE_PICTURE = 0;

    private static final int REQUEST_CHOOSE_IMAGE = 1;

    private static final int REQUEST_SAVE_CAMERA_IMAGE = 2;

    private static final int REQUEST_START_OCR = 3;

    private static final int HEADER_VIEW_TYPE = 0;

    private static final int RECEIPT_VIEW_TYPE = 1;

    // A reference to a receiver to handle the result of a receipt image upload
    // request.
    private SaveReceiptReceiver saveReceiptReceiver;

    // The filter used to register the save receipt receiver.
    private IntentFilter saveReceiptFilter;

    // A reference to an outstanding request to save a receipt within the
    // receipt store.
    private SaveReceiptRequest saveReceiptRequest;

    // A reference to a receiver to handle the result of a delete receipt
    // request.
    private DeleteReceiptReceiver deleteReceiptReceiver;

    // The filter used to register the delete receipt receiver.
    private IntentFilter deleteReceiptFilter;

    // A reference to an outstanding request to delete a receipt within the
    // receipt store.
    private DeleteReceiptImageRequest deleteReceiptRequest;

    // A reference to the filter used to register the receipts urls receiver.
    private IntentFilter receiptUrlsFilter;

    // A reference to an outstanding request to retrieve a list of receipt store
    // receipts.
    private GetReceiptImageUrlsRequest receiptUrlsRequest;

    // A reference to a receiver to handle the result of retrieving a URL for a
    // specific receipt image.
    private ReceiptUrlReceiver receiptUrlReceiver;

    // A reference to the filter used to register the receipt url receiver.
    private IntentFilter receiptUrlFilter;

    // A reference to an outstanding request to retrieve a URL for a specific
    // receipt image.
    private GetReceiptImageUrlRequest receiptUrlRequest;

    // A reference to a receiver to handle the result of downloading receipt
    // contents.
    private RetrieveUrlReceiver retrieveUrlReceiver;

    // A reference to the filter used to register the retreive url receiver.
    private IntentFilter retrieveUrlFilter;

    // A reference to an outstanding request to retrieve the contents of a URL.
    private RetrieveURLRequest retrieveUrlRequest;

    // Contains a reference to a broadcast receiver to handle receipt share
    // status updates.
    private ReceiptStoreStatusReceiver rsStatusReceiver = new ReceiptStoreStatusReceiver();

    // Contains the intent filter used to register the receipt share service
    // status receiver.
    private final IntentFilter rsStatusFilter = new IntentFilter(
            ReceiptShareService.ACTION_RECEIPT_SHARE_SERVICE_UPDATE);

    // Contains a reference to the list item adapter.
    private ListItemAdapter<ListItem> listItemAdapter;

    // Contains whether path is available or not.
    private boolean isPathAvailable = false;

    // Contains whether or not the receipt image path passed into this activity
    // has
    // been acted upon.
    private static final String IS_PATH_AVAILABLE_KEY = "is.path.available";

    /**
     * Contains the path within the receipt image directory of the image.
     */
    private String receiptImageDataLocalFilePath;

    /**
     * Contains the path provided to the camera activity in which to store a captured image.
     */
    private String receiptCameraImageDataLocalFilePath;

    /**
     * Contains whether or not the receipt image file referenced by 'receiptImageDataLocalFilePath' should be deleted after a save
     * has succeeded.
     */
    private boolean deleteReceiptImageDataLocalFilePath;

    /**
     * Contains a reference to the current selected <code>ReceiptInfo</code> object.
     */
    private ReceiptInfo selectedReceiptInfo;

    /**
     * Contains a reference to the last saved instance state.
     */
    protected Bundle lastSavedInstanceState;

    /**
     * Contains a reference to a current content fetcher.
     */
    protected ContentFetcher contentFetcher;

    /**
     * Contains whether this activity has been invoked for the purpose of report level receipt selection.
     */
    protected boolean reportReceiptSelection;

    /**
     * Contains whether this activity has been invoked for the purpose of report entry receipt selection.
     */
    protected boolean reportEntryReceiptSelection;

    /**
     * Contains whether this activity has been invoked for the purpose of quick expense receipt selection.
     */
    protected boolean quickExpenseReceiptSelection;

    /**
     * Contains whether or not the end-user kicked-off a refresh.
     */
    public boolean endUserRefresh;

    /**
     * Contains the report key if a receipt is being picked to be added at the report level.
     */
    protected String reportKey;

    /**
     * Contains the report entry key if a receipt is being picked to be associated with an expense.
     */
    protected String reportEntryKey;

    /**
     * Contains the passed in report name.
     */
    protected String reportName;

    /**
     * Contains the passed in expense name.
     */
    protected String expenseName;

    /**
     * Contains the formatted transaction amount.
     */
    protected String expenseAmtStr;

    /**
     * Contains the reference to a file on the SD card for which the end-user wanted to view the full-sized receipt.
     */
    protected File receiptImageFile;

    /**
     * Contains the selected selection option for performing an action on an expense receipt.
     */
    private ReceiptPictureSaveAction lastReceiptAction = ReceiptPictureSaveAction.NO_ACTION;

    /**
     * AsyncTask result from invoking the StartOCR MWS endpoint.
     */
    private AsyncTask<Void, Void, Integer> startOcrAsyncTask;

    /**
     * The receiver for calling the StartOCR MWS endpoint.
     */
    private BaseAsyncResultReceiver startOcrReceiver;

    // ///////////////////////////////////////////////
    // Moved over from BaseActivity
    // ///////////////////////////////////////////////

    /**
     * Contains a map from the view state to a child index of the view flipper.
     */
    protected HashMap<ViewState, Integer> viewStateFlipChild;

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
    };

    /**
     * Contains the current view state.
     */
    protected ViewState viewState;

    /**
     * Contains whether or not the handling of a call from 'onActivityResult' was delayed due to the view not being present.
     */
    protected boolean activityResultDelay;

    /**
     * Contains the request code from the delayed handling of the 'onActivityResult' call.
     */
    protected int activityResultRequestCode;

    /**
     * Contains the result code from the delayed handling of the 'onActivityResult' call.
     */
    protected int activityResultResultCode;

    /**
     * Contains the intent data from the delayed handling of the 'onActivityResult' call.
     */
    protected Intent activityResultData;

    /**
     * Flag to indicate whether or not to start OCR after uploading/saving an image.
     */
    protected boolean startOcrOnUpload;

    /**
     * Flag indicating whether to use expenseIt services when uploading image
     */
    protected boolean useExpenseIt;
    /**
     * 
     */
    private ReceiptStoreFragmentCallback receiptStoreCallback;

    /**
     * The listener for the StartOCR MWS result/failed response.
     */
    private AsyncReplyListener startOcrReplyListener = new BaseAsyncRequestTask.AsyncReplyListener() {

        @Override
        public void onRequestSuccess(Bundle resultData) {

            Log.d(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - call to StartOCR returned successfully.");

            if (resultData != null && resultData.containsKey(StartOCRRequestTask.START_OCR_RESULT_KEY)) {

                StartOCR startOcr = (StartOCR) resultData.getSerializable(StartOCRRequestTask.START_OCR_RESULT_KEY);
                if (!"A_PEND".equals(startOcr.ocrStatus)) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - call to StartOCR retured status: "
                            + startOcr.ocrStatus);
                } else {
                    // SUCCESS! Now log it!
                    // OCR: Might want to track via GA
                    Log.d(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - status=" + startOcr.ocrStatus
                            + "; receiptImageId=" + startOcr.receiptImageId);

                }

                // Add an entry to the database so we can display it immediately in the
                // ExpenseListwithout waiting for R.S. refresh.
                Receipt ocrReceipt = new Receipt(ConcurCore.getContext(),
                        ReceiptStoreFragment.this.activity.getUserId());
                ocrReceipt.setId(startOcr.receiptImageId);
                ocrReceipt.setImageOrigin(startOcr.imageOrigin);
                ocrReceipt.setOcrStatus(startOcr.ocrStatus);
                // Just use the current time for the upload time.
                ocrReceipt.setReceiptUploadTime(Calendar.getInstance());

                // Now save the DAO to the database!
                if (!ocrReceipt.update()) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".StartOcrReplyListener - failed to save ReceiptDAO to the database!");
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - call to StartOCR returned null data!!!");
            }

            // Update any listeners (e.g. ExpenseList to refresh).
            // Set the flag that the list of expenses
            // should be refreshed.
            IExpenseEntryCache expEntCache = getConcurCore().getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();

            // Invoke any listeners that StartOCR has completed successfully.
            receiptStoreCallback.onStartOcrSuccess();

            if (ReceiptStoreFragment.this != null) {
                SaveReceiptProgressDialogHandler.dismiss(ReceiptStoreFragment.this);
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {

            Log.e(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - call to StartOCR FAILED!!!");

            if (ReceiptStoreFragment.this != null) {
                SaveReceiptProgressDialogHandler.dismiss(ReceiptStoreFragment.this);
            }

            if (resultData != null && resultData.containsKey(StartOCRRequestTask.START_OCR_RESULT_KEY)) {

                StartOCR startOcr = (StartOCR) resultData.getSerializable(StartOCRRequestTask.START_OCR_RESULT_KEY);
                String errorMessage = "";
                // Just show the first error message.
                if (startOcr != null && startOcr.errors != null && !startOcr.errors.isEmpty()) {
                    com.concur.mobile.platform.service.parser.Error firstError = startOcr.errors.get(0);
                    errorMessage = firstError.getSystemMessage();

                    // Log all the errors to logcat.
                    for (com.concur.mobile.platform.service.parser.Error err : startOcr.errors) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".StartOcrReplyListener - ERROR: " + err.getSystemMessage());
                    }
                }

                // Show error dialog.
                if (ReceiptStoreFragment.this != null && getFragmentManager() != null) {
                    DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_recipts_start_ocr_failed_title,
                            errorMessage).show(getFragmentManager(), null);
                }
            }

            // Invoke any listeners that StartOCR has completed successfully.
            receiptStoreCallback.onStartOcrFailed();
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            // no-op
        }

        @Override
        public void cleanup() {
            startOcrReceiver = null;
            startOcrOnUpload = false;
            useExpenseIt = false;

            // Refresh the list of Receipts.
            // OCR: Do we really want to call this? Or just call it when user manually refreshes?
            // sendGetReceiptList(getView());
        }

    };

    /**
     * Will flip the current view based on the value of <code>viewState</code>.
     */
    protected void flipViewForViewState() {
        if (viewFlipper != null) {
            if (viewStateFlipChild != null) {
                if (viewStateFlipChild.containsKey(viewState)) {
                    int newChildInd = viewStateFlipChild.get(viewState);
                    int curChildInd = viewFlipper.getDisplayedChild();
                    if (newChildInd != curChildInd) {
                        viewFlipper.setDisplayedChild(newChildInd);
                        setFlipViewText(viewFlipper.getCurrentView());
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: current view state '" + viewState
                            + "' not in map!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".flipViewForViewState: null view state flip child map!");
            }
        }
    }

    /**
     * Sets the text string, if any, in <code>view</code> based on the current value of <code>viewState</code>.
     * 
     * @param view
     *            the view on which to set the text.
     */
    protected void setFlipViewText(View view) {

        switch (viewState) {
        case NO_DATA: {
            TextView txtView = (TextView) view.findViewById(R.id.no_data_text);
            if (txtView != null) {
                txtView.setText(getNoDataTextResourceId());
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG + ".setFlipViewText: unable to locate 'no data' text view!");
            }
            break;
        }
        case NO_LOCAL_DATA_REFRESH: {
            TextView txtView = (TextView) view.findViewById(R.id.no_local_data_server_refresh_text);
            if (txtView != null) {
                txtView.setText(getNoLocalDataRefreshTextResourceId());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setFlipViewText: unable to locate 'no local data server refresh' text view!");
            }
            break;
        }
        }
    }

    // ///////////////////////////////////////////////

    // @Override
    // public void onCreate(Bundle savedInstanceState) {
    // super.onCreate(savedInstanceState);
    // Log.d(Const.LOG_TAG, CLS_TAG +
    // ".onCreate: finished calling 'super.onCreate'.");
    //
    // // Set the receipt selection type flags.
    // Intent intent = activity.getIntent();
    // if (intent.getAction() != null &&
    // intent.getAction().equalsIgnoreCase(Intent.ACTION_PICK)) {
    // isPathAvailable = false;
    // reportReceiptSelection =
    // intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_REPORT_RECEIPT_KEY,
    // false);
    // reportEntryReceiptSelection =
    // intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_ENTRY_RECEIPT_KEY,
    // false);
    // quickExpenseReceiptSelection =
    // intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY,
    // false);
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
    // reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
    // }
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
    // reportName = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
    // }
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY)) {
    // reportEntryKey =
    // intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
    // }
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_EXPENSE_NAME)) {
    // expenseName = intent.getStringExtra(Const.EXTRA_EXPENSE_EXPENSE_NAME);
    // }
    // if (intent.hasExtra(Const.EXTRA_EXPENSE_EXPENSE_AMOUNT)) {
    // expenseAmtStr =
    // intent.getStringExtra(Const.EXTRA_EXPENSE_EXPENSE_AMOUNT);
    // }
    // } else {
    // if (intent != null && !activity.orientationChange) {
    // Bundle extras = intent.getExtras();
    // if (extras != null) {
    // String path = extras.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
    // if (path != null && path.length() > 0) {
    // receiptCameraImageDataLocalFilePath = path;
    // isPathAvailable = true;
    // }
    // }
    // Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: activity.retainer -> "
    // + ((activity.retainer != null) ? "non-null" : "null"));
    // // Use value from retainer as an override.
    // if (isPathAvailable && activity.retainer != null &&
    // activity.retainer.contains(IS_PATH_AVAILABLE_KEY)) {
    // Object obj = activity.retainer.get(IS_PATH_AVAILABLE_KEY);
    // if (obj instanceof Boolean) {
    // isPathAvailable = (Boolean) obj;
    // if (!isPathAvailable) {
    // receiptCameraImageDataLocalFilePath = null;
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG +
    // ".onCreate: isPathAvailable object is not a Boolean value!");
    // }
    // // Ensure we re-put the value into the retainer since it gets removed on
    // the 'get'.
    // activity.retainer.put(IS_PATH_AVAILABLE_KEY, obj);
    // }
    // }
    // }
    //
    // // Construct and populate map from view state to child index.
    // viewStateFlipChild = new HashMap<ViewState, Integer>();
    // viewStateFlipChild.put(ViewState.LOCAL_DATA, 0);
    // viewStateFlipChild.put(ViewState.NO_DATA, 2);
    // viewStateFlipChild.put(ViewState.RESTORE_APP_STATE, 1);
    // // The last two states here map to the same view.
    // viewStateFlipChild.put(ViewState.NO_LOCAL_DATA_REFRESH, 3);
    // viewStateFlipChild.put(ViewState.LOCAL_DATA_REFRESH, 3);
    //
    // // Init to local data.
    // viewState = ViewState.LOCAL_DATA;
    //
    // setHasOptionsMenu(true);
    // }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Initialize some state prior to constructing the view.

        // Set the receipt selection type flags.
        Intent intent = activity.getIntent();
        if (intent != null && intent.getAction() != null && intent.getAction().equalsIgnoreCase(Intent.ACTION_PICK)) {
            isPathAvailable = false;
            reportReceiptSelection = intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_REPORT_RECEIPT_KEY, false);
            reportEntryReceiptSelection = intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_ENTRY_RECEIPT_KEY, false);
            quickExpenseReceiptSelection = intent.getBooleanExtra(Const.EXTRA_EXPENSE_SELECT_QUICK_EXPENSE_RECEIPT_KEY,
                    false);
            if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_KEY)) {
                reportKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_KEY);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_NAME)) {
                reportName = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_NAME);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY)) {
                reportEntryKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_EXPENSE_NAME)) {
                expenseName = intent.getStringExtra(Const.EXTRA_EXPENSE_EXPENSE_NAME);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_EXPENSE_AMOUNT)) {
                expenseAmtStr = intent.getStringExtra(Const.EXTRA_EXPENSE_EXPENSE_AMOUNT);
            }
        } else {
            if (intent != null && !activity.orientationChange) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    String path = extras.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
                    if (path != null && path.length() > 0) {
                        receiptCameraImageDataLocalFilePath = path;
                        isPathAvailable = true;
                    }
                }
                // Use value from retainer as an override.
                if (isPathAvailable && activity.retainer != null && activity.retainer.contains(IS_PATH_AVAILABLE_KEY)) {
                    Object obj = activity.retainer.get(IS_PATH_AVAILABLE_KEY);
                    if (obj instanceof Boolean) {
                        isPathAvailable = (Boolean) obj;
                        if (!isPathAvailable) {
                            receiptCameraImageDataLocalFilePath = null;
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: isPathAvailable object is not a Boolean value!");
                    }
                    // Ensure we re-put the value into the retainer since it
                    // gets removed on the 'get'.
                    activity.retainer.put(IS_PATH_AVAILABLE_KEY, obj);
                }
            }
        }

        // OCR stuff.
        if (savedInstanceState != null) {
            // Rotated or paused fragment.
            startOcrOnUpload = savedInstanceState.getBoolean(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, false);
            useExpenseIt = savedInstanceState.getBoolean(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, false);
        } else if (intent != null) {
            // Initially started fragment.
            startOcrOnUpload = intent.getBooleanExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, false);
            useExpenseIt = intent.getBooleanExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, false);
        }

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

        setHasOptionsMenu(true);

        View root = inflater.inflate(R.layout.expense_receipt_store, null);

        // Show the action button.
        View view = root.findViewById(R.id.action_button);
        if (view != null) {
            view.setVisibility(View.VISIBLE);
        }

        viewFlipper = (ViewFlipper) root.findViewById(R.id.view_flipper);

        // Hook up the null state button
        Button uploadButton = (Button) root.findViewById(R.id.add_receipt);
        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                onUploadReceipt(v);
            }
        });

        // Save the passed in instance state.
        lastSavedInstanceState = savedInstanceState;

        // Only start the process of initializing the view if the service
        // component is available.
        if (activity.isServiceAvailable()) {
            initializeState();
            buildView(root);
        } else {
            buildViewDelay = true;
        }

        // Flurry Notification
        Map<String, String> params = new HashMap<String, String>();
        String paramValue = null;
        if (reportReceiptSelection || reportEntryReceiptSelection || quickExpenseReceiptSelection) {
            paramValue = Flurry.PARAM_VALUE_SELECT_RECEIPT;
        } else {
            paramValue = Flurry.PARAM_VALUE_HOME;
        }
        if (paramValue != null) {
            params.put(Flurry.PARAM_NAME_VIEWED_FROM, paramValue);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_VIEWED, params);
        }

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        // Register the receipt share service status receiver with the
        // application context.
        rsStatusReceiver.setFragment(this);
        Intent serviceStatus = activity.getApplicationContext().registerReceiver(rsStatusReceiver, rsStatusFilter);
        if (serviceStatus != null) {
            handleStatus(serviceStatus);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (activity.getChangingConfigurations() == 0) {
            // Unregister the broadcast receiver.
            activity.getApplicationContext().unregisterReceiver(rsStatusReceiver);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        // Save 'DeleteReceiptReceiver'.
        if (deleteReceiptReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            deleteReceiptReceiver.setFragment(null);
            // Add to the retainer
            activity.retainer.put(DELETE_RECEIPT_RECEIVER_KEY, deleteReceiptReceiver);
        }

        // Save ReceiptUrlReceiver
        if (receiptUrlReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            receiptUrlReceiver.setFragment(null);
            // Add to the retainer
            activity.retainer.put(RECEIPT_URL_RECEIVER_KEY, receiptUrlReceiver);
        }

        // Save RetrieveUrlReceiver
        if (retrieveUrlReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            retrieveUrlReceiver.setFragment(null);
            // Add to the retainer
            activity.retainer.put(RETRIEVE_URL_RECEIVER_KEY, retrieveUrlReceiver);
        }

        // Save 'SaveReceiptReceiver'.
        if (saveReceiptReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            saveReceiptReceiver.setFragment(null);
            // Add to the retainer
            activity.retainer.put(SAVE_RECEIPT_RECEIVER_KEY, saveReceiptReceiver);
        }
        // Save the receipt share status receiver.
        if (rsStatusReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate'
            // method.
            rsStatusReceiver.setFragment(null);
            // Add to the retainer.
            activity.retainer.put(SERVICE_STATUS_RECEIVER_KEY, rsStatusReceiver);
        }

        // Save the start OCR receiver.
        // We don't want to remove the listener in the case the user
        // presess back on the screen while StartOCR was being invoked.
        // We still want to capture the result so we can update any other
        // listeners and DBs when StartOCR returns successfully.
        if (startOcrReceiver != null) {
            // startOcrReceiver.setListener(null);
            if (activity.retainer != null) {
                activity.retainer.put(START_OCR_RECEIVER_KEY, startOcrReceiver);
            }
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();

        restoreReceivers();

        ConcurCore app = (ConcurCore) activity.getApplication();
        ReceiptStoreCache receiptStoreCache = app.getReceiptStoreCache();
        if (receiptStoreCache != null && receiptStoreCache.shouldRefetchReceiptList() || isPathAvailable) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onResume: Should refetch receipt list!");
            if (ConcurCore.isConnected()) {
                // OCR: Should probably just set flag to not refetch ReceiptList before starting this activity.
                if (!startOcrOnUpload) {
                    receiptStoreCallback.doGetReceiptList();
                }
            }
        }

        activity.updateOfflineQueueBar();
    }

    protected void restoreReceivers() {
        if (activity.retainer != null) {
            // Restore 'SaveReceiptReceiver'.
            if (activity.retainer.contains(SAVE_RECEIPT_RECEIVER_KEY)) {
                saveReceiptReceiver = (SaveReceiptReceiver) activity.retainer.get(SAVE_RECEIPT_RECEIVER_KEY);
                if (saveReceiptReceiver != null) {
                    // Set the activity on the receiver.
                    saveReceiptReceiver.setFragment(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for save receipt receiver!");
                }
            }
            // Restore 'DeleteReceiptReceiver'.
            if (activity.retainer.contains(DELETE_RECEIPT_RECEIVER_KEY)) {
                deleteReceiptReceiver = (DeleteReceiptReceiver) activity.retainer.get(DELETE_RECEIPT_RECEIVER_KEY);
                if (deleteReceiptReceiver != null) {
                    // Set the activity on the receiver.
                    deleteReceiptReceiver.setFragment(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: retainer contains null reference for delete receipt receiver!");
                }
            }

            // Restore 'ReceiptUrlReceiver'.
            if (activity.retainer.contains(RECEIPT_URL_RECEIVER_KEY)) {
                receiptUrlReceiver = (ReceiptUrlReceiver) activity.retainer.get(RECEIPT_URL_RECEIVER_KEY);
                if (receiptUrlReceiver != null) {
                    // Set the activity on the receiver.
                    receiptUrlReceiver.setFragment(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: nonConfigMap contains null reference for receipt url receiver!");
                }
            }
            // Restore 'RetrieveUrlReceiver'
            if (activity.retainer.contains(RETRIEVE_URL_RECEIVER_KEY)) {
                retrieveUrlReceiver = (RetrieveUrlReceiver) activity.retainer.get(RETRIEVE_URL_RECEIVER_KEY);
                if (retrieveUrlReceiver != null) {
                    // Set the activity on the receiver.
                    retrieveUrlReceiver.setFragment(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".restoreReceivers: nonConfigMap contains null reference for retrieve url receiver!");
                }
            }
            // Restore the service status receiver.
            if (activity.retainer.contains(SERVICE_STATUS_RECEIVER_KEY)) {
                rsStatusReceiver = (ReceiptStoreStatusReceiver) activity.retainer.get(SERVICE_STATUS_RECEIVER_KEY);
                if (rsStatusReceiver != null) {
                    rsStatusReceiver.setFragment(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer has null value for service status!");
                }
            }

            // Restore the Start OCR receiver.
            if (activity.retainer.contains(START_OCR_RECEIVER_KEY)) {
                startOcrReceiver = (BaseAsyncResultReceiver) activity.retainer.get(START_OCR_RECEIVER_KEY);
                if (startOcrReceiver != null) {
                    AsyncReplyListener listener = startOcrReceiver.getListener();
                    if (listener != null) {
                        // OCR: Cancel the last listener so we're not adding the same "Processing" OCR item in the ExpenseList.
                        listener = null;
                    }
                    startOcrReceiver.setListener(startOcrReplyListener);
                }
            }

        }
    }

    /**
     * Will handle a status change update from the receipt share service.
     * 
     * @param data
     *            an <code>Intent</code> object containing the status.
     */
    protected void handleStatus(Intent data) {
        if (data.getAction() != null
                && data.getAction().equals(ReceiptShareService.ACTION_RECEIPT_SHARE_SERVICE_UPDATE)) {
            String statusStr = data.getStringExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_SERVICE_STATUS);
            if (statusStr != null && statusStr.length() > 0) {
                try {
                    ReceiptShareService.Status rsSrvStatus = ReceiptShareService.Status.fromString(statusStr);
                    switch (rsSrvStatus) {
                    case FINISHED_UPLOAD: {
                        if (ConcurCore.isConnected()) {
                            receiptStoreCallback.doGetReceiptList();
                        }
                        break;
                    }
                    default: {
                        // No-op.
                    }
                    }
                } catch (IllegalArgumentException ilaExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleStatus: invalid status value '" + statusStr + "'.", ilaExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleStatus: status intent missing status!");
            }
        }
    }

    /**
     * Constructs the receipt store view.
     */
    private void buildView(final View root) {

        // Initialize the view.
        initView();

        // Check for an orientation change.
        if (!activity.orientationChange) {
            ReceiptStoreCache receiptStoreCache = getConcurCore().getReceiptStoreCache();
            if (receiptStoreCache != null) {
                // Check for no list, an expired one or the flag being set
                // indicating the list should be explicitly
                // re-fetched (due to end-user interaction).
                if (!receiptStoreCache.hasLastReceiptList() || receiptStoreCache.shouldRefetchReceiptList()
                        || isPathAvailable) {
                    // If a path to a captured image is available, first perform
                    // the upload,
                    // then retrieve a list.
                    if (isPathAvailable) {
                        uploadReceiptAfterPhotoCaptured(false, false);
                    } else {
                        if (ConcurCore.isConnected()) {
                            receiptStoreCallback.doGetReceiptList();
                        }
                    }

                } else {
                    List<ReceiptInfo> receiptInfos = receiptStoreCache.getReceiptInfoList();
                    if (receiptStoreCache.hasLastReceiptList() && (receiptInfos == null || receiptInfos.size() == 0)) {
                        // Flip to view indicating no data currently exists.
                        viewState = ViewState.NO_DATA;
                        flipViewForViewState();
                    }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: receipt store cache is null!");
            }
        } else {
            // Clear the orientation change flag.
            activity.orientationChange = false;
        }
    }

    @Override
    public Integer getTitleResource() {
        return R.string.receipt_store_title;
    }

    public void onUploadReceipt(View view) {
        registerForContextMenu(view);
        activity.openContextMenu(view);
    }

    protected void setUploadTextMessage(final View root) {

        if (root == null) {
            Log.w(Const.LOG_TAG, CLS_TAG + ".setUploadTextMessage: root View is null!");
            return;
        }

        TextView txtView = (TextView) root.findViewById(R.id.upload_receipt_message);
        if (txtView != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
            String savedLoginId = Preferences.getLogin(prefs, null);
            if (savedLoginId != null) {

            } else {
                savedLoginId = "";
            }
            if (!navigatedFromQEOrReport()) {
                txtView.setText(com.concur.mobile.base.util.Format.localizeText(activity,
                        R.string.receipt_store_upload_receipt_message, savedLoginId));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".setUploadTextMessage: unable to locate 'upload_receipt_message' text view!");
        }
    }

    public void onClick(View view) {
        final int id = view.getId();
        if (id == R.id.action_button) {
            onUploadReceipt(view);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
        boolean retVal = false;
        ContextMenu.ContextMenuInfo menuInfo = item.getMenuInfo();
        final int itemId = item.getItemId();
        if (menuInfo instanceof AdapterView.AdapterContextMenuInfo) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
            ListItem listItem = listItemAdapter.getItem(info.position);
            if (listItem instanceof ReceiptInfoListItem) {
                selectedReceiptInfo = ((ReceiptInfoListItem) listItem).getReceiptInfo();
                if (itemId == R.id.receipt_view) {
                    if (selectedReceiptInfo != null) {
                        handleViewSelectedReceiptInfo();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onContextItemSelected: selectReceiptInfo is null!");
                    }
                    retVal = true;
                } else if (itemId == R.id.receipt_delete) {
                    if (ConcurCore.isConnected()) {
                        if (selectedReceiptInfo != null) {
                            DeleteReceiptConfirmDialogHandler.show(this);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".onContextItemSelected: selectedReceiptInfo is null!");
                        }
                    } else {
                        activity.showDialog(Const.DIALOG_NO_CONNECTIVITY);
                    }
                    retVal = true;
                } else if (itemId == R.id.receipt_select_for_report) {
                    AddToReportConfirmDialogHandler.show(this);
                } else if (itemId == R.id.receipt_select_for_expense) {
                    SelectForExpenseConfirmDialogHandler.show(this);
                } else if (itemId == R.id.receipt_select_for_quick_expense) {
                    SelectForQuickExpenseConfirmDialogHandler.show(this);
                }
            }
        } else {
            if (itemId == R.id.capture_receipt_picture) {
                // if (ConcurCore.isConnected()) {
                captureReceipt();
                // } else {
                // showDialog(Const.DIALOG_NO_CONNECTIVITY);
                // }
            } else if (itemId == R.id.select_receipt_picture) {
                // if (ConcurCore.isConnected()) {
                selectReceipt();
                // } else {
                // showDialog(Const.DIALOG_NO_CONNECTIVITY);
                // }
                // retVal = true;
            }
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View,
     * android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
        final int id = view.getId();
        if (id == R.id.receipt_list) {
            android.view.MenuInflater infl = activity.getMenuInflater();
            infl.inflate(R.menu.expense_receipt_store_list_item_action, menu);
            menu.setHeaderTitle(getText(R.string.receipt_action_title));
            // Only one of the following should be present.
            if (!reportReceiptSelection) {
                MenuItem menuItem = menu.findItem(R.id.receipt_select_for_report);
                if (menuItem != null) {
                    menuItem.setVisible(false);
                }
            }
            if (!reportEntryReceiptSelection) {
                MenuItem menuItem = menu.findItem(R.id.receipt_select_for_expense);
                if (menuItem != null) {
                    menuItem.setVisible(false);
                }
            }
            if (!quickExpenseReceiptSelection) {
                MenuItem menuItem = menu.findItem(R.id.receipt_select_for_quick_expense);
                if (menuItem != null) {
                    menuItem.setVisible(false);
                }
            }
        } else if (id == R.id.add_receipt) {
            android.view.MenuInflater infl = activity.getMenuInflater();
            infl.inflate(R.menu.expense_receipt_store, menu);
            menu.setHeaderTitle(getText(R.string.receipt_action_title));
            // Hide the refresh action.
            MenuItem refreshItem = menu.findItem(R.id.refresh);
            if (refreshItem != null) {
                refreshItem.setVisible(false);
            }
        } else if (id == R.id.action_button) {
            android.view.MenuInflater infl = activity.getMenuInflater();
            infl.inflate(R.menu.expense_receipt_store, menu);
            menu.setHeaderTitle(getText(R.string.receipt_action_title));
            // Hide the refresh action.
            MenuItem refreshItem = menu.findItem(R.id.refresh);
            if (refreshItem != null) {
                refreshItem.setVisible(false);
            }
        }
    }

    public void initView() {

        // Set the upload text message.
        setUploadTextMessage(getView());

        List<ReceiptInfo> receiptInfos = getReceiptInfos();
        if (receiptInfos != null && receiptInfos.size() > 0) {
            // Report list was not updated, but there's is local cached data.
            // Flip to the view containing the list and button bar.
            viewState = ViewState.LOCAL_DATA;
            flipViewForViewState();
            // Populate the receipt list.
            configureReceiptEntries();
        } else {
            // Report list was not updated, but there's no locally cached data.
            // Flip to the view indicating no local cached data, server refresh
            // being performed.
            viewState = ViewState.NO_DATA;
            flipViewForViewState();
        }
    }

    /**
     * Gets the latest list of receipt infos.
     * 
     * @return the list of receipt infos.
     */
    private List<ReceiptInfo> getReceiptInfos() {
        List<ReceiptInfo> receiptInfos = null;
        ReceiptStoreCache receiptStoreCache = ((ConcurCore) ConcurCore.getContext()).getReceiptStoreCache();
        receiptInfos = receiptStoreCache.getReceiptInfoList();
        return receiptInfos;
    }

    protected int getDataLoadingTextResourceId() {
        int retVal = getRetrievingDataTextResourceId();
        List<ReceiptInfo> receiptInfos = getReceiptInfos();
        if (receiptInfos != null && receiptInfos.size() > 0) {
            retVal = getUpdatingDataTextResourceId();
        }
        return retVal;
    }

    protected int getRetrievingDataTextResourceId() {
        return R.string.retrieving_receipts;
    }

    protected int getUpdatingDataTextResourceId() {
        return R.string.updating_receipts;
    }

    /**
     * Configures a list adapter to populate the list with expense reports.
     */
    private void configureReceiptEntries() {
        List<ReceiptInfo> receiptInfos = getReceiptInfos();

        List<ListItem> listItems = null;
        if (receiptInfos != null) {

            // Sort the receipt info objects.
            Collections.sort(receiptInfos, new ReceiptInfoComparator());

            // Create the appropriate ListItem objects.
            listItems = new ArrayList<ListItem>(receiptInfos.size());
            int curYear = -1;
            int curMonth = -1;
            for (ReceiptInfo rcptInfo : receiptInfos) {
                ListItem listItem = new ReceiptInfoListItem(rcptInfo, RECEIPT_VIEW_TYPE, getConcurCore()
                        .getReceiptStoreCache());
                Calendar uploadDate = rcptInfo.getImageCalendar();
                if (curYear == -1 || curYear != uploadDate.get(Calendar.YEAR)
                        || curMonth != uploadDate.get(Calendar.MONTH)) {
                    curYear = uploadDate.get(Calendar.YEAR);
                    curMonth = uploadDate.get(Calendar.MONTH);
                    String header = FormatUtil.SHORT_MONTH_FULL_YEAR_DISPLAY.format(uploadDate.getTime());
                    listItems.add(new HeaderListItem(header, HEADER_VIEW_TYPE));
                }
                listItems.add(listItem);
            }
        }

        // Use the cached data to immediately display a list.
        if (listItemAdapter == null) {
            listItemAdapter = new ListItemAdapter<ListItem>(activity, listItems);
            ListView listView = (ListView) viewFlipper.getCurrentView().findViewById(R.id.receipt_list);
            if (listView != null) {
                // Prior to setting the adapter on the view, init the image
                // cache receiver to handle
                // updating the list based on images downloaded asychronously.
                ((BaseActivity) getActivity()).setImageCacheReceiver(new BaseActivity.ImageCacheReceiver<ListItem>(
                        listItemAdapter, listView));
                ((BaseActivity) getActivity()).registerImageCacheReceiver();

                listView.setAdapter(listItemAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        ListItem listItem = listItemAdapter.getItem(position);
                        if (listItem instanceof ReceiptInfoListItem) {
                            selectedReceiptInfo = ((ReceiptInfoListItem) listItem).getReceiptInfo();
                            handleViewSelectedReceiptInfo();
                        }
                    }
                });
                registerForContextMenu(listView);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureReceiptEntries: no list view found!");
            }
        } else {
            listItemAdapter.setItems(listItems);
            listItemAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Will start the process of viewing a full-sized receipt, i.e., will download receipt if not already cached and then launch
     * an appropriate viewer.
     */
    private void handleViewSelectedReceiptInfo() {
        ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
        if (rsCache.isImageDownloaded(selectedReceiptInfo)) {
            showDownloadedReceiptContent();
        } else {
            if (ConcurCore.isConnected()) {
                if (!isSelectedReceiptInfoURLExpired()) {
                    // Retrieve the contents of the receipt info URL.
                    sendRetrieveUrlRequest();
                } else {
                    // Receipt URL has expired, need to fetch a new URL, then
                    // proceed with viewing receipt.
                    sendReceiptUrlRequest();
                }
            } else {
                activity.showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
        }
    }

    /**
     * Will determine whether the currently selected receipt info object has an expired receipt image URL.
     * 
     * @return whether the URL to retrieve the full-sized receipt image has expired.
     */
    private boolean isSelectedReceiptInfoURLExpired() {
        boolean expired = false;
        if (selectedReceiptInfo != null) {
            long curTimeMillis = System.currentTimeMillis();
            long urlTimeMillis = 0L;
            if (selectedReceiptInfo.getUpdateTime() != null) {
                urlTimeMillis = selectedReceiptInfo.getUpdateTime().getTimeInMillis();
            } else {
                ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
                if (rsCache.getLastReceiptInfoListUpdateTime() != null) {
                    urlTimeMillis = rsCache.getLastReceiptInfoListUpdateTime().getTimeInMillis();
                } else {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG
                                    + ".isSelectedReceiptInfoURLCurrent: receipt store cache receipt info list update time is null!");
                }
                expired = ((curTimeMillis - urlTimeMillis) > Const.RECEIPT_STORE_RECEIPT_IMAGE_URL_EXPIRATION_MILLISECONDS);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".isSelectedReceiptInfoURLCurrent: selectedReceiptInfo is null!");
        }
        return expired;
    }

    /**
     * Will launch viewing a receipt image.
     */
    private void showDownloadedReceiptContent() {
        String fileType = selectedReceiptInfo.getFileType();
        if (fileType != null) {
            ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
            File receiptContentPath = new File(rsCache.getImageDownloadPath(selectedReceiptInfo));
            if (fileType.equalsIgnoreCase("JPG") || fileType.equalsIgnoreCase("PNG")) {
                File extStoreDir = Environment.getExternalStorageDirectory();
                File destFile = new File(extStoreDir, "receipt." + fileType.toLowerCase());
                ViewUtil.copyFile(receiptContentPath, destFile, (64 * 1024));
                String receiptContentPathStr = URLEncoder.encode(receiptContentPath.getAbsolutePath());
                receiptContentPathStr = "file:/" + receiptContentPathStr;
                Intent intent = new Intent(activity, ViewImage.class);
                try {
                    receiptContentPathStr = destFile.toURL().toExternalForm();
                } catch (MalformedURLException mlfUrlExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".showDownloadedReceiptContent: malformed URL ", mlfUrlExc);
                }
                intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, true);
                intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, receiptContentPathStr);
                intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));
                startActivity(intent);
            } else if (selectedReceiptInfo.getFileType().equalsIgnoreCase("PDF")) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(receiptContentPath), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException anfExc) {
                    // No PDF viewer installed! Display a dialog.
                    activity.showDialog(Const.DIALOG_EXPENSE_NO_PDF_VIEWER);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureReceiptEntries.onItemClick: unknown receipt file type '"
                        + ((selectedReceiptInfo.getFileType() != null) ? selectedReceiptInfo.getFileType() : "null"));
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".showDownloadedReceiptContent: fileType is null!");
        }
    }

    /**
     * Gets the last data update time.
     * 
     * @return the last data update time.
     */
    protected Calendar getLastDataUpdateTime() {
        Calendar lastDataUpdateTime;
        ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
        lastDataUpdateTime = rsCache.getLastReceiptInfoListUpdateTime();
        return lastDataUpdateTime;
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     * 
     * @return the resource id of the text string to be displayed if there exists no data to be presented in this activity.
     */
    protected int getNoDataTextResourceId() {
        return R.string.no_receipts;
    }

    /**
     * Gets the resource id of the text string to be displayed if there exists no local data but with an outstanding request to
     * retrieve data.
     * 
     * @return the resource id of the text string to be displayed if there exists no local data, but with an outstanding request
     *         to retrieve data.
     */
    protected int getNoLocalDataRefreshTextResourceId() {
        return R.string.no_local_data_server_refresh;
    }

    /**
     * Will initialize the state of the activity assuming the service is available.
     */
    private void initializeState() {
        // Check for saved state.
        if (lastSavedInstanceState != null) {
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
                // Restore whether the receipt image file should be punted post
                // save.
                deleteReceiptImageDataLocalFilePath = lastSavedInstanceState.getBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH);
            }
            if (lastSavedInstanceState.containsKey(END_USER_REFRESH_KEY)) {
                // Restore the end-user refresh.
                endUserRefresh = lastSavedInstanceState.getBoolean(END_USER_REFRESH_KEY);
            }
            if (lastSavedInstanceState.containsKey(REPORT_KEY)) {
                // Restore the report key.
                reportKey = lastSavedInstanceState.getString(REPORT_KEY);
            }
            if (lastSavedInstanceState.containsKey(REPORT_ENTRY_KEY)) {
                // Restore the entry key.
                reportEntryKey = lastSavedInstanceState.getString(REPORT_ENTRY_KEY);
            }
            if (lastSavedInstanceState.containsKey(SELECTED_RECEIPT_INFO_KEY)) {
                // Restore the selected receipt info object.
                String selectedReceiptImageId = lastSavedInstanceState.getString(SELECTED_RECEIPT_INFO_KEY);
                if (selectedReceiptImageId != null) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".initializeState: restoring receipt info image id '"
                            + selectedReceiptImageId + "'.");
                    ConcurCore ConcurCore = getConcurCore();
                    List<ReceiptInfo> receiptInfos = ConcurCore.getReceiptStoreCache().getReceiptInfoList();
                    if (receiptInfos != null) {
                        for (ReceiptInfo receiptInfo : receiptInfos) {
                            if (receiptInfo.getReceiptImageId().equalsIgnoreCase(selectedReceiptImageId)) {
                                selectedReceiptInfo = receiptInfo;
                                break;
                            }
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".initializeState: application has null receipt infos list!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initializeState: selected receipt info has null value!");
                }
            }
            // Restore the last receipt action.
            if (lastSavedInstanceState.containsKey(LAST_RECEIPT_ACTION_KEY)) {
                lastReceiptAction = ReceiptPictureSaveAction.valueOf(lastSavedInstanceState
                        .getString(LAST_RECEIPT_ACTION_KEY));
            }

            // Clear the reference.
            lastSavedInstanceState = null;
        }

        // Restore any receivers.
        restoreReceivers();
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
            receiptStoreCallback = (ReceiptStoreFragmentCallback) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ReceiptStoreFragmentCallback");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: build view present, handling result.");
            if (requestCode == REQUEST_TAKE_PICTURE) {
                if (resultCode == Activity.RESULT_OK) {
                    uploadReceiptAfterPhotoCaptured(true, false);
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling taking a photo as canceling the action.
                    Toast toast = Toast.makeText(activity, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(TakePicture): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
                if (resultCode == Activity.RESULT_OK) {
                    if (copySelectedImage(data)) {
                        // Set the last receipt action.
                        lastReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;

                        registerSaveReceiptReceiver();
                        // Send request.
                        ConcurCore ConcurCore = getConcurCore();
                        ConcurService concurService = ConcurCore.getService();
                        SaveReceiptRequest request = concurService.sendSaveReceiptRequest(activity.getUserId(),
                                receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null, true, false);
                        if (request != null) {
                            saveReceiptReceiver.setServiceRequest(saveReceiptRequest);
                            SaveReceiptProgressDialogHandler.show(this);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".onActivityResult(ChoosePicture): unable to create 'SaveReceiptRequest'!");
                            unregisterSaveReceiptReceiver();
                        }
                    } else {
                        // Flurry Notification.
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(Flurry.PARAM_NAME_FAILURE,
                                Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                        activity.showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                    }
                } else if (resultCode == Activity.RESULT_CANCELED) {
                    // Treat canceling choosing a photo as canceling the action.
                    Toast toast = Toast.makeText(activity, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChoosePicture): unhandled result code '"
                            + resultCode + "'.");
                }
            } else if (requestCode == activity.REQUEST_UPLOAD_QUEUE) {
                if (resultCode == Activity.RESULT_OK) {
                    // Refresh the list
                    // We shouldn't need to check because the result should only
                    // be OK if an upload happened.
                    if (ConcurCore.isConnected()) {
                        receiptStoreCallback.doGetReceiptList();
                    }
                }
            } else if (requestCode == REQUEST_SAVE_CAMERA_IMAGE) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onActivityResult: return from camera view to save receipt.");
                if (resultCode == Activity.RESULT_OK) {
                    Bundle extras = data.getExtras();
                    if (extras != null) {
                        String path = extras.getString(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
                        if (path != null && path.length() > 0) {
                            receiptCameraImageDataLocalFilePath = path;
                            Log.d(Const.LOG_TAG,
                                    CLS_TAG
                                            + ".onActivityResult: return from camera view to save receipt - kick off receipt saving.");
                            uploadReceiptAfterPhotoCaptured(false, false);
                        }
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
     * Will copy the image data captured by the camera.
     * 
     * @param data
     *            the intent object containing capture information.
     */
    private boolean copyCapturedImage() {
        boolean retVal = true;
        // First, assign the path written by the camera application.
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
        InputStream inputStream = ViewUtil.getInputStream(activity, data.getData());
        int angle = ViewUtil.getOrientaionAngle(activity, data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                    .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ViewUtil.closeInputStream(inputStream);
            inputStream = null;
            if (recConf != null) {
                // Copy from the input stream to an external file.
                receiptImageDataLocalFilePath = ViewUtil.createExternalMediaImageFilePath();
                inputStream = new BufferedInputStream(ViewUtil.getInputStream(activity, data.getData()), (8 * 1024));
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
     * Captures a receipt image to be imported into the receipt store.
     */
    protected void captureReceipt() {
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
        } else {
            activity.showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Selects a receipt image to be imported into the receipt store.
     */
    protected void selectReceipt() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
        } else {
            activity.showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    /**
     * Invokes the MWS request to start OCR on the receipt with the given image ID.
     * 
     * @param receiptImageId
     */
    protected void sendStartOcr(String receiptImageId) {

        // If this is an OCR user, then start OCR!!!
        if (startOcrOnUpload && Preferences.isExpenseItUser()) {

            if (!TextUtils.isEmpty(receiptImageId)) {

                if (startOcrReceiver == null) {
                    startOcrReceiver = new BaseAsyncResultReceiver(new Handler());
                    startOcrReceiver.setListener(startOcrReplyListener);
                }

                if (startOcrAsyncTask != null && startOcrAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
                    startOcrReceiver.setListener(startOcrReplyListener);
                } else {
                    StartOCRRequestTask startOcrTask = new StartOCRRequestTask(ConcurCore.getContext(),
                            REQUEST_START_OCR, startOcrReceiver, receiptImageId);
                    startOcrAsyncTask = startOcrTask.execute();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".sendStartOcr - receiptImageId is null!!!");
            }
        }
    }

    /**
     * Will send a request off to delete the currently selected receipt.
     */
    protected void sendDeleteReceiptRequest() {
        if (selectedReceiptInfo != null) {
            ConcurService concurService = activity.getConcurService();
            registerDeleteReceiptReceiver();
            deleteReceiptRequest = concurService.sendDeleteReceiptImageRequest(activity.getUserId(),
                    selectedReceiptInfo.getReceiptImageId());
            if (deleteReceiptRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: unable to create 'DeleteReceiptImage' request!");
                unregisterDeleteReceiptReceiver();
            } else {
                // Set the request object on the receiver.
                deleteReceiptReceiver.setServiceRequest(deleteReceiptRequest);
                // Show the delete receipt progress dialog.
                DeleteReceiptProgressDialogHandler.show(this);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendDeleteReceiptRequest: selectedReceiptInfo is null!");
        }
    }

    /**
     * Switches the ReceiptStoreList view to the laoding view.
     */
    public void showLoadingView() {

        viewState = ViewState.LOCAL_DATA_REFRESH;

        // This Fragment may not yet be attached to its parent Activity.
        if (this.isAdded()) {

            if (getView() != null) {
                // Set the view state to indicate data being loaded.
                ViewUtil.setTextViewText(getView(), R.id.loading_data, R.id.data_loading_text,
                    getText(getDataLoadingTextResourceId()).toString(), true);
            }

            flipViewForViewState();
        }

    }

    private BroadcastReceiver brTest = null;

    /**
     * Upload or View the receipt after tacking picture from CAMERA.
     * 
     * @param isViewImg
     *            : contains whether we need to view image immediately after taking photo.
     */
    private void uploadReceiptAfterPhotoCaptured(final boolean isViewImg, boolean bForceMWS) {
        // This flag is always set to 'true' for captured pictures.
        deleteReceiptImageDataLocalFilePath = true;
        lastReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;
        if (!isViewImg) {
            receiptImageDataLocalFilePath = receiptCameraImageDataLocalFilePath;
            registerSaveReceiptReceiver();
            // Send request.
            ConcurCore ConcurCore = getConcurCore();
            ConcurService concurService = ConcurCore.getService();
            String accessToken = Preferences.getAccessToken();
            if (startOcrOnUpload && useExpenseIt) {
                receiptStoreCallback.uploadReceiptToExpenseIt(receiptImageDataLocalFilePath);
            }
            else if (accessToken == null || bForceMWS || startOcrOnUpload) {
                // Temporary Fix for MOB-21386
                saveReceiptRequest = concurService.sendSaveReceiptRequest(activity.getUserId(),
                        receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null, true, false);
            } else {
                brTest = new BroadcastReceiver() {

                    @Override
                    public void onReceive(Context context, Intent intent) {
                        uploadReceiptAfterPhotoCaptured(isViewImg, true);
                        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
                    }
                };

                // Temporary Fix for MOB-21386
                LocalBroadcastManager.getInstance(ConcurCore.getContext()).registerReceiver(brTest,
                        new IntentFilter("temp.image.upload.fix"));

                saveReceiptRequest = concurService.sendConnectPostImageRequest(activity.getUserId(),
                        receiptImageDataLocalFilePath, deleteReceiptImageDataLocalFilePath, null, true, false);
            }
            if (saveReceiptRequest != null) {
                saveReceiptReceiver.setServiceRequest(saveReceiptRequest);
                SaveReceiptProgressDialogHandler.show(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onActivityResult(TakePicture): unable to create 'SaveReceiptRequest'!");
                unregisterSaveReceiptReceiver();
            }
        } else {
            if (copyCapturedImage()) {
                // Set the last receipt action.
                Intent it = new Intent(activity, ViewImage.class);
                StringBuilder strBldr = new StringBuilder("file://");
                strBldr.append(receiptCameraImageDataLocalFilePath);
                it.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, strBldr.toString());
                it.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, true);
                it.putExtra(Const.EXTRA_SHOW_MENU, true);
                it.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, receiptCameraImageDataLocalFilePath);
                it.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_CAMERA);
                it.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_image));

                // MOB-18940 Add Hide Create Expense flag to View Image page
                if (navigatedFromQEOrReport()) {
                    it.putExtra(Const.EXTRA_PICK_RECEIPT_FROM_EXPENSE, true);
                    this.startActivityForResult(it, REQUEST_SAVE_CAMERA_IMAGE);
                } else
                    startActivity(it);

            } else {
                // Flurry Notification.
                Map<String, String> params = new HashMap<String, String>();
                params.put(Flurry.PARAM_NAME_FAILURE,
                        Flurry.PARAM_VALUE_FAILED_TO_CAPTURE_OR_REDUCE_RESOLUTION_FOR_RECEIPT_IMAGE);
                EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

                activity.showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
            }
        }
    }

    /**
     * Will handle initiating a request to retrieve an updated receipt image URL.
     */
    protected void sendReceiptUrlRequest() {
        ConcurService concurService = activity.getConcurService();
        registerReceiptUrlReceiver();
        receiptUrlRequest = concurService.sendGetReceiptImageUrlRequest(activity.getUserId(),
                selectedReceiptInfo.getReceiptImageId());
        if (receiptUrlRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendReceiptUrlRequest: unable to create 'GetReceiptImageUrl' request!");
            unregisterReceiptUrlReceiver();
        } else {
            // Set the request object on the receiver.
            receiptUrlReceiver.setServiceRequest(receiptUrlRequest);
            // Show the delete receipt progress dialog.
            RetrieveReceiptUrlProgressDialogHandler.show(this);
        }
    }

    /**
     * Will send a request to retrieve the contents of the currently select receipt info object.
     */
    protected void sendRetrieveUrlRequest() {
        if (selectedReceiptInfo != null) {
            ConcurService concurService = activity.getConcurService();
            ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
            File receiptFilePath = new File(rsCache.getImageDownloadPath(selectedReceiptInfo));
            registerRetrieveUrlReceiver();
            retrieveUrlRequest = concurService.sendRetrieveURLRequest(selectedReceiptInfo.getImageUrl(),
                    receiptFilePath);
            if (retrieveUrlRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".sendRetrieveUrlRequest: unable to create 'RetrieveURLRequest' request!");
                unregisterRetrieveUrlReceiver();
            } else {
                // Set the request object on the receiver.
                retrieveUrlReceiver.setServiceRequest(retrieveUrlRequest);
                // Show the delete receipt progress dialog.
                RetrieveReceiptProgressDialogHandler.show(this);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".sendRetrieveUrlRequest: selectedReceiptInfo is null!");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.expense_receipt_store, menu);
        // MOB-13329
        // Updated spec states that upload receipt to Receipt Store should be
        // disabled if Receipt Store is opened from quick
        // expense for attaching a receipt.
        if (navigatedFromQEOrReport()) {
            MenuItem captureReceiptPicture = menu.findItem(R.id.capture_receipt_picture);
            MenuItem selectReceiptPicture = menu.findItem(R.id.select_receipt_picture);
            captureReceiptPicture.setVisible(false);
            selectReceiptPicture.setVisible(false);
        }
    }

    public boolean navigatedFromQEOrReport() {
        return (quickExpenseReceiptSelection || reportEntryReceiptSelection || reportReceiptSelection);
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean retVal = false;
        final int itemId = item.getItemId();
        if (itemId == R.id.capture_receipt_picture) {
            captureReceipt();
            retVal = true;
        } else if (itemId == R.id.select_receipt_picture) {
            selectReceipt();
            retVal = true;
        } else if (itemId == R.id.refresh) {
            if (ConcurCore.isConnected()) {
                endUserRefresh = true;
                receiptStoreCallback.doGetReceiptList();
            } else {
                activity.showDialog(Const.DIALOG_NO_CONNECTIVITY);
            }
            retVal = true;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the end-user refresh option.
        outState.putBoolean(END_USER_REFRESH_KEY, endUserRefresh);
        // Save the local camera receipt image data file path.
        outState.putString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY, receiptCameraImageDataLocalFilePath);
        // Save the receipt image data local file path.
        outState.putString(RECEIPT_IMAGE_FILE_PATH_KEY, receiptImageDataLocalFilePath);
        // Save whether the receipt image file should be punted post save.
        outState.putBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH, deleteReceiptImageDataLocalFilePath);
        // Save the last receipt action.
        outState.putString(LAST_RECEIPT_ACTION_KEY, lastReceiptAction.name());
        // Save whether or not to start OCR.
        outState.putBoolean(EXTRA_START_OCR_ON_UPLOAD, startOcrOnUpload);
        // Save whether or not to use ExpenseIt.
        outState.putBoolean(EXTRA_USE_EXPENSEIT, useExpenseIt);
        // Save out the receipt image id of the currently selected receipt info.
        if (selectedReceiptInfo != null) {
            outState.putString(SELECTED_RECEIPT_INFO_KEY, selectedReceiptInfo.getReceiptImageId());
        }
        // Save the report key.
        if (reportKey != null) {
            outState.putString(REPORT_KEY, reportKey);
        }
        // Save the report entry key.
        if (reportEntryKey != null) {
            outState.putString(REPORT_ENTRY_KEY, reportEntryKey);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onCreateDialog(int)
     */
    public Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        default: {
            dialog = null;
            break;
        }
        }
        return dialog;
    }

    /**
     * Will show the save receipt failed dialog.
     * 
     * @param message
     *            contains the error message.
     */
    public void showSaveReceiptFailedDialog(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_save_receipt_failed_title, message).show(
                getFragmentManager(), null);
    }

    /**
     * Will show the delete receipt failed dialog.
     * 
     * @param message
     *            contains the error message.
     */
    public void showDeleteReceiptFailedDialog(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.receipt_delete_fail_dialog_title, message).show(
                getFragmentManager(), null);
    }

    /**
     * Will show the retrieve receipt urls failed dialog.
     * 
     * @param message
     *            contains the error message.
     */
    public void showRetrieveReceiptUrlsFailedDialog(String message) {
        if (message != null && message.trim().length() > 0) {
            DialogFragmentFactory.getAlertOkayInstance(R.string.receipt_retrieve_urls_fail_dialog_title, message).show(
                    getFragmentManager(), null);
        } else {
            DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_e_receipt_unavailable_title,
                    R.string.dlg_e_receipt_unavailable).show(getFragmentManager(), null);
        }
    }

    /**
     * Will show the retrieve receipt url failed dialog.
     * 
     * @param message
     *            contains the error message.
     */
    public void showRetrieveReceiptUrlFailedDialog(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.dlg_expense_retrieve_receipt_image_url_failed_title,
                message).show(getFragmentManager(), null);
    }

    /**
     * Will show the receipt queued dialog.
     * 
     * @param message
     *            contains the message.
     */
    public void showReceiptQueuedDialog(String message) {
        DialogFragmentFactory.getAlertOkayInstance(R.string.offline_receipt_upload_title, message).show(
                getFragmentManager(), null);
    }

    static class SelectForQuickExpenseConfirmDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "SelectForQuickExpenseConfirm";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(ReceiptStoreFragment frag) {
            // Construct the callback handler.
            SelectForQuickExpenseConfirmDialogHandler dlgHndlr = new SelectForQuickExpenseConfirmDialogHandler();
            String title = frag.getText(R.string.confirm).toString();
            String message = "";
            if (frag.selectedReceiptInfo != null) {
                message = com.concur.mobile.base.util.Format.localizeText(frag.activity,
                        R.string.dlg_expense_receipt_store_confirm_select_for_quick_expense, Format.safeFormatCalendar(
                                FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                                frag.selectedReceiptInfo.getImageCalendar()));
            }
            AlertDialogFragment dlgFrag = DialogFragmentFactory.getAlertOkayCancelInstance(title, message, dlgHndlr,
                    dlgHndlr, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    Intent data = new Intent();
                    data.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY,
                            rsFrag.selectedReceiptInfo.getReceiptImageId());
                    rsFrag.activity.setResult(Activity.RESULT_OK, data);
                    rsFrag.activity.finish();
                    break;
                }
                }
            }
        }

        @Override
        public void onCancel(FragmentActivity activity, DialogInterface dialog) {
        }

    }

    static class SelectForExpenseConfirmDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "SelectForExpenseConfirm";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(ReceiptStoreFragment frag) {
            // Construct the callback handler.
            SelectForExpenseConfirmDialogHandler dlgHndlr = new SelectForExpenseConfirmDialogHandler();
            String title = frag.getText(R.string.confirm).toString();
            String message = "";
            if (frag.selectedReceiptInfo != null) {
                StringBuilder strBldr = new StringBuilder();
                strBldr.append(frag.expenseName);
                strBldr.append(" - ");
                strBldr.append(frag.expenseAmtStr);
                String expensePart = strBldr.toString();
                message = com.concur.mobile.base.util.Format.localizeText(frag.activity,
                        R.string.dlg_expense_receipt_store_confirm_select_for_expense, expensePart, Format
                                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                                        frag.selectedReceiptInfo.getImageCalendar()));
            }
            AlertDialogFragment dlgFrag = DialogFragmentFactory.getAlertOkayCancelInstance(title, message, dlgHndlr,
                    dlgHndlr, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    Intent data = new Intent();
                    data.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY,
                            rsFrag.selectedReceiptInfo.getReceiptImageId());
                    rsFrag.activity.setResult(Activity.RESULT_OK, data);
                    rsFrag.activity.finish();
                    break;
                }
                }
            }
        }
    }

    static class AddToReportConfirmDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "AddToReportConfirm";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(ReceiptStoreFragment frag) {
            // Construct the callback handler.
            AddToReportConfirmDialogHandler dlgHndlr = new AddToReportConfirmDialogHandler();
            String title = frag.getText(R.string.confirm).toString();
            String message = "";
            if (frag.selectedReceiptInfo != null) {
                message = com.concur.mobile.base.util.Format.localizeText(frag.activity,
                        R.string.dlg_expense_receipt_store_confirm_add_to_report_message, frag.reportName, Format
                                .safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                                        frag.selectedReceiptInfo.getImageCalendar()));
            }
            AlertDialogFragment dlgFrag = DialogFragmentFactory.getAlertOkayCancelInstance(title, message, dlgHndlr,
                    dlgHndlr, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    Intent data = new Intent();
                    data.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY,
                            rsFrag.selectedReceiptInfo.getReceiptImageId());
                    rsFrag.activity.setResult(Activity.RESULT_OK, data);
                    rsFrag.activity.finish();
                    break;
                }
                }
            }
        }
    }

    static class DeleteReceiptConfirmDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "DeleteReceiptConfirm";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(ReceiptStoreFragment frag) {
            // Construct the callback handler.
            DeleteReceiptConfirmDialogHandler dlgHndlr = new DeleteReceiptConfirmDialogHandler();
            String title = frag.getText(R.string.confirm).toString();
            String message = "";
            if (frag.selectedReceiptInfo != null) {
                message = com.concur.mobile.base.util.Format.localizeText(frag.activity,
                        R.string.receipt_action_delete_confirm_message, Format.safeFormatCalendar(
                                FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                                frag.selectedReceiptInfo.getImageCalendar()));
            }
            AlertDialogFragment dlgFrag = DialogFragmentFactory.getAlertOkayCancelInstance(title, message, dlgHndlr,
                    dlgHndlr, dlgHndlr);
            dlgFrag.show(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
            // Hang onto the tag of <code>frag</code>.
            dlgHndlr.fragTag = frag.getTag();
        }

        public static void dismiss(Fragment frag) {
            dismiss(frag.getFragmentManager(), DIALOG_FRAGMENT_ID);
        }

        @Override
        public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
            // Locate the current ReceiptStoreFragment based on the saved tag.
            Fragment frag = activity.getSupportFragmentManager().findFragmentByTag(fragTag);
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                switch (which) {
                case DialogInterface.BUTTON_POSITIVE: {
                    rsFrag.sendDeleteReceiptRequest();
                    break;
                }
                }
            }
        }

    }

    /**
     * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the save receipt dialog fragment.
     */
    static class SaveReceiptProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "SaveReceiptProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag) {
            // Construct the callback handler.
            SaveReceiptProgressDialogHandler dlgHndlr = new SaveReceiptProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = frag.getText(R.string.dlg_saving_receipt).toString();
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
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                if (rsFrag.saveReceiptRequest != null) {
                    rsFrag.saveReceiptRequest.cancel();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".SaveReceiptProgressDialogHandler.onCancel: saveReceiptRequest is null!");
                }
            }
        }
    }

    /**
     * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the delete receipt dialog fragment.
     */
    static class DeleteReceiptProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "DeleteReceiptProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag) {
            // Construct the callback handler.
            DeleteReceiptProgressDialogHandler dlgHndlr = new DeleteReceiptProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = frag.getText(R.string.dlg_deleting_receipt).toString();
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
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                if (rsFrag.deleteReceiptRequest != null) {
                    rsFrag.deleteReceiptRequest.cancel();
                    rsFrag.selectedReceiptInfo = null;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".DeleteReceiptProgressDialogHandler.onCancel: deleteReceiptRequest is null!");
                }
            }
        }
    }

    /**
     * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the retrieve receipt dialog fragment.
     */
    static class RetrieveReceiptProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "RetrieveReceiptProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag) {
            // Construct the callback handler.
            RetrieveReceiptProgressDialogHandler dlgHndlr = new RetrieveReceiptProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = frag.getText(R.string.dlg_expense_retrieve_receipt_image).toString();
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
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                if (rsFrag.contentFetcher != null) {
                    rsFrag.contentFetcher.cancel();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".RetrieveReceiptProgressDialogHandler.onCancel: contentFetcher is null!");
                }
            }
        }
    }

    /**
     * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the retrieve receipt URL dialog fragment.
     */
    static class RetrieveReceiptUrlProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "RetrieveReceiptUrlProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag) {
            // Construct the callback handler.
            RetrieveReceiptUrlProgressDialogHandler dlgHndlr = new RetrieveReceiptUrlProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = frag.getText(R.string.dlg_expense_retrieve_receipt_image_url).toString();
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
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                if (rsFrag.receiptUrlRequest != null) {
                    rsFrag.receiptUrlRequest.cancel();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".RetrieveReceiptUrlProgressDialogHandler.onCancel: receiptUrlRequest is null!");
                }
            }
        }
    }

    /**
     * An extension of <code>DialogFragmentHandler</code> to handle showing/dismissing the retrieve receipt URL dialog fragment.
     */
    static class RetrieveReceiptUrlsProgressDialogHandler extends DialogFragmentHandler {

        private static final String DIALOG_FRAGMENT_ID = "RetrieveReceiptUrlsProgress";

        // Contains the fragment tag for the fragment passed into the
        // <code>show</code> method.
        // This tag value is used to look-up the fragment below when this
        // callback handler needs
        // to refer to the current fragment.
        private String fragTag;

        public static void show(Fragment frag) {
            // Construct the callback handler.
            RetrieveReceiptUrlsProgressDialogHandler dlgHndlr = new RetrieveReceiptUrlsProgressDialogHandler();
            // Construct and show the dialog fragment.
            String message = frag.getText(R.string.dlg_expense_retrieve_receipts).toString();
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
            if (frag instanceof ReceiptStoreFragment) {
                ReceiptStoreFragment rsFrag = (ReceiptStoreFragment) frag;
                if (rsFrag.receiptUrlsRequest != null) {
                    rsFrag.receiptUrlsRequest.cancel();
                    if (!rsFrag.endUserRefresh) {
                        activity.finish();
                    }
                    rsFrag.endUserRefresh = false;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".RetrieveReceiptUrlsProgressDialogHandler.onCancel: receiptUrlsRequest is null!");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onPrepareDialog(int, android.app.Dialog)
     */
    public void onPrepareDialog(int id, Dialog dialog) {
        switch (id) {
        case Const.DIALOG_NO_IMAGING_CONFIGURATION: {
            AlertDialog alertDlg = (AlertDialog) dialog;
            alertDlg.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    // Dismiss the activity if there is no imaging
                    // configuration.
                    activity.finish();
                }
            });
            break;
        }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.BaseActivity#onServiceAvailable()
     */
    @Override
    public void onServiceAvailable() {
        super.onServiceAvailable();

        // If view building was delayed, then build it now.
        if (buildViewDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: build view was delayed, constructing view now.");
            initializeState();
            buildView(getView());
            buildViewDelay = false;
        }

        // If there was a delay to handling an activity result, then process it
        // now.
        if (activityResultDelay) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceAvailable: activity result was delayed, handling result now.");
            onActivityResult(activityResultRequestCode, activityResultResultCode, activityResultData);
            activityResultDelay = false;
            activityResultData = null;
        }
    }

    /**
     * Will register an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> attribute.
     */
    private void registerSaveReceiptReceiver() {
        if (saveReceiptReceiver == null) {
            saveReceiptReceiver = new SaveReceiptReceiver(this);
            if (saveReceiptFilter == null) {
                saveReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_SAVE);
            }
            activity.getApplicationContext().registerReceiver(saveReceiptReceiver, saveReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveReceiptReceiver: saveReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>SaveReceiptReceiver</code> with the application context and set the
     * <code>saveReceiptReceiver</code> to <code>null</code>.
     */
    private void unregisterSaveReceiptReceiver() {
        if (saveReceiptReceiver != null) {
            activity.getApplicationContext().unregisterReceiver(saveReceiptReceiver);
            saveReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveReceiptReceiver: saveReceiptReceiver is null!");
        }
    }

    /**
     * Will register an instance of <code>DeleteReceiptReceiver</code> with the application context and set the
     * <code>deleteReceiptReceiver</code> attribute.
     */
    private void registerDeleteReceiptReceiver() {
        if (deleteReceiptReceiver == null) {
            deleteReceiptReceiver = new DeleteReceiptReceiver(this);
            if (deleteReceiptFilter == null) {
                deleteReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_DELETED);
            }
            activity.getApplicationContext().registerReceiver(deleteReceiptReceiver, deleteReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerDeleteReceiptReceiver: deleteReceiptReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>DeleteReceiptReceiver</code> with the application context and set the
     * <code>deleteReceiptReceiver</code> to <code>null</code>.
     */
    private void unregisterDeleteReceiptReceiver() {
        if (deleteReceiptReceiver != null) {
            activity.getApplicationContext().unregisterReceiver(deleteReceiptReceiver);
            deleteReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterDeleteReceiptReceiver: deleteReceiptReceiver is null!");
        }
    }

    /**
     * Will register an instance of <code>ReceiptUrlReceiver</code> with the application context and set the
     * <code>receiptUrlReceiver</code> attribute.
     */
    public void registerReceiptUrlReceiver() {
        if (receiptUrlReceiver == null) {
            receiptUrlReceiver = new ReceiptUrlReceiver(this);
            if (receiptUrlFilter == null) {
                receiptUrlFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_IMAGE_URL_DOWNLOADED);
            }
            activity.getApplicationContext().registerReceiver(receiptUrlReceiver, receiptUrlFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerReceiptUrlReceiver: receiptUrlReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>ReceiptUrlReceiver</code> with the application context and set the
     * <code>receiptUrlReceiver</code> to <code>null</code>.
     */
    public void unregisterReceiptUrlReceiver() {
        if (receiptUrlReceiver != null) {
            activity.getApplicationContext().unregisterReceiver(receiptUrlReceiver);
            receiptUrlReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterReceiptUrlReceiver: receiptUrlReceiver is null!");
        }
    }

    /**
     * Will register an instance of <code>RetrieveUrlReceiver</code> with the application context and set the
     * <code>receiptUrlReceiver</code> attribute.
     */
    public void registerRetrieveUrlReceiver() {
        if (retrieveUrlReceiver == null) {
            retrieveUrlReceiver = new RetrieveUrlReceiver(this);
            if (retrieveUrlFilter == null) {
                retrieveUrlFilter = new IntentFilter(Const.ACTION_EXPENSE_RETRIEVE_URL);
            }
            activity.getApplicationContext().registerReceiver(retrieveUrlReceiver, retrieveUrlFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerRetrieveUrlReceiver: retrieveUrlReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>RetrieveUrlReceiver</code> with the application context and set the
     * <code>retrieveUrlReceiver</code> to <code>null</code>.
     */
    public void unregisterRetrieveUrlReceiver() {
        if (retrieveUrlReceiver != null) {
            activity.getApplicationContext().unregisterReceiver(retrieveUrlReceiver);
            retrieveUrlReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterRetrieveUrlReceiver: retrieveUrlReceiver is null!");
        }
    }

    /**
     * An extension of <code>BaseAdapter</code> for providing a list of receipts.
     */
    class ReceiptInfoListAdapter extends BaseAdapter {

        private List<ReceiptInfo> receiptInfos;

        private final Context context;

        /**
         * Constructs an instance of <code>ReceiptListAdapter</code> with a list of receipts.
         * 
         * @param receiptInfos
         *            the list of receipt info objects.
         */
        public ReceiptInfoListAdapter(Context context, List<ReceiptInfo> receiptInfos) {
            this.context = context;
            this.receiptInfos = receiptInfos;
        }

        /**
         * Sets the current list of receipt info objects.
         * 
         * @param receiptInfos
         *            the current list of receipt info objects.
         */
        void setReceiptInfos(List<ReceiptInfo> receiptInfos) {
            this.receiptInfos = receiptInfos;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getCount()
         */
        @Override
        public int getCount() {
            int count = 0;
            if (receiptInfos != null) {
                count = receiptInfos.size();
            }
            return count;
        }

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getItem(int)
         */
        @Override
        public Object getItem(int position) {
            return receiptInfos.get(position);
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

        /*
         * (non-Javadoc)
         * 
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = null;
            ReceiptInfo receiptInfo = receiptInfos.get(position);
            // Note: Due to asynchronously loading the thumbnail images via the
            // AsyncImageView class, the client
            // does not re-use views as that would require constructing a new
            // instance of AsyncImageView, setting the URL
            // and replacing any previous inflated instance.
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.expense_receipt_store_row, null);
            // Set the image upload date.
            TextView txtView = (TextView) view.findViewById(R.id.receipt_image_date);
            if (txtView != null) {
                txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                        receiptInfo.getImageCalendar()));
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt image date text view!");
            }
            // Set the image source.
            txtView = (TextView) view.findViewById(R.id.receipt_image_source);
            if (txtView != null) {
                String receiptOrigin = receiptInfo.getImageOrigin().toLowerCase();
                if (receiptOrigin.indexOf("mobile") != -1) {
                    txtView.setText(getText(R.string.receipt_source_mobile));
                } else if (receiptOrigin.indexOf("fax") != -1) {
                    txtView.setText(getText(R.string.receipt_source_fax));
                } else if (receiptOrigin.indexOf("ereceipt") != -1) {
                    txtView.setText(getText(R.string.receipt_source_fax));
                } else if (receiptOrigin.indexOf("email") != -1) {
                    txtView.setText(getText(R.string.receipt_source_email));
                } else if (receiptOrigin.indexOf("card") != -1) {
                    txtView.setText(getText(R.string.receipt_source_card));
                } else if (receiptOrigin.indexOf("imaging_ws") != -1) {
                    txtView.setText(getText(R.string.receipt_source_web_services));
                } else {
                    // Use the non-localized value.
                    txtView.setText(receiptInfo.getImageOrigin());
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt image source text view!");
            }
            // Set the image file type.
            txtView = (TextView) view.findViewById(R.id.receipt_file_type);
            if (txtView != null) {
                if (receiptInfo.getFileType().equalsIgnoreCase("JPG")
                        || receiptInfo.getFileType().equalsIgnoreCase("PNG")) {
                    txtView.setText(getText(R.string.receipt_document_type_image));
                } else if (receiptInfo.getFileType().equalsIgnoreCase("PDF")) {
                    txtView.setText(getText(R.string.receipt_document_type_pdf));
                } else {
                    // Use non-localized value.
                    txtView.setText(receiptInfo.getFileType());
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt file type text view!");
            }

            // Populate the thumnail view if a thumbnail URL exists.
            if (receiptInfo.getThumbUrl() != null && receiptInfo.getThumbUrl().length() > 0) {
                try {
                    URI thumbUri = new URL(receiptInfo.getThumbUrl()).toURI();
                    txtView = (TextView) view.findViewById(R.id.receipt_no_thumbnail_message);
                    if (txtView != null) {
                        txtView.setVisibility(View.GONE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG
                                + ".getView: unable to locate receipt no thumbnail message text view!");
                    }
                    AsyncImageView asyncImgView = (AsyncImageView) view.findViewById(R.id.receipt_thumbnail);
                    if (asyncImgView != null) {
                        asyncImgView.setVisibility(View.VISIBLE);
                        ReceiptStoreCache rsCache = getConcurCore().getReceiptStoreCache();
                        File tnDestFilePath = new File(rsCache.getThumbnailDownloadPath(receiptInfo));
                        asyncImgView.setDestinationFilePath(tnDestFilePath);
                        asyncImgView.setAsyncUri(thumbUri);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt thumbnail async image view!");
                    }
                } catch (MalformedURLException mlfUrlExc) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".getView: malformed receipt thumbnail URL '" + receiptInfo.getThumbUrl() + "'",
                            mlfUrlExc);
                } catch (URISyntaxException uriSynExc) {
                    Log.e(Const.LOG_TAG,
                            CLS_TAG + ".getView: URI syntax exception for thumbnail URL '" + receiptInfo.getThumbUrl()
                                    + "'", uriSynExc);
                }
            }
            return view;
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a save receipt action.
     * 
     * @author AndrewK
     */
    static class SaveReceiptReceiver extends BaseBroadcastReceiver<ReceiptStoreFragment, SaveReceiptRequest> {

        /**
         * Constructs an instance of <code>SaveReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        SaveReceiptReceiver(ReceiptStoreFragment fragment) {
            super(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearFragmentServiceRequest(ReceiptStoreFragment fragment) {
            fragment.saveReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            if (!fragment.startOcrOnUpload) {
                SaveReceiptProgressDialogHandler.dismiss(fragment);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_FAILED_TO_UPLOAD_RECEIPT_IMAGE);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS, Flurry.EVENT_NAME_FAILURE, params);

            fragment.showSaveReceiptFailedDialog(fragment.actionStatusErrorMessage);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {

            // Check whether 'isPathAvailable' is set to 'true', if so, then
            // store
            // an override value in the 'retainer' object to prevent the image
            // path being pulled
            // out of the intent that started this activity.
            if (fragment.isPathAvailable) {
                // Set the variable to 'false'.
                // Store the new value in the 'retainer' object where it will
                // now override the
                // value in the 'intent'.
                fragment.isPathAvailable = false;
                // Store the new value.
                if (fragment.activity.retainer != null) {
                    fragment.activity.retainer.put(IS_PATH_AVAILABLE_KEY, fragment.isPathAvailable);
                }
            }

            // Perform a sanity check that a receipt image ID is contained
            // within 'intent'.
            String receiptImageId = null;
            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                if (receiptImageId != null) {
                    receiptImageId = receiptImageId.trim();
                }
            }
            if (receiptImageId != null && receiptImageId.length() > 0) {

                if (SaveReceiptReply.OFFLINE_RECEIPT_ID.equals(receiptImageId)) {
                    // TODO
                    // Offline receipt
                    fragment.showReceiptQueuedDialog(fragment.getString(R.string.offline_receipt_upload_msg).toString());
                    // Save to the DB

                } else {

                    // Flurry Notification.
                    boolean offlineCreate = intent.getBooleanExtra(Flurry.PARAM_NAME_OFFLINE_CREATE, false);
                    if (!offlineCreate) {

                        // MOB-22375 - Google Analytics for Receipt Upload.
                        LastLocationTracker locTracker = fragment.getConcurCore().getLocationTracker();
                        Location loc = locTracker.getCurrentLocaton();
                        String lat = "0";
                        String lon = "0";
                        if (loc != null) {
                            lat = Double.toString(loc.getLatitude());
                            lon = Double.toString(loc.getLongitude());
                        }
                        String eventLabel = receiptImageId + "|" + lat + "|" + lon;
                        EventTracker.INSTANCE.track("Receipts", "Receipt Capture Location", eventLabel);

                        if (fragment.lastReceiptAction != null) {
                            Map<String, String> params = new HashMap<String, String>();
                            String paramValue = null;
                            switch (fragment.lastReceiptAction) {
                            case CHOOSE_PICTURE: {
                                paramValue = Flurry.PARAM_VALUE_CAMERA;
                                break;
                            }
                            case TAKE_PICTURE: {
                                paramValue = Flurry.PARAM_VALUE_ALBUM;
                                break;
                            }
                            }
                            if (paramValue != null) {
                                params.put(Flurry.PARAM_NAME_CAME_FROM, paramValue);
                                EventTracker.INSTANCE.track(Flurry.CATEGORY_RECEIPTS,
                                        Flurry.EVENT_NAME_RECEIPT_STORE_CREATE, params);
                            }
                        }
                    } else {

                    }
                    // For now just re-fresh the list, may want to just create
                    // the item locally and
                    // then refresh the display.
                    fragment.isPathAvailable = false;
                    if (ConcurCore.isConnected()) {
                        //Since we don't use OCR anymore directly through our MWS. Anytime we upload receipts we
                        //refresh the list only.
                        fragment.receiptStoreCallback.doGetReceiptList();
                    } else {
                        // OCR: If OCR but no connection, what to do?
                    }
                }
            } else {
                handleFailure(context, intent);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setFragmentServiceRequest(SaveReceiptRequest request) {
            fragment.saveReceiptRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            fragment.unregisterSaveReceiptReceiver();
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a delete receipt action.
     * 
     * @author AndrewK
     */
    static class DeleteReceiptReceiver extends BaseBroadcastReceiver<ReceiptStoreFragment, DeleteReceiptImageRequest> {

        // private final String CLS_TAG = ReceiptStore.CLS_TAG + "." +
        // DeleteReceiptReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>DeleteReceiptReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        DeleteReceiptReceiver(ReceiptStoreFragment fragment) {
            super(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearFragmentServiceRequest(ReceiptStoreFragment fragment) {
            fragment.deleteReceiptRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            DeleteReceiptProgressDialogHandler.dismiss(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            fragment.showDeleteReceiptFailedDialog(fragment.actionStatusErrorMessage);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {

            // Flurry Notification.
            Map<String, String> params = new HashMap<String, String>();
            params.put(Flurry.PARAM_NAME_TYPE, Flurry.PARAM_VALUE_RECEIPT_STORE_RECEIPT);
            EventTracker.INSTANCE.track(Flurry.CATEGORY_DELETE, Flurry.EVENT_NAME_ACTION, params);

            // For now just re-fresh the list, may want to just create the item
            // locally and
            // then refresh the display.
            if (ConcurCore.isConnected()) {
                fragment.receiptStoreCallback.doGetReceiptList();
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setFragmentServiceRequest(DeleteReceiptImageRequest request) {
            fragment.deleteReceiptRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            fragment.unregisterDeleteReceiptReceiver();
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a request to retrieve a URL for a
     * specific receipt. <br>
     * <b>NOTE:</b><br>
     * This receiver is used to update the URL for a receipt image if the receipt info list update time is older than 20 minutes
     * from the current time.
     * 
     * @author AndrewK
     */
    static class ReceiptUrlReceiver extends BaseBroadcastReceiver<ReceiptStoreFragment, GetReceiptImageUrlRequest> {

        private final String CLS_TAG = ReceiptStoreFragment.CLS_TAG + "." + ReceiptUrlReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>ReceiptUrlReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        ReceiptUrlReceiver(ReceiptStoreFragment fragment) {
            super(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearFragmentServiceRequest(ReceiptStoreFragment fragment) {
            fragment.receiptUrlRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            fragment.showRetrieveReceiptUrlFailedDialog(fragment.actionStatusErrorMessage);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (fragment.selectedReceiptInfo != null) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY)) {
                    // Retrieve the new URL for the receipt.
                    fragment.selectedReceiptInfo.setImageUrl(intent
                            .getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY));
                    if (fragment.selectedReceiptInfo.getImageUrl() != null) {
                        // Set the receipt update time.
                        fragment.selectedReceiptInfo.setUpdateTime(Calendar.getInstance());
                        // Kick-off viewing the receipt.
                        fragment.handleViewSelectedReceiptInfo();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent has null receipt image URL!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".handleSuccess: successful result, but missing receipt URL in intent!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: selected receipt info is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setFragmentServiceRequest(GetReceiptImageUrlRequest request) {
            fragment.receiptUrlRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            fragment.unregisterReceiptUrlReceiver();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            RetrieveReceiptUrlProgressDialogHandler.dismiss(fragment);
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of a request to retrieve a URL for a
     * specific receipt. <br>
     * <b>NOTE:</b><br>
     * This receiver is used to update the URL for a receipt image if the receipt info list update time is older than 20 minutes
     * from the current time.
     * 
     * @author AndrewK
     */
    static class RetrieveUrlReceiver extends BaseBroadcastReceiver<ReceiptStoreFragment, RetrieveURLRequest> {

        private final String CLS_TAG = ReceiptStoreFragment.CLS_TAG + "." + RetrieveUrlReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>RetrieveUrlReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        RetrieveUrlReceiver(ReceiptStoreFragment fragment) {
            super(fragment);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearFragmentServiceRequest(ReceiptStoreFragment fragment) {
            fragment.retrieveUrlRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            fragment.activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_UNAVAILABLE);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            if (fragment.selectedReceiptInfo != null) {
                if (intent.hasExtra(Const.EXTRA_EXPENSE_FILE_PATH)) {
                    fragment.showDownloadedReceiptContent();
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".handleSuccess: successful result, but missing receipt file path in intent!");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: selected receipt info is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setFragmentServiceRequest(RetrieveURLRequest request) {
            fragment.retrieveUrlRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            fragment.unregisterRetrieveUrlReceiver();
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            RetrieveReceiptProgressDialogHandler.dismiss(fragment);
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle updates to the display based on ReceiptShareService status
     * updates.
     */
    protected static class ReceiptStoreStatusReceiver extends BroadcastReceiver {

        protected ReceiptStoreFragment fragment;

        protected Intent intent;

        public void setFragment(ReceiptStoreFragment fragment) {
            this.fragment = fragment;
            if (this.fragment != null) {
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the
                    // 'setActivity', so process
                    // the intent now.
                    onReceive(fragment.activity.getApplicationContext(), intent);
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent data) {

            // Does this receiver have a current activity?
            if (fragment != null) {
                fragment.handleStatus(data);
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = data;
            }
        }

    }

}
