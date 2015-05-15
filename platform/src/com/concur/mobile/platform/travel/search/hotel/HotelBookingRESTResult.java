package com.concur.mobile.platform.travel.search.hotel;

import com.concur.mobile.platform.service.parser.Error;

import java.io.Serializable;

/**
 * object to hold hotel booking response
 *
 * @author tejoa
 */
public class HotelBookingRESTResult implements Serializable {

    /**
     * generated serialVersionUID
     */
    private static final long serialVersionUID = 3288052321819645226L;
    public String inventoryId;
    public String recordLocator;
    public String ConfirmationNumber;
    public String itineraryLocator;
    public Error error;

}
