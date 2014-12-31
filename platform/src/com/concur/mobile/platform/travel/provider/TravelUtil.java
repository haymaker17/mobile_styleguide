package com.concur.mobile.platform.travel.provider;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.provider.PlatformContentProvider;
import com.concur.mobile.platform.travel.location.LocationChoice;
import com.concur.mobile.platform.travel.trip.Itinerary;
import com.concur.mobile.platform.travel.triplist.TripSummary;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a set of travel provider utility methods.
 */
public class TravelUtil {

    private static final String CLS_TAG = "TravelUtil";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Will update the list of location search results.
     * 
     * @param context
     *            contains a reference to an application context.
     * @param locationChoices
     *            contains the list of search locations.
     */
    public static void updateLocationSearch(Context context, List<LocationChoice> locationChoices) {
        ContentResolver resolver = context.getContentResolver();

        // Punt all location choice information.
        int rowsAffected = resolver.delete(Travel.LocationChoiceColumns.CONTENT_URI, null, null);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateLocationSearchInfo: deleted " + Integer.toString(rowsAffected)
                    + " location choice rows.");
        }

        // Insert new location choice results.

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (locationChoices != null) {
            for (LocationChoice locChoice : locationChoices) {

                // Set city.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.CITY, locChoice.city);

                // Set country
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.COUNTRY, locChoice.country);

                // Set country abbreviation.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.COUNTRY_ABBREVIATION,
                        locChoice.countryAbbrev);

                // Set IATA.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.IATA, locChoice.iata);

                // Set location.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.LOCATION, locChoice.location);

                // Set state.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.STATE, locChoice.state);

                // Set lat.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.LAT, locChoice.lat);

                // Set lon.
                ContentUtils.putValue(values, Travel.LocationChoiceColumns.LON, locChoice.lon);

                Uri locChoiceUri = resolver.insert(Travel.LocationChoiceColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertLocationChoiceInfo: new location choice uri '"
                            + ((locChoiceUri != null) ? locChoiceUri.toString() : "null"));
                }
                values.clear();
            }
        }
    }

    /**
     * Will update a trip within the travel provider.
     * 
     * @param context
     *            contains an application context.
     * @param trip
     *            contains the trip.
     * @param userId
     *            contains the user id.
     * @throws IllegalArgumentException
     *             if <code>trip</code> is null or <code>userId</code> is null or empty.
     */
    public static void updateTripInfo(Context context, Itinerary trip, String userId) {

        if (trip == null) {
            throw new IllegalArgumentException(CLS_TAG + ".updateTripInfo: trip is null!");
        }
        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".updateTripInfo: userId is null or empty!");
        }

        // Punt any existing trip detail object based on 'trip.itinLocator'.
        ContentResolver resolver = context.getContentResolver();
        StringBuilder strBldr = new StringBuilder();
        boolean foundIdToLookUpTrip = false;
        String[] whereArgs = new String[1];
        if (trip.cliqBookTripId != null) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".updateTripInfo: using 'cliqBookTripId' to locate trip locally.");
            }
            strBldr.append(Travel.TripColumns.CLIQBOOK_TRIP_ID);
            strBldr.append(" = ?");
            whereArgs[0] = Integer.toString(trip.cliqBookTripId);
            foundIdToLookUpTrip = true;
        } else if (trip.itinLocator != null) {
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".updateTripInfo: using 'itinLocator' to locate trip locally.");
            }
            strBldr.append(Travel.TripColumns.ITIN_LOCATOR);
            strBldr.append(" = ?");
            whereArgs[0] = trip.itinLocator;
            foundIdToLookUpTrip = true;
        }

        if (foundIdToLookUpTrip) {
            // Punt all trip information.
            String whereClause = strBldr.toString();
            int rowsAffected = resolver.delete(Travel.TripColumns.CONTENT_URI, whereClause, whereArgs);
            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".updateTripInfo: deleted " + Integer.toString(rowsAffected)
                        + " trip rows.");
                if (DEBUG) {
                    // Print out report trip detail information.
                    reportTripDetailTableRowCounts(context);
                }
            }
        } else {
            Log.w(Const.LOG_TAG, CLS_TAG
                    + ".updateTripInfo: unable to find non-null ID in itinerary to look up locally stored itinerary.");
        }

        // Insert the trip.
        TripUtil.insertTrip(context.getContentResolver(), trip, userId);

        if (DEBUG) {
            // Print out report trip detail information.
            reportTripDetailTableRowCounts(context);
        }
    }

    /**
     * Will report the row counts of all tables associated with a trip detail object.
     * 
     * @param context
     *            contains an application context.
     */
    public static void reportTripDetailTableRowCounts(Context context) {

        ContentResolver resolver = context.getContentResolver();

        reportTableRows(resolver, Travel.TripColumns.TABLE_NAME, Travel.TripColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.EnhancementDayColumns.TABLE_NAME, Travel.EnhancementDayColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.SortableSegmentColumns.TABLE_NAME, Travel.SortableSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.EnhancementOfferColumns.TABLE_NAME, Travel.EnhancementOfferColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.OfferLinkColumns.TABLE_NAME, Travel.OfferLinkColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.OfferContentColumns.TABLE_NAME, Travel.OfferContentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.ContentLinkColumns.TABLE_NAME, Travel.ContentLinkColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.MapDisplayColumns.TABLE_NAME, Travel.MapDisplayColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.DisplayOverlayColumns.TABLE_NAME, Travel.DisplayOverlayColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.ValidityLocationColumns.TABLE_NAME, Travel.ValidityLocationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.ValidityTimeRangeColumns.TABLE_NAME,
                Travel.ValidityTimeRangeColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.TripRuleViolationColumns.TABLE_NAME,
                Travel.TripRuleViolationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.CarRuleViolationColumns.TABLE_NAME, Travel.CarRuleViolationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.HotelRuleViolationColumns.TABLE_NAME,
                Travel.HotelRuleViolationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.FlightRuleViolationColumns.TABLE_NAME,
                Travel.FlightRuleViolationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.RailRuleViolationColumns.TABLE_NAME,
                Travel.RailRuleViolationColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.RuleColumns.TABLE_NAME, Travel.RuleColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.RuleViolationReasonColumns.TABLE_NAME,
                Travel.RuleViolationReasonColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.TravelPointColumns.TABLE_NAME, Travel.TravelPointColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.BookingColumns.TABLE_NAME, Travel.BookingColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.AirlineTicketColumns.TABLE_NAME, Travel.AirlineTicketColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.PassengerColumns.TABLE_NAME, Travel.PassengerColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.FrequentTravelerProgramColumns.TABLE_NAME,
                Travel.FrequentTravelerProgramColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.SegmentColumns.TABLE_NAME, Travel.SegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.AirSegmentColumns.TABLE_NAME, Travel.AirSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.FlightStatusColumns.TABLE_NAME, Travel.FlightStatusColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.SeatColumns.TABLE_NAME, Travel.SeatColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.HotelSegmentColumns.TABLE_NAME, Travel.HotelSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.CarSegmentColumns.TABLE_NAME, Travel.CarSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.RailSegmentColumns.TABLE_NAME, Travel.RailSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.DiningSegmentColumns.TABLE_NAME, Travel.DiningSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.EventSegmentColumns.TABLE_NAME, Travel.EventSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.ParkingSegmentColumns.TABLE_NAME, Travel.ParkingSegmentColumns.CONTENT_URI);
        reportTableRows(resolver, Travel.RideSegmentColumns.TABLE_NAME, Travel.RideSegmentColumns.CONTENT_URI);
    }

    private static void reportTableRows(ContentResolver resolver, String tableName, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, null, null, null, null);
            if (cursor != null) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".reportTableRows: table (" + tableName + ") -> " + cursor.getCount());
            } else {
                Log.d(Const.LOG_TAG, CLS_TAG + ".reportTableRows: table (" + tableName + ") -> 0.");
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    /**
     * Will update the list of trip summary objects within the travel provider.
     * 
     * @param context
     *            contains an application context.
     * @param tripSummaries
     *            contains a list of trip summaries.
     * @param userId
     *            contains the user id.
     * @throws IllegalArgumentException
     *             if <code>userId</code> is null or empty.
     */
    public static void updateTripSummaryInfo(Context context, List<TripSummary> tripSummaries, String userId) {

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".updateTripSummaryInfo: userId is null or empty!");
        }

        ContentResolver resolver = context.getContentResolver();

        StringBuilder strBldr = new StringBuilder();
        strBldr.append(Travel.TripSummaryColumns.USER_ID);
        strBldr.append(" = ?");
        String whereClause = strBldr.toString();
        String[] whereArgs = { userId };

        // Punt all trip summary information.
        int rowsAffected = resolver.delete(Travel.TripSummaryColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateTripSummaryInfo: deleted " + Integer.toString(rowsAffected)
                    + " trip summary rows.");
        }
        // Insert new trip summary information.
        insertTripSummaryInfo(resolver, tripSummaries, userId);

        strBldr.setLength(0);
        strBldr.append(Travel.TripSummaryMessageColumns.USER_ID);
        strBldr.append(" = ?");
        whereClause = strBldr.toString();
        // Punt all trip summary information.
        rowsAffected = resolver.delete(Travel.TripSummaryMessageColumns.CONTENT_URI, whereClause, whereArgs);
        if (DEBUG) {
            Log.d(Const.LOG_TAG, CLS_TAG + ".updateTripSummaryInfo: deleted " + Integer.toString(rowsAffected)
                    + " trip summary message rows.");
        }
        // Insert new trip summary message information.
        insertTripSummaryMessageInfo(resolver, tripSummaries, userId);
    }

    /**
     * Will insert trip summary information into the <code>Travel.TripSummaryColumns.TABLE_NAME</code> table.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param tripSummaries
     *            contains a list of <code>TripSummary</code> objects.
     * @param userId
     *            contains the user id.
     */
    private static void insertTripSummaryInfo(ContentResolver resolver, List<TripSummary> tripSummaries, String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (tripSummaries != null) {
            for (TripSummary tripSum : tripSummaries) {
                // Set approval status.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.APPROVAL_STATUS, tripSum.approvalStatus);
                // Set approver id.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.APPROVER_ID, tripSum.approverId);
                // Set approver name.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.APPROVER_NAME, tripSum.approverName);
                // Set authorization number.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.AUTHORIZATION_NUMBER,
                        tripSum.authorizationNumber);
                // Set booked via
                ContentUtils.putValue(values, Travel.TripSummaryColumns.BOOKED_VIA, tripSum.bookedVia);
                // Set booking source.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.BOOKING_SOURCE, tripSum.bookingSource);
                // Set can be expensed.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.CAN_BE_EXPENSED, tripSum.canBeExpensed);
                // Set cliqbook state.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.CLIQ_BOOK_STATE, tripSum.cliqBookState);
                // Set end date local.
                String formattedDate = null;
                if (tripSum.endDateLocal != null) {
                    formattedDate = Format.safeFormatCalendar(Parse.XML_DF_LOCAL, tripSum.endDateLocal);
                }
                ContentUtils.putValue(values, Travel.TripSummaryColumns.END_DATE_LOCAL, formattedDate);
                // Set end date UTC.
                formattedDate = null;
                if (tripSum.endDateUtc != null) {
                    formattedDate = Format.safeFormatCalendar(Parse.XML_DF, tripSum.endDateUtc);
                }
                ContentUtils.putValue(values, Travel.TripSummaryColumns.END_DATE_UTC, formattedDate);
                // Set has others.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.HAS_OTHERS, tripSum.hasOthers);
                // Set has tickets.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.HAS_TICKETS, tripSum.hasTickets);
                // Set is expensed.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.IS_EXPENSED, tripSum.isExpensed);
                // Set is GDS booking.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.IS_GDS_BOOKING, tripSum.isGdsBooking);
                // Set is personal
                ContentUtils.putValue(values, Travel.TripSummaryColumns.IS_PERSONAL, tripSum.isPersonal);
                // Set is withdrawn.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.IS_WITHDRAWN, tripSum.isWithdrawn);
                // Set is public.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.IS_PUBLIC, tripSum.isPublic);
                // Set itin id.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.ITIN_ID, tripSum.itinId);
                // Set itin locator.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.ITIN_LOCATOR, tripSum.itinLocator);
                // Set itin source list.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.ITIN_SOURCE_LIST, tripSum.itinSourceList);
                // Set record locator.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.RECORD_LOCATOR, tripSum.recordLocator);
                // Set segment types.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.SEGMENT_TYPES, tripSum.segmentTypes);
                // Set start date local.
                formattedDate = null;
                if (tripSum.startDateLocal != null) {
                    formattedDate = Format.safeFormatCalendar(Parse.XML_DF_LOCAL, tripSum.startDateLocal);
                }
                ContentUtils.putValue(values, Travel.TripSummaryColumns.START_DATE_LOCAL, formattedDate);
                // Set start date UTC.
                formattedDate = null;
                if (tripSum.startDateUtc != null) {
                    formattedDate = Format.safeFormatCalendar(Parse.XML_DF, tripSum.startDateUtc);
                }
                ContentUtils.putValue(values, Travel.TripSummaryColumns.START_DATE_UTC, formattedDate);
                // Set trip id.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.TRIP_ID, tripSum.tripId);
                // Set trip key.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.TRIP_KEY, tripSum.tripKey);
                // Set trip name.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.TRIP_NAME, tripSum.tripName);
                // Set trip status.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.TRIP_STATUS, tripSum.tripStatus);

                // Set the user id.
                ContentUtils.putValue(values, Travel.TripSummaryColumns.USER_ID, userId);

                Uri tripSumUri = resolver.insert(Travel.TripSummaryColumns.CONTENT_URI, values);
                if (DEBUG) {
                    Log.d(Const.LOG_TAG, CLS_TAG + ".insertTripSummaryInfo: new trip summary uri '"
                            + ((tripSumUri != null) ? tripSumUri.toString() : "null"));
                }
                values.clear();
            }
        }
    }

    /**
     * Will insert trip summary message information into the <code>Travel.TripSummaryMessageColumns.TABLE_NAME</code> table.
     * 
     * @param resolver
     *            contains a reference to a content resolver.
     * @param tripSummaries
     *            contains a list of <code>TripSummary</code> objects.
     * @param userId
     *            contains the user id.
     */
    private static void insertTripSummaryMessageInfo(ContentResolver resolver, List<TripSummary> tripSummaries,
            String userId) {

        // Set up the content values object.
        ContentValues values = new ContentValues();

        if (tripSummaries != null) {
            for (TripSummary tripSum : tripSummaries) {

                if (tripSum.tripStateMessages != null && tripSum.tripStateMessages.messages != null) {

                    for (String msg : tripSum.tripStateMessages.messages) {

                        // Set message.
                        ContentUtils.putValue(values, Travel.TripSummaryMessageColumns.MESSAGE, msg);

                        // Set trip id.
                        ContentUtils.putValue(values, Travel.TripSummaryMessageColumns.TRIP_ID, tripSum.tripId);

                        // Set the user id.
                        ContentUtils.putValue(values, Travel.TripSummaryMessageColumns.USER_ID, userId);

                        Uri tripSumMsgUri = resolver.insert(Travel.TripSummaryMessageColumns.CONTENT_URI, values);
                        if (DEBUG) {
                            Log.d(Const.LOG_TAG, CLS_TAG
                                    + ".insertTripSummaryMessageInfo: new trip summary message uri '"
                                    + ((tripSumMsgUri != null) ? tripSumMsgUri.toString() : "null"));
                        }
                        values.clear();
                    }
                }
            }
        }
    }

    /**
     * Will set the passphrase used to access content from the <code>Travel</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param passphrase
     *            contains the passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>passphrase</code> is null or empty
     */
    @SuppressLint("NewApi")
    public static boolean setPassphrase(Context context, String passphrase) {

        if (TextUtils.isEmpty(passphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".setPassphrase: passphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Travel.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE,
                    passphrase, null);
        } else {
            // First, attempt to retrieve an instance of TravelProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            TravelProvider travelProvider = TravelProvider.getTravelProvider();
            if (travelProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the TravelProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Travel provider.
                try {
                    resolver.query(Travel.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".setPassphrase: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                travelProvider = TravelProvider.getTravelProvider();
            }
            if (travelProvider != null) {
                result = travelProvider.call(PlatformContentProvider.PROVIDER_METHOD_SET_PASSPHRASE, passphrase, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".setPassphrase: unable to force creation of the Travel content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will reset the passphrase used to access content from the <code>Travel</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @param currentPassphrase
     *            contains the current passphrase.
     * @param newPassphrase
     *            contains the new passphrase.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     * @throws IllegalArgumentException
     *             if <code>currentPassphrase</code> or <code>newPassphrase</code> is null or empty.
     */
    @SuppressLint("NewApi")
    public static boolean resetPassphrase(Context context, String currentPassphrase, String newPassphrase) {

        if (TextUtils.isEmpty(currentPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: currentPassphrase is null or empty!");
        }
        if (TextUtils.isEmpty(newPassphrase)) {
            throw new IllegalArgumentException(CLS_TAG + ".resetPassphrase: newPassphrase is null or empty!");
        }

        boolean retVal = true;
        Bundle result = null;
        Bundle extras = new Bundle();
        extras.putString(PlatformContentProvider.PROVIDER_METHOD_PASSPHRASE_KEY, newPassphrase);
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Travel.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                    currentPassphrase, extras);
        } else {
            // First, attempt to retrieve an instance of TravelProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            TravelProvider travelProvider = TravelProvider.getTravelProvider();
            if (travelProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the TravelProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Travel provider.
                try {
                    resolver.query(Travel.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG
                            + ".resetPassphrase: forced creation of provider -- ignore this error: " + exc.getMessage());
                }
                travelProvider = TravelProvider.getTravelProvider();
            }
            if (travelProvider != null) {
                result = travelProvider.call(PlatformContentProvider.PROVIDER_METHOD_RESET_PASSPHRASE,
                        currentPassphrase, extras);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".resetPassphrase: unable to force creation of the Travel content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

    /**
     * Will clear all content contained in the <code>Travel</code> content provider.
     * 
     * @param context
     *            contains an application context.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    @SuppressLint("NewApi")
    public static boolean clearContent(Context context) {
        boolean retVal = true;
        Bundle result = null;
        // Check for HoneyComb or later to execute 'call' on the ContentResolver object.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ContentResolver resolver = context.getContentResolver();
            result = resolver.call(Travel.AUTHORITY_URI, PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null,
                    null);
        } else {
            // First, attempt to retrieve an instance of TravelProvider if the content resolver
            // has created one, if not, then force the content resolver to create it with a bogus query.
            TravelProvider travelProvider = TravelProvider.getTravelProvider();
            if (travelProvider == null) {
                ContentResolver resolver = context.getContentResolver();
                // Force the content resolver to create the TravelProvider. The query will fail immediately since
                // it doesn't refer to any actual table within the Travel provider.
                try {
                    resolver.query(Travel.AUTHORITY_URI, null, null, null, null);
                } catch (Exception exc) {
                    // No-op...
                    Log.i(Const.LOG_TAG, CLS_TAG + ".clearContent: forced creation of provider -- ignore this error: "
                            + exc.getMessage());
                }
                travelProvider = TravelProvider.getTravelProvider();
            }
            if (travelProvider != null) {
                result = travelProvider.call(PlatformContentProvider.PROVIDER_METHOD_CLEAR_CONTENT, null, null);
            } else {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".clearContent: unable to force creation of the Travel content provider!");
            }
        }
        if (result != null) {
            retVal = result.getBoolean(PlatformContentProvider.PROVIDER_METHOD_RESULT_KEY, false);
        } else {
            retVal = false;
        }
        return retVal;
    }

}
