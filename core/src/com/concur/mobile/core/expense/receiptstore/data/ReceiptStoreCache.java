/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.data;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.service.ConcurService;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.util.ViewUtil;

/**
 * Models a cache to retrieve receipt store information.
 */
public class ReceiptStoreCache {

    private static final String CLS_TAG = ReceiptStoreCache.class.getSimpleName();

    // Contains the name of the directory within the private application files external directory
    // where both receipt full-size images and thumbnails will be placed.
    private static final String RECEIPT_STORE_DIRECTORY = "rs";

    // Contains the list of receipt info objects.
    private List<ReceiptInfo> receiptInfos;

    // Contains the last update time of the list.
    private Calendar updateTime;

    // Contains the application reference.
    private ConcurCore concurMobile;

    // Contains whether the receipt list should be refetched.
    private boolean refetchReceiptList;

    // Contains whether the receipt list should be refreshed.
    private boolean refreshReceiptList;

    // Contains the map from receipt image id's to receipt info objects.
    private HashMap<String, ReceiptInfo> receiptImageIdInfoMap = new HashMap<String, ReceiptInfo>();

    // Contains the external receipt store directory.
    private File externalCacheDirectory;

    /**
     * Constructs an instance of <code>ReceiptStoreCache</code>.
     * 
     * @param concurMobile
     *            the application object.
     */
    public ReceiptStoreCache(ConcurCore concurMobile) {
        this.concurMobile = concurMobile;
        File externalFilesDir = ViewUtil.getExternalFilesDir(concurMobile);
        if (externalFilesDir != null) {
            externalCacheDirectory = new File(externalFilesDir, RECEIPT_STORE_DIRECTORY);
            try {
                if (!externalCacheDirectory.exists()) {
                    if (!externalCacheDirectory.mkdirs()) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to initialize receipt store cache directory '"
                                + externalCacheDirectory.getAbsolutePath() + "'.");
                        externalCacheDirectory = null;
                    }
                }
            } catch (Exception exc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: exception initializing receipt store cache directory '"
                        + externalCacheDirectory.getAbsolutePath() + "'.", exc);
                externalCacheDirectory = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".<init>: unable to initialize receipt store cache directory!");
        }
    }

    /**
     * Gets the current list of receipt info objects.
     * 
     * @return the current list of receipt info objects.
     */
    public List<ReceiptInfo> getReceiptInfoList() {
        if (receiptInfos == null) {
            loadReceiptListFromPersistence();
        }
        return receiptInfos;
    }

    /**
     * Sets the current list of receipt store receipts objects.
     * 
     * @param receiptInfos
     *            the list of receipt store receipt objects.
     * @param updateTime
     *            the update time of the list.
     */
    public void setReceiptInfoList(List<ReceiptInfo> receiptInfos, Calendar updateTime) {
        removedUnreferencedThumbnailImages(this.receiptInfos, receiptInfos);
        this.receiptInfos = receiptInfos;
        this.updateTime = updateTime;
        removeUnreferencedReceiptImages();
    }

    /**
     * Gets the time at which the last receipt list was received.
     * 
     * @return the time at which the last receipt list was received.
     */
    public Calendar getLastReceiptInfoListUpdateTime() {
        return updateTime;
    }

    // Will load from presistence the receipt info objects.
    private void loadReceiptListFromPersistence() {
        ConcurService concurService = concurMobile.getService();
        if (concurService != null) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(concurMobile
                    .getApplicationContext());
            String userId = prefs.getString(Const.PREF_USER_ID, null);
            receiptInfos = concurService.getReceiptInfos(userId);
            if (receiptInfos != null && receiptInfos.size() > 0) {
                // All receipt info objects actually have the same update time, so we'll just use the first update
                // time as the update time for the entire list.
                updateTime = receiptInfos.get(0).getUpdateTime();
            } else {
                updateTime = null;
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".loadReceiptListFromPersistence: concur service unavailable.");
        }
    }

    /**
     * Will remove any unreferenced thumbnail image files.
     * 
     * @param oldInfos
     *            contains the list of old <code>ReceiptInfo</code> objects.
     * @param newInfos
     *            contains the list of new <code>ReceiptInfo</code> objects.
     */
    private void removedUnreferencedThumbnailImages(List<ReceiptInfo> oldInfos, List<ReceiptInfo> newInfos) {
        if (oldInfos != null && oldInfos.size() > 0) {
            // First, construct a map that will be used to look-up
            Map<String, ReceiptInfo> newInfoMap = new HashMap<String, ReceiptInfo>();
            if (newInfos != null) {
                for (ReceiptInfo rcptInfo : newInfos) {
                    newInfoMap.put(rcptInfo.getReceiptImageId(), rcptInfo);
                }
            }
            // Second, iterate through the list of old receipt info items and attempt to locate a match
            // by receipt image ID in the newly created map from above.
            for (ReceiptInfo rcptInfo : oldInfos) {
                if (rcptInfo.getReceiptImageId() != null && !newInfoMap.containsKey(rcptInfo.getReceiptImageId())) {
                    // New map does not contain old item, so if old item has thumbnail URI, then punt it
                    // from the ImageCache.
                    deleteReceiptThumbnail(rcptInfo);
                }
            }
        }
    }

    /**
     * Deletes any thumbnail image for a <code>ReceiptInfo</code> object that has been deleted.
     * 
     * @param rcptInfo
     *            contains the <code>ReceiptInfo</code> object for which a referenced thumbnail should be deleted.
     */
    private void deleteReceiptThumbnail(ReceiptInfo rcptInfo) {
        ImageCache imgCache = ImageCache.getInstance(concurMobile);
        if (rcptInfo.getThumbUrl() != null) {
            try {
                URI thumbUri = new URL(rcptInfo.getThumbUrl()).toURI();
                imgCache.deleteBitmap(thumbUri);
            } catch (MalformedURLException mlfUrlExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".deleteReceiptThumbnail: malformed receipt thumbnail URL '" + rcptInfo.getThumbUrl()
                                + "'", mlfUrlExc);
            } catch (URISyntaxException uriSynExc) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptThumbnail: URI syntax exception for thumbnail URL '"
                        + rcptInfo.getThumbUrl() + "'", uriSynExc);
            }
        }
    }

    /**
     * Will remove any receipt images (both full-sized and thumbnail) from the external storage directory if they are not
     * referenced by the current receipt store receipt list.
     */
    private void removeUnreferencedReceiptImages() {
        if (externalCacheDirectory != null) {
            // First, populate a map that will be used as a look-up.
            receiptImageIdInfoMap.clear();
            if (receiptInfos != null) {
                for (ReceiptInfo rcptInfo : receiptInfos) {
                    receiptImageIdInfoMap.put(rcptInfo.getReceiptImageId(), rcptInfo);
                }
            }
            // Second, iterate over the files in the directory and determine if they are
            // referenced by the map.
            File[] files = externalCacheDirectory.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    int dotIndex = fileName.lastIndexOf('.');
                    if (dotIndex > 0) {
                        String fileNamePart = fileName.substring(0, dotIndex);
                        String receiptImageId = fileNamePart;
                        int thumbNailIndex = fileNamePart.lastIndexOf("_tn");
                        if (thumbNailIndex > 0) {
                            receiptImageId = fileNamePart.substring(0, thumbNailIndex);
                        }
                        if (!receiptImageIdInfoMap.containsKey(receiptImageId)) {
                            try {
                                if (!file.delete()) {
                                    Log.e(Const.LOG_TAG,
                                            CLS_TAG + ".removeUnreferencedReceiptImages: unable to remove file '"
                                                    + file.getAbsolutePath() + "'.");
                                }
                            } catch (SecurityException secExc) {
                                Log.e(Const.LOG_TAG,
                                        CLS_TAG
                                                + ".removeUnreferencedReceiptImages: security exception removing file '"
                                                + file.getAbsolutePath() + "'.");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Will delete from the receipt store cache a receipt.
     * 
     * @param receiptImageId
     *            the receipt image id.
     */
    public void deleteReceiptInfo(String receiptImageId) {
        if (receiptInfos != null) {

            ReceiptInfo rcptInfo = null;
            ListIterator<ReceiptInfo> rcptInfoIter = receiptInfos.listIterator();
            while (rcptInfoIter.hasNext()) {
                ReceiptInfo receiptInfo = rcptInfoIter.next();
                if (receiptInfo.getReceiptImageId() != null
                        && receiptInfo.getReceiptImageId().equalsIgnoreCase(receiptImageId)) {
                    rcptInfoIter.remove();
                    rcptInfo = receiptInfo;
                    break;
                }
            }
            if (rcptInfo != null) {
                // Punt any downloaded images.
                if (isThumbnailDownloaded(rcptInfo)) {
                    File thumbnailFile = new File(getThumbnailDownloadPath(rcptInfo));
                    try {
                        thumbnailFile.delete();
                    } catch (Exception exc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptInfo: exception deleting thumbnail file", exc);
                    }
                }
                if (isImageDownloaded(rcptInfo)) {
                    File imageFile = new File(getImageDownloadPath(rcptInfo));
                    try {
                        imageFile.delete();
                    } catch (Exception exc) {
                        Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptInfo: exception deleting image file", exc);
                    }
                }
                // Punt any thumbnail from ImageCache.
                deleteReceiptThumbnail(rcptInfo);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".deleteReceiptInfo: receiptInfos is null!");
        }
    }

    /**
     * Gets whether or not this cache has a receipt list.
     * 
     * @return whether a receipt list has been set on the cache.
     */
    public boolean hasLastReceiptList() {
        if (receiptInfos == null) {
            loadReceiptListFromPersistence();
        }
        return (receiptInfos != null);
    }

    /**
     * Determines whether the last receipt list update is older than <code>expiration</code> milliseconds.
     * 
     * <br>
     * <b>NOTE:</b><br>
     * Clients should call <code>hasLastReceiptList</code> to determine whether the cache has a receipt list backed by
     * persistence.
     * 
     * @param expiration
     *            the expiration time in milliseconds.
     * 
     * @return If the cache has a receipt list, then will return <code>true</code> if the last update time is older than
     *         <code>expiration</code> milliseconds; otherwise, <code>false</code> will be returned. If the cache has no receipt
     *         list, then <code>false</code> will be returned.
     */
    public boolean isLastReceiptListUpdateExpired(long expiration) {
        boolean retVal = false;
        if (hasLastReceiptList()) {
            if (updateTime != null) {
                long curTimeMillis = System.currentTimeMillis();
                try {
                    long updateTimeMillis = updateTime.getTimeInMillis();
                    retVal = ((curTimeMillis - updateTimeMillis) > expiration);
                } catch (IllegalArgumentException ilaArgExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG
                            + ".isLastReceiptListUpdateExpired: unable to get millisecond time from 'updateTime'!",
                            ilaArgExc);
                    // Err to the side of caution.
                    retVal = true;
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG
                        + ".isLastReportListUpdateExpired: expense report info has null 'updateTime'!");
                // Err to the side of caution.
                retVal = true;
            }
        }
        return retVal;
    }

    /**
     * Sets the receipt list refetch flag.
     */
    public void setShouldFetchReceiptList() {
        refetchReceiptList = true;
    }

    /**
     * Clears the receipt list refetch flag.
     */
    public void clearShouldRefetchReceiptList() {
        refetchReceiptList = false;
    }

    /**
     * Contains whether an activity has used a receipt in such a fashion that the receipt list should be retrieved again from the
     * server.
     * 
     * @return whether the receipt list should be refetched.
     */
    public boolean shouldRefetchReceiptList() {
        return refetchReceiptList;
    }

    /**
     * Clears the flag indicating that the report list should be refetched.
     */
    public void clearShouldRefreshReceiptList() {
        refreshReceiptList = false;
    }

    /**
     * Sets whether the report list should be refreshed using local data.
     */
    public void setShouldRefreshReceiptList() {
        refreshReceiptList = true;
    }

    /**
     * Gets whether the receipt list should be refreshed locally.
     * 
     * @return whether the receipt list should be refreshed using local data.
     */
    public boolean shouldRefreshReceiptList() {
        return refreshReceiptList;
    }

    /**
     * Gets whether or not the full-sized receipt image has been downloaded.
     * 
     * @param receiptImageId
     *            the receipt image id.
     * @return whether the full-sized receipt image has been downloaded.
     */
    public boolean isImageDownloaded(ReceiptInfo receiptInfo) {
        boolean retVal = false;
        String imgPath = getDownloadPath(receiptInfo, false);
        File imgFile = new File(imgPath);
        retVal = imgFile.exists();
        return retVal;
    }

    /**
     * Gets the absolute path of the full-sized receipt image file.
     * 
     * @param receiptImageId
     *            the receipt image id.
     * @return the absolute path of the full-sized receipt image file if downloaded; otherwise, returns <code>null</code>.
     */
    public String getImageDownloadPath(ReceiptInfo receiptInfo) {
        return getDownloadPath(receiptInfo, false);
    }

    /**
     * Gets whether the thumnail-sized receipt image has been downloaded.
     * 
     * @param receiptImageId
     *            the receipt image id.
     * @return whether the thumbnail-sized receipt image has been downloaded.
     */
    public boolean isThumbnailDownloaded(ReceiptInfo receiptInfo) {
        boolean retVal = false;
        String imgPath = getDownloadPath(receiptInfo, true);
        if (imgPath != null) {
            File tnFile = new File(imgPath);
            retVal = tnFile.exists();
        }
        return retVal;
    }

    /**
     * Gets the absolute path to the thumbnail-sized receipt image.
     * 
     * @param receiptImageId
     *            the receipt image id.
     * @return the absolute path of the thumbnail-sized receipt image file if downloaded; otherwise, returns <code>null</code>.
     */
    public String getThumbnailDownloadPath(ReceiptInfo receiptInfo) {
        return getDownloadPath(receiptInfo, true);
    }

    /**
     * Gets the download path for a receipt file.
     * 
     * @param receiptInfo
     *            the receipt info object for which a path is requested.
     * @param thumbnail
     *            whether this is for a thumbnail image.
     * @return a string containing the receipt download path.
     */
    private String getDownloadPath(ReceiptInfo receiptInfo, boolean thumbnail) {
        String imgPath = null;
        if (externalCacheDirectory != null) {
            StringBuilder strBldr = new StringBuilder();
            strBldr.append(receiptInfo.getReceiptImageId());
            if (thumbnail) {
                strBldr.append("_tn");
            }
            strBldr.append('.');
            strBldr.append(receiptInfo.getFileType().toLowerCase());
            File tnFile = new File(externalCacheDirectory, strBldr.toString());
            imgPath = tnFile.getAbsolutePath();
        }
        return imgPath;
    }

}
