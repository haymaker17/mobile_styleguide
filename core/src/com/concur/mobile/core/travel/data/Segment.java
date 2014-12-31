package com.concur.mobile.core.travel.data;

import java.net.URI;
import java.util.Calendar;
import java.util.TimeZone;

import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.travel.activity.DiningSegmentDetail;
import com.concur.mobile.core.travel.activity.EventSegmentDetail;
import com.concur.mobile.core.travel.activity.ParkingSegmentDetail;
import com.concur.mobile.core.travel.activity.RideSegmentDetail;
import com.concur.mobile.core.travel.activity.SegmentDetail;
import com.concur.mobile.core.travel.air.activity.AirSegmentDetail;
import com.concur.mobile.core.travel.car.activity.CarSegmentDetail;
import com.concur.mobile.core.travel.hotel.activity.HotelSegmentDetail;
import com.concur.mobile.core.travel.rail.activity.RailSegmentDetail;
import com.concur.mobile.platform.util.Format;
import com.concur.mobile.platform.util.Parse;

public abstract class Segment {

    public static enum SegmentType {
        AIR(AirSegmentDetail.class), CAR(CarSegmentDetail.class), DINING(DiningSegmentDetail.class), EVENT(
                EventSegmentDetail.class), HOTEL(HotelSegmentDetail.class), PARKING(ParkingSegmentDetail.class), RAIL(
                RailSegmentDetail.class), RIDE(RideSegmentDetail.class), UNDEFINED();

        public final Class<? extends SegmentDetail> activity;

        SegmentType(Class<? extends SegmentDetail> activity) {
            this.activity = activity;
        }

        SegmentType() {
            this(null);
        }

    };

    protected SegmentType type;

    // TODO: In general we have issues right now with timestamps.
    // - We don't seem to know what the timezone is (service needs to provide it)
    // - UTC is nice but much of what we need to display and operate on is local. In Java
    // that becomes complicated because of the inherent need for the timezone to properly
    // mess with any timestamp (Date assumes UTC, Calendar defaults to local (device) timezone)
    protected Calendar startDateUtc;
    protected Calendar endDateUtc;
    protected Calendar startDateLocal;
    protected Calendar endDateLocal;

    // Hold onto the day values (no time) as well for quick lookups
    protected Calendar startDayUtc;
    protected Calendar endDayUtc;
    protected Calendar startDayLocal;
    protected Calendar endDayLocal;

    // Segment type name from server
    public String segmentTypeName;

    public int gdsId;
    public String bookingSource;

    // The record locator.
    public String locator;

    public String confirmNumber;
    public String creditCardId;
    public String creditCardLastFour;
    public String creditCardType;
    public String creditCardTypeLocalized;
    public String currency;
    public String eReceiptStatus;
    public String endAddress;
    public String endAddress2;
    public String endCity;
    public String endCityCode;
    public String endCityCodeLocalized;
    public String endCountry;
    public String endCountryCode;
    public Double endLat;
    public Double endLong;
    public String endPostCode;
    public String endState;
    public String frequentTravelerId;
    public URI imageVendorUri;
    public Integer numPersons;
    public String operatedByVendor;
    public String operatedByVendorName;
    public String phoneNumber;
    public String rateCode;
    public String segmentKey;
    public String segmentName;
    public String startAddress;
    public String startAddress2;
    public String startCity;
    public String startCityCode;
    public String startCountry;
    public String startCountryCode;
    public Double startLat;
    public Double startLong;
    public String startPostCode;
    public String startState;
    public String status;
    public String statusLocalized;
    public TimeZone timeZone;
    public Double totalRate;
    public String vendor;
    public String vendorName;
    public String vendorURL;

    public TravelPoint travelPoint;
    public boolean inTravelPoint;

    // /////////////////////////////////////////////////////////////////////////

    public SegmentType getType() {
        return type;
    }

    /**
     * Create a {@link Calendar} with only the date fields set. Everything else is zero'd out to allow for exact day comparisons.
     * 
     * @param fullDateTime
     *            The original {@link Calendar} with a potentially full set of values
     * @return A new {@link Calendar} with only the year, month, and day of month fields set.
     */
    protected Calendar createDayCalendar(Calendar fullDateTime) {
        Calendar dayOnly = null;

        if (fullDateTime != null) {
            dayOnly = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            for (int i = 1; i < 17; i++)
                dayOnly.set(i, 0);
            dayOnly.set(fullDateTime.get(Calendar.YEAR), fullDateTime.get(Calendar.MONTH),
                    fullDateTime.get(Calendar.DAY_OF_MONTH));
        }

        return dayOnly;
    }

