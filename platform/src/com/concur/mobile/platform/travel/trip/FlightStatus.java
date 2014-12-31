package com.concur.mobile.platform.travel.trip;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.parser.BaseParser;
import com.concur.mobile.platform.util.Const;
import com.concur.mobile.platform.util.Parse;

/**
 * An extension of <code>BaseParser</code> for the purpose of parsing flight status information.
 * 
 * @author andrewk
 */
public class FlightStatus extends BaseParser {

    private static final String CLS_TAG = "FlightStatus";

    // Tags.
    private static final String TAG_EQUIPMENT_SCHEDULED = "EquipmentScheduled";
    private static final String TAG_EQUIPMENT_ACTUAL = "EquipmentActual";
    private static final String TAG_EQUIPMENT_REGISTRATION = "EquipmentRegistration";
    private static final String TAG_DEPARTURE_TERMINAL_SCHEDULED = "DepartureTerminalScheduled";
    private static final String TAG_DEPARTURE_TERMINAL_ACTUAL = "DepartureTerminalActual";
    private static final String TAG_DEPARTURE_GATE = "DepartureGate";
    private static final String TAG_DEPARTURE_SCHEDULED = "DepartureScheduled";
    private static final String TAG_DEPARTURE_ESTIMATED = "DepartureEstimated";
    private static final String TAG_DEPARTURE_ACTUAL = "DepartureActual";
    private static final String TAG_DEPARTURE_STATUS_REASON = "DepartureStatusReason";
    private static final String TAG_DEPARTURE_SHORT_STATUS = "DepartureShortStatus";
    private static final String TAG_DEPARTURE_LONG_STATUS = "DepartureLongStatus";
    private static final String TAG_ARRIVAL_TERMINAL_SCHEDULED = "ArrivalTerminalScheduled";
    private static final String TAG_ARRIVAL_TERMINAL_ACTUAL = "ArrivalTerminalActual";
    private static final String TAG_ARRIVAL_GATE = "ArrivalGate";
    private static final String TAG_ARRIVAL_SCHEDULED = "ArrivalScheduled";
    private static final String TAG_ARRIVAL_ESTIMATED = "ArrivalEstimated";
    private static final String TAG_ARRIVAL_ACTUAL = "ArrivalActual";
    private static final String TAG_BAGGAGE_CLAIM = "BaggageClaim";
    private static final String TAG_DIVERSION_CITY = "DiversionCity";
    private static final String TAG_DIVERSION_AIRPORT = "DiversionAirport";
    private static final String TAG_ARRIVAL_STATUS_REASON = "ArrivalStatusReason";
    private static final String TAG_ARRIVAL_SHORT_STATUS = "ArrivalShortStatus";
    private static final String TAG_ARRIVAL_LONG_STATUS = "ArrivalLongStatus";
    private static final String TAG_CLIQBOOK_MESSAGE = "CliqbookMessage";
    private static final String TAG_LAST_UPDATED_UTC = "LastUpdatedUTC";

    // Tag codes.
    private static final int CODE_EQUIPMENT_SCHEDULED = 0;
    private static final int CODE_EQUIPMENT_ACTUAL = 1;
    private static final int CODE_EQUIPMENT_REGISTRATION = 2;
    private static final int CODE_DEPARTURE_TERMINAL_SCHEDULED = 3;
    private static final int CODE_DEPARTURE_TERMINAL_ACTUAL = 4;
    private static final int CODE_DEPARTURE_GATE = 5;
    private static final int CODE_DEPARTURE_SCHEDULED = 6;
    private static final int CODE_DEPARTURE_ESTIMATED = 7;
    private static final int CODE_DEPARTURE_ACTUAL = 8;
    private static final int CODE_DEPARTURE_STATUS_REASON = 9;
    private static final int CODE_DEPARTURE_SHORT_STATUS = 10;
    private static final int CODE_DEPARTURE_LONG_STATUS = 11;
    private static final int CODE_ARRIVAL_TERMINAL_SCHEDULED = 12;
    private static final int CODE_ARRIVAL_TERMINAL_ACTUAL = 13;
    private static final int CODE_ARRIVAL_GATE = 14;
    private static final int CODE_ARRIVAL_SCHEDULED = 15;
    private static final int CODE_ARRIVAL_ESTIMATED = 16;
    private static final int CODE_ARRIVAL_ACTUAL = 17;
    private static final int CODE_BAGGAGE_CLAIM = 18;
    private static final int CODE_DIVERSION_CITY = 19;
    private static final int CODE_DIVERSION_AIRPORT = 20;
    private static final int CODE_ARRIVAL_STATUS_REASON = 21;
    private static final int CODE_ARRIVAL_SHORT_STATUS = 22;
    private static final int CODE_ARRIVAL_LONG_STATUS = 23;
    private static final int CODE_CLIQBOOK_MESSAGE = 24;
    private static final int CODE_LAST_UPDATED_UTC = 25;

