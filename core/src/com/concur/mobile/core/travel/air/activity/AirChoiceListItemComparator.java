/**
 * 
 */
package com.concur.mobile.core.travel.air.activity;

import java.util.Comparator;

import com.concur.mobile.core.travel.air.activity.AirResultsList.SortCriteria;
import com.concur.mobile.core.travel.air.data.AirBookingSegment;
import com.concur.mobile.core.travel.air.data.AirChoice;
import com.concur.mobile.core.travel.air.data.Flight;

/**
 * An implementation of <code>Comparator<T></code>
 */
public class AirChoiceListItemComparator implements Comparator<AirChoiceListItem> {

    // Contains the sort criteria.
    private SortCriteria sortCriteria;

    /**
     * Constructs an instance of <code>AirChoiceListItemComparator</code> with sort criteria <code>sortCriteria</code>.
     * 
     * @param sortCriteria
     *            the sort criteria.
     */
    public AirChoiceListItemComparator(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    /**
     * Sets the current sort criteria for comparing <code>AirChoice</code> items.
     * 
     * @param sortCriteria
     *            the <code>AirChoice</code> sort criteria.
     */
    public void setSortCriteria(SortCriteria sortCriteria) {
        this.sortCriteria = sortCriteria;
    }

    /**
     * Gets the current sort criteria for comparing <code>AirChoice</code> items.
     * 
     * @return the <code>AirChoice</code> sort criteria.
     */
    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    public int compare(AirChoiceListItem acli1, AirChoiceListItem acli2) {
        int retVal = 0;
        switch (sortCriteria) {
        case PREFERENCE: {
            retVal = compareByPreference(acli1.getAirChoice(), acli2.getAirChoice());
            break;
        }
        case PRICE: {
            retVal = compareByPrice(acli1.getAirChoice(), acli2.getAirChoice());
            break;
        }
        case EARLIEST_DEPARTURE: {
            retVal = compareByEarliestDeparture(acli1.getAirChoice(), acli2.getAirChoice());
            break;
        }
        case TOTAL_TRAVEL_TIME: {
            retVal = compareByTotalTravelTime(acli1.getAirChoice(), acli2.getAirChoice());
            break;
        }
        }
        return retVal;
    }

    private int compareByTotalTravelTime(AirChoice ac1, AirChoice ac2) {
        int retVal = 0;

        int ac1TotalTravelTime = 0;
        if (ac1.segments != null) {
            for (AirBookingSegment airBkgSeg : ac1.segments) {
                ac1TotalTravelTime += airBkgSeg.elapsedTime;
            }
        }
        int ac2TotalTravelTime = 0;
        if (ac2.segments != null) {
            for (AirBookingSegment airBkSeg : ac2.segments) {
                ac2TotalTravelTime += airBkSeg.elapsedTime;
            }
        }
        if (ac1TotalTravelTime < ac2TotalTravelTime) {
            retVal = -1;
        } else if (ac1TotalTravelTime > ac2TotalTravelTime) {
            retVal = 1;
        }
        return retVal;
    }

    private int compareByPreference(AirChoice ac1, AirChoice ac2) {
        int retVal = 0;
        int ac1Rank = AirResultsList.getAirChoicePreference(ac1);
        int ac2Rank = AirResultsList.getAirChoicePreference(ac2);
        // Preference ranking is really a descending sort.
        if (ac1Rank < ac2Rank) {
            retVal = 1;
        } else if (ac1Rank > ac2Rank) {
            retVal = -1;
        }
        return retVal;
    }

    private int compareByPrice(AirChoice ac1, AirChoice ac2) {
        int retVal = 0;
        if (ac1.fare != null && ac2.fare != null) {
            if (ac1.fare < ac2.fare) {
                retVal = -1;
            } else if (ac1.fare > ac2.fare) {
                retVal = 1;
            }
        } else if (ac1.fare != null && ac2.fare == null) {
            retVal = 1;
        } else if (ac1.fare == null && ac2.fare != null) {
            retVal = -1;
        }
        return retVal;
    }

    private int compareByEarliestDeparture(AirChoice ac1, AirChoice ac2) {
        int retVal = 0;
        Flight ac1FstSegFstFlt = ac1.segments.get(0).flights.get(0);
        Flight ac2FstSegFstFlt = ac2.segments.get(0).flights.get(0);
        if (ac1FstSegFstFlt.departureDateTime != null && ac2FstSegFstFlt.departureDateTime != null) {
            retVal = ac1FstSegFstFlt.departureDateTime.compareTo(ac2FstSegFstFlt.departureDateTime);
        } else if (ac1FstSegFstFlt.departureDateTime != null && ac2FstSegFstFlt.departureDateTime == null) {
            retVal = -1;
        } else if (ac1FstSegFstFlt.departureDateTime == null && ac2FstSegFstFlt.departureDateTime != null) {
            retVal = 1;
        }
        return retVal;
    }

}
