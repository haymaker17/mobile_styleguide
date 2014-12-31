package com.concur.mobile.platform.travel.trip;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>SegmentData</code> for identifying air specific segment information.
 */
public class AirSegmentData extends SegmentData {

    private static final String CLS_TAG = "AirSegmentData";

    private static final String TAG_SEGMENT = "Segment";

    private static final String TAG_SEATS = "Seats";
    private static final String TAG_SEAT_DATA = "SeatData";
    private static final String TAG_FLIGHT_STATUS = "FlightStatus";

    // Tags
    private static final String TAG_AIRCRAFT_CODE = "AircraftCode";
    private static final String TAG_AIRCRAFT_NAME = "AircraftName";
    private static final String TAG_CABIN = "Cabin";
    private static final String TAG_CHECKED_BAGGAGE = "CheckedBaggage";
    private static final String TAG_CLASS_OF_SERVICE = "ClassOfService";
    private static final String TAG_CLASS_OF_SERVICE_LOCALIZED = "ClassOfServiceLocalized";
    private static final String TAG_DURATION = "Duration";
    private static final String TAG_END_AIRPORT_CITY = "EndAirportCity";
    private static final String TAG_END_AIRPORT_COUNTRY = "EndAirportCountry";
    private static final String TAG_END_AIRPORT_COUNTRY_CODE = "EndAirportCountryCode";
    private static final String TAG_END_AIRPORT_NAME = "EndAirportName";
    private static final String TAG_END_AIRPORT_STATE = "EndAirportState";
    private static final String TAG_END_GATE = "EndGate";
    private static final String TAG_END_TERMINAL = "EndTerminal";
    private static final String TAG_FARE_BASIS_CODE = "FareBasisCode";
    private static final String TAG_FLIGHT_NUMBER = "FlightNumber";
    private static final String TAG_LEG_ID = "LegId";
    private static final String TAG_MEALS = "Meals";
    private static final String TAG_MILES = "Miles";
    private static final String TAG_NUM_STOPS = "NumStops";
    private static final String TAG_OPERATED_BY_FLIGHT_NUMBER = "OperatedByFlightNumber";
    private static final String TAG_SPECIAL_INSTRUCTIONS = "SpecialInstructions";
    private static final String TAG_START_AIRPORT_CITY = "StartAirportCity";
    private static final String TAG_START_AIRPORT_COUNTRY = "StartAirportCountry";
    private static final String TAG_START_AIRPORT_COUNTRY_CODE = "StartAirportCountryCode";
    private static final String TAG_START_AIRPORT_NAME = "StartAirportName";
    private static final String TAG_START_AIRPORT_STATE = "StartAirportState";
    private static final String TAG_START_GATE = "StartGate";
    private static final String TAG_START_TERMINAL = "StartTerminal";

    // Tag codes.
    private static final int CODE_AIRCRAFT_CODE = 0;
    private static final int CODE_AIRCRAFT_NAME = 1;
    private static final int CODE_CABIN = 2;
    private static final int CODE_CHECKED_BAGGAGE = 3;
    private static final int CODE_CLASS_OF_SERVICE = 4;
    private static final int CODE_CLASS_OF_SERVICE_LOCALIZED = 5;
    private static final int CODE_DURATION = 6;
    private static final int CODE_END_AIRPORT_CITY = 7;
    private static final int CODE_END_AIRPORT_COUNTRY = 8;
    private static final int CODE_END_AIRPORT_COUNTRY_CODE = 9;
    private static final int CODE_END_AIRPORT_NAME = 10;
    private static final int CODE_END_AIRPORT_STATE = 11;
    private static final int CODE_END_GATE = 12;
    private static final int CODE_END_TERMINAL = 13;
    private static final int CODE_FARE_BASIS_CODE = 14;
    private static final int CODE_FLIGHT_NUMBER = 15;
    private static final int CODE_LEG_ID = 16;
    private static final int CODE_MEALS = 17;
    private static final int CODE_MILES = 18;
    private static final int CODE_NUM_STOPS = 19;
    private static final int CODE_OPERATED_BY_FLIGHT_NUMBER = 20;
    private static final int CODE_SPECIAL_INSTRUCTIONS = 21;
    private static final int CODE_START_AIRPORT_CITY = 22;
    private static final int CODE_START_AIRPORT_COUNTRY = 23;
    private static final int CODE_START_AIRPORT_COUNTRY_CODE = 24;
    private static final int CODE_START_AIRPORT_NAME = 25;
    private static final int CODE_START_AIRPORT_STATE = 26;
    private static final int CODE_START_GATE = 27;
    private static final int CODE_START_TERMINAL = 28;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> asdTagMap;

