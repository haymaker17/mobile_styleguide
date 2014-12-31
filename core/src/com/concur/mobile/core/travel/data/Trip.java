package com.concur.mobile.core.travel.data;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.travel.data.Segment.SegmentType;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.travel.rail.data.RailSegment;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * Models a trip as defined as a sequence of itinerary segments.
 */
public class Trip {

    // Contains the itinerary locator.
    public String itinLocator;

    // Contains the client locator.
    public String clientLocator;

    // Contains the record locator of the primary booking record (record with
    // 'IsCliqbookSystemOfRecord=true') XML node.
    public String recordLocator;

    // Contains the CliqBook trip id.
    public String cliqbookTripId;

    // Trip name.
    public String name;

    // Trip description.
    public String desc;

    // Trip comment.
    public String comment;

    // Trip start UTC.
    public Calendar startUtc;

    // Trip end UTC.
    public Calendar endUtc;

    // Trip start local time.
    public Calendar startLocal;

    // Trip end local time.
    public Calendar endLocal;

    // If this is a list of summary trip objects, then this list will contain the booking record locators.
    public List<String> bookingRecordLocators;

    // Contains the authorization number for gov user.
    public String authNumber;

    // Workflow State
    public int state;
    // const cCBSTATE_None = 0
    // const cCBSTATE_EditInProcess = 10
    // const cCBSTATE_AwaitingVendorInfo = 20
    // const cCBSTATE_TripHold = 100
    // const cCBSTATE_AwaitingApproval = 101
    // const cCBSTATE_FinishErrorsAwaitingApproval = 111
    // const cCBSTATE_WaitingForDirectConnectFinishing = 150
    // const cCBSTATE_FinishQueueTicketing = 200
    // const cCBSTATE_FinishApprovalHold = 201
    // const cCBSTATE_FinishEditTrip = 202
    // ' Queue states for non refund/exchange
    // const cCBSTATE_QueueNeedsTicketing = 300
    // const cCBSTATE_QueueNeedsApprovalHold = 301
    // ' Queue states for refund/exchange
    // const cCBSTATE_QueueVoidComplete = 360
    // const cCBSTATE_QueueNeedsCancelOffline = 361 'agency needs to cancel this.
    // const cCBSTATE_FinishNeedsCancelOffline = 362 'agency needs to cancel this add comments to PNR.
    // ' Error states
    // const cCBSTATE_FinishErrorsNeedTicket = 900
    // const cCBSTATE_FinishErrorsApprovalHold = 901
    // const cCBSTATE_QueueTicketingError = 910
    // const cCBSTATE_QueueApprovalHoldError = 911
    // const cCBSTATE_FinishErrorsQueueApprovalHoldError = 921
    // const cCBSTATE_QueueVoidCompleteError = 960
    // const cCBSTATE_QueueNeedsCancelOfflineError = 961
    // const cCBSTATE_FinishErrorsNeedsCancelOffline = 962
    // const cCBSTATE_FinishErrorsQueueNeedsCancelOfflineError = 963
    // ' Terminal (finished) states
    // const cCBSTATE_FinishComplete = 1000
    // const cCBSTATE_FinishFailButQueueTicketOK = 1001
    // const cCBSTATE_CancelSentToAgency = 1100

    // Store segments. We care about them (not bookings). We want them grouped
    // by start day for the
    // segment list so let's just store them that way.
    TreeMap<Calendar, List<Segment>> segmentStartDayMap = new TreeMap<Calendar, List<Segment>>();

    // With the advent of InTouch offerings, we want them grouped by end day as
    // well.
    TreeMap<Calendar, List<Segment>> segmentEndDayMap = new TreeMap<Calendar, List<Segment>>();

    // But we also want quick access so store a map based on the segment key
    TreeMap<String, Segment> segmentKeyMap = new TreeMap<String, Segment>();

    // Contains a mapping from a segment key to a list of offerings.
    TreeMap<String, List<Offer>> segmentOfferMap = new TreeMap<String, List<Offer>>();

    // Contains all the trip offers. Some offers may not be specifically tied to
    // segments.
    List<Offer> offers;

    // Contains all the allowed actions for this Trip.
    public boolean allowAddAir = true;
    public boolean allowAddCar = true;
    public boolean allowAddHotel = true;
    public boolean allowAddRail = true;
    public boolean allowCancel = true;

    // start of additional data from new end point GetUserTripListV2
    public ApprovalStatusEnum approvalStatusEnum;
    public String approverId;
    public String approverName;
    public String bookedVia;
    public String bookingSource;
    public boolean canBeExpensed;
    public int cliqbookState;
    public boolean hasOthers;
    public boolean hasTickets;
    public boolean isExpensed;
    public boolean isGdsBooking;
    public boolean isPersonal;
    public boolean isWithdrawn;
    public boolean isPublic;
    public int itinId;
    public String itinSourceList;
    public String recordLocatorFromXml;
    public String segmentTypes;
    public int tripId;
    public String tripKey;
    public List<String> tripStateMessages = new ArrayList<String>();
    public int tripStatus; // may be the State
    // end of additional data from new end point

    // flag to track that entire trip is cancelled after the successful last segment cancellation
    // used in redirecting the user to trip list UI rather than staying at the empty segment detail UI
    private boolean entireTripCancelled;

