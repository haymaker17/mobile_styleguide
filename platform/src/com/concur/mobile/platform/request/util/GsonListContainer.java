package com.concur.mobile.platform.request.util;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by OlivierB on 20/01/2015.
 */
public class GsonListContainer<T> {

    @SerializedName("Items")
    private List<T> list;

    public List<T> getList() {
        return list != null ? list : new ArrayList<T>();
    }
}
