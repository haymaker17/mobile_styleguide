package com.concur.mobile.core.expense.travelallowance.datamodel;

import android.content.Context;

import com.concur.mobile.core.expense.travelallowance.util.DateUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.core.R;
import com.concur.mobile.core.expense.travelallowance.util.StringUtilities;

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
    private MealProvision breakfastProvision;

    /**
     * Denotes, how the lunch is provisioned
     */
    private MealProvision lunchProvision;

    /**
     * Denotes, how the dinner is provisioned
     */
    private MealProvision dinnerProvision;

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
    public MealProvision getBreakfastProvision() {
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
    public MealProvision getLunchProvision() {
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
    public MealProvision getDinnerProvision() {
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FixedTravelAllowance)) return false;

        FixedTravelAllowance that = (FixedTravelAllowance) o;

        if (excludedIndicator != that.excludedIndicator) return false;
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
        return !(dinnerProvision != null ? !dinnerProvision.equals(that.dinnerProvision) : that.dinnerProvision != null);

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

    /**
     * Builds a string based on the meals provisions (breakfast, lunch and dinner)
     *
     * @return The textual representation of the meal provisions. Empty String, if
     * there is nothing.
     */
    public String mealsProvisionToText(Context context) {

        if (context == null) {
            return "";
        }

        String resultString = StringUtilities.EMPTY_STRING;
        String provisionText = StringUtilities.EMPTY_STRING;
        List<String> mealsList;
        Map<MealProvision, List<String>> provisionMap = new HashMap<MealProvision, List<String>>();

        if (!StringUtilities.isNullOrEmpty(breakfastProvision.getCode())
                && !breakfastProvision.getCode().equals(notProvidedCode)) {
            mealsList = new ArrayList<String>();
            mealsList.add(context.getString(R.string.itin_breakfast));
            provisionMap.put(breakfastProvision, mealsList);
        }

        if (!StringUtilities.isNullOrEmpty(lunchProvision.getCode())
                && !lunchProvision.getCode().equals(notProvidedCode)) {
            if (provisionMap.containsKey(lunchProvision)) {
                mealsList = provisionMap.get(lunchProvision);
                mealsList.add(context.getString(R.string.itin_lunch));
            } else {
                mealsList = new ArrayList<String>();
                mealsList.add(context.getString(R.string.itin_lunch));
                provisionMap.put(lunchProvision, mealsList);
            }
        }

        if (!StringUtilities.isNullOrEmpty(dinnerProvision.getCode())
                && !dinnerProvision.getCode().equals(notProvidedCode)) {
            if (provisionMap.containsKey(dinnerProvision)) {
                mealsList = provisionMap.get(dinnerProvision);
                mealsList.add(context.getString(R.string.itin_dinner));
            } else {
                mealsList = new ArrayList<String>();
                mealsList.add(context.getString(R.string.itin_dinner));
                provisionMap.put(dinnerProvision, mealsList);
            }
        }

        for (MealProvision key: provisionMap.keySet()) {
            int i = 0;
            resultString = resultString + key + ": ";
            for (String value: provisionMap.get(key)){
                i++;
                resultString = resultString + value;
                if (i < provisionMap.get(key).size()) {
                    resultString = resultString + ", ";
                }
            }
        }
        return resultString;
    }
}
