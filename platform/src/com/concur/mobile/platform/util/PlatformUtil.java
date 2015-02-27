package com.concur.mobile.platform.util;

import java.util.Calendar;

/**
 * util class
 * 
 * @author tejoa
 * 
 */

public class PlatformUtil {

    /**
     * create end point url for Hotel search
     * 
     * @param end_point
     * @param lat
     * @param lon
     * @param distanceUnit
     * @param checkInDate
     * @param checkOutDate
     * @return
     */
    public static String getEndpointurl(String end_point, Double lat, Double lon, String distanceUnit,
            Calendar checkInDate, Calendar checkOutDate) {
        StringBuilder endPointUrlBldr = new StringBuilder(end_point);

        endPointUrlBldr.append("?latitude=");
        endPointUrlBldr.append(lat);
        endPointUrlBldr.append("&longitude=");
        endPointUrlBldr.append(lon);
        endPointUrlBldr.append("&distanceUnit=");
        endPointUrlBldr.append(distanceUnit);
        endPointUrlBldr.append("&checkin=");
        endPointUrlBldr.append(Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, checkInDate));
        endPointUrlBldr.append("&checkout=");
        endPointUrlBldr.append(Format.safeFormatCalendar(Parse.LONG_YEAR_MONTH_DAY, checkOutDate));
        endPointUrlBldr.append("&radius=25");
        return endPointUrlBldr.toString();
    }

}
