package com.concur.mobile.platform.travel.search.hotel;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An implementation of <code>Serializable</code> holding the state of a hotel search result.
 */
public class HotelSearchResult implements Serializable {

    /**
     * Contains the serialization version ID.
     */
    private static final long serialVersionUID = 5932990394308548894L;

    public ArrayList<HotelChoice> hotelChoices;

    /**
     * Contains the start index of this result set into the entire list of cached results on the server.
     */
    public Integer startIndex;

    /**
     * Contains the total count of cached results on the server.
     */
    public Integer totalCount;

    /**
     * Contains the number of results returned in this reply.
     */
    public Integer length;

    /**
     * hotel list has a recommendation - to be used to identify the default sorting of hotel search list
     */
    public boolean hasRecommendation;

    /**
     * Contains the hotel search polling id.
     */
    public String pollingId;

    /**
     * Contains whether or not this set of results constitutes the final result set.
     */
    public boolean isFinal;

}
