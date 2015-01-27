package com.concur.mobile.core.request.task;

import android.content.Context;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.ConnectHelper.Action;
import com.concur.mobile.core.request.util.ConnectHelper.Module;
import com.concur.mobile.core.request.util.RequestStatus;
import com.concur.mobile.core.service.ServiceRequestException;

import java.util.HashMap;
import java.util.Map;

/**
 * @author olivierb
 */
public class RequestListTask extends AbstractRequestWSCallTask {

    private static final String PARAM_WITH_SEGMENT_TYPES = "withSegmentTypes";
    private static final String PARAM_STATUS = "status";

    // default value : active
    private RequestStatus statusRequested = RequestStatus.ACTIVE;

    public RequestListTask(Context context, int requestId, BaseAsyncResultReceiver receiver) {
        super(context, requestId, receiver);
    }

    public RequestListTask(Context context, int requestId, BaseAsyncResultReceiver receiver,
            RequestStatus statusRequested) {
        this(context, requestId, receiver);
        this.statusRequested = statusRequested;
    }

    public void setStatusRequested(RequestStatus statusRequested) {
        this.statusRequested = statusRequested;
    }

    /**
     * Gets the service end-point for this request.
     *
     * @return returns the service end-point for this request.
     * @throws ServiceRequestException
     */
    protected String getServiceEndPoint() throws ServiceRequestException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_STATUS, this.statusRequested);
        params.put(PARAM_WITH_SEGMENT_TYPES, Boolean.TRUE);
        params.put(ConnectHelper.PARAM_LIMIT, 100);
        return ConnectHelper.getServiceEndpointURI(Module.REQUEST, Action.LIST, params, true);
    }
}
