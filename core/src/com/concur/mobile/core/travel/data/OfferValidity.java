/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;
import java.util.List;

/**
 * Models offer validity information.
 */
public class OfferValidity {

    public List<TimeRange> timeRanges;

    public List<ValidLocation> locations;

    public void addTimeRange(TimeRange timeRange) {
        if (timeRanges == null) {
            timeRanges = new ArrayList<TimeRange>();
        }
        timeRanges.add(timeRange);
    }

    public void addLocation(ValidLocation location) {
        if (locations == null) {
            locations = new ArrayList<ValidLocation>();
        }
        locations.add(location);
    }

}
