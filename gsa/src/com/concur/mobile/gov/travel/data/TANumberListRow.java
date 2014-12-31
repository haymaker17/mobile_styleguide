/**
 * @author sunill
 */
package com.concur.mobile.gov.travel.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class TANumberListRow implements Serializable {

    private static final long serialVersionUID = -7502722476112728657L;

    public String taNumber;
    public String taType;
    public String purposeCode;
    public String taLabel;
    public String taDocType;
    public String taDocName;
    public String pdmLocation;

    public Calendar tripBeginDate;
    public Calendar tripEndDate;

    public TANumberListRow() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("TANumber")) {
            taNumber = cleanChars;
        } else if (localName.equalsIgnoreCase("TAType")) {
            taType = cleanChars;
        } else if (localName.equalsIgnoreCase("PurposeCode")) {
            purposeCode = cleanChars;
        } else if (localName.equalsIgnoreCase("PdmLocation")) {
            pdmLocation = cleanChars;
        } else if (localName.equalsIgnoreCase("TADocName")) {
            taDocName = cleanChars;
        } else if (localName.equalsIgnoreCase("TADocType")) {
            taDocType = cleanChars;
        } else if (localName.equalsIgnoreCase("TALabel")) {
            taLabel = cleanChars;
        } else if (localName.equalsIgnoreCase("TripBeginDate")) {
            tripBeginDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("TripEndDate")) {
            tripEndDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        }
    }
}
