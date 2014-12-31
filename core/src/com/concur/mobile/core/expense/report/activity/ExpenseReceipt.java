/**
 * 
 */
package com.concur.mobile.core.expense.report.activity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpStatus;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.concur.core.R;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.expense.report.data.ExpenseReportDetail;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.service.DownloadMobileEntryReceiptRequest;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.ContentFetcher;
import com.concur.mobile.core.util.net.ContentFetcher.ContentFetcherListener;
import com.concur.mobile.platform.util.Format;

/**
 * Provides an activity to display expense report receipts.
 * 
 * @author AndrewK
 */
public class ExpenseReceipt extends AbstractExpenseActivity implements ContentFetcherListener {

    private static final String CLS_TAG = ExpenseReceipt.class.getSimpleName();

    // Request code used to launch a PDF viewer.
    private static final int VIEW_PDF_REQUEST_CODE = 1;

    // Contains the key used to store/restore the receipt image url receiver.
    private static String GET_RECEIPT_IMAGE_URL_RECEIVER_KEY = "receipt.image.url.receiver";

    /**
     * Contains the the file created within the application's "files" directory that contains this receipt image.
     */
    private File receiptImageFile;

    /**
     * Contains a reference to the receipt image web view.
     */
    private WebView receiptImageView;

    /**
     * Contains a reference to an expense entry containing a receipt to be shown.
     */
    private ExpenseReportEntry expenseReportEntry;

    /**
     * Contains the broadcast receiver for handling the result of retrieving a receipt image URL.
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

    /**
     * Contains a reference to an external storage directory in which to write files.
     */
    private File extTmpDir;

    /**
     * Contains whether or not this activity is showing the dialog indicating no PDF viewer is available.
     */
    private boolean noPDFViewDialogVisible;

    private boolean isEreceipt;

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

