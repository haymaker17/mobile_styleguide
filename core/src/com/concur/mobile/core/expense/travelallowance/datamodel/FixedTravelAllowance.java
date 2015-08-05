package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.DateUtils;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;

/**
 * Representation of a Fixed Travel Allowance (Per Diem)
 *
 * @author Michael Becherer
 */
public class FixedTravelAllowance implements Serializable, Comparable<FixedTravelAllowance> {

    private static final long serialVersionUID = -7015146162877548013L;

    private static final String notProvidedCode = "NPR";

    /**
     * The identifier of the FixedTravelAllowance
     */
    private String fixedTravelAllowanceId;

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
     * The location name related to the FixedTravelAllowance
     */
    private String locationName;

    /**
     * Denotes, how the breakfast is provisioned
     */
    private ICode breakfastProvision;

    /**
     * Denotes, how the lunch is provisioned
     */
    private ICode lunchProvision;

    /**
     * Denotes, how the dinner is provisioned
     */
    private ICode dinnerProvision;

    /**
     * Indicates, whether overnight stay can be reimbursed or not
     */
    private boolean overnightIndicator;

    /**
     * Denotes the lodging type
     */
    private ICode lodgingType;

    /**
     * Indicates if the allowance is locked
     */
    private boolean locked;

    /**
     * Creates an instance of a FixedTravelAllowance
     */
    public FixedTravelAllowance() {

    }
    /**
     * Creates an instance of a FixedTravelAllowance
     *
     * @param fixedTravelAllowanceId The identifier of this FixedTravelAllowance
     */
    public FixedTravelAllowance(String fixedTravelAllowanceId) {
        this.fixedTravelAllowanceId = fixedTravelAllowanceId;
    }

    /**
     * Getter method
     *
     * @return The identifier of this FixedTravelAllowance
     */
    public String getFixedTravelAllowanceId() {
        return fixedTravelAllowanceId;
    }

    /**
     * Setter method
     *
     * @param fixedTravelAllowanceId The identifier of this FixedTravelAllowance
     */
    public void setFixedTravelAllowanceId(String fixedTravelAllowanceId) {
        this.fixedTravelAllowanceId = fixedTravelAllowanceId;
    }

    /**
     * Getter method
     *
     * @return The date related to the FixedTravelAllowance
     */
    public Date getDate() {
        return date;
    }

    /**
     * Setter method
     *
     * @param date The date related to the FixedTravelAllowance
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * Getter method
     *
     * @return The resulting amount calculated in the backend (Per Diem minus deductions)
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Setter method
     *
     * @param amount The resulting amount calculated in the backend (Per Diem minus deductions)
     */
    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Getter method
     *
     * @return The currency code related to the amount
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Setter method
     *
     * @param currencyCode The currency code related to the amount
     */
    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    /**
     * Getter method
     *
     * @return true, if allowance has been excluded
     */
    public boolean getExcludedIndicator() {
        return excludedIndicator;
    }

    /**
     * Setter method
     *
     * @param excludedIndicator true, if the allowance has been excluded
     */
    public void setExcludedIndicator(boolean excludedIndicator) {
        this.excludedIndicator = excludedIndicator;
    }

    /**
     * Getter method
     *
     * @return The location name related to the FixedTravelAllowance
     */
    public String getLocationName() {
        return locationName;
    }

    /**
     * Setter method
     *
     * @param locationName The location name related to the FixedTravelAllowance
     */
    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    /**
     * Getter method
     *
     * @return Denotes, how the breakfast is provisioned
     */
    public ICode getBreakfastProvision() {
        return breakfastProvision;
    }

    /**
     * Setter method Sets breakfast provision
     *
     * @param breakfastProvision
     */
    public void setBreakfastProvision(MealProvision breakfastProvision) {
        this.breakfastProvision = breakfastProvision;
    }

    /**
     * Getter method
     *
     * @return Denotes, how the lunch is provisioned
     */
    public ICode getLunchProvision() {
        return lunchProvision;
    }

