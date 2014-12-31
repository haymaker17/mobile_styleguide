package com.concur.mobile.core.request.task;

import java.util.HashMap;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.ConnectHelper.Action;
import com.concur.mobile.core.request.util.ConnectHelper.Module;
import com.concur.mobile.core.service.ServiceRequestException;

public class RequestSubmitTask extends AbstractRequestWSCallTask {

    private String requestID = null;

    /**
     * Contains values
     */

    public RequestSubmitTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String requestID_) {
        super(context, requestId, receiver);

        requestID = requestID_;
    }

    /**
     * Gets the service end-point for this request.
     * 
     * @return returns the service end-point for this request.
     * @throws ServiceRequestException
     */
    protected String getServiceEndPoint() throws ServiceRequestException {
        return ConnectHelper.getServiceEndpointURI(Module.TRAVEL_REQUEST, Action.SUBMIT, new HashMap<String, Object>(),
                requestID, false);
    }
    
    @Override
    protected String getPostBody() {
        return "";
    }
}
