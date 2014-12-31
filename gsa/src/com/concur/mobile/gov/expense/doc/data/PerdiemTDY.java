/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class PerdiemTDY implements Serializable {

    private static final long serialVersionUID = 5409402870328264036L;
    public Calendar beginTripday;
    public Calendar endTripday;
    public String rate;
    public String perdiemLocation;

    public PerdiemTDY() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("PerdiemLocation")) {
            perdiemLocation = cleanChars;
        } else if (localName.equalsIgnoreCase("Rate")) {
            rate = cleanChars;
        } else if (localName.equalsIgnoreCase("BeginTdy")) {
            beginTripday = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("EndTdy")) {
            endTripday = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        }
    }
}
