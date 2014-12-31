package com.concur.mobile.platform.base;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;
import android.util.Log;

/**
 * Track whether the top level activity is visible.
 * 
 * Used by classes that need to start/stop service depending on application state.
 * 
 * This is a close approximation. The callbacks are called when app goes background/foreground and top level activity switches
 * orientation.
 * 
 * @author yiwenw
 */
public class VisibleActivityStateTracker implements ActivityLifecycleCallbacks {

    private static final String CLS_TAG = VisibleActivityStateTracker.class.getSimpleName();

    // Thread safe singleton
    public final static VisibleActivityStateTracker INSTANCE = new VisibleActivityStateTracker();

    private Set<VisibleActivityStateCallbacks> subscribers;

    // Used to determine whether the app is in background
    private Activity currentVisibleActivity;

    protected VisibleActivityStateTracker() {
        currentVisibleActivity = null;
        subscribers = Collections.synchronizedSet(new HashSet<VisibleActivityStateCallbacks>());
    }

    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        Log.d(CLS_TAG, "onActivityCreated");
    }

    public void onActivityStarted(Activity activity) {
    }

    public void onActivityResumed(Activity activity) {
        setCurrentVisibleActivity(activity);
    }

    public void onActivityPaused(Activity activity) {
    }

    public void onActivityStopped(Activity activity) {
        Log.d(CLS_TAG, "onActivityStopped - isFinishing " + (activity.isFinishing() ? "Y" : "N"));
        clearCurrentVisibleActivity(activity);
    }

    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        Log.d(CLS_TAG, "onActivitySaveInstanceState");
    }

    public void onActivityDestroyed(Activity activity) {
        Log.d(CLS_TAG, "onActivityDestroyed");
    }

    private void notifyGoingBackground() {
        Log.d(CLS_TAG, "Notify going to background");

        Iterator<VisibleActivityStateCallbacks> iter = subscribers.iterator();
        while (iter.hasNext()) {
            VisibleActivityStateCallbacks sub = iter.next();
            if (sub != null)
                sub.onGoingToInvisibility();
        }
    }

    private void notifyReturningForeground() {
        Log.d(CLS_TAG, "Notify returning to foreground");
        Iterator<VisibleActivityStateCallbacks> iter = subscribers.iterator();
        while (iter.hasNext()) {
            VisibleActivityStateCallbacks sub = iter.next();
            if (sub != null)
                sub.onReturningToVisibility();
        }
    }

    /**
     * Forward the application state change events to the given subscriber
     * 
     * @param subscriber
     */
    public void registerSubscriber(VisibleActivityStateCallbacks subscriber) {
        subscribers.add(subscriber);
    }

    /**
     * Remove the subscriber
     * 
     * @param subscriber
     */
    public void unregisterSubscriber(VisibleActivityStateCallbacks subscriber) {
        subscribers.remove(subscriber);
    }

    /**
     * Allow an Activity class to declare itself as the current visible activity. When application is in the foreground, there is
     * always a visible activity. If there is no previous visible activity, then the application has been brought to foreground.
     * 
     * This depends on the fact when switching from one activity to another, the newActivity.onResume() is called before
     * oldActivity.onStop().
     * 
     * This should be called during Activity.onResume().
     * 
     * @param activity
     */
    public void setCurrentVisibleActivity(Activity activity) {
        Log.d(CLS_TAG, "Current visible activity:" + activity.getLocalClassName());
        if (currentVisibleActivity == null) {
            notifyReturningForeground();
        }
        currentVisibleActivity = activity;
    }

    /**
     * Notify the given activity is no longer visible.
     * 
     * This should be called during Activity.onStop(). This is to ensure during activity transition, there is always a current
     * visible activity.
     * 
     * This also clears the reference to the last visible activity, so that the object may be destroyed.
     * 
     * @param activity
     */
    public void clearCurrentVisibleActivity(Activity activity) {
        Log.d(CLS_TAG, "Clear visible activity:" + activity.getLocalClassName());

        if (currentVisibleActivity == activity) {
            currentVisibleActivity = null;
            notifyGoingBackground(); // does not work. switch orientation cause this to happen
        }
    }

    /**
     * Return the current visible activity
     * 
     * @return
     */
    public Activity getCurrentVisibleActivity() {
        return currentVisibleActivity;
    }

    /**
     * Return whether there is a visible activity.
     * 
     * We can use this as a test to see whether the app has entered background.
     * 
     * @return
     */
    public boolean hasVisibleActivity() {
        return currentVisibleActivity != null;
    }

}