    static {
        // Initialize the map from tags to integer codes.
        asdTagMap = new HashMap<String, Integer>();
        asdTagMap.put(TAG_AIRCRAFT_CODE, CODE_AIRCRAFT_CODE);
        asdTagMap.put(TAG_AIRCRAFT_NAME, CODE_AIRCRAFT_NAME);
        asdTagMap.put(TAG_CABIN, CODE_CABIN);
        asdTagMap.put(TAG_CHECKED_BAGGAGE, CODE_CHECKED_BAGGAGE);
        asdTagMap.put(TAG_CLASS_OF_SERVICE, CODE_CLASS_OF_SERVICE);
        asdTagMap.put(TAG_CLASS_OF_SERVICE_LOCALIZED, CODE_CLASS_OF_SERVICE_LOCALIZED);
        asdTagMap.put(TAG_DURATION, CODE_DURATION);
        asdTagMap.put(TAG_END_AIRPORT_CITY, CODE_END_AIRPORT_CITY);
        asdTagMap.put(TAG_END_AIRPORT_COUNTRY, CODE_END_AIRPORT_COUNTRY);
        asdTagMap.put(TAG_END_AIRPORT_COUNTRY_CODE, CODE_END_AIRPORT_COUNTRY_CODE);
        asdTagMap.put(TAG_END_AIRPORT_NAME, CODE_END_AIRPORT_NAME);
        asdTagMap.put(TAG_END_AIRPORT_STATE, CODE_END_AIRPORT_STATE);
        asdTagMap.put(TAG_END_GATE, CODE_END_GATE);
        asdTagMap.put(TAG_END_TERMINAL, CODE_END_TERMINAL);
        asdTagMap.put(TAG_FARE_BASIS_CODE, CODE_FARE_BASIS_CODE);
        asdTagMap.put(TAG_FLIGHT_NUMBER, CODE_FLIGHT_NUMBER);
        asdTagMap.put(TAG_LEG_ID, CODE_LEG_ID);
        asdTagMap.put(TAG_MEALS, CODE_MEALS);
        asdTagMap.put(TAG_MILES, CODE_MILES);
        asdTagMap.put(TAG_NUM_STOPS, CODE_NUM_STOPS);
        asdTagMap.put(TAG_OPERATED_BY_FLIGHT_NUMBER, CODE_OPERATED_BY_FLIGHT_NUMBER);
        asdTagMap.put(TAG_SPECIAL_INSTRUCTIONS, CODE_SPECIAL_INSTRUCTIONS);
        asdTagMap.put(TAG_START_AIRPORT_CITY, CODE_START_AIRPORT_CITY);
        asdTagMap.put(TAG_START_AIRPORT_COUNTRY, CODE_START_AIRPORT_COUNTRY);
        asdTagMap.put(TAG_START_AIRPORT_COUNTRY_CODE, CODE_START_AIRPORT_COUNTRY_CODE);
        asdTagMap.put(TAG_START_AIRPORT_NAME, CODE_START_AIRPORT_NAME);
        asdTagMap.put(TAG_START_AIRPORT_STATE, CODE_START_AIRPORT_STATE);
        asdTagMap.put(TAG_START_GATE, CODE_START_GATE);
        asdTagMap.put(TAG_START_TERMINAL, CODE_START_TERMINAL);
    }

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

    /**
     * Contains the segment flight status.
     */
    public FlightStatus flightStatus;

    /**
     * Contains whether or not flight status is being parsed.
     */
    private boolean inFlightStats;

    /**
     * Contains a list of seat data objects.
     */
    public List<SeatData> seats;

    /**
     * Contains the current seat data being parsed.
     */
    private SeatData seatData;

    /**
     * Contains whether or not seat data is being parsed.
     */
    private boolean inSeatData;

    public AirSegmentData() {
        super(SegmentType.AIR);
    }

