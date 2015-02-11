package com.concur.mobile.platform.ui.travel.hotel.fragment;

import java.net.URI;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.concur.mobile.platform.ui.common.util.ImageCache;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.activity.ImageDetailActivity;

/**
 * This fragment will populate the children of the ViewPager from {@link ImageDetailActivity}.
 */
public class ImageDetailFragment extends Fragment {

    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ImageView mImageView;

    // private ImageFetcher mImageFetcher;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     * 
     * @param imageUrl
     *            The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageDetailFragment newInstance(String imageUrl) {
        final ImageDetailFragment f = new ImageDetailFragment();

        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);

        return f;
    }

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageDetailFragment() {
    }

    /**
     * Populate image using a url from extras, use the convenience factory method {@link ImageDetailFragment#newInstance(String)}
     * to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate and locate the main ImageView
        final View v = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        URI uri = URI.create(mImageUrl);
        // Attempt to load the image from the image cache, if not there, then the
        // ImageCache will load it asynchronously and this view will be updated via
        // the ImageCache broadcast receiver available in BaseActivity.
        ImageCache imgCache = ImageCache.getInstance(getActivity());
        Bitmap bitmap = imgCache.getBitmap(uri, null);
        if (bitmap != null) {
            mImageView.setImageBitmap(bitmap);
        }
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            mImageView.setImageDrawable(null);
        }
    }
}
