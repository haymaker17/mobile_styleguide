package com.concur.mobile.platform.travel.provider;

import android.util.Log;

import com.concur.mobile.platform.provider.PlatformSQLiteDatabase;
import com.concur.mobile.platform.util.Const;

/**
 * Provides database schema support for the Travel provider.
 *
 * @author andrewk
 */
public class TravelDBSchema {

    // Creates the trip summary table.
    protected static final String SCHEMA_CREATE_TRIP_SUMMARY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.TripSummaryColumns.TABLE_NAME + " (" + Travel.TripSummaryColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.TripSummaryColumns.APPROVAL_STATUS + " TEXT, "
                    + Travel.TripSummaryColumns.APPROVER_ID + " TEXT, " + Travel.TripSummaryColumns.APPROVER_NAME
                    + " TEXT, " + Travel.TripSummaryColumns.AUTHORIZATION_NUMBER + " TEXT, "
                    + Travel.TripSummaryColumns.BOOKED_VIA + " TEXT, " + Travel.TripSummaryColumns.BOOKING_SOURCE
                    + " TEXT, " + Travel.TripSummaryColumns.CAN_BE_EXPENSED + " INTEGER, "
                    + Travel.TripSummaryColumns.CLIQ_BOOK_STATE + " INTEGER, "
                    + Travel.TripSummaryColumns.END_DATE_LOCAL + " TEXT, " + Travel.TripSummaryColumns.END_DATE_UTC
                    + " TEXT, " + Travel.TripSummaryColumns.HAS_OTHERS + " INTEGER, "
                    + Travel.TripSummaryColumns.HAS_TICKETS + " INTEGER, " + Travel.TripSummaryColumns.IS_EXPENSED
                    + " INTEGER, " + Travel.TripSummaryColumns.IS_GDS_BOOKING + " INTEGER, "
                    + Travel.TripSummaryColumns.IS_PERSONAL + " INTEGER, " + Travel.TripSummaryColumns.IS_WITHDRAWN
                    + " INTEGER, " + Travel.TripSummaryColumns.IS_PUBLIC + " INTEGER, "
                    + Travel.TripSummaryColumns.ITIN_ID + " INTEGER, " + Travel.TripSummaryColumns.ITIN_LOCATOR
                    + " TEXT, " + Travel.TripSummaryColumns.ITIN_SOURCE_LIST + " TEXT, "
                    + Travel.TripSummaryColumns.RECORD_LOCATOR + " TEXT, " + Travel.TripSummaryColumns.SEGMENT_TYPES
                    + " TEXT, " + Travel.TripSummaryColumns.START_DATE_LOCAL + " TEXT, "
                    + Travel.TripSummaryColumns.START_DATE_UTC + " TEXT, " + Travel.TripSummaryColumns.TRIP_ID
                    + " INTEGER, " + Travel.TripSummaryColumns.TRIP_KEY + " TEXT, "
                    + Travel.TripSummaryColumns.TRIP_NAME + " TEXT, " + Travel.TripSummaryColumns.TRIP_STATUS
                    + " INTEGER, " + Travel.TripSummaryColumns.USER_ID + " TEXT" + ")";
    // Drop the trip summary table.
    protected static final String DROP_TRIP_SUMMARY_TABLE =
            "DROP TABLE IF EXISTS " + Travel.TripSummaryColumns.TABLE_NAME + ";";
    // Creates the trip summary message table.
    protected static final String SCHEMA_CREATE_TRIP_SUMMARY_MESSAGE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.TripSummaryMessageColumns.TABLE_NAME + " ("
                    + Travel.TripSummaryMessageColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.TripSummaryMessageColumns.MESSAGE + " TEXT, " + Travel.TripSummaryMessageColumns.TRIP_ID
                    + " INTEGER, " + Travel.TripSummaryMessageColumns.USER_ID + " TEXT" + ")";
    // Drop the trip summary message table.
    protected static final String DROP_TRIP_SUMMARY_MESSAGE_TABLE =
            "DROP TABLE IF EXISTS " + Travel.TripSummaryMessageColumns.TABLE_NAME + ";";
    // Creates the Trip table.
    protected static final String SCHEMA_CREATE_TRIP_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.TripColumns.TABLE_NAME + " (" + Travel.TripColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.TripColumns.CLIENT_LOCATOR + " TEXT, "
                    + Travel.TripColumns.CLIQBOOK_TRIP_ID + " INTEGER, " + Travel.TripColumns.DESCRIPTION + " TEXT, "
                    + Travel.TripColumns.END_DATE_LOCAL + " TEXT, " + Travel.TripColumns.END_DATE_UTC + " TEXT, "
                    + Travel.TripColumns.ITIN_LOCATOR + " TEXT, " + Travel.TripColumns.RECORD_LOCATOR + " TEXT, "
                    + Travel.TripColumns.START_DATE_LOCAL + " TEXT, " + Travel.TripColumns.START_DATE_UTC + " TEXT, "
                    + Travel.TripColumns.STATE + " INTEGER, " + Travel.TripColumns.TRIP_NAME + " TEXT, "
                    + Travel.TripColumns.USER_ID + " TEXT, " + Travel.TripColumns.ALLOW_ADD_AIR + " INTEGER, "
                    + Travel.TripColumns.ALLOW_ADD_CAR + " INTEGER, " + Travel.TripColumns.ALLOW_ADD_HOTEL
                    + " INTEGER, " + Travel.TripColumns.ALLOW_ADD_RAIL + " INTEGER, " + Travel.TripColumns.ALLOW_CANCEL
                    + " INTEGER " + ")";
    // Drop the trip table.
    protected static final String DROP_TRIP_TABLE = "DROP TABLE IF EXISTS " + Travel.TripColumns.TABLE_NAME + ";";
    // Creates the Enhancement Day table.
    protected static final String SCHEMA_CREATE_ENHANCEMENT_DAY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.EnhancementDayColumns.TABLE_NAME + " ("
                    + Travel.EnhancementDayColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.EnhancementDayColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.EnhancementDayColumns.TYPE + " TEXT, "
                    + Travel.EnhancementDayColumns.TRIP_LOCAL_DATE + " TEXT " + ")";
    // Drop the Enhancement Day table.
    protected static final String DROP_ENHANCEMENT_DAY_TABLE =
            "DROP TABLE IF EXISTS " + Travel.EnhancementDayColumns.TABLE_NAME + ";";
    // Creates the Enhancement Day index.
    protected static final String SCHEMA_CREATE_ENHANCEMENT_DAY_INDEX =
            "CREATE INDEX " + Travel.EnhancementDayColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.EnhancementDayColumns.TABLE_NAME + "(" + Travel.EnhancementDayColumns.TRIP_ID + ")";
    // Creates the Sortable Segment table.
    protected static final String SCHEMA_CREATE_SORTABLE_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.SortableSegmentColumns.TABLE_NAME + " ("
                    + Travel.SortableSegmentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.SortableSegmentColumns.ENHANCEMENT_DAY_ID + " INTEGER REFERENCES "
                    + Travel.EnhancementDayColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.SortableSegmentColumns.BOOKING_SOURCE + " TEXT, "
                    + Travel.SortableSegmentColumns.RECORD_LOCATOR + " TEXT, "
                    + Travel.SortableSegmentColumns.SEGMENT_KEY + " TEXT, " + Travel.SortableSegmentColumns.SEGMENT_SIDE
                    + " TEXT, " + Travel.SortableSegmentColumns.SORT_VALUE + " TEXT " + ")";
    // Drop the Sortable Segment table.
    protected static final String DROP_SORTABLE_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.SortableSegmentColumns.TABLE_NAME + ";";
    // Creates the Sortable Segment index.
    protected static final String SCHEMA_CREATE_SORTABLE_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.SortableSegmentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.SortableSegmentColumns.TABLE_NAME + "(" + Travel.SortableSegmentColumns.ENHANCEMENT_DAY_ID
                    + ")";
    // Creates the Enhancement Offer table.
    protected static final String SCHEMA_CREATE_ENHANCEMENT_OFFER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.EnhancementOfferColumns.TABLE_NAME + " ("
                    + Travel.EnhancementOfferColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.EnhancementOfferColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.EnhancementOfferColumns.ID + " TEXT, "
                    + Travel.EnhancementOfferColumns.DESCRIPTION + " TEXT, " + Travel.EnhancementOfferColumns.TYPE
                    + " TEXT " + ")";
    // Drop the Enhancement Offer table.
    protected static final String DROP_ENHANCEMENT_OFFER_TABLE =
            "DROP TABLE IF EXISTS " + Travel.EnhancementOfferColumns.TABLE_NAME + ";";
    // Creates the Enhancement Offer index.
    protected static final String SCHEMA_CREATE_ENHANCEMENT_OFFER_INDEX =
            "CREATE INDEX " + Travel.EnhancementOfferColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.EnhancementOfferColumns.TABLE_NAME + "(" + Travel.EnhancementOfferColumns.TRIP_ID + ")";
    // Creates the Offer Link table.
    protected static final String SCHEMA_CREATE_OFFER_LINK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.OfferLinkColumns.TABLE_NAME + " (" + Travel.OfferLinkColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.OfferLinkColumns.ENHANCEMENT_OFFER_ID
                    + " INTEGER REFERENCES " + Travel.EnhancementOfferColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.OfferLinkColumns.BOOKING_SOURCE + " TEXT, " + Travel.OfferLinkColumns.RECORD_LOCATOR
                    + " TEXT, " + Travel.OfferLinkColumns.SEGMENT_KEY + " TEXT, " + Travel.OfferLinkColumns.SEGMENT_SIDE
                    + " TEXT " + ")";
    // Drop the Offer Link table.
    protected static final String DROP_OFFER_LINK_TABLE =
            "DROP TABLE IF EXISTS " + Travel.OfferLinkColumns.TABLE_NAME + ";";
    // Creates the Offer Link index.
    protected static final String SCHEMA_CREATE_OFFER_LINK_INDEX =
            "CREATE INDEX " + Travel.OfferLinkColumns.TABLE_NAME + "_INDEX ON " + Travel.OfferLinkColumns.TABLE_NAME
                    + "(" + Travel.OfferLinkColumns.ENHANCEMENT_OFFER_ID + ")";
    // Creates the Offer Content table.
    protected static final String SCHEMA_CREATE_OFFER_CONTENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.OfferContentColumns.TABLE_NAME + " ("
                    + Travel.OfferContentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.OfferContentColumns.ENHANCEMENT_OFFER_ID + " INTEGER REFERENCES "
                    + Travel.EnhancementOfferColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.OfferContentColumns.TITLE + " TEXT, " + Travel.OfferContentColumns.VENDOR + " TEXT, "
                    + Travel.OfferContentColumns.ACTION + " TEXT, " + Travel.OfferContentColumns.APPLICATION + " TEXT, "
                    + Travel.OfferContentColumns.IMAGE_NAME + " TEXT " + ")";
    // Drop the Offer Content table.
    protected static final String DROP_OFFER_CONTENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.OfferContentColumns.TABLE_NAME + ";";
    // Creates the Offer Content index.
    protected static final String SCHEMA_CREATE_OFFER_CONTENT_INDEX =
            "CREATE INDEX " + Travel.OfferContentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.OfferContentColumns.TABLE_NAME + "(" + Travel.OfferContentColumns.ENHANCEMENT_OFFER_ID
                    + ")";
    // Creates the Content Link table.
    protected static final String SCHEMA_CREATE_CONTENT_LINK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.ContentLinkColumns.TABLE_NAME + " (" + Travel.ContentLinkColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.ContentLinkColumns.OFFER_CONTENT_ID
                    + " INTEGER REFERENCES " + Travel.OfferContentColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.ContentLinkColumns.TITLE + " TEXT, " + Travel.ContentLinkColumns.ACTION_URL + " TEXT "
                    + ")";
    // Drop the Content Link table.
    protected static final String DROP_CONTENT_LINK_TABLE =
            "DROP TABLE IF EXISTS " + Travel.ContentLinkColumns.TABLE_NAME + ";";
    // Creates the Content Link index.
    protected static final String SCHEMA_CREATE_CONTENT_LINK_INDEX =
            "CREATE INDEX " + Travel.ContentLinkColumns.TABLE_NAME + "_INDEX ON " + Travel.ContentLinkColumns.TABLE_NAME
                    + "(" + Travel.ContentLinkColumns.OFFER_CONTENT_ID + ")";
    // Creates the Map Display table.
    protected static final String SCHEMA_CREATE_MAP_DISPLAY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.MapDisplayColumns.TABLE_NAME + " (" + Travel.MapDisplayColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.MapDisplayColumns.OFFER_CONTENT_ID
                    + " INTEGER REFERENCES " + Travel.OfferContentColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.MapDisplayColumns.LATITUDE + " REAL, " + Travel.MapDisplayColumns.LONGITUDE + " REAL, "
                    + Travel.MapDisplayColumns.DIMENSION_KM + " REAL " + ")";
    // Drop the Map Display table.
    protected static final String DROP_MAP_DISPLAY_TABLE =
            "DROP TABLE IF EXISTS " + Travel.MapDisplayColumns.TABLE_NAME + ";";
    // Creates the Map Display index.
    protected static final String SCHEMA_CREATE_MAP_DISPLAY_INDEX =
            "CREATE INDEX " + Travel.MapDisplayColumns.TABLE_NAME + "_INDEX ON " + Travel.MapDisplayColumns.TABLE_NAME
                    + "(" + Travel.MapDisplayColumns.OFFER_CONTENT_ID + ")";
    // Creates the Display Overlay table.
    protected static final String SCHEMA_CREATE_DISPLAY_OVERLAY_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.DisplayOverlayColumns.TABLE_NAME + " ("
                    + Travel.DisplayOverlayColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.DisplayOverlayColumns.MAP_DISPLAY_ID + " INTEGER REFERENCES "
                    + Travel.MapDisplayColumns.TABLE_NAME + " ON DELETE CASCADE, " + Travel.DisplayOverlayColumns.NAME
                    + " TEXT " + ")";
    // Drop the Display Overlay table.
    protected static final String DROP_DISPLAY_OVERLAY_TABLE =
            "DROP TABLE IF EXISTS " + Travel.DisplayOverlayColumns.TABLE_NAME + ";";
    // Creates the Display Overlay index.
    protected static final String SCHEMA_CREATE_DISPLAY_OVERLAY_INDEX =
            "CREATE INDEX " + Travel.DisplayOverlayColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.DisplayOverlayColumns.TABLE_NAME + "(" + Travel.DisplayOverlayColumns.MAP_DISPLAY_ID + ")";
    // Creates the Validity Location table.
    protected static final String SCHEMA_CREATE_VALIDITY_LOCATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.ValidityLocationColumns.TABLE_NAME + " ("
                    + Travel.ValidityLocationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.ValidityLocationColumns.ENHANCEMENT_OFFER_ID + " INTEGER REFERENCES "
                    + Travel.EnhancementOfferColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.ValidityLocationColumns.LATITUDE + " REAL, " + Travel.ValidityLocationColumns.LONGITUDE
                    + " REAL, " + Travel.ValidityLocationColumns.PROXIMITY + " REAL " + ")";
    // Drop the Validity Location table.
    protected static final String DROP_VALIDITY_LOCATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.ValidityLocationColumns.TABLE_NAME + ";";
    // Creates the Validity Location index.
    protected static final String SCHEMA_CREATE_VALIDITY_LOCATION_INDEX =
            "CREATE INDEX " + Travel.ValidityLocationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.ValidityLocationColumns.TABLE_NAME + "("
                    + Travel.ValidityLocationColumns.ENHANCEMENT_OFFER_ID + ")";
    // Creates the Validity Time Range table.
    protected static final String SCHEMA_CREATE_VALIDITY_TIME_RANGE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.ValidityTimeRangeColumns.TABLE_NAME + " ("
                    + Travel.ValidityTimeRangeColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.ValidityTimeRangeColumns.ENHANCEMENT_OFFER_ID + " INTEGER REFERENCES "
                    + Travel.EnhancementOfferColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.ValidityTimeRangeColumns.START_DATE_TIME_UTC + " TEXT, "
                    + Travel.ValidityTimeRangeColumns.END_DATE_TIME_UTC + " TEXT " + ")";
    // Drop the Validity Time Range table.
    protected static final String DROP_VALIDITY_TIME_RANGE_TABLE =
            "DROP TABLE IF EXISTS " + Travel.ValidityTimeRangeColumns.TABLE_NAME + ";";
    // Creates the ValidityTimeRange index.
    protected static final String SCHEMA_CREATE_VALIDITY_TIME_RANGE_INDEX =
            "CREATE INDEX " + Travel.ValidityTimeRangeColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.ValidityTimeRangeColumns.TABLE_NAME + "("
                    + Travel.ValidityTimeRangeColumns.ENHANCEMENT_OFFER_ID + ")";
    // Creates the Trip Rule Violation table.
    protected static final String SCHEMA_CREATE_TRIP_RULE_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.TripRuleViolationColumns.TABLE_NAME + " ("
                    + Travel.TripRuleViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.TripRuleViolationColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + "  ON DELETE CASCADE" + ")";
    // Drop the Trip Rule Violation table.
    protected static final String DROP_TRIP_RULE_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.TripRuleViolationColumns.TABLE_NAME + ";";
    // Creates the Trip Rule Violation index.
    protected static final String SCHEMA_CREATE_TRIP_RULE_VIOLATION_INDEX =
            "CREATE INDEX " + Travel.TripRuleViolationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.TripRuleViolationColumns.TABLE_NAME + "(" + Travel.TripRuleViolationColumns.TRIP_ID + ")";
    // Creates the Car Rule Violation table.
    protected static final String SCHEMA_CREATE_CAR_RULE_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.CarRuleViolationColumns.TABLE_NAME + " ("
                    + Travel.CarRuleViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.CarRuleViolationColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.CarRuleViolationColumns.DAILY_RATE + " TEXT " + ")";
    // Drop the Car Rule Violation table.
    protected static final String DROP_CAR_RULE_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.CarRuleViolationColumns.TABLE_NAME + ";";
    // Creates the CarRuleViolation index.
    protected static final String SCHEMA_CREATE_CAR_RULE_VIOLATION_INDEX =
            "CREATE INDEX " + Travel.CarRuleViolationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.CarRuleViolationColumns.TABLE_NAME + "(" + Travel.CarRuleViolationColumns.TRIP_ID + ")";
    // Creates the Hotel Rule Violation table.
    protected static final String SCHEMA_CREATE_HOTEL_RULE_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelRuleViolationColumns.TABLE_NAME + " ("
                    + Travel.HotelRuleViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelRuleViolationColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.HotelRuleViolationColumns.RATE + " TEXT, "
                    + Travel.HotelRuleViolationColumns.NAME + " TEXT, " + Travel.HotelRuleViolationColumns.ADDRESS
                    + " TEXT, " + Travel.HotelRuleViolationColumns.DESCRIPTION + " TEXT " + ")";
    // Drop the Hotel Rule Violation table.
    protected static final String DROP_HOTEL_RULE_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelRuleViolationColumns.TABLE_NAME + ";";
    // Creates the Hotel Rule Violation index.
    protected static final String SCHEMA_CREATE_HOTEL_RULE_VIOLATION_INDEX =
            "CREATE INDEX " + Travel.HotelRuleViolationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.HotelRuleViolationColumns.TABLE_NAME + "(" + Travel.HotelRuleViolationColumns.TRIP_ID
                    + ")";
    // Creates the Flight Rule Violation table.
    protected static final String SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.FlightRuleViolationColumns.TABLE_NAME + " ("
                    + Travel.FlightRuleViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.FlightRuleViolationColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.FlightRuleViolationColumns.REFUNDABLE + " INTEGER, "
                    + Travel.FlightRuleViolationColumns.COST + " TEXT " + ")";
    // Drop the Flight Rule Violation table.
    protected static final String DROP_FLIGHT_RULE_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.FlightRuleViolationColumns.TABLE_NAME + ";";
    // Creates the Flight Rule Violation index.
    protected static final String SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_INDEX =
            "CREATE INDEX " + Travel.FlightRuleViolationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.FlightRuleViolationColumns.TABLE_NAME + "(" + Travel.FlightRuleViolationColumns.TRIP_ID
                    + ")";
    // Creates the Rail Rule Violation table.
    protected static final String SCHEMA_CREATE_RAIL_RULE_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.RailRuleViolationColumns.TABLE_NAME + " ("
                    + Travel.RailRuleViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.RailRuleViolationColumns.TRIP_ID + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.RailRuleViolationColumns.RATE + " TEXT " + ")";
    // Drop the Rail Rule Violation table.
    protected static final String DROP_RAIL_RULE_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.RailRuleViolationColumns.TABLE_NAME + ";";
    // Creates the RailRuleViolation index.
    protected static final String SCHEMA_CREATE_RAIL_RULE_VIOLATION_INDEX =
            "CREATE INDEX " + Travel.RailRuleViolationColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.RailRuleViolationColumns.TABLE_NAME + "(" + Travel.RailRuleViolationColumns.TRIP_ID + ")";
    // Creates the Rule table.
    protected static final String SCHEMA_CREATE_RULE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.RuleColumns.TABLE_NAME + " (" + Travel.RuleColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.RuleColumns.TEXT + " TEXT, "
                    + Travel.RuleColumns.TRIP_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleColumns.CAR_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleColumns.RAIL_RULE_VIOLATION_ID + " INTEGER " + ")";
    // Drop the Rule table.
    protected static final String DROP_RULE_TABLE = "DROP TABLE IF EXISTS " + Travel.RuleColumns.TABLE_NAME + ";";
    // Creates 5 indices on the Rule table.
    protected static final String SCHEMA_CREATE_RULE_TRIP_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleColumns.TABLE_NAME + "_INDEX_1 ON " + Travel.RuleColumns.TABLE_NAME + "("
                    + Travel.RuleColumns.TRIP_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_CAR_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleColumns.TABLE_NAME + "_INDEX_2 ON " + Travel.RuleColumns.TABLE_NAME + "("
                    + Travel.RuleColumns.CAR_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_HOTEL_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleColumns.TABLE_NAME + "_INDEX_3 ON " + Travel.RuleColumns.TABLE_NAME + "("
                    + Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_FLIGHT_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleColumns.TABLE_NAME + "_INDEX_4 ON " + Travel.RuleColumns.TABLE_NAME + "("
                    + Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_RAIL_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleColumns.TABLE_NAME + "_INDEX_5 ON " + Travel.RuleColumns.TABLE_NAME + "("
                    + Travel.RuleColumns.RAIL_RULE_VIOLATION_ID + ")";
    // Creates the Rule Violation Reason table.
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.RuleViolationReasonColumns.TABLE_NAME + " ("
                    + Travel.RuleViolationReasonColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.RuleViolationReasonColumns.REASON + " TEXT, " + Travel.RuleViolationReasonColumns.COMMENTS
                    + " TEXT, " + Travel.RuleColumns.TRIP_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID + " INTEGER, "
                    + Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID + " INTEGER " + ")";
    // Drop the Rule Violation Reason table.
    protected static final String DROP_RULE_VIOLATION_REASON_TABLE =
            "DROP TABLE IF EXISTS " + Travel.RuleViolationReasonColumns.TABLE_NAME + ";";
    // Creates 5 indices on the Rule Violation Reason table.
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_TRIP_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleViolationReasonColumns.TABLE_NAME + "_INDEX_1 ON "
                    + Travel.RuleViolationReasonColumns.TABLE_NAME + "("
                    + Travel.RuleViolationReasonColumns.TRIP_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_CAR_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleViolationReasonColumns.TABLE_NAME + "_INDEX_2 ON "
                    + Travel.RuleViolationReasonColumns.TABLE_NAME + "("
                    + Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_HOTEL_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleViolationReasonColumns.TABLE_NAME + "_INDEX_3 ON "
                    + Travel.RuleViolationReasonColumns.TABLE_NAME + "("
                    + Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_FLIGHT_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleViolationReasonColumns.TABLE_NAME + "_INDEX_4 ON "
                    + Travel.RuleViolationReasonColumns.TABLE_NAME + "("
                    + Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID + ")";
    protected static final String SCHEMA_CREATE_RULE_VIOLATION_REASON_RAIL_RULE_ID_INDEX =
            "CREATE INDEX " + Travel.RuleViolationReasonColumns.TABLE_NAME + "_INDEX_5 ON "
                    + Travel.RuleViolationReasonColumns.TABLE_NAME + "("
                    + Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID + ")";
    // Create the Trip Rule Violation deletion trigger.
    protected static final String SCHEMA_CREATE_TRIP_RULE_VIOLATION_TRIGGER = //
            "CREATE TRIGGER ON_TRIP_RULE_VIOLATION_DELETION AFTER DELETE ON " //
                    + Travel.TripRuleViolationColumns.TABLE_NAME //
                    + " BEGIN " //
                    + "DELETE FROM " + Travel.RuleColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleColumns.TRIP_RULE_VIOLATION_ID + " = old._ID; " //
                    + "DELETE FROM " + Travel.RuleViolationReasonColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleViolationReasonColumns.TRIP_RULE_VIOLATION_ID + " = old._ID;" //
                    + " END"; //
    // Create the Car Rule Violation deletion trigger.
    protected static final String SCHEMA_CREATE_CAR_RULE_VIOLATION_TRIGGER = //
            "CREATE TRIGGER ON_CAR_RULE_VIOLATION_DELETION AFTER DELETE ON " //
                    + Travel.CarRuleViolationColumns.TABLE_NAME //
                    + " BEGIN " //
                    + "DELETE FROM " + Travel.RuleColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleColumns.CAR_RULE_VIOLATION_ID + " = old._ID; " //
                    + "DELETE FROM " + Travel.RuleViolationReasonColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleViolationReasonColumns.CAR_RULE_VIOLATION_ID + " = old._ID;" //
                    + " END"; //
    // Create the Hotel Rule Violation deletion trigger.
    protected static final String SCHEMA_CREATE_HOTEL_RULE_VIOLATION_TRIGGER = //
            "CREATE TRIGGER ON_HOTEL_RULE_VIOLATION_DELETION AFTER DELETE ON " //
                    + Travel.HotelRuleViolationColumns.TABLE_NAME //
                    + " BEGIN " //
                    + "DELETE FROM " + Travel.RuleColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleColumns.HOTEL_RULE_VIOLATION_ID + " = old._ID; " //
                    + "DELETE FROM " + Travel.RuleViolationReasonColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleViolationReasonColumns.HOTEL_RULE_VIOLATION_ID + " = old._ID;" //
                    + " END"; //
    // Create the Flight Rule Violation deletion trigger.
    protected static final String SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_TRIGGER = //
            "CREATE TRIGGER ON_FLIGHT_RULE_VIOLATION_DELETION AFTER DELETE ON " //
                    + Travel.FlightRuleViolationColumns.TABLE_NAME //
                    + " BEGIN " //
                    + "DELETE FROM " + Travel.RuleColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleColumns.FLIGHT_RULE_VIOLATION_ID + " = old._ID; " //
                    + "DELETE FROM " + Travel.RuleViolationReasonColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleViolationReasonColumns.FLIGHT_RULE_VIOLATION_ID + " = old._ID;" //
                    + " END"; //
    // Create the Rail Rule Violation deletion trigger.
    protected static final String SCHEMA_CREATE_RAIL_RULE_VIOLATION_TRIGGER = //
            "CREATE TRIGGER ON_RAIL_RULE_VIOLATION_DELETION AFTER DELETE ON " //
                    + Travel.RailRuleViolationColumns.TABLE_NAME //
                    + " BEGIN " //
                    + "DELETE FROM " + Travel.RuleColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleColumns.RAIL_RULE_VIOLATION_ID + " = old._ID; " //
                    + "DELETE FROM " + Travel.RuleViolationReasonColumns.TABLE_NAME + " WHERE " //
                    + Travel.RuleViolationReasonColumns.RAIL_RULE_VIOLATION_ID + " = old._ID;" //
                    + " END"; //
    // Creates the Travel Point table.
    protected static final String SCHEMA_CREATE_TRAVEL_POINT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.TravelPointColumns.TABLE_NAME + " (" + Travel.TravelPointColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.TravelPointColumns.TRIP_ID
                    + " INTEGER REFERENCES " + Travel.TripColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.TravelPointColumns.SEGMENT_ID + " INTEGER REFERENCES " + Travel.SegmentColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.TravelPointColumns.BENCHMARK + " TEXT, "
                    + Travel.TravelPointColumns.BENCHMARK_CURRENCY + " TEXT, " + Travel.TravelPointColumns.POINTS_POSTED
                    + " TEXT, " + Travel.TravelPointColumns.POINTS_PENDING + " TEXT, "
                    + Travel.TravelPointColumns.TOTAL_POINTS + " TEXT " + ")";
    // Drop the Travel Point table.
    protected static final String DROP_TRAVEL_POINT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.TravelPointColumns.TABLE_NAME + ";";
    // Creates the TravelPoint trip id index.
    protected static final String SCHEMA_CREATE_TRAVEL_POINT_TRIP_ID_INDEX =
            "CREATE INDEX " + Travel.TravelPointColumns.TABLE_NAME + "_TRIP_ID_INDEX ON "
                    + Travel.TravelPointColumns.TABLE_NAME + "(" + Travel.TravelPointColumns.TRIP_ID + ")";
    // Creates the TravelPoint segment id index.
    protected static final String SCHEMA_CREATE_TRAVEL_POINT_SEGMENT_ID_INDEX =
            "CREATE INDEX " + Travel.TravelPointColumns.TABLE_NAME + "_SEGMENT_ID__INDEX ON "
                    + Travel.TravelPointColumns.TABLE_NAME + "(" + Travel.TravelPointColumns.SEGMENT_ID + ")";
    // Creates the Booking table.
    protected static final String SCHEMA_CREATE_BOOKING_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.BookingColumns.TABLE_NAME + " (" + Travel.BookingColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.BookingColumns.TRIP_ID + " INTEGER REFERENCES "
                    + Travel.TripColumns.TABLE_NAME + " ON DELETE CASCADE, " + Travel.BookingColumns.AGENCY_PCC
                    + " TEXT, " + Travel.BookingColumns.BOOKING_SOURCE + " TEXT, "
                    + Travel.BookingColumns.COMPANY_ACCOUNTING_CODE + " TEXT, "
                    + Travel.BookingColumns.DATE_BOOKED_LOCAL + " TEXT, "
                    + Travel.BookingColumns.IS_CLIQBOOK_SYSTEM_OF_RECORD + " INTEGER, "
                    + Travel.BookingColumns.RECORD_LOCATOR + " TEXT, " + Travel.BookingColumns.TRAVEL_CONFIG_ID
                    + " TEXT, " + Travel.BookingColumns.TYPE + " TEXT, " + Travel.BookingColumns.IS_GDS_BOOKING
                    + " INTEGER " + ")";
    // Drop the Booking table.
    protected static final String DROP_BOOKING_TABLE = "DROP TABLE IF EXISTS " + Travel.BookingColumns.TABLE_NAME + ";";
    // Creates the Booking index.
    protected static final String SCHEMA_CREATE_BOOKING_INDEX =
            "CREATE INDEX " + Travel.BookingColumns.TABLE_NAME + "_INDEX ON " + Travel.BookingColumns.TABLE_NAME + "("
                    + Travel.BookingColumns.TRIP_ID + ")";
    // Creates the Airline Ticket table.
    protected static final String SCHEMA_CREATE_AIRLINE_TICKET_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.AirlineTicketColumns.TABLE_NAME + " ("
                    + Travel.AirlineTicketColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.AirlineTicketColumns.BOOKING_ID + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME
                    + " ON DELETE CASCADE" + ")";
    // Drop the Airline Ticket table.
    protected static final String DROP_AIRLINE_TICKET_TABLE =
            "DROP TABLE IF EXISTS " + Travel.AirlineTicketColumns.TABLE_NAME + ";";
    // Creates the AirlineTicket index.
    protected static final String SCHEMA_CREATE_AIRLINE_TICKET_INDEX =
            "CREATE INDEX " + Travel.AirlineTicketColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.AirlineTicketColumns.TABLE_NAME + "(" + Travel.AirlineTicketColumns.BOOKING_ID + ")";
    // Creates the Passenger table.
    protected static final String SCHEMA_CREATE_PASSENGER_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.PassengerColumns.TABLE_NAME + " (" + Travel.PassengerColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.PassengerColumns.BOOKING_ID
                    + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.PassengerColumns.FIRST_NAME + " TEXT, " + Travel.PassengerColumns.LAST_NAME + " TEXT, "
                    + Travel.PassengerColumns.IDENTIFIER + " TEXT, " + Travel.PassengerColumns.PASSENGER_KEY + " TEXT "
                    + ")";
    // Drop the Passenger table.
    protected static final String DROP_PASSENGER_TABLE =
            "DROP TABLE IF EXISTS " + Travel.PassengerColumns.TABLE_NAME + ";";
    // Creates the Passenger index.
    protected static final String SCHEMA_CREATE_PASSENGER_INDEX =
            "CREATE INDEX " + Travel.PassengerColumns.TABLE_NAME + "_INDEX ON " + Travel.PassengerColumns.TABLE_NAME
                    + "(" + Travel.PassengerColumns.BOOKING_ID + ")";
    // Creates the Frequent Traveler Program table.
    protected static final String SCHEMA_CREATE_FREQUENT_TRAVELER_PROGRAM_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.FrequentTravelerProgramColumns.TABLE_NAME + " ("
                    + Travel.FrequentTravelerProgramColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.FrequentTravelerProgramColumns.PASSENGER_ID + " INTEGER REFERENCES "
                    + Travel.PassengerColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.FrequentTravelerProgramColumns.AIRLINE_VENDOR + " TEXT, "
                    + Travel.FrequentTravelerProgramColumns.PROGRAM_NUMBER + " TEXT, "
                    + Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR + " TEXT, "
                    + Travel.FrequentTravelerProgramColumns.PROGRAM_VENDOR_CODE + " TEXT, "
                    + Travel.FrequentTravelerProgramColumns.STATUS + " TEXT " + ")";
    // Drop the Frequent Traveler Program table.
    protected static final String DROP_FREQUENT_TRAVELER_PROGRAM_TABLE =
            "DROP TABLE IF EXISTS " + Travel.FrequentTravelerProgramColumns.TABLE_NAME + ";";
    // Creates the Frequent Traveler Program index.
    protected static final String SCHEMA_CREATE_FREQUENT_TRAVELER_PROGRAM_INDEX =
            "CREATE INDEX " + Travel.FrequentTravelerProgramColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.FrequentTravelerProgramColumns.TABLE_NAME + "("
                    + Travel.FrequentTravelerProgramColumns.PASSENGER_ID + ")";
    // Creates the Segment table.
    protected static final String SCHEMA_CREATE_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.SegmentColumns.TABLE_NAME + " (" + Travel.SegmentColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.SegmentColumns.BOOKING_ID + " INTEGER REFERENCES "
                    + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, " + Travel.SegmentColumns.START_DATE_UTC
                    + " TEXT, " + Travel.SegmentColumns.END_DATE_UTC + " TEXT, "
                    + Travel.SegmentColumns.START_DATE_LOCAL + " TEXT, " + Travel.SegmentColumns.END_DATE_LOCAL
                    + " TEXT, " + Travel.SegmentColumns.CONFIRMATION_NUMBER + " TEXT, "
                    + Travel.SegmentColumns.CREDIT_CARD_ID + " TEXT, " + Travel.SegmentColumns.CREDIT_CARD_LAST_FOUR
                    + " TEXT, " + Travel.SegmentColumns.CREDIT_CARD_TYPE + " TEXT, "
                    + Travel.SegmentColumns.CREDIT_CARD_TYPE_LOCALIZED + " TEXT, " + Travel.SegmentColumns.CURRENCY
                    + " TEXT, " + Travel.SegmentColumns.ERECEIPT_STATUS + " TEXT, " + Travel.SegmentColumns.END_ADDRESS
                    + " TEXT, " + Travel.SegmentColumns.END_ADDRESS_2 + " TEXT, " + Travel.SegmentColumns.END_CITY
                    + " TEXT, " + Travel.SegmentColumns.END_CITY_CODE + " TEXT, "
                    + Travel.SegmentColumns.END_CITY_CODE_LOCALIZED + " TEXT, " + Travel.SegmentColumns.END_COUNTRY
                    + " TEXT, " + Travel.SegmentColumns.END_COUNTRY_CODE + " TEXT, "
                    + Travel.SegmentColumns.END_LATITUDE + " REAL, " + Travel.SegmentColumns.END_LONGITUDE + " REAL, "
                    + Travel.SegmentColumns.END_POSTAL_CODE + " TEXT, " + Travel.SegmentColumns.END_STATE + " TEXT, "
                    + Travel.SegmentColumns.FREQUENT_TRAVELER_ID + " TEXT, " + Travel.SegmentColumns.IMAGE_VENDOR_URI
                    + " TEXT, " + Travel.SegmentColumns.NUM_PERSONS + " INTEGER, "
                    + Travel.SegmentColumns.OPERATED_BY_VENDOR + " TEXT, "
                    + Travel.SegmentColumns.OPERATED_BY_VENDOR_NAME + " TEXT, " + Travel.SegmentColumns.PHONE_NUMBER
                    + " TEXT, " + Travel.SegmentColumns.RATE_CODE + " TEXT, " + Travel.SegmentColumns.SEGMENT_KEY
                    + " TEXT, " + Travel.SegmentColumns.SEGMENT_LOCATOR + " TEXT, " + Travel.SegmentColumns.SEGMENT_NAME
                    + " TEXT, " + Travel.SegmentColumns.START_ADDRESS + " TEXT, "
                    + Travel.SegmentColumns.START_ADDRESS_2 + " TEXT, " + Travel.SegmentColumns.START_CITY + " TEXT, "
                    + Travel.SegmentColumns.START_CITY_CODE + " TEXT, " + Travel.SegmentColumns.START_COUNTRY
                    + " TEXT, " + Travel.SegmentColumns.START_COUNTRY_CODE + " TEXT, "
                    + Travel.SegmentColumns.START_LATITUDE + " REAL, " + Travel.SegmentColumns.START_LONGITUDE
                    + " REAL, " + Travel.SegmentColumns.START_POSTAL_CODE + " TEXT, "
                    + Travel.SegmentColumns.START_STATE + " TEXT, " + Travel.SegmentColumns.STATUS + " TEXT, "
                    + Travel.SegmentColumns.STATUS_LOCALIZED + " TEXT, " + Travel.SegmentColumns.TIMEZONE_ID + " TEXT, "
                    + Travel.SegmentColumns.TOTAL_RATE + " REAL, " + Travel.SegmentColumns.TYPE_LOCALIZED + " TEXT, "
                    + Travel.SegmentColumns.VENDOR + " TEXT, " + Travel.SegmentColumns.VENDOR_NAME + " TEXT, "
                    + Travel.SegmentColumns.VENDOR_URL + " TEXT, " + Travel.SegmentColumns.ETICKET + " TEXT, "
                    + Travel.SegmentColumns.TYPE + " TEXT, " + Travel.SegmentColumns.ID_KEY + " TEXT " + ")";
    // Drop the Segment table.
    protected static final String DROP_SEGMENT_TABLE = "DROP TABLE IF EXISTS " + Travel.SegmentColumns.TABLE_NAME + ";";
    // Creates the Segment index.
    protected static final String SCHEMA_CREATE_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.SegmentColumns.TABLE_NAME + "_INDEX ON " + Travel.SegmentColumns.TABLE_NAME + "("
                    + Travel.SegmentColumns.BOOKING_ID + ")";
    // Creates the Air Segment table.
    protected static final String SCHEMA_CREATE_AIR_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.AirSegmentColumns.TABLE_NAME + " (" + Travel.AirSegmentColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.AirSegmentColumns.BOOKING_ID
                    + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.AirSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES " + Travel.SegmentColumns.TABLE_NAME
                    + ", " + Travel.AirSegmentColumns.AIRCRAFT_CODE + " TEXT, " + Travel.AirSegmentColumns.AIRCRAFT_NAME
                    + " TEXT, " + Travel.AirSegmentColumns.CABIN + " TEXT, " + Travel.AirSegmentColumns.CHECKED_BAGGAGE
                    + " TEXT, " + Travel.AirSegmentColumns.CLASS_OF_SERVICE + " TEXT, "
                    + Travel.AirSegmentColumns.CLASS_OF_SERVICE_LOCALIZED + " TEXT, "
                    + Travel.AirSegmentColumns.DURATION + " INTEGER, " + Travel.AirSegmentColumns.END_AIRPORT_CITY
                    + " TEXT, " + Travel.AirSegmentColumns.END_AIRPORT_COUNTRY + " TEXT, "
                    + Travel.AirSegmentColumns.END_AIRPORT_COUNTRY_CODE + " TEXT, "
                    + Travel.AirSegmentColumns.END_AIRPORT_NAME + " TEXT, " + Travel.AirSegmentColumns.END_AIRPORT_STATE
                    + " TEXT, " + Travel.AirSegmentColumns.END_GATE + " TEXT, " + Travel.AirSegmentColumns.END_TERMINAL
                    + " TEXT, " + Travel.AirSegmentColumns.FARE_BASIS_CODE + " TEXT, "
                    + Travel.AirSegmentColumns.FLIGHT_NUMBER + " TEXT, " + Travel.AirSegmentColumns.LEG_ID
                    + " INTEGER, " + Travel.AirSegmentColumns.MEALS + " TEXT, " + Travel.AirSegmentColumns.MILES
                    + " INTEGER, " + Travel.AirSegmentColumns.NUM_STOPS + " INTEGER, "
                    + Travel.AirSegmentColumns.OPERATED_BY_FLIGHT_NUMBER + " TEXT, "
                    + Travel.AirSegmentColumns.SPECIAL_INSTRUCTIONS + " TEXT, "
                    + Travel.AirSegmentColumns.START_AIRPORT_CITY + " TEXT, "
                    + Travel.AirSegmentColumns.START_AIRPORT_COUNTRY + " TEXT, "
                    + Travel.AirSegmentColumns.START_AIRPORT_COUNTRY_CODE + " TEXT, "
                    + Travel.AirSegmentColumns.START_AIRPORT_NAME + " TEXT, "
                    + Travel.AirSegmentColumns.START_AIRPORT_STATE + " TEXT, " + Travel.AirSegmentColumns.START_GATE
                    + " TEXT, " + Travel.AirSegmentColumns.START_TERMINAL + " TEXT " + ")";
    // Drop the Air Segment table.
    protected static final String DROP_AIR_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.AirSegmentColumns.TABLE_NAME + ";";
    // Creates the Air Segment index.
    protected static final String SCHEMA_CREATE_AIR_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.AirSegmentColumns.TABLE_NAME + "_INDEX ON " + Travel.AirSegmentColumns.TABLE_NAME
                    + "(" + Travel.AirSegmentColumns.BOOKING_ID + ")";
    // Creates the Flight Status table.
    protected static final String SCHEMA_CREATE_FLIGHT_STATUS_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.FlightStatusColumns.TABLE_NAME + " ("
                    + Travel.FlightStatusColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.FlightStatusColumns.AIR_SEGMENT_ID + " INTEGER REFERENCES "
                    + Travel.AirSegmentColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.FlightStatusColumns.EQUIPMENT_SCHEDULED + " TEXT, "
                    + Travel.FlightStatusColumns.EQUIPMENT_ACTUAL + " TEXT, "
                    + Travel.FlightStatusColumns.EQUIPMENT_REGISTRATION + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_TERMINAL_SCHEDULED + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_TERMINAL_ACTUAL + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_GATE + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_SCHEDULED + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_ESTIMATED + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_ACTUAL + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_STATUS_REASON + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_SHORT_STATUS + " TEXT, "
                    + Travel.FlightStatusColumns.DEPARTURE_LONG_STATUS + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_TERMINAL_SCHEDULED + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_TERMINAL_ACTUAL + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_GATE + " TEXT, " + Travel.FlightStatusColumns.ARRIVAL_SCHEDULED
                    + " TEXT, " + Travel.FlightStatusColumns.ARRIVAL_ESTIMATED + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_ACTUAL + " TEXT, " + Travel.FlightStatusColumns.BAGGAGE_CLAIM
                    + " TEXT, " + Travel.FlightStatusColumns.DIVERSION_CITY + " TEXT, "
                    + Travel.FlightStatusColumns.DIVERSION_AIRPORT + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_STATUS_REASON + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_SHORT_STATUS + " TEXT, "
                    + Travel.FlightStatusColumns.ARRIVAL_LONG_STATUS + " TEXT, "
                    + Travel.FlightStatusColumns.CLIQBOOK_MESSAGE + " TEXT, "
                    + Travel.FlightStatusColumns.LAST_UPDATED_UTC + " TEXT " + ")";
    // Drop the Flight Status table.
    protected static final String DROP_FLIGHT_STATUS_TABLE =
            "DROP TABLE IF EXISTS " + Travel.FlightStatusColumns.TABLE_NAME + ";";
    // Creates the Flight Status index.
    protected static final String SCHEMA_CREATE_FLIGHT_STATUS_INDEX =
            "CREATE INDEX " + Travel.FlightStatusColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.FlightStatusColumns.TABLE_NAME + "(" + Travel.FlightStatusColumns.AIR_SEGMENT_ID + ")";
    // Creates the Seat table.
    protected static final String SCHEMA_CREATE_SEAT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.SeatColumns.TABLE_NAME + " (" + Travel.SeatColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.SeatColumns.AIR_SEGMENT_ID
                    + " INTEGER REFERENCES " + Travel.AirSegmentColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.SeatColumns.PASSENGER_RPH + " TEXT, " + Travel.SeatColumns.SEAT_NUMBER + " TEXT, "
                    + Travel.SeatColumns.STATUS_CODE + " TEXT " + ")";
    // Drop the Seat table.
    protected static final String DROP_SEAT_TABLE = "DROP TABLE IF EXISTS " + Travel.SeatColumns.TABLE_NAME + ";";
    // Creates the Seat index.
    protected static final String SCHEMA_CREATE_SEAT_INDEX =
            "CREATE INDEX " + Travel.SeatColumns.TABLE_NAME + "_INDEX ON " + Travel.SeatColumns.TABLE_NAME + "("
                    + Travel.SeatColumns.AIR_SEGMENT_ID + ")";
    // Creates the Hotel Segment table.
    protected static final String SCHEMA_CREATE_HOTEL_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelSegmentColumns.TABLE_NAME + " ("
                    + Travel.HotelSegmentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelSegmentColumns.BOOKING_ID + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.HotelSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES "
                    + Travel.SegmentColumns.TABLE_NAME + ", " + Travel.HotelSegmentColumns.CHECK_IN_TIME + " TEXT, "
                    + Travel.HotelSegmentColumns.CHECK_OUT_TIME + " TEXT, " + Travel.HotelSegmentColumns.DISCOUNT_CODE
                    + " TEXT, " + Travel.HotelSegmentColumns.NUM_ROOMS + " TEXT, "
                    + Travel.HotelSegmentColumns.RATE_CODE + " TEXT, " + Travel.HotelSegmentColumns.ROOM_TYPE
                    + " TEXT, " + Travel.HotelSegmentColumns.ROOM_TYPE_LOCALIZED + " TEXT, "
                    + Travel.HotelSegmentColumns.DAILY_RATE + " REAL, " + Travel.HotelSegmentColumns.TOTAL_RATE
                    + " REAL, " + Travel.HotelSegmentColumns.CANCELLATION_POLICY + " TEXT, "
                    + Travel.HotelSegmentColumns.SPECIAL_INSTRUCTIONS + " TEXT, "
                    + Travel.HotelSegmentColumns.ROOM_DESCRIPTION + " TEXT, " + Travel.HotelSegmentColumns.RATE_TYPE
                    + " TEXT, " + Travel.HotelSegmentColumns.PROPERTY_ID + " TEXT, "
                    + Travel.HotelSegmentColumns.PROPERTY_IMAGE_COUNT + " INTEGER " + ")";
    // Drop the Hotel Segment table.
    protected static final String DROP_HOTEL_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelSegmentColumns.TABLE_NAME + ";";
    // Creates the Hotel Segment index.
    protected static final String SCHEMA_CREATE_HOTEL_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.HotelSegmentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.HotelSegmentColumns.TABLE_NAME + "(" + Travel.HotelSegmentColumns.BOOKING_ID + ")";
    // Creates the Car Segment table.
    protected static final String SCHEMA_CREATE_CAR_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.CarSegmentColumns.TABLE_NAME + " (" + Travel.CarSegmentColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.CarSegmentColumns.BOOKING_ID
                    + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.CarSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES " + Travel.SegmentColumns.TABLE_NAME
                    + ", " + Travel.CarSegmentColumns.AIR_CONDITION + " TEXT, "
                    + Travel.CarSegmentColumns.AIR_CONDITION_LOCALIZED + " TEXT, " + Travel.CarSegmentColumns.BODY
                    + " TEXT, " + Travel.CarSegmentColumns.BODY_LOCALIZED + " TEXT, "
                    + Travel.CarSegmentColumns.CLASS_OF_CAR + " TEXT, "
                    + Travel.CarSegmentColumns.CLASS_OF_CAR_LOCALIZED + " TEXT, " + Travel.CarSegmentColumns.DAILY_RATE
                    + " REAL, " + Travel.CarSegmentColumns.DISCOUNT_CODE + " TEXT, "
                    + Travel.CarSegmentColumns.END_AIRPORT_CITY + " TEXT, "
                    + Travel.CarSegmentColumns.END_AIRPORT_COUNTRY + " TEXT, "
                    + Travel.CarSegmentColumns.END_AIRPORT_COUNTRY_CODE + " TEXT, "
                    + Travel.CarSegmentColumns.END_AIRPORT_NAME + " TEXT, " + Travel.CarSegmentColumns.END_AIRPORT_STATE
                    + " TEXT, " + Travel.CarSegmentColumns.END_LOCATION + " TEXT, "
                    + Travel.CarSegmentColumns.IMAGE_CAR_URI + " TEXT, " + Travel.CarSegmentColumns.NUM_CARS
                    + " INTEGER, " + Travel.CarSegmentColumns.RATE_TYPE + " TEXT, "
                    + Travel.CarSegmentColumns.SPECIAL_EQUIPMENT + " TEXT, "
                    + Travel.CarSegmentColumns.START_AIRPORT_CITY + " TEXT, "
                    + Travel.CarSegmentColumns.START_AIRPORT_COUNTRY + " TEXT, "
                    + Travel.CarSegmentColumns.START_AIRPORT_COUNTRY_CODE + " TEXT, "
                    + Travel.CarSegmentColumns.START_AIRPORT_NAME + " TEXT, "
                    + Travel.CarSegmentColumns.START_AIRPORT_STATE + " TEXT, " + Travel.CarSegmentColumns.START_LOCATION
                    + " TEXT, " + Travel.CarSegmentColumns.TRANSMISSION + " TEXT, "
                    + Travel.CarSegmentColumns.TRANSMISSION_LOCALIZED + " TEXT " + ")";
    // Drop the Car Segment table.
    protected static final String DROP_CAR_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.CarSegmentColumns.TABLE_NAME + ";";
    // Creates the Car Segment index.
    protected static final String SCHEMA_CREATE_CAR_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.CarSegmentColumns.TABLE_NAME + "_INDEX ON " + Travel.CarSegmentColumns.TABLE_NAME
                    + "(" + Travel.CarSegmentColumns.BOOKING_ID + ")";
    // Creates the Rail Segment table.
    protected static final String SCHEMA_CREATE_RAIL_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.RailSegmentColumns.TABLE_NAME + " (" + Travel.RailSegmentColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.RailSegmentColumns.BOOKING_ID
                    + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.RailSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES " + Travel.SegmentColumns.TABLE_NAME
                    + ", " + Travel.RailSegmentColumns.AMENITIES + " TEXT, " + Travel.RailSegmentColumns.CABIN
                    + " TEXT, " + Travel.RailSegmentColumns.CLASS_OF_SERVICE + " TEXT, "
                    + Travel.RailSegmentColumns.DISCOUNT_CODE + " TEXT, " + Travel.RailSegmentColumns.DURATION
                    + " INTEGER, " + Travel.RailSegmentColumns.END_PLATFORM + " TEXT, "
                    + Travel.RailSegmentColumns.END_RAIL_STATION + " TEXT, "
                    + Travel.RailSegmentColumns.END_RAIL_STATION_LOCALIZED + " TEXT, "
                    + Travel.RailSegmentColumns.LEG_ID + " INTEGER, " + Travel.RailSegmentColumns.MEALS + " TEXT, "
                    + Travel.RailSegmentColumns.MILES + " INTEGER, " + Travel.RailSegmentColumns.NUM_STOPS
                    + " INTEGER, " + Travel.RailSegmentColumns.OPERATED_BY_TRAIN_NUMBER + " TEXT, "
                    + Travel.RailSegmentColumns.PIN + " TEXT, " + Travel.RailSegmentColumns.START_PLATFORM + " TEXT, "
                    + Travel.RailSegmentColumns.START_RAIL_STATION + " TEXT, "
                    + Travel.RailSegmentColumns.START_RAIL_STATION_LOCALIZED + " TEXT, "
                    + Travel.RailSegmentColumns.TRAIN_NUMBER + " TEXT, " + Travel.RailSegmentColumns.TRAIN_TYPE_CODE
                    + " TEXT, " + Travel.RailSegmentColumns.WAGON_NUMBER + " TEXT " + ")";
    // Drop the Rail Segment table.
    protected static final String DROP_RAIL_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.RailSegmentColumns.TABLE_NAME + ";";
    // Creates the RailSegment index.
    protected static final String SCHEMA_CREATE_RAIL_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.RailSegmentColumns.TABLE_NAME + "_INDEX ON " + Travel.RailSegmentColumns.TABLE_NAME
                    + "(" + Travel.RailSegmentColumns.BOOKING_ID + ")";
    // Creates the Dining Segment table.
    protected static final String SCHEMA_CREATE_DINING_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.DiningSegmentColumns.TABLE_NAME + " ("
                    + Travel.DiningSegmentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.DiningSegmentColumns.BOOKING_ID + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.DiningSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES "
                    + Travel.SegmentColumns.TABLE_NAME + ", " + Travel.DiningSegmentColumns.RESERVATION_ID + " TEXT "
                    + ")";
    // Drop the Dining Segment table.
    protected static final String DROP_DINING_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.DiningSegmentColumns.TABLE_NAME + ";";
    // Creates the Dining Segment index.
    protected static final String SCHEMA_CREATE_DINING_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.DiningSegmentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.DiningSegmentColumns.TABLE_NAME + "(" + Travel.DiningSegmentColumns.BOOKING_ID + ")";
    // Creates the Event Segment table.
    protected static final String SCHEMA_CREATE_EVENT_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.EventSegmentColumns.TABLE_NAME + " ("
                    + Travel.EventSegmentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.EventSegmentColumns.BOOKING_ID + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME
                    + " ON DELETE CASCADE, " + Travel.EventSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES "
                    + Travel.SegmentColumns.TABLE_NAME + " " + ")";
    // Drop the Event Segment table.
    protected static final String DROP_EVENT_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.EventSegmentColumns.TABLE_NAME + ";";
    // Creates the Event Segment index.
    protected static final String SCHEMA_CREATE_EVENT_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.EventSegmentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.EventSegmentColumns.TABLE_NAME + "(" + Travel.EventSegmentColumns.BOOKING_ID + ")";
    // Creates the Parking Segment table.
    protected static final String SCHEMA_CREATE_PARKING_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.ParkingSegmentColumns.TABLE_NAME + " ("
                    + Travel.ParkingSegmentColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.ParkingSegmentColumns.BOOKING_ID + " INTEGER REFERENCES "
                    + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.ParkingSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES "
                    + Travel.SegmentColumns.TABLE_NAME + ", " + Travel.ParkingSegmentColumns.PARKING_LOCATION_ID
                    + " TEXT, " + Travel.ParkingSegmentColumns.PIN + " TEXT, "
                    + Travel.ParkingSegmentColumns.START_LOCATION + " TEXT " + ")";
    // Drop the Parking Segment table.
    protected static final String DROP_PARKING_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.ParkingSegmentColumns.TABLE_NAME + ";";
    // Creates the ParkingSegment index.
    protected static final String SCHEMA_CREATE_PARKING_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.ParkingSegmentColumns.TABLE_NAME + "_INDEX ON "
                    + Travel.ParkingSegmentColumns.TABLE_NAME + "(" + Travel.ParkingSegmentColumns.BOOKING_ID + ")";
    // Creates the Ride Segment table.
    protected static final String SCHEMA_CREATE_RIDE_SEGMENT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.RideSegmentColumns.TABLE_NAME + " (" + Travel.RideSegmentColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.RideSegmentColumns.BOOKING_ID
                    + " INTEGER REFERENCES " + Travel.BookingColumns.TABLE_NAME + " ON DELETE CASCADE, "
                    + Travel.RideSegmentColumns.SEGMENT_ID + " INTEGER REFERENCES " + Travel.SegmentColumns.TABLE_NAME
                    + ", " + Travel.RideSegmentColumns.CANCELLATION_POLICY + " TEXT, "
                    + Travel.RideSegmentColumns.DROP_OFF_INSTRUCTIONS + " TEXT, " + Travel.RideSegmentColumns.DURATION
                    + " INTEGER, " + Travel.RideSegmentColumns.MEETING_INSTRUCTIONS + " TEXT, "
                    + Travel.RideSegmentColumns.MILES + " INTEGER, " + Travel.RideSegmentColumns.NUMBER_OF_HOURS
                    + " REAL, " + Travel.RideSegmentColumns.PICK_UP_INSTRUCTIONS + " TEXT, "
                    + Travel.RideSegmentColumns.RATE + " REAL, " + Travel.RideSegmentColumns.RATE_DESCRIPTION
                    + " TEXT, " + Travel.RideSegmentColumns.RATE_TYPE + " TEXT, "
                    + Travel.RideSegmentColumns.START_LOCATION + " TEXT, "
                    + Travel.RideSegmentColumns.START_LOCATION_CODE + " TEXT, "
                    + Travel.RideSegmentColumns.START_LOCATION_NAME + " TEXT " + ")";
    // Drop the Ride Segment table.
    protected static final String DROP_RIDE_SEGMENT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.RideSegmentColumns.TABLE_NAME + ";";
    // Creates the RideSegment index.
    protected static final String SCHEMA_CREATE_RIDE_SEGMENT_INDEX =
            "CREATE INDEX " + Travel.RideSegmentColumns.TABLE_NAME + "_INDEX ON " + Travel.RideSegmentColumns.TABLE_NAME
                    + "(" + Travel.RideSegmentColumns.BOOKING_ID + ")";
    // Creates the Location Search table.
    protected static final String SCHEMA_CREATE_LOCATION_CHOICE_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.LocationChoiceColumns.TABLE_NAME + " ("
                    + Travel.LocationChoiceColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.LocationChoiceColumns.CITY + " TEXT, " + Travel.LocationChoiceColumns.COUNTRY + " TEXT, "
                    + Travel.LocationChoiceColumns.COUNTRY_ABBREVIATION + " TEXT, " + Travel.LocationChoiceColumns.IATA
                    + " TEXT, " + Travel.LocationChoiceColumns.LOCATION + " TEXT, " + Travel.LocationChoiceColumns.STATE
                    + " TEXT, " + Travel.LocationChoiceColumns.LAT + " REAL, " + Travel.LocationChoiceColumns.LON
                    + " REAL " + ")";
    // Drop the Location Choice table.
    protected static final String DROP_LOCATION_CHOICE_TABLE =
            "DROP TABLE IF EXISTS " + Travel.LocationChoiceColumns.TABLE_NAME + ";";
    // Creates Hotel Search Result table.
    protected static final String SCHEMA_CREATE_HOTEL_SEARCH_RESULT_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelSearchResultColumns.TABLE_NAME + " ("
                    + Travel.HotelSearchResultColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
                    + Travel.HotelSearchResultColumns.DISTANCE_UNIT + " TEXT, "
                    + Travel.HotelSearchResultColumns.CURRENCY + " TEXT, "
                    + Travel.HotelSearchResultColumns.SEARCH_CRITERIA_URL + " TEXT NOT  NULL UNIQUE , "
                    + Travel.HotelSearchResultColumns.INSERT_DATETIME + " DEFAULT CURRENT_TIMESTAMP , "
                    + Travel.HotelSearchResultColumns.EXPIRY_DATETIME + " DEFAULT CURRENT_TIMESTAMP" + ")";
    // Drop the Hotel Search Result table.
    protected static final String DROP_HOTEL_SEARCH_RESULT_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelSearchResultColumns.TABLE_NAME + ";";
    // Creates the Hotel Detail table - will have a foreign key reference to Hotel Search Result table
    protected static final String SCHEMA_CREATE_HOTEL_DETAIL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelDetailColumns.TABLE_NAME + " (" + Travel.HotelDetailColumns._ID
                    + " INTEGER PRIMARY KEY AUTOINCREMENT, " + Travel.HotelDetailColumns.NAME + " TEXT, "
                    + Travel.HotelDetailColumns.CHAIN_NAME + " TEXT, " + Travel.HotelDetailColumns.CHAIN_CODE
                    + " TEXT, " + Travel.HotelDetailColumns.STREET + " TEXT, "
                    + Travel.HotelDetailColumns.ADDRESS_LINE_1 + " TEXT, " + Travel.HotelDetailColumns.CITY + " TEXT, "
                    + Travel.HotelDetailColumns.STATE + " TEXT, " + Travel.HotelDetailColumns.COUNTRY + " TEXT, "
                    + Travel.HotelDetailColumns.COUNTRY_CODE + " TEXT, " + Travel.HotelDetailColumns.PHONE + " TEXT, "
                    + Travel.HotelDetailColumns.TOLL_FREE_PHONE + " TEXT, " + Travel.HotelDetailColumns.ZIP + " TEXT, "
                    + Travel.HotelDetailColumns.DISTANCE + " REAL, " + Travel.HotelDetailColumns.PRICE_TO_BEAT
                    + " REAL, " + Travel.HotelDetailColumns.DISTANCE_UNIT + " TEXT, "
                    + Travel.HotelDetailColumns.LOWEST_RATE + " REAL, " + Travel.HotelDetailColumns.CURRENCY_CODE
                    + " TEXT, " + Travel.HotelDetailColumns.STAR_RATING + " TEXT, "
                    + Travel.HotelDetailColumns.COMPANY_PREFERENCE + " TEXT, "
                    + Travel.HotelDetailColumns.SUGESTED_CATEGORY + " TEXT, " + Travel.HotelDetailColumns.LAT
                    + " REAL, " + Travel.HotelDetailColumns.LON + " REAL, " + Travel.HotelDetailColumns.LOWEST_ENF_LEVEL
                    + " INTEGER, " + Travel.HotelDetailColumns.RATES_URL + " TEXT, "
                    + Travel.HotelDetailColumns.SUGESTED_SCORE + " REAL, " + Travel.HotelDetailColumns.THUMBNAIL_URL
                    + " TEXT, " + Travel.HotelDetailColumns.AVAILABILITY_ERROR_CODE + " TEXT, "
                    + Travel.HotelDetailColumns.TRAVEL_POINTS_FOR_LOWEST_RATE + " INTEGER, "
                    + Travel.HotelDetailColumns.PROPERTY_IDS + " BLOB, "
                    + Travel.HotelDetailColumns.HOTEL_SEARCH_RESULT_ID + " INTEGER REFERENCES "
                    + Travel.HotelSearchResultColumns.TABLE_NAME + " ON DELETE CASCADE )";
    // Drop the Hotel Detail table.
    protected static final String DROP_HOTEL_DETAIL_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelDetailColumns.TABLE_NAME + ";";
    // Creates the Hotel Image Pair table - will have a foreign key reference to Hotel Detail table
    protected static final String SCHEMA_CREATE_HOTEL_IMAGE_PAIR_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelImagePairColumns.TABLE_NAME + " ("
                    + Travel.HotelImagePairColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelImagePairColumns.IMAGE_URL + " TEXT, " + Travel.HotelImagePairColumns.THUMBNAIL_URL
                    + " TEXT, " + Travel.HotelImagePairColumns.HOTEL_DETAIL_ID + " INTEGER REFERENCES "
                    + Travel.HotelDetailColumns.TABLE_NAME + " ON DELETE CASCADE )";
    // Drop the Hotel Image Pair table
    protected static final String DROP_HOTEL_IMAGE_PAIR_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelImagePairColumns.TABLE_NAME + ";";
    // Creates the Hotel Rate Detail table - will have a foreign key reference to Hotel Detail table
    protected static final String SCHEMA_CREATE_HOTEL_RATE_DETAIL_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelRateDetailColumns.TABLE_NAME + " ("
                    + Travel.HotelRateDetailColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelRateDetailColumns.RATE_ID + " TEXT," + Travel.HotelRateDetailColumns.AMOUNT + " REAL,"
                    + Travel.HotelRateDetailColumns.CURRENCY_CODE + " TEXT," + Travel.HotelRateDetailColumns.SOURCE
                    + " TEXT," + Travel.HotelRateDetailColumns.ROOM_TYPE + " TEXT,"
                    + Travel.HotelRateDetailColumns.DESCRIPTION + " TEXT,"
                    + Travel.HotelRateDetailColumns.ESTIMATED_BED_TYPE + " TEXT,"
                    + Travel.HotelRateDetailColumns.GUARANTEE_SURCHARGE + " REAL,"
                    + Travel.HotelRateDetailColumns.RATE_CHANGES_OVERSTAY + " INTEGER,"
                    + Travel.HotelRateDetailColumns.MAX_ENF_LEVEL + " INTEGER,"
                    + Travel.HotelRateDetailColumns.SELL_OPTIONS_URL + " TEXT,"
                    + Travel.HotelRateDetailColumns.VIOLATION_VALUE_IDS + " TEXT,"
                    + Travel.HotelRateDetailColumns.TRAVEL_POINTS + " INTEGER,"
                    + Travel.HotelRateDetailColumns.CAN_REDEEM_TP_AGAINST_VIOLATIONS + " INTEGER,"
                    + Travel.HotelRateDetailColumns.HOTEL_DETAIL_ID + " INTEGER REFERENCES "
                    + Travel.HotelDetailColumns.TABLE_NAME + " ON DELETE CASCADE )";
    // Drop the Hotel Rate Detail table.
    protected static final String DROP_HOTEL_RATE_DETAIL_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelRateDetailColumns.TABLE_NAME + ";";