    @Override
    public void startTag(String tag) {

        super.startTag(tag);

        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_SEATS)) {
                inSeatData = true;
                seats = new ArrayList<SeatData>();
            } else if (tag.equalsIgnoreCase(TAG_FLIGHT_STATUS)) {
                inFlightStats = true;
                flightStatus = new FlightStatus();
            } else if (inSeatData) {
                if (tag.equalsIgnoreCase(TAG_SEAT_DATA)) {
                    seatData = new SeatData();
                }
                if (seatData != null) {
                    seatData.startTag(tag);
                }
            } else if (inFlightStats) {
                if (flightStatus != null) {
                    flightStatus.startTag(tag);
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".startTag: flightStatus is null.");
                    }
                }
            }
        }
    }

    @Override
    public void endTag(String tag) {

        super.endTag(tag);

        if (!TextUtils.isEmpty(tag)) {
            if (tag.equalsIgnoreCase(TAG_SEATS)) {
                inSeatData = false;
            } else if (tag.equalsIgnoreCase(TAG_FLIGHT_STATUS)) {
                inFlightStats = false;
            } else if (inSeatData) {
                if (tag.equalsIgnoreCase(TAG_SEAT_DATA)) {
                    if (seatData != null) {
                        seatData.endTag(tag);
                        seats.add(seatData);
                        seatData = null;
                    }
                } else {
                    if (seatData != null) {
                        seatData.endTag(tag);
                    } else {
                        if (Const.DEBUG_PARSING) {
                            Log.e(Const.LOG_TAG, CLS_TAG + ".endTag: seatData is null.");
                        }
                    }
                }
            } else if (inFlightStats) {
                if (flightStatus != null) {
                    flightStatus.endTag(tag);
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".endTag: flightStatus is null.");
                    }
                }
            } else if (tag.equalsIgnoreCase(TAG_SEGMENT)) {
                // Merge any current flight status information.
                // This should really be done by the UI.
                // mergeFlightStats();
            }
        }
    }

    @Override
    public boolean handleSegmentText(String tag, String text) {
        boolean retVal = false;
        retVal = super.handleSegmentText(tag, text);
        if (!retVal) {
            if (inSeatData) {
                if (seatData != null) {
                    seatData.handleText(tag, text);
                    retVal = true;
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleSegmentText: seatData is null.");
                    }
                }
            } else if (inFlightStats) {
                if (flightStatus != null) {
                    flightStatus.handleText(tag, text);
                    retVal = true;
                } else {
                    if (Const.DEBUG_PARSING) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".handleSegmentText: flightStatus is null.");
                    }
                }
            } else {
                Integer tagCode = asdTagMap.get(tag);
                if (tagCode != null) {
                    if (!TextUtils.isEmpty(text)) {
                        switch (tagCode) {
                        case CODE_AIRCRAFT_CODE: {
                            aircraftCode = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_AIRCRAFT_NAME: {
                            aircraftName = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_CABIN: {
                            cabin = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_CHECKED_BAGGAGE: {
                            checkedBaggage = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_CLASS_OF_SERVICE: {
                            classOfService = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_CLASS_OF_SERVICE_LOCALIZED: {
                            classOfServiceLocalized = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_DURATION: {
                            duration = Parse.safeParseInteger(text.trim());
                            retVal = true;
                            break;
                        }
                        case CODE_END_AIRPORT_CITY: {
                            endAirportCity = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_AIRPORT_COUNTRY: {
                            endAirportCountry = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_AIRPORT_COUNTRY_CODE: {
                            endAirportCountryCode = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_AIRPORT_NAME: {
                            endAirportName = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_AIRPORT_STATE: {
                            endAirportState = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_GATE: {
                            endGate = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_END_TERMINAL: {
                            endTerminal = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_FARE_BASIS_CODE: {
                            fareBasisCode = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_FLIGHT_NUMBER: {
                            flightNumber = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_LEG_ID: {
                            legId = Parse.safeParseInteger(text.trim());
                            retVal = true;
                            break;
                        }
                        case CODE_MEALS: {
                            meals = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_MILES: {
                            miles = Parse.safeParseInteger(text.trim());
                            retVal = true;
                            break;
                        }
                        case CODE_NUM_STOPS: {
                            numStops = Parse.safeParseInteger(text.trim());
                            retVal = true;
                            break;
                        }
                        case CODE_OPERATED_BY_FLIGHT_NUMBER: {
                            operatedByFlightNumber = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_SPECIAL_INSTRUCTIONS: {
                            specialInstructions = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_AIRPORT_CITY: {
                            startAirportCity = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_AIRPORT_COUNTRY: {
                            startAirportCountry = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_AIRPORT_COUNTRY_CODE: {
                            startAirportCountryCode = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_AIRPORT_NAME: {
                            startAirportName = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_AIRPORT_STATE: {
                            startAirportState = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_GATE: {
                            startGate = text.trim();
                            retVal = true;
                            break;
                        }
                        case CODE_START_TERMINAL: {
                            startTerminal = text.trim();
                            retVal = true;
                            break;
                        }
                        default: {
                            if (Const.DEBUG_PARSING) {
                                Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: missing case statement for tag '" + tag
                                        + "'.");
                            }
                            break;
                        }
                        }
                    }
                }
            }
        }
        return retVal;
    }

    public boolean hasFlightStats() {
        return (flightStatus != null && (flightStatus.cliqbookMessage == null || flightStatus.cliqbookMessage.trim()
                .length() == 0));
    }

    public void mergeFlightStats() {
        if (hasFlightStats()) {
            // We have something. Merge it in.
            // updatedByFlightStats = true;
            FlightStatus fs = flightStatus;

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

}