    /**
     * Setter method Sets lunch provision
     *
     * @param lunchProvision
     */
    public void setLunchProvision(MealProvision lunchProvision) {
        this.lunchProvision = lunchProvision;
    }

    /**
     * Getter method
     *
     * @return Denotes, how the dinner is provisioned
     */
    public ICode getDinnerProvision() {
        return dinnerProvision;
    }

    /**
     * Setter method Sets dinner provision
     *
     * @param dinnerProvision
     */
    public void setDinnerProvision(MealProvision dinnerProvision) {
        this.dinnerProvision = dinnerProvision;
    }

    /**
     * Getter method
     *
     * @return the overnight indicator
     */
    public boolean getOvernightIndicator() {
        return overnightIndicator;
    }

    /**
     * Setter method sets the overnight indicator
     *
     * @param overnightIndicator
     */
    public void setOvernightIndicator(boolean overnightIndicator) {
        this.overnightIndicator = overnightIndicator;
    }

    /**
     * Getter method
     *
     * @return the lodging type
     */
    public ICode getLodgingType() {
        return lodgingType;
    }

    /**
     * Setter method sets the lodging type
     *
     * @param lodgingType
     */
    public void setLodgingType(LodgingType lodgingType) {
        this.lodgingType = lodgingType;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
//        if (!(o instanceof FixedTravelAllowance)) return false;

        FixedTravelAllowance that = (FixedTravelAllowance) o;

        if (excludedIndicator != that.excludedIndicator) return false;
        if (overnightIndicator != that.overnightIndicator) return false;
        if (locked != that.locked) return false;
        if (fixedTravelAllowanceId != null ? !fixedTravelAllowanceId.equals(that.fixedTravelAllowanceId) : that.fixedTravelAllowanceId != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (amount != null ? !amount.equals(that.amount) : that.amount != null) return false;
        if (currencyCode != null ? !currencyCode.equals(that.currencyCode) : that.currencyCode != null)
            return false;
        if (locationName != null ? !locationName.equals(that.locationName) : that.locationName != null)
            return false;
        if (breakfastProvision != null ? !breakfastProvision.equals(that.breakfastProvision) : that.breakfastProvision != null)
            return false;
        if (lunchProvision != null ? !lunchProvision.equals(that.lunchProvision) : that.lunchProvision != null)
            return false;
        if (dinnerProvision != null ? !dinnerProvision.equals(that.dinnerProvision) : that.dinnerProvision != null)
            return false;
        return !(lodgingType != null ? !lodgingType.equals(that.lodgingType) : that.lodgingType != null);

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = fixedTravelAllowanceId != null ? fixedTravelAllowanceId.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        result = 31 * result + (currencyCode != null ? currencyCode.hashCode() : 0);
        result = 31 * result + (excludedIndicator ? 1 : 0);
        result = 31 * result + (locationName != null ? locationName.hashCode() : 0);
        result = 31 * result + (breakfastProvision != null ? breakfastProvision.hashCode() : 0);
        result = 31 * result + (lunchProvision != null ? lunchProvision.hashCode() : 0);
        result = 31 * result + (dinnerProvision != null ? dinnerProvision.hashCode() : 0);
        result = 31 * result + (overnightIndicator ? 1 : 0);
        result = 31 * result + (lodgingType != null ? lodgingType.hashCode() : 0);
        result = 31 * result + (locked ? 1 : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FixedTravelAllowance another) {
        if (another == null) {
            return -1;
        }

        if (getDate() != null && another.getDate() != null) {
            /* Both dates initialized hence compare the dates. */
            Calendar cal1 = Calendar.getInstance();
            cal1.setTime(getDate());

            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(another.getDate());

            Comparator<Calendar> comp = DateUtils
                    .getCalendarIgnoringTimeComparator();

            return comp.compare(cal2, cal1);
        } else if (getDate() != null && another.getDate() == null) {
			/* This date is non null hence consider this object as greater. */
            return 1;
        } else if (getDate() == null && another.getDate() != null) {
			/* This date is null hence consider this object as less. */
            return -1;
        }
        return 0;
    }
}
