/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.service;

import java.util.Date;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONObject;

import android.content.Context;
import android.location.Address;
import android.location.Location;
import android.util.Log;

import com.concur.mobile.eva.R;
import com.concur.mobile.eva.data.EvaLocation;
import com.concur.mobile.eva.data.EvaMoney;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.util.Const;

/**
 * Object representing a Hotel from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaHotelReply {

    public final String CLS_TAG = EvaHotelReply.class.getSimpleName();

    public String latitude = null;
    public String longitude = null;
    public String locationName = null;
    public String distanceUnit = "M"; // default to miles
    public String distanceValue = "5"; // default to 5 miles
    public String checkInDate;
    public String checkOutDate;
    public String smoking = "N"; // default to non-smoking
    public String namesContaining = null; // no specific hotel
    public EvaMoney amountRestriction;

    /**
     * Constructor that parses the EvaApiReply and generates a Hotel Search object suitable for the Concur MWS.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param currentLocation
     *            contains a reference to a current location.
     * @param currentAddress
     *            contains a reference to a current address.
     * @param apiReply
     * 
     * @throws IllegalArgumentException
     */
    public EvaHotelReply(Context context, Location currentLocation, Address currentAddress, EvaApiReply apiReply)
            throws IllegalArgumentException {

        // Get the locations
        EvaLocation[] locations = apiReply.locations;

        // We need to get the 0-index Location to find the "Next" Location.
        if (locations != null && locations.length > 0) {

            // Go through all the Locations and sort them based on Index.
            SortedMap<Integer, EvaLocation> locationMap = new TreeMap<Integer, EvaLocation>();
            for (EvaLocation currLoc : locations) {
                locationMap.put(currLoc.index, currLoc);
            }

            // Get the first non-0 (non Home) Location.
            // i.e. the lowest non-0 Index.
            EvaLocation loc = null;
            for (Integer key : locationMap.keySet()) {
                if (key != 0) {
                    loc = locationMap.get(key);
                    break;
                }
            }

            // No appropriate Location, so throw errror.
            if (loc == null) {
                String msg = context.getString(R.string.voice_book_unable_to_loc);
                throw new IllegalArgumentException(msg);
            }

            // Get the lat/long of the location.
            // If it's not specified, default to the user's current (GPS/cell) location.
            if (loc.latitude != null && loc.longitude != null) {
                latitude = loc.latitude;
                longitude = loc.longitude;
            } else {
                // Default to current (GPS/cell) location.
                Location currLoc = currentLocation;
                if (currLoc != null) {
                    latitude = Double.toString(currLoc.getLatitude());
                    longitude = Double.toString(currLoc.getLongitude());
                } else {
                    // Couldn't get the user's current location...
                    // can't proceed without a location!!!
                    String msg = context.getString(R.string.voice_book_unable_to_loc);
                    throw new IllegalArgumentException(msg);
                }
            }

            // Get the location name. If it's not specified,
            // use the current (GPS/cell) Address attribute(s).
            if (loc.airports != null && !loc.airports.isEmpty()) {

                // MOB-12081 - If this is an Airport search, then the Airport property should only have 1 entry
                // unless the user specifically searched for an airport (e.g. "Near Edinburgh airport"),
                // which may return many airport codes.
                if (loc.airports.size() == 1 || (loc.geoAttributes != null && loc.geoAttributes.airport)) {
                    locationName = loc.airports.get(0);
                }
            }

            // If we didn't get the location name from the airport,
            // try setting it using the text search name or geo location.
            if (locationName == null) {

                if (loc.name != null) {
                    // Using the Location.Name element works if the
                    // Location.Type is also Airport
                    locationName = loc.name;
                } else {
                    // Try to get user's current address.
                    Address currAdd = currentAddress;
                    if (currAdd != null) {
                        if (currAdd.getLocality() != null) {
                            locationName = currAdd.getLocality();
                        } else if (currAdd.getFeatureName() != null) {
                            locationName = currAdd.getFeatureName();
                        } else if (currAdd.getThoroughfare() != null) {
                            locationName = currAdd.getThoroughfare();
                        }
                    }
                }
            }

            // Cannot determine the Location name, so throw error.
            if (locationName == null) {
                String msg = context.getString(R.string.voice_book_unable_to_loc);
                throw new IllegalArgumentException(msg);
            }

            // Get the Geo Attributes to determine search radius.
            if (loc.geoAttributes != null) {
                if (loc.geoAttributes.distanceUnit != null) {
                    distanceUnit = loc.geoAttributes.distanceUnit.substring(0, 1); // We only need the first letter.
                }
                if (loc.geoAttributes.distanceValue != null) {
                    distanceValue = loc.geoAttributes.distanceValue;
                }
            }

            // Get the check-in/out dates.
            // The Arrival.Date is the check-in date. We can ignore the Arrival.Delta value
            // since that represents the delta from the current/today's date.
            if (loc.arrivalTime != null && loc.arrivalTime.date != null) {
                checkInDate = loc.arrivalTime.date;
            } else {
                // If Arrival Date isn't specified, try using the Home Departure Date.
                EvaLocation homeLocation = locationMap.get(0);
                if (homeLocation != null && homeLocation.departureTime != null
                        && homeLocation.departureTime.date != null) {
                    checkInDate = homeLocation.departureTime.date;
                }
            }

            // If we still can't determine the check-in date,
            // just default the check-in date to today.
            if (checkInDate == null) {
                long time = (new Date()).getTime() + 3600000; // Add 1 hour for check-in time.
                checkInDate = EvaTime.formatDate(time);
            }

            // Use the Location.Stay to determine the check-out date.
            if (loc.stay != null && loc.stay.length > 0) {

                try {
                    for (JSONObject j : loc.stay) {
                        if (j.getString("Delta") != null) {
                            String delta = j.getString("Delta");
                            int index = delta.indexOf("days=+");
                            if (index != -1) {
                                String dayS = delta.substring(index + 6);
                                int day = Integer.parseInt(dayS);

                                // Get the check-in date and add the delta
                                // to determine the check-out date.
                                Date date = EvaTime.parseDate(checkInDate);
                                long time = date.getTime() + (86400000 * day);
                                checkOutDate = EvaTime.formatDate(time);
                            }
                            break;
                        }
                    }
                } catch (Exception e) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".EvaHotelSearch() - couldn't parse Stay Delta value.", e);
                }

            }

            // MOB-13143 - Default checkout date to be 1 day after check-in day.
            if (checkOutDate == null) {

                Date date = EvaTime.parseDate(checkInDate);
                if (date != null) {
                    long time = date.getTime() + (86400000); // adding 3 days.
                    checkOutDate = EvaTime.formatDate(time);
                } else {
                    checkOutDate = checkInDate; // Worst-case scenario, checkout the same day.
                }
            }

            // Get the Hotel Attributes so we can determine room type, hotel chain, rating, etc.
            if (loc.hotelAttributes != null) {

                if (loc.hotelAttributes.chainName != null) {
                    namesContaining = loc.hotelAttributes.chainName;
                }

                if (loc.hotelAttributes.smoking) {
                    smoking = "Y";
                } else {
                    smoking = "N";
                }

            }
        } else {
            String msg = context.getString(R.string.voice_book_unable_to_loc);
            throw new IllegalArgumentException(msg);
        }

        // Get the EvaMoney if specifying amount limit.
        // NOTE: Concur MWS cannot support specifying amount yet, so we'll have to
        // filter/sort on the client side.
        if (apiReply.money != null) {
            amountRestriction = apiReply.money;
        }

    } // end constructor
} // end EvaHotelReply
