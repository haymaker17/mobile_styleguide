//package com.concur.mobile.core.ipm.activity;
//
//import android.content.Context;
//import android.os.Bundle;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.LinearLayout;
//
//import com.concur.core.R;
//import com.concur.mobile.core.ipm.data.IpmMsg;
//import com.concur.mobile.core.ipm.data.IpmParams;
//import com.concur.mobile.core.util.LoggingAdListener;
//import com.concur.mobile.platform.ui.common.view.ListItem;
//import com.google.android.gms.ads.AdSize;
//import com.google.android.gms.ads.doubleclick.AppEventListener;
//import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
//import com.google.android.gms.ads.doubleclick.PublisherAdView;
//import com.google.android.gms.ads.mediation.admob.AdMobExtras;
//
///**
// * DFP adview ListItem subclass to work with ListItemAdapter
// */
//public class DfpMessageCenterListItem extends ListItem implements AppEventListener {
//
//    // private String adUnitId;
//    // private String body;
//    private IpmMsg ipmMsg;
//    private String versionName;
//
//    /**
//     * Constructs an instance of <code>DfpMessageCenterListItem</code>.
//     * 
//     * @param adUnitId
//     *            adUnitId of the message
//     * @param body
//     *            body of the message
//     * 
//     */
//    // public DfpMessageCenterListItem(String adUnitId, String body, int listItemViewType) {
//    // this.adUnitId = adUnitId;
//    // this.body = body;
//    // this.listItemViewType = listItemViewType;
//    // }
//
//    /**
//     * Constructs an instance of <code>DfpMessageCenterListItem</code>.
//     * 
//     * @param ipmMsg
//     *            ipmMsg details
//     * 
//     */
//    public DfpMessageCenterListItem(IpmMsg ipmMsg, String versionName) {
//        this.ipmMsg = ipmMsg;
//        this.versionName = versionName;
//
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see com.concur.mobile.activity.expense.ListItem#buildView(android.content.Context, android.view.View,
//     * android.view.ViewGroup)
//     */
//    @Override
//    public View buildView(Context context, View convertView, ViewGroup parent) {
//        View messageView = null;
//        PublisherAdView adView = null;
//        if (convertView == null) {
//            // Create the main row container and static elements
//            LayoutInflater inflater = LayoutInflater.from(context);
//            messageView = inflater.inflate(R.layout.dfp_ads_footer, null);
//            adView = new PublisherAdView(context);
//        } else {
//            messageView = convertView;
//        }
//        // Look up the PublisherAdView as a resource and load a request.
//
//        if (ipmMsg != null && ipmMsg.adUnitId != null && adView != null) {
//            adView.setAdUnitId(ipmMsg.adUnitId);
//            // adView.setAdSizes(new AdSize(360, 56), new AdSize(360, 198), new AdSize(480, 264), new AdSize(720, 396),
//            // new AdSize(1080, 594), new AdSize(1440, 792), new AdSize(480, 264), AdSize.SMART_BANNER);// , AdSize.BANNER
//            // new AdSize(540, 297)
//            adView.setAdSizes(new AdSize(300, 165), AdSize.SMART_BANNER); // setAdSizes(new AdSize(360, 56), AdSize.SMART_BANNER);
//
//            adView.setAdListener(new LoggingAdListener());
//            LinearLayout layout = (LinearLayout) messageView.findViewById(R.id.adView);
//            // Add the adView to it.
//            if (layout != null) {
//                layout.addView(adView); // , new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
//                // LayoutParams.WRAP_CONTENT));
//                PublisherAdRequest pr = null;
//                if (ipmMsg.params != null) {
//
//                    IpmParams extras = ipmMsg.params;
//                    Bundle bundle = new Bundle();
//                    bundle.putString("CteProduct", extras.toString(extras.cteProduct));
//                    bundle.putString("Lang", extras.getLang());
//                    bundle.putString("AppVersion", versionName);
//
//                    pr = new PublisherAdRequest.Builder().addNetworkExtras(new AdMobExtras(bundle)).build();
//                } else {
//                    pr = new PublisherAdRequest.Builder().build();
//
//                }
//                if (adView != null) {
//                    adView.setAppEventListener((AppEventListener) context);
//                }
//                // Initiate an request to load the AdView with an ad.
//                adView.loadAd(pr);
//                // ipmMsg = null;
//            }
//        }
//        return messageView;
//    }
//
//    /*
//     * (non-Javadoc)
//     * 
//     * @see com.concur.mobile.activity.expense.ListItem#isEnabled()
//     */
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//
//    @Override
//    public void onAppEvent(String arg0, String arg1) {
//        // TODO Auto-generated method stub
//
//    }
//
//}