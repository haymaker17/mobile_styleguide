/**
 * 
 */
package com.concur.mobile.platform.expense.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import com.concur.mobile.platform.config.provider.Config.UserColumns;

/**
 * This class provide a contract between the expense content provider and calling application code.
 * 
 * @author andrewk
 */
public class Expense {

    /**
     * The authority for the expense provider.
     */
    public static final String AUTHORITY = "com.concur.mobile.platform.expense";

    /**
     * A content:// style uri to the authority for the expense provider
     */
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     * Models expense type column information.
     */
    public static final class ExpenseTypeColumns implements BaseColumns {

        // Prevent instantiation.
        private ExpenseTypeColumns() {
        }

        /**
         * Contains the expense type table name.
         */
        public static final String TABLE_NAME = "EXPENSE_TYPE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the expense types URI
         */
        private static final String PATH_EXPENSE_TYPES = "/expense_types";

        /**
         * Path part for the expense types ID URI
         */
        private static final String PATH_EXPENSE_TYPES_ID = "/expense_types/";

        /**
         * 0-relative position of a Expense type ID segment in the path part of a expense type ID URI
         */
        public static final int EXPENSE_TYPES_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPES);

        /**
         * The content URI base for a single expense type. Callers must append a numeric expense type id to this Uri to retrieve
         * an expense type
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPES_ID);

        /**
         * The content URI match pattern for a single expense type, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSE_TYPES_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of expense types.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.expense_type";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single expense type.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.expense_type";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the type code (TEXT) column name.
         */
        public static final String TYPE_CODE = "TYPE_CODE";

        /**
         * Contains the type key (TEXT) column name.
         */
        public static final String TYPE_KEY = "TYPE_KEY";

        /**
         * Contains the type name (TEXT) column name.
         */
        public static final String TYPE_NAME = "TYPE_NAME";

        /**
         * Contains the form key (TEXT) column name.
         */
        public static final String FORM_KEY = "FORM_KEY";

        /**
         * Contains the "has post amount calculation" (BOOLEAN) column name.
         */
        public static final String HAS_POST_AMT_CALC = "HAS_POST_AMT_CALC";

        /**
         * Contains the "has tax form" (BOOLEAN) column name.
         */
        public static final String HAS_TAX_FORM = "HAS_TAX_FORM";

        /**
         * Contains the "itemization unallowed expense keys" (TEXT) column name.
         */
        public static final String ITEMIZATION_UNALLOW_EXP_KEYS = "ITEMIZATION_UNALLOW_EXP_KEYS";

        /**
         * Contains the "itemization form key" (TEXT) column name.
         */
        public static final String ITEMIZATION_FORM_KEY = "ITEMIZATION_FORM_KEY";

        /**
         * Contains the "itemization style" (TEXT) column name.
         */
        public static final String ITEMIZATION_STYLE = "ITEMIZATION_STYLE";

        /**
         * Contains the "itemization type" (TEXT) column name.
         */
        public static final String ITEMIZATION_TYPE = "ITEMIZATION_TYPE";

        /**
         * Contains the "parent expense key" (TEXT) column name.
         */
        public static final String PARENT_EXP_KEY = "PARENT_EXP_KEY";

        /**
         * Contains the "parent expense name" (TEXT) column name.
         */
        public static final String PARENT_EXP_NAME = "PARENT_EXP_NAME";

        /**
         * Contains the "supports attendees" (BOOLEAN) column name.
         */
        public static final String SUPPORTS_ATTENDEES = "SUPPORTS_ATTENDEES";

        /**
         * Contains the "vendor list key" (INTEGER) column name.
         */
        public static final String VENDOR_LIST_KEY = "VENDOR_LIST_KEY";

        /**
         * Contains the "allow edit attendee amount" (BOOLEAN) column name.
         */
        public static final String ALLOW_EDIT_ATTENDEE_AMOUNT = "ALLOW_EDIT_ATTENDEE_AMOUNT";

        /**
         * Contains the "allow edit attendee count" (BOOLEAN) column name.
         */
        public static final String ALLOW_EDIT_ATTENDEE_COUNT = "ALLOW_EDIT_ATTENDEE_COUNT";

        /**
         * Contains the "allow no shows" (BOOLEAN) column name.
         */
        public static final String ALLOW_NO_SHOWS = "ALLOW_NO_SHOWS";

        /**
         * Contains the "display add attendee on form" (BOOLEAN) column name.
         */
        public static final String DISPLAY_ADD_ATTENDEE_ON_FORM = "DISPLAY_ADD_ATTENDEE_ON_FORM";

        /**
         * Contains the "display attendee amounts" (BOOLEAN) column name.
         */
        public static final String DISPLAY_ATTENDEE_AMOUNTS = "DISPLAY_ATTENDEE_AMOUNTS";

        /**
         * Contains the "user as attended default" (BOOLEAN) column name.
         */
        public static final String USER_AS_ATTENDEE_DEFAULT = "USER_AS_ATTENDEE_DEFAULT";

        /**
         * Contains the "unallow attendee type keys" (TEXT) column name.
         */
        public static final String UNALLOW_ATN_TYPE_KEYS = "UNALLOW_ATN_TYPE_KEYS";

        /**
         * Contains the "policy key" (TEXT) column name.
         */
        public static final String POLICY_KEY = "POLICY_KEY";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Model expense column information. <br>
     * NOTE:<br>
     * The information exposed through the Expense view is read-only.
     */
    public static final class ExpenseColumns implements BaseColumns {

        // Prevent instantiation.
        private ExpenseColumns() {
        }

        /**
         * Contains the expense view name.
         */
        public static final String VIEW_NAME = "EXPENSE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the expenses URI
         */
        private static final String PATH_EXPENSES = "/expenses";

        /**
         * Path part for the expenses ID URI
         */
        private static final String PATH_EXPENSES_ID = "/expenses/";

        /**
         * 0-relative position of a expense ID segment in the path part of an expense ID URI
         */
        public static final int EXPENSES_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSES);

