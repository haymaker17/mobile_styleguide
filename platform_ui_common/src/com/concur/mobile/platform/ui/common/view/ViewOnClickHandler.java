package com.concur.mobile.platform.ui.common.view;

import java.util.HashMap;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.util.Log;
import android.view.View;

import com.concur.mobile.platform.ui.common.util.Const;
import com.concur.mobile.platform.ui.common.util.IEventLogger;

/**
 * Provides an implementation of an <code>View.OnClickListener</code> to handle launching intents for various view elements.
 * 
 * @author AndrewK
 */
public class ViewOnClickHandler implements View.OnClickListener {

    private static final String CLS_TAG = ViewOnClickHandler.class.getSimpleName();

    // A map for quick look-up of what intent should be launched when a view
    // has been clicked on.
    private HashMap<View, ActivityLaunchInfo> viewIntentMap = new HashMap<View, ActivityLaunchInfo>();

    // A reference to an activity that will be used to launch other activities.
    private Activity activity;

    /**
     * Constructs an instance of <code>ViewOnClickHandler</code> with an activity used to launch other intents.
     * 
     * @param activity
     *            the activity used to launch other intents.
     */
    public ViewOnClickHandler(Activity activity) {
        this.activity = activity;
    }

    /**
     * Adds a new intent to be started with an activity when clicked on.
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch.
     * @param viewOnClickCheck
     *            a reference to a checker to determine whether the activity should be launched.
     * @param flurryEvent
     *            contains an optional flurry event that will be generated.
     * @param flurryParams
     *            contains an option set of flurry parameters.
     */
    public void addViewLauncher(View view, Intent intent, IEventLogger eventLogger, IViewOnClickCheck viewOnClickCheck) {
        ActivityLaunchInfo actLaunchInfo = new ActivityLaunchInfo();
        actLaunchInfo.intent = intent;
        actLaunchInfo.viewOnClickCheck = viewOnClickCheck;
        actLaunchInfo.eventLogger = eventLogger;
        viewIntentMap.put(view, actLaunchInfo);
    }

    /**
     * Adds a new intent to be started with an activity when clicked on.
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch.
     */
    public void addViewLauncher(View view, Intent intent) {
        addViewLauncher(view, intent, null, null);
    }

    /**
     * Adds a new intent to be started with an activity when clicked on.
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch.
     * @param flurryEvent
     *            the Flurry event name to be logged
     * @param flurryParams
     *            contains an optional set of flurry parameters.
     */
    public void addViewLauncher(View view, Intent intent, IEventLogger eventLogger) {
        addViewLauncher(view, intent, eventLogger, null);
    }

    /**
     * Adds a new intent describing an activity to be started "for result".
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch "for result".
     * @param requestCode
     *            the request code associated with the activity.
     */
    public void addViewLauncherForResult(View view, Intent intent, int requestCode) {
        addViewLauncherForResult(view, intent, requestCode, null, null);
    }

    /**
     * Adds a new intent describing an activity to be started "for result".
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch "for result".
     * @param requestCode
     *            the request code associated with the activity.
     * @param flurryEvent
     *            the Flurry event name to be logged
     * @param flurryParams
     *            contains an optional set of flurry parameters.
     */
    public void addViewLauncherForResult(View view, Intent intent, int requestCode, IEventLogger eventLogger) {
        addViewLauncherForResult(view, intent, requestCode, eventLogger, null);
    }

    /**
     * Adds a new intent describing an activity to be started "for result".
     * 
     * @param view
     *            the view to be clicked on.
     * @param intent
     *            the intent describing the activity to launch "for result".
     * @param requestCode
     *            the request code associated with the activity.
     * @param flurryEvent
     *            contains an optional flurry event that will be generated.
     * @param flurryParams
     *            contains an option set of flurry parameters.
     * @param viewOnClickCheck
     *            a reference to a checker to determine whether the activity should be launched.
     */
    public void addViewLauncherForResult(View view, Intent intent, int requestCode, IEventLogger eventLogger,
            IViewOnClickCheck viewOnClickCheck) {
        ActivityLaunchInfo actLaunchInfo = new ActivityLaunchInfo();
        actLaunchInfo.intent = intent;
        actLaunchInfo.withResult = true;
        actLaunchInfo.requestCode = requestCode;
        actLaunchInfo.viewOnClickCheck = viewOnClickCheck;
        actLaunchInfo.eventLogger = eventLogger;
        viewIntentMap.put(view, actLaunchInfo);
    }

    /**
     * Return the intent that will be launched for a given view. Allows updating the intent after the view has been created.
     * 
     * @param view
     * @return
     */
    public Intent getIntentForView(View view) {
        ActivityLaunchInfo li = viewIntentMap.get(view);
        return li.intent;
    }

    /**
     * Will clear out all view/launcher information.
     */
    public void clear() {
        if (!viewIntentMap.isEmpty()) {
            viewIntentMap.clear();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.view.View.OnClickListener#onClick(android.view.View)
     */
    public void onClick(View view) {

        ActivityLaunchInfo launchInfo = viewIntentMap.get(view);
        if (launchInfo != null) {
            if (launchInfo.intent != null) {
                try {
                    if (launchInfo.viewOnClickCheck == null || launchInfo.viewOnClickCheck.onClickCheck()) {
                        if (!launchInfo.withResult) {
                            activity.startActivity(launchInfo.intent);
                        } else {
                            activity.startActivityForResult(launchInfo.intent, launchInfo.requestCode);
                        }
                    }
                } catch (ActivityNotFoundException actNotFndExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: ", actNotFndExc);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: null intent registered for view: " + view.getId() + ".");
            }

            // Log the event
            if (launchInfo.eventLogger != null) {
                launchInfo.eventLogger.logEvent(activity);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onClick: no intent registered for view: " + view.getId() + ".");
        }
    }
}

/**
 * Models activity launch information.
 * 
 * @author AndrewK
 */
class ActivityLaunchInfo {

    /**
     * Contains a reference to the intent describing the activity to be launched.
     */
    Intent intent;

    /**
     * Contains whether the activity should be launched "with result".
     */
    boolean withResult;

    /**
     * Contains the request code used to launch an activity "with result".
     */
    int requestCode;

    /**
     * Contains a reference to a view on click check to determine whether the intent associated with a view should be launched.
     */
    IViewOnClickCheck viewOnClickCheck;

    /**
     * Instead of holding on to flurry event/params, we call event logger to handle the details.
     */
    IEventLogger eventLogger;
}
