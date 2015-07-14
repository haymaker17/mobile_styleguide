/**
 *
 */
package com.concur.mobile.platform.expense.provider;

import android.util.Log;

import com.concur.mobile.platform.expense.provider.upgrade.AddCommentToSmartExpenseUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.AddExpenseListTablesUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.AddIsAttachedLastAccessTimeToReceiptUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.AddPcaKeyToSmartExpenseUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.AddReceiptContentIdToMobileEntryUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.AddReceiptTableUpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.DBVer10UpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.DBVer7UpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.DBVer9UpgradeAction;
import com.concur.mobile.platform.expense.provider.upgrade.SmartExpenseUpgradeAction;
import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.SchemaUpgradeAction;

/**
 * Provides database schema support for the Expense provider.
 *
 * @author andrewk
 */
public class ExpenseDBSchema {

    private static final String CLS_TAG = "ExpenseDBSchema";

    // Contains the expense database name.
    static final String DATABASE_NAME = "expense.db";

    // Contains the current database version.
    // DB History
    // DATABASE_VERSION = 1 -- initial version.
    // DATABASE_VERSION = 2 -- added CorporateCardTransaction, PersonalCard, PersonalCardTransaction,
    // MobileEntry and ReceiptCapture tables.
    // DATABASE_VERSION = 3 -- added Receipt table.
    // DATABASE_VERSION = 4 -- added 'RECEIPT_CONTENT_URI' to MOBILE_ENTRY table.
    // -- added 'IS_ATTACHED' and 'LAST_ACCESS_TIME' to RECEIPT table.
    // DATABASE_VERSION = 5 -- added 'SMART_EXPENSE' table.
    // DATABASE_VERSION = 6 -- added 'Comment' field to 'SMART_EXPENSE' table.
    // DATABASE_VERSION = 7 -- added 'TotalDays', 'ConfirmationNumber', 'PickupDate', 'ReturnDate' and
    // 'AverageDailyRate' to the 'SmartExpense' table.
    // DATABASE_VERSION = 8 -- added 'PCA_KEY' to 'SMART_EXPENSE' table.
    // DATABASE_VERSION = 9 -- added additional receipt columns to 'RECEIPT' table.
    // DATABASE_VERSION = 10 -- added support for 'REJECT_CODE' column in 'SMART_EXPENSE' table.
    static final int DATABASE_VERSION = 10;
    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the expense type table.
    protected static final String SCHEMA_CREATE_EXPENSE_TYPE_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.ExpenseTypeColumns.TABLE_NAME + " ("

        + Expense.ExpenseTypeColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.ExpenseTypeColumns.TYPE_CODE + " TEXT, "

        + Expense.ExpenseTypeColumns.TYPE_KEY + " TEXT, "

        + Expense.ExpenseTypeColumns.TYPE_NAME + " TEXT, "

        + Expense.ExpenseTypeColumns.FORM_KEY + " INTEGER, "

        + Expense.ExpenseTypeColumns.HAS_POST_AMT_CALC + " INTEGER, "

        + Expense.ExpenseTypeColumns.HAS_TAX_FORM + " INTEGER, "

        + Expense.ExpenseTypeColumns.ITEMIZATION_UNALLOW_EXP_KEYS + " TEXT, "

        + Expense.ExpenseTypeColumns.ITEMIZATION_FORM_KEY + " INTEGER, "

        + Expense.ExpenseTypeColumns.ITEMIZATION_STYLE + " TEXT, "

        + Expense.ExpenseTypeColumns.ITEMIZATION_TYPE + " TEXT, "

        + Expense.ExpenseTypeColumns.PARENT_EXP_KEY + " TEXT, "

        + Expense.ExpenseTypeColumns.PARENT_EXP_NAME + " TEXT, "

        + Expense.ExpenseTypeColumns.SUPPORTS_ATTENDEES + " INTEGER, "

        + Expense.ExpenseTypeColumns.VENDOR_LIST_KEY + " INTEGER, "

        + Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_AMOUNT + " INTEGER, "

        + Expense.ExpenseTypeColumns.ALLOW_EDIT_ATTENDEE_COUNT + " INTEGER, "

        + Expense.ExpenseTypeColumns.ALLOW_NO_SHOWS + " INTEGER, "

        + Expense.ExpenseTypeColumns.DISPLAY_ADD_ATTENDEE_ON_FORM + " INTEGER, "

