/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;
import java.util.List;

/**
 * An interface for providing trip analysis services.
 * 
 * @author AndrewK
 */
public interface ITripAnalyzer {

    /**
     * Will analyze a trip for potential locations and dates where car rental is likely.
     * 
     * @param trip
     *            the trip to analyze.
     * @return a list of <code>TransportSearchOption</code> objects describing possible locations and dates when transport may be
     *         required.
     */
    public List<TransportSearchSuggestion> findCarSuggestions(Trip trip);

    /**
     * Will analyze a trip for potential locations and dates where hotel reserve is likely.
     * 
     * @param trip
     *            the trip to analyze.
     * @return a list of <code>LodgeSearchOption</code> objects describing possible locations and dates where lodging may be
     *         required.
     */
    public List<LodgeSearchSuggestion> findHotelSuggestions(Trip trip);

    /**
     * Will analyze a trip for the list of cities visited on the trip.
     * 
     * @param trip
     *            the trip to analyze.
     * @param useAirportName
     *            whether to use the airport name or the city name for the display text.
     * @return a list of <code>String</code> objects containing the address lines representing locations within cities.
     */
    public List<CitySearchSuggestion> findTripCities(Trip trip, boolean useAirportName);

    /**
     * Will return a list of trips with end dates after <code>localDay</code> ordered by end-date.
     * 
     * @param trips
     *            the list of current trips.
     * @param localDay
     *            the local date.
     * @return a list of <code>Trip</code> objects ordered by end-date; <code>null</code> otherwise.
     */
    public List<Trip> findTrips(List<Trip> trips, Calendar localDay);

    /**
     * 
     * @param trip
     *            the trip to analyze
     * @return a <code>LodgeSearchSuggestion</code> object defaulted to location and dates depending on the flight segments
     */
    public LodgeSearchSuggestion findHotelSearchSuggestionForFlight(Trip trip);

    /**
     * 
     * @param trip
     *            the trip to analyze
     * @return a <code>TransportSearchSuggestion</code> object defaulted to location and dates depending on the flight segments
     */
    public TransportSearchSuggestion findCarSearchSuggestionForFlight(Trip trip);

}
