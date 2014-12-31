package com.concur.mobile.platform.common.formfield;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.concur.mobile.platform.common.Cache;

/**
 * The cache implementation to handle Forms & Fields
 * 
 * @author OlivierB
 */
public class ConnectFormFieldsCache implements Cache<String, ConnectForm> {

    // --- Cache map
    private Map<String, ConnectForm> requestFormFieldsCache = new HashMap<String, ConnectForm>();

    // --- Per Form refresh management
    private Set<String> refreshingformsSet = new HashSet<String>();

    // --- Inherited methods
    @Override
    public ConnectForm getValue(String k) {
        return requestFormFieldsCache.get(k);
    }

    @Override
    public Collection<ConnectForm> getValues() {
        return requestFormFieldsCache.values();
    }

    @Override
    public void addValue(String key, ConnectForm value) {
        requestFormFieldsCache.put(key, value);
    }

    @Override
    public void removeValue(String key) {
        requestFormFieldsCache.remove(key);
    }

    @Override
    public boolean hasCachedValues() {
        return requestFormFieldsCache.size() > 0;
    }

    @Override
    public void clear() {
        requestFormFieldsCache.clear();
    }

    public void setFormRefreshStatus(String idForm, boolean status) {
        if (status) {
            refreshingformsSet.add(idForm);
            requestFormFieldsCache.remove(idForm);
        } else
            refreshingformsSet.remove(idForm);
    }

    public boolean isFormBeingRefreshed(String idForm) {
        return refreshingformsSet.contains(idForm);
    }

    // --- Custom Methods
    public ConnectForm getFormFields(String idForm) {
        return requestFormFieldsCache.get(idForm);
    }

    public void addForm(String idForm, ConnectForm values) {
        requestFormFieldsCache.put(idForm, values != null ? values : new ConnectForm());
    }

    public void addField(String idForm, ConnectFormField field) {
        requestFormFieldsCache.get(idForm).add(field);
    }

}