        + Expense.ExpenseTypeColumns.DISPLAY_ATTENDEE_AMOUNTS + " INTEGER, "

        + Expense.ExpenseTypeColumns.USER_AS_ATTENDEE_DEFAULT + " INTEGER, "

        + Expense.ExpenseTypeColumns.UNALLOW_ATN_TYPE_KEYS + " TEXT, "

        + Expense.ExpenseTypeColumns.POLICY_KEY + " TEXT, "

        + Expense.ExpenseTypeColumns.USER_ID + " TEXT"

        + ")";

    // Drop the expense type table.
    protected static final String DROP_EXP_TYPE_TABLE = "DROP TABLE IF EXISTS " + Expense.ExpenseTypeColumns.TABLE_NAME
        + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the expense table.
    public static final String SCHEMA_CREATE_EXPENSE_VIEW = "CREATE VIEW IF NOT EXISTS "
        + Expense.ExpenseColumns.VIEW_NAME
        + " AS "

        // Select CCT's

        + "SELECT " + Expense.CorporateCardTransactionColumns.TYPE + " AS " + Expense.ExpenseColumns.TYPE + ", "

        + Expense.CorporateCardTransactionColumns.MERCHANT_NAME + " AS " + Expense.ExpenseColumns.VENDOR_NAME
        + ", "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_AMOUNT
        + ", "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_DATE
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_DATE
        + ", "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_CRN_CODE
        + ", "

        + Expense.CorporateCardTransactionColumns.USER_ID
        + " AS "
        + Expense.ExpenseColumns.USER_ID
        + ", "

        + Expense.CorporateCardTransactionColumns._ID
        + " AS "
        + Expense.ExpenseColumns.EXP_ID

        + " FROM "
        + Expense.CorporateCardTransactionColumns.TABLE_NAME

        + " UNION "

        // Select PCT's

        + "SELECT " + Expense.PersonalCardTransactionColumns.TYPE + " AS " + Expense.ExpenseColumns.TYPE + ", "

        + Expense.PersonalCardTransactionColumns.DESCRIPTION + " AS " + Expense.ExpenseColumns.VENDOR_NAME + ", "

        + Expense.PersonalCardTransactionColumns.AMOUNT + " AS " + Expense.ExpenseColumns.TRANSACTION_AMOUNT + ", "

        + Expense.PersonalCardTransactionColumns.DATE_POSTED
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_DATE
        + ", "

        + Expense.PersonalCardTransactionColumns.CRN_CODE
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_CRN_CODE
        + ", "

        + Expense.PersonalCardTransactionColumns.USER_ID
        + " AS "
        + Expense.ExpenseColumns.USER_ID
        + ", "

        + Expense.PersonalCardTransactionColumns._ID
        + " AS "
        + Expense.ExpenseColumns.EXP_ID

        + " FROM "
        + Expense.PersonalCardTransactionColumns.TABLE_NAME

        + " UNION "

        // Select REC's

        + "SELECT " + Expense.ReceiptCaptureColumns.TYPE + " AS " + Expense.ExpenseColumns.TYPE + ", "

        + Expense.ReceiptCaptureColumns.VENDOR_NAME + " AS " + Expense.ExpenseColumns.VENDOR_NAME + ", "

        + Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT + " AS " + Expense.ExpenseColumns.TRANSACTION_AMOUNT
        + ", "

        + Expense.ReceiptCaptureColumns.TRANSACTION_DATE + " AS " + Expense.ExpenseColumns.TRANSACTION_DATE
        + ", "

        + Expense.ReceiptCaptureColumns.CRN_CODE
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_CRN_CODE
        + ", "

        + Expense.ReceiptCaptureColumns.USER_ID
        + " AS "
        + Expense.ExpenseColumns.USER_ID
        + ", "

        + Expense.ReceiptCaptureColumns._ID
        + " AS "
        + Expense.ExpenseColumns.EXP_ID

        + " FROM "
        + Expense.ReceiptCaptureColumns.TABLE_NAME

        + " UNION "

        // Select MOB's

        + "SELECT " + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.TYPE + " AS "
        + Expense.ExpenseColumns.TYPE + ", "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.VENDOR_NAME + " AS "
        + Expense.ExpenseColumns.VENDOR_NAME + ", "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.TRANSACTION_AMOUNT + " AS "
        + Expense.ExpenseColumns.TRANSACTION_AMOUNT + ", "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.TRANSACTION_DATE + " AS "
        + Expense.ExpenseColumns.TRANSACTION_DATE
        + ", "

