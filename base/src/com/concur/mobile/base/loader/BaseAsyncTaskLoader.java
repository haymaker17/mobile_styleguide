package com.concur.mobile.base.loader;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.concur.mobile.base.util.Const;

/**
 * An extension of <code>AsyncTaskLoader<T></code> for the purpose of abstracting the obtain/release of data with regards to the
 * <code>Loader</code> life-cycle.
 * 
 * @author andrewk
 * 
 * @param <T>
 *            contains the type of the data being loaded.
 */
public abstract class BaseAsyncTaskLoader<T> extends AsyncTaskLoader<T> {

    // Contains the class name for a log statement.
    private static final String CLS_TAG = "BaseAsyncTaskLoader<T>";

    // Controls whether debug statements are enabled.
    private static final Boolean DEBUG = true;

    /**
     * Contains a reference to the currently loaded data.
     */
    protected T data;

    /**
     * The {@link Bundle} of data containing request results. This should generally not contain the full request result body. That
     * data should be cached or stored elsewhere before the request completes.
     */
    protected Bundle resultData;

    /**
     * Constructs an instance of <code>BaseAsyncTaskLoader<T></code> given an application context.
     * 
     * @param context
     *            contains an application context.
     */
    public BaseAsyncTaskLoader(Context context) {
        super(context);
    }

    @Override
    public abstract T loadInBackground();

    @Override
    public void deliverResult(T data) {

        if (isReset()) {
            if (DEBUG) {
                Log.w(Const.LOG_TAG, CLS_TAG
                        + ".deliverResult: +++ Warning! An async query came in while the Loader was reset! +++");
            }
            // The Loader has been reset; ignore the result and invalidate the data.
            // This can happen when the Loader is reset while an asynchronous query
            // is working in the background. That is, when the background thread
            // finishes its work and attempts to deliver the results to the client,
            // it will see here that the Loader has been reset and discard any
            // resources associated with the new data as necessary.
            if (data != null) {
                releaseResources(data);
                return;
            }
        }

        // Hold a reference to the old data so it doesn't get garbage collected.
        // We must protect it until the new data has been delivered.
        T oldData = this.data;
        this.data = data;

        if (isStarted()) {
            if (DEBUG) {
                Log.i(Const.LOG_TAG, "+++ Delivering results to the LoaderManager. +++");
            }
            // If the Loader is in a started state, have the superclass deliver the
            // results to the client.
            super.deliverResult(this.data);
        }

        // Invalidate the old data as we don't need it any more.
        if (oldData != null && oldData != this.data) {
            if (DEBUG) {
                Log.i(Const.LOG_TAG, "+++ Releasing any old data associated with this Loader. +++");
            }
            releaseResources(oldData);
        }

    }

    @Override
    protected void onStartLoading() {

        if (DEBUG) {
            Log.i(Const.LOG_TAG, "+++ onStartLoading() called! +++");
        }

        if (data != null) {
            // Deliver any previously loaded data immediately.
            if (DEBUG) {
                Log.i(Const.LOG_TAG, "+++ Delivering previously loaded data to the client...");
            }
            deliverResult(data);
        }

        // Register any data observer.
        registerDataObserver();

        if (takeContentChanged()) {
            // When the observer detects a change in data, it will call
            // onContentChanged() on the Loader, which will cause the next call to
            // takeContentChanged() to return true. If this is ever the case (or if
            // the current data is null), we force a new load.
            if (DEBUG) {
                Log.i(Const.LOG_TAG, "+++ A content change has been detected... so force load! +++");
            }
            forceLoad();
        } else if (this.data == null) {
            // If the current data is null... then we should make it non-null! :)
            if (DEBUG) {
                Log.i(Const.LOG_TAG, "+++ The current data is null... so force load! +++");
            }
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        if (DEBUG) {
            Log.i(Const.LOG_TAG, "+++ onStopLoading() called! +++");
        }
        // The Loader has been put in a stopped state, so we should attempt to
        // cancel the current load (if there is one).
        cancelLoad();

        // Loaders in a stopped state should still monitor the data source
        // for changes so that the Loader will know to force a new load if
        // it is ever started again.
    }

    @Override
    protected void onReset() {

        if (DEBUG) {
            Log.i(Const.LOG_TAG, "+++ onReset() called! +++");
        }

        // Ensure the loader is stopped.
        onStopLoading();

        // At this point we can release the resources associated with 'apps'.
        if (data != null) {
            releaseResources(data);
            data = null;
        }

        // Unregister any data observers.
        unregisterDataObserver();
    }

    @Override
    public void onCanceled(T data) {

        if (DEBUG) {
            Log.i(Const.LOG_TAG, "+++ onCanceled() called! +++");
        }

        // Attempt to cancel the current asynchronous load.
        super.onCanceled(data);

        // The load has been canceled, so we should release the resources
        // associated with 'data'.
        releaseResources(data);
    }

    @Override
    public void forceLoad() {
        if (DEBUG) {
            Log.i(Const.LOG_TAG, "+++ forceLoad() called! +++");
        }
        super.forceLoad();
    }

    /**
     * Releases the data associated with an actively loaded data set.
     * 
     * @param data
     *            contains the data to be released.
     */
    protected abstract void releaseResources(T data);

    /**
     * Will register a data observer for the type of data being loaded.
     */
    protected void registerDataObserver() {
    }

    /**
     * Will unregister a data observer for the type of data being loaded.
     */
    protected void unregisterDataObserver() {
    }

}
