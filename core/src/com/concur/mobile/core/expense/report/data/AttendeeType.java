/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models attendee type information passed down in user configuration data.
 */
public class AttendeeType {

    private static final String CLS_TAG = AttendeeType.class.getSimpleName();

    public static final String ALLOW_EDIT_ATN_COUNT = "AllowEditAtnCount";
    public static final String ATN_TYPE_KEY = "AtnTypeKey";
    public static final String ATN_TYPE_NAME = "AtnTypeName";
    public static final String ATN_TYPE_CODE = "AtnTypeCode";
    public static final String FORM_KEY = "FormKey";
    public static final String IS_EXTERNAL = "IsExternal";

    /**
     * Contains whether attendee count editing is enabled.
     */
    public Boolean allowEditAtnCount;

    /**
     * Contains the attendee type key.
     */
    public String atnTypeKey;

    /**
     * Contains the attendee type code.
     */
    public String atnTypeCode;

    /**
     * Contains the attendee type name.
     */
    public String atnTypeName;

    /**
     * Contains the attendee form key.
     */
    public String formKey;

    /**
     * Contains whether this attendee represents an external type.
     */
    public Boolean isExternal;

    /**
     * Sets <code>value</code> based on <code>name</code>.
     * 
     * @param name
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public void handleElement(String name, String value) {

        if (name.equalsIgnoreCase(ALLOW_EDIT_ATN_COUNT)) {
            allowEditAtnCount = Parse.safeParseBoolean(value);
        } else if (name.equalsIgnoreCase(IS_EXTERNAL)) {
            isExternal = Parse.safeParseBoolean(value);
        } else if (name.equalsIgnoreCase(ATN_TYPE_KEY)) {
            atnTypeKey = value;
        } else if (name.equalsIgnoreCase(ATN_TYPE_CODE)) {
            atnTypeCode = value;
        } else if (name.equalsIgnoreCase(ATN_TYPE_NAME)) {
            atnTypeName = value;
        } else if (name.equalsIgnoreCase(FORM_KEY)) {
            formKey = value;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled element name '" + name + "'.");
        }

    }

}
