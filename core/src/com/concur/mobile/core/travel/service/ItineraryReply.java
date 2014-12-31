/**
 * 
 */
package com.concur.mobile.core.travel.service;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.Trip;

/**
 * An extension of <code>ServiceReply</code> for the purpose of handling a respons to retrieve an itinerary.
 * 
 * @author andy
 */
public class ItineraryReply extends ServiceReply {

    public Trip trip;

    public String xmlReply;

}