    public Calendar getStartDateUtc() {
        return startDateUtc;
    }

    public void setStartDateUtc(Calendar startDateUtc) {
        this.startDateUtc = startDateUtc;
        this.startDayUtc = createDayCalendar(startDateUtc);
    }

    public Calendar getStartDayUtc() {
        return startDayUtc;
    }

    public Calendar getEndDateUtc() {
        return endDateUtc;
    }

    public void setEndDateUtc(Calendar endDateUtc) {
        this.endDateUtc = endDateUtc;
        this.endDayUtc = createDayCalendar(endDateUtc);
    }

    public Calendar getEndDayUtc() {
        return endDayUtc;
    }

    public Calendar getStartDateLocal() {
        return startDateLocal;
    }

    public void setStartDateLocal(Calendar startDateLocal) {
        this.startDateLocal = startDateLocal;
        this.startDayLocal = createDayCalendar(startDateLocal);
    }

    public Calendar getStartDayLocal() {
        return startDayLocal;
    }

    public Calendar getEndDateLocal() {
        return endDateLocal;
    }

    public void setEndDateLocal(Calendar endDateLocal) {
        this.endDateLocal = endDateLocal;
        this.endDayLocal = createDayCalendar(endDateLocal);
    }

    public Calendar getEndDayLocal() {
        return endDayLocal;
    }

    // TODO: This probably should go to FormatUtil and it needs to be localizable/international,
    // perhaps using a localized string and replacement parameters
    protected String buildAddress(String address, String city, String state) {
        StringBuilder addr = new StringBuilder();
        addr.append(address).append(", ").append(city).append(' ').append(state);
        return addr.toString();
    }

    public String getStartAddress() {
        return buildAddress(startAddress, startCity, startState);
    }

    public String getEndAddress() {
        return buildAddress(endAddress, endCity, endState);
    }

    /**
     * Gets the segment end city.
     * 
     * @return the segment end city.
     */
    public String getEndCity() {
        return endCity;
    }

    /**
     * Gets the segment end state.
     * 
     * @return the segment end state.
     */
    public String getEndState() {
        return endState;
    }

    /**
     * Gets the segment end country.
     * 
     * @return the segment end country.
     */
    public String getEndCountry() {
        return endCountry;
    }

