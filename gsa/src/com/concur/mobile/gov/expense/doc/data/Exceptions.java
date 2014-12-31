/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;

public class Exceptions implements Serializable {

    private static final long serialVersionUID = -6021863409239279177L;
    public String name;
    public String errorStatus;
    public String comments;

    public Exceptions() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("Name")) {
            name = cleanChars;
        } else if (localName.equalsIgnoreCase("Error_Status")) {
            errorStatus = cleanChars;
        } else if (localName.equalsIgnoreCase("Comments")) {
            comments = cleanChars;
        }
    }
}
