package com.concur.mobile.core.travel.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * List of trips for approval
 * 
 * @author RatanK
 * 
 */
public class TripsToApproveList implements Serializable {

    private static final long serialVersionUID = 5244493258395653474L;

    public final static String TRIP_TO_APPROVE = "TripToApprove";

    public final List<TripToApprove> tripsToApproveList = new ArrayList<TripToApprove>();

    public void add(TripToApprove tripToApprove) {
        tripsToApproveList.add(tripToApprove);
    }
}
