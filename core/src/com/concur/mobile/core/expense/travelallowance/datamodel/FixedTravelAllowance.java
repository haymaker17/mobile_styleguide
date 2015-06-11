package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;
import java.util.Date;

/**
 * Representation of a Fixed Travel Allowance (Per Diem)
 *
 * @author Michael Becherer
 */
public class FixedTravelAllowance implements Serializable, Comparable<FixedTravelAllowance>, Cloneable {

    /**
     * The identifier of the FixedTravelAllowance
     */
    private String id;

    /**
     * The date related to the FixedTravelAllowance
     */
    private Date date;

    /**
     * The resulting amount calculated in the backend (Per Diem minus deductions)
     */
    private Double amount;

    /**
     * The currency code related to the amount
     */
    private String currencyCode;

    /**
     * Indicates, whether allowance has been excluded or not
     */
    private boolean excludedIndicator;

    /**
     * The location related to the FixedTravelAllowance
     */
    private Location location;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public boolean isExcludedIndicator() {
        return excludedIndicator;
    }

    public void setExcludedIndicator(boolean excludedIndicator) {
        this.excludedIndicator = excludedIndicator;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof FixedTravelAllowance)) {
            return false;
        }

        FixedTravelAllowance that = (FixedTravelAllowance) o;

        if (excludedIndicator != that.excludedIndicator) {
            return false;
        }

        if (!amount.equals(that.amount)) {
            return false;
        }

        if (!currencyCode.equals(that.currencyCode)) {
            return false;
        }

        if (!date.equals(that.date)) {
            return false;
        }

        if (!id.equals(that.id)) {
            return false;
        }

        if (!location.equals(that.location)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + date.hashCode();
        result = 31 * result + amount.hashCode();
        result = 31 * result + currencyCode.hashCode();
        result = 31 * result + (excludedIndicator ? 1 : 0);
        result = 31 * result + location.hashCode();
        return result;
    }

    @Override
    public int compareTo(FixedTravelAllowance another) {
        return 0;
    }
}
