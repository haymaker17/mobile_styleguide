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
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing a cash expense.
 * 
 * @author andrewk
 */
public class MobileEntry extends BaseParser implements MobileEntryDAO {

    private static final String CLS_TAG = "MobileEntry";

    public static final String TAG_MOBILE_ENTRY = "MobileEntry";

    public static String[] fullColumnList = { Expense.MobileEntryColumns._ID, Expense.MobileEntryColumns.CRN_CODE,
            Expense.MobileEntryColumns.EXP_KEY, Expense.MobileEntryColumns.EXP_NAME,
            Expense.MobileEntryColumns.LOCATION_NAME, Expense.MobileEntryColumns.VENDOR_NAME,
            Expense.MobileEntryColumns.TYPE, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY,
            Expense.MobileEntryColumns.PCA_KEY, Expense.MobileEntryColumns.PCT_KEY, Expense.MobileEntryColumns.CCT_KEY,
            Expense.MobileEntryColumns.RC_KEY, Expense.MobileEntryColumns.TRANSACTION_AMOUNT,
            Expense.MobileEntryColumns.TRANSACTION_DATE, Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE,
            Expense.MobileEntryColumns.RECEIPT_IMAGE_ID, Expense.MobileEntryColumns.RECEIPT_CONTENT_ID,
            Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA,
            Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH, Expense.MobileEntryColumns.COMMENT,
            Expense.MobileEntryColumns.TAG };

    // tags.
    private static final String TAG_CRN_CODE = "CrnCode";
    private static final String TAG_EXP_KEY = "ExpKey";
    private static final String TAG_EXP_NAME = "ExpName";
    private static final String TAG_HAS_RECEIPT_IMAGE = "HasReceiptImage";
    private static final String TAG_RECEIPT_IMAGE_ID = "ReceiptImageId";
    private static final String TAG_RECEIPT_IMAGE = "ReceiptImage";
    private static final String TAG_LOCATION_NAME = "LocationName";
    private static final String TAG_ME_KEY = "MeKey";
    private static final String TAG_TRANSACTION_AMOUNT = "TransactionAmount";
    private static final String TAG_TRANSACTION_DATE = "TransactionDate";
    private static final String TAG_COMMENT = "Comment";
    private static final String TAG_VENDOR_NAME = "VendorName";

    // tag codes.
    private static final int TAG_CRN_CODE_CODE = 0;
    private static final int TAG_EXP_KEY_CODE = 1;
    private static final int TAG_EXP_NAME_CODE = 2;
    private static final int TAG_HAS_RECEIPT_IMAGE_CODE = 3;
    private static final int TAG_RECEIPT_IMAGE_ID_CODE = 4;
    private static final int TAG_RECEIPT_IMAGE_CODE = 5;
    private static final int TAG_LOCATION_NAME_CODE = 6;
    private static final int TAG_ME_KEY_CODE = 7;
    private static final int TAG_TRANSACTION_AMOUNT_CODE = 8;
    private static final int TAG_TRANSACTION_DATE_CODE = 9;
    private static final int TAG_COMMENT_CODE = 10;
    private static final int TAG_VENDOR_NAME_CODE = 11;

