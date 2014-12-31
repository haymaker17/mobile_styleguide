package com.concur.mobile.platform.service.parser;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * Class for the new MWS Response format coming from REST API like new Hotel API, MarketListing API
 * 
 * @author RatanK
 * 
 * @param <T>
 *            the object encapsulating the elements in the 'data' element of the MWS response
 */
public class MWSResponse<T> {

    @SerializedName("data")
    private T data;

    @SerializedName("errors")
    private List<Error> errors;

    @SerializedName("info")
    private List<String> info;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<Error> getErrors() {
        return errors;
    }

    public void setErrors(List<Error> errors) {
        this.errors = errors;
    }

    public List<String> getInfo() {
        return info;
    }

    public void setInfo(List<String> info) {
        this.info = info;
    }

}