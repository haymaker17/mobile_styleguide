package com.concur.mobile.core.expense.travelallowance.service;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;

/**
 * Created by D049515 on 20.07.2015.
 */
public class DeleteItineraryRequest extends AbstractItineraryDeleteRequest {


    private String itinKey;

    public DeleteItineraryRequest(Context context, BaseAsyncResultReceiver receiver, String itinKey) {
        super(context, receiver);
        this.itinKey = itinKey;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/TravelAllowance/DeleteItinerary/" + itinKey;
    }


}
