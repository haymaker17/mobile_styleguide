package com.concur.mobile.core.service;

import android.app.Activity;
import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;

/**
 * A custom AsyncTask class automatically saves a reference to itself with the Application when it is executed and will also
 * remove itself from the Application when it completes to prevent a memory leak.
 * 
 * @author RatanK
 * 
 */
// Read more at http://www.fattybeagle.com/2011/02/15/android-asynctasks-during-a-screen-rotation-part-ii/
// may be the functionality from this class can be merged into CoreAsyncRequestTask
public abstract class CustomAsyncRequestTask extends CoreAsyncRequestTask {

    // Application object
    protected ConcurCore concurCoreApp;

    // Activity to which the Async Task is tied to
    protected Activity activity;

    public CustomAsyncRequestTask(Activity activity, Context context, int id, BaseAsyncResultReceiver receiver) {
        super(context, id, receiver);
        this.activity = activity;
        concurCoreApp = (ConcurCore) activity.getApplication();
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
        if (activity == null) {
            onActivityDetached();
        } else {
            onActivityAttached();
        }
    }

    // to be overridden by the concrete Async Task
    protected void onActivityAttached() {
    }

    // to be overridden by the concrete Async Task
    protected void onActivityDetached() {
    }

    @Override
    protected void onPreExecute() {
        concurCoreApp.addTask(activity, this);
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer result) {
        concurCoreApp.removeTask(this);
        super.onPostExecute(result);
    }

    @Override
    protected void onCancelled() {
        concurCoreApp.removeTask(this);
        super.onCancelled();
    }
}
