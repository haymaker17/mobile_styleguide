/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class Audit implements Serializable {

    private static final long serialVersionUID = 6151196638297771353L;
    public int passed, failed;

    public Audit() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Passed")) {
            passed = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("Failed")) {
            failed = Parse.safeParseInteger(cleanChars);
        }
    }
}
