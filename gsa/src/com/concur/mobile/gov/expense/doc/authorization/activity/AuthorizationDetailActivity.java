/**
 * @author sunill
 */
package com.concur.mobile.gov.expense.doc.authorization.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.concur.gov.R;
import com.concur.mobile.gov.expense.doc.activity.DocumentDetail;

public class AuthorizationDetailActivity extends DocumentDetail {

    private static final String CLS_TAG = AuthorizationDetailActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected String getTitleText() {
        return getString(R.string.gov_authorization_title);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(DocumentDetail.CLS_TAG, CLS_TAG + ".onActivityResult");
        super.onActivityResult(requestCode, resultCode, data);
    }
}
