/**
 * @author sunill
 */
package com.concur.mobile.gov.util;

import android.database.Cursor;
import android.os.AsyncTask;

import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.gov.service.GovService;

public class GovDBAsyncTask extends AsyncTask<Void, Void, Cursor> {

    private IGovDBListener listener;
    private GovService service;
    private String docType, docName, travId;

    public GovDBAsyncTask(String docname, String doctype, String travId, GovService service) {
        this.service = service;
        this.docType = doctype;
        this.docName = docname;
        this.travId = travId;
    }

    public void setGovDBListener(IGovDBListener listener) {
        this.listener = listener;
    }

    @Override
    protected Cursor doInBackground(Void... params) {
        MobileDatabase db = service.getMobileDatabase();
        Cursor cur = db
            .loadGovDocument(service.prefs.getString(Const.PREF_USER_ID, null), travId, docName, docType);
        return cur;
    }

    @Override
    protected void onPostExecute(Cursor result) {
        try {
            listener.onDocDetailListenerSucceeded(result);
        } finally {
            if (result != null && !result.isClosed()) {
                result.close();
            }
        }
    }
}
