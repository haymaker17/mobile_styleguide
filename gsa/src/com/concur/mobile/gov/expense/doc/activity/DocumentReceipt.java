package com.concur.mobile.gov.expense.doc.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpStatus;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.data.ReceiptPictureSaveAction;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.expense.service.SaveReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.ContentFetcher;
import com.concur.mobile.core.util.net.ContentFetcher.ContentFetcherListener;
import com.concur.mobile.gov.expense.charge.activity.UnAppliedList;
import com.concur.mobile.gov.expense.doc.data.DsDocDetailInfo;
import com.concur.mobile.gov.expense.doc.data.GovExpense;
import com.concur.mobile.gov.expense.doc.service.AttachTMReceiptRequest;
import com.concur.mobile.gov.service.GovService;
import com.concur.mobile.gov.util.GovDBAsyncTask;
import com.concur.mobile.gov.util.GovFlurry;
import com.concur.mobile.gov.util.IGovDBListener;

public class DocumentReceipt extends BaseActivity implements ContentFetcherListener, IGovDBListener {

    private static final String CLS_TAG = DocumentReceipt.class.getSimpleName();

    private static final int DIALOG_RECEIPT_IMAGE = 1;
    private static final int DIALOG_SAVE_RECEIPT = 2;
    private static final int DIALOG_ATTACH_RECEIPT_FAILED = 3;

    private static final int REQUEST_TAKE_PICTURE = 1;
    private static final int REQUEST_CHOOSE_IMAGE = 2;
    private static final int VIEW_PDF_REQUEST_CODE = 3;

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

    // Contains the key used to store/restore the receipt image url receiver.
    private static final String GET_RECEIPT_IMAGE_URL_RECEIVER_KEY = "receipt.image.url.receiver";

    protected ReceiptImageOptionListAdapter receiptActionAdapter;

    // Contains the path provided to the camera activity in which to store a captured image.
    protected String receiptCameraImageDataLocalFilePath;

    // Contains the path within the receipt image directory of the image.
    protected String receiptImageDataLocalFilePath;

    // Contains whether or not the receipt image file referenced by 'receiptImageDataLocalFilePath'
    // should be deleted post-save.
    private boolean deleteReceiptImageDataLocalFilePath;

    // Contains whether creates new quick expense or not
    private boolean isCreatingNewQE;
    // Contains whether view from quick expense or document detail or unapplied expense.
    private boolean isFromDocumentDrillInOption;
    // Contains whether user update receipt or attach receipt;
    private boolean isUpdate = false;
    /**
     * Contains the the file created within the application's "files" directory
     * that contains this receipt image.
     */
    protected File receiptImageFile;

    /**
     * Contains a reference to the receipt image web view.
     */
    protected WebView receiptImageView;

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
     * Contains the broadcast receiver for handling the result of retrieving
     * a receipt image URL.
     */
    protected GetReceiptImageUrlReceiver getReceiptImageUrlReceiver;

    /**
     * Contains the intent filter used to register the get receipt image url receiver.
     */
    protected IntentFilter getReceiptImageUrlFilter;

    /**
     * Contains a reference to an outstanding request to retrieve an image URL.
     */
    protected GetReceiptImageUrlRequest getReceiptImageUrlRequest;

    protected AttachReceiptReceiver attachReceiptReceiver;
    protected IntentFilter attachReceiptFilter;
    protected AttachTMReceiptRequest attachReceiptRequest;

    /**
     * Contains a reference to an external storage directory in which to write files.
     */
    private File extTmpDir;

    /**
     * Contains whether or not this activity is showing the dialog indicating no PDF viewer is available.
     */
    protected String receiptImageId;

    protected DsDocDetailInfo docDetailInfo;

