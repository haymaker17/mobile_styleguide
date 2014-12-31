/**
 * 
 */
package com.concur.mobile.core.expense.data;

import android.util.Log;

import com.concur.mobile.core.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * Models expense policy configuration information.
 */
public class ExpensePolicy {

    public static final String CLS_TAG = ExpensePolicy.class.getSimpleName();

    public static final String POL_KEY = "PolKey";
    public static final String APPROVAL_CONFIRMATION_KEY = "ApprovalConfirmationKey";
    public static final String SUBMIT_CONFIRMATION_KEY = "SubmitConfirmationKey";
    public static final String SUPPORTS_IMAGING = "SupportsImaging";

    /**
     * Contains the expense report policy key.
     */
    public String polKey;

    /**
     * Contains the approval confirmation key.
     */
    public String appConfKey;

    /**
     * Contains the submit confirmation key.
     */
    public String subConfKey;

    /**
     * Contains whether this expense policy configuration supports imaging.
     */
    public Boolean supportsImaging;

    /**
     * Sets <code>value</code> based on <code>name</code>.
     * 
     * @param name
     *            the attribute name.
     * @param value
     *            the attribute value.
     */
    public void handleElement(String name, String value) {
        if (name.equalsIgnoreCase(POL_KEY)) {
            polKey = value;
        } else if (name.equalsIgnoreCase(APPROVAL_CONFIRMATION_KEY)) {
            appConfKey = value;
        } else if (name.equalsIgnoreCase(SUBMIT_CONFIRMATION_KEY)) {
            subConfKey = value;
        } else if (name.equalsIgnoreCase(SUPPORTS_IMAGING)) {
            supportsImaging = Parse.safeParseBoolean(value);
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG + ".handleElement: unhandled element name '" + name + "'.");
        }
    }

}
