/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import android.util.SparseArray;

import com.concur.mobile.core.travel.air.data.AirSegment;
import com.concur.mobile.core.travel.car.data.CarSegment;
import com.concur.mobile.core.travel.hotel.data.HotelSegment;
import com.concur.mobile.core.travel.rail.data.RailSegment;

/**
 * An implementation of <code>ITripAnalyzer</code>.
 * 
 * This implementation examines a trip for prospective lodging and transportion dates and locations.
 * 
 * For Lodging options, this implementation examines the transportation segments (segments that cause a traveler to change
 * location) and looks for gaps between successive stop and start times that are a minimum of 8 hours. It then uses these gaps in
 * travel to suggestion lodging dates and locations.
 * 
 * For transportation options, this implementation examines the transportation segments (air, rail, car, ride) and looks for gaps
 * in city continuity, i.e., two transportation segments (one following the other in time order) where the end of one transport is
 * in a different city then the start of the next transport segment.
 * 
 * For trip cities, this implementation examines all segments that contain location information and creates a unique list (set) of
 * all cities.
 * 
 * @author AndrewK
 */
class SimpleTripAnalyzer implements ITripAnalyzer {

    private static final long HOURS_8_MILLISECOND = (1000 * 60 * 60 * 8);

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.ITripAnalyzer#findLodgeSuggestions(com.concur.mobile.data.travel.Trip)
     */
    public List<LodgeSearchSuggestion> findHotelSuggestions(Trip trip) {

        ArrayList<LodgeSearchSuggestion> lodgeSuggestions = new ArrayList<LodgeSearchSuggestion>();

        // First, obtain all the transport (air, rail, car, ride) segments and place them into order based on start times UTC.
        ArrayList<Segment> transportSegments = new ArrayList<Segment>();
        ArrayList<AirSegment> airSegs = trip.getSegmentsByType(Segment.SegmentType.AIR, 0);
        if (airSegs != null) {
            transportSegments.addAll(airSegs);
        }
        ArrayList<RailSegment> railSegs = trip.getSegmentsByType(Segment.SegmentType.RAIL, 0);
        if (railSegs != null) {
            transportSegments.addAll(railSegs);
        }
        ArrayList<CarSegment> carSegs = trip.getSegmentsByType(Segment.SegmentType.CAR, 0);
        if (carSegs != null) {
            transportSegments.addAll(carSegs);
        }
        ArrayList<RideSegment> rideSegs = trip.getSegmentsByType(Segment.SegmentType.RIDE, 0);
        if (rideSegs != null) {
            transportSegments.addAll(rideSegs);
        }
        SegmentStartDateUtcComparator segmentComparator = new SegmentStartDateUtcComparator();
        Collections.sort(transportSegments, segmentComparator);
        // Second, obtain the current list of hotel segments and also place them in order based on start date.
        ArrayList<HotelSegment> hotelSegs = trip.getSegmentsByType(Segment.SegmentType.HOTEL, 0);
        if (hotelSegs != null) {
            Collections.sort(hotelSegs, segmentComparator);
        }
        // Second, iterate through the transport segments and look for gaps greater than 8 hours. Then, check for any existing
        // hotel segments
        // that fall within those gaps and if none, then suggest the gap as a potential hotel location/check-in/out.
        Segment prevSeg = null;
        long curTimeMillis = System.currentTimeMillis();
        for (Segment curSeg : transportSegments) {
            if (!isCircularTransport(curSeg)) {
                if (prevSeg != null) {
                    Calendar prevSegEndDateUtc = prevSeg.getEndDateUtc();
                    Calendar curSegStartDateUtc = curSeg.getStartDateUtc();
                    if (curSegStartDateUtc.equals(prevSegEndDateUtc) || curSegStartDateUtc.after(prevSegEndDateUtc)) {
                        long curSegStartDateUtcMillis = curSegStartDateUtc.getTimeInMillis();
                        long prevSegEndDateUtcMillis = prevSegEndDateUtc.getTimeInMillis();
                        // Check for a difference of 8 hours or more and current time before or in this 8+ hour window.
                        if ((curSegStartDateUtcMillis - prevSegEndDateUtcMillis) >= HOURS_8_MILLISECOND
                                && (curTimeMillis < curSegStartDateUtcMillis)) {
                            // Determine whether we already have a hotel segment that covers this >= 8 hour gap.
                            // If not, then add it as a lodging suggestion.
                            if (hotelSegs != null && hotelSegs.size() > 0) {
                                boolean foundHotelSegment = false;
                                for (Segment hotelSegment : hotelSegs) {
                                    // NOTE: The check for hotel instance just checks whether a hotel has already been booked
                                    // in the same location within this 8+ hour period. It does not look for hotel gaps within
                                    // this
                                    // 8+ hour period.
                                    // Sanity check that the hotel segment has a non-null UTC start date.
                                    if (hotelSegment.getStartDateUtc() != null) {
                                        long hotelStartDateUtcMillis = hotelSegment.getStartDateUtc().getTimeInMillis();
                                        if (hotelStartDateUtcMillis >= prevSegEndDateUtcMillis
                                                && hotelStartDateUtcMillis <= curSegStartDateUtcMillis) {
                                            foundHotelSegment = true;
                                            break;
                                        }
                                    }
                                }
                                if (!foundHotelSegment) {
                                    lodgeSuggestions.add(createLodgeSearchSuggestion(prevSeg, curSeg));
                                }
                            } else {
                                lodgeSuggestions.add(createLodgeSearchSuggestion(prevSeg, curSeg));
                            }
                        }
                    }
                }
                prevSeg = curSeg;
            }
        }

        // Third, look at any car segments and if a car's pick-up and drop-off is the same city, then determine if a lodge
        // suggestion
        // has already been created for this location.
        if (carSegs != null && carSegs.size() > 0) {
            for (CarSegment carSeg : carSegs) {
                // Determine if pick-up/drop-off is same city, whether airport or not.
                String pickUpDropOffCity = null;
                String pickUpDropOffState = null;
                String pickUpDropOffCountry = null;
                if (carSeg.startAirportCity != null && carSeg.endAirportCity != null
                        && carSeg.startAirportCity.equalsIgnoreCase(carSeg.endAirportCity)) {
                    pickUpDropOffCity = carSeg.startAirportCity;
                    pickUpDropOffState = carSeg.startAirportState;
                    pickUpDropOffCountry = carSeg.startAirportCountry;
                } else if (carSeg.startCity != null && carSeg.endCity != null
                        && carSeg.startCity.equalsIgnoreCase(carSeg.endCity)) {
                    pickUpDropOffCity = carSeg.startCity;
                    pickUpDropOffState = carSeg.startState;
                    pickUpDropOffCountry = carSeg.startCountry;
                }
                if (pickUpDropOffCity != null) {
                    // Longer than 8 hour period?
                    if ((carSeg.endDateUtc.getTimeInMillis() - carSeg.startDateUtc.getTimeInMillis()) >= HOURS_8_MILLISECOND) {
                        // Already suggested in above transport segment analysis?
                        boolean foundHotelSegmentForCarPeriod = false;
                        for (HotelSegment hotelSeg : hotelSegs) {
                            // Sanity check that hotelSeg has non-null start/end day local values.
                            if (hotelSeg.startDayLocal != null && hotelSeg.endDayLocal != null) {
                                // Determine if a hotel check-in/check-out falls within the car rental time period.
                                if ((hotelSeg.startDayLocal.equals(carSeg.startDayLocal) || hotelSeg.startDayLocal
                                        .after(carSeg.startDayLocal))
                                        && (hotelSeg.endDayLocal.equals(carSeg.endDayLocal) || hotelSeg.endDayLocal
                                                .before(carSeg.endDayLocal))) {
                                    foundHotelSegmentForCarPeriod = true;
                                    break;
                                }
                            }
                        }
                        if (!foundHotelSegmentForCarPeriod) {
                            // Verify an existing lodging suggestion doesn't already cover this location/time period.
                            boolean foundLodgeSuggestion = false;
                            for (LodgeSearchSuggestion lodgeSuggestion : lodgeSuggestions) {
                                if (lodgeSuggestion.checkInDay != null
                                        && lodgeSuggestion.checkInDay.equals(carSeg.startDayLocal)
                                        && lodgeSuggestion.checkOutDay != null
                                        && lodgeSuggestion.checkOutDay.equals(carSeg.endDayLocal)
                                        && lodgeSuggestion.city != null
                                        && lodgeSuggestion.city.equalsIgnoreCase(pickUpDropOffCity)
                                        && lodgeSuggestion.country != null
                                        && lodgeSuggestion.country.equalsIgnoreCase(pickUpDropOffCountry)) {
                                    foundLodgeSuggestion = true;
                                    break;
                                }
                            }
                            // Does a lodge search suggestion already exist?
                            if (!foundLodgeSuggestion) {
                                LodgeSearchSuggestion suggestion = new LodgeSearchSuggestion();
                                suggestion.checkInDate = carSeg.startDateLocal;
                                suggestion.checkInDay = carSeg.startDayLocal;
                                suggestion.checkOutDate = carSeg.endDateLocal;
                                suggestion.checkOutDay = carSeg.endDayLocal;
                                suggestion.city = pickUpDropOffCity;
                                suggestion.state = pickUpDropOffState;
                                suggestion.country = pickUpDropOffCountry;
                                lodgeSuggestions.add(suggestion);
                            }
                        }
                    }
                }
            }
        }
        return lodgeSuggestions;
    }

