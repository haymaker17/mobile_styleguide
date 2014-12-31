/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.travel.activity;

import android.content.Intent;

import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.service.ServiceRequest;

/**
 * Base class when a travel search is in progress.
 * 
 * @author Chris N. Diaz
 * 
 */
public abstract class AbstractTravelSearchProgress extends BaseActivity {

    /**
     * Returns the <code>ServiceRequest</code>.
     * 
     * @return the <code>ServiceRequest</code>.
     */
    public abstract ServiceRequest getSearchRequest();

    /**
     * Sets the <code>ServiceRequst</code> to set and associate with a broadcast receiver.
     * 
     * @param searchRequest
     *            the <code>ServiceRequst</code> to set and associate with a broadcast receiver.
     */
    public abstract void setSearchRequest(ServiceRequest searchRequest);

    /**
     * Invoked after the broadcast receiver's <code>onReceive()</code> methods has completed successfully.
     */
    public abstract void onReceiveComplete();

    /**
     * Gets the <code>Intent</code> used to launch after successfully receiving search results.
     * 
     * @return the <code>Intent</code> used to launch after successfully receiving search results.
     */
    public abstract Intent getResultsIntent();

    /**
     * Gets the <code>Intent</code> used to launch when there are no search results.
     * 
     * @return the <code>Intent</code> used to launch when there are no search results.
     */
    public abstract Intent getNoResultsIntent();

    /**
     * Returns <code>true</code> to finish this activity when there is no search results.
     * 
     * @return <code>true</code> to finish this activity when there is no search results.
     */
    public abstract boolean finishActivityOnNoResults();

}
