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

    /**
     * Rate Location Key needed e.g. for updating itineraries
     */
    private String rateLocationKey;

    /**
     * The offset between the local time related to this location to UTC in minutes
     */
    private long timeZoneOffset;

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

    public String getRateLocationKey() {
        return rateLocationKey;
    }

    public void setRateLocationKey(String rateLocationKey) {
        this.rateLocationKey = rateLocationKey;
    }

    public long getTimeZoneOffset() {
        return timeZoneOffset;
    }

    public void setTimeZoneOffset(long timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItineraryLocation)) return false;

        ItineraryLocation that = (ItineraryLocation) o;

        if (timeZoneOffset != that.timeZoneOffset) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null)
            return false;
        if (countryName != null ? !countryName.equals(that.countryName) : that.countryName != null)
            return false;
        return !(rateLocationKey != null ? !rateLocationKey.equals(that.rateLocationKey) : that.rateLocationKey != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (rateLocationKey != null ? rateLocationKey.hashCode() : 0);
        result = 31 * result + (int) (timeZoneOffset ^ (timeZoneOffset >>> 32));
        return result;
    }
}
