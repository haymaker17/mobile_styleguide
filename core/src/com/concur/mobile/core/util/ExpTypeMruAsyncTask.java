package com.concur.mobile.core.util;

import java.util.Calendar;
import java.util.TimeZone;

import android.os.AsyncTask;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.service.ConcurService;

/**
 * Use this class to update/insert Expense type MRUs.
 * 
 * @author sunill
 */
public class ExpTypeMruAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String CLS_TAG = ExpTypeMruAsyncTask.class.getSimpleName();
    private MobileDatabase mdb;
    private String userId, expKey, polkey;
    private ConcurService concurService;

    public ExpTypeMruAsyncTask(MobileDatabase mdb, String userId, String expKey, String polKey,
            ConcurService concurService) {
        this.mdb = mdb;
        this.userId = userId;
        this.expKey = expKey;
        this.polkey = polKey;
        this.concurService = concurService;
    }

    @Override
    protected String doInBackground(Void... params) {
        ConcurCore app = (ConcurCore) concurService.getApplication();
        IExpenseEntryCache expEntCache = app.getExpenseEntryCache();
        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        if (!(mdb.updateExpenseType(userId, expKey, polkey, 1, now))) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doInBackground : insert into expense type table is falied");
        } else {
            // update cache.
            expEntCache.updateExpenseTypesCacheForDB(concurService, polkey);
        }
        return expKey;

    }

}