    // to identify the trip state, can be used in categorizing trips by their state in the trip list
    public static enum ApprovalStatusEnum {
        AwaitingApproval, AwaitingPreprocessing, AwaitingProcessing, AwaitingFulfillment, Fulfilled, RejectedCantOverride, RejectedAndClosed, RejectedOverridable, Withdrawn
    };

    private List<RuleViolation> ruleViolations;

    // record locator that to be displayed in the trip header
    public String recordLocatorForAgent;

    // itinerary level travel points
    public TravelPoint itinTravelPoint;

    protected Trip() {
    }

    /**
     * Gets the concatenated string of all the trip state messages separated by a dot and space
     * 
     * @return
     */
    public String getFormattedTripStateMessages() {
        StringBuilder sb = new StringBuilder();
        if (tripStateMessages.size() > 0) {
            sb.append(TextUtils.join(". ", tripStateMessages));
        }
        return sb.toString();
    }

    /**
     * Gets the formatted start time of the trip.
     * 
     * @return the formatted start time of the trip.
     */
    public String getFormattedStart() {
        String start = "";
        if (startLocal != null) {
            start = FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(startLocal.getTime());
        }
        return start;
    }

    /**
     * Gets the formatted end time of the trip.
     * 
     * @return the formatted end time of the trip.
     */
    public String getFormattedEnd() {
        String end = "";
        if (endLocal != null) {
            end = FormatUtil.SHORT_MONTH_DAY_FULL_YEAR_DISPLAY.format(endLocal.getTime());
        }
        return end;
    }

    /**
     * Gets one string with formatted start/end times.
     * 
     * @return a string formatted with start/end time.
     */
    public String getDateSpan() {
        StringBuilder sb = new StringBuilder();
        sb.append(getFormattedStart()).append(" - ").append(getFormattedEnd());
        return sb.toString();
    }

    /**
     * Gets the distance in time (hours) of the closest segment to "now" based on looking at the <code>Segment.startDateUTC</code>
     * .
     * 
     * This method only looks at segments with start times in the future, not past.
     * 
     * @return returns the segment with the closest time to "now" (hours) in UTC.
     */
    public int getClosestSegmentToNow() {
        int closestSegmentHours = -1;
        if (segmentKeyMap != null) {
            Calendar utcNowCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            long utcNowMillis = utcNowCal.getTimeInMillis();
            Iterator<String> segKeys = segmentKeyMap.keySet().iterator();
            long closestTimeMillis = -1;
            while (segKeys.hasNext()) {
                String segKey = segKeys.next();
                Segment seg = segmentKeyMap.get(segKey);
                if (seg.startDateUtc != null) {
                    long segStartMillis = seg.startDateUtc.getTimeInMillis();
                    if (segStartMillis > utcNowMillis) {
                        if (closestTimeMillis == -1) {
                            closestTimeMillis = (segStartMillis - utcNowMillis);
                        } else {
                            closestTimeMillis = Math.min(closestTimeMillis, (segStartMillis - utcNowMillis));
                        }
                    }
                }
            }
            if (closestTimeMillis != -1) {
                closestSegmentHours = (int) (closestTimeMillis / (1000L * 60L * 60L));
            }
        }
        return closestSegmentHours;
    }

    /**
     * Adds the <code>segment</code> segment to the trip.
     * 
     * @param segment
     *            the segment.
     */
    public void addSegment(Segment segment) {
        // Store them by key
        segmentKeyMap.put(segment.segmentKey, segment);

        // Then put them in the start day map
        // Pull the segment day and see if it is in the hash
        Calendar day = segment.getStartDayLocal();

        // Sanity check. Date should never be null but we don't want to die.
        if (day != null) {
            List<Segment> segments = segmentStartDayMap.get(day);

            if (segments == null) {
                // If it is not then create a new list and put it in the map
                segments = new ArrayList<Segment>(3);
                segmentStartDayMap.put(day, segments);
            }

            // Then add our segment to the list
            segments.add(segment);
        }

        // And the end day map
        // Pull the segment day and see if it is in the hash
        day = segment.getEndDayLocal();

        // Sanity check. Date should never be null but we don't want to die.
        if (day != null) {
            List<Segment> segments = segmentEndDayMap.get(day);

            if (segments == null) {
                // If it is not then create a new list and put it in the map
                segments = new ArrayList<Segment>(3);
                segmentEndDayMap.put(day, segments);
            }

            // Then add our segment to the list
            segments.add(segment);
        }
    }

    /**
     * Gets the map of segment start days to segment list.
     * 
     * @return the segment day to segment list map.
     */
    public TreeMap<Calendar, List<Segment>> getSegmentStartDayMap() {
        return segmentStartDayMap;
    }

    /**
     * Gets the map of segment end days to segment list.
     * 
     * @return the segment day to segment list map.
     */
    public TreeMap<Calendar, List<Segment>> getSegmentEndDayMap() {
        return segmentEndDayMap;
    }

    /**
     * Gets a segment by key.
     * 
     * @param segKey
     *            the segment key.
     * @return the segment.
     */
    public Segment getSegment(String segKey) {
        return segmentKeyMap.get(segKey);
    }

    /**
     * Return a full count of the number of segments in this trip.
     */
    public int getSegmentCount() {
        return segmentKeyMap.size();
    }

