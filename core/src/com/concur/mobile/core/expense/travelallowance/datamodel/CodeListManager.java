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

    private static CodeListManager instance = new CodeListManager();
    private Map<String, ItineraryLocation> itineraryLocations;

    private CodeListManager() {
        itineraryLocations = new HashMap<>();
    }

    public static CodeListManager getInstance() {
        return instance;
    }

    public synchronized void clearAll() {
        itineraryLocations = new HashMap<>();
    }

    public Map<String, ItineraryLocation> getItineraryLocations() {
        return itineraryLocations;
    }

    public ItineraryLocation getItineraryLocation(final String code) {
        if (itineraryLocations == null || StringUtilities.isNullOrEmpty(code)) {
            return null;
        }
        return itineraryLocations.get(code);
    }

    /**
     * Updates the list of itinerary locations with a clone of the given itinerary location.
     * If the location is not buffered yet, the cloned itinerary location will be added.
     * Important: If there is no valid rate location key within the given itinerary
     * location, then the cloned itinerary will be updated with the former key, if available.
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
        if (updateLocation != null) {
            oldRateLocationKey = updateLocation.getRateLocationKey();
        }
        itineraryLocations.put(newLocation.getCode(), newLocation);
        if (StringUtilities.isNullOrEmpty(newLocation.getRateLocationKey())) {
            newLocation.setRateLocationKey(oldRateLocationKey);
        }
        return newLocation;
    }
}
