/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.activity;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.receiptstore.data.ShareItem;
import com.concur.mobile.core.expense.receiptstore.data.ShareItem.UIStatus;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService.ReceiptShareLocalBinder;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ViewUtil;

/**
 * An extension of <code>BaseActivity</code> used to display the status of sharing a receipt to the Receipt Store.
 * 
 * This activity is intended to be called from within the ConcurMobile application rather than directly from the notification
 * manager.
 * 
 * @author andy
 */
public class ReceiptShareStatus extends BaseActivity {

    private static final String CLS_TAG = ReceiptShareStatus.class.getSimpleName();

    private static final String SERVICE_CONNECTION_KEY = "receipt.share.service.connection";
    private static final String SERVICE_STATUS_RECEIVER_KEY = "receipt.share.service.status.receiver";
    private static final String SERVICE_STATUS_TITLE_KEY = "receipt.share.service.status.title";
    private static final String SERVICE_STATUS_DETAIL_KEY = "receipt.share.service.status.detail";

    /**
     * Contains the list share items.
     */
    protected List<ShareItem> shareItems;

    /**
     * Contains a share position within the adapter.
     */
    private int sharePosition = -1;

    /**
     * Contains the adapter populating the grid view.
     */
    protected ShareItemAdapter shareAdapter;

    /**
     * Contains a reference to the share list view.
     */
    protected ListView shareList;

    /**
     * Contains a reference to the Receipt Share service connection.
     */
    protected ReceiptShareServiceConnection rsServiceConn;

    /**
     * Contains a reference to a broadcast receiver to handle receipt share status updates.
     */
    protected ReceiptShareStatusReceiver rsStatusReceiver = new ReceiptShareStatusReceiver();

    /**
     * Contains the intent filter used to register the receipt share service status receiver.
     */
    protected IntentFilter rsStatusFilter = new IntentFilter(ReceiptShareService.ACTION_RECEIPT_SHARE_SERVICE_UPDATE);

    /**
     * Contains whether building of the view is delayed.
     */
    protected boolean buildViewDelayed = false;

    /**
     * Contains the current status title.
     */
    protected String statusTitle;

    /**
     * Contains the current status detail.
     */
    protected String statusDetail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Construct and populate map from view state to child index.
        viewStateFlipChild = new HashMap<ViewState, Integer>();
        viewStateFlipChild.put(ViewState.LOCAL_DATA, 0);
        viewStateFlipChild.put(ViewState.NO_DATA, 2);
        viewStateFlipChild.put(ViewState.RESTORE_APP_STATE, 1);
        // The last two states here map to the same view.
        viewStateFlipChild.put(ViewState.NO_LOCAL_DATA_REFRESH, 3);
        viewStateFlipChild.put(ViewState.LOCAL_DATA_REFRESH, 3);

        // Init to refresh state.
        viewState = ViewState.LOCAL_DATA_REFRESH;

        if (retainer != null) {
            // Restore the service connection handler.
            if (retainer.contains(SERVICE_CONNECTION_KEY)) {
                rsServiceConn = (ReceiptShareServiceConnection) retainer.get(SERVICE_CONNECTION_KEY);
            }
            // Restore the service status receiver.
            if (retainer.contains(SERVICE_STATUS_RECEIVER_KEY)) {
                rsStatusReceiver = (ReceiptShareStatusReceiver) retainer.get(SERVICE_STATUS_RECEIVER_KEY);
                if (rsStatusReceiver != null) {
                    rsStatusReceiver.setActivity(this);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: retainer has null value for service status!");
                }
            }
            // Restore the status title and detail.
            if (retainer.contains(SERVICE_STATUS_TITLE_KEY)) {
                statusTitle = (String) retainer.get(SERVICE_STATUS_TITLE_KEY);
            }
            if (retainer.contains(SERVICE_STATUS_DETAIL_KEY)) {
                statusDetail = (String) retainer.get(SERVICE_STATUS_DETAIL_KEY);
            }
        }

        setContentView(R.layout.receipt_share_status);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        if (isServiceAvailable()) {
            initView();
        } else {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: service unavailable, delayed building view.");
            buildViewDelayed = true;
            viewState = ViewState.RESTORE_APP_STATE;
        }

