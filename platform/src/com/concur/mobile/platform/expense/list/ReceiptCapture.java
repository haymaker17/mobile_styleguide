/**
 * 
 */
package com.concur.mobile.platform.expense.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.expense.list.dao.ReceiptCaptureDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a receipt capture.
 * 
 * @author andrewk
 */
public class ReceiptCapture extends BaseParser implements ReceiptCaptureDAO {

    private static final String CLS_TAG = "ReceiptCapture";

    public static final String TAG_RECEIPT_CAPTURE = "ReceiptCapture";

    public static String[] fullColumnList = { Expense.ReceiptCaptureColumns._ID, Expense.ReceiptCaptureColumns.TYPE,
            Expense.ReceiptCaptureColumns.CRN_CODE, Expense.ReceiptCaptureColumns.EXP_KEY,
            Expense.ReceiptCaptureColumns.EXP_NAME, Expense.ReceiptCaptureColumns.VENDOR_NAME,
            Expense.ReceiptCaptureColumns.RC_KEY, Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID,
            Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT, Expense.ReceiptCaptureColumns.TRANSACTION_DATE,
            Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID, Expense.ReceiptCaptureColumns.TAG, };

    // tags.
    private static final String TAG_CRN_CODE = "CrnCode";
    private static final String TAG_EXP_KEY = "ExpKey";
    private static final String TAG_EXP_NAME = "ExpName";
    private static final String TAG_RECEIPT_IMAGE_ID = "ReceiptImageId";
    private static final String TAG_RC_KEY = "RcKey";
    private static final String TAG_TRANSACTION_AMOUNT = "TransactionAmount";
    private static final String TAG_TRANSACTION_DATE = "TransactionDate";
    private static final String TAG_SMRT_EXP_ID = "SmartExpenseId";
    private static final String TAG_VENDOR_NAME = "VendorName";

    // tag codes.
    private static final int TAG_CRN_CODE_CODE = 0;
    private static final int TAG_EXP_KEY_CODE = 1;
    private static final int TAG_EXP_NAME_CODE = 2;
    private static final int TAG_RECEIPT_IMAGE_ID_CODE = 3;
    private static final int TAG_RC_KEY_CODE = 4;
    private static final int TAG_TRANSACTION_AMOUNT_CODE = 5;
    private static final int TAG_TRANSACTION_DATE_CODE = 6;
    private static final int TAG_SMRT_EXP_ID_CODE = 7;
    private static final int TAG_VENDOR_NAME_CODE = 8;

