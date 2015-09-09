package com.concur.mobile.platform.common.formfield;

import com.concur.mobile.platform.common.Cache;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The cache implementation to handle Forms & Fields
 *
 * @author OlivierB
 */
public class ConnectFormFieldsCache implements Cache<String, ConnectForm> {

    // --- Cache map
    private Map<String, ConnectForm> requestFormFieldsCache = new HashMap<String, ConnectForm>();

    // --- Inherited methods
    @Override public ConnectForm getValue(String k) {
        return requestFormFieldsCache.get(k);
    }

    @Override public Collection<ConnectForm> getValues() {
        return requestFormFieldsCache.values();
    }

    @Override public void addValue(String key, ConnectForm value) {
        requestFormFieldsCache.put(key, value);
    }

    @Override public void removeValue(String key) {
        requestFormFieldsCache.remove(key);
    }

    @Override public boolean hasCachedValues() {
        return requestFormFieldsCache.size() > 0;
    }

    @Override public void clear() {
        requestFormFieldsCache.clear();
    }

    // --- Custom Methods
    public ConnectForm getFormFields(String idForm) {
        return requestFormFieldsCache.get(idForm);
    }

    public void addForms(List<ConnectForm> values) {
        if (values != null) {
            for (ConnectForm form : values) {
                requestFormFieldsCache.put(form.getId(), form != null ? form : new ConnectForm());
            }
        }
    }

}
