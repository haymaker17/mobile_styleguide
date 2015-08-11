package com.concur.mobile.platform.ui.common.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.ListView;
import com.concur.mobile.platform.ui.common.view.ListItem;
import com.concur.mobile.platform.ui.common.view.ListItemAdapter;

import java.net.URI;

/**
 * An extension of <code>BroadcastReceiver</code> for the purposes of handling a notification that an image has been downloaded.
 */
public class ImageCacheReceiver<T extends ListItem> extends BroadcastReceiver {

    private static final String CLS_TAG = ImageCacheReceiver.class.getSimpleName();

    private ListItemAdapter<T> listItemAdapter;

    private ListView listView;

    /**
     * Constructs an instance of <code>ImageCacheReceiver</code> with an list adapter and view.
     *
     * @param listItemAdapter contains the list item adapter.
     * @param listView        contains the list view.
     */
    public ImageCacheReceiver(ListItemAdapter<T> listItemAdapter, ListView listView) {
        this.listItemAdapter = listItemAdapter;
        this.listView = listView;
    }

    /**
     * Will reset the list adapter and view.
     *
     * @param listItemAdapter contains the list item adapter.
     * @param listView        contains the list view.
     */
    protected void setListAdapter(ListItemAdapter<T> listItemAdapter, ListView listView) {
        this.listItemAdapter = listItemAdapter;
        this.listView = listView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equalsIgnoreCase(ImageCache.IMAGE_DOWNLOAD_ACTION)) {
            boolean result = intent.getBooleanExtra(ImageCache.EXTRA_IMAGE_DOWNLOAD_RESULT, false);
            if (result) {
                URI uri = (URI) intent.getSerializableExtra(ImageCache.EXTRA_IMAGE_DOWNLOAD_URI);
                if (listItemAdapter != null) {
                    listItemAdapter.refreshView(listView, uri);
                    //
                    listView.invalidateViews();
                    listView.refreshDrawableState();
                }
            } else {
                Log.e(Const.LOG_TAG, CLS_TAG + ".onReceive: listItemAdapter is null!");
            }
        }
    }

}