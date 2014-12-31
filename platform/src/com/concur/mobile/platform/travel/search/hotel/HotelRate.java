package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;

/**
 * 
 * @author RatanK
 * 
 */
public class HotelRate implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = 8136223454047648920L;
    public String rateId;
    public Double amount;
    public String currency;
    public String source;
    public String roomType;
    public String description;
    public String estimatedBedType;
    public String guaranteeSurcharge;
    public boolean rateChangesOverStay;
    public int maxEnforcementLevel;
    public URLInfo sellOptions;
    public int[] violationValueIds;
    public Double travelPoints;
    public boolean canRedeemTravelPointsAgainstViolations;
}
