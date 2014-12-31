package com.concur.mobile.gov.expense.doc.service;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class AttachTMReceiptReply extends ServiceReply {

    public String message;

    public void parse(InputStream is, String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, encoding);

            parse(xpp);

            mwsStatus = Const.REPLY_STATUS_SUCCESS;

        } catch (XmlPullParserException xppe) {
            Log.e(Const.LOG_TAG, "XPP exception parsing response", xppe);
        } catch (IOException ioe) {
            Log.e(Const.LOG_TAG, "IO exception parsing response", ioe);
        }

    }

    private static final String ERROR = "error";

    protected void parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        String tag = "";

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tag = xpp.getName();
                break;
            case XmlPullParser.TEXT:
                if (ERROR.equalsIgnoreCase(tag)) {
                    message = xpp.getText();
                }
                break;
            case XmlPullParser.END_TAG:
                tag = ""; // Make sure to clear this because TEXT can exist outside elements and we don't want it
                break;
            }
            eventType = xpp.next();
        }
    }

}