        /**
         * The content URI base for a single expense. Callers must append a numeric expense id to this Uri to retrieve an expense.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSES_ID);

        /**
         * The content URI match pattern for a single expense, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_EXPENSES_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of personal cards.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.expense";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single personal card.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.expense";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = Expense.ExpenseColumns.TRANSACTION_DATE + " ASC";

        // Column definitions

        /**
         * Contains the "expense entry type" (TEXT) column name. This value should store one of the values of
         * <code>PERSONAL_CARD_ENTRY_TYPE</code>, <code>CORPORATE_CARD_ENTRY_TYPE</code> <code>CASH_ENTRY_TYPE</code>,
         * <code>SMART_CORPORATE_ENTRY_TYPE</code>, <code>SMART_PERSONAL_ENTRY_TYPE</code> or
         * <code>RECEIPT_CAPTURE_ENTRY_TYPE</code>.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "personal card transaction" value for <code>TYPE</code>. This value also matches the associated
         * enumeration value <code>ExpenseDAO.ExpenseType.PERSONAL_CARD</code>.
         */
        public static final String PERSONAL_CARD_TYPE = "PERSONAL_CARD";

        /**
         * Contains the "corporate card transaction" value for <code>TYPE</code>. This value also matches the associated
         * enumeration value <code>ExpenseDAO.ExpenseType.CORPORATE_CARD</code>.
         */
        public static final String CORPORATE_CARD_TYPE = "CORPORATE_CARD";

        /**
         * Contains the "cash transaction" value for <code>TYPE</code>. This value also matches the associated enumeration value
         * <code>ExpenseDAO.ExpenseType.CASH</code>.
         */
        public static final String CASH_TYPE = "CASH";

        /**
         * Contains the "smart corporate card transaction" value for <code>TYPE</code>. This value also matches the associated
         * enumeration value <code>ExpenseDAO.ExpenseType.SMART_CORPORATE</code>.
         */
        public static final String SMART_CORPORATE_TYPE = "SMART_CORPORATE";

        /**
         * Contains the "smart personal card transaction" value for <code>TYPE</code>. This value also matches the associated
         * enumeration value <code>ExpenseDAO.ExpenseType.SMART_PERSONAL</code>.
         */
        public static final String SMART_PERSONAL_TYPE = "SMART_PERSONAL";

        /**
         * Contains the "receipt capture transaction" value for <code>TYPE</code>. This value also matches the associated
         * enumeration value <code>ExpenseDAO.ExpenseType.RECEIPT_CAPTURE</code>.
         */
        public static final String RECEIPT_CAPTURE_TYPE = "RECEIPT_CAPTURE";

        /**
         * Contains the "transaction amount" (REAL) column name.
         */
        public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";

        /**
         * Contains the "transaction currency code" (TEXT) column name.
         */
        public static final String TRANSACTION_CRN_CODE = "TRANSACTION_CRN_CODE";

        /**
         * Contains the "transaction date" (INTEGER - UTC milliseconds) column name.
         */
        public static final String TRANSACTION_DATE = "TRANSACTION_DATE";

        /**
         * Contains the "vendor name" (TEXT) column name.
         */
        public static final String VENDOR_NAME = "VENDOR_NAME";

