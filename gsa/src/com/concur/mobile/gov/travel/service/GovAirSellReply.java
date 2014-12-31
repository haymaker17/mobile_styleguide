/**
 * 
 */
package com.concur.mobile.gov.travel.service;

import java.io.InputStream;

import android.sax.Element;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.util.Log;
import android.util.Xml;
import android.util.Xml.Encoding;

import com.concur.mobile.core.travel.air.service.AirSellReply;
import com.concur.mobile.core.util.Const;

/**
 * An extension of <code>ActionStatusServiceReply</code> for
 * handling a AirSellRequest response.
 */
public class GovAirSellReply extends AirSellReply {

    private static final String CLS_TAG = GovAirSellReply.class.getSimpleName();

    private static final String NAMESPACE = "";

    public String authorizationNumber;
    public String tripLocator, errorMsg;

    public static GovAirSellReply parseXMLReply(InputStream inputStream, Encoding encoding) {

        final GovAirSellReply reply = new GovAirSellReply();

        if (inputStream != null && encoding != null) {

            RootElement root = new RootElement(NAMESPACE, "AirSellResponse");

            Element recordLocatorEl = root.getChild(NAMESPACE, "ItinLocator");
            recordLocatorEl.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    reply.itinLocator = body;
                }
            });

            Element authorizationNumberEl = root.getChild(NAMESPACE, "AuthorizationNumber");
            authorizationNumberEl.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    reply.authorizationNumber = body;
                }
            });

            Element tripLocatorEl = root.getChild(NAMESPACE, "TripLocator");
            tripLocatorEl.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    reply.tripLocator = body;
                }
            });

            Element statusEl = root.getChild(NAMESPACE, "Status");
            statusEl.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    reply.mwsStatus = body;
                }
            });

            Element errorMsg = root.getChild(NAMESPACE, "ErrorMessage");
            errorMsg.setEndTextElementListener(new EndTextElementListener() {

                public void end(String body) {
                    reply.errorMsg = body;
                }
            });
            try {
                Xml.parse(inputStream, encoding, root.getContentHandler());
            } catch (Exception e) {
                Log.e(Const.LOG_TAG, CLS_TAG + ".respnseXml - error parsing XML.", e);
                throw new RuntimeException(e);
            }

        }

        return reply;
    }
}
