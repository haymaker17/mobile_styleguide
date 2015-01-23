package com.concur.mobile.core.request.task;

import android.content.Context;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.service.RequestParser;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.platform.request.dto.RequestDTO;

import java.util.HashMap;

/**
 * Created by OlivierB on 20/01/2015.
 */
public class RequestSaveTask extends AbstractRequestWSCallTask {

    private RequestDTO tr = null;

    public RequestSaveTask(Context context, int id, BaseAsyncResultReceiver receiver, RequestDTO tr) {
        super(context, id, receiver);
        this.tr = tr;
    }

    @Override
    protected String getServiceEndPoint() throws ServiceRequestException {
        return ConnectHelper.getServiceEndpointURI(ConnectHelper.Module.REQUEST,
                (tr.getId() != null ? ConnectHelper.Action.UPDATE : ConnectHelper.Action.CREATE),
                new HashMap<String, Object>(), tr.getId(), false);
    }

    @Override
    protected String getPostBody() {
        return RequestParser.toJson(tr);
    }
}
