package com.concur.mobile.core.expense.travelallowance.controller.test;

import android.content.Context;
import android.os.Bundle;

import com.concur.mobile.core.expense.travelallowance.controller.TravelAllowanceItineraryController;
import com.concur.mobile.core.expense.travelallowance.service.GetAssignableItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.service.GetTAItinerariesRequest;
import com.concur.mobile.core.expense.travelallowance.testutils.FileRequestTaskWrapper;

/**
 * Created by D028778 on 29-Sep-15.
 */
public class TravelAllowanceItineraryControllerMock extends TravelAllowanceItineraryController {


    public TravelAllowanceItineraryControllerMock(Context context) {
        super(context);
    }

    public Bundle refreshItineraries(String path, String file) {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetTAItinerariesRequest(null, null, null, false));
        return requestWrapper.parseFile(path, file);
    }

    public Bundle refreshAssignableItineraries(String path, String file) {
        FileRequestTaskWrapper requestWrapper = new FileRequestTaskWrapper(new GetAssignableItinerariesRequest(null, null, null));
        return requestWrapper.parseFile(path, file);
    }

}
