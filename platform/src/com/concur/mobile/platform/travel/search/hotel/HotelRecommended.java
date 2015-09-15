package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;

/**
 * @author RatanK
 */
public class HotelRecommended implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = 9211627311935235166L;
    public String category;
    public Double totalScore;

    public String getSuggestedCategory() {
        if (category != null) { //totalScore > 1 && now we are showing more recommened hotels even lessthan 1 score
            return category;
        }
        return null;
    }
}
