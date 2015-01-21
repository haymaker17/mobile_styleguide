package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;

/**
 * Object to hold the hotel contact
 * 
 * @author RatanK
 * 
 */
public class Contact implements Serializable {

    /**
     * generated
     */
    private static final long serialVersionUID = -923791296583895751L;
    public String addressLine1;
    public String street;
    public String city;
    public String state;
    public String country;
    public String countryCode;
    public String phone;
    public String tollFree;
    public String zip;

    public final String delimiter = ", ";

}
