/**
 * 
 */
package com.concur.mobile.core.expense.charge.service;

import java.util.ArrayList;

import com.concur.mobile.core.expense.charge.data.Expense;
import com.concur.mobile.core.expense.charge.data.PersonalCard;
import com.concur.mobile.core.service.ServiceReply;

/**
 * 
 * An extension of <code>ServiceReply</code> for capturing the reply to a request to retrieve all expenses.
 * 
 * @deprecated v9.16 - This has been replaced with the SmartExpense and ExpenseProvider.
 * 
 * @author AndrewK
 */
@Deprecated
public class AllExpenseReply extends ServiceReply {

    /**
     * Contains a list of parsed <code>Expense</code> objects.
     */
    public ArrayList<Expense> expenses;

    /**
     * Contains a list of parsed <code>PersonalCard</code> objects.
     */
    public ArrayList<PersonalCard> personalCards;

    /**
     * Contains the XML representation of the response body.
     */
    public String xmlReply;

}
