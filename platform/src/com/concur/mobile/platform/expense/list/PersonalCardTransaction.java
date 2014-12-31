package com.concur.mobile.platform.expense.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.Assert;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.base.service.parser.ItemParser;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.list.dao.PersonalCardTransactionDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing personal card transaction.
 * 
 * @author andrewk
 */
public class PersonalCardTransaction extends BaseParser implements PersonalCardTransactionDAO {

    private static final String CLS_TAG = "PersonalCardTransaction";

    public static final String TAG_PERSONAL_CARD_TRANSACTION = "PersonalCardTransaction";

    public static String[] fullColumnList = { Expense.PersonalCardTransactionColumns._ID,
            Expense.PersonalCardTransactionColumns.TYPE, Expense.PersonalCardTransactionColumns.PCT_KEY,
            Expense.PersonalCardTransactionColumns.DATE_POSTED, Expense.PersonalCardTransactionColumns.DESCRIPTION,
            Expense.PersonalCardTransactionColumns.AMOUNT, Expense.PersonalCardTransactionColumns.CRN_CODE,
            Expense.PersonalCardTransactionColumns.STATUS, Expense.PersonalCardTransactionColumns.CATEGORY,
            Expense.PersonalCardTransactionColumns.EXP_KEY, Expense.PersonalCardTransactionColumns.EXP_NAME,
            Expense.PersonalCardTransactionColumns.RPT_KEY, Expense.PersonalCardTransactionColumns.RPT_NAME,
            Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY,
            Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID,
            Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID, Expense.PersonalCardTransactionColumns.TAG,
            Expense.PersonalCardTransactionColumns.IS_SPLIT };

    // tags
    private static final String TAG_PCT_KEY = "PctKey";
    private static final String TAG_DATE_POSTED = "DatePosted";
    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_AMOUNT = "Amount";
    private static final String TAG_STATUS = "Status";
    private static final String TAG_CATEGORY = "Category";
    private static final String TAG_EXP_KEY = "ExpKey";
    private static final String TAG_EXP_NAME = "ExpName";
    private static final String TAG_RPT_KEY = "";
    private static final String TAG_RPT_NAME = "";
    private static final String TAG_SMART_EXPENSE_ME_KEY = "SmartExpense";

    // tag codes.
    private static final int TAG_PCT_KEY_CODE = 0;
    private static final int TAG_DATE_POSTED_CODE = 1;
    private static final int TAG_DESCRIPTION_CODE = 2;
    private static final int TAG_AMOUNT_CODE = 3;
    private static final int TAG_STATUS_CODE = 4;
    private static final int TAG_CATEGORY_CODE = 5;
    private static final int TAG_EXP_KEY_CODE = 6;
    private static final int TAG_EXP_NAME_CODE = 7;
    private static final int TAG_RPT_KEY_CODE = 8;
    private static final int TAG_RPT_NAME_CODE = 9;
    private static final int TAG_SMART_EXPENSE_ME_KEY_CODE = 10;

