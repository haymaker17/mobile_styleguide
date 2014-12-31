package com.concur.mobile.core.travel.air.data;

import java.util.Calendar;

import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.platform.util.Parse;

/**
 * @author AndrewK
 * 
 */
public class AirSegment extends Segment {

    public FlightStatusInfo flightStatus;

    /**
     * Default constructor does nothing but set the type
     */
    public AirSegment() {
        type = SegmentType.AIR;
        updatedByFlightStats = false;
    }

    public boolean updatedByFlightStats;

    public String aircraftCode;
    public String aircraftName;
    public String cabin;
    public String checkedBaggage;
    public String classOfService;
    public String classOfServiceLocalized;
    public Integer duration;
    public String endAirportCity;
    public String endAirportCountry;
    public String endAirportCountryCode;
    public String endAirportName;
    public String endAirportState;
    public String endGate;
    public String endTerminal;
    public String fareBasisCode;
    public String flightNumber;
    public Integer legId;
    public String meals;
    public Integer miles;
    public Integer numStops;
    public String operatedByFlightNumber;
    public String specialInstructions;
    public String startAirportCity;
    public String startAirportCountry;
    public String startAirportCountryCode;
    public String startAirportName;
    public String startAirportState;
    public String startGate;
    public String startTerminal;

    public String seat;

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.Segment#getEndCity()
     */
    @Override
    public String getEndCity() {
        return endAirportCity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.Segment#getEndState()
     */
    @Override
    public String getEndState() {
        return endAirportState;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.Segment#getEndCountry()
     */
    @Override
    public String getEndCountry() {
        return endAirportCountry;
    }

    public boolean hasFlightStats() {
        return (flightStatus != null && (flightStatus.cliqbookMessage == null || flightStatus.cliqbookMessage.trim()
                .length() == 0));
    }

    public void mergeFlightStats() {
        if (hasFlightStats()) {
            // We have something. Merge it in.
            updatedByFlightStats = true;
            FlightStatusInfo fs = flightStatus;

            if (fs.arrivalActual != null) {
                endDateLocal = fs.arrivalActual;
            } else if (fs.arrivalEstimated != null) {
                endDateLocal = fs.arrivalEstimated;
            }

            if (fs.arrivalGate != null) {
                endGate = fs.arrivalGate;
            }

            if (fs.arrivalTerminalActual != null) {
                endTerminal = fs.arrivalTerminalActual;
            } else if (fs.arrivalTerminalScheduled != null) {
                endTerminal = fs.arrivalTerminalScheduled;
            }

            if (fs.departureActual != null) {
                startDateLocal = fs.departureActual;
            } else if (fs.departureEstimated != null) {
                startDateLocal = fs.departureEstimated;
            }

            if (fs.departureGate != null) {
                startGate = fs.departureGate;
            }

            if (fs.departureTerminalActual != null) {
                startTerminal = fs.departureTerminalActual;
            } else if (fs.departureTerminalScheduled != null) {
                startTerminal = fs.departureTerminalScheduled;
            }

            if (fs.equipmentActual != null) {
                aircraftCode = fs.equipmentActual;
                aircraftName = fs.equipmentActual;
            } else if (fs.equipmentScheduled != null) {
                aircraftCode = fs.equipmentScheduled;
                aircraftName = fs.equipmentScheduled;
            }
        }
    }

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("AircraftCode")) {
                aircraftCode = chars;
            } else if (localName.equalsIgnoreCase("AircraftName")) {
                aircraftName = chars;
            } else if (localName.equalsIgnoreCase("Cabin")) {
                cabin = chars;
            } else if (localName.equalsIgnoreCase("CheckedBaggage")) {
                checkedBaggage = chars;
            } else if (localName.equalsIgnoreCase("ClassOfService")) {
                classOfService = chars;
            } else if (localName.equalsIgnoreCase("ClassOfServiceLocalized")) {
                classOfServiceLocalized = chars;
            } else if (localName.equalsIgnoreCase("Duration")) {
                duration = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("EndAirportCity")) {
                endAirportCity = chars;
            } else if (localName.equalsIgnoreCase("EndAirportCountry")) {
                endAirportCountry = chars;
            } else if (localName.equalsIgnoreCase("EndAirportCountryCode")) {
                endAirportCountryCode = chars;
            } else if (localName.equalsIgnoreCase("EndAirportName")) {
                endAirportName = chars;
            } else if (localName.equalsIgnoreCase("EndAirportState")) {
                endAirportState = chars;
            } else if (localName.equalsIgnoreCase("EndGate")) {
                endGate = chars;
            } else if (localName.equalsIgnoreCase("EndTerminal")) {
                endTerminal = chars;
            } else if (localName.equalsIgnoreCase("FareBasisCode")) {
                fareBasisCode = chars;
            } else if (localName.equalsIgnoreCase("FlightNumber")) {
                flightNumber = chars;
            } else if (localName.equalsIgnoreCase("LegId")) {
                legId = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("Meals")) {
                meals = chars;
            } else if (localName.equalsIgnoreCase("Miles")) {
                miles = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("NumStops")) {
                numStops = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("OperatedByFlightNumber")) {
                operatedByFlightNumber = chars;
            } else if (localName.equalsIgnoreCase("SpecialInstructions")) {
                specialInstructions = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCity")) {
                startAirportCity = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCountry")) {
                startAirportCountry = chars;
            } else if (localName.equalsIgnoreCase("StartAirportCountryCode")) {
                startAirportCountryCode = chars;
            } else if (localName.equalsIgnoreCase("StartAirportName")) {
                startAirportName = chars;
            } else if (localName.equalsIgnoreCase("StartAirportState")) {
                startAirportState = chars;
            } else if (localName.equalsIgnoreCase("StartGate")) {
                startGate = chars;
            } else if (localName.equalsIgnoreCase("StartTerminal")) {
                startTerminal = chars;
            } else if (localName.equalsIgnoreCase("SeatNumber")) {
                if (seat == null) {
                    seat = chars;
                } else {
                    StringBuilder sb = new StringBuilder(seat).append(", ").append(chars);
                    seat = sb.toString();
                }
            }
        }

