/**
 * 
 */
package com.concur.mobile.core.data;

import java.util.Calendar;

import com.concur.mobile.core.travel.data.Trip;

/**
 * An implementation of <code>IItineraryDBInfo</code> for storing the XML representation.
 * 
 * @author andy
 */
public class ItineraryDBInfo extends ItineraryInfo implements IItineraryDBInfo {

    // Contains the itinerary XML representation.
    protected String itinXml;

    /**
     * Constructs an instance of <code>ItineraryDBInfo</code> with a locator, itinerary, update time and Xml representation.
     * 
     * @param itineraryLocator
     *            the itinerary locator.
     * @param itinerary
     *            the itinerary.
     * @param updateTime
     *            the update time.
     * @param itinXml
     *            the XML representation.
     */
    protected ItineraryDBInfo(String itineraryLocator, Trip itinerary, Calendar updateTime, String itinXml) {
        super(itineraryLocator, itinerary, updateTime);
        this.itinXml = itinXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryDBInfo#clearXML()
     */
    public void clearXML() {
        itinXml = null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryDBInfo#getXML()
     */
    public String getXML() {
        return itinXml;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.IItineraryDBInfo#setItinerary(com.concur.mobile.data.travel.Trip)
     */
    public void setItinerary(Trip itinerary) {
        this.itinerary = itinerary;
    }

}
