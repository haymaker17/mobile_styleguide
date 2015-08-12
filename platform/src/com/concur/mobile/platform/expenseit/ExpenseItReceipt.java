/*
* Copyright (c) 2015 Concur Technologies, Inc.
*/

package com.concur.mobile.platform.expenseit;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.expenseit.dao.ExpenseItReceiptDAO;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;
import com.google.gson.annotations.SerializedName;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ExpenseItReceipt implements ExpenseItReceiptDAO, Serializable {

    private static final String CLS_TAG = ExpenseItReceipt.class.getSimpleName();

    private static final long serialVersionUID = -8302495324270296222L;

    private static final int MAX_IMAGE_BYTE_SIZE = 800000;

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

    @SerializedName("errorCode")
    private int errorCode;

    @SerializedName("errorMessage")
    private String errorMessage;

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
     * Default constructor.
     */
    public ExpenseItReceipt() {
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
     * get the full list of expenseIt Receipts from expense.db
     *
     * @return
     */
    @Override
    public List<ExpenseItPostReceipt> getReceipts() {

        List<ExpenseItPostReceipt> receipts = new ArrayList<>();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        if (userId == null) {
            throw new IllegalArgumentException("UserId should be set");
        }
        try {
            StringBuilder statement = new StringBuilder();
            statement.append(Expense.ExpenseItReceiptColumns.USER_ID);
            statement.append(" = ?");
            String[] whereArgs = {userId};
            cursor = resolver.query(Expense.ExpenseItReceiptColumns.CONTENT_URI, null, statement.toString(), whereArgs, Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    receipts.add(getReceipt(cursor));
                }
            }
        } catch (Exception e) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getReceipts() Error while retrieving receipt info");
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return receipts;
    }

    private static ExpenseItPostReceipt getReceipt(Cursor cursor) {
        ExpenseItPostReceipt receipt = new ExpenseItPostReceipt();
        receipt.setId(CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.ID));
        receipt.setUserId(CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.USER_ID));
        receipt.setReportId(CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.REPORT_ID));
        receipt.setNote(CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.NOTE));
        receipt.setCcType(CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.CCTYPE));
        Long createdMilliSeconds = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.CREATED_AT);
        if (createdMilliSeconds != null) {
            Calendar createdAtCol = Calendar.getInstance(Parse.UTC);
            createdAtCol.setTimeInMillis(createdMilliSeconds);
            createdAtCol.set(Calendar.MILLISECOND, 0);
            receipt.setCreatedAt(createdAtCol.getTime());
        }
        Long sentToCteMilliSeconds = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT);
        if (sentToCteMilliSeconds != null) {
            Calendar sendToCteAtCol = Calendar.getInstance(Parse.UTC);
            sendToCteAtCol.setTimeInMillis(sentToCteMilliSeconds);
            sendToCteAtCol.set(Calendar.MILLISECOND, 0);
            receipt.setSendToCteAt(sendToCteAtCol.getTime());
        }

        receipt.setTotalImageCount(CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT));
        receipt.setTotalImagesUploaded(CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED));
        receipt.setParsingStatusCode(CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE));
        receipt.setProcessingEngine(CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE));
        receipt.setExpenseErrorCode(CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.ERROR_CODE));
        receipt.setExpenseErrorMessage(CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.ERROR_MESSAGE));

        receipt.setEta(CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.ETA));

        receipt.setContentId(CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns._ID));

        return receipt;
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
        Long sentToCtedMilliSeconds = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT);
        if (sentToCtedMilliSeconds != null) {
            sendToCteAt = Calendar.getInstance(Parse.UTC);
            sendToCteAt.setTimeInMillis(sentToCtedMilliSeconds);
            sendToCteAt.set(Calendar.MILLISECOND, 0);
        }
        //imageData = CursorUtil.getBlobValue(cursor, Expense.ExpenseItReceiptColumns.IMAGE_DATA);
        totalImageCount = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT);
        totalImagesUploaded = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED);
        parsingStatusCode = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE);
        processingEngine = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE);

        Integer errCode = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.ERROR_CODE);
        if (errCode != null) {
            errorCode = errCode;
        }
        errorMessage = CursorUtil.getStringValue(cursor, Expense.ExpenseItReceiptColumns.ERROR_MESSAGE);


        eta = CursorUtil.getIntValue(cursor, Expense.ExpenseItReceiptColumns.ETA);


        Long contentId = CursorUtil.getLongValue(cursor, Expense.ExpenseItReceiptColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.ExpenseItReceiptColumns.CONTENT_URI, contentId);
        }
    }

    @Override
    public Uri getContentUri(Context context, String userId) {
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
    public boolean delete(Context context, String userId) {
        boolean retVal;

        contentUri = getContentUri(context, userId);
        if (contentUri != null) {
            ContentResolver resolver = context.getContentResolver();
            int count = resolver.delete(contentUri, null, null);
            retVal = (count == 1);
            // Clear out the URI.
            contentUri = null;
        } else {
            retVal = false;
        }
        return retVal;
    }

    @Override
    public boolean update(Context context, String userId) {
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
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT, totalImageCount);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED, totalImagesUploaded);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE, parsingStatusCode);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE, processingEngine);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.ERROR_CODE, errorCode);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.ERROR_MESSAGE, errorMessage);
        ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.ETA, eta);

        // Grab the content URI if any.
        Uri expenseItUri = getContentUri(context, userId);

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
    public Bitmap getImageData() {
        ContentResolver resolver = context.getContentResolver();
        Uri expenseItUri = getContentUri(context, userId);
        Cursor cursor = null;
        byte[] blob = null;
        Bitmap bitmap = null;

        cursor = resolver.query(expenseItUri, null, null, null, Expense.ExpenseItReceiptColumns
                .DEFAULT_SORT_ORDER);
        if (cursor != null && cursor.moveToNext()) {
            blob = CursorUtil.getBlobValue(cursor, Expense.ExpenseItReceiptColumns.IMAGE_DATA);
        }

        if (blob != null) {
            bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.length);
        }

        return bitmap;
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
    public int getErrorCode() {
        // TODO - CDIAZ - parse error code from JSON.
        return errorCode;
    }

    @Override
    public String getErrorMessage() {
        // TODO - CDIAZ - parse error message from JSON.
        return errorMessage;
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
    public void setImageData(Bitmap imageData) {
        ContentResolver resolver = context.getContentResolver();
        ContentValues values = new ContentValues();
        ByteArrayOutputStream byteArrayOutputStream = null;
        byte[] imageBytes = null;
        Uri expenseItUri = null;
        int rowsUpdated = 0;

        if (imageData == null) return;

        try {
            byteArrayOutputStream = new ByteArrayOutputStream();
            imageData.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            byteArrayOutputStream.close();
            imageBytes = byteArrayOutputStream.toByteArray();

            if (imageBytes != null) {
                if (imageBytes.length > MAX_IMAGE_BYTE_SIZE) {
                    //TODO: Attempt to reduce image size
                    //ViewUtils.compressAndRotateImage();
                }

                if (imageBytes.length < MAX_IMAGE_BYTE_SIZE) {
                    ContentUtils.putValue(values, Expense.ExpenseItReceiptColumns.IMAGE_DATA,
                            imageBytes);

                    expenseItUri = getContentUri(context, userId);
                    if (expenseItUri != null) {
                        rowsUpdated = resolver.update(expenseItUri, values, null, null);

                        if (rowsUpdated == 0){
                            Log.e(Const.LOG_TAG, CLS_TAG + ".Unable to update image data to " +
                                    "receipt");
                        }
                    }
                }
            }
        } catch (Exception ex) {
            Log.e(Const.LOG_TAG, CLS_TAG + ex.getMessage());
        }
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

    @Override
    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
