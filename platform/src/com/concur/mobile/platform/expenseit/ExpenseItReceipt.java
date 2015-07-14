/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expenseit.dao.ExpenseItReceiptDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.annotations.SerializedName;

import java.util.Calendar;

public class ExpenseItReceipt implements ExpenseItReceiptDAO {

    private static final String CLS_TAG = ExpenseItReceipt.class.getSimpleName();

    /**
     * String array containing all the ETag column names.
     */
    public static String[] fullColumnList = { //
        Expense.ExpenseItReceiptColumns._ID, //
        Expense.ExpenseItReceiptColumns.ID, //
        Expense.ExpenseItReceiptColumns.USER_ID, //
        Expense.ExpenseItReceiptColumns.REPORT_ID,
        Expense.ExpenseItReceiptColumns.NOTE,
        Expense.ExpenseItReceiptColumns.CCTYPE,
        Expense.ExpenseItReceiptColumns.CREATED_AT,
        Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT,
        Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL,
        Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT,
        Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED,
        Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE,
        Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE,
        Expense.ExpenseItReceiptColumns.ETA
    };

    /**
     * Contains the receipt image ID.
     */
    @SerializedName("id")
    private long id;

    @SerializedName("reportId")
    private long reportId;

    @SerializedName("note")
    private String note;

    @SerializedName("ccType")
    private String ccType;

    @SerializedName("createdAt")
    private Calendar createdAt;

    @SerializedName("sentToCteAt")
    private Calendar sendToCteAt;

    @SerializedName("imageDataUrl")
    private String imageDataUrl;

    @SerializedName("totalImageCount")
    private int totalImageCount;

    @SerializedName("totalImagesUploaded")
    private int totalImagesUploaded;

    @SerializedName("parsingStatusCode")
    private int parsingStatusCode;

    @SerializedName("processingEngine")
    private String processingEngine;

    @SerializedName("eta")
    private int eta;

    /**
     * Contains the Uri associated with this receipt item.
     */
    protected transient Uri contentUri;

    /**
     * Contains the content-type of the image.
     */
    protected transient String contentType;

    /**
     * Contains the user id.
     */
    protected transient String userId;

    /**
     * Contains the application context.
     */
    protected transient Context context;

    /**
     * Will construct an instance of <code>ExpenseItReceipt</code> with an application context.
     *
     * @param context contains a reference to an application context.
     * @param userId  contains the user id.
     */
    public ExpenseItReceipt(Context context, String userId) {
        this.context = context;
        this.userId = userId;
        this.id = -1;
    }

    /**
     * Constructs an instance of <code>ExpenseItReceipt</code> with information populated from a Uri.
     *
     * @param context    contains an application context.
     * @param contentUri contains the Uri.
     */
    public ExpenseItReceipt(Context context, Uri contentUri) {
        this.context = context;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null, Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER);
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
     * Constructs an instance of <code>ExpenseItReceipt</code> with information stored in a cursor.
     *
     * @param context contains an application context.
     * @param cursor  contains a cursor.
     */
    public ExpenseItReceipt(Context context, Cursor cursor) {
        this.context = context;
        init(cursor);
    }

