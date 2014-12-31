package com.concur.mobile.core.util.net;

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.content.Context;
import android.os.Handler;

import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.core.R;

public class PasswordLoginRequest extends RequestThread {

    private final Context context;
    private final Handler uiHandler;

    public PasswordLoginRequest(Context context, Handler ui, Product product, String serverAdd, String loginId,
            String password) {
        super("ConcurPasswordLoginThread", product);

        this.context = context;
        this.uiHandler = ui;

        // Put together the login URI
        StringBuilder uri = new StringBuilder(serverAdd).append("/Mobile/MobileSession/PasswordLogin");

        setUri(uri.toString());

        // Build up our POST
        StringBuilder post = new StringBuilder();
        post.append("<Credentials><LoginID>").append(FormatUtil.escapeForXML(loginId)).append("</LoginID>");
        post.append("<Password>").append(FormatUtil.escapeForXML(password)).append("</Password></Credentials>");

        setBody(post.toString());

    }

    @Override
    protected void handleResponse(int status) {

        HashMap<String, Object> response = new HashMap<String, Object>();

        switch (status) {
        case -1:
            response.put(Const.LR_STATUS, statusMessage);
            break;

        case HttpStatus.SC_OK:
            Document doc = getResponseAsDoc();

            if (doc != null) {
                NodeList idNode = doc.getElementsByTagName("ID");
                String sessionId = idNode.item(0).getFirstChild().getNodeValue();

                response.put(Const.LR_SESSION_ID, sessionId);
            } else {
                response.put(Const.LR_STATUS, "Unidentifiable Server Error Has Occurred");
            }

            break;

        case HttpStatus.SC_UNAUTHORIZED:
            response.put(Const.LR_STATUS, context.getResources().getText(R.string.register_login_unathorized)
                    .toString());

            break;

        case HttpStatus.SC_FORBIDDEN:
            doc = getResponseAsDoc();
            String msg = context.getText(R.string.login_failure).toString();
            if (doc != null) {
                NodeList msgNode = doc.getElementsByTagName("Message");
                if (msgNode != null) {
                    try {
                        msg = msgNode.item(0).getFirstChild().getNodeValue();
                    } catch (NullPointerException npe) {
                        // Just in case the 403 comes from somewhere unexpected and does not have the expected doc...
                        // Eat it.
                    }
                }
            }
            response.put(Const.LR_STATUS, msg);

            break;
        }

        uiHandler.sendMessage(uiHandler.obtainMessage(Const.MSG_LOGIN_RESULT, response));
    }

}