        return true;
    }

    public class FlightStatusInfo {

        public String equipmentScheduled;
        public String equipmentActual;
        public String equipmentRegistration;

        public String departureTerminalScheduled;
        public String departureTerminalActual;
        public String departureGate;
        public Calendar departureScheduled;
        public Calendar departureEstimated;
        public Calendar departureActual;
        public String departureStatusReason;
        public String departureShortStatus;
        public String departureLongStatus;

        public String arrivalTerminalScheduled;
        public String arrivalTerminalActual;
        public String arrivalGate;
        public Calendar arrivalScheduled;
        public Calendar arrivalEstimated;
        public Calendar arrivalActual;
        public String baggageClaim;
        public String diversionCity;
        public String diversionAirport;
        public String arrivalStatusReason;
        public String arrivalShortStatus;
        public String arrivalLongStatus;

        public String cliqbookMessage;

        public Calendar lastUpdatedUTC;

        public void handleElement(String localName, String chars) {

            if (localName.equalsIgnoreCase("EquipmentScheduled")) {
                equipmentScheduled = chars;
            } else if (localName.equalsIgnoreCase("EquipmentActual")) {
                equipmentActual = chars;
            } else if (localName.equalsIgnoreCase("EquipmentRegistration")) {
                equipmentRegistration = chars;
            } else if (localName.equalsIgnoreCase("DepartureTerminalScheduled")) {
                departureTerminalScheduled = chars;
            } else if (localName.equalsIgnoreCase("DepartureTerminalActual")) {
                departureTerminalActual = chars;
            } else if (localName.equalsIgnoreCase("DepartureGate")) {
                departureGate = chars;
            } else if (localName.equalsIgnoreCase("DepartureScheduled")) {
                departureScheduled = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("DepartureEstimated")) {
                departureEstimated = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("DepartureActual")) {
                departureActual = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("DepartureStatusReason")) {
                departureStatusReason = chars;
            } else if (localName.equalsIgnoreCase("DepartureShortStatus")) {
                departureShortStatus = chars;
            } else if (localName.equalsIgnoreCase("DepartureLongStatus")) {
                departureLongStatus = chars;
            } else if (localName.equalsIgnoreCase("ArrivalTerminalScheduled")) {
                arrivalTerminalScheduled = chars;
            } else if (localName.equalsIgnoreCase("ArrivalTerminalActual")) {
                arrivalTerminalActual = chars;
            } else if (localName.equalsIgnoreCase("ArrivalGate")) {
                arrivalGate = chars;
            } else if (localName.equalsIgnoreCase("ArrivalScheduled")) {
                arrivalScheduled = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("ArrivalEstimated")) {
                arrivalEstimated = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("ArrivalActual")) {
                arrivalActual = Parse.parseXMLTimestamp(chars);
            } else if (localName.equalsIgnoreCase("BaggageClaim")) {
                baggageClaim = chars;
            } else if (localName.equalsIgnoreCase("DiversionCity")) {
                diversionCity = chars;
            } else if (localName.equalsIgnoreCase("DiversionAirport")) {
                diversionAirport = chars;
            } else if (localName.equalsIgnoreCase("ArrivalStatusReason")) {
                arrivalStatusReason = chars;
            } else if (localName.equalsIgnoreCase("ArrivalShortStatus")) {
                arrivalShortStatus = chars;
            } else if (localName.equalsIgnoreCase("ArrivalLongStatus")) {
                arrivalLongStatus = chars;
            } else if (localName.equalsIgnoreCase("CliqbookMessage")) {
                cliqbookMessage = chars;
            } else if (localName.equalsIgnoreCase("LastUpdatedUTC")) {
                lastUpdatedUTC = Parse.parseXMLTimestamp(chars);
            }

        }
    }
}
