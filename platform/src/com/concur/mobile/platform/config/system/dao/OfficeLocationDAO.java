package com.concur.mobile.platform.config.system.dao;

/**
 * Office location data access object which holds data for Office location.
 * 
 * @author sunill
 * 
 */

public class OfficeLocationDAO {

    /**
     * Contains the address.
     */
    private String address;

    /**
     * Contains the city.
     */
    private String city;

    /**
     * Contains the country.
     */
    private String country;

    /**
     * Contains the latitude.
     */
    private Double lat;

    /**
     * Contains the longitude.
     */
    private Double lon;

    /**
     * Contains the state.
     */
    private String state;

    /**
     * Default Construction of the Office location.
     * */
    public OfficeLocationDAO() {
    }

    /**
     * Construct the office location reference using the data passed.
     * 
     * @param address
     *            address of the office location.
     * @param city
     *            city of the office location.
     * @param country
     *            country of the office location.
     * @param state
     *            state of the office location.
     * @param lat
     *            latitude of the office location.
     * @param lon
     *            longitude of the office location.
     */
    public OfficeLocationDAO(String address, String city, String country, String state, Double lat, Double lon) {
        this.address = address;
        this.city = city;
        this.country = country;
        this.state = state;
        this.lat = lat;
        this.lon = lon;
    }

    /**
     * Gets address of the office location.
     * 
     * @return returns address of the office location.
     */
    public String getAddress() {
        return address;
    }

    /**
     * Gets city of the office location.
     * 
     * @return returns city of the office location.
     */
    public String getCity() {
        return city;
    }

    /**
     * Gets country of the office location.
     * 
     * @return returns country of the office location
     */
    public String getCountry() {
        return country;
    }

    /**
     * Gets Latitude of the office location.
     * 
     * @return returns latitude of the office location.
     */
    public Double getLat() {
        return lat;
    }

    /**
     * Gets Longitude of the office location.
     * 
     * @return returns longitude of the office location.
     */
    public Double getLon() {
        return lon;
    }

    /**
     * Gets state of the office location.
     * 
     * @return returns state of the office location.
     */
    public String getState() {
        return state;
    }

}
