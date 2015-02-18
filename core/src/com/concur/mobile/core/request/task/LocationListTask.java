package com.concur.mobile.core.request.task;

import android.content.Context;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 17/02/2015.
 */
public class LocationListTask extends AbstractRequestWSCallTask {

    /* *** parameters ***
    offset 		Starting page offset 	query 	string
    limit 		Number of records to return. The default is 25. 	query 	Int32
    name 		A common name associated with this location. The name can be a location description such as a neighborhood (SoHo), a landmark (Statue of Liberty), or a city name (New York). 	query 	string
    city 		The city name. 	query 	string
    countrySubdivision 		ISO 3166-2:2007 country subdivision. 	query 	string
    country 		2-letter ISO 3166-1 country code. 	query 	string
    administrativeRegion 		Administrative region. 	query 	string
    isAirport 		Whether the location is an Airport. format: true or false 	query 	Boolean
    includeHubs 		Whether the Airport location search should includes hubs or not. format: true or false 	query 	Boolean
    typeCode  	Whether the location search should be restricted by the location type code. Possible values are: STD (Standard) 	query 	LocationTypeCode
    */
    private static final String PARAM_OFFSET = "offset";
    private static final String PARAM_NAME = "name";
    private static final String PARAM_CITY = "city";
    private static final String PARAM_COUNTRY_SUBDIVISION = "countrySubdivision";
    private static final String PARAM_COUNTRY = "country";
    private static final String PARAM_ADMINISTRATIVE_REGION = "administrativeRegion";
    private static final String PARAM_IS_AIRPORT = "isAirport";
    private static final String PARAM_INCLUDE_HUBS = "includeHubs";
    private static final String PARAM_TYPE_CODE = "typeCode";

    private String searchedText = null;
    private boolean isAirport = false;

    public LocationListTask(Context context, int id, BaseAsyncResultReceiver receiver, String input,
            boolean isAirport) {
        super(context, id, receiver);
        this.searchedText = input;
        this.isAirport = isAirport;
    }

    @Override protected String getServiceEndPoint() throws ServiceRequestException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_NAME, URLEncoder.encode(searchedText));
        params.put(PARAM_IS_AIRPORT, isAirport);
        params.put(PARAM_INCLUDE_HUBS, isAirport);
        params.put(ConnectHelper.PARAM_LIMIT, 15);
        return ConnectHelper
                .getServiceEndpointURI(ConnectHelper.ConnectVersion.VERSION_3_0, ConnectHelper.Module.LOCATION,
                        ConnectHelper.Action.LIST, params, true);
    }
}
