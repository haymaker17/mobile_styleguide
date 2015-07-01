package com.concur.mobile.corp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.expense.report.approval.activity.Approval;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.platform.authentication.EmailLookUpRequestTask;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * This activity will foreground the app, if running or will perform startup if not.
 * 
 * @author sunill
 * 
 */
public class ForegroundApp extends Activity {

    private static final String CLS_TAG = ForegroundApp.class.getSimpleName();

    private static final String TYPE = "type";
    private static final String RPT_KEY = "rptkey";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_data);
        TextView view = (TextView) findViewById(R.id.data_loading_text);
        view.setText(getString(R.string.general_loading).toString());
        try {
            getDataFromEmail();
        } catch (URISyntaxException e) {
            // TODO Auto-generated catch block
            // create dialog
            e.printStackTrace();
        }
    }

    protected boolean getDataFromEmail() throws URISyntaxException {
        boolean returnValue = false;
        Intent i = getIntent();
        String data = i.getDataString();

        if (data != null && data.length() > 0) {
            URI uri = new URI(data);
            // URI uri = new URI("http://www.concursolutions.com/mobile/ConcurMobileRedirect.asp?type=EXP_RPT_APPR&rptkey=1");
            String query = uri.getQuery();
            if (query != null && query.length() > 0 && query.charAt(query.length() - 1) == '/') {
                query = query.substring(0, query.length() - 1);
            }
            Log.d(CLS_TAG, query);
            Map<String, String> queryMap = getQueryParamters(query);
            boolean hasCorrectParams = false;
            if (queryMap != null && queryMap.size() > 0) {
                Set<String> keys = queryMap.keySet();
                for (String key : keys) {
                    if (key.equalsIgnoreCase(TYPE)) {
                        hasCorrectParams = true;
                        String type = queryMap.get(key);
                        if (type != null && type.length() > 0) {
                            if (type.equalsIgnoreCase(Const.PUSH_CONCUR_NOTIF_TYPE_REPORT_APPR)
                                    || type.equalsIgnoreCase(Const.PUSH_CONCUR_NOTIF_TYPE_TRIP_APPR)) {
                                Log.d(CLS_TAG, type);
                                Intent intent = new Intent(ForegroundApp.this, Approval.class);
                                intent.putExtra(ConcurCore.FROM_NOTIFICATION, true);
                                intent.putExtra(Flurry.EXTRA_FLURRY_CATEGORY, Flurry.CATEGORY_EMAIL_NOTIFICATION);
                                intent.putExtra(Flurry.EXTRA_FLURRY_ACTION_PARAM_VALUE, type);
                                TaskStackBuilder sb = TaskStackBuilder.create(this);
                                sb.addParentStack(Approval.class);
                                sb.addNextIntent(intent);
                                returnValue = true;
                                sb.startActivities();
                                finish();
                            } else if (type != null && type.equalsIgnoreCase("MOB_PIN_RSET")) {
                                Intent intent = new Intent(this, MobilePasswordSet.class);
                                String keyPartB = queryMap.get("keypart");
                                intent.putExtra("key_part_b", keyPartB);
                                intent.putExtra(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY,
                                        com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_MOBILE_PASSWORD);
                                startActivity(intent);
                                finish();
                            } else if (type != null && type.equalsIgnoreCase("MOB_PWD_RSET")) {
                                Intent intent = new Intent(this, PasswordSet.class);
                                String keyPartB = queryMap.get("keypart");
                                intent.putExtra("key_part_b", keyPartB);
                                intent.putExtra(EmailLookUpRequestTask.EXTRA_SIGN_IN_METHOD_KEY,
                                        com.concur.mobile.platform.ui.common.util.Const.LOGIN_METHOD_PASSWORD);
                                startActivity(intent);
                                finish();
                            } else if (type != null && type.equalsIgnoreCase("MOB_SSO_LGIN")) {
                                String companyCode = queryMap.get("companycode");
                                Intent intent = new Intent(this, EmailPasswordActivity.class);
                                intent.putExtra(EmailLookupActivity.EXTRA_ADVANCE_TO_COMPANY_SIGN_ON, true);
                                if (companyCode != null && companyCode.length() > 0) {
                                    intent.putExtra(Const.EXTRA_SSO_COMPANY_CODE, companyCode);
                                }
                                startActivity(intent);
                                finish();
                            }
                        }
                    }
                } // for-loop
            } // if-statement

            if (!hasCorrectParams) {
                // Just launch the app.
                Intent intent = new Intent(this, Startup.class);
                // Setting these flags brings the app to the foreground if it has already started,
                // or launches a new instance if it isn't.
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setAction(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                startActivity(intent);
                finish();
            }
        }
        return returnValue;
        // concurmobile://notification?type=EXP_RPT_APPR

    }

    protected Map<String, String> getQueryParamters(String query) {
        // TODO move to utility class.
        String[] params = query.split("&");
        Map<String, String> map = new HashMap<String, String>();
        for (String param : params) {
            String[] values = param.split("=");
            if (values.length >= 2) {
                String name = values[0];
                String value = values[1];
                map.put(name, value);
            }
        }
        return map;
    }

}