    private void init(Cursor cursor) {
        id = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.ID);
        userId = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.USER_ID);
        reportId = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.REPORT_ID);
        note = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.NOTE);
        ccType = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.CCTYPE);
        Long createdMilliSeconds = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.CREATED_AT);
        if (createdMilliSeconds != null) {
            createdAt = Calendar.getInstance(Parse.UTC);
            createdAt.setTimeInMillis(createdMilliSeconds);
            createdAt.set(Calendar.MILLISECOND, 0);
        }
        Long SentToCtedMilliSeconds = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT);
        if (SentToCtedMilliSeconds != null) {
            sendToCteAt = Calendar.getInstance(Parse.UTC);
            sendToCteAt.setTimeInMillis(SentToCtedMilliSeconds);
            sendToCteAt.set(Calendar.MILLISECOND, 0);
        }
        imageDataUrl = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL);
        totalImageCount = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT);
        totalImagesUploaded = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED);
        parsingStatusCode = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE);
        processingEngine = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE);
        eta = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.ETA);


        Long contentId = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.ExpenseItReceiptColumns.CONTENT_URI, contentId);
        }
    }

    public Uri getContentUri() {
        if (contentUri == null) {
            if (id != -1) {
                String[] columnNames = {Expense.ExpenseItReceiptColumns.ID, Expense.ExpenseItReceiptColumns.USER_ID};
                String[] columnValues = {Long.toString(id), userId};
                contentUri = ContentUtils.getContentUri(context, Expense.ExpenseItReceiptColumns.CONTENT_URI, columnNames,
                    columnValues);
            }
        }
        return contentUri;
    }

    @Override
    public boolean delete() {
        boolean retVal;

        Uri recUri = getContentUri();
        if (recUri != null) {
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

    /**
     * Delete all receipts from ExpenseIt result
     */
    @Override
    public void deleteAll() {
        ContentResolver resolver = context.getContentResolver();

        // delete all records
        int numOfRecordsDeleted = resolver.delete(Expense.ExpenseItReceiptColumns.CONTENT_URI, null, null);

        Log.d(Const.LOG_TAG, CLS_TAG + ".deleteAll: number of expenseIt entries deleted '" + numOfRecordsDeleted);
    }


    @Override
    public boolean update() {
        boolean retVal;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.ID, id);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.USER_ID, userId);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.REPORT_ID, reportId);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.NOTE, note);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.CCTYPE, ccType);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.CREATED_AT, createdAt == null ? null : createdAt.getTimeInMillis());
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT, sendToCteAt == null ? null : sendToCteAt.getTimeInMillis());
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL, imageDataUrl);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT, totalImageCount);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED, totalImagesUploaded);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE, parsingStatusCode);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE, processingEngine);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.ETA, eta);

        // Grab the content URI if any.
        Uri expenseItUri = getContentUri();

        if (expenseItUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(expenseItUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + expenseItUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.ExpenseItReceiptColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + expenseItUri.toString()
                        + "'.");
                }
                retVal = true;
            }
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.ExpenseItReceiptColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }

        return retVal;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Long getReportId() {
        return reportId;
    }

    @Override
    public String getNote() {
        return note;
    }

    @Override
    public String getCcType() {
        return ccType;
    }

    @Override
    public Calendar getCreatedAt() {
        return createdAt;
    }

    @Override
    public Calendar getSendToCteAt() {
        return sendToCteAt;
    }

    @Override
    public String getImageDataUrl() {
        return imageDataUrl;
    }

    @Override
    public int getTotalImageCount() {
        return totalImageCount;
    }

    @Override
    public int getTotalImagesUploaded() {
        return totalImagesUploaded;
    }

    @Override
    public int getParsingStatusCode() {
        return parsingStatusCode;
    }

    @Override
    public String getProcessingEngine() {
        return processingEngine;
    }

    @Override
    public int getEta() {
        return eta;
    }

    @Override
    public void setId(long id) {
        this.id = id;
    }

    @Override
    public void setReportId(long reportId) {
        this.reportId = reportId;
    }

    @Override
    public void setNote(String note) {
        this.note = note;
    }

    @Override
    public void setCcType(String ccType) {
        this.ccType = ccType;
    }

    @Override
    public void setCreatedAt(Calendar createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public void setSendToCteAt(Calendar sendToCteAt) {
        this.sendToCteAt = sendToCteAt;
    }

    @Override
    public void setImageDataUrl(String imageDataUrl) {
        this.imageDataUrl = imageDataUrl;
    }

    @Override
    public void setTotalImageCount(int totalImageCount) {
        this.totalImageCount = totalImageCount;
    }

    @Override
    public void setTotalImagesUploaded(int totalImagesUploaded) {
        this.totalImagesUploaded = totalImagesUploaded;
    }

    @Override
    public void setParsingStatusCode(int parsingStatusCode) {
        this.parsingStatusCode = parsingStatusCode;
    }

    @Override
    public void setProcessingEngine(String processingEngine) {
        this.processingEngine = processingEngine;
    }

    @Override
    public void setEta(int eta) {
        this.eta = eta;
    }


}
