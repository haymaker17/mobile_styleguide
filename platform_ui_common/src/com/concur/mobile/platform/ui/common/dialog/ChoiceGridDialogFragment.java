package com.concur.mobile.platform.ui.common.dialog;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.concur.mobile.platform.ui.common.R;

/**
 * A dialog to display rows of action buttons with image and text
 * 
 * @author yiwenw
 * 
 */
public class ChoiceGridDialogFragment extends DialogFragment implements OnItemClickListener {

    private GridView mGridView;
    private ChoiceItem[] mChoiceItems;
    private Integer mTitle;

    public ChoiceGridDialogFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        getDialog().setTitle(mTitle);
        View view = inflater.inflate(R.layout.grid_dialog, container);

        mGridView = (GridView) view.findViewById(R.id.grid);

        // initialize your gridview
        mGridView.setAdapter(getAdapter(getActivity()));
        mGridView.setOnItemClickListener(this);

        return view;

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
    }

    public void setItems(ChoiceItem[] items) {
        mChoiceItems = items;
    }

    public void setTitle(Integer title) {
        mTitle = title;
    }

    /**
     * 
     * @return an adapter for the grid view
     */
    public BaseAdapter getAdapter(Context ctx) {
        return new ImageAdapter(ctx, mChoiceItems);
    }

    static public class ChoiceItem {

        private Integer mImageResId;
        private Integer mTitle;

        public ChoiceItem(Integer resId, Integer title) {
            mImageResId = resId;
            mTitle = title;
        }

        public Integer getTitle() {
            return mTitle;
        }

        public Integer getImageResId() {
            return mImageResId;
        }
    }

    static class ViewHolder {

        TextView imageTitle;
        ImageView image;
    }

    static public class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private ChoiceItem[] mItems;

        public ImageAdapter(Context c, ChoiceItem[] items) {
            mContext = c;
            mItems = items;
        }

        public int getCount() {
            return mItems.length;
        }

        public Object getItem(int position) {
            return mItems[position];
        }

        public long getItemId(int position) {
            return mItems[position].mImageResId;
        }

        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            /*
             * ImageView imageView; if (convertView == null) { // if it's not recycled, initialize some attributes imageView = new
             * ImageView(mContext); imageView.setLayoutParams(new GridView.LayoutParams(85, 85));
             * imageView.setScaleType(ImageView.ScaleType.CENTER_CROP); imageView.setPadding(8, 8, 8, 8); } else { imageView =
             * (ImageView) convertView; } imageView.setImageResource(mItems[position]); return imageView;
             */

            View cell = convertView;
            ViewHolder holder = null;

            if (cell == null) {
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                cell = inflater.inflate(R.layout.grid_dialog_cell, parent, false);
                holder = new ViewHolder();
                holder.imageTitle = (TextView) cell.findViewById(R.id.text);
                holder.image = (ImageView) cell.findViewById(R.id.image);
                cell.setTag(holder);
            } else {
                holder = (ViewHolder) cell.getTag();
            }

            ChoiceItem item = mItems[position];
            holder.imageTitle.setText(item.getTitle());
            holder.image.setImageResource(item.getImageResId());
            return cell;
        }
    }
}