        + Expense.MobileEntryColumns.TABLE_NAME
        + "."
        + Expense.MobileEntryColumns.CRN_CODE
        + " AS "
        + Expense.ExpenseColumns.TRANSACTION_CRN_CODE
        + ", "

        + Expense.MobileEntryColumns.TABLE_NAME
        + "."
        + Expense.MobileEntryColumns.USER_ID
        + " AS "
        + Expense.ExpenseColumns.USER_ID
        + ", "

        + Expense.MobileEntryColumns.TABLE_NAME
        + "."
        + Expense.MobileEntryColumns._ID
        + " AS "
        + Expense.ExpenseColumns.EXP_ID

        + " FROM "
        + Expense.MobileEntryColumns.TABLE_NAME
        + ","
        + Expense.CorporateCardTransactionColumns.TABLE_NAME
        + ","
        + Expense.PersonalCardTransactionColumns.TABLE_NAME

        + " WHERE "

        // select * from MOB where _ID NOT IN (select DISTINCT MOB._ID from MOB,CCT,PCT WHERE (CCT.SMART_ME_KEY = MOB.ME_KEY
        // AND CCT.IS_SPLIT = 0) OR (PCT.SMART_ME_KEY = MOB.ME_KEY AND PCT.IS_SPLIT = 0)) AND MOB.PCT_KEY IS NULL AND
        // MOB.CCT_KEY IS NULL;

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns._ID
        + " NOT IN (SELECT DISTINCT "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns._ID + " FROM "
        + Expense.MobileEntryColumns.TABLE_NAME + ","

        + Expense.CorporateCardTransactionColumns.TABLE_NAME + ","
        + Expense.PersonalCardTransactionColumns.TABLE_NAME +

        " WHERE ("

        + Expense.CorporateCardTransactionColumns.TABLE_NAME + "."
        + Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY

        + " = "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.MOBILE_ENTRY_KEY

        + " AND "

        + Expense.CorporateCardTransactionColumns.TABLE_NAME + "."
        + Expense.CorporateCardTransactionColumns.IS_SPLIT + " = 0) OR ("

        + Expense.PersonalCardTransactionColumns.TABLE_NAME + "."
        + Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY

        + " = "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.MOBILE_ENTRY_KEY

        + " AND "

        + Expense.PersonalCardTransactionColumns.TABLE_NAME + "." + Expense.PersonalCardTransactionColumns.IS_SPLIT
        + " = 0)) AND "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.PCT_KEY

        + " IS NULL AND "

        + Expense.MobileEntryColumns.TABLE_NAME + "." + Expense.MobileEntryColumns.CCT_KEY

        + " IS NULL"

        + ";";

    // Drops the expense view.
    protected static final String DROP_EXPENSE_VIEW = "DROP VIEW IF EXISTS " + Expense.ExpenseColumns.VIEW_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the corporate card transaction table.
    public static final String SCHEMA_CREATE_CORPORATE_CARD_TRANSACTION_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.CorporateCardTransactionColumns.TABLE_NAME + " ("

        + Expense.CorporateCardTransactionColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.CorporateCardTransactionColumns.TYPE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.CARD_TYPE_CODE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.CARD_TYPE_NAME + " TEXT, "

        + Expense.CorporateCardTransactionColumns.CCT_KEY + " TEXT, "

        + Expense.CorporateCardTransactionColumns.CCT_TYPE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.HAS_RICH_DATA + " INTEGER, "

        + Expense.CorporateCardTransactionColumns.DESCRIPTION + " TEXT, "

        + Expense.CorporateCardTransactionColumns.DOING_BUSINESS_AS + " TEXT, "

        + Expense.CorporateCardTransactionColumns.EXPENSE_KEY + " TEXT, "

        + Expense.CorporateCardTransactionColumns.EXPENSE_NAME + " TEXT, "

        + Expense.CorporateCardTransactionColumns.MERCHANT_CITY + " TEXT, "

        + Expense.CorporateCardTransactionColumns.MERCHANT_COUNTRY_CODE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.MERCHANT_NAME + " TEXT, "

        + Expense.CorporateCardTransactionColumns.MERCHANT_STATE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.SMART_EXPENSE_ME_KEY + " TEXT, "

