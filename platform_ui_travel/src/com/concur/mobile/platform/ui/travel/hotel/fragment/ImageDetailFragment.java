package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.util.ImageFetcher;
import com.concur.mobile.platform.ui.travel.util.ImageWorker;
import com.concur.mobile.platform.ui.travel.util.ViewUtil;

/**
 * This fragment will populate the children of the ViewPager from {@link }.
 */
public class ImageDetailFragment extends Fragment {

    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private ImageView mImageView;
    private ImageFetcher mImageFetcher;
    private ImagesFragmentListener callBackListener;
    private View view;

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
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
        setRetainInstance(true);
    }

    /**
     * Populate image using a url from extras, use the convenience factory method {@link ImageDetailFragment #newInstance(String)}
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
        view = inflater.inflate(R.layout.image_detail_fragment, container, false);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        setActionBar();
        return view;
    }


    private void setActionBar() {
        ActionBar actionBar = getActivity().getActionBar();
        //no title currently
        actionBar.setTitle(R.string.hotel_tab_images);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (ImagesFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFragmentListener");
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        mImageFetcher = callBackListener.getImageFetcher();
        if (mImageFetcher != null) {
            //view.findViewById(R.id.image_progress).setVisibility(View.VISIBLE);
            mImageFetcher.loadImage(mImageUrl, mImageView);
        }

        // Pass clicks on the ImageView to the parent activity to handle
        if (View.OnClickListener.class.isInstance(getActivity()) && ViewUtil.hasHoneycomb()) {
            mImageView.setOnClickListener((View.OnClickListener) getActivity());
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
        if (mImageFetcher != null) {
            mImageFetcher = null;
        }
    }

    // Container Activity must implement this call back interface
    public interface ImagesFragmentListener {

        public ImageFetcher getImageFetcher();

    }
}
