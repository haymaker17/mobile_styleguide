package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;

public class DeleteItineraryRowRequest extends AbstractItineraryDeleteRequest {

    private String itinKey;
    private String irKey;

    public DeleteItineraryRowRequest(Context context, BaseAsyncResultReceiver receiver, String itinKey,
            String irKey) {
        super(context, receiver);
        this.itinKey = itinKey;
        this.irKey = irKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/DeleteItineraryRow/" + itinKey + "/" + irKey;
    }

}