        + Expense.CorporateCardTransactionColumns.MOBILE_ENTRY_ID + " INTEGER, "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_AMOUNT + " REAL, "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_CRN_CODE + " TEXT, "

        + Expense.CorporateCardTransactionColumns.TRANSACTION_DATE + " INTEGER, "

        + Expense.CorporateCardTransactionColumns.TAG + " TEXT, "

        + Expense.CorporateCardTransactionColumns.IS_SPLIT + " INTEGER DEFAULT 0, "

        + Expense.CorporateCardTransactionColumns.USER_ID + " TEXT"

        + ")";

    // Drops the corporate card transaction table.
    protected static final String DROP_CORPORATE_CARD_TRANSACTION_TABLE = "DROP TABLE IF EXISTS "
        + Expense.CorporateCardTransactionColumns.TABLE_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the personal card table.
    public static final String SCHEMA_CREATE_PERSONAL_CARD_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.PersonalCardColumns.TABLE_NAME + " ("

        + Expense.PersonalCardColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.PersonalCardColumns.PCA_KEY + " TEXT, "

        + Expense.PersonalCardColumns.CARD_NAME + " TEXT, "

        + Expense.PersonalCardColumns.ACCT_NUM_LAST_FOUR + " TEXT, "

        + Expense.PersonalCardColumns.CRN_CODE + " TEXT, "

        + Expense.PersonalCardColumns.TAG + " TEXT, "

        + Expense.PersonalCardColumns.USER_ID + " TEXT"

        + ")";

    // Drops the personal card table.
    protected static final String DROP_PERSONAL_CARD_TABLE = "DROP TABLE IF EXISTS "
        + Expense.PersonalCardColumns.TABLE_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the personal card transaction table.
    public static final String SCHEMA_CREATE_PERSONAL_CARD_TRANSACTION_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.PersonalCardTransactionColumns.TABLE_NAME + " ("

        + Expense.PersonalCardTransactionColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.PersonalCardTransactionColumns.TYPE + " TEXT, "

        + Expense.PersonalCardTransactionColumns.PCT_KEY + " TEXT, "

        + Expense.PersonalCardTransactionColumns.DATE_POSTED + " INTEGER, "

        + Expense.PersonalCardTransactionColumns.DESCRIPTION + " TEXT, "

        + Expense.PersonalCardTransactionColumns.AMOUNT + " REAL, "

        + Expense.PersonalCardTransactionColumns.CRN_CODE + " TEXT, "

        + Expense.PersonalCardTransactionColumns.STATUS + " TEXT, "

        + Expense.PersonalCardTransactionColumns.CATEGORY + " TEXT, "

        + Expense.PersonalCardTransactionColumns.EXP_KEY + " TEXT, "

        + Expense.PersonalCardTransactionColumns.EXP_NAME + " TEXT, "

        + Expense.PersonalCardTransactionColumns.RPT_KEY + " TEXT, "

        + Expense.PersonalCardTransactionColumns.RPT_NAME + " TEXT, "

        + Expense.PersonalCardTransactionColumns.SMART_EXPENSE_ME_KEY + " TEXT, "

        + Expense.PersonalCardTransactionColumns.MOBILE_ENTRY_ID + " INTEGER, "

        + Expense.PersonalCardTransactionColumns.PERSONAL_CARD_ID + " INTEGER REFERENCES "
        + Expense.PersonalCardColumns.TABLE_NAME + " ON DELETE CASCADE, "

        + Expense.PersonalCardTransactionColumns.TAG + " TEXT, "

        + Expense.PersonalCardTransactionColumns.IS_SPLIT + " INTEGER DEFAULT 0, "

        + Expense.PersonalCardTransactionColumns.USER_ID + " TEXT"

        + ")";

    // Drops the personal card transaction table.
    protected static final String DROP_PERSONAL_CARD_TRANSACTION_TABLE = "DROP TABLE IF EXISTS "
        + Expense.PersonalCardTransactionColumns.TABLE_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the mobile entry table.
    public static final String SCHEMA_CREATE_MOBILE_ENTRY_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.MobileEntryColumns.TABLE_NAME + " ("

        + Expense.MobileEntryColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.MobileEntryColumns.CRN_CODE + " TEXT, "

        + Expense.MobileEntryColumns.EXP_KEY + " TEXT, "

        + Expense.MobileEntryColumns.EXP_NAME + " TEXT, "

