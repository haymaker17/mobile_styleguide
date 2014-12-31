/**
 * 
 */
package com.concur.mobile.core.travel.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models refundable information.
 */
public class RefundableInfo {

    private static final String CLS_TAG = RefundableInfo.class.getSimpleName();

    private static final String CHECKBOX_DEFAULT = "CheckboxDefault";
    private static final String MESSAGE = "Message";
    private static final String SHOW_CHECKBOX = "ShowCheckbox";

    /**
     * Contains the default state of the refundable checkbox.
     */
    public Boolean checkBoxDefault;

    // TODO: Determine use for this.
    public String message;

    /**
     * Contains whether the checkbox should be displayed.
     */
    public Boolean showCheckBox;

    /**
     * Will handle assigning <code>value</code> to <code>localName</code>.
     * 
     * @param localName
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public void handleElement(String localName, String value) {

        if (localName.equalsIgnoreCase(CHECKBOX_DEFAULT)) {
            checkBoxDefault = Parse.safeParseBoolean(value);
        } else if (localName.equalsIgnoreCase(MESSAGE)) {
            message = value;
        } else if (localName.equalsIgnoreCase(SHOW_CHECKBOX)) {
            showCheckBox = Parse.safeParseBoolean(value);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleElement: unknown tag '" + localName + "' with value '" + value
                    + "'.");
        }

    }

}
