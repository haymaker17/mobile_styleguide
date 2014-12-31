/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.core.expense.charge.data.ExpenseTypeCategory;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * An implementation of <code>IExpenseType</code>
 * 
 * @author AndrewK
 */
public class ExpenseType {

    private static final String CLS_TAG = ExpenseType.class.getSimpleName();

    public enum Access {
        REGULAR("REGULAR"), PARENT("PARENT"), CHILD("CHILD");

        private final String value;

        Access(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static Access findByValue(String text) {
            if (text != null) {
                for (Access access : Access.values()) {
                    if (text.equalsIgnoreCase(access.value)) {
                        return access;
                    }
                }
            }
            return null;
        }
    };

    /**
     * Contains the expense type name.
     */
    public String name;

    /**
     * Contains the expense type key.
     */
    public String key;

    /**
     * Contains the expense type parent name.
     */
    public String parentName;

    /**
     * Contains the expense type parent key.
     */
    public String parentKey;

    /**
     * Contains the parent expense type.
     */
    public ExpenseType parent;

    /**
     * Contains the form key for this expense type.
     */
    public String formKey;

    /**
     * Contains the type of access.
     */
    public Access access;

    /**
     * Contains the expense type expense code.
     */
    public String expCode;

    /**
     * Contains the list of expense types that are not allowed for itemizations of this expense type.
     */
    public String[] unallowedItemizationExpenseKeys;

    /**
     * Contains the itemization form key.
     */
    public String itemizeFormKey;

    /**
     * Contains the itemization style.
     */
    public String itemizeStyle;

    /**
     * Contains the itemization type.
     */
    public String itemizeType;

    /**
     * Contains the vendor list key.
     */
    public String vendorListKey;

    /**
     * Contains whether the expense type supports attendees.
     */
    public Boolean supportsAttendees;

    /**
     * Contains the attendee keys for attendee types that are not allowed for this expense type
     */
    public String[] unallowedAttendeeTypeKeys;

    /**
     * Contains whether the expense type permits editing of attendee amounts not filled in by the server.
     */
    public Boolean allowEditAtnAmt;

    /**
     * Contains whether the expense type permits editing of attendee counts not filled in by the server.
     */
    public Boolean allowEditAtnCount;

    /**
     * Contains whether no shows are permitted.
     */
    public Boolean allowNoShows;

    /**
     * Contains whether attendee amounts should be displayed.
     */
    public Boolean displayAtnAmts;

    /**
     * Contains whether the user can be the default attendee.
     */
    public Boolean userAsAtnDefault;

    /**
     * Contains whether or not the posted amount has been calculated.
     */
    public Boolean hasPostAmtCalc;

    /**
     * Contains whether the VAT form available or not.
     */
    public Boolean hasTaxForm;

    /** These variables are used for Database only for MRU MOB-8452 */
    public String userID, polKey;
    public int userCount;
    public Calendar lastUsed;
    /**
     * Contains a map from category expense type names to icons.
     */
    public static final HashMap<String, Integer> categoryIconMap = new HashMap<String, Integer>();

    static {
        categoryIconMap.put("COMMU", R.drawable.phone_24);
        categoryIconMap.put("ENTRT", R.drawable.entertainment_24);
        categoryIconMap.put("LODGE", R.drawable.hotel_24);
        categoryIconMap.put("MEALS", R.drawable.dining_24);
        categoryIconMap.put("MLENT", R.drawable.dining_24);
        categoryIconMap.put("OTHER", R.drawable.help_24);

        // These following 4 require real icons...just using the help for now.
        categoryIconMap.put("MRKNG", R.drawable.help_24);
        categoryIconMap.put("OFFIC", R.drawable.help_24);

        categoryIconMap.put("PROMO", R.drawable.business_promotions_24);
        categoryIconMap.put("TRANS", R.drawable.transportation_24);
        categoryIconMap.put("TRAVL", R.drawable.transportation_24);
        categoryIconMap.put("CAREX", R.drawable.rental_car_24);
    }

    public int getuseCount() {
        return userCount;
    }

