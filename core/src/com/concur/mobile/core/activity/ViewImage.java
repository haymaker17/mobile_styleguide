/**
 * 
 */
package com.concur.mobile.core.activity;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

import android.app.Activity;
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
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.activity.ExpensesAndReceipts;
import com.concur.mobile.core.expense.charge.activity.QuickExpense;
import com.concur.mobile.core.expense.receiptstore.activity.ReceiptStoreFragment;
import com.concur.mobile.core.expense.receiptstore.service.GetReceiptImageUrlRequest;
import com.concur.mobile.core.receiver.NetworkActivityReceiver;
import com.concur.mobile.core.receiver.NetworkActivityReceiver.INetworkActivityListener;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.util.net.ContentFetcher;
import com.concur.mobile.core.util.net.ContentFetcher.ContentFetcherListener;

/**
 * An extension of <code>Activity</code> for the purpose of viewing an image.
 * 
 * @author AndrewK
 */
public class ViewImage extends BaseActivity implements ContentFetcherListener, INetworkActivityListener {

    private static final String CLS_TAG = ViewImage.class.getSimpleName();

    /**
     * Extra Bundle flag for hiding the Create Expense button in the ActionBar.
     */
    public static final String EXTRA_HIDE_CREATE_EXPENSE_ACTION_MENU = "view.image.hide.create.expense.action.menu";

    // Request code used to launch a PDF viewer.
    private static final int VIEW_PDF_REQUEST_CODE = 1;

    // Contains the key used to store/restore the receipt image url receiver.
    private static String GET_RECEIPT_IMAGE_URL_RECEIVER_KEY = "receipt.image.url.receiver";

    /**
     * Contains a reference to the receipt image web view.
     */
    private WebView webView;

    /**
     * Contains the name of the file created within the application's "files" directory that contains this receipt image.
     */
    private String receiptImageFilePath;

    /**
     * Contains a reference to a broadcast receiver for enabling/disabling the progress indicator.
     */
    private NetworkActivityReceiver networkActivityReceiver;

    /**
     * Contains a reference to an intent filter for network activity notification.
     */
    private IntentFilter networkActivityFilter;

    /**
     * Contains whether or not the network activity receiver has been registered.
     */
    private boolean networkActivityRegistered;

    /**
     * Contains whether or not a local external file should be deleted in the 'onDestroy' call.
     */
    private boolean deleteLocalFileOnDestroy;

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
     * Contains a reference to an external file in which receipt image data will be downloaded.
     */
    private File receiptImageFile;

    /**
     * Contains whether or not this activity is showing the dialog indicating no PDF viewer is available.
     */
    private boolean noPDFViewDialogVisible;

    private boolean isEreceipt;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

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

        setContentView(R.layout.view_image);