        + Expense.MobileEntryColumns.LOCATION_NAME + " TEXT, "

        + Expense.MobileEntryColumns.VENDOR_NAME + " TEXT, "

        + Expense.MobileEntryColumns.TYPE + " TEXT, "

        + Expense.MobileEntryColumns.MOBILE_ENTRY_KEY + " TEXT, "

        + Expense.MobileEntryColumns.PCA_KEY + " TEXT, "

        + Expense.MobileEntryColumns.PCT_KEY + " TEXT, "

        + Expense.MobileEntryColumns.CCT_KEY + " TEXT, "

        + Expense.MobileEntryColumns.RC_KEY + " TEXT, "

        + Expense.MobileEntryColumns.TRANSACTION_AMOUNT + " REAL, "

        + Expense.MobileEntryColumns.TRANSACTION_DATE + " INTEGER, "

        + Expense.MobileEntryColumns.HAS_RECEIPT_IMAGE + " INTEGER, "

        + Expense.MobileEntryColumns.RECEIPT_IMAGE_ID + " TEXT, "

        + Expense.MobileEntryColumns.RECEIPT_CONTENT_ID + " INTEGER DEFAULT NULL, "

        + Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA + " TEXT, "

        + Expense.MobileEntryColumns.RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + " TEXT, "

        + Expense.MobileEntryColumns.COMMENT + " TEXT, "

        + Expense.MobileEntryColumns.TAG + " TEXT, "

        + Expense.MobileEntryColumns.USER_ID + " TEXT"

        + ")";

    // Drops the mobile entry table.
    protected static final String DROP_MOBILE_ENTRY_TABLE = "DROP TABLE IF EXISTS "
        + Expense.MobileEntryColumns.TABLE_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the receipt capture table.
    public static final String SCHEMA_CREATE_RECEIPT_CAPTURE_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.ReceiptCaptureColumns.TABLE_NAME + " ("

        + Expense.ReceiptCaptureColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.ReceiptCaptureColumns.TYPE + " TEXT, "

        + Expense.ReceiptCaptureColumns.CRN_CODE + " TEXT, "

        + Expense.ReceiptCaptureColumns.EXP_KEY + " TEXT, "

        + Expense.ReceiptCaptureColumns.EXP_NAME + " TEXT, "

        + Expense.ReceiptCaptureColumns.VENDOR_NAME + " TEXT, "

        + Expense.ReceiptCaptureColumns.RC_KEY + " TEXT, "

        + Expense.ReceiptCaptureColumns.SMART_EXPENSE_ID + " TEXT, "

        + Expense.ReceiptCaptureColumns.TRANSACTION_AMOUNT + " REAL, "

        + Expense.ReceiptCaptureColumns.TRANSACTION_DATE + " INTEGER, "

        + Expense.ReceiptCaptureColumns.RECEIPT_IMAGE_ID + " TEXT, "

        + Expense.ReceiptCaptureColumns.TAG + " TEXT, "

        + Expense.ReceiptCaptureColumns.USER_ID + " TEXT"

        + ")";

    // Drops the receipt capture table.
    protected static final String DROP_RECEIPT_CAPTURE_TABLE = "DROP TABLE IF EXISTS "
        + Expense.ReceiptCaptureColumns.TABLE_NAME + ";";

    // Creates the receipt table.
    public static final String SCHEMA_CREATE_RECEIPT_TABLE = "CREATE TABLE IF NOT EXISTS " //
        + Expense.ReceiptColumns.TABLE_NAME + " ("

        + Expense.ReceiptColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.ReceiptColumns.ETAG + " TEXT, "

        + Expense.ReceiptColumns.ID + " TEXT, "

        + Expense.ReceiptColumns.URI + " TEXT, "

        + Expense.ReceiptColumns.RECEIPT_CONTENT_TYPE + " TEXT, "

        + Expense.ReceiptColumns.LOCAL_PATH + " TEXT, "

        + Expense.ReceiptColumns.RECEIPT_DATA + " TEXT, "

        + Expense.ReceiptColumns.THUMBNAIL_URI + " TEXT, "

        + Expense.ReceiptColumns.THUMBNAIL_CONTENT_TYPE + " TEXT, "

        + Expense.ReceiptColumns.THUMBNAIL_LOCAL_PATH + " TEXT, "

        + Expense.ReceiptColumns.THUMBNAIL_RECEIPT_DATA + " TEXT, "

