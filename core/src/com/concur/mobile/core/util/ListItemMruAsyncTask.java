package com.concur.mobile.core.util;

import android.os.AsyncTask;
import android.util.Log;

import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.data.MobileDatabase;
import com.concur.mobile.core.expense.data.IExpenseEntryCache;
import com.concur.mobile.core.expense.data.ListItem;
import com.concur.mobile.core.service.ConcurService;

/**
 * Use this class to update/insert List Item MRUs. e.g. Currency MRU, Location MRU.
 * 
 * @author sunill
 */
public class ListItemMruAsyncTask extends AsyncTask<Void, Void, String> {

    private static final String CLS_TAG = ListItemMruAsyncTask.class.getSimpleName();
    /** Contains Selected MRU List Item */
    private ListItem selMruListItem;
    /** Contains reference of <code>MobileDatabase</code> */
    private MobileDatabase mdb;
    /** Contains logged in user id */
    private String userId;
    /** Contains reference of <code> ConcurService</code> */
    private ConcurService concurService;

    public ListItemMruAsyncTask(ListItem selMruListItem, MobileDatabase mdb, String userId, ConcurService concurService) {
        this.selMruListItem = selMruListItem;
        this.mdb = mdb;
        this.userId = userId;
        this.concurService = concurService;
    }

    @Override
    protected String doInBackground(Void... params) {
        ConcurCore ConcurCore = (ConcurCore) concurService.getApplication();
        IExpenseEntryCache expEntCache = ConcurCore.getExpenseEntryCache();
        if (!(mdb.insertListItemToDB(selMruListItem))) {
            Log.e(Const.LOG_TAG, CLS_TAG + ".doInBackground : insert into mru table  is falied");
        } else {
            expEntCache.updateListItemCache(concurService, userId, selMruListItem.fieldId);
        }
        return selMruListItem.fieldId;
    }

}
