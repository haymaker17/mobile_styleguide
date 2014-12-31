/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.concur.gov.R;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.util.Const;

public class CommentsActivity extends BaseActivity {

    private static final String CLS_TAG = CommentsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle = getIntent().getExtras();
        setContentView(R.layout.drill_in_comments);
        initScreenHeader();
        initValue(bundle);
    }

    /**
     * This method set screen header title.
     * */
    private void initScreenHeader() {
        getSupportActionBar().setTitle(getString(R.string.gov_docdetail_comment).toString());
    }

    /**
     * set comment value
     * 
     * @param bundle
     *            : bundle data from previous activity
     * */
    private void initValue(Bundle bundle) {
        if (bundle != null) {
            EditText comments = (EditText) findViewById(R.id.comment_edittext_value);
            TextView noValue = (TextView) findViewById(R.id.comment_novalue);
            String value = bundle.getString(DocumentDetail.COMMENTS);
            if (comments != null && noValue != null) {
                comments.setEnabled(false);
                if (value != null && value.length() > 0) {
                    comments.setText(value);
                    noValue.setVisibility(View.GONE);
                } else {
                    noValue.setVisibility(View.VISIBLE);
                }
            }
            Log.e(Const.LOG_TAG, CLS_TAG + ".initValue : views are null");
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }
}
