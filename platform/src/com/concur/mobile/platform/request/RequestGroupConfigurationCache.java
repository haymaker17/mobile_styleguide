package com.concur.mobile.platform.request;

import com.concur.mobile.platform.common.Cache;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfiguration;
import com.concur.mobile.platform.request.groupConfiguration.RequestGroupConfigurationsContainer;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by OlivierB on 19/01/2015.
 */
public class RequestGroupConfigurationCache implements Cache<String, RequestGroupConfiguration> {

    // --- Cache map
    private Map<String, RequestGroupConfiguration> requestMap = new HashMap<String, RequestGroupConfiguration>();

    // --- Inherited methods
    @Override
    public RequestGroupConfiguration getValue(String k) {
        return requestMap.get(k);
    }

    @Override
    public Collection<RequestGroupConfiguration> getValues() {
        return requestMap.values();
    }

    @Override
    public void addValue(String key, RequestGroupConfiguration value) {
        requestMap.put(key, value);
    }

    @Override
    public void removeValue(String key) {
        requestMap.remove(key);
    }

    @Override
    public boolean hasCachedValues() {
        return !requestMap.isEmpty();
    }

    @Override
    public void clear() {
        requestMap.clear();
    }

}
