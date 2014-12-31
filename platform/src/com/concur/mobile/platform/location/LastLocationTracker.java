package com.concur.mobile.platform.location;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.base.util.DataConnectivityManager;
import com.concur.mobile.base.util.DataConnectivityManager.ConnectivityListener;
import com.concur.mobile.platform.base.VisibleActivityStateCallbacks;
import com.concur.mobile.platform.base.VisibleActivityStateTracker;
import com.concur.mobile.platform.util.Const;

/**
 * Utility class for finding the user's last known location.
 * 
 * The caller can choose to continuously tracking the last location, or just do one-time lookup.
 * 
 * This class Check service before initiate location look up.
 * 
 * LastLocationTracker listens on App foreground and app background events and suspend GPS and resume GPS accordingly. [When app
 * goes to background (!BaseActivity.hasVisibleActivity()), need to call stopLocationTrack() to remove location listeners.]
 * 
 * Usage: Activity using location service should call the following APIs:
 * 
 * Call startLocationTrace(..) with a unique requestorID in onResume();
 * 
 * Call stopLocationTrace(..) with the same unique requestorID in onStop();
 * 
 * @author yiwenw
 */
public class LastLocationTracker implements VisibleActivityStateCallbacks {

    private static final String CLS_TAG = LastLocationTracker.class.getSimpleName();

    protected LocationListener locListener;
    protected LocationManager locManager;

    // Overall callback
    protected LocationLookupCallbacks locUpdateCallback;

    protected Criteria locCriteria;
    protected boolean oneTimeOnly;

    protected Context context;

    protected LocationListener locUpdateListener;
    protected LocationListener lpBestInactiveListener;

    // Use a TimerTask to remove listener if no location update after 5 min
    protected final int MAX_WAIT_TIME = 5 * 60 * 1000; // Default to 5 minutes
    protected TimerTask currentTimerTask;
    protected Timer timer;

    protected Location lastKnownLocation;

    protected DataConnectivityManager dcm;

    protected Address currentAddress;

    static protected class RequestorRecord {

        public String requestorID;
        public LocationLookupCallbacks callback;
        public boolean oneTimeOnly;
        public long minTime;
        public float minDistance;
        public int accuracy;
        public long lastRequestTime;  // last time startTrack() or getLastKnowLoc() is called by with request id
    }

    protected Map<String, RequestorRecord> requestorMap;

    public interface LocationLookupCallbacks {

        public void setCurrentLocation(Location loc);
    }

    /**
     * Construct a new Last Location Tracker.
     * 
     * @param context
     *            Context
     */
    public LastLocationTracker(Context context, LocationLookupCallbacks callback) {
        locManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locCriteria = new Criteria();
        locUpdateCallback = callback;

        // Coarse accuracy is specified here to get the fastest possible result.
        // The calling Activity will likely (or have already) request ongoing // updates using the Fine location provider.
        locCriteria.setAccuracy(Criteria.ACCURACY_COARSE);
        this.context = context;
        requestorMap = new HashMap<String, RequestorRecord>();

        oneTimeOnly = true;
        currentTimerTask = null;
        timer = new Timer();

        VisibleActivityStateTracker.INSTANCE.registerSubscriber(this);
    }

    private RequestorRecord getRequestorRecord(String requestorID, LocationLookupCallbacks callback,
            boolean oneTimeOnly, long minTime, float minDistance) {
        RequestorRecord result = requestorMap.get(requestorID);
        if (result == null) {
            result = new RequestorRecord();
            result.requestorID = requestorID;
            result.oneTimeOnly = oneTimeOnly;
            result.callback = callback;
            requestorMap.put(requestorID, result);
        }

        result.minTime = minTime;
        result.minDistance = minDistance;
        result.accuracy = Criteria.ACCURACY_COARSE;
        // Define the accuracy based on minTime & minDistance
        if (result.minTime < Const.LOC_UPDATE_MIN_TIME && result.minDistance < Const.LOC_UPDATE_MIN_DISTANCE)
            result.accuracy = Criteria.ACCURACY_FINE;

        return result;
    }