    /**
     * Determines whether or not the transport segment starts and ends with the same city.
     * 
     * @param segment
     *            the transport segment.
     * @return <code>true</code> if <code>segment</code> starts and ends in the same location.
     */
    private boolean isCircularTransport(Segment segment) {
        boolean retVal = false;

        if (segment instanceof CarSegment) {
            CarSegment carSegment = (CarSegment) segment;
            if (carSegment.startAirportCity != null && carSegment.startAirportCity.length() > 0
                    && carSegment.endAirportCity != null && carSegment.endAirportCity.length() > 0) {
                retVal = carSegment.startAirportCity.equalsIgnoreCase(carSegment.endAirportCity);
            } else if (carSegment.startCity != null && carSegment.startCity.length() > 0 && carSegment.endCity != null
                    && carSegment.endCity.length() > 0) {
                retVal = carSegment.startCity.equalsIgnoreCase(carSegment.endCity);
            } else if (carSegment.startAirportCity != null && carSegment.startAirportCity.length() > 0
                    && carSegment.endCity != null && carSegment.endCity.length() > 0) {
                retVal = carSegment.startAirportCity.equalsIgnoreCase(carSegment.endCity);
            } else if (carSegment.startCity != null && carSegment.startCity.length() > 0
                    && carSegment.endAirportCity != null && carSegment.endAirportCity.length() > 0) {
                retVal = carSegment.startCity.equalsIgnoreCase(carSegment.endAirportCity);
            }
        } else if (segment instanceof RideSegment) {
            RideSegment rideSegment = (RideSegment) segment;
            retVal = (rideSegment.startCity != null && rideSegment.endCity != null && rideSegment.startCity
                    .equalsIgnoreCase(rideSegment.endCity));
        }

        return retVal;
    }