    // Contains a map from a tag name to an integer code.
    private static final Map<String, Integer> tagMap;

    static {
        // Initialize the map from tags to integer codes.
        tagMap = new HashMap<String, Integer>();
        tagMap.put(TAG_EQUIPMENT_SCHEDULED, CODE_EQUIPMENT_SCHEDULED);
        tagMap.put(TAG_EQUIPMENT_ACTUAL, CODE_EQUIPMENT_ACTUAL);
        tagMap.put(TAG_EQUIPMENT_REGISTRATION, CODE_EQUIPMENT_REGISTRATION);
        tagMap.put(TAG_DEPARTURE_TERMINAL_SCHEDULED, CODE_DEPARTURE_TERMINAL_SCHEDULED);
        tagMap.put(TAG_DEPARTURE_TERMINAL_ACTUAL, CODE_DEPARTURE_TERMINAL_ACTUAL);
        tagMap.put(TAG_DEPARTURE_GATE, CODE_DEPARTURE_GATE);
        tagMap.put(TAG_DEPARTURE_SCHEDULED, CODE_DEPARTURE_SCHEDULED);
        tagMap.put(TAG_DEPARTURE_ESTIMATED, CODE_DEPARTURE_ESTIMATED);
        tagMap.put(TAG_DEPARTURE_ACTUAL, CODE_DEPARTURE_ACTUAL);
        tagMap.put(TAG_DEPARTURE_STATUS_REASON, CODE_DEPARTURE_STATUS_REASON);
        tagMap.put(TAG_DEPARTURE_SHORT_STATUS, CODE_DEPARTURE_SHORT_STATUS);
        tagMap.put(TAG_DEPARTURE_LONG_STATUS, CODE_DEPARTURE_LONG_STATUS);
        tagMap.put(TAG_ARRIVAL_TERMINAL_SCHEDULED, CODE_ARRIVAL_TERMINAL_SCHEDULED);
        tagMap.put(TAG_ARRIVAL_TERMINAL_ACTUAL, CODE_ARRIVAL_TERMINAL_ACTUAL);
        tagMap.put(TAG_ARRIVAL_GATE, CODE_ARRIVAL_GATE);
        tagMap.put(TAG_ARRIVAL_SCHEDULED, CODE_ARRIVAL_SCHEDULED);
        tagMap.put(TAG_ARRIVAL_ESTIMATED, CODE_ARRIVAL_ESTIMATED);
        tagMap.put(TAG_ARRIVAL_ACTUAL, CODE_ARRIVAL_ACTUAL);
        tagMap.put(TAG_BAGGAGE_CLAIM, CODE_BAGGAGE_CLAIM);
        tagMap.put(TAG_DIVERSION_CITY, CODE_DIVERSION_CITY);
        tagMap.put(TAG_DIVERSION_AIRPORT, CODE_DIVERSION_AIRPORT);
        tagMap.put(TAG_ARRIVAL_STATUS_REASON, CODE_ARRIVAL_STATUS_REASON);
        tagMap.put(TAG_ARRIVAL_SHORT_STATUS, CODE_ARRIVAL_SHORT_STATUS);
        tagMap.put(TAG_ARRIVAL_LONG_STATUS, CODE_ARRIVAL_LONG_STATUS);
        tagMap.put(TAG_CLIQBOOK_MESSAGE, CODE_CLIQBOOK_MESSAGE);
        tagMap.put(TAG_LAST_UPDATED_UTC, CODE_LAST_UPDATED_UTC);
    }

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

