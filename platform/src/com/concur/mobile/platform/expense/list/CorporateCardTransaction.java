package com.concur.mobile.platform.expense.list;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
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
import com.concur.mobile.platform.expense.list.dao.CorporateCardTransactionDAO;
import com.concur.mobile.platform.expense.list.dao.MobileEntryDAO;
import com.concur.mobile.platform.expense.provider.Expense;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.CursorUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing corporate card charges.
 * 
 * @author andrewk
 */
public class CorporateCardTransaction extends BaseParser implements CorporateCardTransactionDAO {

    private static final String CLS_TAG = "CorporateCardTransaction";

    public static final String TAG_CORPORATE_CARD_TRANSACTION = "CorporateCardTransaction";

    public static String[] fullColumnList = { Expense.CorporateCardTransactionColumns._ID,
            Expense.CorporateCardTransactionColumns.TYPE, Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE,
            Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME, Expense.CorporateCardTransactionColumns.CCT_KEY,
            Expense.CorporateCardTransactionColumns.CCT_TYPE, Expense.CorporateCardTransactionColumns.DESCRIPTION,
            Expense.CorporateCardTransactionColumns.HAS_RICH_DATA,
            Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS,
            Expense.CorporateCardTransactionColumns.EXPENSE_KEY, Expense.CorporateCardTransactionColumns.EXPENSE_NAME,
            Expense.CorporateCardTransactionColumns.MERCHANT_CITY,
            Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE,
            Expense.CorporateCardTransactionColumns.MERCHANT_NAME,
            Expense.CorporateCardTransactionColumns.MERCHANT_STATE,
            Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY,
            Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID,
            Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT,
            Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE,
            Expense.CorporateCardTransactionColumns.TRANSACTION_DATE, Expense.CorporateCardTransactionColumns.TAG,
            Expense.CorporateCardTransactionColumns.IS_SPLIT };

    // tags.
    private static final String TAG_CARD_TYPE_CODE = "CardTypeCode";
    private static final String TAG_CARD_TYPE_NAME = "CardTypeName";
    private static final String TAG_CCT_KEY = "CctKey";
    private static final String TAG_CCT_TYPE = "CctType";
    private static final String TAG_DESCRIPTION = "Description";
    private static final String TAG_HAS_RICH_DATA = "HasRichData";
    private static final String TAG_DOING_BUSINESS_AS = "DoingBusinessAs";
    private static final String TAG_EXPENSE_KEY = "ExpKey";
    private static final String TAG_EXPENSE_NAME = "ExpName";
    private static final String TAG_MERCHANT_CITY = "MerchantCity";
    private static final String TAG_MERCHANT_COUNTRY_CODE = "MerchantCtryCode";
    private static final String TAG_MERCHANT_NAME = "MerchantName";
    private static final String TAG_MERCHANT_STATE = "MerchantState";
    private static final String TAG_SMART_EXPENSE = "SmartExpense";
    private static final String TAG_TRANSACTION_AMOUNT = "TransactionAmount";
    private static final String TAG_TRANSACTION_CRN_CODE = "TransactionCrnCode";
    private static final String TAG_TRANSACTION_DATE = "TransactionDate";

    // tag codes.
    private static final int TAG_CARD_TYPE_CODE_CODE = 0;
    private static final int TAG_CARD_TYPE_NAME_CODE = 1;
    private static final int TAG_CCT_KEY_CODE = 2;
    private static final int TAG_CCT_TYPE_CODE = 3;
    private static final int TAG_DESCRIPTION_CODE = 4;
    private static final int TAG_HAS_RICH_DATA_CODE = 5;
    private static final int TAG_DOING_BUSINESS_AS_CODE = 6;
    private static final int TAG_EXPENSE_KEY_CODE = 7;
    private static final int TAG_EXPENSE_NAME_CODE = 8;
    private static final int TAG_MERCHANT_CITY_CODE = 9;
    private static final int TAG_MERCHANT_COUNTRY_CODE_CODE = 10;
    private static final int TAG_MERCHANT_NAME_CODE = 11;
    private static final int TAG_MERCHANT_STATE_CODE = 12;
    private static final int TAG_SMART_EXPENSE_CODE = 13;
    private static final int TAG_TRANSACTION_AMOUNT_CODE = 14;
    private static final int TAG_TRANSACTION_CRN_CODE_CODE = 15;
    private static final int TAG_TRANSACTION_DATE_CODE = 16;