    /**
     * Creates a lodging search suggestion based on examining two segments ordered by start date utc.
     * 
     * @param prevSeg
     *            the previous segment.
     * @param curSeg
     *            the current segment.
     * @return an instance of <code>LodgeSearchSuggestion</code>.
     */
    private LodgeSearchSuggestion createLodgeSearchSuggestion(Segment prevSeg, Segment curSeg) {
        LodgeSearchSuggestion suggestion = new LodgeSearchSuggestion();
        suggestion.checkInDate = prevSeg.endDateLocal;
        suggestion.checkInDay = prevSeg.endDayLocal;
        suggestion.checkOutDate = curSeg.startDateLocal;
        suggestion.checkOutDay = curSeg.startDayLocal;
        suggestion.city = prevSeg.getEndCity();
        suggestion.state = prevSeg.getEndState();
        suggestion.country = prevSeg.getEndCountry();
        // Rail segments only contain rail station code and not city, state and country information.
        // So, we'll set the flag that rail station information is required.
        if (prevSeg instanceof RailSegment) {
            suggestion.requireRailStationInformation = true;
            suggestion.railStationCode = ((RailSegment) prevSeg).endRailStation;
        }
        return suggestion;
    }

    /**
     * Creates a city search suggestion based on examining end city, state and country information in <code>seg</code>.
     * 
     * @param seg
     *            the segment.
     * @param useAirportName
     *            whether to use the airport name or the city name for the display text
     * @return an instance of <code>CitySearchSuggestion</code>.
     */
    private CitySearchSuggestion createCitySearchSuggestion(Segment seg, boolean useAirportName) {
        CitySearchSuggestion suggestion = new CitySearchSuggestion();
        suggestion.city = seg.getEndCity();
        suggestion.state = seg.getEndState();
        suggestion.country = seg.getEndCountry();
        // Rail segments only contain rail station code and not city, state and country information.
        // So, we'll set the flag that rail station information is required.
        if (seg instanceof RailSegment) {
            suggestion.requireRailStationInformation = true;
            suggestion.railStationCode = ((RailSegment) seg).endRailStation;
        }
        // If this is an AirSegment, we want to use the airport name instead of city name.
        if (seg instanceof AirSegment && useAirportName) {
            suggestion.city = ((AirSegment) seg).endAirportName;
        }

        return suggestion;
    }

