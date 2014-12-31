/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class MttDocument implements Serializable {

    private static final long serialVersionUID = 7370122290180995545L;
    public String docName;
    public String docType;
    public String travId;
    public String userId;
    public Boolean sigRequired;

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("docname")) {
            docName = cleanChars;
        } else if (localName.equalsIgnoreCase("doctype")) {
            docType = cleanChars;
        } else if (localName.equalsIgnoreCase("travid")) {
            travId = cleanChars;
        } else if (localName.equalsIgnoreCase("user_id")) {
            userId = cleanChars;
        } else if (localName.equalsIgnoreCase("sig_required")) {
            sigRequired = Parse.safeParseBoolean(cleanChars);
        }
    }
}
