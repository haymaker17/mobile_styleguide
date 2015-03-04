package com.concur.mobile.core.request.task;

import android.content.Context;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 16/01/2015.
 */
public class RequestGroupConfigurationsTask extends AbstractRequestWSCallTask {

    public RequestGroupConfigurationsTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    /**
     * Gets the service end-point for this request.
     *
     * @return returns the service end-point for this request.
     * @throws com.concur.mobile.core.service.ServiceRequestException
     */
    protected String getServiceEndPoint() throws ServiceRequestException {
        final Map<String, Object> params = new HashMap<String, Object>();
        return ConnectHelper.getServiceEndpointURI(ConnectHelper.ConnectVersion.VERSION_3_1,
                ConnectHelper.Module.GROUP_CONFIGURATIONS, ConnectHelper.Action.LIST, params, true);
    }
}
