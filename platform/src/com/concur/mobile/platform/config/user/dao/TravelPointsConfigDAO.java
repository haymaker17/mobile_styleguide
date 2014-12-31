package com.concur.mobile.platform.config.user.dao;

public class TravelPointsConfigDAO {

    /**
     * Contains whether air travel points is enabled.
     */
    private Boolean airTravelPointsEnabled;

    /**
     * Contains whether hotel travel points is enabled.
     */
    private Boolean hotelTravelPointsEnabled;

    /**
     * Default Construction of the Travel Points Config DAO.
     * */
    public TravelPointsConfigDAO() {
    }

    /**
     * Construct the Travel Points Config reference using the data passed.
     * 
     * @param airTravelPointsEnabled
     *            contains a flag on whether air travel points is enabled.
     * @param hotelTravelPointsEnabled
     *            contains a flag on whether hotel travel points is enabled.
     */
    public TravelPointsConfigDAO(Boolean airTravelPointsEnabled, Boolean hotelTravelPointsEnabled) {
        this.airTravelPointsEnabled = airTravelPointsEnabled;
        this.hotelTravelPointsEnabled = hotelTravelPointsEnabled;
    }

    /**
     * Gets a flag on whether air travel points is enabled.
     * 
     * @return returns a flag on whether air travel points is enabled.
     */
    public Boolean getAirTravelPointsEnabled() {
        return airTravelPointsEnabled;
    }

    /**
     * Gets a flag on whether hotel travel points is enabled.
     * 
     * @return returns a flag on whether hotel travel points is enabled.
     */
    public Boolean getHotelTravelPointsEnabled() {
        return hotelTravelPointsEnabled;
    }
}