    // Contains the map from tags to codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CRN_CODE, TAG_CRN_CODE_CODE);
        tagMap.put(TAG_EXP_KEY, TAG_EXP_KEY_CODE);
        tagMap.put(TAG_EXP_NAME, TAG_EXP_NAME_CODE);
        tagMap.put(TAG_RECEIPT_IMAGE_ID, TAG_RECEIPT_IMAGE_ID_CODE);
        tagMap.put(TAG_RC_KEY, TAG_RC_KEY_CODE);
        tagMap.put(TAG_TRANSACTION_AMOUNT, TAG_TRANSACTION_AMOUNT_CODE);
        tagMap.put(TAG_TRANSACTION_DATE, TAG_TRANSACTION_DATE_CODE);
        tagMap.put(TAG_SMRT_EXP_ID, TAG_SMRT_EXP_ID_CODE);
        tagMap.put(TAG_VENDOR_NAME, TAG_VENDOR_NAME_CODE);
    }

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.RECEIPT_CAPTURE;

    /**
     * Contains the transaction currency code.
     */
    String crnCode;

    /**
     * Contains the expense entry type key.
     */
    String expKey;

    /**
     * Contains the expense name.
     */
    String expName;

    /**
     * Contains the expense vendor name.
     */
    String vendorName;

    /**
     * Contains the receipt capture expense key
     */
    String rcKey;

    /**
     * Contains smart expense id which is same as rcKey
     */
    String smartExpId;

    /**
     * Contains the expense transaction amount.
     */
    Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    Calendar transactionDate;

    /**
     * Contains the receipt image id.
     */
    String receiptImageId;

    /**
     * Contains a tag.
     */
    String tag;

    /**
     * Contains the content Uri.
     */
    Uri contentUri;

    public ReceiptCapture() {
    }

    /**
     * Constructs an instance of <code>ReceiptCapture</code> given a context and an Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content uri.
     */
    public ReceiptCapture(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.ReceiptCaptureColumns.DEFAULT_SORT_ORDER);
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
     * Constructs an instance of <code>ReceiptCapture</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     */
    public ReceiptCapture(Context context, Cursor cursor) {
        init(cursor);
    }

    /**
     * Will initialize this <code>ReceiptCapture</code> object given information in <code>cursor</code>.
     * 
     * @param cursor
     *            contains the cursor.
     */
    private void init(Cursor cursor) {
        String typeName = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.TYPE);
        if (!TextUtils.isEmpty(typeName)) {
            type = ExpenseTypeEnum.valueOf(typeName);
        }
        crnCode = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.CRN_CODE);
        expKey = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.EXP_KEY);
        expName = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.EXP_NAME);
        vendorName = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.VENDOR_NAME);
        rcKey = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.RC_KEY);
        smartExpId = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID);
        transactionAmount = CursorUtil.getDoubleValue(cursor, Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT);
        Long transDateMillis = CursorUtil.getLongValue(cursor, Expense.ReceiptCaptureColumns.TRANSACTION_DATE);
        if (transDateMillis != null) {
            transactionDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            transactionDate.setTimeInMillis(transDateMillis);
            transactionDate.set(Calendar.MILLISECOND, 0);
        }
        receiptImageId = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID);
        tag = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.TAG);
        Long contentId = CursorUtil.getLongValue(cursor, Expense.ReceiptCaptureColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.ReceiptCaptureColumns.CONTENT_URI, contentId);
        }
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case TAG_CRN_CODE_CODE: {
                    crnCode = text.trim();
                    break;
                }
                case TAG_EXP_KEY_CODE: {
                    expKey = text.trim();
                    break;
                }
                case TAG_EXP_NAME_CODE: {
                    expName = text.trim();
                    break;
                }
                case TAG_RECEIPT_IMAGE_ID_CODE: {
                    receiptImageId = text.trim();
                    break;
                }
                case TAG_RC_KEY_CODE: {
                    rcKey = text.trim();
                    break;
                }
                case TAG_TRANSACTION_AMOUNT_CODE: {
                    transactionAmount = Parse.safeParseDouble(text.trim());
                    break;
                }
                case TAG_TRANSACTION_DATE_CODE: {
                    transactionDate = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case TAG_SMRT_EXP_ID_CODE: {
                    smartExpId = text.trim();
                    break;
                }
                case TAG_VENDOR_NAME_CODE: {
                    vendorName = text.trim();
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + "'.");
            }
        }
    }

    // Start DAO methods.

    @Override
    public ExpenseTypeEnum getType() {
        return type;
    }

    @Override
    public String getCurrencyCode() {
        return crnCode;
    }

    @Override
    public void setCurrencyCode(String crnCode) {
        this.crnCode = crnCode;
    }

    @Override
    public String getExpKey() {
        return expKey;
    }

    @Override
    public void setExpKey(String expKey) {
        this.expKey = expKey;
    }

    @Override
    public String getExpName() {
        return expName;
    }

    @Override
    public void setExpName(String expName) {
        this.expName = expName;
    }

    @Override
    public String getVendorName() {
        return vendorName;
    }

    @Override
    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    @Override
    public String getRCKey() {
        return rcKey;
    }

    @Override
    public void setRCKey(String rcKey) {
        this.rcKey = rcKey;
    }

    @Override
    public String getSmartExpenseId() {
        return smartExpId;
    }

    @Override
    public void setSmartExpenseId(String smartExpenseId) {
        this.smartExpId = smartExpenseId;
    }

    @Override
    public Double getTransactionAmount() {
        return transactionAmount;
    }

    @Override
    public void setTransactionAmount(Double transactionAmount) {
        this.transactionAmount = transactionAmount;
    }

    @Override
    public Calendar getTransactionDate() {
        return transactionDate;
    }

    @Override
    public void setTransactionDate(Calendar transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public String getReceiptImageId() {
        return receiptImageId;
    }

    @Override
    public void setReceiptImageId(String receiptImageId) {
        this.receiptImageId = receiptImageId;
    }

    @Override
    public String getTag() {
        return tag;
    }

    @Override
    public void setTag(String tag) {
        this.tag = tag;
    }

    @Override
    public boolean update(Context context, String userId) {
        boolean retVal = true;

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        String typeName = null;
        if (type != null) {
            typeName = type.name();
        }
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.TYPE, typeName);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.CRN_CODE, crnCode);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.EXP_KEY, expKey);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.EXP_NAME, expName);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.VENDOR_NAME, vendorName);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.RC_KEY, rcKey);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID, smartExpId);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT, transactionAmount);
        Long transDateInMillis = null;
        if (transactionDate != null) {
            transDateInMillis = transactionDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.TRANSACTION_DATE, transDateInMillis);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID, receiptImageId);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.TAG, tag);
        ContentUtils.putValue(values, Expense.ReceiptCaptureColumns.USER_ID, userId);

        // Grab the content URI if any.
        Uri recCapUri = getContentURI(context);

        if (recCapUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(recCapUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + recCapUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.ReceiptCaptureColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + recCapUri.toString()
                            + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.ReceiptCaptureColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }
        return retVal;
    }

    @Override
    public Uri getContentURI(Context context) {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(rcKey)) {
                contentUri = ContentUtils.getContentUri(context, Expense.ReceiptCaptureColumns.CONTENT_URI,
                        Expense.ReceiptCaptureColumns.RC_KEY, rcKey);
            }
        }
        return contentUri;
    }

    @Override
    public void setContentURI(Uri contentUri) {
        this.contentUri = contentUri;
    }

    // End DAO methods.

    /**
     * Will reconcile a list of <code>ReceiptCapture</code> objects with entries in the expense database punting any entries in
     * the database with non-null keys that are not contained within <code>recCaps</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @param recCaps
     *            contains a list of <code>ReceiptCapture</code> objects.
     */
    public static void reconcile(Context context, String userId, List<ReceiptCapture> recCaps) {

        // First, build a simple map to permit quick look-up based on RC key.
        Map<String, ReceiptCapture> rcKeyMap = new HashMap<String, ReceiptCapture>(recCaps.size());
        for (ReceiptCapture recCap : recCaps) {
            rcKeyMap.put(recCap.rcKey, recCap);
        }

        // Second, read in content id and RC_KEY.
        List<Long> idsToBePunted = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] recCapColumns = { Expense.ReceiptCaptureColumns._ID, Expense.ReceiptCaptureColumns.RC_KEY };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.ReceiptCaptureColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.ReceiptCaptureColumns.CONTENT_URI, recCapColumns, where, whereArgs,
                    Expense.ReceiptCaptureColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Long contentId = CursorUtil.getLongValue(cursor, Expense.ReceiptCaptureColumns._ID);
                        String recCapKey = CursorUtil.getStringValue(cursor, Expense.ReceiptCaptureColumns.RC_KEY);
                        if (!TextUtils.isEmpty(recCapKey) && !rcKeyMap.containsKey(recCapKey)) {
                            idsToBePunted.add(contentId);
                        }
                    } while (cursor.moveToNext());
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        // Third, punt transactions.
        for (Long contId : idsToBePunted) {
            Uri contUri = ContentUris.withAppendedId(Expense.ReceiptCaptureColumns.CONTENT_URI, contId);
            int rowsAffected = resolver.delete(contUri, null, null);
            if (rowsAffected != 1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".reconcile: 0 rows affected for deletion of '" + contUri.toString()
                        + "'.");
            }
        }
    }

}