        flipViewForViewState();

    }

    @Override
    protected void onServiceAvailable() {
        super.onServiceAvailable();
        if (buildViewDelay) {
            buildViewDelay = false;
            // Init to refresh state.
            viewState = ViewState.LOCAL_DATA_REFRESH;
            flipViewForViewState();
            initView();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {

            // Save service retainer.
            if (rsServiceConn != null) {
                retainer.put(SERVICE_CONNECTION_KEY, rsServiceConn);
            }

            // Save the receipt share status receiver.
            if (rsStatusReceiver != null) {
                retainer.put(SERVICE_STATUS_RECEIVER_KEY, rsStatusReceiver);
            }
            // Save the status title & detail.
            if (statusTitle != null) {
                retainer.put(SERVICE_STATUS_TITLE_KEY, statusTitle);
            }
            if (statusDetail != null) {
                retainer.put(SERVICE_STATUS_DETAIL_KEY, statusDetail);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (retainer != null) {
            if (retainer.contains(SERVICE_STATUS_RECEIVER_KEY)) {
                rsStatusReceiver = (ReceiptShareStatusReceiver) retainer.get(SERVICE_STATUS_RECEIVER_KEY);
                rsStatusReceiver.setActivity(this);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind the service connection.
        if (getChangingConfigurations() == 0) {
            if (rsServiceConn != null) {
                getApplicationContext().unbindService(rsServiceConn);
                rsServiceConn = null;
            }
        }
    }

    @Override
    protected int getNoDataTextResourceId() {
        return R.string.receipt_share_no_data;
    }

    protected boolean isImage(String mimeType) {
        boolean retVal = false;
        if (mimeType != null) {
            retVal = (mimeType.startsWith("image/") ? true : false);
        }
        return retVal;
    }

    protected boolean isPDF(String mimeType) {
        boolean retVal = false;
        if (mimeType != null) {
            retVal = (mimeType.startsWith("application/pdf") ? true : false);
        }
        return retVal;
    }

    protected void initView() {

        // Initialize the header text.
        getSupportActionBar().setTitle(R.string.receipt_share_status);

        // Initialize the service status title and detail.
        setStatus(statusTitle, statusDetail);

        // Set the data loading text.
        ViewUtil.setTextViewText(this, R.id.loading_data, R.id.data_loading_text,
                getText(R.string.receipt_share_status_loading_data).toString(), true);

        // Set the adapter on the list view.
        shareList = (ListView) findViewById(R.id.share_list);
        if (shareList != null) {
            shareAdapter = new ShareItemAdapter(this, shareItems, new ShareItemClickListener());
            shareList.setAdapter(shareAdapter);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initView: 'list_view' not found!");
        }

        if (rsServiceConn == null) {
            // Launch the receipt store share service.
            Intent serviceIntent = new Intent(this, ReceiptShareService.class);
            Log.d(Const.LOG_TAG, CLS_TAG + ".onClick: starting ReceiptShare service.");
            if (startService(serviceIntent) == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: unable to start ReceiptShareService!");
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onClick: binding to ReceiptShare service.");
                // Bind to the service.
                rsServiceConn = new ReceiptShareServiceConnection();
                getApplicationContext().bindService(serviceIntent, rsServiceConn, Context.BIND_AUTO_CREATE);
            }
        } else {
            updateListItems();
        }
    }

    /**
     * Will update the list of share items based on retrieving them from the service connection.
     */
    protected void updateListItems() {
        if (rsServiceConn != null && rsServiceConn.rsService != null) {
            List<ReceiptShareItem> srvShareItems = rsServiceConn.rsService.getReceiptShareItems();
            List<ShareItem> newShareItems = null;
            if (srvShareItems != null) {
                newShareItems = new ArrayList<ShareItem>(srvShareItems.size());
                // Iterate over the list returned by the service and populate with
                // items from the existing list, or create new ones.
                for (ReceiptShareItem rsItem : srvShareItems) {
                    // Look for a match on Uri in our existing list.
                    ShareItem shareItem = null;
                    if (shareItems != null) {
                        for (ShareItem si : shareItems) {
                            if (si.uri.equals(rsItem.uri)) {
                                shareItem = si;
                                break;
                            }
                        }
                    }
                    if (shareItem == null) {
                        shareItem = new ShareItem();
                        shareItem.uri = rsItem.uri;
                        shareItem.mimeType = rsItem.mimeType;
                        shareItem.fileName = rsItem.fileName;
                        shareItem.displayName = rsItem.displayName;
                    }
                    newShareItems.add(shareItem);
                }
            }
            shareItems = newShareItems;
            shareAdapter.setShareItems(shareItems);
            shareAdapter.notifyDataSetChanged();

            // If there are no share items left, then flip to the negative view.
            if (shareAdapter.getCount() == 0) {
                viewState = ViewState.NO_DATA;
            } else {
                viewState = ViewState.LOCAL_DATA;
            }
            flipViewForViewState();
        }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.footer_button_one) {
            // Disable the share button immediately.
            view.setEnabled(false);
            // Launch the receipt store share service.
            Intent serviceIntent = new Intent(this, ReceiptShareService.class);
            Log.d(Const.LOG_TAG, CLS_TAG + ".onClick: starting ReceiptShare service.");
            if (startService(serviceIntent) == null) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: unable to start ReceiptShareService!");
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onClick: binding to ReceiptShare service.");
                // Bind to the service.
                rsServiceConn = new ReceiptShareServiceConnection();
                bindService(serviceIntent, rsServiceConn, Context.BIND_AUTO_CREATE);
            }
        }
    }

    protected void setStatus(String title, String detail) {
        statusTitle = title;
        statusDetail = detail;
        TextView txtView = (TextView) findViewById(R.id.receipt_share_server_status_title);
        if (txtView != null) {
            txtView.setText(title);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setStatus: unable to locate title view!");
        }
        txtView = (TextView) findViewById(R.id.receipt_share_server_status_detail);
        if (txtView != null) {
            if (detail != null && detail.length() > 0) {
                txtView.setText(detail);
                txtView.setVisibility(View.VISIBLE);
            } else {
                txtView.setVisibility(View.INVISIBLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".setStatus: unable to locate title detail!");
        }
    }

    /**
     * Gets the share item associated with the uri.
     * 
     * @param uri
     *            contains the uri used to match against a share item.
     * @return returns an instance of <code>ShareItem</code> matching on <code>uri</code>.
     */
    protected ShareItem findShareItem(Uri uri) {
        ShareItem retVal = null;
        if (shareAdapter != null) {
            sharePosition = -1;
            for (int i = 0; i < shareAdapter.getCount(); ++i) {
                ShareItem si = (ShareItem) shareAdapter.getItem(i);
                if (si.uri.equals(uri)) {
                    sharePosition = i;
                    retVal = si;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Will handle the status of "prepare receipt share".
     * 
     * @param uri
     *            contains the Uri of the receipt share item preparing to be uploaded.
     * @param progress
     *            contains the value to set in the progress bar.
     */
    protected void handlePrepare(Uri uri, int progress) {
        ShareItem si = findShareItem(uri);
        if (si != null) {
            si.uiStatus = UIStatus.PREPARE;
            si.progress = progress;
            updateForCurrentShareItemPosition(si, sharePosition);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handlePrepare: unable to find ShareItem in list!");
        }
    }

    /**
     * Will handle the status of "uploading receipt share".
     * 
     * @param uri
     *            contains the Uri of the receipt share item being uploaded.
     * @param progress
     *            contains the value to set in the progress bar.
     */
    protected void handleUpload(Uri uri, int progress) {
        ShareItem si = findShareItem(uri);
        if (si != null) {
            si.uiStatus = UIStatus.UPLOAD;
            si.progress = progress;
            updateForCurrentShareItemPosition(si, sharePosition);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handlePrepare: unable to find ShareItem in list!");
        }

    }

    /**
     * Will handle the status of "upload finished".
     * 
     * @param uri
     *            contains the Uri of the receipt share item upload attempt.
     * @param progress
     *            contains the value to be set in the progress bar.
     * @param result
     *            contains the result of upload.
     * @param retry
     *            contains whether the current receipt share item is being retried.
     * @param reason
     *            contains the reason.
     */
    protected void handleFinished(Uri uri, int progress, boolean result, boolean retry, String reason) {
        ShareItem si = findShareItem(uri);
        if (si != null) {
            si.progress = progress;
            si.reason = reason;
            if (result) {
                si.uiStatus = UIStatus.FINISH_COMPLETE;
            } else if (retry) {
                si.uiStatus = UIStatus.FINISH_RETRY;
            } else {
                si.uiStatus = UIStatus.FINISH_FAILED;
            }
            updateForCurrentShareItemPosition(si, sharePosition);
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".handlePrepare: unable to find ShareItem in list!");
        }

    }

    /**
     * Will update the appropriate view child within the list view based on the share item and the position.
     * 
     * @param si
     *            contains the share item.
     * @param position
     *            contains the position of the share item within the list.
     */
    private void updateForCurrentShareItemPosition(final ShareItem si, int position) {
        runOnUiThread(new Runnable() {

            public void run() {
                shareAdapter.refreshRow(si);
            }
        });
    }

    /**
     * Will update a view with information contained in a share item.
     * 
     * @param view
     *            contains the view to be updated.
     * @param si
     *            contains the share item.
     */
    protected void updateShareItemStatus(View view, ShareItem si) {

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
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleStatus: status: " + statusStr);
                try {
                    ReceiptShareService.Status rsSrvStatus = ReceiptShareService.Status.fromString(statusStr);
                    switch (rsSrvStatus) {
                    case RUNNING: {
                        setStatus(getText(R.string.receipt_share_status_running).toString(), null);
                        break;
                    }
                    case SHUTTING_DOWN: {
                        setStatus(getText(R.string.receipt_share_status_shutting_down).toString(), null);
                        break;
                    }
                    case STARTING_UP: {
                        setStatus(getText(R.string.receipt_share_status_starting_up).toString(), null);
                        break;
                    }
                    case PREPARING_UPLOAD: {
                        Uri uri = data.getParcelableExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_URI);
                        int progress = data.getIntExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                        handlePrepare(uri, progress);
                        break;
                    }
                    case UPLOADING: {
                        setStatus(getText(R.string.receipt_share_status_uploading).toString(), null);
                        Uri uri = data.getParcelableExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_URI);
                        int progress = data.getIntExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                        handleUpload(uri, progress);
                        break;
                    }
                    case FINISHED_UPLOAD: {
                        Uri uri = data.getParcelableExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_URI);
                        int progress = data.getIntExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_PROGRESS, 0);
                        boolean result = data.getBooleanExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_RESULT,
                                true);
                        String reason = data.getStringExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_REASON);
                        boolean retry = data.getBooleanExtra(ReceiptShareService.EXTRA_RECEIPT_SHARE_UPLOAD_RETRY,
                                false);
                        handleFinished(uri, progress, result, retry, reason);
                        break;
                    }
                    case WAITING_FOR_CONCUR_SERVICE: {
                        // No-op.
                        break;
                    }
                    case WAITING_FOR_CONNECTIVITY: {
                        setStatus(getText(R.string.sharing_receipt_wait_on_connectivity_subtitle).toString(),
                                getText(R.string.sharing_receipt_wait_on_connectivity_detail).toString());
                        break;
                    }
                    case WAITING_FOR_SESSION: {
                        setStatus(getText(R.string.sharing_receipt_wait_on_session_subtitle).toString(),
                                getText(R.string.sharing_receipt_wait_on_session_detail).toString());
                        break;
                    }
                    case WAITING_FOR_SHARE_ITEMS: {
                        setStatus(getText(R.string.receipt_share_status_waiting_for_share_items).toString(), null);
                        break;
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

    class ReceiptShareServiceConnection implements ServiceConnection {

        private String CLS_TAG = ReceiptShareStatus.CLS_TAG + "." + ReceiptShareServiceConnection.class.getSimpleName();

        /**
         * Contains a reference to the Receipt Share service.
         */
        protected ReceiptShareService rsService;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceConnected: bound to the ReceiptShare service.");
            // We've bound to ReceiptShareLocalService, cast the IBinder and get ReceiptShareLocalService instance.
            ReceiptShareLocalBinder rsBinder = (ReceiptShareLocalBinder) service;
            rsService = rsBinder.getService();
            // Update the list of receipt share items.
            updateListItems();

            // Register the receipt share service status receiver with the application context.
            rsStatusReceiver.setActivity(ReceiptShareStatus.this);
            Intent serviceStatus = getApplicationContext().registerReceiver(rsStatusReceiver, rsStatusFilter);
            if (serviceStatus != null) {
                handleStatus(serviceStatus);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceDisconnected: unbound from ReceiptShare service.");
            rsService = null;
            rsServiceConn = null;
            // Unregister the broadcast receiver.
            getApplicationContext().unregisterReceiver(rsStatusReceiver);
        }
    }

    class ShareItemAdapter extends BaseAdapter {

        private String CLS_TAG = ReceiptShareStatus.CLS_TAG + "." + ShareItemAdapter.class.getSimpleName();

        List<ShareItem> shareItems;

        OnClickListener clickListener;

        Context context;

        ShareItemAdapter(Context context, List<ShareItem> shareItems, OnClickListener clickListener) {
            this.context = context;
            this.shareItems = shareItems;
            this.clickListener = clickListener;
        }

        /**
         * Sets a new list of <code>ShareItem</code> objects into the list.
         * 
         * @param shareItems
         *            contains the new list of share items.
         */
        public void setShareItems(List<ShareItem> shareItems) {
            this.shareItems = shareItems;
        }

        @Override
        public int getCount() {
            return (shareItems != null) ? shareItems.size() : 0;
        }

        @Override
        public Object getItem(int position) {
            return (shareItems != null) ? shareItems.get(position) : null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        /**
         * Will refresh the row referenced by a share item.
         * 
         * @param si
         *            contains the referenced share item.
         */
        public void refreshRow(ShareItem si) {
            int start = shareList.getFirstVisiblePosition();
            for (int i = start, j = shareList.getLastVisiblePosition(); i <= j; i++) {
                if (si == shareList.getItemAtPosition(i)) {
                    View view = shareList.getChildAt(i - start);
                    getView(i, view, shareList);
                    break;
                }
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View view = null;

            ShareItem si = shareItems.get(position);
            if (si.thumbnail == null) {
                if (isImage(si.mimeType)) {
                    si.thumbnail = loadImageBitmap(si);
                } else if (isPDF(si.mimeType)) {
                    si.thumbnail = loadPdfBitmap(si.uri);
                }
            }
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(context);
                view = inflater.inflate(R.layout.expense_receipt_share_row, null);
                view.setOnClickListener(clickListener);
            } else {
                view = convertView;
            }

            // Set the thumbnail image.
            ImageView imgView = (ImageView) view.findViewById(R.id.receipt_thumbnail);
            if (imgView != null) {
                imgView.setImageBitmap(si.thumbnail);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'receipt_thumbnail' view.");
            }

            // Set the document type.
            if (isPDF(si.mimeType) && si.displayName != null) {
                // Reset the title to 'Document Name'.
                TextView txtView = (TextView) view.findViewById(R.id.receipt_document_type_title);
                if (txtView != null) {
                    txtView.setText(getText(R.string.receipt_document_name));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'receipt_document_type_title'.");
                }
                // Set the document name.
                txtView = (TextView) view.findViewById(R.id.receipt_document_type);
                if (txtView != null) {
                    txtView.setText(si.displayName);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'receipt_document_type'.");
                }
            } else {
                // Reset the title to 'Document Type'.
                TextView txtView = (TextView) view.findViewById(R.id.receipt_document_type_title);
                if (txtView != null) {
                    txtView.setText(getText(R.string.receipt_document_type));
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'receipt_document_type_title'.");
                }
                txtView = (TextView) view.findViewById(R.id.receipt_document_type);
                if (txtView != null) {
                    if (isImage(si.mimeType)) {
                        txtView.setText(R.string.receipt_document_type_image);
                    } else if (isPDF(si.mimeType)) {
                        txtView.setText(R.string.receipt_document_type_pdf);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'receipt_document_type' view.");
                }
            }

            // Set the status.
            TextView txtView = (TextView) view.findViewById(R.id.share_status);
            if (txtView != null) {
                switch (si.uiStatus) {
                case PENDING: {
                    txtView.setText(getText(R.string.general_pending));
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setVisibility(View.GONE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                case PREPARE: {
                    txtView.setText(getText(R.string.general_preparing));
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setProgress(si.progress);
                        progBar.setVisibility(View.VISIBLE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                case UPLOAD: {
                    txtView.setText(getText(R.string.general_uploading));
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setProgress(si.progress);
                        progBar.setVisibility(View.VISIBLE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                case FINISH_RETRY: {
                    txtView.setText(si.reason);
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setVisibility(View.GONE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                case FINISH_COMPLETE: {
                    txtView.setText(si.reason);
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setVisibility(View.GONE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                case FINISH_FAILED: {
                    txtView.setText(si.reason);
                    ProgressBar progBar = (ProgressBar) view.findViewById(R.id.progress_bar);
                    if (progBar != null) {
                        progBar.setVisibility(View.GONE);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".updateShareItemStatus: unable to find 'progress_bar' view!");
                    }
                    break;
                }
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'share_status' view.");
            }

            view.setTag(si);
            return view;
        }

        protected Bitmap loadImageBitmap(ReceiptShareItem rsItem) {
            Bitmap retVal = null;
            if (rsItem != null && rsItem.fileName != null) {
                String filePath = null;
                try {
                    filePath = new File(ReceiptShareService.externalCacheDirectory, rsItem.fileName).getAbsolutePath();
                    InputStream inStream = new BufferedInputStream(new FileInputStream(filePath), (8 * 1024));
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 8;
                    retVal = BitmapFactory.decodeStream(inStream, null, options);
                    ViewUtil.closeInputStream(inStream);
                } catch (FileNotFoundException fnfExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".loadImageBitmap: unable to load '" + filePath + "'.", fnfExc);
                }
            }
            return retVal;
        }

        protected Bitmap loadPdfBitmap(Uri uri) {
            Bitmap retVal = null;
            if (uri != null) {
                retVal = BitmapFactory.decodeResource(getResources(), R.drawable.pdf_icon);
            }
            return retVal;
        }

    }

    /**
     * An implementation of <code>View.OnClickListener</code> to handle clicking on receipt share items.
     */
    class ShareItemClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof ShareItem) {
                ShareItem si = (ShareItem) tag;
                if (isImage(si.mimeType)) {
                    viewImageFile(si);
                } else if (isPDF(si.mimeType)) {
                    viewPdfFile(si);
                }
            }
        }

        protected boolean viewImageFile(ShareItem rsItem) {
            boolean retVal = false;
            if (rsItem.fileName != null) {
                File receiptContentFile = new File(ReceiptShareService.externalCacheDirectory, rsItem.fileName);
                String receiptContentPathStr = URLEncoder.encode(receiptContentFile.getAbsolutePath());
                receiptContentPathStr = "file:/" + receiptContentPathStr;
                Intent intent = new Intent(ReceiptShareStatus.this, ViewImage.class);
                try {
                    receiptContentPathStr = receiptContentFile.toURL().toExternalForm();
                } catch (MalformedURLException mlfUrlExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".viewImageFile: malformed URL ", mlfUrlExc);
                }
                intent.putExtra(Const.EXTRA_EXPENSE_DELETE_EXTERNAL_RECEIPT_FILE, false);
                intent.putExtra(Const.EXTRA_EXPENSE_RECEIPT_URL_KEY, receiptContentPathStr);
                intent.putExtra(Const.EXTRA_EXPENSE_SCREEN_TITLE_KEY, getText(R.string.receipt_share_preview));
                try {
                    startActivity(intent);
                    retVal = true;
                } catch (ActivityNotFoundException anfExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".viewImageFile: unable to launch activity view receipt image!",
                            anfExc);
                }
            }
            return retVal;
        }

        /**
         * Will view a PDF file represented by <code>rsItem</code>.
         * 
         * @param rsItem
         *            contains the receipt share item.
         * 
         * @return returns <code>true</code> upon success; <code>false</code> otherwise.
         */
        protected boolean viewPdfFile(ShareItem rsItem) {
            boolean retVal = false;
            if (rsItem.fileName != null) {
                File receiptContentFile = new File(ReceiptShareService.externalCacheDirectory, rsItem.fileName);
                String receiptContentPathStr = URLEncoder.encode(receiptContentFile.getAbsolutePath());
                receiptContentPathStr = "file:/" + receiptContentPathStr;

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile((receiptContentFile.getAbsoluteFile())), "application/pdf");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                try {
                    startActivity(intent);
                    retVal = true;
                } catch (ActivityNotFoundException anfExc) {
                    // No PDF viewer installed! Display a dialog.
                    showDialog(Const.DIALOG_EXPENSE_NO_PDF_VIEWER);
                }
            }
            return retVal;
        }

    }

    /**
     * An extension of <code>BroadcastReceiver</code> to handle updates to the display based on ReceiptShareService status
     * updates.
     */
    protected static class ReceiptShareStatusReceiver extends BroadcastReceiver {

        protected ReceiptShareStatus activity;

        protected Intent intent;

        public void setActivity(ReceiptShareStatus activity) {
            this.activity = activity;
            if (this.activity != null) {
                if (this.intent != null) {
                    // The 'onReceive' method was called prior to the 'setActivity', so process
                    // the intent now.
                    onReceive(activity.getApplicationContext(), intent);
                }
            }
        }

        @Override
        public void onReceive(Context context, Intent data) {

            // Does this receiver have a current activity?
            if (activity != null) {
                activity.handleStatus(data);
            } else {
                // The new activity has not yet been set on the receiver, defer
                // the processing of this intent until then.
                this.intent = data;
            }
        }

    }

}
