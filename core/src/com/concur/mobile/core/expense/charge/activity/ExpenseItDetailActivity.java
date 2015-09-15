package com.concur.mobile.core.expense.charge.activity;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.dialog.ReceiptChoiceDialogFragment;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.fragment.ExpenseItDetailActivityFragment;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.expenseit.DeleteExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;
import com.concur.mobile.platform.expenseit.ExpenseItNote;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.expenseit.GetExpenseItImageUrlAsyncTask;
import com.concur.mobile.platform.expenseit.PutExpenseItNoteAsyncTask;
import com.concur.mobile.platform.ui.common.dialog.AlertDialogFragment;
import com.concur.mobile.platform.ui.common.dialog.DialogFragmentFactory;
import com.concur.mobile.platform.ui.common.dialog.ProgressDialogFragment;
import com.concur.mobile.platform.ui.common.util.ImageUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author Elliott Jacobsen-Watts
 */
@EventTracker.EventTrackerClassName(getClassName = "Expense-AnalyzingReceiptDetail")
public class ExpenseItDetailActivity extends BaseActivity
        implements ExpenseItDetailActivityFragment.ExpenseItDetailsViewReceiptCallback, ReceiptChoiceDialogFragment.ReceiptChoiceListener {

    public static final String CLS_TAG = ExpenseItDetailActivity.class.getSimpleName();

    public static final int VIEW_RECEIPT_REQUEST_CODE = 777;

    public static final int REPLACE_RECEIPT_RESULT_CODE = 999;

    public static final String EXTRA_PREFERENCE_CONFIRM_USER_CHOICE_KEY = "EXTRA_PREFERENCE_CONFIRM_USER_CHOICE";

    public static final String EXTRA_EXPENSEIT_COMMENT_KEY = "EXTRA_EXPENSEIT_COMMENT_KEY";

    private static final String FRAGMENT_EXPENSEIT_DETAIL = "FRAGMENT_EXPENSEIT_DETAIL";

    public static final String EXPENSEIT_RECEIPT_ID_KEY = "EXPENSEIT_RECEIPT_ID_KEY";

    private static final String EXPENSEIT_PROCESSING_DIALOG_TAG = "EXPENSEIT_PROCESSING_DIALOG";

    private static final String RECEIPT_IMAGE_BITMAP_KEY = "RECEIPT_IMAGE_BITMAP_KEY";

    private static final String NEW_COMMENT_FROM_FRAGMENT_KEY = "NEW_COMMENT_FROM_FRAGMENT_KEY";

    private static final String LOCAL_IMAGE_FILE_PATH_KEY = "LOCAL_IMAGE_FILE_PATH_KEY";

    private static final String RECEIPT_IMAGE_ID_KEY = "RECEIPT_IMAGE_ID_KEY";

    private static final String METRICS_TIMING_KEY = "METRICS_TIMING_KEY";

    private static final String MENU_ACTION_KEY = "MENU_ACTION_KEY";

    private static final int MENU_ACTION_CANCEL = 1;

    private static final int MENU_ACTION_REPLACE = 2;

    private static int MENU_ACTION_EDIT = 3;

    private Menu menu;

    private ExpenseItNote newComment;

    private ProgressDialogFragment mProgressDialogFragment;

    private DeleteExpenseItReceiptAsyncTask mDeleteExpenseItReceiptAsyncTask;

    private GetExpenseItImageUrlAsyncTask mGetExpenseItImageUrlAsyncTask;

    private PutExpenseItNoteAsyncTask mPutExpenseItNoteAsyncTask;

//    Uncomment after refactoring upload receipt into an AsyncTask.
//    private SaveReceiptRequestTask mSaveReceiptRequestTask;

    private SaveReceiptRequest mSaveReceiptRequestTask;

    private static String EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER = "EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER";
    private SaveExpenseItReceiver mSaveExpenseItReceiptReceiver;

    private IntentFilter mSaveExpenseItReceiptFilter;

    private static String EXPENSEIT_RECEIPT_RECEIVER = "EXPENSEIT_RECEIPT_RECEIVER";
    private BaseAsyncResultReceiver mDeleteExpenseItReceiptReceiver;

    private static String EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER = "EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER";
    private BaseAsyncResultReceiver mGetExpenseItReceiptImageUrlReceiver;

    private static String EXPENSEIT_PUT_COMMENT_RECEIVER = "EXPENSEIT_PUT_COMMENT_RECEIVER";
    private BaseAsyncResultReceiver mPutExpenseItCommentReceiver;

//    Uncomment after refactoring upload receipt into an AsyncTask.
//    private static String EXPENSEIT_RECEIPT_UPLOAD_RECEIVER = "EXPENSEIT_RECEIPT_UPLOAD_RECEIVER";
//    private BaseAsyncResultReceiver mExpenseItReceiptUploadReceiver;

    private ExpenseItReceipt item;

    private Bitmap receiptImage;

    private String receiptImageId;

    private String localImageFilePath;

    private int menuAction = 0;

    private long metricsTiming = 0L;

    protected BaseAsyncRequestTask.AsyncReplyListener mGetExpenseItReceiptImageUrlListener = new BaseAsyncRequestTask.AsyncReplyListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            if (resultData != null) {
                onRetrieveUrlRequestSuccess(resultData);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess was called with null resultData!");
                showUnexpectedErrorDialog();
            }
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail was called. Call failed to get ExpenseItReceiptImage");
            showUnexpectedErrorDialog();
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel was called.");
            mGetExpenseItImageUrlAsyncTask.cancel(true);
        }

        @Override
        public void cleanup() {
            mGetExpenseItReceiptImageUrlReceiver = null;
        }
    };

    protected BaseAsyncRequestTask.AsyncReplyListener mDeleteExpenseItAsyncReplyListener = new BaseAsyncRequestTask.AsyncReplyListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess for DeleteExpenseItAsyncReplyListener called!");

            // Log the event.
            EventTracker.INSTANCE.trackTimings("Expense-ExpenseIt",
                    System.currentTimeMillis() - metricsTiming, "Delete Expense", "");
            onDeleteRequestSuccess(resultData);
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail for DeleteExpenseItAsyncReplyListener called!");
            hideProgressDialog();
            onDeleteRequestFailure(resultData);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel for DeleteExpenseItAsyncReplyListener called!");
            hideProgressDialog();
            mDeleteExpenseItReceiptAsyncTask.cancel(true);
        }

        @Override
        public void cleanup() {
            metricsTiming = 0L;
            mDeleteExpenseItReceiptReceiver = null;
        }
    };

    protected BaseAsyncRequestTask.AsyncReplyListener mPutCommentAsyncReplyListener = new BaseAsyncRequestTask.AsyncReplyListener() {
        @Override
        public void onRequestSuccess(Bundle resultData) {
            // Track count.
            EventTracker.INSTANCE.eventTrack("Expense-ExpenseIt", "Save Comment");

            // Track timing.
            EventTracker.INSTANCE.trackTimings("Expense-ExpenseIt",
                    System.currentTimeMillis() - metricsTiming, "Save Comment", "");

            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess for PutCommentAsyncReplyListener called.");
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            // TODO: Handle failure, currently fails silently if ExpenseIt is down.
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFailed for PutCommentAsyncReplyListener called.");
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancelled for PutCommentAsyncReplyListener called.");
            mPutExpenseItNoteAsyncTask.cancel(true);
        }

        @Override
        public void cleanup() {
            metricsTiming = 0L;
            mPutExpenseItCommentReceiver = null;
        }
    };

