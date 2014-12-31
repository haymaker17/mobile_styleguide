/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.travel.data.Trip;

/**
 * An implementation of <code>IItineraryInfo</code>.
 * 
 * @author andy
 */
public abstract class ItineraryInfo implements IItineraryInfo {

    /**
     * Contains the itinerary locator.
     */
    protected String itineraryLocator;

    /**
     * Contains the itinerary modeled in a <code>Trip</code> object.
     */
    protected Trip itinerary;

    /**
     * Contains the itinerary last client update time.
     */
    protected Calendar updateTime;

    /**
     * Constructs an instance of <code>ItineraryInfo</code> with a locator, trip and update time.
     * 
     * @param itineraryLocator
     *            the itinerary locator.
     * @param itinerary
     *            the itinerary.
     * @param updateTime
     *            the update time.
     */
    protected ItineraryInfo(String itineraryLocator, Trip itinerary, Calendar updateTime) {
        this.itineraryLocator = itineraryLocator;
        this.itinerary = itinerary;
        this.updateTime = updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryInfo#getItinerary()
     */
    public Trip getItinerary() {
        return itinerary;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryInfo#getItineraryLocator()
     */
    public String getItineraryLocator() {
        return itineraryLocator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryInfo#getUpdateTime()
     */
    public Calendar getUpdateTime() {
        return updateTime;
    }

}
