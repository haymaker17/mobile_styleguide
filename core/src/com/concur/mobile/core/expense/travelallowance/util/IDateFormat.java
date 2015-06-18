package com.concur.mobile.core.expense.travelallowance.util;

import java.util.Date;

/**
 * @author Patricius Komarnicki, Michael Becherer
 *
 */
public interface IDateFormat {

    String format(final Date date, final boolean includeTime, final boolean includeDayOfWeek);

}