    // Contains the map from tags to codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CRN_CODE, TAG_CRN_CODE_CODE);
        tagMap.put(TAG_EXP_KEY, TAG_EXP_KEY_CODE);
        tagMap.put(TAG_EXP_NAME, TAG_EXP_NAME_CODE);
        tagMap.put(TAG_HAS_RECEIPT_IMAGE, TAG_HAS_RECEIPT_IMAGE_CODE);
        tagMap.put(TAG_RECEIPT_IMAGE_ID, TAG_RECEIPT_IMAGE_ID_CODE);
        tagMap.put(TAG_RECEIPT_IMAGE, TAG_RECEIPT_IMAGE_CODE);
        tagMap.put(TAG_LOCATION_NAME, TAG_LOCATION_NAME_CODE);
        tagMap.put(TAG_ME_KEY, TAG_ME_KEY_CODE);
        tagMap.put(TAG_TRANSACTION_AMOUNT, TAG_TRANSACTION_AMOUNT_CODE);
        tagMap.put(TAG_TRANSACTION_DATE, TAG_TRANSACTION_DATE_CODE);
        tagMap.put(TAG_COMMENT, TAG_COMMENT_CODE);
        tagMap.put(TAG_VENDOR_NAME, TAG_VENDOR_NAME_CODE);
    }

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
     * Contains the expense location name.
     */
    String locationName;

    /**
     * Contains the expense vendor name.
     */
    String vendorName;

    /**
     * Contains the expense entry type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.CASH;

    /**
     * Contains the mobile expense entry key.
     */
    String meKey;

    /**
     * Contains the personal card account key containing a transaction associated with this mobile entry.
     */
    String pcaKey;

    /**
     * Contains the personal card transaction key for a transaction associated with this mobile entry.
     */
    String pctKey;

    /**
     * Contains the corporate card transaction key for a corporate card transaction associated with this mobile entry.
     */
    String cctKey;

    /**
     * Contains the receipt capture key for a receipt capture transaction associated with this mobile entry.
     */
    String rcKey;

    /**
     * Contains the expense transaction amount.
     */
    Double transactionAmount;

    /**
     * Contains the expense transaction date.
     */
    Calendar transactionDate;

    /**
     * Contains whether the expense entry has a receipt image.
     */
    boolean hasReceiptImage;

    /**
     * Contains the receipt image id.
     */
    String receiptImageId;

    /**
     * Contains the URI of the associated receipt content in the <code>Expense.ReceiptColumns.TABLE_NAME</code> table.
     */
    Uri receiptContentUri;

    /**
     * Contains the base-64 encoded receipt image data.
     */
    String receiptImageData;

    /**
     * Contains the receipt image local file path.
     */
    String receiptImageDataLocalFilePath;

    /**
     * Contains the expense comment.
     */
    String comment;

    /**
     * Contains a tag.
     */
    String tag;

    /**
     * Contains the content Uri.
     */
    Uri contentUri;

    /**
     * Constructs an instance of <code>MobileEntry</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     *            contains the cursor.
     */
    public MobileEntry(Cursor cursor) {
        init(cursor);
    }

    /**
     * Constructs an instance of <code>MobileEntry</code> based on reading values from a <code>Uri</code> object.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content Uri.
     */
    public MobileEntry(Context context, Uri contentUri) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.MobileEntryColumns.DEFAULT_SORT_ORDER);
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
     * Constructs a new instnace of <code>MobileEntry</code>.
     */
    public MobileEntry() {
    }

    /**
     * Will initialize the mobile entry fields from a cursor object.
     * 
     * @param cursor
     *            contains the cursor used to initialize fields.
     */
    private void init(Cursor cursor) {
        crnCode = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.CRN_CODE);
        expKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.EXP_KEY);
        expName = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.EXP_NAME);
        locationName = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.LOCATION_NAME);
        vendorName = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.VENDOR_NAME);
        String entryTypeName = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.TYPE);
        if (!TextUtils.isEmpty(entryTypeName)) {
            type = ExpenseTypeEnum.valueOf(entryTypeName);
        }
        meKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY);
        pcaKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.PCA_KEY);
        pctKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.PCT_KEY);
        cctKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.CCT_KEY);
        rcKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.RC_KEY);
        transactionAmount = CursorUtil.getDoubleValue(cursor, Expense.MobileEntryColumns.TRANSACTION_AMOUNT);
        Long transDateMillis = CursorUtil.getLongValue(cursor, Expense.MobileEntryColumns.TRANSACTION_DATE);
        if (transDateMillis != null) {
            transactionDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            transactionDate.setTimeInMillis(transDateMillis);
            transactionDate.set(Calendar.MILLISECOND, 0);
        }
        hasReceiptImage = CursorUtil.getBooleanValue(cursor, Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE);
        receiptImageId = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.RECEIPT_IMAGE_ID);
        Long receiptContentId = CursorUtil.getLongValue(cursor, Expense.MobileEntryColumns.RECEIPT_CONTENT_ID);
        if (receiptContentId != null) {
            receiptContentUri = ContentUris.withAppendedId(Expense.ReceiptColumns.CONTENT_URI, receiptContentId);
        }
        receiptImageData = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA);
        receiptImageDataLocalFilePath = CursorUtil.getStringValue(cursor,
                Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
        comment = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.COMMENT);
        tag = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.TAG);

        Long contentId = CursorUtil.getLongValue(cursor, Expense.MobileEntryColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.MobileEntryColumns.CONTENT_URI, contentId);
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
                case TAG_HAS_RECEIPT_IMAGE_CODE: {
                    hasReceiptImage = Parse.safeParseBoolean(text.trim());
                    break;
                }
                case TAG_RECEIPT_IMAGE_ID_CODE: {
                    receiptImageId = text.trim();
                    break;
                }
                case TAG_RECEIPT_IMAGE_CODE: {
                    receiptImageData = text.trim();
                    break;
                }
                case TAG_LOCATION_NAME_CODE: {
                    locationName = text.trim();
                    break;
                }
                case TAG_ME_KEY_CODE: {
                    meKey = text.trim();
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
                case TAG_COMMENT_CODE: {
                    comment = text.trim();
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
    public String getCrnCode() {
        return crnCode;
    }

    @Override
    public void setCrnCode(String crnCode) {
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
    public String getLocationName() {
        return locationName;
    }

    @Override
    public void setLocationName(String locationName) {
        this.locationName = locationName;
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
    public ExpenseTypeEnum getEntryType() {
        return type;
    }

    @Override
    public void setEntryType(ExpenseTypeEnum entryType) {
        this.type = entryType;
    }

    @Override
    public String getMeKey() {
        return meKey;
    }

    @Override
    public void setMeKey(String meKey) {
        this.meKey = meKey;
    }

    @Override
    public String getPcaKey() {
        return pcaKey;
    }

    @Override
    public void setPcaKey(String pcaKey) {
        this.pcaKey = pcaKey;
    }

    @Override
    public String getPctKey() {
        return pctKey;
    }

    @Override
    public void setPctKey(String pctKey) {
        this.pctKey = pctKey;
    }

    @Override
    public String getCctKey() {
        return cctKey;
    }

    @Override
    public void setCctKey(String cctKey) {
        this.cctKey = cctKey;
    }

    @Override
    public String getRcKey() {
        return rcKey;
    }

    @Override
    public void setRcKey(String rcKey) {
        this.rcKey = rcKey;
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
    public boolean hasReceiptImage() {
        return hasReceiptImage;
    }

    @Override
    public void setHasReceiptImage(boolean hasReceiptImage) {
        this.hasReceiptImage = hasReceiptImage;
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
    public Uri getReceiptContentUri() {
        return receiptContentUri;
    }

    @Override
    public void setReceiptContentUri(Uri receiptContentUri) {
        this.receiptContentUri = receiptContentUri;
    }

    @Override
    public String getReceiptImageData() {
        return receiptImageData;
    }

    @Override
    public void setReceiptImageData(String receiptImageData) {
        this.receiptImageData = receiptImageData;
    }

    @Override
    public String getReceiptImageDataLocalFilePath() {
        return receiptImageDataLocalFilePath;
    }

    @Override
    public void setReceiptImageDataLocalFilePath(String receiptImageDataLocalFilePath) {
        this.receiptImageDataLocalFilePath = receiptImageDataLocalFilePath;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
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
        ContentUtils.putValue(values, Expense.MobileEntryColumns.CRN_CODE, crnCode);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.EXP_KEY, expKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.EXP_NAME, expName);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.LOCATION_NAME, locationName);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.VENDOR_NAME, vendorName);
        String entryTypeStr = null;
        if (type != null) {
            entryTypeStr = type.name();
        }
        ContentUtils.putValue(values, Expense.MobileEntryColumns.TYPE, entryTypeStr);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY, meKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.PCA_KEY, pcaKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.PCT_KEY, pctKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.CCT_KEY, cctKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.RC_KEY, rcKey);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.TRANSACTION_AMOUNT, transactionAmount);
        Long transDateInMillis = null;
        if (transactionDate != null) {
            transDateInMillis = transactionDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.MobileEntryColumns.TRANSACTION_DATE, transDateInMillis);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE, hasReceiptImage);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.RECEIPT_IMAGE_ID, receiptImageId);
        if (receiptContentUri != null) {
            ContentUtils.putValue(values, Expense.MobileEntryColumns.RECEIPT_CONTENT_ID,
                    ContentUris.parseId(receiptContentUri));
        }
        ContentUtils.putValue(values, Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA, receiptImageData);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH,
                receiptImageDataLocalFilePath);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.COMMENT, comment);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.TAG, tag);
        ContentUtils.putValue(values, Expense.MobileEntryColumns.USER_ID, userId);

        // Ensure 'contentUri' gets set.
        getContentURI(context);
        if (contentUri != null) {
            // Perform an update.
            int rowsUpdated = resolver.update(contentUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + contentUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.MobileEntryColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + contentUri.toString()
                            + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.MobileEntryColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }
        return retVal;
    }

    @Override
    public Uri getContentURI(Context context) {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(meKey)) {
                contentUri = ContentUtils.getContentUri(context, Expense.MobileEntryColumns.CONTENT_URI,
                        Expense.MobileEntryColumns.MOBILE_ENTRY_KEY, meKey);
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
     * Will reconcile a list of <code>MobileEntry</code> objects with entries in the expense database punting any entries in the
     * database with non-null keys that are not contained within <code>persCards</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @param mobileEntries
     *            contains a list of <code>MobileEntry</code> objects.
     */
    public static void reconcile(Context context, String userId, List<MobileEntry> mobileEntries) {

        // First, build a simple map to permit quick look-up based on RC key.
        Map<String, MobileEntry> meKeyMap = new HashMap<String, MobileEntry>(mobileEntries.size());
        for (MobileEntry mobileEntry : mobileEntries) {
            meKeyMap.put(mobileEntry.meKey, mobileEntry);
        }

        // Second, read in content id and ME_KEY.
        List<Long> idsToBePunted = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] meColumns = { Expense.MobileEntryColumns._ID, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.MobileEntryColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.MobileEntryColumns.CONTENT_URI, meColumns, where, whereArgs,
                    Expense.MobileEntryColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Long contentId = CursorUtil.getLongValue(cursor, Expense.MobileEntryColumns._ID);
                        String meKey = CursorUtil.getStringValue(cursor, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY);
                        if (!TextUtils.isEmpty(meKey) && !meKeyMap.containsKey(meKey)) {
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

        // Third, punt mobile entries.
        for (Long contId : idsToBePunted) {
            Uri contUri = ContentUris.withAppendedId(Expense.MobileEntryColumns.CONTENT_URI, contId);
            int rowsAffected = resolver.delete(contUri, null, null);
            if (rowsAffected != 1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".reconcile: 0 rows affected for deletion of '" + contUri.toString()
                        + "'.");
            }
        }
    }

}
