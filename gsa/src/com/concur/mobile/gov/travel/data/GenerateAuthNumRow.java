package com.concur.mobile.gov.travel.data;

import java.io.Serializable;

public class GenerateAuthNumRow implements Serializable {

    private static final long serialVersionUID = 6938898824351970239L;

    public String taNumber;

    public GenerateAuthNumRow() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("TANumber")) {
            taNumber = cleanChars;
        }
    }
}
