package com.concur.mobile.platform.expense.receipt.list;

import java.io.File;
import java.util.Calendar;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expense.receipt.list.dao.ReceiptDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.annotations.SerializedName;

/**
 * Provides a model of a receipt item.
 * 
 * @author andrewk
 */
public class Receipt implements ReceiptDAO {

    private static final String CLS_TAG = "Receipt";

    /**
     * String array containing all the ETag column names.
     */
    // NOTE: The RECEIPT_DATA and THUMBNAIL_RECEIPT_DATA columns are not provided
    // below...this will be lazily loaded.
    public static String[] fullColumnList = { //
    Expense.ReceiptColumns._ID, //
            Expense.ReceiptColumns.ETAG, //
            Expense.ReceiptColumns.ID, //
            Expense.ReceiptColumns.URI, //
            Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE, //
            Expense.ReceiptColumns.LOCAL_PATH, //
            Expense.ReceiptColumns.THUMBNAIL_URI, //
            Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE, //
            Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH, //
            Expense.ReceiptColumns.IS_ATTACHED, //
            Expense.ReceiptColumns.LAST_ACCESS_TIME, //
            Expense.ReceiptColumns.IMAGE_UPLOAD_TIME, //
            Expense.ReceiptColumns.FILE_NAME, //
            Expense.ReceiptColumns.FILE_TYPE, //
            Expense.ReceiptColumns.SYSTEM_ORIGIN, //
            Expense.ReceiptColumns.IMAGE_ORIGIN, //
            Expense.ReceiptColumns.IMAGE_URL, //
            Expense.ReceiptColumns.THUMB_URL, //
            Expense.ReceiptColumns.OCR_IMAGE_ORIGIN, //
            Expense.ReceiptColumns.OCR_STAT_KEY, //
            Expense.ReceiptColumns.OCR_REJECT_CODE, //
            Expense.ReceiptColumns.USER_ID //
    };

    /**
     * Contains the receipt image ID.
     */
    @SerializedName("receiptImageId")
    protected String id;

    /**
     * Contains the receipt image file type.
     */
    @SerializedName("fileType")
    protected String fileType;

    /**
     * Contains the receipt image URI.
     */
    protected String uri;

    /**
     * Contains the file name associated with the receipt.
     */
    @SerializedName("fileName")
    protected String fileName;

    /**
     * Contains the receipt image upload time.
     */
    @SerializedName("imageDate")
    protected Calendar imageUploadTime;

    /**
     * Contains the receipt system origin.
     */
    @SerializedName("systemOrigin")
    protected String systemOrigin;

    /**
     * Contains the receipt image origin.
     */
    @SerializedName("imageOrigin")
    protected String imageOrigin;

    /**
     * Contains the receipt full-sized image URL.
     */
    @SerializedName("imageUrl")
    protected String imageUrl;

    /**
     * Contains the receipt thumbnail image URL.
     */
    @SerializedName("thumbUrl")
    protected String thumbUrl;

    /**
     * Contains the receipt OCR image origin.
     */
    @SerializedName("ocrImageOrigin")
    protected String ocrImageOrigin;

    /**
     * Contains the receipt OCR status.
     */
    @SerializedName("ocrStatus")
    protected String ocrStatus;

    /**
     * Contains the receipt OCR reject code.
     */
    @SerializedName("rejectCode")
    protected String ocrRejectCode;

    /**
     * Contains the eTag value for the receipt.
     */
    protected transient String eTag;

    /**
     * Contains the Uri associated with this receipt item.
     */
    protected transient Uri contentUri;

    /**
     * Contains the content-type of the image.
     */
    protected transient String contentType;

    /**
     * Contains the local path for the receipt data.
     */
    protected transient String localPath;

    /**
     * Contains the receipt image data.
     */
    protected transient byte[] receiptData;

    /**
     * Contains whether the receipt data was loaded.
     */
    protected transient boolean receiptDataLoaded;