    /**
     * Creates a transport search suggestion based on examining two segments ordered by start date utc.
     * 
     * @param prevSeg
     *            the previous segment.
     * @param curSeg
     *            the current segment.
     * @return an instance of <code>TransportSearchSuggestion</code>.
     */
    private TransportSearchSuggestion createTransportSearchSuggestion(Segment prevSeg, Segment curSeg,
            boolean useAirportName) {
        TransportSearchSuggestion suggestion = new TransportSearchSuggestion();

        // Arrival city information.
        suggestion.arrivalCity = new CitySearchSuggestion();
        suggestion.arrivalCity.city = curSeg.getEndCity();
        suggestion.arrivalCity.state = curSeg.getEndState();
        suggestion.arrivalCity.country = curSeg.getEndCountry();
        if (curSeg instanceof RailSegment) {
            suggestion.arrivalCity.requireRailStationInformation = true;
            suggestion.arrivalCity.railStationCode = ((RailSegment) curSeg).endRailStation;
        }
        // If this is an AirSegment, we want to use the airport name instead of city name.
        if (curSeg instanceof AirSegment && useAirportName) {
            suggestion.arrivalCity.city = ((AirSegment) curSeg).endAirportName;
            // airport iata code needed for Car search
            suggestion.arrivalCity.iataCode = ((AirSegment) curSeg).endCityCode;
        }

        // Departure city information.
        suggestion.departureCity = new CitySearchSuggestion();
        suggestion.departureCity.city = prevSeg.getEndCity();
        suggestion.departureCity.state = prevSeg.getEndState();
        suggestion.departureCity.country = prevSeg.getEndCountry();
        if (prevSeg instanceof RailSegment) {
            suggestion.departureCity.requireRailStationInformation = true;
            suggestion.departureCity.railStationCode = ((RailSegment) prevSeg).endRailStation;
        }
        // If this is an AirSegment, we want to use the airport name instead of city name.
        if (prevSeg instanceof AirSegment && useAirportName) {
            suggestion.departureCity.city = ((AirSegment) prevSeg).endAirportName;
            // airport iata code needed for Car search
            suggestion.departureCity.iataCode = ((AirSegment) prevSeg).endCityCode;
        }

        // Set the pick-up/drop-off dates.
        // NOTE: The pick-up/drop-off time of day is not currently specified, only the days themselves.
        suggestion.arrivalDate = curSeg.startDateLocal;
        suggestion.arrivalDay = curSeg.startDayLocal;
        suggestion.departureDate = prevSeg.endDateLocal;
        suggestion.departureDay = prevSeg.endDayLocal;

        return suggestion;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.ITripAnalyzer#findTransportSuggestions(com.concur.mobile.data.travel.Trip)
     */
    public List<TransportSearchSuggestion> findCarSuggestions(Trip trip) {

        ArrayList<TransportSearchSuggestion> transportSuggestions = new ArrayList<TransportSearchSuggestion>();

        // First, obtain all the transport (air, rail, ride) segments and place them into order based on start times UTC.
        ArrayList<Segment> transportSegments = new ArrayList<Segment>();
        ArrayList<AirSegment> airSegs = trip.getSegmentsByType(Segment.SegmentType.AIR, 0);
        if (airSegs != null) {
            transportSegments.addAll(airSegs);
        }
        ArrayList<RailSegment> railSegs = trip.getSegmentsByType(Segment.SegmentType.RAIL, 0);
        if (railSegs != null) {
            transportSegments.addAll(railSegs);
        }
        ArrayList<RideSegment> rideSegs = trip.getSegmentsByType(Segment.SegmentType.RIDE, 0);
        if (rideSegs != null) {
            transportSegments.addAll(rideSegs);
        }
        SegmentStartDateUtcComparator segmentComparator = new SegmentStartDateUtcComparator();
        Collections.sort(transportSegments, segmentComparator);
        // Second, obtain the current list of car segments and also place them in order based on start date.
        ArrayList<CarSegment> carSegs = trip.getSegmentsByType(Segment.SegmentType.CAR, 0);
        if (carSegs != null) {
            Collections.sort(carSegs, segmentComparator);
        }
        ArrayList<HotelSegment> hotelSegs = trip.getSegmentsByType(Segment.SegmentType.HOTEL, 0);
        if (hotelSegs != null) {
            Collections.sort(hotelSegs, segmentComparator);
        }
        // Second, iterate through the transport segments and look for gaps greater than 8 hours. Then, check for any existing
        // hotel segments
        // that fall within those gaps and if none, then suggest the gap as a potential hotel location/check-in/out.
        Segment prevSeg = null;
        long curTimeMillis = System.currentTimeMillis();
        for (Segment curSeg : transportSegments) {
            if (!isCircularTransport(curSeg)) {
                if (prevSeg != null) {
                    Calendar prevSegEndDateUtc = prevSeg.getEndDateUtc();
                    Calendar curSegStartDateUtc = curSeg.getStartDateUtc();
                    if (curSegStartDateUtc.equals(prevSegEndDateUtc) || curSegStartDateUtc.after(prevSegEndDateUtc)) {
                        long curSegStartDateUtcMillis = curSegStartDateUtc.getTimeInMillis();
                        long prevSegEndDateUtcMillis = prevSegEndDateUtc.getTimeInMillis();
                        // Check for a difference of 8 hours or more and current time before or in this 8+ hour window.
                        if ((curSegStartDateUtcMillis - prevSegEndDateUtcMillis) >= HOURS_8_MILLISECOND
                                && (curTimeMillis < curSegStartDateUtcMillis)) {
                            // Determine whether we already have a car segment that covers this >= 8 hour gap.
                            // If not, then add it as a transport suggestion.
                            if (carSegs != null && carSegs.size() > 0) {
                                boolean foundCarSegment = false;
                                for (Segment carSegment : carSegs) {
                                    // NOTE: The check for car instance just checks whether a car has already been booked
                                    // in the same location within this 8+ hour period. It does not look for car gaps within this
                                    // 8+ hour period.
                                    long carStartDateUtcMillis = carSegment.getStartDateUtc().getTimeInMillis();
                                    if (carStartDateUtcMillis >= prevSegEndDateUtcMillis
                                            && carStartDateUtcMillis <= curSegStartDateUtcMillis) {
                                        foundCarSegment = true;
                                        break;
                                    }
                                }
                                if (!foundCarSegment) {
                                    transportSuggestions.add(createTransportSearchSuggestion(prevSeg, curSeg, true));
                                }
                            } else {
                                transportSuggestions.add(createTransportSearchSuggestion(prevSeg, curSeg, true));
                            }
                        }
                    }
                }
                prevSeg = curSeg;
            }
        }
        // Third, look at any hotel segments as a suggestion for a possible car rental period.
        if (hotelSegs != null && hotelSegs.size() > 0) {
            for (HotelSegment hotelSeg : hotelSegs) {

                // Already found an existing car segment for 'hotelSeg'?
                boolean foundCarSegmentForHotelPeriod = false;
                for (CarSegment carSeg : carSegs) {
                    // Determine if a car pick-up/drop-off falls within the hotel rental time period.
                    // Sanity check that hotel start/end day local values are non-null.
                    if (hotelSeg.startDayLocal != null && hotelSeg.endDayLocal != null && carSeg.startDayLocal != null
                            && carSeg.endDayLocal != null) {
                        if ((carSeg.startDayLocal.equals(hotelSeg.startDayLocal) || carSeg.startDayLocal
                                .after(hotelSeg.startDayLocal))
                                && (carSeg.endDayLocal.equals(hotelSeg.endDayLocal) || carSeg.endDayLocal
                                        .before(hotelSeg.endDayLocal))) {
                            foundCarSegmentForHotelPeriod = true;
                            break;
                        }
                    }
                }
                if (!foundCarSegmentForHotelPeriod) {
                    // Verify an existing transport suggestion doesn't already cover this time period.
                    boolean foundTransportSuggestion = false;
                    for (TransportSearchSuggestion transportSuggestion : transportSuggestions) {
                        if (transportSuggestion.departureDay.equals(hotelSeg.startDayLocal)
                                && transportSuggestion.arrivalDay.equals(hotelSeg.endDayLocal)) {
                            foundTransportSuggestion = true;
                            break;
                        }
                    }
                    // Does a lodge search suggestion already exist?
                    if (!foundTransportSuggestion) {
                        TransportSearchSuggestion suggestion = new TransportSearchSuggestion();
                        suggestion.departureDate = hotelSeg.startDateLocal;
                        suggestion.departureDay = hotelSeg.startDayLocal;
                        suggestion.arrivalDate = hotelSeg.endDateLocal;
                        suggestion.arrivalDay = hotelSeg.endDayLocal;
                        suggestion.departureCity = new CitySearchSuggestion();
                        suggestion.departureCity.city = hotelSeg.startCity;
                        suggestion.departureCity.state = hotelSeg.startState;
                        suggestion.departureCity.country = hotelSeg.startCountry;
                        suggestion.arrivalCity = suggestion.departureCity;
                        transportSuggestions.add(suggestion);
                    }
                }
            }
        }
        return transportSuggestions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.ITripAnalyzer#findTripCities(com.concur.mobile.data.travel.Trip)
     */
    public List<CitySearchSuggestion> findTripCities(Trip trip, boolean useAirportName) {
        ArrayList<CitySearchSuggestion> citySuggestions = new ArrayList<CitySearchSuggestion>();

        // First, obtain all the transport (air, rail, car, ride) segments and place them into order based on start times UTC.
        ArrayList<Segment> transportSegments = new ArrayList<Segment>();
        ArrayList<AirSegment> airSegs = trip.getSegmentsByType(Segment.SegmentType.AIR, 0);
        if (airSegs != null) {
            transportSegments.addAll(airSegs);
        }
        ArrayList<RailSegment> railSegs = trip.getSegmentsByType(Segment.SegmentType.RAIL, 0);
        if (railSegs != null) {
            transportSegments.addAll(railSegs);
        }
        ArrayList<CarSegment> carSegs = trip.getSegmentsByType(Segment.SegmentType.CAR, 0);
        if (carSegs != null) {
            transportSegments.addAll(carSegs);
        }
        ArrayList<RideSegment> rideSegs = trip.getSegmentsByType(Segment.SegmentType.RIDE, 0);
        if (rideSegs != null) {
            transportSegments.addAll(rideSegs);
        }
        SegmentStartDateUtcComparator segmentComparator = new SegmentStartDateUtcComparator();
        Collections.sort(transportSegments, segmentComparator);
        // Second, iterate through the transport segments and look for gaps greater than 8 hours. Then, check for any existing
        // hotel segments
        // that fall within those gaps and if none, then suggest the gap as a potential hotel location/check-in/out.
        Segment prevSeg = null;
        long curTimeMillis = System.currentTimeMillis();
        for (Segment curSeg : transportSegments) {
            if (!isCircularTransport(curSeg)) {
                if (prevSeg != null) {
                    Calendar prevSegEndDateUtc = prevSeg.getEndDateUtc();
                    Calendar curSegStartDateUtc = curSeg.getStartDateUtc();
                    if (curSegStartDateUtc.equals(prevSegEndDateUtc) || curSegStartDateUtc.after(prevSegEndDateUtc)) {
                        long curSegStartDateUtcMillis = curSegStartDateUtc.getTimeInMillis();
                        long prevSegEndDateUtcMillis = prevSegEndDateUtc.getTimeInMillis();
                        // Check for a difference of 8 hours or more and current time before or in this 8+ hour window.
                        if ((curSegStartDateUtcMillis - prevSegEndDateUtcMillis) >= HOURS_8_MILLISECOND
                                && (curTimeMillis < curSegStartDateUtcMillis)) {
                            citySuggestions.add(createCitySearchSuggestion(prevSeg, useAirportName));
                        }
                    }
                }
                prevSeg = curSeg;
            }
        }
        // Third, look at any car segments and if a car's pick-up and drop-off is the same city and the duration of the
        // rental is 8+ hours.
        if (carSegs != null && carSegs.size() > 0) {
            for (CarSegment carSeg : carSegs) {
                // Determine if pick-up/drop-off is same city, whether airport or not.
                String pickUpDropOffCity = null;
                String pickUpDropOffState = null;
                String pickUpDropOffCountry = null;
                if (carSeg.startAirportCity != null && carSeg.endAirportCity != null
                        && carSeg.startAirportCity.equalsIgnoreCase(carSeg.endAirportCity)) {
                    pickUpDropOffCity = carSeg.startAirportCity;
                    pickUpDropOffState = carSeg.startAirportState;
                    pickUpDropOffCountry = carSeg.startAirportCountry;
                } else if (carSeg.startCity != null && carSeg.endCity != null
                        && carSeg.startCity.equalsIgnoreCase(carSeg.endCity)) {
                    pickUpDropOffCity = carSeg.startCity;
                    pickUpDropOffState = carSeg.startState;
                    pickUpDropOffCountry = carSeg.startCountry;
                }
                if (pickUpDropOffCity != null) {
                    // Longer than 8 hour period?
                    if ((carSeg.endDateUtc.getTimeInMillis() - carSeg.startDateUtc.getTimeInMillis()) >= HOURS_8_MILLISECOND) {
                        boolean foundCitySuggestion = false;
                        for (CitySearchSuggestion citySuggestion : citySuggestions) {
                            // Only compare city and country. Cities within foreign countries outside of any region may not have
                            // a state.
                            if (citySuggestion.city != null && pickUpDropOffCity != null
                                    && citySuggestion.city.equalsIgnoreCase(pickUpDropOffCity)
                                    && citySuggestion.country != null && pickUpDropOffCountry != null
                                    && citySuggestion.country.equalsIgnoreCase(pickUpDropOffCountry)) {
                                foundCitySuggestion = true;
                                break;
                            }
                        }
                        if (!foundCitySuggestion) {
                            CitySearchSuggestion suggestion = new CitySearchSuggestion();
                            suggestion.city = pickUpDropOffCity;
                            suggestion.state = pickUpDropOffState;
                            suggestion.country = pickUpDropOffCountry;
                            citySuggestions.add(suggestion);
                        }
                    }
                }
            }
        }

        // Fourth, look at any hotel reservations.
        ArrayList<HotelSegment> hotelSegs = trip.getSegmentsByType(Segment.SegmentType.HOTEL, 0);
        if (hotelSegs != null) {
            // Sort the segments.
            Collections.sort(hotelSegs, segmentComparator);
            for (HotelSegment hotelSeg : hotelSegs) {
                // Do we already have a city suggestion?
                boolean foundCitySuggestion = false;
                for (CitySearchSuggestion citySuggestion : citySuggestions) {
                    if (citySuggestion.city != null && hotelSeg.startCity != null
                            && citySuggestion.city.equalsIgnoreCase(hotelSeg.startCity)
                            && citySuggestion.country != null && hotelSeg.startCountry != null
                            && citySuggestion.country.equalsIgnoreCase(hotelSeg.startCountry)) {
                        foundCitySuggestion = true;
                        break;
                    }
                }
                if (!foundCitySuggestion) {
                    CitySearchSuggestion suggestion = new CitySearchSuggestion();
                    suggestion.city = hotelSeg.startCity;
                    suggestion.state = hotelSeg.startState;
                    suggestion.country = hotelSeg.startCountry;
                    citySuggestions.add(suggestion);
                }
            }
        }
        return citySuggestions;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.ITripAnalyzer#findTrips(java.util.List, java.util.Calendar)
     */
    public List<Trip> findTrips(List<Trip> trips, Calendar localDay) {
        ArrayList<Trip> candidateTrips = null;

        if (trips != null && trips.size() > 0) {
            // Add to 'candidateTrips' those trips whose end date is after 'localDay'.
            for (Trip trip : trips) {
                if (trip.endLocal.after(localDay)) {
                    if (candidateTrips == null) {
                        candidateTrips = new ArrayList<Trip>();
                    }
                    candidateTrips.add(trip);
                }
            }
            // If 'candidateTrips' is populated, then sort by 'endLocal'.
            if (candidateTrips != null) {
                Collections.sort(candidateTrips, new TripEndDateLocalComparator());
            }
        }
        return candidateTrips;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.ITripAnalyzer#getDefaultHotelSearchSuggestion(com.concur.mobile.data.travel.Trip)
     */
    public LodgeSearchSuggestion findHotelSearchSuggestionForFlight(Trip trip) {
        // MOB-13546 - Add hotel to my flight
        Segment prevSeg;
        Segment currSeg;
        LodgeSearchSuggestion hotelSearchSuggestion = null;

        // get the Air segments for creating a search suggestion
        SparseArray<Segment> segMap = getSegmentsForSearchSuggestion(trip, Segment.SegmentType.AIR);
        if (segMap.size() > 0) {

            // get the first segment
            prevSeg = segMap.get(1);

            if (segMap.size() == 1) {
                // for only one segment (or leg)
                hotelSearchSuggestion = createLodgeSearchSuggestion(prevSeg);
            } else {
                // get the second segment
                currSeg = segMap.get(2);
                hotelSearchSuggestion = createLodgeSearchSuggestion(prevSeg, currSeg);
            }
        }

        return hotelSearchSuggestion;
    }

    /**
     * Creates a hotel search suggestion defaulting to the given segment
     */
    private LodgeSearchSuggestion createLodgeSearchSuggestion(Segment seg) {
        LodgeSearchSuggestion suggestion = new LodgeSearchSuggestion();

        suggestion.checkInDate = seg.endDateLocal;

        // Make check-out one day beyond check-in.
        Calendar checkOutDate = (Calendar) suggestion.checkInDate.clone();
        checkOutDate.add(Calendar.DAY_OF_MONTH, 1);
        suggestion.checkOutDate = checkOutDate;

        suggestion.city = seg.getEndCity();
        suggestion.state = seg.getEndState();
        suggestion.country = seg.getEndCountry();

        return suggestion;
    }

    // common method to retrieve the segments that to be used for the search suggestion
    // TODO need to be refactored when other segments are handled in future
    private SparseArray<Segment> getSegmentsForSearchSuggestion(Trip trip, Segment.SegmentType segType) {
        SparseArray<Segment> segMap = new SparseArray<Segment>(1);
        // get the air segments in the trip
        if (segType == Segment.SegmentType.AIR) {
            ArrayList<AirSegment> airSegs = trip.getSegmentsByType(Segment.SegmentType.AIR, 0);
            if (airSegs != null) {
                if (airSegs.size() == 1) {
                    // only one segment exists
                    AirSegment currSeg = (AirSegment) airSegs.get(0);
                    segMap.put(1, currSeg);
                } else {
                    // more than one segment hence, place the air segments into order based on start times UTC.
                    SegmentStartDateUtcComparator segmentComparator = new SegmentStartDateUtcComparator();
                    Collections.sort(airSegs, segmentComparator);

                    AirSegment prevSeg = null;
                    Calendar prevSegEndDateUtc = null;
                    Calendar curSegStartDateUtc = null;
                    long curTimeMillis = System.currentTimeMillis();
                    boolean onlyOneSegmentLeg = false;
                    for (AirSegment currSeg : airSegs) {
                        if (prevSeg != null) {
                            // commented the below leg id logic as for air segments with different carriers, the leg ids are
                            // different in same bound
                            // if the segments have same legId then they are in the same bound (i.e either in-bound or out-bound)
                            // if (prevSeg.legId != null && currSeg.legId != null) {
                            // if (prevSeg.legId != currSeg.legId) {
                            // // this current segment is in-bound, so a potential suggestion
                            // segMap.put(1, prevSeg);
                            // segMap.put(2, currSeg);
                            // onlyOneSegmentLeg = false;
                            // break;
                            // }
                            // onlyOneSegmentLeg = true;
                            // } else {
                            // segments do not have legIds, hence look for gaps greater than 8 hours, then suggest the gap as
                            // a potential suggestion
                            prevSegEndDateUtc = prevSeg.getEndDateUtc();
                            curSegStartDateUtc = currSeg.getStartDateUtc();
                            if (curSegStartDateUtc.equals(prevSegEndDateUtc)
                                    || curSegStartDateUtc.after(prevSegEndDateUtc)) {
                                long curSegStartDateUtcMillis = curSegStartDateUtc.getTimeInMillis();
                                long prevSegEndDateUtcMillis = prevSegEndDateUtc.getTimeInMillis();
                                // Check for a difference of 8 hours or more and current time before or in this 8+ hour
                                // window.
                                if ((curSegStartDateUtcMillis - prevSegEndDateUtcMillis) >= HOURS_8_MILLISECOND
                                        && (curTimeMillis < curSegStartDateUtcMillis)) {
                                    segMap.put(1, prevSeg);
                                    segMap.put(2, currSeg);
                                    break;
                                }
                            }
                            // }
                        }
                        prevSeg = currSeg;
                    }

                    // if all the segments are in out-bound i.e no return flight
                    if (onlyOneSegmentLeg) {
                        segMap.put(1, prevSeg);
                    }
                }
            }
        }
        return segMap;
    }

    // MOB-13547 - Add Car to my flight
    public TransportSearchSuggestion findCarSearchSuggestionForFlight(Trip trip) {
        Segment prevSeg;
        Segment currSeg;
        TransportSearchSuggestion carSearchSuggestion = null;

        // get the Air segments for creating a search suggestion
        SparseArray<Segment> segMap = getSegmentsForSearchSuggestion(trip, Segment.SegmentType.AIR);
        if (segMap.size() > 0) {

            // get the first segment
            prevSeg = segMap.get(1);

            if (segMap.size() == 1) {
                // for only one segment (or leg) send the same segment as currSeg
                currSeg = prevSeg;
            } else {
                // get the second segment
                currSeg = segMap.get(2);
            }

            // create a Car search suggestion
            carSearchSuggestion = createTransportSearchSuggestion(prevSeg, currSeg, true);
        }

        return carSearchSuggestion;
    }
}