    // Creates the Hotel Violation table - will have a foreign key reference to Hotel Search Result table
    protected static final String SCHEMA_CREATE_HOTEL_VIOLATION_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelViolationColumns.TABLE_NAME + " ("
                    + Travel.HotelViolationColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelViolationColumns.ENFORCEMENT_LEVEL + " TEXT," + Travel.HotelViolationColumns.MESSAGE
                    + " TEXT," + Travel.HotelViolationColumns.VIOLATION_VALUE_ID + " TEXT,"
                    + Travel.HotelViolationColumns.HOTEL_SEARCH_RESULT_ID + " INTEGER REFERENCES "
                    + Travel.HotelSearchResultColumns.TABLE_NAME + " ON DELETE CASCADE )";

    // Creates the Hotel Benchmark table - will have a foreign key reference to Hotel Search Result table
    protected static final String SCHEMA_CREATE_HOTEL_BENCHMARK_TABLE =
            "CREATE TABLE IF NOT EXISTS " + Travel.HotelBenchmarkColumns.TABLE_NAME + " ("
                    + Travel.HotelBenchmarkColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + Travel.HotelBenchmarkColumns.LOCATION_NAME + " TEXT," + Travel.HotelBenchmarkColumns.CRN_CODE
                    + " TEXT," + Travel.HotelBenchmarkColumns.PRICE + " REAL,"
                    + Travel.HotelBenchmarkColumns.SUB_DIV_CODE + " TEXT,"
                    + Travel.HotelBenchmarkColumns.HOTEL_SEARCH_RESULT_ID + " INTEGER REFERENCES "
                    + Travel.HotelSearchResultColumns.TABLE_NAME + " ON DELETE CASCADE )";

