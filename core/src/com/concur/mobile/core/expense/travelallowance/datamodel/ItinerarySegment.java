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
    private boolean locked;
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

    public boolean getLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ItinerarySegment that = (ItinerarySegment) o;

        if (locked != that.locked) return false;
        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (departureLocation != null ? !departureLocation.equals(that.departureLocation) : that.departureLocation != null)
            return false;
        if (departureDateTime != null ? !departureDateTime.equals(that.departureDateTime) : that.departureDateTime != null)
            return false;
        if (arrivalLocation != null ? !arrivalLocation.equals(that.arrivalLocation) : that.arrivalLocation != null)
            return false;
        if (arrivalDateTime != null ? !arrivalDateTime.equals(that.arrivalDateTime) : that.arrivalDateTime != null)
            return false;
        if (borderCrossDateTime != null ? !borderCrossDateTime.equals(that.borderCrossDateTime) : that.borderCrossDateTime != null)
            return false;
        return true;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (departureLocation != null ? departureLocation.hashCode() : 0);
        result = 31 * result + (departureDateTime != null ? departureDateTime.hashCode() : 0);
        result = 31 * result + (arrivalLocation != null ? arrivalLocation.hashCode() : 0);
        result = 31 * result + (arrivalDateTime != null ? arrivalDateTime.hashCode() : 0);
        result = 31 * result + (borderCrossDateTime != null ? borderCrossDateTime.hashCode() : 0);
        result = 31 * result + (locked ? 1 : 0);
        return result;
    }
}
