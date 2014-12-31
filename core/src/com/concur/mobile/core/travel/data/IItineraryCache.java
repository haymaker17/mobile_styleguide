/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;
import java.util.List;

import com.concur.mobile.core.data.IItineraryInfo;

/**
 * An interface providing itinerary caching services.
 * 
 * @author andy
 */
public interface IItineraryCache {

    /**
     * Gets an instance of <code>Trip</code> containing only itinerary summary data based on a Cliqbook trip id.
     * 
     * @param tripId
     *            contains a Cliqbook trip id.
     * @return returns an instance of <code>Trip</code> containing itinerary summary data matching on <code>tripId</code>;
     *         otherwise, returns <code>null</code>
     */
    public Trip getItinerarySummaryByCliqbookTripId(String tripId);

    /**
     * Gets an instance of <code>Trip</code> containing only itinerary summary data based on a record locator.
     * 
     * @param recordLocator
     *            contains a record locator.
     * @return returns an instance of <code>Trip</code> containing itinerary summary data matching on <code>recordLocator</code>;
     *         otherwise, returns <code>null</code>
     */
    public Trip getItinerarySummaryByRecordLocator(String recordLocator);

    /**
     * Gets an instance of <code>Trip</code> containing only itinerary summmary data based on a booking record locator.
     * 
     * @param bookingRecordLocator
     *            contains a booking record locator.
     * @return returns an instance of <code>Trip</code> containing itinerary summary data for the itinerary with booking record
     *         locator <code>bookingRecordLocator</code>.
     */
    public Trip getItinerarySummaryByBookingRecordLocator(String bookingRecordLocator);

    /**
     * Gets an instance of <code>Trip</code> containing only itinerary summmary data based on a client locator.
     * 
     * @param itinLocator
     *            contains a client locator.
     * @return returns an instance of <code>Trip</code> containing itinerary summary data for the itinerary with itinLocator
     *         locator <code>itinLocator</code>.
     */

    public Trip getItinerarySummaryByClientLocator(String itinLocator);

    /**
     * Gets the list of <code>Trip</code> objects that contain itinerary summary information, i.e., no booking objects.
     * 
     * @return returns a list of <code>Trip</code> objects containing itinerary summary data. If no summary data exists, then
     *         <code>null</code> is returned.
     */
    public List<Trip> getItinerarySummaryList();

    /**
     * Sets the list of <code>Trip</code> objects that contain itinerary summary information, i.e., no booking data.
     * 
     * @param itinSummaryList
     *            the itinerary summary list.
     */
    public void setItinerarySummaryList(List<Trip> itinSummaryList);

    /**
     * Gets the last update time for the itinerary summary list.
     * 
     * @return returns a <code>Calendar</code> object containing the itinerary summary list. If no summary data exists, then
     *         <code>null</code> is returned.
     */
    public Calendar getItinerarySummaryListUpdateTime();

    /**
     * Sets the last update time for the itinerary summary list.
     * 
     * @param updateTime
     *            the itinerary summary list update time.
     */
    public void setItinerarySummaryListUpdateTime(Calendar updateTime);

    /**
     * Gets the <code>Trip</code> for a detailed itinerary based on an itinerary locator.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @return returns a <code>Trip</code> object containing the detailed itinerary; otherwise, returns <code>null</code> if a
     *         detailed itinerary data is not available locally for the locator.
     */
    public Trip getItinerary(String itinLocator);

    /**
     * Will add an intinerary to the cache given a locator.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @param itinInfo
     *            the itinerary info object.
     */
    public void addItinerary(String itinLocator, IItineraryInfo itinInfo);

    /**
     * Will remove an itinerary from the cache given a locator.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @return returns <code>true</code> if the itinerary was removed; <code>false</code> otherwise.
     */
    public boolean removeItinerary(String itinLocator);

    /**
     * Gets a <code>Calendar</code> object containing the update time for the detailed itinerary based on a locator.
     * 
     * @param itinLocator
     *            the itinerary locator.
     * @return returns an instance of <code>Calendar</code> containing the itinerary detail update time.
     */
    public Calendar getItineraryUpdateTime(String itinLocator);

    /**
     * Will clear out all non-summary itineraries stored within the cache.
     */
    public void clearItineraries();

    /**
     * Gets whether the itinerary summary list should be refetched.
     * 
     * @return whether the itinerary summary list should be refetched.
     */
    public boolean shouldRefetchSummaryList();

    /**
     * Sets whether the itinerary summary list should be refetched.
     * 
     * @param refetch
     *            whether the summary list should be refetched.
     */
    public void setShouldRefetchSummaryList(boolean refetch);

    /**
     * Gets whether the itinerary summary list should be refreshed.
     * 
     * @return whether the itinerary summary list should be refreshed.
     */
    public boolean shouldRefreshSummaryList();

    /**
     * Sets whether the itinary summary list should be refreshed.
     * 
     * @param refresh
     *            whether the itinerary summary list should be refreshed.
     */
    public void setShouldRefreshSummaryList(boolean refresh);

}
