/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.ConcurCore;

/**
 * An extension of <code>SearchOption</code> representing a custom option, i.e, no locations/dates.
 * 
 * @author AndrewK
 */
public class CustomSearchSuggestion extends SearchSuggestion {

    private String displayText;

    /**
     * Constructs an instance of <code>CustomSearchSuggestion</code> with display text <code>displayText</code>.
     * 
     * @param displayText
     *            the display text.
     */
    public CustomSearchSuggestion(String displayText) {
        this.displayText = displayText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#requiresRailStations()
     */
    @Override
    public boolean requiresRailStations() {
        return false;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getDisplayText()
     */
    @Override
    public String getDisplayText(ConcurCore concurMobile) {
        return displayText;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStartDate()
     */
    @Override
    public Calendar getStartDate() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStartLocationChoice()
     */
    @Override
    public LocationChoice getStartLocationChoice(Context context) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStopDate()
     */
    @Override
    public Calendar getStopDate() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchOption#getStopLocationChoice()
     */
    @Override
    public LocationChoice getStopLocationChoice(Context context) {
        return null;
    }

}
