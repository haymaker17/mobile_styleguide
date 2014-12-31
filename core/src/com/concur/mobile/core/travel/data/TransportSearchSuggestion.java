/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Format;

/**
 * A prospective transport search option with pick-up/drop-off
 * 
 * @author AndrewK
 */
public class TransportSearchSuggestion extends SearchSuggestion {

    public CitySearchSuggestion departureCity;

    public Calendar departureDate;

    public Calendar departureDay;

    public CitySearchSuggestion arrivalCity;

    public Calendar arrivalDate;

    public Calendar arrivalDay;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#requiresRailStations()
     */
    @Override
    public boolean requiresRailStations() {
        return (((departureCity != null) ? departureCity.requiresRailStations() : false) || ((arrivalCity != null) ? arrivalCity
                .requiresRailStations() : false));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getDisplayText()
     */
    @Override
    public String getDisplayText(ConcurCore concurMobile) {
        String displayText = departureCity.getDisplayText(concurMobile);
        StringBuilder strBldr = new StringBuilder();
        strBldr.append(displayText);
        strBldr.append(" (");
        strBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, departureDate));
        strBldr.append(" - ");
        strBldr.append(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_DISPLAY, arrivalDate));
        strBldr.append(')');
        displayText = strBldr.toString();
        return displayText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStartLocationChoice(android.content.Context)
     */
    @Override
    public LocationChoice getStartLocationChoice(Context context) {
        return departureCity.getStartLocationChoice(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStartDate()
     */
    @Override
    public Calendar getStartDate() {
        return departureDate;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStopLocationChoice(android.content.Context)
     */
    @Override
    public LocationChoice getStopLocationChoice(Context context) {
        return arrivalCity.getStopLocationChoice(context);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStopDate()
     */
    @Override
    public Calendar getStopDate() {
        return arrivalDate;
    }

}
