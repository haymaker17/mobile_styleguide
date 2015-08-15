package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.util.Date;

/**
 * Interface to be implemented by objects in order to convert date periods into
 * UTC via the corresponding getter methods
 *
 * Created by Michael Becherer on 14-Jul-15.
 */

public interface IDatePeriodUTC {

    /**
     * To get the start date of the validity period.
     *
     * @return the start date of the validity period in UTC(Locale date/time + Offset)
     */
    Date getStartDateUTC();

    /**
     * To get the end date of the validity period.
     *
     * @return the end date of the validity period in UTC (Local date/time + Offset)
     */
    Date getEndDateUTC();

}
