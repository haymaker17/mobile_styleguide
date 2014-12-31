/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class StampApproveResponseRow implements Serializable {

    private static final long serialVersionUID = -2078281625078924900L;
    public String errorDesc;
    public Boolean errorFlag;

    public StampApproveResponseRow() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("ErrorFlag")) {
            errorFlag = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("ErrorDesc")) {
            errorDesc = cleanChars;
        }
    }
}
