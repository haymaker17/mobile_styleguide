/**
 * 
 */
package com.concur.mobile.core.travel.service;

import java.util.List;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.Trip;

/**
 * An extension of <code>ServiceReply</code> for handling the result of a current itineraries request.
 * 
 * @author andy
 */
public class ItinerarySummaryListReply extends ServiceReply {

    public List<Trip> trips;

    public String xmlReply;

}
