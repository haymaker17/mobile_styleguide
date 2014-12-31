/**
 * 
 */
package com.concur.mobile.core.travel.data;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.IItineraryInfo;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;

/**
 * An implementation of <code>IItineraryCache</code> for the purposes of retrieving itinerary related information.
 * 
 * @author andy
 */
public class ItineraryCache implements IItineraryCache {

    private static String CLS_TAG = ItineraryCache.class.getSimpleName();

    // Contains the map from itinerary locator values to itinerary info objects.
    private Map<String, IItineraryInfo> itinMap = new HashMap<String, IItineraryInfo>();

    // Contains the itinerary summary list.
    private List<Trip> itinSummaryList;

    // Contains the itinerary summary list update time.
    private Calendar itinSummaryListUpdateTime;

    // Contains the reference to the app.
    private ConcurCore concurMobile;

    // Contains whether the itinerary summary list should be refetched.
    private boolean refetchSummaryList;

    // Contains whether the itinerary summary list should be redisplayed.
    private boolean refreshSummaryList;

    /**
     * Constructs an instance of <code>ItineraryCache</code> with a reference to the application.
     * 
     * @param concurMobile
     *            the app reference.
     */
    public ItineraryCache(ConcurCore concurMobile) {
        this.concurMobile = concurMobile;
        // Init the refetch summary list to true.
        refetchSummaryList = true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerary(long)
     */
    public Trip getItinerary(String itinLocator) {
        Trip itin = null;
        IItineraryInfo itinInfo = itinMap.get(itinLocator);
        if (itinInfo != null) {
            itin = itinInfo.getItinerary();
        } else {
            // Attempt to load from persistence.
            ConcurService concurService = concurMobile.getService();
            if (concurService != null) {
                itinInfo = concurService.getItinerary(itinLocator);
                if (itinInfo != null) {
                    itin = itinInfo.getItinerary();
                    itinMap.put(itinLocator, itinInfo);
                }
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".getItinerary: concur service is unavailable!");
            }
        }
        return itin;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItineraryUpdateTime(long)
     */
    public Calendar getItineraryUpdateTime(String itinLocator) {
        Calendar updateTime = null;
        IItineraryInfo itinInfo = itinMap.get(itinLocator);
        if (itinInfo == null) {
            // Attempt to load from persistence.
            ConcurService concurService = concurMobile.getService();
            if (concurService != null) {
                updateTime = concurService.getItineraryUpdateTime(itinLocator);
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".getItineraryUpdateTime: concur service is unavailable!");
            }
        } else {
            updateTime = itinInfo.getUpdateTime();
        }
        return updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#addItinerary(long, com.concur.mobile.data.IItineraryInfo)
     */
    public void addItinerary(String itinLocator, IItineraryInfo itinInfo) {
        itinMap.put(itinLocator, itinInfo);
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#removeItinerary(long)
     */
    public boolean removeItinerary(String itinLocator) {
        boolean retVal = false;
        if (itinMap.containsKey(itinLocator)) {
            itinMap.remove(itinLocator);
            retVal = true;
        }
        return retVal;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#clearItineraries()
     */
    public void clearItineraries() {
        itinMap.clear();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryByCliqbookTripId(java.lang.String)
     */
    public Trip getItinerarySummaryByCliqbookTripId(String tripId) {
        Trip trip = null;
        if (tripId != null) {
            List<Trip> summaryList = getItinerarySummaryList();
            if (summaryList != null) {
                for (Trip t : summaryList) {
                    if (t.cliqbookTripId != null && t.cliqbookTripId.equalsIgnoreCase(tripId)) {
                        trip = t;
                        break;
                    }
                }
            }
        }
        return trip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryByRecordLocator(java.lang.String)
     */
    public Trip getItinerarySummaryByRecordLocator(String recordLocator) {
        Trip trip = null;
        if (recordLocator != null) {
            List<Trip> summaryList = getItinerarySummaryList();
            if (summaryList != null) {
                for (Trip t : summaryList) {
                    if (t.recordLocator != null && t.recordLocator.equalsIgnoreCase(recordLocator)) {
                        trip = t;
                        break;
                    }
                }
            }
        }
        return trip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryByBookingRecordLocator(java.lang.String)
     */
    public Trip getItinerarySummaryByBookingRecordLocator(String bookingRecordLocator) {
        Trip trip = null;
        if (bookingRecordLocator != null) {
            List<Trip> summaryList = getItinerarySummaryList();
            if (summaryList != null) {
                for (Trip t : summaryList) {
                    if ((t.bookingRecordLocators != null && t.bookingRecordLocators.contains(bookingRecordLocator))
                            || (t.recordLocatorFromXml != null && t.recordLocatorFromXml.contains(bookingRecordLocator))) {
                        trip = t;
                        break;
                    }
                }
            }
        }
        return trip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryByBookingRecordLocator(java.lang.String)
     */
    /*
     * public Trip getItinerarySummaryByBookingRecordLocator(String bookingRecordLocator) { Trip trip = null; if
     * (bookingRecordLocator != null) { List<Trip> summaryList = getItinerarySummaryList(); if (summaryList != null) { for (Trip t
     * : summaryList) { if (t.recordLocatorFromXml != null && t.recordLocatorFromXml.contains(bookingRecordLocator)) { trip = t;
     * break; } } } } return trip; }
     */

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryByClientLocator(java.lang.Long)
     */
    public Trip getItinerarySummaryByClientLocator(String clientLocator) {
        Trip trip = null;
        if (clientLocator != null) {
            List<Trip> summaryList = getItinerarySummaryList();
            if (summaryList != null) {
                for (Trip t : summaryList) {
                    if (t.itinLocator != null && t.itinLocator.equals(clientLocator)) {
                        trip = t;
                        break;
                    }
                }
            }
        }
        return trip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryList()
     */
    public List<Trip> getItinerarySummaryList() {
        if (itinSummaryList == null) {
            ConcurService concurService = concurMobile.getService();
            if (concurService != null) {
                itinSummaryList = concurService.getItinerarySummaryList();
            } else {
                Log.i(Const.LOG_TAG, CLS_TAG + ".getItinerarySummaryList: concur service is unavailable!");
            }
        }
        return itinSummaryList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#setItinerarySummaryList(java.util.List)
     */
    public void setItinerarySummaryList(List<Trip> itinSummaryList) {
        this.itinSummaryList = itinSummaryList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#getItinerarySummaryListUpdateTime()
     */
    public Calendar getItinerarySummaryListUpdateTime() {
        return itinSummaryListUpdateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#setItinerarySummaryListUpdateTime(java.util.Calendar)
     */
    public void setItinerarySummaryListUpdateTime(Calendar updateTime) {
        itinSummaryListUpdateTime = updateTime;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#setShouldRefetchSummaryList(boolean)
     */
    public void setShouldRefetchSummaryList(boolean refetch) {
        refetchSummaryList = refetch;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#shouldRefetchSummaryList()
     */
    public boolean shouldRefetchSummaryList() {
        return refetchSummaryList;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#setShouldRefreshSummaryList(boolean)
     */
    public void setShouldRefreshSummaryList(boolean refresh) {
        refreshSummaryList = refresh;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.data.travel.IItineraryCache#shouldRefreshSummaryList()
     */
    public boolean shouldRefreshSummaryList() {
        return refreshSummaryList;
    }

}
