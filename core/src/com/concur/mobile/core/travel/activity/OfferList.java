package com.concur.mobile.core.travel.activity;

import java.net.URI;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.activity.BaseActivity;
import com.concur.mobile.core.travel.data.IItineraryCache;
import com.concur.mobile.core.travel.data.Offer;
import com.concur.mobile.core.travel.data.OfferContent;
import com.concur.mobile.core.travel.data.Trip;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.view.AsyncImageView;

public class OfferList extends BaseActivity implements OnItemClickListener {

    private static final String CLS_TAG = OfferList.class.getSimpleName();

    private Offer offer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.offerlist);

        if (isServiceAvailable()) {
            buildView();
        } else {
            buildViewDelay = true;
        }
    }

    @Override
    protected void onServiceAvailable() {
        buildView();
        buildViewDelay = false;
    }

    protected void buildView() {

        // Grab our trip out of the list of trips stored in the application
        Intent origIntent = getIntent();
        String itinLocator = origIntent.getStringExtra(Const.EXTRA_ITIN_LOCATOR);
        Trip trip = null;
        if (itinLocator != null) {
            IItineraryCache itinCache = getConcurCore().getItinCache();
            if (itinCache != null) {
                trip = itinCache.getItinerary(itinLocator);
                String offerId = origIntent.getStringExtra(Const.EXTRA_OFFER_ID);
                if (offerId != null) {
                    offer = trip.getOffer(offerId);
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: itin cache is null!");
            }
        }

        if (offer != null) {
            initScreenHeader();

            ListView listView = (ListView) findViewById(R.id.offer_list);
            listView.setAdapter(new OfferLinkAdapter(this, offer.content));
            listView.setOnItemClickListener(this);
        }
    }

    protected void initScreenHeader() {
        TextView txtView = (TextView) (findViewById(R.id.header).findViewById(R.id.header_navigation_bar_title));
        if (txtView != null) {
            txtView.setText(Format.localizeText(this, R.string.offerlist_title, offer.content.vendor));
        }
    }

    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        OfferContent.Link link = (OfferContent.Link) parent.getItemAtPosition(position);
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(link.actionUrl));
        startActivity(i);
    }

    private class OfferLinkAdapter extends BaseAdapter {

        private Context context;
        private OfferContent content;
        private URI imageURI;

        public OfferLinkAdapter(Context context, OfferContent content) {
            this.context = context;
            this.content = content;
            this.imageURI = content.getVendorImageURI(context);
        }

        public int getCount() {
            return content.actionLinks.size();
        }

        public OfferContent.Link getItem(int position) {
            return content.actionLinks.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.segmentlist_offer, null);

            OfferContent.Link link = getItem(position);

            // Set the icon
            AsyncImageView icon = (AsyncImageView) v.findViewById(R.id.segmentListSegmentIconView);
            icon.setAsyncUri(imageURI);

            // The offer text
            TextView tv = (TextView) v.findViewById(R.id.segmentListOfferTitle);
            tv.setText(link.title);

            return v;
        }

    }
}
