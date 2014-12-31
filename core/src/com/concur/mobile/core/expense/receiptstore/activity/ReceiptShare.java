/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.activity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.activity.ViewImage;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptShareItem;
import com.concur.mobile.core.expense.receiptstore.data.ShareItem;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService;
import com.concur.mobile.core.expense.receiptstore.service.ReceiptShareService.ReceiptShareLocalBinder;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>BaseActivity</code> for presenting a screen permitting the end-user to kick-off a receipt share (upload)
 * background process.
 * 
 * @author andy
 */
public class ReceiptShare extends BaseActivity {

    private static final String CLS_TAG = ReceiptShare.class.getSimpleName();

    private static final String SERVICE_CONNECTION_KEY = "receipt.share.service.connection";

    private static final String SELECTED_SHARE_ITEM_KEY = "selected.share.item";

    private static final String SHARE_ITEMS_KEY = "receipt.share.items";

    private static final int DIALOG_SHOW_SHARE_MESSAGE = 1;

    private static final int DIALOG_CONFIRM_SHARE_REMOVAL = 2;

    private static final int DIALOG_IMPORT_PROGRESS = 3;

    /**
     * Contains the list share items.
     */
    protected List<ShareItem> shareItems;

    /**
     * Contains the adapter populating the grid view.
     */
    protected ShareItemAdapter shareAdapter;

    /**
     * Contains a reference to the Receipt Share service.
     */
    protected ReceiptShareService rsService;

    /**
     * Contains a reference to the Receipt Share service connection.
     */
    protected ReceiptShareServiceConnection rsServiceConn;

    /**
     * Contains the path of a file to be deleted after viewing. (Viewing PDF's).
     */
    protected String deleteFilePath;

    /**
     * Contains a reference to a selected share item.
     */
    protected ShareItem selectedShareItem;

