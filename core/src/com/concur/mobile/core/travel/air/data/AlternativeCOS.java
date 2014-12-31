/**
 * Helper class to parse AlternativeAirScheduleReply.
 * 
 * @author sunill
 * */
package com.concur.mobile.core.travel.air.data;

public class AlternativeCOS {

    public String cabin;
    public String seats;

    public String isCabin() {
        return cabin;
    }

    public void setCabin(String cabin) {
        this.cabin = cabin;
    }

    public String getSeats() {
        return seats;
    }

    public void setSeats(String seats) {
        this.seats = seats;
    }

}
