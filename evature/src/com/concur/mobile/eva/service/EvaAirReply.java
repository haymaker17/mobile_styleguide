/**
 * Copyright (c) 2013 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.service;

import java.util.Calendar;
import java.util.SortedMap;
import java.util.TreeMap;

import org.json.JSONException;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.eva.R;
import com.concur.mobile.eva.data.EvaLocation;
import com.concur.mobile.eva.data.EvaMoney;
import com.concur.mobile.eva.data.EvaTime;
import com.concur.mobile.eva.util.Const;

/**
 * Object representing an Air from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaAirReply {

    public final String CLS_TAG = EvaAirReply.class.getSimpleName();

    public static enum SearchMode {
        None, OneWay, RoundTrip, MultiSegment
    };

    public SearchMode searchMode;

    public String departLocation;
    public String departCountry;
    public String departLocIATA;
    public Bundle departLocationBundle = new Bundle();
    public Calendar departDate;

    public String arriveLocation;
    public String arriveCountry;
    public String arriveLocIATA;
    public Bundle arriveLocationBundle = new Bundle();
    public Calendar returnDate;

    public String cabinClass;
    public boolean refundableOnly;

    public EvaMoney amountRestriction;

    private String locationKeyName;
    private String locationKeyLatitude;
    private String locationKeyLongitude;

    /**
     * Constructor that parses the EvaApiReply and generates a Hotel Search object suitable for the Concur MWS.
     * 
     * 
     * @param context
     *            contains a reference to an application context.
     * @param seatClassEconomy
     *            contains the name for economy cabin class.
     * @param seatClassPremium
     *            contains the name for premium cabin class.
     * @param seatClassBusiness
     *            contains the name for business cabin class.
     * @param seatClassFirst
     *            contains the name for first-class cabin class.
     * @param locationKeyName
     *            contains the name of the key to place the location name into location bundles.
     * @param locationKeyLatitude
     *            contains the name of the key to place the location latitude into location bundles.
     * @param locationKeyLongitude
     *            contains the name of the key to place the location longitude into location bundles.
     * @param apiReply
     *            contains a reference to the Eva api reply.
     * 
     * 
     * @throws IllegalArgumentException
     */
    public EvaAirReply(Context context, String seatClassEconomy, String seatClassPremium, String seatClassBusiness,
            String seatClassFirst, String locationKeyName, String locationKeyLatitude, String locationKeyLongitude,
            EvaApiReply apiReply) throws IllegalArgumentException {

        this.locationKeyName = locationKeyName;
        this.locationKeyLatitude = locationKeyLatitude;
        this.locationKeyLongitude = locationKeyLongitude;

        // Default seat class to economy.
        cabinClass = seatClassEconomy;

        // Check flight attributes.
        if (apiReply.flightAttributes != null) {

            // Check if one-way or round trip.
            if (apiReply.flightAttributes.oneWay != null) {
                searchMode = (apiReply.flightAttributes.oneWay) ? SearchMode.OneWay : SearchMode.RoundTrip;
            }

            // Check seat class.
            // NOTE: The seat class needs to be filtered
            // on the client based on user's settings/policy.
            if (apiReply.flightAttributes.seatClass != null && apiReply.flightAttributes.seatClass.length() > 0) {
                // This can return multiple seat classes.
                // e.g. if user asks for "business or first class".
                // We'll just use the first seat class.
                try {
                    String reqCabinClass = apiReply.flightAttributes.seatClass.getString(0);
                    if (reqCabinClass.equalsIgnoreCase("First")) {
                        cabinClass = seatClassFirst;
                    } else if (reqCabinClass.equalsIgnoreCase("Business")) {
                        cabinClass = seatClassBusiness;
                    } else if (reqCabinClass.equalsIgnoreCase("Premium")) {
                        cabinClass = seatClassPremium;
                    } else if (reqCabinClass.equalsIgnoreCase("Economy")) {
                        cabinClass = seatClassEconomy;
                    }

                } catch (JSONException e) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".EvaAirReply() - could not parse Cabin Class!", e);
                }
            }
        }

        // Get the EvaMoney if specifying amount limit.
        // NOTE: Concur MWS cannot support specifying amount yet, so we'll have to
        // filter/sort on the client side.
        if (apiReply.money != null) {
            amountRestriction = apiReply.money;
        }

        // Get the locations
        EvaLocation[] locations = apiReply.locations;

        // We need to get the 0-index Location to find the "Next" Location.
        if (locations != null && locations.length >= 1) {

            EvaLocation departLoc = null;
            EvaLocation arriveLoc = null;

            // Go through all the Locations and sort them based on Index.
            SortedMap<Integer, EvaLocation> locationMap = new TreeMap<Integer, EvaLocation>();
            for (EvaLocation currLoc : locations) {
                locationMap.put(currLoc.index, currLoc);

                // The departure location is the EvaLocation with an Index of 0 and a non-null Next value.
                // If the location has an Index of 0 and no Next value, then it is the return location back HOME.
                if (currLoc.index == 0 && currLoc.next > 0) {
                    departLoc = currLoc;
                }
            }

            // No appropriate Location, so throw error.
            if (departLoc == null) {
                String msg = context.getString(R.string.voice_book_unable_to_determine_departure_loc);
                throw new IllegalArgumentException(msg);
            }

            // The arrival location is specified by the departure location's next index.
            // XXX: Notice - this algorithm doesn't handle multi-segment flights.
            arriveLoc = locationMap.get(departLoc.next);
            // No appropriate Location, so throw error.
            if (arriveLoc == null) {
                String msg = context.getString(R.string.voice_book_unable_to_determine_arrival_loc);
                throw new IllegalArgumentException(msg);
            }

            // If searchMode hasn't been set yet...
            if (searchMode == null) {
                // If the arrive Location has a Next of 0, then this is a round-trip flight.
                if (arriveLoc.next == 0) {
                    searchMode = SearchMode.RoundTrip;
                } else {
                    // Default to one-way.
                    // XXX multi-segment is not supported here!
                    searchMode = SearchMode.OneWay;
                }
            }

            // Departure Airport
            departLocationBundle = generateLocationBundle(departLoc);
            departLocation = getLocationName(departLoc);
            if (departLocationBundle == null || departLocation == null) {
                String msg = context.getString(R.string.voice_book_unable_to_determine_departure_loc);
                throw new IllegalArgumentException(msg);
            }
            departLocIATA = (String) departLocationBundle.get(locationKeyName);
            departCountry = departLoc.country;
            // Get Departure date and time.
            departDate = getDepartureDate(departLoc, null);

            // Arrival Airport
            arriveLocationBundle = generateLocationBundle(arriveLoc);
            arriveLocation = getLocationName(arriveLoc);
            if (arriveLocationBundle == null || arriveLocation == null) {
                String msg = context.getString(R.string.voice_book_unable_to_determine_arrival_loc);
                throw new IllegalArgumentException(msg);
            }
            arriveLocIATA = (String) arriveLocationBundle.get(locationKeyName);
            arriveCountry = arriveLoc.country;
            // Return date and time.
            if (searchMode == SearchMode.OneWay) {
                returnDate = null;
            } else {
                returnDate = getDepartureDate(arriveLoc, departDate);
            }

        } else {
            String msg = context.getString(R.string.voice_book_unable_to_loc);
            throw new IllegalArgumentException(msg);
        }

    } // end constructor

    // ///////////////////// HELPER METHODS ////////////////////////

    private Calendar getDepartureDate(EvaLocation loc, Calendar departDate) {

        String depDate = null;
        String depTime = null;

        if (loc != null) {
            if (loc.departureTime != null) {
                depDate = loc.departureTime.date;
                depTime = loc.departureTime.time;
            }
        }

        // MOB-14270 - If this is a return date (i.e. departDate is NOT null),
        // then default to 3 days after departDate.
        // Else, default the date to today and the time to current time.
        if (depDate == null) {

            if (departDate != null) {
                long time = departDate.getTimeInMillis() + 259200000L; // Add 3 days.
                depDate = EvaTime.formatDate(time);
                depTime = "09:00:00";
            } else {
                Calendar now = Calendar.getInstance();
                long time = now.getTimeInMillis();
                depDate = EvaTime.formatDate(time);
                // If the current time is past 9:00 AM, then use the current time + 1 hour.
                if (now.get(Calendar.HOUR_OF_DAY) < 9) {
                    depTime = "09:00:00";
                } else {
                    depTime = EvaTime.formatTime(time + 3600000L); // Add 1 hour.
                }

            }

        }

        // If the date is specified, but not the time, then default to 9:00 AM.
        if (depTime == null) {
            depTime = "09:00:00";
        }

        return EvaTime.convertToConcurServerCalendar(depDate, depTime, "UTC");
    }

    private String getLocationName(EvaLocation loc) {

        String locationName = null;

        // Get the location name.
        if (loc.type != null && loc.name != null) {

            if (!loc.type.equalsIgnoreCase("Airport")) {
                // If this is a non-airport (e.g. City) location,
                // parse out the country, GID, and other junk.
                // We just want the City and state/province.
                String[] locName = loc.name.split(",");
                if (locName.length > 1) {
                    locationName = locName[0] + "," + locName[1];
                } else {
                    locationName = locName[0];
                }

            } else if (loc.type.equals("Airport")) {
                // Parse the airport name.
                int index = loc.name.indexOf('=');
                if (index != -1) {
                    locationName = loc.name.substring(index + 1).trim();
                } else {
                    locationName = loc.name;
                }

                // Don't need the country code, just the airport name.
                locationName = locationName.split(",")[0];

            }
        }

        return locationName;
    }

    private Bundle generateLocationBundle(EvaLocation loc) {

        if (loc == null) {
            return null;
        }

        Bundle locBundle = new Bundle();

        // Get the lat/long of the location.
        // If it's not specified, default to the user's current (GPS/cell) location.
        if (loc.latitude != null && loc.longitude != null) {
            locBundle.putString(locationKeyLatitude, loc.latitude);
            locBundle.putString(locationKeyLongitude, loc.longitude);
        } else {
            return null;
        }

        // Set the depart airport IATA code.
        if (loc.airports != null && !loc.airports.isEmpty()) {
            // Use the first Airport code.
            locBundle.putString(locationKeyName, loc.airports.get(0));
        } else {
            return null;
        }

        return locBundle;
    }

} // end EvaAirReply
