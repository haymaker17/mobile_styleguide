/**
 * 
 */
package com.concur.mobile.core.expense.report.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;

/**
 * Models expense confirmation message configuration information.
 */
public class ExpenseConfirmation {

    private static final String CLS_TAG = ExpenseConfirmation.class.getSimpleName();

    public static final String CONFIRMATION_KEY = "ConfirmationKey";
    public static final String TEXT = "Text";
    public static final String TITLE = "Title";

    /**
     * Contains the key.
     */
    public String confKey;

    /**
     * Contains the text.
     */
    public String text;

    /**
     * Contains the title.
     */
    public String title;

    /**
     * Sets <code>value</code> based on <code>name</code>.
     * 
     * @param name
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public void handleElement(String name, String value) {

        if (name.equalsIgnoreCase(CONFIRMATION_KEY)) {
            confKey = value;
        } else if (name.equalsIgnoreCase(TEXT)) {
            text = value;
        } else if (name.equalsIgnoreCase(TITLE)) {
            title = value;
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled element name '" + name + "'.");
        }

    }

}
