package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelRecommended implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = 9211627311935235166L;
    public String category;
    public Double totalScore;

    public String getSuggestedCategory() {
        if (totalScore > 1 && category != null) {
            return category;
        }
        return null;
    }
}
