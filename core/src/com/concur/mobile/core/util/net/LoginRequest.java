package com.concur.mobile.core.util.net;

import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpStatus;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ConcurCore.Product;
import com.concur.mobile.core.activity.Preferences;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.platform.util.Parse;

/**
 * This class handles the login operation in a separate thread. When run, it will attempt to login and return either a valid
 * session ID or null.
 */
public class LoginRequest extends RequestThread {

    // private static final String CLS_TAG = LoginRequest.class.getSimpleName();

    protected final ConcurCore app;
    protected final Handler uiHandler;
    protected HashMap<String, Object> responseMap;

    public LoginRequest(String threadName, ConcurCore app, Handler ui, Product product) {
        super(threadName, product);
        this.app = app;
        this.uiHandler = ui;
    }

    /**
     * Constructs a login request based on a user id and pin.
     * 
     * @param app
     *            the application instance.
     * @param ui
     *            the UI handler.
     * @param product
     *            the product version.
     * @param serverAdd
     *            the server address.
     * @param loginId
     *            the login id.
     * @param pinOrPassword
     *            either the login pin or password.
     */
    public LoginRequest(ConcurCore app, Handler ui, Product product, String serverAdd, String loginId,
            String pinOrPassword) {
        super("ConcurLoginThread", product);

        this.app = app;
        this.uiHandler = ui;

        Locale locale = app.getResources().getConfiguration().locale;

        // Put together the login URI
        StringBuilder uri = new StringBuilder(serverAdd).append("/mobile/MobileSession/PPLogin");

        setUri(uri.toString());

        // Build up our POST
        StringBuilder post = new StringBuilder();
        post.append("<Credentials>");
        post.append("<Locale>").append(locale.toString()).append("</Locale>");
        post.append("<LoginID>").append(FormatUtil.escapeForXML(loginId)).append("</LoginID>");
        post.append("<Password>").append(FormatUtil.escapeForXML(pinOrPassword)).append("</Password>");
        post.append("</Credentials>");

        setBody(post.toString());

    }

    /**
     * Constructs a login request based on an company SSO web session id.
     * 
     * @param app
     *            the application instance.
     * @param ui
     *            the UI handler.
     * @param product
     *            the product version.
     * @param serverAdd
     *            the server address.
     * @param webSessionId
     *            the web session id.
     */
    public LoginRequest(ConcurCore app, Handler ui, Product product, String serverAdd, String webSessionId) {
        super("ConcurLoginThread", product);

        this.app = app;
        this.uiHandler = ui;

        Locale locale = app.getResources().getConfiguration().locale;

        // Put together the login URI
        StringBuilder uri = new StringBuilder(serverAdd).append("/Mobile/MobileSession/CorpSsoLogin");

        setUri(uri.toString());

        StringBuilder strBldr = new StringBuilder();
        strBldr.append("<WebSession>");
        ViewUtil.addXmlElement(strBldr, "Locale", locale.toString());
        ViewUtil.addXmlElement(strBldr, "SessionId", webSessionId);
        strBldr.append("</WebSession>");
        setBody(strBldr.toString());
    }

    /**
     * Gets the map of response objects.
     * 
     * @return the map of response objects.
     */
    public HashMap<String, Object> getResponseMap() {
        return responseMap;
    }

    protected String getNodeValue(Node node) {

        String val = null;
        if (node != null) {
            Node textNode = node.getFirstChild();
            if (textNode != null) {
                val = textNode.getNodeValue();
            }
        }

        return val;
    }

    @Override
    protected void handleResponse(int status) {

        responseMap = new HashMap<String, Object>();

        responseMap.put(Const.REPLY_HTTP_STATUS_CODE, status);

        Document doc;
        switch (status) {
        case -1:
            responseMap.put(Const.LR_STATUS, statusMessage);
            break;

        case HttpStatus.SC_OK:

            doc = getResponseAsDoc();

            if (doc == null) {
                // This should never happen but Market error reports show it happening
                // Just fail the login so they can try again.
                responseMap.put(Const.LR_STATUS, app.getText(R.string.login_invalid_concur_credentials).toString());
            } else {
                // Check for remote wipe
                NodeList wipeNode = doc.getElementsByTagName("RemoteWipe");
                String wipeValue = getNodeValue(wipeNode.item(0));
                boolean shouldWipe = Parse.safeParseBoolean(wipeValue);

                if (shouldWipe) {
                    Log.d(Const.LOG_TAG, "Remote wipe activated");
                    responseMap.put(Const.LR_WIPED, shouldWipe);

                    // Do the wipe
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(app);
                    Preferences.clearUser(prefs);
                    app.clearLocalData();

                } else {
                    responseMap.putAll(parseLoginResponse(doc));
                }
            }
            break;

        case HttpStatus.SC_UNAUTHORIZED:
            responseMap.put(Const.LR_STATUS, app.getText(R.string.login_invalid_concur_credentials).toString());
            break;

        case HttpStatus.SC_FORBIDDEN:
            doc = getResponseAsDoc();
            String msg = app.getText(R.string.login_failure).toString();
            if (doc != null) {
                NodeList msgNode = doc.getElementsByTagName("Message");
                if (msgNode != null) {
                    msg = msgNode.item(0).getFirstChild().getNodeValue();
                }
            }
            responseMap.put(Const.LR_STATUS, msg);

            break;
        case HttpStatus.SC_INTERNAL_SERVER_ERROR:
            responseMap.put(Const.LR_STATUS, app.getText(R.string.general_server_error).toString());
            break;
        }

        if (uiHandler != null) {
            uiHandler.sendMessage(uiHandler.obtainMessage(Const.MSG_LOGIN_RESULT, responseMap));
        }
    }

    public HashMap<String, Object> parseLoginResponse(Document doc) {
        return LoginResponseParser.parseV2(doc);
    }

}
