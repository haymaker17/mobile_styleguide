/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;

import android.content.Context;

import com.concur.mobile.core.ConcurCore;

/**
 * @author AndrewK
 */
public abstract class SearchSuggestion {

    /**
     * Gets the display text for this search suggestion.
     * 
     * @param concurMobile
     *            the application instance.
     * 
     * @return the display text for this search suggestion.
     */
    public abstract String getDisplayText(ConcurCore concurMobile);

    /**
     * Gets an instance of <code>LocationChoice</code> associated with this search option marking a beginning location.
     * 
     * @param context
     *            an application context object.
     * @return an instance of <code>LocationChoice</code> to be used with this search suggestion marking a beginning location.
     */
    public abstract LocationChoice getStartLocationChoice(Context context);

    /**
     * Gets an instance of <code>Calendar</code> associated with this search option containing the start date.
     * 
     * @return an instance of <code>Calendar</code> to be used with this search suggestion marking a beginning date.
     */
    public abstract Calendar getStartDate();

    /**
     * Gets an instance of <code>LocationChoice</code> associated with this search option marking an ending location.
     * 
     * @param context
     *            an application context.
     * 
     * @return an instance of <code>LocationChoice</code> to be used with this search suggestion marking an ending location.
     */
    public abstract LocationChoice getStopLocationChoice(Context context);

    /**
     * Gets an instance of <code>Calendar</code> associated with this search option containing the stop date.
     * 
     * @return an instance of <code>Calendar</code> to be used with this search suggestion marking an end date.
     */
    public abstract Calendar getStopDate();

    /**
     * Gets whether this search suggestion requires rail station information.
     * 
     * @return whether this search suggestion requires rail station information.
     */
    public abstract boolean requiresRailStations();

}
