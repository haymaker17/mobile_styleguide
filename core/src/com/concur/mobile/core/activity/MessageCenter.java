package com.concur.mobile.core.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.concur.core.R;
import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
import com.concur.mobile.base.service.BaseAsyncResultReceiver;
import com.concur.mobile.core.ConcurCore;
import com.concur.mobile.core.ipm.activity.AbstractIpmMsgProgress;
import com.concur.mobile.core.ipm.service.IpmRequest;
import com.concur.mobile.core.util.EventTracker;
import com.concur.mobile.core.util.Flurry;
import com.concur.mobile.core.view.ListItemAdapter;

public class MessageCenter extends AbstractIpmMsgProgress {

    public static final String CLS_TAG = MessageCenter.class.getSimpleName();
    public static final String MESSAGE_CENTER = "mobileMessageCentre";

    private BaseAsyncResultReceiver ipmMsgResultsReceiver;
    private AsyncReplyListener ipmResultsListener;
    private IpmRequest ipmAsyncTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.message_center);
        initScreenHeader();
        // IPM ads request
        createIPMrequest();
        initScreenSubHeader();

        // Flurry Notification
        boolean badgeShown = Preferences.shouldShowNotificationBadge();
        Map<String, String> params = new HashMap<String, String>();
        params.put("Badge Shown", badgeShown ? Flurry.PARAM_VALUE_YES : Flurry.PARAM_VALUE_NO);
        EventTracker.INSTANCE.track(Flurry.CATEGORY_MESSAGE_CENTER, Flurry.EVENT_NAME_VIEWED, params);

        // Clear the preference indicating that messages have been viewed
        Preferences.setShowNotificationBadge(false);

        // Always set our result to OK for now so the home screen will properly update the action bar
        setResult(RESULT_OK);
    }

    /**
     * Will initialize the screen header.
     * 
     */
    protected void initScreenHeader() {
        getSupportActionBar().setTitle(R.string.msg_cntr_title);
    }

    /**
     * Will initialize the screen sub header.
     * 
     */
    protected void initScreenSubHeader() {

        List<MessageCenterListItem> msgList = new ArrayList<MessageCenterListItem>();

        Resources res = getResources();

        // Grab texts for MessageCenterListItem pieces.
        String whatsNew = getMessageArrayToString(res.getStringArray(R.array.whats_new));

        MessageCenterListItem msg = new MessageCenterListItem(getString(R.string.whats_new), whatsNew, 0);
        msgList.add(msg);

        ListItemAdapter<MessageCenterListItem> adapter = new ListItemAdapter<MessageCenterListItem>(this, msgList, 1);

        // list view after dfp adview
        ListView listView = (ListView) this.findViewById(R.id.msgCenterList);
        listView.setAdapter(adapter);

    }

    private String getMessageArrayToString(String[] messageText) {
        StringBuilder retValue = new StringBuilder("");
        if (messageText == null || messageText.length == 0) {
            return retValue.toString();
        }
        final int length = messageText.length;
        for (int i = 0; i < length - 1; i++) {
            retValue.append(messageText[i]).append("\n");
        }
        retValue.append(messageText[length - 1]);
        return retValue.toString();
    }

    private void createIPMrequest() {

        if (ConcurCore.isConnected()) {
            // Clear out any current results.
            getConcurCore().setIpmMsgResults(null);

            ipmMsgResultsReceiver = new BaseAsyncResultReceiver(new Handler());
            ipmResultsListener = new IpmResultsListener();
            ((IpmResultsListener) ipmResultsListener).setActivity(this);
            ipmMsgResultsReceiver.setListener(ipmResultsListener);
            ipmAsyncTask = new IpmRequest(getApplicationContext(), 1, ipmMsgResultsReceiver, MESSAGE_CENTER);
            ipmAsyncTask.execute();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

}