//    Uncomment after refactoring upload receipt into an AsyncTask.
//    protected BaseAsyncRequestTask.AsyncReplyListener mExpenseItReceiptUploadListener = new BaseAsyncRequestTask.AsyncReplyListener() {
//        @Override
//        public void onRequestSuccess(Bundle resultData) {
//            receiptImageFromUri = resultData.getString(SaveReceiptRequestTask.RECEIPT_URI_KEY);
//            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess for mExpenseItReceiptUploadListener called!" +
//                    " receiptImageFromUri is " + receiptImageFromUri);
//        }
//
//        @Override
//        public void onRequestFail(Bundle resultData) {
//            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestFail for mExpenseItReceiptUploadListener called!");
//            showReceiptUrlRetrievalDialog();
//        }
//
//        @Override
//        public void onRequestCancel(Bundle resultData) {
//            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel for mExpenseItReceiptUploadListener called!");
//        }
//
//        @Override
//        public void cleanup() {
//            mExpenseItReceiptUploadReceiver = null;
//        }
//    };

    private void showPositivePrompt(String title, String messageText) {
        String buttonText = getString(R.string.okay);
        AlertDialogFragment.OnClickListener okListener = new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                IExpenseEntryCache expEntCache = ExpenseItDetailActivity.this.getConcurCore().getExpenseEntryCache();
                expEntCache.setShouldFetchExpenseList();
                ExpenseItDetailActivity.this.setResult(Activity.RESULT_OK);
                ExpenseItDetailActivity.this.finish();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                IExpenseEntryCache expEntCache = ExpenseItDetailActivity.this.getConcurCore().getExpenseEntryCache();
                expEntCache.setShouldFetchExpenseList();
                ExpenseItDetailActivity.this.setResult(Activity.RESULT_OK);
                ExpenseItDetailActivity.this.finish();
            }
        };
        DialogFragmentFactory.getPositiveDialogFragment(title, messageText, buttonText, okListener)
                .show(getSupportFragmentManager(), CLS_TAG);
    }

    private void showUnexpectedErrorDialog() {
        String title = getString(R.string.general_server_error);
        String message = getString(R.string.expenseit_unexpected_error);
        showPositivePrompt(title, message);
    }

    private void showUnexpectedErrorAlert() {
        String title = getString(R.string.expenseit_service_unavailable);
        String message = "";
        showPositivePrompt(title, message);
    }

    private void showReceiptHasFinishedProcessingPrompt() {
        String title = getString(R.string.expenseit_finished_processing_title);
        String message = getString(R.string.expenseit_finished_processing_message);
        showPositivePrompt(title, message);
    }

    private void doManualExpenseTransitionOperations() {
        Bitmap image = null;
        Exception error = null;

        // Check that the receipt exists in the DB.
        try {
            image = item.getImageData(); // context is null.
        } catch (Exception e) {
            error = e;
            Log.e(Const.LOG_TAG, CLS_TAG + ".doManualExpenseTransitionOperations: image.getImageData() threw exception!\n", e);
        }

        if (error == null && image != null) { // accessing the content resolver results in nullPointerException.
            // Try to get the image from local DB.
            Log.d(Const.LOG_TAG, CLS_TAG + ".doManualExpenseTransitionOperations: got image data from DB.");
            receiptImage = image;

            // save receipt.
            if (saveReceiptImageToLocalStorage()) {
                // delete receipt call.
                doDeleteExpenseItExpenseAsyncTask();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".doManualExpenseTransitionOperations: failed to save image to storage!");
                showUnexpectedErrorAlert();
            }

        } else {
            // Get the image from ExpenseIt service. If the receipt has been exported, then
            // this call will fail. This is expected and handled.
            Log.d(Const.LOG_TAG, CLS_TAG + ".doManualExpenseTransitionOperations: getting image from ExpenseIt.");
            doGetExpenseItReceiptImageUrlAsyncTask();
        }
    }

    private boolean saveReceiptImageToLocalStorage() {
        localImageFilePath = ViewUtil.createExternalMediaImageFilePath();
        return ImageUtil.writeBitmapToFile(receiptImage, Const.RECEIPT_COMPRESS_BITMAP_FORMAT,
                Const.RECEIPT_COMPRESS_BITMAP_QUALITY, localImageFilePath);
    }

    private void doUploadReceipt() {

        if (receiptImage != null && (menuAction == MENU_ACTION_CANCEL || menuAction == MENU_ACTION_EDIT)) {
            uploadReceiptImageToReceiptStore();
        } else if (!TextUtils.isEmpty(localImageFilePath) && menuAction == MENU_ACTION_REPLACE) {
            uploadReceiptToExpenseIt(localImageFilePath);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".doUploadReceipt() - unhandled action.");
            if (currProgressDialog != null) {
                currProgressDialog.dismiss();
            }
            showUnexpectedErrorDialog();
        }

    }

    private void uploadReceiptToExpenseIt(String filePath) {

        // After capturing the image, launching the ExpenseAndReceipts class
        // to upload/save the image to the R.S. and refresh the Receipts List UI.
        Intent newIt = new Intent(this, ExpensesAndReceipts.class);
        newIt.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, false);
        newIt.putExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, true);
        //We may need to check for more conditions here such as if we're connected successfully to expenseit.
        newIt.putExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, Preferences.isExpenseItUser());
        newIt.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, filePath);
        newIt.putExtra(Flurry.PARAM_NAME_FROM, Flurry.PARAM_VALUE_CAMERA);
        newIt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(newIt);
    }

    private void uploadReceiptImageToReceiptStore() {
        if (receiptImage != null) {

            boolean writtenToFile = saveReceiptImageToLocalStorage();

            if (writtenToFile) {
                metricsTiming = System.currentTimeMillis();
                registerSaveExpenseItReceiptReceiver();
                ConcurCore core = getConcurCore();
                ConcurService service = core.getService();
                SaveReceiptRequest request = service.sendSaveReceiptRequest(ExpenseItDetailActivity.this.getUserId(),
                        localImageFilePath, true, null, true, false);
                if (request != null) {
                    mSaveExpenseItReceiptReceiver.setServiceRequest(mSaveReceiptRequestTask);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptImageToReceiptStore: unable to create new request for mSaveReceiptRequestTask!");
                    hideProgressDialog();
                    unregisterSaveExpenseItReceiptReceiver();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptImageToReceiptStore: receiptImage could not be written to file!");
                hideProgressDialog();
                showUnexpectedErrorDialog();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptImageToReceiptStore: receiptImage is null!");
            hideProgressDialog();
            showUnexpectedErrorDialog();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.expense_detail);

        // Removed the default item set.
        if (getIntent().getExtras() != null && getIntent().getExtras().containsKey(ExpenseItDetailActivity.EXPENSEIT_RECEIPT_ID_KEY)) {
            // when setting the item here, the context and userID would become null because
            // we were passing an ExpenseItItem, not the ExpenseItReceipt.
            long expenseItId = getIntent().getExtras().getLong(ExpenseItDetailActivity.EXPENSEIT_RECEIPT_ID_KEY);
            // passing in the expense ID to get the ExpenseItReceipt from the DB.
            item = ExpenseUtil.getExpenseIt(this, this.getUserId(), expenseItId);
        }

        if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_EXPENSEIT_DETAIL) == null) {
            ExpenseItDetailActivityFragment frag = ExpenseItDetailActivityFragment.newInstance(item);
            getSupportFragmentManager().beginTransaction().add(R.id.container, frag, FRAGMENT_EXPENSEIT_DETAIL).commit();
        }

        configureViewHeader();
    }

    protected void configureViewHeader() {
        String title = getText(R.string.quick_expense_title).toString();
        getSupportActionBar().setTitle(title);
    }

    @Override
    public void initializeViewReceipt(long receiptId) {
        Intent intent = new Intent(this, ExpenseItReceiptView.class);
        intent.putExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID, receiptId);
        startActivityForResult(intent, VIEW_RECEIPT_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.expenseit_details_options, this.menu);

        // Only show the "replace" menu if this ExpenseIt item has failed analysis.

        if (item.isInErrorState()) {
            this.menu.findItem(R.id.replace_expenseit_receipt).setVisible(true);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (this.item.isInErrorState()) {
            menu.findItem(R.id.edit_expenseit_receipt).setVisible(true);
            menu.findItem(R.id.cancel_expenseit_receipt).setVisible(false);
        }
        return true;
    }

    private void showProgressDialog(int messageResId) {
        String message = getString(messageResId);
        mProgressDialogFragment = DialogFragmentFactory.getProgressDialog(message, false, true, null);
        mProgressDialogFragment.show(getSupportFragmentManager(), EXPENSEIT_PROCESSING_DIALOG_TAG);
    }

    private void hideProgressDialog() {
        if (mProgressDialogFragment != null) {
            mProgressDialogFragment.dismiss();
        }
    }

    private void showExpenseItCancelConfirmationPrompt() {
        String titleString = getString(R.string.confirm);
        String messageString = getString(R.string.expenseit_cancel_confirmation_message);
        AlertDialogFragment.OnClickListener yesListener = new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                EventTracker.INSTANCE.eventTrack("Expense-ExpenseIt", "Stop Analysis");
                showProgressDialog(R.string.expenseit_cancel_dialog_message);
                doManualExpenseTransitionOperations();
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                // not needed.
            }
        };

        DialogFragmentFactory.getAlertDialog(titleString, messageString, R.string.general_yes, -1, R.string.general_no,
                yesListener, null, null, null).show(getSupportFragmentManager(), CLS_TAG);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        if (id == R.id.cancel_expenseit_receipt) {
            menuAction = MENU_ACTION_CANCEL;
            showExpenseItCancelConfirmationPrompt();
        } else if (id == R.id.replace_expenseit_receipt) {
            menuAction = MENU_ACTION_REPLACE;

            EventTracker.INSTANCE.eventTrack("Expense-ExpenseIt", "Replace Receipt");

            DialogFragment receiptChoiceDialog = new ReceiptChoiceDialogFragment();
            receiptChoiceDialog.show(this.getSupportFragmentManager(), ReceiptChoiceDialogFragment.DIALOG_FRAGMENT_ID);
        } else if (id == R.id.edit_expenseit_receipt) {
            menuAction = MENU_ACTION_EDIT;

            EventTracker.INSTANCE.eventTrack("Expense-ExpenseIt", "Edit Receipt");
            showProgressDialog(R.string.expenseit_converting_dialog_message);
            doManualExpenseTransitionOperations();
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (newComment != null) {
            retainer.put(NEW_COMMENT_FROM_FRAGMENT_KEY, newComment);
        }

        // retain the comment reciever.
        if (mPutExpenseItCommentReceiver != null) {
            mPutExpenseItCommentReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(EXPENSEIT_PUT_COMMENT_RECEIVER, mPutExpenseItCommentReceiver);
            }
        }

        // Retain the delete receiver.
        if (mDeleteExpenseItReceiptReceiver != null) {
            mDeleteExpenseItReceiptReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(EXPENSEIT_RECEIPT_RECEIVER, mDeleteExpenseItReceiptReceiver);
            }
        }

        // Retain the url receiver.
        if (mGetExpenseItReceiptImageUrlReceiver != null) {
            mGetExpenseItReceiptImageUrlReceiver.setListener(null);
            if (retainer != null) {
                retainer.put(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER, mGetExpenseItReceiptImageUrlReceiver);
            }
        }

        // Retain the save result receipt.
        if (mSaveExpenseItReceiptReceiver != null) {
            mSaveExpenseItReceiptReceiver.setActivity(null);
            retainer.put(EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER, mSaveExpenseItReceiptReceiver);
        }

        // Retain the Receipt ID.
        if (!TextUtils.isEmpty(receiptImageId)) {
            retainer.put(RECEIPT_IMAGE_ID_KEY, receiptImageId);
        }

        // Retain the path of the local image.
        if (!TextUtils.isEmpty(localImageFilePath)) {
            retainer.put(LOCAL_IMAGE_FILE_PATH_KEY, localImageFilePath);
        }

        // Retain the Image bitmap.
        if (receiptImage != null) {
            retainer.put(RECEIPT_IMAGE_BITMAP_KEY, receiptImage);
        }

        // Retain the last selected menu action.
        if (menuAction != 0) {
            retainer.put(MENU_ACTION_KEY, menuAction);
        }

        // Retain the metrics timing for cancel/replace.
        if (metricsTiming != 0L) {
            retainer.put(METRICS_TIMING_KEY, metricsTiming);
        }

