package com.concur.mobile.platform.ui.travel.hotel.fragment;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.*;
import com.concur.mobile.platform.travel.search.hotel.HotelImagePair;
import com.concur.mobile.platform.ui.common.fragment.PlatformFragmentV1;
import com.concur.mobile.platform.ui.travel.BuildConfig;
import com.concur.mobile.platform.ui.travel.R;
import com.concur.mobile.platform.ui.travel.hotel.fragment.HotelChoiceDetailsFragment.HotelChoiceDetailsFragmentListener;
import com.concur.mobile.platform.ui.travel.util.*;
import com.concur.mobile.platform.ui.travel.view.CustomGridView;

import java.net.URI;
import java.util.List;

public class HotelImagesFragment extends PlatformFragmentV1 implements AdapterView.OnItemClickListener {

    private CustomGridView mGridView;
    private List<HotelImagePair> images;
    private HotelChoiceDetailsFragmentListener callBackListener;
    private ImageAdapter imgAdapter;
    private ImageFetcher mImageFetcher;
    private int mImageThumbSize;
    private int mImageThumbSpacing;
    private static final String IMAGE_CACHE_DIR = "thumbs";
    private static final String TAG = "HotelImagesFragment";

    public HotelImagesFragment() {
        super();
    }

    public HotelImagesFragment(List<HotelImagePair> images) {
        this.images = images;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

        imgAdapter = new ImageAdapter(getActivity());

        TravelImageCache.ImageCacheParams cacheParams = new TravelImageCache.ImageCacheParams(getActivity(),
                IMAGE_CACHE_DIR);

        cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

        // The ImageFetcher takes care of loading images into our ImageView children asynchronously
        mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);

