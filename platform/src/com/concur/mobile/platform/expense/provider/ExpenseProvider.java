/**
 * 
 */
package com.concur.mobile.platform.expense.provider;

import android.content.Context;
import android.content.UriMatcher;
import android.util.SparseArray;

import com.concur.mobile.platform.provider.EncryptedSQLiteOpenHelper;
import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.provider.PlatformSQLiteOpenHelper;
import com.concur.mobile.platform.provider.UriMatcherInfo;

import java.util.HashMap;

/**
 * An extension of <code>PlatformContentProvider</code> providing expense content.
 * 
 * @author andrewk
 */
public class ExpenseProvider extends PlatformContentProvider {

    private static final String CLS_TAG = "ExpenseProvider";

    // Contains a static reference to the last instantiated <code>ExpenseProvider</code>.
    private static ExpenseProvider provider;

    // expense type response.
    private static final int EXPENSE_TYPES = 1;
    private static final int EXPENSE_TYPE_ID = 2;

    // expense list response.
    private static final int EXPENSES = 3;
    private static final int EXPENSE_ID = 4;
    private static final int CORPORATE_CARD_TRANSACTIONS = 5;
    private static final int CORPORATE_CARD_TRANSACTION_ID = 6;
    private static final int PERSONAL_CARDS = 7;
    private static final int PERSONAL_CARD_ID = 8;
    private static final int PERSONAL_CARD_TRANSACTIONS = 9;
    private static final int PERSONAL_CARD_TRANSACTION_ID = 10;
    private static final int MOBILE_ENTRIES = 11;
    private static final int MOBILE_ENTRY_ID = 12;
    private static final int RECEIPT_CAPTURES = 13;
    private static final int RECEIPT_CAPTURE_ID = 14;
    private static final int RECEIPTS = 15;
    private static final int RECEIPT_ID = 16;
    private static final int SMART_EXPENSES = 17;
    private static final int SMART_EXPENSE_ID = 18;
    private static final int EXPENSEIT_RECEIPTS = 19;
    private static final int EXPENSEIT_RECEIPT_ID = 20;

    // Contains the expense type projection map.
    private static HashMap<String, String> expenseTypeProjMap;

    // Contains the expense projection map.
    private static HashMap<String, String> expenseProjMap;

    // Contains the corporate card transaction projection map.
    private static HashMap<String, String> corporateCardTransactionProjMap;

    // Contains the personal card projection map.
    private static HashMap<String, String> personalCardProjMap;

    // Contains the personal card transaction projection map.
    private static HashMap<String, String> personalCardTransactionProjMap;

    // Contains the mobile entry projection map.
    private static HashMap<String, String> mobileEntryProjMap;

    // Contains the receipt capture projection map.
    private static HashMap<String, String> receiptCaptureProjMap;

    // Contains the receipt projection map.
    private static HashMap<String, String> receiptProjMap;

    // Contains the smart expense projection map.
    private static HashMap<String, String> smartExpenseProjMap;

    //Contains the expenseIt receipt projections map.
    private static HashMap<String, String> expenseItReceiptProjMap;

    @Override
    public boolean onCreate() {
        boolean retVal = super.onCreate();

        // Set the static reference.
        provider = this;

        return retVal;
    }

