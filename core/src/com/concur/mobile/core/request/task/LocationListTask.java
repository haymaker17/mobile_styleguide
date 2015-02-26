package com.concur.mobile.core.request.task;

import android.content.Context;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.platform.request.location.Location;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 17/02/2015.
 */
public class LocationListTask extends AbstractRequestWSCallTask {

    /* *** parameters ***
    searchText 		A common name associated with this location. The name can be a location description such as a neighborhood (SoHo), a landmark (Statue of Liberty), or a city name (New York), or IATA code (CDG). 	query 	string
    lookup The lookup search term specifies which type of location to return. If no lookup value is sent, the default value CITY will be used. Possible values are: CITY, AIRPORT 	query 	string
    */
    private static final String PARAM_SEARCH_TEXT = "searchText";
    private static final String PARAM_LOCATION_TYPE = "lookup";

    private String searchedText = null;
    private Location.LocationType locationType = Location.LocationType.CITY;

    public LocationListTask(Context context, int id, BaseAsyncResultReceiver receiver, String input,
            Location.LocationType locationType) {
        super(context, id, receiver);
        this.searchedText = input;
        this.locationType = locationType;
    }

    @Override protected String getServiceEndPoint() throws ServiceRequestException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_SEARCH_TEXT, URLEncoder.encode(searchedText));
        params.put(PARAM_LOCATION_TYPE, locationType.name());
        params.put(ConnectHelper.PARAM_LIMIT, 15);
        return ConnectHelper
                .getServiceEndpointURI(ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.REQUEST_LOCATION,
                        ConnectHelper.Action.LIST, params, true);
    }
}
