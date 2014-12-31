package com.concur.mobile.core.fragment;

import java.util.HashMap;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * @deprecated - use {@link com.concur.platform.ui.common.fragment.RetainerFragment} instead.
 */
public class RetainerFragment extends Fragment {

    HashMap<String, Object> retainedObjects;

    public RetainerFragment() {
    }

    /**
     * This is called when the Fragment's Activity is ready to go, after its content view has been installed; it is called both
     * after the initial fragment creation and after the fragment is re-attached to a new activity.
     */
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize our container
        retainedObjects = new HashMap<String, Object>();

        // Tell the framework to try to keep this fragment around
        // during a configuration change.
        setRetainInstance(true);

    }

    /**
     * This is called when the fragment is going away. It is NOT called when the fragment is being propagated between activity
     * instances.
     */
    @Override
    public void onDestroy() {
        // Be paranoid and dump the map
        retainedObjects.clear();

        super.onDestroy();
    }

    /**
     * This is called right before the fragment is detached from its current activity instance.
     */
    @Override
    public void onDetach() {
        super.onDetach();
    }

    public boolean contains(String key) {
        if (retainedObjects != null) {
            return retainedObjects.containsKey(key);
        }

        return false;
    }

    public void put(String key, Object obj) {
        if (retainedObjects != null) {
            retainedObjects.put(key, obj);
        } else {
            throw new UnsupportedOperationException("Fragment not yet created");
        }
    }

    /**
     * Return a previously stored object. This is a one-shot get and the object is removed from the retainer. The retainer is not
     * meant to be used for long-term holding of objects or multiple fetches.
     * 
     * @param key
     * @return The Object or null if it does not exist in the retainer.
     */
    public Object get(String key) {
        if (retainedObjects != null) {
            return retainedObjects.remove(key);
        }

        return null;
    }
}