        /**
         * Contains the "expense content ID" (INTEGER - Long value) column name. This value should be used as a content ID for a
         * particular content-type based on the value of <code>TYPE</code> according to the following:
         * <table>
         * <tr>
         * <td><b>Type</b></td>
         * <td><b>Content Id</b></td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.CASH_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.MobileEntryColumns._ID</td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.CORPORATE_CARD_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.CorporateCardTransactionColumns._ID</td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.SMART_CORPORATE_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.CorporateCardTransactionColumns._ID</td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.PERSONAL_CARD_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.PersonalCardTransactionColumns._ID</td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.SMART_PERSONAL_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.PersonalCardTransactionColumns._ID</td>
         * </tr>
         * <tr>
         * <td>ExpenseColumns.RECEIPT_CAPTURE_TYPE&nbsp;->&nbsp;</td>
         * <td>Expense.ReceiptCaptureColumns._ID</td>
         * </tr>
         * </table>
         */
        public static final String EXP_ID = "EXP_ID";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;

    }

    /**
     * Models corporate card transaction column information.
     */
    public static final class CorporateCardTransactionColumns implements BaseColumns {

        // Prevent instantiation.
        private CorporateCardTransactionColumns() {
        }

        /**
         * Contains the corporate card transaction table name.
         */
        public static final String TABLE_NAME = "CORPORATE_CARD_TRANSACTION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the corporate card transactions URI
         */
        private static final String PATH_CORPORATE_CARD_TRANSACTIONS = "/corporate_card_transactions";

        /**
         * Path part for the corporate card transactions ID URI
         */
        private static final String PATH_CORPORATE_CARD_TRANSACTIONS_ID = "/corporate_card_transactions/";

        /**
         * 0-relative position of a corporate card transaction ID segment in the path part of a corporate card transaction ID URI
         */
        public static final int CORPORATE_CARD_TRANSACTIONS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_CORPORATE_CARD_TRANSACTIONS);

        /**
         * The content URI base for a single corporate card transaction. Callers must append a numeric corporate card transaction
         * id to this Uri to retrieve a corporate card transaction.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY
                + PATH_CORPORATE_CARD_TRANSACTIONS_ID);

        /**
         * The content URI match pattern for a single corporate card transaction, specified by its ID. Use this to match incoming
         * URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
                + PATH_CORPORATE_CARD_TRANSACTIONS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of corporate card transactions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.corporate_card_transaction";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single corporate card transaction.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.corporate_card_transaction";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "expense entry type" (TEXT) column name. This value should store one of the values of
         * <code>Expense.ExpenseColumns.CORPORATE_CARD_TYPE</code> <code>Expense.ExpenseColumns.SMART_CORPORATE_TYPE</code>.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "card type code" (TEXT) column name.
         */
        public static final String CARD_TYPE_CODE = "CARD_TYPE_CODE";

        /**
         * Contains the "card type name" (TEXT) column name.
         */
        public static final String CARD_TYPE_NAME = "CARD_TYPE_NAME";

        /**
         * Contains the "credit card transaction key" (TEXT) column name.
         */
        public static final String CCT_KEY = "CCT_KEY";

        /**
         * Contains the "credit card type" (TEXT) column name.
         */
        public static final String CCT_TYPE = "CCT_TYPE";

        /**
         * Contains the "has rich data" (BOOLEAN) column name.
         */
        public static final String HAS_RICH_DATA = "HAS_RICH_DATA";

        /**
         * Contains the "description" (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the "doing business as" (TEXT) column name.
         */
        public static final String DOING_BUSINESS_AS = "DOING_BUSINESS_AS";

        /**
         * Contains the "expense key" (TEXT) column name.
         */
        public static final String EXPENSE_KEY = "EXPENSE_KEY";

        /**
         * Contains the "expense name" (TEXT) column name.
         */
        public static final String EXPENSE_NAME = "EXPENSE_NAME";

        /**
         * Contains the "merchant city" (TEXT) column name.
         */
        public static final String MERCHANT_CITY = "MERCHANT_CITY";

        /**
         * Contains the "merchant country code" (TEXT) column name.
         */
        public static final String MERCHANT_COUNTRY_CODE = "MERCHANT_COUNTRY_CODE";

        /**
         * Contains the "merchant name" (TEXT) column name.
         */
        public static final String MERCHANT_NAME = "MERCHANT_NAME";

        /**
         * Contains the "merchant state" (TEXT) column name.
         */
        public static final String MERCHANT_STATE = "MERCHANT_STATE";

        /**
         * Contains the "smart expense mobile entry key" (TEXT) column name.
         */
        public static final String SMART_EXPENSE_ME_KEY = "SMART_EXPENSE_ME_KEY";

        /**
         * Contains the "mobile entry ID" (INTEGER REFERENCES MOBILE_ENTRY._ID) column name.
         */
        public static final String MOBILE_ENTRY_ID = "MOBILE_ENTRY_ID";

        /**
         * Contains the "transaction amount" (REAL) column name.
         */
        public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";

        /**
         * Contains the "transaction currency code" (TEXT) column name.
         */
        public static final String TRANSACTION_CRN_CODE = "TRANSACTION_CRN_CODE";

        /**
         * Contains the "transaction date" (INTEGER - UTC milliseconds) column name.
         */
        public static final String TRANSACTION_DATE = "TRANSACTION_DATE";

        /**
         * Contains the "tag" (TEXT) column name.
         */
        public static final String TAG = "TAG";

        /**
         * Contains the "is split" (BOOLEAN) column name.
         */
        public static final String IS_SPLIT = "IS_SPLIT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models personal card information.
     */
    public static final class PersonalCardColumns implements BaseColumns {

        // Prevent instantiation.
        private PersonalCardColumns() {
        }

        /**
         * Contains the personal card table name.
         */
        public static final String TABLE_NAME = "PERSONAL_CARD";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the personal cards URI
         */
        private static final String PATH_PERSONAL_CARDS = "/personal_cards";

        /**
         * Path part for the personal cards ID URI
         */
        private static final String PATH_PERSONAL_CARDS_ID = "/personal_cards/";

        /**
         * 0-relative position of a personal card ID segment in the path part of a personal card ID URI
         */
        public static final int PERSONAL_CARDS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PERSONAL_CARDS);

        /**
         * The content URI base for a single personal card. Callers must append a numeric personal card id to this Uri to retrieve
         * a personal card.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_PERSONAL_CARDS_ID);

        /**
         * The content URI match pattern for a single personal card, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_PERSONAL_CARDS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of personal cards.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.personal_card";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single personal card.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.personal_card";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "personal card account key" (TEXT) column name.
         */
        public static final String PCA_KEY = "PCA_KEY";

        /**
         * Contains the "card name" (TEXT) column name.
         */
        public static final String CARD_NAME = "CARD_NAME";

        /**
         * Contains the "card account last four digits" (TEXT) column name.
         */
        public static final String ACCT_NUM_LAST_FOUR = "ACCT_NUM_LAST_FOUR";

        /**
         * Contains the "card account currency code" (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the "tag" (TEXT) column name.
         */
        public static final String TAG = "TAG";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models personal card transaction information.
     */
    public static final class PersonalCardTransactionColumns implements BaseColumns {

        // Prevent instantiation.
        private PersonalCardTransactionColumns() {
        }

        /**
         * Contains the personal card transaction table name.
         */
        public static final String TABLE_NAME = "PERSONAL_CARD_TRANSACTION";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the personal card transactions URI
         */
        private static final String PATH_PERSONAL_CARD_TRANSACTIONS = "/personal_card_transactions";

        /**
         * Path part for the personal card transactions ID URI
         */
        private static final String PATH_PERSONAL_CARD_TRANSACTIONS_ID = "/personal_card_transactions/";

        /**
         * 0-relative position of a personal card transaction ID segment in the path part of a personal card transaction ID URI
         */
        public static final int PERSONAL_CARD_TRANSACTIONS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_PERSONAL_CARD_TRANSACTIONS);

        /**
         * The content URI base for a single personal card transaction. Callers must append a numeric personal card transaction id
         * to this Uri to retrieve a personal card transaction.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri
                .parse(SCHEME + AUTHORITY + PATH_PERSONAL_CARD_TRANSACTIONS_ID);

        /**
         * The content URI match pattern for a single personal card transaction, specified by its ID. Use this to match incoming
         * URIs or to construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY
                + PATH_PERSONAL_CARD_TRANSACTIONS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of personal card transactions.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.personal_card_transaction";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single personal card transaction.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.personal_card_transaction";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "expense entry type" (TEXT) column name. This value should store one of the values of
         * <code>Expense.ExpenseColumns.PERSONAL_CARD_TYPE</code> <code>Expense.ExpenseColumns.SMART_PERSONAL_TYPE</code>.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "personal card transaction key" (TEXT) column name.
         */
        public static final String PCT_KEY = "PCT_KEY";

        /**
         * Contains the "date posted" (INTEGER - UTC milliseconds) column name.
         */
        public static final String DATE_POSTED = "DATE_POSTED";

        /**
         * Contains the "description" (TEXT) column name.
         */
        public static final String DESCRIPTION = "DESCRIPTION";

        /**
         * Contains the "amount" (REAL) column name.
         */
        public static final String AMOUNT = "AMOUNT";

        /**
         * Contains the "currency code" (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the "status" (TEXT) column name.
         */
        public static final String STATUS = "STATUS";

        /**
         * Contains the "category" (TEXT) column name.
         */
        public static final String CATEGORY = "CATEGORY";

        /**
         * contains the "expense type key" (TEXT) column name.
         */
        public static final String EXP_KEY = "EXP_KEY";

        /**
         * Contains the "expense type name" (TEXT) column name.
         */
        public static final String EXP_NAME = "EXP_NAME";

        /**
         * Contains the "report key" (TEXT) column name.
         */
        public static final String RPT_KEY = "RPT_KEY";

        /**
         * Contains the "report name" (TEXT) column name.
         */
        public static final String RPT_NAME = "RPT_NAME";

        /**
         * Contains the "smart expense mobile entry key" (TEXT) column name.
         */
        public static final String SMART_EXPENSE_ME_KEY = "SMART_EXPENSE_ME_KEY";

        /**
         * Contains the "mobile entry ID" (INTEGER REFERENCES MOBILE_ENTRY._ID) column name.
         */
        public static final String MOBILE_ENTRY_ID = "MOBILE_ENTRY_ID";

        /**
         * Contains the "personal card ID" (INTEGER REFERENCES PERSONAL_CARD._ID) column name.
         */
        public static final String PERSONAL_CARD_ID = "PERSONAL_CARD_ID";

        /**
         * Contains the "tag" (TEXT) column name.
         */
        public static final String TAG = "TAG";

        /**
         * Contains the "is split" (BOOLEAN) column name.
         */
        public static final String IS_SPLIT = "IS_SPLIT";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models mobile entry information.
     */
    public static final class MobileEntryColumns implements BaseColumns {

        // Prevent instantiation.
        private MobileEntryColumns() {
        }

        /**
         * Contains the personal card table name.
         */
        public static final String TABLE_NAME = "MOBILE_ENTRY";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the mobile entries URI
         */
        private static final String PATH_MOBILE_ENTRIES = "/mobile_entries";

        /**
         * Path part for the mobile entries ID URI
         */
        private static final String PATH_MOBILE_ENTRIES_ID = "/mobile_entries/";

        /**
         * 0-relative position of a mobile entry ID segment in the path part of a mobile entry ID URI
         */
        public static final int MOBILE_ENTRIES_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_MOBILE_ENTRIES);

        /**
         * The content URI base for a single mobile entry. Callers must append a numeric mobile entry id to this Uri to retrieve a
         * mobile entry.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_MOBILE_ENTRIES_ID);

        /**
         * The content URI match pattern for a single mobile entry, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_MOBILE_ENTRIES_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of mobile entries.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.mobile_entry";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single mobile entry.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.mobile_entry";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "currency code" (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the "expense type key" (TEXT) column name.
         */
        public static final String EXP_KEY = "EXP_KEY";

        /**
         * Contains the "expense type name" (TEXT) column name.
         */
        public static final String EXP_NAME = "EXP_NAME";

        /**
         * Contains the "location name" (TEXT) column name.
         */
        public static final String LOCATION_NAME = "LOCATION_NAME";

        /**
         * Contains the "vendor name" (TEXT) column name.
         */
        public static final String VENDOR_NAME = "VENDOR_NAME";

        /**
         * Contains the "expense entry type" (TEXT) column name. This value should store one of the values of
         * <code>Expense.ExpenseColumns.PERSONAL_CARD_TYPE</code>, <code>Expense.ExpenseColumns.CORPORATE_CARD_TYPE</code>
         * <code>Expense.ExpenseColumns.CASH_TYPE</code>, <code>Expense.ExpenseColumns.SMART_CORPORATE_TYPE</code>,
         * <code>Expense.ExpenseColumns.SMART_PERSONAL_TYPE</code> or <code>Expense.ExpenseColumns.RECEIPT_CAPTURE_TYPE</code>.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "mobile entry key" (TEXT) column name.
         */
        public static final String MOBILE_ENTRY_KEY = "ME_KEY";

        /**
         * Contains the "personal card account key" (TEXT) column name.
         */
        public static final String PCA_KEY = "PCA_KEY";

        /**
         * Contains the "personal card transaction key" (TEXT) column name.
         */
        public static final String PCT_KEY = "PCT_KEY";

        /**
         * Contains the "corporate card transaction key" (TEXT) column name.
         */
        public static final String CCT_KEY = "CCT_KEY";

        /**
         * Contains the "receipt capture key" (TEXT) column name.
         */
        public static final String RC_KEY = "RC_KEY";

        /**
         * Contains the "transaction amount" (REAL) column name.
         */
        public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";

        /**
         * Contains the "transaction date" (INTEGER - UTC milliseconds) column name.
         */
        public static final String TRANSACTION_DATE = "TRANSACTION_DATE";

        /**
         * Contains the "has receipt image" (BOOLEAN) column name.
         */
        public static final String HAS_RECEIPT_IMAGE = "HAS_RECEIPT_IMAGE";

        /**
         * Contains the "receipt image ID" (TEXT) column name.
         */
        public static final String RECEIPT_IMAGE_ID = "RECEIPT_IMAGE_ID";

        /**
         * Contains the "receipt content ID" (INTEGER) column name.
         */
        public static final String RECEIPT_CONTENT_ID = "RECEIPT_CONTENT_ID";

        /**
         * Contains the encoded "receipt image data" (TEXT) column name.
         */
        public static final String RECEIPT_IMAGE_DATA = "RECEIPT_IMAGE_DATA";

        /**
         * Contains the encoded "receipt image data local file path" (TEXT) column name.
         */
        public static final String RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH = "RECEIPT_IMAGE_DATA_LOCAL_FILE_PATH";

        /**
         * Contains the "comment" (TEXT) column name.
         */
        public static final String COMMENT = "COMMENT";

        /**
         * Contains the "tag" (TEXT) column name.
         */
        public static final String TAG = "TAG";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models receipt capture information.
     */
    public static final class ReceiptCaptureColumns implements BaseColumns {

        // Prevent instantiation.
        private ReceiptCaptureColumns() {
        }

        /**
         * Contains the personal card table name.
         */
        public static final String TABLE_NAME = "RECEIPT_CAPTURE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the receipt captures URI
         */
        private static final String PATH_RECEIPT_CAPTURES = "/receipt_captures";

        /**
         * Path part for the receipt captures ID URI
         */
        private static final String PATH_RECEIPT_CAPTURES_ID = "/receipt_captures/";

        /**
         * 0-relative position of a receipt capture ID segment in the path part of a receipt capture ID URI
         */
        public static final int RECEIPT_CAPTURES_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RECEIPT_CAPTURES);

        /**
         * The content URI base for a single receipt capture. Callers must append a numeric personal card id to this Uri to
         * retrieve a receipt capture.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RECEIPT_CAPTURES_ID);

        /**
         * The content URI match pattern for a single receipt capture, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri
                .parse(SCHEME + AUTHORITY + PATH_RECEIPT_CAPTURES_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of receipt captures.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.receipt_capture";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single receipt capture.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.receipt_capture";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "expense entry type" (TEXT) column name. This value should store the value of
         * <code>Expense.ExpenseColumns.RECEIPT_CAPTURE_TYPE</code>.
         */
        public static final String TYPE = "TYPE";

        /**
         * Contains the "currency code" (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * contains the "expense type key" (TEXT) column name.
         */
        public static final String EXP_KEY = "EXP_KEY";

        /**
         * Contains the "expense type name" (TEXT) column name.
         */
        public static final String EXP_NAME = "EXP_NAME";

        /**
         * Contains the "vendor name" (TEXT) column name.
         */
        public static final String VENDOR_NAME = "VENDOR_NAME";

        /**
         * Contains the "receipt capture key" (TEXT) column name.
         */
        public static final String RC_KEY = "RC_KEY";

        /**
         * Contains the "smart expense ID" (TEXT) column name.
         */
        public static final String SMART_EXPENSE_ID = "SMART_EXPENSE_ID";

        /**
         * Contains the "transaction amount" (REAL) column name.
         */
        public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";

        /**
         * Contains the "transaction date" (INTEGER - UTC milliseconds) column name.
         */
        public static final String TRANSACTION_DATE = "TRANSACTION_DATE";

        /**
         * Contains the "receipt image ID" (TEXT) column name.
         */
        public static final String RECEIPT_IMAGE_ID = "RECEIPT_IMAGE_ID";

        /**
         * Contains the "tag" (TEXT) column name.
         */
        public static final String TAG = "TAG";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models Receipt data.
     */
    public static final class ReceiptColumns implements BaseColumns {

        // Prevent instantiation.
        private ReceiptColumns() {
        }

        /**
         * Contains the receipt table name.
         */
        public static final String TABLE_NAME = "RECEIPT";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        /**
         * Contains the "thumbnail query parameter" (boolean) name that can be used to retrieve to read/write a thumbnail image
         * for a receipt. This parameter value should be either 'true' or 'false'.
         */
        public static final String THUMBNAIL_QUERY_PARAMETER = "thumbnail";

        // Path parts for the URIs

        /**
         * Path part for the receipts URI
         */
        private static final String PATH_RECEIPTS = "/receipts";

        /**
         * Path part for the Receipt ID URI
         */
        private static final String PATH_RECEIPTS_ID = "/receipts/";

        /**
         * 0-relative position of a receipt ID segment in the path part of a receipt ID URI
         */
        public static final int RECEIPTS_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_RECEIPTS);

        /**
         * The content URI base for a single receipt. Callers must append a numeric receipt id to this Uri to retrieve a receipt.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_RECEIPTS_ID);

        /**
         * The content URI match pattern for a single receipt, specified by its ID. Use this to match incoming URIs or to
         * construct an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_RECEIPTS_ID + "/#");

        // MIME type definitions

        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of receipts.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.expense.receipt";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a single receipt.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.expense.receipt";

        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the "etag value" (TEXT) column name.
         */
        public static final String ETAG = "ETAG";

        /**
         * Contains the "receipt image ID" (TEXT) column name.
         */
        public static final String ID = "ID";

        /**
         * Contains the "receipt image URI" (TEXT) column name. <br>
         * <br>
         * Values stored in this column can be used to retrieve/update full-sized/thumbnail receipt data.<br>
         * The <code>THUMBNAIL_QUERY_PARAMETER</code> should be specified in order to read/write thumbnail receipt image data.
         * 
         * @see Expense.ReceiptColumns#THUMBNAIL_QUERY_PARAMETER
         * @see ContentResolver#openInputStream(Uri)
         * @see ContentResolver#openOutputStream(Uri)
         */
        public static final String URI = "URI";

        /**
         * Contains the "receipt image content type" (TEXT) column name.
         */
        public static final String RECEIPT_CONTENT_TYPE = "CONTENT_TYPE";

        /**
         * Contains the "receipt image local path" (TEXT) column name.<br>
         * <br>
         * Values stored in this column can be used to directly access any locally stored receipt data that is contained in a
         * file.
         */
        public static final String LOCAL_PATH = "LOCAL_PATH";

        /**
         * Contains the "receipt image data" (BLOB) column name.<br>
         * <br>
         * Values stored in this column contain the binary full-sized receipt image data.
         */
        public static final String RECEIPT_DATA = "DATA";

        /**
         * Contains the "receipt image thumbnail URI" (TEXT) column name. <br>
         * <br>
         * Values stored in this column can be used to retrieve/update thumbnail receipt data.<br>
         * The <code>THUMBNAIL_QUERY_PARAMETER</code> should be a part of this URI if when using the expense content provider to
         * read/update receipt thumbnail receipt image data.
         * 
         * @see Expense.ReceiptColumns#THUMBNAIL_QUERY_PARAMETER
         * @see ContentResolver#openInputStream(Uri)
         * @see ContentResolver#openOutputStream(Uri)
         */
        public static final String THUMBNAIL_URI = "THUMBNAIL_URI";

        /**
         * Contains the "receipt image thumbnail content type" (TEXT) column name.
         */
        public static final String THUMBNAIL_CONTENT_TYPE = "THUMBNAIL_CONTENT_TYPE";

        /**
         * Contains the "receipt image thumbnail local path" (TEXT) column name. <br>
         * <br>
         * Values stored in this column can be used to directly access any locally stored thumbnail receipt data that is contained
         * in a file.
         */
        public static final String THUMBNAIL_LOCAL_PATH = "THUMBNAIL_LOCAL_PATH";

        /**
         * Contains the "receipt image thumbnail receipt data" (BLOB) column name. <br>
         * <br>
         * Values stored in this column contain the binary thumbnail-sized receipt image data.
         */
        public static final String THUMBNAIL_RECEIPT_DATA = "THUMBNAIL_RECEIPT_DATA";

        /**
         * Contains the "is attached" (BOOLEAN) column name.
         */
        public static final String IS_ATTACHED = "IS_ATTACHED";

        /**
         * Contains the "last access time" (INTEGER - UTC milliseconds) column name.
         */
        public static final String LAST_ACCESS_TIME = "LAST_ACCESS_TIME";

        /**
         * Contains the "image upload time" (INTEGER - UTC milliseconds) column name.<br>
         * <br>
         * Values stored in this column contain the original time of the receipt upload.
         */
        public static final String IMAGE_UPLOAD_TIME = "IMAGE_UPLOAD_TIME";

        /**
         * Contains the "file name" (TEXT) column name. <br>
         * <br>
         * Values stored in this column contain the original file name of the receipt data as it was uploaded.
         */
        public static final String FILE_NAME = "FILE_NAME";

        /**
         * Contains the "file type" (TEXT) column name. <br>
         * <br>
         * Values in this column contain the original file type, i.e., "JPEG", "JPG", "PDF" of the receipt data as it was
         * uploaded.
         */
        public static final String FILE_TYPE = "FILE_TYPE";

        /**
         * Contains the "system origin" (TEXT) column name.
         */
        public static final String SYSTEM_ORIGIN = "SYSTEM_ORIGIN";

        /**
         * Contains the "image origin" (TEXT) column name.
         */
        public static final String IMAGE_ORIGIN = "IMAGE_ORIGIN";

        /**
         * Contains the "image URL" (TEXT) column name. <br>
         * <br>
         * This contains the URL of the receipt image data that can be retrieved from the server.<br>
         * <br>
         * <b>NOTE:</b>&nbsp;This URL is time sensitive and is only active for a period of 15 minutes from the time the receipt
         * list data was loaded.
         */
        public static final String IMAGE_URL = "IMAGE_URL";

        /**
         * Contains the "thumb URL" (TEXT) column name. <br>
         * <br>
         * This contains the URL of the thumbnail receipt image data that can be retrieved from the server.<br>
         * <br>
         * <b>NOTE:</b>&nbsp;This URL is time sensitive and is only active for a period of 15 minutes from the time the receipt
         * list data was loaded.
         */
        public static final String THUMB_URL = "THUMB_URL";

        /**
         * Contains the "ocr image origin" (TEXT) column name.
         */
        public static final String OCR_IMAGE_ORIGIN = "OCR_IMAGE_ORIGIN";

        /**
         * Contains the "ocr stat key" (TEXT) column name. <br>
         * <br>
         * The following values are supported:<br>
         * <table border="1">
         * <tr>
         * <td><b>Value</b></td>
         * <td><b>Meaning</b></td>
         * </tr>
         * <tr>
         * <td>NOT_COMPANY_ENABLED</td>
         * <td>OCR is not enabled for the users company.</td>
         * </tr>
         * <tr>
         * <td>OCR_NOT_AVAILABLE</td>
         * <td>OCR is not available.</td>
         * </tr>
         * <tr>
         * <td>OCR_STAT_UNKNOWN</td>
         * <td>OCR has no knowledge of this receipt.</td>
         * </tr>
         * <tr>
         * <td>A_PEND</td>
         * <td>OCR is auto pending.</td>
         * </tr>
         * <tr>
         * <td>A_DONE</td>
         * <td>OCR auto has completed.</td>
         * </tr>
         * <tr>
         * <td>A_CNCL</td>
         * <td>OCR auto was cancelled.</td>
         * </tr>
         * <tr>
         * <td>A_FAIL</td>
         * <td>OCR auto has failed.</td>
         * </tr>
         * <tr>
         * <td>M_PEND</td>
         * <td>OCR is manual.</td>
         * </tr>
         * <tr>
         * <td>M_DONE</td>
         * <td>OCR manual has completed.</td>
         * </tr>
         * <tr>
         * <td>M_CNCL</td>
         * <td>OCR manual was cancelled.</td>
         * </tr>
         * </table>
         */
        public static final String OCR_STAT_KEY = "OCR_STAT_KEY";

        /**
         * Contains the "ocr reject code" (TEXT) column name. <br>
         * <br>
         * The following values are supported:<br>
         * <table border="1">
         * <tr>
         * <td><b>Value</b></td>
         * <td><b>Meaning</b></td>
         * </tr>
         * <tr>
         * <td>PF</td>
         * <td>Processing has failed.</td>
         * </tr>
         * <tr>
         * <td>NR</td>
         * <td>Not a receipt.</td>
         * </tr>
         * <tr>
         * <td>UR</td>
         * <td>Unreadable receipt.</td>
         * </tr>
         * <tr>
         * <td>MR</td>
         * <td>Multiple receipts.</td>
         * </tr>
         * <tr>
         * <td>AC</td>
         * <td>OCR auto was cancelled.</td>
         * </tr>
         * <tr>
         * <td>MC</td>
         * <td>OCR manual was cancelled.</td>
         * </tr>
         * </table>
         */
        public static final String OCR_REJECT_CODE = "OCR_REJECT_CODE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;
    }

    /**
     * Models Smart Expense data.
     */
    public static final class SmartExpenseColumns implements BaseColumns {

        // Prevent instantiation.
        private SmartExpenseColumns() {
        }

        /**
         * Contains the expense type table name.
         */
        public static final String TABLE_NAME = "SMART_EXPENSE";

        // URI definitions

        /**
         * The scheme part for this provider's URI
         */
        private static final String SCHEME = "content://";

        // Path parts for the URIs

        /**
         * Path part for the smart expense URI
         */
        private static final String PATH_SMART_EXPENSE = "/smart_expenses";

        /**
         * Path part for the smart expense ID URI
         */
        private static final String PATH_SMART_EXPENSE_ID = "/smart_expenses/";

        /**
         * 0-relative position of a smart_expense ID segment in the path part of a smart expense ID URI
         */
        public static final int SMART_EXPENSE_ID_PATH_POSITION = 1;

        /**
         * The content:// style URL for this table
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY + PATH_SMART_EXPENSE);

        /**
         * The content URI base for a single smart expense. Callers must append a numeric smart expense id to this Uri to retrieve
         * a smart expense
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME + AUTHORITY + PATH_SMART_EXPENSE_ID);

        /**
         * The content URI match pattern for a smart expense, specified by its ID. Use this to match incoming URIs or to construct
         * an Intent.
         */
        public static final Uri CONTENT_ID_URI_PATTERN = Uri.parse(SCHEME + AUTHORITY + PATH_SMART_EXPENSE_ID + "/#");

        // MIME type definitions
        /**
         * The MIME type of {@link #CONTENT_URI} providing a directory of smart expense.
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.com.concur.mobile.platform.config.smart_expesne";

        /**
         * The MIME type of a {@link #CONTENT_URI} subdirectory of a smart expense.
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.com.concur.mobile.platform.config.smart_expense";
        /**
         * Contains the default sort order.
         */
        public static final String DEFAULT_SORT_ORDER = BaseColumns._ID + " ASC";

        // Column definitions

        /**
         * Contains the Fuel service charge (REAL) column name.
         */
        public static final String FUEL_SERVICE_CHARGE = "FUEL_SERVICE_CHARGE";

        /**
         * Contains the Estimated Amount (REAL) column name.
         */
        public static final String ESTIMATED_AMOUNT = "ESTIMATED_AMOUNT";

        /**
         * Contains the CCA Key (TEXT) column name.
         */
        public static final String CCA_KEY = "CCA_KEY";

        /**
         * Contains the CCT Key (TEXT) column name.
         */
        public static final String CCT_KEY = "CCT_KEY";

        /**
         * Contains the Extracted CCT Key(TEXT) column name.
         */
        public static final String EXTRACT_CCT_KEY = "EXTRACT_CCT_KEY";

        /**
         * Contains the Smart Expense Id (TEXT) column name.
         */
        public static final String SMART_EXPENSE_ID = "SMART_EXPENSE_ID";

        /**
         * Contains the E-receipt Source (TEXT) column name.
         */
        public static final String E_RECEIPT_SOURCE = "E_RECEIPT_SOURCE";

        /**
         * Contains the transaction date (INTEGER - milliseconds) column name.
         */
        public static final String TRANSACTION_DATE = "TRANSACTION_DATE";

        /**
         * Contains the Has Rich Data (BOOLEAN) column name.
         */
        public static final String HAS_RICH_DATA = "HAS_RICH_DATA";

        /**
         * Contains the E-receipt Id (TEXT) column name.
         */
        public static final String E_RECEIPT_ID = "E_RECEIPT_ID";

        /**
         * Contains the Segment Id (TEXT) column name.
         */
        public static final String SEGMENT_ID = "SEGMENT_ID";

        /**
         * Contains the Expense Name (TEXT) column name.
         */
        public static final String EXP_NAME = "EXP_NAME";

        /**
         * Contains the Transaction Group (TEXT) column name.
         */
        public static final String TRANSACTION_GROUP = "TRANSACTION_GROUP";

        /**
         * Contains the Vendor Code (TEXT) column name.
         */
        public static final String VENDOR_CODE = "VENDOR_CODE";

        /**
         * Contains the Country (TEXT) column name.
         */
        public static final String COUNTRY = "COUNTRY";

        /**
         * Contains the Trip Id (TEXT) column name.
         */
        public static final String TRIP_ID = "TRIP_ID";

        /**
         * Contains the Vendor Description (TEXT) column name.
         */
        public static final String VENDOR_DESCRIPTION = "VENDOR_DESCRIPTION";

        /**
         * Contains the Exchange Rate (REAL) column name.
         */
        public static final String EXCHANGE_RATE = "EXCHANGE_RATE";

        /**
         * Contains the Posted Amount (REAL) column name.
         */
        public static final String POSTED_AMOUNT = "POSTED_AMOUNT";

        /**
         * Contains the Transaction Amount (REAL) column name.
         */
        public static final String TRANSACTION_AMOUNT = "TRANSACTION_AMOUNT";

        /**
         * Contains the Insurance Charge(REAL) column name.
         */
        public static final String INSURANCE_CHARGE = "INSURANCE_CHARGE";

        /**
         * Contains the Card Last Segment (TEXT) column name.
         */
        public static final String CARD_LAST_SEGMENT = "CARD_LAST_SEGMENT";

        /**
         * Contains the Currency Code (TEXT) column name.
         */
        public static final String CRN_CODE = "CRN_CODE";

        /**
         * Contains the Location Name (TEXT) column name.
         */
        public static final String LOC_NAME = "LOC_NAME";

        /**
         * Contains the Expense Key (TEXT) column name.
         */
        public static final String EXP_KEY = "EXP_KEY";

        /**
         * Contains the Travel Company Code (TEXT) column name.
         */
        public static final String TRAVEL_COMPANY_CODE = "TRAVEL_COMPANY_CODE";

        /**
         * Contains the Merchant State (TEXT) column name.
         */
        public static final String MERCHANT_STATE = "MERCHANT_STATE";

        /**
         * Contains the Merchant City (TEXT) column name.
         */
        public static final String MERCHANT_CITY = "MERCHANT_CITY";

        /**
         * Contains the Merchant Country Code (TEXT) column name.
         */
        public static final String MERCHANT_CUNTRY_CODE = "MERCHANT_CUNTRY_CODE";

        /**
         * Contains the Merchant Name (TEXT) column name.
         */
        public static final String MERCHANT_NAME = "MERCHANT_NAME";

        /**
         * Contains the Posted CrnCode (TEXT) column name.
         */
        public static final String POSTED_CRN_CODE = "POSTED_CRN_CODE";

        /**
         * Contains the Trip Name (TEXT) column name.
         */
        public static final String TRIP_NAME = "TRIP_NAME";

        /**
         * Contains the City (TEXT) column name.
         */
        public static final String CITY = "CITY";

        /**
         * Contains the E-receipt Type (TEXT) column name.
         */
        public static final String E_RECEIPT_TYPE = "E_RECEIPT_TYPE";

        /**
         * Contains the State (TEXT) column name.
         */
        public static final String STATE = "STATE";

        /**
         * Contains the Transaction CrnCode (TEXT) column name.
         */
        public static final String TRANSACTION_CRN_CODE = "TRANSACTION_CRN_CODE";

        /**
         * Contains the Ticket Number (TEXT) column name.
         */
        public static final String TICKET_NUMBER = "TICKET_NUMBER";

        /**
         * Contains the E Receipt Image Id (TEXT) column name.
         */
        public static final String E_RECEIPT_IMAGE_ID = "E_RECEIPT_IMAGE_ID";

        /**
         * Contains the VenLiName (TEXT) column name.
         */
        public static final String VEN_LI_NAME = "VEN_LI_NAME";

        /**
         * Contains the Gps Charge (REAL) column name.
         */
        public static final String GPS_CHARGE = "GPS_CHARGE";

        /**
         * Contains the Report Entry Key (TEXT) column name.
         */
        public static final String RPE_KEY = "RPE_KEY";

        /**
         * Contains the Airline Code (TEXT) column name.
         */
        public static final String AIRLINE_CODE = "AIRLINE_CODE";

        /**
         * Contains the Segment Type Key (TEXT) column name.
         */
        public static final String SEGMENT_TYPE_KEY = "SEGMENT_TYPE_KEY";

        /**
         * Contains the Doing Business As (TEXT) column name.
         */
        public static final String DOING_BUSINESS_AS = "DOING_BUSINESS_AS";

        /**
         * Contains the Card Type Code (TEXT) column name.
         */
        public static final String CARD_TYPE_CODE = "CARD_TYPE_CODE";

        /**
         * Contains the ME Key (TEXT) column name.
         */
        public static final String ME_KEY = "ME_KEY";

        /**
         * Contains the PCT Key (TEXT) column name.
         */
        public static final String PCT_KEY = "PCT_KEY";

        /**
         * Contains the PCA Key (TEXT) column name.
         */
        public static final String PCA_KEY = "PCA_KEY";

        /**
         * Contains the Charge Description (TEXT) column name.
         */
        public static final String CHARGE_DESC = "CHARGE_DESC";

        /**
         * Contains the Card Category Name (TEXT) column name.
         */
        public static final String CARD_CATEGORY_NAME = "CARD_CATEGORY_NAME";

        /**
         * Contains the Mobile Receipt ImageId (TEXT) column name.
         */
        public static final String MOB_RECEIPT_ID = "MOB_RECEIPT_ID";

        /**
         * Contains the Card Icon File Name (TEXT) column name.
         */
        public static final String CARD_ICON_FILE_NAME = "CARD_ICON_FILE_NAME";

        /**
         * Contains the Card Program Type Name (TEXT) column name.
         */
        public static final String CARD_PROGRAM_TYPE_NAME = "CARD_PROGRAM_TYPE_NAME";

        /**
         * Contains the RC Key (TEXT) column name.
         */
        public static final String RC_KEY = "RC_KEY";

        /**
         * Contains the Status Key (TEXT) column name. <br>
         * <br>
         * For a definition of values:
         * 
         * @see Expense.ReceiptColumns#OCR_STAT_KEY
         */
        public static final String STATUS_KEY = "STATUS_KEY";

        /**
         * Contains the Reject Code (TEXT) column name. <br>
         * <br>
         * For a definition of values:
         * 
         * @see Expense.ReceiptColumns#OCR_REJECT_CODE
         */
        public static final String REJECT_CODE = "REJECT_CODE";

        /**
         * Contains the Receipt Image Id (TEXT) column name.
         */
        public static final String RECEIPT_IMAGE_ID = "RECEIPT_IMAGE_ID";

        /**
         * Contains the CCT Receipt Image Id (TEXT) column name.
         */
        public static final String CCT_RECEIPT_IMG_ID = "CCT_RECEIPT_IMG_ID";

        /**
         * Contains the Comment (TEXT) column name.
         */
        public static final String COMMENT = "COMMENT";

        /**
         * Contains the "total days" (INTEGER) column name.
         */
        public static final String TOTAL_DAYS = "TOTAL_DAYS";

        /**
         * Contains the "pick-up date" (INTEGER - milliseconds) column name.
         */
        public static final String PICK_UP_DATE = "PICK_UP_DATE";

        /**
         * Contains the "return date" (INTEGER - milliseconds) column name.
         */
        public static final String RETURN_DATE = "RETURN_DATE";

        /**
         * Contains the "confirmation number" (TEXT) column name.
         */
        public static final String CONFIRMATION_NUMBER = "CONFIRMATION_NUMBER";

        /**
         * Contains the "average daily rate" (REAL) column name.
         */
        public static final String AVERAGE_DAILY_RATE = "AVERAGE_DAILY_RATE";

        /**
         * Contains the user id column name.
         */
        public static final String USER_ID = UserColumns.USER_ID;


        // ##### CUSTOM COLUMNS (i.e. not included in actual server response) ###### //

        /**
         * Contains the local file path (TEXT) of the SmartExpense receipt image.
         * This is usually used for creating an offline SmartExpense.
         */
        public static final String RECEIPT_IMAGE_LOCAL_PATH = "RECEIPT_IMAGE_LOCAL_PATH";

        /**
         * Contains the timestamp (INTEGER - UTC milliseconds) of the last time
         * this SmartExpense was edited locally.
         */
        public static final String LAST_EDIT_TIME = "LAST_EDIT_TIME";

        /**
         * Contains the timestamp (INTEGER - UTC milliseconds) of the last
         * time this SmartExpense was synced with the server.
         */
        public static final String LAST_SYNC_TIME = "LAST_SYNC_TIME";


        // ######### ADDITIONAL SORT ORDER ######### //

        /**
         * Sort by most current transactions.
         */
        public static final String DATE_NEWEST_SORT_ORDER = TRANSACTION_DATE + " DESC";

        /**
         * Sort by oldest transaction date.
         */
        public static final String DATE_OLDEST_SORT_ORDER = TRANSACTION_DATE + " ASC";

        /**
         * Sort by lowest amount.
         */
        public static final String AMOUNT_LOWEST_SORT_ORDER = TRANSACTION_AMOUNT + " ASC";

        /**
         * Sort by highest amount.
         */
        public static final String AMOUNT_HIGHEST_SORT_ORDER = TRANSACTION_AMOUNT + " DESC";

        /**
         * Sort by expense name (ascending).
         */
        public static final String EXPENSE_TYPE_SORT_ORDER = EXP_NAME + " ASC";

        /**
         * Sort by vendor description (ascending) with null values at the end.
         */
        public static final String VENDOR_SORT_ORDER = VENDOR_DESCRIPTION + " ASC";
//                "WHEN " + VENDOR_DESCRIPTION + " IS NULL THEN 1 ELSE 0 END, " + VENDOR_DESCRIPTION;
//                VENDOR_DESCRIPTION + " ASC, " + DOING_BUSINESS_AS + " ASC, " + MERCHANT_NAME + " ASC"; // TODO fix this order to handle null/empty

        // ######## END ADDITIONAL SORT ORDER ##### //
    }

}
