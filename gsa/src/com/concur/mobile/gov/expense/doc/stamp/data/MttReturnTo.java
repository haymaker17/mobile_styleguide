/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.stamp.data;

import java.io.Serializable;

public class MttReturnTo implements Serializable {

    private static final long serialVersionUID = -7612243120338291086L;
    public String returntoId;
    public String returntoSSN;
    public String returntoName;

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("returnto_id")) {
            returntoId = cleanChars;
        } else if (localName.equalsIgnoreCase("returnto_ssn")) {
            returntoSSN = cleanChars;
        } else if (localName.equalsIgnoreCase("returnto_name")) {
            returntoName = cleanChars;
        }
    }

    @Override
    public String toString() {
        return returntoName;
    }
}
