/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.database.Cursor;

import com.concur.mobile.core.data.MobileDatabaseHelper;
import com.concur.mobile.platform.util.Parse;

/**
 * Models a search result list item.
 * 
 * @author AndrewK
 */
public class ListItem implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1439140531132320078L;

    private static final String KEY = "Key";
    private static final String TEXT = "Text";

    public static final String DEFAULT_KEY_CURRENCY = "TransactionCurrencyName";
    public static final String DEFAULT_KEY_LOCATION = "LocName";

    public static final char DEFAULT_SEPARATOR = 0x03;// END_OF_TEXT
    public static final char DEFAULT_FIELD_SEPARATOR = 0x02;// START_OF_TEXT

    // Currency MRU
    private String userID;
    private Calendar lastUsed;
    private int lastUseCount;

    /**
     * Contains the list item code.
     */
    public String code;

    /**
     * Contains the list item key.
     */
    public String key;

    /**
     * Contains the list item text, or value.
     */
    public String text;

    /**
     * Contains whether or not this list item is external.
     */
    public Boolean external;

    /**
     * Contains the list item field id for MRU
     */
    public String fieldId;

    /**
     * Contains a list of fields associated with the list item.
     */
    public List<ListItemField> fields;

    public ListItem() {
        // TODO Auto-generated constructor stub
    }

    public ListItem(CurrencyType currencyType) {
        this.text = currencyType.getName();
        this.lastUsed = currencyType.getLastUsed();
        this.userID = currencyType.getUserID();
        this.code = currencyType.getCode();
        this.lastUseCount = currencyType.getLastUseCount();
        // for quick expense field id is always "crnkey"
        this.fieldId = DEFAULT_KEY_CURRENCY;
    }

    public ListItem(String name) {
        this.text = name;
    }

    /**
     * Convenience method for getting the <code>ListItemField</code> value with the given <code>id</code>.
     * 
     * @param id
     *            the ID of the <code>ListItemField</code> whose value to get.
     * @return the <code>ListItemField</code> value with the given <code>id</code>.
     */
    public String getFieldValueById(String id) {
        if (fields != null && id != null) {

            for (ListItemField field : fields) {
                if (field.id.equals(id)) {
                    return field.value;
                }
            }
        }

        return null;
    }

    /**
     * Will handle parsing a <code>ListItem</code> element.
     * 
     * @param localName
     *            contains the XML node name.
     * @param cleanChars
     *            contains the XML node inner text.
     */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase(KEY)) {
            key = cleanChars;
        } else if (localName.equalsIgnoreCase(TEXT)) {
            text = cleanChars;
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

    /**
     * get contentvalues for list item
     * */
    public ContentValues getContentValuesForListItem(ListItem listItem) {
        ContentValues contVals = new ContentValues();
        contVals.put(MobileDatabaseHelper.COLUMN_USER_ID, listItem.getUserID());
        contVals.put(MobileDatabaseHelper.COLUMN_USE_COUNT, listItem.getLastUseCount());
        contVals.put(MobileDatabaseHelper.COLUMN_FIELD_ID, listItem.fieldId);
        contVals.put(MobileDatabaseHelper.COLUMN_LICODE, listItem.code);
        contVals.put(MobileDatabaseHelper.COLUMN_FIELD_VALUE, buildFieldValuesForDB(listItem));
        return contVals;
    }

    /**
     * convert db values to real holders.
     * **/
    public ListItem(Cursor cursor) {
        String userID = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USER_ID));
        String lastUse = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_LAST_USED));
        int count = cursor.getInt(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_USE_COUNT));
        String fieldId = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_FIELD_ID));
        String fieldValue = cursor.getString(cursor.getColumnIndex(MobileDatabaseHelper.COLUMN_FIELD_VALUE));

        this.lastUsed = Parse.parseXMLTimestamp(lastUse);
        this.userID = userID;
        this.lastUseCount = count;
        this.fieldId = fieldId;
        String[] fieldValues = parseFieldValuesFromDB(fieldValue, DEFAULT_SEPARATOR);
        this.key = fieldValues[0];
        if (this.key.equalsIgnoreCase("null")) {
            this.key = null;
        }
        this.code = fieldValues[1];
        this.text = fieldValues[2];

        for (int i = 3; i < fieldValues.length; i++) {
            String listItemField = fieldValues[i];
            String[] listItemFieldValues = parseFieldValuesFromDB(listItemField, DEFAULT_FIELD_SEPARATOR);
            if (this.fields == null) {
                this.fields = new ArrayList<ListItemField>();
            }
            this.fields.add(new ListItemField(listItemFieldValues[0], listItemFieldValues[1]));
        }
    }

    /**
     * Set field values for MRU
     * 
     * @param listItem
     * */
    public String buildFieldValuesForDB(ListItem listItem) {
        StringBuilder strBldr = new StringBuilder("");
        strBldr.append(key);
        strBldr.append(DEFAULT_SEPARATOR);
        strBldr.append(code);
        strBldr.append(DEFAULT_SEPARATOR);
        strBldr.append(text);
        List<ListItemField> fieldItems = listItem.fields;
        if (fieldItems != null && fieldItems.size() > 0) {
            for (ListItemField listItemField : fieldItems) {
                strBldr.append(DEFAULT_SEPARATOR);
                strBldr.append(buildListFieldValueForDB(listItemField));
            }
        }
        return strBldr.toString();
    }

    /**
     * Set List item field values for MRU
     * 
     * @param listItemField
     * */
    public String buildListFieldValueForDB(ListItemField listItemField) {
        StringBuilder strBldr = new StringBuilder("");
        if (listItemField != null) {
            strBldr.append(listItemField.id);
            strBldr.append(DEFAULT_FIELD_SEPARATOR);
            strBldr.append(listItemField.value);
        }
        return strBldr.toString();
    }

    /**
     * Get field values for MRU from passed fieldValue
     * */
    public String[] parseFieldValuesFromDB(String fieldValue, char separator) {
        String splt = Character.toString(separator);
        String[] result = fieldValue.split(splt);
        return result;
    }
}