    public void setuseCount(int count) {
        this.userCount = count;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getPolKey() {
        return polKey;
    }

    public void setPolKey(String polKey) {
        this.polKey = polKey;
    }

    public Calendar getLastUsed() {
        return lastUsed;
    }

    public void setLastUsed(Calendar lastUsed) {
        this.lastUsed = lastUsed;
    }

    public Boolean getHasTaxForm() {
        return hasTaxForm;
    }

    public void setHasTaxForm(Boolean hasTaxForm) {
        this.hasTaxForm = hasTaxForm;
    }

    /**
     * Constructs an instance of <code>ExpenseType</code>. MOB-8452 required public constructor.
     */
    private ExpenseType() {
        this.access = Access.REGULAR;
    }

    /**
     * Constructs an instance of <code>ExpenseType</code> with the name <code>name</code>.
     * 
     * @param name
     *            the expense type name.
     */
    public ExpenseType(String name) {
        this(name, null);
    }

    /**
     * Constructs an instance of <code>ExpenseType</code> with an expense name and key.
     * 
     * @param name
     *            the expense type name.
     * @param key
     *            the expense type key.
     */
    public ExpenseType(String name, String key) {
        this(name, key, null, null);
    }

    /**
     * Constructs an instance of <code>ExpenseType</code> with an expense name, key and parent.
     * 
     * @param name
     *            the expense type name.
     * @param key
     *            the expense type key.
     * @param parent
     *            the parent expense type.
     */
    public ExpenseType(String name, String key, ExpenseType parent) {
        this(name, key, null, parent);
    }

    /**
     * Constructs an instance of <code>ExpenseType</code> with an expense name, key and parent.
     * 
     * @param name
     *            the expense type name.
     * @param key
     *            the expense type key.
     * @param formKey
     *            the expense form key.
     * @param parent
     *            the parent expense type.
     */
    public ExpenseType(String name, String key, String formKey, ExpenseType parent) {
        this.access = Access.REGULAR;
        this.name = name;
        this.key = key;
        this.formKey = formKey;
        this.parent = parent;
    }

    public ExpenseType(Cursor cursor) {

        String userID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ID));
        String polKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_POL_KEY));
        String lastUsed = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LAST_USED));
        String key = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_KEY));
        String name = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_NAME));
        String parentKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_PARENT_KEY));
        String parentName = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_PARENT_NAME));
        String formKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_FORM_KEY));
        String expCode = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_CODE));
        String itemizationKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_ITEMIZATION_KEY));
        String itemizationType = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_ITEMIZATION_TYPE));
        String itemizationStyle = cursor
                .getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_ITEMIZATION_STYLE));
        String vendorListKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_VENDOR_LIST_KEY));
        Integer supportAttendee = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_SUPPORT_ATTENDEE));
        Integer editAtnAmt = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EDIT_ATN_AMT));
        Integer editAtnCnt = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EDIT_ATN_COUNT));
        Integer allowNoShow = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_ALLOW_NO_SHOWS));
        Integer displayAtnAmt = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_DISPLAY_ATN_AMTS));
        Integer usrAtnDefault = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ATN_DEFAULT));
        Integer hasPostAmtCalc = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_HAS_POST_AMT_CALC));
        Integer hasVatForm = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_HAS_TAX_FORM));
        String access = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_EXP_TYPE_ACCESS));
        String unallowItemization = cursor.getString(cursor
                .getColumnIndex(MobileDatabaseHelper.COLUMN_UNALLOW_ITEMIZATION));
        String unallowAttendee = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_UNALLOW_ATTENDEE));
        int count = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USE_COUNT));

        this.name = name;
        this.key = key;
        this.parentKey = parentKey;
        this.parentName = parentName;
        this.formKey = formKey;
        this.expCode = expCode;
        this.itemizeFormKey = itemizationKey;
        this.itemizeStyle = itemizationStyle;
        this.itemizeType = itemizationType;
        this.vendorListKey = vendorListKey;
        this.supportsAttendees = FormatUtil.getValueFromInt(supportAttendee);
        this.allowEditAtnAmt = FormatUtil.getValueFromInt(editAtnAmt);
        this.allowEditAtnCount = FormatUtil.getValueFromInt(editAtnCnt);
        this.allowNoShows = FormatUtil.getValueFromInt(allowNoShow);
        this.userAsAtnDefault = FormatUtil.getValueFromInt(usrAtnDefault);
        this.hasPostAmtCalc = FormatUtil.getValueFromInt(hasPostAmtCalc);
        this.hasTaxForm = FormatUtil.getValueFromInt(hasVatForm);
        this.displayAtnAmts = FormatUtil.getValueFromInt(displayAtnAmt);
        this.unallowedAttendeeTypeKeys = FormatUtil.convertStringToArray(unallowAttendee);
        this.unallowedItemizationExpenseKeys = FormatUtil.convertStringToArray(unallowItemization);
        this.access = Access.findByValue(access);
        this.userID = userID;
        this.polKey = polKey;
        this.lastUsed = Parse.parseXMLTimestamp(lastUsed);
        this.userCount = count;
        // TODO parent
    }

    public String getName() {
        return name;
    }

    public String getKey() {
        return key;
    }

    public String getFormKey() {
        return formKey;
    }

    public void setParentExpenseType(ExpenseType parent) {
        this.parent = parent;
    }

    public ExpenseType getParentExpenseType() {
        return parent;
    }

    /**
     * find expense type using polKey and expKey.
     * 
     * @param polKey
     *            : policy key from expense report.
     * @param expKey
     *            : expense type key
     * @return null : if no expense type found.
     * */
    public static ExpenseType findExpenseType(final String polKey, final String expKey) {
        ExpenseType result = null;

        if (polKey != null && expKey != null) {
            ConcurCore concurMobile = (ConcurCore) ConcurCore.getContext();
            IExpenseEntryCache expEntCache = concurMobile.getExpenseEntryCache();
            List<ExpenseType> expTypes = expEntCache.getExpenseTypes(polKey);

            if (expTypes != null) {
                for (ExpenseType et : expTypes) {
                    if (expKey.equals(et.key)) {
                        result = et;
                        break;
                    }
                }
            }
        }

        return result;
    }

    /**
     * Create ContentValues object for SQL query insertion and return it.
     * 
     * @param userId
     *            : logged in user id
     * @param expenseType
     *            : ExpenseType Object.
     * @return : ContentValues object having key value pair.
     */
    public ContentValues getContentValuesForExpType(ExpenseType expenseType) {
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, expenseType.userID);
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_KEY, expenseType.key);
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_NAME, expenseType.name);
        contVals.put(MobileDatabaseHelper.COLUMN_POL_KEY, expenseType.getPolKey());
        contVals.put(MobileDatabaseHelper.COLUMN_PARENT_KEY, expenseType.parentKey);
        contVals.put(MobileDatabaseHelper.COLUMN_PARENT_NAME, expenseType.parentName);
        contVals.put(MobileDatabaseHelper.COLUMN_FORM_KEY, expenseType.formKey);
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_CODE, expenseType.expCode);
        contVals.put(MobileDatabaseHelper.COLUMN_ITEMIZATION_KEY, expenseType.itemizeFormKey);
        contVals.put(MobileDatabaseHelper.COLUMN_ITEMIZATION_TYPE, expenseType.itemizeType);
        contVals.put(MobileDatabaseHelper.COLUMN_ITEMIZATION_STYLE, expenseType.itemizeStyle);
        contVals.put(MobileDatabaseHelper.COLUMN_VENDOR_LIST_KEY, expenseType.vendorListKey);

        Integer intResult = FormatUtil.convertBooleanIntoInt(expenseType.allowEditAtnAmt);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_EDIT_ATN_AMT, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_EDIT_ATN_AMT);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.supportsAttendees);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_SUPPORT_ATTENDEE, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_SUPPORT_ATTENDEE);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.allowEditAtnCount);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_EDIT_ATN_COUNT, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_EDIT_ATN_COUNT);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.allowNoShows);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_ALLOW_NO_SHOWS, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_ALLOW_NO_SHOWS);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.displayAtnAmts);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_DISPLAY_ATN_AMTS, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_DISPLAY_ATN_AMTS);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.userAsAtnDefault);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_USER_ATN_DEFAULT, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_USER_ATN_DEFAULT);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.hasPostAmtCalc);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_HAS_POST_AMT_CALC, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_HAS_POST_AMT_CALC);
        }

        intResult = FormatUtil.convertBooleanIntoInt(expenseType.hasTaxForm);
        if (intResult != null) {
            contVals.put(MobileDatabaseHelper.COLUMN_HAS_TAX_FORM, intResult);
        } else {
            contVals.putNull(MobileDatabaseHelper.COLUMN_HAS_TAX_FORM);
        }
        contVals.put(MobileDatabaseHelper.COLUMN_EXP_TYPE_ACCESS, expenseType.access.getValue());
        contVals.put(MobileDatabaseHelper.COLUMN_UNALLOW_ITEMIZATION,
                FormatUtil.convertArrayIntoString(expenseType.unallowedItemizationExpenseKeys));
        contVals.put(MobileDatabaseHelper.COLUMN_UNALLOW_ATTENDEE,
                FormatUtil.convertArrayIntoString(expenseType.unallowedAttendeeTypeKeys));
        contVals.put(MobileDatabaseHelper.COLUMN_USE_COUNT, expenseType.getuseCount());
        return contVals;
    }

    /**
     * Will parse the XML representation of a list of expense types.
     * 
     * @param responseXml
     *            the XML representation of the expense types.
     * 
     * @return a list of <code>ExpenseType</code> objects.
     */
    public static ArrayList<ExpenseType> parseExpenseTypeXml(String responseXml) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".parseExpenseTypeXML: ");

        ArrayList<ExpenseType> expenseTypes = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            ExpenseTypeSAXHandler handler = new ExpenseTypeSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            expenseTypes = handler.getExpenseTypes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return expenseTypes;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing expense type information.
     * 
     * @author AndrewK
     */
    public static class ExpenseTypeSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = ExpenseType.CLS_TAG + "." + ExpenseTypeSAXHandler.class.getSimpleName();

        private static final String EXPENSE_TYPE_LIST = "ArrayOfExpenseType";

        private static final String EXPENSE_TYPE = "ExpenseType";

        private static final String EXPENSE_KEY = "ExpKey";

        private static final String FORM_KEY = "FormKey";

        private static final String EXPENSE_NAME = "ExpName";

        private static final String PARENT_EXPENSE_NAME = "ParentExpName";

        private static final String PARENT_EXPENSE_KEY = "ParentExpKey";

        private static final String ACCESS = "Access";

        private static final String EXP_CODE = "ExpCode";

        private static final String ITEMIZATIONS_UNALLOW_FOR_EXP_KEYS = "ItemizationUnallowExpKeys";

        private static final String ITEMIZE_FORM_KEY = "ItemizeFormKey";

        private static final String ITEMIZE_STYLE = "ItemizeStyle";

        private static final String ITEMIZE_TYPE = "ItemizeType";

        private static final String SUPPORTS_ATTENDEES = "SupportsAttendees";

        private static final String UNALLOWED_ATTENDEE_TYPES = "UnallowAtnTypeKeys";

        private static final String VENDOR_LIST_KEY = "VendorListKey";

        private static final String ALLOW_EDIT_ATN_AMT = "AllowEditAtnAmt";

        private static final String ALLOW_EDIT_ATN_COUNT = "AllowEditAtnCount";

        private static final String ALLOW_NO_SHOWS = "AllowNoShows";

        private static final String DISPLAY_ATN_AMOUNTS = "DisplayAtnAmounts";

        private static final String USER_AS_ATN_DEFAULT = "UserAsAtnDefault";

        private static final String HAS_POST_AMT_CALC = "HasPostAmtCalc";

        private static final String HAS_TAX_FORM = "HasTaxForm";

        private static final String USER_ID = "userID";
        private static final String USER_POL_KEY = "polKey";
        private static final String USE_COUNT = "userCount";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a map from expense keys to expense type categories.
         */
        private HashMap<String, ExpenseTypeCategory> parentExpMap = new HashMap<String, ExpenseTypeCategory>();

        /**
         * Contains a reference to a list of <code>ExpenseType</code> objects that have been parsed.
         */
        private ArrayList<ExpenseType> expenseTypes = new ArrayList<ExpenseType>();

        /**
         * Contains a reference to the expense type currently being built.
         */
        private ExpenseType expenseType;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets an icon resource id based on a category expense type.
         * 
         * @param expType
         *            the category expense type.
         * 
         * @return an icon resource id for a category expense type.
         */
        private int getIconResIdForExpType(String expType) {

            int iconResId = R.drawable.help_24;
            if (categoryIconMap.containsKey(expType)) {
                iconResId = categoryIconMap.get(expType);
            } else {
                if (expType != null && expType.length() == 0) {
                    iconResId = 0;
                }
            }
            return iconResId;
        }

        /**
         * Gets the list of <code>ExpenseReportComment</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>ExpenseReportComment</code> objects.
         */
        public ArrayList<ExpenseType> getExpenseTypes() {
            return expenseTypes;
        }

        /**
         * Will perform any post-parsing clean-up.
         */
        public void cleanUp() {
            parentExpMap.clear();
            parentExpMap = null;
            elementHandled = true;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
         */
        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);
            chars.append(ch, start, length);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#startElement(java.lang.String, java.lang.String, java.lang.String,
         * org.xml.sax.Attributes)
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

            elementHandled = false;

            super.startElement(uri, localName, qName, attributes);

            if (localName.equalsIgnoreCase(EXPENSE_TYPE_LIST)) {
                expenseTypes = new ArrayList<ExpenseType>();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(EXPENSE_TYPE)) {
                expenseType = new ExpenseType();
                chars.setLength(0);
                elementHandled = true;
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.xml.sax.helpers.DefaultHandler#endElement(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {

            elementHandled = false;

            super.endElement(uri, localName, qName);

            if (expenseType != null) {
                if (localName.equalsIgnoreCase(EXPENSE_KEY)) {
                    expenseType.key = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(FORM_KEY)) {
                    expenseType.formKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXPENSE_NAME)) {
                    expenseType.name = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_EXPENSE_NAME)) {
                    expenseType.parentName = chars.toString().trim();
                    setupParentChildExpenseType();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(PARENT_EXPENSE_KEY)) {
                    expenseType.parentKey = chars.toString().trim();
                    setupParentChildExpenseType();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ACCESS)) {
                    String access = chars.toString().trim();
                    if (access.equalsIgnoreCase("CH")) {
                        expenseType.access = Access.CHILD;
                    } else if (access.equalsIgnoreCase("PA")) {
                        expenseType.access = Access.PARENT;
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXP_CODE)) {
                    expenseType.expCode = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ITEMIZATIONS_UNALLOW_FOR_EXP_KEYS)) {
                    String expTypeKeys = chars.toString().trim();
                    expenseType.unallowedItemizationExpenseKeys = expTypeKeys.split(",");
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ITEMIZE_FORM_KEY)) {
                    expenseType.itemizeFormKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ITEMIZE_STYLE)) {
                    expenseType.itemizeStyle = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ITEMIZE_TYPE)) {
                    expenseType.itemizeType = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(USER_ID)) {
                    expenseType.userID = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(USER_POL_KEY)) {
                    expenseType.polKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(USE_COUNT)) {
                    expenseType.userCount = Parse.safeParseInteger(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(SUPPORTS_ATTENDEES)) {
                    expenseType.supportsAttendees = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(UNALLOWED_ATTENDEE_TYPES)) {
                    // Parse the comma-delimited string and store the list
                    String atnTypeKeys = chars.toString().trim();
                    expenseType.unallowedAttendeeTypeKeys = atnTypeKeys.split(",");
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(VENDOR_LIST_KEY)) {
                    expenseType.vendorListKey = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ALLOW_EDIT_ATN_AMT)) {
                    expenseType.allowEditAtnAmt = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ALLOW_EDIT_ATN_COUNT)) {
                    expenseType.allowEditAtnCount = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(ALLOW_NO_SHOWS)) {
                    expenseType.allowNoShows = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(DISPLAY_ATN_AMOUNTS)) {
                    expenseType.displayAtnAmts = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(USER_AS_ATN_DEFAULT)) {
                    expenseType.userAsAtnDefault = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_POST_AMT_CALC)) {
                    expenseType.hasPostAmtCalc = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(HAS_TAX_FORM)) {
                    expenseType.hasTaxForm = Parse.safeParseBoolean(chars.toString().trim());
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXPENSE_TYPE)) {
                    expenseTypes.add(expenseType);
                    expenseType = null;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(EXPENSE_TYPE_LIST)) {
                    // Post process the list of parsed expense types.
                    postProcessList();
                    // Finished parsing.
                    cleanUp();
                } else if (this.getClass().equals(ExpenseTypeSAXHandler.class)) {
                    // Log.w(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled '" + localName + "' with value '" +
                    // chars.toString() + "'.");
                }
            }

            // Clear out the stored element values.
            chars.setLength(0);
        }

        /**
         * Will post process the list of parsed expense types by ensuring all categories are alphabetically ordered and then those
         * expenses within each category are ordered.
         */
        public void postProcessList() {
            if (expenseTypes != null && expenseTypes.size() > 0) {
                ExpenseTypeComparator expTypeComparator = new ExpenseTypeComparator();
                ArrayList<ExpenseTypeCategory> expTypeCatList = new ArrayList<ExpenseTypeCategory>();
                // First, extract all expense type categories and place into
                // separate list.
                for (ExpenseType expType : expenseTypes) {
                    if (expType instanceof ExpenseTypeCategory) {
                        expTypeCatList.add((ExpenseTypeCategory) expType);
                    }
                }
                // Second, alphabetically order list of cateogories.
                if (expTypeCatList.size() > 0) {
                    Collections.sort(expTypeCatList, expTypeComparator);
                }
                // Third, alphabetically order all expense types within categories and add category
                // and ordered expense type entries into a new list.
                if (expTypeCatList.size() > 0) {
                    ArrayList<ExpenseType> postProcessedTypes = new ArrayList<ExpenseType>();
                    for (ExpenseTypeCategory expTypeCat : expTypeCatList) {
                        postProcessedTypes.add(expTypeCat);
                        Collections.sort(expTypeCat.getExpenseTypes(), expTypeComparator);
                        postProcessedTypes.addAll(expTypeCat.getExpenseTypes());
                    }
                    // Reset the new list.
                    expenseTypes = postProcessedTypes;
                }
            }
        }

        /**
         * Will examine the parsed information thus far and set up a parent child relationship. This method will determine if both
         * <code>ParentExpName</code> and <code>ParentExpType</code> have been parsed and if so, will then set up the expense type
         * parent/child relationship.
         */
        private void setupParentChildExpenseType() {
            // Both have to be specified prior to setting up this relationship.
            if (expenseType.parentKey != null && expenseType.parentName != null) {
                ExpenseTypeCategory expTypeCat = parentExpMap.get(expenseType.parentKey);
                if (expTypeCat == null) {
                    // TODO: Pull in real icons!
                    expTypeCat = new ExpenseTypeCategory(expenseType.parentName, expenseType.parentKey,
                            getIconResIdForExpType(expenseType.parentKey));
                    expenseTypes.add(expTypeCat);
                    parentExpMap.put(expenseType.parentKey, expTypeCat);
                }
                expTypeCat.addExpenseType(expenseType);
            }
        }

        /**
         * Will serialize to XML a list of <code>ExpenseType</code> objects.
         * 
         * @param strBldr
         *            the string builder to write the serialized expense types.
         * @param expTypes
         *            the list of <code>ExpenseType</code> objects to serialize.
         */
        public static void serializeToXML(StringBuilder strBldr, List<ExpenseType> expTypes) {
            if (strBldr != null) {
                strBldr.append('<');
                strBldr.append(EXPENSE_TYPE_LIST);
                strBldr.append('>');
                if (expTypes != null) {
                    for (ExpenseType expType : expTypes) {
                        // Don't serialize category objects...they're created automatically upon re-parse.
                        if (!(expType instanceof ExpenseTypeCategory)) {
                            serializeToXML(strBldr, expType);
                        }
                    }
                }
                strBldr.append("</");
                strBldr.append(EXPENSE_TYPE_LIST);
                strBldr.append('>');
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeAllToXML: strBldr is null!");
            }
        }

        /**
         * Will serialize to XML an instance of <code>ExpenseType</code>.
         * 
         * @param strBldr
         *            the string builder to write the serialized expense type.
         * @param expType
         *            the expense type to serialize.
         */
        public static void serializeToXML(StringBuilder strBldr, ExpenseType expType) {
            if (strBldr != null) {
                strBldr.append('<');
                strBldr.append(EXPENSE_TYPE);
                strBldr.append('>');

                // EXPENSE_KEY
                ViewUtil.addXmlElement(strBldr, EXPENSE_KEY, expType.key);
                // FORM_KEY
                ViewUtil.addXmlElement(strBldr, FORM_KEY, expType.formKey);
                // EXPENSE_NAME
                ViewUtil.addXmlElement(strBldr, EXPENSE_NAME, expType.name);
                // PARENT_EXPENSE_NAME
                ViewUtil.addXmlElement(strBldr, PARENT_EXPENSE_NAME, expType.parentName);
                // PARENT_EXPENSE_KEY
                ViewUtil.addXmlElement(strBldr, PARENT_EXPENSE_KEY, expType.parentKey);
                // ACCESS
                switch (expType.access) {
                case CHILD: {
                    ViewUtil.addXmlElement(strBldr, ACCESS, "CH");
                    break;
                }
                case PARENT: {
                    ViewUtil.addXmlElement(strBldr, ACCESS, "PA");
                    break;
                }
                }
                // EXP_CODE
                ViewUtil.addXmlElement(strBldr, EXP_CODE, expType.expCode);
                // ITEMIZATIONS_UNALLOWED_FOR_EXP_KEYS
                if (expType.unallowedItemizationExpenseKeys != null) {
                    StringBuilder sb = new StringBuilder();
                    int size = expType.unallowedItemizationExpenseKeys.length;
                    for (int i = 0; i < size; i++) {
                        sb.append(expType.unallowedItemizationExpenseKeys[i]);
                        if (i < size - 1) {
                            sb.append(',');
                        }
                    }
                    ViewUtil.addXmlElement(strBldr, ITEMIZATIONS_UNALLOW_FOR_EXP_KEYS, sb.toString());
                }
                // ITEMIZE_FORM_KEY
                ViewUtil.addXmlElement(strBldr, ITEMIZE_FORM_KEY, expType.itemizeFormKey);
                // ITEMIZE_STYLE
                ViewUtil.addXmlElement(strBldr, ITEMIZE_STYLE, expType.itemizeStyle);
                // ITEMIZE_TYPE
                ViewUtil.addXmlElement(strBldr, ITEMIZE_TYPE, expType.itemizeType);

                // USER_AS_ATN_DEFAULT
                ViewUtil.addXmlElement(strBldr, USER_ID, expType.getUserID());
                // USER_AS_ATN_DEFAULT
                ViewUtil.addXmlElement(strBldr, USER_POL_KEY, expType.getPolKey());
                // USER_AS_ATN_DEFAULT
                ViewUtil.addXmlElement(strBldr, USE_COUNT, expType.getuseCount());
                // USER_AS_ATN_DEFAULT
                // ViewUtil.addXmlElement(strBldr, LAST_USED, FormatUtil.XML_DF.format(expType.getLastUsed()));

                // SUPPORTS_ATTENDEES
                ViewUtil.addXmlElementYN(strBldr, SUPPORTS_ATTENDEES, expType.supportsAttendees);

                // UNALLOWED_ATTENDEE_TYPES
                if (expType.unallowedAttendeeTypeKeys != null) {
                    StringBuilder sb = new StringBuilder();
                    int size = expType.unallowedAttendeeTypeKeys.length;
                    for (int i = 0; i < size; i++) {
                        sb.append(expType.unallowedAttendeeTypeKeys[i]);
                        if (i < size - 1) {
                            sb.append(',');
                        }
                    }
                    ViewUtil.addXmlElement(strBldr, UNALLOWED_ATTENDEE_TYPES, sb.toString());
                }
                // VENDOR_LIST_KEY
                ViewUtil.addXmlElement(strBldr, VENDOR_LIST_KEY, expType.vendorListKey);
                // ALLOW_EDIT_ATN_AMT
                ViewUtil.addXmlElementYN(strBldr, ALLOW_EDIT_ATN_AMT, expType.allowEditAtnAmt);
                // ALLOW_EDIT_ATN_COUNT
                ViewUtil.addXmlElementYN(strBldr, ALLOW_EDIT_ATN_COUNT, expType.allowEditAtnCount);
                // ALLOW_NO_SHOWS
                ViewUtil.addXmlElementYN(strBldr, ALLOW_NO_SHOWS, expType.allowNoShows);
                // DISPLAY_ATN_AMOUNTS
                ViewUtil.addXmlElementYN(strBldr, DISPLAY_ATN_AMOUNTS, expType.displayAtnAmts);
                // USER_AS_ATN_DEFAULT
                ViewUtil.addXmlElementYN(strBldr, USER_AS_ATN_DEFAULT, expType.userAsAtnDefault);
                // HAS_POST_AMT_CALC
                ViewUtil.addXmlElementYN(strBldr, HAS_POST_AMT_CALC, expType.hasPostAmtCalc);
                // HAS_TAX_FORM
                ViewUtil.addXmlElementYN(strBldr, HAS_TAX_FORM, expType.hasTaxForm);

                strBldr.append("</");
                strBldr.append(EXPENSE_TYPE);
                strBldr.append('>');
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".serializeToXML: strBldr is null!");
            }
        }

    }

    /**
     * Filter out itemization expense type list.
     * 
     * @param expTypes
     *            : List of expense types
     * @param filteredObject
     *            : Filtered expense objected.
     * @return list of Filtered expense type to show as itemization list.
     */
    public List<ExpenseType> filterExpensetype(List<ExpenseType> expTypes, ExpenseType filteredObject) {
        List<ExpenseType> resultantList = new ArrayList<ExpenseType>();
        if (filteredObject != null) {
            // MOB-13754
            // If 'unallowedItemizationExpenseKeys' is null, then we won't block non-parent expense
            // types from being able to be selected as an itemization expense type.
            String[] array = null;
            if (filteredObject.unallowedItemizationExpenseKeys != null) {
                array = filteredObject.unallowedItemizationExpenseKeys;
            } else {
                array = new String[0];
            }
            List<String> arrayList = Arrays.asList(array);
            for (int j = 0; j < expTypes.size(); j++) {
                ExpenseType l = expTypes.get(j);
                if (l.access != null && l.access != ExpenseType.Access.PARENT) {
                    if (l.key != null) {
                        if (!(arrayList.contains(l.key))) {
                            resultantList.add(l);
                        }
                    }
                }// check parent or child access
            }// end of for loop
        }// end of outermost if
         // MOB-12732
         // We always return the resultantList. If it's empty, we don't want to show anything, even in MRU.
        return resultantList;
    }

    /**
     * Filters out the array of <code>filteredExpCodes</code> from the list of <code>ExpenseTypes</code>.
     * 
     * @param expTypes
     *            the list of <code>ExpenseType</code> to filer.
     * @param filteredExpCodes
     *            an array of expense codes to filter out from the given <code>expTypes</code>.
     * @return a list of <code>ExpenseType</code> that does <strong>not</code> contain any expense codes from the
     *         <code>filteredExpCodes</code> array.
     */
    public static List<ExpenseType> filterExpenseTypes(List<ExpenseType> expTypes, String... filteredExpCodes) {

        List<ExpenseType> resultantList = new ArrayList<ExpenseType>();
        if (filteredExpCodes != null) {
            List<String> filteredList = Arrays.asList(filteredExpCodes);
            for (ExpenseType exp : expTypes) {
                if (!filteredList.contains(exp.expCode)) {
                    resultantList.add(exp);
                }
            }// end of for loop
        }// end of outermost if

        Log.d(CLS_TAG, "size of expense type after filter out exp codes: " + resultantList.size());
        return resultantList;
    }
}
