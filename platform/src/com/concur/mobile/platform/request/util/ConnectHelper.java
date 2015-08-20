package com.concur.mobile.platform.request.util;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;
import com.concur.mobile.base.service.BaseAsyncRequestTask;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author olivierb
 */
public class ConnectHelper {

    private static final String ENDPOINT_URI = "/api/";

    public static final String PARAM_LIMIT = "limit";
    private static final int DEFAULT_LIST_LIMIT_VALUE = 10;

    // reminder - TR SERVICE_END_POINT =
    // "/api/v3.0/travelrequest/requests?status=PENDING_EBOOKING&offset=0&limit=25";

    public enum Module {
        REQUEST_LOCATION("travelrequest/locations"),
        REQUEST("travelrequest/requests"),
        REQUEST_ENTRY("travelrequest/segmentsentries"),
        GROUP_CONFIGURATIONS("travelrequest/requestgroupconfigurations"),
        FORM_FIELDS("travelrequest/formsfields");

        public String moduleValue;

        Module(String module) {
            this.moduleValue = module;
        }
    }

    public enum Action {
        LIST(null),
        DETAIL(null),
        RECALL("recall"),
        CREATE_AND_SUBMIT(null),
        UPDATE_AND_SUBMIT(null);

        public String actionValue;

        Action(String action) {
            this.actionValue = action;
        }
    }

    public enum ConnectVersion {
        VERSION_3_0("v3.0"),
        VERSION_3_1("v3.1");

        public String versionNumber;

        ConnectVersion(String versionNumber) {
            this.versionNumber = versionNumber;
        }
    }

    /**
     * Generates the webservice endpoint url
     *
     * @param module                the module name for the ws to call
     * @param action                the specific action in the module
     * @param queryStringParameters get || post parameters
     * @param isGet                 == (task.getPostBody() == null)
     * @return the endpoint url
     * @throws Exception exception which occurred
     */
    public static String getServiceEndpointURI(ConnectVersion connectVersion, Module module, Action action,
            Map<String, Object> queryStringParameters, boolean isGet) throws Exception {
        return getServiceEndpointURI(connectVersion, module, action, queryStringParameters, null, isGet);
    }

    /**
     * Generates the webservice endpoint url
     *
     * @param module                the module name for the ws to call
     * @param action                the specific action in the module
     * @param queryStringParameters get || post parameters
     * @param isGet                 == (task.getPostBody() == null)
     * @param id                    the id of the specific entity we want to retrieve/work on
     * @return the endpoint url
     * @throws Exception exception which occurred
     */
    public static String getServiceEndpointURI(ConnectVersion connectVersion, Module module, Action action,
            Map<String, Object> queryStringParameters, String id, boolean isGet) throws Exception {
        // --- "/api/v3.0/travelrequest"
        final StringBuilder serviceUri = new StringBuilder(
                ENDPOINT_URI + connectVersion.versionNumber + "/" + module.moduleValue);

        switch (action) {

        //get output status (example) : "/api/v3.0/travelrequest/requests"
        case LIST:
            // Set the limit parameter close to server limit (100) to avoid any pagination
            queryStringParameters.put(PARAM_LIMIT, queryStringParameters.containsKey(PARAM_LIMIT) ?
                    queryStringParameters.get(PARAM_LIMIT) :
                    DEFAULT_LIST_LIMIT_VALUE);
            break;

        //get output status (example) : "/api/v3.0/travelrequest/requests/{id}"
        case DETAIL:

            if (id != null && id.length() > 0) {
                serviceUri.append("/" + id);
            } else {
                throw new Exception("ID cannot be null");
            }

            break;

        //post output status (example) "/api/v3.0/travelrequest/requests/{id}/submit"
        case RECALL:
            if (id != null && id.length() > 0) {
                serviceUri.append("/" + id);
            } else {
                throw new Exception("ID cannot be null");
            }

            if (action.actionValue != null) {
                serviceUri.append("/" + action.actionValue);
            } else {
                throw new Exception("Action cannot be null");
            }
            break;

        //post output status (example) "/api/v3.0/travelrequest/requests"
        case CREATE_AND_SUBMIT:
            break;

        //post output status (example) "/api/v3.0/travelrequest/requests/{id}"
        case UPDATE_AND_SUBMIT:
            if (id != null && id.length() > 0) {
                serviceUri.append("/" + id);
            } else {
                throw new Exception("ID cannot be null");
            }
            break;

        //DEFAULT
        default:
            break;
        }

        // --- output status (example) : "/api/v3.0/travelrequest/...?...&..."
        addParameters(queryStringParameters, serviceUri, isGet);

        return serviceUri.toString();
    }

    /**
     * @param parameters parameters to add
     * @param serviceUri string representation of the uri to call
     */
    private static void addParameters(Map<String, Object> parameters, final StringBuilder serviceUri, boolean isGet) {
        boolean first = true;
        if (parameters != null) {
            for (Entry<String, Object> param : parameters.entrySet()) {
                if (param.getValue() != null) {
                    if (first) {
                        serviceUri.append("?");
                        first = false;
                    } else {
                        serviceUri.append("&");
                    }
                    serviceUri.append(param.getKey() + "=" + (param.getValue() instanceof String ?
                            param.getValue() :
                            param.getValue().toString()));
                }
            }
        }
    }

    // should be moved in another helper probably
    public static void displayResponseMessage(Context context, Bundle resultData, String defaultMessage) {
        CharSequence text = resultData.getString(BaseAsyncRequestTask.HTTP_STATUS_MESSAGE);
        if (text == null || text.toString().trim().length() == 0) {
            text = defaultMessage;
        }
        final Toast toast = Toast.makeText(context, text, Toast.LENGTH_LONG);
        toast.show();
    }

    // should be moved in another helper probably
    public static void displayMessage(Context context, String message) {
        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
