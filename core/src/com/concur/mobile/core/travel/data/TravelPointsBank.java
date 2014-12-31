package com.concur.mobile.core.travel.data;

import java.io.Serializable;

import com.concur.mobile.platform.util.Parse;

public class TravelPointsBank implements Serializable {

    /**
     * generated id
     */
    private static final long serialVersionUID = 7424550206038927129L;

    private Integer pointsPosted;
    private Integer pointsPending;
    private Integer pointsAvailableToSpend;

    public Integer getPointsPosted() {
        return pointsPosted;
    }

    public void setPointsPosted(Integer pointsPosted) {
        this.pointsPosted = pointsPosted;
    }

    public Integer getPointsPending() {
        return pointsPending;
    }

    public void setPointsPending(Integer pointsPending) {
        this.pointsPending = pointsPending;
    }

    public Integer getPointsAvailableToSpend() {
        return pointsAvailableToSpend;
    }

    public void setPointsAvailableToSpend(Integer pointsAvailableToSpend) {
        this.pointsAvailableToSpend = pointsAvailableToSpend;
    }

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("PointsPosted")) {
            pointsPosted = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("PointsPending")) {
            pointsPending = Parse.safeParseInteger(cleanChars);
        } else if (localName.equalsIgnoreCase("PointsAvailableToSpend")) {
            pointsAvailableToSpend = Parse.safeParseInteger(cleanChars);
        }
    }
}
