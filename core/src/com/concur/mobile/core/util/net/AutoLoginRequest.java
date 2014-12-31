/**
 * Copyright (c) 2012 Concur Technologies, Inc.
 */
package com.concur.mobile.core.util.net;

import java.util.Locale;

import org.apache.http.client.methods.HttpRequestBase;

import android.os.Handler;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.util.Const;

/**
 * @author Chris N. Diaz
 * 
 */
public class AutoLoginRequest extends LoginRequest {

    protected String accessToken;

    /**
     * @param app
     * @param product
     * @param serverAdd
     * @param accessToken
     */
    public AutoLoginRequest(ConcurCore app, Handler ui, Product product, String serverAdd, String accessToken) {

        super("ConcurLoginThread", app, ui, product);

        this.accessToken = accessToken;

        // Put together the login URI
        StringBuilder uri = new StringBuilder(serverAdd).append("/mobile/MobileSession/AutoLoginV3");
        setUri(uri.toString());

        Locale locale = app.getResources().getConfiguration().locale;

        // Build up our POST
        StringBuilder post = new StringBuilder();
        post.append("<Credentials>");
        post.append("<Locale>").append(locale.toString()).append("</Locale>");
        post.append("</Credentials>");

        setBody(post.toString());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.net.RequestThread#addPostHeaders(org.apache.http.client.methods.HttpRequestBase)
     */
    @Override
    protected void addPostHeaders(HttpRequestBase request) {
        super.addPostHeaders(request);

        // Set the oAuth token.
        if (accessToken != null) {
            request.setHeader(Const.HTTP_HEADER_AUTHORIZATION, "OAuth " + accessToken);
        }
    }

}