    /**
     * Contains whether or not the end-user clicked the share button.
     */
    protected boolean userClickedShare;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.receipt_share);
        initState(savedInstanceState);
        initView();

        if (retainer != null) {
            // Restore the service connection.
            if (retainer.contains(SERVICE_CONNECTION_KEY)) {
                rsServiceConn = (ReceiptShareServiceConnection) retainer.get(SERVICE_CONNECTION_KEY);
            }
            // Restore the selected share item.
            if (retainer.contains(SELECTED_SHARE_ITEM_KEY)) {
                selectedShareItem = (ShareItem) retainer.get(SELECTED_SHARE_ITEM_KEY);
            }
            // Restore the list of share items.
            if (retainer.contains(SHARE_ITEMS_KEY)) {
                shareItems = (List<ShareItem>) retainer.get(SHARE_ITEMS_KEY);
            }
        }
        // If a saved instance state was passed in and share items is still null/empty, then
        // check.
        if (savedInstanceState != null && (shareItems == null || shareItems.size() == 0)) {
            if (savedInstanceState.containsKey(SHARE_ITEMS_KEY)) {
                ShareItemList siList = (ShareItemList) savedInstanceState.getSerializable(SHARE_ITEMS_KEY);
                shareItems = siList.shareItems;
            }
        }

        // dumpUris();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // If there are item to share, kick-off a background import.
        if (shareItems != null && shareItems.size() > 0) {
            // If 'fileNames' have not already been provided, then the share items were recovered due to
            // the activity being re-created.
            if (shareItems.get(0).fileName == null) {
                ShareItem[] params = shareItems.toArray(new ShareItem[0]);
                ReceiptShareImportTask shareImportTask = new ReceiptShareImportTask();
                shareImportTask.execute(params);
            } else {
                // Set the adapter on the grid view.
                GridView gv = (GridView) findViewById(R.id.gridview);
                if (gv != null) {
                    shareAdapter = new ShareItemAdapter(shareItems, new ThumbnailShareItemClickListener(),
                            new DeleteShareItemClickListener());
                    gv.setAdapter(shareAdapter);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".initView: 'gridview' not found!");
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        Dialog dlg = null;
        switch (id) {
        case DIALOG_SHOW_SHARE_MESSAGE: {
            AlertDialog.Builder dlgBldr = new AlertDialog.Builder(this);
            dlgBldr.setTitle(R.string.general_share);
            dlgBldr.setMessage(R.string.receipt_share_message);
            dlgBldr.setPositiveButton(getText(R.string.okay), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    finish();
                }
            });
            dlg = dlgBldr.create();
            break;
        }
        case DIALOG_CONFIRM_SHARE_REMOVAL: {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getText(R.string.confirm));
            builder.setMessage(getText(R.string.dlg_receipt_share_remove_confirm_message));
            builder.setPositiveButton(getText(R.string.general_ok), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (selectedShareItem != null) {
                        shareItems.remove(selectedShareItem);
                        shareAdapter.setItems(shareItems);
                        shareAdapter.notifyDataSetChanged();
                        if (selectedShareItem.thumbnail != null) {
                            selectedShareItem.thumbnail = null;
                        }
                        // Ensure the imported file is punted.
                        if (selectedShareItem.fileName != null) {
                            String absFilePath = new File(ReceiptShareService.externalCacheDirectory,
                                    selectedShareItem.fileName).getAbsolutePath();
                            ViewUtil.deleteFile(absFilePath);
                        }
                        selectedShareItem = null;
                        // If the number of items has dropped to 0, then finish the activity.
                        if (shareAdapter.getCount() == 0) {
                            finish();
                        }
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: selectedShareItem is null!");
                    }
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(getText(R.string.general_cancel), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int which) {
                    selectedShareItem = null;
                    dialog.dismiss();
                }
            });
            dlg = builder.create();
            dlg.setOnDismissListener(new DialogInterface.OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    selectedShareItem = null;
                }
            });
            break;
        }
        case DIALOG_IMPORT_PROGRESS: {
            ProgressDialog progDlg = new ProgressDialog(this);
            progDlg.setMessage(this.getText(R.string.dlg_receipt_share_import_progress));
            progDlg.setIndeterminate(true);
            progDlg.setCancelable(false);
            dlg = progDlg;
            break;
        }
        default: {
            dlg = super.onCreateDialog(id);
            break;
        }
        }
        return dlg;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (retainer != null) {

            // Save service retainer.
            if (rsServiceConn != null) {
                retainer.put(SERVICE_CONNECTION_KEY, rsServiceConn);
            }

            // Save the selected share item.
            if (selectedShareItem != null) {
                retainer.put(SELECTED_SHARE_ITEM_KEY, selectedShareItem);
            }

            // Save the current list of share items.
            if (shareItems != null && shareItems.size() > 0) {
                retainer.put(SHARE_ITEMS_KEY, shareItems);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the list of share items.
        if (shareItems != null && shareItems.size() > 0) {
            ShareItemList siList = new ShareItemList();
            siList.shareItems = shareItems;
            outState.putSerializable(SHARE_ITEMS_KEY, siList);
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Unbind the service connection.
        if (rsServiceConn != null) {
            unbindService(rsServiceConn);
            rsServiceConn = null;
        }

        // If the end-user did not click the share button, then ensure imported files are
        // deleted.
        if (getChangingConfigurations() == 0) {
            if (!userClickedShare) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".onDestroy: user did not click share .. deleting imported files.");
                if (shareItems != null) {
                    for (ShareItem si : shareItems) {
                        if (si.fileName != null) {
                            String absPathName = new File(ReceiptShareService.externalCacheDirectory, si.fileName)
                                    .getAbsolutePath();
                            ViewUtil.deleteFile(absPathName);
                        }
                    }
                }
            }
        }
    }

    // private void dumpUris() {
    // if( shareItems != null ) {
    // for( ReceiptShareItem shareItem : shareItems ) {
    // Log.d(Const.LOG_TAG, CLS_TAG + ".dumpUris: uri -> '" + shareItem.uri.toString());
    // }
    // } else {
    // Log.e(Const.LOG_TAG, CLS_TAG + ".dumpUris: no selected images.");
    // }
    // }

    protected void initState(Bundle inState) {
        if (inState == null) {
            Intent intent = getIntent();
            String mimeType = intent.getType();
            if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND) && mimeType != null) {
                int flags = intent.getFlags();
                if ((flags & Intent.FLAG_GRANT_READ_URI_PERMISSION) == Intent.FLAG_GRANT_READ_URI_PERMISSION) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".initState: read URI permission granted!");
                }
                if (isImage(mimeType) || isPDF(mimeType)) {
                    // Single image shared.
                    shareItems = new ArrayList<ShareItem>(1);
                    ShareItem si = new ShareItem();
                    si.mimeType = intent.getType();
                    si.uri = intent.getParcelableExtra(Intent.EXTRA_STREAM);
                    si.displayName = getDisplayName(si);
                    si.status = ReceiptShareItem.Status.PENDING;
                    shareItems.add(si);
                }
            } else if (intent.getAction().equalsIgnoreCase(Intent.ACTION_SEND_MULTIPLE) && mimeType != null) {
                if (isImage(mimeType) || isPDF(mimeType)) {
                    // Multiple images shared.
                    ArrayList<Uri> uriList = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
                    shareItems = new ArrayList<ShareItem>(uriList.size());
                    for (Uri uri : uriList) {
                        ShareItem si = new ShareItem();
                        si.mimeType = mimeType;
                        si.uri = uri;
                        si.displayName = getDisplayName(si);
                        si.status = ReceiptShareItem.Status.PENDING;
                        shareItems.add(si);
                    }
                }
            }
        }
    }

    /**
     * Will attempt to retrieve an appropriate display name for a uri.
     * 
     * @param uri
     *            contains the uri.
     * @return returns a display name for <code>uri</code> if it can; otherwise, returns <code>null</code>.
     */
    protected String getDisplayName(ReceiptShareItem rsItem) {
        String displayName = null;
        if (rsItem.uri != null) {
            if (rsItem.uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_FILE)
                    || rsItem.uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_ANDROID_RESOURCE)) {
                displayName = rsItem.uri.getLastPathSegment();
                int dotIndex = displayName.lastIndexOf('.');
                if (dotIndex != -1) {
                    displayName = displayName.substring(0, dotIndex);
                }
            } else if (rsItem.uri.getScheme().equalsIgnoreCase(ContentResolver.SCHEME_CONTENT)) {
                String authority = rsItem.uri.getAuthority();
                if (authority != null) {
                    if (authority.equalsIgnoreCase(MediaStore.AUTHORITY) || rsItem.uri.getPath().contains("picasa")) {
                        String[] proj = { MediaStore.Images.ImageColumns.DATE_TAKEN };
                        try {
                            Cursor cursor = getContentResolver().query(rsItem.uri, proj, null, null, null);
                            if (cursor != null && cursor.getCount() != 0) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN);
                                if (columnIndex != -1) {
                                    long dateTaken = cursor.getLong(columnIndex);
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTimeInMillis(dateTaken);
                                    displayName = Format.safeFormatCalendar(
                                            FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY_LOCAL, cal);
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".getDisplayName: unable to locate column '"
                                            + MediaStore.Images.ImageColumns.DATE_TAKEN + "'!");
                                }
                            }
                        } catch (IllegalArgumentException ilaExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".getDisplayName: ", ilaExc);
                        }
                    } else if (authority.equalsIgnoreCase("downloads")) {
                        final String FILENAME_COLUMN = "_data";
                        String[] proj = { FILENAME_COLUMN };
                        try {
                            Cursor cursor = getContentResolver().query(rsItem.uri, proj, null, null, null);
                            if (cursor != null && cursor.getCount() != 0) {
                                cursor.moveToFirst();
                                int columnIndex = cursor.getColumnIndex("_data");
                                if (columnIndex != -1) {
                                    String fileName = cursor.getString(columnIndex);
                                    File file = new File(fileName).getAbsoluteFile();
                                    displayName = file.getName();
                                    int dotIndex = displayName.lastIndexOf('.');
                                    if (dotIndex != -1) {
                                        displayName = displayName.substring(0, dotIndex);
                                    }
                                } else {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".getDisplayName: unable to locate column '"
                                            + FILENAME_COLUMN + "'!");
                                }
                            }
                        } catch (IllegalArgumentException ilaExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".getDisplayName: ", ilaExc);
                        }
                    }
                }
            }
        }
        return displayName;
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
        getSupportActionBar().setTitle(R.string.receipt_share_title);

        // Initialize the text on the share button.
        Button button = (Button) findViewById(R.id.footer_button_one);
        if (button != null) {
            button.setText(getText(R.string.general_share));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".initView: 'footer_button_one' not found!");
        }

        // Set the adapter on the grid view.
        // GridView gv = (GridView) findViewById(R.id.gridview);
        // if( gv != null ) {
        // shareAdapter = new ShareItemAdapter(shareItems, new ThumbnailShareItemClickListener(),
        // new DeleteShareItemClickListener());
        // gv.setAdapter(shareAdapter);
        // } else {
        // Log.e(Const.LOG_TAG, CLS_TAG + ".initView: 'gridview' not found!");
        // }
    }

    public void onClick(View view) {
        if (view.getId() == R.id.footer_button_one) {
            // Set the flag indicating the end-user has decided to share.
            userClickedShare = true;
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

    class ReceiptShareServiceConnection implements ServiceConnection {

        private String CLS_TAG = ReceiptShare.CLS_TAG + "." + ReceiptShareServiceConnection.class.getSimpleName();

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceConnected: bound to the ReceiptShare service.");
            // We've bound to ReceiptShareLocalService, cast the IBinder and get ReceiptShareLocalService instance.
            ReceiptShareLocalBinder rsBinder = (ReceiptShareLocalBinder) service;
            rsService = rsBinder.getService();
            // Add the current list of images to the list of receipts being added.
            List<ReceiptShareItem> rsItems = new ArrayList<ReceiptShareItem>(shareItems.size());
            rsItems.addAll(shareItems);
            rsService.addReceiptShareItems(rsItems);
            showDialog(DIALOG_SHOW_SHARE_MESSAGE);
            unbindService(this);
            rsService = null;
            rsServiceConn = null;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".onServiceDisconnected: unbound from ReceiptShare service.");
            rsService = null;
            rsServiceConn = null;
        }
    }

    class ShareItemAdapter extends BaseAdapter {

        private String CLS_TAG = ReceiptShare.CLS_TAG + "." + ShareItemAdapter.class.getSimpleName();

        List<ShareItem> shareItems;

        OnClickListener thumbnailClickListener;
        OnClickListener deleteShareClickListener;

        ShareItemAdapter(List<ShareItem> shareItems, OnClickListener thumbnailClickListener,
                OnClickListener deleteShareClickListener) {
            this.shareItems = shareItems;
            this.thumbnailClickListener = thumbnailClickListener;
            this.deleteShareClickListener = deleteShareClickListener;
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
         * Sets the list of share items.
         * 
         * @param shareItems
         *            contains the list of share items.
         */
        public void setItems(List<ShareItem> shareItems) {
            this.shareItems = shareItems;
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
            ImageView tnImgView = null;
            ImageView delImgView = null;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(ReceiptShare.this);
                view = inflater.inflate(R.layout.expense_receipt_share_item, null);
                tnImgView = (ImageView) view.findViewById(R.id.image);
                if (tnImgView != null) {
                    tnImgView.setOnClickListener(thumbnailClickListener);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'image' view!");
                }
                delImgView = (ImageView) view.findViewById(R.id.delete);
                if (delImgView != null) {
                    delImgView.setOnClickListener(deleteShareClickListener);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'delete' view!");
                }
            } else {
                view = convertView;
                tnImgView = (ImageView) view.findViewById(R.id.image);
                if (tnImgView == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'image' view!");
                }
                delImgView = (ImageView) view.findViewById(R.id.delete);
                if (delImgView == null) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'delete' view!");
                }
            }

            TextView txtView = (TextView) view.findViewById(R.id.name);
            if (txtView != null) {
                if (si.displayName != null) {
                    txtView.setText(si.displayName);
                } else {
                    txtView.setVisibility(View.GONE);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'name' view!");
            }

            if (tnImgView != null) {
                tnImgView.setImageBitmap(si.thumbnail);
                tnImgView.setTag(si);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'image' view.");
            }

            if (delImgView != null) {
                delImgView.setTag(si);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate 'delete' view.");
            }

            view.setTag(si);
            return view;
        }

        protected Bitmap loadImageBitmap(ShareItem rsItem) {
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
     * Will delete a file given a path.
     * 
     * @param filePath
     *            the absolute path of the file to be deleted.
     */
    protected void deleteLocalFile(String filePath) {
        File fileToDelete = new File(deleteFilePath);
        try {
            fileToDelete.delete();
        } catch (SecurityException secExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteFile: security exception deleting file '" + filePath + "'.");
        }
    }

    /**
     * An implementation of <code>View.OnClickListener</code> to handle clicking on receipt share items.
     */
    class DeleteShareItemClickListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            Object tag = v.getTag();
            if (tag instanceof ShareItem) {
                ShareItem si = (ShareItem) tag;
                selectedShareItem = si;
                showDialog(DIALOG_CONFIRM_SHARE_REMOVAL);
            }
        }

    }

    /**
     * An implementation of <code>View.OnClickListener</code> to handle clicking on receipt share items.
     */
    class ThumbnailShareItemClickListener implements OnClickListener {

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
                Intent intent = new Intent(ReceiptShare.this, ViewImage.class);
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
     * An extension of <code>AsyncTask</code> to handle background import of receipt share files.
     */
    class ReceiptShareImportTask extends AsyncTask<ShareItem, Void, Void> {

        @Override
        protected void onPostExecute(Void result) {
            dismissDialog(DIALOG_IMPORT_PROGRESS);
            // Set the adapter on the grid view.
            runOnUiThread(new Runnable() {

                public void run() {
                    GridView gv = (GridView) findViewById(R.id.gridview);
                    if (gv != null) {
                        shareAdapter = new ShareItemAdapter(shareItems, new ThumbnailShareItemClickListener(),
                                new DeleteShareItemClickListener());
                        gv.setAdapter(shareAdapter);
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".initView: 'gridview' not found!");
                    }
                }
            });
        }

        @Override
        protected void onPreExecute() {
            showDialog(DIALOG_IMPORT_PROGRESS);
        }

        @Override
        protected Void doInBackground(ShareItem... params) {
            Void retVal = null;
            if (ViewUtil.isExternalMediaMounted()) {
                if (params != null) {
                    for (ShareItem si : params) {
                        if (si.uri != null) {
                            if (isImage(si.mimeType)) {
                                si.fileName = Long.toString(System.currentTimeMillis()) + ".jpg";
                                if (!copyImageFile(si)) {

                                }
                            } else if (isPDF(si.mimeType)) {
                                si.fileName = Long.toString(System.currentTimeMillis()) + ".pdf";
                                copyPDFFile(si);
                            }
                        }
                    }
                }
            }
            return retVal;
        }

        /**
         * Will read the content from the content provider based on the share item URI and place it in a share directory sampled
         * and ready for upload.
         * 
         * @param rsItem
         *            contains the receipt share item.
         * @return returns <code>true</code> upon success; <code>false</code> otherwise.
         */
        protected boolean copyImageFile(ReceiptShareItem rsItem) {
            boolean retVal = false;
            if (rsItem.uri != null) {
                InputStream inStream = ViewUtil.getInputStream(ReceiptShare.this, rsItem.uri);
                int angle = ViewUtil.getOrientaionAngle(ReceiptShare.this, rsItem.uri);
                if (inStream != null) {
                    String tempReceiptImageDataLocalFilePath = ViewUtil.createExternalMediaImageFilePath();
                    BufferedOutputStream bufOut = null;
                    try {
                        // First make a temporary copy of the original content since some content providers give us "one-shot"
                        // access
                        // to the content before the URI permission gets revoked (i.e., Android downloads content provider).
                        bufOut = new BufferedOutputStream(new FileOutputStream(tempReceiptImageDataLocalFilePath),
                                (8 * 1024));
                        ViewUtil.writeAllBytes(inStream, bufOut, (8 * 1024));
                        bufOut.flush();
                        bufOut.close();
                        bufOut = null;
                        ViewUtil.closeInputStream(inStream);
                        inStream = null;
                        // Second, make a sampled version of the copied image file based on a computed recommended sample
                        // size and quality.
                        inStream = new BufferedInputStream(new FileInputStream(tempReceiptImageDataLocalFilePath),
                                (8 * 1024));
                        // Obtain the recommended sampling size, etc.
                        ViewUtil.SampleSizeCompressFormatQuality recConf = ViewUtil
                                .getRecommendedSampleSizeCompressFormatQuality(inStream);
                        ViewUtil.closeInputStream(inStream);
                        inStream = null;
                        if (recConf != null) {
                            String receiptImageDataLocalFilePath = new File(ReceiptShareService.externalCacheDirectory,
                                    rsItem.fileName).getAbsolutePath();
                            inStream = new BufferedInputStream(new FileInputStream(tempReceiptImageDataLocalFilePath),
                                    (8 * 1024));
                            if (!ViewUtil.copySampledBitmap(inStream, receiptImageDataLocalFilePath,
                                    recConf.sampleSize, recConf.compressFormat, recConf.compressQuality, angle)) {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".copyImageFile: unable to copy sampled image from '"
                                        + tempReceiptImageDataLocalFilePath + "' to '" + receiptImageDataLocalFilePath
                                        + "'");
                            } else {
                                retVal = true;
                            }
                            ViewUtil.closeInputStream(inStream);
                        } else {
                            Log.e(Const.LOG_TAG, CLS_TAG
                                    + ".copyImageFile: unable to obtain recommended samplesize, etc.!");
                        }
                    } catch (FileNotFoundException fnfExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".copyImageFile: file not found exception opening file", fnfExc);
                    } catch (IOException ioExc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".copyImageFile: I/O exception writing file", ioExc);
                    } finally {
                        if (bufOut != null) {
                            ViewUtil.closeOutputStream(bufOut);
                            bufOut = null;
                        }
                        if (inStream != null) {
                            ViewUtil.closeInputStream(inStream);
                            inStream = null;
                        }
                        if (tempReceiptImageDataLocalFilePath != null) {
                            ViewUtil.deleteFile(tempReceiptImageDataLocalFilePath);
                            tempReceiptImageDataLocalFilePath = null;
                        }
                    }

                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copyImageFile: unable to obtain input stream for bitmap for Uri '"
                            + rsItem.uri.toString() + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copyImageFile: rsItem.uri is null!");
            }
            return retVal;
        }

        /**
         * Will read the content from the content provider based on the share item URI and place it in a share directory sampled
         * and ready for upload.
         * 
         * @param rsItem
         *            contains the receipt share item.
         * @return returns <code>true</code> upon success; <code>false</code> otherwise.
         */
        protected boolean copyPDFFile(ReceiptShareItem rsItem) {
            boolean retVal = false;
            if (rsItem.uri != null) {
                InputStream inStream = ViewUtil.getInputStream(ReceiptShare.this, rsItem.uri);
                if (inStream != null) {
                    String receiptImageDataLocalFilePath = new File(ReceiptShareService.externalCacheDirectory,
                            rsItem.fileName).getAbsolutePath();
                    if (receiptImageDataLocalFilePath != null) {
                        BufferedOutputStream bufOut = null;
                        try {
                            // Write out the file.
                            bufOut = new BufferedOutputStream(new FileOutputStream(receiptImageDataLocalFilePath),
                                    (8 * 1024));
                            ViewUtil.writeAllBytes(inStream, bufOut, (8 * 1024));
                            bufOut.flush();
                            bufOut.close();
                            bufOut = null;
                        } catch (FileNotFoundException fnfExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".copyPdfFile: file not found exception opening file '"
                                    + receiptImageDataLocalFilePath + "'.");
                        } catch (IOException ioExc) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".copyPdfFile: I/O exception writing file '"
                                    + receiptImageDataLocalFilePath + "'.");
                        } finally {
                            if (bufOut != null) {
                                try {
                                    bufOut.close();
                                    bufOut = null;
                                } catch (IOException ioExc) {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".copyPdfFile: I/O exception closing output stream!");
                                }
                            }
                        }
                    }
                    ViewUtil.closeInputStream(inStream);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".copyPdfFile: unable to obtain input stream for PDF for Uri '"
                            + rsItem.uri.toString() + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".copyPdfFile: rsItem.uri is null!");
            }
            return retVal;
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

    }

}

class ShareItemList implements Serializable {

    private static final long serialVersionUID = 1L;

    public List<ShareItem> shareItems;

}
