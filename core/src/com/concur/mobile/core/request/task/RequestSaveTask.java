package com.concur.mobile.core.request.task;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.platform.request.dto.RequestDTO;
import com.concur.mobile.platform.util.Const;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;

/**
 * Created by OlivierB on 20/01/2015.
 */
public class RequestSaveTask extends AbstractRequestWSCallTask {

    private static final String CLS_TAG = "AbstractRequestWSCallTask";

    private RequestDTO tr = null;

    public RequestSaveTask(Context context, int id, BaseAsyncResultReceiver receiver, RequestDTO tr) {
        super(context, id, receiver);
        this.tr = tr;
    }

    @Override
    protected String getServiceEndPoint() throws ServiceRequestException {
        return ConnectHelper
                .getServiceEndpointURI(ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.REQUEST,
                        (tr.getId() != null ? ConnectHelper.Action.UPDATE : ConnectHelper.Action.CREATE),
                        new HashMap<String, Object>(), tr.getId(), false);
    }

    @Override
    protected String getPostBody() {
        return RequestParser.toJson(tr);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#configureConnection(java.net.HttpURLConnection)
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        if (tr.getId() != null) {
            try {
                connection.setRequestMethod(REQUEST_METHOD_PUT);
            } catch (ProtocolException protExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                        + REQUEST_METHOD_POST + "'", protExc);
            }
        }
    }
}