    /**
     * Handle specific XML elements for a segment
     * 
     * @param localName
     *            The element name parsed by the handler
     * @param chars
     *            The character sequence from the XML. Should be trimmed.
     * 
     * @return true if the element was handled (chars was consumed), false otherwise
     */
    protected boolean handleSegmentElement(String localName, String chars) {
        boolean handled = false;

        if (localName.equalsIgnoreCase("StartDateUtc")) {
            setStartDateUtc(Parse.parseXMLTimestamp(chars));
        } else if (localName.equalsIgnoreCase("EndDateUtc")) {
            setEndDateUtc(Parse.parseXMLTimestamp(chars));
        } else if (localName.equalsIgnoreCase("StartDateLocal")) {
            setStartDateLocal(Parse.parseXMLTimestamp(chars));
        } else if (localName.equalsIgnoreCase("EndDateLocal")) {
            setEndDateLocal(Parse.parseXMLTimestamp(chars));
        } else if (localName.equalsIgnoreCase("ConfirmationNumber")) {
            confirmNumber = chars;
        } else if (localName.equalsIgnoreCase("CreditCardId")) {
            creditCardId = chars;
        } else if (localName.equalsIgnoreCase("CreditCardLastFour")) {
            creditCardLastFour = chars;
        } else if (localName.equalsIgnoreCase("CreditCardType")) {
            creditCardType = chars;
        } else if (localName.equalsIgnoreCase("CreditCardTypeLocalized")) {
            creditCardTypeLocalized = chars;
        } else if (localName.equalsIgnoreCase("Currency")) {
            currency = chars;
        } else if (localName.equalsIgnoreCase("EReceiptStatus")) {
            eReceiptStatus = chars;
        } else if (localName.equalsIgnoreCase("EndAddress")) {
            endAddress = chars;
        } else if (localName.equalsIgnoreCase("EndAddress2")) {
            endAddress2 = chars;
        } else if (localName.equalsIgnoreCase("EndCity")) {
            endCity = chars;
        } else if (localName.equalsIgnoreCase("EndCityCode")) {
            endCityCode = chars;
        } else if (localName.equalsIgnoreCase("EndCityCodeLocalized")) {
            endCityCodeLocalized = chars;
        } else if (localName.equalsIgnoreCase("EndCountry")) {
            endCountry = chars;
        } else if (localName.equalsIgnoreCase("EndCountryCode")) {
            endCountryCode = chars;
        } else if (localName.equalsIgnoreCase("EndLatitude")) {
            endLat = Parse.safeParseDouble(chars);
        } else if (localName.equalsIgnoreCase("EndLongitude")) {
            endLong = Parse.safeParseDouble(chars);
        } else if (localName.equalsIgnoreCase("EndPostalCode")) {
            endPostCode = chars;
        } else if (localName.equalsIgnoreCase("EndState")) {
            endState = chars;
        } else if (localName.equalsIgnoreCase("FrequentTravelerId")) {
            frequentTravelerId = chars;
        } else if (localName.equalsIgnoreCase("ImageVendorUri")) {
            imageVendorUri = Format.formatServerURI(false, Preferences.getServerAddress(), chars);
        } else if (localName.equalsIgnoreCase("NumPersons")) {
            numPersons = Parse.safeParseInteger(chars);
        } else if (localName.equalsIgnoreCase("OperatedByVendor")) {
            operatedByVendor = chars;
        } else if (localName.equalsIgnoreCase("OperatedByVendorName")) {
            operatedByVendorName = chars;
        } else if (localName.equalsIgnoreCase("PhoneNumber")) {
            // The system has a habit if using a / to separate area code. Replace it so
            // the built-in phone number parsers will see the whole number.
            phoneNumber = chars.replace('/', '-');
        } else if (localName.equalsIgnoreCase("RateCode")) {
            rateCode = chars;
        } else if (localName.equalsIgnoreCase("SegmentKey")) {
            segmentKey = chars;
        } else if (localName.equalsIgnoreCase("SegmentName")) {
            segmentName = chars;
        } else if (localName.equalsIgnoreCase("StartAddress")) {
            startAddress = chars;
        } else if (localName.equalsIgnoreCase("StartAddress2")) {
            startAddress2 = chars;
        } else if (localName.equalsIgnoreCase("StartCity")) {
            startCity = chars;
        } else if (localName.equalsIgnoreCase("StartCityCode")) {
            startCityCode = chars;
        } else if (localName.equalsIgnoreCase("StartCountry")) {
            startCountry = chars;
        } else if (localName.equalsIgnoreCase("StartCountryCode")) {
            startCountryCode = chars;
        } else if (localName.equalsIgnoreCase("StartLatitude")) {
            startLat = Parse.safeParseDouble(chars);
        } else if (localName.equalsIgnoreCase("StartLongitude")) {
            startLong = Parse.safeParseDouble(chars);
        } else if (localName.equalsIgnoreCase("StartPostalCode")) {
            startPostCode = chars;
        } else if (localName.equalsIgnoreCase("StartState")) {
            startState = chars;
        } else if (localName.equalsIgnoreCase("Status")) {
            status = chars;
        } else if (localName.equalsIgnoreCase("StatusLocalized")) {
            statusLocalized = chars;
        } else if (localName.equalsIgnoreCase("TimeZoneId")) {
            // TODO: Get TZ straightened out
            timeZone = null;
        } else if (localName.equalsIgnoreCase("TotalRate")) {
            totalRate = Parse.safeParseDouble(chars);
        } else if (localName.equalsIgnoreCase("TypeLocalized")) {
            segmentTypeName = chars;
        } else if (localName.equalsIgnoreCase("Vendor")) {
            vendor = chars;
        } else if (localName.equalsIgnoreCase("VendorName")) {
            vendorName = chars;
        } else if (localName.equalsIgnoreCase("VendorURL")) {
            vendorURL = chars;
        } else if (localName.equalsIgnoreCase("Benchmark")) {
            travelPoint.setBenchmark(chars);
        } else if (localName.equalsIgnoreCase("BenchmarkCurrency")) {
            travelPoint.setBenchmarkCurrency(chars);
        } else if (localName.equalsIgnoreCase("PointsPosted")) {
            travelPoint.setPointsPosted(chars);
        } else if (localName.equalsIgnoreCase("PointsPending")) {
            travelPoint.setPointsPending(chars);
        }

        return handled;
    }
}
