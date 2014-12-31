package com.concur.mobile.core.travel.service.parser;

import java.util.ArrayList;
import java.util.List;

import com.concur.mobile.base.service.parser.Parser;
import com.concur.mobile.core.travel.data.TripToApprove;
import com.concur.mobile.platform.util.Parse;

/**
 * Parser for TripToApprove elements a response for /Mobile/TripApproval/TripsV3 request
 * 
 * @author RatanK
 * 
 */
public class TripsForApprovalParser implements Parser {

    public List<TripToApprove> tripsToApprove = new ArrayList<TripToApprove>();
    public TripToApprove tripToApprove;

    @Override
    public void startTag(String tag) {
        if (tag.equals("TripToApprove")) {
            tripToApprove = new TripToApprove();
        }
    }

    @Override
    public void handleText(String tag, String text) {
        if (tag.equals("ItinLocator")) {
            tripToApprove.setItinLocator(text);
            tripToApprove.setTripId(text);
        } else if (tag.equals("TripName")) {
            tripToApprove.setTripName(text);
        } else if (tag.equals("TravelerName")) {
            tripToApprove.setTravelerName(text);
        } else if (tag.equals("TravelerCompanyId")) {
            tripToApprove.setTravelerCompanyId(text);
        } else if (tag.equals("TravelerUserId")) {
            tripToApprove.setTravelerUserId(text);
        } else if (tag.equals("ApproveByDate")) {
            tripToApprove.setApproveByDate(Parse.parseXMLTimestamp(text));
        } else if (tag.equals("TotalTripCost")) {
            tripToApprove.setTotalTripCost(Double.parseDouble(text));
        } else if (tag.equals("TotalTripCostCrnCode")) {
            tripToApprove.setTotalTripCostCrnCode(text);
        }
    }

    @Override
    public void endTag(String tag) {
        if (tag.equals("TripToApprove")) {
            tripsToApprove.add(tripToApprove);
        }
    }

    public List<TripToApprove> getTripsToApprove() {
        return tripsToApprove;
    }
}