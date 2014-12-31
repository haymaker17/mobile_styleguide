package com.concur.mobile.core.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.concur.mobile.core.util.Const;

public class MobileDatabaseHelper extends SQLiteOpenHelper {

    private static final String CLS_TAG = MobileDatabaseHelper.class.getSimpleName();

    protected static final String RESPONSE_ID = "ID";
    protected static final String RESPONSE_CLIENT_LAST_UPDATE = "CLIENT_LAST_UPDATE";
    protected static final String RESPONSE_SERVER_LAST_UPDATE = "SERVER_LAST_UPDATE";
    protected static final String RESPONSE_RESPONSE = "RESPONSE";

    protected static final String TABLE_RESPONSE = "RESPONSE";
    protected static final String TABLE_REPORT_LIST = "REPORT_LIST";
    protected static final String TABLE_REPORT_DETAIL = "REPORT_DETAIL";
    protected static final String TABLE_REPORT_SUBMIT_APPROVE = "REPORT_SUBMIT_APPROVE";
    protected static final String TABLE_REPORT_SUBMIT_REJECT = "REPORT_SUBMIT_REJECT";
    protected static final String TABLE_EXPENSE_TYPE = "EXPENSE_TYPE";
    protected static final String TABLE_EXPENSE_ENTRY = "EXPENSE_ENTRY";
    protected static final String TABLE_HTTP_REQUEST = "HTTP_REQUEST";
    protected static final String TABLE_CARD_CHANGES = "CARD_TRANSACTION_CHANGES";
    protected static final String TABLE_CURRENCY = "CURRENCY";
    protected static final String TABLE_SPLIT_SMART_EXPENSE = "SPLIT_SMART_EXPENSE";
    protected static final String TABLE_REPORT_HEADER = "REPORT_HEADER";
    protected static final String TABLE_REPORT_ENTRY = "REPORT_ENTRY";
    protected static final String TABLE_ITINERARY = "ITINERARY";
    protected static final String TABLE_COM_COMPONENT = "COM_COMPONENT";
    protected static final String TABLE_RECEIPT_SHARE = "RECEIPT_SHARE";
    protected static final String TABLE_MRU = "MRU";

    // GOV
    protected static final String TABLE_GOV_DOCUMENT_DETAIL = "GOV_DOCUMENT_DETAIL";
    protected static final String TABLE_GOV_DOC_STAMP_REQ_REASON = "GOV_DOCUMENT_STAMP_REQUIREMENT_INFO";
    protected static final String TABLE_GOV_MESSAGES = "GOV_MESSAGES";

    protected static final String CARD_STATUS_HIDDEN = "HD";

    protected static final String PERSONAL_CARD_STATUS_HIDDEN = "HD:P";
    protected static final String CORPORATE_CARD_STATUS_HIDDEN = "HD:C";

    protected static final String COLUMN_REPORT_KEY = "REPORT_KEY";
    protected static final String COLUMN_REPORT_ENTRY_KEY = "REPORT_ENTRY_KEY";
    protected static final String COLUMN_REPORT_ENTRY = "REPORT_ENTRY";
    protected static final String COLUMN_CLIENT_REQUEST_ID = "REQUEST_ID";
    protected static final String COLUMN_CLIENT_TRANSACTION_ID = "CLIENT_TRANSACTION_ID";
    protected static final String COLUMN_REQUEST = "REQUEST";
    protected static final String COLUMN_EXPENSE_TYPE = "EXPENSE_TYPE";
    public static final String COLUMN_USER_ID = "USER_ID";
    public static final String COLUMN_POL_KEY = "POL_KEY";
    protected static final String COLUMN_ID = "ID";

    public static final String COLUMN_CRN_CODE = "CRN_CODE";
    public static final String COLUMN_CURRENCY = "CURRENCY";
    public static final String COLUMN_FIELD_ID = "FIELD_ID";
    public static final String COLUMN_FIELD_VALUE = "FIELD_VALUE";
    public static final String COLUMN_LICODE = "LIST_ITEM_CODE";

    protected static final String COLUMN_FILE_NAME = "FILE_NAME";
    protected static final String COLUMN_RECEIPT_IMAGE = "RECEIPT_IMAGE";
    protected static final String COLUMN_RECEIPT_IMAGE_ID = "RECEIPT_IMAGE_ID";
    protected static final String COLUMN_RECEIPT_IMAGE_DATA_LOCAL = "RECEIPT_IMAGE_DATA_LOCAL";
    protected static final String COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH = "RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH";
    protected static final String COLUMN_LOCATION_NAME = "LOCATION_NAME";
    protected static final String COLUMN_VENDOR_NAME = "VENDOR_NAME";
    protected static final String COLUMN_ENTRY_KEY = "ENTRY_KEY";
    protected static final String COLUMN_TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";
    protected static final String COLUMN_TRANSACTION_DATE = "TRANSACTION_DATE";
    protected static final String COLUMN_COMMENT = "COMMENT";
    protected static final String COLUMN_UPDATE_DATE = "UPDATE_DATE";
    protected static final String COLUMN_CREATE_DATE = "CREATE_DATE";
    protected static final String COLUMN_RESPONSE_CLIENT_LAST_UPDATE = "CLIENT_LAST_UPDATE";
    protected static final String COLUMN_VERB = "VERB";
    protected static final String COLUMN_SERVICE_ENDPOINT = "SERVICE_ENDPOINT";
    protected static final String COLUMN_BODY = "BODY";
    protected static final String COLUMN_STATUS = "STATUS";
    protected static final String COLUMN_DISPLAY_NAME = "DISPLAY_NAME";
    protected static final String COLUMN_OTHER = "OTHER";
    protected static final String COLUMN_CA_KEY = "CA_KEY";
    protected static final String COLUMN_CT_KEY = "CT_KEY";
    protected static final String COLUMN_EXPENSE_ENTRY_TYPE = "TYPE";
    protected static final String COLUMN_REPORT_TYPE = "REPORT_TYPE";
    protected static final String COLUMN_REPORT_HEADER = "REPORT_HEADER";
    protected static final String COLUMN_IS_DETAIL = "IS_DETAIL";
    protected static final String COLUMN_HEADER_KEY = "HEADER_KEY";
    protected static final String COLUMN_ITINERARY_LOCATOR = "ITINERARY_LOCATOR";
    protected static final String COLUMN_ITINERARY = "ITINERARY";
    protected static final String COLUMN_COMP_ID = "ID";
    protected static final String COLUMN_COMP_VALUE = "VALUE";
    protected static final String COLUMN_URI = "URI";
    protected static final String COLUMN_MIME_TYPE = "MIME_TYPE";