    // Contains the map from tags to codes.
    private static final Map<String, Integer> tagMap;

    static {
        // Construct the map from text tags to tag codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_CARD_TYPE_CODE, TAG_CARD_TYPE_CODE_CODE);
        tagMap.put(TAG_CARD_TYPE_NAME, TAG_CARD_TYPE_NAME_CODE);
        tagMap.put(TAG_CCT_KEY, TAG_CCT_KEY_CODE);
        tagMap.put(TAG_CCT_TYPE, TAG_CCT_TYPE_CODE);
        tagMap.put(TAG_DESCRIPTION, TAG_DESCRIPTION_CODE);
        tagMap.put(TAG_HAS_RICH_DATA, TAG_HAS_RICH_DATA_CODE);
        tagMap.put(TAG_DOING_BUSINESS_AS, TAG_DOING_BUSINESS_AS_CODE);
        tagMap.put(TAG_EXPENSE_KEY, TAG_EXPENSE_KEY_CODE);
        tagMap.put(TAG_EXPENSE_NAME, TAG_EXPENSE_NAME_CODE);
        tagMap.put(TAG_MERCHANT_CITY, TAG_MERCHANT_CITY_CODE);
        tagMap.put(TAG_MERCHANT_COUNTRY_CODE, TAG_MERCHANT_COUNTRY_CODE_CODE);
        tagMap.put(TAG_MERCHANT_NAME, TAG_MERCHANT_NAME_CODE);
        tagMap.put(TAG_MERCHANT_STATE, TAG_MERCHANT_STATE_CODE);
        tagMap.put(TAG_SMART_EXPENSE, TAG_SMART_EXPENSE_CODE);
        tagMap.put(TAG_TRANSACTION_AMOUNT, TAG_TRANSACTION_AMOUNT_CODE);
        tagMap.put(TAG_TRANSACTION_CRN_CODE, TAG_TRANSACTION_CRN_CODE_CODE);
        tagMap.put(TAG_TRANSACTION_DATE, TAG_TRANSACTION_DATE_CODE);
    }

    /**
     * Contains the expense type.
     */
    ExpenseTypeEnum type = ExpenseTypeEnum.CORPORATE_CARD;

    /**
     * Contains the card type code.
     */
    String cardTypeCode;

    /**
     * Contains the card type name.
     */
    String cardTypeName;

    /**
     * Contains the corporate card transaction key.
     */
    String cctKey;

    /**
     * Contains the corporate card transaction type.
     */
    String cctType;

    /**
     * Contains the description.
     */
    String description;

    /**
     * Contains whether the corporate card transaction has rich data.
     */
    Boolean hasRichData;

    /**
     * Contains the "doing business as".
     */
    String doingBusinessAs;

    /**
     * Contains the expense type key.
     */
    String expenseKey;

    /**
     * Contains the expense type name.
     */
    String expenseName;

    /**
     * Contains the merchant city.
     */
    String merchantCity;

    /**
     * Contains the merchant country code.
     */
    String merchantCountryCode;

    /**
     * Contains the merchant name.
     */
    String merchantName;

    /**
     * Contains the merchant state.
     */
    String merchantState;

    /**
     * Contains the matched smart expense mobile entry key.
     */
    String smartExpenseMeKey;

    /**
     * Contains the content Id of the matched smart expense mobile entry.
     */
    Long mobileEntryId;

    /**
     * Contains the transaction amount.
     */
    Double transactionAmount;

    /**
     * Contains the transaction currency code.
     */
    String transactionCrnCode;

    /**
     * Contains the transaction date.
     */
    Calendar transactionDate;

    /**
     * Contains the tag.
     */
    String tag;