    /**
     * Start location trace with the given criteria for a given requestor
     * 
     * @param requestorID
     *            unique id identifies the entity requesting the trace
     * @param callback
     *            callback if current location is not available at the time. If null, the callback from constructor is still
     *            updated when current location is found.
     * @param oneTimeOnly
     *            whether one location lookup or continuous tracking is necessary. If true, the location trace request will expire
     *            after the request is fulfilled, and there is no need to call stopLocationTrace();
     * @param minTime
     *            location accuracy in time
     * @param minDistance
     *            location accuracy in distance
     */
    public void startLocationTrace(String requestorID, LocationLookupCallbacks callback, boolean oneTimeOnly,
            long minTime, float minDistance) {

        Log.d(CLS_TAG, "startLocationTrace requested from " + requestorID);

        // We have to calculate the earliest acceptable GPS lookup time in the beginning, so that the same number is used in
        // lookup and acceptance criteria
        long minLastKnownTime = System.currentTimeMillis() - minTime;

        // Get new last know location, if none exists or the existing one is too old
        if (this.lastKnownLocation == null || lastKnownLocation.getTime() < minLastKnownTime) {
            Location bestLoc = getLastBestLocation(minDistance, minLastKnownTime);
            if (this.lastKnownLocation == null
                    || (bestLoc != null && bestLoc.getTime() > this.lastKnownLocation.getTime())) {
                // This will update this.lastKnownLocation
                setCurrentLocation(bestLoc);
            }
        }

        // Check whether need to get more accurate location
        boolean needToIncreaseAccuracy = false;

        // If the best result is beyond the allowed time limit, or the accuracy of the
        // best result is wider than the acceptable maximum distance, request a single update.
        // This check simply implements the same conditions we set when requesting regular
        // location updates every [minTime] and [minDistance].
        // Prior to Gingerbread "one-shot" updates weren't available, so we need to implement
        // this manually.
        if ((lastKnownLocation == null || lastKnownLocation.getTime() < minLastKnownTime || lastKnownLocation
                .getAccuracy() > minDistance)) {
            // If last known location is not satisfactory, start a new location trace request.
            // If location listening is already in progress for the given accuracy, no action taken
            if (this.locListener == null)
                needToIncreaseAccuracy = true;

            Log.d(CLS_TAG, "startLocationTrace - current best location is not sufficient "
                    + (this.lastKnownLocation == null ? "none" : this.lastKnownLocation.toString()));

        } else {
            Log.d(CLS_TAG,
                    "startLocationTrace - current best location is accepted : " + this.lastKnownLocation.toString());
            // No need for further processing, if the current know location is good enough for this one time request
            if (oneTimeOnly)
                return;
        }

        int accuracy = Criteria.ACCURACY_COARSE; // Default to coarse accuracy for now
        if (accuracy == Criteria.ACCURACY_FINE && this.locCriteria.getAccuracy() == Criteria.ACCURACY_COARSE) {
            this.locCriteria = new Criteria();
            locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
            needToIncreaseAccuracy = true;
        }

        RequestorRecord req = getRequestorRecord(requestorID, callback, oneTimeOnly, minTime, minDistance);
        req.accuracy = accuracy;

        req.lastRequestTime = System.currentTimeMillis();
        // Check need to increase accuracy or frequency
        if (!req.oneTimeOnly && this.oneTimeOnly) {
            this.oneTimeOnly = false;
        }

        // We need to re-start listeners
        if (locListener == null || needToIncreaseAccuracy) {
            startLocationUpdates();
        }
    }

    /**
     * If we have any recurring location update request, return false.
     * 
     * @return
     */
    private boolean calculateOneTimeOnly() {
        Iterator<RequestorRecord> iter = requestorMap.values().iterator();
        while (iter.hasNext()) {
            RequestorRecord rec = iter.next();
            if (!rec.oneTimeOnly)
                return false;
        }
        return true;
    }

    private int calculateAccuracy() {
        Iterator<RequestorRecord> iter = requestorMap.values().iterator();
        while (iter.hasNext()) {
            RequestorRecord rec = iter.next();
            if (rec.accuracy == Criteria.ACCURACY_FINE)
                return Criteria.ACCURACY_FINE;
        }
        return Criteria.ACCURACY_COARSE;
    }

    private void clearListeners() {
        Log.d(CLS_TAG, "clearListeners");

        if (locListener != null) {
            // remove existing listener
            locManager.removeUpdates(locListener);
            locListener = null;
        }

        if (lpBestInactiveListener != null) {
            locManager.removeUpdates(lpBestInactiveListener);
            lpBestInactiveListener = null;
        }

        if (currentTimerTask != null) {
            currentTimerTask.cancel();
            currentTimerTask = null;
        }
    }

