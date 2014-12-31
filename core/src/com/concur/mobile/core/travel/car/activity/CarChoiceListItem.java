/**
 * 
 */
package com.concur.mobile.core.travel.car.activity;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.base.util.Format;
import com.concur.mobile.core.travel.car.data.CarChain;
import com.concur.mobile.core.travel.car.data.CarChoice;
import com.concur.mobile.core.travel.car.data.CarDescription;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;

/**
 * An extension of <code>ListItem</code> for rendering a <code>CarChoice</code> object in a list.
 */
public class CarChoiceListItem extends ListItem {

    private static final String CLS_TAG = CarChoiceListItem.class.getSimpleName();

    private CarChoice carChoice;

    private CarChain carChain;

    private CarDescription carDescription;

    /**
     * Constructs an instance of <code>CarChoiceListItem</code> for rendering a car choice.
     * 
     * @param carChoice
     *            contains an instance of <code>CarChoice</code>.
     */
    public CarChoiceListItem(CarChoice carChoice, CarChain carChain, CarDescription carDescription) {
        this.carChoice = carChoice;
        this.carChain = carChain;
        this.carDescription = carDescription;
    }

    /**
     * Gets the instance of <code>CarChoice</code> being rendered by this list item.
     * 
     * @return returns an instance of <code>CarChoice</code> being rendered by this list item.
     */
    public CarChoice getCarChoice() {
        return carChoice;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {

        View carView = null;

        LayoutInflater inflater = null;

        if (convertView == null) {
            inflater = LayoutInflater.from(context);
            carView = inflater.inflate(R.layout.car_search_results_row, null);
        } else {
            carView = convertView;
        }

        // Create the main row container and static elements
        String classBody = new StringBuilder(carDescription.carClass).append(' ').append(carDescription.carBody)
                .toString();

        if (carChain.imageUri != null) {
            ImageView logoImg = (ImageView) carView.findViewById(R.id.carResultVendorImage);
            if (logoImg != null) {
                // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                // to refresh the appropriate view items once images have been loaded.
                listItemTag = carChain.imageUri;
                // Attempt to load the image from the image cache, if not there, then the
                // ImageCache will load it asynchronously and this view will be updated via
                // the ImageCache broadcast receiver available in BaseActivity.
                ImageCache imgCache = ImageCache.getInstance(context);
                Bitmap bitmap = imgCache.getBitmap(carChain.imageUri, null);
                if (bitmap != null) {
                    logoImg.setImageBitmap(bitmap);
                    // MOB-17497 : adding vendor name in tag name.
                    logoImg.setTag(carChain.chainName);
                    logoImg.setVisibility(View.VISIBLE);
                } else {
                    // Since the bitmap isn't available at the moment, set the visibility to 'INVISIBLE' so that
                    // the client is not showing a previously loaded image for a different carrier!
                    logoImg.setVisibility(View.INVISIBLE);
                }

            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate logo image view!");
            }
        } else {
            ViewUtil.setVisibility(carView, R.id.carResultVendorImage, View.GONE);
        }

        // Set the color for the daily rate based on any possible violations.
        TextView txtView = (TextView) carView.findViewById(R.id.carResultDailyRate);
        ImageView violationIconView = (ImageView) carView.findViewById(R.id.violation_icon);
        switch (ViewUtil.getRuleEnforcementLevel(carChoice.maxEnforcementLevel)) {
        case NONE: {
            txtView.setTextAppearance(context, R.style.FareNormal);
            violationIconView.setVisibility(View.GONE);
            break;
        }
        case WARNING: {
            txtView.setTextAppearance(context, R.style.FareWarning);
            violationIconView.setImageResource(R.drawable.icon_yellowex);
            violationIconView.setVisibility(View.VISIBLE);
            break;
        }
        case ERROR: {
            txtView.setTextAppearance(context, R.style.FareError);
            violationIconView.setImageResource(R.drawable.icon_redex);
            violationIconView.setVisibility(View.VISIBLE);
            break;
        }
        case INACTIVE: {
            txtView.setTextAppearance(context, R.style.FareInactive);
            violationIconView.setVisibility(View.GONE);
            break;
        }
        case HIDE: {
            // No-op.
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: rule enforcement level of hide!");
            break;
        }
        }
        txtView.setText(FormatUtil.formatAmount(carChoice.dailyRate, context.getResources().getConfiguration().locale,
                carChoice.currency, true, true));
        ((TextView) carView.findViewById(R.id.carResultClassBody)).setText(classBody);
        ((TextView) carView.findViewById(R.id.carResultTransmission)).setText(carDescription.carTrans);
        ((TextView) carView.findViewById(R.id.carResultFuelAC)).setText(carDescription.carFuel);

        if (carChoice.freeMiles != null && carChoice.freeMiles.trim().length() > 0) {
            if ("UNL".equals(carChoice.freeMiles)) {
                ((TextView) carView.findViewById(R.id.carResultMiles)).setText(context
                        .getText(R.string.car_results_unlimited_miles));
                ViewUtil.setVisibility(carView, R.id.carResultMiles, View.VISIBLE);
            } else {
                ((TextView) carView.findViewById(R.id.carResultMiles)).setText(Format.localizeText(context,
                        R.string.car_results_free_miles, carChoice.freeMiles));
                ViewUtil.setVisibility(carView, R.id.carResultMiles, View.VISIBLE);
            }
        } else {
            ViewUtil.setVisibility(carView, R.id.carResultMiles, View.GONE);
        }
        if (carChoice.gdsName != null) {
            TextView gdsNameView = (TextView) carView.findViewById(R.id.carGDSName);
            ViewUtil.showGDSName(context, gdsNameView, carChoice.gdsName);
        }

        TextView total = (TextView) carView.findViewById(R.id.carResultTotalRate);
        TextView totalLabel = (TextView) carView.findViewById(R.id.carResultTotalRateLabel);
        if (carChoice.totalRate > 0.0) {
            total.setVisibility(View.VISIBLE);
            totalLabel.setVisibility(View.VISIBLE);
            String formattedAmtStr = FormatUtil.formatAmount(carChoice.totalRate, context.getResources()
                    .getConfiguration().locale, carChoice.currency, true, true);
            total.setText(formattedAmtStr);
        } else {
            total.setVisibility(View.GONE);
            totalLabel.setVisibility(View.GONE);
        }

        return carView;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.core.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