        mImageFetcher.setLoadingImage(R.drawable.hotel_results_default_image);
        mImageFetcher.addImageCache(getActivity().getFragmentManager(), cacheParams);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.hotel_images_layout, container, false);

        mGridView = (CustomGridView) view.findViewById(R.id.gridview);
        mGridView.setAdapter(imgAdapter);

        mGridView.setOnItemClickListener(this);
        if (images == null || images.size() == 0) {
            TextView tv = (TextView) view.findViewById(R.id.no_photos);
            mGridView.setVisibility(View.GONE);
            tv.setVisibility(View.VISIBLE);
        }
        mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!ViewUtil.hasHoneycomb()) {
                        mImageFetcher.setPauseWork(true);
                    }
                } else {
                    mImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount,
                    int totalItemCount) {
            }
        });

        // This listener is used to get the final width of the GridView and then calculate the
        // number of columns and the width of each column. The width of each column is variable
        // as the GridView has stretchMode=columnWidth. The column width is used to set the height
        // of each view so we get nice square thumbnails.
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN) @Override
            public void onGlobalLayout() {
                if (imgAdapter.getNumColumns() == 0) {
                    final int numColumns = 2;
                    //(int) Math
                    //      .floor(mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (mGridView.getWidth() / numColumns) - mImageThumbSpacing;
                        imgAdapter.setNumColumns(numColumns);
                        imgAdapter.setItemHeight((int) (columnWidth * 0.6));
                        if (BuildConfig.DEBUG) {
                            Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
                        }
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            mGridView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        } else {
                            mGridView.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        }
                    }
                }
            }
        });

        setRetainInstance(true);
        return view;

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            callBackListener = (HotelChoiceDetailsFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HotelSearchResultsFragmentListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // TODO Auto-generated method stub
        super.onSaveInstanceState(outState);

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onSaveInstanceState *****  ");
    }

    @Override
    public void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        mImageFetcher.setPauseWork(false);
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();

        Log.d(Const.LOG_TAG, " ***** HotelChoiceDetailsFragment, in onPause *****  ");

    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        mImageFetcher.setExitTasksEarly(false);
        imgAdapter.notifyDataSetChanged();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
        //final Intent i = new Intent(getActivity(), ImageDetailActivity.class);
        // i.putExtra(ImageDetailActivity.EXTRA_IMAGE, (int) id);
        callBackListener.onImageClicked(v, (int) position);
    }

    /**
     * @return an adapter for the grid view
     */
    public BaseAdapter getAdapter(Context ctx) {
        return new ImageAdapter(ctx);
    }

    static class ViewHolder {

        // TextView imageTitle;
        ImageView image;
    }

    private class ImageAdapter extends BaseAdapter {

        private Context mContext;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private GridView.LayoutParams mImageViewLayoutParams;

        public ImageAdapter(Context c) {
            mContext = c;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT);
            // mItems = items;
        }

        public int getCount() {
            return images != null ? images.size() : 0;
        }

        public Object getItem(int position) {
            return images.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position < mNumColumns ? 0 : position - mNumColumns;
        }

        @Override
        public int getViewTypeCount() {
            // Two types of views, the normal ImageView and the top row of empty views
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return (position < mNumColumns) ? 1 : 0;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        //        // create a new ImageView for each item referenced by the Adapter
        //        public View getView(int position, View convertView, ViewGroup parent) {
        //
        //            View cell = convertView;
        //            ViewHolder holder = null;
        //
        //            if (cell == null) {
        //                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
        //                cell = inflater.inflate(R.layout.image_view, parent, false);
        //                holder = new ViewHolder();
        //                holder.image = (ImageView) cell.findViewById(R.id.picture);
        //                cell.setTag(holder);
        //            } else {
        //                holder = (ViewHolder) cell.getTag();
        //            }
        //            if (images != null && images.size() > 0) {
        //                // Set the list item tag to the uri, this tag value is used in 'ListItemAdapter.refreshView'
        //                // to refresh the appropriate view items once images have been loaded.
        //                URI uri = URI.create(images.get(position).image);
        //                // Attempt to load the image from the image cache, if not there, then the
        //                // ImageCache will load it asynchronously and this view will be updated via
        //                // the ImageCache broadcast receiver available in BaseActivity.
        //                ImageCache imgCache = ImageCache.getInstance(mContext);
        //                Bitmap bitmap = imgCache.getBitmap(uri, null);
        //                if (bitmap != null) {
        //                    holder.image.setScaleType(ImageView.ScaleType.FIT_XY);
        //                    // holder.image.setBackgroundColor(getResources().getColor(R.color.white_background));
        //                    holder.image.setImageBitmap(bitmap);
        //                } else {
        //                    holder.image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //                    holder.image.setBackgroundColor(getResources().getColor(R.color.grey_view_background));
        //                    holder.image.setImageResource(R.drawable.hotel_results_default_image);
        //                }
        //
        //            }
        //            return cell;
        //        }

        @Override
        public View getView(int position, View convertView, ViewGroup container) {
            //BEGIN_INCLUDE(load_gridview_item)
            // First check if this is the top row
            //            if (position < mNumColumns) {
            //                if (convertView == null) {
            //                    convertView = new View(mContext);
            //                }
            //                // Set empty view with height of ActionBar
            //                convertView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
            //                return convertView;
            //            }

            // Now handle the main ImageView thumbnails
            ImageView imageView;
            if (convertView == null) { // if it's not recycled, instantiate and initialize
                imageView = new RecyclingImageView(mContext);
                // imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                imageView.setBackgroundColor(getResources().getColor(R.color.grey_view_background));
                imageView.setLayoutParams(mImageViewLayoutParams);
            } else { // Otherwise re-use the converted view
                imageView = (ImageView) convertView;
            }

            // Check the height matches our calculated column width
            if (imageView.getLayoutParams().height != mItemHeight) {
                imageView.setLayoutParams(mImageViewLayoutParams);
            }

            // Finally load the image asynchronously into the ImageView, this also takes care of
            // setting a placeholder image while the background thread runs
            URI uri = URI.create(images.get(position).image);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            mImageFetcher.loadImage(uri, imageView);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            return imageView;
            //END_INCLUDE(load_gridview_item)

        }

        /**
         * Sets the item height. Useful for when we know the column width so the height can be set
         * to match.
         *
         * @param height
         */
        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mItemHeight);
            mImageFetcher.setImageSize(height);
            notifyDataSetChanged();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }
    }
}
