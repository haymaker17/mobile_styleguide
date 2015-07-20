package com.concur.mobile.core.expense.travelallowance.datamodel;

import com.concur.mobile.core.expense.travelallowance.util.DateUtils;
import com.concur.mobile.core.expense.travelallowance.util.Message;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by D049515 on 23.06.2015.
 */
public class ItinerarySegment implements Serializable, IDatePeriodUTC {


    private static final long serialVersionUID = -5416179865430781088L;

    private String id;
    private ItineraryLocation departureLocation;
    private Date departureDateTime;
    private ItineraryLocation arrivalLocation;
    private Date arrivalDateTime;
    private Date borderCrossDateTime;
    private SynchronizationStatusEnum syncStatus;
    private Message message;

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
     * Setter method
     * @param message The message object to be set
     */
    public void setMessage(Message message) {
        this.message = message;
    }

    /**
     * Getter method
     * @return The message object
     */
    public Message getMessage() {
        return this.message;
    }

    /**
     * Setter method
     * @param syncStatus The synchronization status to be set
     */
    public void setSyncStatus(SynchronizationStatusEnum syncStatus) {
        this.syncStatus = syncStatus;
    }

    /**
     * Getter method
     * @return The synchronization status of this object
     */
    public SynchronizationStatusEnum getSyncStatus() {
        return this.syncStatus;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getStartDateUTC() {
        Date dateUTC = new Date();
        long departureTimeZoneOffset = 0;
        if (departureLocation != null) {
            departureTimeZoneOffset = departureLocation.getTimeZoneOffset();
        }
        dateUTC.setTime(departureDateTime.getTime() - DateUtils.convertMinutesToMilliseconds(departureTimeZoneOffset));
        return dateUTC;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Date getEndDateUTC() {
        Date dateUTC = new Date();
        long arrivalTimeZoneOffset = 0;
        if (arrivalLocation != null) {
            arrivalTimeZoneOffset = arrivalLocation.getTimeZoneOffset();
        }
        dateUTC.setTime(arrivalDateTime.getTime() - DateUtils.convertMinutesToMilliseconds(arrivalTimeZoneOffset));
        return dateUTC;
    }

}
