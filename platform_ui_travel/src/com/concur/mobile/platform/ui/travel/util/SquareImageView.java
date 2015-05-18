package com.concur.mobile.platform.ui.travel.util;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * view util to stretch all images to same size
 *
 * @author tejoa
 */
public class SquareImageView extends ImageView {

    public SquareImageView(Context context) {
        super(context);
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), (int) (getMeasuredWidth() * 0.6));
    }

    // public void previewPhoto(View view) {
    // ((ImageView) view).setScaleType(ScaleType.FIT_XY);
    // Intent intent = new Intent(view.getContext(), ImageActivity.class);
    // intent.putExtra(Const.EXTRA_IMAGE_URL, hotelImage.image);
    // view.getContext().startActivity(intent);
    //
    // // return view;
    // }
}