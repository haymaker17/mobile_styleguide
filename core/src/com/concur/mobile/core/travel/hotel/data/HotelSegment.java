package com.concur.mobile.core.travel.hotel.data;

import java.util.ArrayList;

import com.concur.mobile.core.travel.data.ImagePair;
import com.concur.mobile.core.travel.data.Segment;
import com.concur.mobile.platform.util.Parse;

public class HotelSegment extends Segment {

    /**
     * Default constructor does nothing but set the type
     */
    public HotelSegment() {
        type = SegmentType.HOTEL;
    }

    public String checkinTime; // I don't believe we will ever need to do calcs with these
    public String checkoutTime; // so we're just leaving them as strings for now
    public String discountCode;
    public Integer numRooms;
    public String rateCode;
    public String roomType;
    public String roomTypeLocalized;
    public Double dailyRate;
    public Double totalRate;
    public String cancellationPolicy;
    public String specialInstructions;
    public String roomDescription;
    public String rateType;
    public String propertyId;
    public int propertyImageCount;

    // This is only set when something has made the call to the server to get the image list and
    // specifically set it here. See HotelSegmentDetail for an example.
    public ArrayList<ImagePair> imagePairs;

    @Override
    protected boolean handleSegmentElement(String localName, String chars) {

        if (!super.handleSegmentElement(localName, chars)) {
            if (localName.equalsIgnoreCase("CheckinTime")) {
                checkinTime = chars;
            } else if (localName.equalsIgnoreCase("CheckoutTime")) {
                checkoutTime = chars;
            } else if (localName.equalsIgnoreCase("DiscountCode")) {
                discountCode = chars;
            } else if (localName.equalsIgnoreCase("NumRooms")) {
                numRooms = Parse.safeParseInteger(chars);
            } else if (localName.equalsIgnoreCase("RateCode")) {
                rateCode = chars;
            } else if (localName.equalsIgnoreCase("RoomType")) {
                roomType = chars;
            } else if (localName.equalsIgnoreCase("RoomTypeLocalized")) {
                roomTypeLocalized = chars;
            } else if (localName.equalsIgnoreCase("DailyRate")) {
                dailyRate = Parse.safeParseDouble(chars);
            } else if (localName.equalsIgnoreCase("TotalRate")) {
                totalRate = Parse.safeParseDouble(chars);
            } else if (localName.equalsIgnoreCase("SpecialInstructions")) {
                specialInstructions = chars;
            } else if (localName.equalsIgnoreCase("CancellationPolicy")) {
                cancellationPolicy = chars;
            } else if (localName.equalsIgnoreCase("RoomDescription")) {
                roomDescription = chars;
            } else if (localName.equalsIgnoreCase("RateType")) {
                rateType = chars;
            } else if (localName.equalsIgnoreCase("HotelPropertyId")) {
                propertyId = chars;
            } else if (localName.equalsIgnoreCase("HotelImage")) {
                // The itin XML is busted and we get empty elements but the count will correspond.
                propertyImageCount++;
            }
        }

        return true;
    }
}