    protected String ccExpId;
    protected String docName;
    protected String docType;
    protected String expId;
    protected String travId;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        // Initialize the tmp download directory and image file.
        File extFilesDir = ViewUtil.getExternalFilesDir(getConcurCore());
        if (extFilesDir != null) {
            extTmpDir = new File(extFilesDir, "tmp");
            try {
                if (!extTmpDir.exists()) {
                    if (!extTmpDir.mkdirs()) {
                        Log.e(Const.LOG_TAG,
                            CLS_TAG + ".<init>: unable to initialize external storage temporary directory '"
                                + extTmpDir.getAbsolutePath() + "'.");
                        extTmpDir = null;
                    } else {
                        receiptImageFile = new File(extTmpDir, "receipt.png");
                    }
                } else {
                    receiptImageFile = new File(extTmpDir, "receipt.png");
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: exception initializing external storage temporary directory '"
                    + extTmpDir.getAbsolutePath() + "'.", exc);
                extTmpDir = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to object external files directory reference.");
        }

        super.onCreate(savedInstanceState);

        initializeState(savedInstanceState);
        // Restore any receivers.
        restoreReceivers();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            isCreatingNewQE = extras.getBoolean(Expense.CREATING_EXPENSE);
            isFromDocumentDrillInOption = extras.getBoolean(Expense.FROM_DOC_DETAIL_EXPENSE_DRILL_IN);
            if (isCreatingNewQE) {
                receiptImageId = null;
                receiptImageId = extras.getString(Expense.CREATING_EXPENSE_IMG_ID);
                buildView();
            } else {
                Bundle bundle = extras.getBundle(DocumentDetail.BUNDLE);
                if (bundle != null) {
                    docName = bundle.getString(DocumentListActivity.DOC_NAME);
                    docType = bundle.getString(DocumentListActivity.DOCTYPE);
                    expId = bundle.getString(DocumentListActivity.EXP_ID);
                    travId = bundle.getString(DocumentListActivity.TRAV_ID);
                    ccExpId = bundle.getString(UnAppliedList.CCEXPID);

                    boolean isDocNameAvail = (docName == null || docName.length() == 0) ? false : true;
                    boolean isDocTypeAvail = (docType == null || docType.length() == 0) ? false : true;
                    boolean isTravIdAvail = (travId == null || travId.length() == 0) ? false : true;

                    if (isDocNameAvail && isDocTypeAvail && isTravIdAvail) {
                        GovService service = (GovService) getConcurService();
                        GovDBAsyncTask task = new GovDBAsyncTask(docName, docType, travId, service);
                        task.setGovDBListener(this);
                        task.execute();
                    } else {
                        if (ccExpId != null) {
                            receiptImageId = null;
                            receiptImageId = extras.getString(Expense.CREATING_EXPENSE_IMG_ID);
                            buildView();
                        }
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + " .onCreate : bundle is null, so can not show anything to new screen!"
                        + "finishing activity go back and try again...");
                    finish();
                }
            }
        }
    }

    /**
     * Will initialize the state of the activity assuming the service is available.
     */
    private void initializeState(Bundle lastSavedInstanceState) {
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
        }
    }

    @Override
    public void onDocDetailListenerSucceeded(Cursor result) {
        if (result.getCount() > 0) {
            if (result.moveToFirst()) {
                docDetailInfo = new DsDocDetailInfo(result);
                if (docDetailInfo != null) {
                    receiptImageId = null;
                    if (expId != null) {
                        // Looking at expense, use that image ID
                        GovExpense exp = docDetailInfo.findExpense(expId);
                        if (exp != null) {
                            receiptImageId = exp.imageid;
                        }
                    } else if (ccExpId != null) {
                        // Looking at unapplied expense, usa that image ID
                    } else {
                        // Looking at the doc
                        receiptImageId = docDetailInfo.imageId;
                    }
                    buildView();
                } else {
                    Log.e(CLS_TAG, " .BasicListActivity : info from cursor is null. Something is  in DB table/query wrong.");
                }
            } else {
                Log.e(CLS_TAG, " .BasicListActivity : cursor is not empty but cursor.movetofirst is false");
            }
        } else {
            Log.e(CLS_TAG, " .BasicListActivity : cursor is null. Something is  in DB table/query wrong.");
        }
    }

    protected void buildView() {
        setContentView(R.layout.document_receipt);

        getSupportActionBar().setTitle(R.string.gov_docdetail_receipt);

        // Swap the views if no receipt ID
        receiptImageView = (WebView) findViewById(R.id.receipt_web_view);
        if (receiptImageId != null && receiptImageId.length() > 0) {
            receiptImageView.setVisibility(View.VISIBLE);
            findViewById(R.id.receipt_neg_view).setVisibility(View.GONE);

            // Enable on screen image zooming capabilities.
            WebSettings webSettings = receiptImageView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setUseWideViewPort(true);
            receiptImageView.setInitialScale(1);

            // Go get the receipt
            sendGetReceiptImageUrlRequest(receiptImageId);

        } else {
            findViewById(R.id.receipt_web_view).setVisibility(View.GONE);
            findViewById(R.id.receipt_neg_view).setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Save 'GetReceiptImageUrlReceiver'.
        if (getReceiptImageUrlReceiver != null) {
            // Clear the activity reference, it will be set in the 'onCreate' method.
            getReceiptImageUrlReceiver.setActivity(null);
            // Add to the map.
            retainer.put(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY, getReceiptImageUrlReceiver);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        // Restore any get receipt image receiver.
        if (retainer.contains(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY)) {
            getReceiptImageUrlReceiver = (GetReceiptImageUrlReceiver) retainer.get(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY);
            // Reset the activity reference.
            getReceiptImageUrlReceiver.setActivity(this);
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
        cleanUpReceiptFile();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dialog = null;
        switch (id) {
        case DIALOG_RECEIPT_IMAGE: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.expense_receipt_options));
            receiptActionAdapter = new ReceiptImageOptionListAdapter();
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.CHOOSE_PICTURE);
            receiptActionAdapter.options.add(ReceiptPictureSaveAction.TAKE_PICTURE);
            builder.setSingleChoiceItems(receiptActionAdapter, -1, new ReceiptImageDialogListener());
            dialog = builder.create();
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
        case com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT_UNAVAILABLE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(getText(R.string.gov_dlg_receipt_unavailable));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            return dlgBldr.create();
        }
        case com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT: {
            ProgressDialog pDialog = new ProgressDialog(this);
            pDialog.setMessage(this.getText(R.string.gov_dlg_retrieve_receipt));
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(true);
            pDialog.setOnCancelListener(new OnCancelListener() {

                @Override
                public void onCancel(DialogInterface dialog) {
                    // If they cancel this then we can't do anything. Get out.
                    finish();
                }
            });

            dialog = pDialog;
            break;
        }
        case DIALOG_ATTACH_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setMessage(getText(R.string.gov_dlg_receipt_attach_failed));
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            return dlgBldr.create();
        }
        case Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.dlg_expense_save_receipt_failed_title);
            dlgBldr.setMessage("");
            dlgBldr.setPositiveButton(this.getText(R.string.okay), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            return dlgBldr.create();
        }
        default: {
            dialog = super.onCreateDialog(id);
            break;
        }
        }
        return dialog;
    }

    protected void chooseReceiptPicture() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // lastReceiptAction = ReceiptPictureSaveAction.CHOOSE_PICTURE;
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            startActivityForResult(intent, REQUEST_CHOOSE_IMAGE);
        } else {
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    protected void takeReceiptPicture() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // lastReceiptAction = ReceiptPictureSaveAction.TAKE_PICTURE;

            // Create a file name based on the current date.
            String receiptFilePath = ViewUtil.createExternalMediaImageFilePath();
            File receiptFile = new File(receiptFilePath);
            Uri outputFileUri = Uri.fromFile(receiptFile);
            receiptCameraImageDataLocalFilePath = receiptFile.getAbsolutePath();
            Log.d(Const.LOG_TAG, CLS_TAG + ".takeReceiptPicture: receipt image path -> '"
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
            showDialog(Const.DIALOG_EXPENSE_NO_EXTERNAL_STORAGE_AVAILABLE);
        }
    }

    private void cleanUpReceiptFile() {
        if (receiptImageFile != null) {
            try {
                receiptImageFile.delete();
            } catch (SecurityException secExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onDestroy: security exception deleting receipt file '"
                    + receiptImageFile.getAbsolutePath() + "'.", secExc);
            }
        }
    }

    @Override
    public void fetchSucceeded(final String localURL, final String fileName) {
        View view = findViewById(R.id.document_receipt_view);
        if (view != null) {
            view.post(new Runnable() {

                @Override
                public void run() {
                    // Set the URL on the web view.
                    final WebView receiptImageView = (WebView) findViewById(R.id.receipt_web_view);
                    if (receiptImageView != null) {
                        receiptImageView.post(new Runnable() {

                            @Override
                            public void run() {
                                // Compute the scale of the webview based on the downloaded receipt file.
                                String localFilePath = receiptImageFile.getAbsolutePath();
                                if (!isPDF(receiptImageFile)) {
                                    // Set the WebView initial scale based on image width.
                                    setWebViewInitialScaleFromImageWidth(localFilePath);
                                    // Display the image.
                                    receiptImageView.loadUrl(localURL);
                                } else {
                                    loadPDF();
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "fetchSucceeded: can't find receipt image view!");
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchSucceeded: can't find document receipt view!");
        }
    }

    /**
     * Load pdf receipts
     * */
    private void loadPDF() {
        // PDF, attempt to launch a PDF viewer.
        File pdfFile = new File(extTmpDir, "receipt.pdf").getAbsoluteFile();
        receiptImageFile.renameTo(pdfFile);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        try {
            // Launch any PDF viewer activity.
            startActivityForResult(intent, VIEW_PDF_REQUEST_CODE);

            // Dismiss the dialog.
            dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT);
            // Flurry Notification
            logReceiptViewFlurryEvents(true);

        } catch (ActivityNotFoundException anfExc) {
            // Flurry Notification
            logReceiptViewFlurryEvents(false);
            // No PDF viewer installed! Display a dialog.
            showDialog(Const.DIALOG_EXPENSE_NO_PDF_VIEWER);
        }
    }

    @Override
    public void fetchFailed(int status, String message) {
        Log.e(Const.LOG_TAG, CLS_TAG + ".fetchFailed: fetch of document receipt failed with " + "status code ("
            + status
            + ") and reason (" + message + ").");
        View view = findViewById(R.id.document_receipt_view);
        if (view != null) {
            view.post(new Runnable() {

                @Override
                public void run() {
                    // Dismiss the progress dialog.
                    dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT);
                    // Display a dialog indicating failure.
                    showDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT_UNAVAILABLE);
                    logReceiptViewFlurryEvents(false);
                }
            });
        } else {
            logReceiptViewFlurryEvents(false);
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchFailed: can't find document receipt view!");
        }
    }

    @Override
    public void fetchCancelled(URL url) {
    }

    private boolean isPDF(File file) {
        boolean retVal = false;
        retVal = (ViewUtil.getDocumentType(file) == ViewUtil.DocumentType.PDF);
        return retVal;
    }

    /**
     * Will set the initial scale value of the webview control based on examining
     * the image width of an image stored at <code>filePath</code>.
     * 
     * @param filePath
     *            the file path containing the image.
     */
    private void setWebViewInitialScaleFromImageWidth(String filePath) {
        if (filePath != null) {
            BitmapFactory.Options bmptOpts = ViewUtil.loadBitmapBounds(filePath);
            if (bmptOpts != null) {
                Display display = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
                int width = display.getWidth();
                Double val = Double.valueOf(width) / Double.valueOf(bmptOpts.outWidth);
                val = val * 100d;
                receiptImageView.setInitialScale(val.intValue());
                // Flurry Notification
                logReceiptViewFlurryEvents(true);
            } else {
                // Flurry Notification
                logReceiptViewFlurryEvents(false);
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".setWebViewInitialScaleFromImageWidth: unable to load bitmap bounds from '" + filePath
                    + "'.");
            }
        } else {
            // Flurry Notification
            logReceiptViewFlurryEvents(false);
            Log.e(Const.LOG_TAG, CLS_TAG + ".setWebViewInitialScaleFromImageWidth: filePath is null!");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PICTURE) {
            if (resultCode == Activity.RESULT_OK) {
                removePreviousCopiedPicture();
                // This flag is always set to 'true' for captured pictures.
                deleteReceiptImageDataLocalFilePath = true;
                if (!copyCapturedImage()) {
                    logAttachReceiptFlurryEvents(false);
                    showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                } else {
                    displayAndSaveReceipt();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Treat canceling taking a photo as canceling the action.
                // lastReceiptAction = ReceiptPictureSaveAction.CANCEL;
                Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(TakePicture): unhandled result code '"
                    + resultCode + "'.");
            }
        } else if (requestCode == REQUEST_CHOOSE_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                removePreviousCopiedPicture();
                if (!copySelectedImage(data)) {
                    logAttachReceiptFlurryEvents(false);
                    showDialog(Const.DIALOG_EXPENSE_CAMERA_IMAGE_IMPORT_FAILED);
                } else {
                    displayAndSaveReceipt();
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // Treat canceling choosing a photo as canceling the action.
                // lastReceiptAction = ReceiptPictureSaveAction.CANCEL;
                Toast toast = Toast.makeText(this, getText(R.string.activity_canceled), Toast.LENGTH_SHORT);
                toast.show();
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + "onActivityResult(ChoosePicture): unhandled result code '"
                    + resultCode + "'.");
            }
        } else if (requestCode == VIEW_PDF_REQUEST_CODE) {
            View view = findViewById(R.id.document_receipt_view);
            if (view != null) {
                LinearLayout layout = (LinearLayout) findViewById(R.id.view_pdf_layout);
                layout.setVisibility(View.VISIBLE);
                Button viewPDF = (Button) findViewById(R.id.view_pdf_receipt);
                viewPDF.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        loadPDF();
                    }
                });
            }
            // cleanUpReceiptFile();
            // if (!noPDFViewDialogVisible) {
            // finish();
            // }
        }
    }

    private void removePreviousCopiedPicture() {
        if (receiptImageDataLocalFilePath != null && deleteReceiptImageDataLocalFilePath) {
            File file = new File(receiptImageDataLocalFilePath);
            if (file.exists()) {
                if (!file.delete()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: failed to delete file '"
                        + receiptImageDataLocalFilePath + "'.");
                } else {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: deleted file '"
                        + receiptImageDataLocalFilePath + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".removePreviousCopiedPicture: picture file '"
                    + receiptImageDataLocalFilePath + "does not exist!");
            }
        }
        receiptImageDataLocalFilePath = null;
        deleteReceiptImageDataLocalFilePath = false;
    }

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

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
     */
    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save the local camera receipt image data file path.
        outState.putString(RECEIPT_CAMERA_IMAGE_FILE_PATH_KEY, receiptCameraImageDataLocalFilePath);
        // Save the receipt image data local file path.
        outState.putString(RECEIPT_IMAGE_FILE_PATH_KEY, receiptImageDataLocalFilePath);
        // Save whether the receipt image file should be punted post save.
        outState.putBoolean(DELETE_RECEIPT_IMAGE_FILE_PATH, deleteReceiptImageDataLocalFilePath);
    }

    /**
     * Log flurry events for viewing receipt
     * 
     * @param isSuccess
     *            : successfully viewd
     * */
    private void logReceiptViewFlurryEvents(boolean isSuccess) {
        Map<String, String> params = new HashMap<String, String>();
        if (isCreatingNewQE) {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            params.put(GovFlurry.PARAM_NAME_VIEWED_FROM, Flurry.PARAM_VALUE_QUICK_EXPENSE);
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_VIEWED,
                params);
        } else if (isFromDocumentDrillInOption) {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            params.put(GovFlurry.PARAM_NAME_VIEWED_FROM, GovFlurry.PARAM_VALUE_VIEW_EXPENSES);
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_VIEWED,
                params);
        } else {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            params.put(GovFlurry.PARAM_NAME_VIEWED_FROM, GovFlurry.PARAM_VALUE_DOC_DETAIL);
            EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_VIEWED,
                params);
        }
    }

    /**
     * Log flurry events for attaching/updating receipt
     * 
     * @param isSuccess
     *            : successfully attahced
     * */
    private void logAttachReceiptFlurryEvents(boolean isSuccess) {
        Map<String, String> params = new HashMap<String, String>();
        if (isCreatingNewQE) {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            if (isUpdate) {
                params.put(GovFlurry.PARAM_NAME_ATTACHED_FROM, Flurry.PARAM_VALUE_QUICK_EXPENSE);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_ATTACH,
                    params);
            } else {
                params.put(GovFlurry.PARAM_NAME_UPDATE_FROM, Flurry.PARAM_VALUE_QUICK_EXPENSE);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_UPDATE,
                    params);
            }

        } else if (isFromDocumentDrillInOption) {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            if (isUpdate) {
                params.put(GovFlurry.PARAM_NAME_ATTACHED_FROM, GovFlurry.PARAM_VALUE_VIEW_EXPENSES);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_ATTACH,
                    params);
            } else {
                params.put(GovFlurry.PARAM_NAME_UPDATE_FROM, GovFlurry.PARAM_VALUE_VIEW_EXPENSES);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_UPDATE,
                    params);
            }
        } else {
            // Flurry Notifications
            if (isSuccess) {
                params.put(Flurry.PARAM_NAME_SUCCESS, Flurry.PARAM_VALUE_RECEIPT);
            } else {
                params.put(Flurry.PARAM_NAME_FAILURE, Flurry.PARAM_VALUE_RECEIPT);
            }
            if (isUpdate) {
                params.put(GovFlurry.PARAM_NAME_ATTACHED_FROM, GovFlurry.PARAM_VALUE_DOC_DETAIL);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_ATTACH,
                    params);
            } else {
                params.put(GovFlurry.PARAM_NAME_UPDATE_FROM, GovFlurry.PARAM_VALUE_DOC_DETAIL);
                EventTracker.INSTANCE.track(GovFlurry.CATEGORY_RECEIPTS, GovFlurry.EVENT_NAME_UPDATE,
                    params);
            }
        }
    }

    private boolean copySelectedImage(Intent data) {

        boolean retVal = true;

        // First, obtain the file path of the selected gallery image.
        InputStream inputStream = ViewUtil.getInputStream(this, data.getData());
        int angle = ViewUtil.getOrientaionAngle(this, data.getData());
        if (inputStream != null) {
            // Obtain the recommended sampling size, etc.
            ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                .getRecommendedSampleSizeCompressFormatQuality(inputStream);
            ViewUtil.closeInputStream(inputStream);
            if (recConf != null) {
                // Check whether the sample-size is greater than 1, if so, then perform a copy;
                // if not, then just directly use the selected receipt image file and setting
                // the "delete receipt image file" flag to 'true'.
                // Second, load a half-resolution image and then write it out to a file.
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

    protected void displayAndSaveReceipt() {
        if (receiptImageView != null) {
            // Swap the views
            receiptImageView.setVisibility(View.VISIBLE);
            findViewById(R.id.receipt_neg_view).setVisibility(View.GONE);

            // Set the WebView initial scale based on image width.
            setWebViewInitialScaleFromImageWidth(receiptImageDataLocalFilePath);

            // Display the image.
            String localURL = "file://" + receiptImageDataLocalFilePath;
            receiptImageView.loadUrl(localURL);

            // Proceed with the save
            sendSaveReceiptRequest();
        }
    }

    public void onAttachReceipt(View v) {
        if (receiptImageId != null) {
            isUpdate = true;
        } else {
            isUpdate = false;
        }
        showDialog(DIALOG_RECEIPT_IMAGE);
    }

    private void sendSaveReceiptRequest() {
        ConcurService concurService = getConcurService();
        registerSaveReceiptReceiver();
        saveReceiptRequest = concurService.sendSaveReceiptRequest(getUserId(), receiptImageDataLocalFilePath,
            deleteReceiptImageDataLocalFilePath, null, false);
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
     * Will unregister an instance of <code>SaveReceiptReceiver</code> with the
     * application context and set the <code>saveReceiptReceiver</code> to <code>null</code>.
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
     * An extension of <code>BaseBroadcastReceiver</code> for the purposes of
     * handling the response to saving a receipt.
     */
    static class SaveReceiptReceiver extends BaseBroadcastReceiver<DocumentReceipt, SaveReceiptRequest> {

        /**
         * Constructs an instance of <code>ReceiptSaveReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        protected SaveReceiptReceiver(DocumentReceipt activity) {
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
        protected void clearActivityServiceRequest(DocumentReceipt activity) {
            activity.saveReceiptRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(DIALOG_SAVE_RECEIPT);
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.logAttachReceiptFlurryEvents(false);
            activity.showDialog(Const.DIALOG_EXPENSE_SAVE_RECEIPT_FAILED);
        }

        @Override
        protected boolean handleHttpError(Context context, Intent intent, int httpStatus) {
            boolean handled = false;
            activity.logAttachReceiptFlurryEvents(false);
            if (httpStatus == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
                String mwsErrorMessage = intent.getStringExtra(Const.REPLY_ERROR_MESSAGE);
                if (mwsErrorMessage != null
                    && mwsErrorMessage.equalsIgnoreCase(Const.REPLY_IMAGING_CONFIGURATION_NOT_AVAILABLE)) {
                    activity.showDialog(Const.DIALOG_NO_IMAGING_CONFIGURATION);
                    handled = true;
                }
            }

            return handled;
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {

            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                activity.logAttachReceiptFlurryEvents(true);
                // Set the Receipt Image ID on the local reference
                String receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
                if (receiptImageId != null) {
                    receiptImageId = receiptImageId.trim();
                }
                if (receiptImageId != null && receiptImageId.length() > 0) {
                    activity.receiptImageId = receiptImageId;

                    if (activity.isCreatingNewQE) {
                        Intent it = new Intent();
                        it.putExtra(Expense.CREATING_EXPENSE_IMG_ID, activity.receiptImageId);
                        activity.dismissDialog(DocumentReceipt.DIALOG_SAVE_RECEIPT);
                        activity.setResult(Activity.RESULT_OK, it);
                        activity.finish();
                    } else {
                        // Proceed with attaching the receipt.
                        activity.sendAttachReceiptRequest();
                    }

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".handleSuccess: save receipt result intent has null/empty receipt image id!");
                    handleFailure(context, intent);
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

    protected void sendAttachReceiptRequest() {
        if (receiptImageId != null) {
            GovService govService = (GovService) getConcurService();
            registerAttachReceiptReceiver();
            attachReceiptRequest = govService.sendAttachTMReceiptRequest(receiptImageId, ccExpId, docName, docType, expId);
            if (attachReceiptRequest == null) {
                Log.e(Const.LOG_TAG, CLS_TAG
                    + ".attachReceiptRequest: unable to create attach receipt request.");
                unregisterAttachReceiptReceiver();
            } else {
                // Set the request object on the receiver.
                attachReceiptReceiver.setServiceRequest(attachReceiptRequest);
            }
        }
    }

    protected void registerAttachReceiptReceiver() {
        if (attachReceiptReceiver == null) {
            attachReceiptReceiver = new AttachReceiptReceiver(this);
            if (attachReceiptFilter == null) {
                attachReceiptFilter = new IntentFilter(com.concur.mobile.gov.util.Const.ACTION_ATTACH_TM_RECEIPT);
            }
            getApplicationContext().registerReceiver(attachReceiptReceiver, attachReceiptFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerAttachReceiptReceiver: attachReceiptReceiver is *not* null!");
        }
    }

    protected void unregisterAttachReceiptReceiver() {
        if (attachReceiptReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(attachReceiptReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttachReceiptReceiver: illegal argument", ilaExc);
            }
            attachReceiptReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterAttachReceiptReceiver: attachReceiptReceiver is null!");
        }
    }

    static class AttachReceiptReceiver extends BaseBroadcastReceiver<DocumentReceipt, AttachTMReceiptRequest> {

        AttachReceiptReceiver(DocumentReceipt activity) {
            super(activity);
        }

        @Override
        protected void clearActivityServiceRequest(DocumentReceipt activity) {
            activity.attachReceiptRequest = null;
        }

        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
        }

        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(DIALOG_ATTACH_RECEIPT_FAILED);
        }

        @Override
        protected void handleSuccess(Context context, Intent intent) {
            activity.dismissDialog(DocumentReceipt.DIALOG_SAVE_RECEIPT);
            activity.setResult(Activity.RESULT_OK);
            activity.finish();
        }

        @Override
        protected void setActivityServiceRequest(AttachTMReceiptRequest request) {
            activity.attachReceiptRequest = request;
        }

        @Override
        protected void unregisterReceiver() {
            activity.unregisterAttachReceiptReceiver();
        }

    }

    /**
     * Will send off a request to retrieve the url for an image.
     */
    protected void sendGetReceiptImageUrlRequest(String receiptImageId) {
        ConcurService concurService = getConcurService();
        registerGetReceiptImageUrlReceiver();
        getReceiptImageUrlRequest = concurService.sendGetReceiptImageUrlRequest(getUserId(), receiptImageId);
        if (getReceiptImageUrlRequest == null) {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".sendGetReceiptImageUrlRequest: unable to create get receipt image url request.");
            unregisterGetReceiptImageUrlReceiver();
        } else {
            // Set the request object on the receiver.
            getReceiptImageUrlReceiver.setServiceRequest(getReceiptImageUrlRequest);
            // Show the progress dialog.
            showDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT);
        }
    }

    /**
     * Will register an instance of <code>GetReceiptImageUrlReceiver</code> with the application context and set the
     * <code>getReceiptImageUrlReceiver</code> attribute.
     */
    protected void registerGetReceiptImageUrlReceiver() {
        if (getReceiptImageUrlReceiver == null) {
            getReceiptImageUrlReceiver = new GetReceiptImageUrlReceiver(this);
            if (getReceiptImageUrlFilter == null) {
                getReceiptImageUrlFilter = new IntentFilter(Const.ACTION_EXPENSE_RECEIPT_IMAGE_URL_DOWNLOADED);
            }
            getApplicationContext().registerReceiver(getReceiptImageUrlReceiver, getReceiptImageUrlFilter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG
                + ".registerGetReceiptImageUrlReceiver: getReceiptImageUrlReceiver is *not* null!");
        }
    }

    /**
     * Will unregister an instance of <code>GetReceiptImageUrlReceiver</code> with the
     * application context and set the <code>getReceiptImageUrlReceiver</code> to <code>null</code>.
     */
    protected void unregisterGetReceiptImageUrlReceiver() {
        if (getReceiptImageUrlReceiver != null) {
            try {
                getApplicationContext().unregisterReceiver(getReceiptImageUrlReceiver);
            } catch (IllegalArgumentException ilaExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterGetReceiptImageUrlReceiver: illegal argument", ilaExc);
            }
            getReceiptImageUrlReceiver = null;
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".unregisterGetReceiptImageUrlReceiver: getReceiptImageUrlReceiver is null!");
        }
    }

    /**
     * An extension of <code>BroadcastReceiver</code> for handling notification of
     * the result of an attempt to retrieve a receipt image Url.
     * 
     * @author AndrewK
     */
    static class GetReceiptImageUrlReceiver extends BaseBroadcastReceiver<DocumentReceipt, GetReceiptImageUrlRequest> {

        private final String CLS_TAG = DocumentReceipt.CLS_TAG + "." + GetReceiptImageUrlReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>GetReceiptImageUrlReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        GetReceiptImageUrlReceiver(DocumentReceipt activity) {
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
        protected void clearActivityServiceRequest(DocumentReceipt activity) {
            activity.getReceiptImageUrlRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            // Dismiss the progress dialog.
            activity.dismissDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT);
            // Display a dialog indicating failure.
            activity.showDialog(com.concur.mobile.gov.util.Const.DIALOG_RETRIEVE_RECEIPT_UNAVAILABLE);
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
            return handled;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleSuccess(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleSuccess(Context context, Intent intent) {
            String receiptImageUrl = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY);
            if (receiptImageUrl != null) {
                try {
                    String sessionId = Preferences.getSessionId();
                    URL url = new URL(receiptImageUrl);
                    Thread fetchThread = new Thread(new ContentFetcher(activity, url, false, sessionId, activity,
                        activity.receiptImageFile));
                    fetchThread.start();
                } catch (MalformedURLException mlfUrlExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: malformed URL retrieved for receipt image '"
                        + receiptImageUrl + "'", mlfUrlExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".handleSuccess: success but receiptImageUrl is null!");
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#setActivityServiceRequest(com.concur.mobile.activity.
         * BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(GetReceiptImageUrlRequest request) {
            activity.getReceiptImageUrlRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterGetReceiptImageUrlReceiver();
        }

    }

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

            LayoutInflater inflater = LayoutInflater.from(DocumentReceipt.this);

            int textResId = 0;
            switch (options.get(position)) {
            case TAKE_PICTURE:
                textResId = R.string.take_picture;
                break;
            case CHOOSE_PICTURE:
                textResId = R.string.select_from_device;
                break;
            default:
                break;
            }
            view = inflater.inflate(R.layout.expense_receipt_option, null);

            // Set the text.
            if (textResId != 0) {
                TextView txtView = (TextView) view.findViewById(R.id.text);
                ImageView imgView = (ImageView) view.findViewById(R.id.icon);
                if (imgView != null) {
                    imgView.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate image view!");
                }
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
                chooseReceiptPicture();
                break;
            }
            case TAKE_PICTURE: {
                takeReceiptPicture();
                break;
            }
            default:
                break;
            }
            removeDialog(DIALOG_RECEIPT_IMAGE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menuUpload) {
            onAttachReceipt(null);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