    /**
     * Gets the current instance of <code>ExpenseProvider</code>.
     * 
     * @return returns the current instance of <code>ExpenseProvider</code>.
     */
    public static ExpenseProvider getExpenseProvider() {
        return provider;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformContentProvider#getDatabaseName()
     */
    @Override
    protected String getDatabaseName() {
        return ExpenseDBSchema.DATABASE_NAME;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformContentProvider#initUriMatcher()
     */
    @Override
    protected UriMatcher initUriMatcher() {

        UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // Add expense types.
        matcher.addURI(Expense.AUTHORITY, "expense_types", EXPENSE_TYPES);
        matcher.addURI(Expense.AUTHORITY, "expense_types/#", EXPENSE_TYPE_ID);

        // Add expenses.
        matcher.addURI(Expense.AUTHORITY, "expenses", EXPENSES);
        matcher.addURI(Expense.AUTHORITY, "expenses/#", EXPENSE_ID);

        // Add corporate card transactions.
        matcher.addURI(Expense.AUTHORITY, "corporate_card_transactions", CORPORATE_CARD_TRANSACTIONS);
        matcher.addURI(Expense.AUTHORITY, "corporate_card_transactions/#", CORPORATE_CARD_TRANSACTION_ID);

        // Add personal cards.
        matcher.addURI(Expense.AUTHORITY, "personal_cards", PERSONAL_CARDS);
        matcher.addURI(Expense.AUTHORITY, "personal_cards/#", PERSONAL_CARD_ID);

        // Add personal card transactions.
        matcher.addURI(Expense.AUTHORITY, "personal_card_transactions", PERSONAL_CARD_TRANSACTIONS);
        matcher.addURI(Expense.AUTHORITY, "personal_card_transactions/#", PERSONAL_CARD_TRANSACTION_ID);

        // Add mobile entries.
        matcher.addURI(Expense.AUTHORITY, "mobile_entries", MOBILE_ENTRIES);
        matcher.addURI(Expense.AUTHORITY, "mobile_entries/#", MOBILE_ENTRY_ID);

        // Add receipt captures.
        matcher.addURI(Expense.AUTHORITY, "receipt_captures", RECEIPT_CAPTURES);
        matcher.addURI(Expense.AUTHORITY, "receipt_captures/#", RECEIPT_CAPTURE_ID);

        // Add receipts.
        matcher.addURI(Expense.AUTHORITY, "receipts", RECEIPTS);
        matcher.addURI(Expense.AUTHORITY, "receipts/#", RECEIPT_ID);

        // Add smart expense.
        matcher.addURI(Expense.AUTHORITY, "smart_expenses", SMART_EXPENSES);
        matcher.addURI(Expense.AUTHORITY, "smart_expenses/#", SMART_EXPENSE_ID);

        // Add expenseIt receipt.
        matcher.addURI(Expense.AUTHORITY, "expenseit_receipts", EXPENSEIT_RECEIPTS);
        matcher.addURI(Expense.AUTHORITY, "expenseit_receipts/#", EXPENSEIT_RECEIPT_ID);

        return matcher;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformContentProvider#initProjectionMaps()
     */
    @Override
    protected void initProjectionMaps() {

        // Expense type projection map.
        expenseTypeProjMap = new HashMap<String, String>();
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns._ID, Expense.ExpenseTypeColumns._ID);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns._COUNT, Expense.ExpenseTypeColumns._COUNT);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.TYPE_NAME, Expense.ExpenseTypeColumns.TYPE_NAME);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.TYPE_CODE, Expense.ExpenseTypeColumns.TYPE_CODE);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.TYPE_KEY, Expense.ExpenseTypeColumns.TYPE_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.FORM_KEY, Expense.ExpenseTypeColumns.FORM_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.HAS_POST_AMT_CALC,
                Expense.ExpenseTypeColumns.HAS_POST_AMT_CALC);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS,
                Expense.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.HAS_TAX_FORM, Expense.ExpenseTypeColumns.HAS_TAX_FORM);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ITEMIZATION_FORM_KEY,
                Expense.ExpenseTypeColumns.ITEMIZATION_FORM_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ITEMIZATION_STYLE,
                Expense.ExpenseTypeColumns.ITEMIZATION_STYLE);
        expenseTypeProjMap
                .put(Expense.ExpenseTypeColumns.ITEMIZATION_TYPE, Expense.ExpenseTypeColumns.ITEMIZATION_TYPE);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.PARENT_EXP_KEY, Expense.ExpenseTypeColumns.PARENT_EXP_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.PARENT_EXP_NAME, Expense.ExpenseTypeColumns.PARENT_EXP_NAME);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.SUPPORTS_ATTENDEES,
                Expense.ExpenseTypeColumns.SUPPORTS_ATTENDEES);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.VENDOR_LIST_KEY, Expense.ExpenseTypeColumns.VENDOR_LIST_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT,
                Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT,
                Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.ALLOW_NO_SHOWS, Expense.ExpenseTypeColumns.ALLOW_NO_SHOWS);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM,
                Expense.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS,
                Expense.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT,
                Expense.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS,
                Expense.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.POLICY_KEY, Expense.ExpenseTypeColumns.POLICY_KEY);
        expenseTypeProjMap.put(Expense.ExpenseTypeColumns.USER_ID, Expense.ExpenseTypeColumns.USER_ID);
        // Add more projection maps below.

        // Expense projection map.
        expenseProjMap = new HashMap<String, String>();
        expenseProjMap.put(Expense.ExpenseColumns._ID, Expense.ExpenseColumns._ID);
        expenseProjMap.put(Expense.ExpenseColumns._COUNT, Expense.ExpenseColumns._COUNT);
        expenseProjMap.put(Expense.ExpenseColumns.TYPE, Expense.ExpenseColumns.TYPE);
        expenseProjMap.put(Expense.ExpenseColumns.TRANSACTION_AMOUNT, Expense.ExpenseColumns.TRANSACTION_AMOUNT);
        expenseProjMap.put(Expense.ExpenseColumns.TRANSACTION_CRN_CODE, Expense.ExpenseColumns.TRANSACTION_CRN_CODE);
        expenseProjMap.put(Expense.ExpenseColumns.TRANSACTION_DATE, Expense.ExpenseColumns.TRANSACTION_DATE);
        expenseProjMap.put(Expense.ExpenseColumns.VENDOR_NAME, Expense.ExpenseColumns.VENDOR_NAME);
        expenseProjMap.put(Expense.ExpenseColumns.EXP_ID, Expense.ExpenseColumns.EXP_ID);
        expenseProjMap.put(Expense.ExpenseColumns.USER_ID, Expense.ExpenseColumns.USER_ID);

        // Corporate card transaction projection map.
        corporateCardTransactionProjMap = new HashMap<String, String>();
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns._ID,
                Expense.CorporateCardTransactionColumns._ID);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns._COUNT,
                Expense.CorporateCardTransactionColumns._COUNT);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.TYPE,
                Expense.CorporateCardTransactionColumns.TYPE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE,
                Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME,
                Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.CCT_KEY,
                Expense.CorporateCardTransactionColumns.CCT_KEY);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.CCT_TYPE,
                Expense.CorporateCardTransactionColumns.CCT_TYPE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.HAS_RICH_DATA,
                Expense.CorporateCardTransactionColumns.HAS_RICH_DATA);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.DESCRIPTION,
                Expense.CorporateCardTransactionColumns.DESCRIPTION);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS,
                Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.EXPENSE_KEY,
                Expense.CorporateCardTransactionColumns.EXPENSE_KEY);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.EXPENSE_NAME,
                Expense.CorporateCardTransactionColumns.EXPENSE_NAME);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MERCHANT_CITY,
                Expense.CorporateCardTransactionColumns.MERCHANT_CITY);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE,
                Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MERCHANT_NAME,
                Expense.CorporateCardTransactionColumns.MERCHANT_NAME);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MERCHANT_STATE,
                Expense.CorporateCardTransactionColumns.MERCHANT_STATE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY,
                Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID,
                Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT,
                Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.MERCHANT_NAME,
                Expense.CorporateCardTransactionColumns.MERCHANT_NAME);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE,
                Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.TRANSACTION_DATE,
                Expense.CorporateCardTransactionColumns.TRANSACTION_DATE);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.TAG,
                Expense.CorporateCardTransactionColumns.TAG);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.IS_SPLIT,
                Expense.CorporateCardTransactionColumns.IS_SPLIT);
        corporateCardTransactionProjMap.put(Expense.CorporateCardTransactionColumns.USER_ID,
                Expense.CorporateCardTransactionColumns.USER_ID);

        // Personal card projection map.
        personalCardProjMap = new HashMap<String, String>();
        personalCardProjMap.put(Expense.PersonalCardColumns._ID, Expense.PersonalCardColumns._ID);
        personalCardProjMap.put(Expense.PersonalCardColumns._COUNT, Expense.PersonalCardColumns._COUNT);
        personalCardProjMap.put(Expense.PersonalCardColumns.PCA_KEY, Expense.PersonalCardColumns.PCA_KEY);
        personalCardProjMap.put(Expense.PersonalCardColumns.CARD_NAME, Expense.PersonalCardColumns.CARD_NAME);
        personalCardProjMap.put(Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR,
                Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR);
        personalCardProjMap.put(Expense.PersonalCardColumns.CRN_CODE, Expense.PersonalCardColumns.CRN_CODE);
        personalCardProjMap.put(Expense.PersonalCardColumns.TAG, Expense.PersonalCardColumns.TAG);
        personalCardProjMap.put(Expense.PersonalCardColumns.USER_ID, Expense.PersonalCardColumns.USER_ID);

        // Personal card transaction projection map.
        personalCardTransactionProjMap = new HashMap<String, String>();
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns._ID,
                Expense.PersonalCardTransactionColumns._ID);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns._COUNT,
                Expense.PersonalCardTransactionColumns._COUNT);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.TYPE,
                Expense.PersonalCardTransactionColumns.TYPE);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.PCT_KEY,
                Expense.PersonalCardTransactionColumns.PCT_KEY);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.DATE_POSTED,
                Expense.PersonalCardTransactionColumns.DATE_POSTED);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.DESCRIPTION,
                Expense.PersonalCardTransactionColumns.DESCRIPTION);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.AMOUNT,
                Expense.PersonalCardTransactionColumns.AMOUNT);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.CRN_CODE,
                Expense.PersonalCardTransactionColumns.CRN_CODE);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.STATUS,
                Expense.PersonalCardTransactionColumns.STATUS);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.CATEGORY,
                Expense.PersonalCardTransactionColumns.CATEGORY);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.EXP_KEY,
                Expense.PersonalCardTransactionColumns.EXP_KEY);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.EXP_NAME,
                Expense.PersonalCardTransactionColumns.EXP_NAME);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.RPT_KEY,
                Expense.PersonalCardTransactionColumns.RPT_KEY);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.RPT_NAME,
                Expense.PersonalCardTransactionColumns.RPT_NAME);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY,
                Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID,
                Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID,
                Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.TAG,
            Expense.PersonalCardTransactionColumns.TAG);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.IS_SPLIT,
                Expense.PersonalCardTransactionColumns.IS_SPLIT);
        personalCardTransactionProjMap.put(Expense.PersonalCardTransactionColumns.USER_ID,
                Expense.PersonalCardTransactionColumns.USER_ID);

        // Mobile entry projection map.
        mobileEntryProjMap = new HashMap<String, String>();
        mobileEntryProjMap.put(Expense.MobileEntryColumns._ID, Expense.MobileEntryColumns._ID);
        mobileEntryProjMap.put(Expense.MobileEntryColumns._COUNT, Expense.MobileEntryColumns._COUNT);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.CRN_CODE, Expense.MobileEntryColumns.CRN_CODE);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.EXP_KEY, Expense.MobileEntryColumns.EXP_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.EXP_NAME, Expense.MobileEntryColumns.EXP_NAME);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.LOCATION_NAME, Expense.MobileEntryColumns.LOCATION_NAME);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.VENDOR_NAME, Expense.MobileEntryColumns.VENDOR_NAME);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.TYPE, Expense.MobileEntryColumns.TYPE);
        mobileEntryProjMap
                .put(Expense.MobileEntryColumns.MOBILE_ENTRY_KEY, Expense.MobileEntryColumns.MOBILE_ENTRY_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.PCA_KEY, Expense.MobileEntryColumns.PCA_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.PCT_KEY, Expense.MobileEntryColumns.PCT_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.CCT_KEY, Expense.MobileEntryColumns.CCT_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.RC_KEY, Expense.MobileEntryColumns.RC_KEY);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.TRANSACTION_AMOUNT,
                Expense.MobileEntryColumns.TRANSACTION_AMOUNT);
        mobileEntryProjMap
                .put(Expense.MobileEntryColumns.TRANSACTION_DATE, Expense.MobileEntryColumns.TRANSACTION_DATE);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE,
                Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE);
        mobileEntryProjMap
                .put(Expense.MobileEntryColumns.RECEIPT_IMAGE_ID, Expense.MobileEntryColumns.RECEIPT_IMAGE_ID);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.RECEIPT_CONTENT_ID,
                Expense.MobileEntryColumns.RECEIPT_CONTENT_ID);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA,
                Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH,
                Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.COMMENT, Expense.MobileEntryColumns.COMMENT);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.TAG, Expense.MobileEntryColumns.TAG);
        mobileEntryProjMap.put(Expense.MobileEntryColumns.USER_ID, Expense.MobileEntryColumns.USER_ID);

        // Receipt capture projection map.
        receiptCaptureProjMap = new HashMap<String, String>();
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns._ID, Expense.ReceiptCaptureColumns._ID);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns._COUNT, Expense.ReceiptCaptureColumns._COUNT);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.TYPE, Expense.ReceiptCaptureColumns.TYPE);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.CRN_CODE, Expense.ReceiptCaptureColumns.CRN_CODE);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.EXP_KEY, Expense.ReceiptCaptureColumns.EXP_KEY);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.EXP_NAME, Expense.ReceiptCaptureColumns.EXP_NAME);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.VENDOR_NAME, Expense.ReceiptCaptureColumns.VENDOR_NAME);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.RC_KEY, Expense.ReceiptCaptureColumns.RC_KEY);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID,
                Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT,
                Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.TRANSACTION_DATE,
                Expense.ReceiptCaptureColumns.TRANSACTION_DATE);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID,
                Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.TAG, Expense.ReceiptCaptureColumns.TAG);
        receiptCaptureProjMap.put(Expense.ReceiptCaptureColumns.USER_ID, Expense.ReceiptCaptureColumns.USER_ID);

        // Receipt projection map.
        receiptProjMap = new HashMap<String, String>();
        receiptProjMap.put(Expense.ReceiptColumns._ID, Expense.ReceiptColumns._ID);
        receiptProjMap.put(Expense.ReceiptColumns._COUNT, Expense.ReceiptColumns._COUNT);
        receiptProjMap.put(Expense.ReceiptColumns.ETAG, Expense.ReceiptColumns.ETAG);
        receiptProjMap.put(Expense.ReceiptColumns.ID, Expense.ReceiptColumns.ID);
        receiptProjMap.put(Expense.ReceiptColumns.URI, Expense.ReceiptColumns.URI);
        receiptProjMap.put(Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE, Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE);
        receiptProjMap.put(Expense.ReceiptColumns.LOCAL_PATH, Expense.ReceiptColumns.LOCAL_PATH);
        receiptProjMap.put(Expense.ReceiptColumns.RECEIPT_DATA, Expense.ReceiptColumns.RECEIPT_DATA);
        receiptProjMap.put(Expense.ReceiptColumns.THUMBNAIL_URI, Expense.ReceiptColumns.THUMBNAIL_URI);
        receiptProjMap
                .put(Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE, Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE);
        receiptProjMap.put(Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH, Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH);
        receiptProjMap
                .put(Expense.ReceiptColumns.THUMBNAIL_RECEIPT_DATA, Expense.ReceiptColumns.THUMBNAIL_RECEIPT_DATA);
        receiptProjMap.put(Expense.ReceiptColumns.IS_ATTACHED, Expense.ReceiptColumns.IS_ATTACHED);
        receiptProjMap.put(Expense.ReceiptColumns.LAST_ACCESS_TIME, Expense.ReceiptColumns.LAST_ACCESS_TIME);
        receiptProjMap.put(Expense.ReceiptColumns.IMAGE_UPLOAD_TIME, Expense.ReceiptColumns.IMAGE_UPLOAD_TIME);
        receiptProjMap.put(Expense.ReceiptColumns.FILE_NAME, Expense.ReceiptColumns.FILE_NAME);
        receiptProjMap.put(Expense.ReceiptColumns.FILE_TYPE, Expense.ReceiptColumns.FILE_TYPE);
        receiptProjMap.put(Expense.ReceiptColumns.SYSTEM_ORIGIN, Expense.ReceiptColumns.SYSTEM_ORIGIN);
        receiptProjMap.put(Expense.ReceiptColumns.IMAGE_ORIGIN, Expense.ReceiptColumns.IMAGE_ORIGIN);
        receiptProjMap.put(Expense.ReceiptColumns.IMAGE_URL, Expense.ReceiptColumns.IMAGE_URL);
        receiptProjMap.put(Expense.ReceiptColumns.THUMB_URL, Expense.ReceiptColumns.THUMB_URL);
        receiptProjMap.put(Expense.ReceiptColumns.OCR_IMAGE_ORIGIN, Expense.ReceiptColumns.OCR_IMAGE_ORIGIN);
        receiptProjMap.put(Expense.ReceiptColumns.OCR_STAT_KEY, Expense.ReceiptColumns.OCR_STAT_KEY);
        receiptProjMap.put(Expense.ReceiptColumns.OCR_REJECT_CODE, Expense.ReceiptColumns.OCR_REJECT_CODE);
        receiptProjMap.put(Expense.ReceiptColumns.USER_ID, Expense.ReceiptColumns.USER_ID);

        // Smart expense projection map.
        smartExpenseProjMap = new HashMap<String, String>();
        smartExpenseProjMap.put(Expense.SmartExpenseColumns._ID, Expense.SmartExpenseColumns._ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE,
                Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.ESTIMATED_AMOUNT,
                Expense.SmartExpenseColumns.ESTIMATED_AMOUNT);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CCA_KEY, Expense.SmartExpenseColumns.CCA_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CCT_KEY, Expense.SmartExpenseColumns.CCT_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.EXTRACT_CCT_KEY,
                Expense.SmartExpenseColumns.EXTRACT_CCT_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.SMART_EXPENSE_ID,
                Expense.SmartExpenseColumns.SMART_EXPENSE_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.E_RECEIPT_SOURCE,
                Expense.SmartExpenseColumns.E_RECEIPT_SOURCE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRANSACTION_DATE,
                Expense.SmartExpenseColumns.TRANSACTION_DATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.HAS_RICH_DATA, Expense.SmartExpenseColumns.HAS_RICH_DATA);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.E_RECEIPT_ID, Expense.SmartExpenseColumns.E_RECEIPT_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.SEGMENT_ID, Expense.SmartExpenseColumns.SEGMENT_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.EXP_NAME, Expense.SmartExpenseColumns.EXP_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRANSACTION_GROUP,
                Expense.SmartExpenseColumns.TRANSACTION_GROUP);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.VENDOR_CODE, Expense.SmartExpenseColumns.VENDOR_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.COUNTRY, Expense.SmartExpenseColumns.COUNTRY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRIP_ID, Expense.SmartExpenseColumns.TRIP_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.VENDOR_DESCRIPTION,
                Expense.SmartExpenseColumns.VENDOR_DESCRIPTION);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.EXCHANGE_RATE, Expense.SmartExpenseColumns.EXCHANGE_RATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.POSTED_AMOUNT, Expense.SmartExpenseColumns.POSTED_AMOUNT);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRANSACTION_AMOUNT,
                Expense.SmartExpenseColumns.TRANSACTION_AMOUNT);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.INSURANCE_CHARGE,
                Expense.SmartExpenseColumns.INSURANCE_CHARGE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.GPS_CHARGE, Expense.SmartExpenseColumns.GPS_CHARGE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CARD_LAST_SEGMENT,
                Expense.SmartExpenseColumns.CARD_LAST_SEGMENT);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CRN_CODE, Expense.SmartExpenseColumns.CRN_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.LOC_NAME, Expense.SmartExpenseColumns.LOC_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.EXP_KEY, Expense.SmartExpenseColumns.EXP_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE,
                Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.MERCHANT_STATE, Expense.SmartExpenseColumns.MERCHANT_STATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.MERCHANT_CITY, Expense.SmartExpenseColumns.MERCHANT_CITY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE,
                Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.MERCHANT_NAME, Expense.SmartExpenseColumns.MERCHANT_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.POSTED_CRN_CODE,
                Expense.SmartExpenseColumns.POSTED_CRN_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRIP_NAME, Expense.SmartExpenseColumns.TRIP_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CITY, Expense.SmartExpenseColumns.CITY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.E_RECEIPT_TYPE, Expense.SmartExpenseColumns.E_RECEIPT_TYPE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.STATE, Expense.SmartExpenseColumns.STATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE,
                Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TICKET_NUMBER, Expense.SmartExpenseColumns.TICKET_NUMBER);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID,
                Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.VEN_LI_NAME, Expense.SmartExpenseColumns.VEN_LI_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.RPE_KEY, Expense.SmartExpenseColumns.RPE_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.AIRLINE_CODE, Expense.SmartExpenseColumns.AIRLINE_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY,
                Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.DOING_BUSINESS_AS,
                Expense.SmartExpenseColumns.DOING_BUSINESS_AS);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CARD_TYPE_CODE, Expense.SmartExpenseColumns.CARD_TYPE_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.ME_KEY, Expense.SmartExpenseColumns.ME_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.PCT_KEY, Expense.SmartExpenseColumns.PCT_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.PCA_KEY, Expense.SmartExpenseColumns.PCA_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CHARGE_DESC, Expense.SmartExpenseColumns.CHARGE_DESC);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CARD_CATEGORY_NAME,
                Expense.SmartExpenseColumns.CARD_CATEGORY_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.MOB_RECEIPT_ID, Expense.SmartExpenseColumns.MOB_RECEIPT_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME,
                Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME,
                Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.RC_KEY, Expense.SmartExpenseColumns.RC_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.STATUS_KEY, Expense.SmartExpenseColumns.STATUS_KEY);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.REJECT_CODE, Expense.SmartExpenseColumns.REJECT_CODE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID,
                Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID,
                Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.COMMENT, Expense.SmartExpenseColumns.COMMENT);

        smartExpenseProjMap.put(Expense.SmartExpenseColumns.TOTAL_DAYS, Expense.SmartExpenseColumns.TOTAL_DAYS);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.PICK_UP_DATE, Expense.SmartExpenseColumns.PICK_UP_DATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.RETURN_DATE, Expense.SmartExpenseColumns.RETURN_DATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.CONFIRMATION_NUMBER,
                Expense.SmartExpenseColumns.CONFIRMATION_NUMBER);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE,
                Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE);
        smartExpenseProjMap.put(Expense.SmartExpenseColumns.USER_ID, Expense.SmartExpenseColumns.USER_ID);

        //ExpenseIt receipt projection map
        expenseItReceiptProjMap = new HashMap();
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns._ID, Expense.ExpenseItReceiptColumns._ID);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.ID, Expense.ExpenseItReceiptColumns.ID);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.USER_ID,Expense.ExpenseItReceiptColumns.USER_ID);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.REPORT_ID,Expense.ExpenseItReceiptColumns.REPORT_ID);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.NOTE,Expense.ExpenseItReceiptColumns.NOTE);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.CCTYPE,Expense.ExpenseItReceiptColumns.CCTYPE);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.CREATED_AT,Expense.ExpenseItReceiptColumns.CREATED_AT);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT,Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL,Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT,Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED,Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE,Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE,Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE);
        expenseItReceiptProjMap.put(Expense.ExpenseItReceiptColumns.ETA,Expense.ExpenseItReceiptColumns.ETA);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.platform.provider.PlatformContentProvider#initCodeUriMatcherInfoMap()
     */
    @Override
    protected SparseArray<UriMatcherInfo> initCodeUriMatcherInfoMap() {

        SparseArray<UriMatcherInfo> map = new SparseArray<UriMatcherInfo>();

        // Init the Expense type info.
        UriMatcherInfo info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.ExpenseTypeColumns.CONTENT_TYPE;
        info.tableName = Expense.ExpenseTypeColumns.TABLE_NAME;
        info.nullColumnName = Expense.ExpenseTypeColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseTypeColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.ExpenseTypeColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = expenseTypeProjMap;
        map.put(EXPENSE_TYPES, info);

        // Init the Expense type id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.ExpenseTypeColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.ExpenseTypeColumns.TABLE_NAME;
        info.nullColumnName = Expense.ExpenseTypeColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseTypeColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.ExpenseTypeColumns._ID;
        info.projectionMap = expenseTypeProjMap;
        info.defaultSortOrder = Expense.ExpenseTypeColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.ExpenseTypeColumns.EXPENSE_TYPES_ID_PATH_POSITION;
        map.put(EXPENSE_TYPE_ID, info);

        // Init the expenses info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.ExpenseColumns.CONTENT_TYPE;
        info.tableName = Expense.ExpenseColumns.VIEW_NAME;
        info.nullColumnName = Expense.ExpenseColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.ExpenseColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = expenseProjMap;
        map.put(EXPENSES, info);

        // Init the expense id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.ExpenseColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.ExpenseColumns.VIEW_NAME;
        info.nullColumnName = Expense.ExpenseColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.ExpenseColumns._ID;
        info.projectionMap = expenseProjMap;
        info.defaultSortOrder = Expense.ExpenseColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.ExpenseColumns.EXPENSES_ID_PATH_POSITION;
        map.put(EXPENSE_ID, info);

        // Init the corporate card transaction info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.CorporateCardTransactionColumns.CONTENT_TYPE;
        info.tableName = Expense.CorporateCardTransactionColumns.TABLE_NAME;
        info.nullColumnName = Expense.CorporateCardTransactionColumns.USER_ID;
        info.contentIdUriBase = Expense.CorporateCardTransactionColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.CorporateCardTransactionColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = corporateCardTransactionProjMap;
        map.put(CORPORATE_CARD_TRANSACTIONS, info);

        // Init the corporate card transaction id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.CorporateCardTransactionColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.CorporateCardTransactionColumns.TABLE_NAME;
        info.nullColumnName = Expense.CorporateCardTransactionColumns.USER_ID;
        info.contentIdUriBase = Expense.CorporateCardTransactionColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.CorporateCardTransactionColumns._ID;
        info.projectionMap = corporateCardTransactionProjMap;
        info.defaultSortOrder = Expense.CorporateCardTransactionColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.CorporateCardTransactionColumns.CORPORATE_CARD_TRANSACTIONS_ID_PATH_POSITION;
        map.put(CORPORATE_CARD_TRANSACTION_ID, info);

        // Init the personal card info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.PersonalCardColumns.CONTENT_TYPE;
        info.tableName = Expense.PersonalCardColumns.TABLE_NAME;
        info.nullColumnName = Expense.PersonalCardColumns.USER_ID;
        info.contentIdUriBase = Expense.PersonalCardColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.PersonalCardColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = personalCardProjMap;
        map.put(PERSONAL_CARDS, info);

        // Init the personal card id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.PersonalCardColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.PersonalCardColumns.TABLE_NAME;
        info.nullColumnName = Expense.PersonalCardColumns.USER_ID;
        info.contentIdUriBase = Expense.PersonalCardColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.PersonalCardColumns._ID;
        info.projectionMap = personalCardProjMap;
        info.defaultSortOrder = Expense.PersonalCardColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.PersonalCardColumns.PERSONAL_CARDS_ID_PATH_POSITION;
        map.put(PERSONAL_CARD_ID, info);

        // Init the personal card transaction info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.PersonalCardTransactionColumns.CONTENT_TYPE;
        info.tableName = Expense.PersonalCardTransactionColumns.TABLE_NAME;
        info.nullColumnName = Expense.PersonalCardTransactionColumns.USER_ID;
        info.contentIdUriBase = Expense.PersonalCardTransactionColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = personalCardTransactionProjMap;
        map.put(PERSONAL_CARD_TRANSACTIONS, info);

        // Init the personal card transaction id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.PersonalCardTransactionColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.PersonalCardTransactionColumns.TABLE_NAME;
        info.nullColumnName = Expense.PersonalCardTransactionColumns.USER_ID;
        info.contentIdUriBase = Expense.PersonalCardTransactionColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.PersonalCardTransactionColumns._ID;
        info.projectionMap = personalCardTransactionProjMap;
        info.defaultSortOrder = Expense.PersonalCardTransactionColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.PersonalCardTransactionColumns.PERSONAL_CARD_TRANSACTIONS_ID_PATH_POSITION;
        map.put(PERSONAL_CARD_TRANSACTION_ID, info);

        // Init the mobile entry info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.MobileEntryColumns.CONTENT_TYPE;
        info.tableName = Expense.MobileEntryColumns.TABLE_NAME;
        info.nullColumnName = Expense.MobileEntryColumns.USER_ID;
        info.contentIdUriBase = Expense.MobileEntryColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.MobileEntryColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = mobileEntryProjMap;
        map.put(MOBILE_ENTRIES, info);

        // Init the mobile entry id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.MobileEntryColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.MobileEntryColumns.TABLE_NAME;
        info.nullColumnName = Expense.MobileEntryColumns.USER_ID;
        info.contentIdUriBase = Expense.MobileEntryColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.MobileEntryColumns._ID;
        info.projectionMap = mobileEntryProjMap;
        info.defaultSortOrder = Expense.MobileEntryColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.MobileEntryColumns.MOBILE_ENTRIES_ID_PATH_POSITION;
        map.put(MOBILE_ENTRY_ID, info);

        // Init the receipt capture info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.ReceiptCaptureColumns.CONTENT_TYPE;
        info.tableName = Expense.ReceiptCaptureColumns.TABLE_NAME;
        info.nullColumnName = Expense.ReceiptCaptureColumns.USER_ID;
        info.contentIdUriBase = Expense.ReceiptCaptureColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.ReceiptCaptureColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = receiptCaptureProjMap;
        map.put(RECEIPT_CAPTURES, info);

        // Init the receipt capture id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.ReceiptCaptureColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.ReceiptCaptureColumns.TABLE_NAME;
        info.nullColumnName = Expense.ReceiptCaptureColumns.USER_ID;
        info.contentIdUriBase = Expense.ReceiptCaptureColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.ReceiptCaptureColumns._ID;
        info.projectionMap = receiptCaptureProjMap;
        info.defaultSortOrder = Expense.ReceiptCaptureColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.ReceiptCaptureColumns.RECEIPT_CAPTURES_ID_PATH_POSITION;
        map.put(RECEIPT_CAPTURE_ID, info);

        // Init the receipt info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.ReceiptColumns.CONTENT_TYPE;
        info.tableName = Expense.ReceiptColumns.TABLE_NAME;
        info.nullColumnName = Expense.ReceiptColumns.USER_ID;
        info.contentIdUriBase = Expense.ReceiptColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.ReceiptColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = receiptProjMap;
        map.put(RECEIPTS, info);

        // Init the receipt id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.ReceiptColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.ReceiptColumns.TABLE_NAME;
        info.nullColumnName = Expense.ReceiptColumns.USER_ID;
        info.contentIdUriBase = Expense.ReceiptColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.ReceiptColumns._ID;
        info.projectionMap = receiptProjMap;
        info.defaultSortOrder = Expense.ReceiptColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.ReceiptColumns.RECEIPTS_ID_PATH_POSITION;
        info.fileOpener = new ReceiptFileOpener(receiptProjMap);
        map.put(RECEIPT_ID, info);

        // Init the smart expense list.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.SmartExpenseColumns.CONTENT_TYPE;
        info.tableName = Expense.SmartExpenseColumns.TABLE_NAME;
        info.nullColumnName = Expense.SmartExpenseColumns.USER_ID;
        info.contentIdUriBase = Expense.SmartExpenseColumns.CONTENT_ID_URI_BASE;
        info.defaultSortOrder = Expense.SmartExpenseColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = smartExpenseProjMap;
        map.put(SMART_EXPENSES, info);

        // Init the smart expense list id info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.SmartExpenseColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.SmartExpenseColumns.TABLE_NAME;
        info.nullColumnName = Expense.SmartExpenseColumns.USER_ID;
        info.contentIdUriBase = Expense.SmartExpenseColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.SmartExpenseColumns._ID;
        info.projectionMap = smartExpenseProjMap;
        info.defaultSortOrder = Expense.SmartExpenseColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.SmartExpenseColumns.SMART_EXPENSE_ID_PATH_POSITION;
        map.put(SMART_EXPENSE_ID, info);


        // Init the ExpenseIt Receipt list info.
        info = new UriMatcherInfo();
        info.isIdSelection = false;
        info.mimeType = Expense.ExpenseItReceiptColumns.CONTENT_TYPE;
        info.tableName = Expense.ExpenseItReceiptColumns.TABLE_NAME;
        info.nullColumnName = Expense.ExpenseItReceiptColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseItReceiptColumns.CONTENT_URI;
        info.defaultSortOrder = Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER;
        info.projectionMap = expenseItReceiptProjMap;
        map.put(EXPENSEIT_RECEIPTS, info);

        //Init the ExpenseIt Receipt list info.
        info = new UriMatcherInfo();
        info.isIdSelection = true;
        info.mimeType = Expense.ExpenseItReceiptColumns.CONTENT_ITEM_TYPE;
        info.tableName = Expense.ExpenseItReceiptColumns.TABLE_NAME;
        info.nullColumnName = Expense.ExpenseItReceiptColumns.USER_ID;
        info.contentIdUriBase = Expense.ExpenseItReceiptColumns.CONTENT_ID_URI_BASE;
        info.idColumnName = Expense.ExpenseItReceiptColumns._ID;
        info.projectionMap = expenseItReceiptProjMap;
        info.defaultSortOrder = Expense.ExpenseItReceiptColumns.DEFAULT_SORT_ORDER;
        info.idPathPosition = Expense.ExpenseItReceiptColumns.EXPENSEIT_RECEIPT_ID_PATH_POSITION;
        map.put(EXPENSEIT_RECEIPT_ID, info);

        return map;
    }

    @Override
    public PlatformSQLiteOpenHelper initPlatformSQLiteOpenHelper(Context context) {
        // This implementation will use an encrypted database.
        PlatformSQLiteOpenHelper helper = new EncryptedSQLiteOpenHelper(new EncryptedExpenseDBHelper(context));
        return helper;
    }

}
