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
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.ReceiptView;
import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.fragment.ExpenseItDetailActivityFragment;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.expense.provider.ExpenseUtil;
import com.concur.mobile.platform.expenseit.DeleteExpenseItReceiptAsyncTask;
import com.concur.mobile.platform.expenseit.ErrorResponse;
import com.concur.mobile.platform.expenseit.ExpenseItGetImageUrlResponse;
import com.concur.mobile.platform.expenseit.ExpenseItReceipt;
import com.concur.mobile.platform.expenseit.GetExpenseItImageUrlAsyncTask;
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
public class ExpenseItDetailActivity extends BaseActivity implements ExpenseItDetailActivityFragment.ExpenseItDetailsViewReceiptCallback {

    public static final String CLS_TAG = ExpenseItDetailActivity.class.getSimpleName();

    private static final String FRAGMENT_EXPENSEIT_DETAIL = "FRAGMENT_EXPENSEIT_DETAIL";

    public static final String EXPENSEIT_RECEIPT_ID_KEY = "EXPENSEIT_RECEIPT_ID_KEY";

    private static final String CANCEL_EXPENSEIT_PROCESSING_DIALOG_TAG = "CANCEL_EXPENSEIT_PROCESSING_DIALOG";

    private ProgressDialogFragment mCancelProgressDialogFragment;

    private DeleteExpenseItReceiptAsyncTask mDeleteExpenseItReceiptAsyncTask;

    private GetExpenseItImageUrlAsyncTask mGetExpenseItImageUrlAsyncTask;

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

//    Uncomment after refactoring upload receipt into an AsyncTask.
//    private static String EXPENSEIT_RECEIPT_UPLOAD_RECEIVER = "EXPENSEIT_RECEIPT_UPLOAD_RECEIVER";
//    private BaseAsyncResultReceiver mExpenseItReceiptUploadReceiver;

//    private ExpenseItItem item;

    private ExpenseItReceipt item;

    private Bitmap receiptImage;

    private String receiptImageId;