    /**
     * Return a list of segments of the specified type in day order.
     * 
     * @param desiredType
     *            The {@link SegmentType} of the desired segments
     * @param max
     *            The maximum number of segments to return
     * @return An {@link ArrayList} of segments of the desired type up to the max number
     */
    @SuppressWarnings("unchecked")
    public <T> ArrayList<T> getSegmentsByType(Segment.SegmentType desiredType, int max) {

        if (max < 1)
            max = Integer.MAX_VALUE;

        ArrayList<T> segs = new ArrayList<T>(max < 10 ? max : 10);

        Calendar cal = null;
        int count = 0;

        // Get the iterator for the map
        Set<Calendar> days = segmentStartDayMap.keySet();
        Iterator<Calendar> it = days.iterator();

        // Walk each day and then the segments within. Bail if max is hit
        while (it.hasNext() && count < max) {
            cal = it.next();
            List<Segment> daySegs = segmentStartDayMap.get(cal);
            for (Segment seg : daySegs) {
                if (seg.type == desiredType) {
                    segs.add((T) seg);
                    count++;
                }
                if (count >= max)
                    break;
            }
        }

        return segs;
    }

    public Offer getOffer(String offerId) {
        Offer offer = null;

        if (offerId != null) {
            for (Offer o : offers) {
                if (offerId.equals(o.id)) {
                    offer = o;
                    break;
                }
            }
        }

        return offer;
    }

    public boolean hasOffers() {
        return (offers != null && offers.size() > 0);
    }

    /**
     * Get the offers for a segment
     * 
     * @param segKey
     * @return
     */
    public List<Offer> getValidSegmentOffers(String segKey) {
        return getValidSegmentOffers(segKey, OfferSegmentLink.SIDE_START | OfferSegmentLink.SIDE_END
                | OfferSegmentLink.SIDE_DURATION);
    }

    public List<Offer> getValidSegmentOffers(String segKey, int offerSide) {
        List<Offer> allOffers = segmentOfferMap.get(segKey);
        List<Offer> validOffers = new ArrayList<Offer>();

        // Iterate the offers and keep the valid ones and the ones on the
        // appropriate side of the segment
        if (allOffers != null) {
            for (Offer o : allOffers) {
                if (o.isValid()) {
                    if ((o.segmentLink.segmentSide & offerSide) != 0) {
                        validOffers.add(o);
                    }
                }
            }
        }

        return validOffers;
    }

