package com.concur.mobile.platform.ui.travel.activity;

import android.app.Activity;
import android.content.Intent;

public abstract class AbstractTravelSearchProgress extends Activity {

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