    /**
     * Stop location trace from the given requestor
     * 
     * @param requestorID
     *            unique id identifies the entity requesting the trace
     */
    public void stopLocationTrace(String requestorID) {
        Log.d(CLS_TAG, "stopLocationTrace requested from " + requestorID);

        RequestorRecord req = requestorMap.remove(requestorID);
        if (req != null && requestorMap.size() > 0) {
            if (!req.oneTimeOnly && !oneTimeOnly)
                oneTimeOnly = calculateOneTimeOnly();

            if (req.accuracy == Criteria.ACCURACY_FINE && this.locCriteria.getAccuracy() == req.accuracy) {
                int newAccuracy = calculateAccuracy();
                if (newAccuracy != this.locCriteria.getAccuracy()) {
                    this.locCriteria = new Criteria();
                    locCriteria.setAccuracy(newAccuracy);

                    startLocationUpdates();
                }
            }
        } else if (requestorMap.size() == 0 && locListener != null) {
            clearListeners();
        }
    }

    private void notifyLocationChange() {
        List<String> oneTimerToRemove = new ArrayList<String>();
        for (RequestorRecord rec : requestorMap.values()) {
            if (rec.callback != null) {
                rec.callback.setCurrentLocation(this.lastKnownLocation);
            }
            if (rec.oneTimeOnly)
                oneTimerToRemove.add(rec.requestorID);
        }

        if (oneTimerToRemove.size() > 0) {
            for (int ix = 0; ix < oneTimerToRemove.size(); ix++) {
                requestorMap.remove(oneTimerToRemove.get(ix));
            }

            if (requestorMap.size() <= 0) {
                // Stop GPS altogether by removing existing listener
                clearListeners();
            } else {
                // Recalculate accuracy, and if necessary restart loc listener
                int newAccu = this.calculateAccuracy();
                if (newAccu != this.locCriteria.getAccuracy()) {
                    this.locCriteria = new Criteria();
                    locCriteria.setAccuracy(Criteria.ACCURACY_FINE);
                    this.startLocationUpdates();
                }
            }

        }
    }

    /**
     * Restart location listening, if no one is listening and there is location request
     */
    public void onReturningToVisibility() {
        if (locListener == null && this.requestorMap.size() > 0) {
            startLocationUpdates();
        }
    }

    public void onGoingToInvisibility() {
        clearListeners();
    }

    private boolean isConnected() {
        // If we still have context then proceed
        if (context != null) {
            // Grab a DCM to monitor connectivity
            ConnectivityListener connectivityListener = new ConnectivityListener() {

                public void connectionLost(int connectionType) {
                    // Do nothing
                }

                public void connectionEstablished(int connectionType) {
                    // Do nothing
                }
            };
            dcm = DataConnectivityManager.getInstance(context, connectivityListener);
        } else {
            return false;
        }

        // Only proceed if connected
        return dcm.isConnected();
    }

    /**
     * Start location update with the current criteria. Make sure older listeners are removed first, since the older criteria no
     * longer apply.
     * 
     * If the best provider is not available, use a listener to see when it comes around. When it does, restart location update.
     * 
     */
    private void startLocationUpdates() {

        // remove existing listener
        clearListeners();

        if (!isConnected()) {
            return;
        }

        Log.d(CLS_TAG, "startLocationUpdates");

        // Create locListener, upon provider disable event, restart location update
        locListener = new LocationListener() {

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
            }

            public void onProviderDisabled(String provider) {
                startLocationUpdates();
            }

            public void onLocationChanged(Location location) {
                Log.d(CLS_TAG, "LocationListener - onLocationChanged");
                setCurrentLocation(location);
                if (oneTimeOnly) {
                    // Finished location update and stop GPS tracking
                    clearListeners();
                }

                notifyLocationChange();

            }
        };

        String provider = locManager.getBestProvider(locCriteria, true);
        if (provider != null) {
            locManager.requestLocationUpdates(provider, Const.LOC_UPDATE_MIN_DISTANCE, Const.LOC_UPDATE_MIN_TIME,
                    locListener, context.getMainLooper());

            if (this.oneTimeOnly) {
                // Set up a timer task to remove the listeners if more than 5 minutes have past
                currentTimerTask = new TimerTask() {

                    @Override
                    public void run() {
                        Log.d(CLS_TAG, "TimerTask.run to stop location listening");

                        clearListeners();
                    }
                };

                timer.schedule(currentTimerTask, MAX_WAIT_TIME);
            }
        }
        // Create the listener for activation of the best LP. Will just loop
        // back into startLocationUpdates to
        // request updates from the then current best provider
        lpBestInactiveListener = new LocationListener() {

            public void onStatusChanged(String provider, int status, Bundle extras) {
            }

            public void onProviderEnabled(String provider) {
                locManager.removeUpdates(lpBestInactiveListener);
                lpBestInactiveListener = null;
                startLocationUpdates();
            }

            public void onProviderDisabled(String provider) {
            }

            public void onLocationChanged(Location location) {
            }
        };

