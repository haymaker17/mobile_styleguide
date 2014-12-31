package com.concur.mobile.core.util.net;

import java.util.HashMap;

import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import android.os.Handler;

import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;

public class RegisterRequest extends RequestThread {

    private final Handler uiHandler;

    public RegisterRequest(Handler ui, Product product, String serverAdd, String sessionId, String pin) {
        super("ConcurRegisterThread", product);

        this.uiHandler = ui;

        // Put together the registerURI
        StringBuilder uri = new StringBuilder(serverAdd).append("/Mobile/MobileSession/Register");

        setUri(uri.toString());

        setSession(sessionId);

        // Build up our POST
        StringBuilder post = new StringBuilder();
        post.append("<Registration><Pin>").append(FormatUtil.escapeForXML(pin)).append("</Pin></Registration>");

        setBody(post.toString());
    }

    @Override
    protected void handleResponse(int status) {
        HashMap<String, Object> response = new HashMap<String, Object>();

        response.put(Const.RR_STATUS, status);

        if (status != HttpStatus.SC_OK) {
            response.put(Const.RR_STATUS_MESSAGE, statusMessage);
        } else {
            // Check the response to make sure the registration was successful
            Document doc = getResponseAsDoc();

            NodeList statusNode = doc.getElementsByTagName("Status");
            String regStatus = statusNode.item(0).getFirstChild().getNodeValue();
            if (Const.STATUS_FAILURE.equals(regStatus)) {
                NodeList errorNode = doc.getElementsByTagName("ErrorMessage");
                String errorId = errorNode.item(0).getFirstChild().getNodeValue();
                response.put(Const.RR_STATUS_MESSAGE, errorId);
            }

        }

        uiHandler.sendMessage(uiHandler.obtainMessage(Const.MSG_REGISTER_RESULT, response));
    }

}
