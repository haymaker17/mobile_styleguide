package com.concur.mobile.core.expense.travelallowance.datamodel;

/**
 * Interface to be implemented by domain classes to inform interested
 * parties about their current synchronization status.
 *
 * Created by Michael Becherer on 14-Jul-15.
 */
public interface ISynchronizationStatus {

    /**
     * Returns the current synchronization status.
     *
     * @return the current synchronization status
     *
     * @see SynchronizationStatusEnum
     */
    SynchronizationStatusEnum getStatus();

    /**
     * Sets the current synchronization status.
     *
     * @param status the new {@code SynchronizationStatus} to set
     */
    void setStatus(SynchronizationStatusEnum status);

}
