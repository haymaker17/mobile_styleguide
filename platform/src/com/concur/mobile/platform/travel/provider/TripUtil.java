package com.concur.mobile.platform.travel.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.travel.trip.AirSegmentData;
import com.concur.mobile.platform.travel.trip.AirlineTicket;
import com.concur.mobile.platform.travel.trip.Booking;
import com.concur.mobile.platform.travel.trip.CarRuleViolation;
import com.concur.mobile.platform.travel.trip.CarSegmentData;
import com.concur.mobile.platform.travel.trip.ContentLink;
import com.concur.mobile.platform.travel.trip.Day;
import com.concur.mobile.platform.travel.trip.DiningSegmentData;
import com.concur.mobile.platform.travel.trip.EventSegmentData;
import com.concur.mobile.platform.travel.trip.FlightRuleViolation;
import com.concur.mobile.platform.travel.trip.FlightStatus;
import com.concur.mobile.platform.travel.trip.FrequentTravelerProgram;
import com.concur.mobile.platform.travel.trip.HotelRuleViolation;
import com.concur.mobile.platform.travel.trip.HotelSegmentData;
import com.concur.mobile.platform.travel.trip.Itinerary;
import com.concur.mobile.platform.travel.trip.Location;
import com.concur.mobile.platform.travel.trip.MapDisplay;
import com.concur.mobile.platform.travel.trip.Offer;
import com.concur.mobile.platform.travel.trip.OfferContent;
import com.concur.mobile.platform.travel.trip.OfferLink;
import com.concur.mobile.platform.travel.trip.Overlay;
import com.concur.mobile.platform.travel.trip.ParkingSegmentData;
import com.concur.mobile.platform.travel.trip.Passenger;
import com.concur.mobile.platform.travel.trip.RailRuleViolation;
import com.concur.mobile.platform.travel.trip.RailSegmentData;
import com.concur.mobile.platform.travel.trip.RideSegmentData;
import com.concur.mobile.platform.travel.trip.Rule;
import com.concur.mobile.platform.travel.trip.RuleViolation;
import com.concur.mobile.platform.travel.trip.RuleViolationReason;
import com.concur.mobile.platform.travel.trip.SeatData;
import com.concur.mobile.platform.travel.trip.Segment;
import com.concur.mobile.platform.travel.trip.SegmentData;
import com.concur.mobile.platform.travel.trip.SortableSegment;
import com.concur.mobile.platform.travel.trip.TimeRange;
import com.concur.mobile.platform.travel.trip.TravelPoint;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.ContentUtils;
import com.concur.mobile.platform.util.Parse;

/**
 * Provides a utility class for updating the travel content provider with information in an <code>Itinerary</code> object.
 */
public class TripUtil {

    private static final String CLS_TAG = "ItineraryUtil";

    private static final Boolean DEBUG = Boolean.TRUE;

