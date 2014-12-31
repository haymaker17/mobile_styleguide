package com.concur.mobile.gov.expense.charge.data;

import java.io.Serializable;

public class ActionStatus implements Serializable {

    private static final long serialVersionUID = 1985502430783449816L;
    public String status, expenseId;

    public ActionStatus() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Status")) {
            status = cleanChars;
        } else if (localName.equalsIgnoreCase("expenseId")) {
            expenseId = cleanChars;
        }
    }
}
