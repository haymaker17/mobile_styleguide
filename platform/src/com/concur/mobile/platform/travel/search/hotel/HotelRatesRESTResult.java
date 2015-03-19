package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;
import java.util.List;

/**
 * object to hold hotel booking response
 * 
 * @author tejoa
 * 
 */
public class HotelRatesRESTResult implements Serializable {

    /**
     * generated serialVersionUID
     */
    private static final long serialVersionUID = -3108275929875863986L;
    public Hotel hotel;
    public List<HotelViolation> violations;

}