    /**
     * Will insert trip information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param trip
     *            contains the itinerary object.
     * @param userId
     *            contains the user id.
     * @returns returns the content provider id of the inserted trip information.
     * @throws IllegalArgumentException
     *             <code>userId</code> is null or empty.
     */
    public static int insertTrip(ContentResolver resolver, Itinerary trip, String userId) {

        if (TextUtils.isEmpty(userId)) {
            throw new IllegalArgumentException(CLS_TAG + ".insertTrip: userId is null or empty!");
        }

        int retVal = -1;

        if (trip != null) {
            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Client locator.
            ContentUtils.putValue(values, Travel.TripColumns.CLIENT_LOCATOR, trip.clientLocator);
            // Cliqbook Trip id.
            ContentUtils.putValue(values, Travel.TripColumns.CLIQBOOK_TRIP_ID, trip.cliqBookTripId);
            // Description.
            ContentUtils.putValue(values, Travel.TripColumns.DESCRIPTION, trip.description);
            // End date local.
            ContentUtils.putValue(values, Travel.TripColumns.END_DATE_LOCAL, Parse.XML_DF_LOCAL, trip.endDateLocal);
            // End date UTC.
            ContentUtils.putValue(values, Travel.TripColumns.END_DATE_UTC, Parse.XML_DF, trip.endDateUtc);
            // Itin locator.
            ContentUtils.putValue(values, Travel.TripColumns.ITIN_LOCATOR, trip.itinLocator);
            // Record locator.
            ContentUtils.putValue(values, Travel.TripColumns.RECORD_LOCATOR, trip.recordLocator);
            // Start date local.
            ContentUtils.putValue(values, Travel.TripColumns.START_DATE_LOCAL, Parse.XML_DF_LOCAL, trip.startDateLocal);
            // Start date UTC.
            ContentUtils.putValue(values, Travel.TripColumns.START_DATE_UTC, Parse.XML_DF, trip.startDateUtc);
            // Trip state.
            ContentUtils.putValue(values, Travel.TripColumns.STATE, trip.state);
            // Trip name.
            ContentUtils.putValue(values, Travel.TripColumns.TRIP_NAME, trip.tripName);
            // Allow add air.
            ContentUtils.putValue(values, Travel.TripColumns.ALLOW_ADD_AIR,
                    ((trip.actions != null) ? trip.actions.allowAddAir : null));
            // Allow add hotel.
            ContentUtils.putValue(values, Travel.TripColumns.ALLOW_ADD_HOTEL,
                    ((trip.actions != null) ? trip.actions.allowAddHotel : null));
            // Allow add car.
            ContentUtils.putValue(values, Travel.TripColumns.ALLOW_ADD_CAR,
                    ((trip.actions != null) ? trip.actions.allowAddCar : null));
            // Allow add rail.
            ContentUtils.putValue(values, Travel.TripColumns.ALLOW_ADD_RAIL,
                    ((trip.actions != null) ? trip.actions.allowAddRail : null));
            // Allow cancel.
            ContentUtils.putValue(values, Travel.TripColumns.ALLOW_CANCEL,
                    ((trip.actions != null) ? trip.actions.allowCancel : null));
            // User Id.
            ContentUtils.putValue(values, Travel.TripColumns.USER_ID, userId);

            Uri tripUri = resolver.insert(Travel.TripColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertTrip: new trip uri '"
                        + ((tripUri != null) ? tripUri.toString() : "null"));
            }
            if (tripUri != null) {

                try {
                    int tripId = Integer.parseInt(tripUri.getPathSegments().get(
                            Travel.TripColumns.TRIP_ID_PATH_POSITION));

                    retVal = tripId;

                    // Insert any trip enhancement information.
                    if (trip.enhancements != null) {
                        // Insert enhancement days.
                        if (trip.enhancements.days != null) {
                            for (Day day : trip.enhancements.days) {
                                insertEnhancementDay(resolver, tripId, day);
                            }
                        }
                        // Insert enhancement offers.
                        if (trip.enhancements.offers != null) {
                            for (Offer offer : trip.enhancements.offers) {
                                insertEnhancementOffer(resolver, tripId, offer);
                            }
                        }
                    }

                    // Insert any rule violation information.
                    if (trip.ruleViolations != null) {
                        // Insert any trip rule violations.
                        if (trip.ruleViolations.itinRuleViolations != null) {
                            for (RuleViolation viol : trip.ruleViolations.itinRuleViolations) {
                                insertTripRuleViolation(resolver, tripId, viol);
                            }
                        }
                        // Insert any car rule violations.
                        if (trip.ruleViolations.carRuleViolations != null) {
                            for (CarRuleViolation viol : trip.ruleViolations.carRuleViolations) {
                                insertCarRuleViolation(resolver, tripId, viol);
                            }
                        }
                        // Insert any hotel rule violations.
                        if (trip.ruleViolations.hotelRuleViolations != null) {
                            for (HotelRuleViolation viol : trip.ruleViolations.hotelRuleViolations) {
                                insertHotelRuleViolation(resolver, tripId, viol);
                            }
                        }
                        // Insert any flight rule violations.
                        if (trip.ruleViolations.flightRuleViolations != null) {
                            for (FlightRuleViolation viol : trip.ruleViolations.flightRuleViolations) {
                                insertFlightRuleViolation(resolver, tripId, viol);
                            }
                        }
                        // Insert any rail rule violations.
                        if (trip.ruleViolations.railRuleViolations != null) {
                            for (RailRuleViolation viol : trip.ruleViolations.railRuleViolations) {
                                insertRailRuleViolation(resolver, tripId, viol);
                            }
                        }
                    }

                    // Insert any booking information.
                    if (trip.bookings != null) {
                        for (Booking booking : trip.bookings) {
                            insertBooking(resolver, tripId, booking);
                        }
                    }

                    // Insert any travel points information.
                    if (trip.travelPoint != null) {
                        insertTravelPoint(resolver, tripId, null, trip.travelPoint);
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTrip: trip content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTrip: trip content id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTrip: unable to insert trip!");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert trip enhancement day information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param day
     *            contains the <code>Day</code> information.
     * @returns returns the content provider id of the inserted enhancement day information.
     */
    public static int insertEnhancementDay(ContentResolver resolver, int tripId, Day day) {
        int retVal = -1;

        if (day != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip Id.
            ContentUtils.putValue(values, Travel.EnhancementDayColumns.TRIP_ID, tripId);
            // Day type.
            ContentUtils.putValue(values, Travel.EnhancementDayColumns.TYPE, day.dayType);
            // Trip local date.
            ContentUtils.putValue(values, Travel.EnhancementDayColumns.TRIP_LOCAL_DATE, Parse.XML_DF_LOCAL,
                    day.tripLocalDate);

            Uri insUri = resolver.insert(Travel.EnhancementDayColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertEnhancementDay: new enhancement day uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.EnhancementDayColumns.ENHANCEMENT_DAY_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any sortable segment information.
                    if (day.sortableSegments != null) {
                        for (SortableSegment sortSeg : day.sortableSegments) {
                            insertSortableSegment(resolver, insId, sortSeg);
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementDay: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementDay: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementDay: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert sortable segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param dayId
     *            contains the <code>Travel.EnhancementDayColumns._ID</code> content provider enhancement day id.
     * @param sortSeg
     *            contains the <code>SortableSegment</code> information.
     * @returns returns the content provider id of the inserted sortable segment information.
     */
    public static int insertSortableSegment(ContentResolver resolver, int dayId, SortableSegment sortSeg) {
        int retVal = -1;

        if (sortSeg != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Day id.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.ENHANCEMENT_DAY_ID, dayId);
            // Booking source.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.BOOKING_SOURCE, sortSeg.bookingSource);
            // Record locator.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.RECORD_LOCATOR, sortSeg.recordLocator);
            // Segment key.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.SEGMENT_KEY, sortSeg.segmentKey);
            // Segment side.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.SEGMENT_SIDE, sortSeg.segmentSide);
            // Sort value.
            ContentUtils.putValue(values, Travel.SortableSegmentColumns.SORT_VALUE, sortSeg.sortValue);

            Uri insUri = resolver.insert(Travel.SortableSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertSortableSegment: new sortable segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.SortableSegmentColumns.SORTABLE_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSortableSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSortableSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSortableSegment: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert trip enhancement offer information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param offer
     *            contains the <code>Offer</code> information.
     * @returns returns the content provider id of the inserted enhancement offer information.
     */
    public static int insertEnhancementOffer(ContentResolver resolver, int tripId, Offer offer) {
        int retVal = -1;

        if (offer != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.EnhancementOfferColumns.TRIP_ID, tripId);
            // Id.
            ContentUtils.putValue(values, Travel.EnhancementOfferColumns.ID, offer.id);
            // Description.
            ContentUtils.putValue(values, Travel.EnhancementOfferColumns.DESCRIPTION, offer.offerDescription);
            // Type.
            ContentUtils.putValue(values, Travel.EnhancementOfferColumns.TYPE, offer.offerType);

            Uri insUri = resolver.insert(Travel.EnhancementOfferColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertEnhancementOffer: new enhancement offer uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.EnhancementOfferColumns.ENHANCEMENT_OFFER_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any offer link information.
                    if (offer.link != null) {
                        insertOfferLink(resolver, insId, offer.link);
                    }

                    // Insert any offer content information.
                    if (offer.offerContent != null) {
                        insertOfferContent(resolver, insId, offer.offerContent);
                    }

                    // Insert any offer validity information.
                    if (offer.validity != null) {
                        // Insert any offer validity location information.
                        if (offer.validity.locations != null) {
                            for (Location loc : offer.validity.locations) {
                                insertValidityLocation(resolver, insId, loc);
                            }
                        }
                        // Insert any offer validity time range information.
                        if (offer.validity.timeRanges != null) {
                            for (TimeRange timeRange : offer.validity.timeRanges) {
                                insertValidityTimeRange(resolver, insId, timeRange);
                            }
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementOffer: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementOffer: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEnhancementOffer: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert offer link information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerId
     *            contains the <code>Travel.EnhancementOffer._ID</code> content provider offer id.
     * @param offerLink
     *            an <code>OfferLink</code> object.
     * @returns returns the content provider id of the inserted offer link information.
     */
    public static int insertOfferLink(ContentResolver resolver, int offerId, OfferLink offerLink) {
        int retVal = -1;

        if (offerLink != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Offer id.
            ContentUtils.putValue(values, Travel.OfferLinkColumns.ENHANCEMENT_OFFER_ID, offerId);
            if (offerLink.segment != null) {
                ContentUtils.putValue(values, Travel.OfferLinkColumns.BOOKING_SOURCE, offerLink.segment.bookingSource);
                ContentUtils.putValue(values, Travel.OfferLinkColumns.RECORD_LOCATOR, offerLink.segment.recordLocator);
                ContentUtils.putValue(values, Travel.OfferLinkColumns.SEGMENT_KEY, offerLink.segment.segmentKey);
                ContentUtils.putValue(values, Travel.OfferLinkColumns.SEGMENT_SIDE, offerLink.segment.segmentSide);
            }

            Uri insUri = resolver.insert(Travel.OfferLinkColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertOfferLink: new offer link uri '"
                                + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.OfferLinkColumns.OFFER_LINK_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferLink: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferLink: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferLink: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert offer content information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerId
     *            contains the <code>Travel.EnhancementOffer._ID</code> content provider offer id.
     * @param offerContent
     *            an <code>OfferContent</code> object.
     * @returns returns the content provider id of the inserted offer content information.
     */
    public static int insertOfferContent(ContentResolver resolver, int offerId, OfferContent offerContent) {
        int retVal = -1;

        if (offerContent != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Offer id.
            ContentUtils.putValue(values, Travel.OfferContentColumns.ENHANCEMENT_OFFER_ID, offerId);
            // Title.
            ContentUtils.putValue(values, Travel.OfferContentColumns.TITLE, offerContent.title);
            // Vendor.
            ContentUtils.putValue(values, Travel.OfferContentColumns.VENDOR, offerContent.vendor);
            // Action.
            ContentUtils.putValue(values, Travel.OfferContentColumns.ACTION, offerContent.action);
            // Application.
            ContentUtils.putValue(values, Travel.OfferContentColumns.APPLICATION, offerContent.application);
            // Image name.
            ContentUtils.putValue(values, Travel.OfferContentColumns.IMAGE_NAME, offerContent.imageName);

            Uri insUri = resolver.insert(Travel.OfferContentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertOfferContent: new offer content uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.OfferContentColumns.OFFER_CONTENT_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any content links.
                    if (offerContent.contentLinks != null) {
                        for (ContentLink contentLink : offerContent.contentLinks) {
                            insertContentLink(resolver, insId, contentLink);
                        }
                    }

                    // Insert any map display.
                    if (offerContent.mapDisplay != null) {
                        insertMapDisplay(resolver, insId, offerContent.mapDisplay);
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferContent: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferContent: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertOfferContent: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert content link information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerContentId
     *            contains the <code>Travel.OfferContent._ID</code> content provider offer content id.
     * @param contentLink
     *            a <code>ContentLink</code> object.
     * @returns returns the content provider id of the inserted content link information.
     */
    public static int insertContentLink(ContentResolver resolver, int offerContentId, ContentLink contentLink) {
        int retVal = -1;

        if (contentLink != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Content id.
            ContentUtils.putValue(values, Travel.ContentLinkColumns.OFFER_CONTENT_ID, offerContentId);
            // Title
            ContentUtils.putValue(values, Travel.ContentLinkColumns.TITLE, contentLink.title);
            // Action URL
            ContentUtils.putValue(values, Travel.ContentLinkColumns.ACTION_URL, contentLink.actionUrl);

            Uri insUri = resolver.insert(Travel.ContentLinkColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertContentLink: new content link uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.ContentLinkColumns.CONTENT_LINK_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertContentLink: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertContentLink: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertContentLink: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert map display information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerContentId
     *            contains the <code>Travel.OfferContent._ID</code> content provider offer content id.
     * @param mapDisplay
     *            a <code>MapDisplay</code> object.
     * @returns returns the content provider id of the inserted map display information.
     */
    public static int insertMapDisplay(ContentResolver resolver, int offerContentId, MapDisplay mapDisplay) {
        int retVal = -1;

        if (mapDisplay != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Content id.
            ContentUtils.putValue(values, Travel.MapDisplayColumns.OFFER_CONTENT_ID, offerContentId);
            // Latitude.
            ContentUtils.putValue(values, Travel.MapDisplayColumns.LATITUDE, mapDisplay.latitude);
            // Longitude.
            ContentUtils.putValue(values, Travel.MapDisplayColumns.LONGITUDE, mapDisplay.longitude);
            // Dimension Km.
            ContentUtils.putValue(values, Travel.MapDisplayColumns.DIMENSION_KM, mapDisplay.dimensionKm);

            Uri insUri = resolver.insert(Travel.MapDisplayColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertMapDisplay: new content link uri '"
                                + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.MapDisplayColumns.MAP_DISPLAY_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any overlays.
                    if (mapDisplay.overlays != null) {
                        for (Overlay overlay : mapDisplay.overlays) {
                            insertDisplayOverlay(resolver, insId, overlay);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertMapDisplay: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertMapDisplay: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertMapDisplay: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert display overlay information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param mapDisplayId
     *            contains the <code>Travel.MapDisplay._ID</code> content provider map display id.
     * @param displayOverlay
     *            an <code>Overlay</code> object.
     * @returns returns the content provider id of the inserted display overlay information.
     */
    public static int insertDisplayOverlay(ContentResolver resolver, int mapDisplayId, Overlay displayOverlay) {
        int retVal = -1;

        if (displayOverlay != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Map display id.
            ContentUtils.putValue(values, Travel.DisplayOverlayColumns.MAP_DISPLAY_ID, mapDisplayId);
            // Name.
            ContentUtils.putValue(values, Travel.DisplayOverlayColumns.NAME, displayOverlay.name);

            Uri insUri = resolver.insert(Travel.DisplayOverlayColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertDisplayOverlay: new display overlay uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.DisplayOverlayColumns.DISPLAY_OVERLAY_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDisplayOverlay: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDisplayOverlay: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDisplayOverlay: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert validity location information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerId
     *            contains the <code>Travel.EnhancementOffer._ID</code> content provider offer id.
     * @param location
     *            a <code>Location</code> object.
     * @returns returns the content provider id of the inserted validity location information.
     */
    public static int insertValidityLocation(ContentResolver resolver, int offerId, Location location) {
        int retVal = -1;

        if (location != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Offer id.
            ContentUtils.putValue(values, Travel.ValidityLocationColumns.ENHANCEMENT_OFFER_ID, offerId);
            // Latitude.
            ContentUtils.putValue(values, Travel.ValidityLocationColumns.LATITUDE, location.latitude);
            // Longitude.
            ContentUtils.putValue(values, Travel.ValidityLocationColumns.LONGITUDE, location.longitude);
            // Proximity.
            ContentUtils.putValue(values, Travel.ValidityLocationColumns.PROXIMITY, location.proximity);

            Uri insUri = resolver.insert(Travel.ValidityLocationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertValidityLocation: new validity location uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.ValidityLocationColumns.VALIDITY_LOCATION_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityLocation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityLocation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityLocation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert validity time range information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param offerId
     *            contains the <code>Travel.EnhancementOffer._ID</code> content provider offer id.
     * @param location
     *            a <code>TimeRange</code> object.
     * @returns returns the content provider id of the inserted validity time range information.
     */
    public static int insertValidityTimeRange(ContentResolver resolver, int offerId, TimeRange timeRange) {
        int retVal = -1;

        if (timeRange != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Offer id.
            ContentUtils.putValue(values, Travel.ValidityTimeRangeColumns.ENHANCEMENT_OFFER_ID, offerId);
            ContentUtils.putValue(values, Travel.ValidityTimeRangeColumns.START_DATE_TIME_UTC, Parse.XML_DF,
                    timeRange.startDateUtc);
            ContentUtils.putValue(values, Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC, Parse.XML_DF,
                    timeRange.endDateUtc);

            Uri insUri = resolver.insert(Travel.ValidityTimeRangeColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertValidityTimeRange: new validity time range uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.ValidityTimeRangeColumns.VALIDITY_TIME_RANGE_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityTimeRange: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityTimeRange: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertValidityTimeRange: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert trip rule violation information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param violation
     *            contains a <code>RuleViolation</code> object.
     * @returns the content provider id of the inserted trip rule violation information.
     * 
     */
    public static int insertTripRuleViolation(ContentResolver resolver, int tripId, RuleViolation violation) {
        int retVal = -1;

        if (violation != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.TripRuleViolationColumns.TRIP_ID, tripId);
            // NOTE: No further attributes.

            Uri insUri = resolver.insert(Travel.TripRuleViolationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertTripRuleViolation: new trip rule violation uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.TripRuleViolationColumns.TRIP_RULE_VIOLATION_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any rules.
                    if (violation.rules != null) {
                        for (Rule rule : violation.rules) {
                            insertRuleViolation(resolver, insId, null, null, null, null, rule);
                        }
                    }

                    // Insert any rule violation reasons.
                    if (violation.violationReasons != null) {
                        for (RuleViolationReason ruleViolReason : violation.violationReasons) {
                            insertRuleViolationReason(resolver, insId, null, null, null, null, ruleViolReason);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTripRuleViolation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTripRuleViolation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTripRuleViolation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert car rule violation information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param violation
     *            contains a <code>CarRuleViolation</code> object.
     * @returns the content provider id of the inserted car rule violation information.
     * 
     */
    public static int insertCarRuleViolation(ContentResolver resolver, int tripId, CarRuleViolation violation) {
        int retVal = -1;

        if (violation != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.CarRuleViolationColumns.TRIP_ID, tripId);
            // Daily rate.
            ContentUtils.putValue(values, Travel.CarRuleViolationColumns.DAILY_RATE, violation.dailyRate);

            Uri insUri = resolver.insert(Travel.CarRuleViolationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertCarRuleViolation: new car rule violation uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.CarRuleViolationColumns.CAR_RULE_VIOLATION_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any rules.
                    if (violation.rules != null) {
                        for (Rule rule : violation.rules) {
                            insertRuleViolation(resolver, null, insId, null, null, null, rule);
                        }
                    }

                    // Insert any rule violation reasons.
                    if (violation.violationReasons != null) {
                        for (RuleViolationReason ruleViolReason : violation.violationReasons) {
                            insertRuleViolationReason(resolver, null, insId, null, null, null, ruleViolReason);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarRuleViolation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarRuleViolation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarRuleViolation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert hotel rule violation information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param violation
     *            contains a <code>HotelRuleViolation</code> object.
     * @returns the content provider id of the inserted hotel rule violation information.
     * 
     */
    public static int insertHotelRuleViolation(ContentResolver resolver, int tripId, HotelRuleViolation violation) {
        int retVal = -1;

        if (violation != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.HotelRuleViolationColumns.TRIP_ID, tripId);
            // Rate.
            ContentUtils.putValue(values, Travel.HotelRuleViolationColumns.RATE, violation.rate);
            // Name.
            ContentUtils.putValue(values, Travel.HotelRuleViolationColumns.NAME, violation.name);
            // Address.
            ContentUtils.putValue(values, Travel.HotelRuleViolationColumns.ADDRESS, violation.address);
            // Description.
            ContentUtils.putValue(values, Travel.HotelRuleViolationColumns.DESCRIPTION, violation.description);

            Uri insUri = resolver.insert(Travel.HotelRuleViolationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelRuleViolation: new hotel rule violation uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.HotelRuleViolationColumns.HOTEL_RULE_VIOLATION_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any rules.
                    if (violation.rules != null) {
                        for (Rule rule : violation.rules) {
                            insertRuleViolation(resolver, null, null, insId, null, null, rule);
                        }
                    }

                    // Insert any rule violation reasons.
                    if (violation.violationReasons != null) {
                        for (RuleViolationReason ruleViolReason : violation.violationReasons) {
                            insertRuleViolationReason(resolver, null, null, insId, null, null, ruleViolReason);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelRuleViolation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelRuleViolation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelRuleViolation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert flight rule violation information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param violation
     *            contains a <code>FlightRuleViolation</code> object.
     * @returns the content provider id of the inserted flight rule violation information.
     */
    public static int insertFlightRuleViolation(ContentResolver resolver, int tripId, FlightRuleViolation violation) {
        int retVal = -1;

        if (violation != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.FlightRuleViolationColumns.TRIP_ID, tripId);
            // Refundable.
            ContentUtils.putValue(values, Travel.FlightRuleViolationColumns.REFUNDABLE, violation.refundable);
            // Cost.
            ContentUtils.putValue(values, Travel.FlightRuleViolationColumns.COST, violation.cost);

            Uri insUri = resolver.insert(Travel.FlightRuleViolationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertFlightRuleViolation: new flight rule violation uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.FlightRuleViolationColumns.FLIGHT_RULE_VIOLATION_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any rules.
                    if (violation.rules != null) {
                        for (Rule rule : violation.rules) {
                            insertRuleViolation(resolver, null, null, null, insId, null, rule);
                        }
                    }

                    // Insert any rule violation reasons.
                    if (violation.violationReasons != null) {
                        for (RuleViolationReason ruleViolReason : violation.violationReasons) {
                            insertRuleViolationReason(resolver, null, null, null, insId, null, ruleViolReason);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightRuleViolation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightRuleViolation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightRuleViolation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert rail rule violation information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param violation
     *            contains a <code>RailRuleViolation</code> object.
     * @returns the content provider id of the inserted rail rule violation information.
     */
    public static int insertRailRuleViolation(ContentResolver resolver, int tripId, RailRuleViolation violation) {
        int retVal = -1;

        if (violation != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.RailRuleViolationColumns.TRIP_ID, tripId);
            // Rate.
            ContentUtils.putValue(values, Travel.RailRuleViolationColumns.RATE, violation.rate);

            Uri insUri = resolver.insert(Travel.RailRuleViolationColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertRailRuleViolation: new rail rule violation uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.RailRuleViolationColumns.RAIL_RULE_VIOLATION_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any rules.
                    if (violation.rules != null) {
                        for (Rule rule : violation.rules) {
                            insertRuleViolation(resolver, null, null, null, null, insId, rule);
                        }
                    }

                    // Insert any rule violation reasons.
                    if (violation.violationReasons != null) {
                        for (RuleViolationReason ruleViolReason : violation.violationReasons) {
                            insertRuleViolationReason(resolver, null, null, null, null, insId, ruleViolReason);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailRuleViolation: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailRuleViolation: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailRuleViolation: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert rule information for either a trip, car, hotel, flight or rail rule violation.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripRuleViolId
     *            contains the <code>Travel.TripRuleViolation._ID</code> content provider trip rule violation id.
     * @param carRuleViolId
     *            contains the <code>Travel.CarRuleViolation._ID</code> content provider car rule violation id.
     * @param hotelRuleViolId
     *            contains the <code>Travel.HotelRuleViolation._ID</code> content provider hotel rule violation id.
     * @param flightRuleViolId
     *            contains the <code>Travel.FlightRuleViolation._ID</code> content provider flight rule violation id.
     * @param railRuleViolId
     *            contains the <code>Travel.RailRuleViolation._ID</code> content provider rail rule violation id.
     * @param rule
     *            contains a <code>Rule</code> object.
     * @returns the content provider id of the inserted rule information.
     */
    public static int insertRuleViolation(ContentResolver resolver, Integer tripRuleViolId, Integer carRuleViolId,
            Integer hotelRuleViolId, Integer flightRuleViolId, Integer railRuleViolId, Rule rule) {
        int retVal = -1;

        if (rule != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Text
            ContentUtils.putValue(values, Travel.RuleColumns.TEXT, rule.text);
            // Trip rule violation id.
            ContentUtils.putValue(values, Travel.RuleColumns.TRIP_RULE_VIOLATION_ID, tripRuleViolId);
            // Car rule violation id.
            ContentUtils.putValue(values, Travel.RuleColumns.CAR_RULE_VIOLATION_ID, carRuleViolId);
            // Hotel rule violation id.
            ContentUtils.putValue(values, Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID, hotelRuleViolId);
            // Flight rule violation id.
            ContentUtils.putValue(values, Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID, flightRuleViolId);
            // Rail rule violation id.
            ContentUtils.putValue(values, Travel.RuleColumns.RAIL_RULE_VIOLATION_ID, railRuleViolId);

            Uri insUri = resolver.insert(Travel.RuleColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertRule: new rule uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer
                            .parseInt(insUri.getPathSegments().get(Travel.RuleColumns.RULE_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRule: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRule: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRule: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert rule violation reason information for either a trip, car, hotel, flight or rail rule violation.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripRuleViolId
     *            contains the <code>Travel.TripRuleViolation._ID</code> content provider trip rule violation id.
     * @param carRuleViolId
     *            contains the <code>Travel.CarRuleViolation._ID</code> content provider car rule violation id.
     * @param hotelRuleViolId
     *            contains the <code>Travel.HotelRuleViolation._ID</code> content provider hotel rule violation id.
     * @param flightRuleViolId
     *            contains the <code>Travel.FlightRuleViolation._ID</code> content provider flight rule violation id.
     * @param railRuleViolId
     *            contains the <code>Travel.RailRuleViolation._ID</code> content provider rail rule violation id.
     * @param rule
     *            contains a <code>RuleViolationReason</code> object.
     * @returns the content provider id of the inserted rule violation information.
     */
    public static int insertRuleViolationReason(ContentResolver resolver, Integer tripRuleViolId,
            Integer carRuleViolId, Integer hotelRuleViolId, Integer flightRuleViolId, Integer railRuleViolId,
            RuleViolationReason rule) {
        int retVal = -1;

        if (rule != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Reason
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.REASON, rule.reasonCode);
            // Comments
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.COMMENTS, rule.bookerComments);
            // Trip rule violation id.
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.TRIP_RULE_VIOLATION_ID, tripRuleViolId);
            // Car rule violation id.
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID, carRuleViolId);
            // Hotel rule violation id.
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID, hotelRuleViolId);
            // Flight rule violation id.
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID, flightRuleViolId);
            // Rail rule violation id.
            ContentUtils.putValue(values, Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID, railRuleViolId);

            Uri insUri = resolver.insert(Travel.RuleViolationReasonColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertRuleViolationReason: new rule violation reason uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.RuleViolationReasonColumns.RULE_VIOLATION_REASON_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRuleViolationReason: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRuleViolationReason: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRuleViolationReason: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert travel point information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param travelPoint
     *            contains a <code>TravelPoint</code> object.
     * @returns the content provider id of the inserted travel point information.
     */
    public static int insertTravelPoint(ContentResolver resolver, Integer tripId, Integer segmentId,
            TravelPoint travelPoint) {
        int retVal = -1;

        if (travelPoint != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.TravelPointColumns.TRIP_ID, tripId);
            // Segment id.
            ContentUtils.putValue(values, Travel.TravelPointColumns.SEGMENT_ID, segmentId);
            // Benchmark
            ContentUtils.putValue(values, Travel.TravelPointColumns.BENCHMARK, travelPoint.benchmark);
            // Benchmark currency.
            ContentUtils.putValue(values, Travel.TravelPointColumns.BENCHMARK_CURRENCY, travelPoint.benchmarkCurrency);
            // Points posted.
            ContentUtils.putValue(values, Travel.TravelPointColumns.POINTS_POSTED, travelPoint.pointsPosted);
            // Points pending.
            ContentUtils.putValue(values, Travel.TravelPointColumns.POINTS_PENDING, travelPoint.pointsPending);
            // Total points.
            ContentUtils.putValue(values, Travel.TravelPointColumns.TOTAL_POINTS, travelPoint.totalPoints);

            Uri insUri = resolver.insert(Travel.TravelPointColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertTravelPoint: new travel point uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.TravelPointColumns.TRAVEL_POINT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTravelPoint: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTravelPoint: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertTravelPoint: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert booking information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param tripId
     *            contains the <code>Travel.TripColumns._ID</code> content provider trip id.
     * @param booking
     *            contains a <code>Booking</code> object.
     * @return the content provider id of the inserted booking information.
     */
    public static int insertBooking(ContentResolver resolver, int tripId, Booking booking) {
        int retVal = -1;

        if (booking != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Trip id.
            ContentUtils.putValue(values, Travel.BookingColumns.TRIP_ID, tripId);
            // Agency PCC.
            ContentUtils.putValue(values, Travel.BookingColumns.AGENCY_PCC, booking.agencyPCC);
            // Booking source.
            ContentUtils.putValue(values, Travel.BookingColumns.BOOKING_SOURCE, booking.bookingSource);
            // Company accounting code.
            ContentUtils.putValue(values, Travel.BookingColumns.COMPANY_ACCOUNTING_CODE, booking.companyAccountingCode);
            // Date booked local.
            ContentUtils.putValue(values, Travel.BookingColumns.DATE_BOOKED_LOCAL, Parse.XML_DF_LOCAL,
                    booking.dateBookedLocal);
            // Is Cliqbook system of record.
            ContentUtils.putValue(values, Travel.BookingColumns.IS_CLIQBOOK_SYSTEM_OF_RECORD,
                    booking.isCliqbookSystemOfRecord);
            // Record locator.
            ContentUtils.putValue(values, Travel.BookingColumns.RECORD_LOCATOR, booking.recordLocator);
            // Travel config ID.
            ContentUtils.putValue(values, Travel.BookingColumns.TRAVEL_CONFIG_ID, booking.travelConfigID);
            // Type.
            ContentUtils.putValue(values, Travel.BookingColumns.TYPE, booking.type);
            // Is GDS booking.
            ContentUtils.putValue(values, Travel.BookingColumns.IS_GDS_BOOKING, booking.isGdsBooking);

            Uri insUri = resolver.insert(Travel.BookingColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertBooking: new booking uri '" + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.BookingColumns.BOOKING_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any airline tickets.
                    if (booking.airlineTickets != null) {
                        for (AirlineTicket ticket : booking.airlineTickets) {
                            insertAirlineTicket(resolver, insId, ticket);
                        }
                    }

                    // Insert any passenger data.
                    if (booking.passengers != null) {
                        for (Passenger passenger : booking.passengers) {
                            insertPassenger(resolver, insId, passenger);
                        }
                    }

                    // Insert any segment data.
                    if (booking.segments != null) {
                        for (Segment segment : booking.segments) {

                            // First, insert any generic segment data.
                            int segId = insertSegment(resolver, insId, segment.segmentData);
                            if (segId != -1) {
                                // Second, insert data specific to the segment type.
                                try {
                                    switch (segment.segmentData.getSegmentType()) {
                                    case AIR: {
                                        insertAirSegment(resolver, insId, segId, (AirSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case CAR: {
                                        insertCarSegment(resolver, insId, segId, (CarSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case DINING: {
                                        insertDiningSegment(resolver, insId, segId,
                                                (DiningSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case EVENT: {
                                        insertEventSegment(resolver, insId, segId,
                                                (EventSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case HOTEL: {
                                        insertHotelSegment(resolver, insId, segId,
                                                (HotelSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case PARKING: {
                                        insertParkingSegment(resolver, insId, segId,
                                                (ParkingSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case RAIL: {
                                        insertRailSegment(resolver, insId, segId, (RailSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case RIDE: {
                                        insertRailSegment(resolver, insId, segId, (RailSegmentData) segment.segmentData);
                                        break;
                                    }
                                    case UNDEFINED: {
                                        // No-op.
                                        break;
                                    }
                                    }
                                } catch (ClassCastException ccExc) {
                                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertBooking: unable to cast segment data.",
                                            ccExc);
                                }
                            } else {
                                Log.e(Const.LOG_TAG, CLS_TAG + ".insertBooking: unable to insert generic segment data.");
                            }
                        }
                    }
                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertBooking: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertBooking: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertBooking: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert airline ticket information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param ticket
     *            contains an <code>AirlineTicket</code> object.
     * @return the content provider id of the inserted airline ticket information.
     */
    public static int insertAirlineTicket(ContentResolver resolver, int bookingId, AirlineTicket ticket) {
        int retVal = -1;

        if (ticket != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Booking id.
            ContentUtils.putValue(values, Travel.AirlineTicketColumns.BOOKING_ID, bookingId);
            // TODO: insert any other values once the columns are determined.

            Uri insUri = resolver.insert(Travel.AirlineTicketColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertAirlineTicket: new airline ticket uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.AirlineTicketColumns.AIRLINE_TICKET_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirlineTicket: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirlineTicket: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirlineTicket: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert passenger information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param passenger
     *            contains a <code>Passenger</code> object.
     * @return the content provider id of the inserted passenger information.
     */
    public static int insertPassenger(ContentResolver resolver, int bookingId, Passenger passenger) {
        int retVal = -1;

        if (passenger != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Booking id.
            ContentUtils.putValue(values, Travel.PassengerColumns.BOOKING_ID, bookingId);
            // First name.
            ContentUtils.putValue(values, Travel.PassengerColumns.FIRST_NAME, passenger.firstName);
            // Last name.
            ContentUtils.putValue(values, Travel.PassengerColumns.LAST_NAME, passenger.lastName);
            // Name identifier
            ContentUtils.putValue(values, Travel.PassengerColumns.IDENTIFIER, passenger.nameIdentifier);
            // Passenger key.
            ContentUtils.putValue(values, Travel.PassengerColumns.PASSENGER_KEY, passenger.passengerKey);

            Uri insUri = resolver.insert(Travel.PassengerColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertPassenger: new passenger uri '"
                                + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.PassengerColumns.PASSENGER_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any frequent traveler program information.
                    if (passenger.frequentTravelerPrograms != null) {
                        for (FrequentTravelerProgram prog : passenger.frequentTravelerPrograms) {
                            insertFrequentTravelerProgram(resolver, insId, prog);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertPassenger: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertPassenger: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertPassenger: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert freqent traveler program information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param passengerId
     *            contains the <code>Travel.PassengerColumns._ID</code> content provider passenger id.
     * @param freqTravProg
     *            contains a <code>FrequentTravelerProgram</code> object.
     * @return the content provider id of the inserted frequent traveler program information.
     */
    public static int insertFrequentTravelerProgram(ContentResolver resolver, int passengerId,
            FrequentTravelerProgram freqTravProg) {
        int retVal = -1;

        if (freqTravProg != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Passenger id.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.PASSENGER_ID, passengerId);
            // Airline vendor.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.AIRLINE_VENDOR,
                    freqTravProg.airlineVendor);
            // Program number.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.PROGRAM_NUMBER,
                    freqTravProg.programNumber);
            // Program vendor.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR,
                    freqTravProg.programVendor);
            // Program vendor code.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR_CODE,
                    freqTravProg.programVendorCode);
            // Status.
            ContentUtils.putValue(values, Travel.FrequentTravelerProgramColumns.STATUS, freqTravProg.status);

            Uri insUri = resolver.insert(Travel.FrequentTravelerProgramColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertFrequentTravelerProgram: new frequent traveler program uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.FrequentTravelerProgramColumns.FREQUENT_TRAVELER_PROGRAM_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFrequentTravelerProgram: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFrequentTravelerProgram: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFrequentTravelerProgram: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segData
     *            contains a <code>SegmentData</code> object.
     * @return the content provider id of the inserted segment information.
     */
    public static int insertSegment(ContentResolver resolver, int bookingId, SegmentData segData) {
        int retVal = -1;

        if (segData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            // Booking id.
            ContentUtils.putValue(values, Travel.SegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_DATE_UTC, Parse.XML_DF, segData.startDateUtc);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_DATE_UTC, Parse.XML_DF, segData.endDateUtc);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_DATE_LOCAL, Parse.XML_DF_LOCAL,
                    segData.startDateLocal);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_DATE_LOCAL, Parse.XML_DF_LOCAL,
                    segData.endDateLocal);
            ContentUtils.putValue(values, Travel.SegmentColumns.CONFIRMATION_NUMBER, segData.confirmNumber);
            ContentUtils.putValue(values, Travel.SegmentColumns.CREDIT_CARD_ID, segData.creditCardId);
            ContentUtils.putValue(values, Travel.SegmentColumns.CREDIT_CARD_LAST_FOUR, segData.creditCardLastFour);
            ContentUtils.putValue(values, Travel.SegmentColumns.CREDIT_CARD_TYPE, segData.creditCardType);
            ContentUtils.putValue(values, Travel.SegmentColumns.CREDIT_CARD_TYPE_LOCALIZED,
                    segData.creditCardTypeLocalized);
            ContentUtils.putValue(values, Travel.SegmentColumns.CURRENCY, segData.currency);
            ContentUtils.putValue(values, Travel.SegmentColumns.ERECEIPT_STATUS, segData.eReceiptStatus);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_ADDRESS, segData.endAddress);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_ADDRESS_2, segData.endAddress2);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_CITY, segData.endCity);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_CITY_CODE, segData.endCityCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_CITY_CODE_LOCALIZED, segData.endCityCodeLocalized);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_COUNTRY, segData.endCountry);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_COUNTRY_CODE, segData.endCountryCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_LATITUDE, segData.endLat);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_LONGITUDE, segData.endLong);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_POSTAL_CODE, segData.endPostCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.END_STATE, segData.endState);
            ContentUtils.putValue(values, Travel.SegmentColumns.FREQUENT_TRAVELER_ID, segData.frequentTravelerId);
            ContentUtils.putValue(values, Travel.SegmentColumns.IMAGE_VENDOR_URI, segData.imageVendorUri);
            ContentUtils.putValue(values, Travel.SegmentColumns.NUM_PERSONS, segData.numPersons);
            ContentUtils.putValue(values, Travel.SegmentColumns.OPERATED_BY_VENDOR, segData.operatedByVendor);
            ContentUtils.putValue(values, Travel.SegmentColumns.OPERATED_BY_VENDOR_NAME, segData.operatedByVendorName);
            ContentUtils.putValue(values, Travel.SegmentColumns.PHONE_NUMBER, segData.phoneNumber);
            ContentUtils.putValue(values, Travel.SegmentColumns.RATE_CODE, segData.rateCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.SEGMENT_KEY, segData.segmentKey);
            ContentUtils.putValue(values, Travel.SegmentColumns.SEGMENT_LOCATOR, segData.segmentLocator);
            ContentUtils.putValue(values, Travel.SegmentColumns.SEGMENT_NAME, segData.segmentName);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_ADDRESS, segData.startAddress);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_ADDRESS_2, segData.startAddress2);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_CITY, segData.startCity);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_CITY_CODE, segData.startCityCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_COUNTRY, segData.startCountry);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_COUNTRY_CODE, segData.startCountryCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_LATITUDE, segData.startLat);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_LONGITUDE, segData.startLong);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_POSTAL_CODE, segData.startPostCode);
            ContentUtils.putValue(values, Travel.SegmentColumns.START_STATE, segData.startState);
            ContentUtils.putValue(values, Travel.SegmentColumns.STATUS, segData.status);
            ContentUtils.putValue(values, Travel.SegmentColumns.STATUS_LOCALIZED, segData.statusLocalized);
            String tzId = null;
            if (segData.timeZone != null) {
                tzId = segData.timeZone.getID();
            }
            ContentUtils.putValue(values, Travel.SegmentColumns.TIMEZONE_ID, tzId);
            ContentUtils.putValue(values, Travel.SegmentColumns.TOTAL_RATE, segData.totalRate);
            ContentUtils.putValue(values, Travel.SegmentColumns.TYPE_LOCALIZED, segData.segmentTypeName);
            ContentUtils.putValue(values, Travel.SegmentColumns.VENDOR, segData.vendor);
            ContentUtils.putValue(values, Travel.SegmentColumns.VENDOR_NAME, segData.vendorName);
            ContentUtils.putValue(values, Travel.SegmentColumns.VENDOR_URL, segData.vendorURL);
            ContentUtils.putValue(values, Travel.SegmentColumns.ETICKET, segData.eTicket);
            ContentUtils.putValue(values, Travel.SegmentColumns.TYPE, segData.type);
            ContentUtils.putValue(values, Travel.SegmentColumns.ID_KEY, segData.idKey);

            Uri insUri = resolver.insert(Travel.SegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertSegment: new segment uri '" + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.SegmentColumns.SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any travel point data.
                    if (segData.travelPoint != null) {
                        insertTravelPoint(resolver, null, insId, segData.travelPoint);
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSegment: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert air segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param airSegData
     *            contains an <code>AirSegmentData</code> object.
     * @return the content provider id of the inserted air segment information.
     */
    public static int insertAirSegment(ContentResolver resolver, int bookingId, int segmentId, AirSegmentData airSegData) {
        int retVal = -1;

        if (airSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.AirSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.AIRCRAFT_CODE, airSegData.aircraftCode);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.AIRCRAFT_NAME, airSegData.aircraftName);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.CABIN, airSegData.cabin);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.CHECKED_BAGGAGE, airSegData.checkedBaggage);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.CLASS_OF_SERVICE, airSegData.classOfService);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.CLASS_OF_SERVICE_LOCALIZED,
                    airSegData.classOfServiceLocalized);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.DURATION, airSegData.duration);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_AIRPORT_CITY, airSegData.endAirportCity);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_AIRPORT_COUNTRY, airSegData.endAirportCountry);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_AIRPORT_COUNTRY_CODE,
                    airSegData.endAirportCountryCode);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_AIRPORT_NAME, airSegData.endAirportName);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_AIRPORT_STATE, airSegData.endAirportState);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_GATE, airSegData.endGate);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.END_TERMINAL, airSegData.endTerminal);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.FARE_BASIS_CODE, airSegData.fareBasisCode);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.FLIGHT_NUMBER, airSegData.flightNumber);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.LEG_ID, airSegData.legId);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.MEALS, airSegData.meals);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.MILES, airSegData.miles);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.NUM_STOPS, airSegData.numStops);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.OPERATED_BY_FLIGHT_NUMBER,
                    airSegData.operatedByFlightNumber);
            ContentUtils
                    .putValue(values, Travel.AirSegmentColumns.SPECIAL_INSTRUCTIONS, airSegData.specialInstructions);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_AIRPORT_CITY, airSegData.startAirportCity);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_AIRPORT_COUNTRY,
                    airSegData.startAirportCountry);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_AIRPORT_COUNTRY_CODE,
                    airSegData.startAirportCountryCode);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_AIRPORT_NAME, airSegData.startAirportName);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_AIRPORT_STATE, airSegData.startAirportState);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_GATE, airSegData.startGate);
            ContentUtils.putValue(values, Travel.AirSegmentColumns.START_TERMINAL, airSegData.startTerminal);

            Uri insUri = resolver.insert(Travel.AirSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertAirSegment: new air segment uri '"
                                + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.AirSegmentColumns.AIR_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                    // Insert any flight status.
                    if (airSegData.flightStatus != null) {
                        insertFlightStatus(resolver, insId, airSegData.flightStatus);
                    }

                    // Insert any seat data.
                    if (airSegData.seats != null) {
                        for (SeatData seat : airSegData.seats) {
                            insertSeat(resolver, insId, seat);
                        }
                    }

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertAirSegment: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert flight status information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param airSegmentId
     *            contains the <code>Travel.AirSegmentColumns._ID</code> content provider air segment id.
     * @param flightStatus
     *            contains a <code>FlightStatus</code> object.
     * @return the content provider id of the inserted flight status information.
     */
    public static int insertFlightStatus(ContentResolver resolver, int airSegmentId, FlightStatus flightStatus) {
        int retVal = -1;

        if (flightStatus != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.FlightStatusColumns.AIR_SEGMENT_ID, airSegmentId);

            ContentUtils.putValue(values, Travel.FlightStatusColumns.EQUIPMENT_SCHEDULED,
                    flightStatus.equipmentScheduled);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.EQUIPMENT_ACTUAL, flightStatus.equipmentActual);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.EQUIPMENT_REGISTRATION,
                    flightStatus.equipmentRegistration);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_TERMINAL_SCHEDULED,
                    flightStatus.departureTerminalScheduled);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_TERMINAL_ACTUAL,
                    flightStatus.departureTerminalActual);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_GATE, flightStatus.departureGate);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_SCHEDULED, Parse.XML_DF,
                    flightStatus.departureScheduled);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_ESTIMATED, Parse.XML_DF,
                    flightStatus.departureEstimated);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_ACTUAL, Parse.XML_DF,
                    flightStatus.departureActual);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_STATUS_REASON,
                    flightStatus.departureStatusReason);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_SHORT_STATUS,
                    flightStatus.departureShortStatus);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DEPARTURE_LONG_STATUS,
                    flightStatus.departureLongStatus);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_TERMINAL_SCHEDULED,
                    flightStatus.arrivalTerminalScheduled);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_TERMINAL_ACTUAL,
                    flightStatus.arrivalTerminalActual);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_GATE, flightStatus.arrivalGate);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_SCHEDULED, Parse.XML_DF,
                    flightStatus.arrivalScheduled);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_ESTIMATED, Parse.XML_DF,
                    flightStatus.arrivalEstimated);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_ACTUAL, Parse.XML_DF,
                    flightStatus.arrivalActual);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.BAGGAGE_CLAIM, flightStatus.baggageClaim);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DIVERSION_CITY, flightStatus.diversionCity);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.DIVERSION_AIRPORT, flightStatus.diversionAirport);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_STATUS_REASON,
                    flightStatus.arrivalStatusReason);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_SHORT_STATUS,
                    flightStatus.arrivalShortStatus);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.ARRIVAL_LONG_STATUS,
                    flightStatus.arrivalLongStatus);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.CLIQBOOK_MESSAGE, flightStatus.cliqbookMessage);
            ContentUtils.putValue(values, Travel.FlightStatusColumns.LAST_UPDATED_UTC, Parse.XML_DF,
                    flightStatus.lastUpdatedUTC);

            Uri insUri = resolver.insert(Travel.FlightStatusColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertFlightStatus: new flight status uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.FlightStatusColumns.FLIGHT_STATUS_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightStatus: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightStatus: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertFlightStatus: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert air segment seat information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param airSegmentId
     *            contains the <code>Travel.AirSegmentColumns._ID</code> content provider air segment id.
     * @param seat
     *            contains a <code>SeatData</code> object.
     * @return the content provider id of the inserted seat information.
     */
    public static int insertSeat(ContentResolver resolver, int airSegmentId, SeatData seat) {
        int retVal = -1;

        if (seat != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.SeatColumns.AIR_SEGMENT_ID, airSegmentId);
            ContentUtils.putValue(values, Travel.SeatColumns.PASSENGER_RPH, seat.passengerRPH);
            ContentUtils.putValue(values, Travel.SeatColumns.SEAT_NUMBER, seat.seatNumber);
            ContentUtils.putValue(values, Travel.SeatColumns.STATUS_CODE, seat.statusCode);

            Uri insUri = resolver.insert(Travel.SeatColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertSeat: new seat uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer
                            .parseInt(insUri.getPathSegments().get(Travel.SeatColumns.SEAT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSeat: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSeat: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertSeat: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert hotel segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param hotelSegData
     *            contains a <code>HotelSegmentData</code> object.
     * @return the content provider id of the inserted hotel segment information.
     */
    public static int insertHotelSegment(ContentResolver resolver, int bookingId, int segmentId,
            HotelSegmentData hotelSegData) {
        int retVal = -1;

        if (hotelSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.HotelSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.CHECK_IN_TIME, hotelSegData.checkinTime);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.CHECK_OUT_TIME, hotelSegData.checkoutTime);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.DISCOUNT_CODE, hotelSegData.discountCode);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.NUM_ROOMS, hotelSegData.numRooms);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.RATE_CODE, hotelSegData.rateCode);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.ROOM_TYPE, hotelSegData.roomType);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.ROOM_TYPE_LOCALIZED,
                    hotelSegData.roomTypeLocalized);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.DAILY_RATE, hotelSegData.dailyRate);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.TOTAL_RATE, hotelSegData.totalRate);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.CANCELLATION_POLICY,
                    hotelSegData.cancellationPolicy);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.SPECIAL_INSTRUCTIONS,
                    hotelSegData.specialInstructions);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.ROOM_DESCRIPTION, hotelSegData.roomDescription);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.RATE_TYPE, hotelSegData.rateType);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.PROPERTY_ID, hotelSegData.propertyId);
            ContentUtils.putValue(values, Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT,
                    hotelSegData.propertyImageCount);

            Uri insUri = resolver.insert(Travel.HotelSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertHotelSegment: new hotel segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.HotelSegmentColumns.HOTEL_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertHotelSegment: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert car segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param carSegData
     *            contains a <code>CarSegmentData</code> object.
     * @return the content provider id of the inserted car segment information.
     */
    public static int insertCarSegment(ContentResolver resolver, int bookingId, int segmentId, CarSegmentData carSegData) {
        int retVal = -1;

        if (carSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.CarSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.AIR_CONDITION, carSegData.airCondition);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.AIR_CONDITION_LOCALIZED,
                    carSegData.airConditionLocalized);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.BODY, carSegData.body);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.BODY_LOCALIZED, carSegData.bodyLocalized);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.CLASS_OF_CAR, carSegData.classOfCar);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.CLASS_OF_CAR_LOCALIZED,
                    carSegData.classOfCarLocalized);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.DAILY_RATE, carSegData.dailyRate);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.DISCOUNT_CODE, carSegData.discountCode);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_AIRPORT_CITY, carSegData.endAirportCity);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_AIRPORT_COUNTRY, carSegData.endAirportCountry);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_AIRPORT_COUNTRY_CODE,
                    carSegData.endAirportCountryCode);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_AIRPORT_NAME, carSegData.endAirportName);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_AIRPORT_STATE, carSegData.endAirportState);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.END_LOCATION, carSegData.endLocation);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.IMAGE_CAR_URI, carSegData.imageCarUri);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.NUM_CARS, carSegData.numCars);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.RATE_TYPE, carSegData.rateType);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.SPECIAL_EQUIPMENT, carSegData.specialEquipment);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_AIRPORT_CITY, carSegData.startAirportCity);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_AIRPORT_COUNTRY,
                    carSegData.startAirportCountry);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_AIRPORT_COUNTRY_CODE,
                    carSegData.startAirportCountryCode);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_AIRPORT_NAME, carSegData.startAirportName);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_AIRPORT_STATE, carSegData.startAirportState);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.START_LOCATION, carSegData.startLocation);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.TRANSMISSION, carSegData.transmission);
            ContentUtils.putValue(values, Travel.CarSegmentColumns.TRANSMISSION_LOCALIZED,
                    carSegData.transmissionLocalized);

            Uri insUri = resolver.insert(Travel.CarSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG,
                        CLS_TAG + ".insertCarSegment: new car segment uri '"
                                + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.CarSegmentColumns.CAR_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertCarSegment: insertion uri is null.");
                }
            }
        }

