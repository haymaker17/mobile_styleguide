package com.concur.mobile.platform.base;

/**
 * Used by classes that need to start/stop service depending on application state.
 * 
 * This is a close approximation. The callbacks are called when app goes background/foreground and top level activity switches
 * orientation.
 * 
 * @author yiwenw
 */
public interface VisibleActivityStateCallbacks {

    public void onReturningToVisibility();

    public void onGoingToInvisibility();

}