    /**
     * Will parse a list of itinerary XML objects and return a list of <code>Trip</code> objects.
     * 
     * @param responseXml
     *            the XML to parse.
     * @return the list of <code>Trip</code> objects.
     */
    public static ArrayList<Trip> parseItineraryXml(String responseXml) {

        ArrayList<Trip> trips = null;

        if (responseXml != null && responseXml.length() > 0) {

            trips = new ArrayList<Trip>();

            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser parser = factory.newSAXParser();
                ItinListSAXHandler handler = new ItinListSAXHandler();
                parser.parse(new ByteArrayInputStream(responseXml.getBytes()), handler);
                trips = handler.getTrips();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return trips;
    }

    public List<RuleViolation> getRuleViolations() {
        return ruleViolations;
    }

    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////
    //
    // BELOW HERE BE SAX DRAGONS
    //
    // ////////////////////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper class to handle parsing of itinerary XML.
     */
    protected static class ItinListSAXHandler extends DefaultHandler {

        private static final String DINING = "Dining";
        private static final String BOOKING = "Booking";
        private static final String RIDE = "Ride";
        private static final String RAIL = "Rail";
        private static final String PARKING = "Parking";
        private static final String HOTEL = "Hotel";
        private static final String EVENT = "Event";
        private static final String CAR = "Car";
        private static final String AIR = "Air";
        private static final String SEGMENT = "Segment";
        private static final String SEGMENTS = "Segments";
        private static final String BOOKINGS = "Bookings";
        private static final String ITINERARY = "Itinerary";
        private static final String ITINERARY_SUMMARY = "ItinerarySummary";
        private static final String FLIGHT_STATUS = "FlightStatus";
        private static final String BOOKING_LOCATORS = "BookingLocators";
        private static final String BOOKING_LOCATOR = "BookingLocator";
        private static final String ACTIONS = "Actions";
        private static final String ALLOW_ADD_AIR = "AllowAddAir";
        private static final String ALLOW_ADD_CAR = "AllowAddCar";
        private static final String ALLOW_ADD_HOTEL = "AllowAddHotel";
        private static final String ALLOW_ADD_RAIL = "AllowAddRail";
        private static final String ALLOW_CANCEL = "AllowCancel";

        private static final String OFFERS = "Offers";

        private static final String TRIP_LIST_ITINERARY = "TripListItinerary";

        // start of rule violations
        private static final String RULE_VIOLATIONS = "RuleViolations";
        private boolean inRuleViolations;
        private boolean inItinRuleViolation;
        private boolean inCarRuleViolation;
        private boolean inHotelRuleViolation;
        private boolean inFlightRuleViolation;
        private boolean inRailRuleViolation;
        private List<RuleViolation> itinRuleViolations;
        private List<CarRuleViolation> carRuleViolations;
        private List<HotelRuleViolation> hotelRuleViolations;
        private List<FlightRuleViolation> flightRuleViolations;
        private List<RailRuleViolation> railRuleViolations;
        private RuleViolation itinRuleViolation;
        private CarRuleViolation carRuleViolation;
        private HotelRuleViolation hotelRuleViolation;
        private FlightRuleViolation flightRuleViolation;
        private RailRuleViolation railRuleViolation;
        private RuleViolationReason ruleViolationReason;
        private List<RuleViolationReason> ruleViolationReasons;
        private List<String> rules;
        // end of rule violations

        // Fields to help parsing
        private StringBuilder chars;

        // Tracking flags for our position in the hierarchy.
        // A stack of element names would be cleaner and prettier but the
        // booleans will be a lot faster.
        private boolean inBookings;
        private boolean inBooking; // Yes, horrid to differ only by
                                   // singular/plural but we'll live with it
                                   // since the XML does it
        private boolean inSegments;
        private boolean inSegment;
        private boolean inFlightStats;
        private boolean inOffers;
        private boolean inBookingLocators;
        private boolean inActions;
        private boolean inTripListItinerary;

        private boolean inPrimaryBookingRecord;
        private String bookingRecordLocator;

        // Holders for our parsed data
        private ArrayList<Trip> trips;
        private Trip trip;
        private Segment segment;
        private int bookingSourceId;
        private String bookingSource;

        // Sub-element SAX parsers
        private Offer.OfferSAXHandler offerSAXHandler;

        private String iPrefixUri;

        // itinerary level travel points
        private boolean inItinTravelPoint;

        /**
         * Retrieve our list of parsed trips
         * 
         * @return A List of {@link Trip} objects parsed from the XML
         */
        protected ArrayList<Trip> getTrips() {
            return trips;
        }

        @Override
        public void startDocument() throws SAXException {
            super.startDocument();

            chars = new StringBuilder();
            trips = new ArrayList<Trip>();
        }

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (inOffers) {
                offerSAXHandler.characters(ch, start, length);
            } else {
                chars.append(ch, start, length);
            }
        }

        @Override
        public void startPrefixMapping(String prefix, String uri) throws SAXException {
            super.startPrefixMapping(prefix, uri);

            if ("i".equals(prefix)) {
                iPrefixUri = uri;
            }
        }

        /**
         * Handle the opening of all elements. Create data objects as needed for use in endElement(). See endElement() for a
         * detailed comment on the logic in use here.
         */
        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            super.startElement(uri, localName, qName, attributes);

            if (handleRuleViolationStartElement(localName)) {
                // continue
            } else if (localName.equalsIgnoreCase(ITINERARY) || localName.equalsIgnoreCase(ITINERARY_SUMMARY)
                    || localName.equalsIgnoreCase(TRIP_LIST_ITINERARY)) {
                trip = new Trip();
                if (localName.equalsIgnoreCase(TRIP_LIST_ITINERARY)) {
                    inTripListItinerary = true;
                }
            } else if (localName.equalsIgnoreCase(BOOKING_LOCATORS)) {
                trip.bookingRecordLocators = new ArrayList<String>();
                inBookingLocators = true;
            } else if (localName.equalsIgnoreCase(BOOKINGS)) {
                inBookings = true;
            } else if (localName.equalsIgnoreCase(OFFERS)) {
                inOffers = true;
                trip.offers = new ArrayList<Offer>();
                offerSAXHandler = new Offer.OfferSAXHandler();
            } else if (inOffers) {
                offerSAXHandler.startElement(uri, localName, qName, attributes);
            } else if (localName.equalsIgnoreCase(ACTIONS)) {
                inActions = true;
            } else if (inBookings) {
                // Capture the nested start elements that we care about
                if (localName.equalsIgnoreCase(BOOKING)) {
                    inBooking = true;
                } else if (inBooking) {
                    if (localName.equalsIgnoreCase(SEGMENTS)) {
                        inSegments = true;
                    } else if (inSegments) {
                        if (localName.equalsIgnoreCase(SEGMENT)) {
                            inSegment = true;
                            String segType = attributes.getValue(iPrefixUri, "type");

                            if (segType.equalsIgnoreCase(AIR)) {
                                // Create the air segment
                                segment = new AirSegment();
                            } else if (segType.equalsIgnoreCase(CAR)) {
                                // Create the car segment
                                segment = new CarSegment();
                            } else if (segType.equalsIgnoreCase(DINING)) {
                                // Create the dining segment
                                segment = new DiningSegment();
                            } else if (segType.equalsIgnoreCase(EVENT)) {
                                // Create the event segment
                                segment = new EventSegment();
                            } else if (segType.equalsIgnoreCase(HOTEL)) {
                                // Create the hotel segment
                                segment = new HotelSegment();
                            } else if (segType.equalsIgnoreCase(PARKING)) {
                                // Create the parking segment
                                segment = new ParkingSegment();
                            } else if (segType.equalsIgnoreCase(RAIL)) {
                                // Create the rail segment
                                segment = new RailSegment();
                            } else if (segType.equalsIgnoreCase(RIDE)) {
                                // Create the ride segment
                                segment = new RideSegment();
                            } else {
                                // Unknown segment type
                                Log.i(Const.LOG_TAG, "Unknown segment type found.  Ignoring. segType = " + segType);
                            }

                            if (segment != null) {
                                segment.gdsId = bookingSourceId;
                                segment.bookingSource = bookingSource;
                            }
                        } else if (localName.equalsIgnoreCase(FLIGHT_STATUS)) {
                            inFlightStats = true;
                            // Sanity
                            if (segment.type == SegmentType.AIR) {
                                ((AirSegment) segment).flightStatus = ((AirSegment) segment).new FlightStatusInfo();
                            }
                        } else if (inSegment && localName.equalsIgnoreCase("TravelPoints")) {
                            segment.inTravelPoint = true;
                            segment.travelPoint = new TravelPoint();
                        }
                    }
                }
            } else if (localName.equalsIgnoreCase("TravelPoints")) {
                // itinerary level travel points
                inItinTravelPoint = true;
                trip.itinTravelPoint = new TravelPoint();
            }
        }