        // Listen for when a better receiver comes around
        String bestProvider = locManager.getBestProvider(locCriteria, false);
        if (bestProvider != null && !bestProvider.equals(provider)) {
            locManager.requestLocationUpdates(bestProvider, 0, 0, lpBestInactiveListener, context.getMainLooper());
        }
    }

    /**
     * Replace ConcurCore.getCurrentAddress()
     * 
     * @return
     */
    public Address getCurrentAddress() {
        return currentAddress;
    }

    /**
     * Replace ConcurCore.getCurrentLocaton()
     * 
     * @return current location
     */
    public Location getCurrentLocaton() {
        return lastKnownLocation;
    }

    /**
     * Retrieve current location, and keep live the current recurring location request
     * 
     * @param requestorID
     * @return
     */
    public Location getCurrentLocation(String requestorID) {
        RequestorRecord req = this.requestorMap.get(requestorID);
        if (req != null) {
            if (req.oneTimeOnly) {
                this.stopLocationTrace(requestorID);
            } else
                req.lastRequestTime = System.currentTimeMillis();
        }
        return lastKnownLocation;
    }

    public synchronized void setCurrentLocation(Location loc) {

        // Set the location and kick off a task to find the address
        lastKnownLocation = loc;

        currentAddress = null;
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {

                // Call the overall call back to set location
                if (locUpdateCallback != null)
                    locUpdateCallback.setCurrentLocation(getCurrentLocaton());

                // TODO - restart geo lookup, if failed the first time
                // Only do all this if we are connected. If we are not connected
                // then the
                // geocoder service won't work and will give us nothing anyway.
                if (isConnected()) {
                    Location loc = lastKnownLocation;
                    if (loc != null) {
                        Geocoder geo = new Geocoder(context);
                        List<Address> addrs = null;
                        try {
                            addrs = geo.getFromLocation(loc.getLatitude(), loc.getLongitude(), 3);
                        } catch (IOException e) {
                            Log.e(Const.LOG_TAG, "Error determing location: " + e.getMessage(), e);
                        }

                        // This isn't an off-by-1 error. The geocoder can return
                        // data so specific
                        // that the locality is too specific (a sub-unit within
                        // the city). We do
                        // this to step out a level and hopefully hit the city
                        // name.
                        if (addrs != null && addrs.size() > 1) {
                            Address addr = addrs.get(1);

                            // However, sometimes the first address is the one
                            // we want...
                            if (addr.getLocality() == null) {
                                addr = addrs.get(0);
                            }

                            currentAddress = addr;
                        }
                    }
                }

                return null;
            }

        }.execute();

    }

    /**
     * Returns the most accurate and timely previously detected location. Where the last result is beyond the specified maximum
     * distance or latency a one-off location update is returned via the {@link LocationListener} specified in
     * {@link setChangedLocationListener}.
     * 
     * @param minDistance
     *            Minimum distance before we require a location update.
     * @param earliestLookupTime
     *            earliest acceptable GPS lookup time.
     * @return The most accurate and / or timely previously detected location.
     */
    public Location getLastBestLocation(float minDistance, long earliestLookupTime) {
        Location bestResult = null;
        float bestAccuracy = Float.MAX_VALUE;
        long bestTime = Long.MIN_VALUE;

        // cutOffTime is the earliest time value we accept
        long cutOffTime = earliestLookupTime;
        Log.d(CLS_TAG, "getLastBestLocation cutOffTime - " + cutOffTime + " minDistance " + minDistance);

        // Iterate through all the providers on the system, keeping
        // note of the most accurate result within the acceptable time limit.
        // If no result is found within maxTime, return the newest Location.
        List<String> matchingProviders = locManager.getAllProviders();
        for (String provider : matchingProviders) {
            Location location = locManager.getLastKnownLocation(provider);

            if (location != null) {
                Log.d(CLS_TAG,
                        "getLastBestLocation - location: " + location.toString() + " time: " + location.getTime()
                                + " provider: " + provider);

                float accuracy = location.getAccuracy();
                long time = location.getTime();

                if ((time > cutOffTime && time > bestTime && accuracy < bestAccuracy)) {
                    // Our new best location
                    bestResult = location;
                    bestAccuracy = accuracy;
                    bestTime = time;
                } else if (bestResult == null) {
                    // We don't have a location yet, just use the first one come along
                    bestResult = location;
                    bestTime = time;
                    bestAccuracy = accuracy;
                }
            }
        }

        return bestResult;
    }

}
