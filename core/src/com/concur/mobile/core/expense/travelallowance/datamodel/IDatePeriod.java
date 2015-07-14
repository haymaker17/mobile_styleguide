package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.util.Date;

/**
 * Interface for all objects which have a validity period.
 *
 * Created by Michael Becherer on 14-Jul-15.
 */

public interface IDatePeriod {

    /**
     * To get the start date of the validity period.
     *
     * @return the start date of the validity period
     */
    Date getStartDate();

    /**
     * To get the end date of the validity period.
     *
     * @return the end date of the validity period
     */
    Date getEndDate();

}
