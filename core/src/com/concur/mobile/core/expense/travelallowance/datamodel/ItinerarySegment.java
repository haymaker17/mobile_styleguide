package com.concur.mobile.core.expense.travelallowance.datamodel;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ItinerarySegment implements Serializable, IDatePeriod, ISynchronizationStatus {


    private static final long serialVersionUID = -5416179865430781088L;

    private String id;
    private ItineraryLocation departureLocation;
    private Date departureDateTime;
    private ItineraryLocation arrivalLocation;
    private Date arrivalDateTime;
    private Date borderCrossDateTime;

    /**
     * The synchronization status of this {@code ItinerarySegment}
     */
    private SynchronizationStatusEnum syncStatus;


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ItineraryLocation getDepartureLocation() {
        return departureLocation;
    }

    public void setDepartureLocation(ItineraryLocation departureLocation) {
        this.departureLocation = departureLocation;
    }

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public ItineraryLocation getArrivalLocation() {
        return arrivalLocation;
    }

    public void setArrivalLocation(ItineraryLocation arrivalLocation) {
        this.arrivalLocation = arrivalLocation;
    }

    public Date getArrivalDateTime() {
        return arrivalDateTime;
    }

    public void setArrivalDateTime(Date arrivalDateTime) {
        this.arrivalDateTime = arrivalDateTime;
    }

    public Date getBorderCrossDateTime() {
        return borderCrossDateTime;
    }

    public void setBorderCrossDateTime(Date borderCrossDateTime) {
        this.borderCrossDateTime = borderCrossDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartDate() {
        return this.departureDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getEndDate() {
        return this.arrivalDateTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SynchronizationStatusEnum getStatus() {
        return this.syncStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStatus(SynchronizationStatusEnum status) {
        this.syncStatus = status;
    }
}
