package com.concur.mobile.platform.authentication.system.config.test;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "OfficeChoice")
public class Office {

    /**
     * Contains the address.
     */
    @Element(name = "Address", required = false)
    public String address;

    /**
     * Contains the city.
     */
    @Element(name = "City", required = false)
    public String city;

    /**
     * Contains the country.
     */
    @Element(name = "Country")
    public String country;

    /**
     * Contains the state.
     */
    @Element(name = "State", required = false)
    public String state;

    /**
     * Contains the latitude.
     */
    @Element(name = "Lat")
    public Double latitude;

    /**
     * Contains the longitude.
     */
    @Element(name = "Lon")
    public Double longitude;

}