    /**
     * Contains whether or not this transaction has been split.
     */
    boolean split;

    /**
     * Contains the content Uri for this transaction.
     */
    Uri contentUri;

    /**
     * Contains a reference to a mobile entry associated with the corporate card transaction.
     */
    MobileEntry mobileEntry;

    /**
     * Contains a reference to a mobile entry that was matched with this corporate card transaction.
     */
    MobileEntry matchedMobileEntry;

    private ItemParser<MobileEntry> mobileEntryParser;

    private String startTag;

    private Context context;

    /**
     * Contains a reference to the common parser.
     */
    private CommonParser parser;

    /**
     * Constructs an instance of <code>CorporateCardTransaction</code> given a context and an Uri.
     * 
     * @param context
     *            contains an application context.
     * @param contentUri
     *            contains the content uri.
     */
    public CorporateCardTransaction(Context context, Uri contentUri) {
        this.context = context;
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(contentUri, fullColumnList, null, null,
                    Expense.CorporateCardTransactionColumns.DEFAULT_SORT_ORDER);
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
     * Constructs an instance of <code>CorporateCardTransaction</code> based on reading values from a <code>Cursor</code> object.
     * 
     * @param cursor
     */
    public CorporateCardTransaction(Context context, Cursor cursor) {
        this.context = context;
        init(cursor);
    }

    /**
     * Will initialize this <code>CorporateCardTransaction</code> object given information in <code>cursor</code>.
     * 
     * @param cursor
     *            contains the cursor.
     */
    private void init(Cursor cursor) {
        String typeName = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.TYPE);
        if (!TextUtils.isEmpty(typeName)) {
            type = ExpenseTypeEnum.valueOf(typeName);
        }
        cardTypeCode = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE);
        cardTypeName = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME);
        cctKey = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.CCT_KEY);
        cctType = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.CCT_TYPE);
        description = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.DESCRIPTION);
        hasRichData = CursorUtil.getBooleanValue(cursor, Expense.CorporateCardTransactionColumns.HAS_RICH_DATA);
        doingBusinessAs = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS);
        expenseKey = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.EXPENSE_KEY);
        expenseName = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.EXPENSE_NAME);
        merchantCity = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.MERCHANT_CITY);
        merchantCountryCode = CursorUtil.getStringValue(cursor,
                Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE);
        merchantName = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.MERCHANT_NAME);
        merchantState = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.MERCHANT_STATE);
        smartExpenseMeKey = CursorUtil.getStringValue(cursor,
                Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY);
        mobileEntryId = CursorUtil.getLongValue(cursor, Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID);
        transactionAmount = CursorUtil.getDoubleValue(cursor,
                Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT);
        transactionCrnCode = CursorUtil.getStringValue(cursor,
                Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE);
        Long transDateMillis = CursorUtil
                .getLongValue(cursor, Expense.CorporateCardTransactionColumns.TRANSACTION_DATE);
        if (transDateMillis != null) {
            transactionDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            transactionDate.setTimeInMillis(transDateMillis);
            transactionDate.set(Calendar.MILLISECOND, 0);
        }
        tag = CursorUtil.getStringValue(cursor, Expense.CorporateCardTransactionColumns.TAG);
        split = CursorUtil.getBooleanValue(cursor, Expense.CorporateCardTransactionColumns.IS_SPLIT);

        // Set the content id.
        Long contentId = CursorUtil.getLongValue(cursor, Expense.CorporateCardTransactionColumns._ID);
        if (contentId != null) {
            contentUri = ContentUris.withAppendedId(Expense.CorporateCardTransactionColumns.CONTENT_URI, contentId);
        }
    }

    /**
     * Constructs an instance of <code>CorporateCardTransaction</code> with a parser and a start tag.
     * 
     * @param parser
     *            contains a reference to the common parser.
     * @param startTag
     *            contains the start tag.
     */
    public CorporateCardTransaction(CommonParser parser, String startTag) {

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
                case TAG_CARD_TYPE_CODE_CODE: {
                    cardTypeCode = text.trim();
                    break;
                }
                case TAG_CARD_TYPE_NAME_CODE: {
                    cardTypeName = text.trim();
                    break;
                }
                case TAG_CCT_KEY_CODE: {
                    cctKey = text.trim();
                    break;
                }
                case TAG_CCT_TYPE_CODE: {
                    cctType = text.trim();
                    break;
                }
                case TAG_DESCRIPTION_CODE: {
                    description = text.trim();
                    break;
                }
                case TAG_HAS_RICH_DATA_CODE: {
                    hasRichData = Parse.safeParseBoolean(text.trim());
                    break;
                }
                case TAG_DOING_BUSINESS_AS_CODE: {
                    doingBusinessAs = text.trim();
                    break;
                }
                case TAG_EXPENSE_KEY_CODE: {
                    expenseKey = text.trim();
                    break;
                }
                case TAG_EXPENSE_NAME_CODE: {
                    expenseName = text.trim();
                    break;
                }
                case TAG_MERCHANT_CITY_CODE: {
                    merchantCity = text.trim();
                    break;
                }
                case TAG_MERCHANT_COUNTRY_CODE_CODE: {
                    merchantCountryCode = text.trim();
                    break;
                }
                case TAG_MERCHANT_NAME_CODE: {
                    merchantName = text.trim();
                    break;
                }
                case TAG_MERCHANT_STATE_CODE: {
                    merchantState = text.trim();
                    break;
                }
                case TAG_SMART_EXPENSE_CODE: {
                    smartExpenseMeKey = text.trim();
                    break;
                }
                case TAG_TRANSACTION_AMOUNT_CODE: {
                    transactionAmount = Parse.safeParseDouble(text.trim());
                    break;
                }
                case TAG_TRANSACTION_CRN_CODE_CODE: {
                    transactionCrnCode = text.trim();
                    break;
                }
                case TAG_TRANSACTION_DATE_CODE: {
                    transactionDate = Parse.parseXMLTimestamp(text.trim());
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

                // Get any parsed mobile entry item and unregister the parser.
                mobileEntry = mobileEntryParser.getItem();
                parser.unregisterParser(mobileEntryParser, MobileEntry.TAG_MOBILE_ENTRY);

                // Set the cct key and type.
                if (mobileEntry != null) {
                    // Set the transaction key.
                    mobileEntry.setCctKey(cctKey);
                    // Set the expense type on the mobile entry.
                    mobileEntry
.setEntryType(ExpenseTypeEnum.CORPORATE_CARD);
                }

                // If the 'SmartMeKey' field has a value, then set the type to 'SMART_CORPORATE'.
                if (!TextUtils.isEmpty(smartExpenseMeKey)) {
                    type = ExpenseTypeEnum.SMART_CORPORATE;
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
    public String getCardTypeCode() {
        return cardTypeCode;
    }

    @Override
    public void setCardTypeCode(String cardTypeCode) {
        this.cardTypeCode = cardTypeCode;
    }

    @Override
    public String getCardTypeName() {
        return cardTypeName;
    }

    @Override
    public void setCardTypeName(String cardTypeName) {
        this.cardTypeName = cardTypeName;
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
    public String getCctType() {
        return cctType;
    }

    @Override
    public void setCctType(String cctType) {
        this.cctType = cctType;
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
    public Boolean getHasRichData() {
        return hasRichData;
    }

    @Override
    public void setHasRichData(Boolean hasRichData) {
        this.hasRichData = hasRichData;
    }

    @Override
    public String getDoingBusinessAs() {
        return doingBusinessAs;
    }

    @Override
    public void setDoingBusinessAs(String doingBusinessAs) {
        this.doingBusinessAs = doingBusinessAs;
    }

    @Override
    public String getExpenseKey() {
        return expenseKey;
    }

    @Override
    public void setExpenseKey(String expenseKey) {
        this.expenseKey = expenseKey;
    }

    @Override
    public String getExpenseName() {
        return expenseName;
    }

    @Override
    public void setExpenseName(String expenseName) {
        this.expenseName = expenseName;
    }

    @Override
    public String getMerchantCity() {
        return merchantCity;
    }

    @Override
    public void setMerchantCity(String merchantCity) {
        this.merchantCity = merchantCity;
    }

    @Override
    public String getMerchantCountryCode() {
        return merchantCountryCode;
    }

    @Override
    public void setMerchantCountryCode(String merchantCountryCode) {
        this.merchantCountryCode = merchantCountryCode;
    }

    @Override
    public String getMerchantName() {
        return merchantName;
    }

    @Override
    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
    }

    @Override
    public String getMerchantState() {
        return merchantState;
    }

    @Override
    public void setMerchantState(String merchantState) {
        this.merchantState = merchantState;
    }

    @Override
    public String getSmartExpenseMeKey() {
        return smartExpenseMeKey;
    }

    @Override
    public void setSmartExpenseMeKey(String smartExpenseMeKey) {
        this.smartExpenseMeKey = smartExpenseMeKey;
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
    public String getTransactionCrnCode() {
        return transactionCrnCode;
    }

    @Override
    public void setTransactionCrnCode(String transactionCrnCode) {
        this.transactionCrnCode = transactionCrnCode;
    }

    @Override
    public Calendar getTransactionDate() {
        return transactionDate;
    }

    @Override
    public void setTransactionDate(Calendar transactionDate) {
        this.transactionDate = transactionDate;
    }

    /**
     * Gets the instance of <code>MobileEntry</code> associated with this transaction.
     * 
     * @return the instance of <code>MobileEntry</code> associated with this transaction.
     */
    MobileEntry getMobileEntry() {
        if (mobileEntry == null) {
            try {
                if (mobileEntryId != null) {
                    Uri mobEntUri = ContentUris.withAppendedId(
                            com.concur.mobile.platform.expense.provider.Expense.MobileEntryColumns.CONTENT_URI,
                            mobileEntryId);
                    Assert.assertTrue("mobileEntryId is invalid", ContentUtils.uriExists(context, mobEntUri));
                    mobileEntry = new MobileEntry(context, mobEntUri);
                }
            } catch (AssertionFailedError afe) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getMobileEntry: " + afe.getMessage());
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
        this.mobileEntry.cctKey = cctKey;
        this.mobileEntry.type = ExpenseTypeEnum.CORPORATE_CARD;
    }

    @Override
    public MobileEntryDAO getSmartMatchedMobileEntryDAO() {
        if (matchedMobileEntry == null) {
            if (!split && type == ExpenseTypeEnum.SMART_CORPORATE && !TextUtils.isEmpty(smartExpenseMeKey)) {
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

        ContentResolver resolver = context.getContentResolver();

        // Invoke update on any related mobile entry.
        if (mobileEntry != null) {
            if (mobileEntry.update(context, userId)) {
                if (mobileEntry.contentUri != null) {
                    mobileEntryId = ContentUris.parseId(mobileEntry.contentUri);
                }
            }
        }

        // Build the content values object for insert/update.
        ContentValues values = new ContentValues();

        String typeName = null;
        if (type != null) {
            typeName = type.name();
        }
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TYPE, typeName);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE, cardTypeCode);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME, cardTypeName);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.CCT_KEY, cctKey);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.CCT_TYPE, cctType);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.DESCRIPTION, description);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.HAS_RICH_DATA, hasRichData);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS, doingBusinessAs);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.EXPENSE_KEY, expenseKey);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.EXPENSE_NAME, expenseName);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.MERCHANT_CITY, merchantCity);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE,
                merchantCountryCode);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.MERCHANT_NAME, merchantName);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.MERCHANT_STATE, merchantState);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY, smartExpenseMeKey);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID, mobileEntryId);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT, transactionAmount);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE, transactionCrnCode);
        Long transDateInMillis = null;
        if (transactionDate != null) {
            transDateInMillis = transactionDate.getTimeInMillis();
        }
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TRANSACTION_DATE, transDateInMillis);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TAG, tag);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.IS_SPLIT, split);
        ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.USER_ID, userId);

        // Ensure 'contentUri' gets set.
        getContentURI(context);

        if (contentUri != null) {

            if (preserveSplitType) {
                // Preserve any persisted values for 'IS_SPLIT' and 'TYPE'.
                Boolean savedIsSplit = ContentUtils.getColumnBooleanValue(context, contentUri,
                        Expense.CorporateCardTransactionColumns.IS_SPLIT);
                if (savedIsSplit != null) {
                    split = savedIsSplit;
                    ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.IS_SPLIT, split);
                }
                String savedTypeName = ContentUtils.getColumnStringValue(context, contentUri,
                        Expense.CorporateCardTransactionColumns.TYPE);
                if (!TextUtils.isEmpty(savedTypeName)) {
                    type = ExpenseTypeEnum.valueOf(typeName);
                    ContentUtils.putValue(values, Expense.CorporateCardTransactionColumns.TYPE, type.name());
                }
            }

            // Perform an update.
            int rowsUpdated = resolver.update(contentUri, values, null, null);
            if (rowsUpdated == 0) {
                Log.w(Const.LOG_TAG, CLS_TAG + ".update: 0 rows updated for Uri '" + contentUri.toString() + "'.");
                // Perform an insertion.
                contentUri = resolver.insert(Expense.CorporateCardTransactionColumns.CONTENT_URI, values);
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
            contentUri = resolver.insert(Expense.CorporateCardTransactionColumns.CONTENT_URI, values);
            retVal = (contentUri != null);
        }

        return retVal;
    }

    @Override
    public Uri getContentURI(Context context) {
        if (contentUri == null) {
            if (!TextUtils.isEmpty(cctKey)) {
                contentUri = ContentUtils.getContentUri(context, Expense.CorporateCardTransactionColumns.CONTENT_URI,
                        Expense.CorporateCardTransactionColumns.CCT_KEY, cctKey);
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
     * Will reconcile a list of <code>CorporateCardTransaction</code> objects with entries in the expense database punting any
     * entries in the database with non-null transaction keys that are not contained within <code>corpCardTrans</code>.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param userId
     *            contains the user id.
     * @param corpCardTrans
     *            contains a list of <code>CorporateCardTransaction</code> objects.
     */
    public static void reconcile(Context context, String userId, List<CorporateCardTransaction> corpCardTrans) {

        // First, build a simple map to permit quick look-up based on CCT key.
        Map<String, CorporateCardTransaction> cctKeyMap = new HashMap<String, CorporateCardTransaction>(
                corpCardTrans.size());
        for (CorporateCardTransaction cctTrans : corpCardTrans) {
            cctKeyMap.put(cctTrans.cctKey, cctTrans);
        }

        // Second, read in content id and CCT_KEY.
        List<Long> idsToBePunted = new ArrayList<Long>();
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            String[] cctColumns = { Expense.CorporateCardTransactionColumns._ID,
                    Expense.CorporateCardTransactionColumns.CCT_KEY };
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(Expense.CorporateCardTransactionColumns.USER_ID);
            strBldr.append(" = ?");
            String where = strBldr.toString();
            String[] whereArgs = { userId };

            cursor = resolver.query(Expense.CorporateCardTransactionColumns.CONTENT_URI, cctColumns, where, whereArgs,
                    Expense.CorporateCardTransactionColumns.DEFAULT_SORT_ORDER);
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Long contentId = CursorUtil.getLongValue(cursor, Expense.CorporateCardTransactionColumns._ID);
                        String cctKey = CursorUtil.getStringValue(cursor,
                                Expense.CorporateCardTransactionColumns.CCT_KEY);
                        if (!TextUtils.isEmpty(cctKey) && !cctKeyMap.containsKey(cctKey)) {
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
            Uri contUri = ContentUris.withAppendedId(Expense.CorporateCardTransactionColumns.CONTENT_URI, contId);
            int rowsAffected = resolver.delete(contUri, null, null);
            if (rowsAffected != 1) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".reconcile: 0 rows affected for deletion of '" + contUri.toString()
                        + "'.");
            }
        }
    }

}
