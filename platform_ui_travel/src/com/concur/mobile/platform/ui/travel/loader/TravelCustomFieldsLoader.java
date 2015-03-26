package com.concur.mobile.platform.ui.travel.loader;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author RatanK
 */
public class TravelCustomFieldsLoader extends PlatformAsyncTaskLoader<TravelCustomFieldsConfig> {

    public static final String SERVICE_END_POINT = "/Mobile/Config/TravelCustomFields";
    private static final String CLS_TAG = "TravelCustomFieldsLoader";
    protected TravelCustomFieldsConfig travelCustomFieldsConfig;

    public TravelCustomFieldsLoader(Context context) {
        super(context);
    }

    /**
     * Configure connection properties. The default implementation sets the user agent, content type to type/xml, connect timeout
     * to 10 seconds, and read timeout to 30 seconds.
     *
     * @param connection The open but not yet connected {@link HttpURLConnection} to the server
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
        super.configureConnection(connection);
        connection.addRequestProperty(HTTP_HEADER_XSESSION_ID, PlatformProperties.getSessionId());
    }

    @Override
    public TravelCustomFieldsConfig loadInBackground() {
        travelCustomFieldsConfig = super.loadInBackground();
        if (result == RESULT_ERROR && travelCustomFieldsConfig != null) {
            travelCustomFieldsConfig.errorOccuredWhileRetrieving = true;
        }
        return travelCustomFieldsConfig;
    }

    @Override
    protected TravelCustomFieldsConfig parseStream(InputStream is) {

        try {

            travelCustomFieldsConfig = TravelCustomFieldsConfig.parseTravelCustomFieldsConfig(is);

            // if(obj != null) {
            // travelCustomFieldsConfig = (T) travelCustomFieldsConfig;
            // }

            // mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"),
            // responseType.getType());
            //
            // if (mwsResp != null) {
            // if (mwsResp.getData() != null) {
            // preSellOption = ((T) mwsResp.getData());
            // } else {
            // Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
            // Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
            // }
            // }

        } catch (IOException ioExc) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".parseStream: I/O exception parsing data.", ioExc);
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

        return travelCustomFieldsConfig;
    }

    @Override
    protected String getServiceEndPoint() {
        return SERVICE_END_POINT;
    }

}
