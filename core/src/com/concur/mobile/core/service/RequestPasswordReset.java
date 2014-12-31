package com.concur.mobile.core.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.service.parser.RequestPasswordResetParser;
import com.concur.mobile.core.util.FormatUtil;

public class RequestPasswordReset extends BaseRequestPasswordReset {

    private String email;
    private String locale;

    public RequestPasswordReset(Context context, int id, BaseAsyncResultReceiver receiver, String email, String locale) {
        super(context, id, receiver);

        this.email = email;
        this.locale = locale;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/mobile/MobileSession/RequestPasswordReset";
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        sb.append("<RequestPasswordReset>");
        FormatUtil.addXMLElementEscaped(sb, "Email", email);
        FormatUtil.addXMLElement(sb, "Locale", locale);
        sb.append("</RequestPasswordReset>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {

        int result = RESULT_OK;

        // register the parser of interest
        resetPasswordRequestParser = new RequestPasswordResetParser();
        parser.registerParser(resetPasswordRequestParser, "RequestPasswordResetResult");

        try {
            parser.parse();
        } catch (XmlPullParserException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            result = RESULT_ERROR;
            e.printStackTrace();
        }
        return result;

    }

    @Override
    protected int onPostParse() {
        if (resetPasswordRequestParser.isSuccess()) {
            Preferences.setGoodPasswordMessageString(resetPasswordRequestParser.getGoodPasswordDescripton());
        }
        return super.onPostParse();
    }

}
