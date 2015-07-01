package com.concur.mobile.corp.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import com.concur.breeze.R;
import com.concur.mobile.core.util.Const;

public class LoginHelpTopic extends ActionBarActivity {

    private final String CLS_TAG = LoginHelpTopic.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login_help_topic);
        Bundle args = getIntent().getExtras();

        if (args != null) {
            // Set the sub-header.
            if (args.containsKey(Const.EXTRA_LOGIN_HELP_TOPIC_SUBHEADER)) {
                TextView tv = (TextView) findViewById(R.id.help_topic_subheader);
                tv.setText(args.getString(Const.EXTRA_LOGIN_HELP_TOPIC_SUBHEADER));
            }

            // Set the actual message.
            if (args.containsKey(Const.EXTRA_LOGIN_HELP_TOPIC_MESSAGE)) {
                TextView tv = (TextView) findViewById(R.id.help_topic_message);
                tv.setText(Html.fromHtml(args.getString(Const.EXTRA_LOGIN_HELP_TOPIC_MESSAGE)),
                        TextView.BufferType.SPANNABLE);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".onCreate: unable to get bundle, destroying activity!");
            finish();
        }
    }
}
