/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.travel.data.Trip;

/**
 * An interface describing an itinerary object.
 * 
 * @author andy
 */
public interface IItineraryInfo {

    /**
     * Gets the itinerary locator.
     * 
     * @return the itinerary locator.
     */
    public String getItineraryLocator();

    /**
     * Gets the itinerary.
     * 
     * @return an instance of <code>Trip</code>.
     */
    public Trip getItinerary();

    /**
     * Gets the report update time.
     * 
     * @return the report update time.
     */
    public Calendar getUpdateTime();

}
