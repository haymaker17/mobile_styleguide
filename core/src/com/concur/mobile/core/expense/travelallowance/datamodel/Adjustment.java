package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;

/**
 * Representation of a Travel Allowance Adjustment
 *
 * @author Michael Becherer
 */
public class Adjustment implements Serializable, Comparable<Adjustment>, Cloneable {

    /**
     * The identifier of the Adjustment
     */
    private String adjustmentId;

    /**
     * The resulting amount calculated in the backend (Per Diem minus deductions)
     */
    private Double allowanceAmount;

    /**
     * The currency code related to the allowanceAmount
     */
    private String allowanceCurrencyCode;

    /**
     * Indicates, whether allowance has been excluded or not
     */
    private boolean excludedIndicator;

    /**
     * The location related to the allowance
     */
    private Location location;

    public String getAdjustmentId() {
        return adjustmentId;
    }

    public void setAdjustmentId(String adjustmentId) {
        this.adjustmentId = adjustmentId;
    }

    public Double getAllowanceAmount() {
        return allowanceAmount;
    }

    public void setAllowanceAmount(Double allowanceAmount) {
        this.allowanceAmount = allowanceAmount;
    }

    public String getAllowanceCurrencyCode() {
        return allowanceCurrencyCode;
    }

    public void setAllowanceCurrencyCode(String allowanceCurrencyCode) {
        this.allowanceCurrencyCode = allowanceCurrencyCode;
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Adjustment that = (Adjustment) o;

        if (excludedIndicator != that.excludedIndicator) {
            return false;
        }
        if (adjustmentId != null ? !adjustmentId.equals(that.adjustmentId) : that.adjustmentId != null) {
            return false;
        }
        if (allowanceAmount != null ? !allowanceAmount.equals(that.allowanceAmount) : that.allowanceAmount != null) {
            return false;
        }
        if (allowanceCurrencyCode != null ? !allowanceCurrencyCode.equals(that.allowanceCurrencyCode) : that.allowanceCurrencyCode != null) {
            return false;
        }
        if (location != null ? !location.equals(that.location) : that.location != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = adjustmentId != null ? adjustmentId.hashCode() : 0;
        result = 31 * result + (allowanceAmount != null ? allowanceAmount.hashCode() : 0);
        result = 31 * result + (allowanceCurrencyCode != null ? allowanceCurrencyCode.hashCode() : 0);
        result = 31 * result + (excludedIndicator ? 1 : 0);
        result = 31 * result + (location != null ? location.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Adjustment another) {
        return 0;
    }
}
