/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.data;

import java.io.Serializable;
import java.util.Calendar;

import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.platform.util.Parse;

public class GovExpense implements Serializable {

    private static final long serialVersionUID = -5113920670670490818L;
    public String expenseDesc;
    public String expenseCategory;
    public String paymentMethod;
    public String imageid;
    public String exceptionCmt;
    public Boolean reimbursable;
    public Boolean exception;
    public String ccexpid;
    public Double amount;
    public String expid;
    public Calendar expDate;
    public Calendar tripEndDate;

    public GovExpense() {
        // TODO Auto-generated constructor stub
    }

    /** handle parsing elements */
    public void handleElement(String localName, String cleanChars) {
        if (localName.equalsIgnoreCase("ExpenseDesc")) {
            expenseDesc = cleanChars;
        } else if (localName.equalsIgnoreCase("ExpenseCategory")) {
            expenseCategory = cleanChars;
        } else if (localName.equalsIgnoreCase("PaymentMethod")) {
            paymentMethod = cleanChars;
        } else if (localName.equalsIgnoreCase("imageid")) {
            imageid = cleanChars;
        } else if (localName.equalsIgnoreCase("ccexpid")) {
            ccexpid = cleanChars;
        } else if (localName.equalsIgnoreCase("ExpDate")) {
            expDate = Parse.parseTimestamp(cleanChars, FormatUtil.LONG_YEAR_MONTH_DAY);
        } else if (localName.equalsIgnoreCase("expid")) {
            expid = cleanChars;
        } else if (localName.equalsIgnoreCase("Amount")) {
            amount = Parse.safeParseDouble(cleanChars);
        } else if (localName.equalsIgnoreCase("Reimbursable")) {
            reimbursable = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("Exception")) {
            exception = Parse.safeParseBoolean(cleanChars);
        } else if (localName.equalsIgnoreCase("ExceptionCmt")) {
            exceptionCmt = cleanChars;
        }
    }
}
