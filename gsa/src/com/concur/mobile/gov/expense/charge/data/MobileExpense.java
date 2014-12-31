/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.charge.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.gov.util.Const;
import com.concur.mobile.platform.util.Parse;

public class MobileExpense implements Serializable {

    private static final long serialVersionUID = -6830436095382413763L;
    public Calendar tranDate;

    public Double postedAmt;

    public String tranDescription;
    public String ccexpid;
    public String imageid;

    public MobileExpense() {
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("tran_date")) {
            tranDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("posted_amt")) {
            postedAmt = FormatUtil.parseAmount(cleanChars, Const.GOV_LOCALE);
        } else if (localName.equalsIgnoreCase("tran_description")) {
            tranDescription = cleanChars;
        } else if (localName.equalsIgnoreCase("ccexpid")) {
            ccexpid = cleanChars;
        } else if (localName.equalsIgnoreCase("imageid")) {
            imageid = cleanChars;
        }
    }

    public String getCcexpid() {
        return ccexpid;
    }

    public void setCcexpid(String ccexpid) {
        this.ccexpid = ccexpid;
    }
}
