/**
 * 
 */
package com.concur.mobile.core.expense.receiptstore.activity;

import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.TextView;

import com.concur.core.R;
import com.concur.mobile.core.expense.receiptstore.data.ReceiptInfo;
import com.concur.mobile.core.util.Const;
import com.concur.mobile.core.util.FormatUtil;
import com.concur.mobile.core.view.AsyncImageView;
import com.concur.mobile.core.view.ListItem;
import com.concur.mobile.platform.util.Format;

/**
 * An extension of <code>ListItem</code> for rendering <code>ReceiptInfo</code> objects.
 */
public class OfflineReceiptListItem extends ListItem {

    private static final String CLS_TAG = OfflineReceiptListItem.class.getSimpleName();

    protected ReceiptInfo receiptInfo;

    protected HashMap<ReceiptInfo, CompoundButton> receiptButtonMap;

    protected HashSet<ReceiptInfo> checkedReceipts;

    protected OnCheckedChangeListener checkChangeListener;

    /**
     * Constructs an instance of <code>ReceiptInfoListItem</code> given a <code>ReceiptInfo</code> object.
     * 
     * @param receiptInfo
     *            the receipt info object.
     */
    public OfflineReceiptListItem(ReceiptInfo receiptInfo, HashMap<ReceiptInfo, CompoundButton> receiptButtonMap,
            HashSet<ReceiptInfo> checkedReceipts, OnCheckedChangeListener checkChangeListener, int listItemViewType) {
        this.receiptInfo = receiptInfo;
        this.receiptButtonMap = receiptButtonMap;
        this.checkedReceipts = checkedReceipts;
        this.checkChangeListener = checkChangeListener;
        this.listItemViewType = listItemViewType;
    }

    @Override
    public Calendar getCalendar() {
        if (receiptInfo != null) {
            return receiptInfo.getUpdateTime();
        } else {
            return null;
        }
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
        // Note: Due to asynchronously loading the thumbnail images via the AsyncImageView class, the client
        // does not re-use views as that would require constructing a new instance of AsyncImageView, setting the URL
        // and replacing any previous inflated instance.
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.offline_receipt_row, null);

        // Set the image upload date.
        TextView txtView = (TextView) view.findViewById(R.id.receipt_image_date);
        if (txtView != null) {
            txtView.setText(Format.safeFormatCalendar(FormatUtil.SHORT_MONTH_DAY_SHORT_YEAR_SHORT_TIME_DISPLAY,
                    receiptInfo.getImageCalendar()));
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt image date text view!");
        }

        // Populate the thumnail view if a thumbnail exists.
        if (receiptInfo.getThumbnail() != null) {
            txtView = (TextView) view.findViewById(R.id.receipt_no_thumbnail_message);
            if (txtView != null) {
                txtView.setVisibility(View.GONE);
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt no thumbnail message text view!");
            }

            AsyncImageView asyncImgView = (AsyncImageView) view.findViewById(R.id.receipt_thumbnail);
            if (asyncImgView != null) {
                asyncImgView.setVisibility(View.VISIBLE);
                asyncImgView.setImageBitmap(receiptInfo.getThumbnail());
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".getView: unable to locate receipt thumbnail async image view!");
            }
        }

        // Add a listener to check for at least one selected item.
        CheckBox ckBox = (CheckBox) view.findViewById(R.id.receipt_check);
        if (ckBox != null) {
            // Disable the checkbox if not wanted
            if (receiptButtonMap == null) {
                ckBox.setVisibility(View.GONE);
            } else {
                // Add to the button/expense mapping.
                receiptButtonMap.put(receiptInfo, ckBox);
                // Set the on check change listener.
                ckBox.setChecked(checkedReceipts.contains(receiptInfo));
                ckBox.setOnCheckedChangeListener(checkChangeListener);
            }
        } else {
            Log.e(Const.LOG_TAG, CLS_TAG + ".buildView: unable to locate checkbox!");
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