        /**
         * Handle the closing of all elements. The nested structure looks complex but it isn't that bad. It is basically a single
         * path hierarchy down to the lowest level where most of the real data parsing occurs.
         * 
         * The XML document is intended to have a structure like this: <Itinerary> <Bookings> <Booking> <Segments> <Segment
         * i:type=[Air | Car | Event | ...]> <... various data elements ...> </Segment> </Segments> <... various data elements
         * ...> </Booking> </Bookings> <... various data elements ...> </Itinerary>
         * 
         * So the collection of actual data only occurs at three levels: itinerary, booking, and segment-type. Everything else is
         * just bookkeeping to make sure we are collecting the data we think we are since the data element names are common across
         * different segment types.
         */
        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            super.endElement(uri, localName, qName);

            if (trip != null) { // paranoia

                if (inOffers) {
                    if (localName.equalsIgnoreCase(OFFERS)) {
                        inOffers = false;
                        trip.offers = offerSAXHandler.getOffers();
                        offerSAXHandler = null;
                    } else {
                        offerSAXHandler.endElement(uri, localName, qName);
                    }
                } else if (inActions) {
                    if (localName.equalsIgnoreCase(ACTIONS)) {
                        inActions = false;
                    } else if (localName.equalsIgnoreCase(ALLOW_ADD_AIR)) {
                        trip.allowAddAir = Parse.safeParseBoolean(chars.toString().trim());
                    } else if (localName.equalsIgnoreCase(ALLOW_ADD_CAR)) {
                        trip.allowAddCar = Parse.safeParseBoolean(chars.toString().trim());
                    } else if (localName.equalsIgnoreCase(ALLOW_ADD_HOTEL)) {
                        trip.allowAddHotel = Parse.safeParseBoolean(chars.toString().trim());
                    } else if (localName.equalsIgnoreCase(ALLOW_ADD_RAIL)) {
                        trip.allowAddRail = Parse.safeParseBoolean(chars.toString().trim());
                    } else if (localName.equalsIgnoreCase(ALLOW_CANCEL)) {
                        trip.allowCancel = Parse.safeParseBoolean(chars.toString().trim());
                    }

                } else if (inBookings) {
                    if (localName.equalsIgnoreCase(BOOKINGS)) {
                        // End the bookings elements
                        inBookings = false;
                    } else if (inBooking) {
                        if (localName.equalsIgnoreCase(BOOKING)) {
                            // End the booking element
                            inBooking = false;
                            // Reset the booking source
                            bookingSourceId = determineGDSId("");
                            bookingSource = null;
                            // Process and reset the primary booking record
                            // information.
                            if (inPrimaryBookingRecord && bookingRecordLocator != null) {
                                trip.recordLocator = bookingRecordLocator;
                            }
                            inPrimaryBookingRecord = false;
                            bookingRecordLocator = null;
                        } else if (localName.equalsIgnoreCase("BookingSource")) {
                            bookingSource = chars.toString().trim();
                            bookingSourceId = determineGDSId(bookingSource);
                        } else if (localName.equalsIgnoreCase("IsCliqbookSystemOfRecord")) {
                            inPrimaryBookingRecord = Parse.safeParseBoolean(chars.toString().trim());
                        } else if (localName.equalsIgnoreCase("RecordLocator")) {
                            bookingRecordLocator = chars.toString().trim();
                        } else if (inSegments) {
                            String cleanChars = chars.toString().trim();

                            if (localName.equalsIgnoreCase(SEGMENTS)) {
                                // End the segments element
                                inSegments = false;

                                // Since we should now have all segments and offers, iterate the offers and update the maps
                                if (trip.offers != null) {
                                    for (Offer offer : trip.offers) {
                                        if (offer.segmentLink.segmentKey != null) {

                                            // Point the link back at it's segment
                                            offer.segmentLink.segment = trip.segmentKeyMap
                                                    .get(offer.segmentLink.segmentKey);

                                            // And add the link to the list of segment offers
                                            List<Offer> offerList = trip.segmentOfferMap
                                                    .get(offer.segmentLink.segmentKey);
                                            if (offerList == null) {
                                                offerList = new ArrayList<Offer>();
                                                trip.segmentOfferMap.put(offer.segmentLink.segmentKey, offerList);
                                            }
                                            if (!offerList.contains(offer)) {
                                                offerList.add(offer);
                                            }
                                        }
                                    }
                                }

                            } else if (inSegment) {
                                if (localName.equalsIgnoreCase(SEGMENT)) {
                                    inSegment = false;

                                    if (segment != null) {
                                        // Merge in flight stats since we now
                                        // have all the data
                                        if (segment.type == SegmentType.AIR) {
                                            ((AirSegment) segment).mergeFlightStats();
                                        }

                                        // Set the booking record locator that
                                        // this segment is a part of.
                                        segment.locator = bookingRecordLocator;

                                        trip.addSegment(segment);
                                    }
                                } else if (inFlightStats) {
                                    if (localName.equalsIgnoreCase(FLIGHT_STATUS)) {
                                        inFlightStats = false;
                                    } else {
                                        // Sanity
                                        if (segment.type == SegmentType.AIR) {
                                            ((AirSegment) segment).flightStatus.handleElement(localName, cleanChars);
                                        }
                                    }
                                } else if (segment.inTravelPoint && localName.equalsIgnoreCase("TravelPoints")) {
                                    segment.inTravelPoint = false;
                                } else if (segment != null) {
                                    segment.handleSegmentElement(localName, cleanChars);
                                }
                            }
                        } // We do not care about the data elements in booking
                    } // There are no data elements to bookings so do nothing if
                      // we're not in a booking
                } else if (inBookingLocators) {
                    if (localName.equalsIgnoreCase(BOOKING_LOCATOR)) {
                        String cleanChars = chars.toString().trim();
                        trip.bookingRecordLocators.add(cleanChars);
                    } else if (localName.equalsIgnoreCase(BOOKING_LOCATORS)) {
                        inBookingLocators = false;
                    }
                } else if (handleRuleViolationEndElement(localName)) {
                    // continue
                } else if (inItinTravelPoint) {
                    // itinerary level travel points
                    if (localName.equalsIgnoreCase("PointsPosted")) {
                        String cleanChars = (chars == null ? "" : chars.toString());
                        trip.itinTravelPoint.setPointsPosted(cleanChars);
                    } else if (localName.equalsIgnoreCase("PointsPending")) {
                        String cleanChars = (chars == null ? "" : chars.toString());
                        trip.itinTravelPoint.setPointsPending(cleanChars);
                    } else if (localName.equalsIgnoreCase("TotalPoints")) {
                        String cleanChars = (chars == null ? "" : chars.toString());
                        trip.itinTravelPoint.setTotalPoints(cleanChars);
                    } else if (localName.equalsIgnoreCase("TravelPoints")) {
                        inItinTravelPoint = false;
                    }
                } else {
                    // Handle the itin data elements. Note that unlike
                    // everything else we do not
                    // track being in the itinerary so the closing out of the
                    // element (and its
                    // addition to the trip collection) all occurs in this
                    // handling routine.
                    handleItineraryElement(localName);
                }

                chars.setLength(0);
            }
        }

        /**
         * Handle the itinerary level elements
         * 
         * @param localName
         */
        private void handleItineraryElement(String localName) {

            final String cleanChars = chars.toString().trim();
            if (localName.equalsIgnoreCase("ItinLocator")) {
                trip.itinLocator = cleanChars;
            } else if (localName.equalsIgnoreCase("ClientLocator")) {
                trip.clientLocator = cleanChars;
            } else if (localName.equalsIgnoreCase("CliqbookTripId")) {
                trip.cliqbookTripId = cleanChars;
            } else if (localName.equalsIgnoreCase("TripName")) {
                trip.name = cleanChars;
            } else if (localName.equalsIgnoreCase("Description")) {
                trip.desc = cleanChars;
            } else if (localName.equalsIgnoreCase("Comments")) {
                trip.comment = cleanChars;
            } else if (localName.equalsIgnoreCase("StartDateUtc")) {
                trip.startUtc = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("EndDateUtc")) {
                trip.endUtc = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("StartDateLocal")) {
                trip.startLocal = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("EndDateLocal")) {
                trip.endLocal = Parse.parseXMLTimestamp(cleanChars);
            } else if (localName.equalsIgnoreCase("State")) {
                trip.state = Parse.safeParseInteger(cleanChars);
            } else if (localName.equalsIgnoreCase("AuthorizationNumber")) {
                trip.authNumber = cleanChars;
            } else if (!inBooking && !inTripListItinerary && localName.equalsIgnoreCase("RecordLocator")) {
                // MOB-14267 the record locator which needs to be displayed in the segment list header
                trip.recordLocatorForAgent = cleanChars;
            } else if (handledAdditionalItinElement(localName)) {
                // continue;
            } else if (localName.equalsIgnoreCase(ITINERARY) || localName.equalsIgnoreCase(ITINERARY_SUMMARY)
                    || localName.equalsIgnoreCase(TRIP_LIST_ITINERARY)) {
                trips.add(trip);
                if (localName.equalsIgnoreCase(TRIP_LIST_ITINERARY)) {
                    inTripListItinerary = false;
                }
            }
        }

        /**
         * Handle the additional Itinerary level end elements
         * 
         * @param localName
         * @return boolean - true if handled else false
         */
        private boolean handledAdditionalItinElement(String localName) {
            final String cleanChars = chars.toString().trim();
            boolean handled = true;
            if (localName.equalsIgnoreCase("ApprovalStatus")) {
                trip.approvalStatusEnum = ApprovalStatusEnum.valueOf(cleanChars);
            } else if (localName.equalsIgnoreCase("ApproverId")) {
                trip.approverId = cleanChars;
            } else if (localName.equalsIgnoreCase("ApproverName")) {
                trip.approverName = cleanChars;
            } else if (localName.equalsIgnoreCase("BookedVia")) {
                trip.bookedVia = cleanChars;
            } else if (localName.equalsIgnoreCase("BookingSource")) {
                trip.bookingSource = cleanChars;
            } else if (localName.equalsIgnoreCase("CanBeExpensed")) {
                trip.canBeExpensed = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("CliqbookState")) {
                trip.cliqbookState = Parse.safeParseInteger(cleanChars);
            } else if (localName.equalsIgnoreCase("HasOthers")) {
                trip.hasOthers = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("HasTickets")) {
                trip.hasTickets = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("IsExpensed")) {
                trip.isExpensed = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("IsGdsBooking")) {
                trip.isGdsBooking = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("IsPersonal")) {
                trip.isPersonal = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("IsWithdrawn")) {
                trip.isWithdrawn = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("IsPublic")) {
                trip.isPublic = Parse.safeParseBoolean(cleanChars);
            } else if (localName.equalsIgnoreCase("ItinId")) {
                trip.itinId = safeParseIntegerDefaultToZero(cleanChars);
            } else if (localName.equalsIgnoreCase("ItinSourceList")) {
                trip.itinSourceList = cleanChars;
            } else if (localName.equalsIgnoreCase("RecordLocator")) {
                trip.recordLocatorFromXml = cleanChars;
            } else if (localName.equalsIgnoreCase("SegmentTypes")) {
                trip.segmentTypes = cleanChars;
            } else if (localName.equalsIgnoreCase("TripId")) {
                trip.tripId = safeParseIntegerDefaultToZero(cleanChars);
            } else if (localName.equalsIgnoreCase("TripKey")) {
                trip.tripKey = cleanChars;
            } else if (localName.equalsIgnoreCase("TripStateMessage")) {
                trip.tripStateMessages.add(cleanChars);
            } else if (localName.equalsIgnoreCase("TripStatus")) { // may be the existing State field?
                trip.tripStatus = safeParseIntegerDefaultToZero(cleanChars);
            } else {
                handled = false;
            }
            return handled;
        }

        /**
         * handle the rule violations start elements
         * 
         * @param localName
         * @return true if element is handled otherwise returns false
         */
        private boolean handleRuleViolationStartElement(String localName) {
            boolean handled = true;
            if (localName.equalsIgnoreCase(RULE_VIOLATIONS)) {
                inRuleViolations = true;
                trip.ruleViolations = new ArrayList<RuleViolation>();
            } else if (inRuleViolations) {
                if (localName.equalsIgnoreCase(ITINERARY)) {
                    inItinRuleViolation = true;
                    if (itinRuleViolations == null) {
                        itinRuleViolations = new ArrayList<RuleViolation>();
                        ruleViolationReason = new RuleViolationReason();
                        rules = new ArrayList<String>();
                        ruleViolationReasons = new ArrayList<RuleViolationReason>();
                    }
                    itinRuleViolation = new RuleViolation();
                } else if (localName.equalsIgnoreCase(CAR)) {
                    inCarRuleViolation = true;
                    if (carRuleViolations == null) {
                        carRuleViolations = new ArrayList<CarRuleViolation>();
                        ruleViolationReason = new RuleViolationReason();
                        rules = new ArrayList<String>();
                        ruleViolationReasons = new ArrayList<RuleViolationReason>();
                    }
                    carRuleViolation = new CarRuleViolation();
                } else if (localName.equalsIgnoreCase(HOTEL)) {
                    inHotelRuleViolation = true;
                    if (hotelRuleViolations == null) {
                        hotelRuleViolations = new ArrayList<HotelRuleViolation>();
                        ruleViolationReason = new RuleViolationReason();
                        rules = new ArrayList<String>();
                        ruleViolationReasons = new ArrayList<RuleViolationReason>();
                    }
                    hotelRuleViolation = new HotelRuleViolation();
                } else if (localName.equalsIgnoreCase(AIR)) {
                    inFlightRuleViolation = true;
                    if (flightRuleViolations == null) {
                        flightRuleViolations = new ArrayList<FlightRuleViolation>();
                        ruleViolationReason = new RuleViolationReason();
                        rules = new ArrayList<String>();
                        ruleViolationReasons = new ArrayList<RuleViolationReason>();
                    }
                    flightRuleViolation = new FlightRuleViolation();
                } else if (localName.equalsIgnoreCase(RAIL)) {
                    inRailRuleViolation = true;
                    if (railRuleViolations == null) {
                        railRuleViolations = new ArrayList<RailRuleViolation>();
                        ruleViolationReason = new RuleViolationReason();
                        rules = new ArrayList<String>();
                        ruleViolationReasons = new ArrayList<RuleViolationReason>();
                    }
                    railRuleViolation = new RailRuleViolation();
                }
            } else {
                handled = false;
            }
            return handled;
        }

        /**
         * handle the rule violations end elements
         * 
         * @param localName
         * @return true if element is handled otherwise returns false
         */
        private boolean handleRuleViolationEndElement(String localName) {
            boolean handled = true;
            if (inRuleViolations) {
                if (localName.equalsIgnoreCase("Rule")) {
                    rules.add(chars.toString().trim());
                } else if (localName.equalsIgnoreCase("Reason")) {
                    ruleViolationReason.setReasonCode(chars.toString().trim());
                } else if (localName.equalsIgnoreCase("Comments")) {
                    ruleViolationReason.setBookerComments(chars.toString().trim());
                } else if (localName.equalsIgnoreCase("ViolationReasons")) {
                    ruleViolationReasons.add(ruleViolationReason);
                } else if (inItinRuleViolation) {
                    if (localName.equalsIgnoreCase(ITINERARY)) {
                        itinRuleViolation.setSegmentType(ITINERARY);
                        itinRuleViolation.setRules(rules);
                        itinRuleViolation.setViolationReasons(ruleViolationReasons);
                        trip.ruleViolations.add(itinRuleViolation);
                        inItinRuleViolation = false;
                    }
                } else if (inCarRuleViolation) {
                    if (localName.equalsIgnoreCase(CAR)) {
                        carRuleViolation.setSegmentType(CAR);
                        carRuleViolation.setRules(rules);
                        carRuleViolation.setViolationReasons(ruleViolationReasons);
                        trip.ruleViolations.add(carRuleViolation);
                        inCarRuleViolation = false;
                    } else if (localName.equalsIgnoreCase("DailyRate")) {
                        carRuleViolation.setDailyRate(chars.toString().trim());
                    }
                } else if (inHotelRuleViolation) {
                    if (localName.equalsIgnoreCase(HOTEL)) {
                        hotelRuleViolation.setSegmentType(HOTEL);
                        hotelRuleViolation.setRules(rules);
                        hotelRuleViolation.setViolationReasons(ruleViolationReasons);
                        trip.ruleViolations.add(hotelRuleViolation);
                        inHotelRuleViolation = false;
                    } else if (localName.equalsIgnoreCase("Rate")) {
                        hotelRuleViolation.setRate(chars.toString().trim());
                    }
                } else if (inFlightRuleViolation) {
                    if (localName.equalsIgnoreCase(AIR)) {
                        flightRuleViolation.setSegmentType(AIR);
                        flightRuleViolation.setRules(rules);
                        flightRuleViolation.setViolationReasons(ruleViolationReasons);
                        trip.ruleViolations.add(flightRuleViolation);
                        inFlightRuleViolation = false;
                    } else if (localName.equalsIgnoreCase("Cost")) {
                        flightRuleViolation.setCost(chars.toString().trim());
                    } else if (localName.equalsIgnoreCase("Refundable")) {
                        flightRuleViolation.setRefundable(chars.toString().trim());
                    }
                } else if (inRailRuleViolation) {
                    if (localName.equalsIgnoreCase(RAIL)) {
                        railRuleViolation.setSegmentType(RAIL);
                        railRuleViolation.setRules(rules);
                        railRuleViolation.setViolationReasons(ruleViolationReasons);
                        trip.ruleViolations.add(railRuleViolation);
                        inRailRuleViolation = false;
                    } else if (localName.equalsIgnoreCase("Rate")) {
                        railRuleViolation.setRate(chars.toString().trim());
                    }
                } else if (localName.equalsIgnoreCase(RULE_VIOLATIONS)) {
                    inRuleViolations = false;
                }
            } else {
                handled = false;
            }
            return handled;
        }

        private int determineGDSId(String bookingSource) {

            int id;

            if (bookingSource.equalsIgnoreCase("Apollo")) {
                id = 1;
            } else if (bookingSource.equalsIgnoreCase("Galileo")) {
                id = 2;
            } else if (bookingSource.equalsIgnoreCase("Sabre")) {
                id = 4;
            } else if (bookingSource.equalsIgnoreCase("Farechase")) {
                id = 8;
            } else if (bookingSource.equalsIgnoreCase("Worldspan")) {
                id = 16;
            } else if (bookingSource.equalsIgnoreCase("Amadeus")) {
                id = 32;
            } else if (bookingSource.equalsIgnoreCase("Alternate")) {
                id = 64;
            } else if (bookingSource.equalsIgnoreCase("Ita")) {
                id = 128;
            } else if (bookingSource.equalsIgnoreCase("G2")) {
                id = 256;
            } else if (bookingSource.equalsIgnoreCase("Navitaire")) {
                id = 512;
            } else if (bookingSource.equalsIgnoreCase("NewSkies")) {
                id = 1024;
            } else if (bookingSource.equalsIgnoreCase("VirginBlue")) {
                id = 2048;
            } else if (bookingSource.equalsIgnoreCase("AirCanada")) {
                id = 4096;
            } else if (bookingSource.equalsIgnoreCase("DeutscheBahn")) {
                id = 8192;
            } else if (bookingSource.equalsIgnoreCase("SNCF")) {
                id = 16384;
            } else if (bookingSource.equalsIgnoreCase("Rail1")) {
                id = 32768;
            } else if (bookingSource.equalsIgnoreCase("Pegasus")) {
                id = 131072;
            } else {
                id = 268435456;
            }

            return id;
        }
    }

    /**
     * Gets an instance of <code>ITripAnalyzer</code> used to analyze a trip for selecting possible dates and locations for
     * prospective lodging/transport bookings.
     * 
     * @return an instance of <code>ITripAnalyzer</code> for querying for potential lodging/transport dates/locations.
     */
    public static ITripAnalyzer getTripAnalyzer() {
        return new SimpleTripAnalyzer();
    }

    public boolean isEntireTripCancelled() {
        return entireTripCancelled;
    }

    public void setEntireTripCancelled(boolean entireTripCancelled) {
        this.entireTripCancelled = entireTripCancelled;
    }

    /**
     * Handle the scenario if MWS sends an empty string in case of int
     * 
     * @param text
     * @return
     */
    // @TODO - need to be moved to a common utils class
    public static Integer safeParseIntegerDefaultToZero(String text) {
        Integer val = Parse.safeParseInteger(text);
        if (val == null)
            val = 0;
        return val;
    }
}