    /**
     * Contains the receipt thumbnail URI.
     */
    protected transient String thumbnailUri;

    /**
     * Contains the receipt thumbnail content type.
     */
    protected transient String thumbnailContentType;

    /**
     * Contains the local path for the receipt thumbnail data.
     */
    protected transient String thumbnailLocalPath;

    /**
     * Contains the thumbnail receipt image data.
     */
    protected transient byte[] thumbnailReceiptData;

    /**
     * Contains whether the thumbnail receipt data was loaded.
     */
    protected transient boolean thumbnailReceiptDataLoaded;

    /**
     * Contains the UTC millisecond timestamp of the last time the full-sized image was accessed.
     */
    protected transient Long lastAccessTime;

    /**
     * Contains whether or not this receipt is attached to an expense.
     */
    protected transient boolean attached;

    /**
     * Contains the user id.
     */
    protected transient String userId;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    /**
     * Constructs an empty <code>Receipt</code> object.
     */
    Receipt() {
        // No-args constructor.
    }

    /**
     * Will construct an instance of <code>Receipt</code> with an application context.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     */
    public Receipt(Context context, String userId) {
        this.context = context;
        this.userId = userId;
    }

    /**
     * Constructs an instance of <code>Receipt</code> with information populated from a Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the Uri.
     */
    public Receipt(Context context, Uri contentUri) {
        this.context = context;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null, Expense.ReceiptColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    init(cursor);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Constructs an instance of <code>Receipt</code> with information stored in a cursor.
     * 
     * @param context
     *            contains an application context.
     * @param cursor
     *            contains a cursor.
     */
    public Receipt(Context context, Cursor cursor) {
        this.context = context;
        init(cursor);
    }

    private void init(Cursor cursor) {
        eTag = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.ETAG);
        id = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.ID);
        uri = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.URI);
        contentType = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE);
        localPath = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.LOCAL_PATH);
        thumbnailUri = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.THUMBNAIL_URI);
        thumbnailContentType = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE);
        thumbnailLocalPath = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH);
        attached = CursorUtil.getBooleanValue(cursor, Expense.ReceiptColumns.IS_ATTACHED);
        lastAccessTime = CursorUtil.getLongValue(cursor, Expense.ReceiptColumns.LAST_ACCESS_TIME);
        fileName = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.FILE_NAME);
        fileType = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.FILE_TYPE);
        Long imageUploadTimeMillis = CursorUtil.getLongValue(cursor, Expense.ReceiptColumns.IMAGE_UPLOAD_TIME);
        if (imageUploadTimeMillis != null) {
            imageUploadTime = Calendar.getInstance(Parse.UTC);
            imageUploadTime.setTimeInMillis(imageUploadTimeMillis);
            imageUploadTime.set(Calendar.MILLISECOND, 0);
        }
        systemOrigin = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.SYSTEM_ORIGIN);
        imageOrigin = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.IMAGE_ORIGIN);
        imageUrl = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.IMAGE_URL);
        thumbUrl = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.THUMB_URL);
        ocrImageOrigin = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.OCR_IMAGE_ORIGIN);
        ocrStatus = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.OCR_STAT_KEY);
        ocrRejectCode = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.OCR_REJECT_CODE);

        userId = CursorUtil.getStringValue(cursor, Expense.ReceiptColumns.USER_ID);
        Long contentId = CursorUtil.getLongValue(cursor, Expense.ReceiptColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.ReceiptColumns.CONTENT_URI, contentId);
        }
    }

    @Override
    public String getETag() {
        return eTag;
    }

    @Override
    public void setETag(String eTag) {
        this.eTag = eTag;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String getUri() {
        return uri;
    }

    @Override
    public void setUri(String uri) {
        this.uri = uri;
    }

    @Override
    public String getLocalPath() {
        return localPath;
    }

    @Override
    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public byte[] getReceiptData() {
        if (receiptData == null) {
            if (!receiptDataLoaded) {
                getContentUri();
                if (contentUri != null) {
                    receiptData = ContentUtils.getColumnBlobValue(context, contentUri,
                            Expense.ReceiptColumns.RECEIPT_DATA);
                    receiptDataLoaded = true;
                }
            }
        }
        return receiptData;
    }

    @Override
    public void setReceiptData(byte[] receiptData) {
        this.receiptData = receiptData;
        this.receiptDataLoaded = true;
    }

    @Override
    public String getThumbnailUri() {
        return thumbnailUri;
    }

    @Override
    public void setThumbnailUri(String uri) {
        this.thumbnailUri = uri;
    }

    @Override
    public String getThumbnailLocalPath() {
        return thumbnailLocalPath;
    }

    @Override
    public void setThumbnailLocalPath(String localPath) {
        this.thumbnailLocalPath = localPath;
    }

    @Override
    public String getThumbnailContentType() {
        return thumbnailContentType;
    }

    @Override
    public void setThumbnailContentType(String contentType) {
        this.thumbnailContentType = contentType;
    }

    @Override
    public byte[] getThumbnailReceiptData() {
        if (thumbnailReceiptData == null) {
            if (!thumbnailReceiptDataLoaded) {
                getContentUri();
                if (contentUri != null) {
                    thumbnailReceiptData = ContentUtils.getColumnBlobValue(context, contentUri,
                            Expense.ReceiptColumns.THUMBNAIL_RECEIPT_DATA);
                    thumbnailReceiptDataLoaded = true;
                }
            }
        }
        return thumbnailReceiptData;
    }

    @Override
    public void setThumbnailReceiptData(byte[] receiptData) {
        this.thumbnailReceiptData = receiptData;
        this.thumbnailReceiptDataLoaded = true;
    }

    @Override
    public boolean isAttached() {
        return attached;
    }

    @Override
    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    @Override
    public Long getLastAccessTime() {
        return lastAccessTime;
    }

    @Override
    public String getFileName() {
        return fileName;
    }

    @Override
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String getFileType() {
        return fileType;
    }

    @Override
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Override
    public Calendar getReceiptUploadTime() {
        return imageUploadTime;
    }

    @Override
    public void setReceiptUploadTime(Calendar receiptUploadTime) {
        this.imageUploadTime = receiptUploadTime;

    }

    @Override
    public String getSystemOrigin() {
        return systemOrigin;
    }

    @Override
    public void setSystemOrigin(String systemOrigin) {
        this.systemOrigin = systemOrigin;
    }

    @Override
    public String getImageOrigin() {
        return imageOrigin;
    }

    @Override
    public void setImageOrigin(String imageOrigin) {
        this.imageOrigin = imageOrigin;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    @Override
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String getThumbUrl() {
        return thumbUrl;
    }

    @Override
    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    @Override
    public String getOcrImageOrigin() {
        return ocrImageOrigin;
    }

    @Override
    public void setOcrImageOrigin(String ocrImageOrigin) {
        this.ocrImageOrigin = ocrImageOrigin;
    }

    @Override
    public String getOcrStatus() {
        return ocrStatus;
    }

    @Override
    public void setOcrStatus(String ocrStatus) {
        this.ocrStatus = ocrStatus;
    }

    @Override
    public String getOcrRejectCode() {
        return ocrRejectCode;
    }

    @Override
    public void setOcrRejectCode(String ocrRejectCode) {
        this.ocrRejectCode = ocrRejectCode;
    }

    @Override
    public Uri getContentUri() {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(id)) {
                String[] columnNames = { Expense.ReceiptColumns.ID, Expense.ReceiptColumns.USER_ID };
                String[] columnValues = { id, userId };
                contentUri = ContentUtils.getContentUri(context, Expense.ReceiptColumns.CONTENT_URI, columnNames,
                        columnValues);
            }
        }
        return contentUri;
    }

    @Override
    public boolean delete() {
        boolean retVal = true;

        Uri recUri = getContentUri();
        if (recUri != null) {
            // Punt any full-sized receipt image.
            if (!TextUtils.isEmpty(localPath)) {
                File file = new File(localPath);
                if (file.exists()) {
                    retVal = file.delete();
                }
                // Clear local path.
                localPath = null;
            }
            // Punt any thumbnail-sized image.
            if (!TextUtils.isEmpty(thumbnailLocalPath)) {
                File file = new File(thumbnailLocalPath);
                if (file.exists()) {
                    retVal = file.delete();
                }
                // Clear thumbnail local path.
                thumbnailLocalPath = null;
            }
            // Punt the actual entry in the database.
            ContentResolver resolver = context.getContentResolver();
            int count = resolver.delete(recUri, null, null);
            retVal = (count == 1);

            // Clear out the URI.
            contentUri = null;
        } else {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public boolean update() {
        boolean retVal = true;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        ContentUtils.putValue(values, Expense.ReceiptColumns.ETAG, eTag);
        ContentUtils.putValue(values, Expense.ReceiptColumns.ID, id);
        ContentUtils.putValue(values, Expense.ReceiptColumns.URI, uri);
        ContentUtils.putValue(values, Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE, contentType);
        ContentUtils.putValue(values, Expense.ReceiptColumns.LOCAL_PATH, localPath);
        if (receiptDataLoaded) {
            ContentUtils.putValue(values, Expense.ReceiptColumns.RECEIPT_DATA, receiptData);
        }
        ContentUtils.putValue(values, Expense.ReceiptColumns.THUMBNAIL_URI, thumbnailUri);
        ContentUtils.putValue(values, Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE, thumbnailContentType);
        ContentUtils.putValue(values, Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH, thumbnailLocalPath);
        if (thumbnailReceiptDataLoaded) {
            ContentUtils.putValue(values, Expense.ReceiptColumns.THUMBNAIL_RECEIPT_DATA, thumbnailReceiptData);
        }
        ContentUtils.putValue(values, Expense.ReceiptColumns.LAST_ACCESS_TIME, lastAccessTime);
        ContentUtils.putValue(values, Expense.ReceiptColumns.IS_ATTACHED, attached);
        ContentUtils.putValue(values, Expense.ReceiptColumns.FILE_NAME, fileName);
        ContentUtils.putValue(values, Expense.ReceiptColumns.FILE_TYPE, fileType);
        if (imageUploadTime != null) {
            ContentUtils.putValue(values, Expense.ReceiptColumns.IMAGE_UPLOAD_TIME, imageUploadTime.getTimeInMillis());
        }
        ContentUtils.putValue(values, Expense.ReceiptColumns.SYSTEM_ORIGIN, systemOrigin);
        ContentUtils.putValue(values, Expense.ReceiptColumns.IMAGE_ORIGIN, imageOrigin);
        ContentUtils.putValue(values, Expense.ReceiptColumns.IMAGE_URL, imageUrl);
        ContentUtils.putValue(values, Expense.ReceiptColumns.THUMB_URL, thumbUrl);
        ContentUtils.putValue(values, Expense.ReceiptColumns.OCR_IMAGE_ORIGIN, ocrImageOrigin);
        ContentUtils.putValue(values, Expense.ReceiptColumns.OCR_STAT_KEY, ocrStatus);
        ContentUtils.putValue(values, Expense.ReceiptColumns.OCR_REJECT_CODE, ocrRejectCode);
        ContentUtils.putValue(values, Expense.ReceiptColumns.USER_ID, userId);

        // Grab the content URI if any.
        Uri receiptUri = getContentUri();

        if (receiptUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(receiptUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + receiptUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.ReceiptColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + receiptUri.toString()
                            + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.ReceiptColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }

        return retVal;
    }

}
