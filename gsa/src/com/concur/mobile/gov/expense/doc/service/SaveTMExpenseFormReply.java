package com.concur.mobile.gov.expense.doc.service;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;

public class SaveTMExpenseFormReply extends ServiceReply {

    public String resultText;

    public String expId;

    public void parse(InputStream is, String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, encoding);

            parse(xpp);

        } catch (XmlPullParserException xppe) {
            Log.e(Const.LOG_TAG, "XPP exception parsing response", xppe);
        } catch (IOException ioe) {
            Log.e(Const.LOG_TAG, "IO exception parsing response", ioe);
        }

    }

    private static final String RESULT = "result";
    private static final String ERROR_MESSAGE = "ErrorMessage";
    private static final String STATUS = "Status";
    private static final String ERROR = "Error";
    private static final String EXPDATA = "expid_data";
    private static final String EXPID = "expID";

    public void parse(XmlPullParser xpp) throws XmlPullParserException, IOException {
        String tag = "";

        boolean inResult = false;
        boolean inExp = false;

        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            switch (eventType) {
            case XmlPullParser.START_TAG:
                tag = xpp.getName();
                if (RESULT.equalsIgnoreCase(tag)) {
                    inResult = true;
                } else if (EXPDATA.equalsIgnoreCase(tag)) {
                    inExp = true;
                }
                break;
            case XmlPullParser.TEXT:
                if (inResult) {
                    if (ERROR.equalsIgnoreCase(tag)) {
                        // This isn't necessarily an error. The TM pieces return general status here.
                        resultText = xpp.getText();
                    }
                } else if (inExp) {
                    if (EXPID.equalsIgnoreCase(tag)) {
                        expId = xpp.getText();
                    }
                } else {
                    if (STATUS.equalsIgnoreCase(tag)) {
                        mwsStatus = xpp.getText();
                    } else if (ERROR_MESSAGE.equalsIgnoreCase(tag)) {
                        mwsErrorMessage = xpp.getText();
                    }
                }

                break;
            case XmlPullParser.END_TAG:
                tag = xpp.getName();
                if (RESULT.equalsIgnoreCase(tag)) {
                    inResult = false;
                } else if (EXPDATA.equalsIgnoreCase(tag)) {
                    inExp = false;
                }

                tag = ""; // Make sure to clear this because TEXT can exist outside elements and we don't want it
                break;
            }

            eventType = xpp.next();
        }
    }

}
