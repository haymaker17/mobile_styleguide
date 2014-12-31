package com.concur.mobile.core.data;

import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.data.ExpensePolicy;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.expense.report.data.AttendeeType;
import com.concur.mobile.core.expense.report.data.ExpenseConfirmation;
import com.concur.mobile.core.travel.car.data.CarType;
import com.concur.mobile.core.travel.data.TravelPointsConfig;
import com.concur.mobile.core.view.SpinnerItem;

public class UserConfig {

    public ArrayList<CarType> allowedCarTypes;
    public ArrayList<ListItem> currencies;
    public ArrayList<ListItem> reimbursementCurrencies;
    public ArrayList<AttendeeType> attendeeTypes;
    public ArrayList<ExpenseConfirmation> expenseConfirmations;
    public ArrayList<ExpensePolicy> expensePolicies;
    public ArrayList<String> allowedAirClassService;
    public ArrayList<ListItem> yodleePaymentTypes;
    public TravelPointsConfig travelPointsConfig;
    public boolean showGDSNameInSearchResults;

    /**
     * Contains the server computed hash code for the information contained in this user configuration object.
     */
    public String hash;

    /**
     * Contains the response string, either one of 'UPDATED' or 'NO_CHANGE' depending upon changes occurring on the server.
     */
    public String responseId;

    public void handleElement(String localName, String cleanChars) {

        if (localName.equalsIgnoreCase("Hash")) {
            hash = cleanChars;
        } else if (localName.equalsIgnoreCase("ResponseId")) {
            responseId = cleanChars;
        }

    }

    /**
     * Gets an array of <code>SpinnerItem</code> that can be used to populate a discrete choice list.
     * 
     * @return an array of <code>SpinnerItem</code> objects that can be used to populate a discrete choice.
     */
    public SpinnerItem[] getYodleePaymentTypeItems() {
        SpinnerItem[] items = null;
        if (yodleePaymentTypes != null) {
            items = new SpinnerItem[yodleePaymentTypes.size()];
            for (int i = 0; i < yodleePaymentTypes.size(); ++i) {
                ListItem listItem = yodleePaymentTypes.get(i);
                if (listItem != null) {
                    items[i] = new SpinnerItem(listItem.key, listItem.text);
                }
            }
        }
        return items;
    }

    public void populateCarTypes() {
        // If the only car type is the "any" then load up the list with all valid car types
        if (allowedCarTypes != null && allowedCarTypes.size() == 1 && allowedCarTypes.get(0).code.trim().length() == 0) {

            Context ctx = ConcurCore.getContext();

            allowedCarTypes.add(new CarType("C*", ctx.getString(R.string.car_search_car_type_C)));
            allowedCarTypes.add(new CarType("E*", ctx.getString(R.string.car_search_car_type_E)));
            allowedCarTypes.add(new CarType("F*", ctx.getString(R.string.car_search_car_type_F)));
            allowedCarTypes.add(new CarType("I*", ctx.getString(R.string.car_search_car_type_I), true));
            allowedCarTypes.add(new CarType("L*", ctx.getString(R.string.car_search_car_type_L)));
            allowedCarTypes.add(new CarType("M*", ctx.getString(R.string.car_search_car_type_M)));
            allowedCarTypes.add(new CarType("O*", ctx.getString(R.string.car_search_car_type_O)));
            allowedCarTypes.add(new CarType("P*", ctx.getString(R.string.car_search_car_type_P)));
            allowedCarTypes.add(new CarType("S*", ctx.getString(R.string.car_search_car_type_S)));
        }
    }

    /**
     * Will retrieve the instance of <code>AttendeeType</code> for <code>atnTypeKey</code>.
     * 
     * @param atnTypeKey
     *            contains an attendee type key.
     * @return returns an instance of <code>AttendeeType</code> if found; otherwise, <code>null</code>.
     */
    public AttendeeType getAttendeeType(String atnTypeKey) {
        AttendeeType retVal = null;
        if (attendeeTypes != null) {
            for (AttendeeType atdType : attendeeTypes) {
                if (atdType.atnTypeKey != null && atdType.atnTypeKey.length() > 0
                        && atdType.atnTypeKey.equalsIgnoreCase(atnTypeKey)) {
                    retVal = atdType;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Gets the report submit expense confirmation text/title information.
     * 
     * @param polKey
     *            the policy key.
     * @return returns an instance of <code>ExpenseConfirmation</code> for <code>polKey</code> if one has been set; otherwise,
     *         <code>null</code>.
     */
    public ExpenseConfirmation getSubmitConfirmation(String polKey) {
        ExpenseConfirmation expConf = null;
        if (polKey != null) {
            // Locate the policy first.
            ExpensePolicy expPolicy = findExpensePolicy(polKey);
            if (expPolicy != null) {
                // Locate the report submission confirmation.
                if (expPolicy.subConfKey != null) {
                    expConf = findExpenseConfirmation(expPolicy.subConfKey);
                }
            }
        }
        return expConf;
    }

    /**
     * Gets the report approve expense confirmation text/title information.
     * 
     * @param polKey
     *            the policy key.
     * @return returns an instance of <code>ExpenseConfirmation</code> for <code>polKey</code> if one has been set; otherwise,
     *         <code>null</code>.
     */
    public ExpenseConfirmation getApproveConfirmation(String polKey) {
        ExpenseConfirmation expConf = null;
        if (polKey != null) {
            // Locate the policy first.
            ExpensePolicy expPolicy = findExpensePolicy(polKey);
            if (expPolicy != null) {
                // Locate the report submission confirmation.
                if (expPolicy.appConfKey != null) {
                    expConf = findExpenseConfirmation(expPolicy.appConfKey);
                }
            }
        }
        return expConf;
    }

    private ExpensePolicy findExpensePolicy(String polKey) {
        ExpensePolicy retVal = null;
        if (expensePolicies != null) {
            for (ExpensePolicy policy : expensePolicies) {
                if (policy.polKey != null && policy.polKey.equalsIgnoreCase(polKey)) {
                    retVal = policy;
                    break;
                }
            }
        }
        return retVal;
    }

    private ExpenseConfirmation findExpenseConfirmation(String confKey) {
        ExpenseConfirmation retVal = null;
        if (expenseConfirmations != null) {
            for (ExpenseConfirmation confirmation : expenseConfirmations) {
                if (confirmation.confKey != null && confirmation.confKey.equalsIgnoreCase(confKey)) {
                    retVal = confirmation;
                    break;
                }
            }
        }
        return retVal;
    }

    /**
     * Populates an air class list with the values from a space-delimited string.
     * 
     * @param airClassListString
     */
    public void populateAirClass(String airClassListString) {
        if (airClassListString != null && airClassListString.trim().length() > 0) {
            Collections.addAll(allowedAirClassService, airClassListString.split(" "));
        }
    }
}
