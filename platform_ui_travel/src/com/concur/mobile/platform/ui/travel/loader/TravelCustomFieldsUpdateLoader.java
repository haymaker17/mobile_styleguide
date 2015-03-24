package com.concur.mobile.platform.ui.travel.loader;

import android.content.Context;
import android.util.Log;
import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.List;

/**
 * Loader for posting a set of travel custom fields in order to retrieve an updated set of fields
 *
 * @author RatanK
 */
public class TravelCustomFieldsUpdateLoader extends PlatformAsyncTaskLoader<TravelCustomFieldsConfig> {

    public static final String SERVICE_END_POINT = "/Mobile/Config/UpdateTravelCustomFields";
    private static final String CLS_TAG = TravelCustomFieldsUpdateLoader.class.getSimpleName();
    /**
     * Contains the list of <code>TravelCustomField</code> objects containing the values to be sent for update.
     */
    public List<TravelCustomField> fields;

    protected TravelCustomFieldsConfig travelCustomFieldsConfig;

    public TravelCustomFieldsUpdateLoader(Context context, List<TravelCustomField> fields) {
        super(context);
        this.fields = fields;
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
    protected TravelCustomFieldsConfig parseStream(InputStream is) {

        try {

            // TODO - handle the error scenarios

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

    @Override
    protected String getPostBody() {
        StringBuilder strBldr = new StringBuilder();
        if (fields != null) {
            TravelCustomField.serializeToXMLForWire(strBldr, fields, false);
        }
        return strBldr.toString();
    }

}