    // Drop the Hotel Benchmark table.
    protected static final String DROP_HOTEL_BENCHMARK_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelBenchmarkColumns.TABLE_NAME + ";";

    // Contains the config schema creation SQL. Must be in execution order
    protected static final String[] SCHEMA_CREATE_SQL = { //
            SCHEMA_CREATE_TRIP_SUMMARY_TABLE, //
            SCHEMA_CREATE_TRIP_SUMMARY_MESSAGE_TABLE, //
            SCHEMA_CREATE_TRIP_TABLE, //
            SCHEMA_CREATE_ENHANCEMENT_DAY_TABLE, //
            SCHEMA_CREATE_ENHANCEMENT_DAY_INDEX, //
            SCHEMA_CREATE_SORTABLE_SEGMENT_TABLE, //
            SCHEMA_CREATE_SORTABLE_SEGMENT_INDEX, //
            SCHEMA_CREATE_ENHANCEMENT_OFFER_TABLE, //
            SCHEMA_CREATE_ENHANCEMENT_OFFER_INDEX, //
            SCHEMA_CREATE_OFFER_LINK_TABLE, //
            SCHEMA_CREATE_OFFER_LINK_INDEX, //
            SCHEMA_CREATE_OFFER_CONTENT_TABLE, //
            SCHEMA_CREATE_OFFER_CONTENT_INDEX, //
            SCHEMA_CREATE_CONTENT_LINK_TABLE, //
            SCHEMA_CREATE_CONTENT_LINK_INDEX, //
            SCHEMA_CREATE_MAP_DISPLAY_TABLE, //
            SCHEMA_CREATE_MAP_DISPLAY_INDEX, //
            SCHEMA_CREATE_DISPLAY_OVERLAY_TABLE, //
            SCHEMA_CREATE_DISPLAY_OVERLAY_INDEX, //
            SCHEMA_CREATE_VALIDITY_LOCATION_TABLE, //
            SCHEMA_CREATE_VALIDITY_LOCATION_INDEX, //
            SCHEMA_CREATE_VALIDITY_TIME_RANGE_TABLE, //
            SCHEMA_CREATE_VALIDITY_TIME_RANGE_INDEX, //
            SCHEMA_CREATE_TRIP_RULE_VIOLATION_TABLE, //
            SCHEMA_CREATE_TRIP_RULE_VIOLATION_INDEX, //
            SCHEMA_CREATE_TRIP_RULE_VIOLATION_TRIGGER, //
            SCHEMA_CREATE_CAR_RULE_VIOLATION_TABLE, //
            SCHEMA_CREATE_CAR_RULE_VIOLATION_INDEX, //
            SCHEMA_CREATE_CAR_RULE_VIOLATION_TRIGGER, //
            SCHEMA_CREATE_HOTEL_RULE_VIOLATION_TABLE, //
            SCHEMA_CREATE_HOTEL_RULE_VIOLATION_INDEX, //
            SCHEMA_CREATE_HOTEL_RULE_VIOLATION_TRIGGER, //
            SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_TABLE, //
            SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_INDEX, //
            SCHEMA_CREATE_FLIGHT_RULE_VIOLATION_TRIGGER, //
            SCHEMA_CREATE_RAIL_RULE_VIOLATION_TABLE, //
            SCHEMA_CREATE_RAIL_RULE_VIOLATION_INDEX, //
            SCHEMA_CREATE_RAIL_RULE_VIOLATION_TRIGGER, //
            SCHEMA_CREATE_RULE_TABLE, //
            SCHEMA_CREATE_RULE_TRIP_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_CAR_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_HOTEL_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_FLIGHT_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_RAIL_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_TABLE, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_TRIP_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_CAR_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_HOTEL_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_FLIGHT_RULE_ID_INDEX, //
            SCHEMA_CREATE_RULE_VIOLATION_REASON_RAIL_RULE_ID_INDEX, //
            SCHEMA_CREATE_TRAVEL_POINT_TABLE, //
            SCHEMA_CREATE_TRAVEL_POINT_TRIP_ID_INDEX, //
            SCHEMA_CREATE_TRAVEL_POINT_SEGMENT_ID_INDEX, //
            SCHEMA_CREATE_BOOKING_TABLE, //
            SCHEMA_CREATE_BOOKING_INDEX, //
            SCHEMA_CREATE_AIRLINE_TICKET_TABLE, //
            SCHEMA_CREATE_AIRLINE_TICKET_INDEX, //
            SCHEMA_CREATE_PASSENGER_TABLE, //
            SCHEMA_CREATE_PASSENGER_INDEX, //
            SCHEMA_CREATE_FREQUENT_TRAVELER_PROGRAM_TABLE, //
            SCHEMA_CREATE_FREQUENT_TRAVELER_PROGRAM_INDEX, //
            SCHEMA_CREATE_SEGMENT_TABLE, //
            SCHEMA_CREATE_SEGMENT_INDEX, //
            SCHEMA_CREATE_AIR_SEGMENT_TABLE, //
            SCHEMA_CREATE_AIR_SEGMENT_INDEX, //
            SCHEMA_CREATE_FLIGHT_STATUS_TABLE, //
            SCHEMA_CREATE_FLIGHT_STATUS_INDEX, //
            SCHEMA_CREATE_SEAT_TABLE, //
            SCHEMA_CREATE_SEAT_INDEX, //
            SCHEMA_CREATE_HOTEL_SEGMENT_TABLE, //
            SCHEMA_CREATE_HOTEL_SEGMENT_INDEX, //
            SCHEMA_CREATE_CAR_SEGMENT_TABLE, //
            SCHEMA_CREATE_CAR_SEGMENT_INDEX, //
            SCHEMA_CREATE_RAIL_SEGMENT_TABLE, //
            SCHEMA_CREATE_RAIL_SEGMENT_INDEX, //
            SCHEMA_CREATE_DINING_SEGMENT_TABLE, //
            SCHEMA_CREATE_DINING_SEGMENT_INDEX, //
            SCHEMA_CREATE_EVENT_SEGMENT_TABLE, //
            SCHEMA_CREATE_EVENT_SEGMENT_INDEX, //
            SCHEMA_CREATE_PARKING_SEGMENT_TABLE, //
            SCHEMA_CREATE_PARKING_SEGMENT_INDEX, //
            SCHEMA_CREATE_RIDE_SEGMENT_TABLE, //
            SCHEMA_CREATE_RIDE_SEGMENT_INDEX, //
            SCHEMA_CREATE_LOCATION_CHOICE_TABLE, //
            SCHEMA_CREATE_HOTEL_SEARCH_RESULT_TABLE, //
            SCHEMA_CREATE_HOTEL_DETAIL_TABLE, //
            SCHEMA_CREATE_HOTEL_IMAGE_PAIR_TABLE, //
            SCHEMA_CREATE_HOTEL_RATE_DETAIL_TABLE, //
            SCHEMA_CREATE_HOTEL_VIOLATION_TABLE, //
            SCHEMA_CREATE_HOTEL_BENCHMARK_TABLE //
    };
    // Drop the Hotel Violation table.
    protected static final String DROP_HOTEL_VIOLATION_TABLE =
            "DROP TABLE IF EXISTS " + Travel.HotelViolationColumns.TABLE_NAME + ";";
    // Contains the config schema deletion SQL. Must be in execution order
    protected static final String[] SCHEMA_DELETE_SQL = {DROP_TRIP_SUMMARY_TABLE, //
            DROP_TRIP_SUMMARY_MESSAGE_TABLE, //
            DROP_TRIP_TABLE, //
            DROP_ENHANCEMENT_DAY_TABLE, //
            DROP_SORTABLE_SEGMENT_TABLE, //
            DROP_ENHANCEMENT_OFFER_TABLE, //
            DROP_OFFER_LINK_TABLE, //
            DROP_OFFER_CONTENT_TABLE, //
            DROP_CONTENT_LINK_TABLE, //
            DROP_MAP_DISPLAY_TABLE, //
            DROP_DISPLAY_OVERLAY_TABLE, //
            DROP_VALIDITY_LOCATION_TABLE, //
            DROP_VALIDITY_TIME_RANGE_TABLE, //
            DROP_TRIP_RULE_VIOLATION_TABLE, //
            DROP_CAR_RULE_VIOLATION_TABLE, //
            DROP_HOTEL_RULE_VIOLATION_TABLE, //
            DROP_FLIGHT_RULE_VIOLATION_TABLE, //
            DROP_RAIL_RULE_VIOLATION_TABLE, //
            DROP_RULE_TABLE, //
            DROP_RULE_VIOLATION_REASON_TABLE, //
            DROP_TRAVEL_POINT_TABLE, //
            DROP_BOOKING_TABLE, //
            DROP_AIRLINE_TICKET_TABLE, //
            DROP_PASSENGER_TABLE, //
            DROP_FREQUENT_TRAVELER_PROGRAM_TABLE, //
            DROP_SEGMENT_TABLE, //
            DROP_AIR_SEGMENT_TABLE, //
            DROP_FLIGHT_STATUS_TABLE, //
            DROP_SEAT_TABLE, //
            DROP_HOTEL_SEGMENT_TABLE, //
            DROP_CAR_SEGMENT_TABLE, //
            DROP_RAIL_SEGMENT_TABLE, //
            DROP_DINING_SEGMENT_TABLE, //
            DROP_EVENT_SEGMENT_TABLE, //
            DROP_PARKING_SEGMENT_TABLE, //
            DROP_RIDE_SEGMENT_TABLE, //
            DROP_LOCATION_CHOICE_TABLE, //
            DROP_HOTEL_SEARCH_RESULT_TABLE, //
            DROP_HOTEL_DETAIL_TABLE, //
            DROP_HOTEL_IMAGE_PAIR_TABLE, //
            DROP_HOTEL_RATE_DETAIL_TABLE, //
            DROP_HOTEL_VIOLATION_TABLE, //
            DROP_HOTEL_BENCHMARK_TABLE //
    };
    // Contains the config database name.
    static final String DATABASE_NAME = "travel.db";
    // DB History
    // Contains the current database version.
    // static final int DATABASE_VERSION = 1; // Initial version.
    // static final int DATABASE_VERSION = 2; // added Jarvis Hotel tables.
    static final int DATABASE_VERSION = 3; // added new column PROPERTY_IDS in Jarvis Travel.HotelDetailColumns.TABLE_NAME table.
    private static final String CLS_TAG = "TravelDBSchema";

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onCreate(android.database.sqlite.SQLiteDatabase)
     */
    static void onCreate(PlatformSQLiteDatabase db) {

        String sqliteVersion = db.getSQLiteVersion();
        Log.d(Const.LOG_TAG,
                CLS_TAG + ".onCreate: SQLite version -> '" + ((sqliteVersion != null) ? sqliteVersion : "unknown")
                        + "'.");

        Log.v(Const.LOG_TAG, "Creating schema.");

        for (int i = 0; i < SCHEMA_CREATE_SQL.length; i++) {
            db.execSQL(SCHEMA_CREATE_SQL[i]);
        }

    }

