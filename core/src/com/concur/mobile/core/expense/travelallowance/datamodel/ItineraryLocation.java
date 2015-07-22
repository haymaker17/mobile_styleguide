package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;
import android.util.Log;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ItineraryLocation implements Cloneable, Serializable {


    /**
     * The serialization id.
     */
    private static final long serialVersionUID = -8127263293626722440L;

    /**
     * The name of this {@code Class} for logging purpose.
     */
    private static final String CLASS_TAG = ItineraryLocation.class
            .getSimpleName();

    /**
     * The coded representation of an itinerary location
     */
    private String code;

    /**
     * The human readable location description
     */
    private String name;

    /**
     * The country code associated with this itinerary location
     */
    private String countryCode;

    /**
     * The human readable country description
     */
    private String countryName;

    /**
     * The rate location key
     */
    private String rateLocationKey;

    /**
     * The offset between the local time related to this location to UTC in minutes
     */
    private Long timeZoneOffset;

    /**
     * Getter method
     * @return {@link #code}
     */
    public String getCode() {
        return code;
    }

    /**
     * Setter method
     * @param code {@link #code}
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Getter method
     * @return {@link #name}
     */
    public String getName()
    {
        return name;
    }

    /**
     * Setter method
     * @param name {@link #name}
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter method
     * @return {@link #countryCode}
     */
    public String getCountryCode() {
        return countryCode;
    }

    /**
     * Setter method
     * @param countryCode {@link #countryCode}
     */
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    /**
     * Setter method
     * @return {@link #countryName}
     */
    public String getCountryName() {
        return countryName;
    }

    /**
     * Getter method
     * @param countryName {@link #countryName}
     */
    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    /**
     * Getter method
     * @return {@link #rateLocationKey}
     */
    public String getRateLocationKey() {
        return rateLocationKey;
    }

    /**
     * Setter method
     * @param rateLocationKey {@link #rateLocationKey}
     */
    public void setRateLocationKey(String rateLocationKey) {
        this.rateLocationKey = rateLocationKey;
    }

    /**
     * Getter method
     * @return {@link #timeZoneOffset}
     */
    public Long getTimeZoneOffset() {
        return timeZoneOffset;
    }

    /**
     * Setter method
     * @param timeZoneOffset {@link #timeZoneOffset}
     */
    public void setTimeZoneOffset(Long timeZoneOffset) {
        this.timeZoneOffset = timeZoneOffset;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ItineraryLocation)) return false;

        ItineraryLocation that = (ItineraryLocation) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        if (countryCode != null ? !countryCode.equals(that.countryCode) : that.countryCode != null)
            return false;
        if (countryName != null ? !countryName.equals(that.countryName) : that.countryName != null)
            return false;
        if (rateLocationKey != null ? !rateLocationKey.equals(that.rateLocationKey) : that.rateLocationKey != null)
            return false;
        return !(timeZoneOffset != null ? !timeZoneOffset.equals(that.timeZoneOffset) : that.timeZoneOffset != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (code != null ? code.hashCode() : 0);
        result = 31 * result + (countryCode != null ? countryCode.hashCode() : 0);
        result = 31 * result + (countryName != null ? countryName.hashCode() : 0);
        result = 31 * result + (rateLocationKey != null ? rateLocationKey.hashCode() : 0);
        result = 31 * result + (timeZoneOffset != null ? timeZoneOffset.hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ItineraryLocation clone() {
        try {
            super.clone();
        } catch (CloneNotSupportedException e) {
            Log.e(CLASS_TAG, "Clone failed with CloneNotSupportedException");
            return null;
        }
        ItineraryLocation itineraryLocation = new ItineraryLocation();
        if (this.code != null) {
            itineraryLocation.code = new String(this.code);
        }
        if (this.name != null) {
            itineraryLocation.name = new String(this.name);
        }
        if (this.countryCode != null) {
            itineraryLocation.countryCode = new String(this.countryCode);
        }
        if (this.countryName != null) {
            itineraryLocation.countryName = new String(this.countryName);
        }
        if (this.rateLocationKey != null) {
            itineraryLocation.rateLocationKey = new String(this.rateLocationKey);
        }
        if (this.timeZoneOffset != null) {
            itineraryLocation.timeZoneOffset = new Long(this.timeZoneOffset);
        }
        return itineraryLocation;
    }
}
