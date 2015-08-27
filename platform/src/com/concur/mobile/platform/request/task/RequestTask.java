package com.concur.mobile.platform.request.task;

import android.content.Context;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.request.util.ConnectHelper;
import com.concur.mobile.platform.util.Const;

import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 04/03/2015.
 * This class execute a request through Connect with given args.
 * => Should be used for any ws call within Travel Request using Connect API
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
    public static final String P_REQUESTS_WITH_USER_PERMISSIONS = "withUserPermissions";
    // --- Request creation
    public static final String P_REQUEST_ID = "RequestID";
    public static final String P_REQUEST_DO_SUBMIT = "doSubmit";
    public static final String P_REQUEST_FORCE_SUBMIT = "forceSubmit";

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
    private String postBody = null;

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
     * Gets the service end-point for this request.
     *
     * @return returns the service end-point for this request.
     * @throws Exception
     */
    @Override
    protected String getServiceEndPoint() throws Exception {
        return ConnectHelper
                .getServiceEndpointURI(version, module, action, params, entityId, requestType == HttpRequestType.GET);
    }

    @Override
    protected String getPostBody() {
        return postBody;
    }

    /**
     * (non-Javadoc)
     *
     * @see com.concur.mobile.platform.service.PlatformAsyncRequestTask#configureConnection(java.net.HttpURLConnection)
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
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

    public RequestTask addUrlParameter(String key, String value) {
        params.put(key, value);
        return this;
    }

    public RequestTask setPostBody(String postBody) {
        if (requestType != HttpRequestType.POST) {
            requestType = HttpRequestType.POST;
        }
        // --- null won't work as a value if it really is a POST, things are made to work with "" in this case.
        this.postBody = postBody != null ? postBody : (requestType == HttpRequestType.POST ? "" : null);
        return this;
    }

    public RequestTask setAction(ConnectHelper.Action action) {
        this.action = action;
        switch (action) {

            case LIST:
            case DETAIL:
                postBody = null;
                requestType = HttpRequestType.GET;
                break;

        case UPDATE_AND_SUBMIT:
            postBody = "";
            requestType = HttpRequestType.PUT;
            break;

        case CREATE_AND_SUBMIT:
        default:
            // --- any custom action not specifically defined will be considered as a POST action
            postBody = "";
            requestType = HttpRequestType.POST;
            break;
        }
        return this;
    }

    public RequestTask addResultData(String name, String value) {
        resultData.putString(name, value);
        return this;
    }
}