    /*
     * (non-Javadoc)
     * 
     * @see android.database.sqlite.SQLiteOpenHelper#onUpgrade(android.database.sqlite.SQLiteDatabase, int, int)
     */
    static void onUpgrade(PlatformSQLiteDatabase db, int oldVersion, int newVersion) {

        Log.v(Const.LOG_TAG, "Upgrading database from " + oldVersion + " to " + newVersion);

        // Disable foreign key support.
        if (db.disableForeignKeySupport()) {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: disabling foreign key support: succeeded");
        } else {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: disabling foreign key support: failed.");
        }

        switch (newVersion) {
            case 3:
            case 2: {
                // Drop all the tables that do not need to be migrated. These are tables that are easily reloadable from the server.
                for (int i = 0; i < SCHEMA_DELETE_SQL.length; i++) {
                    db.execSQL(SCHEMA_DELETE_SQL[i]);
                }
                // Recreate all tables while ignoring any tables that were not dropped.
                onCreate(db);
                break;
            }
            default: {
                Log.v(Const.LOG_TAG, "DB version provided no upgrade path: " + newVersion);
                break;
            }
        }

        // Enable foreign key support.
        if (db.enableForeignKeySupport()) {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: enabling foreign key support: succeeded");
        } else {
            Log.v(Const.LOG_TAG, CLS_TAG + ".onUpgrade: enabling foreign key support: failed.");
        }

    }

}
