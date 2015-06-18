package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.report.data.ExpenseReportEntry;
import com.concur.mobile.core.expense.report.data.ExpenseReportEntryDetail;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Test data representing an expense report
 *
 * @author Michael Becherer
 */
public class FixedTravelAllowanceTestData {

    private List<FixedTravelAllowance> allowances;

    public FixedTravelAllowanceTestData() {

        allowances = new ArrayList<FixedTravelAllowance>();
        Calendar cal = Calendar.getInstance();

        for (int i = 1; i <= 12; i++){
            FixedTravelAllowance allowance = new FixedTravelAllowance("FTA" + i);
            cal.set(2015, 5, i);
            allowance.setDate(new Date(cal.getTimeInMillis()));
            allowance.setAmount(new Double(i));
            allowance.setCurrencyCode("USD");
            allowance.setBreakfastProvision(new MealProvision("NPR", "Not Provided"));
            allowance.setLunchProvision(new MealProvision("NPR", "Not Provided"));
            allowance.setDinnerProvision(new MealProvision("NPR", "Not Provided"));
            allowance.setLocationName("Chicago, IL");
            allowances.add(allowance);
        }

        allowances.get(1).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(2).setLunchProvision(new MealProvision("PRO", "Provided"));
        allowances.get(3).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(3).setLunchProvision(new MealProvision("PRO", "Provided"));
        allowances.get(4).setDinnerProvision(new MealProvision("PRO", "Provided"));

        allowances.get(5).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(5).setDinnerProvision(new MealProvision("PRO", "Provided"));
        allowances.get(5).setLocationName("San Francisco, CA");
        allowances.get(6).setLunchProvision(new MealProvision("PRO", "Provided"));
        allowances.get(6).setDinnerProvision(new MealProvision("PRO", "Provided"));
        allowances.get(6).setLocationName("San Francisco, CA");
        allowances.get(7).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(7).setLunchProvision(new MealProvision("PRO", "Provided"));
        allowances.get(7).setDinnerProvision(new MealProvision("PRO", "Provided"));
        allowances.get(7).setLocationName("San Francisco, CA");

        allowances.get(8).setExcludedIndicator(true);
        allowances.get(8).setLocationName("Los Angeles, CA");
        allowances.get(9).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(9).setExcludedIndicator(true);
        allowances.get(9).setLocationName("Los Angeles, CA");

        allowances.get(10).setBreakfastProvision(new MealProvision("PRO", "Provided"));
        allowances.get(10).setDinnerProvision(new MealProvision("BSE", "Business Meal"));
        allowances.get(10).setLocationName("Seattle, WA");
        allowances.get(11).setBreakfastProvision(new MealProvision("BSE", "Business Meal"));
        allowances.get(11).setDinnerProvision(new MealProvision("PRO", "Provided"));
        allowances.get(11).setLocationName("Seattle, WA");

    }

    /**
     * Gets the mock data for fixed travel allowances
     * @return List of allowances
     */
    public List<FixedTravelAllowance> getAllowances(){
        return allowances;
    }
}
