package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

import java.util.HashMap;
import java.util.Map;

/**
 * Singleton caching code lists
 *
 * Created by Michael Becherer on 22-Jul-15.
 */
public class CodeListManager {

    /**
     * Tag for logging purpose.
     */
    public static final String CLASS_TAG = CodeListManager.class.getSimpleName();

    /**
     * Single instance of this class
     */
    private static CodeListManager instance = new CodeListManager();

    /**
     * Cache for itinerary locations
     */
    private Map<String, ItineraryLocation> itineraryLocations;

    /**
     * Private constructor. Do not create further instances of this class.
     */
    private CodeListManager() {
        itineraryLocations = new HashMap<>();
    }

    /**
     * To be called to get access to this singleton
     * @return {@link #instance}
     */
    public static CodeListManager getInstance() {
        return instance;
    }

    /**
     * Clears all cached data
     */
    public synchronized void clearAll() {
        itineraryLocations = new HashMap<>();
    }

    /**
     * Getter method
     * @return The cached list of itinerary locations
     */
    public Map<String, ItineraryLocation> getItineraryLocations() {
        return itineraryLocations;
    }

    /**
     * Getter method
     * @param code The code to be looked up
     * @return The cached list entry
     */
    public ItineraryLocation getItineraryLocation(final String code) {
        if (itineraryLocations == null || StringUtilities.isNullOrEmpty(code)) {
            return null;
        }
        return itineraryLocations.get(code);
    }

    /**
     * Updates the list of itinerary locations with a clone of the given itinerary location.
     * If the location is not buffered yet, the cloned itinerary location will be added.
     * Important to know: This method is usually fed via web services. As the web services do not
     * always deliver complete location information the following rules are applied:
     * If there is no valid rate location key within the given itinerary
     * location, then the cloned itinerary will possibly be updated with the former key.
     * If there is no valid describing name the clone will possibly be updated with the
     * former name.
     *
     * @param itineraryLocation The itinerary location to be cloned
     * @return The updated itinerary location or null, if no update has been possible
     */
    public synchronized ItineraryLocation updateItineraryLocation(final ItineraryLocation itineraryLocation) {
        if (itineraryLocations == null || itineraryLocation == null
                || StringUtilities.isNullOrEmpty(itineraryLocation.getCode())) {
            return null;
        }
        ItineraryLocation newLocation = itineraryLocation.clone();
        if (newLocation == null) {
            return null;
        }
        ItineraryLocation updateLocation = getItineraryLocation(itineraryLocation.getCode());
        String oldRateLocationKey = null;
        String oldName = null;
        Long oldOffset = null;
        if (updateLocation != null) {
            oldRateLocationKey = updateLocation.getRateLocationKey();
            oldName = updateLocation.getName();
            oldOffset = updateLocation.getTimeZoneOffset();
        }
        itineraryLocations.put(newLocation.getCode(), newLocation);
        if (StringUtilities.isNullOrEmpty(newLocation.getRateLocationKey())) {
            newLocation.setRateLocationKey(oldRateLocationKey);
        }
        if (StringUtilities.isNullOrEmpty(newLocation.getName())) {
            newLocation.setName(oldName);
        }
        if (newLocation.getTimeZoneOffset() == null) {
            newLocation.setTimeZoneOffset(oldOffset);
        }
        return newLocation;
    }
}