    // Contains the map from tags to codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_PCT_KEY, TAG_PCT_KEY_CODE);
        tagMap.put(TAG_DATE_POSTED, TAG_DATE_POSTED_CODE);
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_AMOUNT, TAG_AMOUNT_CODE);
        tagMap.put(TAG_STATUS, TAG_STATUS_CODE);
        tagMap.put(TAG_CATEGORY, TAG_CATEGORY_CODE);
        tagMap.put(TAG_EXP_KEY, TAG_EXP_KEY_CODE);
        tagMap.put(TAG_EXP_NAME, TAG_EXP_NAME_CODE);
        tagMap.put(TAG_RPT_KEY, TAG_RPT_KEY_CODE);
        tagMap.put(TAG_RPT_NAME, TAG_RPT_NAME_CODE);
        tagMap.put(TAG_SMART_EXPENSE_ME_KEY, TAG_SMART_EXPENSE_ME_KEY_CODE);
    }

    // Contains the start tag.
    private String startTag;

    // Contains the item parser for an associated mobile entry object.
    private ItemParser<MobileEntry> mobileEntryParser;

    /**
     * Contains a reference to the common parser.
     */
    private CommonParser parser;

    /**
     * Contains the mobile entry item tag.
     */
    private String mobileEntryItemTag = "";

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.PERSONAL_CARD;

    /**
     * Contains the personal card transaction key.
     */
    String pctKey;
    /**
     * Contains the posted date.
     */
    Calendar datePosted;
    /**
     * Contains the description.
     */
    String description;
    /**
     * Contains the amount.
     */
    Double amount;

    /**
     * Contains the currency code.
     */
    String crnCode;

    /**
     * Contains the status.
     */
    String status;

    /**
     * Contains the category.
     */
    String category;

    /**
     * Contains the expense type key.
     */
    String expKey;

    /**
     * Contains the expense type name.
     */
    String expName;

    /**
     * Contains the report key.
     */
    String rptKey;

    /**
     * Contains the report name.
     */
    String rptName;

    /**
     * Contains the smart expense mobile entry key.
     */
    String smartExpenseMeKey;

    /**
     * Contains the mobile entry content id.
     */
    Long mobileEntryId;

    /**
     * Contains a reference to a mobile entry associated with this transaction.
     */
    MobileEntry mobileEntry;

    /**
     * Contains a reference to a mobile entry that was matched with this corporate card transaction.
     */
    MobileEntry matchedMobileEntry;

    /**
     * Contains a tag.
     */
    String tag;

    /**
     * Contains whether or not this transaction has been split.
     */
    boolean split;

    /**
     * Contains the personal card content id.
     */
    Long personalCardId;

    /**
     * Contains the content Uri.
     */
    Uri contentUri;

    private Context context;

    /**
     * Constructs an instance of <code>PersonalCardTransaction</code> given a context and an Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content uri.
     */
    public PersonalCardTransaction(Context context, Uri contentUri) {
        this.context = context;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER);
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
     * Constructs an instance of <code>PersonalCardTransaction</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     */
    public PersonalCardTransaction(Context context, Cursor cursor) {
        this.context = context;
        init(cursor);
    }

    /**
     * Will initialize this <code>PersonalCardTransaction</code> object given information in <code>cursor</code>.
     * 
     * @param cursor
     *            contains the cursor.
     */
    private void init(Cursor cursor) {
        String typeName = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.TYPE);
        if (!TextUtils.isEmpty(typeName)) {
            type = ExpenseTypeEnum.valueOf(typeName);
        }
        pctKey = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.PCT_KEY);

        Long datePostedMillis = CursorUtil.getLongValue(cursor, Expense.PersonalCardTransactionColumns.DATE_POSTED);
        if (datePostedMillis != null) {
            datePosted = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            datePosted.setTimeInMillis(datePostedMillis);
            datePosted.set(Calendar.MILLISECOND, 0);
        }
        description = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.DESCRIPTION);
        amount = CursorUtil.getDoubleValue(cursor, Expense.PersonalCardTransactionColumns.AMOUNT);
        crnCode = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.CRN_CODE);
        status = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.STATUS);
        category = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.CATEGORY);
        expKey = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.EXP_KEY);
        expName = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.EXP_NAME);
        rptKey = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.RPT_KEY);
        rptName = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.RPT_NAME);
        smartExpenseMeKey = CursorUtil.getStringValue(cursor,
                Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY);
        mobileEntryId = CursorUtil.getLongValue(cursor, Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID);
        personalCardId = CursorUtil.getLongValue(cursor, Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID);
        tag = CursorUtil.getStringValue(cursor, Expense.PersonalCardTransactionColumns.TAG);
        split = CursorUtil.getBooleanValue(cursor, Expense.PersonalCardTransactionColumns.IS_SPLIT);

        // Set the content id.
        Long contentId = CursorUtil.getLongValue(cursor, Expense.PersonalCardTransactionColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.PersonalCardTransactionColumns.CONTENT_URI, contentId);
        }
    }

    /**
     * Constructs an instance of <code>PersonalCardTransaction</code> for parsing a personal card transaction.
     * 
     * @param parser
     *            contains a reference to a common parser.
     * @param startTag
     *            contains the parse start tag.
     */
    public PersonalCardTransaction(CommonParser parser, String startTag) {

        // Set the parser reference.
        this.parser = parser;

        // Set the start tag.
        this.startTag = startTag;

        // Register the mobile entry parser.
        mobileEntryParser = new ItemParser<MobileEntry>(MobileEntry.TAG_MOBILE_ENTRY, MobileEntry.class);
        parser.registerParser(mobileEntryParser, MobileEntry.TAG_MOBILE_ENTRY);
    }

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (text != null) {
                switch (tagCode) {
                case TAG_AMOUNT_CODE: {
                    amount = Parse.safeParseDouble(text.trim());
                    break;
                }
                case TAG_CATEGORY_CODE: {
                    category = text.trim();
                    break;
                }
                case TAG_DATE_POSTED_CODE: {
                    datePosted = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case TAG_DESCRIPTION_CODE: {
                    description = text.trim();
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
                case TAG_PCT_KEY_CODE: {
                    pctKey = text.trim();
                    break;
                }
                case TAG_RPT_KEY_CODE: {
                    rptKey = text.trim();
                    break;
                }
                case TAG_RPT_NAME_CODE: {
                    rptName = text.trim();
                    break;
                }
                case TAG_SMART_EXPENSE_ME_KEY_CODE: {
                    smartExpenseMeKey = text.trim();
                    break;
                }
                case TAG_STATUS_CODE: {
                    status = text.trim();
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

    @Override
    public void endTag(String tag) {
        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(startTag)) {

                // Get any parsed mobile entry item and unregister parser.
                mobileEntry = mobileEntryParser.getItem();
                parser.unregisterParser(mobileEntryParser, MobileEntry.TAG_MOBILE_ENTRY);

                // Set the pct key and type.
                if (mobileEntry != null) {
                    // Set the transaction key.
                    mobileEntry.setPctKey(pctKey);
                    // Set the expense type on the mobile entry.
                    mobileEntry.setEntryType(ExpenseTypeEnum.PERSONAL_CARD);
                }

                // If the 'SmartMeKey' field has a value, then set the type to 'SMART_PERSONAL'.
                if (!TextUtils.isEmpty(smartExpenseMeKey)) {
                    type = ExpenseTypeEnum.SMART_PERSONAL;
                }
            }
        }
    }

    // Start DAO methods.

    @Override
    public ExpenseTypeEnum getType() {
        return type;
    }

    @Override
    public String getCrnCode() {
        return crnCode;
    }

    @Override
    public void setCrnCode(String crnCode) {
        this.crnCode = crnCode;
    }

    @Override
    public String getPctKey() {
        return this.pctKey;
    }

    @Override
    public void setPctKey(String pctKey) {
        this.pctKey = pctKey;
    }

    @Override
    public Calendar getDatePosted() {
        return datePosted;
    }

    @Override
    public void setDatePosted(Calendar datePosted) {
        this.datePosted = datePosted;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Double getAmount() {
        return amount;
    }

    @Override
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    @Override
    public String getStatus() {
        return status;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getCategory() {
        return category;
    }

    @Override
    public void setCategory(String category) {
        this.category = category;
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
    public String getRptKey() {
        return rptKey;
    }

    @Override
    public void setRptKey(String rptKey) {
        this.rptKey = rptKey;
    }

    @Override
    public String getRptName() {
        return rptName;
    }

    @Override
    public void setRptName(String rptName) {
        this.rptName = rptName;
    }

    @Override
    public String getSmartExpenseMeKey() {
        return smartExpenseMeKey;
    }

    @Override
    public void setSmartExpenseMeKey(String smartExpenseMeKey) {
        this.smartExpenseMeKey = smartExpenseMeKey;
    }

    /**
     * Gets the instance of <code>MobileEntry</code> associated with this transaction.
     * 
     * @return the instance of <code>MobileEntry</code> associated with this transaction.
     */
    MobileEntry getMobileEntry() {
        if (mobileEntry == null) {
            if (mobileEntryId != null) {
                Uri mobEntUri = ContentUris.withAppendedId(
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                        mobileEntryId);
                Assert.assertTrue("mobileEntryId is invalid", ContentUtils.uriExists(context, mobEntUri));
                mobileEntry = new MobileEntry(context, mobEntUri);
            }
        }
        return mobileEntry;
    }

    @Override
    public MobileEntryDAO getMobileEntryDAO() {
        return getMobileEntry();
    }

    @Override
    public void setMobileEntry(MobileEntry mobileEntry) {
        this.mobileEntry = mobileEntry;
        this.mobileEntry.type = ExpenseTypeEnum.PERSONAL_CARD;
        // Set the pct key.
        this.mobileEntry.pctKey = pctKey;
        if (personalCardId != null) {
            // Look up the pcaKey value from the referenced personal card.
            Uri persCardUri = ContentUris
                    .withAppendedId(
                            com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.CONTENT_URI,
                            personalCardId);
            if (ContentUtils.uriExists(context, persCardUri)) {
                String pcaKey = ContentUtils.getColumnStringValue(context, persCardUri,
                        com.concur.mobile.platform.expense.provider.Expense.PersonalCardColumns.PCA_KEY);
                if (!TextUtils.isEmpty(pcaKey)) {
                    this.mobileEntry.pcaKey = pcaKey;
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".setMobileEntry: no pcaKey set on personal card!");
                }
            } else {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".setMobileEntry: personal card Uri does not exist -- '" + persCardUri.toString()
                                + "'");
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".setMobileEntry: personalCardId is null.");
        }
    }

    @Override
    public MobileEntryDAO getSmartMatchedMobileEntryDAO() {
        if (matchedMobileEntry == null) {
            if (!split && type == ExpenseTypeEnum.SMART_PERSONAL && !TextUtils.isEmpty(smartExpenseMeKey)) {
                Uri mobEntUri = ContentUtils.getContentUri(context,
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                        com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.MOBILE_ENTRY_KEY,
                        smartExpenseMeKey);
                matchedMobileEntry = new MobileEntry(context, mobEntUri);
            }
        }
        return matchedMobileEntry;
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
    public boolean isSplit() {
        return split;
    }

    @Override
    public void setSplit(boolean split) {
        this.split = split;
    }

    @Override
    public boolean update(Context context, String userId) {
        return update(context, userId, true);
    }

    boolean update(Context context, String userId, boolean preserveSplitType) {
        boolean retVal = true;

        // Invoke update on any related mobile entry.
        if (mobileEntry != null) {
            if (mobileEntry.update(context, userId)) {
                if (mobileEntry.contentUri != null) {
                    mobileEntryId = ContentUris.parseId(mobileEntry.contentUri);
                }
            }
        }

        ContentResolver resolver = context.getContentResolver();

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        String typeName = null;
        if (type != null) {
            typeName = type.name();
        }
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.TYPE, typeName);

        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.PCT_KEY, pctKey);
        Long datePostedInMillis = null;
        if (datePosted != null) {
            datePostedInMillis = datePosted.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.DATE_POSTED, datePostedInMillis);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.DESCRIPTION, description);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.AMOUNT, amount);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.CRN_CODE, crnCode);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.STATUS, status);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.CATEGORY, category);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.EXP_KEY, expKey);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.EXP_NAME, expName);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.RPT_KEY, rptKey);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.RPT_NAME, rptName);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY, smartExpenseMeKey);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID, mobileEntryId);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID, personalCardId);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.TAG, tag);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.IS_SPLIT, split);
        ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.USER_ID, userId);

        // Grab the content URI if any.
        Uri pcTransUri = getContentURI(context);

        if (pcTransUri != null) {

            if (preserveSplitType) {
                // Preserve any persisted values for 'IS_SPLIT' and 'TYPE'.
                Boolean savedIsSplit = ContentUtils.getColumnBooleanValue(context, contentUri,
                        Expense.PersonalCardTransactionColumns.IS_SPLIT);
                if (savedIsSplit != null) {
                    split = savedIsSplit;
                    ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.IS_SPLIT, split);
                }
                String savedTypeName = ContentUtils.getColumnStringValue(context, contentUri,
                        Expense.PersonalCardTransactionColumns.TYPE);
                if (!TextUtils.isEmpty(savedTypeName)) {
                    type = ExpenseTypeEnum.valueOf(typeName);
                    ContentUtils.putValue(values, Expense.PersonalCardTransactionColumns.TYPE, type.name());
                }
            }

            // Perform an update.
            int rowsUpdated = resolver.update(pcTransUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + pcTransUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.PersonalCardTransactionColumns.CONTENT_URI, values);
                retVal = (contentUri != null);
            } else {
                retVal = true;
                if (rowsUpdated > 1) {
                    Log.w(Const.LOG_TAG, CLS_TAG + ".update: more than 1 row updated for Uri '" + pcTransUri.toString()
                            + "'.");
                }
            }
            retVal = (rowsUpdated == 1);
        } else {
            // Perform an insertion.
            contentUri = resolver.insert(Expense.PersonalCardTransactionColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }

        return retVal;
    }

    @Override
    public Uri getContentURI(Context context) {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(pctKey)) {
                contentUri = ContentUtils.getContentUri(context, Expense.PersonalCardTransactionColumns.CONTENT_URI,
                        Expense.PersonalCardTransactionColumns.PCT_KEY, pctKey);
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
     * Will reconcile a list of <code>PersonalCardTransaction</code> objects with entries in the expense database punting any
     * entries in the database with non-null keys that are not contained within <code>persCardTrans</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @param persCardTrans
     *            contains a list of <code>PersonalCardTransaction</code> objects.
     */
    public static void reconcile(Context context, String userId, List<PersonalCardTransaction> persCardTrans) {

        // First, build a simple map to permit quick look-up based on CCT key.
        Map<String, PersonalCardTransaction> pctKeyMap = new HashMap<String, PersonalCardTransaction>(
                persCardTrans.size());
        for (PersonalCardTransaction pctTrans : persCardTrans) {
            pctKeyMap.put(pctTrans.pctKey, pctTrans);
        }

        // Second, read in content id and PCT_KEY.
        List<Long> idsToBePunted = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] pctColumns = { Expense.PersonalCardTransactionColumns._ID,
                    Expense.PersonalCardTransactionColumns.PCT_KEY };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.PersonalCardTransactionColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.PersonalCardTransactionColumns.CONTENT_URI, pctColumns, where, whereArgs,
                    Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Long contentId = CursorUtil.getLongValue(cursor, Expense.PersonalCardTransactionColumns._ID);
                        String pctKey = CursorUtil.getStringValue(cursor,
                                Expense.PersonalCardTransactionColumns.PCT_KEY);
                        if (!TextUtils.isEmpty(pctKey) && !pctKeyMap.containsKey(pctKey)) {
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
            Uri contUri = ContentUris.withAppendedId(Expense.PersonalCardTransactionColumns.CONTENT_URI, contId);
            int rowsAffected = resolver.delete(contUri, null, null);
            if (rowsAffected != 1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".reconcile: 0 rows affected for deletion of '" + contUri.toString()
                        + "'.");
            }
        }

    }

}