        + Expense.ReceiptColumns.IS_ATTACHED + " INTEGER DEFAULT 0, "

        + Expense.ReceiptColumns.LAST_ACCESS_TIME + " INTEGER, "

        + Expense.ReceiptColumns.IMAGE_UPLOAD_TIME + " INTEGER, "

        + Expense.ReceiptColumns.FILE_NAME + " TEXT, "

        + Expense.ReceiptColumns.FILE_TYPE + " TEXT, "

        + Expense.ReceiptColumns.SYSTEM_ORIGIN + " TEXT, "

        + Expense.ReceiptColumns.IMAGE_ORIGIN + " TEXT, "

        + Expense.ReceiptColumns.IMAGE_URL + " TEXT, "

        + Expense.ReceiptColumns.THUMB_URL + " TEXT, "

        + Expense.ReceiptColumns.OCR_IMAGE_ORIGIN + " TEXT, "

        + Expense.ReceiptColumns.OCR_STAT_KEY + " TEXT, "

        + Expense.ReceiptColumns.OCR_REJECT_CODE + " TEXT, "

        + Expense.ReceiptColumns.USER_ID + " TEXT"

        + ")";

    // Drops the receipt capture table.
    protected static final String DROP_RECEIPT_TABLE = "DROP TABLE IF EXISTS " //
        + Expense.ReceiptColumns.TABLE_NAME + ";";

    // ****
    // NOTE: Please leave one blank line between table column definitions, it prevents formatting
    // from randomly combining on different lines making code browsing more difficult.
    // ****

    // Creates the smart expense table.
    public static final String SCHEMA_CREATE_SMART_EXPENSE_TABLE = "CREATE TABLE IF NOT EXISTS "
        + Expense.SmartExpenseColumns.TABLE_NAME + " ("

        + Expense.SmartExpenseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.SmartExpenseColumns.FUEL_SERVICE_CHARGE + " REAL, "

        + Expense.SmartExpenseColumns.ESTIMATED_AMOUNT + " REAL, "

        + Expense.SmartExpenseColumns.CCA_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.CCT_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.EXTRACT_CCT_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.SMART_EXPENSE_ID + " TEXT, "

        + Expense.SmartExpenseColumns.E_RECEIPT_SOURCE + " TEXT, "

        + Expense.SmartExpenseColumns.TRANSACTION_DATE + " INTEGER, "

        + Expense.SmartExpenseColumns.HAS_RICH_DATA + " INTEGER, "

        + Expense.SmartExpenseColumns.E_RECEIPT_ID + " TEXT, "

        + Expense.SmartExpenseColumns.SEGMENT_ID + " TEXT, "

        + Expense.SmartExpenseColumns.EXP_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.TRANSACTION_GROUP + " TEXT, "

        + Expense.SmartExpenseColumns.VENDOR_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.COUNTRY + " TEXT, "

        + Expense.SmartExpenseColumns.TRIP_ID + " TEXT, "

        + Expense.SmartExpenseColumns.VENDOR_DESCRIPTION + " TEXT,"

        + Expense.SmartExpenseColumns.EXCHANGE_RATE + " REAL, "

        + Expense.SmartExpenseColumns.POSTED_AMOUNT + " REAL, "

        + Expense.SmartExpenseColumns.TRANSACTION_AMOUNT + " REAL, "

        + Expense.SmartExpenseColumns.INSURANCE_CHARGE + " REAL, "

        + Expense.SmartExpenseColumns.GPS_CHARGE + " REAL, "

        + Expense.SmartExpenseColumns.CARD_LAST_SEGMENT + " TEXT, "

        + Expense.SmartExpenseColumns.CRN_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.LOC_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.EXP_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.TRAVEL_COMPANY_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.MERCHANT_STATE + " TEXT, "

        + Expense.SmartExpenseColumns.MERCHANT_CITY + " TEXT, "

        + Expense.SmartExpenseColumns.MERCHANT_CUNTRY_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.MERCHANT_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.POSTED_CRN_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.TRIP_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.CITY + " TEXT, "

        + Expense.SmartExpenseColumns.E_RECEIPT_TYPE + " TEXT, "

        + Expense.SmartExpenseColumns.STATE + " TEXT, "

        + Expense.SmartExpenseColumns.TRANSACTION_CRN_CODE + " TEXT,"

