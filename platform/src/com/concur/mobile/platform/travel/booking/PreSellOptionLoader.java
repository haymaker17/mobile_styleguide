package com.concur.mobile.platform.travel.booking;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.concur.mobile.platform.service.PlatformAsyncTaskLoader;
import com.concur.mobile.platform.service.parser.MWSResponse;
import com.concur.mobile.platform.util.Const;
import com.concur.platform.PlatformProperties;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Base async task loader class to be used by travel segments in retrieving PreSellOptions
 * 
 * @author RatanK
 * 
 * @param <T>
 *            the object encapsulating the elements to be parsed
 */
public class PreSellOptionLoader<T> extends PlatformAsyncTaskLoader<T> {

    private static final String CLS_TAG = "PreSellOptionLoader";

    private String preSellOptionURL;

    /**
     * Contains the parsed MWS response
     */
    private MWSResponse<T> mwsResp;

    /**
     * Contains the object Type expected in MWS response 'data' element
     */
    private TypeToken<MWSResponse<T>> responseType;

    public PreSellOptionLoader(Context context, TypeToken<MWSResponse<T>> responseType, String preSellOptionURL) {
        super(context);

        this.responseType = responseType;
        this.preSellOptionURL = preSellOptionURL;
    }

    @Override
    protected String getServiceEndPoint() {
        return preSellOptionURL;
    }

    /**
     * Configure connection properties. The default implementation sets the user agent, content type to type/xml, connect timeout
     * to 10 seconds, and read timeout to 30 seconds.
     * 
     * @param connection
     *            The open but not yet connected {@link HttpURLConnection} to the server
     */
    @Override
    protected void configureConnection(HttpURLConnection connection) {
        // Set the access token.
        String accessToken = PlatformProperties.getAccessToken();
        if (!TextUtils.isEmpty(accessToken)) {
            connection.addRequestProperty(PlatformAsyncTaskLoader.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
    }

    @Override
    protected T parseStream(InputStream is) {
        T preSellOption = null;
        try {

            mwsResp = new Gson().fromJson(new InputStreamReader(new BufferedInputStream(is), "UTF-8"),
                    responseType.getType());

            if (mwsResp != null) {
                if (mwsResp.getData() != null) {
                    preSellOption = ((T) mwsResp.getData());
                } else {
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Info " + mwsResp.getInfo());
                    Log.i(Const.LOG_TAG, "\n\n\n ****** Errors " + mwsResp.getErrors());
                }
            }

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

        return preSellOption;
    }

}
