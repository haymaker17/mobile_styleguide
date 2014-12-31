package com.concur.mobile.core.service;

import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;

import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.base.service.parser.CommonParser;
import com.concur.mobile.core.service.parser.ResetPasswordParser;
import com.concur.mobile.core.util.FormatUtil;

public class ResetMobilePassword extends BaseResetPassword {

    private String email;
    private String keyPartA;
    private String keyPartB;
    private String mobilePassword;

    public ResetMobilePassword(Context context, int id, BaseAsyncResultReceiver receiver, String email,
            String keyPartA, String keyPartB, String mobilePassword) {

        super(context, id, receiver);

        this.email = email;
        this.keyPartA = keyPartA;
        this.keyPartB = keyPartB;
        this.mobilePassword = mobilePassword;
    }

    @Override
    protected String getServiceEndpoint() {
        return "/Mobile/MobileSession/ResetUserPin";
    }

    @Override
    protected String getPostBody() {
        StringBuilder sb = new StringBuilder();

        sb.append("<ResetUserPin>");
        FormatUtil.addXMLElementEscaped(sb, "Email", email);
        FormatUtil.addXMLElement(sb, "KeyPartA", keyPartA);
        FormatUtil.addXMLElement(sb, "KeyPartB", keyPartB);
        FormatUtil.addXMLElementEscaped(sb, "Pin", mobilePassword);
        sb.append("</ResetUserPin>");

        return sb.toString();
    }

    @Override
    protected int parse(CommonParser parser) {
        int result = RESULT_OK;

        // register the parser of interest
        resetPasswordParser = new ResetPasswordParser();
        parser.registerParser(resetPasswordParser, "ResetUserPinResult");

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

}
