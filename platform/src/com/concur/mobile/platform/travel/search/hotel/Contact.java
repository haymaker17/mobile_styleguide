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

    /***
     * formatter to show address on Hotel Details screen
     * 
     * @return
     */
    public String getAddress() {
        StringBuilder stb = new StringBuilder();
        if (!addressLine1.isEmpty()) {
            stb.append(addressLine1);
            stb.append(delimiter);
        } else if (!street.isEmpty()) {
            stb.append(street);
            stb.append(delimiter);
        } else if (!city.isEmpty()) {
            stb.append(city);
            stb.append(delimiter);
        } else if (!state.isEmpty()) {
            stb.append(state);
            stb.append(delimiter);
        } else if (!country.isEmpty()) {
            stb.append(country);
            stb.append(delimiter);
        } else if (!zip.isEmpty()) {
            stb.append(zip);
            stb.append(delimiter);
        }

        return stb.toString();
    }
}