    private String localImageFilePath = ViewUtil.createExternalMediaImageFilePath();

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
            onDeleteRequestSuccess(resultData);
        }

        @Override
        public void onRequestFail(Bundle resultData) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestFail for DeleteExpenseItAsyncReplyListener called!");
            hideCancelProgressDialog();
            onDeleteRequestFailure(resultData);
        }

        @Override
        public void onRequestCancel(Bundle resultData) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestCancel for DeleteExpenseItAsyncReplyListener called!");
            hideCancelProgressDialog();
            mDeleteExpenseItReceiptAsyncTask.cancel(true);
        }

        @Override
        public void cleanup() {
            mDeleteExpenseItReceiptReceiver = null;
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

    private boolean saveReceiptImageToLocalStorage() {
        return ImageUtil.writeBitmapToFile(receiptImage, Const.RECEIPT_COMPRESS_BITMAP_FORMAT,
                Const.RECEIPT_COMPRESS_BITMAP_QUALITY, localImageFilePath);
    }

    private void uploadReceiptToConcur() {
        if (receiptImage != null) {

            boolean writtenToFile = saveReceiptImageToLocalStorage();

            if (writtenToFile) {
                registerSaveExpenseItReceiptReceiver();
                ConcurCore core = getConcurCore();
                ConcurService service = core.getService();
                SaveReceiptRequest request = service.sendSaveReceiptRequest(ExpenseItDetailActivity.this.getUserId(),
                        localImageFilePath, true, null, true, false);
                if (request != null) {
                    mSaveExpenseItReceiptReceiver.setServiceRequest(mSaveReceiptRequestTask);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToConcur: unable to create new request for mSaveReceiptRequestTask!");
                    hideCancelProgressDialog();
                    unregisterSaveExpenseItReceiptReceiver();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToConcur: receiptImage could not be written to file!");
                hideCancelProgressDialog();
                showUnexpectedErrorDialog();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".uploadReceiptToConcur: receiptImage is null!");
            hideCancelProgressDialog();
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
        Intent intent = new Intent(this, ReceiptView.class);
        intent.putExtra(Const.EXTRA_EXPENSE_IT_RECEIPT_ID, receiptId);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.expenseit_details_options, menu);
        return true;
    }

    private void showCancelProgressDialog() {
        String message = getString(R.string.expenseit_cancel_dialog_message);
        mCancelProgressDialogFragment = DialogFragmentFactory.getProgressDialog(message, false, true,
                new ProgressDialogFragment.OnCancelListener() {
                    @Override
                    public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                        Log.i(Const.LOG_TAG, CLS_TAG + "cancel called on cancel request processing.");
                    }
                });
        mCancelProgressDialogFragment.show(getSupportFragmentManager(), CANCEL_EXPENSEIT_PROCESSING_DIALOG_TAG);
    }

    private void hideCancelProgressDialog() {
        if (mCancelProgressDialogFragment != null) {
            mCancelProgressDialogFragment.dismiss();
        }
    }

    private void showExpenseItCancelConfirmationPrompt() {
        String title = getString(R.string.confirm);
        String message = getString(R.string.expenseit_confirmation_message);
        AlertDialogFragment.OnClickListener yesListener = new AlertDialogFragment.OnClickListener() {
            @Override
            public void onClick(FragmentActivity activity, DialogInterface dialog, int which) {
                showCancelProgressDialog();

                Bitmap image = null;
                Exception error = null;

                // Check that the receipt exists in the DB.
                try {
                    image = item.getImageData(); // context is null.
                } catch (Exception e) {
                    error = e;
                    Log.e(Const.LOG_TAG, CLS_TAG + ".showExpenseItCancelConfirmationPrompt: image.getImageData() threw exception.");
                }

                if (error == null && image != null) { // accessing the content resolver results in nullPointerException.
                    // Try to get the image from local DB.
                    Log.d(Const.LOG_TAG, CLS_TAG + ".showExpenseItCancelConfirmationPrompt: got image data from DB.");
                    receiptImage = image;

                    // save receipt.
                    if (saveReceiptImageToLocalStorage()) {
                        // delete receipt call.
                        doDeleteExpenseItExpenseAsyncTask();
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".showExpenseItCancelConfirmationPrompt: failed to save image to storage!");
                        showUnexpectedErrorAlert();
                    }

                } else {
                    // Get the image from ExpenseIt service. If the receipt has been exported, then
                    // this call will fail. This is expected and handled.
                    Log.d(Const.LOG_TAG, CLS_TAG + ".showExpenseItCancelConfirmationPrompt: getting image from ExpenseIt.");
                    doGetExpenseItReceiptImageUrlAsyncTask();
                }
            }

            @Override
            public void onCancel(FragmentActivity activity, DialogInterface dialog) {
                // not needed.
            }
        };

        DialogFragmentFactory.getAlertDialog(title, message, R.string.general_yes, -1, R.string.general_no,
                yesListener, null, null, null).show(getSupportFragmentManager(), CLS_TAG);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        int id = item.getItemId();

        if (id == R.id.cancel_expenseit_receipt) {
            showExpenseItCancelConfirmationPrompt();
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

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
                retainer.put(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER, mGetExpenseItReceiptImageUrlListener);
            }
        }

        // Retain the save result receipt.
        if (mSaveExpenseItReceiptReceiver != null) {
            mSaveExpenseItReceiptReceiver.setActivity(null);
            retainer.put(EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER, mSaveExpenseItReceiptReceiver);
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

        // Recover the delete receiver
        if (retainer != null) {
            if (retainer.contains(EXPENSEIT_RECEIPT_RECEIVER)) {
                mDeleteExpenseItReceiptReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_RECEIPT_RECEIVER);
                if (mDeleteExpenseItReceiptReceiver != null) {
                    mDeleteExpenseItReceiptReceiver.setListener(mDeleteExpenseItAsyncReplyListener);
                }
            }
        }

        // Recover the url receiver
        if (retainer != null) {
            if (retainer.contains(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER)) {
                mGetExpenseItReceiptImageUrlReceiver = (BaseAsyncResultReceiver) retainer.get(EXPENSEIT_RECEIPT_IMAGE_URL_RECEIVER);
                if (mGetExpenseItReceiptImageUrlReceiver != null) {
                    mGetExpenseItReceiptImageUrlReceiver.setListener(mGetExpenseItReceiptImageUrlListener);
                }
            }
        }

        // Recover upload receiver
        if (retainer != null) {
            mSaveExpenseItReceiptReceiver = (SaveExpenseItReceiver) retainer.get(EXPENSEIT_SAVE_RECEIPT_IMAGE_RECEIVER);
            if (mSaveExpenseItReceiptReceiver != null) {
                mSaveExpenseItReceiptReceiver.setActivity(this);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onResume: retainer contains null reference for save receipt receiver!");
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
        }
        else {
            mGetExpenseItImageUrlAsyncTask = new GetExpenseItImageUrlAsyncTask(getApplicationContext(), 1,
                    mGetExpenseItReceiptImageUrlReceiver, item.getId());
            mGetExpenseItImageUrlAsyncTask.execute();
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
                Log.d(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: response was not null, and the URL is: " + url);

                new AsyncTask<Void, Void, Void>() {
                    Exception error;
                    boolean writtenToFile = true;

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            receiptImage = getBitmapFromURL(url);
                            if (receiptImage != null ) {
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
                Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: response.getImages() is null!");
                showUnexpectedErrorDialog();
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onRequestSuccess: response is null!");
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
                uploadReceiptToConcur();
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called with error. The error code was " + errorCode);
                hideCancelProgressDialog();
                showUnexpectedErrorAlert();
            }
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onDeleteRequestSuccess called, but there was no ErrorResponse.");
            hideCancelProgressDialog();
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
        }
        else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".registerSaveExpenseItReceiptReceiver: mSaveExpenseItReceiptReceiver is null!");
        }
    }

    private void unregisterSaveExpenseItReceiptReceiver() {
        if (mSaveExpenseItReceiptReceiver != null) {
            this.getApplicationContext().unregisterReceiver(mSaveExpenseItReceiptReceiver);
            mSaveExpenseItReceiptReceiver = null;
        }
        else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterSaveExpenseItReceiptReceiver: mSaveExpenseItReceiptReceiver is null!");
        }
    }

    private void initializeExpenseFromExpenseIt() {
        IExpenseEntryCache expEntCache = ExpenseItDetailActivity.this.getConcurCore().getExpenseEntryCache();
        Intent newExpenseIntent = new Intent(this, QuickExpense.class);
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_ENTRY_TYPE_KEY, Expense.ExpenseEntryType.CASH.name());
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY, receiptImageId);
        newExpenseIntent.putExtra(Const.EXTRA_EXPENSE_TRANSACTION_DATE_KEY, item.getCreatedAt().getTimeInMillis());
        startActivity(newExpenseIntent);
        expEntCache.setShouldFetchExpenseList();
        this.setResult(Activity.RESULT_OK);
        this.finish();
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
            Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess called!");
            // get imageID.
            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                activity.receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleSuccess: receipt image ID is " + activity.receiptImageId);
                if (activity.receiptImageId != null) {
                    activity.receiptImageId.trim();
                    activity.hideCancelProgressDialog();
                    activity.initializeExpenseFromExpenseIt();
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
