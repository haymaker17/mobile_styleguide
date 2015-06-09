package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Representation of a Location used in the context of Travel Allowances
 *
 * @author Michael Becherer
 */
public class Location implements Serializable, Comparable<Adjustment>, Cloneable {

    private String locationId;

    private String countryName;

    private String cityName;

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
    public int compareTo(Adjustment another) {
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
