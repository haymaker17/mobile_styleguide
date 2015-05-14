package com.concur.mobile.core.request.task;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.service.ServiceRequestException;
import com.concur.mobile.platform.util.Const;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 04/03/2015.
 * This class execute a request through Connect with given args.
 * => Should be used for any ws call within Travel Request using Connect API
 * TODO : migrate all other tasks to this one
 */
public class RequestTask extends AbstractRequestWSCallTask {

    private static final String CLS_TAG = "RequestTask";

    // --- Locations
    // searchText 	A common name (or IATA code) associated with this location.
    // lookup       The lookup search term specifies which type of location to return. default value : CITY.
    public static final String P_LOCATION_SEARCH_TEXT = "searchText";
    public static final String P_LOCATION_TYPE = "lookup";
    // --- Forms & fields - used both for parameter sent & to retrieve id on ws deserialization
    public static final String P_FORM_ID = "formID";
    // --- Request List
    public static final String P_REQUESTS_WITH_SEG_TYPES = "withSegmentTypes";
    public static final String P_REQUESTS_STATUS = "status";
    // --- Request creation
    public static final String P_REQUEST_ID = "RequestID";

    public enum HttpRequestType {
        GET,
        POST,
        PUT
    }

    private String entityId;
    private Map<String, Object> params;
    // --- Args that can be set after creation
    private ConnectHelper.ConnectVersion version = ConnectHelper.ConnectVersion.VERSION_3_1;
    private ConnectHelper.Module module = ConnectHelper.Module.REQUEST;
    private ConnectHelper.Action action;
    private HttpRequestType requestType = HttpRequestType.GET;
    private String postBody = "";

    /**
     * Default TR constructor without url parameters
     *
     * @param context
     * @param taskId
     * @param receiver
     * @param action
     * @param entityId
     */
    public RequestTask(Context context, int taskId, BaseAsyncResultReceiver receiver, ConnectHelper.Action action,
            String entityId) {
        super(context, taskId, receiver);
        this.entityId = entityId;
        this.params = new HashMap<String, Object>();
        setAction(action);
    }

    /**
     * Full parameterized constructor
     *
     * @param context
     * @param taskId
     * @param receiver
     * @param version
     * @param module
     * @param action
     * @param entityId
     */
    public RequestTask(Context context, int taskId, BaseAsyncResultReceiver receiver,
            ConnectHelper.ConnectVersion version, ConnectHelper.Module module, ConnectHelper.Action action,
            String entityId) {
        super(context, taskId, receiver);
        this.version = version;
        this.module = module;
        this.entityId = entityId;
        this.params = new HashMap<String, Object>();
        setAction(action);
    }

    /**
     * Gets the service end-point for this request.
     *
     * @return returns the service end-point for this request.
     * @throws com.concur.mobile.core.service.ServiceRequestException
     */
    @Override protected String getServiceEndPoint() throws ServiceRequestException {
        return ConnectHelper
                .getServiceEndpointURI(version, module, action, params, entityId, requestType == HttpRequestType.GET);
    }

    @Override protected String getPostBody() {
        return postBody;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#configureConnection(java.net.HttpURLConnection)
     */
    @Override protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        if (requestType == HttpRequestType.PUT) {
            try {
                connection.setRequestMethod(HttpRequestType.PUT.name());
            } catch (ProtocolException protExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".configureConnection: protocol exception setting request method to '"
                        + HttpRequestType.PUT.name() + "'", protExc);
            }
        }
    }

    public void addUrlParameter(String key, String value) {
        params.put(key, value);
    }

    public void setPostBody(String postBody) {
        // --- null won't work as a value if it really is a POST, things are made to work with "" in this case.
        this.postBody = postBody != null ? postBody : (requestType == HttpRequestType.POST ? "" : null);
    }

    public void setHttpRequestType(HttpRequestType requestType) {
        this.requestType = requestType;
    }

    public void setVersion(ConnectHelper.ConnectVersion version) {
        this.version = version;
    }

    public void setModule(ConnectHelper.Module module) {
        this.module = module;
    }

    public void setAction(ConnectHelper.Action action) {
        this.action = action;
        if (action == ConnectHelper.Action.UPDATE) {
            requestType = HttpRequestType.PUT;
        } else if (action == ConnectHelper.Action.CREATE) {
            requestType = HttpRequestType.POST;
        }
    }
}