        if (getIntent().hasExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE)) {
            deleteLocalFileOnDestroy = getIntent().getBooleanExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE,
                    false);
        }

        // Grab a reference to the web view.
        webView = (WebView) findViewById(R.id.web_view);
        if (webView == null) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: can't locate web view!");
        }
        networkActivityReceiver = new NetworkActivityReceiver(this, this);
        networkActivityFilter = new IntentFilter(Const.ACTION_NETWORK_ACTIVITY_START);
        networkActivityFilter.addAction(Const.ACTION_NETWORK_ACTIVITY_STOP);
        registerReceiver(networkActivityReceiver, networkActivityFilter);
        networkActivityRegistered = true;

        String screenTitle;
        // Set the title to any custom title.
        Intent intent = getIntent();
        if (intent.hasExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY)) {
            screenTitle = intent.getStringExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY);
            if (screenTitle == null) {
                screenTitle = "";
            }
        } else {
            screenTitle = getText(R.string.expense_receipt).toString();
        }
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            isEreceipt = bundle.getBoolean(Const.EXTRA_E_RECEIPT_EXPENSE);
        }
        getSupportActionBar().setTitle(screenTitle);

        // Restore any receivers.
        restoreReceivers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreateDialog(int)
     */
    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = getConcurCore().createDialog(this, id);
        if (dlg != null) {
            switch (id) {
            case Const.DIALOG_EXPENSE_NO_PDF_VIEWER: {
                dlg.setOnCancelListener(new OnCancelListener() {

                    @Override
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
                // Set the flag.
                noPDFViewDialogVisible = true;
                break;
            }
            }
        }
        return dlg;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onDestroy()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (getChangingConfigurations() == 0) {
            if (receiptImageFilePath != null && receiptImageFilePath.length() > 0) {
                String extStoreDir = Environment.getExternalStorageDirectory().getAbsolutePath();
                if (!receiptImageFilePath.startsWith(extStoreDir)) {
                    deleteFile(receiptImageFilePath);
                } else if (deleteLocalFileOnDestroy) {
                    File fileToDelete = new File(receiptImageFilePath);
                    try {
                        fileToDelete.delete();
                    } catch (SecurityException secExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onDestroy: security exception deleting file '"
                                + receiptImageFilePath + "'.");
                    }
                }
            }
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

        if (retainer != null) {
            // Save 'GetReceiptImageUrlReceiver'.
            if (getReceiptImageUrlReceiver != null) {
                // Clear the activity reference, it will be set in the
                // 'onCreate' method.
                getReceiptImageUrlReceiver.setActivity(null);
                // Add to the map.
                retainer.put(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY, getReceiptImageUrlReceiver);
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceivers();
        restoreReceivers();
    }

    protected void restoreReceivers() {
        if (retainer != null) {
            // Restore any get receipt image receiver.
            if (retainer.contains(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY)) {
                getReceiptImageUrlReceiver = (GetReceiptImageUrlReceiver) retainer
                        .get(GET_RECEIPT_IMAGE_URL_RECEIVER_KEY);
                // Reset the activity reference.
                getReceiptImageUrlReceiver.setActivity(this);
            }
        }
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
                webView.setInitialScale(val.intValue());
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
     * @see android.app.Activity#onStart()
     */
    @Override
    protected void onStart() {
        super.onStart();
        if (webView != null) {
            // Enable on screen image zooming capabilities.
            WebSettings webSettings = webView.getSettings();
            webSettings.setBuiltInZoomControls(true);
            webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
            webSettings.setUseWideViewPort(true);
            webView.setInitialScale(1);

            Intent intent = getIntent();
            String urlStr = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY);
            String receiptImageId = intent.getStringExtra(Const.EXTRA_EXPENSE_RECEIPT_IMAGE_ID_KEY);
            if (urlStr != null) {
                URL url = null;
                try {
                    url = new URL(urlStr);
                    if (url.getProtocol().startsWith("file")) {
                        // Local file, just immediately load it in the web view.
                        String imageFilePath = URLDecoder.decode(url.getPath());
                        receiptImageFilePath = imageFilePath;
                        setWebViewInitialScaleFromImageWidth(imageFilePath);
                        File imageFilePathFile = new File(imageFilePath);
                        urlStr = imageFilePathFile.toURL().toExternalForm();
                        webView.loadUrl(urlStr);
                    } else {
                        // Create a content fetcher and download the image.
                        String sessionId = Preferences.getSessionId();
                        if (sessionId != null && sessionId.length() > 0) {
                            Thread fetchThread = new Thread(new ContentFetcher(this, url, false, sessionId, this,
                                    receiptImageFile));
                            fetchThread.start();
                            // Broadcast the start network activity message.
                            broadcastStartNetworkActivity(Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST,
                                    getText(R.string.retrieve_mobile_entry_receipt).toString());
                        }
                    }
                } catch (MalformedURLException mlfUrlExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: invalid URL", mlfUrlExc);
                }
            } else if (receiptImageId != null) {
                // Retrieve the URL from the server.
                sendGetReceiptImageUrlRequest(receiptImageId);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onStart: missing URL/receiptImageId string!");
            }
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onStop()
     */
    @Override
    protected void onStop() {
        super.onStop();
    }

    /**
     * Notifies the view that it should register any broadcast receivers it has registered.
     */
    public void registerReceivers() {
        // Register the network activity receiver.
        if (!networkActivityRegistered) {
            registerReceiver(networkActivityReceiver, networkActivityFilter);
            networkActivityRegistered = true;
        }
    }

    /**
     * Notifies the view that it should unregister any broadcast receivers it has registered.
     */
    public void unregisterReceivers() {
        // Unregister the network activity receiver.
        if (networkActivityRegistered) {
            unregisterReceiver(networkActivityReceiver);
            networkActivityRegistered = false;
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
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #getNetworkActivityText(int, java.lang.String)
     */
    @Override
    public String getNetworkActivityText(int networkMsgType, String defaultText) {
        return "";
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.NetworkActivityReceiver.INetworkActivityListener #isNetworkRequestInteresting(int)
     */
    @Override
    public boolean isNetworkRequestInteresting(int networkMsgType) {
        return (networkMsgType == Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);
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
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener# fetchCancelled(java.net.URL)
     */
    @Override
    public void fetchCancelled(URL url) {
        // No-op.
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener#fetchFailed (int, java.lang.String)
     */
    @Override
    public void fetchFailed(int status, String message) {
        Log.i(Const.LOG_TAG, CLS_TAG + ".fetchFailed: fetch of expense receipt failed with " + "status code (" + status
                + ") and reason (" + message + ").");
        // Broadcast the stop network activity message.
        broadcastStopNetworkActivity(Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);

        if (webView != null) {
            webView.post(new Runnable() {

                @Override
                public void run() {
                    if (isEreceipt) {
                        EventTracker.INSTANCE.track(Flurry.CATEGORY_ALL_MOBILE_EXPENSES,
                                Flurry.ACTION_E_RECEIPT_IMAGE_ERROR, Flurry.LABEL_REPORT_ENTRY_DETAIL);
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_E_RECEIPT_UNAVAILABLE);
                    } else {
                        // Display a dialog indicating failure.
                        showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_UNAVAILABLE);
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchFailed: web view is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.net.ContentFetcher.ContentFetcherListener# fetchSucceeded(java.lang.String, java.lang.String)
     */
    @Override
    public void fetchSucceeded(final String localURL, final String localFile) {
        // Broadcast the stop network activity message.
        broadcastStopNetworkActivity(Const.MSG_EXPENSE_DOWNLOAD_RECEIPT_REQUEST);

        if (webView != null) {
            webView.post(new Runnable() {

                @Override
                public void run() {
                    // Set the name of the file to be deleted.
                    receiptImageFilePath = localFile;
                    // Compute the scale of the webview based on the downloaded
                    // receipt file.
                    String localFilePath = receiptImageFile.getAbsolutePath();
                    if (!isPDF(receiptImageFile)) {
                        // Set the WebView initial scale based on image width.
                        setWebViewInitialScaleFromImageWidth(localFilePath);
                        // Display the image.
                        webView.loadUrl(localURL);
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
                        } catch (ActivityNotFoundException anfExc) {
                            // No PDF viewer installed! Display a dialog.
                            showDialog(Const.DIALOG_EXPENSE_NO_PDF_VIEWER);
                        }
                    }
                }
            });
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".fetchSucceeded: web view is null!");
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.activity.expense.AbstractExpenseActivity#onActivityResult (int, int, android.content.Intent)
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VIEW_PDF_REQUEST_CODE) {
            if (!noPDFViewDialogVisible) {
                finish();
            }
        }
        if (requestCode == Const.CREATE_MOBILE_ENTRY) {
            if (resultCode == Activity.RESULT_OK) {
                // Go straight to the entries list
                Intent i = new Intent(this, ExpensesAndReceipts.class);
                i.putExtra(Const.EXTRA_CHECK_PROMPT_TO_RATE, true);
                startActivity(i);
            } else {
                deleteLocalFileOnDestroy = true;
            }
            finish();
        }
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
            showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
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
    static class GetReceiptImageUrlReceiver extends BaseBroadcastReceiver<ViewImage, GetReceiptImageUrlRequest> {

        private final String CLS_TAG = ViewImage.CLS_TAG + "." + GetReceiptImageUrlReceiver.class.getSimpleName();

        /**
         * Constructs an instance of <code>GetReceiptImageUrlReceiver</code>.
         * 
         * @param activity
         *            the activity.
         */
        GetReceiptImageUrlReceiver(ViewImage activity) {
            super(activity);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * clearActivityServiceRequest(com.concur.mobile.activity .BaseActivity)
         */
        @Override
        protected void clearActivityServiceRequest(ViewImage activity) {
            activity.getReceiptImageUrlRequest = null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# dismissRequestDialog(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void dismissRequestDialog(Context context, Intent intent) {
            activity.removeDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleFailure(android.content.Context,
         * android.content.Intent)
         */
        @Override
        protected void handleFailure(Context context, Intent intent) {
            activity.showDialog(Const.DIALOG_EXPENSE_RETRIEVE_RECEIPT_IMAGE_URL_FAILED);
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# handleSuccess(android.content.Context,
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
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver#
         * setActivityServiceRequest(com.concur.mobile.activity. BaseActivity, com.concur.mobile.service.ServiceRequest)
         */
        @Override
        protected void setActivityServiceRequest(GetReceiptImageUrlRequest request) {
            activity.getReceiptImageUrlRequest = request;
        }

        /*
         * (non-Javadoc)
         * 
         * @see com.concur.mobile.activity.BaseActivity.BaseBroadcastReceiver# unregisterReceiver()
         */
        @Override
        protected void unregisterReceiver() {
            activity.unregisterGetReceiptImageUrlReceiver();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean showMenu = false;
        boolean hideCreateExpense = false;
        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT)) {
                showMenu = intent.getBooleanExtra(Const.EXTRA_SHOW_MENU, false);
                hideCreateExpense = (intent.getBooleanExtra(Const.EXTRA_PICK_RECEIPT_FROM_EXPENSE, false) || intent
                        .getBooleanExtra(ViewImage.EXTRA_HIDE_CREATE_EXPENSE_ACTION_MENU, false));
            }
        }
        if (showMenu) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.view_img, menu);
            if (hideCreateExpense) {
                MenuItem item = menu.findItem(R.id.menuquickexpense);
                item.setVisible(false);
            }
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent newIt;
        Intent intent = getIntent();
        String path = null;
        boolean recOnlyFrag = false;
        boolean startOcr = false;
        boolean useExpenseIt = false;
        String flurryParam = null;
        if (intent != null && intent.getExtras() != null) {
            if (intent.hasExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT)) {
                recOnlyFrag = intent.getBooleanExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, false);
            }
            if (intent.hasExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH)) {
                path = intent.getStringExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH);
            }
            if (intent.hasExtra(Flurry.PARAM_NAME_FROM)) {
                flurryParam = intent.getStringExtra(Flurry.PARAM_NAME_FROM);
            } else {
                flurryParam = Flurry.PARAM_VALUE_CAMERA;
            }

            if (intent.hasExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD)) {
                startOcr = intent.getBooleanExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, false);
            }
            if (intent.hasExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT)) {
                useExpenseIt = intent.getBooleanExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, false);
            }
        }
        int key = item.getItemId();
        if (key == R.id.menuCamera) {
            if (intent.getBooleanExtra(Const.EXTRA_PICK_RECEIPT_FROM_EXPENSE, false)) {
                // Return result to existing ExpensesAndReceipts/ReceiptStoreFragment
                newIt = new Intent();
                newIt.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, recOnlyFrag);
                newIt.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, path);
                newIt.putExtra(Flurry.PARAM_NAME_FROM, flurryParam);
                newIt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                setResult(RESULT_OK, newIt);
                finish();
            } else {
                newIt = new Intent(ViewImage.this, ExpensesAndReceipts.class);
                newIt.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, recOnlyFrag);
                newIt.putExtra(ReceiptStoreFragment.EXTRA_START_OCR_ON_UPLOAD, startOcr);
                newIt.putExtra(ReceiptStoreFragment.EXTRA_USE_EXPENSEIT, useExpenseIt);
                newIt.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, path);
                newIt.putExtra(Flurry.PARAM_NAME_FROM, flurryParam);
                newIt.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                finish();
                startActivity(newIt);
            }
        } else if (key == R.id.menuquickexpense) {
            newIt = new Intent(ViewImage.this, QuickExpense.class);
            newIt.putExtra(Const.EXTRA_RECEIPT_ONLY_FRAGMENT, recOnlyFrag);
            newIt.putExtra(Const.EXTRA_EXPENSE_IMAGE_FILE_PATH, path);
            newIt.putExtra(Flurry.PARAM_NAME_FROM, flurryParam);
            startActivityForResult(newIt, Const.CREATE_MOBILE_ENTRY);
        }
        return super.onOptionsItemSelected(item);
    }
}
