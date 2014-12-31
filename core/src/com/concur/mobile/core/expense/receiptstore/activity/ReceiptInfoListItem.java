/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.activity;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptStoreCache;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.util.ImageCache;
import com.concur.mobile.core.util.ViewUtil;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering <code>ReceiptInfo</code> objects.
 */
public class ReceiptInfoListItem extends ListItem {

    private static final String CLS_TAG = ReceiptInfoListItem.class.getSimpleName();

    protected ReceiptInfo receiptInfo;

    protected ReceiptStoreCache rsCache;

    /**
     * Constructs an instance of <code>ReceiptInfoListItem</code> given a <code>ReceiptInfo</code> object.
     * 
     * @param receiptInfo
     *            the receipt info object.
     */
    public ReceiptInfoListItem(ReceiptInfo receiptInfo, int listItemViewType, ReceiptStoreCache rsCache) {
        this.receiptInfo = receiptInfo;
        this.listItemViewType = listItemViewType;
        this.rsCache = rsCache;
    }

    /**
     * Gets the instance of <code>ReceiptInfo</code> backing this object.
     * 
     * @return the receipt info object backing this object.
     */
    public ReceiptInfo getReceiptInfo() {
        return receiptInfo;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#buildView(android.content.Context, android.view.View, android.view.ViewGroup)
     */
    @Override
    public View buildView(Context context, View convertView, ViewGroup parent) {
        View view = null;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            view = inflater.inflate(R.layout.expense_receipt_store_row, null);
        } else {
            view = convertView;
        }

        // Set the image upload date.
        TextView txtView = (TextView) view.findViewById(R.id.receipt_image_date);
        if (txtView != null) {
            txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                    receiptInfo.getImageCalendar()));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt image date text view!");
        }
        // Set the image source.
        txtView = (TextView) view.findViewById(R.id.receipt_image_source);
        if (txtView != null) {
            String receiptOrigin = receiptInfo.getImageOrigin().toLowerCase();
            if (receiptOrigin.indexOf("mobile") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_mobile));
            } else if (receiptOrigin.indexOf("fax") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_fax));
            } else if (receiptOrigin.indexOf("ereceipt") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_fax));
            } else if (receiptOrigin.indexOf("email") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_email));
            } else if (receiptOrigin.indexOf("card") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_card));
            } else if (receiptOrigin.indexOf("imaging_ws") != -1) {
                txtView.setText(context.getText(R.string.receipt_source_web_services));
            } else {
                // Use the non-localized value.
                txtView.setText(receiptInfo.getImageOrigin());
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt image source text view!");
        }
        // Set the image file type.
        txtView = (TextView) view.findViewById(R.id.receipt_file_type);
        if (txtView != null) {
            if (receiptInfo.getFileType().equalsIgnoreCase("JPG") || receiptInfo.getFileType().equalsIgnoreCase("PNG")) {
                txtView.setText(context.getText(R.string.receipt_document_type_image));
            } else if (receiptInfo.getFileType().equalsIgnoreCase("PDF")) {
                txtView.setText(context.getText(R.string.receipt_document_type_pdf));
            } else {
                // Use non-localized value.
                txtView.setText(receiptInfo.getFileType());
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt file type text view!");
        }

        // Populate the thumnail view if a thumbnail URL exists.
        if (receiptInfo.getThumbUrl() != null && receiptInfo.getThumbUrl().length() > 0) {
            try {
                URI thumbUri = new URL(receiptInfo.getThumbUrl()).toURI();
                txtView = (TextView) view.findViewById(R.id.receipt_no_thumbnail_message);
                if (txtView != null) {
                    txtView.setVisibility(View.GONE);
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt no thumbnail message text view!");
                }

                ImageView imgView = (ImageView) view.findViewById(R.id.receipt_thumbnail);
                if (imgView != null) {
                    // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
                    // to refresh the appropriate view items once images have been loaded.
                    listItemTag = thumbUri;
                    // Attempt to load the image from the image cache, if not there, then the
                    // ImageCache will load it asynchronously and this view will be updated via
                    // the ImageCache broadcast receiver available in BaseActivity.
                    ImageCache imgCache = ImageCache.getInstance(context);
                    Bitmap bitmap = imgCache.getBitmap(thumbUri, null);
                    if (bitmap != null) {
                        imgView.setImageBitmap(bitmap);
                        imgView.setVisibility(View.VISIBLE);
                    } else {
                        // Since the bitmap isn't available at the moment, set the visibility to 'INVISIBLE' so that
                        // the client is not showing a previously loaded image for a different receipt!!
                        imgView.setVisibility(View.INVISIBLE);
                    }
                } else {
                    Log.e(Const.LOG_TAG, CLS_TAG + ".getView: can't locate image view!");
                }
            } catch (MalformedURLException mlfUrlExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getView: malformed receipt thumbnail URL '" + receiptInfo.getThumbUrl() + "'",
                        mlfUrlExc);
            } catch (URISyntaxException uriSynExc) {
                Log.e(Const.LOG_TAG,
                        CLS_TAG + ".getView: URI syntax exception for thumbnail URL '" + receiptInfo.getThumbUrl()
                                + "'", uriSynExc);
            }
        } else {
            // No thumbnail available, hide the image view and make the "no thumbnail" visible.
            ViewUtil.setVisibility(view, R.id.receipt_thumbnail, View.GONE);
            ViewUtil.setVisibility(view, R.id.receipt_no_thumbnail_message, View.VISIBLE);
        }
        return view;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.concur.mobile.util.ListItem#isEnabled()
     */
    @Override
    public boolean isEnabled() {
        return true;
    }

}