    public static final String COLUMN_PARENT_KEY = "PARENT_KEY";
    public static final String COLUMN_PARENT_NAME = "PARENT_NAME";
    public static final String COLUMN_FORM_KEY = "FORM_KEY";
    public static final String COLUMN_EXP_CODE = "EXP_CODE";
    public static final String COLUMN_ITEMIZATION_KEY = "ITEMIZATION_KEY";
    public static final String COLUMN_ITEMIZATION_TYPE = "ITEMIZATION_TYPE";
    public static final String COLUMN_ITEMIZATION_STYLE = "ITEMIZATION_STYLE";
    public static final String COLUMN_VENDOR_LIST_KEY = "VENDOR_LIST_KEY";
    public static final String COLUMN_SUPPORT_ATTENDEE = "SUPPORT_ATTENDEE";
    public static final String COLUMN_EDIT_ATN_AMT = "EDIT_ATN_AMT";
    public static final String COLUMN_EDIT_ATN_COUNT = "EDIT_ATN_COUNT";
    public static final String COLUMN_ALLOW_NO_SHOWS = "ALLOW_NO_SHOWS";
    public static final String COLUMN_DISPLAY_ATN_AMTS = "DISPLAY_ATN_AMTS";
    public static final String COLUMN_USER_ATN_DEFAULT = "USER_ATN_DEFAULT";
    public static final String COLUMN_HAS_POST_AMT_CALC = "HAS_POST_AMT_CALC";
    public static final String COLUMN_HAS_TAX_FORM = "HAS_TAX_FORM";
    public static final String COLUMN_EXP_TYPE_ACCESS = "EXP_TYPE_ACCESS";
    public static final String COLUMN_UNALLOW_ITEMIZATION = "UNALLOW_ITEMIZATION";
    public static final String COLUMN_UNALLOW_ATTENDEE = "UNALLOW_ATTENDEE";
    public static final String COLUMN_LAST_USED = "LAST_USED";
    public static final String COLUMN_USE_COUNT = "USE_COUNT";
    public static final String COLUMN_EXP_KEY = "EXP_KEY";
    public static final String COLUMN_EXP_NAME = "EXP_NAME";

    // Gov columns
    public static final String COLUMN_GOV_DOCNAME = "DOC_DETAIL_NAME";
    public static final String COLUMN_GOV_DOCTYPE = "DOC_DETAIL_TYPE";
    public static final String COLUMN_GOV_TRAVID = "DOC_DETAIL_TRAVELER_ID";
    public static final String COLUMN_GOV_TANUMBER = "DOC_DETAIL_TANUM";
    public static final String COLUMN_GOV_CURRENT_STATUS = "DOC_DETAIL_CURN_STATUS";
    public static final String COLUMN_GOV_PURPOSECODE = "DOC_DETAIL_PURPOSE_CODE";
    public static final String COLUMN_GOV_COMMENT = "DOC_DETAIL_COMMENT";
    public static final String COLUMN_GOV_IMAGE_ID = "IMAGE_ID";
    public static final String COLUMN_GOV_NON_REIMBURS_AMOUNT = "DOC_DETAIL_REIMBURSMENT";
    public static final String COLUMN_GOV_ADV_AMTREQ = "DOC_DETAIL_ADV_AMOUNTREQ";
    public static final String COLUMN_GOV_ADV_APPLIED = "DOC_DETAIL_ADV_AMOUNTAPPLIED";
    public static final String COLUMN_GOV_ADV_PAYTO_CARD = "DOC_DETAIL_PAYTOCARD";
    public static final String COLUMN_GOV_PAYTO_TRAVELER = "DOC_DETAIL_PAYTOTRAVELER";
    public static final String COLUMN_GOV_TOTAL_EST_AMT = "DOC_DETAIL_TOATAL_EST_AMT";
    public static final String COLUMN_GOV_EMISSIONS = "DOC_DETAIL_EMISSIONS";
    public static final String COLUMN_GOV_TRIP_BEGINDATE = "DOC_DETAIL_TRIP_BEGINDATE";
    public static final String COLUMN_GOV_TRIP_ENDDATE = "DOC_DETAIL_TRIP_ENDDATE";
    public static final String COLUMN_GOV_PERDIEM_LIST = "DOC_PERDIEM_LIST";
    public static final String COLUMN_GOV_ACC_CODELIST = "DOC_ACC_CODELIST";
    public static final String COLUMN_GOV_EXCEPTIONLIST = "DOC_EXCEPTIONLIST";
    public static final String COLUMN_GOV_EXPENSELIST = "DOC_EXPENSELIST";
    public static final String COLUMN_GOV_REASON_CODE = "DOC_REASONCODE";
    public static final String COLUMN_GOV_AUDIT = "DOC_AUDIT";
    public static final String COLUMN_GOV_STAMP_NAME = "STAMP_NAME";
    public static final String COLUMN_GOV_REQUIRED_REASON_CODE = "REQ_REASON_CODE";
    public static final String COLUMN_GOV_REQUIRED_REASON_USERID = "REQ_REASON_USESRID";
    public static final String COLUMN_GOV_BEHAVE_TITLE = "GOV_BEHAVIOR_TITLE";
    public static final String COLUMN_GOV_BEHAVE_MSG = "GOV_BEHAVIOR_TEXT";
    public static final String COLUMN_GOV_PRIVACY_TITLE = "GOV_PRIVACY_TITLE";
    public static final String COLUMN_GOV_PRIVACY_SHORT_MSG = "GOV_PRIVACY_TEXT_SHORT";
    public static final String COLUMN_GOV_PRIVACY_MSG = "GOV_PRIVACY_TEXT";
    public static final String COLUMN_GOV_WARNING_TITLE = "GOV_WARNING_TITLE";
    public static final String COLUMN_GOV_WARNING_SHORT_MSG = "GOV_WARNING_TEXT_SHORT";
    public static final String COLUMN_GOV_WARNING_MSG = "GOV_WARNING_TEXT";

    protected static final String WHERE_REPORT_KEY = COLUMN_REPORT_KEY + " = ?";
    protected static final String WHERE_REPORT_ENTRY_KEY = COLUMN_REPORT_ENTRY_KEY + " = ?";
    protected static final String WHERE_REPORT_KEY_NOT_IN = COLUMN_REPORT_KEY + " NOT IN (?)";
    protected static final String WHERE_USER_ID = COLUMN_USER_ID + " = ?";
    protected static final String WHERE_ID = COLUMN_ID + " = ?";
    protected static final String WHERE_ENTRY_KEY = COLUMN_ENTRY_KEY + " = ?";
    protected static final String WHERE_CLIENT_TRANSACTION_ID = COLUMN_CLIENT_TRANSACTION_ID + " = ?";
    protected static final String WHERE_CLIENT_REQUEST_ID = COLUMN_CLIENT_REQUEST_ID + " = ?";
    protected static final String WHERE_CT_KEY = COLUMN_CT_KEY + " = ?";
    protected static final String WHERE_USER_ID_AND_CT_KEY = WHERE_USER_ID + " AND " + WHERE_CT_KEY;
    protected static final String WHERE_REPORT_HEADER = COLUMN_REPORT_KEY + " = ? AND " + COLUMN_USER_ID + " = ? AND "
            + COLUMN_REPORT_TYPE + " = ? AND " + COLUMN_IS_DETAIL + " = ?";
    protected static final String WHERE_REPORT_ENTRY = COLUMN_HEADER_KEY + " = ? AND " + COLUMN_REPORT_KEY
            + " = ? AND " + COLUMN_REPORT_ENTRY_KEY + " = ? AND " + COLUMN_IS_DETAIL + " = ?";

