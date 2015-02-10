package com.concur.mobile.core.request.task;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.platform.request.dto.RequestEntryDTO;
import com.concur.mobile.platform.request.util.RequestParser;
import com.concur.mobile.platform.util.Const;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;

/**
 * Created by OlivierB on 06/02/2015.
 * //TODO factorize TR tasks somehow (should be feasible)
 */
public class RequestEntrySaveTask extends AbstractRequestWSCallTask {

    private static final String CLS_TAG = "AbstractRequestWSCallTask";

    private RequestEntryDTO entry = null;

    public RequestEntrySaveTask(Context context, int id, BaseAsyncResultReceiver receiver,
            final RequestEntryDTO entry) {
        super(context, id, receiver);
        this.entry = entry;
    }

    @Override
    protected String getServiceEndPoint() throws ServiceRequestException {
        return ConnectHelper
                .getServiceEndpointURI(ConnectHelper.ConnectVersion.VERSION_3_1, ConnectHelper.Module.REQUEST_ENTRY,
                        (entry.getId() != null ? ConnectHelper.Action.UPDATE : ConnectHelper.Action.CREATE),
                        new HashMap<String, Object>(), entry.getId(), false);
    }

    @Override
    protected String getPostBody() {
        return RequestParser.toJson(entry);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#configureConnection(java.net.HttpURLConnection)
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        if (entry.getId() != null) {
            try {
                connection.setRequestMethod(REQUEST_METHOD_PUT);
            } catch (ProtocolException protExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                        + REQUEST_METHOD_POST + "'", protExc);
            }
        }
    }
}
