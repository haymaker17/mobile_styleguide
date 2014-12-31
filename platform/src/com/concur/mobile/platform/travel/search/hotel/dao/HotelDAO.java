package com.concur.mobile.platform.travel.search.hotel.dao;

import android.content.Context;
import android.net.Uri;

public interface HotelDAO {

    /**
     * Gets the content uri associated with this DAO object.
     * 
     * @param context
     *            contains a reference to an application context.
     * @return the content Uri associated with this DAO object.
     */
    public Uri getContentURI(Context context);

    /**
     * Will perform an update based on current values.
     * 
     * @param context
     *            contains a reference to an application context.
     * @return returns <code>true</code> upon success; <code>false</code> otherwise.
     */
    public boolean updateHotel(Context context);

    // /**
    // * Will delete the hotel.
    // *
    // * @param context
    // * contains a reference to an application context.
    // * @return <code>true</code> upon success; <code>false</code> otherwise.
    // */
    // public boolean delete(Context context);

}