    @Override
    public void handleText(String tag, String text) {
        Integer tagCode = tagMap.get(tag);
        if (tagCode != null) {
            if (!TextUtils.isEmpty(text)) {
                switch (tagCode) {
                case CODE_EQUIPMENT_SCHEDULED: {
                    equipmentScheduled = text.trim();
                    break;
                }
                case CODE_EQUIPMENT_ACTUAL: {
                    equipmentActual = text.trim();
                    break;
                }
                case CODE_EQUIPMENT_REGISTRATION: {
                    equipmentRegistration = text.trim();
                    break;
                }
                case CODE_DEPARTURE_TERMINAL_SCHEDULED: {
                    departureTerminalScheduled = text.trim();
                    break;
                }
                case CODE_DEPARTURE_TERMINAL_ACTUAL: {
                    departureTerminalActual = text.trim();
                    break;
                }
                case CODE_DEPARTURE_GATE: {
                    departureGate = text.trim();
                    break;
                }
                case CODE_DEPARTURE_SCHEDULED: {
                    departureScheduled = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_DEPARTURE_ESTIMATED: {
                    departureEstimated = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_DEPARTURE_ACTUAL: {
                    departureActual = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_DEPARTURE_STATUS_REASON: {
                    departureStatusReason = text.trim();
                    break;
                }
                case CODE_DEPARTURE_SHORT_STATUS: {
                    departureShortStatus = text.trim();
                    break;
                }
                case CODE_DEPARTURE_LONG_STATUS: {
                    departureLongStatus = text.trim();
                    break;
                }
                case CODE_ARRIVAL_TERMINAL_SCHEDULED: {
                    arrivalTerminalScheduled = text.trim();
                    break;
                }
                case CODE_ARRIVAL_TERMINAL_ACTUAL: {
                    arrivalTerminalActual = text.trim();
                    break;
                }
                case CODE_ARRIVAL_GATE: {
                    arrivalGate = text.trim();
                    break;
                }
                case CODE_ARRIVAL_SCHEDULED: {
                    arrivalScheduled = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_ARRIVAL_ESTIMATED: {
                    arrivalEstimated = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_ARRIVAL_ACTUAL: {
                    arrivalActual = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                case CODE_BAGGAGE_CLAIM: {
                    baggageClaim = text.trim();
                    break;
                }
                case CODE_DIVERSION_CITY: {
                    diversionCity = text.trim();
                    break;
                }
                case CODE_DIVERSION_AIRPORT: {
                    diversionAirport = text.trim();
                    break;
                }
                case CODE_ARRIVAL_STATUS_REASON: {
                    arrivalStatusReason = text.trim();
                    break;
                }
                case CODE_ARRIVAL_SHORT_STATUS: {
                    arrivalShortStatus = text.trim();
                    break;
                }
                case CODE_ARRIVAL_LONG_STATUS: {
                    arrivalLongStatus = text.trim();
                    break;
                }
                case CODE_CLIQBOOK_MESSAGE: {
                    cliqbookMessage = text.trim();
                    break;
                }
                case CODE_LAST_UPDATED_UTC: {
                    lastUpdatedUTC = Parse.parseXMLTimestamp(text.trim());
                    break;
                }
                default: {
                    if (Const.DEBUG_PARSING) {
                        Log.w(Const.LOG_TAG, CLS_TAG + ".handleText: missing case statement for tag '" + tag + "'.");
                    }
                    break;
                }
                }
            }
        } else {
            if (Const.DEBUG_PARSING) {
                Log.d(Const.LOG_TAG, CLS_TAG + ".handleText: unexpected tag '" + tag + ".");
            }
        }
    }

}
