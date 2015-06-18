package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Representation of a Location used in the context of Travel Allowances
 *
 * @author Michael Becherer
 */
public class Location implements Serializable, Comparable<FixedTravelAllowance>, Cloneable {

    private static final long serialVersionUID = -4867542158593138834L;

    private String locationId;

    private String countryName;

    private String cityName;

    public Location(String locationId, String countryName, String cityName) {
        this.locationId = locationId;
        this.countryName = countryName;
        this.cityName = cityName;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public int compareTo(FixedTravelAllowance another) {
        return 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Location location = (Location) o;

        if (cityName != null ? !cityName.equals(location.cityName) : location.cityName != null) {
            return false;
        }
        if (countryName != null ? !countryName.equals(location.countryName) : location.countryName != null) {
            return false;
        }
        if (locationId != null ? !locationId.equals(location.locationId) : location.locationId != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = locationId != null ? locationId.hashCode() : 0;
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (cityName != null ? cityName.hashCode() : 0);
        return result;
    }
}
