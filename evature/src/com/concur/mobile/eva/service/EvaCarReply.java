/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.eva.service;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.location.Address;
import android.location.Location;

import com.concur.mobile.eva.R;
import com.concur.mobile.eva.data.EvaLocation;
import com.concur.mobile.eva.data.EvaMoney;
import com.concur.mobile.eva.data.EvaTime;

/**
 * Object representing an Car from the Eva JSON response.
 * 
 * @author Chris N. Diaz
 * 
 */
public class EvaCarReply {

    public final String CLS_TAG = EvaCarReply.class.getSimpleName();

    public String locationName;
    public String pickupLat;
    public String pickupLong;
    public Calendar pickupDateTime;
    public String dropoffLat;
    public String dropoffLong;
    public Calendar dropoffDateTime;
    public String carType;
    public boolean smoking;

    public EvaMoney amountRestriction;

    /**
     * Constructor that parses the EvaApiReply and generates a Car Search object suitable for the Concur MWS.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param currentLocation
     *            contains a reference to the current location.
     * @param currentAddress
     *            contains a reference to the current address.
     * @param apiReply
     * 
     * @throws IllegalArgumentException
     */
    public EvaCarReply(Context context, Location currentLocation, Address currentAddress, EvaApiReply apiReply)
            throws IllegalArgumentException {

        // Set the different car attributes.
        if (apiReply.carAttributes != null) {
            // The car type should be one that is available in the user's settings.
            if (apiReply.carAttributes.carType != null) {
                carType = apiReply.carAttributes.carType;
            }
            smoking = apiReply.carAttributes.smoking;
        }

        // Get the EvaMoney if specifying amount limit.
        // NOTE: Concur MWS cannot support specifying amount yet, so we'll have to
        // filter/sort on the client side.
        if (apiReply.money != null) {
            amountRestriction = apiReply.money;
        }

        // Get the locations
        EvaLocation[] locations = apiReply.locations;

        // Go through all the Locations and get the one with the lowest non-zero index.
        if (locations != null && locations.length >= 1) {

            // Go through all the Locations and get the 0-index Location.
            // Note, we don't care about the other Locations right now because
            // our MWS doesn't support picking up at one location and
            // dropping off at a different location.
            EvaLocation pickupLoc = null;
            for (EvaLocation currLoc : locations) {

                if (currLoc.index == 0) {
                    pickupLoc = currLoc;
                    break;
                }
            }

            // No appropriate Location, so throw error.
            if (pickupLoc == null) {
                String msg = context.getString(R.string.voice_book_unable_to_determine_pickup_loc);
                throw new IllegalArgumentException(msg);
            }

            if (pickupLoc.airports != null && !pickupLoc.airports.isEmpty()) {

                // MOB-12081 - If this is an Airport search, then the Airport property should only have 1 entry
                // unless the user specifically searched for an airport (e.g. "Near Edinburgh airport"),
                // which may return many airport codes.
                if (pickupLoc.airports.size() == 1
                        || (pickupLoc.geoAttributes != null && pickupLoc.geoAttributes.airport)) {
                    locationName = pickupLoc.airports.get(0);
                } else {
                    locationName = pickupLoc.name;
                }
            } else {
                locationName = pickupLoc.name;
            }

            pickupLat = pickupLoc.latitude;
            pickupLong = pickupLoc.longitude;

            if (pickupLat == null || pickupLong == null) {

                // Try using the GPS location, if we have it.
                Location currLoc = currentLocation;
                if (currLoc != null) {
                    pickupLat = Double.toString(currLoc.getLatitude());
                    pickupLong = Double.toString(currLoc.getLongitude());

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

                if (pickupLat == null || pickupLong == null) {
                    String msg = context.getString(R.string.voice_book_unable_to_determine_pickup_loc);
                    throw new IllegalArgumentException(msg);
                }
            }

            // NOTE: Pickup and dropoff location is the same until we support
            // different dropoff location.
            dropoffLat = pickupLat;
            dropoffLong = pickupLong;

            pickupDateTime = getPickupDateTime(pickupLoc.pickupTime);
            dropoffDateTime = getReturnDateTime(pickupLoc.returnTime, pickupLoc.pickupTime,
                    pickupLoc.rentalCarDuration, pickupDateTime);

        } else {
            String msg = context.getString(R.string.voice_book_unable_to_loc);
            throw new IllegalArgumentException(msg);
        }

    } // end constructor

    // ///////////////////// HELPER METHODS ////////////////////////

    private Calendar getPickupDateTime(EvaTime evaTime) {

        String date = null;
        String time = null;

        if (evaTime != null) {
            date = evaTime.date;
            time = evaTime.time;
        }

        // If the date is null, default the date to today and the time to current time.
        if (date == null) {
            long t = (new Date()).getTime();
            date = EvaTime.formatDate(t);
        }

        // If the date is specified, but not the time, then default to 9:00 AM.
        if (time == null) {
            // If the pickup date is today and the current time is past
            // 9:00 AM, then use the current time.
            Calendar c = EvaTime.convertToConcurServerCalendar(date, "UTC");
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DATE);
            int year = c.get(Calendar.YEAR);
            Calendar now = Calendar.getInstance();
            if (now.get(Calendar.MONTH) == month && now.get(Calendar.DATE) == day && now.get(Calendar.YEAR) == year
                    && now.get(Calendar.HOUR_OF_DAY) >= 9) {

                long t = (new Date()).getTime() + 3600000; // Get the next hour.
                time = EvaTime.formatTime(t);
            } else {
                time = "09:00:00";
            }
        }

        return EvaTime.convertToConcurServerCalendar(date, time, "UTC");
    }

    private Calendar getReturnDateTime(EvaTime evaReturnTime, EvaTime evaPickupTime, int rentalCarDuration,
            Calendar pickupTime) {

        String date = null;
        String time = null;

        if (evaReturnTime != null) {
            date = evaReturnTime.date;
            time = evaReturnTime.time;
        }

        // If the date is null, first check to see if the Eva pickup time
        // contains a delta. If not, try the rental car duration.
        // If that fails, default the date to one day after the pickupTime.
        if (date == null) {

            if (evaPickupTime != null && evaPickupTime.delta != null) {
                String delta = evaPickupTime.delta.substring(evaPickupTime.delta.indexOf("+") + 1);
                int day = pickupTime.get(Calendar.DATE) + Integer.parseInt(delta);
                int month = pickupTime.get(Calendar.MONTH) + 1; // month is 0-index based
                int year = pickupTime.get(Calendar.YEAR);

                date = year + "-" + month + "-" + day;

            } else if (rentalCarDuration > 0) {
                int day = pickupTime.get(Calendar.DATE) + rentalCarDuration;
                int month = pickupTime.get(Calendar.MONTH) + 1; // month is 0-index based
                int year = pickupTime.get(Calendar.YEAR);

                date = year + "-" + month + "-" + day;

            } else {
                long t = pickupTime.getTimeInMillis() + 86400000; // Add 24 hours.
                date = EvaTime.formatDate(t);
            }
        }

        // If the time is not specified, then default to 5:00 PM.
        if (time == null) {
            time = "17:00:00";
        }

        return EvaTime.convertToConcurServerCalendar(date, time, "UTC");
    }

} // end EvaCarReply

