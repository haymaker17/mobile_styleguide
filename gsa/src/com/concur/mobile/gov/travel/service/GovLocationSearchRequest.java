package com.concur.mobile.gov.travel.service;

import com.concur.mobile.core.travel.service.LocationSearchRequest;

public class GovLocationSearchRequest extends LocationSearchRequest {

    public static final String SERVICE_END_POINT = "/Mobile/GovTravelManager/LocationSearch";

    @Override
    protected String getServiceEndpointURI() {
        return SERVICE_END_POINT;
    }

}