    protected static final String WHERE_REPORT_HEADER_KEY = COLUMN_HEADER_KEY + " = ?";

    protected static final String WHERE_DELETE_REPORT_ENTRY_SQL = COLUMN_HEADER_KEY + " = ? AND " + COLUMN_USER_ID
            + " = ? AND " + COLUMN_REPORT_KEY + " = ? AND " + COLUMN_REPORT_ENTRY_KEY + " = ? AND " + COLUMN_IS_DETAIL
            + " = ?";

    protected static final String WHERE_USER_ID_AND_NON_LOCAL = COLUMN_USER_ID + " = ? AND " + COLUMN_STATUS
            + " <> 'NEW'";

    // Itinerary WHERE clauses.
    protected static final String WHERE_ITINERARY_SQL = COLUMN_USER_ID + " = ? AND " + COLUMN_ITINERARY_LOCATOR
            + " = ?";
    protected static final String WHERE_DELETE_ITINERARY_SQL = COLUMN_USER_ID + " = ? AND " + COLUMN_ITINERARY_LOCATOR
            + " = ?";

    protected static final String WHERE_UPDATE_EXPENSE_TYPE_ROW = COLUMN_USER_ID + " = ?" + " AND " + COLUMN_EXP_KEY
            + "= ?" + " AND " + COLUMN_POL_KEY + "= ?";

    protected static final String WHERE_UPDATE_EXPENSE_TYPE_ROW_USING_POLKEY = COLUMN_USER_ID + " = ?" + " AND "
            + COLUMN_POL_KEY + "= ?";

    protected static final String SCHEMA_CREATE_COM_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_COM_COMPONENT + " ("
            + COLUMN_COMP_ID + " INTEGER, " + COLUMN_COMP_VALUE + " BLOB)";

    protected static final String SCHEMA_CREATE_RECEIPT_SHARE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_RECEIPT_SHARE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COLUMN_URI + " TEXT, "
            + COLUMN_MIME_TYPE + " TEXT, " + COLUMN_FILE_NAME + " TEXT, " + COLUMN_DISPLAY_NAME + " TEXT, "
            + COLUMN_STATUS + " TEXT)";

