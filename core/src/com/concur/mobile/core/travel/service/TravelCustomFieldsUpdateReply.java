package com.concur.mobile.core.travel.service;

import com.concur.mobile.core.service.ActionStatusServiceReply;
import com.concur.mobile.core.travel.data.TravelCustomFieldsConfig;

public class TravelCustomFieldsUpdateReply extends ActionStatusServiceReply {

    /**
     * Contains the serialized XML reply.
     */
    public String xmlReply;

    /**
     * Contains a reference to the travel custom field configuration.
     */
    public TravelCustomFieldsConfig config;

}
