package com.concur.mobile.core.travel.data;

public class Agency {

    private String name;
    private String address;
    private String dayTimeHoursStarts;
    private String dayTimeHoursEnds;
    private String dayTimeMessage;
    private String dayTimePhone;
    private String nightTimeHoursStarts;
    private String nightTimeHoursEnds;
    private String nightTimeMessage;
    private String nightTimePhone;
    private PreferredTimeForPhoneEnum preferredTimeForPhoneEnum;

    // this enum is used in the AgencyParser
    public static enum PreferredTimeForPhoneEnum {
        Daytime, Night
    };

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDayTimeHoursStarts() {
        return dayTimeHoursStarts;
    }

    public void setDayTimeHoursStarts(String dayTimeHoursStarts) {
        this.dayTimeHoursStarts = dayTimeHoursStarts;
    }

    public String getDayTimeHoursEnds() {
        return dayTimeHoursEnds;
    }

    public void setDayTimeHoursEnds(String dayTimeHoursEnds) {
        this.dayTimeHoursEnds = dayTimeHoursEnds;
    }

    public String getDayTimeMessage() {
        return dayTimeMessage;
    }

    public void setDayTimeMessage(String dayTimeMessage) {
        this.dayTimeMessage = dayTimeMessage;
    }

    public String getDayTimePhone() {
        return dayTimePhone;
    }

    public void setDayTimePhone(String dayTimePhone) {
        this.dayTimePhone = dayTimePhone;
    }

    public String getNightTimeHoursStarts() {
        return nightTimeHoursStarts;
    }

    public void setNightTimeHoursStarts(String nightTimeHoursStarts) {
        this.nightTimeHoursStarts = nightTimeHoursStarts;
    }

    public String getNightTimeHoursEnds() {
        return nightTimeHoursEnds;
    }

    public void setNightTimeHoursEnds(String nightTimeHoursEnds) {
        this.nightTimeHoursEnds = nightTimeHoursEnds;
    }

    public String getNightTimeMessage() {
        return nightTimeMessage;
    }

    public void setNightTimeMessage(String nightTimeMessage) {
        this.nightTimeMessage = nightTimeMessage;
    }

    public String getNightTimePhone() {
        return nightTimePhone;
    }

    public void setNightTimePhone(String nightTimePhone) {
        this.nightTimePhone = nightTimePhone;
    }

    public PreferredTimeForPhoneEnum getPreferredTimeForPhoneEnum() {
        return preferredTimeForPhoneEnum;
    }

    public void setPreferredTimeForPhoneEnum(PreferredTimeForPhoneEnum preferredTimeForPhoneEnum) {
        this.preferredTimeForPhoneEnum = preferredTimeForPhoneEnum;
    }

    /**
     * Retrieve the preferred phone number
     * 
     * @return
     */
    public String getPreferredPhoneNumber() {
        // default to day time phone number
        String preferredPhoneNumber = getDayTimePhone();

        // check if preference is night time
        switch (getPreferredTimeForPhoneEnum()) {
        case Night:
            preferredPhoneNumber = getNightTimePhone();
            break;
        default:
            break;
        }

        return preferredPhoneNumber;
    }
}
