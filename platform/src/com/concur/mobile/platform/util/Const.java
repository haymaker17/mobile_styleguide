package com.concur.mobile.platform.util;

public class Const {

    // Instantiation is a crime.
    private Const() {
    }

    // -------------------------------------------------
    // General
    // -------------------------------------------------
    public static final String LOG_TAG = "CNQR.PLATFORM";

    // Contains whether various parsers should report unexpected tags.
    public static final Boolean DEBUG_PARSING = true;

    // Last Location fields.
    public static final int LOC_UPDATE_MIN_DISTANCE = 10000; // minimum distance
                                                             // in meters
    public static final long LOC_UPDATE_MIN_TIME = 30 * 60000; // minimum time
                                                               // in
                                                               // milliseconds

}
