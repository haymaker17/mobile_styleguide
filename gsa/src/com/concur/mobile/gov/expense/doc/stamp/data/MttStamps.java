/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class MttStamps implements Serializable {

    private static final long serialVersionUID = -8931387674029017656L;
    public String stamp;
    public Boolean returntoRequired;
    public Boolean defaultStamp;

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("stamp")) {
            stamp = cleanChars;
        } else if (localName.equalsIgnoreCase("default_stamp")) {
            defaultStamp = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("returnto_required")) {
            returntoRequired = Parse.safeParseBoolean(cleanChars);
        }
    }

    @Override
    public String toString() {
        // required for dialog list item.
        return stamp;
    }

}
