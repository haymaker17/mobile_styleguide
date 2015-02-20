package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;
import java.util.HashMap;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelPreference implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = 8082985319335216696L;

    public String starRating;
    // public boolean isContracted;
    // public int preferenceType; // boolean?
    // public int companyPriority; // boolean?
    // public boolean isCompanyPreferredChain;
    public String companyPreference;

    public static HashMap<String, Integer> PreferenceTypesArray = new HashMap<String, Integer>(6);
    static {
        PreferenceTypesArray.put("ChainLessPreferred", 1);
        PreferenceTypesArray.put("ChainPreferred", 2);
        PreferenceTypesArray.put("ChainMostPreferred", 3);
        PreferenceTypesArray.put("PropertyLessPreferred", 4);
        PreferenceTypesArray.put("PropertyPreferred", 5);
        PreferenceTypesArray.put("PropertyMostPreferred", 10);
    }

    public Integer getPreferenceTypeOrder() {
        return PreferenceTypesArray.get(this.companyPreference);
    }

}