//        Uncomment after refactoring upload receipt into an AsyncTask.
//        // Retain the upload receiver.
//        if (mExpenseItReceiptUploadReceiver != null) {
//            mExpenseItReceiptUploadReceiver.setListener(null);
//            if (retainer != null) {
//                retainer.put(EXPENSEIT_RECEIPT_UPLOAD_RECEIVER, mExpenseItReceiptUploadListener);
//            }
//        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (retainer != null) {

            if (retainer.contains(NEW_COMMENT_FROM_FRAGMENT_KEY)) {
                newComment = (ExpenseItNote) retainer.get(NEW_COMMENT_FROM_FRAGMENT_KEY);
            }

            // Recover the comment receiver
            if (retainer.contains(EXPENSEIT_PUT_COMMENT_RECEIVER)) {
                mPutExpenseItCommentReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_PUT_COMMENT_RECEIVER);
                if (mPutExpenseItCommentReceiver != null) {
                    mPutExpenseItCommentReceiver.setListener(mPutCommentAsyncReplyListener);
                }
            }

            // Recover the delete receiver
            if (retainer.contains(EXPENSEIT_RECEIPT_RECEIVER)) {
                mDeleteExpenseItReceiptReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_RECEIPT_RECEIVER);
                if (mDeleteExpenseItReceiptReceiver != null) {
                    mDeleteExpenseItReceiptReceiver.setListener(mDeleteExpenseItAsyncReplyListener);
                }
            }

            // Recover the url receiver
            if (retainer.contains(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER)) {
                mGetExpenseItReceiptImageUrlReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER);
                if (mGetExpenseItReceiptImageUrlReceiver != null) {
                    mGetExpenseItReceiptImageUrlReceiver.setListener(mGetExpenseItReceiptImageUrlListener);
                }
            }

            // Recover upload receiver
            if (retainer.contains(EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER)) {
                mSaveExpenseItReceiptReceiver = (SaveExpenseItReceiver) retainer.get(EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER);
                if (mSaveExpenseItReceiptReceiver != null) {
                    mSaveExpenseItReceiptReceiver.setActivity(this);
                }
            }

            // Recover the Receipt ID.
            if (retainer.contains(RECEIPT_IMAGE_ID_KEY)) {
                receiptImageId = (String) retainer.get(RECEIPT_IMAGE_ID_KEY);
            }

            // Recover the path of the local image.
            if (retainer.contains(LOCAL_IMAGE_FILE_PATH_KEY)) {
                localImageFilePath = (String) retainer.get(LOCAL_IMAGE_FILE_PATH_KEY);
            }

            // Recover the Image bitmap.
            if (retainer.contains(RECEIPT_IMAGE_BITMAP_KEY)) {
                receiptImage = (Bitmap) retainer.get(RECEIPT_IMAGE_BITMAP_KEY);
            }

            // Recover the last selected menu action.
            if (retainer.contains(MENU_ACTION_KEY)) {
                menuAction = (int) retainer.get(MENU_ACTION_KEY);
            }

            // Recover the last stop/replace metrics timing.
            if (retainer.contains(METRICS_TIMING_KEY)) {
                metricsTiming = (long) retainer.get(METRICS_TIMING_KEY);
            }

        }

