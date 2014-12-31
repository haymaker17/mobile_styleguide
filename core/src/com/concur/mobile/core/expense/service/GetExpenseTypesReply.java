/**
 * 
 */
package com.concur.mobile.core.expense.service;

import java.util.List;

import com.concur.mobile.core.expense.data.ExpenseType;
import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ServiceReply</code> for parsing a response to a <code>GetExpenseTypesRequest</code>.
 * 
 * @author AndrewK
 */
public class GetExpenseTypesReply extends ServiceReply {

    /**
     * Contains the list of parsed expense types.
     */
    public List<ExpenseType> expenseTypes;

    /**
     * Parses the list of expense types contained in <code>responseXml</code>.
     * 
     * @param responseXml
     *            the XML encoded list of expense types.
     * @return an instance of <code>GetExpenseTypesReply</code> containing the parsed response.
     */
    public static GetExpenseTypesReply parseReply(String responseXml) {
        GetExpenseTypesReply srvReply = new GetExpenseTypesReply();
        srvReply.expenseTypes = ExpenseType.parseExpenseTypeXml(responseXml);
        srvReply.mwsStatus = Const.STATUS_SUCCESS;
        return srvReply;
    }

}
