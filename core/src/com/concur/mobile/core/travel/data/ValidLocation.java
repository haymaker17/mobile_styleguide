/**
 * 
 */
package com.concur.mobile.core.travel.data;

import android.location.Location;

/**
 * Models a simple location.
 */
public class ValidLocation {

    public Double latitude;

    public Double longitude;

    public Double proximity;

    public Location asLocation() {
        Location loc = new Location("Concur");
        loc.setLatitude(latitude);
        loc.setLongitude(longitude);
        return loc;
    }
}