    // The schema creation SQL for our database. Must be in execution order
    protected static final String[] SCHEMA_CREATE_SQL = {

            // Table holds the XML representation of server responses.
            "CREATE TABLE " + TABLE_RESPONSE + " (" + RESPONSE_ID + " INTEGER, " + // The response ID (Const.MSG_*_RESULT)
                    RESPONSE_CLIENT_LAST_UPDATE + " TEXT," + // The last time the data was retrieved from the server.
                    COLUMN_USER_ID + " TEXT," + // User ID of end-user when the response was received.
                    RESPONSE_SERVER_LAST_UPDATE + " TEXT," + // The last time the data was updated on the server.
                    RESPONSE_RESPONSE + " TEXT)", // The full XML response

            // Table holds summary/detail report header only objects serialized to XML.
            "CREATE TABLE " + TABLE_REPORT_HEADER + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                               // integer-based
                                                                                                               // key.
                    COLUMN_USER_ID + " TEXT," + // User ID associated with record.
                    COLUMN_REPORT_KEY + " TEXT," + // The report key
                    COLUMN_REPORT_TYPE + " TEXT," + // The report type, i.e., ACTIVE, APPROVAL
                    COLUMN_REPORT_HEADER + " TEXT," + // The XML representation of the report header.
                    COLUMN_IS_DETAIL + " TEXT," + // Whether the header is for a detail object.
                    RESPONSE_CLIENT_LAST_UPDATE + " TEXT" + // The last client update time.
                    ")",

            // Table holds summary/detail report entry objects serialized to XML.
            "CREATE TABLE " + TABLE_REPORT_ENTRY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                              // integer-based
                                                                                                              // key.
                    COLUMN_HEADER_KEY + " INTEGER," + // Foreign key into REPORT_HEADER.ID column.
                    COLUMN_USER_ID + " TEXT," + // User ID associated with record.
                    COLUMN_REPORT_KEY + " TEXT," + // The report key
                    COLUMN_REPORT_ENTRY_KEY + " TEXT," + // The report entry key.
                    COLUMN_IS_DETAIL + " TEXT," + // Whether the entry is for a detail object.
                    COLUMN_REPORT_ENTRY + " BLOB" + // The encrypted, GZip'd compressed report entry XML.
                    ")",

            // Table holds detailed itinerary objects serialized to XML.
            "CREATE TABLE " + TABLE_ITINERARY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                           // integer-based key.
                    COLUMN_USER_ID + " TEXT," + // User ID associated with record.
                    COLUMN_ITINERARY_LOCATOR + " TEXT," + // The itinerary locator.
                    COLUMN_ITINERARY + " BLOB," + // The encrypted, GZip'd compressed itinerary XML.
                    RESPONSE_CLIENT_LAST_UPDATE + " TEXT" + // The last client update time.
                    ")",

            // Table holds the XML body of an HTTP POST request for a report approval.
            "CREATE TABLE " + TABLE_REPORT_SUBMIT_APPROVE + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                                       // integer-based
                                                                                                                       // key.
                    COLUMN_USER_ID + " TEXT," + // User ID of end-user when the report detail was retrieved.
                    COLUMN_REPORT_KEY + " TEXT," + // The report key.
                    COLUMN_CLIENT_TRANSACTION_ID + " INTEGER," + // Client-generated transaction id.
                    COLUMN_REQUEST + " TEXT" + // The XML of the request.
                    ")",

            // Table holds the XML body of an HTTP POST request for a report rejection.
            "CREATE TABLE " + TABLE_REPORT_SUBMIT_REJECT + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                                      // integer-based
                                                                                                                      // key.
                    COLUMN_USER_ID + " TEXT," + // User ID of end-user when the report detail was retrieved.
                    COLUMN_REPORT_KEY + " TEXT," + // Report key.
                    COLUMN_CLIENT_TRANSACTION_ID + " INTEGER," + // Client-generated transaction id.
                    COLUMN_REQUEST + " TEXT" + // The XML of the request.
                    ")",

            // Table holds the XML representation of the set of expense types.
            "CREATE TABLE " + TABLE_EXPENSE_TYPE + " (" + COLUMN_USER_ID + " TEXT," + // User ID of end-user when expense types
                                                                                      // was retrieved.
                    COLUMN_EXP_KEY + " TEXT," + // Expense type key.
                    COLUMN_EXP_NAME + " TEXT," + // Expense name.
                    COLUMN_POL_KEY + " INTEGER," + // Policy key of expense type
                    COLUMN_PARENT_KEY + " TEXT," + // Parent key of expense type
                    COLUMN_PARENT_NAME + " TEXT," + // Parent name of expense type
                    COLUMN_FORM_KEY + " INTEGER," + // Form key of expense type
                    COLUMN_EXP_CODE + " TEXT," + // Expense code of expense type
                    COLUMN_ITEMIZATION_KEY + " INTEGER," + // itemization form key of expense type
                    COLUMN_ITEMIZATION_TYPE + " TEXT," + // itemization form type of expense type
                    COLUMN_ITEMIZATION_STYLE + " TEXT," + // itemization form style of expense type
                    COLUMN_VENDOR_LIST_KEY + " INTEGER," + // vendor list of expense type
                    COLUMN_SUPPORT_ATTENDEE + " INTEGER," + // whether the expense type supports attendees
                    COLUMN_EDIT_ATN_AMT + " INTEGER," + // whether the expense type permits editing of attendee amounts
                    COLUMN_EDIT_ATN_COUNT + " INTEGER," + // whether the expense type permits editing of attendee counts
                    COLUMN_ALLOW_NO_SHOWS + " INTEGER," + // whether no shows are permitted.
                    COLUMN_DISPLAY_ATN_AMTS + " INTEGER," + // whether attendee amounts should be displayed.
                    COLUMN_USER_ATN_DEFAULT + " INTEGER," + // whether the user can be the default attendee.
                    COLUMN_HAS_POST_AMT_CALC + " INTEGER," + // whether posted amount is calculated.
                    COLUMN_HAS_TAX_FORM + " INTEGER," + // whether TAX form available.
                    COLUMN_EXP_TYPE_ACCESS + " TEXT," + // the type of access.
                    COLUMN_UNALLOW_ITEMIZATION + " TEXT," + // unallowItemization of expense type saparated by ','
                    COLUMN_UNALLOW_ATTENDEE + " TEXT," + // unallowAttendeeType of expense type saparated by ','
                    COLUMN_LAST_USED + " TEXT," + // Client retrieval data retrieval time.
                    COLUMN_USE_COUNT + " INTEGER," + // whether the user can be the default attendee.
                    "PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_EXP_KEY + ", " + COLUMN_POL_KEY + ")" + ")",

            // Table holds the XML representation of the set of reimbursement currencies.
            "CREATE TABLE " + TABLE_CURRENCY + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary integer-based
                                                                                                          // key.
                    COLUMN_USER_ID + " TEXT," + // User ID of end-user when currencies where retrieved.
                    COLUMN_CURRENCY + " TEXT," + // XML representation of the currency list.
                    RESPONSE_CLIENT_LAST_UPDATE + " TEXT" + // Client retrieval data retrieval time.
                    ")",

            // TODO Currency MRU
            // Table holds the XML representation of the set of currency MRU
            "CREATE TABLE IF NOT EXISTS " + TABLE_MRU + " (" + COLUMN_USER_ID + " TEXT,"
                    + // User ID of end-user when currencies where retrieved.
                    COLUMN_FIELD_ID + " TEXT,"
                    + // Field ID of list.
                    COLUMN_FIELD_VALUE + " TEXT,"
                    + // Field Value (likey+licode+litext)
                    COLUMN_LICODE + " TEXT," + COLUMN_USE_COUNT + " INTEGER," + COLUMN_LAST_USED + " TEXT,"
                    + "PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_FIELD_ID + ", " + COLUMN_LICODE + ")" + ")",

            // Table holds the mobile expense entries.
            "CREATE TABLE IF NOT EXISTS " + TABLE_EXPENSE_ENTRY + " (" + COLUMN_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary integer-based key.
                    COLUMN_USER_ID + " TEXT," + // User ID associated with this record.
                    COLUMN_CRN_CODE + " TEXT," + // Currency code.
                    COLUMN_EXP_KEY + " TEXT," + // Expense type key.
                    COLUMN_EXP_NAME + " TEXT," + // Expense name.
                    COLUMN_RECEIPT_IMAGE_ID + " TEXT," + // Receipt image id.
                    COLUMN_RECEIPT_IMAGE + " TEXT," + // Whether the mobile entry has a receipt image.
                    COLUMN_RECEIPT_IMAGE_DATA_LOCAL + " TEXT," + // Whether the mobile entry has local receipt image data.
                    COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + " TEXT," + // Contains the filename within the 'receipts' local
                    // directory.
                    COLUMN_LOCATION_NAME + " TEXT," + // Location name.
                    COLUMN_VENDOR_NAME + " TEXT," + // Vendor name.
                    COLUMN_EXPENSE_ENTRY_TYPE + " TEXT," + // Expense entry type. See
                    // 'com.concur.mobile.data.expense.Expense.ExpenseEntryType'.
                    COLUMN_ENTRY_KEY + " TEXT," + // Expense entry key.
                    COLUMN_CA_KEY + " TEXT," + // Card account key. (Will be null for corporate card transactions).
                    COLUMN_CT_KEY + " TEXT," + // Card transaction key. (Used for personal/corporate card transactions).
                    COLUMN_TRANSACTION_AMOUNT + " TEXT," + // Transaction amount.
                    COLUMN_TRANSACTION_DATE + " TEXT," + // Transaction date.
                    COLUMN_COMMENT + " TEXT," + // Comment
                    COLUMN_UPDATE_DATE + " TEXT," + // Last save time.
                    COLUMN_CREATE_DATE + " TEXT," + // Creation time.
                    COLUMN_STATUS + " TEXT" + // Status - See 'com.concur.mobile.data.expense.MobileEntryStatus'.
                    ")",

            // Table holds server-bound HTTP requests.
            "CREATE TABLE " + TABLE_HTTP_REQUEST + " (" + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + // Primary
                                                                                                              // integer-based
                                                                                                              // key.
                    COLUMN_USER_ID + " TEXT," + // User ID associated with this record.
                    COLUMN_VERB + " TEXT," + // HTTP verb, i.e., POST, PUT, etc.
                    COLUMN_SERVICE_ENDPOINT + " TEXT," + // The service end-point, including any arguments.
                    COLUMN_BODY + " TEXT," + // The body of the request.
                    COLUMN_CLIENT_REQUEST_ID + " INTEGER," + // The request id (the msg.what).
                    COLUMN_CLIENT_TRANSACTION_ID + " TEXT," + // The client-side transaction id, unique message ID.
                    COLUMN_OTHER + " TEXT" + // An opaque text column that can be used by clients of this table.
                    ")",

            // Holds in-process card changes
            "CREATE TABLE " + TABLE_CARD_CHANGES + " (" + "PCT_KEY TEXT," + "STATUS TEXT)",

            // GOV TABLES
            // Table holds the XML representation of the set of gov doc detail.
            "CREATE TABLE " + TABLE_GOV_DOCUMENT_DETAIL + " (" + COLUMN_USER_ID + " TEXT," + COLUMN_GOV_DOCNAME
                    + " TEXT," + COLUMN_GOV_DOCTYPE + " TEXT," + COLUMN_GOV_TRAVID + " TEXT," + COLUMN_GOV_TANUMBER
                    + " TEXT," + COLUMN_GOV_CURRENT_STATUS + " TEXT," + COLUMN_GOV_PURPOSECODE + " TEXT,"
                    + COLUMN_GOV_COMMENT + " TEXT," + COLUMN_GOV_IMAGE_ID + " TEXT," + COLUMN_GOV_NON_REIMBURS_AMOUNT
                    + " TEXT," + COLUMN_GOV_ADV_AMTREQ + " TEXT," + COLUMN_GOV_ADV_APPLIED + " TEXT,"
                    + COLUMN_GOV_ADV_PAYTO_CARD + " TEXT," + COLUMN_GOV_PAYTO_TRAVELER + " TEXT,"
                    + COLUMN_GOV_TOTAL_EST_AMT + " TEXT," + COLUMN_GOV_EMISSIONS + " TEXT," + COLUMN_GOV_TRIP_BEGINDATE
                    + " TEXT," + COLUMN_GOV_TRIP_ENDDATE + " TEXT," + COLUMN_GOV_PERDIEM_LIST + " BLOB,"
                    + COLUMN_GOV_ACC_CODELIST + " BLOB," + COLUMN_GOV_EXCEPTIONLIST + " BLOB," + COLUMN_GOV_EXPENSELIST
                    + " BLOB," + COLUMN_GOV_REASON_CODE + " BLOB," + COLUMN_GOV_AUDIT + " BLOB," + COLUMN_LAST_USED
                    + " TEXT,"
                    + // whether the user can be the default attendee.
                    "PRIMARY KEY (" + COLUMN_USER_ID + ", " + COLUMN_GOV_TRAVID + ", " + COLUMN_GOV_DOCNAME + ", "
                    + COLUMN_GOV_DOCTYPE + ")" + ")",

            // Table holds the XML representation of the set of stamp doc.
            "CREATE TABLE " + TABLE_GOV_DOC_STAMP_REQ_REASON + " (" + COLUMN_USER_ID + " TEXT,"
                    + COLUMN_GOV_REQUIRED_REASON_USERID + " TEXT," + COLUMN_GOV_DOCNAME + " TEXT," + COLUMN_GOV_DOCTYPE
                    + " TEXT," + COLUMN_GOV_TRAVID + " TEXT," + COLUMN_GOV_STAMP_NAME + " TEXT,"
                    + COLUMN_GOV_REQUIRED_REASON_CODE + " INTEGER,"
                    + // whether the user can be the default attendee.
                    "PRIMARY KEY (" + COLUMN_GOV_REQUIRED_REASON_USERID + ", " + COLUMN_GOV_TRAVID + ", "
                    + COLUMN_GOV_STAMP_NAME + ")" + ")",

            // Table holds the XML representation of the set gov messages.
            "CREATE TABLE IF NOT EXISTS " + TABLE_GOV_MESSAGES + " (" + COLUMN_ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT," + COLUMN_GOV_BEHAVE_TITLE + " TEXT,"
                    + COLUMN_GOV_BEHAVE_MSG + " TEXT," + COLUMN_GOV_PRIVACY_TITLE + " TEXT,"
                    + COLUMN_GOV_PRIVACY_SHORT_MSG + " TEXT," + COLUMN_GOV_PRIVACY_MSG + " TEXT,"
                    + COLUMN_GOV_WARNING_TITLE + " TEXT," + COLUMN_GOV_WARNING_SHORT_MSG + " TEXT,"
                    + COLUMN_GOV_WARNING_MSG + " TEXT," + COLUMN_LAST_USED + " TEXT" + ")",

            // Communication component table
            SCHEMA_CREATE_COM_TABLE,

            // Table holds Receipt URI's to be shared with the Receipt Store.
            SCHEMA_CREATE_RECEIPT_SHARE_TABLE

    };

    // The schema deletion SQL for our database. Must be in execution order
    protected static final String[] SCHEMA_DELETE_SQL = {

            // Table holds the XML representation of server responses.
            "DROP TABLE IF EXISTS " + TABLE_RESPONSE + ";",

            // Table holds the XML representation of a report details.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_DETAIL + ";",

            // Table holds the XML representation of summary/detail report header objects.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_HEADER + ";",

            // Table holds the XML representation of summary/detail report entry objects.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_ENTRY + ";",

            // Table holds the XML representation of a detailed itinerary.
            "DROP TABLE IF EXISTS " + TABLE_ITINERARY + ";",

            // Table holds the XML representation of the list of reports to approve.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_LIST + ";",

            // Table holds the XML body of an HTTP POST request for a report approval.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_SUBMIT_APPROVE + ";",

            // Table holds the XML body of an HTTP POST request for a report rejection.
            "DROP TABLE IF EXISTS " + TABLE_REPORT_SUBMIT_REJECT + ";",

            // Table holds the XML representation of the set of expense types.
            "DROP TABLE IF EXISTS " + TABLE_EXPENSE_TYPE + ";",

            // Table holds the XML representation of the set of reimbursement currencies.
            "DROP TABLE IF EXISTS " + TABLE_CURRENCY + ";",

            // Table holds the mobile expense entries.
            // This table is no longer deleted because it holds offline data. If it changes, it must be manually handled.
            // "DROP TABLE IF EXISTS " + TABLE_EXPENSE_ENTRY + ";",

            // Table holds server-bound HTTP requests.
            "DROP TABLE IF EXISTS " + TABLE_HTTP_REQUEST + ";",

            // Holds in-process card changes
            "DROP TABLE IF EXISTS " + TABLE_CARD_CHANGES + ";",

            // Holds split smart expense keys.
            "DROP TABLE IF EXISTS " + TABLE_SPLIT_SMART_EXPENSE + ";",

            // MRU
            "DROP TABLE IF EXISTS " + TABLE_MRU + ";",

            // Holds Gov document details.
            "DROP TABLE IF EXISTS " + TABLE_GOV_DOCUMENT_DETAIL + ";",

            // Holds Gov document stamp req reasons.
            "DROP TABLE IF EXISTS " + TABLE_GOV_DOC_STAMP_REQ_REASON + ";"

    // Communication components
    // This table is no longer deleted. If it changes, it must be manually handled.
    // "DROP TABLE IF EXISTS " + TABLE_COM_COMPONENT + ";",

    // Table holds the receipt share URI list.
    // "DROP TABLE IF EXISTS " + TABLE_RECEIPT_SHARE + ";"

    };

    // The SQL we need. Centralized here for maintenance purposes and to keep the rest of
    // the code clean. Not concatenated with the column name constants because that just becomes
    // insane to read.
    protected static final String DELETE_RESPONSE_SQL = "DELETE FROM RESPONSE WHERE ID = ? AND USER_ID = ?";
    protected static final String INSERT_RESPONSE_SQL = "INSERT INTO RESPONSE (ID, USER_ID, CLIENT_LAST_UPDATE, RESPONSE) VALUES (?, ?, ?, ?)";
    protected static final String UPDATE_RESPONSE_SQL = "UPDATE RESPONSE SET RESPONSE = ? WHERE ID = ? AND USER_ID = ?";
    protected static final String LOAD_RESPONSE_SQL = "SELECT RESPONSE FROM RESPONSE WHERE ID = ? AND USER_ID = ?";
    protected static final String RESPONSE_LAST_UPDATE_SQL = "SELECT CLIENT_LAST_UPDATE FROM RESPONSE WHERE ID = ? AND USER_ID = ?";
    protected static final String UPDATE_RESPONSE_TIME_SQL = "UPDATE RESPONSE SET CLIENT_LAST_UPDATE = ? WHERE ID = ? AND USER_ID = ?";

    // Server communication components
    protected static final String INSERT_COM_COMPONENT_SQL = "INSERT INTO COM_COMPONENT (ID, VALUE) VALUES (?, ?)";
    protected static final String UPDATE_COM_COMPONENT_SQL = "UPDATE COM_COMPONENT SET VALUE = ? WHERE ID = ?";
    protected static final String LOAD_COM_COMPONENT_SQL = "SELECT VALUE FROM COM_COMPONENT WHERE ID = ?";
    protected static final String DELETE_COM_COMPONENT_SQL = "DELETE FROM COM_COMPONENT WHERE ID = ?";

    // Expense related SQL commands.

    // Report Header table SQL statements.
    protected static final String SELECT_REPORT_HEADER_SQL = "SELECT REPORT_HEADER, CLIENT_LAST_UPDATE FROM REPORT_HEADER "
            + "WHERE REPORT_TYPE = ? AND USER_ID = ? AND REPORT_KEY = ? AND IS_DETAIL = ?";
    protected static final String SELECT_REPORT_HEADERS_NO_XML_SQL = "SELECT REPORT_KEY, CLIENT_LAST_UPDATE FROM REPORT_HEADER "
            + "WHERE REPORT_TYPE = ? AND USER_ID = ? AND IS_DETAIL = ?";
    protected static final String SELECT_REPORT_HEADERS_SQL = "SELECT REPORT_KEY, REPORT_HEADER, CLIENT_LAST_UPDATE FROM REPORT_HEADER "
            + "WHERE REPORT_TYPE = ? AND USER_ID = ? AND IS_DETAIL = ?";
    protected static final String SELECT_REPORT_HEADER_ID_SQL = "SELECT ID FROM REPORT_HEADER WHERE REPORT_TYPE = ? AND REPORT_KEY = ? AND USER_ID = ? AND IS_DETAIL = ?";

    // Report entry table SQL statements.
    protected static final String SELECT_REPORT_ENTRIES_SQL = "SELECT REPORT_ENTRY, IS_DETAIL FROM REPORT_ENTRY "
            + "WHERE HEADER_KEY = ?";
    protected static final String SELECT_REPORT_ENTRY_SQL = "SELECT REPORT_ENTRY, IS_DETAIL FROM REPORT_ENTRY "
            + "WHERE HEADER_KEY = ? AND REPORT_KEY = ? AND REPORT_ENTRY_KEY = ? AND USER_ID = ?";

    // Itinerary SQL statements.
    protected static final String SELECT_ITINERARY_SQL = "SELECT " + COLUMN_ITINERARY + ", "
            + RESPONSE_CLIENT_LAST_UPDATE + " FROM " + TABLE_ITINERARY + " WHERE " + COLUMN_USER_ID + " = ? AND "
            + COLUMN_ITINERARY_LOCATOR + " = ?";

    // ReceiptShare SQL statements.
    protected static final String SELECT_RECEIPT_SHARE_SQL = "SELECT " + COLUMN_URI + ", " + COLUMN_MIME_TYPE + ", "
            + COLUMN_FILE_NAME + ", " + COLUMN_DISPLAY_NAME + ", " + COLUMN_STATUS + " FROM " + TABLE_RECEIPT_SHARE;
    protected static final String DELETE_RECEIPT_SHARE_SQL = "DELETE FROM " + TABLE_RECEIPT_SHARE + " WHERE "
            + COLUMN_URI + " = ?";
    protected static final String INSERT_RECEIPT_SHARE_SQL = "INSERT INTO " + TABLE_RECEIPT_SHARE + "(" + COLUMN_URI
            + "," + COLUMN_MIME_TYPE + "," + COLUMN_FILE_NAME + "," + COLUMN_DISPLAY_NAME + COLUMN_STATUS
            + ") VALUES (?,?,?,?,?)";
    protected static final String WHERE_RECEIPT_SHARE_URI = COLUMN_URI + " = ?";
    protected static final String WHERE_RECEIPT_SHARE_FILE = COLUMN_FILE_NAME + " = ?";
    protected static final String WHERE_RECEIPT_SHARE_STATUS = COLUMN_STATUS + " = ?";
    protected static final String COUNT_OFFLINE_RECEIPTS_SQL = "SELECT COUNT(*) FROM " + TABLE_RECEIPT_SHARE
            + " WHERE " + COLUMN_STATUS + " = 'HOLD'";

    protected static final String SELECT_ITINERARY_UPDATE_TIME_SQL = "SELECT " + RESPONSE_CLIENT_LAST_UPDATE + " FROM "
            + TABLE_ITINERARY + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_ITINERARY_LOCATOR + " = ?";

    // Report Approval Submission Table.
    protected static final String SELECT_REPORT_KEY_SUBMIT_APPROVE_SQL = "SELECT REPORT_KEY FROM REPORT_SUBMIT_APPROVE WHERE "
            + COLUMN_USER_ID + " = ?";

    // Report Approval Submission Table.
    protected static final String SELECT_REPORT_KEY_SUBMIT_SQL = "SELECT " + COLUMN_OTHER + " FROM "
            + TABLE_HTTP_REQUEST + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_CLIENT_REQUEST_ID + " = ?";

    // Report Rejected Submission Table.
    protected static final String INSERT_REPORT_SUBMIT_REJECT_SQL = "INSERT INTO REPORT_SUBMIT_REJECT (REPORT_KEY,CLIENT_TRANSACTION_ID,REQUEST_TEXT) VALUES(?,?,?)";
    protected static final String DELETE_REPORT_SUBMIT_REJECT_SQL = "DELETE FROM REPORT_SUBMIT_REJECT WHERE REPORT_KEY = ?";
    protected static final String SELECT_ALL_REPORT_SUBMIT_REJECT_SQL = "SELECT REPORT_KEY, CLIENT_TRANSACTION_ID, REQUEST FROM REPORT_SUBMIT_REJECT";
    protected static final String SELECT_REPORT_KEY_SUBMIT_REJECT_SQL = "SELECT REPORT_KEY FROM REPORT_SUBMIT_REJECT WHERE "
            + COLUMN_USER_ID + " = ?";

    // Expense Types SQL statements.
    protected static final String SELECT_EXPENSE_TYPE_SQL = "SELECT EXPENSE_TYPE, CLIENT_LAST_UPDATE FROM EXPENSE_TYPE WHERE USER_ID = ?";

    protected static final String SELECT_EXPENSE_TYPE_USING_USERID = "SELECT * FROM "
            + MobileDatabaseHelper.TABLE_EXPENSE_TYPE + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_POL_KEY
            + " =?";

    protected static final String SELECT_EXPENSE_TYPE_USING_COMPOUNDKEY = "SELECT * FROM "
            + MobileDatabaseHelper.TABLE_EXPENSE_TYPE + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_EXP_KEY
            + "= ?" + " AND " + COLUMN_POL_KEY + "= ?";

    protected static final String SELECT_EXPENSE_TYPE_USING_USER_ID_POL_KEY = "SELECT * FROM "
            + MobileDatabaseHelper.TABLE_EXPENSE_TYPE + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_POL_KEY
            + "= ?";

    protected static final String SELECT_EXPENSE_TYPE_USING_USER_ID_POL_KEY_ORDER = "SELECT * FROM "
            + MobileDatabaseHelper.TABLE_EXPENSE_TYPE + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_POL_KEY
            + "= ? AND " + COLUMN_PARENT_KEY + " IS NOT NULL AND " + COLUMN_PARENT_NAME + " IS NOT NULL";

    protected static final String SELECT_POL_KEY_COUNT = "SELECT " + COLUMN_USE_COUNT + " FROM "
            + MobileDatabaseHelper.TABLE_EXPENSE_TYPE + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_EXP_KEY
            + "= ?" + " AND " + COLUMN_POL_KEY + "= ?";

    // Currency Types SQL statements.
    protected static final String SELECT_CURRENCY_TYPE_SQL = "SELECT " + COLUMN_CURRENCY + ", "
            + RESPONSE_CLIENT_LAST_UPDATE + " FROM " + TABLE_CURRENCY + " WHERE " + COLUMN_USER_ID + " = ?";

    // Expense entry SQL statements.
    protected static final String SELECT_EXPENSE_ENTRY_SQL = "SELECT " + COLUMN_ID + ", " + COLUMN_USER_ID + ", "
            + COLUMN_CRN_CODE + ", " + COLUMN_EXP_KEY + ", " + COLUMN_EXP_NAME + ", " + COLUMN_RECEIPT_IMAGE_ID + ", "
            + COLUMN_RECEIPT_IMAGE + ", " + COLUMN_RECEIPT_IMAGE_DATA_LOCAL + ", "
            + COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + ", " + COLUMN_LOCATION_NAME + ", " + COLUMN_VENDOR_NAME
            + ", " + COLUMN_EXPENSE_ENTRY_TYPE + ", " + COLUMN_ENTRY_KEY + ", " + COLUMN_CA_KEY + ", " + COLUMN_CT_KEY
            + ", " + COLUMN_TRANSACTION_AMOUNT + ", " + COLUMN_TRANSACTION_DATE + ", " + COLUMN_COMMENT + ", "
            + COLUMN_UPDATE_DATE + ", " + COLUMN_CREATE_DATE + ", " + COLUMN_STATUS + " FROM " + TABLE_EXPENSE_ENTRY
            + " WHERE " + COLUMN_USER_ID + " = ?";
    public static final String SELECT_EXPENSE_ENTRY_BY_STATUS_SQL = SELECT_EXPENSE_ENTRY_SQL + " AND " + COLUMN_STATUS
            + " = ?";
    protected static final String SELECT_EXPENSE_RECEIPT_IMAGE_DATA = "SELECT "
            + COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH + " FROM " + TABLE_EXPENSE_ENTRY + " WHERE " + COLUMN_ID
            + " = ?";
    protected static final String SELECT_MOBILE_EXPENSE_RECEIPT_REFERENCE_COUNT_SQL = "SELECT " + COLUMN_ID + " FROM "
            + TABLE_EXPENSE_ENTRY + " WHERE " + COLUMN_ID + " != ? AND " + COLUMN_RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH
            + " = ?";
    protected static final String COUNT_OFFLINE_EXPENSES_SQL = "SELECT COUNT(*) FROM " + TABLE_EXPENSE_ENTRY
            + " WHERE " + COLUMN_USER_ID + " = ? AND " + COLUMN_STATUS + " = 'NEW'";

    // Expense entries.
    protected static final String LOAD_EXPENSE_LIST_SQL = "SELECT RESPONSE, CLIENT_LAST_UPDATE FROM RESPONSE WHERE ID = ? AND USER_ID = ?";

    // Card transactions
    protected static final String INSERT_CARD_TRANSACTION_SQL = "INSERT INTO CARD_TRANSACTION_CHANGES (PCT_KEY, STATUS) VALUES(?, ?)";
    protected static final String SELECT_CARD_TRANSACTIONS_SQL = "SELECT PCT_KEY, STATUS FROM CARD_TRANSACTION_CHANGES WHERE STATUS = ?";
    protected static final String CLEAR_CARD_TRANSACTIONS_SQL = "DELETE FROM CARD_TRANSACTION_CHANGES WHERE STATUS = ?";

    // Gov Doc
    protected static final String SELECT_GOV_DOC_DETAIL = "SELECT * FROM " + TABLE_GOV_DOCUMENT_DETAIL + " WHERE "
            + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_GOV_TRAVID + "= ? AND " + COLUMN_GOV_DOCNAME + "= ? AND "
            + COLUMN_GOV_DOCTYPE + "= ? ";

    protected static final String WHERE_UPDATE_DELETE_DOC_DETAIL = COLUMN_USER_ID + " = ?" + " AND "
            + COLUMN_GOV_TRAVID + "= ? AND " + COLUMN_GOV_DOCNAME + "= ? AND " + COLUMN_GOV_DOCTYPE + "= ? ";

    protected static final String WHERE_INSERT_STAMP_DOC_REQ_INFO = COLUMN_GOV_REQUIRED_REASON_USERID + " = ?"
            + " AND " + COLUMN_GOV_TRAVID + "= ? AND " + COLUMN_GOV_STAMP_NAME + "= ?";

    protected static final String SELECT_STAMP_DOC_REQ_INFO = "SELECT * FROM "
            + MobileDatabaseHelper.TABLE_GOV_DOC_STAMP_REQ_REASON + " WHERE " + COLUMN_GOV_REQUIRED_REASON_USERID
            + " = ?" + " AND " + COLUMN_GOV_TRAVID + "= ? AND " + COLUMN_GOV_STAMP_NAME + "= ?";

    protected static final String SELECT_GOV_MESGS = "SELECT * FROM " + MobileDatabaseHelper.TABLE_GOV_MESSAGES;

    // TODO MRU select
    protected static final String SELECT_MRU_FIELD_VALUE = "SELECT * FROM " + MobileDatabaseHelper.TABLE_MRU
            + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_FIELD_ID + "= ?";

    // TODO MRU select
    protected static final String SELECT_MRU_FIELD_ID_CODE = "SELECT * FROM " + MobileDatabaseHelper.TABLE_MRU
            + " WHERE " + COLUMN_USER_ID + " = ?" + " AND " + COLUMN_LICODE + "= ?" + " AND " + COLUMN_FIELD_ID + "= ?";

    // TODO MRU WHERE
    protected static final String WHERE_UPDATE_FIELD_VALUE_CODE = COLUMN_USER_ID + " = ?" + " AND " + COLUMN_FIELD_ID
            + "= ?" + " AND " + COLUMN_LICODE + "= ?";

    protected static final String DATABASE_NAME = "MobileDatabase.db";
    protected static final int DATABASE_VERSION = 18;

    // DB History
    // DATABASE_VERSION = 1; // Original unencrypted version
    // DATABASE_VERSION = 2; // First encrypted version. Addition of SPLIT_SMART_EXPENSE table.
    // DATABASE_VERSION = 3; // Modification of mobile entry amount encryption (convert to double)
    // DATABASE_VERSION = 4; // Added 'USER_ID' column to the 'REPORT_DETAIL' table.
    // DATABASE_VERSION = 5; // Dropped the 'REPORT_DETAIL', 'REPORT_LIST_TABLE tables,
    // added the 'REPORT_HEADER' and 'REPORT_ENTRY' tables.
    // DATABASE_VERSION = 6; // Fix for REPORT_ENTRY XML containing un-escaped XML characters in business data.
    // DATABASE_VERSION = 7; // Added 'RECEIPT_IMAGE_ID' column in table 'EXPENSE_ENTRY'.
    // DATABASE_VERSION = 8; // Added 'ITINERARY' table to store full single itineraries.
    // DATABASE_VERSION = 9; // Added COM_COMPONENT table to store server communication components
    // DATABASE_VERSION = 10; // Added RECEIPT_SHARE table to store a list of receipt UThe reaso:RI's to be shared with the
    // receipt store.
    // DATABASE_VERSION = 11/12; // Inadvertant commit of version 11 with no upgrade path! Set to '12' such that a hotfix will
    // force an upgrade.

    // DATABASE_VERSION = 13;
    // 1. New version required for MRU. MOB-8452
    // 2. MOB-10765 (Addition of STATUS column in RECEIPT_SHARE table)

    // DATABASE_VERSION = 14;
    // New version required for Gov doc detail.
    // for Gov document stamp process.
    // for Gov Privacy Act Notice.
    // Changed 'EXP_KEY'/'PARENT_KEY' column data type (table EXPENSE_TYPE) from 'INTEGER' to 'TEXT' (MOB-11732).

    // DATABASE_VERSION = 15;
    // New version to force a hot-fix for issue MOB-13008

    // DATABASE_VERSION = 16
    // New version required for MOB-11451
    // Added support for MRU: TABLE_MRU;

    // DATABASE_VERSION = 17
    // New version required for MOB-13592
    // Converted COLUMN_ITINERARY_LOCATOR from INTEGER to TEXT;

    // DATABASE_VERSION = 18
    // New version required for MOB-14015
    // VAT Form

    // DATABASE_VERSION = 19
    // Removed SPLIT_SMART_EXPENSE Table

    protected Context context;

    protected MobileDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.v(Const.LOG_TAG, "Creating schema (while ignoring extant tables)");
        for (int i = 0; i < SCHEMA_CREATE_SQL.length; i++) {
            db.execSQL(SCHEMA_CREATE_SQL[i]);
        }
    }

    @Override
    // //////////////////////////////////////////////////////////////////////////////////////
    //
    // Description of DB migration history from an email thread
    //
    // At the very beginning, we didn’t do any actual preservation/migration of data. The whole database, as a single statement I
    // believe, was dropped. We then recreated everything using the individual create schema strings for each table
    // (SCHEMA_CREATE_SQL[]). That happened way down in ‘case 2’.
    //
    // At some point, we had to move login/password hashes out of the prefs file and into the database (COM_COMPONENT table). At
    // that point we could no longer just drop the entire database because users would have to login again after every upgrade. We
    // created another set of SQL strings, SCHEMA_DELETE_SQL, to match up with the original create SQL. We then stopped doing the
    // atomic drop of the entire DB and just executed all the delete SQL statements for the tables (replacing the code in
    // 'case 2'). There is no SCHEMA_DELETE_SQL[] for the COM_COMPONENT table so it is never dropped (see ‘case 12’). Any
    // potentially not dropped table uses the ‘IF NOT EXISTS’ clause in its create SQL so that it is re-runnable.
    //
    // So, at this point, we have a mechanism to selectively not drop tables. Once the decision is made to not drop a table it is
    // a permanent decision and we should never reverse it. The delete SQL for that table is removed and any manual (re-runnable)
    // migration code is created. This has been done for receipt sharing (ReceiptShareSchemaUpgradeAction). These schema upgrade
    // classes need to make sure to handle upgrades from any DB version (but that is usually not a big deal since we don’t really
    // have a history of migrated tables to worry about).
    //
    // //////////////////////////////////////////////////////////////////////////////////////
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.v(Const.LOG_TAG, "Upgrading database from " + oldVersion + " to " + newVersion);

        switch (newVersion) {
        case 19:
            // Fall through to a normal reset.
        case 18:
            // Fall through to a normal reset.
        case 17:
            // Fall through to a normal reset.
        case 16:
        case 15:
            // Fall through to a normal reset.
            // NOTE: As of this point, RECEIPT_SHARE table is not dropped anymore.
            if (oldVersion == 12 || oldVersion == 14) {
                // NOTE: Hard-coding 'oldVersion' to '12' and 'newVersion' to '13' to force the upgrade path
                // as though the end-user was upgrading from 12 -> 13, this will result in the 'STATUS'
                // column being added.
                ReceiptShareSchemaUpgradeAction updateAction = new ReceiptShareSchemaUpgradeAction(db, 12, 13);
                if (!updateAction.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: upgrading receipt share table from version "
                            + oldVersion + " to version " + newVersion + " failed!");
                }
            }
        case 13:
            // Fall through to a normal reset.
            // NOTE: As of this point, RECEIPT_SHARE table is not dropped anymore.
            if (oldVersion == 12) {
                ReceiptShareSchemaUpgradeAction updateAction = new ReceiptShareSchemaUpgradeAction(db, oldVersion,
                        newVersion);
                if (!updateAction.upgrade()) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onUpgrade: upgrading receipt share table from version "
                            + oldVersion + " to version " + newVersion + " failed!");
                }
            }
        case 14:
        case 12:
            // Fall through to a normal reset.
            // NOTE: As of this point, COM_COMPONENT is not dropped anymore. If it changes then a manual modification routine
            // must be created and that routine must preserve data.
        case 11:
            // Fall through to a normal reset.
        case 10:
            // Fall through to a normal reset.
        case 9:
            // Fall through to a normal reset.
        case 8:
            // Fall through to a normal reset.
        case 7:
            // Fall through to a normal reset.
        case 6:
            // Fall through to a normal reset.
        case 5:
            // Fall through to a normal reset.
        case 4:
            // Fall through to a normal reset.
        case 3:
            // Fall through to a normal reset.
        case 2:
            // Drop all the tables that do not need to be migrated. These are tables that are easily reloadable from the server.
            for (int i = 0; i < SCHEMA_DELETE_SQL.length; i++) {
                db.execSQL(SCHEMA_DELETE_SQL[i]);
            }

            // Recreate all tables while ignoring any tables that were not dropped.
            onCreate(db);
            break;
        default:
            Log.v(Const.LOG_TAG, "DB version provided no upgrade path: " + newVersion);
        }
    }

    public void deleteDatabase() {
        close();
        context.deleteDatabase(DATABASE_NAME);
    }
}
