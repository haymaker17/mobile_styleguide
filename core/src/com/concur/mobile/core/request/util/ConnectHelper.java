package com.concur.mobile.core.request.util;

import com.concur.mobile.core.service.ServiceRequestException;

import java.util.Map;
import java.util.Map.Entry;

/**
 * @author olivierb
 */
public class ConnectHelper {

    private static final String ENDPOINT_URI = "/api/v3.0/";
    public static final String REQUEST_ID = "id";

    public static final String PARAM_LIMIT = "limit";
    private static final int DEFAULT_LIST_LIMIT_VALUE = 10;

    // reminder - TR SERVICE_END_POINT =
    // "/api/v3.0/travelrequest/requests?status=PENDING_EBOOKING&offset=0&limit=25";

    public enum Module {
        REQUEST("travelrequest/requests"),
        GROUP_CONFIGURATIONS("travelrequest/requestgroupconfigurations"),
        FORM_FIELDS("expense/formfields");

        public String moduleValue;

        Module(String module) {
            this.moduleValue = module;
        }
    }

    public enum Action {
        LIST(null),
        DETAIL(null),
        FORM_FIELDS_HEADER(null),
        FORM_FIELDS_SEGMENT(null),
        SUBMIT("submit"),
        CREATE(null),
        UPDATE(null);

        public String actionValue;

        Action(String action) {
            this.actionValue = action;
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
     * @throws ServiceRequestException exception which occurred
     */
    public static String getServiceEndpointURI(Module module, Action action, Map<String, Object> queryStringParameters,
            boolean isGet) throws ServiceRequestException {
        return getServiceEndpointURI(module, action, queryStringParameters, null, isGet);
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
     * @throws ServiceRequestException exception which occurred
     */
    public static String getServiceEndpointURI(Module module, Action action, Map<String, Object> queryStringParameters,
            String id, boolean isGet) throws ServiceRequestException {
        // --- "/api/v3.0/travelrequest"
        final StringBuilder serviceUri = new StringBuilder(ENDPOINT_URI + module.moduleValue);

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
                throw new ServiceRequestException("ID cannot be null");
            }

            break;

        //post output status (example) "/api/v3.0/travelrequest/requests/{id}/submit"
        case SUBMIT:
            if (id != null && id.length() > 0) {
                serviceUri.append("/" + id);
            } else {
                throw new ServiceRequestException("ID cannot be null");
            }

            if (action.actionValue != null) {
                serviceUri.append("/" + action.actionValue);
            } else {
                throw new ServiceRequestException("Action cannot be null");
            }
            break;

        //post output status (example) "/api/v3.0/travelrequest/requests"
        case CREATE:
            break;

        //post output status (example) "/api/v3.0/travelrequest/requests/{id}"
        case UPDATE:
            if (id != null && id.length() > 0) {
                serviceUri.append("/" + id);
            } else {
                throw new ServiceRequestException("ID cannot be null");
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
        if (isGet) {
            // --- GET behavior
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
        // --- POST is handled by the getPostBody() method of the task
    }
}
