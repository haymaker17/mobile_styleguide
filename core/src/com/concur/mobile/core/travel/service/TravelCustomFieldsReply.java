/**
 * 
 */
package com.concur.mobile.core.travel.service;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;

/**
 * An extension of <code>ServiceReply</code> to handle the response to a travel custom fields request.
 */
public class TravelCustomFieldsReply extends ServiceReply {

    /**
     * Contains the serialized XML reply.
     */
    public String xmlReply;

    /**
     * Contains a reference to the travel custom field configuration.
     */
    public TravelCustomFieldsConfig config;

}
