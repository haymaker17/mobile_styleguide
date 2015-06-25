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
        MealProvision notProvided = new MealProvision("NPR", "@Not Provided@");
        MealProvision provided =  new MealProvision("PRO", "@Provided@");
        MealProvision business = new MealProvision("BSE", "@Business Meal@");
        LodgingType hotel = new LodgingType("HOTEL", "@Hotel@");
        LodgingType motel = new LodgingType("MOTEL", "@Motel@");
        Calendar cal = Calendar.getInstance();
        
        for (int i = 1; i <= 15; i++){
            FixedTravelAllowance allowance = new FixedTravelAllowance("FTA" + i);
            cal.set(2015, 5, i);
            allowance.setDate(new Date(cal.getTimeInMillis()));
            allowance.setAmount(new Double(i));
            allowance.setCurrencyCode("USD");
            allowance.setBreakfastProvision(notProvided);
            allowance.setLunchProvision(notProvided);
            allowance.setDinnerProvision(notProvided);
            allowance.setLocationName("@Chicago, IL@");
            allowances.add(allowance);
        }

        allowances.get(1).setBreakfastProvision(provided);
        allowances.get(2).setLunchProvision(provided);
        allowances.get(3).setBreakfastProvision(provided);
        allowances.get(3).setLunchProvision(provided);
        allowances.get(4).setDinnerProvision(provided);

        allowances.get(5).setBreakfastProvision(provided);
        allowances.get(5).setDinnerProvision(provided);
        allowances.get(5).setLocationName("@San Francisco, CA@");
        allowances.get(6).setLunchProvision(provided);
        allowances.get(6).setDinnerProvision(provided);
        allowances.get(6).setLocationName("@San Francisco, CA@");
        allowances.get(7).setBreakfastProvision(provided);
        allowances.get(7).setLunchProvision(provided);
        allowances.get(7).setDinnerProvision(provided);
        allowances.get(7).setLocationName("@San Francisco, CA@");

        allowances.get(8).setExcludedIndicator(true);
        allowances.get(8).setLocationName("@Los Angeles, CA@");
        allowances.get(9).setBreakfastProvision(provided);
        allowances.get(9).setExcludedIndicator(true);
        allowances.get(9).setLocationName("@Los Angeles, CA@");

        allowances.get(10).setBreakfastProvision(provided);
        allowances.get(10).setDinnerProvision(business);
        allowances.get(10).setLocationName("@Seattle, WA@");
        allowances.get(11).setBreakfastProvision(business);
        allowances.get(11).setDinnerProvision(provided);
        allowances.get(11).setLocationName("@Seattle, WA@");
        allowances.get(12).setLunchProvision(provided);
        allowances.get(12).setDinnerProvision(business);
        allowances.get(12).setLocationName("@Seattle, WA@");

        allowances.get(13).setOvernightIndicator(true);
        allowances.get(13).setLodgingType(hotel);
        allowances.get(13).setLocationName("@New York@");
        allowances.get(14).setOvernightIndicator(true);
        allowances.get(14).setLodgingType(motel);
        allowances.get(14).setLocationName("@New York@");

    }

    /**
     * Gets the mock data for fixed travel allowances
     * NTreturn List of allowances
     */
    public List<FixedTravelAllowance> getAllowances(final boolean firstLocation){
        if (firstLocation) {
            List<FixedTravelAllowance> firstLocationAllowances = new ArrayList<FixedTravelAllowance>();
            for (FixedTravelAllowance allowance: allowances){
                if (allowances.get(0).getLocationName().equals(allowance.getLocationName())){
                    firstLocationAllowances.add(allowance);
                }
            }
            return firstLocationAllowances;
        }
        return allowances;
    }

}
