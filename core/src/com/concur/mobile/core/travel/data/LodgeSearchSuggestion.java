/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * A prospective lodging search option with check-in/check-out and address.
 * 
 * @author AndrewK
 */
public class LodgeSearchSuggestion extends CitySearchSuggestion {

    public Calendar checkInDate;

    public Calendar checkInDay;

    public Calendar checkOutDate;

    public Calendar checkOutDay;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.CitySearchOption#getStartDate()
     */
    @Override
    public Calendar getStartDate() {
        return checkInDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.CitySearchOption#getStopDate()
     */
    @Override
    public Calendar getStopDate() {
        return checkOutDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.CitySearchSuggestion#getDisplayText()
     */
    @Override
    public String getDisplayText(ConcurCore concurMobile) {
        String displayText = super.getDisplayText(concurMobile);
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(displayText);
        strBldr.append(" (");
        strBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, checkInDate));
        strBldr.append(" - ");
        strBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, checkOutDate));
        strBldr.append(')');
        displayText = strBldr.toString();
        return displayText;
    }

}
