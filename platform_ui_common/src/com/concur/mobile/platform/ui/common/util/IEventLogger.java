package com.concur.mobile.platform.ui.common.util;

import android.app.Activity;

/**
 * An interface used by platform ui objects to trigger event logging.
 * 
 * Implementation can call Flurry or any other APIs that performs the actual logging For example:
 * 
 * <pre>
 * <code>
 *  public class myEventLogger implements IEventLogger {
 * 
 *      String eventName; 
 *      Map<String, String> eventParams;
 * 
 *      public void logEvent(Activity context) 
 *      { 
 *          EventTracker.track(eventName, eventParams, context); 
 *      }
 *  }
 * </code>
 * </pre>
 * 
 * @author yiwenw
 * 
 */
public interface IEventLogger {

    /**
     * Log the event with the event name and parameters
     */
    public void logEvent(Activity context);
}
