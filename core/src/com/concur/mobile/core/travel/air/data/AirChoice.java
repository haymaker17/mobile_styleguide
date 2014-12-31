package com.concur.mobile.core.travel.air.data;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.core.travel.data.Violation;
import com.concur.mobile.platform.util.Parse;

public class AirChoice {

    public String crnCode;
    public Double fare;
    public String fareId;
    public Boolean refundable;
    public String choiceId;
    public Integer maxEnforcementLevel;
    public boolean instantPurchase;
    public Integer travelPoints;
    public boolean canRedeemTravelPointsAgainstViolations;
    public String gdsName;

    public ArrayList<AirBookingSegment> segments;

    public List<Violation> violations;

    public AirChoice() {
        segments = new ArrayList<AirBookingSegment>();
        violations = new ArrayList<Violation>();
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("MaxEnforcementLevel")) {
            maxEnforcementLevel = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("Crn")) {
            crnCode = cleanChars;
        } else if (localName.equalsIgnoreCase("Fare")) {
            fare = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("FareId")) {
            fareId = cleanChars;
        } else if (localName.equalsIgnoreCase("Refundable")) {
            refundable = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("ChoiceId")) {
            choiceId = cleanChars;
        } else if (localName.equalsIgnoreCase("InstantPurchase")) {
            instantPurchase = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("TravelPoints")) {
            travelPoints = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("CanRedeemTravelPointsAgainstViolations")) {
            canRedeemTravelPointsAgainstViolations = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("GdsName")) {
            gdsName = cleanChars;
        }
    }

}
