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

    /**
     * The expense report holding the travel allowances
     */
    private ExpenseReport expenseReport;

    /**
     * An usual expense
     */
    private ExpenseReportEntry expenseReportEntry;

    /**
     * An expense with transaction data 5th of may 2015 and expKey "FXMLS".
     * This one should match the matchable allowance of the report
     */
    private ExpenseReportEntryDetail expenseReportEntryDetail;

    public FixedTravelAllowanceTestData() {

        List<FixedTravelAllowance> allowances = new ArrayList<FixedTravelAllowance>(2);
        Calendar cal = Calendar.getInstance();
        this.expenseReport = new ExpenseReport("ER1");

        FixedTravelAllowance matchingAllowance = new FixedTravelAllowance("FA1");
        FixedTravelAllowance nonMatchingAllowance = new FixedTravelAllowance("FA2");

        cal.set(2015, 5, 5);
        matchingAllowance.setAmount(5.0);
        matchingAllowance.setDate(new Date(cal.getTimeInMillis()));
        matchingAllowance.setCurrencyCode("USD");
        matchingAllowance.setBreakfastProvision(new MealProvision("PRO", "Provided"));
        matchingAllowance.setLunchProvision(new MealProvision("NPR", "Not Provided"));
        matchingAllowance.setDinnerProvision(new MealProvision("NPR", "Not Provided"));
        allowances.add(matchingAllowance);

        cal.set(2015, 5, 6);
        nonMatchingAllowance.setAmount(6.0);
        nonMatchingAllowance.setDate(new Date(cal.getTimeInMillis()));
        nonMatchingAllowance.setCurrencyCode("USD");
        nonMatchingAllowance.setBreakfastProvision(new MealProvision("NPR", "Provided"));
        nonMatchingAllowance.setLunchProvision(new MealProvision("NPR", "Not Provided"));
        nonMatchingAllowance.setDinnerProvision(new MealProvision("NPR", "Not Provided"));
        allowances.add(nonMatchingAllowance);

        this.expenseReport.setAllowances(allowances);

        //TODO: Setup Expense Report Entries...

    }

    public ExpenseReport getExpenseReport() {
        return expenseReport;
    }

    public ExpenseReportEntry getExpenseReportEntry() {
        return expenseReportEntry;
    }

    public ExpenseReportEntryDetail getExpenseReportEntryDetail() {
        return expenseReportEntryDetail;
    }
}