//        Uncomment after refactoring upload receipt into an AsyncTask.
//        // Recover the upload receiver
//        if (retainer != null) {
//            if (retainer.contains(EXPENSEIT_RECEIPT_UPLOAD_RECEIVER)) {
//                mExpenseItReceiptUploadReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_RECEIPT_UPLOAD_RECEIVER);
//                if (mExpenseItReceiptUploadReceiver != null) {
//                    mExpenseItReceiptUploadReceiver.setListener(mExpenseItReceiptUploadListener);
//                }
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VIEW_RECEIPT_REQUEST_CODE
                && resultCode == REPLACE_RECEIPT_RESULT_CODE) {

            menuAction = MENU_ACTION_REPLACE;
            DialogFragment receiptChoiceDialog = new ReceiptChoiceDialogFragment();
            receiptChoiceDialog.show(this.getSupportFragmentManager(), ReceiptChoiceDialogFragment.DIALOG_FRAGMENT_ID);
        }
    }

    //    Uncomment after refactoring upload receipt into an AsyncTask.
//    private void doUploadReceiptImageToReceiptStoreAsyncTask() {
//        // set up the receiver
//        if (mExpenseItReceiptUploadReceiver == null) {
//            mExpenseItReceiptUploadReceiver = new BaseAsyncResultReceiver(new Handler());
//            mExpenseItReceiptUploadReceiver.setListener(mExpenseItReceiptUploadListener);
//        }
//
//        // make the call, unless it's already in use.
//        if (mSaveReceiptRequestTask != null && mSaveReceiptRequestTask.getStatus() != AsyncTask.Status.FINISHED) {
//            mExpenseItReceiptUploadReceiver.setListener(mExpenseItReceiptUploadListener);
//        }
//        else {
//            mSaveReceiptRequestTask = new SaveReceiptRequestTask(this, 1,
//                    mExpenseItReceiptUploadReceiver, localReceiptImageFileUri, null);
//            mGetExpenseItImageUrlAsyncTask.execute();
//        }
//    }

    private void doGetExpenseItReceiptImageUrlAsyncTask() {
        // set up the receiver
        if (mGetExpenseItReceiptImageUrlReceiver == null) {
            mGetExpenseItReceiptImageUrlReceiver = new BaseAsyncResultReceiver(new Handler());
            mGetExpenseItReceiptImageUrlReceiver.setListener(mGetExpenseItReceiptImageUrlListener);
        }

        // make the call, unless it's already in use.
        if (mGetExpenseItImageUrlAsyncTask != null && mGetExpenseItImageUrlAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mGetExpenseItReceiptImageUrlReceiver.setListener(mGetExpenseItReceiptImageUrlListener);
        } else {
            mGetExpenseItImageUrlAsyncTask = new GetExpenseItImageUrlAsyncTask(getApplicationContext(), 1,
                    mGetExpenseItReceiptImageUrlReceiver, item.getId());
            mGetExpenseItImageUrlAsyncTask.execute();
        }
    }

    private void doSaveCommentAsyncTask() {
        // set up the receiver
        if (mPutExpenseItCommentReceiver == null) {
            mPutExpenseItCommentReceiver = new BaseAsyncResultReceiver(new Handler());
            mPutExpenseItCommentReceiver.setListener(mPutCommentAsyncReplyListener);
        }

        // make the call, unless it's already in use.
        if (mPutExpenseItNoteAsyncTask != null && mPutExpenseItNoteAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mPutExpenseItCommentReceiver.setListener(mPutCommentAsyncReplyListener);
        } else {
            metricsTiming = System.currentTimeMillis();
            mPutExpenseItNoteAsyncTask = new PutExpenseItNoteAsyncTask(getApplicationContext(), 1,
                    mPutExpenseItCommentReceiver, newComment);
            mPutExpenseItNoteAsyncTask.execute();
        }
    }

    private void doDeleteExpenseItExpenseAsyncTask() {
        // set up the receiver.
        if (mDeleteExpenseItReceiptReceiver == null) {
            mDeleteExpenseItReceiptReceiver = new BaseAsyncResultReceiver(new Handler());
            mDeleteExpenseItReceiptReceiver.setListener(mDeleteExpenseItAsyncReplyListener);
        }
        // make the call.
        // if the call is still being made, then set the listener to the current call.
        if (mDeleteExpenseItReceiptAsyncTask != null && mDeleteExpenseItReceiptAsyncTask.getStatus() != AsyncTask.Status.FINISHED) {
            mDeleteExpenseItReceiptReceiver.setListener(mDeleteExpenseItAsyncReplyListener);
        }
        // otherwise, if everything's all good, then make the call.
        else {
            metricsTiming = System.currentTimeMillis();
            mDeleteExpenseItReceiptAsyncTask = new DeleteExpenseItReceiptAsyncTask(
                    getApplicationContext(), 1, mDeleteExpenseItReceiptReceiver, item.getId());
            mDeleteExpenseItReceiptAsyncTask.execute();
        }
    }

    private void onRetrieveUrlRequestSuccess(Bundle resultData) {
        // get the response
        final ExpenseItGetImageUrlResponse response = (ExpenseItGetImageUrlResponse)
                resultData.get(GetExpenseItImageUrlAsyncTask.GET_EXPENSEIT_IMAGE_URL_RESULT_KEY);

        // save the Bitmap locally
        if (response != null) {
            if (response.getImages() != null && response.getImages().get(0) != null) {
                final String url = response.getImages().get(0).getImageDataUrl();
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRetrieveUrlRequestSuccess: response was not null, and the URL is: " + url);

                new AsyncTask<Void, Void, Void>() {
                    Exception error;
                    boolean writtenToFile = true;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            receiptImage = getBitmapFromURL(url);
                            if (receiptImage != null) {
                                writtenToFile = saveReceiptImageToLocalStorage();
                                if (!writtenToFile) {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".onRetrieveUrlRequestSuccess: did not save image to storage!");
                                    showUnexpectedErrorDialog();
                                }
                            }
                        } catch (Exception e) {
                            error = e;
                            showUnexpectedErrorDialog();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        super.onPostExecute(aVoid);
                        // Handle error scenario - if we couldn't get the receipt image,
                        // don't delete the ExpenseIt receipt.
                        if (error == null && writtenToFile) {
                            doDeleteExpenseItExpenseAsyncTask();
                        }
                    }

                }.execute();
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRetrieveUrlRequestSuccess: response.getImages() is null!");
                showUnexpectedErrorDialog();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRetrieveUrlRequestSuccess: response is null!");
            showUnexpectedErrorDialog();
        }
    }

    private void onDeleteRequestSuccess(Bundle resultData) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess has been called...");
        String erKey = DeleteExpenseItReceiptAsyncTask.DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK;
        if (resultData != null && resultData.containsKey(erKey)) {
            ErrorResponse errorResponse = (ErrorResponse) resultData.get(erKey);
            int errorCode = errorResponse.getErrorCode();
            Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called! ErrorResponse code was " + errorCode);
            // If there was no error with deletion...
            if (errorCode == ErrorResponse.ERROR_CODE_NO_ERROR) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called! Creating a new expense.");
                doUploadReceipt();
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called with error. The error code was " + errorCode);
                hideProgressDialog();
                showUnexpectedErrorAlert();
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called, but there was no ErrorResponse.");
            hideProgressDialog();
            showUnexpectedErrorAlert();
        }
    }

    private void onDeleteRequestFailure(Bundle resultData) {
        String erKey = DeleteExpenseItReceiptAsyncTask.DELETE_EXPENSEIT_RECEIPT_ASYNC_TASK;
        Log.e(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestFailure called!");
        if (resultData != null && resultData.containsKey(erKey)) {
            ErrorResponse errorResponse = (ErrorResponse) resultData.get(erKey);
            int errorCode = errorResponse.getErrorCode();
            Log.e(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestFailure called! ErrorResponse code was " + errorCode);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestFailure was called! Result data did not contain key.");
        }
        showReceiptHasFinishedProcessingPrompt();
    }

    private Bitmap getBitmapFromURL(String url) throws Exception {
        InputStream is = null;
        try {
            is = (InputStream) new URL(url).getContent();
            Bitmap d;
            if (is != null) {
                d = BitmapFactory.decodeStream(is);
            } else {
                throw new IllegalArgumentException("Receipt image could not be found!");
            }
            return d;
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getBitmapFromURL InputStream threw an exception:\n" + e.getStackTrace());
                }
            }
        }
    }

    private void registerSaveExpenseItReceiptReceiver() {
        if (mSaveExpenseItReceiptReceiver == null) {
            mSaveExpenseItReceiptReceiver = new SaveExpenseItReceiver(this);
            if (mSaveExpenseItReceiptFilter == null) {
                mSaveExpenseItReceiptFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_SAVE);
            }
            this.getApplicationContext().registerReceiver(mSaveExpenseItReceiptReceiver, mSaveExpenseItReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveExpenseItReceiptReceiver: mSaveExpenseItReceiptReceiver is null!");
        }
    }

    private void unregisterSaveExpenseItReceiptReceiver() {
        if (mSaveExpenseItReceiptReceiver != null) {
            this.getApplicationContext().unregisterReceiver(mSaveExpenseItReceiptReceiver);
            mSaveExpenseItReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveExpenseItReceiptReceiver: mSaveExpenseItReceiptReceiver is null!");
        }
    }

    private void initializeExpenseFromExpenseIt() {
        IExpenseEntryCache expEntCache = ExpenseItDetailActivity.this.getConcurCore().getExpenseEntryCache();
        Intent newExpenseIntent = new Intent(this, QuickExpense.class);
        String comment = "";
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, Expense.ExpenseEntryType.CASH.name());
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, receiptImageId);
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY, item.getCreatedAt().getTimeInMillis());
        if (newComment != null) {
            comment = newComment.getNote().getNote();
        } else if (item.getNote() != null) {
            comment = item.getNote();
        }
        newExpenseIntent.putExtra(EXTRA_EXPENSEIT_COMMENT_KEY, comment);
        newExpenseIntent.putExtra(EXTRA_PREFERENCE_CONFIRM_USER_CHOICE_KEY, true);
        startActivity(newExpenseIntent);
        expEntCache.setShouldFetchExpenseList();
        this.setResult(Activity.RESULT_OK);
        this.finish();
    }

    private void onUploadToExpenseItFinished() {
        if (menuAction == MENU_ACTION_REPLACE) {
            // This was a Receipt replace action, so go back to the ExpenseList.
            IExpenseEntryCache expEntCache = this.getConcurCore().getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();
            ExpenseItDetailActivity.this.setResult(Activity.RESULT_OK);
            ExpenseItDetailActivity.this.finish();
        } else {
            initializeExpenseFromExpenseIt();
        }
    }

    @Override
    public void onCameraSuccess(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            showProgressDialog(R.string.expenseit_replace_dialog_message);
            localImageFilePath = filePath;
            doDeleteExpenseItExpenseAsyncTask();
        } else {
            showUnexpectedErrorDialog();
        }
    }

    @Override
    public void onCameraFailure(String filePath) {
        String title = getString(R.string.dlg_expense_camera_image_import_failed_title);
        String message = getString(R.string.dlg_expense_camera_image_import_failed_message);
        DialogFragmentFactory.getAlertOkayInstance(title, message)
                .show(getSupportFragmentManager(), null);
    }

    @Override
    public void onGallerySuccess(String filePath) {
        if (!TextUtils.isEmpty(filePath)) {
            showProgressDialog(R.string.expenseit_replace_dialog_message);
            localImageFilePath = filePath;
            doDeleteExpenseItExpenseAsyncTask();
        } else {
            showUnexpectedErrorDialog();
        }
    }

    @Override
    public void onGalleryFailure(String filePath) {
        String title = getString(R.string.dlg_expense_camera_image_import_failed_title);
        String message = getString(R.string.dlg_expense_camera_image_import_failed_message);
        DialogFragmentFactory.getAlertOkayInstance(title, message);
    }

    @Override
    public void onStorageMountFailure(String filePath) {
        String title = getString(R.string.dlg_expense_no_external_storage_available_title);
        String message = getString(R.string.dlg_expense_no_external_storage_available_message);
        DialogFragmentFactory.getAlertOkayInstance(title, message);
    }

    @Override
    public void onBackPressed() {
        if (newComment != null) {
            ConcurCore ConcurCore = this.getConcurCore();
            IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
            expEntCache.setShouldFetchExpenseList();
        }
        super.onBackPressed();
    }

    @Override
    public void saveComment(String comment) {
        if (comment.equals("") && item.getNote() == null) {
            return;
        } else if (item.getNote() != null && item.getNote().equals(comment)) {
            return;
        }
        if (newComment == null) {
            newComment = new ExpenseItNote();
        }
        newComment.setInfo(comment, item.getId());
        doSaveCommentAsyncTask();
    }

    static class SaveExpenseItReceiver extends BaseBroadcastReceiver<ExpenseItDetailActivity, SaveReceiptRequest> {

        /**
         * Constructs an instance of <code>BaseBroadcastReceiver</code> associated with <code>activity</code>.
         *
         * @param activity the associated activity.
         */
        protected SaveExpenseItReceiver(ExpenseItDetailActivity activity) {
            super(activity);
        }

        @Override
        protected void setActivityServiceRequest(SaveReceiptRequest request) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".saveActivityServiceRequest called!");
            getActivity().mSaveReceiptRequestTask = request;
        }

        @Override
        protected void clearActivityServiceRequest(ExpenseItDetailActivity activity) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".clearActivityServiceRequest called!");
            getActivity().mSaveReceiptRequestTask = null;
        }

        @Override
        protected void unregisterReceiver() {
            Log.d(Const.LOG_TAG, CLS_TAG + ".unregisterReceiver called!");
            getActivity().unregisterSaveExpenseItReceiptReceiver();
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            ExpenseItDetailActivity activity = getActivity();

            // Log the event.
            EventTracker.INSTANCE.trackTimings("Expense-ReceiptStore",
                    System.currentTimeMillis() - activity.metricsTiming, "Upload Receipt", "");

            Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess called!");
            // get imageID.
            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                activity.receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess: receipt image ID is " + activity.receiptImageId);
                if (activity.receiptImageId != null) {
                    activity.receiptImageId.trim();
                    activity.hideProgressDialog();
                    activity.onUploadToExpenseItFinished();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: intent does not contain key!");
            }

        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handleFailure called!");
            getActivity().showUnexpectedErrorDialog();
        }

    }

}
