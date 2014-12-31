package com.concur.mobile.gov.expense.doc.service;

import java.io.IOException;
import java.io.InputStream;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.util.Log;

import com.concur.mobile.core.service.ServiceReply;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.expense.doc.data.GovExpenseForm;

public class GetTMExpenseFormReply extends ServiceReply {

    public GovExpenseForm form;

    public void parse(InputStream is, String encoding) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(is, encoding);

            form = new GovExpenseForm();
            form.parse(xpp);

            mwsStatus = Const.REPLY_STATUS_SUCCESS;

        } catch (XmlPullParserException xppe) {
            Log.e(Const.LOG_TAG, "XPP exception parsing response", xppe);
        } catch (IOException ioe) {
            Log.e(Const.LOG_TAG, "IO exception parsing response", ioe);
        }

    }

}