        return retVal;
    }

    /**
     * Will insert rail segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param railSegData
     *            contains a <code>RailSegmentData</code> object.
     * @return the content provider id of the inserted rail segment information.
     */
    public static int insertRailSegment(ContentResolver resolver, int bookingId, int segmentId,
            RailSegmentData railSegData) {
        int retVal = -1;

        if (railSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.RailSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.SEGMENT_ID, segmentId);

            ContentUtils.putValue(values, Travel.RailSegmentColumns.AMENITIES, railSegData.amenities);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.CABIN, railSegData.cabin);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.CLASS_OF_SERVICE, railSegData.classOfService);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.DISCOUNT_CODE, railSegData.discountCode);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.DURATION, railSegData.duration);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.END_PLATFORM, railSegData.endPlatform);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.END_RAIL_STATION, railSegData.endRailStation);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.END_RAIL_STATION_LOCALIZED,
                    railSegData.endRailStationLocalized);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.LEG_ID, railSegData.legId);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.MEALS, railSegData.meals);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.MILES, railSegData.miles);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.NUM_STOPS, railSegData.numStops);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.OPERATED_BY_TRAIN_NUMBER,
                    railSegData.operatedByTrainNumber);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.PIN, railSegData.pin);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.START_PLATFORM, railSegData.startPlatform);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.START_RAIL_STATION, railSegData.startRailStation);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.START_RAIL_STATION_LOCALIZED,
                    railSegData.startRailStationLocalized);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.TRAIN_NUMBER, railSegData.trainNumber);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.TRAIN_TYPE_CODE, railSegData.trainTypeCode);
            ContentUtils.putValue(values, Travel.RailSegmentColumns.WAGON_NUMBER, railSegData.wagonNumber);

            Uri insUri = resolver.insert(Travel.RailSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertRailSegment: new rail segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.RailSegmentColumns.RAIL_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRailSegment: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert dining segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param dinSegData
     *            contains a <code>DiningSegmentData</code> object.
     * @return the content provider id of the inserted dining segment information.
     */
    public static int insertDiningSegment(ContentResolver resolver, int bookingId, int segmentId,
            DiningSegmentData dinSegData) {
        int retVal = -1;

        if (dinSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.DiningSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.DiningSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils.putValue(values, Travel.DiningSegmentColumns.RESERVATION_ID, dinSegData.reservationId);

            Uri insUri = resolver.insert(Travel.DiningSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertDiningSegment: new rail segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.DiningSegmentColumns.DINING_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDiningSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDiningSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertDiningSegment: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert event segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param evtSegData
     *            contains a <code>EventSegmentData</code> object.
     * @return the content provider id of the inserted event segment information.
     */
    public static int insertEventSegment(ContentResolver resolver, int bookingId, int segmentId,
            EventSegmentData evtSegData) {
        int retVal = -1;

        if (evtSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.EventSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.EventSegmentColumns.SEGMENT_ID, segmentId);
            // TODO: Determine what, if any, columns should be defined.

            Uri insUri = resolver.insert(Travel.EventSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertEventSegment: new event segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.EventSegmentColumns.EVENT_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEventSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEventSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertEventSegment: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert parking segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param parkSegData
     *            contains a <code>ParkingSegmentData</code> object.
     * @return the content provider id of the inserted parking segment information.
     */
    public static int insertParkingSegment(ContentResolver resolver, int bookingId, int segmentId,
            ParkingSegmentData parkSegData) {
        int retVal = -1;

        if (parkSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.ParkingSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.ParkingSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils.putValue(values, Travel.ParkingSegmentColumns.PARKING_LOCATION_ID,
                    parkSegData.parkingLocationId);
            ContentUtils.putValue(values, Travel.ParkingSegmentColumns.PIN, parkSegData.pin);
            ContentUtils.putValue(values, Travel.ParkingSegmentColumns.START_LOCATION, parkSegData.startLocation);

            Uri insUri = resolver.insert(Travel.ParkingSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertParkingSegment: new event segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.ParkingSegmentColumns.PARKING_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertParkingSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertParkingSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertParkingSegment: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

    /**
     * Will insert ride segment information.
     * 
     * @param resolver
     *            contains an application resolver.
     * @param bookingId
     *            contains the <code>Travel.BookingColumns._ID</code> content provider booking id.
     * @param segmentId
     *            contains the <code>Travel.SegmentColumns._ID</code> content provider segment id.
     * @param rideSegData
     *            contains a <code>RideSegmentData</code> object.
     * @return the content provider id of the inserted ride segment information.
     */
    public static int insertParkingSegment(ContentResolver resolver, int bookingId, int segmentId,
            RideSegmentData rideSegData) {
        int retVal = -1;

        if (rideSegData != null) {

            // Set up the content values object.
            ContentValues values = new ContentValues();

            ContentUtils.putValue(values, Travel.RideSegmentColumns.BOOKING_ID, bookingId);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.SEGMENT_ID, segmentId);
            ContentUtils
                    .putValue(values, Travel.RideSegmentColumns.CANCELLATION_POLICY, rideSegData.cancellationPolicy);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.DROP_OFF_INSTRUCTIONS,
                    rideSegData.dropoffInstructions);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.DURATION, rideSegData.duration);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.MEETING_INSTRUCTIONS,
                    rideSegData.meetingInstructions);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.MILES, rideSegData.miles);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.NUMBER_OF_HOURS, rideSegData.numberOfHours);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.PICK_UP_INSTRUCTIONS,
                    rideSegData.pickupInstructions);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.RATE, rideSegData.rate);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.RATE_DESCRIPTION, rideSegData.rateDescription);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.RATE_TYPE, rideSegData.rateType);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.START_LOCATION, rideSegData.startLocation);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.START_LOCATION_CODE, rideSegData.startLocationCode);
            ContentUtils.putValue(values, Travel.RideSegmentColumns.START_LOCATION_NAME, rideSegData.startLocationName);

            Uri insUri = resolver.insert(Travel.RideSegmentColumns.CONTENT_URI, values);

            if (DEBUG) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".insertRideSegment: new event segment uri '"
                        + ((insUri != null) ? insUri.toString() : "null"));
            }
            if (insUri != null) {
                try {
                    int insId = Integer.parseInt(insUri.getPathSegments().get(
                            Travel.RideSegmentColumns.RIDE_SEGMENT_ID_PATH_POSITION));

                    retVal = insId;

                } catch (NumberFormatException nfe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRideSegment: content id is not an integer!", nfe);
                } catch (IndexOutOfBoundsException iobe) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRideSegment: id is not in content uri!", iobe);
                }
            } else {
                if (DEBUG) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".insertRideSegment: insertion uri is null.");
                }
            }
        }
        return retVal;
    }

}