        // Restore any receivers.
        restoreReceivers();
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
        super.restoreReceivers();
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

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getHeaderNavBarTitleResourceId()
     */
    @Override
    protected int getHeaderNavBarTitleResourceId() {
        return (expenseReportEntry == null) ? R.string.expense_entries_list_title : R.string.expense;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#isDetailReportRequired()
     */
    @Override
    protected boolean isDetailReportRequired() {
        return (reportKeySource == Const.EXPENSE_REPORT_SOURCE_ACTIVE);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener#fetchCancelled(java.net.URL)
     */
    public void fetchCancelled(URL url) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener#fetchFailed(int, java.lang.String)
     */
    public void fetchFailed(int status, String message) {

        Log.e(Const.LOG_TAG, CLS_TAG + ".fetchFailed: fetch of expense receipt failed with " + "status code (" + status
                + ") and reason (" + message + ").");
        View view = findViewById(R.id.expense_receipt_view);
        if (view != null) {
            view.post(new Runnable() {

                public void run() {
                    // Dismiss the dialog.
                    if (expenseReportEntry != null) {
                        dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
                    } else {
                        dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT);
                    }
                    if (isEreceipt) {
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES,
                                Flurry.ACTION_E_RECEIPT_IMAGE_ERROR, Flurry.LABEL_QUICK_EXPENSE_DETAIL);
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_E_RECEIPT_UNAVAILABLE);
                    } else {
                        // Display a dialog indicating failure.
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_UNAVAILABLE);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchFailed: can't find expense receipt view!");
        }
    }

    /**
     * Will send the appropriate receipt viewed request (report-level/entry-level) to the server.
     */
    private void sendReceiptViewedAuditTrail() {
        if (reportKeySource == Const.EXTRA_EXPENSE_REPORT_SOURCE_APPROVAL) {
            if (!getIntent().hasExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY)) {
                // Send a request indicating that report-level receipts have been viewed.
                getConcurService().sendReceiptsViewedRequest(expRep.reportKey);
            } else {
                if (expenseReportEntry != null) {
                    // Send a request indicating that a report entry receipt has been viewed.
                    getConcurService().sendEntryReceiptViewedRequest(expenseReportEntry.reportEntryKey);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".fetchSucceeded: expenseReportEntry is null!");
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener#fetchSucceeded(java.lang.String, java.lang.String)
     */
    public void fetchSucceeded(final String localURL, final String fileName) {

        View view = findViewById(R.id.expense_receipt_view);
        if (view != null) {
            view.post(new Runnable() {

                public void run() {
                    // Dismiss the dialog.
                    if (expenseReportEntry != null) {
                        dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
                    } else {
                        dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT);
                    }
                    // Set the URL on the web view.
                    final WebView receiptImageView = (WebView) findViewById(R.id.receipt_web_view);
                    if (receiptImageView != null) {
                        receiptImageView.post(new Runnable() {

                            public void run() {
                                // Compute the scale of the webview based on the downloaded receipt file.
                                String localFilePath = receiptImageFile.getAbsolutePath();
                                if (!isPDF(receiptImageFile)) {
                                    // Set the WebView initial scale based on image width.
                                    setWebViewInitialScaleFromImageWidth(localFilePath);
                                    // Display the image.
                                    receiptImageView.loadUrl(localURL);
                                    // Send the "receipt viewed" request.
                                    sendReceiptViewedAuditTrail();
                                } else {
                                    // PDF, attempt to launch a PDF viewer.
                                    File pdfFile = new File(extTmpDir, "receipt.pdf").getAbsoluteFile();
                                    receiptImageFile.renameTo(pdfFile);
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.fromFile(pdfFile), "application/pdf");
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    try {
                                        // Launch any PDF viewer activity.
                                        startActivityForResult(intent, VIEW_PDF_REQUEST_CODE);
                                        // Send the "receipt viewed" request.
                                        sendReceiptViewedAuditTrail();
                                    } catch (ActivityNotFoundException anfExc) {
                                        // No PDF viewer installed! Display a dialog.
                                        showDialog(Const.DIALOG_EXPENSE_NO_PDF_VIEWER);
                                    }
                                }
                            }
                        });
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + "fetchSucceeded: can't find receipt image view!");
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchSucceeded: can't find expense receipt view!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIEW_PDF_REQUEST_CODE) {
            cleanUpReceiptFile();
            if (!noPDFViewDialogVisible) {
                finish();
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = super.onCreateDialog(id);
        if (dlg != null) {
            switch (id) {
            case Const.DIALOG_EXPENSE_NO_PDF_VIEWER: {
                dlg.setOnCancelListener(new OnCancelListener() {

                    public void onCancel(DialogInterface dialog) {
                        // Clear the flag.
                        noPDFViewDialogVisible = false;
                        // Ensure the activity is finished.
                        finish();
                    }
                });
                // Set the flag.
                noPDFViewDialogVisible = true;
                break;
            }
            case Const.DIALOG_EXPENSE_RETRIEVE_E_RECEIPT_UNAVAILABLE: {
                dlg.setOnCancelListener(new OnCancelListener() {

                    @Override
                    public void onCancel(DialogInterface dialog) {
                        finish();
                    }
                });
                // Set the flag, if e- receipt is in pdf form
                noPDFViewDialogVisible = true;
                break;
            }
            }
        }
        return dlg;
    }

    /**
     * Determines whether the downloaded file is of type PDF.
     * 
     * @param filePath
     *            the absolute file path.
     * @return
     * 
     */
    private boolean isPDF(File file) {
        boolean retVal = false;
        retVal = (ViewUtil.getDocumentType(file) == ViewUtil.DocumentType.PDF);
        return retVal;
    }

    /**
     * Will set the initial scale value of the webview control based on examining the image width of an image stored at
     * <code>filePath</code>.
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
                Double val = new Double(width) / new Double(bmptOpts.outWidth);
                val = val * 100d;
                receiptImageView.setInitialScale(val.intValue());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".setWebViewInitialScaleFromImageWidth: unable to load bitmap bounds from '" + filePath
                        + "'.");
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setWebViewInitialScaleFromImageWidth: filePath is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#buildView()
     */
    @Override
    protected void buildView() {

        // Grab a reference to the expense report entry, if any.
        Intent intent = getIntent();
        String expRepEntKey = intent.getStringExtra(Const.EXTRA_EXPENSE_REPORT_ENTRY_KEY);
        if (expRepEntKey != null) {
            expenseReportEntry = expRepCache.getReportEntry(expRep, expRepEntKey);
            if (expenseReportEntry == null) {
                // Try to obtain a detailed expense report.
                ExpenseReportDetail expRepDet = expRepCache.getReportDetail(expRep.reportKey);
                if (expRepDet != null) {
                    expenseReportEntry = expRepDet.findEntryByReportKey(expRepEntKey);
                    if (expenseReportEntry != null) {
                        // Found a detailed report containing the passed in key. Set the detailed report
                        // as the expense report.
                        expRep = expRepDet;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate expense report entry ("
                                + expRepEntKey + ") in cache!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate expense report entry (" + expRepEntKey
                            + ") in cache!");
                }
            }
        }

        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            isEreceipt = bundle.getBoolean(Const.EXTRA_E_RECEIPT_EXPENSE);
        }
        // Set the content view.
        setContentView(R.layout.expense_receipt);

        // Configure the screen header.
        configureScreenHeader(expRep);

        // Configure the screen footer.
        configureScreenFooter();

        // Set the expense title header information.
        populateExpenseTitleHeaderInfo(expRep);

        // Set the receipt uri.
        receiptImageView = (WebView) findViewById(R.id.receipt_web_view);
        if (receiptImageView != null) {

            // Enable on screen image zooming capabilities.
            WebSettings webSettings = receiptImageView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setUseWideViewPort(true);
            receiptImageView.setInitialScale(1);

            try {
                // Initially, download the content to a temporary file.
                // Use the PDF url if available
                boolean usePost = true;
                String receiptUrl = expRep.realPdfUrl;
                if (receiptUrl == null || receiptUrl.trim().length() == 0) {
                    receiptUrl = expRep.receiptUrl;
                    usePost = false;
                }

                if (receiptUrl != null && receiptUrl.length() > 0) {
                    String sessionId = Preferences.getSessionId();
                    if (sessionId != null && sessionId.length() > 0) {

                        URL url = null;
                        if (expenseReportEntry == null) {
                            if (usePost) {
                                // Getting the PDF. The URL is already complete and absolute.
                                url = new URL(receiptUrl);
                            } else {
                                url = buildReportReceiptImageUrl(receiptUrl);
                            }
                        } else {
                            // Ensure 'usePost' is set to 'false'.
                            usePost = false;
                            if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY)) {
                                url = new URL(intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_URL_KEY));
                            } else {
                                url = buildReportEntryReceiptImageUrl();
                            }
                        }
                        // Check whether a receipt image ID key is passed in, if so, then this
                        // activity needs to first fetch the URL of the full receipt image, then
                        // fetch its content.
                        if (intent.hasExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY)) {
                            // Send the request to obtain the receipt image url.
                            sendGetReceiptImageUrlRequest(intent
                                    .getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY));
                        } else {
                            // Set up and kick-off the asynchronous download.
                            // Show the dialog.
                            if (expenseReportEntry != null) {
                                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
                            } else {
                                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT);
                            }
                            Thread fetchThread = new Thread(new ContentFetcher(this, url, usePost, sessionId, this,
                                    receiptImageFile));
                            fetchThread.start();
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: no session id!");
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: receipt Url is null!");
                }
            } catch (MalformedURLException mlfUrlException) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: malformed URL for receipt image view for receipt URL '"
                        + expRep.receiptUrl + "'", mlfUrlException);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: can't find receipt image view!");
        }
    }

    /**
     * Constructs a URL suitable for retrieving report level receipt images.
     * 
     * @param receiptUrl
     *            the receipt Url string associated with the report.
     * @return the receipt Url object.
     * @throws MalformedURLException
     *             if the receipt URL object is malformed.
     */
    private URL buildReportReceiptImageUrl(String receiptUrl) throws MalformedURLException {
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(serverAdd);
        if (!serverAdd.endsWith("/") && !receiptUrl.startsWith("/")) {
            strBldr.append('/');
        }
        strBldr.append(receiptUrl);
        URL url = new URL(strBldr.toString());
        return url;
    }

    /**
     * Constructs a URL suitable for retrieving a report entry mobile receipt.
     * 
     * @return the report entry receipt Url object.
     * @throws MalformedURLException
     *             if the receipt URL object is malformed.
     */
    private URL buildReportEntryReceiptImageUrl() throws MalformedURLException {
        String serverAdd = Format.formatServerAddress(true, Preferences.getServerAddress());
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(serverAdd);
        strBldr.append(DownloadMobileEntryReceiptRequest.getServiceEndPointURI(expenseReportEntry.meKey));
        strBldr.append('/');
        strBldr.append(expRep.reportKey);
        URL url = new URL(strBldr.toString());
        return url;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#getBroadcastReceiverIntentFilter()
     */
    @Override
    protected IntentFilter getBroadcastReceiverIntentFilter() {
        return new IntentFilter(Const.ACTION_EXPENSE_REPORT_DETAIL_UPDATED);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#shouldReceiveDataEvents()
     */
    @Override
    protected boolean shouldReceiveDataEvents() {
        return true;
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
            if (expenseReportEntry != null) {
                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
            } else {
                showDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT);
            }
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
     * Will unregister an instance of <code>GetReceiptImageUrlReceiver</code> with the application context and set the
     * <code>getReceiptImageUrlReceiver</code> to <code>null</code>.
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
     * An extension of <code>BroadcastReceiver</code> for handling notification of the result of an attempt to retrieve a receipt
     * image Url.
     * 
     * @author AndrewK
     */
    static class GetReceiptImageUrlReceiver extends BaseBroadcastReceiver<ExpenseReceipt, GetReceiptImageUrlRequest> {

        private final String CLS_TAG = ExpenseReceipt.CLS_TAG + "." + GetReceiptImageUrlReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>GetReceiptImageUrlReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        GetReceiptImageUrlReceiver(ExpenseReceipt activity) {
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
        protected void clearActivityServiceRequest(ExpenseReceipt activity) {
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
            if (activity.expenseReportEntry != null) {
                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
            } else {
                activity.dismissDialog(Const.DIALOG_EXPENSE_RETRIEVE_REPORT_RECEIPT);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL_FAILED);
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

}
