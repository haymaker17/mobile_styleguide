/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class AccountCode implements Serializable {

    private static final long serialVersionUID = 3596773630184353569L;
    public String account;
    public Double amount;

    public AccountCode() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Account")) {
            account = cleanChars;
        } else if (localName.equalsIgnoreCase("Amount")) {
            amount = Parse.safeParseDouble(cleanChars);
        }
    }
}
