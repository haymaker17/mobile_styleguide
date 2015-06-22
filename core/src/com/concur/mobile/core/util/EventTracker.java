/**
 * Copyright (c) 2014 Concur Technologies, Inc.
 */
package com.concur.mobile.core.util;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;
import java.util.Map;

/**
 * Thin layer for making logging events to Flurry and Google Analytics.
 * <p/>
 * Note that this singleton was implemented as an <code>Enum</code> to guarantee a thread-safe single instance.
 *
 * @author Chris N. Diaz
 */
public enum EventTracker {
    /**
     * The single instance of this <code>EventTracker</code>.
     */
    INSTANCE;

    private static final String USER_ID_TAG = "&uid";
    private static final int CUSTOM_ID_INDEX = 4;

    private Context appContext;
    private String analyticsId;

    @SuppressWarnings("unused")
    private String gaTrackingId; // Google Analytics Tracking ID

    @Retention(value = RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE})
    public @interface EventTrackerClassName {

        String getClassName();
    }

    /**
     * Empty constructor needed by this Enum.
     */
    private EventTracker() {
        // Empty constructor needed by Enum.
    }

    /**
     * Initializes the <code>Flurry</code> and <code>Google Analytics</code> utilities.
     *
     * @param context      the application context required for invoking GA tracking.
     * @param userId       unique user identifier used for tracking.
     * @param gaTrackingId the Google Analytics tracking ID
     */
    public void init(Context context, String userId, String gaTrackingId) {

        this.appContext = context;
        this.analyticsId = userId;

        // Initialize Google Analytics.
        // Note that the Google Analytics
        this.gaTrackingId = gaTrackingId;

        // When dry run is set, hits will not be dispatched, but will still be logged as
        // though they were dispatched.
        boolean dryRun = false; // TODO: this should be in some config file.
        GoogleAnalytics.getInstance(appContext).setDryRun(dryRun);
    }

    /**
     * Sets the unique ID of this client used to track user/sessions.
     *
     * @param userId an ID unique to this user client.
     */
    public void setUserId(String userId) {
        this.analyticsId = userId;
    }

    /**
     * Tracks the given event to Flurry and Google Analytics.
     *
     * @param eventCategory the event category to track
     * @param eventAction   the event action to track
     * @param paramKeys     array of parameter keys (must be same length as <code>paramValues</code>).
     * @param paramValues   array of parameter values with indicies matching the <code>paramKeys</code>.
     */
    public void track(String eventCategory, String eventAction, String[] paramKeys, String[] paramValues) {

        // Convert parameter arrays to Map.
        if (paramKeys == null || paramValues == null || paramKeys.length != paramValues.length) {

            throw new IllegalArgumentException(
                    "Parameter keys and values must not be null and must be the same length!");
        }

        int count = paramKeys.length;
        Map<String, String> params = new HashMap<String, String>(count);
        for (int i = 0; i < count; i++) {
            params.put(paramKeys[i], paramValues[i]);
        }

        this.track(eventCategory, eventAction, params);
    }

    /**
     * Tracks the given event to Flurry and Google Analytics.
     *
     * @param eventCategory the event category to track
     * @param eventAction   the event action to track
     */
    public void track(String eventCategory, String eventAction) {
        track(eventCategory, eventAction, (Map<String, String>) null);
    }

    /**
     * Tracks the given event to Google Analytics.
     *
     * @param eventCategory the event category to track
     * @param eventAction   the event action to track
     * @param eventLabel    the event label to track
     */
    public void track(String eventCategory, String eventAction, String eventLabel) {
        MapBuilder builder = MapBuilder.createEvent(eventCategory, eventAction, eventLabel, null);
        builder.set(USER_ID_TAG, analyticsId);
        builder.set(Fields.customDimension(CUSTOM_ID_INDEX), analyticsId);
        EasyTracker.getInstance(appContext).send(builder.build());
    }

    /**
     * Tracks the given event to Google Analytics.
     *
     * @param eventCategory the event category to track
     * @param eventAction   the event action to track
     * @param value         the event value
     */
    public void track(String eventCategory, String eventAction, String eventLabel, Long value) {
        MapBuilder builder = MapBuilder.createEvent(eventCategory, eventAction, eventLabel, value);
        builder.set(USER_ID_TAG, analyticsId);
        builder.set(Fields.customDimension(CUSTOM_ID_INDEX), analyticsId);
        EasyTracker.getInstance(appContext).send(builder.build());
    }

    /**
     * Tracks the given event to Flurry and Google Analytics.
     *
     * @param eventCategory the event category to track
     * @param eventAction   the event action to track
     * @param parameters    <code>Map</code> of parameters to track.
     */
    public void track(String eventCategory, String eventAction, Map<String, String> parameters) {

        if (parameters != null && !parameters.isEmpty()) {
            // GA can't handle custom map of parameters like Flurry.
            // So instead, we need to send each key-value parameter a single GA Event.
            for (String paramKey : parameters.keySet()) {
                MapBuilder builder = MapBuilder.createEvent(eventCategory, eventAction,
                        Flurry.formatFlurryEvent(paramKey, parameters.get(paramKey)), null);
                builder.set(USER_ID_TAG, analyticsId);
                builder.set(Fields.customDimension(CUSTOM_ID_INDEX), analyticsId);
                EasyTracker.getInstance(appContext).send(builder.build());
            }

        } else {
            MapBuilder builder = MapBuilder.createEvent(eventCategory, eventAction, null, null);
            builder.set(USER_ID_TAG, analyticsId);
            builder.set(Fields.customDimension(CUSTOM_ID_INDEX), analyticsId);
            EasyTracker.getInstance(appContext).send(builder.build());
        }
    }

    private String getAnnotation(Activity activity, String className) {
        Annotation annotation = activity.getClass().getAnnotation(EventTrackerClassName.class);
        if (annotation != null) {
            EventTrackerClassName myAnnotation = (EventTrackerClassName) annotation;
            String annotationClassName = myAnnotation.getClassName();
            className = (!TextUtils.isEmpty(annotationClassName) ? annotationClassName : className);
        }
        return className;
    }

    /**
     * Tracks the given event to Google Analytics after a list of high scores finishes loading
     *
     * @param eventCategory    Event Category
     * @param eventTimingName  Timing Event Name
     * @param eventTimingLabel Timing Event Lable
     * @param timingValue      Timing Event Value
     */
    public void trackTimings(String eventCategory, String eventTimingName, String eventTimingLabel, Long timingValue) {
        MapBuilder builder = MapBuilder.createTiming(eventCategory, timingValue, eventTimingName, eventTimingLabel);
        builder.set(USER_ID_TAG, analyticsId);
        builder.set(Fields.customDimension(CUSTOM_ID_INDEX), analyticsId);
        if (this.analyticsId != null) {
            EasyTracker.getInstance(appContext).set(USER_ID_TAG, analyticsId);
        }
        EasyTracker.getInstance(appContext).send(builder.build());
    }

    /**
     * Used to indicate an <code>Activity</code> is about to start.
     *
     * @param activity The <code>Activity</code> to start tracking.
     */
    public void activityStart(Activity activity) {
        String className = activity.getClass().getName();

        className = getAnnotation(activity, className);
        EasyTracker.getInstance(appContext).set(Fields.SCREEN_NAME, className);

        //assigned user id for user id view.
        if (this.analyticsId != null) {
            EasyTracker.getInstance(appContext).set(USER_ID_TAG, analyticsId);
        }

        EasyTracker.getInstance(appContext).activityStart(activity);
    }
    
    /**
     * Used to indicate an <code>Activity</code> has stopped.
     *
     * @param activity The <code>Activity</code> to stop tracking.
     */
    public void activityStop(Activity activity) {

        String className = activity.getClass().getName();
        className = getAnnotation(activity, className);

        EasyTracker.getInstance(appContext).set(Fields.SCREEN_NAME, className);

        //assigned user id for user id view.
        if (this.analyticsId != null) {
            EasyTracker.getInstance(appContext).set(USER_ID_TAG, analyticsId);
        }
        EasyTracker.getInstance(appContext).activityStop(activity);
    }
}
