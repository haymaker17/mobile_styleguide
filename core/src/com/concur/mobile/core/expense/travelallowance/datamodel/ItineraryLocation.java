package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ItineraryLocation implements Serializable {


    private static final long serialVersionUID = -8127263293626722440L;

    private String name;
    private String code;
    private String countryCode;
    private String countryName;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ItineraryLocation location = (ItineraryLocation) o;

        if (name != null ? !name.equals(location.name) : location.name != null) {
            return false;
        }
        if (countryName != null ? !countryName.equals(location.countryName) : location.countryName != null) {
            return false;
        }
        if (code != null ? !code.equals(location.code) : location.code != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
