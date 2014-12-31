package com.concur.mobile.platform.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.concur.mobile.platform.common.Cache;
import com.concur.mobile.platform.request.dto.RequestDTO;

/**
 * The cache implementation to handle the list of requests fetched
 * 
 * @author OlivierB
 */
public class RequestListCache implements Cache<String, RequestDTO> {

    // --- Cache map
    private Map<String, RequestDTO> requestMap = new HashMap<String, RequestDTO>();
    private Boolean beingRefreshed = false;

    // --- Inherited methods
    @Override
    public RequestDTO getValue(String k) {
        return requestMap.get(k);
    }

    @Override
    public Collection<RequestDTO> getValues() {
        return requestMap.values();
    }

    @Override
    public void addValue(String key, RequestDTO value) {
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

    public void setRefreshStatus(boolean isBeingRefreshed) {
        this.beingRefreshed = isBeingRefreshed;
    }

    public boolean isBeingRefreshed() {
        return beingRefreshed;
    }

    // --- Custom Methods
    public void addValue(RequestDTO tr) {
        requestMap.put(tr.getId(), tr);
    }

    public void addValues(List<RequestDTO> list) {
        for (RequestDTO tr : list) {
            requestMap.put(tr.getId(), tr);
        }
    }
}
