/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.ConcurCore;

/**
 * An extension of <code>SearchSuggestion</code> for defining a category.
 * 
 * @author AndrewK
 */
public class CategorySearchSuggestion extends SearchSuggestion {

    private String displayText;

    /**
     * Constructs an instance of <code>CategorySearchSuggestion</code> with a category name.
     * 
     * @param categoryName
     *            the category name.
     */
    public CategorySearchSuggestion(String categoryName) {
        displayText = categoryName;
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
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStartDate()
     */
    @Override
    public Calendar getStartDate() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStartLocationChoice(android.content.Context)
     */
    @Override
    public LocationChoice getStartLocationChoice(Context context) {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStopDate()
     */
    @Override
    public Calendar getStopDate() {
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.SearchSuggestion#getStopLocationChoice(android.content.Context)
     */
    @Override
    public LocationChoice getStopLocationChoice(Context context) {
        return null;
    }

}