        + Expense.SmartExpenseColumns.TICKET_NUMBER + " TEXT, "

        + Expense.SmartExpenseColumns.E_RECEIPT_IMAGE_ID + " TEXT, "

        + Expense.SmartExpenseColumns.VEN_LI_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.RPE_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.AIRLINE_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.SEGMENT_TYPE_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.DOING_BUSINESS_AS + " TEXT, "

        + Expense.SmartExpenseColumns.CARD_TYPE_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.ME_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.PCT_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.PCA_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.CHARGE_DESC + " TEXT, "

        + Expense.SmartExpenseColumns.CARD_CATEGORY_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.MOB_RECEIPT_ID + " TEXT, "

        + Expense.SmartExpenseColumns.CARD_ICON_FILE_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.CARD_PROGRAM_TYPE_NAME + " TEXT, "

        + Expense.SmartExpenseColumns.RC_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.STATUS_KEY + " TEXT, "

        + Expense.SmartExpenseColumns.REJECT_CODE + " TEXT, "

        + Expense.SmartExpenseColumns.RECEIPT_IMAGE_ID + " TEXT, "

        + Expense.SmartExpenseColumns.CCT_RECEIPT_IMG_ID + " TEXT, "

        + Expense.SmartExpenseColumns.COMMENT + " TEXT, "

        + Expense.SmartExpenseColumns.TOTAL_DAYS + " INTEGER, "

        + Expense.SmartExpenseColumns.PICK_UP_DATE + " INTEGER, "

        + Expense.SmartExpenseColumns.RETURN_DATE + " INTEGER, "

        + Expense.SmartExpenseColumns.CONFIRMATION_NUMBER + " TEXT, "

        + Expense.SmartExpenseColumns.AVERAGE_DAILY_RATE + " REAL, "

        + Expense.SmartExpenseColumns.USER_ID + " TEXT " + ")";

    // Drop the expense type table.
    protected static final String DROP_SMART_EXPENSE_TABLE = "DROP TABLE IF EXISTS "
        + Expense.SmartExpenseColumns.TABLE_NAME + ";";

    // Creates the expenseIt receipts table.
    public static final String SCHEMA_CREATE_EXPENSEIT_RECEIPT_TABLE =
        "CREATE TABLE IF NOT EXISTS "

        + Expense.ExpenseItReceiptColumns.TABLE_NAME + " ("

        + Expense.ExpenseItReceiptColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "

        + Expense.ExpenseItReceiptColumns.ID + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.REPORT_ID + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.NOTE + " TEXT, "

        + Expense.ExpenseItReceiptColumns.CCTYPE + " TEXT, "

        + Expense.ExpenseItReceiptColumns.CREATED_AT + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.SEND_TO_CTE_AT + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.IMAGE_DATA_URL + " TEXT, "

        + Expense.ExpenseItReceiptColumns.TOTAL_IMAGE_COUNT + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.TOTAL_IMAGES_UPLOADED + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.PARSING_STATUS_CODE + " TEXT, "

        + Expense.ExpenseItReceiptColumns.PROCESSING_ENGINE + " TEXT, "

        + Expense.ExpenseItReceiptColumns.ETA + " INTEGER, "

        + Expense.ExpenseItReceiptColumns.USER_ID + " TEXT"

        + ")";

    // Drop the expense type table.
    protected static final String DROP_EXPENSEIT_RECEIPT_TABLE = "DROP TABLE IF EXISTS "
        + Expense.ExpenseItReceiptColumns.TABLE_NAME + ";";

    // Contains the expense schema creation SQL. Must be in execution order
    protected static final String[] SCHEMA_CREATE_SQL = {SCHEMA_CREATE_EXPENSE_TYPE_TABLE,
        SCHEMA_CREATE_CORPORATE_CARD_TRANSACTION_TABLE, SCHEMA_CREATE_PERSONAL_CARD_TABLE,
        SCHEMA_CREATE_PERSONAL_CARD_TRANSACTION_TABLE, SCHEMA_CREATE_MOBILE_ENTRY_TABLE,
        SCHEMA_CREATE_RECEIPT_CAPTURE_TABLE, SCHEMA_CREATE_EXPENSE_VIEW, SCHEMA_CREATE_RECEIPT_TABLE,
        SCHEMA_CREATE_SMART_EXPENSE_TABLE,
        SCHEMA_CREATE_EXPENSEIT_RECEIPT_TABLE};

