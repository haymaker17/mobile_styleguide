package com.concur.mobile.platform.config.user;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.util.List;

import org.apache.http.HttpStatus;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.base.service.BaseAsyncRequestTask;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.platform.service.PlatformAsyncRequestTask;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.Const;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Async task to retrieve the apps listing from the Market Place API
 * 
 * @author RatanK
 * 
 */

// At present this Async task is not used in the app. After the Market Place App Center Workflow requirements are finalised, this
// class will be moved to appropriate package
public class AppsListingRequestTask extends PlatformAsyncRequestTask {

    private static final String CLS_TAG = "AppsListingRequestTask";

    // Contains the service end-point for the <code>GetListings</code> MWS call.
    private static final String SERVICE_END_POINT = "/mobile/marketplace/v1.0/appcenter/GetListings";

    private String listingId;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<AppListingData> mwsResp;

    public AppsListingRequestTask(Context context, int requestId, BaseAsyncResultReceiver receiver, String listingId) {
        super(context, requestId, receiver);
        this.listingId = listingId;
    }

    @Override
    protected String getServiceEndPoint() {
        String srvEndPoint = SERVICE_END_POINT;

        if (!TextUtils.isEmpty(listingId)) {
            srvEndPoint = SERVICE_END_POINT + '/' + listingId;
        }
        return srvEndPoint;
    }

    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);

        // Set the accept header to JSON.
        connection.setRequestProperty(HEADER_ACCEPT, CONTENT_TYPE_JSON);
    }

    @Override
    public int parseStream(HttpURLConnection connection, InputStream is) {
        int result = BaseAsyncRequestTask.RESULT_OK;
        try {
            if (connection.getResponseCode() == HttpStatus.SC_OK) {

                // prepare the object Type expected in MWS response 'data' element
                Type type = new TypeToken<MWSResponse<AppListingData>>() {}.getType();

                mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"), type);
            }
        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
            result = BaseAsyncRequestTask.RESULT_ERROR;
        } finally {
            if (is != null) {
                try {
                    is.close();
                    is = null;
                } catch (IOException ioExc) {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception closing input stream.", ioExc);
                }
            }
        }
        return result;
    }

    @Override
    public int onPostParse() {
        int result = super.onPostParse();

        // TODO - below code is temporary and will be addressed in full when the requirements are finalised.
        if (mwsResp != null) {
            if (mwsResp.getData() != null) {
                AppListingData appListing = ((AppListingData) mwsResp.getData());
                if (appListing != null && appListing.appsListing != null && appListing.appsListing.size() > 0) {
                    MarketplaceListingApp app = appListing.appsListing.get(0);
                    Log.i(Const.LOG_TAG,
                            "\n\n\n ****** " + app.getPartnerName() + " - ListingId : " + app.getListingID());
                } else {
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
                }
            } else {
                Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
            }
        }

        return result;
    }

    class AppListingData {

        public List<MarketplaceListingApp> appsListing;

    }
}
