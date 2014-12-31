package com.concur.mobile.core.request.util;

import java.util.Map;
import java.util.Map.Entry;

import com.concur.mobile.core.service.ServiceRequestException;

/**
 * @author olivierb
 */
public class ConnectHelper {

    private static final String ENDPOINT_URI = "/api/v3.0/";
    public static final String REQUEST_ID = "id";

    // reminder - TR SERVICE_END_POINT =
    // "/api/v3.0/travelrequest/requests?status=PENDING_EBOOKING&offset=0&limit=25";

    public enum Module {
        TRAVEL_REQUEST("travelrequest/requests"),
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
        CREATE("save");

        public String actionValue;

        Action(String action) {
            this.actionValue = action;
        }
    }

    /**
     * Generates the webservice endpoint url
     * 
     * @param module the module name for the ws to call
     * @param action the specific action in the module
     * @param queryStringParameters get || post parameters
     * @param isGet == (task.getPostBody() == null)
     * @return the endpoint url
     * @throws ServiceRequestException exception which occurred
     */
    public static String getServiceEndpointURI(Module module, Action action, Map<String, Object> queryStringParameters, boolean isGet)
            throws ServiceRequestException {
        return getServiceEndpointURI(module, action, queryStringParameters, null, isGet);
    }


    /**
     * Generates the webservice endpoint url
     * 
     * @param module the module name for the ws to call
     * @param action the specific action in the module
     * @param queryStringParameters get || post parameters
     * @param isGet == (task.getPostBody() == null)
     * @param id the id of the specific entity we want to retrieve/work on
     * @return the endpoint url
     * @throws ServiceRequestException exception which occurred
     */
    public static String getServiceEndpointURI(Module module, Action action, Map<String, Object> queryStringParameters, String id, 
            boolean isGet) throws ServiceRequestException {
        // --- "/api/v3.0/travelrequest"
        final StringBuilder serviceUri = new StringBuilder(ENDPOINT_URI + module.moduleValue);

		switch(action){

		//get output status (example) : "/api/v3.0/travelrequest/requests"
		case LIST:			
			break;

		//get output status (example) : "/api/v3.0/travelrequest/requests/{id}"
		case DETAIL:
			
			if (id != null && id.length() > 0)
				serviceUri.append("/" + id);
			else
				throw new ServiceRequestException("ID cannot be null");

			break;

		//post output status (example) "/api/v3.0/travelrequest/requests/{id}/submit"
		case SUBMIT:

            if (id != null && id.length() > 0)
                serviceUri.append("/" + id);
            else
                throw new ServiceRequestException("ID cannot be null");

            if (action.actionValue != null)
                serviceUri.append("/" + action.actionValue);
			else
				throw new ServiceRequestException("Action cannot be null");
			break;

		//DEFAULT
		default :
			break;
        }

		// --- output status (example) : "/api/v3.0/travelrequest/...?...&..."
        addParameters(queryStringParameters, serviceUri, isGet);

        return serviceUri.toString();
    }

    /**
     * @param parameters
     * @param serviceUri
     */
    private static void addParameters(Map<String, Object> parameters, final StringBuilder serviceUri, boolean isGet) {
        if (isGet){
            // --- GET behavior
            boolean first = true;
            if (parameters != null)
                for (Entry<String, Object> param : parameters.entrySet()) {
                    if (param.getValue() != null) {
                        if (first) {
                            serviceUri.append("?");
                            first = false;
                        } else
                            serviceUri.append("&");
                        serviceUri.append(param.getKey() + "="
                                + (param.getValue() instanceof String ? param.getValue() : param.getValue().toString()));
                    }
                }
        }
        // --- POST is handled by the getPostBody() method of the task
    }
}
