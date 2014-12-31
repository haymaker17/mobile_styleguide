/**
 * 
 */
package com.concur.mobile.core.expense.data;

import java.util.ArrayList;
import java.util.Calendar;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.PersonalCard;

/**
 * Models a list of expense items with update time information.
 * 
 * @author AndrewK
 */
public class ExpenseListInfo {

    // The list of expenses.
    protected ArrayList<Expense> expenses;

    // The list of personal cards.
    protected ArrayList<PersonalCard> personalCards;

    // The last update time.
    protected Calendar updateTime;

    /**
     * Constructs an instance of <code>ExpenseReportList</code> with a list of reports and the last update time.
     * 
     * @param expenses
     *            the list of expenses.
     * @param personalCards
     *            the list of personal cards.
     * @param updateTime
     *            the last update time.
     */
    public ExpenseListInfo(ArrayList<Expense> expenses, ArrayList<PersonalCard> personalCards, Calendar updateTime) {
        if (expenses != null) {
            this.expenses = expenses;
        } else {
            this.expenses = new ArrayList<Expense>();
        }
        if (personalCards != null) {
            this.personalCards = personalCards;
        } else {
            this.personalCards = new ArrayList<PersonalCard>();
        }
        this.updateTime = updateTime;
    }

    /**
     * Gets the expense list.
     * 
     * @return the expense list.
     */
    public ArrayList<Expense> getExpenseList() {
        return expenses;
    }

    /**
     * Gets the personal card list.
     * 
     * @return the personal card list.
     */
    public ArrayList<PersonalCard> getPersonalCards() {
        return personalCards;
    }

    /**
     * Gets the expense list last update time.
     * 
     * @return the expense list last update time.
     */
    public Calendar getUpdateTime() {
        return updateTime;
    }

}
