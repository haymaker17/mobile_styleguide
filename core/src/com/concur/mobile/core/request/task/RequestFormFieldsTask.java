/**
 * 
 */
package com.concur.mobile.core.request.task;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.request.util.ConnectHelper;
import com.concur.mobile.core.request.util.ConnectHelper.Action;
import com.concur.mobile.core.request.util.ConnectHelper.Module;
import com.concur.mobile.core.service.ServiceRequestException;

/**
 * @author OlivierB
 */
public class RequestFormFieldsTask extends AbstractRequestWSCallTask {
    // --- used both for parameter sent & to retrieve id on ws deserialization
    public static final String PARAM_FORM_ID = "formID";

    // true to retrieve fields for a header form, false for a segment one
    private boolean isHeader;
    private String formId;
    
    public RequestFormFieldsTask(Context context, int id, BaseAsyncResultReceiver receiver, String formID, boolean isHeader) {
        super(context, id, receiver);
        this.formId = formID;
        this.isHeader = isHeader;
        resultData.putString(PARAM_FORM_ID, formId);
    }

    @Override
    protected String getServiceEndPoint() throws ServiceRequestException {
        final Map<String, Object> params = new HashMap<String, Object>();
        params.put(PARAM_FORM_ID, this.formId);
        return ConnectHelper.getServiceEndpointURI(Module.FORM_FIELDS, isHeader ? Action.FORM_FIELDS_HEADER : Action.FORM_FIELDS_SEGMENT, params,
            true);
    }

}
