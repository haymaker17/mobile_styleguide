/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.core.expense.charge.data.CategoryListItem;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a model of a reimbursement currency.
 * 
 * @author AndrewK
 */
public class CurrencyType {

    static final String CLS_TAG = CurrencyType.class.getSimpleName();

    /**
     * Contains the currency code.
     */
    private String code;

    /**
     * Contains the currency name.
     */
    private String name;

    /**
     * Contains the currency number of decimal digits.
     */
    private int numDecimalDigits;

    /**
     * Contains the currency type category.
     */
    private CategoryListItem category;

    // TODO Currency MRU
    private String userID;
    private Calendar lastUsed;
    private int lastUseCount;
    private String polKey;

    /**
     * Constructs an empty instance of <code>CurrencyType</code>
     */
    protected CurrencyType() {
    }

    /**
     * Constructs an instance of <code>CurrencyType</code> with a name.
     * 
     * @param name
     *            contains the currency name.
     */
    protected CurrencyType(String name) {
        this.name = name;
    }

    /**
     * Constructs an instance of <code>CurrencyType</code> with a code, name and number of decimal digits specified.
     * 
     * @param code
     *            the currency code.
     * @param name
     *            the currency name.
     * @param numDecimalDigits
     *            the number of decimal digits.
     */
    public CurrencyType(String code, String name, int numDecimalDigits) {
        this.code = code;
        this.name = name;
        this.numDecimalDigits = numDecimalDigits;
    }

    public CurrencyType(Cursor cursor) {
        String userID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ID));
        String lastUse = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LAST_USED));
        String name = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_CURRENCY));
        String code = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LICODE));
        String polKey = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_POL_KEY));
        int count = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USE_COUNT));

        this.name = name;
        this.lastUsed = Parse.parseXMLTimestamp(lastUse);
        this.userID = userID;
        this.code = code;
        this.lastUseCount = count;
        this.polKey = polKey;
    }

    /**
     * Gets the currency code.
     * 
     * @return the currency code.
     */
    public String getCode() {
        return code;
    }

    /**
     * Gets the currency name.
     * 
     * @return the currency name.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the currency number of decimal digits.
     * 
     * @return the currency number of decimal digits.
     */
    public int getNumDecimalDigits() {
        return numDecimalDigits;
    }

    /**
     * Gets the currency type category.
     * 
     * @return the currency type category if set; otherwise <code>null</code> is returned.
     */
    public CategoryListItem getCategory() {
        return category;
    }

    /**
     * Sets the currency type category.
     * 
     * @param category
     *            the currency type category.
     */
    public void setCategory(CategoryListItem category) {
        this.category = category;
    }

    /**
     * Will parse the XML representation of a list of currency types.
     * 
     * @param responseXml
     *            the XML representation of the currency types.
     * 
     * @return a list of <code>CurrencyType</code> objects.
     */
    public static ArrayList<CurrencyType> parseCurrencyTypeXml(String responseXml) {
        Log.d(Const.LOG_TAG, CLS_TAG + ".parseCurrencyTypeXML: ");

        ArrayList<CurrencyType> currencyTypes = null;

        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser parser = factory.newSAXParser();
            CurrencyTypeSAXHandler handler = new CurrencyTypeSAXHandler();
            parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
            currencyTypes = handler.getCurrencyTypes();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return currencyTypes;
    }

    /**
     * Provides an extension of <code>DefaultHandler</code> to support parsing currency type information.
     * 
     * @author AndrewK
     */
    public static class CurrencyTypeSAXHandler extends DefaultHandler {

        private static final String CLS_TAG = CurrencyType.CLS_TAG + "." + CurrencyTypeSAXHandler.class.getSimpleName();

        private static final String CURRENCY_TYPE_LIST = "ArrayOfCurrency";

        private static final String CURRENCY_TYPE = "Currency";

        private static final String CRN_CODE = "CrnCode";

        private static final String CRN_NAME = "CrnName";

        private static final String DECIMAL_DIGITS = "DecimalDigits";

        // Fields to help parsing
        private StringBuilder chars = new StringBuilder();

        /**
         * Contains a reference to a list of <code>CurrencyType</code> objects that have been parsed.
         */
        private ArrayList<CurrencyType> currencyTypes = new ArrayList<CurrencyType>();

        /**
         * Contains a reference to the currency type currently being built.
         */
        private CurrencyType currencyType;

        /**
         * Contains whether or not this parser has handled an element tag.
         */
        protected boolean elementHandled;

        /**
         * Gets the list of <code>CurrentyType</code> objects that have been parsed.
         * 
         * @return the list of parsed <code>CurrencyType</code> objects.
         */
        public ArrayList<CurrencyType> getCurrencyTypes() {
            return currencyTypes;
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
            if (localName.equalsIgnoreCase(CURRENCY_TYPE_LIST)) {
                currencyTypes = new ArrayList<CurrencyType>();
                elementHandled = true;
            } else if (localName.equalsIgnoreCase(CURRENCY_TYPE)) {
                currencyType = new CurrencyType();
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
            if (currencyType != null) {
                if (localName.equalsIgnoreCase(CRN_CODE)) {
                    currencyType.code = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CRN_NAME)) {
                    currencyType.name = chars.toString().trim();
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(DECIMAL_DIGITS)) {
                    String decimalDigitsStr = chars.toString().trim();
                    Integer decimalDigits = Parse.safeParseInteger(decimalDigitsStr);
                    if (decimalDigits != null) {
                        currencyType.numDecimalDigits = decimalDigits;
                    } else {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unable to parse decimal digits value '"
                                + decimalDigitsStr + "'.");
                    }
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CURRENCY_TYPE)) {
                    currencyTypes.add(currencyType);
                    currencyType = null;
                    elementHandled = true;
                } else if (localName.equalsIgnoreCase(CURRENCY_TYPE_LIST)) {
                    // Finished parsing.
                    elementHandled = true;
                } else {
                    // Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: unhandled tag '" + localName + "'.");
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".endElement: null current currency type!");
            }
            // Clear out the stored element values.
            chars.setLength(0);
        }

    }

    public void setLastUsed(Calendar now) {
        this.lastUsed = now;
    }

    public Calendar getLastUsed() {
        return lastUsed;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getLastUseCount() {
        return lastUseCount;
    }

    public void setLastUseCount(int lastUseCount) {
        this.lastUseCount = lastUseCount;
    }

    public String getPolKey() {
        return polKey;
    }

    public void setPolKey(String polKey) {
        this.polKey = polKey;
    }

    public ContentValues getContentValuesForCurrType(CurrencyType currencyType) {
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, currencyType.getUserID());
        contVals.put(MobileDatabaseHelper.COLUMN_LICODE, currencyType.getCode());
        contVals.put(MobileDatabaseHelper.COLUMN_CURRENCY, currencyType.getName());
        contVals.put(MobileDatabaseHelper.COLUMN_POL_KEY, currencyType.getPolKey());
        contVals.put(MobileDatabaseHelper.COLUMN_USE_COUNT, currencyType.getLastUseCount());
        return contVals;
    }
}