    // Contains the expense schema deletion SQL. Must be in execution order
    protected static final String[] SCHEMA_DELETE_SQL = {DROP_EXP_TYPE_TABLE, DROP_EXPENSE_VIEW,
        DROP_CORPORATE_CARD_TRANSACTION_TABLE, DROP_PERSONAL_CARD_TRANSACTION_TABLE, DROP_PERSONAL_CARD_TABLE,
        DROP_MOBILE_ENTRY_TABLE, DROP_RECEIPT_CAPTURE_TABLE, DROP_RECEIPT_TABLE, DROP_SMART_EXPENSE_TABLE,
        DROP_EXPENSEIT_RECEIPT_TABLE};

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    static void onCreate(PlatformSQLiteDatabase db) {

        String sqliteVersion = db.getSQLiteVersion();
        Log.d(Const.LOG_TAG, CLS_TAG + ".onCreate: SQLite version -> '"
            + ((sqliteVersion != null) ? sqliteVersion : "unknown") + "'.");

        Log.v(Const.LOG_TAG, "Creating schema.");

        for (int i = 0; i < SCHEMA_CREATE_SQL.length; i++) {
            db.execSQL(SCHEMA_CREATE_SQL[i]);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     * 
     * The pattern here is that the last numbered case statement should be the only one with a break; IE Case 1: code, Case 2:
     * code, Case 3: code, break;
     * 
     * The case used will be one less than the current db version because if we upgrade from version 3 to 4, the oldVersion will
     * be 3. The logic here is that if a user upgrades multiple versions (IE they have version 2 and upgrade to version 5), the
     * database upgrades will happen in the order that updates were written. That is, Case 2 will be executed, then 3, then 4. The
     * upgrades made in 4 will have the user up to date in version 5.
     */
    static void onUpgrade(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {

        Log.v(Const.LOG_TAG, "Upgrading database from " + oldVersion + " to " + newVersion);

        // Disable foreign key support.
        if (db.disableForeignKeySupport()) {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: disabling foreign key support: succeeded");
        } else {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: disabling foreign key support: failed.");
        }

        switch (oldVersion) {
            case 1: {
                // No-op.
            }
            case 2: {
                // Note: The 'AddExpenseListTablesUpgradeAction' should have been in 'case 1' from above, but
                // it was placed here in error. The upgrade action for database version 3 has been added
                // to 'case2' (which is where it would normally belong, current version - 1).
                SchemaUpgradeAction sua = new AddExpenseListTablesUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddExpenseListTablesUpgradeAction!");
                }
                sua = new AddReceiptTableUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddReceiptTableUpgradeAction!");
                }
            }
            case 3: {
                SchemaUpgradeAction sua = new AddReceiptContentIdToMobileEntryUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onUpgrade: failed to execute AddReceiptContentURIToMobileEntryUpgradeAction!");
                }
                sua = new AddIsAttachedLastAccessTimeToReceiptUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                        + ".onUpgrade: failed to execute AddIsAttachedLastAccessTimeToReceiptUpgradeAction!");
                }
            }
            case 4: {
                SchemaUpgradeAction sua = new SmartExpenseUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute SmartExpenseUpgradeAction!");
                }
            }
            case 5: {
                SchemaUpgradeAction sua = new AddCommentToSmartExpenseUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddCommentToSmartExpenseUpgradeAction!");
                }
            }
            case 6: {
                SchemaUpgradeAction sua = new DBVer7UpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute DBVer7UpgradeAction!");
                }
            }
            case 7: {
                SchemaUpgradeAction sua = new AddPcaKeyToSmartExpenseUpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute AddPcaKeyToSmartExpenseUpgradeAction!");
                }
            }
            case 8: {
                SchemaUpgradeAction sua = new DBVer9UpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute DBVer9UpgradeAction!");
                }
            }
            case 9: {
                SchemaUpgradeAction sua = new DBVer10UpgradeAction(db, oldVersion, newVersion);
                if (!sua.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: failed to execute DBVer10UpgradeAction!");
                }
                break;
            }

            default: {
                Log.v(Const.LOG_TAG, "DB version provided no upgrade path: " + newVersion);
                break;
            }
        }

        // Enable foreign key support.
        if (db.enableForeignKeySupport()) {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: enabling foreign key support: succeeded");
        } else {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: enabling foreign key support: failed.");
        }

    }

}
