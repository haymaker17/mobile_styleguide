/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;

public class ReasonCodes implements Serializable {

    private static final long serialVersionUID = -8819452675655004661L;
    public String comments;
    public String code;

    public ReasonCodes() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Comments")) {
            comments = cleanChars;
        } else if (localName.equalsIgnoreCase("Code")) {
            code = cleanChars;
        }
    }

    @Override
    public String toString() {
        return comments;
    }
}
