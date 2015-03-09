///**
// * Copyright (c) 2012 Concur Technologies, Inc.
// */
//package com.concur.mobile.core.ipm.activity;
//
//import android.app.Activity;
//import android.content.pm.PackageInfo;
//import android.content.pm.PackageManager.NameNotFoundException;
//import android.os.Bundle;
//import android.view.View;
//import android.widget.ListView;
//
//import com.concur.core.R;
//import com.concur.mobile.base.service.BaseAsyncRequestTask.AsyncReplyListener;
//import com.concur.mobile.core.activity.BaseActivity;
//import com.concur.mobile.core.ipm.data.IpmMsg;
//import com.concur.mobile.core.ipm.service.IpmReply;
//import com.concur.mobile.platform.ui.common.view.ListItemAdapter;
//import com.google.android.gms.ads.doubleclick.AppEventListener;
//import com.google.android.gms.ads.doubleclick.PublisherAdView;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * Base wrapper to show google dfp ads
// * 
// * @author Tejo A
// * 
// */
//public abstract class AbstractIpmMsgProgress extends BaseActivity implements AppEventListener {
//
//    private static final String DFP_ADS = "DFP";
//
//    // listener filter
//    protected IpmResultsListener ipmResultsReceiver;
//
//    // view for DFP ads
//    protected PublisherAdView adView;
//
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//
//        // TODO dfp events
//        // if (adView != null) {
//        //
//        // // adView.setAppEventListener(this);
//        // OnClickListener l = new OnClickListener() {
//        //
//        // Intent i;
//        //
//        // @Override
//        // public void onClick(View v) {
//        // // v.
//        // i = new Intent(v.getContext(), HotelSearch.class);
//        // i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
//        //
//        // startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
//        //
//        // }
//        //
//        // };
//        // adView.setOnClickListener(l);
//        // }
//    }
//
//    /** Called when the refresh button is clicked. */
//    // TODO dfp events
//    // public void refreshAd(View unusedView) {
//    // if (adView != null) {
//    // adView.loadAd(new PublisherAdRequest.Builder().build());
//    // }
//    // }
//
//    /**
//     * DFP events
//     */
//    @Override
//    public void onAppEvent(String name, String info) {
//        // String message = String.format("Received app event (%s, %s)", name, info);
//        // Log.d(CLS_TAG, message);
//        // Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
//        // if ("page".equals(name)) {
//        // LinearLayout layout = (LinearLayout) findViewById(R.layout.message_center);
//        // if ("MessageCenter".equals(info)) {
//        // layout.setBackgroundColor(Color.RED);
//        // } else if ("hotelSearch".equals(info)) {
//        // startActivity(new Intent(this, HotelSearch.class));
//        //
//        // }
//        // }
//
//    }
//
//    /**
//     * AsyncReplyListener for IPM Results
//     */
//    public class IpmResultsListener implements AsyncReplyListener {
//
//        public Activity activity;
//
//        /**
//         * @return the activity
//         */
//        public Activity getActivity() {
//            return activity;
//        }
//
//        /**
//         * @param activity
//         *            the activity to set
//         */
//        public void setActivity(Activity activity) {
//            this.activity = activity;
//        }
//
//        public void onRequestSuccess(Bundle resultData) {
//
//            IpmReply ipmReply = getConcurCore().getIpmMsgResults();
//
//            if (ipmReply != null && ipmReply.ipmMsgs != null && ipmReply.ipmMsgs.size() > 0) {
//                List<DfpMessageCenterListItem> msgList = new ArrayList<DfpMessageCenterListItem>();
//                PackageInfo pinfo = null;
//                String versionName = null;
//                // get app current version
//                try {
//                    pinfo = activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0);
//                } catch (NameNotFoundException e) {
//                    e.printStackTrace();
//                }
//                if (pinfo != null) {
//                    versionName = pinfo.versionName;
//
//                }
//                // Currently only showing 1 ad in future may have multiples
//                for (IpmMsg ipmMsg : ipmReply.ipmMsgs) {
//
//                    // only show dfp ads
//                    if (ipmMsg.adType.equalsIgnoreCase(DFP_ADS)) {
//
//                        DfpMessageCenterListItem msg = new DfpMessageCenterListItem(ipmMsg, versionName);
//                        msgList.add(msg);
//                        // util to show ads
//                        // adView = ViewUtil.showDFPAds(activity, ipmMsg, R.id.ads);
//                        // if (adView != null) {
//                        // adView.setAppEventListener((AppEventListener) activity);
//                        // }
//                        // setContentView(R.layout.message_center);
//                        // adView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);// performClick();
//                        // OnClickListener l = new OnClickListener() {
//                        //
//                        // Intent i;
//                        //
//                        // @Override
//                        // public void onClick(View v) {
//                        // // v.
//                        // i = new Intent(v.getContext(), HotelSearch.class);
//                        // i.putExtra(Flurry.PARAM_NAME_BOOKED_FROM, Flurry.PARAM_VALUE_HOME);
//                        //
//                        // startActivityForResult(i, Const.REQUEST_CODE_BOOK_HOTEL);
//                        //
//                        // }
//                        //
//                        // };
//                        // adView.setClickable(false);
//                        // adView.setOnClickListener(l);
//                    }
//                }
//                ListItemAdapter<DfpMessageCenterListItem> adapter = new ListItemAdapter<DfpMessageCenterListItem>(
//                        activity, msgList);
//
//                // list view after dfp adview
//                ListView listView = (ListView) findViewById(R.id.dfpAdsCenterList);
//                listView.setVisibility(View.VISIBLE);
//                listView.setAdapter(adapter);
//
//            }
//
//        }
//
//        @Override
//        public void cleanup() {
//            ipmResultsReceiver = null;
//        }
//
//        @Override
//        public void onRequestFail(Bundle resultData) {
//            // TODO Auto-generated method stub
//
//        }
//
//        @Override
//        public void onRequestCancel(Bundle resultData) {
//            // TODO Auto-generated method stub
//
//        }
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        if (adView != null) {
//            adView.resume();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        if (adView != null) {
//            adView.pause();
//        }
//        super.onPause();
//    }
//
//    @Override
//    protected void onDestroy() {
//        if (adView != null) {
//            adView.destroy();
//        }
//        super.onDestroy();
//    }
//
//}
