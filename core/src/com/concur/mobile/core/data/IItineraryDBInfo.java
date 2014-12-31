/**
 * 
 */
package com.concur.mobile.core.data;

import com.concur.mobile.core.travel.data.Trip;

/**
 * An extension of <code>IItineraryInfo</code> containing parsed XML information.
 * 
 * @author andy
 */
public interface IItineraryDBInfo extends IItineraryInfo {

    /**
     * Gets the itinerary XML representation.
     * 
     * @return the itinerary XML representation.
     */
    String getXML();

    /**
     * Clears the itinerary XML representation.
     */
    void clearXML();

    /**
     * Sets the itinerary object.
     * 
     * @param itinerary
     *            the itinerary object.
     */
    void setItinerary(Trip itinerary);

}
